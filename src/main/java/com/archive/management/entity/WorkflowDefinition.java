package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作流定义实体
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_definitions")
public class WorkflowDefinition {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作流名称
     */
    @TableField("name")
    private String name;

    /**
     * 工作流编码
     */
    @TableField("code")
    private String code;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 是否当前版本（0-否，1-是）
     */
    @TableField("is_current")
    private Integer isCurrent;

    /**
     * 工作流描述
     */
    @TableField("description")
    private String description;

    /**
     * 工作流定义（BPMN XML）
     */
    @TableField("definition_xml")
    private String definitionXml;

    /**
     * 工作流图片
     */
    @TableField("diagram_image")
    private String diagramImage;

    /**
     * 状态（draft, published, archived）
     */
    @TableField("status")
    private String status;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @TableField("created_by_name")
    private String createdByName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
