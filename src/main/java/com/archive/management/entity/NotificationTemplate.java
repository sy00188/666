package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知模板实体类
 * 对应数据库表：sys_notification_template
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_notification_template")
public class NotificationTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 模板编码（唯一标识）
     */
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 50, message = "模板编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z_][A-Z0-9_]*$", message = "模板编码只能包含大写字母、数字和下划线，且必须以大写字母或下划线开头")
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    @TableField("template_name")
    private String templateName;

    /**
     * 模板类型
     * EMAIL-邮件，SMS-短信，SYSTEM-系统通知，PUSH-推送通知
     */
    @NotBlank(message = "模板类型不能为空")
    @Pattern(regexp = "^(EMAIL|SMS|SYSTEM|PUSH)$", message = "模板类型只能是EMAIL、SMS、SYSTEM或PUSH")
    @TableField("template_type")
    private String templateType;

    /**
     * 模板标题
     */
    @Size(max = 200, message = "模板标题长度不能超过200个字符")
    @TableField("template_title")
    private String templateTitle;

    /**
     * 模板内容（支持Freemarker语法）
     */
    @NotBlank(message = "模板内容不能为空")
    @Size(max = 5000, message = "模板内容长度不能超过5000个字符")
    @TableField("template_content")
    private String templateContent;

    /**
     * 模板变量（JSON格式）
     * 存储模板中可用的变量定义和说明
     */
    @Size(max = 2000, message = "模板变量长度不能超过2000个字符")
    @TableField("template_variables")
    private String templateVariables;

    /**
     * 模板版本
     */
    @NotNull(message = "模板版本不能为空")
    @Min(value = 1, message = "模板版本必须大于0")
    @TableField("template_version")
    private Integer templateVersion;

    /**
     * 是否为当前版本
     * 0-否，1-是
     */
    @NotNull(message = "当前版本标识不能为空")
    @Min(value = 0, message = "当前版本标识值不正确")
    @Max(value = 1, message = "当前版本标识值不正确")
    @TableField("is_current")
    private Integer isCurrent;

    /**
     * 模板状态
     * 0-禁用，1-启用
     */
    @NotNull(message = "模板状态不能为空")
    @Min(value = 0, message = "模板状态值不正确")
    @Max(value = 1, message = "模板状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 模板描述
     */
    @Size(max = 500, message = "模板描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 发布人ID
     */
    @TableField("publish_user_id")
    private Long publishUserId;

    // ==================== 非数据库字段 ====================

    /**
     * 创建人姓名（关联查询时使用）
     */
    @TableField(exist = false)
    private String createUserName;

    /**
     * 发布人姓名（关联查询时使用）
     */
    @TableField(exist = false)
    private String publishUserName;

    /**
     * 模板变量Map（用于模板渲染）
     */
    @TableField(exist = false)
    private Map<String, Object> variableMap;

    /**
     * 渲染后的内容（临时存储）
     */
    @TableField(exist = false)
    private String renderedContent;

    /**
     * 渲染后的标题（临时存储）
     */
    @TableField(exist = false)
    private String renderedTitle;

    // ==================== 业务方法 ====================

    /**
     * 判断模板是否启用
     * 
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 判断是否为当前版本
     * 
     * @return true-当前版本，false-历史版本
     */
    public boolean isCurrentVersion() {
        return isCurrent != null && isCurrent == 1;
    }

    /**
     * 判断是否已发布
     * 
     * @return true-已发布，false-未发布
     */
    public boolean isPublished() {
        return publishTime != null && publishUserId != null;
    }

    /**
     * 启用模板
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 禁用模板
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 设置为当前版本
     */
    public void setAsCurrent() {
        this.isCurrent = 1;
    }

    /**
     * 设置为历史版本
     */
    public void setAsHistory() {
        this.isCurrent = 0;
    }

    /**
     * 发布模板
     * 
     * @param publishUserId 发布人ID
     */
    public void publish(Long publishUserId) {
        this.publishTime = LocalDateTime.now();
        this.publishUserId = publishUserId;
        this.status = 1; // 发布时自动启用
    }

    /**
     * 获取模板类型的中文描述
     * 
     * @return 中文描述
     */
    public String getTemplateTypeDesc() {
        if (templateType == null) {
            return "未知";
        }
        return switch (templateType) {
            case "EMAIL" -> "邮件通知";
            case "SMS" -> "短信通知";
            case "SYSTEM" -> "系统通知";
            case "PUSH" -> "推送通知";
            default -> "未知类型";
        };
    }

    /**
     * 获取状态的中文描述
     * 
     * @return 中文描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "启用" : "禁用";
    }
}