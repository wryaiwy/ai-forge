package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI对话消息添加DTO
 */
@Data
@Schema(description = "AI对话消息添加请求参数")
public class AiChatContextAddDTO {

    @Schema(description = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @Schema(description = "消息的具体内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;
}