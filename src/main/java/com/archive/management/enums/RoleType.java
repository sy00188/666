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
    SYSTEM("SYSTEM", "系统角色", 1),
    
    /**
     * 业务角色
     */
    BUSINESS("BUSINESS", "业务角色", 2),
    
    /**
     * 自定义角色
     */
    CUSTOM("CUSTOM", "自定义角色", 3);
    
    /**
     * 类型编码
     */
    private final String code;
    
    /**
     * 类型描述
     */
    private final String message;
    
    /**
     * 数据库存储值
     */
    private final Integer value;
    
    /**
     * 构造方法
     * 
     * @param code 类型编码
     * @param message 类型描述
     * @param value 数据库存储值
     */
    RoleType(String code, String message, Integer value) {
        this.code = code;
        this.message = message;
        this.value = value;
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
     * 获取数据库存储值
     * 
     * @return 数据库存储值
     */
    public Integer getValue() {
        return value;
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
     * 根据数据库值获取枚举
     * 
     * @param value 数据库值
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static RoleType fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (RoleType type : RoleType.values()) {
            if (type.getValue().equals(value)) {
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
    
    /**
     * 检查数据库值是否有效
     * 
     * @param value 数据库值
     * @return 是否有效
     */
    public static boolean isValidValue(Integer value) {
        return fromValue(value) != null;
    }
}

