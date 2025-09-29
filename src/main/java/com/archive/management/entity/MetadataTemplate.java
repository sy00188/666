package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 元数据模板实体类
 * 对应数据库表：arc_metadata_template
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arc_metadata_template")
public class MetadataTemplate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段编码
     */
    @TableField("field_code")
    private String fieldCode;

    /**
     * 字段类型(text/number/date/select/boolean/file)
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 字段选项(下拉框等)，JSON格式
     */
    @TableField("field_options")
    private String fieldOptions;

    /**
     * 是否必填(0:否 1:是)
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 验证规则
     */
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态(0:禁用 1:启用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    // 构造函数
    public MetadataTemplate() {
        super();
        this.isRequired = 0;
        this.sortOrder = 0;
        this.status = 1;
    }

    public MetadataTemplate(Long categoryId, String fieldName, String fieldCode, String fieldType) {
        this();
        this.categoryId = categoryId;
        this.fieldName = fieldName;
        this.fieldCode = fieldCode;
        this.fieldType = fieldType;
    }

    // 业务方法

    /**
     * 获取字段类型描述
     */
    public String getFieldTypeDesc() {
        if (fieldType == null) {
            return "未知";
        }
        switch (fieldType.toLowerCase()) {
            case "text":
                return "文本";
            case "number":
                return "数字";
            case "date":
                return "日期";
            case "select":
                return "下拉选择";
            case "boolean":
                return "布尔值";
            case "file":
                return "文件";
            default:
                return fieldType;
        }
    }

    /**
     * 获取是否必填描述
     */
    public String getRequiredDesc() {
        return isRequired != null && isRequired == 1 ? "必填" : "非必填";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "启用";
            default:
                return "未知";
        }
    }

    /**
     * 检查字段类型是否有效
     */
    public boolean isValidFieldType() {
        if (fieldType == null) {
            return false;
        }
        String[] validTypes = {"text", "number", "date", "select", "boolean", "file"};
        for (String type : validTypes) {
            if (type.equalsIgnoreCase(fieldType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为选择类型字段
     */
    public boolean isSelectType() {
        return "select".equalsIgnoreCase(fieldType);
    }

    /**
     * 检查是否为文件类型字段
     */
    public boolean isFileType() {
        return "file".equalsIgnoreCase(fieldType);
    }

    /**
     * 检查是否为日期类型字段
     */
    public boolean isDateType() {
        return "date".equalsIgnoreCase(fieldType);
    }

    /**
     * 检查是否为数字类型字段
     */
    public boolean isNumberType() {
        return "number".equalsIgnoreCase(fieldType);
    }

    /**
     * 检查是否为布尔类型字段
     */
    public boolean isBooleanType() {
        return "boolean".equalsIgnoreCase(fieldType);
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查是否必填
     */
    public boolean isRequired() {
        return isRequired != null && isRequired == 1;
    }

    // 删除手动编写的toString()和equals()方法，让Lombok自动生成
}