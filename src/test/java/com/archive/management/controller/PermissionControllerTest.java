package com.archive.management.controller;

import com.archive.management.BaseTest;
import com.archive.management.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PermissionController API测试
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@WebMvcTest(PermissionController.class)
@DisplayName("PermissionController API测试")
class PermissionControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("权限同步 - 成功场景")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldSyncPermissionsSuccessfully() throws Exception {
        // Given
        String source = "all";
        Long syncBy = 1L;
        int expectedSyncCount = 15;
        
        given(permissionService.syncPermissions(source, syncBy))
            .willReturn(expectedSyncCount);

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("权限同步成功"))
                .andExpect(jsonPath("$.data.syncCount").value(expectedSyncCount))
                .andExpect(jsonPath("$.data.source").value(source))
                .andExpect(jsonPath("$.data.duration").exists())
                .andExpect(jsonPath("$.data.timestamp").exists());

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }

    @Test
    @DisplayName("权限同步 - 默认参数")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldSyncPermissionsWithDefaultSource() throws Exception {
        // Given
        Long syncBy = 1L;
        int expectedSyncCount = 10;
        
        given(permissionService.syncPermissions("all", syncBy))
            .willReturn(expectedSyncCount);

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.source").value("all"));

        // Verify
        then(permissionService).should().syncPermissions("all", syncBy);
    }

    @Test
    @DisplayName("权限同步 - 指定注解源")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldSyncPermissionsFromAnnotationSource() throws Exception {
        // Given
        String source = "annotation";
        Long syncBy = 2L;
        int expectedSyncCount = 8;
        
        given(permissionService.syncPermissions(source, syncBy))
            .willReturn(expectedSyncCount);

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value(source))
                .andExpect(jsonPath("$.data.syncCount").value(expectedSyncCount));

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }

    @Test
    @DisplayName("权限同步 - 指定配置源")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldSyncPermissionsFromConfigSource() throws Exception {
        // Given
        String source = "config";
        Long syncBy = 3L;
        int expectedSyncCount = 12;
        
        given(permissionService.syncPermissions(source, syncBy))
            .willReturn(expectedSyncCount);

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value(source))
                .andExpect(jsonPath("$.data.syncCount").value(expectedSyncCount));

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }

    @Test
    @DisplayName("权限同步 - 缺少权限")
    @WithMockUser(authorities = {"user:read"})
    void shouldRejectSyncWithoutPermission() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("syncBy", "1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Verify
        then(permissionService).should(never()).syncPermissions(any(), any());
    }

    @Test
    @DisplayName("权限同步 - 未认证用户")
    void shouldRejectSyncForUnauthenticatedUser() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("syncBy", "1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Verify
        then(permissionService).should(never()).syncPermissions(any(), any());
    }

    @Test
    @DisplayName("权限同步 - 缺少必需参数")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldRejectSyncWithoutRequiredParameter() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", "all")
                // 缺少 syncBy 参数
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Verify
        then(permissionService).should(never()).syncPermissions(any(), any());
    }

    @Test
    @DisplayName("权限同步 - 无效参数值")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldRejectSyncWithInvalidParameter() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", "all")
                .param("syncBy", "-1") // 负数无效
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Verify
        then(permissionService).should(never()).syncPermissions(any(), any());
    }

    @Test
    @DisplayName("权限同步 - 服务异常")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldHandleServiceException() throws Exception {
        // Given
        String source = "all";
        Long syncBy = 1L;
        String errorMessage = "数据库连接失败";
        
        given(permissionService.syncPermissions(source, syncBy))
            .willThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("权限同步失败：" + errorMessage));

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }

    @Test
    @DisplayName("权限同步 - 空结果")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldHandleEmptySyncResult() throws Exception {
        // Given
        String source = "annotation";
        Long syncBy = 1L;
        int expectedSyncCount = 0; // 没有权限需要同步
        
        given(permissionService.syncPermissions(source, syncBy))
            .willReturn(expectedSyncCount);

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.syncCount").value(0));

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }

    @Test
    @DisplayName("权限同步 - 大量权限同步")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldHandleLargeSyncOperation() throws Exception {
        // Given
        String source = "all";
        Long syncBy = 1L;
        int expectedSyncCount = 500; // 大量权限
        
        given(permissionService.syncPermissions(source, syncBy))
            .willReturn(expectedSyncCount);

        // When & Then
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.syncCount").value(expectedSyncCount));

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }

    @Test
    @DisplayName("权限同步 - 并发请求")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldHandleConcurrentSyncRequests() throws Exception {
        // Given
        String source = "all";
        Long syncBy1 = 1L;
        Long syncBy2 = 2L;
        int expectedSyncCount = 10;
        
        given(permissionService.syncPermissions(source, syncBy1))
            .willReturn(expectedSyncCount);
        given(permissionService.syncPermissions(source, syncBy2))
            .willReturn(expectedSyncCount);

        // When & Then - 模拟并发请求
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy1.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy2.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy1);
        then(permissionService).should().syncPermissions(source, syncBy2);
    }

    @Test
    @DisplayName("权限同步 - 响应时间测试")
    @WithMockUser(authorities = {"permission:sync"})
    void shouldCompleteWithinReasonableTime() throws Exception {
        // Given
        String source = "all";
        Long syncBy = 1L;
        int expectedSyncCount = 50;
        
        given(permissionService.syncPermissions(source, syncBy))
            .willReturn(expectedSyncCount);

        // When
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/v1/permissions/sync")
                .param("source", source)
                .param("syncBy", syncBy.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duration").exists());

        long duration = System.currentTimeMillis() - startTime;
        
        // Then - 验证响应时间合理（小于5秒）
        assert duration < 5000 : "API响应时间过长: " + duration + "ms";

        // Verify
        then(permissionService).should().syncPermissions(source, syncBy);
    }
}
