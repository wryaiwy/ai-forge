package com.aiforge.common.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 线程池状态VO
 *
 * 用于返回线程池运行状态信息，支持监控和调优
 *
 * @author wengrunyang
 * @date 2026/06/08
 */
@Data
@Builder
public class ThreadPoolStatusVO {

    /**
     * asyncExecutor 线程池状态
     */
    private Integer asyncCoreSize;
    private Integer asyncMaxSize;
    private Integer asyncActiveCount;
    private Integer asyncQueueSize;
    private Long asyncCompletedCount;

    /**
     * ioExecutor 线程池状态
     */
    private Integer ioCoreSize;
    private Integer ioMaxSize;
    private Integer ioActiveCount;
    private Integer ioQueueSize;
    private Long ioCompletedCount;

    /**
     * cpuExecutor 线程池状态
     */
    private Integer cpuCoreSize;
    private Integer cpuMaxSize;
    private Integer cpuActiveCount;
    private Integer cpuQueueSize;
    private Long cpuCompletedCount;

    /**
     * scheduledExecutor 线程池状态
     */
    private Integer scheduledPoolSize;
}
