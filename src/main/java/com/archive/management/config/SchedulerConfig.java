package com.archive.management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时任务配置类
 * 配置定时任务线程池、调度器等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Value("${scheduler.pool-size:10}")
    private int poolSize;

    @Value("${scheduler.thread-name-prefix:scheduler-}")
    private String threadNamePrefix;

    @Value("${scheduler.await-termination-seconds:60}")
    private int awaitTerminationSeconds;

    @Value("${scheduler.wait-for-tasks-to-complete-on-shutdown:true}")
    private boolean waitForTasksToCompleteOnShutdown;

    @Value("${scheduler.remove-on-cancel-policy:true}")
    private boolean removeOnCancelPolicy;

    /**
     * 配置任务调度器
     */
    @Bean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 线程池配置
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
        scheduler.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        scheduler.setRemoveOnCancelPolicy(removeOnCancelPolicy);
        
        // 拒绝策略
        scheduler.setRejectedExecutionHandler((r, executor) -> {
            System.err.println("定时任务被拒绝执行: " + r.toString());
        });
        
        // 错误处理
        scheduler.setErrorHandler(throwable -> {
            System.err.println("定时任务执行出错: " + throwable.getMessage());
            throwable.printStackTrace();
        });
        
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 配置档案定时任务调度器
     */
    @Bean(name = "archiveScheduler")
    public ScheduledExecutorService archiveScheduler() {
        return new ScheduledThreadPoolExecutor(
            Math.max(2, poolSize / 4),
            new CustomThreadFactory("archive-scheduler-")
        );
    }

    /**
     * 配置用户定时任务调度器
     */
    @Bean(name = "userScheduler")
    public ScheduledExecutorService userScheduler() {
        return new ScheduledThreadPoolExecutor(
            Math.max(2, poolSize / 4),
            new CustomThreadFactory("user-scheduler-")
        );
    }

    /**
     * 配置系统维护定时任务调度器
     */
    @Bean(name = "systemMaintenanceScheduler")
    public ScheduledExecutorService systemMaintenanceScheduler() {
        return new ScheduledThreadPoolExecutor(
            Math.max(2, poolSize / 4),
            new CustomThreadFactory("system-maintenance-scheduler-")
        );
    }

    /**
     * 配置系统定时任务调度器
     */
    @Bean(name = "systemScheduler")
    public ScheduledExecutorService systemScheduler() {
        return new ScheduledThreadPoolExecutor(
            Math.max(2, poolSize / 4),
            new CustomThreadFactory("system-scheduler-")
        );
    }

    /**
     * 自定义线程工厂
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            
            // 设置异常处理器
            thread.setUncaughtExceptionHandler((t, e) -> {
                System.err.println("线程 " + t.getName() + " 发生未捕获异常: " + e.getMessage());
                e.printStackTrace();
            });
            
            return thread;
        }
    }

    /**
     * 创建Cron触发器
     */
    public CronTrigger createCronTrigger(String cronExpression) {
        return new CronTrigger(cronExpression);
    }

    /**
     * 验证Cron表达式
     */
    public boolean isValidCronExpression(String cronExpression) {
        try {
            new CronTrigger(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取调度器状态信息
     */
    public String getSchedulerStatus() {
        return String.format(
            "定时任务调度器状态 - 线程池大小: %d, 线程名前缀: %s, 等待终止时间: %d秒",
            poolSize, threadNamePrefix, awaitTerminationSeconds
        );
    }

    /**
     * 获取线程池使用情况
     */
    public String getThreadPoolUsage(ScheduledExecutorService executor) {
        if (executor instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor) executor;
            return String.format(
                "线程池使用情况 - 核心线程数: %d, 活跃线程数: %d, 任务队列大小: %d, 已完成任务数: %d",
                scheduledExecutor.getCorePoolSize(),
                scheduledExecutor.getActiveCount(),
                scheduledExecutor.getQueue().size(),
                scheduledExecutor.getCompletedTaskCount()
            );
        }
        return "无法获取线程池使用情况";
    }

    /**
     * 关闭调度器
     */
    public void shutdownScheduler(ScheduledExecutorService executor) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(awaitTerminationSeconds, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 检查调度器健康状态
     */
    public boolean checkSchedulerHealth(ScheduledExecutorService executor) {
        return executor != null && !executor.isShutdown() && !executor.isTerminated();
    }
}