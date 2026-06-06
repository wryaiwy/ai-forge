package com.aiforge.biz.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.aiforge.biz.service.GlobalSearchService;
import com.aiforge.biz.vo.SearchResultVO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private final ElasticsearchClient elasticsearchClient;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class EsArticleDoc {
        private Long articleId;
        private String title;
        private String content;
    }

    @Override
    public List<SearchResultVO> globalSearch(String keyword) {
        List<SearchResultVO> resultList = new ArrayList<>();
        try {
            SearchResponse<EsArticleDoc> response = elasticsearchClient.search(s -> s
                    .index("article_index") // 后续有别的索引可以用通配符或多个索引
                    .query(q -> q
                            .multiMatch(m -> m
                                    .fields("title^3", "content") // 标题权重加大
                                    .query(keyword)
                            )
                    )
                    .highlight(h -> h
                            .preTags("<em class='search-highlight'>")
                            .postTags("</em>")
                            .fields("title", f -> f)
                            // 内容高亮，只取一个片段，最大150字符
                            .fields("content", f -> f.fragmentSize(150).numberOfFragments(1))
                            .requireFieldMatch(false)
                    ), EsArticleDoc.class
            );

            for (Hit<EsArticleDoc> hit : response.hits().hits()) {
                EsArticleDoc source = hit.source();
                if (source == null) continue;

                SearchResultVO vo = new SearchResultVO();
                vo.setType("article");
                vo.setId(source.getArticleId());

                // 处理标题高亮
                if (hit.highlight().containsKey("title")) {
                    vo.setTitle(hit.highlight().get("title").get(0));
                } else {
                    vo.setTitle(source.getTitle() != null ? source.getTitle() : "");
                }

                // 处理内容高亮
                if (hit.highlight().containsKey("content")) {
                    vo.setContentSnippet(hit.highlight().get("content").get(0));
                } else {
                    // 如果内容中没命中，就截取原文前150个字作为摘要
                    String originalContent = source.getContent() != null ? source.getContent() : "";
                    vo.setContentSnippet(originalContent.length() > 150 ? originalContent.substring(0, 150) + "..." : originalContent);
                }

                resultList.add(vo);
            }
        } catch (Exception e) {
            log.error("全局搜索失败, keyword: {}", keyword, e);
        }
        return resultList;
    }
}
