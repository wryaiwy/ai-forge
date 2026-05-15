package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: 单条客服聊天消息DTO参数
 */
@Data
@Schema(description = "单条客服聊天消息DTO参数")
public class ChatMessageDTO {

    @Schema(description = "消息角色: user/assistant/system")
    private String role;

    @Schema(description = "消息内容")
    private String content;
}
