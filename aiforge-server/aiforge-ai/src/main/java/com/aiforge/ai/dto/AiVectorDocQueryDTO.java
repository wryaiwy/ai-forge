package com.aiforge.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 向量文档查询 DTO
 */
@Data
@Schema(description = "向量文档查询参数")
public class AiVectorDocQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;

    @Schema(description = "业务类型过滤", example = "article")
    private String bizType;

    @Schema(description = "业务ID过滤", example = "123456")
    private String bizId;

    @Schema(description = "知识库ID过滤")
    private String kbId;

    @Schema(description = "文本内容关键词搜索")
    private String keyword;
}
