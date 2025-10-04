package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.entity.WorkflowInstance;
import com.archive.management.entity.WorkflowTask;
import com.archive.management.entity.WorkflowDefinition;
import com.archive.management.service.WorkflowService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流控制器
 * 提供工作流管理相关API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Tag(name = "工作流管理", description = "工作流管理相关接口")
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * 启动工作流实例
     */
    @PostMapping("/start")
    @Operation(summary = "启动工作流实例", description = "启动新的工作流实例")
    @PreAuthorize("hasAuthority('workflow:start')")
    public ResponseEntity<ApiResponse<WorkflowInstance>> startWorkflow(
            @RequestBody Map<String, Object> request) {
        try {
            Long workflowDefinitionId = Long.valueOf(request.get("workflowDefinitionId").toString());
            String businessType = request.get("businessType").toString();
            Long businessId = Long.valueOf(request.get("businessId").toString());
            Long initiatorId = Long.valueOf(request.get("initiatorId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.get("variables");
            
            WorkflowInstance instance = workflowService.startWorkflow(
                workflowDefinitionId, businessType, businessId, initiatorId, variables);
            
            return ResponseEntity.ok(ApiResponse.success(instance, "工作流启动成功"));
        } catch (Exception e) {
            log.error("启动工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("启动工作流失败: " + e.getMessage()));
        }
    }

    /**
     * 完成任务
     */
    @PostMapping("/task/{taskId}/complete")
    @Operation(summary = "完成任务", description = "完成指定的工作流任务")
    @PreAuthorize("hasAuthority('workflow:complete')")
    public ResponseEntity<ApiResponse<Boolean>> completeTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody Map<String, Object> request) {
        try {
            Long assigneeId = Long.valueOf(request.get("assigneeId").toString());
            String result = request.get("result").toString();
            String comment = request.get("comment") != null ? request.get("comment").toString() : "";
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.get("variables");
            
            boolean success = workflowService.completeTask(taskId, assigneeId, result, comment, variables);
            
            return ResponseEntity.ok(ApiResponse.success(success, "任务完成成功"));
        } catch (Exception e) {
            log.error("完成任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("完成任务失败: " + e.getMessage()));
        }
    }

    /**
     * 转派任务
     */
    @PostMapping("/task/{taskId}/transfer")
    @Operation(summary = "转派任务", description = "将任务转派给其他用户")
    @PreAuthorize("hasAuthority('workflow:transfer')")
    public ResponseEntity<ApiResponse<Boolean>> transferTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody Map<String, Object> request) {
        try {
            Long fromAssigneeId = Long.valueOf(request.get("fromAssigneeId").toString());
            Long toAssigneeId = Long.valueOf(request.get("toAssigneeId").toString());
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";
            
            boolean success = workflowService.transferTask(taskId, fromAssigneeId, toAssigneeId, reason);
            
            return ResponseEntity.ok(ApiResponse.success(success, "任务转派成功"));
        } catch (Exception e) {
            log.error("转派任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("转派任务失败: " + e.getMessage()));
        }
    }

    /**
     * 挂起工作流实例
     */
    @PostMapping("/instance/{instanceId}/suspend")
    @Operation(summary = "挂起工作流实例", description = "挂起指定的工作流实例")
    @PreAuthorize("hasAuthority('workflow:suspend')")
    public ResponseEntity<ApiResponse<Boolean>> suspendWorkflow(
            @Parameter(description = "实例ID") @PathVariable Long instanceId,
            @RequestBody Map<String, Object> request) {
        try {
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";
            
            boolean success = workflowService.suspendWorkflow(instanceId, reason);
            
            return ResponseEntity.ok(ApiResponse.success(success, "工作流挂起成功"));
        } catch (Exception e) {
            log.error("挂起工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("挂起工作流失败: " + e.getMessage()));
        }
    }

    /**
     * 恢复工作流实例
     */
    @PostMapping("/instance/{instanceId}/resume")
    @Operation(summary = "恢复工作流实例", description = "恢复挂起的工作流实例")
    @PreAuthorize("hasAuthority('workflow:resume')")
    public ResponseEntity<ApiResponse<Boolean>> resumeWorkflow(
            @Parameter(description = "实例ID") @PathVariable Long instanceId) {
        try {
            boolean success = workflowService.resumeWorkflow(instanceId);
            
            return ResponseEntity.ok(ApiResponse.success(success, "工作流恢复成功"));
        } catch (Exception e) {
            log.error("恢复工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("恢复工作流失败: " + e.getMessage()));
        }
    }

    /**
     * 终止工作流实例
     */
    @PostMapping("/instance/{instanceId}/terminate")
    @Operation(summary = "终止工作流实例", description = "终止指定的工作流实例")
    @PreAuthorize("hasAuthority('workflow:terminate')")
    public ResponseEntity<ApiResponse<Boolean>> terminateWorkflow(
            @Parameter(description = "实例ID") @PathVariable Long instanceId,
            @RequestBody Map<String, Object> request) {
        try {
            String reason = request.get("reason") != null ? request.get("reason").toString() : "";
            
            boolean success = workflowService.terminateWorkflow(instanceId, reason);
            
            return ResponseEntity.ok(ApiResponse.success(success, "工作流终止成功"));
        } catch (Exception e) {
            log.error("终止工作流失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("终止工作流失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户待办任务
     */
    @GetMapping("/tasks/user/{userId}")
    @Operation(summary = "获取用户待办任务", description = "获取指定用户的待办任务列表")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<IPage<WorkflowTask>>> getUserTasks(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<WorkflowTask> page = new Page<>(current, size);
            IPage<WorkflowTask> tasks = workflowService.getUserTasks(userId, page);
            
            return ResponseEntity.ok(ApiResponse.success(tasks, "获取待办任务成功"));
        } catch (Exception e) {
            log.error("获取用户待办任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取待办任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取工作流实例详情
     */
    @GetMapping("/instance/{instanceId}")
    @Operation(summary = "获取工作流实例详情", description = "获取指定工作流实例的详细信息")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<WorkflowInstance>> getWorkflowInstance(
            @Parameter(description = "实例ID") @PathVariable Long instanceId) {
        try {
            WorkflowInstance instance = workflowService.getWorkflowInstance(instanceId);
            
            if (instance != null) {
                return ResponseEntity.ok(ApiResponse.success(instance, "获取工作流实例成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取工作流实例失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工作流实例失败: " + e.getMessage()));
        }
    }

    /**
     * 获取工作流任务详情
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "获取工作流任务详情", description = "获取指定工作流任务的详细信息")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<WorkflowTask>> getWorkflowTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            WorkflowTask task = workflowService.getWorkflowTask(taskId);
            
            if (task != null) {
                return ResponseEntity.ok(ApiResponse.success(task, "获取工作流任务成功"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取工作流任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工作流任务失败: " + e.getMessage()));
        }
    }

    /**
     * 获取工作流实例列表
     */
    @GetMapping("/instances")
    @Operation(summary = "获取工作流实例列表", description = "获取工作流实例列表")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<IPage<WorkflowInstance>>> getWorkflowInstances(
            @Parameter(description = "业务类型") @RequestParam(required = false) String businessType,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<WorkflowInstance> page = new Page<>(current, size);
            IPage<WorkflowInstance> instances = workflowService.getWorkflowInstances(businessType, status, page);
            
            return ResponseEntity.ok(ApiResponse.success(instances, "获取工作流实例列表成功"));
        } catch (Exception e) {
            log.error("获取工作流实例列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工作流实例列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取工作流历史
     */
    @GetMapping("/instance/{instanceId}/history")
    @Operation(summary = "获取工作流历史", description = "获取指定工作流实例的历史记录")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWorkflowHistory(
            @Parameter(description = "实例ID") @PathVariable Long instanceId) {
        try {
            List<Map<String, Object>> history = workflowService.getWorkflowHistory(instanceId);
            
            return ResponseEntity.ok(ApiResponse.success(history, "获取工作流历史成功"));
        } catch (Exception e) {
            log.error("获取工作流历史失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工作流历史失败: " + e.getMessage()));
        }
    }

    /**
     * 创建工作流定义
     */
    @PostMapping("/definition")
    @Operation(summary = "创建工作流定义", description = "创建新的工作流定义")
    @PreAuthorize("hasAuthority('workflow:create')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> createWorkflowDefinition(
            @RequestBody WorkflowDefinition definition) {
        try {
            WorkflowDefinition createdDefinition = workflowService.createWorkflowDefinition(definition);
            
            return ResponseEntity.ok(ApiResponse.success(createdDefinition, "工作流定义创建成功"));
        } catch (Exception e) {
            log.error("创建工作流定义失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("创建工作流定义失败: " + e.getMessage()));
        }
    }

    /**
     * 发布工作流定义
     */
    @PostMapping("/definition/{definitionId}/publish")
    @Operation(summary = "发布工作流定义", description = "发布工作流定义")
    @PreAuthorize("hasAuthority('workflow:publish')")
    public ResponseEntity<ApiResponse<Boolean>> publishWorkflowDefinition(
            @Parameter(description = "定义ID") @PathVariable Long definitionId) {
        try {
            boolean success = workflowService.publishWorkflowDefinition(definitionId);
            
            return ResponseEntity.ok(ApiResponse.success(success, "工作流定义发布成功"));
        } catch (Exception e) {
            log.error("发布工作流定义失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("发布工作流定义失败: " + e.getMessage()));
        }
    }

    /**
     * 获取工作流定义列表
     */
    @GetMapping("/definitions")
    @Operation(summary = "获取工作流定义列表", description = "获取工作流定义列表")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<IPage<WorkflowDefinition>>> getWorkflowDefinitions(
            @Parameter(description = "业务类型") @RequestParam(required = false) String businessType,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<WorkflowDefinition> page = new Page<>(current, size);
            IPage<WorkflowDefinition> definitions = workflowService.getWorkflowDefinitions(businessType, status, page);
            
            return ResponseEntity.ok(ApiResponse.success(definitions, "获取工作流定义列表成功"));
        } catch (Exception e) {
            log.error("获取工作流定义列表失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工作流定义列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取工作流统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取工作流统计信息", description = "获取工作流统计信息")
    @PreAuthorize("hasAuthority('workflow:view')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWorkflowStatistics() {
        try {
            Map<String, Object> statistics = workflowService.getWorkflowStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取工作流统计成功"));
        } catch (Exception e) {
            log.error("获取工作流统计失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取工作流统计失败: " + e.getMessage()));
        }
    }
}
