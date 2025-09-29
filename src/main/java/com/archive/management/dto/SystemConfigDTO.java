package com.archive.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统配置数据传输对象
 * 用于API接口的数据传输
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统配置数据传输对象")
public class SystemConfigDTO {

    @Schema(description = "配置ID", example = "1")
    private Long id;

    @Schema(description = "配置编码", example = "SYSTEM_NAME", required = true)
    @NotBlank(message = "配置编码不能为空")
    @Size(min = 2, max = 100, message = "配置编码长度必须在2-100个字符之间")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "配置编码只能包含大写字母、数字和下划线，且必须以大写字母开头")
    private String configKey;

    @Schema(description = "配置名称", example = "系统名称", required = true)
    @NotBlank(message = "配置名称不能为空")
    @Size(min = 2, max = 200, message = "配置名称长度必须在2-200个字符之间")
    private String configName;

    @Schema(description = "配置值", example = "档案管理系统", required = true)
    @NotBlank(message = "配置值不能为空")
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    private String configValue;

    @Schema(description = "默认值", example = "档案管理系统")
    @Size(max = 2000, message = "默认值长度不能超过2000个字符")
    private String defaultValue;

    @Schema(description = "配置类型", example = "1", allowableValues = {"1", "2", "3", "4", "5", "6", "7", "8"}, required = true)
    @NotNull(message = "配置类型不能为空")
    @Min(value = 1, message = "配置类型值必须为1-8")
    @Max(value = 8, message = "配置类型值必须为1-8")
    private Integer configType;

    @Schema(description = "数据类型", example = "1", allowableValues = {"1", "2", "3", "4", "5", "6", "7"}, required = true)
    @NotNull(message = "数据类型不能为空")
    @Min(value = 1, message = "数据类型值必须为1-7")
    @Max(value = 7, message = "数据类型值必须为1-7")
    private Integer dataType;

    @Schema(description = "配置分组", example = "系统设置", required = true)
    @NotBlank(message = "配置分组不能为空")
    @Size(min = 2, max = 100, message = "配置分组长度必须在2-100个字符之间")
    private String configGroup;

    @Schema(description = "配置分类", example = "基础配置")
    @Size(max = 100, message = "配置分类长度不能超过100个字符")
    private String configCategory;

    @Schema(description = "配置描述", example = "系统显示名称配置")
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String description;

    @Schema(description = "配置备注", example = "修改后需要重启系统")
    @Size(max = 1000, message = "配置备注长度不能超过1000个字符")
    private String remark;

    @Schema(description = "是否必需", example = "true", required = true)
    @NotNull(message = "是否必需不能为空")
    private Boolean required;

    @Schema(description = "是否只读", example = "false")
    private Boolean readonly;

    @Schema(description = "是否加密", example = "false")
    private Boolean encrypted;

    @Schema(description = "是否敏感", example = "false")
    private Boolean sensitive;

    @Schema(description = "是否可见", example = "true")
    private Boolean visible;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean editable;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序号", example = "1")
    @Min(value = 0, message = "排序号不能为负数")
    private Integer sortOrder;

    @Schema(description = "最小值", example = "0")
    private String minValue;

    @Schema(description = "最大值", example = "100")
    private String maxValue;

    @Schema(description = "最小长度", example = "1")
    @Min(value = 0, message = "最小长度不能为负数")
    private Integer minLength;

    @Schema(description = "最大长度", example = "200")
    @Min(value = 1, message = "最大长度必须为正数")
    private Integer maxLength;

    @Schema(description = "正则表达式", example = "^[a-zA-Z0-9_]+$")
    @Size(max = 500, message = "正则表达式长度不能超过500个字符")
    private String pattern;

    @Schema(description = "可选值列表", example = "选项1,选项2,选项3")
    @Size(max = 2000, message = "可选值列表长度不能超过2000个字符")
    private String options;

    @Schema(description = "验证规则", example = "required|string|max:200")
    @Size(max = 500, message = "验证规则长度不能超过500个字符")
    private String validationRules;

    @Schema(description = "输入提示", example = "请输入系统名称")
    @Size(max = 200, message = "输入提示长度不能超过200个字符")
    private String placeholder;

    @Schema(description = "帮助文本", example = "系统名称将显示在页面标题中")
    @Size(max = 500, message = "帮助文本长度不能超过500个字符")
    private String helpText;

    @Schema(description = "单位", example = "MB")
    @Size(max = 20, message = "单位长度不能超过20个字符")
    private String unit;

    @Schema(description = "格式化模式", example = "yyyy-MM-dd HH:mm:ss")
    @Size(max = 100, message = "格式化模式长度不能超过100个字符")
    private String formatPattern;

    @Schema(description = "依赖配置", example = "ENABLE_FEATURE")
    @Size(max = 200, message = "依赖配置长度不能超过200个字符")
    private String dependencies;

    @Schema(description = "影响范围", example = "1", allowableValues = {"1", "2", "3", "4"})
    @Min(value = 1, message = "影响范围值必须为1-4")
    @Max(value = 4, message = "影响范围值必须为1-4")
    private Integer impactScope;

    @Schema(description = "生效方式", example = "1", allowableValues = {"1", "2", "3"})
    @Min(value = 1, message = "生效方式值必须为1-3")
    @Max(value = 3, message = "生效方式值必须为1-3")
    private Integer effectiveMode;

    @Schema(description = "重启要求", example = "false")
    private Boolean restartRequired;

    @Schema(description = "缓存时间(秒)", example = "300")
    @Min(value = 0, message = "缓存时间不能为负数")
    private Integer cacheTime;

    @Schema(description = "版本号", example = "1.0.0")
    @Size(max = 20, message = "版本号长度不能超过20个字符")
    private String version;

    @Schema(description = "环境标识", example = "production")
    @Size(max = 50, message = "环境标识长度不能超过50个字符")
    private String environment;

    @Schema(description = "应用模块", example = "user-service")
    @Size(max = 100, message = "应用模块长度不能超过100个字符")
    private String applicationModule;

    @Schema(description = "配置来源", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "配置来源值必须为1-5")
    @Max(value = 5, message = "配置来源值必须为1-5")
    private Integer configSource;

    @Schema(description = "优先级", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "优先级值必须为1-5")
    @Max(value = 5, message = "优先级值必须为1-5")
    private Integer priority;

    @Schema(description = "标签", example = "系统,基础,必需")
    @Size(max = 200, message = "标签长度不能超过200个字符")
    private String tags;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedProperties;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建人姓名", example = "系统管理员")
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String createdByName;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID", example = "1")
    private Long updatedBy;

    @Schema(description = "更新人姓名", example = "系统管理员")
    @Size(max = 100, message = "更新人姓名长度不能超过100个字符")
    private String updatedByName;

    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "最后访问时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

    @Schema(description = "访问次数", example = "100")
    @Min(value = 0, message = "访问次数不能为负数")
    private Long accessCount;

    @Schema(description = "修改次数", example = "5")
    @Min(value = 0, message = "修改次数不能为负数")
    private Long modifyCount;

    /**
     * 获取配置类型显示文本
     */
    @Schema(description = "配置类型显示文本")
    public String getConfigTypeText() {
        if (configType == null) {
            return "未知";
        }
        switch (configType) {
            case 1:
                return "系统配置";
            case 2:
                return "业务配置";
            case 3:
                return "界面配置";
            case 4:
                return "安全配置";
            case 5:
                return "性能配置";
            case 6:
                return "集成配置";
            case 7:
                return "监控配置";
            case 8:
                return "其他配置";
            default:
                return "未知";
        }
    }

    /**
     * 获取数据类型显示文本
     */
    @Schema(description = "数据类型显示文本")
    public String getDataTypeText() {
        if (dataType == null) {
            return "未知";
        }
        switch (dataType) {
            case 1:
                return "字符串";
            case 2:
                return "整数";
            case 3:
                return "小数";
            case 4:
                return "布尔值";
            case 5:
                return "日期时间";
            case 6:
                return "JSON";
            case 7:
                return "文件路径";
            default:
                return "未知";
        }
    }

    /**
     * 获取影响范围显示文本
     */
    @Schema(description = "影响范围显示文本")
    public String getImpactScopeText() {
        if (impactScope == null) {
            return "未知";
        }
        switch (impactScope) {
            case 1:
                return "全局";
            case 2:
                return "模块";
            case 3:
                return "功能";
            case 4:
                return "用户";
            default:
                return "未知";
        }
    }

    /**
     * 获取生效方式显示文本
     */
    @Schema(description = "生效方式显示文本")
    public String getEffectiveModeText() {
        if (effectiveMode == null) {
            return "未知";
        }
        switch (effectiveMode) {
            case 1:
                return "立即生效";
            case 2:
                return "重启生效";
            case 3:
                return "定时生效";
            default:
                return "未知";
        }
    }

    /**
     * 获取配置来源显示文本
     */
    @Schema(description = "配置来源显示文本")
    public String getConfigSourceText() {
        if (configSource == null) {
            return "未知";
        }
        switch (configSource) {
            case 1:
                return "数据库";
            case 2:
                return "配置文件";
            case 3:
                return "环境变量";
            case 4:
                return "命令行参数";
            case 5:
                return "远程配置中心";
            default:
                return "未知";
        }
    }

    /**
     * 获取优先级显示文本
     */
    @Schema(description = "优先级显示文本")
    public String getPriorityText() {
        if (priority == null) {
            return "未知";
        }
        switch (priority) {
            case 1:
                return "最高";
            case 2:
                return "高";
            case 3:
                return "中";
            case 4:
                return "低";
            case 5:
                return "最低";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否为必需配置
     */
    @Schema(description = "是否为必需配置")
    public boolean isRequired() {
        return required != null && required;
    }

    /**
     * 判断是否为只读配置
     */
    @Schema(description = "是否为只读配置")
    public boolean isReadonly() {
        return readonly != null && readonly;
    }

    /**
     * 判断是否为加密配置
     */
    @Schema(description = "是否为加密配置")
    public boolean isEncrypted() {
        return encrypted != null && encrypted;
    }

    /**
     * 判断是否为敏感配置
     */
    @Schema(description = "是否为敏感配置")
    public boolean isSensitive() {
        return sensitive != null && sensitive;
    }

    /**
     * 判断是否可见
     */
    @Schema(description = "是否可见")
    public boolean isVisible() {
        return visible == null || visible;
    }

    /**
     * 判断是否可编辑
     */
    @Schema(description = "是否可编辑")
    public boolean isEditable() {
        return editable == null || editable;
    }

    /**
     * 判断是否启用
     */
    @Schema(description = "是否启用")
    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    /**
     * 判断是否需要重启
     */
    @Schema(description = "是否需要重启")
    public boolean isRestartRequired() {
        return restartRequired != null && restartRequired;
    }

    /**
     * 判断是否为系统配置
     */
    @Schema(description = "是否为系统配置")
    public boolean isSystemConfig() {
        return configType != null && configType == 1;
    }

    /**
     * 判断是否为安全配置
     */
    @Schema(description = "是否为安全配置")
    public boolean isSecurityConfig() {
        return configType != null && configType == 4;
    }

    /**
     * 判断是否为高优先级配置
     */
    @Schema(description = "是否为高优先级配置")
    public boolean isHighPriority() {
        return priority != null && priority <= 2;
    }

    /**
     * 判断是否有默认值
     */
    @Schema(description = "是否有默认值")
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.trim().isEmpty();
    }

    /**
     * 判断是否使用默认值
     */
    @Schema(description = "是否使用默认值")
    public boolean isUsingDefaultValue() {
        if (configValue == null || defaultValue == null) {
            return false;
        }
        return configValue.equals(defaultValue);
    }

    /**
     * 判断是否有可选值
     */
    @Schema(description = "是否有可选值")
    public boolean hasOptions() {
        return options != null && !options.trim().isEmpty();
    }

    /**
     * 获取可选值数组
     */
    @Schema(description = "可选值数组")
    public String[] getOptionsArray() {
        if (options == null || options.trim().isEmpty()) {
            return new String[0];
        }
        return options.split(",");
    }

    /**
     * 判断是否有验证规则
     */
    @Schema(description = "是否有验证规则")
    public boolean hasValidationRules() {
        return validationRules != null && !validationRules.trim().isEmpty();
    }

    /**
     * 判断是否有依赖配置
     */
    @Schema(description = "是否有依赖配置")
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.trim().isEmpty();
    }

    /**
     * 获取依赖配置数组
     */
    @Schema(description = "依赖配置数组")
    public String[] getDependenciesArray() {
        if (dependencies == null || dependencies.trim().isEmpty()) {
            return new String[0];
        }
        return dependencies.split(",");
    }

    /**
     * 判断是否有标签
     */
    @Schema(description = "是否有标签")
    public boolean hasTags() {
        return tags != null && !tags.trim().isEmpty();
    }

    /**
     * 获取标签数组
     */
    @Schema(description = "标签数组")
    public String[] getTagsArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    /**
     * 获取显示值（敏感信息脱敏）
     */
    @Schema(description = "显示值")
    public String getDisplayValue() {
        if (configValue == null) {
            return null;
        }
        
        if (isSensitive() || isEncrypted()) {
            if (configValue.length() <= 6) {
                return "******";
            } else {
                return configValue.substring(0, 2) + "****" + configValue.substring(configValue.length() - 2);
            }
        }
        
        return configValue;
    }

    /**
     * 获取优先级颜色
     */
    @Schema(description = "优先级颜色")
    public String getPriorityColor() {
        if (priority == null) {
            return "#666666";
        }
        switch (priority) {
            case 1:
                return "#ff4d4f"; // 红色 - 最高
            case 2:
                return "#fa8c16"; // 橙色 - 高
            case 3:
                return "#faad14"; // 黄色 - 中
            case 4:
                return "#1890ff"; // 蓝色 - 低
            case 5:
                return "#52c41a"; // 绿色 - 最低
            default:
                return "#666666"; // 灰色
        }
    }

    /**
     * 获取配置状态颜色
     */
    @Schema(description = "配置状态颜色")
    public String getStatusColor() {
        if (!isEnabled()) {
            return "#d9d9d9"; // 灰色 - 禁用
        }
        
        if (isRequired()) {
            return "#ff4d4f"; // 红色 - 必需
        }
        
        if (isReadonly()) {
            return "#faad14"; // 橙色 - 只读
        }
        
        return "#52c41a"; // 绿色 - 正常
    }

    /**
     * 获取简化的配置描述
     */
    @Schema(description = "简化的配置描述")
    public String getSimpleDescription() {
        if (description != null && description.length() > 50) {
            return description.substring(0, 47) + "...";
        }
        return description;
    }

    /**
     * 获取配置摘要信息
     */
    @Schema(description = "配置摘要信息")
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(getConfigTypeText());
        
        if (isRequired()) {
            summary.append(" | 必需");
        }
        
        if (isReadonly()) {
            summary.append(" | 只读");
        }
        
        if (isSensitive()) {
            summary.append(" | 敏感");
        }
        
        if (isRestartRequired()) {
            summary.append(" | 需重启");
        }
        
        return summary.toString();
    }
}