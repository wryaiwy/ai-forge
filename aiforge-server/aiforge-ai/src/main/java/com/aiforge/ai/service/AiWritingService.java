package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiWritingChatDTO;
import reactor.core.publisher.Flux;

/**
 * AI 写作助手 Service 接口
 */
public interface AiWritingService {

    /**
     * AI 写作对话并入库（流式输出）
     *
     * @param addDTO 请求参数
     * @return AI问答流
     */
    Flux<String> writingChat(AiWritingChatDTO addDTO);
}
