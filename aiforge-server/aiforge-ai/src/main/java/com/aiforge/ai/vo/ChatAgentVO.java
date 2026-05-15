package com.aiforge.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @Description: Agent 客服聊天响应 VO
 */
@Data
@Schema(description = "Agent 客服聊天响应")
public class ChatAgentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "AI 回复内容")
    private String answer;

    @Schema(description = "当前会话 ID")
    private String conversationId;

    @Schema(description = "Token 使用统计")
    private Map<String, Object> usage;
}
