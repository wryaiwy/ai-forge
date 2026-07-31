package com.aiforge.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 向量文档详情 VO
 */
@Data
@Schema(description = "向量文档详情")
public class AiVectorDocVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID")
    private String docId;

    @Schema(description = "文本内容")
    private String text;

    @Schema(description = "向量维度")
    private Integer vectorDims;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "知识库ID")
    private String kbId;

    @Schema(description = "标签列表")
    private String tags;

    @Schema(description = "完整元数据")
    private Map<String, Object> metadata;
}
