package com.archive.management.service;

import com.archive.management.entity.WorkflowInstance;
import com.archive.management.entity.WorkflowTask;
import com.archive.management.entity.WorkflowDefinition;
import com.archive.management.mapper.WorkflowInstanceMapper;
import com.archive.management.mapper.WorkflowTaskMapper;
import com.archive.management.mapper.WorkflowDefinitionMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工作流服务测试类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Mock
    private WorkflowTaskMapper workflowTaskMapper;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private WorkflowServiceImpl workflowService;

    private WorkflowDefinition testDefinition;
    private WorkflowInstance testInstance;
    private WorkflowTask testTask;
    private Map<String, Object> variables;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testDefinition = new WorkflowDefinition();
        testDefinition.setId(1L);
        testDefinition.setName("测试工作流");
        testDefinition.setCode("TEST_WORKFLOW");
        testDefinition.setBusinessType("archive_approval");
        testDefinition.setStatus("published");
        testDefinition.setIsCurrent(1);

        testInstance = new WorkflowInstance();
        testInstance.setId(1L);
        testInstance.setWorkflowDefinitionId(1L);
        testInstance.setBusinessType("archive_approval");
        testInstance.setBusinessId(1L);
        testInstance.setWorkflowName("测试工作流");
        testInstance.setStatus("running");
        testInstance.setInitiatorId(1L);
        testInstance.setStartTime(LocalDateTime.now());

        testTask = new WorkflowTask();
        testTask.setId(1L);
        testTask.setWorkflowInstanceId(1L);
        testTask.setTaskName("审批任务");
        testTask.setTaskType("user_task");
        testTask.setStatus("pending");
        testTask.setAssigneeId(1L);
        testTask.setCreateTime(LocalDateTime.now());

        variables = new HashMap<>();
        variables.put("title", "测试档案");
        variables.put("priority", "high");
    }

    @Test
    void testStartWorkflow() {
        // 准备测试数据
        when(workflowDefinitionMapper.selectById(1L)).thenReturn(testDefinition);
        when(workflowInstanceMapper.insert(any(WorkflowInstance.class))).thenReturn(1);
        when(workflowTaskMapper.insert(any(WorkflowTask.class))).thenReturn(1);

        // 执行测试
        WorkflowInstance result = workflowService.startWorkflow(1L, "archive_approval", 1L, 1L, variables);

        // 验证结果
        assertNotNull(result);
        assertEquals("archive_approval", result.getBusinessType());
        assertEquals(Long.valueOf(1L), result.getBusinessId());
        assertEquals("running", result.getStatus());

        // 验证方法调用
        verify(workflowDefinitionMapper).selectById(1L);
        verify(workflowInstanceMapper).insert(any(WorkflowInstance.class));
        verify(workflowTaskMapper).insert(any(WorkflowTask.class));
    }

    @Test
    void testCompleteTask() {
        // 准备测试数据
        when(workflowTaskMapper.selectById(1L)).thenReturn(testTask);
        when(workflowTaskMapper.updateById(any(WorkflowTask.class))).thenReturn(1);

        // 执行测试
        boolean result = workflowService.completeTask(1L, 1L, "approved", "审批通过", variables);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowTaskMapper).selectById(1L);
        verify(workflowTaskMapper).updateById(any(WorkflowTask.class));
    }

    @Test
    void testTransferTask() {
        // 准备测试数据
        when(workflowTaskMapper.selectById(1L)).thenReturn(testTask);
        when(workflowTaskMapper.updateById(any(WorkflowTask.class))).thenReturn(1);

        // 执行测试
        boolean result = workflowService.transferTask(1L, 1L, 2L, "工作负载调整");

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowTaskMapper).selectById(1L);
        verify(workflowTaskMapper).updateById(any(WorkflowTask.class));
    }

    @Test
    void testSuspendWorkflow() {
        // 准备测试数据
        when(workflowInstanceMapper.selectById(1L)).thenReturn(testInstance);
        when(workflowInstanceMapper.updateById(any(WorkflowInstance.class))).thenReturn(1);

        // 执行测试
        boolean result = workflowService.suspendWorkflow(1L, "系统维护");

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowInstanceMapper).selectById(1L);
        verify(workflowInstanceMapper).updateById(any(WorkflowInstance.class));
    }

    @Test
    void testResumeWorkflow() {
        // 准备测试数据
        testInstance.setStatus("suspended");
        when(workflowInstanceMapper.selectById(1L)).thenReturn(testInstance);
        when(workflowInstanceMapper.updateById(any(WorkflowInstance.class))).thenReturn(1);

        // 执行测试
        boolean result = workflowService.resumeWorkflow(1L);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowInstanceMapper).selectById(1L);
        verify(workflowInstanceMapper).updateById(any(WorkflowInstance.class));
    }

    @Test
    void testTerminateWorkflow() {
        // 准备测试数据
        when(workflowInstanceMapper.selectById(1L)).thenReturn(testInstance);
        when(workflowInstanceMapper.updateById(any(WorkflowInstance.class))).thenReturn(1);
        when(workflowTaskMapper.selectList(any())).thenReturn(new ArrayList<>());

        // 执行测试
        boolean result = workflowService.terminateWorkflow(1L, "业务取消");

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowInstanceMapper).selectById(1L);
        verify(workflowInstanceMapper).updateById(any(WorkflowInstance.class));
    }

    @Test
    void testGetUserTasks() {
        // 准备测试数据
        List<WorkflowTask> tasks = new ArrayList<>();
        tasks.add(testTask);

        Page<WorkflowTask> page = new Page<>(1, 10);
        IPage<WorkflowTask> expectedPage = new Page<>(1, 10, 1);
        expectedPage.setRecords(tasks);

        when(workflowTaskMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        IPage<WorkflowTask> result = workflowService.getUserTasks(1L, page);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("审批任务", result.getRecords().get(0).getTaskName());

        // 验证方法调用
        verify(workflowTaskMapper).selectPage(any(Page.class), any());
    }

    @Test
    void testGetWorkflowInstance() {
        // 准备测试数据
        when(workflowInstanceMapper.selectById(1L)).thenReturn(testInstance);

        // 执行测试
        WorkflowInstance result = workflowService.getWorkflowInstance(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("测试工作流", result.getWorkflowName());
        assertEquals("running", result.getStatus());

        // 验证方法调用
        verify(workflowInstanceMapper).selectById(1L);
    }

    @Test
    void testGetWorkflowTask() {
        // 准备测试数据
        when(workflowTaskMapper.selectById(1L)).thenReturn(testTask);

        // 执行测试
        WorkflowTask result = workflowService.getWorkflowTask(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("审批任务", result.getTaskName());
        assertEquals("pending", result.getStatus());

        // 验证方法调用
        verify(workflowTaskMapper).selectById(1L);
    }

    @Test
    void testGetWorkflowInstances() {
        // 准备测试数据
        List<WorkflowInstance> instances = new ArrayList<>();
        instances.add(testInstance);

        Page<WorkflowInstance> page = new Page<>(1, 10);
        IPage<WorkflowInstance> expectedPage = new Page<>(1, 10, 1);
        expectedPage.setRecords(instances);

        when(workflowInstanceMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        IPage<WorkflowInstance> result = workflowService.getWorkflowInstances("archive_approval", "running", page);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("测试工作流", result.getRecords().get(0).getWorkflowName());

        // 验证方法调用
        verify(workflowInstanceMapper).selectPage(any(Page.class), any());
    }

    @Test
    void testGetWorkflowHistory() {
        // 执行测试
        List<Map<String, Object>> result = workflowService.getWorkflowHistory(1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testCreateWorkflowDefinition() {
        // 准备测试数据
        when(workflowDefinitionMapper.insert(any(WorkflowDefinition.class))).thenReturn(1);

        // 执行测试
        WorkflowDefinition result = workflowService.createWorkflowDefinition(testDefinition);

        // 验证结果
        assertNotNull(result);
        assertEquals("draft", result.getStatus());
        assertEquals(Integer.valueOf(1), result.getVersion());
        assertEquals(Integer.valueOf(1), result.getIsCurrent());

        // 验证方法调用
        verify(workflowDefinitionMapper).insert(any(WorkflowDefinition.class));
    }

    @Test
    void testPublishWorkflowDefinition() {
        // 准备测试数据
        when(workflowDefinitionMapper.selectById(1L)).thenReturn(testDefinition);
        when(workflowDefinitionMapper.selectOne(any())).thenReturn(null);
        when(workflowDefinitionMapper.updateById(any(WorkflowDefinition.class))).thenReturn(1);

        // 执行测试
        boolean result = workflowService.publishWorkflowDefinition(1L);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowDefinitionMapper).selectById(1L);
        verify(workflowDefinitionMapper).updateById(any(WorkflowDefinition.class));
    }

    @Test
    void testGetWorkflowDefinitions() {
        // 准备测试数据
        List<WorkflowDefinition> definitions = new ArrayList<>();
        definitions.add(testDefinition);

        Page<WorkflowDefinition> page = new Page<>(1, 10);
        IPage<WorkflowDefinition> expectedPage = new Page<>(1, 10, 1);
        expectedPage.setRecords(definitions);

        when(workflowDefinitionMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        IPage<WorkflowDefinition> result = workflowService.getWorkflowDefinitions("archive_approval", "published", page);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("测试工作流", result.getRecords().get(0).getName());

        // 验证方法调用
        verify(workflowDefinitionMapper).selectPage(any(Page.class), any());
    }

    @Test
    void testGetWorkflowStatistics() {
        // 准备测试数据
        when(workflowInstanceMapper.selectCount(any())).thenReturn(10L);
        when(workflowTaskMapper.selectCount(any())).thenReturn(5L);

        // 执行测试
        Map<String, Object> result = workflowService.getWorkflowStatistics();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalInstances"));
        assertTrue(result.containsKey("runningInstances"));
        assertTrue(result.containsKey("completedInstances"));
        assertTrue(result.containsKey("totalTasks"));
        assertTrue(result.containsKey("pendingTasks"));

        // 验证方法调用
        verify(workflowInstanceMapper, atLeastOnce()).selectCount(any());
        verify(workflowTaskMapper, atLeastOnce()).selectCount(any());
    }

    @Test
    void testStartWorkflowWithException() {
        // 准备异常情况
        when(workflowDefinitionMapper.selectById(1L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            workflowService.startWorkflow(1L, "archive_approval", 1L, 1L, variables);
        });
    }

    @Test
    void testCompleteTaskWithException() {
        // 准备异常情况
        when(workflowTaskMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        boolean result = workflowService.completeTask(1L, 1L, "approved", "审批通过", variables);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testTransferTaskWithException() {
        // 准备异常情况
        when(workflowTaskMapper.selectById(1L)).thenReturn(null);

        // 执行测试
        boolean result = workflowService.transferTask(1L, 1L, 2L, "工作负载调整");

        // 验证结果
        assertFalse(result);
    }
}
