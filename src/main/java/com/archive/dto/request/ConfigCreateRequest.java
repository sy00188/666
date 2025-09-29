package com.archive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 系统配置创建请求DTO
 * 封装系统配置创建的输入信息
 */
public class ConfigCreateRequest {

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;

    /**
     * 配置值
     */
    @NotBlank(message = "配置值不能为空")
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    private String configValue;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;

    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String description;

    /**
     * 配置分组
     */
    @NotBlank(message = "配置分组不能为空")
    @Size(max = 50, message = "配置分组长度不能超过50个字符")
    private String configGroup;

    /**
     * 配置类型（STRING-字符串，NUMBER-数字，BOOLEAN-布尔值，JSON-JSON对象，ARRAY-数组）
     */
    @NotBlank(message = "配置类型不能为空")
    private String configType;

    /**
     * 是否系统内置配置
     */
    @NotNull(message = "是否系统内置配置不能为空")
    private Boolean isSystem;

    /**
     * 是否加密存储
     */
    @NotNull(message = "是否加密存储不能为空")
    private Boolean encrypted;

    /**
     * 是否可编辑
     */
    @NotNull(message = "是否可编辑不能为空")
    private Boolean editable;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（ACTIVE-启用，INACTIVE-禁用）
     */
    @NotBlank(message = "状态不能为空")
    private String status;

    /**
     * 默认值
     */
    @Size(max = 2000, message = "默认值长度不能超过2000个字符")
    private String defaultValue;

    /**
     * 值范围限制（JSON格式）
     */
    @Size(max = 1000, message = "值范围限制长度不能超过1000个字符")
    private String valueRange;

    /**
     * 验证规则（正则表达式）
     */
    @Size(max = 500, message = "验证规则长度不能超过500个字符")
    private String validationRule;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // 构造函数
    public ConfigCreateRequest() {}

    public ConfigCreateRequest(String configKey, String configValue, String configName,
                              String description, String configGroup, String configType,
                              Boolean isSystem, Boolean encrypted, Boolean editable,
                              Integer sortOrder, String status, String defaultValue,
                              String valueRange, String validationRule, String remark) {
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
        this.remark = remark;
    }

    // Getter和Setter方法
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ConfigCreateRequest{" +
                "configKey='" + configKey + '\'' +
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
                ", remark='" + remark + '\'' +
                '}';
    }
}