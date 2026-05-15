package com.aiforge.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: Agent 知识库文档入库响应 VO
 */
@Data
@Schema(description = "Agent 知识库文档入库响应")
public class KnowledgeDocVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档唯一标识")
    private String docId;

    @Schema(description = "入库状态: success/failed")
    private String status;
}
