package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 * 对应数据库表：sys_login_log
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_login_log")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 登录类型(password/sso/ldap)
     */
    @TableField("login_type")
    private String loginType;

    /**
     * 客户端IP
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 客户端位置
     */
    @TableField("client_location")
    private String clientLocation;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 状态(0:失败 1:成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("login_time")
    private LocalDateTime loginTime;

    // ==================== 构造方法 ====================

    public LoginLog() {
        this.loginTime = LocalDateTime.now();
        this.loginType = "password";
    }

    public LoginLog(String username, String clientIp) {
        this();
        this.username = username;
        this.clientIp = clientIp;
    }

    public LoginLog(Long userId, String username, String clientIp) {
        this(username, clientIp);
        this.userId = userId;
    }

    // ==================== 业务方法 ====================

    /**
     * 检查登录是否成功
     */
    public boolean isLoginSuccess() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查登录是否失败
     */
    public boolean isLoginFailure() {
        return this.status != null && this.status == 0;
    }

    /**
     * 设置登录成功
     */
    public LoginLog setSuccess() {
        this.status = 1;
        this.errorMsg = null;
        return this;
    }

    /**
     * 设置登录失败
     */
    public LoginLog setFailure(String errorMsg) {
        this.status = 0;
        this.errorMsg = errorMsg;
        return this;
    }

    /**
     * 获取登录状态描述
     */
    public String getStatusDesc() {
        if (this.status == null) {
            return "未知";
        }
        return this.status == 1 ? "成功" : "失败";
    }

    /**
     * 获取登录类型描述
     */
    public String getLoginTypeDesc() {
        if (this.loginType == null) {
            return "未知";
        }
        switch (this.loginType) {
            case "password":
                return "密码登录";
            case "sso":
                return "单点登录";
            case "ldap":
                return "LDAP登录";
            default:
                return "其他";
        }
    }

    /**
     * 设置用户代理信息并解析浏览器和操作系统
     */
    public LoginLog setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        if (userAgent != null && !userAgent.isEmpty()) {
            // 简单的浏览器检测
            if (userAgent.contains("Chrome")) {
                this.browser = "Chrome";
            } else if (userAgent.contains("Firefox")) {
                this.browser = "Firefox";
            } else if (userAgent.contains("Safari")) {
                this.browser = "Safari";
            } else if (userAgent.contains("Edge")) {
                this.browser = "Edge";
            } else {
                this.browser = "Other";
            }

            // 简单的操作系统检测
            if (userAgent.contains("Windows")) {
                this.os = "Windows";
            } else if (userAgent.contains("Mac")) {
                this.os = "macOS";
            } else if (userAgent.contains("Linux")) {
                this.os = "Linux";
            } else if (userAgent.contains("Android")) {
                this.os = "Android";
            } else if (userAgent.contains("iOS")) {
                this.os = "iOS";
            } else {
                this.os = "Other";
            }
        }
        return this;
    }

    /**
     * 检查是否为移动端登录
     */
    public boolean isMobileLogin() {
        return this.userAgent != null && 
               (this.userAgent.contains("Mobile") || 
                this.userAgent.contains("Android") || 
                this.userAgent.contains("iPhone"));
    }

    /**
     * 检查是否为本地登录
     */
    public boolean isLocalLogin() {
        return this.clientIp != null && 
               (this.clientIp.equals("127.0.0.1") || 
                this.clientIp.equals("localhost") || 
                this.clientIp.startsWith("192.168.") || 
                this.clientIp.startsWith("10.") || 
                this.clientIp.startsWith("172."));
    }

    /**
     * 创建成功登录日志
     */
    public static LoginLog createSuccessLog(Long userId, String username, String clientIp, String userAgent) {
        LoginLog log = new LoginLog(userId, username, clientIp);
        log.setUserAgent(userAgent);
        log.setSuccess();
        return log;
    }

    /**
     * 创建失败登录日志
     */
    public static LoginLog createFailureLog(String username, String clientIp, String userAgent, String errorMsg) {
        LoginLog log = new LoginLog(username, clientIp);
        log.setUserAgent(userAgent);
        log.setFailure(errorMsg);
        return log;
    }

    @Override
    public String toString() {
        return "LoginLog{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", loginType='" + loginType + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", status=" + status +
                ", loginTime=" + loginTime +
                '}';
    }
}