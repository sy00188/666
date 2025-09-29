package com.archive.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 定时任务配置类
 * 配置定时任务线程池和调度器
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    /**
     * 定时任务线程池大小
     */
    private static final int POOL_SIZE = 10;

    /**
     * 配置定时任务调度器
     * 
     * @param taskRegistrar 任务注册器
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    /**
     * 定时任务执行器
     * 
     * @return Executor
     */
    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(POOL_SIZE, r -> {
            Thread thread = new Thread(r);
            thread.setName("scheduled-task-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Spring Task Scheduler
     * 
     * @return TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 线程池配置
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix("archive-scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // 拒绝策略
        scheduler.setRejectedExecutionHandler((r, executor) -> {
            System.err.println("定时任务被拒绝执行: " + r.toString());
        });
        
        // 错误处理
        scheduler.setErrorHandler(throwable -> {
            System.err.println("定时任务执行异常: " + throwable.getMessage());
            throwable.printStackTrace();
        });
        
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 获取线程池状态
     * 
     * @return 状态信息
     */
    public String getSchedulerStatus() {
        TaskScheduler scheduler = taskScheduler();
        if (scheduler instanceof ThreadPoolTaskScheduler) {
            ThreadPoolTaskScheduler threadPoolScheduler = (ThreadPoolTaskScheduler) scheduler;
            
            return String.format(
                "定时任务调度器状态 [线程池大小: %d, 活跃线程: %d, 已完成任务: %d]",
                threadPoolScheduler.getPoolSize(),
                threadPoolScheduler.getActiveCount(),
                threadPoolScheduler.getThreadPoolExecutor().getCompletedTaskCount()
            );
        }
        
        return "无法获取调度器状态";
    }

    /**
     * 优雅关闭调度器
     */
    public void gracefulShutdown() {
        TaskScheduler scheduler = taskScheduler();
        if (scheduler instanceof ThreadPoolTaskScheduler) {
            ThreadPoolTaskScheduler threadPoolScheduler = (ThreadPoolTaskScheduler) scheduler;
            threadPoolScheduler.shutdown();
            System.out.println("定时任务调度器已优雅关闭");
        }
    }
}