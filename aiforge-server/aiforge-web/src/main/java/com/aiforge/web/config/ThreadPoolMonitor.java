package com.aiforge.web.config;

import com.aiforge.common.vo.ThreadPoolStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控器
 *
 * 定期打印线程池运行状态，便于监控和调优
 *
 * @author wengrunyang
 * @date 2026/06/08
 */
@Slf4j
@Component
public class ThreadPoolMonitor {

    /** 异步任务执行器，处理通用异步操作 */
    private final ThreadPoolTaskExecutor asyncExecutor;
    /** IO密集型任务执行器，处理文件/网络等IO操作 */
    private final ThreadPoolTaskExecutor ioExecutor;
    /** CPU密集型任务执行器，处理计算密集型任务 */
    private final ThreadPoolTaskExecutor cpuExecutor;
    /** 定时任务调度器，处理定时/周期性任务 */
    private final ThreadPoolTaskScheduler scheduledExecutor;
    /** 监控间隔时间（毫秒） */
    private static final long MONITOR_INTERVAL = 60000L; // 60秒

    public ThreadPoolMonitor(
            ThreadPoolTaskExecutor asyncExecutor,
            ThreadPoolTaskExecutor ioExecutor,
            ThreadPoolTaskExecutor cpuExecutor,
            ThreadPoolTaskScheduler scheduledExecutor) {
        this.asyncExecutor = asyncExecutor;
        this.ioExecutor = ioExecutor;
        this.cpuExecutor = cpuExecutor;
        this.scheduledExecutor = scheduledExecutor;
    }

    /**
     * 启动监控线程
     */
    @PostConstruct
    public void startMonitor() {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(MONITOR_INTERVAL);
                    printThreadPoolStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "thread-pool-monitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
        log.info("Thread pool monitor started");
    }

    /**
     * 打印所有线程池状态
     */
    public void printThreadPoolStatus() {
        log.info("============= 线程池状态监控 =============");
        printExecutorStatus("asyncExecutor", asyncExecutor);
        printExecutorStatus("ioExecutor", ioExecutor);
        printExecutorStatus("cpuExecutor", cpuExecutor);
        printSchedulerStatus("scheduledExecutor", scheduledExecutor);
        log.info("==========================================");
    }

    /**
     * 打印线程池执行器状态
     */
    private void printExecutorStatus(String name, ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
        log.info("[{}] 核心线程数: {}, 最大线程数: {}, 当前线程数: {}, 活跃线程数: {}, 队列大小: {}, 已完成任务数: {}, 拒绝次数: {}",
                name,
                threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(),
                threadPoolExecutor.getPoolSize(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getCompletedTaskCount(),
                ((ThreadPoolExecutor) executor.getThreadPoolExecutor()).getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy ? "AbortPolicy" : "CallerRunsPolicy"
        );
    }

    /**
     * 打印定时任务调度器状态
     */
    private void printSchedulerStatus(String name, ThreadPoolTaskScheduler scheduler) {
        log.info("[{}] 线程池大小: {}", name, scheduler.getPoolSize());
    }

    /**
     * 获取线程池状态信息（可用于API接口暴露）
     */
    public ThreadPoolStatusVO getThreadPoolStatus() {
        return ThreadPoolStatusVO.builder()
                .asyncCoreSize(asyncExecutor.getThreadPoolExecutor().getCorePoolSize())
                .asyncMaxSize(asyncExecutor.getThreadPoolExecutor().getMaximumPoolSize())
                .asyncActiveCount(asyncExecutor.getThreadPoolExecutor().getActiveCount())
                .asyncQueueSize(asyncExecutor.getThreadPoolExecutor().getQueue().size())
                .asyncCompletedCount(asyncExecutor.getThreadPoolExecutor().getCompletedTaskCount())
                .ioCoreSize(ioExecutor.getThreadPoolExecutor().getCorePoolSize())
                .ioMaxSize(ioExecutor.getThreadPoolExecutor().getMaximumPoolSize())
                .ioActiveCount(ioExecutor.getThreadPoolExecutor().getActiveCount())
                .ioQueueSize(ioExecutor.getThreadPoolExecutor().getQueue().size())
                .ioCompletedCount(ioExecutor.getThreadPoolExecutor().getCompletedTaskCount())
                .cpuCoreSize(cpuExecutor.getThreadPoolExecutor().getCorePoolSize())
                .cpuMaxSize(cpuExecutor.getThreadPoolExecutor().getMaximumPoolSize())
                .cpuActiveCount(cpuExecutor.getThreadPoolExecutor().getActiveCount())
                .cpuQueueSize(cpuExecutor.getThreadPoolExecutor().getQueue().size())
                .cpuCompletedCount(cpuExecutor.getThreadPoolExecutor().getCompletedTaskCount())
                .scheduledPoolSize(scheduledExecutor.getPoolSize())
                .build();
    }

    /**
     * 动态调整线程池参数（用于运行时调优）
     */
    public void adjustAsyncPool(int coreSize, int maxSize) {
        asyncExecutor.setCorePoolSize(coreSize);
        asyncExecutor.setMaxPoolSize(maxSize);
        log.info("asyncExecutor adjusted: core={}, max={}", coreSize, maxSize);
    }

    public void adjustIoPool(int coreSize, int maxSize) {
        ioExecutor.setCorePoolSize(coreSize);
        ioExecutor.setMaxPoolSize(maxSize);
        log.info("ioExecutor adjusted: core={}, max={}", coreSize, maxSize);
    }

    public void adjustCpuPool(int coreSize, int maxSize) {
        cpuExecutor.setCorePoolSize(coreSize);
        cpuExecutor.setMaxPoolSize(maxSize);
        log.info("cpuExecutor adjusted: core={}, max={}", coreSize, maxSize);
    }
}
