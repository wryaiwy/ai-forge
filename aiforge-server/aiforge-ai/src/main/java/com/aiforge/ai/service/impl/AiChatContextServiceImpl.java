package com.aiforge.ai.service.impl;

import com.aiforge.ai.convert.AiChatContextConvert;
import com.aiforge.ai.dto.AiChatContextAddDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.mapper.AiChatContextMapper;
import com.aiforge.ai.service.AiChatContextService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import com.aiforge.common.utils.SecurityUtils;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

/**
 * AI对话消息Service实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatContextServiceImpl extends ServiceImpl<AiChatContextMapper, AiChatContext>
        implements AiChatContextService {

    private final AiChatContextConvert aiChatContextConvert;
    private final WebClient agentWebClient;

    /**
     * 首页 AI 对话并入库（流式输出）
     */
    @Override
    public Flux<String> homePageChat(AiChatContextAddDTO addDTO) {
        // 1. 记录用户提问
        AiChatContext userCtx = aiChatContextConvert.toEntity(addDTO);
        // Long userId = SecurityUtils.getUserId();
        Long userId = 1L;
        userCtx.setUserId(userId);
        userCtx.setRole("user");
        this.save(userCtx);

        // 2. 调用大模型 (流式) - 转移给 Python 代理端处理
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("user_id", String.valueOf(userId));
        requestBody.put("message", addDTO.getContent());
        requestBody.put("conversation_id", addDTO.getSessionId());

        Flux<String> flux = agentWebClient.post()
                .uri("/agent/home-page-chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class);

        // 3. 收集并记录AI回答
        StringBuffer fullResponse = new StringBuffer();
        return flux.doOnNext(chunk -> {
            if (chunk != null) {
                fullResponse.append(chunk);
            }
        }).doOnComplete(() -> {
            AiChatContext aiCtx = new AiChatContext();
            aiCtx.setSessionId(addDTO.getSessionId());
            aiCtx.setUserId(userId);
            aiCtx.setRole("assistant");
            aiCtx.setContent(fullResponse.toString());
            this.save(aiCtx);
        }).doOnError(e -> {
            log.error("AI 问答流调用失败: {}", e.getMessage(), e);
        });
    }

    /**
     * 删除对话消息
     */
    @Override
    public boolean deleteChatContext(Long chatContextId) {
        return this.removeById(chatContextId);
    }
}