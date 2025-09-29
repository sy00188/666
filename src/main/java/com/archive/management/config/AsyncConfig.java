package com.archive.management.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 * 配置异步任务执行器和异常处理器
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${async.executor.core-pool-size:10}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:200}")
    private int queueCapacity;

    @Value("${async.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${async.executor.thread-name-prefix:async-}")
    private String threadNamePrefix;

    @Value("${async.executor.await-termination-seconds:60}")
    private int awaitTerminationSeconds;

    /**
     * 默认异步任务执行器
     * 
     * @return 异步任务执行器
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        
        // 线程名前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // 拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        
        // 初始化
        executor.initialize();
        
        return executor;
    }

    /**
     * 文件处理异步执行器
     * 
     * @return 文件处理执行器
     */
    @Bean(name = "fileTaskExecutor")
    public Executor fileTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 文件处理通常IO密集，可以设置更多线程
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("file-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        executor.initialize();
        return executor;
    }

    /**
     * 邮件发送异步执行器
     * 
     * @return 邮件发送执行器
     */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 邮件发送不需要太多并发
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("mail-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * 日志处理异步执行器
     * 
     * @return 日志处理执行器
     */
    @Bean(name = "logTaskExecutor")
    public Executor logTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 日志处理需要快速响应
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(75);
        executor.setKeepAliveSeconds(180);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * 数据统计异步执行器
     * 
     * @return 数据统计执行器
     */
    @Bean(name = "statsTaskExecutor")
    public Executor statsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 统计任务通常计算密集
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(30);
        executor.setKeepAliveSeconds(600);
        executor.setThreadNamePrefix("stats-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180);
        
        executor.initialize();
        return executor;
    }

    /**
     * 消息通知异步执行器
     * 
     * @return 消息通知执行器
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 消息通知需要及时处理
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(60);
        executor.setKeepAliveSeconds(240);
        executor.setThreadNamePrefix("notification-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * 异步异常处理器
     * 
     * @return 异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * 自定义异步异常处理器
     */
    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        @Override
        public void handleUncaughtException(Throwable throwable, java.lang.reflect.Method method, Object... objects) {
            // 记录异步任务异常
            System.err.println("异步任务执行异常:");
            System.err.println("方法: " + method.getDeclaringClass().getName() + "." + method.getName());
            System.err.println("参数: " + java.util.Arrays.toString(objects));
            System.err.println("异常: " + throwable.getMessage());
            throwable.printStackTrace();
            
            // 这里可以添加更多的异常处理逻辑，比如：
            // 1. 发送告警邮件
            // 2. 记录到数据库
            // 3. 发送到监控系统
            // 4. 重试机制等
            
            // 示例：记录到日志系统
            logAsyncException(method, objects, throwable);
        }
        
        /**
         * 记录异步异常到日志系统
         * 
         * @param method 执行方法
         * @param params 方法参数
         * @param throwable 异常信息
         */
        private void logAsyncException(java.lang.reflect.Method method, Object[] params, Throwable throwable) {
            try {
                // 构建异常信息
                StringBuilder errorInfo = new StringBuilder();
                errorInfo.append("异步任务异常详情:\n");
                errorInfo.append("类名: ").append(method.getDeclaringClass().getSimpleName()).append("\n");
                errorInfo.append("方法名: ").append(method.getName()).append("\n");
                errorInfo.append("参数数量: ").append(params != null ? params.length : 0).append("\n");
                
                if (params != null && params.length > 0) {
                    errorInfo.append("参数详情:\n");
                    for (int i = 0; i < params.length; i++) {
                        errorInfo.append("  参数").append(i + 1).append(": ");
                        if (params[i] != null) {
                            errorInfo.append(params[i].getClass().getSimpleName())
                                    .append(" = ").append(params[i].toString());
                        } else {
                            errorInfo.append("null");
                        }
                        errorInfo.append("\n");
                    }
                }
                
                errorInfo.append("异常类型: ").append(throwable.getClass().getSimpleName()).append("\n");
                errorInfo.append("异常消息: ").append(throwable.getMessage()).append("\n");
                
                // 获取堆栈跟踪的前几行
                StackTraceElement[] stackTrace = throwable.getStackTrace();
                if (stackTrace.length > 0) {
                    errorInfo.append("堆栈跟踪:\n");
                    int maxLines = Math.min(5, stackTrace.length);
                    for (int i = 0; i < maxLines; i++) {
                        errorInfo.append("  ").append(stackTrace[i].toString()).append("\n");
                    }
                    if (stackTrace.length > maxLines) {
                        errorInfo.append("  ... 还有 ").append(stackTrace.length - maxLines).append(" 行\n");
                    }
                }
                
                // 输出到控制台（实际项目中应该使用日志框架）
                System.err.println(errorInfo.toString());
                
            } catch (Exception e) {
                // 避免在异常处理中再次抛出异常
                System.err.println("记录异步异常时发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 获取线程池状态信息
     * 
     * @param executor 线程池执行器
     * @return 状态信息
     */
    public static String getExecutorStatus(ThreadPoolTaskExecutor executor) {
        if (executor == null) {
            return "执行器为空";
        }
        
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        if (threadPool == null) {
            return "线程池未初始化";
        }
        
        return String.format(
            "线程池状态 [核心线程数: %d, 活跃线程数: %d, 最大线程数: %d, " +
            "队列大小: %d, 已完成任务数: %d, 总任务数: %d]",
            threadPool.getCorePoolSize(),
            threadPool.getActiveCount(),
            threadPool.getMaximumPoolSize(),
            threadPool.getQueue().size(),
            threadPool.getCompletedTaskCount(),
            threadPool.getTaskCount()
        );
    }

    /**
     * 优雅关闭所有执行器
     */
    public void shutdownExecutors() {
        // 这个方法可以在应用关闭时调用，确保所有异步任务正常完成
        System.out.println("开始关闭异步执行器...");
        
        // 可以添加具体的关闭逻辑
        // 比如等待当前任务完成、记录未完成的任务等
        
        System.out.println("异步执行器关闭完成");
    }
}