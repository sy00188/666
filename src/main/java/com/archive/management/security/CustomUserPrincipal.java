package com.archive.management.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义用户主体类
 * 实现Spring Security的UserDetails接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class CustomUserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 账户是否未过期
     */
    private Boolean accountNonExpired;

    /**
     * 凭据是否未过期
     */
    private Boolean credentialsNonExpired;

    /**
     * 账户是否未锁定
     */
    private Boolean accountNonLocked;

    /**
     * 权限集合
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 密码更新时间
     */
    private Date passwordUpdateTime;

    /**
     * 默认构造函数
     */
    public CustomUserPrincipal() {
    }

    /**
     * 完整构造函数
     */
    public CustomUserPrincipal(Long id, String username, String password, String email, 
                              String realName, String phone, Long departmentId, Long roleId,
                              Boolean enabled, Boolean accountNonExpired, 
                              Boolean credentialsNonExpired, Boolean accountNonLocked,
                              Collection<? extends GrantedAuthority> authorities,
                              Date lastLoginTime, Date passwordUpdateTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.realName = realName;
        this.phone = phone;
        this.departmentId = departmentId;
        this.roleId = roleId;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
        this.lastLoginTime = lastLoginTime;
        this.passwordUpdateTime = passwordUpdateTime;
    }

    /**
     * 简化构造函数
     */
    public CustomUserPrincipal(Long id, String username, String password, 
                              Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = true;
        this.accountNonExpired = true;
        this.credentialsNonExpired = true;
        this.accountNonLocked = true;
    }

    /**
     * 创建用户主体实例
     * @param id 用户ID
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param realName 真实姓名
     * @param phone 电话
     * @param departmentId 部门ID
     * @param roleId 角色ID
     * @param enabled 是否启用
     * @param authorities 权限集合
     * @return 用户主体实例
     */
    public static CustomUserPrincipal create(Long id, String username, String password, 
                                           String email, String realName, String phone,
                                           Long departmentId, Long roleId, Boolean enabled,
                                           Collection<? extends GrantedAuthority> authorities) {
        return new CustomUserPrincipal(
                id, username, password, email, realName, phone,
                departmentId, roleId, enabled, true, true, true,
                authorities, null, null
        );
    }

    // UserDetails接口实现

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired != null ? accountNonExpired : true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked != null ? accountNonLocked : true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired != null ? credentialsNonExpired : true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null ? enabled : true;
    }

    // 自定义方法

    /**
     * 检查用户是否具有指定权限
     * @param authority 权限名称
     * @return 是否具有权限
     */
    public boolean hasAuthority(String authority) {
        if (authorities == null) {
            return false;
        }
        return authorities.stream()
                .anyMatch(grantedAuthority -> 
                    grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * 检查用户是否具有指定权限
     * @param permission 权限代码
     * @return 是否具有权限
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        return getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    /**
     * 检查用户是否具有指定角色
     * @param role 角色名称（不需要ROLE_前缀）
     * @return 是否具有角色
     */
    public boolean hasRole(String role) {
        return hasAuthority("ROLE_" + role);
    }

    /**
     * 获取用户的所有角色代码
     * @return 角色代码集合
     */
    public Set<String> getRoleCodes() {
        return getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5)) // 移除ROLE_前缀
            .collect(Collectors.toSet());
    }

    /**
     * 检查用户是否为管理员
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return hasAuthority("ADMIN") || hasRole("ADMIN");
    }

    /**
     * 检查用户是否为超级管理员
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return roleId != null && roleId.equals(1L);
    }

    /**
     * 检查用户是否为档案管理员
     * @return 是否为档案管理员
     */
    public boolean isArchiveManager() {
        return roleId != null && roleId.equals(2L);
    }

    /**
     * 检查用户是否为部门管理员
     * @return 是否为部门管理员
     */
    public boolean isDepartmentManager() {
        return roleId != null && roleId.equals(3L);
    }

    /**
     * 检查用户是否为普通用户
     * @return 是否为普通用户
     */
    public boolean isNormalUser() {
        return roleId != null && roleId.equals(4L);
    }

    /**
     * 获取用户角色名称
     * @return 角色名称
     */
    public String getRoleName() {
        if (roleId == null) {
            return "未分配角色";
        }
        switch (roleId.intValue()) {
            case 1:
                return "超级管理员";
            case 2:
                return "档案管理员";
            case 3:
                return "部门管理员";
            case 4:
                return "普通用户";
            default:
                return "未知角色";
        }
    }

    /**
     * 检查密码是否需要更新
     * @param passwordMaxAge 密码最大有效期（天）
     * @return 是否需要更新
     */
    public boolean isPasswordUpdateRequired(int passwordMaxAge) {
        if (passwordUpdateTime == null) {
            return true; // 从未更新过密码
        }
        long daysSinceUpdate = (System.currentTimeMillis() - passwordUpdateTime.getTime()) / (24 * 60 * 60 * 1000);
        return daysSinceUpdate >= passwordMaxAge;
    }

    /**
     * 检查账户是否为新用户（首次登录）
     * @return 是否为新用户
     */
    public boolean isNewUser() {
        return lastLoginTime == null;
    }

    /**
     * 获取用户显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        return username;
    }

    // Getter和Setter方法

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getPasswordUpdateTime() {
        return passwordUpdateTime;
    }

    public void setPasswordUpdateTime(Date passwordUpdateTime) {
        this.passwordUpdateTime = passwordUpdateTime;
    }

    // equals和hashCode方法

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserPrincipal that = (CustomUserPrincipal) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", realName='" + realName + '\'' +
                ", phone='" + phone + '\'' +
                ", departmentId=" + departmentId +
                ", roleId=" + roleId +
                ", enabled=" + enabled +
                ", accountNonExpired=" + accountNonExpired +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", authorities=" + authorities +
                ", lastLoginTime=" + lastLoginTime +
                ", passwordUpdateTime=" + passwordUpdateTime +
                '}';
    }
}