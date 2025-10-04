package com.archive.management.service.impl;

import com.archive.management.entity.WorkflowInstance;
import com.archive.management.entity.WorkflowTask;
import com.archive.management.entity.WorkflowDefinition;
import com.archive.management.mapper.WorkflowInstanceMapper;
import com.archive.management.mapper.WorkflowTaskMapper;
import com.archive.management.mapper.WorkflowDefinitionMapper;
import com.archive.management.service.WorkflowService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流服务实现类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowDefinitionMapper workflowDefinitionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WorkflowInstance startWorkflow(Long workflowDefinitionId, String businessType, Long businessId, 
                                         Long initiatorId, Map<String, Object> variables) {
        try {
            // 获取工作流定义
            WorkflowDefinition definition = workflowDefinitionMapper.selectById(workflowDefinitionId);
            if (definition == null) {
                throw new RuntimeException("工作流定义不存在");
            }

            // 创建工作流实例
            WorkflowInstance instance = new WorkflowInstance();
            instance.setWorkflowDefinitionId(workflowDefinitionId);
            instance.setBusinessType(businessType);
            instance.setBusinessId(businessId);
            instance.setWorkflowName(definition.getName());
            instance.setStatus("running");
            instance.setPriority(3); // 默认优先级
            instance.setInitiatorId(initiatorId);
            instance.setStartTime(LocalDateTime.now());
            instance.setExpectedEndTime(LocalDateTime.now().plusDays(7)); // 默认7天完成
            
            // 设置变量
            if (variables != null && !variables.isEmpty()) {
                instance.setVariables(convertToJson(variables));
            }

            workflowInstanceMapper.insert(instance);

            // 创建第一个任务
            createFirstTask(instance);

            log.info("工作流实例启动成功: instanceId={}, businessType={}, businessId={}", 
                    instance.getId(), businessType, businessId);
            
            return instance;
        } catch (Exception e) {
            log.error("启动工作流失败", e);
            throw new RuntimeException("启动工作流失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean completeTask(Long taskId, Long assigneeId, String result, String comment, 
                               Map<String, Object> variables) {
        try {
            // 获取任务
            WorkflowTask task = workflowTaskMapper.selectById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }

            // 检查权限
            if (!task.getAssigneeId().equals(assigneeId)) {
                throw new RuntimeException("无权限处理此任务");
            }

            // 更新任务状态
            task.setStatus("completed");
            task.setResult(result);
            task.setComment(comment);
            task.setEndTime(LocalDateTime.now());
            
            if (variables != null && !variables.isEmpty()) {
                task.setVariables(convertToJson(variables));
            }

            workflowTaskMapper.updateById(task);

            // 处理工作流流转
            processWorkflowFlow(task);

            log.info("任务完成成功: taskId={}, assigneeId={}", taskId, assigneeId);
            return true;
        } catch (Exception e) {
            log.error("完成任务失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean transferTask(Long taskId, Long fromAssigneeId, Long toAssigneeId, String reason) {
        try {
            // 获取任务
            WorkflowTask task = workflowTaskMapper.selectById(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }

            // 检查权限
            if (!task.getAssigneeId().equals(fromAssigneeId)) {
                throw new RuntimeException("无权限转派此任务");
            }

            // 更新任务指派人
            task.setAssigneeId(toAssigneeId);
            task.setComment(task.getComment() + "\n转派原因: " + reason);
            task.setUpdateTime(LocalDateTime.now());

            workflowTaskMapper.updateById(task);

            log.info("任务转派成功: taskId={}, fromAssigneeId={}, toAssigneeId={}", 
                    taskId, fromAssigneeId, toAssigneeId);
            return true;
        } catch (Exception e) {
            log.error("转派任务失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean suspendWorkflow(Long instanceId, String reason) {
        try {
            WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
            if (instance == null) {
                throw new RuntimeException("工作流实例不存在");
            }

            instance.setStatus("suspended");
            instance.setRemark(instance.getRemark() + "\n挂起原因: " + reason);
            instance.setUpdateTime(LocalDateTime.now());

            workflowInstanceMapper.updateById(instance);

            log.info("工作流实例挂起成功: instanceId={}", instanceId);
            return true;
        } catch (Exception e) {
            log.error("挂起工作流失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resumeWorkflow(Long instanceId) {
        try {
            WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
            if (instance == null) {
                throw new RuntimeException("工作流实例不存在");
            }

            instance.setStatus("running");
            instance.setUpdateTime(LocalDateTime.now());

            workflowInstanceMapper.updateById(instance);

            log.info("工作流实例恢复成功: instanceId={}", instanceId);
            return true;
        } catch (Exception e) {
            log.error("恢复工作流失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean terminateWorkflow(Long instanceId, String reason) {
        try {
            WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
            if (instance == null) {
                throw new RuntimeException("工作流实例不存在");
            }

            instance.setStatus("cancelled");
            instance.setEndTime(LocalDateTime.now());
            instance.setRemark(instance.getRemark() + "\n终止原因: " + reason);
            instance.setUpdateTime(LocalDateTime.now());

            workflowInstanceMapper.updateById(instance);

            // 取消所有未完成的任务
            QueryWrapper<WorkflowTask> taskWrapper = new QueryWrapper<>();
            taskWrapper.eq("workflow_instance_id", instanceId)
                      .in("status", "pending", "running");
            
            List<WorkflowTask> tasks = workflowTaskMapper.selectList(taskWrapper);
            for (WorkflowTask task : tasks) {
                task.setStatus("cancelled");
                task.setEndTime(LocalDateTime.now());
                task.setComment(task.getComment() + "\n工作流已终止");
                workflowTaskMapper.updateById(task);
            }

            log.info("工作流实例终止成功: instanceId={}", instanceId);
            return true;
        } catch (Exception e) {
            log.error("终止工作流失败", e);
            return false;
        }
    }

    @Override
    public IPage<WorkflowTask> getUserTasks(Long userId, Page<WorkflowTask> page) {
        QueryWrapper<WorkflowTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assignee_id", userId)
                   .in("status", "pending", "running")
                   .orderByDesc("create_time");
        
        return workflowTaskMapper.selectPage(page, queryWrapper);
    }

    @Override
    public WorkflowInstance getWorkflowInstance(Long instanceId) {
        return workflowInstanceMapper.selectById(instanceId);
    }

    @Override
    public WorkflowTask getWorkflowTask(Long taskId) {
        return workflowTaskMapper.selectById(taskId);
    }

    @Override
    public IPage<WorkflowInstance> getWorkflowInstances(String businessType, String status, Page<WorkflowInstance> page) {
        QueryWrapper<WorkflowInstance> queryWrapper = new QueryWrapper<>();
        
        if (businessType != null && !businessType.isEmpty()) {
            queryWrapper.eq("business_type", businessType);
        }
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return workflowInstanceMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getWorkflowHistory(Long instanceId) {
        // 这里可以从工作流历史表中获取历史记录
        // 暂时返回示例数据
        List<Map<String, Object>> history = new ArrayList<>();
        
        Map<String, Object> record = new HashMap<>();
        record.put("id", "1");
        record.put("action", "启动工作流");
        record.put("operator", "张三");
        record.put("time", LocalDateTime.now().minusHours(2));
        record.put("comment", "提交档案审批申请");
        history.add(record);
        
        return history;
    }

    @Override
    @Transactional
    public WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition) {
        definition.setStatus("draft");
        definition.setVersion(1);
        definition.setIsCurrent(1);
        definition.setCreateTime(LocalDateTime.now());
        definition.setUpdateTime(LocalDateTime.now());
        
        workflowDefinitionMapper.insert(definition);
        
        log.info("工作流定义创建成功: definitionId={}, name={}", definition.getId(), definition.getName());
        return definition;
    }

    @Override
    @Transactional
    public boolean publishWorkflowDefinition(Long definitionId) {
        try {
            WorkflowDefinition definition = workflowDefinitionMapper.selectById(definitionId);
            if (definition == null) {
                throw new RuntimeException("工作流定义不存在");
            }

            // 将当前版本设为非当前
            QueryWrapper<WorkflowDefinition> wrapper = new QueryWrapper<>();
            wrapper.eq("code", definition.getCode())
                   .eq("is_current", 1);
            
            WorkflowDefinition currentVersion = workflowDefinitionMapper.selectOne(wrapper);
            if (currentVersion != null) {
                currentVersion.setIsCurrent(0);
                workflowDefinitionMapper.updateById(currentVersion);
            }

            // 发布新版本
            definition.setStatus("published");
            definition.setIsCurrent(1);
            definition.setUpdateTime(LocalDateTime.now());
            
            workflowDefinitionMapper.updateById(definition);

            log.info("工作流定义发布成功: definitionId={}", definitionId);
            return true;
        } catch (Exception e) {
            log.error("发布工作流定义失败", e);
            return false;
        }
    }

    @Override
    public IPage<WorkflowDefinition> getWorkflowDefinitions(String businessType, String status, Page<WorkflowDefinition> page) {
        QueryWrapper<WorkflowDefinition> queryWrapper = new QueryWrapper<>();
        
        if (businessType != null && !businessType.isEmpty()) {
            queryWrapper.eq("business_type", businessType);
        }
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.eq("is_current", 1)
                   .orderByDesc("create_time");
        
        return workflowDefinitionMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Map<String, Object> getWorkflowStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 总实例数
            long totalInstances = workflowInstanceMapper.selectCount(null);
            statistics.put("totalInstances", totalInstances);
            
            // 运行中实例数
            QueryWrapper<WorkflowInstance> runningWrapper = new QueryWrapper<>();
            runningWrapper.eq("status", "running");
            long runningInstances = workflowInstanceMapper.selectCount(runningWrapper);
            statistics.put("runningInstances", runningInstances);
            
            // 已完成实例数
            QueryWrapper<WorkflowInstance> completedWrapper = new QueryWrapper<>();
            completedWrapper.eq("status", "completed");
            long completedInstances = workflowInstanceMapper.selectCount(completedWrapper);
            statistics.put("completedInstances", completedInstances);
            
            // 总任务数
            long totalTasks = workflowTaskMapper.selectCount(null);
            statistics.put("totalTasks", totalTasks);
            
            // 待办任务数
            QueryWrapper<WorkflowTask> pendingWrapper = new QueryWrapper<>();
            pendingWrapper.eq("status", "pending");
            long pendingTasks = workflowTaskMapper.selectCount(pendingWrapper);
            statistics.put("pendingTasks", pendingTasks);
            
        } catch (Exception e) {
            log.error("获取工作流统计失败", e);
        }
        
        return statistics;
    }

    /**
     * 创建第一个任务
     */
    private void createFirstTask(WorkflowInstance instance) {
        WorkflowTask task = new WorkflowTask();
        task.setWorkflowInstanceId(instance.getId());
        task.setTaskName("审批任务");
        task.setTaskType("user_task");
        task.setNodeId("approval_node");
        task.setNodeName("审批节点");
        task.setStatus("pending");
        task.setPriority(instance.getPriority());
        task.setCreateTime(LocalDateTime.now());
        task.setExpectedEndTime(instance.getExpectedEndTime());
        
        workflowTaskMapper.insert(task);
    }

    /**
     * 处理工作流流转
     */
    private void processWorkflowFlow(WorkflowTask completedTask) {
        // 这里可以实现复杂的工作流流转逻辑
        // 根据BPMN定义和任务结果决定下一步操作
        
        // 示例：如果审批通过，创建下一个任务
        if ("approved".equals(completedTask.getResult())) {
            createNextTask(completedTask);
        } else if ("rejected".equals(completedTask.getResult())) {
            // 如果审批拒绝，结束工作流
            endWorkflow(completedTask.getWorkflowInstanceId());
        }
    }

    /**
     * 创建下一个任务
     */
    private void createNextTask(WorkflowTask previousTask) {
        // 这里可以根据工作流定义创建下一个任务
        // 暂时跳过实现
    }

    /**
     * 结束工作流
     */
    private void endWorkflow(Long instanceId) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance != null) {
            instance.setStatus("completed");
            instance.setEndTime(LocalDateTime.now());
            instance.setUpdateTime(LocalDateTime.now());
            workflowInstanceMapper.updateById(instance);
        }
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(Map<String, Object> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            log.error("转换变量为JSON失败", e);
            return "{}";
        }
    }
}
