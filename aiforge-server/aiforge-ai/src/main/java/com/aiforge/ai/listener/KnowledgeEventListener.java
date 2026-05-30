package com.aiforge.ai.listener;

import com.aiforge.ai.dto.KnowledgeDocDTO;
import com.aiforge.ai.service.AgentService;
import com.aiforge.common.event.KnowledgeAddEvent;
import com.aiforge.common.event.KnowledgeDeleteEvent;
import com.aiforge.common.event.KnowledgeUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import com.aiforge.common.constant.MqConstants;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeEventListener {

    private final AgentService agentService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Async
    @EventListener
    public void handleKnowledgeAddEvent(KnowledgeAddEvent event) {
        log.info("接收到知识入库事件，开始发送向量化消息到MQ, bizId: {}, bizType: {}", event.getBizId(), event.getBizType());

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("text", event.getContent());
            Map<String, String> metadata = new HashMap<>();
            metadata.put("bizId", event.getBizId());
            metadata.put("bizType", event.getBizType());
            metadata.put("title", event.getTitle());
            message.put("metadata", metadata);

            rabbitTemplate.convertAndSend(MqConstants.VECTOR_SYNC_QUEUE, objectMapper.writeValueAsString(message));

            log.info("向量化处理消息已成功投递到 MQ, bizId: {}", event.getBizId());
        } catch (Exception e) {
            log.error("向量化消息投递 MQ 失败, bizId: {}", event.getBizId(), e);
        }
    }

    @Async
    @EventListener
    public void handleKnowledgeUpdateEvent(KnowledgeUpdateEvent event) {
        log.info("接收到知识更新事件，开始异步处理向量化更新, bizId: {}, bizType: {}", event.getBizId(), event.getBizType());

        try {
            // 发送更新操作到 MQ (Python 端会处理 先删除旧向量 -> 后添加新向量 的逻辑)
            Map<String, Object> message = new HashMap<>();
            message.put("action", "update");
            message.put("text", event.getContent());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("bizId", event.getBizId());
            metadata.put("bizType", event.getBizType());
            metadata.put("title", event.getTitle());
            message.put("metadata", metadata);

            rabbitTemplate.convertAndSend(MqConstants.VECTOR_SYNC_QUEUE, objectMapper.writeValueAsString(message));

            log.info("向量化更新消息已投递到 MQ, bizId: {}", event.getBizId());
        } catch (Exception e) {
            log.error("向量化更新消息投递失败, bizId: {}", event.getBizId(), e);
        }
    }

    @Async
    @EventListener
    public void handleKnowledgeDeleteEvent(KnowledgeDeleteEvent event) {
        log.info("接收到知识删除事件，开始异步处理向量化删除, bizId: {}, bizType: {}", event.getBizId(), event.getBizType());

        try {
            // 发送删除操作到 MQ
            Map<String, Object> message = new HashMap<>();
            message.put("action", "delete");
            message.put("bizId", event.getBizId());
            message.put("bizType", event.getBizType());

            rabbitTemplate.convertAndSend(MqConstants.VECTOR_SYNC_QUEUE, objectMapper.writeValueAsString(message));

            log.info("向量化删除消息已投递到 MQ, bizId: {}", event.getBizId());
        } catch (Exception e) {
            log.error("向量化删除消息投递失败, bizId: {}", event.getBizId(), e);
        }
    }
}
