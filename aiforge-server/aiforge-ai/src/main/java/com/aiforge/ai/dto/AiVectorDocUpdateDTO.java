package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 向量文档元数据更新 DTO
 */
@Data
@Schema(description = "向量文档元数据更新参数")
public class AiVectorDocUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "文档ID不能为空")
    @Schema(description = "文档ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String docId;

    @Schema(description = "文本内容（可选，不传则不更新）")
    private String text;

    @Schema(description = "元数据（合并更新，新字段会覆盖旧字段，保留未传入的旧字段）")
    private Map<String, Object> metadata;
}
