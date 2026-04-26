package com.aiforge.biz.service;

import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.vo.BizArticleVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BizArticleService extends IService<BizArticle> {

    /**
     * 查看文章详情
     */
    BizArticleVO getByArticleId(Long articleId);
}

