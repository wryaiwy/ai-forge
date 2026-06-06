package com.aiforge.biz.service;

import com.aiforge.biz.vo.SearchResultVO;

import java.util.List;

public interface GlobalSearchService {
    /**
     * 全站跨域搜索
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    List<SearchResultVO> globalSearch(String keyword);
}
