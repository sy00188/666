package com.archive.management.enums;

/**
 * 档案状态枚举
 * 定义档案的各种状态
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public enum ArchiveStatus {
    
    /**
     * 活跃状态 - 档案正常可用
     */
    ACTIVE(1, "活跃"),
    
    /**
     * 已归档状态 - 档案已归档存储
     */
    ARCHIVED(2, "已归档"),
    
    /**
     * 已删除状态 - 档案已被删除
     */
    DELETED(3, "已删除"),
    
    /**
     * 草稿状态 - 档案处于草稿阶段
     */
    DRAFT(4, "草稿"),
    
    /**
     * 审核中状态 - 档案正在审核
     */
    PENDING(5, "审核中"),
    
    /**
     * 已拒绝状态 - 档案审核被拒绝
     */
    REJECTED(6, "已拒绝");
    
    private final int code;
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param description 状态描述
     */
    ArchiveStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码无效
     */
    public static ArchiveStatus fromCode(int code) {
        for (ArchiveStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的档案状态代码: " + code);
    }
    
    /**
     * 根据描述获取枚举值
     * 
     * @param description 状态描述
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果描述无效
     */
    public static ArchiveStatus fromDescription(String description) {
        for (ArchiveStatus status : values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的档案状态描述: " + description);
    }
    
    @Override
    public String toString() {
        return description;
    }
}