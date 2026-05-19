package com.aiforge.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    /** 操作日志ID */
    @TableId(type = IdType.AUTO)
    private Long operId;

    /** 操作用户ID */
    private Long userId;

    /** 操作模块 */
    private String module;

    /** 业务类型 */
    private Integer businessType;

    /** 请求方法 */
    private String method;

    /** HTTP方法 */
    private String requestMethod;

    /** 请求URL */
    private String requestUrl;

    /** 请求参数 */
    private String operParam;

    /** 操作状态 */
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** 消耗时间(ms) */
    private Long costTime;

    /** 操作时间 */
    private LocalDateTime operTime;
}
