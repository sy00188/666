package com.archive.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "sys_config", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key", unique = true),
    @Index(name = "idx_config_category", columnList = "category"),
    @Index(name = "idx_config_status", columnList = "status"),
    @Index(name = "idx_config_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Comment("系统配置表")
public class Config {

    /**
     * 配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("配置ID")
    private Long id;

    /**
     * 配置键
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    @Comment("配置键")
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", columnDefinition = "TEXT")
    @Comment("配置值")
    private String configValue;

    /**
     * 配置名称
     */
    @Column(name = "config_name", nullable = false, length = 200)
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 200, message = "配置名称长度不能超过200个字符")
    @Comment("配置名称")
    private String configName;

    /**
     * 配置分类
     */
    @Column(name = "category", length = 50)
    @Size(max = 50, message = "配置分类长度不能超过50个字符")
    @Comment("配置分类")
    private String category;

    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    @Comment("配置描述")
    private String description;

    /**
     * 配置类型
     */
    @Column(name = "config_type", length = 20)
    @Size(max = 20, message = "配置类型长度不能超过20个字符")
    @Comment("配置类型：string-字符串，number-数字，boolean-布尔值，json-JSON对象")
    private String configType;

    /**
     * 默认值
     */
    @Column(name = "default_value", columnDefinition = "TEXT")
    @Comment("默认值")
    private String defaultValue;

    /**
     * 是否必需
     */
    @Column(name = "is_required", nullable = false)
    @NotNull(message = "是否必需不能为空")
    @Comment("是否必需：0-否，1-是")
    private Boolean isRequired;

    /**
     * 是否可编辑
     */
    @Column(name = "is_editable", nullable = false)
    @NotNull(message = "是否可编辑不能为空")
    @Comment("是否可编辑：0-否，1-是")
    private Boolean isEditable;

    /**
     * 是否敏感信息
     */
    @Column(name = "is_sensitive", nullable = false)
    @NotNull(message = "是否敏感信息不能为空")
    @Comment("是否敏感信息：0-否，1-是")
    private Boolean isSensitive;

    /**
     * 验证规则
     */
    @Column(name = "validation_rule", length = 500)
    @Size(max = 500, message = "验证规则长度不能超过500个字符")
    @Comment("验证规则（正则表达式或其他规则）")
    private String validationRule;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Comment("排序号")
    private Integer sortOrder;

    /**
     * 状态
     */
    @Column(name = "status", nullable = false)
    @NotNull(message = "状态不能为空")
    @Comment("状态：0-禁用，1-启用")
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Comment("备注")
    private String remark;

    /**
     * 创建人ID
     */
    @Column(name = "create_by")
    @Comment("创建人ID")
    private Long createBy;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    @CreationTimestamp
    @Comment("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @Column(name = "update_by")
    @Comment("更新人ID")
    private Long updateBy;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    @UpdateTimestamp
    @Comment("更新时间")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false)
    @Comment("是否删除：0-否，1-是")
    private Boolean isDeleted = false;

    /**
     * 版本号
     */
    @Version
    @Column(name = "version")
    @Comment("版本号")
    private Long version;

    // 业务方法

    /**
     * 判断配置是否启用
     * 
     * @return 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 判断配置是否禁用
     * 
     * @return 是否禁用
     */
    public boolean isDisabled() {
        return status == null || status == 0;
    }

    /**
     * 判断是否为必需配置
     * 
     * @return 是否必需
     */
    public boolean isRequired() {
        return isRequired != null && isRequired;
    }

    /**
     * 判断是否可编辑
     * 
     * @return 是否可编辑
     */
    public boolean isEditable() {
        return isEditable != null && isEditable;
    }

    /**
     * 判断是否为敏感信息
     * 
     * @return 是否敏感
     */
    public boolean isSensitive() {
        return isSensitive != null && isSensitive;
    }

    /**
     * 获取显示值（敏感信息脱敏）
     * 
     * @return 显示值
     */
    public String getDisplayValue() {
        if (isSensitive()) {
            return configValue != null && configValue.length() > 0 ? "******" : "";
        }
        return configValue;
    }

    /**
     * 获取配置的实际值（根据类型转换）
     * 
     * @return 实际值
     */
    public Object getActualValue() {
        if (configValue == null) {
            return defaultValue;
        }
        
        if (configType == null) {
            return configValue;
        }
        
        switch (configType.toLowerCase()) {
            case "number":
                try {
                    return Double.parseDouble(configValue);
                } catch (NumberFormatException e) {
                    return defaultValue != null ? Double.parseDouble(defaultValue) : 0.0;
                }
            case "boolean":
                return Boolean.parseBoolean(configValue);
            case "json":
                return configValue; // JSON字符串，需要在业务层解析
            default:
                return configValue;
        }
    }

    /**
     * 软删除
     */
    public void softDelete() {
        this.isDeleted = true;
        this.status = 0;
    }

    /**
     * 恢复删除
     */
    public void restore() {
        this.isDeleted = false;
        this.status = 1;
    }

    /**
     * 启用配置
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 禁用配置
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 重置为默认值
     */
    public void resetToDefault() {
        this.configValue = this.defaultValue;
    }

    /**
     * 验证配置值
     * 
     * @param value 待验证的值
     * @return 是否有效
     */
    public boolean validateValue(String value) {
        if (isRequired() && (value == null || value.trim().isEmpty())) {
            return false;
        }
        
        if (validationRule != null && !validationRule.trim().isEmpty() && value != null) {
            return value.matches(validationRule);
        }
        
        return true;
    }
}