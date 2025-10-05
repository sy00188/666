package com.archive.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信登录配置类
 * 从application.yml读取微信相关配置参数
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "archive.wechat")
public class WeChatConfig {
    
    /**
     * 是否启用微信登录功能
     */
    private Boolean enabled = true;
    
    /**
     * 是否使用模拟模式（开发环境）
     */
    private Boolean mockMode = true;
    
    /**
     * 微信开放平台AppID
     */
    private String appId;
    
    /**
     * 微信开放平台AppSecret
     */
    private String appSecret;
    
    /**
     * 授权回调地址
     */
    private String redirectUri;
    
    /**
     * 应用授权作用域
     * snsapi_login: 网站应用微信登录
     */
    private String scope = "snsapi_login";
    
    /**
     * 二维码过期时间（分钟）
     */
    private Integer qrcodeExpireMinutes = 5;
    
    /**
     * 登录状态过期时间（分钟）
     */
    private Integer stateExpireMinutes = 10;
    
    /**
     * 是否自动注册新用户
     */
    private Boolean autoRegister = true;
    
    /**
     * 微信API端点
     */
    public static class WeChatApi {
        public static final String QRCONNECT_URL = "https://open.weixin.qq.com/connect/qrconnect";
        public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
        public static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";
        public static final String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token";
    }
}

