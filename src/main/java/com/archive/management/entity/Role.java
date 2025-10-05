package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

// 添加实体类导入
import com.archive.management.entity.Permission;
import com.archive.management.entity.User;

/**
 * 角色实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50个字符之间")
    @TableField("role_name")
    private String roleName;

    /**
     * 角色代码
     */
    @NotBlank(message = "角色代码不能为空")
    @Size(min = 2, max = 50, message = "角色代码长度必须在2-50个字符之间")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色代码只能包含大写字母和下划线")
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    /**
     * 角色类型：1-系统角色，2-业务角色，3-自定义角色
     */
    @NotNull(message = "角色类型不能为空")
    @Min(value = 1, message = "角色类型值不正确")
    @Max(value = 3, message = "角色类型值不正确")
    @TableField("role_type")
    private Integer roleType;

    /**
     * 角色级别（数字越小级别越高）
     */
    @NotNull(message = "角色级别不能为空")
    @Min(value = 1, message = "角色级别必须大于0")
    @Max(value = 999, message = "角色级别不能超过999")
    @TableField("role_level")
    private Integer roleLevel;

    /**
     * 数据权限范围：1-全部数据，2-部门及以下数据，3-部门数据，4-仅本人数据，5-自定义数据
     */
    @NotNull(message = "数据权限范围不能为空")
    @Min(value = 1, message = "数据权限范围值不正确")
    @Max(value = 5, message = "数据权限范围值不正确")
    @TableField("data_scope")
    private Integer dataScope;

    /**
     * 角色状态：0-禁用，1-启用
     */
    @NotNull(message = "角色状态不能为空")
    @Min(value = 0, message = "角色状态值不正确")
    @Max(value = 1, message = "角色状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 是否系统内置角色：0-否，1-是
     */
    @TableField("is_system")
    private Integer isSystem;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能为负数")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

    /**
     * 创建者ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;

    // ==================== 非数据库字段 ====================

    /**
     * 角色权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Permission> permissions;

    /**
     * 角色用户列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<User> users;

    /**
     * 权限ID列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> permissionIds;

    /**
     * 权限代码列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<String> permissionCodes;

    /**
     * 用户数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer userCount;

    /**
     * 权限数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer permissionCount;

    /**
     * 创建者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String createByName;

    /**
     * 更新者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String updateByName;

    // ==================== 业务方法 ====================

    /**
     * 获取角色ID
     * @return 角色ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置角色ID
     * @param id 角色ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 设置更新时间
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 设置更新者ID
     * @param updateBy 更新者ID
     */
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    /**
     * 检查角色是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查是否为系统内置角色
     */
    public boolean isSystemRole() {
        return this.isSystem != null && this.isSystem == 1;
    }

    /**
     * 检查是否为超级管理员角色
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.roleCode);
    }

    /**
     * 检查是否为管理员角色
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.roleCode) || isSuperAdmin();
    }

    /**
     * 获取角色类型描述
     */
    public String getRoleTypeDesc() {
        if (this.roleType == null) {
            return "未知";
        }
        switch (this.roleType) {
            case 1:
                return "系统角色";
            case 2:
                return "业务角色";
            case 3:
                return "自定义角色";
            default:
                return "未知";
        }
    }

    /**
     * 获取数据权限范围描述
     */
    public String getDataScopeDesc() {
        if (this.dataScope == null) {
            return "未知";
        }
        switch (this.dataScope) {
            case 1:
                return "全部数据";
            case 2:
                return "部门及以下数据";
            case 3:
                return "部门数据";
            case 4:
                return "仅本人数据";
            case 5:
                return "自定义数据";
            default:
                return "未知";
        }
    }

    /**
     * 检查角色级别是否高于指定角色
     */
    public boolean isHigherThan(Role other) {
        if (other == null || this.roleLevel == null || other.getRoleLevel() == null) {
            return false;
        }
        return this.roleLevel < other.getRoleLevel();
    }

    /**
     * 检查角色级别是否低于指定角色
     */
    public boolean isLowerThan(Role other) {
        if (other == null || this.roleLevel == null || other.getRoleLevel() == null) {
            return false;
        }
        return this.roleLevel > other.getRoleLevel();
    }

    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        if (permissionCode == null || this.permissionCodes == null) {
            return false;
        }
        return this.permissionCodes.contains(permissionCode);
    }

    /**
     * 检查是否拥有任意一个指定权限
     */
    public boolean hasAnyPermission(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty() || this.permissionCodes == null) {
            return false;
        }
        return permissionCodes.stream().anyMatch(this::hasPermission);
    }

    /**
     * 检查是否拥有所有指定权限
     */
    public boolean hasAllPermissions(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty() || this.permissionCodes == null) {
            return false;
        }
        return permissionCodes.stream().allMatch(this::hasPermission);
    }

    public String getName() {
        return roleName;
    }

    public void setName(String name) {
        this.roleName = name;
    }

    public void setSort(Integer sort) {
        this.sortOrder = sort;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public String getCode() {
        return roleCode;
    }

    public void setCode(String code) {
        this.roleCode = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSort() {
        return sortOrder;
    }

    // 手动添加缺失的getter方法 - 解决Lombok注解处理器问题
    public String getRoleName() {
        return roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRemark() {
        return remark;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public Integer getRoleLevel() {
        return roleLevel;
    }

    public Integer getDataScope() {
        return dataScope;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getIsSystem() {
        return isSystem;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public Integer getVersion() {
        return version;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public Integer getPermissionCount() {
        return permissionCount;
    }

    public String getCreateByName() {
        return createByName;
    }

    public String getUpdateByName() {
        return updateByName;
    }
}