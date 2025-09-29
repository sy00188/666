package com.archive.management.controller;

import com.archive.management.dto.ArchiveDTO;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.enums.ArchiveType;
import com.archive.management.service.ArchiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 档案控制器集成测试类
 * 测试档案REST API接口的各种场景
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@WebMvcTest(ArchiveController.class)
@DisplayName("档案控制器测试")
class ArchiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArchiveService archiveService;

    @Autowired
    private ObjectMapper objectMapper;

    private ArchiveDTO testArchiveDTO;

    @BeforeEach
    void setUp() {
        testArchiveDTO = new ArchiveDTO();
        testArchiveDTO.setId(1L);
        testArchiveDTO.setTitle("测试档案");
        testArchiveDTO.setDescription("这是一个测试档案");
        testArchiveDTO.setType(ArchiveType.DOCUMENT);
        testArchiveDTO.setStatus(ArchiveStatus.ACTIVE);
        testArchiveDTO.setCreatedBy(1L);
        testArchiveDTO.setCreatedAt(LocalDateTime.now());
        testArchiveDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void createArchive_Success() throws Exception {
        // Given
        ArchiveDTO createDTO = new ArchiveDTO();
        createDTO.setTitle("新档案");
        createDTO.setDescription("新档案描述");
        createDTO.setType(ArchiveType.DOCUMENT);
        createDTO.setCreatedBy(1L);

        when(archiveService.createArchive(any(ArchiveDTO.class))).thenReturn(testArchiveDTO);

        // When & Then
        mockMvc.perform(post("/api/archives")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("测试档案"))
                .andExpect(jsonPath("$.data.description").value("这是一个测试档案"));

        verify(archiveService).createArchive(any(ArchiveDTO.class));
    }

    @Test
    @DisplayName("创建档案 - 参数验证失败")
    @WithMockUser(roles = "ADMIN")
    void createArchive_ValidationFailed() throws Exception {
        // Given
        ArchiveDTO invalidDTO = new ArchiveDTO();
        // 缺少必要字段

        // When & Then
        mockMvc.perform(post("/api/archives")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(archiveService, never()).createArchive(any(ArchiveDTO.class));
    }

    @Test
    @DisplayName("根据ID获取档案 - 成功")
    @WithMockUser
    void getArchiveById_Success() throws Exception {
        // Given
        when(archiveService.getArchiveById(1L)).thenReturn(testArchiveDTO);

        // When & Then
        mockMvc.perform(get("/api/archives/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("测试档案"));

        verify(archiveService).getArchiveById(1L);
    }

    @Test
    @DisplayName("根据ID获取档案 - 档案不存在")
    @WithMockUser
    void getArchiveById_NotFound() throws Exception {
        // Given
        when(archiveService.getArchiveById(999L))
                .thenThrow(new RuntimeException("档案不存在"));

        // When & Then
        mockMvc.perform(get("/api/archives/999"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(archiveService).getArchiveById(999L);
    }

    @Test
    @DisplayName("更新档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void updateArchive_Success() throws Exception {
        // Given
        ArchiveDTO updateDTO = new ArchiveDTO();
        updateDTO.setTitle("更新后的标题");
        updateDTO.setDescription("更新后的描述");

        when(archiveService.updateArchive(eq(1L), any(ArchiveDTO.class)))
                .thenReturn(testArchiveDTO);

        // When & Then
        mockMvc.perform(put("/api/archives/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(archiveService).updateArchive(eq(1L), any(ArchiveDTO.class));
    }

    @Test
    @DisplayName("删除档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void deleteArchive_Success() throws Exception {
        // Given
        doNothing().when(archiveService).deleteArchive(1L);

        // When & Then
        mockMvc.perform(delete("/api/archives/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("档案删除成功"));

        verify(archiveService).deleteArchive(1L);
    }

    @Test
    @DisplayName("分页获取档案列表 - 成功")
    @WithMockUser
    void getArchiveList_Success() throws Exception {
        // Given
        List<ArchiveDTO> archives = Arrays.asList(testArchiveDTO);
        Page<ArchiveDTO> archivePage = new PageImpl<>(archives, PageRequest.of(0, 10), 1);

        when(archiveService.getArchiveList(any())).thenReturn(archivePage);

        // When & Then
        mockMvc.perform(get("/api/archives")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(archiveService).getArchiveList(any());
    }

    @Test
    @DisplayName("根据状态获取档案列表 - 成功")
    @WithMockUser
    void getArchivesByStatus_Success() throws Exception {
        // Given
        List<ArchiveDTO> archives = Arrays.asList(testArchiveDTO);
        when(archiveService.getArchivesByStatus(ArchiveStatus.ACTIVE)).thenReturn(archives);

        // When & Then
        mockMvc.perform(get("/api/archives/status/ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

        verify(archiveService).getArchivesByStatus(ArchiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("根据类型获取档案列表 - 成功")
    @WithMockUser
    void getArchivesByType_Success() throws Exception {
        // Given
        List<ArchiveDTO> archives = Arrays.asList(testArchiveDTO);
        when(archiveService.getArchivesByType(ArchiveType.DOCUMENT)).thenReturn(archives);

        // When & Then
        mockMvc.perform(get("/api/archives/type/DOCUMENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].type").value("DOCUMENT"));

        verify(archiveService).getArchivesByType(ArchiveType.DOCUMENT);
    }

    @Test
    @DisplayName("搜索档案 - 成功")
    @WithMockUser
    void searchArchives_Success() throws Exception {
        // Given
        String keyword = "测试";
        List<ArchiveDTO> archives = Arrays.asList(testArchiveDTO);
        when(archiveService.searchArchives(keyword)).thenReturn(archives);

        // When & Then
        mockMvc.perform(get("/api/archives/search")
                .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(archiveService).searchArchives(keyword);
    }

    @Test
    @DisplayName("归档档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void archiveDocument_Success() throws Exception {
        // Given
        testArchiveDTO.setStatus(ArchiveStatus.ARCHIVED);
        when(archiveService.archiveDocument(1L)).thenReturn(testArchiveDTO);

        // When & Then
        mockMvc.perform(patch("/api/archives/1/archive")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.message").value("档案归档成功"));

        verify(archiveService).archiveDocument(1L);
    }

    @Test
    @DisplayName("恢复档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void restoreArchive_Success() throws Exception {
        // Given
        testArchiveDTO.setStatus(ArchiveStatus.ACTIVE);
        when(archiveService.restoreArchive(1L)).thenReturn(testArchiveDTO);

        // When & Then
        mockMvc.perform(patch("/api/archives/1/restore")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.message").value("档案恢复成功"));

        verify(archiveService).restoreArchive(1L);
    }

    @Test
    @DisplayName("获取用户创建的档案 - 成功")
    @WithMockUser
    void getArchivesByCreator_Success() throws Exception {
        // Given
        List<ArchiveDTO> archives = Arrays.asList(testArchiveDTO);
        when(archiveService.getArchivesByCreator(1L)).thenReturn(archives);

        // When & Then
        mockMvc.perform(get("/api/archives/creator/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].createdBy").value(1L));

        verify(archiveService).getArchivesByCreator(1L);
    }

    @Test
    @DisplayName("统计档案数量 - 成功")
    @WithMockUser(roles = "ADMIN")
    void countArchives_Success() throws Exception {
        // Given
        when(archiveService.countArchives()).thenReturn(100L);

        // When & Then
        mockMvc.perform(get("/api/archives/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));

        verify(archiveService).countArchives();
    }

    @Test
    @DisplayName("根据状态统计档案数量 - 成功")
    @WithMockUser(roles = "ADMIN")
    void countArchivesByStatus_Success() throws Exception {
        // Given
        when(archiveService.countArchivesByStatus(ArchiveStatus.ACTIVE)).thenReturn(80L);

        // When & Then
        mockMvc.perform(get("/api/archives/count/status/ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(80));

        verify(archiveService).countArchivesByStatus(ArchiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("未授权访问 - 返回401")
    void unauthorizedAccess_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/archives/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(archiveService, never()).getArchiveById(anyLong());
    }

    @Test
    @DisplayName("权限不足 - 返回403")
    @WithMockUser(roles = "USER")
    void insufficientPermission_Returns403() throws Exception {
        // Given
        ArchiveDTO createDTO = new ArchiveDTO();
        createDTO.setTitle("新档案");

        // When & Then
        mockMvc.perform(post("/api/archives")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(archiveService, never()).createArchive(any(ArchiveDTO.class));
    }

    @Test
    @DisplayName("批量删除档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void batchDeleteArchives_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(archiveService).batchDeleteArchives(ids);

        // When & Then
        mockMvc.perform(delete("/api/archives/batch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量删除成功"));

        verify(archiveService).batchDeleteArchives(ids);
    }

    @Test
    @DisplayName("批量归档档案 - 成功")
    @WithMockUser(roles = "ADMIN")
    void batchArchiveDocuments_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(archiveService).batchArchiveDocuments(ids);

        // When & Then
        mockMvc.perform(patch("/api/archives/batch/archive")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量归档成功"));

        verify(archiveService).batchArchiveDocuments(ids);
    }
}