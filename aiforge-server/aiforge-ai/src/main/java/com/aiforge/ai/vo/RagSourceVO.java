package com.aiforge.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @Description: RAG 检索来源条目
 */
@Data
@Schema(description = "RAG 检索来源条目")
public class RagSourceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "匹配到的文本片段")
    private String content;

    @Schema(description = "相似度得分")
    private Double score;

    @Schema(description = "附加元数据（如文章标题、作者等）")
    private Map<String, Object> metadata;
}
