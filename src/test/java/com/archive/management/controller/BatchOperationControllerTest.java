package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.service.BatchOperationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批量操作控制器测试类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
class BatchOperationControllerTest {

    @Mock
    private BatchOperationService batchOperationService;

    @InjectMocks
    private BatchOperationController batchOperationController;

    private ObjectMapper objectMapper;
    private Map<String, Object> request;
    private List<Long> ids;
    private Map<String, Object> result;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // 初始化测试数据
        ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);

        request = new HashMap<>();
        request.put("archiveIds", ids);
        request.put("status", 2);
        request.put("deletedBy", 1L);

        result = new HashMap<>();
        result.put("success", true);
        result.put("total", 3);
        result.put("successCount", 3);
        result.put("failedCount", 0);
    }

    @Test
    void testBatchUpdateArchiveStatus() throws Exception {
        // 准备测试数据
        when(batchOperationService.batchUpdateArchiveStatus(anyList(), anyInt())).thenReturn(result);

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchUpdateArchiveStatus(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchUpdateArchiveStatus(anyList(), anyInt());
    }

    @Test
    void testBatchDeleteArchives() throws Exception {
        // 准备测试数据
        when(batchOperationService.batchDeleteArchives(anyList(), anyLong())).thenReturn(result);

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchDeleteArchives(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchDeleteArchives(anyList(), anyLong());
    }

    @Test
    void testBatchUpdateUserStatus() throws Exception {
        // 准备测试数据
        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put("userIds", ids);
        userRequest.put("status", 0);

        when(batchOperationService.batchUpdateUserStatus(anyList(), anyInt())).thenReturn(result);

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchUpdateUserStatus(userRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchUpdateUserStatus(anyList(), anyInt());
    }

    @Test
    void testBatchImportArchives() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "test data".getBytes()
        );

        when(batchOperationService.batchImportArchives(any(MultipartFile.class), anyLong()))
            .thenReturn(CompletableFuture.completedFuture(result));

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchImportArchives(file, 1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchImportArchives(any(MultipartFile.class), anyLong());
    }

    @Test
    void testBatchImportUsers() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            "test data".getBytes()
        );

        when(batchOperationService.batchImportUsers(any(MultipartFile.class), anyLong()))
            .thenReturn(CompletableFuture.completedFuture(result));

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchImportUsers(file, 1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchImportUsers(any(MultipartFile.class), anyLong());
    }

    @Test
    void testBatchExportArchives() throws Exception {
        // 准备测试数据
        byte[] fileContent = "Excel file content".getBytes();
        when(batchOperationService.batchExportArchives(anyList(), anyString())).thenReturn(fileContent);

        // 执行测试
        ResponseEntity<byte[]> response = batchOperationController.batchExportArchives(request, "excel");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        // 验证方法调用
        verify(batchOperationService).batchExportArchives(anyList(), anyString());
    }

    @Test
    void testBatchExportUsers() throws Exception {
        // 准备测试数据
        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put("userIds", ids);

        byte[] fileContent = "CSV file content".getBytes();
        when(batchOperationService.batchExportUsers(anyList(), anyString())).thenReturn(fileContent);

        // 执行测试
        ResponseEntity<byte[]> response = batchOperationController.batchExportUsers(userRequest, "csv");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        // 验证方法调用
        verify(batchOperationService).batchExportUsers(anyList(), anyString());
    }

    @Test
    void testBatchAssignRoles() throws Exception {
        // 准备测试数据
        Map<String, Object> roleRequest = new HashMap<>();
        roleRequest.put("userIds", ids);
        List<Long> roleIds = new ArrayList<>();
        roleIds.add(1L);
        roleIds.add(2L);
        roleRequest.put("roleIds", roleIds);

        when(batchOperationService.batchAssignRoles(anyList(), anyList())).thenReturn(result);

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchAssignRoles(roleRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchAssignRoles(anyList(), anyList());
    }

    @Test
    void testBatchAssignPermissions() throws Exception {
        // 准备测试数据
        Map<String, Object> permissionRequest = new HashMap<>();
        permissionRequest.put("userIds", ids);
        List<Long> permissionIds = new ArrayList<>();
        permissionIds.add(1L);
        permissionIds.add(2L);
        permissionRequest.put("permissionIds", permissionIds);

        when(batchOperationService.batchAssignPermissions(anyList(), anyList())).thenReturn(result);

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchAssignPermissions(permissionRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getData().get("total"));

        // 验证方法调用
        verify(batchOperationService).batchAssignPermissions(anyList(), anyList());
    }

    @Test
    void testGetArchiveImportTemplate() throws Exception {
        // 准备测试数据
        byte[] template = "Template content".getBytes();
        when(batchOperationService.generateArchiveImportTemplate()).thenReturn(template);

        // 执行测试
        ResponseEntity<byte[]> response = batchOperationController.getArchiveImportTemplate();

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        // 验证方法调用
        verify(batchOperationService).generateArchiveImportTemplate();
    }

    @Test
    void testGetUserImportTemplate() throws Exception {
        // 准备测试数据
        byte[] template = "User template content".getBytes();
        when(batchOperationService.generateUserImportTemplate()).thenReturn(template);

        // 执行测试
        ResponseEntity<byte[]> response = batchOperationController.getUserImportTemplate();

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        // 验证方法调用
        verify(batchOperationService).generateUserImportTemplate();
    }

    @Test
    void testGetBatchOperationProgress() throws Exception {
        // 准备测试数据
        Map<String, Object> progress = new HashMap<>();
        progress.put("taskId", "task-123");
        progress.put("status", "completed");
        progress.put("progress", 100);
        progress.put("message", "批量操作已完成");

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.getBatchOperationProgress("task-123");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("task-123", response.getBody().getData().get("taskId"));
    }

    @Test
    void testCancelBatchOperation() throws Exception {
        // 执行测试
        ResponseEntity<ApiResponse<String>> response = batchOperationController.cancelBatchOperation("task-123");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("操作已取消", response.getBody().getData());
    }

    @Test
    void testGetBatchOperationHistory() throws Exception {
        // 准备测试数据
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", "1");
        record.put("operation", "批量导入档案");
        record.put("status", "completed");
        record.put("createTime", "2024-01-20 10:00:00");
        record.put("operator", "admin");
        record.put("totalCount", 100);
        record.put("successCount", 95);
        record.put("failedCount", 5);
        history.add(record);

        // 执行测试
        ResponseEntity<ApiResponse<List<Map<String, Object>>>> response = batchOperationController.getBatchOperationHistory(1, 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("批量导入档案", response.getBody().getData().get(0).get("operation"));
    }

    @Test
    void testBatchUpdateArchiveStatusWithException() throws Exception {
        // 准备异常情况
        when(batchOperationService.batchUpdateArchiveStatus(anyList(), anyInt()))
            .thenThrow(new RuntimeException("批量更新失败"));

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchUpdateArchiveStatus(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("批量更新失败"));
    }

    @Test
    void testBatchImportArchivesWithEmptyFile() throws Exception {
        // 准备测试数据
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            new byte[0]
        );

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = batchOperationController.batchImportArchives(emptyFile, 1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("文件不能为空"));
    }
}
