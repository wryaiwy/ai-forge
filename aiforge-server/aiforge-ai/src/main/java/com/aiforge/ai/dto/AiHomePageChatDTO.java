package com.aiforge.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 首页 AI 对话助手消息添加DTO
 */
@Data
@Schema(description = "首页AI对话助手消息添加请求参数")
public class AiHomePageChatDTO {

    @Schema(description = "会话ID", required = true)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @Schema(description = "助手类型", required = true)
    @NotBlank(message = "助手类型不能为空")
    private String agentType;

    @Schema(description = "消息的具体内容", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String content;

    @Schema(description = "是否开启联网搜索")
    @JsonProperty("enable_web_search")
    private Boolean enableWebSearch;

    @Schema(description = "上传的图片地址")
    @JsonProperty("image_url")
    private String imageUrl;
}