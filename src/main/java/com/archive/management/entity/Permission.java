package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
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
import com.archive.management.entity.Role;

/**
 * 权限实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_permission")
public class Permission extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long id;

    // Getter and Setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(min = 2, max = 100, message = "权限名称长度必须在2-100个字符之间")
    @TableField("permission_name")
    private String permissionName;

    // Getter and Setter for permissionName
    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    /**
     * 权限代码
     */
    @NotBlank(message = "权限代码不能为空")
    @Size(min = 2, max = 100, message = "权限代码长度必须在2-100个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9:_-]+$", message = "权限代码只能包含字母、数字、冒号、下划线和横线")
    @TableField("permission_code")
    private String permissionCode;

    // Getter and Setter for permissionCode
    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    /**
     * 权限描述
     */
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    // Getter and Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 权限类型：1-菜单，2-按钮，3-接口，4-数据
     */
    @NotNull(message = "权限类型不能为空")
    @Min(value = 1, message = "权限类型值不正确")
    @Max(value = 4, message = "权限类型值不正确")
    @TableField("permission_type")
    private Integer permissionType;

    // Getter and Setter for permissionType
    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;

    // Getter and Setter for parentId
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 权限路径（层级路径，用逗号分隔）
     */
    @Size(max = 1000, message = "权限路径长度不能超过1000个字符")
    @TableField("permission_path")
    private String permissionPath;

    // Getter and Setter for permissionPath
    public String getPermissionPath() {
        return permissionPath;
    }

    public void setPermissionPath(String permissionPath) {
        this.permissionPath = permissionPath;
    }

    /**
     * 权限级别（从1开始，数字越大级别越深）
     */
    @Min(value = 1, message = "权限级别必须大于0")
    @Max(value = 10, message = "权限级别不能超过10")
    @TableField("permission_level")
    private Integer permissionLevel;

    // Getter and Setter for permissionLevel
    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    /**
     * 菜单URL（仅菜单类型权限有效）
     */
    @Size(max = 500, message = "菜单URL长度不能超过500个字符")
    @TableField("menu_url")
    private String menuUrl;

    // Getter and Setter for menuUrl
    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }

    /**
     * 菜单图标（仅菜单类型权限有效）
     */
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    @TableField("menu_icon")
    private String menuIcon;

    // Getter and Setter for menuIcon
    public String getMenuIcon() {
        return menuIcon;
    }

    public void setMenuIcon(String menuIcon) {
        this.menuIcon = menuIcon;
    }

    /**
     * 菜单组件路径（仅菜单类型权限有效）
     */
    @Size(max = 500, message = "菜单组件路径长度不能超过500个字符")
    @TableField("component_path")
    private String componentPath;

    // Getter and Setter for componentPath
    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    /**
     * 接口URL（仅接口类型权限有效）
     */
    @Size(max = 500, message = "接口URL长度不能超过500个字符")
    @TableField("api_url")
    private String apiUrl;

    // Getter and Setter for apiUrl
    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    /**
     * HTTP方法（仅接口类型权限有效）
     */
    @Size(max = 20, message = "HTTP方法长度不能超过20个字符")
    @TableField("http_method")
    private String httpMethod;

    // Getter and Setter for httpMethod
    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * 权限状态：0-禁用，1-启用
     */
    @NotNull(message = "权限状态不能为空")
    @Min(value = 0, message = "权限状态值不正确")
    @Max(value = 1, message = "权限状态值不正确")
    @TableField("status")
    private Integer status;

    // Getter and Setter for status
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 是否系统内置权限：0-否，1-是
     */
    @TableField("is_system")
    private Integer isSystem;

    // Getter and Setter for isSystem
    public Integer getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Integer isSystem) {
        this.isSystem = isSystem;
    }

    /**
     * 是否在菜单中显示：0-否，1-是
     */
    @TableField("show_in_menu")
    private Integer showInMenu;

    // Getter and Setter for showInMenu
    public Integer getShowInMenu() {
        return showInMenu;
    }

    public void setShowInMenu(Integer showInMenu) {
        this.showInMenu = showInMenu;
    }

    /**
     * 是否缓存：0-否，1-是
     */
    @TableField("keep_alive")
    private Integer keepAlive;

    // Getter and Setter for keepAlive
    public Integer getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能为负数")
    @TableField("sort_order")
    private Integer sortOrder;

    // Getter and Setter for sortOrder
    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    // 添加setSort方法以兼容SystemServiceImpl中的调用
    public void setSort(Integer sort) {
        this.sortOrder = sort;
    }

    // 添加getSort方法以兼容SystemServiceImpl中的调用
    public Integer getSort() {
        return this.sortOrder;
    }

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

    // Getter and Setter for remark
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // ==================== 非数据库字段 ====================

    /**
     * 子权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Permission> children;

    // Getter and Setter for children
    public List<Permission> getChildren() {
        return children;
    }

    public void setChildren(List<Permission> children) {
        this.children = children;
    }

    /**
     * 父权限信息（非数据库字段）
     */
    @TableField(exist = false)
    private Permission parent;

    // Getter and Setter for parent
    public Permission getParent() {
        return parent;
    }

    /**
     * 权限拥有的角色列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Role> roles;

    // Getter and Setter for roles
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * 角色数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer roleCount;

    // Getter and Setter for roleCount
    public Integer getRoleCount() {
        return roleCount;
    }

    public void setRoleCount(Integer roleCount) {
        this.roleCount = roleCount;
    }

    /**
     * 用户数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer userCount;

    // Getter and Setter for userCount
    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    /**
     * 是否选中（用于前端树形结构）（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean checked;

    // Getter and Setter for checked
    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    /**
     * 创建者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String createByName;

    // Getter and Setter for createByName
    public String getCreateByName() {
        return createByName;
    }

    public void setCreateByName(String createByName) {
        this.createByName = createByName;
    }

    /**
     * 更新者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String updateByName;

    // Getter and Setter for updateByName
    public String getUpdateByName() {
        return updateByName;
    }

    public void setUpdateByName(String updateByName) {
        this.updateByName = updateByName;
    }

    // ==================== 业务方法 ====================

    /**
     * 检查权限是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查是否为系统内置权限
     */
    public boolean isSystemPermission() {
        return this.isSystem != null && this.isSystem == 1;
    }

    /**
     * 检查是否为菜单权限
     */
    public boolean isMenuPermission() {
        return this.permissionType != null && this.permissionType == 1;
    }

    /**
     * 检查是否为按钮权限
     */
    public boolean isButtonPermission() {
        return this.permissionType != null && this.permissionType == 2;
    }

    /**
     * 检查是否为接口权限
     */
    public boolean isApiPermission() {
        return this.permissionType != null && this.permissionType == 3;
    }

    /**
     * 检查是否为数据权限
     */
    public boolean isDataPermission() {
        return this.permissionType != null && this.permissionType == 4;
    }

    /**
     * 检查是否为根权限
     */
    public boolean isRootPermission() {
        return this.parentId == null || this.parentId == 0;
    }

    /**
     * 检查是否在菜单中显示
     */
    public boolean isShowInMenu() {
        return this.showInMenu != null && this.showInMenu == 1;
    }

    /**
     * 检查是否缓存
     */
    public boolean isKeepAlive() {
        return this.keepAlive != null && this.keepAlive == 1;
    }

    /**
     * 获取权限类型描述
     */
    public String getPermissionTypeDesc() {
        if (this.permissionType == null) {
            return "未知";
        }
        switch (this.permissionType) {
            case 1:
                return "菜单";
            case 2:
                return "按钮";
            case 3:
                return "接口";
            case 4:
                return "数据";
            default:
                return "未知";
        }
    }

    /**
     * 获取完整的权限路径
     */
    public String getFullPath() {
        if (this.permissionPath == null || this.permissionPath.trim().isEmpty()) {
            return String.valueOf(this.getId());
        }
        return this.permissionPath + "," + this.getId();
    }

    /**
     * 检查是否为指定权限的子权限
     */
    public boolean isChildOf(Permission parent) {
        if (parent == null || this.permissionPath == null) {
            return false;
        }
        return this.permissionPath.contains(String.valueOf(parent.getId()));
    }

    /**
     * 检查是否为指定权限的直接子权限
     */
    public boolean isDirectChildOf(Permission parent) {
        if (parent == null) {
            return false;
        }
        return parent.getId().equals(this.parentId);
    }

    /**
     * 检查HTTP方法是否匹配
     */
    public boolean matchHttpMethod(String method) {
        if (this.httpMethod == null || method == null) {
            return false;
        }
        return this.httpMethod.equalsIgnoreCase(method) || "ALL".equalsIgnoreCase(this.httpMethod);
    }

    /**
     * 检查API URL是否匹配
     */
    public boolean matchApiUrl(String url) {
        if (this.apiUrl == null || url == null) {
            return false;
        }
        // 支持通配符匹配
        String pattern = this.apiUrl.replace("*", ".*");
        return url.matches(pattern);
    }

    /**
     * 添加子权限
     */
    public void addChild(Permission child) {
        if (this.children == null) {
            this.children = new java.util.ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * 移除子权限
     */
    public void removeChild(Permission child) {
        if (this.children != null) {
            this.children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * 设置父权限
     */
    public void setParent(Permission parent) {
        this.parent = parent;
        if (parent != null) {
            this.parentId = parent.getId();
        }
    }

}