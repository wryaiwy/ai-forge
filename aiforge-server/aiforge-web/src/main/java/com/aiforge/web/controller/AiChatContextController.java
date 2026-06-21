package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiChatContextAddDTO;
import com.aiforge.ai.service.AiChatContextService;
import com.aiforge.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI对话消息管理
 */
@Tag(name = "AI 对话消息管理")
@RestController
@RequestMapping("/ai/chat-context")
@RequiredArgsConstructor
public class AiChatContextController {

    private final AiChatContextService aiChatContextService;

    /**
     * 首页AI对话
     */
    @Operation(summary = "首页AI对话")
    @PostMapping(value = "/home-page-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> homePageChat(@Valid @RequestBody AiChatContextAddDTO addDTO) {
        return aiChatContextService.homePageChat(addDTO);
    }

    /**
     * 删除对话消息
     */
    @Operation(summary = "删除对话消息")
    @PostMapping("/delete")
    public Result<Boolean> deleteChatContext(@RequestParam Long chatContextId) {
        boolean result = aiChatContextService.deleteChatContext(chatContextId);
        return Result.success(result);
    }
}