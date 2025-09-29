package com.archive.management.integration;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.enums.ArchiveType;
import com.archive.management.enums.UserRole;
import com.archive.management.enums.UserStatus;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.ArchiveService;
import com.archive.management.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 档案管理系统集成测试类
 * 测试档案管理的完整业务流程
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("档案管理系统集成测试")
class ArchiveIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Archive testArchive;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        archiveRepository.deleteAll();
        userRepository.deleteAll();

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(UserRole.USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // 创建测试档案
        testArchive = new Archive();
        testArchive.setTitle("测试档案");
        testArchive.setDescription("这是一个测试档案");
        testArchive.setType(ArchiveType.DOCUMENT);
        testArchive.setStatus(ArchiveStatus.ACTIVE);
        testArchive.setCreatedBy(testUser.getId());
        testArchive.setCreatedAt(LocalDateTime.now());
        testArchive.setUpdatedAt(LocalDateTime.now());
        testArchive = archiveRepository.save(testArchive);
    }

    @Test
    @DisplayName("完整的档案生命周期测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCompleteArchiveLifecycle() throws Exception {
        // 1. 创建档案
        Archive newArchive = new Archive();
        newArchive.setTitle("新档案");
        newArchive.setDescription("新档案描述");
        newArchive.setType(ArchiveType.IMAGE);
        newArchive.setStatus(ArchiveStatus.ACTIVE);

        mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArchive)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("新档案"))
                .andExpect(jsonPath("$.description").value("新档案描述"))
                .andExpect(jsonPath("$.type").value("IMAGE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 验证档案已创建
        List<Archive> archives = archiveRepository.findAll();
        assertThat(archives).hasSize(2); // 包括setUp中创建的档案

        Archive createdArchive = archives.stream()
                .filter(a -> "新档案".equals(a.getTitle()))
                .findFirst()
                .orElseThrow();

        // 2. 获取档案详情
        mockMvc.perform(get("/api/archives/{id}", createdArchive.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("新档案"))
                .andExpect(jsonPath("$.description").value("新档案描述"));

        // 3. 更新档案
        createdArchive.setTitle("更新后的档案");
        createdArchive.setDescription("更新后的描述");

        mockMvc.perform(put("/api/archives/{id}", createdArchive.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdArchive)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新后的档案"))
                .andExpect(jsonPath("$.description").value("更新后的描述"));

        // 4. 归档档案
        mockMvc.perform(put("/api/archives/{id}/archive", createdArchive.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        // 5. 恢复档案
        mockMvc.perform(put("/api/archives/{id}/restore", createdArchive.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 6. 删除档案
        mockMvc.perform(delete("/api/archives/{id}", createdArchive.getId()))
                .andExpect(status().isNoContent());

        // 验证档案已删除
        assertThat(archiveRepository.findById(createdArchive.getId())).isEmpty();
    }

    @Test
    @DisplayName("档案搜索和分页测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testArchiveSearchAndPagination() throws Exception {
        // 创建多个测试档案
        for (int i = 1; i <= 15; i++) {
            Archive archive = new Archive();
            archive.setTitle("档案 " + i);
            archive.setDescription("描述 " + i);
            archive.setType(i % 2 == 0 ? ArchiveType.DOCUMENT : ArchiveType.IMAGE);
            archive.setStatus(i % 3 == 0 ? ArchiveStatus.ARCHIVED : ArchiveStatus.ACTIVE);
            archive.setCreatedBy(testUser.getId());
            archive.setCreatedAt(LocalDateTime.now());
            archive.setUpdatedAt(LocalDateTime.now());
            archiveRepository.save(archive);
        }

        // 测试分页查询
        mockMvc.perform(get("/api/archives")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(16)) // 包括setUp中的档案
                .andExpect(jsonPath("$.totalPages").value(2));

        // 测试按类型搜索
        mockMvc.perform(get("/api/archives/search")
                .param("type", "DOCUMENT")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 测试按状态搜索
        mockMvc.perform(get("/api/archives/search")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 测试关键词搜索
        mockMvc.perform(get("/api/archives/search")
                .param("keyword", "档案 1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("档案统计功能测试")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testArchiveStatistics() throws Exception {
        // 创建不同状态和类型的档案
        createArchiveWithStatus(ArchiveStatus.ACTIVE, ArchiveType.DOCUMENT);
        createArchiveWithStatus(ArchiveStatus.ACTIVE, ArchiveType.IMAGE);
        createArchiveWithStatus(ArchiveStatus.ARCHIVED, ArchiveType.DOCUMENT);
        createArchiveWithStatus(ArchiveStatus.DELETED, ArchiveType.VIDEO);

        // 测试总数统计
        mockMvc.perform(get("/api/archives/statistics/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());

        // 测试按状态统计
        mockMvc.perform(get("/api/archives/statistics/by-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());

        // 测试按类型统计
        mockMvc.perform(get("/api/archives/statistics/by-type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    @DisplayName("批量操作测试")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testBatchOperations() throws Exception {
        // 创建多个档案
        Archive archive1 = createArchiveWithStatus(ArchiveStatus.ACTIVE, ArchiveType.DOCUMENT);
        Archive archive2 = createArchiveWithStatus(ArchiveStatus.ACTIVE, ArchiveType.IMAGE);
        Archive archive3 = createArchiveWithStatus(ArchiveStatus.ACTIVE, ArchiveType.VIDEO);

        Long[] archiveIds = {archive1.getId(), archive2.getId(), archive3.getId()};

        // 测试批量归档
        mockMvc.perform(put("/api/archives/batch/archive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archiveIds)))
                .andExpect(status().isOk());

        // 验证档案状态已更新
        List<Archive> archivedArchives = archiveRepository.findAllById(List.of(archiveIds));
        assertThat(archivedArchives).allMatch(a -> a.getStatus() == ArchiveStatus.ARCHIVED);

        // 测试批量恢复
        mockMvc.perform(put("/api/archives/batch/restore")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archiveIds)))
                .andExpect(status().isOk());

        // 验证档案状态已恢复
        List<Archive> restoredArchives = archiveRepository.findAllById(List.of(archiveIds));
        assertThat(restoredArchives).allMatch(a -> a.getStatus() == ArchiveStatus.ACTIVE);

        // 测试批量删除
        mockMvc.perform(delete("/api/archives/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archiveIds)))
                .andExpect(status().isNoContent());

        // 验证档案已删除
        List<Archive> deletedArchives = archiveRepository.findAllById(List.of(archiveIds));
        assertThat(deletedArchives).isEmpty();
    }

    @Test
    @DisplayName("权限控制测试")
    void testPermissionControl() throws Exception {
        // 测试未认证用户访问
        mockMvc.perform(get("/api/archives"))
                .andExpect(status().isUnauthorized());

        // 测试普通用户访问管理员功能
        mockMvc.perform(get("/api/archives/statistics/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("用户档案关联测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUserArchiveAssociation() throws Exception {
        // 创建档案
        Archive newArchive = new Archive();
        newArchive.setTitle("用户档案");
        newArchive.setDescription("用户创建的档案");
        newArchive.setType(ArchiveType.DOCUMENT);
        newArchive.setStatus(ArchiveStatus.ACTIVE);

        mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArchive)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy").value(testUser.getId()));

        // 验证档案与用户的关联
        List<Archive> userArchives = archiveRepository.findByCreatedBy(testUser.getId());
        assertThat(userArchives).hasSize(2); // 包括setUp中创建的档案
    }

    @Test
    @DisplayName("档案版本控制测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testArchiveVersionControl() throws Exception {
        // 获取原始档案
        Archive originalArchive = archiveRepository.findById(testArchive.getId()).orElseThrow();
        LocalDateTime originalUpdatedAt = originalArchive.getUpdatedAt();

        // 等待一毫秒确保时间戳不同
        Thread.sleep(1);

        // 更新档案
        originalArchive.setTitle("更新后的标题");
        originalArchive.setDescription("更新后的描述");

        mockMvc.perform(put("/api/archives/{id}", originalArchive.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(originalArchive)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新后的标题"))
                .andExpect(jsonPath("$.description").value("更新后的描述"));

        // 验证更新时间已改变
        Archive updatedArchive = archiveRepository.findById(testArchive.getId()).orElseThrow();
        assertThat(updatedArchive.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("档案数据完整性测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testArchiveDataIntegrity() throws Exception {
        // 测试创建档案时的数据验证
        Archive invalidArchive = new Archive();
        // 不设置必需字段

        mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidArchive)))
                .andExpect(status().isBadRequest());

        // 测试更新不存在的档案
        Archive nonExistentArchive = new Archive();
        nonExistentArchive.setId(99999L);
        nonExistentArchive.setTitle("不存在的档案");

        mockMvc.perform(put("/api/archives/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentArchive)))
                .andExpect(status().isNotFound());

        // 测试删除不存在的档案
        mockMvc.perform(delete("/api/archives/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("并发操作测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testConcurrentOperations() throws Exception {
        // 模拟并发更新同一档案
        Archive archive = archiveRepository.findById(testArchive.getId()).orElseThrow();
        
        // 第一次更新
        archive.setTitle("并发更新1");
        mockMvc.perform(put("/api/archives/{id}", archive.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archive)))
                .andExpect(status().isOk());

        // 第二次更新
        archive.setTitle("并发更新2");
        mockMvc.perform(put("/api/archives/{id}", archive.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archive)))
                .andExpect(status().isOk());

        // 验证最终状态
        Archive finalArchive = archiveRepository.findById(testArchive.getId()).orElseThrow();
        assertThat(finalArchive.getTitle()).isEqualTo("并发更新2");
    }

    /**
     * 创建指定状态和类型的档案
     */
    private Archive createArchiveWithStatus(ArchiveStatus status, ArchiveType type) {
        Archive archive = new Archive();
        archive.setTitle("测试档案 - " + status + " - " + type);
        archive.setDescription("测试描述");
        archive.setType(type);
        archive.setStatus(status);
        archive.setCreatedBy(testUser.getId());
        archive.setCreatedAt(LocalDateTime.now());
        archive.setUpdatedAt(LocalDateTime.now());
        return archiveRepository.save(archive);
    }
}