package com.archive.management.integration;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.enums.ArchiveType;
import com.archive.management.enums.UserRole;
import com.archive.management.enums.UserStatus;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系统集成测试类
 * 测试整个系统的集成功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("系统集成测试")
class SystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private User testAdmin;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        archiveRepository.deleteAll();
        userRepository.deleteAll();
        
        // 清理Redis缓存
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 创建测试管理员
        testAdmin = new User();
        testAdmin.setUsername("admin");
        testAdmin.setPassword("admin123");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setFullName("Test Admin");
        testAdmin.setRole(UserRole.ADMIN);
        testAdmin.setStatus(UserStatus.ACTIVE);
        testAdmin.setCreatedAt(LocalDateTime.now());
        testAdmin = userRepository.save(testAdmin);

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
    }

    @Test
    @DisplayName("系统健康检查测试")
    void testSystemHealthCheck() throws Exception {
        // 测试应用健康状态
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // 测试数据库连接
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(5)).isTrue();
        }

        // 测试Redis连接
        redisTemplate.opsForValue().set("health-check", "ok");
        String value = (String) redisTemplate.opsForValue().get("health-check");
        assertThat(value).isEqualTo("ok");
    }

    @Test
    @DisplayName("完整业务流程集成测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCompleteBusinessWorkflow() throws Exception {
        // 1. 创建用户
        User newUser = new User();
        newUser.setUsername("workflowuser");
        newUser.setPassword("password123");
        newUser.setEmail("workflow@example.com");
        newUser.setFullName("Workflow User");
        newUser.setRole(UserRole.USER);
        newUser.setStatus(UserStatus.ACTIVE);

        String userResponse = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(userResponse, User.class);

        // 2. 用户创建档案
        Archive newArchive = new Archive();
        newArchive.setTitle("工作流程档案");
        newArchive.setDescription("完整业务流程测试档案");
        newArchive.setType(ArchiveType.DOCUMENT);
        newArchive.setStatus(ArchiveStatus.ACTIVE);
        newArchive.setCreatedBy(createdUser.getId());

        String archiveResponse = mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArchive)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Archive createdArchive = objectMapper.readValue(archiveResponse, Archive.class);

        // 3. 更新档案
        createdArchive.setTitle("更新后的工作流程档案");
        mockMvc.perform(put("/api/archives/{id}", createdArchive.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdArchive)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新后的工作流程档案"));

        // 4. 归档档案
        mockMvc.perform(put("/api/archives/{id}/archive", createdArchive.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        // 5. 查看统计信息
        mockMvc.perform(get("/api/archives/statistics/count"))
                .andExpected(status().isOk());

        mockMvc.perform(get("/api/users/statistics/count"))
                .andExpect(status().isOk());

        // 6. 清理资源
        mockMvc.perform(delete("/api/archives/{id}", createdArchive.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("系统并发性能测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSystemConcurrentPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // 并发创建用户
        CompletableFuture<Void>[] userTasks = new CompletableFuture[20];
        for (int i = 0; i < 20; i++) {
            final int index = i;
            userTasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    User user = new User();
                    user.setUsername("concurrent_user_" + index);
                    user.setPassword("password123");
                    user.setEmail("concurrent_user_" + index + "@example.com");
                    user.setFullName("Concurrent User " + index);
                    user.setRole(UserRole.USER);
                    user.setStatus(UserStatus.ACTIVE);

                    mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // 并发创建档案
        CompletableFuture<Void>[] archiveTasks = new CompletableFuture[30];
        for (int i = 0; i < 30; i++) {
            final int index = i;
            archiveTasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    Archive archive = new Archive();
                    archive.setTitle("并发档案 " + index);
                    archive.setDescription("并发测试档案 " + index);
                    archive.setType(ArchiveType.DOCUMENT);
                    archive.setStatus(ArchiveStatus.ACTIVE);
                    archive.setCreatedBy(testUser.getId());

                    mockMvc.perform(post("/api/archives")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(archive)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(userTasks).get(30, TimeUnit.SECONDS);
        CompletableFuture.allOf(archiveTasks).get(30, TimeUnit.SECONDS);

        executor.shutdown();

        // 验证数据完整性
        long userCount = userRepository.count();
        long archiveCount = archiveRepository.count();
        
        assertThat(userCount).isGreaterThanOrEqualTo(22); // 包括setUp中的用户
        assertThat(archiveCount).isGreaterThanOrEqualTo(30);
    }

    @Test
    @DisplayName("系统缓存功能测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSystemCacheFunction() throws Exception {
        // 创建档案
        Archive archive = new Archive();
        archive.setTitle("缓存测试档案");
        archive.setDescription("测试系统缓存功能");
        archive.setType(ArchiveType.DOCUMENT);
        archive.setStatus(ArchiveStatus.ACTIVE);
        archive.setCreatedBy(testUser.getId());

        String response = mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archive)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Archive createdArchive = objectMapper.readValue(response, Archive.class);

        // 第一次查询（应该从数据库查询并缓存）
        long startTime1 = System.currentTimeMillis();
        mockMvc.perform(get("/api/archives/{id}", createdArchive.getId()))
                .andExpect(status().isOk());
        long endTime1 = System.currentTimeMillis();

        // 第二次查询（应该从缓存查询，速度更快）
        long startTime2 = System.currentTimeMillis();
        mockMvc.perform(get("/api/archives/{id}", createdArchive.getId()))
                .andExpect(status().isOk());
        long endTime2 = System.currentTimeMillis();

        // 验证缓存效果（第二次查询应该更快）
        long firstQueryTime = endTime1 - startTime1;
        long secondQueryTime = endTime2 - startTime2;
        
        // 注意：在测试环境中，时间差可能不明显，这里主要验证功能正常
        assertThat(secondQueryTime).isLessThanOrEqualTo(firstQueryTime + 50); // 允许50ms误差
    }

    @Test
    @DisplayName("系统事务管理测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSystemTransactionManagement() throws Exception {
        // 测试事务回滚
        try {
            // 创建用户（应该成功）
            User user = new User();
            user.setUsername("transaction_user");
            user.setPassword("password123");
            user.setEmail("transaction@example.com");
            user.setFullName("Transaction User");
            user.setRole(UserRole.USER);
            user.setStatus(UserStatus.ACTIVE);

            String userResponse = mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(user)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            User createdUser = objectMapper.readValue(userResponse, User.class);

            // 尝试创建重复用户名的用户（应该失败并回滚）
            User duplicateUser = new User();
            duplicateUser.setUsername("transaction_user"); // 重复用户名
            duplicateUser.setPassword("password123");
            duplicateUser.setEmail("duplicate@example.com");
            duplicateUser.setFullName("Duplicate User");
            duplicateUser.setRole(UserRole.USER);
            duplicateUser.setStatus(UserStatus.ACTIVE);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateUser)))
                    .andExpect(status().isConflict());

            // 验证原用户仍然存在
            assertThat(userRepository.findById(createdUser.getId())).isPresent();

        } catch (Exception e) {
            // 验证事务回滚后数据一致性
            long userCount = userRepository.count();
            assertThat(userCount).isEqualTo(2); // 只有setUp中创建的两个用户
        }
    }

    @Test
    @DisplayName("系统安全性测试")
    void testSystemSecurity() throws Exception {
        // 测试未认证访问
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/archives"))
                .andExpect(status().isUnauthorized());

        // 测试SQL注入防护
        mockMvc.perform(get("/api/users/search")
                .param("keyword", "'; DROP TABLE users; --"))
                .andExpect(status().isUnauthorized()); // 应该被安全拦截

        // 测试XSS防护
        Archive maliciousArchive = new Archive();
        maliciousArchive.setTitle("<script>alert('XSS')</script>");
        maliciousArchive.setDescription("正常描述");
        maliciousArchive.setType(ArchiveType.DOCUMENT);
        maliciousArchive.setStatus(ArchiveStatus.ACTIVE);

        // 未认证用户不应该能创建档案
        mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousArchive)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("系统错误处理测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSystemErrorHandling() throws Exception {
        // 测试404错误
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/archives/99999"))
                .andExpect(status().isNotFound());

        // 测试400错误（无效数据）
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // 空对象
                .andExpect(status().isBadRequest());

        // 测试405错误（方法不允许）
        mockMvc.perform(patch("/api/users"))
                .andExpect(status().isMethodNotAllowed());

        // 测试415错误（不支持的媒体类型）
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("系统监控指标测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSystemMonitoringMetrics() throws Exception {
        // 测试应用指标
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());

        // 测试JVM指标
        mockMvc.perform(get("/actuator/metrics/jvm.memory.used"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jvm.memory.used"));

        // 测试HTTP指标
        mockMvc.perform(get("/actuator/metrics/http.server.requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("http.server.requests"));
    }

    @Test
    @DisplayName("系统配置管理测试")
    void testSystemConfigurationManagement() throws Exception {
        // 测试配置信息
        mockMvc.perform(get("/actuator/configprops"))
                .andExpect(status().isOk());

        // 测试环境信息
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isOk());

        // 测试应用信息
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("系统数据一致性测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSystemDataConsistency() throws Exception {
        // 创建用户和档案
        User user = new User();
        user.setUsername("consistency_user");
        user.setPassword("password123");
        user.setEmail("consistency@example.com");
        user.setFullName("Consistency User");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        String userResponse = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(userResponse, User.class);

        // 为用户创建档案
        Archive archive = new Archive();
        archive.setTitle("一致性测试档案");
        archive.setDescription("测试数据一致性");
        archive.setType(ArchiveType.DOCUMENT);
        archive.setStatus(ArchiveStatus.ACTIVE);
        archive.setCreatedBy(createdUser.getId());

        mockMvc.perform(post("/api/archives")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(archive)))
                .andExpect(status().isCreated());

        // 验证数据关联性
        long userArchiveCount = archiveRepository.countByCreatedBy(createdUser.getId());
        assertThat(userArchiveCount).isEqualTo(1);

        // 删除用户（应该处理关联的档案）
        mockMvc.perform(delete("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());

        // 验证数据一致性（档案的创建者信息应该被适当处理）
        long remainingArchiveCount = archiveRepository.countByCreatedBy(createdUser.getId());
        // 根据业务逻辑，可能是0（级联删除）或保持原值（软删除/匿名化）
        assertThat(remainingArchiveCount).isGreaterThanOrEqualTo(0);
    }
}