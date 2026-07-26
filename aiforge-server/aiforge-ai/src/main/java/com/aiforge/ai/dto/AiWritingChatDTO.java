package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 写作助手消息请求 DTO
 */
@Data
@Schema(description = "AI写作助手消息请求参数")
public class AiWritingChatDTO {

    @Schema(description = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @Schema(description = "助手类型")
    private String agentType = "writing_chat";

    @Schema(description = "消息的具体内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
