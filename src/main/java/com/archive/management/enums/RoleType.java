package com.archive.management.enums;

/**
 * 角色类型枚举
 * 定义角色的各种类型
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public enum RoleType {
    
    /**
     * 系统角色
     */
    SYSTEM("SYSTEM", "系统角色"),
    
    /**
     * 业务角色
     */
    BUSINESS("BUSINESS", "业务角色"),
    
    /**
     * 自定义角色
     */
    CUSTOM("CUSTOM", "自定义角色");
    
    /**
     * 类型编码
     */
    private final String code;
    
    /**
     * 类型描述
     */
    private final String message;
    
    /**
     * 构造方法
     * 
     * @param code 类型编码
     * @param message 类型描述
     */
    RoleType(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取类型编码
     * 
     * @return 类型编码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据类型编码获取枚举
     * 
     * @param code 类型编码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static RoleType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (RoleType type : RoleType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查类型编码是否有效
     * 
     * @param code 类型编码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}

