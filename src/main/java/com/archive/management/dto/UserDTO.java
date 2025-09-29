package com.archive.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据传输对象
 * 用于API接口的数据传输
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户数据传输对象")
public class UserDTO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "昵称", example = "管理员")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    @Min(value = 0, message = "性别值必须为0、1或2")
    @Max(value = 2, message = "性别值必须为0、1或2")
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime birthday;

    @Schema(description = "部门ID", example = "1")
    @Positive(message = "部门ID必须为正数")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "软件工程师")
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    @Size(max = 50, message = "工号长度不能超过50个字符")
    private String employeeNumber;

    @Schema(description = "入职日期", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime hireDate;

    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1", "2"})
    @NotNull(message = "用户状态不能为空")
    @Min(value = 0, message = "用户状态值必须为0、1或2")
    @Max(value = 2, message = "用户状态值必须为0、1或2")
    private Integer status;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "账户是否未过期", example = "true")
    private Boolean accountNonExpired;

    @Schema(description = "账户是否未锁定", example = "true")
    private Boolean accountNonLocked;

    @Schema(description = "密码是否未过期", example = "true")
    private Boolean credentialsNonExpired;

    @Schema(description = "最后登录时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;

    @Schema(description = "登录次数", example = "10")
    @Min(value = 0, message = "登录次数不能为负数")
    private Integer loginCount;

    @Schema(description = "密码错误次数", example = "0")
    @Min(value = 0, message = "密码错误次数不能为负数")
    private Integer passwordErrorCount;

    @Schema(description = "账户锁定时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockTime;

    @Schema(description = "密码过期时间", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordExpireTime;

    @Schema(description = "备注", example = "系统管理员账户")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建人姓名", example = "系统管理员")
    private String createdByName;

    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "更新人ID", example = "1")
    private Long updatedBy;

    @Schema(description = "更新人姓名", example = "系统管理员")
    private String updatedByName;

    @Schema(description = "版本号", example = "1")
    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "用户角色列表")
    private List<RoleDTO> roles;

    @Schema(description = "用户权限列表")
    private List<PermissionDTO> permissions;

    @Schema(description = "用户标签列表")
    private List<String> tags;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    // 密码相关字段（仅用于创建和更新时传输，不返回给前端）
    @Schema(description = "密码", example = "password123", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonIgnore
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    @Schema(description = "确认密码", example = "password123", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonIgnore
    private String confirmPassword;

    @Schema(description = "旧密码", example = "oldpassword", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonIgnore
    private String oldPassword;

    /**
     * 验证密码和确认密码是否一致
     */
    @AssertTrue(message = "密码和确认密码不一致")
    @JsonIgnore
    public boolean isPasswordMatching() {
        if (password == null && confirmPassword == null) {
            return true;
        }
        return password != null && password.equals(confirmPassword);
    }

    /**
     * 获取用户显示名称
     */
    @Schema(description = "用户显示名称")
    public String getDisplayName() {
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return username;
    }

    /**
     * 获取性别显示文本
     */
    @Schema(description = "性别显示文本")
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
    @Schema(description = "用户状态显示文本")
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
    @Schema(description = "用户是否可用")
    public boolean isAvailable() {
        return enabled != null && enabled 
            && status != null && status == 1
            && (accountNonExpired == null || accountNonExpired)
            && (accountNonLocked == null || accountNonLocked)
            && (credentialsNonExpired == null || credentialsNonExpired);
    }

    /**
     * 判断密码是否即将过期（7天内）
     */
    @Schema(description = "密码是否即将过期")
    public boolean isPasswordExpiringSoon() {
        if (passwordExpireTime == null) {
            return false;
        }
        return passwordExpireTime.isBefore(LocalDateTime.now().plusDays(7));
    }

    /**
     * 判断账户是否被锁定
     */
    @Schema(description = "账户是否被锁定")
    public boolean isAccountLocked() {
        return lockTime != null && lockTime.isAfter(LocalDateTime.now().minusHours(24));
    }
}