package com.archive.management.dto.response;

import java.time.LocalDateTime;

/**
 * 系统配置响应DTO
 * 封装系统配置信息的输出数据
 */
public class ConfigResponse {

    /**
     * 配置ID
     */
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 配置类型（STRING-字符串，NUMBER-数字，BOOLEAN-布尔值，JSON-JSON对象，ARRAY-数组）
     */
    private String configType;

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
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（ACTIVE-启用，INACTIVE-禁用）
     */
    private String status;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 值范围限制（JSON格式）
     */
    private String valueRange;

    /**
     * 验证规则（正则表达式）
     */
    private String validationRule;

    /**
     * 创建者信息
     */
    private CreatorInfo creator;

    /**
     * 更新者信息
     */
    private UpdaterInfo updater;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者信息内部类
     */
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String realName;

        public CreatorInfo() {}

        public CreatorInfo(Long id, String username, String realName) {
            this.id = id;
            this.username = username;
            this.realName = realName;
        }

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }
    }

    /**
     * 更新者信息内部类
     */
    public static class UpdaterInfo {
        private Long id;
        private String username;
        private String realName;

        public UpdaterInfo() {}

        public UpdaterInfo(Long id, String username, String realName) {
            this.id = id;
            this.username = username;
            this.realName = realName;
        }

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }
    }

    // 构造函数
    public ConfigResponse() {}

    public ConfigResponse(Long id, String configKey, String configValue, String configName,
                         String description, String configGroup, String configType,
                         Boolean isSystem, Boolean encrypted, Boolean editable,
                         Integer sortOrder, String status, String defaultValue,
                         String valueRange, String validationRule, CreatorInfo creator,
                         UpdaterInfo updater, LocalDateTime createTime, LocalDateTime updateTime,
                         String remark) {
        this.id = id;
        this.configKey = configKey;
        this.configValue = configValue;
        this.configName = configName;
        this.description = description;
        this.configGroup = configGroup;
        this.configType = configType;
        this.isSystem = isSystem;
        this.encrypted = encrypted;
        this.editable = editable;
        this.sortOrder = sortOrder;
        this.status = status;
        this.defaultValue = defaultValue;
        this.valueRange = valueRange;
        this.validationRule = validationRule;
        this.creator = creator;
        this.updater = updater;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.remark = remark;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValueRange() {
        return valueRange;
    }

    public void setValueRange(String valueRange) {
        this.valueRange = valueRange;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public CreatorInfo getCreator() {
        return creator;
    }

    public void setCreator(CreatorInfo creator) {
        this.creator = creator;
    }

    public UpdaterInfo getUpdater() {
        return updater;
    }

    public void setUpdater(UpdaterInfo updater) {
        this.updater = updater;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ConfigResponse{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", configName='" + configName + '\'' +
                ", description='" + description + '\'' +
                ", configGroup='" + configGroup + '\'' +
                ", configType='" + configType + '\'' +
                ", isSystem=" + isSystem +
                ", encrypted=" + encrypted +
                ", editable=" + editable +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", valueRange='" + valueRange + '\'' +
                ", validationRule='" + validationRule + '\'' +
                ", creator=" + creator +
                ", updater=" + updater +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}