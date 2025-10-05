package com.archive.management.dto;

import lombok.Data;

/**
 * 微信登录响应DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
public class WeChatLoginResponse {
    
    /**
     * 登录状态标识
     */
    private String state;
    
    /**
     * 二维码URL（真实模式）
     */
    private String qrcodeUrl;
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 是否模拟模式
     */
    private Boolean mockMode;
    
    /**
     * 提示信息
     */
    private String message;
}

