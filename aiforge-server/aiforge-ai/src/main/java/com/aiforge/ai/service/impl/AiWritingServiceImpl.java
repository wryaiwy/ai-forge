package com.aiforge.ai.service.impl;

import com.aiforge.ai.dto.AiWritingChatDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.service.AiWritingService;
import com.aiforge.common.constant.MqConstants;
import com.aiforge.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 写作助手 Service 实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiWritingServiceImpl implements AiWritingService {

    private final WebClient agentWebClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Flux<String> writingChat(AiWritingChatDTO addDTO) {
        Long userId = SecurityUtils.getUserId();
        String agentType = addDTO.getAgentType() != null ? addDTO.getAgentType() : "writing_chat";

        // 1. 记录用户提问，通过 MQ 异步落库到对话表
        AiChatContext userCtx = new AiChatContext();
        userCtx.setSessionId(addDTO.getSessionId());
        userCtx.setUserId(userId);
        userCtx.setRole("user");
        userCtx.setContent(addDTO.getContent());
        userCtx.setAgentType(agentType);
        rabbitTemplate.convertAndSend(MqConstants.CHAT_MEMORY_QUEUE, userCtx);

        // 2. 调用大模型 (流式) - 转至 Python 端 writing-chat 接口
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", String.valueOf(userId));
        requestBody.put("message", addDTO.getContent());
        requestBody.put("session_id", addDTO.getSessionId());

        Flux<String> flux = agentWebClient.post()
                .uri("/agent/writing-chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class);

        // 3. 收集并异步落库 AI 回答
        StringBuffer fullResponse = new StringBuffer();
        return flux.doOnNext(chunk -> {
            if (chunk != null && !chunk.contains("[META]")) {
                fullResponse.append(chunk);
            }
        }).doOnComplete(() -> {
            AiChatContext aiCtx = new AiChatContext();
            aiCtx.setSessionId(addDTO.getSessionId());
            aiCtx.setUserId(userId);
            aiCtx.setRole("assistant");
            aiCtx.setContent(fullResponse.toString());
            aiCtx.setAgentType(agentType);
            rabbitTemplate.convertAndSend(MqConstants.CHAT_MEMORY_QUEUE, aiCtx);
        }).doOnError(e -> {
            log.error("AI 写作问答流调用失败: {}", e.getMessage(), e);
        });
    }
}
