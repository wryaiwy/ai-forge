package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @Description: Agent 客服聊天请求DTO参数
 */
@Data
@Schema(description = "Agent 客服聊天请求DTO参数")
public class ChatAgentDTO {

    @NotBlank(message = "用户 ID 不能为空")
    @Schema(description = "用户 ID")
    private String userId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户输入的消息")
    private String message;

    @Schema(description = "会话 ID，首次对话可为空")
    private String conversationId;

    @Schema(description = "历史对话上下文")
    private List<ChatMessageDTO> history;
}
