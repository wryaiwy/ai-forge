package com.aiforge.biz.service.impl;

import com.aiforge.biz.convert.ArticleConvert;
import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.enums.ArticleStatusEnum;
import com.aiforge.biz.mapper.BizArticleMapper;
import com.aiforge.biz.service.BizArticleService;
import com.aiforge.biz.vo.BizArticleVO;
import com.aiforge.biz.vo.HomeArticleVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.aiforge.common.utils.SecurityUtils;
import com.aiforge.system.entity.SysUser;
import com.aiforge.system.mapper.SysUserMapper;
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
            throw new AiForgeException(ResultCodeEnum.PARAM_ERROR.getCode(), "发布状态参数异常");
        }

        BizArticle article = articleConvert.toEntity(articleDTO);
        article.setAuthorId(SecurityUtils.getUserId());
        article.setArticleStatus(articleDTO.getPublishStatus());
        if (ArticleStatusEnum.PUBLISHED.getCode().equals(articleDTO.getPublishStatus())) {
            article.setPublishTime(LocalDateTime.now());
        }

        this.saveOrUpdate(article);
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
        Map<Long, SysUser> userMap = authorIds.isEmpty() ?
                Collections.emptyMap() :
                sysUserMapper.selectBatchIds(authorIds).stream()
                        .collect(Collectors.toMap(SysUser::getUserId, user -> user));


        return articles.stream().map(article -> {
            HomeArticleVO vo = articleConvert.toHomeVO(article);

            if (article.getAuthorId() != null && userMap.containsKey(article.getAuthorId())) {
                vo.setAuthorName(userMap.get(article.getAuthorId()).getNickName());
            }

            return vo;
        }).collect(Collectors.toList());
    }

}
