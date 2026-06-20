package com.aiforge.biz.service.impl;

import com.aiforge.biz.convert.ArticleConvert;

import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.enums.ArticleStatusEnum;
import com.aiforge.common.enums.BizTypeEnum;
import com.aiforge.biz.mapper.BizArticleMapper;
import com.aiforge.biz.service.BizArticleService;
import com.aiforge.biz.vo.BizArticleVO;
import com.aiforge.biz.vo.HomeArticleVO;
import com.aiforge.biz.vo.PersonalCenterArticleVO;
import com.aiforge.common.annotation.OperationLog;
import com.aiforge.common.enums.OperBusinessTypeIntEnum;
import com.aiforge.common.enums.OperBusinessTypeStringEnum;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.aiforge.common.utils.SecurityUtils;
import com.aiforge.system.entity.SysUser;
import com.aiforge.system.mapper.SysUserMapper;
import com.aiforge.common.facade.AgentFacade;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.aiforge.common.message.ArticleMessage;
import com.aiforge.common.constant.MqConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizArticleServiceImpl extends ServiceImpl<BizArticleMapper, BizArticle> implements BizArticleService {

    private final ArticleConvert articleConvert;
    private final SysUserMapper sysUserMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<AgentFacade> agentFacadeProvider;

    /**
     * 查看文章详情
     */
    @Override
    public BizArticleVO getByArticleId(Long articleId) {
        if (articleId == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章ID不能为空");
        }

        BizArticle article = this.getById(articleId);
        BizArticleVO vo = articleConvert.toVO(article);

        if (article.getAuthorId() != null) {
            SysUser author = sysUserMapper.selectById(article.getAuthorId());
            if (author != null) {
                vo.setAuthorName(author.getNickName());
            }
        }

        return vo;
    }

    /**
     * 新增文章（支持发布/存草稿）
     */
    @Override
    @OperationLog(module = "文章管理", businessType = OperBusinessTypeIntEnum.INSERT)
    public int saveArticle(BizArticleDTO articleDTO) {
        if (articleDTO == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章DTO不能为空");
        }
        if (articleDTO.getPublishStatus() == null || !ArticleStatusEnum.existsByCode(articleDTO.getPublishStatus())) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "发布状态参数异常");
        }

        BizArticle article = articleConvert.toEntity(articleDTO);
        article.setAuthorId(SecurityUtils.getUserId());
        article.setArticleStatus(articleDTO.getPublishStatus());
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(articleDTO.getPublishStatus())) {
            article.setPublishTime(LocalDateTime.now());
        }

        this.saveOrUpdate(article);

        // 发送文章发布消息到 MQ (广播交换机)，供 ES 和向量库消费
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(article.getArticleStatus())) {
            try {
                ArticleMessage message = ArticleMessage.builder()
                        .articleId(article.getArticleId())
                        .title(article.getArticleTitle())
                        .content(article.getContent())
                        .action(OperBusinessTypeStringEnum.INSERT.getCode())
                        .build();
                rabbitTemplate.convertAndSend(MqConstants.ARTICLE_EXCHANGE, "", message);
            } catch (Exception e) {
                log.error("发送文章发布消息到 MQ 失败, articleId: {}", article.getArticleId(), e);
            }
        }

        return 1;
    }

    /**
     * 首页最新文章
     */
    @Override
    public List<HomeArticleVO> getLatestPublished(int limit) {
        LambdaQueryWrapper<BizArticle> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BizArticle::getArticleStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .orderByDesc(BizArticle::getPublishTime)
                .last("LIMIT " + limit);

        List<BizArticle> articles = this.list(lqw);
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> authorIds = articles.stream()
                .map(BizArticle::getAuthorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = authorIds.isEmpty() ? Collections.emptyMap()
                : sysUserMapper.selectBatchIds(authorIds).stream()
                        .collect(Collectors.toMap(SysUser::getUserId, user -> user));

        return articles.stream().map(article -> {
            HomeArticleVO vo = articleConvert.toHomeVO(article);

            if (article.getAuthorId() != null && userMap.containsKey(article.getAuthorId())) {
                vo.setAuthorName(userMap.get(article.getAuthorId()).getNickName());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 修改文章
     */
    @Override
    @OperationLog(module = "文章管理", businessType = OperBusinessTypeIntEnum.UPDATE)
    public boolean updateArticle(BizArticle article) {
        if (article == null || article.getArticleId() == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "修改的文章ID不能为空");
        }

        BizArticle oldArticle = this.getById(article.getArticleId());
        boolean result = this.updateById(article);

        // 当更新后的状态是发布时，触发向量更新（如果原来也是发布则更新；如果原来不是则相当于发布）。如果变为草稿，应该删除。
        // 但前端传来的 article 可能不包含所有的字段（如 content）。为确保完整，我们重新查一次。
        BizArticle newArticle = this.getById(article.getArticleId());

        if (ArticleStatusEnum.PUBLISHED.getCode().equals(newArticle.getArticleStatus())) {
            // 如果新状态是已发布，则发送更新消息
            try {
                ArticleMessage message = ArticleMessage.builder()
                        .articleId(newArticle.getArticleId())
                        .title(newArticle.getArticleTitle())
                        .content(newArticle.getContent())
                        .action(OperBusinessTypeStringEnum.UPDATE.getCode())
                        .build();
                rabbitTemplate.convertAndSend(MqConstants.ARTICLE_EXCHANGE, "", message);
            } catch (Exception e) {
                log.error("发送文章更新消息到 MQ 失败, articleId: {}", newArticle.getArticleId(), e);
            }
        } else if ((ArticleStatusEnum.DRAFT.getCode().equals(newArticle.getArticleStatus()) ||
                ArticleStatusEnum.OFFLINE.getCode().equals(newArticle.getArticleStatus()) ||
                ArticleStatusEnum.PRIVATE.getCode().equals(newArticle.getArticleStatus())) &&
                ArticleStatusEnum.PUBLISHED.getCode().equals(oldArticle.getArticleStatus())) {
            // 如果由已发布变为了草稿、下架或私密，发送删除消息
            try {
                ArticleMessage message = ArticleMessage.builder()
                        .articleId(newArticle.getArticleId())
                        .action(OperBusinessTypeStringEnum.DELETE.getCode())
                        .build();
                rabbitTemplate.convertAndSend(MqConstants.ARTICLE_EXCHANGE, "", message);
            } catch (Exception e) {
                log.error("发送文章删除消息到 MQ 失败, articleId: {}", newArticle.getArticleId(), e);
            }
        }

        return result;
    }

    /**
     * 个人中心文章列表（当前登录用户）
     */
    @Override
    public IPage<PersonalCenterArticleVO> getPersonalCenterArticles(Page<PersonalCenterArticleVO> page) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new AiForgeException(ResultCodeEnum.UNAUTHORIZED);
        }

        LambdaQueryWrapper<BizArticle> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BizArticle::getAuthorId, userId)
                .orderByDesc(BizArticle::getPublishTime)
                .orderByDesc(BizArticle::getArticleId);

        Page<BizArticle> articlePage = this.page(new Page<>(page.getCurrent(), page.getSize()), lqw);

        Page<PersonalCenterArticleVO> voPage = new Page<>(
                articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(articlePage.getRecords().stream()
                .map(articleConvert::toPersonalCenterVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 批量删除文章
     */
    @Override
    @OperationLog(module = "文章管理", businessType = OperBusinessTypeIntEnum.DELETE)
    public boolean deleteArticles(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return false;
        }

        // 删除前发送文章删除消息到 MQ
        for (Long articleId : articleIds) {
            try {
                ArticleMessage message = ArticleMessage.builder()
                        .articleId(articleId)
                        .action(OperBusinessTypeStringEnum.DELETE.getCode())
                        .build();
                rabbitTemplate.convertAndSend(MqConstants.ARTICLE_EXCHANGE, "", message);
            } catch (Exception e) {
                log.error("发送文章删除消息到 MQ 失败, articleId: {}", articleId, e);
            }
        }

        return this.removeByIds(articleIds);
    }

    /**
     * 生成整篇文章的一键摘要（流式输出）
     */
    @Override
    public Flux<String> generateSummaryStream(Long articleId) {
        if (articleId == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章ID不能为空");
        }
        BizArticle article = this.getById(articleId);
        if (article == null || article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章不存在或内容为空");
        }

        // 容器中有 AgentFacade Bean 则返回，没有则返回 null（可选依赖）
        AgentFacade agentFacade = agentFacadeProvider.getIfAvailable();
        if (agentFacade == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI摘要服务不可用");
        }

        return agentFacade.summarizeArticleStream(article.getContent());
    }

    /**
     * 知识问答
     */
    @Override
    public Flux<String> generateQAStream(Long articleId, String question) {
        if (articleId == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章ID不能为空");
        }
        if (question == null || question.trim().isEmpty()) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "提问不能为空");
        }

        // 验证文章是否存在且状态正常
        BizArticle article = this.getById(articleId);
        if (article == null || !ArticleStatusEnum.PUBLISHED.getCode().equals(article.getArticleStatus())) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章不存在或未发布");
        }

        AgentFacade agentFacade = agentFacadeProvider.getIfAvailable();
        if (agentFacade == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI知识问答服务不可用");
        }

        return agentFacade.answerKnowledgeStream(
                String.valueOf(articleId),
                BizTypeEnum.ARTICLE.getCode(),
                question);
    }

    /**
     * 同步所有已发布文章到 MQ（用于重建 ES 和向量库数据）
     */
    @Override
    public void syncAllArticlesToMq() {
        LambdaQueryWrapper<BizArticle> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BizArticle::getArticleStatus, ArticleStatusEnum.PUBLISHED.getCode());
        List<BizArticle> articles = this.list(lqw);
        
        if (articles == null || articles.isEmpty()) {
            log.info("没有需要同步的已发布文章");
            return;
        }

        int successCount = 0;
        for (BizArticle article : articles) {
            try {
                ArticleMessage message = ArticleMessage.builder()
                        .articleId(article.getArticleId())
                        .title(article.getArticleTitle())
                        .content(article.getContent())
                        .action(OperBusinessTypeStringEnum.INSERT.getCode())
                        .build();
                rabbitTemplate.convertAndSend(MqConstants.ARTICLE_EXCHANGE, "", message);
                successCount++;
            } catch (Exception e) {
                log.error("发送文章同步消息到 MQ 失败, articleId: {}", article.getArticleId(), e);
            }
        }
        log.info("同步文章到 MQ 完成，共 {} 条，成功 {} 条", articles.size(), successCount);
    }
}
