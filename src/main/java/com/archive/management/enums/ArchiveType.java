package com.archive.management.enums;

/**
 * 档案类型枚举
 * 定义档案的各种类型
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public enum ArchiveType {
    
    /**
     * 文档类型 - 普通文档档案
     */
    DOCUMENT(1, "文档"),
    
    /**
     * 图片类型 - 图片档案
     */
    IMAGE(2, "图片"),
    
    /**
     * 视频类型 - 视频档案
     */
    VIDEO(3, "视频"),
    
    /**
     * 音频类型 - 音频档案
     */
    AUDIO(4, "音频"),
    
    /**
     * 合同类型 - 合同档案
     */
    CONTRACT(5, "合同"),
    
    /**
     * 报告类型 - 报告档案
     */
    REPORT(6, "报告"),
    
    /**
     * 证书类型 - 证书档案
     */
    CERTIFICATE(7, "证书"),
    
    /**
     * 其他类型 - 其他类型档案
     */
    OTHER(99, "其他");
    
    private final int code;
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param code 类型代码
     * @param description 类型描述
     */
    ArchiveType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取类型代码
     * 
     * @return 类型代码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 类型代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码无效
     */
    public static ArchiveType fromCode(int code) {
        for (ArchiveType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的档案类型代码: " + code);
    }
    
    /**
     * 根据描述获取枚举值
     * 
     * @param description 类型描述
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果描述无效
     */
    public static ArchiveType fromDescription(String description) {
        for (ArchiveType type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的档案类型描述: " + description);
    }
    
    @Override
    public String toString() {
        return description;
    }
}