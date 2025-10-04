package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工作流任务实体
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("workflow_tasks")
public class WorkflowTask {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作流实例ID
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 任务类型（user_task, service_task, script_task等）
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 节点ID
     */
    @TableField("node_id")
    private String nodeId;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 任务状态（pending, running, completed, cancelled, failed）
     */
    @TableField("status")
    private String status;

    /**
     * 指派人ID
     */
    @TableField("assignee_id")
    private Long assigneeId;

    /**
     * 指派人姓名
     */
    @TableField("assignee_name")
    private String assigneeName;

    /**
     * 候选人ID列表（JSON格式）
     */
    @TableField("candidate_ids")
    private String candidateIds;

    /**
     * 候选人姓名列表（JSON格式）
     */
    @TableField("candidate_names")
    private String candidateNames;

    /**
     * 任务优先级（1-5，5最高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 开始时间
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
     * 任务变量（JSON格式）
     */
    @TableField("variables")
    private String variables;

    /**
     * 任务结果
     */
    @TableField("result")
    private String result;

    /**
     * 处理意见
     */
    @TableField("comment")
    private String comment;

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
