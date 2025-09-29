package com.archive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 权限创建请求DTO
 * 封装权限创建所需的输入信息
 */
public class PermissionCreateRequest {

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String name;

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    private String code;

    /**
     * 权限描述
     */
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    private String description;

    /**
     * 权限类型（MENU-菜单权限，BUTTON-按钮权限，API-接口权限）
     */
    private String type;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限路径（菜单路径或API路径）
     */
    @Size(max = 200, message = "权限路径长度不能超过200个字符")
    private String path;

    /**
     * HTTP方法（GET、POST、PUT、DELETE等，API权限使用）
     */
    @Size(max = 10, message = "HTTP方法长度不能超过10个字符")
    private String method;

    /**
     * 图标（菜单权限使用）
     */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否显示（菜单权限使用）
     */
    private Boolean visible;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // 构造函数
    public PermissionCreateRequest() {}

    public PermissionCreateRequest(String name, String code, String description, String type,
                                  Long parentId, String path, String method, String icon,
                                  Integer sortOrder, Boolean visible, String remark) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.type = type;
        this.parentId = parentId;
        this.path = path;
        this.method = method;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.visible = visible;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PermissionCreateRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", parentId=" + parentId +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", icon='" + icon + '\'' +
                ", sortOrder=" + sortOrder +
                ", visible=" + visible +
                ", remark='" + remark + '\'' +
                '}';
    }
}