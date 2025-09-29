package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 创建登录日志请求DTO
 * 用于记录登录日志时的输入数据
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建登录日志请求")
public class CreateLoginLogRequest {

    @Schema(description = "用户ID", example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "登录状态", example = "1")
    @NotNull(message = "登录状态不能为空")
    private Integer status;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    @NotBlank(message = "客户端IP地址不能为空")
    @Size(max = 50, message = "IP地址长度不能超过50个字符")
    private String clientIp;

    @Schema(description = "登录地点", example = "北京市")
    @Size(max = 100, message = "登录地点长度不能超过100个字符")
    private String location;

    @Schema(description = "浏览器信息", example = "Chrome 120.0.0.0")
    @Size(max = 200, message = "浏览器信息长度不能超过200个字符")
    private String browser;

    @Schema(description = "操作系统", example = "Windows 10")
    @Size(max = 100, message = "操作系统长度不能超过100个字符")
    private String operatingSystem;

    @Schema(description = "设备类型", example = "PC")
    @Size(max = 50, message = "设备类型长度不能超过50个字符")
    private String deviceType;

    @Schema(description = "登录类型", example = "WEB")
    @NotBlank(message = "登录类型不能为空")
    @Size(max = 20, message = "登录类型长度不能超过20个字符")
    private String loginType;

    @Schema(description = "登录方式", example = "PASSWORD")
    @Size(max = 20, message = "登录方式长度不能超过20个字符")
    private String loginMethod;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    private String userAgent;

    @Schema(description = "登录时间", example = "2024-01-01 10:00:00")
    @NotNull(message = "登录时间不能为空")
    private LocalDateTime loginTime;

    @Schema(description = "登出时间", example = "2024-01-01 18:00:00")
    private LocalDateTime logoutTime;

    @Schema(description = "会话时长（分钟）", example = "480")
    private Long sessionDuration;

    @Schema(description = "失败原因", example = "密码错误")
    @Size(max = 200, message = "失败原因长度不能超过200个字符")
    private String failureReason;

    @Schema(description = "备注", example = "正常登录")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}