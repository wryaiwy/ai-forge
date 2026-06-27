package com.aiforge.ai.listener;

import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.service.AiChatContextService;
import com.aiforge.common.constant.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 监听聊天记录异步落库队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatMemoryConsumer {

    private final AiChatContextService aiChatContextService;

    @RabbitListener(queuesToDeclare = @Queue(MqConstants.CHAT_MEMORY_QUEUE))
    public void consume(AiChatContext chatContext) {
        try {
            aiChatContextService.save(chatContext);
            log.info("异步落库聊天记录成功: sessionId={}, role={}", chatContext.getSessionId(), chatContext.getRole());
        } catch (Exception e) {
            log.error("异步落库聊天记录失败: sessionId={}", chatContext.getSessionId(), e);
        }
    }
}
