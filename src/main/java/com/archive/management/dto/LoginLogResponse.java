package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志响应DTO
 * 用于登录日志查询接口的响应数据
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录日志响应")
public class LoginLogResponse {

    @Schema(description = "登录日志ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "登录状态", example = "1")
    private Integer status;

    @Schema(description = "登录状态描述", example = "成功")
    private String statusDesc;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "登录地点", example = "北京市")
    private String location;

    @Schema(description = "浏览器信息", example = "Chrome 120.0.0.0")
    private String browser;

    @Schema(description = "操作系统", example = "Windows 10")
    private String operatingSystem;

    @Schema(description = "设备类型", example = "PC")
    private String deviceType;

    @Schema(description = "登录类型", example = "WEB")
    private String loginType;

    @Schema(description = "登录方式", example = "PASSWORD")
    private String loginMethod;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "登录时间", example = "2024-01-01 10:00:00")
    private LocalDateTime loginTime;

    @Schema(description = "登出时间", example = "2024-01-01 18:00:00")
    private LocalDateTime logoutTime;

    @Schema(description = "会话时长（分钟）", example = "480")
    private Long sessionDuration;

    @Schema(description = "失败原因", example = "密码错误")
    private String failureReason;

    @Schema(description = "备注", example = "正常登录")
    private String remark;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01 10:00:00")
    private LocalDateTime updateTime;
}