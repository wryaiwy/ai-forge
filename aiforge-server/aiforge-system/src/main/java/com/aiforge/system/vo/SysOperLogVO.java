package com.aiforge.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志 VO
 */
@Data
@Schema(description = "操作日志")
public class SysOperLogVO {

    @Schema(description = "操作日志ID")
    private Long operId;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "业务类型编码")
    private Integer businessType;

    @Schema(description = "业务类型描述")
    private String businessTypeDesc;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "HTTP方法")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "操作状态编码")
    private Integer status;

    @Schema(description = "操作状态描述")
    private String statusDesc;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "消耗时间(ms)")
    private Long costTime;

    @Schema(description = "操作时间")
    private LocalDateTime operTime;
}
