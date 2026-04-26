package com.aiforge.biz.service.impl;

import com.aiforge.biz.convert.ArticleConvert;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.mapper.BizArticleMapper;
import com.aiforge.biz.service.BizArticleService;
import com.aiforge.biz.vo.BizArticleVO;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BizArticleServiceImpl extends ServiceImpl<BizArticleMapper, BizArticle> implements BizArticleService {

    private final ArticleConvert articleConvert;

    /**
     * 查看文章详情
     */
    @Override
    public BizArticleVO getByArticleId(Long articleId) {
        if (articleId == null) {
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "文章ID不能为空");
        }
        BizArticle article = this.getById(articleId);
        return articleConvert.toVO(article);
    }
}
