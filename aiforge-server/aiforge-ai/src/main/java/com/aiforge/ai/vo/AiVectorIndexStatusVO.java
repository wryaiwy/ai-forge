package com.aiforge.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 向量索引状态 VO
 */
@Data
@Schema(description = "向量索引状态")
public class AiVectorIndexStatusVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "索引名称")
    private String indexName;

    @Schema(description = "索引是否存在")
    private Boolean exists;

    @Schema(description = "文档总数")
    private Long docCount;

    @Schema(description = "已删除文档数")
    private Long deletedCount;

    @Schema(description = "索引存储大小(字节)")
    private Long storeSizeBytes;

    @Schema(description = "索引存储大小(可读)")
    private String storeSize;

    @Schema(description = "主分片数")
    private Integer primaryShards;

    @Schema(description = "副本分片数")
    private Integer replicaShards;

    @Schema(description = "索引健康状态(green/yellow/red)")
    private String healthStatus;
}
