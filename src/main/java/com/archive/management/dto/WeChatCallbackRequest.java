package com.archive.management.dto;

import lombok.Data;

/**
 * 微信回调请求DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
public class WeChatCallbackRequest {
    
    /**
     * 微信授权code
     */
    private String code;
    
    /**
     * 登录状态标识（用于防CSRF）
     */
    private String state;
    
    /**
     * 错误代码（授权失败时返回）
     */
    private String error;
    
    /**
     * 错误描述
     */
    private String errorDescription;
}

