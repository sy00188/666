package com.archive.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色查询请求DTO
 * 封装角色查询的条件参数
 */
public class RoleQueryRequest {

    /**
     * 角色名称（模糊查询）
     */
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String name;

    /**
     * 角色编码（模糊查询）
     */
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String code;

    /**
     * 角色类型列表（SYSTEM-系统角色，CUSTOM-自定义角色）
     */
    private List<String> types;

    /**
     * 状态列表（ACTIVE-启用，INACTIVE-禁用）
     */
    private List<String> statuses;

    /**
     * 数据权限范围列表（ALL-全部数据，DEPT-本部门数据，DEPT_AND_SUB-本部门及子部门数据，SELF-仅本人数据，CUSTOM-自定义数据权限）
     */
    private List<String> dataScopes;

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
    public RoleQueryRequest() {}

    public RoleQueryRequest(String name, String code, List<String> types, List<String> statuses,
                           List<String> dataScopes, Long creatorId, String createTimeStart,
                           String createTimeEnd, String updateTimeStart, String updateTimeEnd,
                           String sortBy, String sortDirection, Integer pageNum, Integer pageSize) {
        this.name = name;
        this.code = code;
        this.types = types;
        this.statuses = statuses;
        this.dataScopes = dataScopes;
        this.creatorId = creatorId;
        this.createTimeStart = createTimeStart;
        this.createTimeEnd = createTimeEnd;
        this.updateTimeStart = updateTimeStart;
        this.updateTimeEnd = updateTimeEnd;
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

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    public List<String> getDataScopes() {
        return dataScopes;
    }

    public void setDataScopes(List<String> dataScopes) {
        this.dataScopes = dataScopes;
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
        return "RoleQueryRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", types=" + types +
                ", statuses=" + statuses +
                ", dataScopes=" + dataScopes +
                ", creatorId=" + creatorId +
                ", createTimeStart='" + createTimeStart + '\'' +
                ", createTimeEnd='" + createTimeEnd + '\'' +
                ", updateTimeStart='" + updateTimeStart + '\'' +
                ", updateTimeEnd='" + updateTimeEnd + '\'' +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}