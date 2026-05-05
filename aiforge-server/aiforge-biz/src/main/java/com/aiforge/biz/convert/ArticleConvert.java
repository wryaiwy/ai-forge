package com.aiforge.biz.convert;

import com.aiforge.biz.dto.BizArticleDTO;
import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.vo.BizArticleVO;

import com.aiforge.biz.vo.HomeArticleVO;
import org.mapstruct.Mapper;

/**
 * @Author: wengrunyang
 * @Description: 文章转换器
 * @DateTime: 2026/4/26 21:39
 **/
@Mapper(componentModel = "spring")
public interface ArticleConvert {

    BizArticle toEntity(BizArticleDTO dto);

    BizArticleVO toVO(BizArticle entity);

    HomeArticleVO toHomeVO(BizArticle entity);

    BizArticleDTO toDTO(BizArticle entity);
}