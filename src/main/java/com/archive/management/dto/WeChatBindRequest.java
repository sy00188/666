package com.archive.management.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 微信账号绑定请求DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
public class WeChatBindRequest {
    
    /**
     * 登录状态标识
     */
    @NotBlank(message = "state不能为空")
    private String state;
    
    /**
     * 系统用户名（绑定现有账号时使用）
     */
    private String username;
    
    /**
     * 系统密码（绑定现有账号时需要验证）
     */
    private String password;
    
    /**
     * 是否创建新账号
     */
    private Boolean createNew = false;
    
    /**
     * 新账号用户名（创建新账号时使用）
     */
    private String newUsername;
    
    /**
     * 新账号邮箱
     */
    private String email;
    
    /**
     * 新账号手机号
     */
    private String phone;
    
    /**
     * 真实姓名
     */
    private String realName;
}

