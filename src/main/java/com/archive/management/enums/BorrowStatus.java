package com.archive.management.enums;

/**
 * 借阅状态枚举
 * 定义借阅记录的各种状态
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
public enum BorrowStatus {
    
    /**
     * 待审批
     */
    PENDING("PENDING", "待审批"),
    
    /**
     * 已审批
     */
    APPROVED("APPROVED", "已审批"),
    
    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),
    
    /**
     * 已借出
     */
    BORROWED("BORROWED", "已借出"),
    
    /**
     * 已归还
     */
    RETURNED("RETURNED", "已归还"),
    
    /**
     * 已逾期
     */
    OVERDUE("OVERDUE", "已逾期");
    
    private final String code;
    private final String description;
    
    BorrowStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 代码
     * @return 枚举值
     */
    public static BorrowStatus fromCode(String code) {
        for (BorrowStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown BorrowStatus code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}