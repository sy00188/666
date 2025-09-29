package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 添加Role和Permission的导入
import com.archive.management.entity.Role;
import com.archive.management.entity.Permission;

/**
 * 用户实体类
 * 对应数据库表：sys_user
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名（登录名）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @TableField("username")
    private String username;

    /**
     * 密码（加密存储）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @JsonIgnore
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @TableField("real_name")
    private String realName;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @TableField("phone")
    private String phone;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @TableField("avatar")
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    @TableField("gender")
    private Integer gender;

    /**
     * 工号
     */
    @NotBlank(message = "工号不能为空")
    @Size(max = 50, message = "工号长度不能超过50个字符")
    @TableField("employee_no")
    private String employeeNo;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 职位
     */
    @Size(max = 100, message = "职位长度不能超过100个字符")
    @TableField("position")
    private String position;

    /**
     * 用户状态：0-禁用，1-启用
     */
    @NotNull(message = "用户状态不能为空")
    @Min(value = 0, message = "用户状态值不正确")
    @Max(value = 1, message = "用户状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 账户是否未过期：0-过期，1-未过期
     */
    @TableField("account_non_expired")
    private Integer accountNonExpired;

    /**
     * 账户是否未锁定：0-锁定，1-未锁定
     */
    @TableField("account_non_locked")
    private Integer accountNonLocked;

    /**
     * 凭证是否未过期：0-过期，1-未过期
     */
    @TableField("credentials_non_expired")
    private Integer credentialsNonExpired;

    /**
     * 账户是否启用：0-禁用，1-启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Size(max = 50, message = "最后登录IP长度不能超过50个字符")
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @Min(value = 0, message = "登录失败次数不能为负数")
    @TableField("login_failure_count")
    private Integer loginFailureCount;

    /**
     * 账户锁定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * 密码修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("password_change_time")
    private LocalDateTime passwordChangeTime;

    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

    // 移除继承自BaseEntity的字段，避免重复定义

    /**
     * 非持久化字段 - 角色列表
     */
    @TableField(exist = false)
    private List<Role> roles;

    /**
     * 非持久化字段 - 权限列表
     */
    @TableField(exist = false)
    private List<Permission> permissions;

    /**
     * 非持久化字段 - 部门名称
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 非持久化字段 - 角色名称列表
     */
    @TableField(exist = false)
    private List<String> roleNames;

    /**
     * 非持久化字段 - 权限代码列表
     */
    @TableField(exist = false)
    private List<String> permissionCodes;

    // ==================== 业务方法 ====================

    /**
     * 判断用户是否启用
     */
    public boolean isEnabled() {
        return this.enabled != null && this.enabled == 1;
    }

    /**
     * 判断账户是否未过期
     */
    public boolean isAccountNonExpired() {
        return this.accountNonExpired != null && this.accountNonExpired == 1;
    }

    /**
     * 判断账户是否未锁定
     */
    public boolean isAccountNonLocked() {
        return this.accountNonLocked != null && this.accountNonLocked == 1;
    }

    /**
     * 判断凭证是否未过期
     */
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired != null && this.credentialsNonExpired == 1;
    }

    /**
     * 判断用户状态是否正常
     */
    public boolean isStatusNormal() {
        return this.status != null && this.status == 1;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return this.realName != null ? this.realName : this.username;
    }

    /**
     * 判断是否需要修改密码
     */
    public boolean needChangePassword() {
        if (this.passwordChangeTime == null) {
            return true;
        }
        // 密码超过90天需要修改
        return this.passwordChangeTime.isBefore(LocalDateTime.now().minusDays(90));
    }

    /**
     * 判断账户是否被锁定
     */
    public boolean isLocked() {
        if (this.lockTime == null) {
            return false;
        }
        // 锁定时间超过24小时自动解锁
        return this.lockTime.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailureCount() {
        this.loginFailureCount = 0;
        this.lockTime = null;
    }

    /**
     * 增加登录失败次数
     */
    public void increaseLoginFailureCount() {
        this.loginFailureCount = (this.loginFailureCount == null ? 0 : this.loginFailureCount) + 1;
        // 失败次数达到5次时锁定账户
        if (this.loginFailureCount >= 5) {
            this.lockTime = LocalDateTime.now();
        }
    }

    /**
     * 更新最后登录信息
     */
    public void updateLastLoginInfo(String loginIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = loginIp;
        this.resetLoginFailureCount();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return this.realName;
    }
}