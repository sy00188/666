package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("system_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(min = 1, max = 100, message = "配置键长度必须在1-100个字符之间")
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    @TableField("config_value")
    private String configValue;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(min = 1, max = 200, message = "配置名称长度必须在1-200个字符之间")
    @TableField("config_name")
    private String configName;

    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    @TableField("config_desc")
    private String configDesc;

    /**
     * 配置分组
     */
    @NotBlank(message = "配置分组不能为空")
    @Size(min = 1, max = 100, message = "配置分组长度必须在1-100个字符之间")
    @TableField("config_group")
    private String configGroup;

    /**
     * 配置类型：1-字符串，2-数字，3-布尔值，4-JSON，5-数组，6-文件路径，7-URL，8-邮箱，9-密码，10-其他
     */
    @NotNull(message = "配置类型不能为空")
    @Min(value = 1, message = "配置类型值不正确")
    @Max(value = 10, message = "配置类型值不正确")
    @TableField("config_type")
    private Integer configType;

    /**
     * 数据类型：string、int、long、double、boolean、json、array
     */
    @Size(max = 50, message = "数据类型长度不能超过50个字符")
    @TableField("data_type")
    private String dataType;

    /**
     * 默认值
     */
    @Size(max = 2000, message = "默认值长度不能超过2000个字符")
    @TableField("default_value")
    private String defaultValue;

    /**
     * 可选值（JSON数组格式）
     */
    @TableField("options")
    private String options;

    /**
     * 验证规则（正则表达式）
     */
    @Size(max = 500, message = "验证规则长度不能超过500个字符")
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 最小值（数字类型）
     */
    @TableField("min_value")
    private Double minValue;

    /**
     * 最大值（数字类型）
     */
    @TableField("max_value")
    private Double maxValue;

    /**
     * 最小长度（字符串类型）
     */
    @Min(value = 0, message = "最小长度不能为负数")
    @TableField("min_length")
    private Integer minLength;

    /**
     * 最大长度（字符串类型）
     */
    @Min(value = 0, message = "最大长度不能为负数")
    @TableField("max_length")
    private Integer maxLength;

    /**
     * 是否必填：0-否，1-是
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 是否只读：0-否，1-是
     */
    @TableField("is_readonly")
    private Integer isReadonly;

    /**
     * 是否敏感：0-否，1-是
     */
    @TableField("is_sensitive")
    private Integer isSensitive;

    /**
     * 是否加密存储：0-否，1-是
     */
    @TableField("is_encrypted")
    private Integer isEncrypted;

    /**
     * 是否系统内置：0-否，1-是
     */
    @TableField("is_system")
    private Integer isSystem;

    /**
     * 是否可见：0-否，1-是
     */
    @TableField("is_visible")
    private Integer isVisible;

    /**
     * 是否启用：0-否，1-是
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能为负数")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 环境：dev-开发，test-测试，prod-生产，all-所有
     */
    @Size(max = 20, message = "环境长度不能超过20个字符")
    @TableField("environment")
    private String environment;

    /**
     * 版本号
     */
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    @TableField("version")
    private String version;

    /**
     * 生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expiry_time")
    private LocalDateTime expiryTime;

    /**
     * 标签（用逗号分隔）
     */
    @Size(max = 500, message = "标签长度不能超过500个字符")
    @TableField("tags")
    private String tags;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extra_attrs")
    private String extraAttrs;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    // 添加兼容性setter方法
    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getCreateBy() {
        return this.createBy;
    }

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    // 添加兼容性setter方法
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Long getUpdateBy() {
        return this.updateBy;
    }

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    // ==================== 非数据库字段 ====================

    /**
     * 创建人信息（非数据库字段）
     */
    @TableField(exist = false)
    private User creator;

    /**
     * 更新人信息（非数据库字段）
     */
    @TableField(exist = false)
    private User updater;

    /**
     * 标签列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<String> tagList;

    /**
     * 可选值列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<String> optionList;

    /**
     * 扩展属性Map（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.Map<String, Object> extraAttrsMap;

    // ==================== 业务方法 ====================

    /**
     * 检查配置类型是否为字符串
     */
    public boolean isStringType() {
        return this.configType != null && this.configType == 1;
    }

    /**
     * 检查配置类型是否为数字
     */
    public boolean isNumberType() {
        return this.configType != null && this.configType == 2;
    }

    /**
     * 检查配置类型是否为布尔值
     */
    public boolean isBooleanType() {
        return this.configType != null && this.configType == 3;
    }

    /**
     * 检查配置类型是否为JSON
     */
    public boolean isJsonType() {
        return this.configType != null && this.configType == 4;
    }

    /**
     * 检查配置类型是否为数组
     */
    public boolean isArrayType() {
        return this.configType != null && this.configType == 5;
    }

    /**
     * 检查配置类型是否为文件路径
     */
    public boolean isFilePathType() {
        return this.configType != null && this.configType == 6;
    }

    /**
     * 检查配置类型是否为URL
     */
    public boolean isUrlType() {
        return this.configType != null && this.configType == 7;
    }

    /**
     * 检查配置类型是否为邮箱
     */
    public boolean isEmailType() {
        return this.configType != null && this.configType == 8;
    }

    /**
     * 检查配置类型是否为密码
     */
    public boolean isPasswordType() {
        return this.configType != null && this.configType == 9;
    }

    /**
     * 检查是否必填
     */
    public boolean isRequired() {
        return this.isRequired != null && this.isRequired == 1;
    }

    /**
     * 检查是否只读
     */
    public boolean isReadonly() {
        return this.isReadonly != null && this.isReadonly == 1;
    }

    /**
     * 检查是否敏感
     */
    public boolean isSensitive() {
        return this.isSensitive != null && this.isSensitive == 1;
    }

    /**
     * 检查是否加密存储
     */
    public boolean isEncrypted() {
        return this.isEncrypted != null && this.isEncrypted == 1;
    }

    /**
     * 检查是否系统内置
     */
    public boolean isSystem() {
        return this.isSystem != null && this.isSystem == 1;
    }

    /**
     * 检查是否可见
     */
    public boolean isVisible() {
        return this.isVisible != null && this.isVisible == 1;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return this.isEnabled != null && this.isEnabled == 1;
    }

    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted == 1;
    }

    /**
     * 检查配置是否在有效期内
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查生效时间
        if (this.effectiveTime != null && now.isBefore(this.effectiveTime)) {
            return false;
        }
        
        // 检查失效时间
        if (this.expiryTime != null && now.isAfter(this.expiryTime)) {
            return false;
        }
        
        return true;
    }

    /**
     * 检查配置是否已过期
     */
    public boolean isExpired() {
        return this.expiryTime != null && LocalDateTime.now().isAfter(this.expiryTime);
    }

    /**
     * 检查配置是否尚未生效
     */
    public boolean isNotYetEffective() {
        return this.effectiveTime != null && LocalDateTime.now().isBefore(this.effectiveTime);
    }

    /**
     * 获取配置类型描述
     */
    public String getConfigTypeDesc() {
        if (this.configType == null) {
            return "未知";
        }
        switch (this.configType) {
            case 1:
                return "字符串";
            case 2:
                return "数字";
            case 3:
                return "布尔值";
            case 4:
                return "JSON";
            case 5:
                return "数组";
            case 6:
                return "文件路径";
            case 7:
                return "URL";
            case 8:
                return "邮箱";
            case 9:
                return "密码";
            case 10:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 获取布尔值
     */
    public Boolean getBooleanValue() {
        if (this.configValue == null || this.configValue.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(this.configValue);
    }

    /**
     * 获取整数值
     */
    public Integer getIntegerValue() {
        if (this.configValue == null || this.configValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取长整数值
     */
    public Long getLongValue() {
        if (this.configValue == null || this.configValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取双精度浮点数值
     */
    public Double getDoubleValue() {
        if (this.configValue == null || this.configValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(this.configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取数组值
     */
    public java.util.List<String> getArrayValue() {
        if (this.configValue == null || this.configValue.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 简单的逗号分隔处理
        return java.util.Arrays.asList(this.configValue.split(","));
    }

    /**
     * 解析标签字符串为列表
     */
    public java.util.List<String> parseTagsToList() {
        if (this.tags == null || this.tags.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(this.tags.split(","));
    }

    /**
     * 解析可选值字符串为列表
     */
    public java.util.List<String> parseOptionsToList() {
        if (this.options == null || this.options.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 简单的JSON数组解析（实际项目中应使用JSON库）
        String cleanOptions = this.options.replaceAll("[\\[\\]\"]", "");
        if (cleanOptions.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        return java.util.Arrays.asList(cleanOptions.split(","));
    }

    /**
     * 检查是否包含指定标签
     */
    public boolean hasTag(String tag) {
        java.util.List<String> tagList = this.parseTagsToList();
        return tagList.contains(tag);
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        java.util.List<String> tagList = this.parseTagsToList();
        if (!tagList.contains(tag)) {
            tagList.add(tag);
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        java.util.List<String> tagList = this.parseTagsToList();
        tagList.remove(tag);
        this.tags = String.join(",", tagList);
    }

    /**
     * 验证配置值
     */
    public boolean validateValue(String value) {
        if (value == null) {
            return !this.isRequired();
        }
        
        // 长度验证
        if (this.minLength != null && value.length() < this.minLength) {
            return false;
        }
        if (this.maxLength != null && value.length() > this.maxLength) {
            return false;
        }
        
        // 正则验证
        if (this.validationRule != null && !this.validationRule.trim().isEmpty()) {
            try {
                if (!value.matches(this.validationRule)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        // 数值范围验证
        if (this.isNumberType()) {
            try {
                double numValue = Double.parseDouble(value);
                if (this.minValue != null && numValue < this.minValue) {
                    return false;
                }
                if (this.maxValue != null && numValue > this.maxValue) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 获取显示值（敏感信息脱敏）
     */
    public String getDisplayValue() {
        if (this.configValue == null) {
            return null;
        }
        
        if (this.isSensitive() || this.isPasswordType()) {
            return "******";
        }
        
        return this.configValue;
    }

    /**
     * 重置为默认值
     */
    public void resetToDefault() {
        this.configValue = this.defaultValue;
    }

    /**
     * 检查环境是否匹配
     */
    public boolean isEnvironmentMatch(String env) {
        if (this.environment == null || this.environment.trim().isEmpty()) {
            return true;
        }
        
        if ("all".equalsIgnoreCase(this.environment)) {
            return true;
        }
        
        return this.environment.equalsIgnoreCase(env);
    }

    // ==================== 兼容性getter方法 ====================
    
    /**
     * 获取敏感标识（兼容性方法）
     */
    public Integer getSensitive() {
        return this.isSensitive;
    }
    
    /**
     * 获取只读标识（兼容性方法）
     */
    public Integer getReadonly() {
        return this.isReadonly;
    }
    
    /**
     * 获取系统内置标识（兼容性方法）
     */
    public Integer getBuiltIn() {
        return this.isSystem;
    }
    
    /**
     * 设置更新时间（兼容性方法）
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updateTime = updatedAt;
    }
    
    /**
     * 获取更新时间（兼容性方法）
     */
    public LocalDateTime getUpdatedAt() {
        return this.updateTime;
    }
    
    /**
     * 设置删除人（兼容性方法）
     */
    public void setDeletedBy(Long deletedBy) {
        // 逻辑删除场景，记录在updateBy中
        this.updateBy = deletedBy;
    }
    
    /**
     * 设置删除时间（兼容性方法）
     */
    public void setDeletedAt(LocalDateTime deletedAt) {
        // 逻辑删除场景，记录在updateTime中
        this.updateTime = deletedAt;
    }
}