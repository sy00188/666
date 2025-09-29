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
 * 角色数据传输对象
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
@Schema(description = "角色数据传输对象")
public class RoleDTO {

    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "角色编码", example = "ADMIN", required = true)
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须在2-50个字符之间")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "角色编码只能包含大写字母、数字和下划线")
    private String roleCode;

    @Schema(description = "角色名称", example = "系统管理员", required = true)
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 100, message = "角色名称长度必须在2-100个字符之间")
    private String roleName;

    @Schema(description = "角色描述", example = "系统管理员角色，拥有所有权限")
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    private String description;

    @Schema(description = "角色类型", example = "1", allowableValues = {"1", "2", "3"})
    @NotNull(message = "角色类型不能为空")
    @Min(value = 1, message = "角色类型值必须为1、2或3")
    @Max(value = 3, message = "角色类型值必须为1、2或3")
    private Integer roleType;

    @Schema(description = "角色级别", example = "1")
    @Min(value = 1, message = "角色级别必须为正数")
    @Max(value = 10, message = "角色级别不能超过10")
    private Integer roleLevel;

    @Schema(description = "父角色ID", example = "0")
    @Min(value = 0, message = "父角色ID不能为负数")
    private Long parentId;

    @Schema(description = "父角色名称", example = "超级管理员")
    private String parentRoleName;

    @Schema(description = "角色路径", example = "0,1,2")
    private String rolePath;

    @Schema(description = "排序号", example = "1")
    @Min(value = 0, message = "排序号不能为负数")
    private Integer sortOrder;

    @Schema(description = "是否系统内置", example = "false")
    @NotNull(message = "系统内置标识不能为空")
    private Boolean systemBuiltIn;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "数据权限范围", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "数据权限范围值必须为1-5")
    @Max(value = 5, message = "数据权限范围值必须为1-5")
    private Integer dataScope;

    @Schema(description = "数据权限部门列表")
    private List<Long> dataScopeDeptIds;

    @Schema(description = "角色权限列表")
    private List<String> permissions;

    @Schema(description = "菜单权限列表")
    private List<Long> menuIds;

    @Schema(description = "备注", example = "系统管理员角色")
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

    @Schema(description = "用户数量", example = "10")
    private Long userCount;

    @Schema(description = "权限数量", example = "20")
    private Long permissionCount;

    @Schema(description = "子角色列表")
    private List<RoleDTO> children;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    /**
     * 获取角色类型显示文本
     */
    @Schema(description = "角色类型显示文本")
    public String getRoleTypeText() {
        if (roleType == null) {
            return "未知";
        }
        switch (roleType) {
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
     * 获取数据权限范围显示文本
     */
    @Schema(description = "数据权限范围显示文本")
    public String getDataScopeText() {
        if (dataScope == null) {
            return "未设置";
        }
        switch (dataScope) {
            case 1:
                return "全部数据权限";
            case 2:
                return "自定义数据权限";
            case 3:
                return "本部门数据权限";
            case 4:
                return "本部门及以下数据权限";
            case 5:
                return "仅本人数据权限";
            default:
                return "未设置";
        }
    }

    /**
     * 判断是否为根角色
     */
    @Schema(description = "是否为根角色")
    public boolean isRootRole() {
        return parentId == null || parentId == 0;
    }

    /**
     * 判断是否有子角色
     */
    @Schema(description = "是否有子角色")
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 判断角色是否可用
     */
    @Schema(description = "角色是否可用")
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
     * 获取角色层级深度
     */
    @Schema(description = "角色层级深度")
    public int getDepth() {
        if (rolePath == null || rolePath.trim().isEmpty()) {
            return 0;
        }
        return rolePath.split(",").length - 1;
    }

    /**
     * 判断是否为指定角色的子角色
     */
    @Schema(description = "是否为指定角色的子角色")
    public boolean isChildOf(Long parentRoleId) {
        if (rolePath == null || parentRoleId == null) {
            return false;
        }
        return rolePath.contains("," + parentRoleId + ",") || rolePath.startsWith(parentRoleId + ",");
    }

    /**
     * 判断是否为指定角色的父角色
     */
    @Schema(description = "是否为指定角色的父角色")
    public boolean isParentOf(String childRolePath) {
        if (id == null || childRolePath == null) {
            return false;
        }
        return childRolePath.contains("," + id + ",") || childRolePath.startsWith(id + ",");
    }
}