package com.aiforge.biz.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.StringReader;

/**
 * Elasticsearch 索引初始化器
 * 在项目启动时，自动检测并创建 article_index 及其 Mapping
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsIndexInitializer implements ApplicationRunner {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public void run(ApplicationArguments args) {
        String indexName = "article_index";
        try {
            // 1. 判断索引是否存在
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(indexName))
                    .value();

            if (!exists) {
                log.info("检测到 Elasticsearch 索引 [{}] 不存在，开始创建并初始化 Mapping...", indexName);

                // 2. 准备 Mapping DSL (用多行字符串非常直观，且不容易写错 Builder API)
                String mappingJson = """
                        {
                          "properties": {
                            "articleId": {
                              "type": "long"
                            },
                            "title": {
                              "type": "text",
                              "analyzer": "ik_max_word",
                              "search_analyzer": "ik_smart"
                            },
                            "content": {
                              "type": "text",
                              "analyzer": "ik_max_word",
                              "search_analyzer": "ik_smart"
                            }
                          }
                        }
                        """;

                // 3. 执行创建
                elasticsearchClient.indices().create(c -> c
                        .index(indexName)
                        // 新版 ES 客户端完美支持直接注入 JSON 流进行设置
                        .mappings(m -> m.withJson(new StringReader(mappingJson)))
                );

                log.info("Elasticsearch 索引 [{}] 创建并初始化成功！", indexName);
            } else {
                log.info("Elasticsearch 索引 [{}] 已存在，跳过初始化。", indexName);
            }
        } catch (Exception e) {
            log.error("初始化 Elasticsearch 索引 [{}] 失败，请检查 ES 连接状态", indexName, e);
        }
    }
}
