package com.archive.management.service;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批量操作服务测试类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
class BatchOperationServiceTest {

    @Mock
    private ArchiveMapper archiveMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private BatchOperationService batchOperationService;

    private List<Long> archiveIds;
    private List<Long> userIds;
    private Archive testArchive;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        archiveIds = new ArrayList<>();
        archiveIds.add(1L);
        archiveIds.add(2L);
        archiveIds.add(3L);

        userIds = new ArrayList<>();
        userIds.add(1L);
        userIds.add(2L);

        testArchive = new Archive();
        testArchive.setId(1L);
        testArchive.setTitle("测试档案");
        testArchive.setArchiveNumber("TEST-001");
        testArchive.setStatus(1);
        testArchive.setDeleted(0);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setDeleted(0);
    }

    @Test
    void testBatchUpdateArchiveStatus() {
        // 准备测试数据
        when(archiveMapper.update(any(), any(UpdateWrapper.class))).thenReturn(3);

        // 执行测试
        Map<String, Object> result = batchOperationService.batchUpdateArchiveStatus(archiveIds, 2);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(3, result.get("total"));
        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));

        // 验证方法调用
        verify(archiveMapper).update(any(), any(UpdateWrapper.class));
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testBatchDeleteArchives() {
        // 准备测试数据
        when(archiveMapper.update(any(), any(UpdateWrapper.class))).thenReturn(3);

        // 执行测试
        Map<String, Object> result = batchOperationService.batchDeleteArchives(archiveIds, 1L);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(3, result.get("total"));
        assertEquals(3, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));

        // 验证方法调用
        verify(archiveMapper).update(any(), any(UpdateWrapper.class));
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testBatchUpdateUserStatus() {
        // 准备测试数据
        when(userMapper.update(any(), any(UpdateWrapper.class))).thenReturn(2);

        // 执行测试
        Map<String, Object> result = batchOperationService.batchUpdateUserStatus(userIds, 0);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("total"));
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));

        // 验证方法调用
        verify(userMapper).update(any(), any(UpdateWrapper.class));
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testBatchImportArchives() throws Exception {
        // 准备测试数据
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        when(archiveMapper.insert(any(Archive.class))).thenReturn(1);

        // 执行测试
        CompletableFuture<Map<String, Object>> future = batchOperationService.batchImportArchives(multipartFile, 1L);
        Map<String, Object> result = future.get();

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));

        // 验证方法调用
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testBatchImportUsers() throws Exception {
        // 准备测试数据
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        CompletableFuture<Map<String, Object>> future = batchOperationService.batchImportUsers(multipartFile, 1L);
        Map<String, Object> result = future.get();

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));

        // 验证方法调用
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testBatchExportArchives() {
        // 准备测试数据
        List<Archive> archives = new ArrayList<>();
        archives.add(testArchive);

        when(archiveMapper.selectList(any())).thenReturn(archives);

        // 执行测试
        byte[] result = batchOperationService.batchExportArchives(archiveIds, "excel");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证方法调用
        verify(archiveMapper).selectList(any());
    }

    @Test
    void testBatchExportUsers() {
        // 准备测试数据
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(userMapper.selectList(any())).thenReturn(users);

        // 执行测试
        byte[] result = batchOperationService.batchExportUsers(userIds, "csv");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证方法调用
        verify(userMapper).selectList(any());
    }

    @Test
    void testBatchAssignRoles() {
        // 准备测试数据
        List<Long> roleIds = new ArrayList<>();
        roleIds.add(1L);
        roleIds.add(2L);

        when(userMapper.update(any(), any(UpdateWrapper.class))).thenReturn(2);

        // 执行测试
        Map<String, Object> result = batchOperationService.batchAssignRoles(userIds, roleIds);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("total"));
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));

        // 验证方法调用
        verify(userMapper).update(any(), any(UpdateWrapper.class));
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testBatchAssignPermissions() {
        // 准备测试数据
        List<Long> permissionIds = new ArrayList<>();
        permissionIds.add(1L);
        permissionIds.add(2L);

        when(userMapper.update(any(), any(UpdateWrapper.class))).thenReturn(2);

        // 执行测试
        Map<String, Object> result = batchOperationService.batchAssignPermissions(userIds, permissionIds);

        // 验证结果
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("total"));
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failedCount"));

        // 验证方法调用
        verify(userMapper).update(any(), any(UpdateWrapper.class));
        verify(monitoringService).recordBatchOperation();
    }

    @Test
    void testGenerateArchiveImportTemplate() {
        // 执行测试
        byte[] result = batchOperationService.generateArchiveImportTemplate();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGenerateUserImportTemplate() {
        // 执行测试
        byte[] result = batchOperationService.generateUserImportTemplate();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testBatchUpdateArchiveStatusWithException() {
        // 准备异常情况
        when(archiveMapper.update(any(), any(UpdateWrapper.class))).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        Map<String, Object> result = batchOperationService.batchUpdateArchiveStatus(archiveIds, 2);

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testBatchImportArchivesWithIOException() throws Exception {
        // 准备异常情况
        when(multipartFile.getInputStream()).thenThrow(new IOException("文件读取失败"));

        // 执行测试
        CompletableFuture<Map<String, Object>> future = batchOperationService.batchImportArchives(multipartFile, 1L);
        Map<String, Object> result = future.get();

        // 验证结果
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.containsKey("error"));
    }

    @Test
    void testBatchExportArchivesWithUnsupportedFormat() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            batchOperationService.batchExportArchives(archiveIds, "unsupported");
        });
    }
}
