package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * @Description: Agent 知识库文档入库请求DTO参数
 */
@Data
@Schema(description = "Agent 知识库文档入库请求DTO参数")
public class KnowledgeDocDTO {

    @NotBlank(message = "关联的业务ID不能为空")
    @Schema(description = "关联的业务ID")
    private String bizId;

    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型（如: article, faq, product）")
    private String bizType;

    @NotBlank(message = "文档标题不能为空")
    @Schema(description = "文档标题")
    private String title;

    @NotBlank(message = "文档内容不能为空")
    @Schema(description = "文档正文内容")
    private String content;

    @Schema(description = "自定义元数据")
    private Map<String, Object> metadata;
}
