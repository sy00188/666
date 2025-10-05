package com.archive.management.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
public class WeChatLoginRequest {
    
    /**
     * 微信授权code（真实模式）
     */
    private String code;
    
    /**
     * 登录状态标识
     */
    @NotBlank(message = "state不能为空")
    private String state;
    
    /**
     * 模拟微信ID（模拟模式）
     */
    private String mockWechatId;
    
    /**
     * 模拟微信昵称（模拟模式）
     */
    private String mockNickname;
}

