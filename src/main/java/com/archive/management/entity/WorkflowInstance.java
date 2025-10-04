package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作流实例实体
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_instances")
public class WorkflowInstance {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作流定义ID
     */
    @TableField("workflow_definition_id")
    private Long workflowDefinitionId;

    /**
     * 业务类型（archive_approval, borrow_approval, user_approval等）
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 业务ID
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * 工作流名称
     */
    @TableField("workflow_name")
    private String workflowName;

    /**
     * 当前节点ID
     */
    @TableField("current_node_id")
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    @TableField("current_node_name")
    private String currentNodeName;

    /**
     * 状态（running, completed, cancelled, suspended）
     */
    @TableField("status")
    private String status;

    /**
     * 优先级（1-5，5最高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 发起人ID
     */
    @TableField("initiator_id")
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    @TableField("initiator_name")
    private String initiatorName;

    /**
     * 发起时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 预计完成时间
     */
    @TableField("expected_end_time")
    private LocalDateTime expectedEndTime;

    /**
     * 工作流变量（JSON格式）
     */
    @TableField("variables")
    private String variables;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
