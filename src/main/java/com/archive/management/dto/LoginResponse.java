package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * 登录响应DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "令牌过期时间（秒）", example = "3600")
    private Object expiresIn;

    @Schema(description = "用户信息")
    private Map<String, Object> user;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, String tokenType, Object expiresIn, Map<String, Object> user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Object getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Object expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public void setUser(Map<String, Object> user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(20, refreshToken.length())) + "..." : null) + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", user=" + user +
                '}';
    }
}