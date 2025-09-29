package com.archive.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 用于标记需要特定权限才能访问的方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 权限标识
     * 
     * @return 权限标识字符串
     */
    String value();
    
    /**
     * 权限描述
     * 
     * @return 权限描述
     */
    String description() default "";
    
    /**
     * 是否必须拥有该权限
     * 
     * @return true表示必须拥有，false表示可选
     */
    boolean required() default true;
}