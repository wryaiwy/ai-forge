package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description: Agent RAG 知识库查询请求DTO参数
 */
@Data
@Schema(description = "Agent RAG 知识库查询请求DTO参数")
public class RagQueryDTO {

    @NotBlank(message = "查询问题不能为空")
    @Schema(description = "查询问题")
    private String query;

    @Schema(description = "返回最相关的 K 条结果", defaultValue = "5")
    private Integer topK = 5;

    @Schema(description = "指定数据集 ID，为空则全局搜索")
    private String datasetId;
}
