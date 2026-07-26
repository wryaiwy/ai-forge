package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiWritingChatDTO;
import com.aiforge.ai.service.AiWritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 写作助手消息管理 Controller
 */
@Tag(name = "AI 写作助手管理")
@RestController
@RequestMapping("/ai/writing")
@RequiredArgsConstructor
public class AiWritingController {

    private final AiWritingService aiWritingService;

    /**
     * 写作 AI 对话并入库（流式输出）
     *
     * @param addDTO 请求体
     * @return AI 回答流
     */
    @Operation(summary = "写作AI对话")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> writingChat(@Valid @RequestBody AiWritingChatDTO addDTO) {
        return aiWritingService.writingChat(addDTO);
    }
}
