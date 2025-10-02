package com.archive.management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 系统配置查询请求DTO
 * 封装系统配置查询的条件参数
 */
public class ConfigQueryRequest {

    /**
     * 配置键（模糊查询）
     */
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;

    /**
     * 配置名称（模糊查询）
     */
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;

    /**
     * 配置分组列表
     */
    private List<String> configGroups;

    /**
     * 配置类型列表（STRING-字符串，NUMBER-数字，BOOLEAN-布尔值，JSON-JSON对象，ARRAY-数组）
     */
    private List<String> configTypes;

    /**
     * 是否系统内置配置
     */
    private Boolean isSystem;

    /**
     * 是否加密存储
     */
    private Boolean encrypted;

    /**
     * 是否可编辑
     */
    private Boolean editable;

    /**
     * 状态列表（ACTIVE-启用，INACTIVE-禁用）
     */
    private List<String> statuses;

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
     * 配置值（模糊查询）
     */
    @Size(max = 500, message = "配置值查询长度不能超过500个字符")
    private String configValue;

    /**
     * 描述（模糊查询）
     */
    @Size(max = 200, message = "描述查询长度不能超过200个字符")
    private String description;

    /**
     * 排序字段（configKey-按配置键，configName-按配置名称，configGroup-按分组，createTime-按创建时间，updateTime-按更新时间，sortOrder-按排序号）
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
    public ConfigQueryRequest() {}

    public ConfigQueryRequest(String configKey, String configName, List<String> configGroups,
                             List<String> configTypes, Boolean isSystem, Boolean encrypted,
                             Boolean editable, List<String> statuses, Long creatorId,
                             String createTimeStart, String createTimeEnd, String updateTimeStart,
                             String updateTimeEnd, String configValue, String description,
                             String sortBy, String sortDirection, Integer pageNum, Integer pageSize) {
        this.configKey = configKey;
        this.configName = configName;
        this.configGroups = configGroups;
        this.configTypes = configTypes;
        this.isSystem = isSystem;
        this.encrypted = encrypted;
        this.editable = editable;
        this.statuses = statuses;
        this.creatorId = creatorId;
        this.createTimeStart = createTimeStart;
        this.createTimeEnd = createTimeEnd;
        this.updateTimeStart = updateTimeStart;
        this.updateTimeEnd = updateTimeEnd;
        this.configValue = configValue;
        this.description = description;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    // Getter和Setter方法
    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public List<String> getConfigGroups() {
        return configGroups;
    }

    public void setConfigGroups(List<String> configGroups) {
        this.configGroups = configGroups;
    }

    public List<String> getConfigTypes() {
        return configTypes;
    }

    public void setConfigTypes(List<String> configTypes) {
        this.configTypes = configTypes;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
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

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "ConfigQueryRequest{" +
                "configKey='" + configKey + '\'' +
                ", configName='" + configName + '\'' +
                ", configGroups=" + configGroups +
                ", configTypes=" + configTypes +
                ", isSystem=" + isSystem +
                ", encrypted=" + encrypted +
                ", editable=" + editable +
                ", statuses=" + statuses +
                ", creatorId=" + creatorId +
                ", createTimeStart='" + createTimeStart + '\'' +
                ", createTimeEnd='" + createTimeEnd + '\'' +
                ", updateTimeStart='" + updateTimeStart + '\'' +
                ", updateTimeEnd='" + updateTimeEnd + '\'' +
                ", configValue='" + configValue + '\'' +
                ", description='" + description + '\'' +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}