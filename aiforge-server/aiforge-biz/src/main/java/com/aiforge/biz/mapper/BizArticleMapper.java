package com.aiforge.biz.mapper;

import com.aiforge.biz.entity.BizArticle;
import com.aiforge.biz.vo.HomeArticleVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizArticleMapper extends BaseMapper<BizArticle> {

    List<HomeArticleVO> selectLatestPublished(@Param("limit") int limit);
}

