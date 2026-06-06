package com.aiforge.ai.listener;

import com.aiforge.common.constant.MqConstants;
import com.aiforge.common.enums.BizTypeEnum;
import com.aiforge.common.enums.OperBusinessTypeStringEnum;
import com.aiforge.common.message.ArticleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 知识库文章向量化同步消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeArticleConsumer {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = MqConstants.ARTICLE_VECTOR_QUEUE)
    public void handleArticleMessage(ArticleMessage articleMessage) {
        try {
            log.info("接收到文章发布消息，准备转为向量化任务, articleId: {}", articleMessage.getArticleId());

            // 转换成 Python 需要的向量同步格式发送给 VECTOR_SYNC_QUEUE
            Map<String, Object> message = new HashMap<>();
            message.put("text", articleMessage.getContent());
            if (articleMessage.getAction() != null) {
                message.put("action", articleMessage.getAction());
            }
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("bizId", String.valueOf(articleMessage.getArticleId()));
            metadata.put("bizType", BizTypeEnum.ARTICLE.getCode()); 
            metadata.put("title", articleMessage.getTitle());
            message.put("metadata", metadata);

            rabbitTemplate.convertAndSend(MqConstants.VECTOR_SYNC_QUEUE, message);
            log.info("已成功将文章投递到向量同步队列(VECTOR_SYNC_QUEUE), articleId: {}", articleMessage.getArticleId());

        } catch (Exception e) {
            log.error("处理文章向量化同步消息失败: {}", articleMessage, e);
        }
    }
}
