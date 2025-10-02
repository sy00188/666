package com.archive.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 权限查询请求DTO
 * 封装权限查询的条件参数
 */
public class PermissionQueryRequest {

    /**
     * 权限名称（模糊查询）
     */
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String name;

    /**
     * 权限编码（模糊查询）
     */
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    private String code;

    /**
     * 权限类型列表（MENU-菜单权限，BUTTON-按钮权限，API-接口权限）
     */
    private List<String> types;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限路径（模糊查询）
     */
    @Size(max = 200, message = "权限路径长度不能超过200个字符")
    private String path;

    /**
     * HTTP方法列表（GET、POST、PUT、DELETE等）
     */
    private List<String> methods;

    /**
     * 状态列表（ACTIVE-启用，INACTIVE-禁用）
     */
    private List<String> statuses;

    /**
     * 是否显示
     */
    private Boolean visible;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间范围开始
     */
    private String createTimeStart;

    /**
     * 创建时间范围结束
     */
    private String createTimeEnd;

    /**
     * 更新时间范围开始
     */
    private String updateTimeStart;

    /**
     * 更新时间范围结束
     */
    private String updateTimeEnd;

    /**
     * 是否包含子权限
     */
    private Boolean includeChildren;

    /**
     * 排序字段（name-按名称，code-按编码，createTime-按创建时间，updateTime-按更新时间，sortOrder-按排序号）
     */
    private String sortBy;

    /**
     * 排序方向（ASC-升序，DESC-降序）
     */
    private String sortDirection;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize;

    // 构造函数
    public PermissionQueryRequest() {}

    public PermissionQueryRequest(String name, String code, List<String> types, Long parentId,
                                 String path, List<String> methods, List<String> statuses,
                                 Boolean visible, Long creatorId, String createTimeStart,
                                 String createTimeEnd, String updateTimeStart, String updateTimeEnd,
                                 Boolean includeChildren, String sortBy, String sortDirection,
                                 Integer pageNum, Integer pageSize) {
        this.name = name;
        this.code = code;
        this.types = types;
        this.parentId = parentId;
        this.path = path;
        this.methods = methods;
        this.statuses = statuses;
        this.visible = visible;
        this.creatorId = creatorId;
        this.createTimeStart = createTimeStart;
        this.createTimeEnd = createTimeEnd;
        this.updateTimeStart = updateTimeStart;
        this.updateTimeEnd = updateTimeEnd;
        this.includeChildren = includeChildren;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
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

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
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

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getUpdateTimeStart() {
        return updateTimeStart;
    }

    public void setUpdateTimeStart(String updateTimeStart) {
        this.updateTimeStart = updateTimeStart;
    }

    public String getUpdateTimeEnd() {
        return updateTimeEnd;
    }

    public void setUpdateTimeEnd(String updateTimeEnd) {
        this.updateTimeEnd = updateTimeEnd;
    }

    public Boolean getIncludeChildren() {
        return includeChildren;
    }

    public void setIncludeChildren(Boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "PermissionQueryRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", types=" + types +
                ", parentId=" + parentId +
                ", path='" + path + '\'' +
                ", methods=" + methods +
                ", statuses=" + statuses +
                ", visible=" + visible +
                ", creatorId=" + creatorId +
                ", createTimeStart='" + createTimeStart + '\'' +
                ", createTimeEnd='" + createTimeEnd + '\'' +
                ", updateTimeStart='" + updateTimeStart + '\'' +
                ", updateTimeEnd='" + updateTimeEnd + '\'' +
                ", includeChildren=" + includeChildren +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}