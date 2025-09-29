package com.archive.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限数据传输对象
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
@Schema(description = "权限数据传输对象")
public class PermissionDTO {

    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "权限编码", example = "user:create", required = true)
    @NotBlank(message = "权限编码不能为空")
    @Size(min = 2, max = 100, message = "权限编码长度必须在2-100个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9:_-]+$", message = "权限编码只能包含字母、数字、冒号、下划线和横线")
    private String permissionCode;

    @Schema(description = "权限名称", example = "创建用户", required = true)
    @NotBlank(message = "权限名称不能为空")
    @Size(min = 2, max = 100, message = "权限名称长度必须在2-100个字符之间")
    private String permissionName;

    @Schema(description = "权限描述", example = "允许创建新用户")
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    private String description;

    @Schema(description = "权限类型", example = "1", allowableValues = {"1", "2", "3", "4"})
    @NotNull(message = "权限类型不能为空")
    @Min(value = 1, message = "权限类型值必须为1-4")
    @Max(value = 4, message = "权限类型值必须为1-4")
    private Integer permissionType;

    @Schema(description = "资源类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "资源类型值必须为1-5")
    @Max(value = 5, message = "资源类型值必须为1-5")
    private Integer resourceType;

    @Schema(description = "资源路径", example = "/api/users")
    @Size(max = 200, message = "资源路径长度不能超过200个字符")
    private String resourcePath;

    @Schema(description = "HTTP方法", example = "POST")
    @Size(max = 20, message = "HTTP方法长度不能超过20个字符")
    private String httpMethod;

    @Schema(description = "父权限ID", example = "0")
    @Min(value = 0, message = "父权限ID不能为负数")
    private Long parentId;

    @Schema(description = "父权限名称", example = "用户管理")
    private String parentPermissionName;

    @Schema(description = "权限路径", example = "0,1,2")
    private String permissionPath;

    @Schema(description = "权限级别", example = "1")
    @Min(value = 1, message = "权限级别必须为正数")
    @Max(value = 10, message = "权限级别不能超过10")
    private Integer permissionLevel;

    @Schema(description = "排序号", example = "1")
    @Min(value = 0, message = "排序号不能为负数")
    private Integer sortOrder;

    @Schema(description = "菜单图标", example = "user")
    @Size(max = 50, message = "菜单图标长度不能超过50个字符")
    private String icon;

    @Schema(description = "菜单路由", example = "/users")
    @Size(max = 200, message = "菜单路由长度不能超过200个字符")
    private String route;

    @Schema(description = "组件路径", example = "views/user/UserList")
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    @Schema(description = "是否外部链接", example = "false")
    private Boolean external;

    @Schema(description = "外部链接地址", example = "https://example.com")
    @Size(max = 500, message = "外部链接地址长度不能超过500个字符")
    private String externalUrl;

    @Schema(description = "是否缓存", example = "true")
    private Boolean cached;

    @Schema(description = "是否隐藏", example = "false")
    private Boolean hidden;

    @Schema(description = "是否系统内置", example = "false")
    @NotNull(message = "系统内置标识不能为空")
    private Boolean systemBuiltIn;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "权限标签")
    private List<String> tags;

    @Schema(description = "权限依赖")
    private List<String> dependencies;

    @Schema(description = "权限约束条件")
    private String constraints;

    @Schema(description = "数据权限配置")
    private String dataPermissionConfig;

    @Schema(description = "备注", example = "用户创建权限")
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

    @Schema(description = "角色数量", example = "5")
    private Long roleCount;

    @Schema(description = "用户数量", example = "10")
    private Long userCount;

    @Schema(description = "子权限列表")
    private List<PermissionDTO> children;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    /**
     * 获取权限类型显示文本
     */
    @Schema(description = "权限类型显示文本")
    public String getPermissionTypeText() {
        if (permissionType == null) {
            return "未知";
        }
        switch (permissionType) {
            case 1:
                return "菜单权限";
            case 2:
                return "按钮权限";
            case 3:
                return "接口权限";
            case 4:
                return "数据权限";
            default:
                return "未知";
        }
    }

    /**
     * 获取资源类型显示文本
     */
    @Schema(description = "资源类型显示文本")
    public String getResourceTypeText() {
        if (resourceType == null) {
            return "未设置";
        }
        switch (resourceType) {
            case 1:
                return "页面";
            case 2:
                return "按钮";
            case 3:
                return "接口";
            case 4:
                return "文件";
            case 5:
                return "数据";
            default:
                return "未设置";
        }
    }

    /**
     * 判断是否为根权限
     */
    @Schema(description = "是否为根权限")
    public boolean isRootPermission() {
        return parentId == null || parentId == 0;
    }

    /**
     * 判断是否有子权限
     */
    @Schema(description = "是否有子权限")
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 判断权限是否可用
     */
    @Schema(description = "权限是否可用")
    public boolean isAvailable() {
        return enabled != null && enabled && (deleted == null || !deleted);
    }

    /**
     * 判断是否可以删除
     */
    @Schema(description = "是否可以删除")
    public boolean isDeletable() {
        return systemBuiltIn == null || !systemBuiltIn;
    }

    /**
     * 判断是否可以编辑
     */
    @Schema(description = "是否可以编辑")
    public boolean isEditable() {
        return systemBuiltIn == null || !systemBuiltIn;
    }

    /**
     * 判断是否为菜单权限
     */
    @Schema(description = "是否为菜单权限")
    public boolean isMenuPermission() {
        return permissionType != null && permissionType == 1;
    }

    /**
     * 判断是否为按钮权限
     */
    @Schema(description = "是否为按钮权限")
    public boolean isButtonPermission() {
        return permissionType != null && permissionType == 2;
    }

    /**
     * 判断是否为接口权限
     */
    @Schema(description = "是否为接口权限")
    public boolean isApiPermission() {
        return permissionType != null && permissionType == 3;
    }

    /**
     * 判断是否为数据权限
     */
    @Schema(description = "是否为数据权限")
    public boolean isDataPermission() {
        return permissionType != null && permissionType == 4;
    }

    /**
     * 获取权限层级深度
     */
    @Schema(description = "权限层级深度")
    public int getDepth() {
        if (permissionPath == null || permissionPath.trim().isEmpty()) {
            return 0;
        }
        return permissionPath.split(",").length - 1;
    }

    /**
     * 判断是否为指定权限的子权限
     */
    @Schema(description = "是否为指定权限的子权限")
    public boolean isChildOf(Long parentPermissionId) {
        if (permissionPath == null || parentPermissionId == null) {
            return false;
        }
        return permissionPath.contains("," + parentPermissionId + ",") || 
               permissionPath.startsWith(parentPermissionId + ",");
    }

    /**
     * 判断是否为指定权限的父权限
     */
    @Schema(description = "是否为指定权限的父权限")
    public boolean isParentOf(String childPermissionPath) {
        if (id == null || childPermissionPath == null) {
            return false;
        }
        return childPermissionPath.contains("," + id + ",") || 
               childPermissionPath.startsWith(id + ",");
    }

    /**
     * 判断是否匹配HTTP请求
     */
    @Schema(description = "是否匹配HTTP请求")
    public boolean matchesRequest(String requestPath, String requestMethod) {
        if (resourcePath == null || requestPath == null) {
            return false;
        }
        
        boolean pathMatches = resourcePath.equals(requestPath) || 
                             resourcePath.endsWith("/**") && requestPath.startsWith(resourcePath.substring(0, resourcePath.length() - 3));
        
        boolean methodMatches = httpMethod == null || httpMethod.equalsIgnoreCase(requestMethod);
        
        return pathMatches && methodMatches;
    }
}