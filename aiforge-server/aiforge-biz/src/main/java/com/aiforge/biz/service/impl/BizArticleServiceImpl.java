package com.aiforge.biz.service.impl;

import com.aiforge.biz.convert.ArticleConvert;

import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.enums.ArticleStatusEnum;
import com.aiforge.biz.enums.BizTypeEnum;
import com.aiforge.biz.mapper.BizArticleMapper;
import com.aiforge.biz.service.BizArticleService;
import com.aiforge.biz.vo.BizArticleVO;
import com.aiforge.biz.vo.HomeArticleVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.aiforge.common.utils.SecurityUtils;
import com.aiforge.system.entity.SysUser;
import com.aiforge.system.mapper.SysUserMapper;
import com.aiforge.common.event.KnowledgeAddEvent;
import com.aiforge.common.event.KnowledgeDeleteEvent;
import com.aiforge.common.event.KnowledgeUpdateEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        // 新增知识库
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(article.getArticleStatus())) {
            eventPublisher.publishEvent(new KnowledgeAddEvent(
                    this,
                    String.valueOf(article.getArticleId()),
                    BizTypeEnum.ARTICLE.getCode(),
                    article.getArticleTitle(),
                    article.getContent()));
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
    public void updateArticle(BizArticle article) {
        if (article == null || article.getArticleId() == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "修改的文章ID不能为空");
        }

        BizArticle oldArticle = this.getById(article.getArticleId());
        this.updateById(article);

        // 当更新后的状态是发布时，触发向量更新（如果原来也是发布则更新；如果原来不是则相当于发布）。如果变为草稿，应该删除。
        // 但前端传来的 article 可能不包含所有的字段（如 content）。为确保完整，我们重新查一次。
        BizArticle newArticle = this.getById(article.getArticleId());

        if (ArticleStatusEnum.PUBLISHED.getCode().equals(newArticle.getArticleStatus())) {
            // 如果新状态是已发布，则更新知识库（无论是修改了标题、内容还是重新发布）
            eventPublisher.publishEvent(new KnowledgeUpdateEvent(
                    this,
                    String.valueOf(newArticle.getArticleId()),
                    BizTypeEnum.ARTICLE.getCode(),
                    newArticle.getArticleTitle(),
                    newArticle.getContent()));
        } else if ((ArticleStatusEnum.DRAFT.getCode().equals(newArticle.getArticleStatus()) ||
                ArticleStatusEnum.OFFLINE.getCode().equals(newArticle.getArticleStatus()) ||
                ArticleStatusEnum.PRIVATE.getCode().equals(newArticle.getArticleStatus())) &&
                ArticleStatusEnum.PUBLISHED.getCode().equals(oldArticle.getArticleStatus())) {
            // 如果由已发布变为了草稿、下架或私密，需要从知识库中删除
            eventPublisher.publishEvent(new KnowledgeDeleteEvent(
                    this,
                    String.valueOf(newArticle.getArticleId()),
                    BizTypeEnum.ARTICLE.getCode()));
        }
    }

    /**
     * 批量删除文章
     */
    @Override
    public void deleteArticles(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return;
        }

        // 删除前触发知识库删除事件
        for (Long articleId : articleIds) {
            eventPublisher.publishEvent(new KnowledgeDeleteEvent(
                    this,
                    String.valueOf(articleId),
                    BizTypeEnum.ARTICLE.getCode()));
        }

        this.removeByIds(articleIds);
    }
}
