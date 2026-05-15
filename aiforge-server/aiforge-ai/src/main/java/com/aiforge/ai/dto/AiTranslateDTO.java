package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Description: AI 翻译请求DTO参数
 */
@Data
@Schema(description = "AI 翻译请求DTO参数")
public class AiTranslateDTO {

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "需要翻译的文章内容 (Markdown)")
    private String content;

    @NotNull(message = "目标语言不能为空")
    @Schema(description = "目标语言代码: en-英语 zh-中文 ja-日语 ko-韩语 fr-法语 de-德语 es-西班牙语")
    private String targetLang;
}
