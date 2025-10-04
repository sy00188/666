package com.archive.management.service;

import com.archive.management.entity.WorkflowInstance;
import com.archive.management.entity.WorkflowTask;
import com.archive.management.entity.WorkflowDefinition;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public interface WorkflowService {

    /**
     * 启动工作流实例
     * @param workflowDefinitionId 工作流定义ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param initiatorId 发起人ID
     * @param variables 工作流变量
     * @return 工作流实例
     */
    WorkflowInstance startWorkflow(Long workflowDefinitionId, String businessType, Long businessId, 
                                   Long initiatorId, Map<String, Object> variables);

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param assigneeId 处理人ID
     * @param result 处理结果
     * @param comment 处理意见
     * @param variables 任务变量
     * @return 是否成功
     */
    boolean completeTask(Long taskId, Long assigneeId, String result, String comment, 
                        Map<String, Object> variables);

    /**
     * 转派任务
     * @param taskId 任务ID
     * @param fromAssigneeId 原处理人ID
     * @param toAssigneeId 新处理人ID
     * @param reason 转派原因
     * @return 是否成功
     */
    boolean transferTask(Long taskId, Long fromAssigneeId, Long toAssigneeId, String reason);

    /**
     * 挂起工作流实例
     * @param instanceId 实例ID
     * @param reason 挂起原因
     * @return 是否成功
     */
    boolean suspendWorkflow(Long instanceId, String reason);

    /**
     * 恢复工作流实例
     * @param instanceId 实例ID
     * @return 是否成功
     */
    boolean resumeWorkflow(Long instanceId);

    /**
     * 终止工作流实例
     * @param instanceId 实例ID
     * @param reason 终止原因
     * @return 是否成功
     */
    boolean terminateWorkflow(Long instanceId, String reason);

    /**
     * 获取用户待办任务
     * @param userId 用户ID
     * @param page 分页参数
     * @return 待办任务列表
     */
    IPage<WorkflowTask> getUserTasks(Long userId, Page<WorkflowTask> page);

    /**
     * 获取工作流实例详情
     * @param instanceId 实例ID
     * @return 工作流实例
     */
    WorkflowInstance getWorkflowInstance(Long instanceId);

    /**
     * 获取工作流任务详情
     * @param taskId 任务ID
     * @return 工作流任务
     */
    WorkflowTask getWorkflowTask(Long taskId);

    /**
     * 获取工作流实例列表
     * @param businessType 业务类型
     * @param status 状态
     * @param page 分页参数
     * @return 工作流实例列表
     */
    IPage<WorkflowInstance> getWorkflowInstances(String businessType, String status, Page<WorkflowInstance> page);

    /**
     * 获取工作流历史
     * @param instanceId 实例ID
     * @return 历史记录
     */
    List<Map<String, Object>> getWorkflowHistory(Long instanceId);

    /**
     * 创建工作流定义
     * @param definition 工作流定义
     * @return 工作流定义
     */
    WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition);

    /**
     * 发布工作流定义
     * @param definitionId 定义ID
     * @return 是否成功
     */
    boolean publishWorkflowDefinition(Long definitionId);

    /**
     * 获取工作流定义列表
     * @param businessType 业务类型
     * @param status 状态
     * @param page 分页参数
     * @return 工作流定义列表
     */
    IPage<WorkflowDefinition> getWorkflowDefinitions(String businessType, String status, Page<WorkflowDefinition> page);

    /**
     * 获取工作流统计信息
     * @return 统计信息
     */
    Map<String, Object> getWorkflowStatistics();
}
