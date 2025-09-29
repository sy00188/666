package com.archive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色更新请求DTO
 * 封装角色更新所需的输入信息
 */
public class RoleUpdateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String code;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    private String description;

    /**
     * 角色类型（SYSTEM-系统角色，CUSTOM-自定义角色）
     */
    private String type;

    /**
     * 数据权限范围（ALL-全部数据，DEPT-本部门数据，DEPT_AND_SUB-本部门及子部门数据，SELF-仅本人数据，CUSTOM-自定义数据权限）
     */
    private String dataScope;

    /**
     * 自定义数据权限部门ID列表（当dataScope为CUSTOM时使用）
     */
    private List<Long> dataScopeDeptIds;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（ACTIVE-启用，INACTIVE-禁用）
     */
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // 构造函数
    public RoleUpdateRequest() {}

    public RoleUpdateRequest(String name, String code, String description, String type,
                            String dataScope, List<Long> dataScopeDeptIds, List<Long> permissionIds,
                            Integer sortOrder, String status, String remark) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.type = type;
        this.dataScope = dataScope;
        this.dataScopeDeptIds = dataScopeDeptIds;
        this.permissionIds = permissionIds;
        this.sortOrder = sortOrder;
        this.status = status;
        this.remark = remark;
    }

    // Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public List<Long> getDataScopeDeptIds() {
        return dataScopeDeptIds;
    }

    public void setDataScopeDeptIds(List<Long> dataScopeDeptIds) {
        this.dataScopeDeptIds = dataScopeDeptIds;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "RoleUpdateRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", dataScope='" + dataScope + '\'' +
                ", dataScopeDeptIds=" + dataScopeDeptIds +
                ", permissionIds=" + permissionIds +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}