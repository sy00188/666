package com.archive.management.controller;

import com.archive.management.BaseIntegrationTest;
import com.archive.management.dto.ArchiveDTO;
import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

/**
 * 档案控制器集成测试类
 * 测试档案管理API的各种场景
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@DisplayName("档案控制器集成测试")
class ArchiveControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("创建档案 - 成功")
    void createArchive_Success() throws Exception {
        // Given
        ObjectNode request = objectMapper.createObjectNode();
        request.put("title", "新档案");
        request.put("code", "NEW-001");
        request.put("description", "新创建的档案");
        request.put("status", "ACTIVE");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(postRequest("/api/archives", requestBody)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.title").value("新档案"))
                .andExpect(jsonPath("$.data.code").value("NEW-001"));
    }

    @Test
    @DisplayName("创建档案 - 未认证")
    void createArchive_Unauthorized() throws Exception {
        // Given
        ObjectNode request = objectMapper.createObjectNode();
        request.put("title", "新档案");
        request.put("code", "NEW-001");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(postRequest("/api/archives", requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("创建档案 - 权限不足")
    void createArchive_Forbidden() throws Exception {
        // Given
        ObjectNode request = objectMapper.createObjectNode();
        request.put("title", "新档案");
        request.put("code", "NEW-001");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(postRequest("/api/archives", requestBody)
                .with(user("testuser").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("创建档案 - 参数验证失败")
    void createArchive_ValidationFailed() throws Exception {
        // Given
        ObjectNode request = objectMapper.createObjectNode();
        request.put("title", ""); // 空标题
        request.put("code", "NEW-001");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(postRequest("/api/archives", requestBody)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数验证失败"));
    }

    @Test
    @DisplayName("根据ID获取档案 - 成功")
    void getArchiveById_Success() throws Exception {
        // Given
        Archive testArchive = createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/" + testArchive.getId())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testArchive.getId()))
                .andExpect(jsonPath("$.data.title").value(testArchive.getTitle()));
    }

    @Test
    @DisplayName("根据ID获取档案 - 档案不存在")
    void getArchiveById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(getRequest("/api/archives/999")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("档案不存在"));
    }

    @Test
    @DisplayName("根据编号获取档案 - 成功")
    void getArchiveByCode_Success() throws Exception {
        // Given
        Archive testArchive = createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/code/" + testArchive.getCode())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value(testArchive.getCode()));
    }

    @Test
    @DisplayName("更新档案 - 成功")
    void updateArchive_Success() throws Exception {
        // Given
        Archive testArchive = createTestArchive();
        
        ObjectNode request = objectMapper.createObjectNode();
        request.put("title", "更新后的档案");
        request.put("description", "更新后的描述");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(putRequest("/api/archives/" + testArchive.getId(), requestBody)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("更新后的档案"));
    }

    @Test
    @DisplayName("更新档案 - 档案不存在")
    void updateArchive_NotFound() throws Exception {
        // Given
        ObjectNode request = objectMapper.createObjectNode();
        request.put("title", "更新后的档案");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(putRequest("/api/archives/999", requestBody)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("档案不存在"));
    }

    @Test
    @DisplayName("删除档案 - 成功")
    void deleteArchive_Success() throws Exception {
        // Given
        Archive testArchive = createTestArchive();

        // When & Then
        mockMvc.perform(deleteRequest("/api/archives/" + testArchive.getId())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    @DisplayName("删除档案 - 档案不存在")
    void deleteArchive_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(deleteRequest("/api/archives/999")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("档案不存在"));
    }

    @Test
    @DisplayName("分页获取档案列表 - 成功")
    void getArchiveList_Success() throws Exception {
        // Given
        createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives?page=0&size=10")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").exists());
    }

    @Test
    @DisplayName("根据状态获取档案列表 - 成功")
    void getArchivesByStatus_Success() throws Exception {
        // Given
        createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/status/ACTIVE")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("搜索档案 - 成功")
    void searchArchives_Success() throws Exception {
        // Given
        createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/search?keyword=测试")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("激活档案 - 成功")
    void activateArchive_Success() throws Exception {
        // Given
        Archive testArchive = createTestArchive();

        // When & Then
        mockMvc.perform(postRequest("/api/archives/" + testArchive.getId() + "/activate", "{}")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("激活成功"));
    }

    @Test
    @DisplayName("停用档案 - 成功")
    void deactivateArchive_Success() throws Exception {
        // Given
        Archive testArchive = createTestArchive();

        // When & Then
        mockMvc.perform(postRequest("/api/archives/" + testArchive.getId() + "/deactivate", "{}")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("停用成功"));
    }

    @Test
    @DisplayName("批量更新档案状态 - 成功")
    void batchUpdateArchiveStatus_Success() throws Exception {
        // Given
        Archive testArchive1 = createTestArchive();
        Archive testArchive2 = createTestArchive();

        ObjectNode request = objectMapper.createObjectNode();
        request.putArray("archiveIds").add(testArchive1.getId()).add(testArchive2.getId());
        request.put("status", "INACTIVE");

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(postRequest("/api/archives/batch/status", requestBody)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量更新成功"));
    }

    @Test
    @DisplayName("批量删除档案 - 成功")
    void batchDeleteArchives_Success() throws Exception {
        // Given
        Archive testArchive1 = createTestArchive();
        Archive testArchive2 = createTestArchive();

        ObjectNode request = objectMapper.createObjectNode();
        request.putArray("archiveIds").add(testArchive1.getId()).add(testArchive2.getId());

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(postRequest("/api/archives/batch/delete", requestBody)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量删除成功"));
    }

    @Test
    @DisplayName("统计档案数量 - 成功")
    void countArchives_Success() throws Exception {
        // Given
        createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/count")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("根据状态统计档案数量 - 成功")
    void countArchivesByStatus_Success() throws Exception {
        // Given
        createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/count/status/ACTIVE")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("导出档案数据 - 成功")
    void exportArchives_Success() throws Exception {
        // Given
        createTestArchive();

        // When & Then
        mockMvc.perform(getRequest("/api/archives/export")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("导入档案数据 - 成功")
    void importArchives_Success() throws Exception {
        // Given
        // 这里需要准备一个Excel文件，实际测试中可以使用MockMultipartFile

        // When & Then
        mockMvc.perform(multipart("/api/archives/import")
                .file("file", "test data".getBytes())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("导入成功"));
    }
}
