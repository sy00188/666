package com.archive.management.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     * 
     * @return 操作模块名称
     */
    String module();
    
    /**
     * 操作类型
     * 
     * @return 操作类型描述
     */
    String operation();
    
    /**
     * 操作描述
     * 
     * @return 操作详细描述
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     * 
     * @return true表示记录，false表示不记录
     */
    boolean logParams() default true;
    
    /**
     * 是否记录返回结果
     * 
     * @return true表示记录，false表示不记录
     */
    boolean logResult() default false;
    
    /**
     * 是否记录异常信息
     * 
     * @return true表示记录，false表示不记录
     */
    boolean logException() default true;
}