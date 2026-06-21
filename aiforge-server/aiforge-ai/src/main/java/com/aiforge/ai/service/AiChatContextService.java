package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiChatContextAddDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Flux;

/**
 * AI对话消息Service接口
 */
public interface AiChatContextService extends IService<AiChatContext> {

    /**
     * 首页 AI 对话并入库（流式输出）
     *
     * @param addDTO 请求体
     * @return AI 回答流
     */
    Flux<String> homePageChat(AiChatContextAddDTO addDTO);

    /**
     * 删除对话消息（逻辑删除）
     * @param chatContextId 主键ID
     * @return 是否删除成功
     */
    boolean deleteChatContext(Long chatContextId);
}