package com.archive.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 部门查询请求DTO
 * 封装部门查询的条件参数
 */
public class DepartmentQueryRequest {

    /**
     * 部门名称（模糊查询）
     */
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String name;

    /**
     * 部门编码（模糊查询）
     */
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String code;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 负责人ID
     */
    private Long managerId;

    /**
     * 负责人姓名（模糊查询）
     */
    @Size(max = 50, message = "负责人姓名长度不能超过50个字符")
    private String managerName;

    /**
     * 部门状态（ACTIVE-启用，INACTIVE-禁用）
     */
    private String status;

    /**
     * 部门状态列表（支持多状态查询）
     */
    private List<String> statuses;

    /**
     * 创建时间开始
     */
    private String createTimeStart;

    /**
     * 创建时间结束
     */
    private String createTimeEnd;

    /**
     * 是否包含子部门
     */
    private Boolean includeChildren;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向（ASC-升序，DESC-降序）
     */
    private String sortOrder;

    // 构造函数
    public DepartmentQueryRequest() {}

    public DepartmentQueryRequest(String name, String code, Long parentId, Long managerId,
                                 String managerName, String status, String createTimeStart,
                                 String createTimeEnd, Boolean includeChildren, Integer pageNum,
                                 Integer pageSize, String sortBy, String sortOrder) {
        this.name = name;
        this.code = code;
        this.parentId = parentId;
        this.managerId = managerId;
        this.managerName = managerName;
        this.status = status;
        this.createTimeStart = createTimeStart;
        this.createTimeEnd = createTimeEnd;
        this.includeChildren = includeChildren;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getIncludeChildren() {
        return includeChildren;
    }

    public void setIncludeChildren(Boolean includeChildren) {
        this.includeChildren = includeChildren;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    // 添加getStatuses方法
    public List<String> getStatuses() {
        return this.statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    @Override
    public String toString() {
        return "DepartmentQueryRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentId=" + parentId +
                ", managerId=" + managerId +
                ", managerName='" + managerName + '\'' +
                ", status='" + status + '\'' +
                ", createTimeStart='" + createTimeStart + '\'' +
                ", createTimeEnd='" + createTimeEnd + '\'' +
                ", includeChildren=" + includeChildren +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                '}';
    }
}