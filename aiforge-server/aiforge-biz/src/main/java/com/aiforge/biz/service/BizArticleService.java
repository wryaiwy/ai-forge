package com.aiforge.biz.service;

import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.vo.BizArticleVO;
import com.aiforge.biz.vo.HomeArticleVO;
import com.aiforge.biz.vo.PersonalCenterArticleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BizArticleService extends IService<BizArticle> {

    /**
     * 查看文章详情
     */
    BizArticleVO getByArticleId(Long articleId);

    /**
     * 获取首页最新已发布文章
     */
    List<HomeArticleVO> getLatestPublished(int limit);

    /**
     * 新增文章
     */
    int saveArticle(BizArticleDTO articleDTO);

    /**
     * 修改文章
     */
    void updateArticle(BizArticle article);

    /**
     * 批量删除文章
     */
    void deleteArticles(List<Long> articleIds);

    /**
     * 个人中心当前用户文章列表
     */
    List<PersonalCenterArticleVO> getPersonalCenterArticles();
}

