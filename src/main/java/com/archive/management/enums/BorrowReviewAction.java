package com.archive.management.enums;

/**
 * 借阅审批操作枚举
 * 定义借阅申请的审批动作
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
public enum BorrowReviewAction {
    
    /**
     * 审批通过
     */
    APPROVED("APPROVED", "审批通过"),
    
    /**
     * 审批拒绝
     */
    REJECTED("REJECTED", "审批拒绝");
    
    private final String code;
    private final String description;
    
    BorrowReviewAction(String code, String description) {
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
    public static BorrowReviewAction fromCode(String code) {
        for (BorrowReviewAction action : values()) {
            if (action.getCode().equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown BorrowReviewAction code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}