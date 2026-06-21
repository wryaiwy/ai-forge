package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiHomePageChatDTO;
import com.aiforge.ai.entity.AiChatContext;
import com.aiforge.ai.service.AiChatContextService;
import com.aiforge.ai.vo.AiChatContextVO;
import com.aiforge.ai.vo.AiHomePageChatListVO;
import com.aiforge.common.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.util.List;

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
     * 首页 AI 对话并入库（流式输出）
     *
     * @param addDTO 请求体
     * @return AI 回答流
     */
    @Operation(summary = "首页AI对话")
    @PostMapping(value = "/home-page-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> homePageChat(@Valid @RequestBody AiHomePageChatDTO addDTO) {
        return aiChatContextService.homePageChat(addDTO);
    }

    /**
     * 删除对话消息（逻辑删除）
     * @param chatContextId 主键ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除对话消息")
    @PostMapping("/delete")
    public Result<Boolean> deleteChatContext(@RequestParam Long chatContextId) {
        boolean result = aiChatContextService.deleteChatContext(chatContextId);
        return Result.success(result);
    }

    /**
     * 首页 AI对话助手 消息列表（按时间倒序，分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 消息列表
     */
    @Operation(summary = "首页AI对话助手消息列表")
    @GetMapping("/home-page-chat/list")
    public Result<IPage<AiHomePageChatListVO>> getHomePageChatList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiChatContext> page = new Page<>(pageNum, pageSize);
        IPage<AiHomePageChatListVO> result = aiChatContextService.getHomePageChatList(page);
        return Result.success(result);
    }

    /**
     * 首页 AI对话助手 消息详情
     * @param chatContextId 主键ID
     * @return 会话消息列表
     */
    @Operation(summary = "首页AI对话助手消息详情")
    @GetMapping("/home-page-chat/detail")
    public Result<List<AiChatContextVO>> getChatContextDetail(@RequestParam Long chatContextId) {
        List<AiChatContextVO> list = aiChatContextService.getChatContextDetail(chatContextId);
        return Result.success(list);
    }
}