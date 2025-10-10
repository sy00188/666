package com.archive.management.enums;

/**
 * 角色状态枚举
 * 定义角色的各种状态
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public enum RoleStatus {
    
    /**
     * 启用状态
     */
    ACTIVE(1, "启用"),
    
    /**
     * 禁用状态
     */
    INACTIVE(0, "禁用"),
    
    /**
     * 锁定状态
     */
    LOCKED(2, "锁定");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String message;
    
    /**
     * 构造方法
     * 
     * @param code 状态码
     * @param message 状态描述
     */
    RoleStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取状态码
     * 
     * @return 状态码
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据状态码获取枚举
     * 
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static RoleStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleStatus status : RoleStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 检查状态码是否有效
     * 
     * @param code 状态码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}

