package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应DTO
 * 用于用户查询接口的响应数据
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户响应")
public class UserResponse {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "性别显示文本", example = "男")
    private String genderText;

    @Schema(description = "生日", example = "1990-01-01T00:00:00")
    private LocalDateTime birthday;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    private String employeeNumber;

    @Schema(description = "入职日期", example = "2024-01-01T00:00:00")
    private LocalDateTime hireDate;

    @Schema(description = "用户状态", example = "1")
    private Integer status;

    @Schema(description = "用户状态显示文本", example = "正常")
    private String statusText;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "账户是否未过期", example = "true")
    private Boolean accountNonExpired;

    @Schema(description = "账户是否未锁定", example = "true")
    private Boolean accountNonLocked;

    @Schema(description = "密码是否未过期", example = "true")
    private Boolean credentialsNonExpired;

    @Schema(description = "最后登录时间", example = "2024-01-01T10:00:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;

    @Schema(description = "登录次数", example = "10")
    private Integer loginCount;

    @Schema(description = "密码错误次数", example = "0")
    private Integer passwordErrorCount;

    @Schema(description = "账户锁定时间", example = "2024-01-01T10:00:00")
    private LocalDateTime accountLockTime;

    @Schema(description = "密码过期时间", example = "2024-12-31T23:59:59")
    private LocalDateTime passwordExpireTime;

    @Schema(description = "备注", example = "系统管理员账户")
    private String remark;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "系统管理员")
    private String createUserName;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    @Schema(description = "更新人姓名", example = "系统管理员")
    private String updateUserName;

    @Schema(description = "版本号", example = "1")
    private Long version;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "用户角色列表")
    private List<RoleDTO> roles;

    @Schema(description = "用户权限列表")
    private List<PermissionDTO> permissions;

    @Schema(description = "用户标签列表", example = "[\"VIP\", \"重要客户\"]")
    private List<String> tags;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    /**
     * 获取显示名称
     * 优先级：昵称 > 真实姓名 > 用户名
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        return username;
    }

    /**
     * 获取性别显示文本
     */
    public String getGenderText() {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 0:
                return "未知";
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

    /**
     * 获取用户状态显示文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "正常";
            case 2:
                return "锁定";
            default:
                return "未知";
        }
    }

    /**
     * 判断用户是否可用
     */
    public boolean isAvailable() {
        return enabled != null && enabled 
               && status != null && status == 1
               && (deleted == null || !deleted)
               && (accountNonExpired == null || accountNonExpired)
               && (accountNonLocked == null || accountNonLocked)
               && (credentialsNonExpired == null || credentialsNonExpired);
    }

    /**
     * 判断密码是否即将过期（30天内）
     */
    public boolean isPasswordExpiringSoon() {
        if (passwordExpireTime == null) {
            return false;
        }
        return passwordExpireTime.isBefore(LocalDateTime.now().plusDays(30));
    }

    /**
     * 判断账户是否被锁定
     */
    public boolean isAccountLocked() {
        return accountLockTime != null && accountLockTime.isAfter(LocalDateTime.now());
    }
}