package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 权限组实体类
 * 对应数据库表：sys_permission_group
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_permission_group")
public class PermissionGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 权限组ID
     */
    @TableId(value = "group_id", type = IdType.AUTO)
    private Long groupId;

    /**
     * 权限组编码（唯一标识）
     */
    @NotBlank(message = "权限组编码不能为空")
    @Size(max = 50, message = "权限组编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "权限组编码只能包含大写字母、数字和下划线，且必须以大写字母或下划线开头")
    @TableField("group_code")
    private String groupCode;

    /**
     * 权限组名称
     */
    @NotBlank(message = "权限组名称不能为空")
    @Size(max = 100, message = "权限组名称长度不能超过100个字符")
    @TableField("group_name")
    private String groupName;

    /**
     * 父权限组ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 权限组层级
     */
    @NotNull(message = "权限组层级不能为空")
    @Min(value = 1, message = "权限组层级必须大于0")
    @Max(value = 10, message = "权限组层级不能超过10级")
    @TableField("group_level")
    private Integer groupLevel;

    /**
     * 权限组路径（用于快速查找层级关系）
     * 格式：/1/2/3/
     */
    @Size(max = 500, message = "权限组路径长度不能超过500个字符")
    @TableField("group_path")
    private String groupPath;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 权限组类型
     * SYSTEM-系统权限组，BUSINESS-业务权限组，CUSTOM-自定义权限组
     */
    @NotBlank(message = "权限组类型不能为空")
    @Pattern(regexp = "^(SYSTEM|BUSINESS|CUSTOM)$", message = "权限组类型只能是SYSTEM、BUSINESS或CUSTOM")
    @TableField("group_type")
    private String groupType;

    /**
     * 权限组状态
     * 0-禁用，1-启用
     */
    @NotNull(message = "权限组状态不能为空")
    @Min(value = 0, message = "权限组状态值不正确")
    @Max(value = 1, message = "权限组状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 是否可继承
     * 0-不可继承，1-可继承
     */
    @NotNull(message = "继承标识不能为空")
    @Min(value = 0, message = "继承标识值不正确")
    @Max(value = 1, message = "继承标识值不正确")
    @TableField("inheritable")
    private Integer inheritable;

    /**
     * 权限组描述
     */
    @Size(max = 500, message = "权限组描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    /**
     * 权限组图标
     */
    @Size(max = 100, message = "权限组图标长度不能超过100个字符")
    @TableField("icon")
    private String icon;

    /**
     * 权限组颜色
     */
    @Size(max = 20, message = "权限组颜色长度不能超过20个字符")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$|^$", message = "权限组颜色格式不正确，应为#RRGGBB格式")
    @TableField("color")
    private String color;

    // ==================== 非数据库字段 ====================

    /**
     * 父权限组信息（关联查询时使用）
     */
    @TableField(exist = false)
    private PermissionGroup parent;

    /**
     * 父权限组名称（关联查询时使用）
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 子权限组列表（树形结构时使用）
     */
    @TableField(exist = false)
    private List<PermissionGroup> children;

    /**
     * 权限列表（关联查询时使用）
     */
    @TableField(exist = false)
    private List<Permission> permissions;

    /**
     * 权限ID列表（用于权限分配）
     */
    @TableField(exist = false)
    private List<Long> permissionIds;

    /**
     * 权限数量（统计信息）
     */
    @TableField(exist = false)
    private Integer permissionCount;

    /**
     * 创建人姓名（关联查询时使用）
     */
    @TableField(exist = false)
    private String createUserName;

    // ==================== 业务方法 ====================

    /**
     * 判断权限组是否启用
     * 
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 判断是否可继承
     * 
     * @return true-可继承，false-不可继承
     */
    public boolean isInheritable() {
        return inheritable != null && inheritable == 1;
    }

    /**
     * 判断是否为根权限组
     * 
     * @return true-根权限组，false-非根权限组
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }

    /**
     * 判断是否为叶子权限组（没有子权限组）
     * 
     * @return true-叶子权限组，false-非叶子权限组
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * 启用权限组
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 禁用权限组
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 设置为可继承
     */
    public void setInheritable() {
        this.inheritable = 1;
    }

    /**
     * 设置为不可继承
     */
    public void setNotInheritable() {
        this.inheritable = 0;
    }

    /**
     * 获取权限组类型的中文描述
     * 
     * @return 中文描述
     */
    public String getGroupTypeDesc() {
        if (groupType == null) {
            return "未知";
        }
        return switch (groupType) {
            case "SYSTEM" -> "系统权限组";
            case "BUSINESS" -> "业务权限组";
            case "CUSTOM" -> "自定义权限组";
            default -> "未知类型";
        };
    }

    /**
     * 获取状态的中文描述
     * 
     * @return 中文描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "启用" : "禁用";
    }

    /**
     * 构建权限组路径
     * 
     * @param parentPath 父权限组路径
     */
    public void buildGroupPath(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            this.groupPath = "/" + this.groupId + "/";
        } else {
            this.groupPath = parentPath + this.groupId + "/";
        }
    }

    /**
     * 获取所有祖先权限组ID
     * 
     * @return 祖先权限组ID列表
     */
    public List<Long> getAncestorIds() {
        if (groupPath == null || groupPath.isEmpty()) {
            return List.of();
        }
        
        String[] pathParts = groupPath.split("/");
        return java.util.Arrays.stream(pathParts)
                .filter(part -> !part.isEmpty() && !part.equals(String.valueOf(groupId)))
                .map(Long::valueOf)
                .toList();
    }

    /**
     * 判断是否为指定权限组的祖先
     * 
     * @param descendantPath 后代权限组路径
     * @return true-是祖先，false-不是祖先
     */
    public boolean isAncestorOf(String descendantPath) {
        return descendantPath != null && 
               groupPath != null && 
               descendantPath.startsWith(groupPath) && 
               !descendantPath.equals(groupPath);
    }

    /**
     * 判断是否为指定权限组的后代
     * 
     * @param ancestorPath 祖先权限组路径
     * @return true-是后代，false-不是后代
     */
    public boolean isDescendantOf(String ancestorPath) {
        return groupPath != null && 
               ancestorPath != null && 
               groupPath.startsWith(ancestorPath) && 
               !groupPath.equals(ancestorPath);
    }
}