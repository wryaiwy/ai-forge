package com.aiforge.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description: Agent RAG 知识库查询响应 VO
 */
@Data
@Schema(description = "Agent RAG 知识库查询响应")
public class RagQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "基于知识库生成的回答")
    private String answer;

    @Schema(description = "引用来源列表")
    private List<RagSourceVO> sources;
}
