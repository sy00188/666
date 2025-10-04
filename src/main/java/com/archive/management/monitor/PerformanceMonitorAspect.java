package com.archive.management.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 性能监控切面类
 * 用于监控方法执行时间和调用次数
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Aspect
@Component
@Slf4j
public class PerformanceMonitorAspect {

    private final MeterRegistry meterRegistry;
    private final Counter serviceCallCounter;
    private final Timer serviceExecutionTimer;

    public PerformanceMonitorAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.serviceCallCounter = Counter.builder("service.calls.total")
                .description("Total number of service method calls")
                .tag("application", "archive-management")
                .register(meterRegistry);
        
        this.serviceExecutionTimer = Timer.builder("service.execution.time")
                .description("Service method execution time")
                .tag("application", "archive-management")
                .register(meterRegistry);
    }

    /**
     * 监控Service层方法执行
     */
    @Around("execution(* com.archive.management.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("开始监控方法执行: {}.{}", className, methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 增加调用次数计数
            serviceCallCounter.increment(
                    io.micrometer.core.instrument.Tags.of(
                            "class", className,
                            "method", methodName
                    )
            );
            
            // 执行方法并记录时间
            Object result = serviceExecutionTimer.recordCallable(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录慢查询
            if (executionTime > 1000) { // 超过1秒的方法
                log.warn("慢方法检测: {}.{} 执行时间: {}ms", className, methodName, executionTime);
                
                // 记录慢方法指标
                Timer.builder("service.slow.methods")
                        .description("Slow service method execution time")
                        .tag("class", className)
                        .tag("method", methodName)
                        .register(meterRegistry)
                        .record(executionTime, TimeUnit.MILLISECONDS);
            }
            
            log.debug("方法执行完成: {}.{} 耗时: {}ms", className, methodName, executionTime);
            
            return result;
            
        } catch (Exception e) {
            // 记录异常指标
            Counter.builder("service.errors.total")
                    .description("Total number of service errors")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            
            log.error("方法执行异常: {}.{}", className, methodName, e);
            throw e;
        }
    }

    /**
     * 监控Repository层方法执行
     */
    @Around("execution(* com.archive.management.repository.*.*(..))")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录数据库操作时间
            Timer.builder("database.operation.time")
                    .description("Database operation execution time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            
            // 记录慢查询
            if (executionTime > 500) { // 超过500ms的数据库操作
                log.warn("慢查询检测: {}.{} 执行时间: {}ms", className, methodName, executionTime);
                
                Timer.builder("database.slow.queries")
                        .description("Slow database query execution time")
                        .tag("class", className)
                        .tag("method", methodName)
                        .register(meterRegistry)
                        .record(executionTime, TimeUnit.MILLISECONDS);
            }
            
            return result;
            
        } catch (Exception e) {
            // 记录数据库异常
            Counter.builder("database.errors.total")
                    .description("Total number of database errors")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            
            log.error("数据库操作异常: {}.{}", className, methodName, e);
            throw e;
        }
    }

    /**
     * 监控Controller层方法执行
     */
    @Around("execution(* com.archive.management.controller.*.*(..))")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录API响应时间
            Timer.builder("api.response.time")
                    .description("API response time")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .record(executionTime, TimeUnit.MILLISECONDS);
            
            // 记录慢API
            if (executionTime > 2000) { // 超过2秒的API
                log.warn("慢API检测: {}.{} 响应时间: {}ms", className, methodName, executionTime);
                
                Timer.builder("api.slow.responses")
                        .description("Slow API response time")
                        .tag("class", className)
                        .tag("method", methodName)
                        .register(meterRegistry)
                        .record(executionTime, TimeUnit.MILLISECONDS);
            }
            
            return result;
            
        } catch (Exception e) {
            // 记录API异常
            Counter.builder("api.errors.total")
                    .description("Total number of API errors")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
            
            log.error("API执行异常: {}.{}", className, methodName, e);
            throw e;
        }
    }
}
