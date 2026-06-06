package com.aiforge.biz.listener;

import com.aiforge.common.constant.MqConstants;
import com.aiforge.common.enums.OperBusinessTypeStringEnum;
import com.aiforge.common.message.ArticleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 文章同步 ES 监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsArticleConsumer {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * 接收文章同步 ES 消息
     * @param message 文章同步 ES 消息
     */
    @RabbitListener(queues = MqConstants.ARTICLE_ES_QUEUE)
    public void handleArticleMessage(ArticleMessage message) {
        try {
            log.info("接收到文章同步 ES 消息, articleId: {}", message.getArticleId());

            if (OperBusinessTypeStringEnum.DELETE.getCode().equals(message.getAction())) {
                elasticsearchClient.delete(d -> d
                        .index("article_index")
                        .id(String.valueOf(message.getArticleId()))
                );
                log.info("文章已从 ES 删除, articleId: {}", message.getArticleId());
            } else {
                Map<String, Object> esDoc = new HashMap<>();
                esDoc.put("articleId", message.getArticleId());
                esDoc.put("title", message.getTitle());
                esDoc.put("content", message.getContent());

                // Elasticsearch的索引操作（ index ）具有幂等性，不会因为重复调用而改变结果
                // 如果文档ID已存在 → 更新；如果文档ID不存在 → 新增
                elasticsearchClient.index(i -> i
                        .index("article_index")
                        .id(String.valueOf(message.getArticleId()))
                        .document(esDoc)
                );
                log.info("文章已成功写入 ES, articleId: {}", message.getArticleId());
            }
            
        } catch (Exception e) {
            log.error("消费文章 ES 同步消息失败: {}", message, e);
        }
    }
}
