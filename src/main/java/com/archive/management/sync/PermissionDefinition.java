package com.archive.management.sync;

import lombok.Data;

/**
 * 权限定义数据传输对象
 * 用于在权限同步过程中传递权限定义数据
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
public class PermissionDefinition {
    
    /**
     * 权限代码
     */
    private String code;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限描述
     */
    private String description;
    
    /**
     * 权限类型：MENU, BUTTON, API, DATA
     */
    private String type;
    
    /**
     * 权限路径
     */
    private String path;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 父权限代码
     */
    private String parentCode;
    
    /**
     * 数据来源：ANNOTATION, CONFIG
     */
    private String source;
}

