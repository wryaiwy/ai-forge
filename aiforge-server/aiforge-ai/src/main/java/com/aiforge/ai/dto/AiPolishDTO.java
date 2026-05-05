package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: AI 润色请求参数
 */
@Data
@Schema(description = "AI 润色请求参数")
public class AiPolishDTO {

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "需要润色的文章内容 (Markdown)")
    private String content;

    @NotNull(message = "润色模式不能为空")
    @Schema(description = "润色模式: 1-语法修正 2-专业润色 3-风格转换")
    private Integer polishMode;

    @Schema(description = "风格转换时的目标风格描述 (仅 polishMode=3 时有效)")
    private String targetStyle;
}
