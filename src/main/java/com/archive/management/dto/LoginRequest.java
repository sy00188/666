package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @Schema(description = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;

    @Schema(description = "验证码", required = true, example = "ABCD")
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度必须在4-6个字符之间")
    private String captcha;

    @Schema(description = "验证码ID", required = true, example = "captcha_123456789")
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginRequest(String username, String password, Boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public LoginRequest(String username, String password, String captcha, String captchaId) {
        this.username = username;
        this.password = password;
        this.captcha = captcha;
        this.captchaId = captchaId;
    }

    public LoginRequest(String username, String password, Boolean rememberMe, String captcha, String captchaId) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
        this.captcha = captcha;
        this.captchaId = captchaId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", rememberMe=" + rememberMe +
                ", captcha='" + captcha + '\'' +
                ", captchaId='" + captchaId + '\'' +
                '}';
    }
}