package com.aiforge.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局线程池配置
 * 线程池规划：
 * 1. asyncExecutor - 通用异步任务线程池（@Async注解使用）
 * 2. scheduledExecutor - 定时任务线程池（@Scheduled注解使用）
 * 3. ioExecutor - IO密集型任务线程池（如HTTP调用、文件操作）
 * 4. cpuExecutor - CPU密集型任务线程池（如数据计算、图片处理）
 *
 * @author wengrunyang
 * @date 2026/06/08
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * CPU核心数（逻辑处理器）
     */
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    /**
     * 通用异步任务线程池
     *
     * 适用场景：@Async注解的异步方法、一般异步任务
     * 参数说明：
     * - 核心线程数 = CPU核心数（CPU密集型基础值）
     * - 最大线程数 = CPU核心数 * 2（允许适度超发）
     * - 队列容量 = 200（缓冲一定量的任务）
     * - 拒绝策略 = CallerRunsPolicy（让调用者线程执行，实现背压）
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORES);
        executor.setMaxPoolSize(CPU_CORES * 2);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("asyncExecutor initialized: core={}, max={}, queue={}", CPU_CORES, CPU_CORES * 2, 200);
        return executor;
    }

    /**
     * IO密集型任务线程池
     *
     * 适用场景：HTTP外部调用、文件上传下载、数据库批量操作
     * 参数说明：
     * - 核心线程数 = CPU核心数 * 2（IO等待时间长，需要更多线程）
     * - 最大线程数 = CPU核心数 * 4（IO高峰时允许更多并发）
     * - 队列容量 = 500（IO任务通常较多，需要较大缓冲）
     * - 拒绝策略 = CallerRunsPolicy（背压机制，防止OOM）
     */
    @Bean("ioExecutor")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORES * 2);
        executor.setMaxPoolSize(CPU_CORES * 4);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("io-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        log.info("ioExecutor initialized: core={}, max={}, queue={}", CPU_CORES * 2, CPU_CORES * 4, 500);
        return executor;
    }

    /**
     * CPU密集型任务线程池
     *
     * 适用场景：数据计算、图片处理、报表生成、复杂业务逻辑
     * 参数说明：
     * - 核心线程数 = CPU核心数（避免过多上下文切换）
     * - 最大线程数 = CPU核心数 + 1（经典CPU密集公式）
     * - 队列容量 = 100（CPU任务不宜积压过多）
     * - 拒绝策略 = AbortPolicy（直接拒绝并抛异常，CPU任务积压是严重问题）
     */
    @Bean("cpuExecutor")
    public Executor cpuExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_CORES);
        executor.setMaxPoolSize(CPU_CORES + 1);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("cpu-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("cpuExecutor initialized: core={}, max={}, queue={}", CPU_CORES, CPU_CORES + 1, 100);
        return executor;
    }

    /**
     * 定时任务线程池
     *
     * 适用场景：@Scheduled注解的定时任务
     * 参数说明：
     * - 线程数 = 4（定时任务通常不多，固定值即可）
     * - 线程名前缀 = scheduled-
     */
    @Bean("scheduledExecutor")
    public ThreadPoolTaskScheduler scheduledExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.initialize();
        log.info("scheduledExecutor initialized: poolSize=4");
        return scheduler;
    }
}
