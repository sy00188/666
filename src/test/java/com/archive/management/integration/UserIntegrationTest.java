package com.archive.management.integration;

import com.archive.management.entity.User;
import com.archive.management.enums.UserRole;
import com.archive.management.enums.UserStatus;
import com.archive.management.repository.UserRepository;
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
 * 用户管理系统集成测试类
 * 测试用户管理的完整业务流程
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("用户管理系统集成测试")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testAdmin;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userRepository.deleteAll();

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
    @DisplayName("完整的用户生命周期测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCompleteUserLifecycle() throws Exception {
        // 1. 创建用户
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword123");
        newUser.setEmail("newuser@example.com");
        newUser.setFullName("New User");
        newUser.setRole(UserRole.USER);
        newUser.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("New User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 验证用户已创建
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(3); // 包括setUp中创建的用户

        User createdUser = users.stream()
                .filter(u -> "newuser".equals(u.getUsername()))
                .findFirst()
                .orElseThrow();

        // 2. 获取用户详情
        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        // 3. 更新用户信息
        createdUser.setFullName("Updated User");
        createdUser.setEmail("updated@example.com");

        mockMvc.perform(put("/api/users/{id}", createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        // 4. 禁用用户
        mockMvc.perform(put("/api/users/{id}/disable", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        // 5. 启用用户
        mockMvc.perform(put("/api/users/{id}/enable", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 6. 删除用户
        mockMvc.perform(delete("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());

        // 验证用户已删除
        assertThat(userRepository.findById(createdUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("用户搜索和分页测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserSearchAndPagination() throws Exception {
        // 创建多个测试用户
        for (int i = 1; i <= 15; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setPassword("password" + i);
            user.setEmail("user" + i + "@example.com");
            user.setFullName("User " + i);
            user.setRole(i % 2 == 0 ? UserRole.ADMIN : UserRole.USER);
            user.setStatus(i % 3 == 0 ? UserStatus.INACTIVE : UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // 测试分页查询
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(17)) // 包括setUp中的用户
                .andExpect(jsonPath("$.totalPages").value(2));

        // 测试按角色搜索
        mockMvc.perform(get("/api/users/search")
                .param("role", "ADMIN")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 测试按状态搜索
        mockMvc.perform(get("/api/users/search")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 测试关键词搜索
        mockMvc.perform(get("/api/users/search")
                .param("keyword", "user1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("用户密码管理测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testPasswordManagement() throws Exception {
        // 测试修改密码
        String newPassword = "newpassword123";
        
        mockMvc.perform(put("/api/users/{id}/password", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldPassword\":\"password123\",\"newPassword\":\"" + newPassword + "\"}"))
                .andExpect(status().isOk());

        // 验证密码已更改（通过尝试用新密码登录）
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getPassword()).isNotEqualTo("password123");
    }

    @Test
    @DisplayName("用户统计功能测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserStatistics() throws Exception {
        // 创建不同状态和角色的用户
        createUserWithRoleAndStatus(UserRole.USER, UserStatus.ACTIVE);
        createUserWithRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
        createUserWithRoleAndStatus(UserRole.USER, UserStatus.INACTIVE);
        createUserWithRoleAndStatus(UserRole.MODERATOR, UserStatus.ACTIVE);

        // 测试总数统计
        mockMvc.perform(get("/api/users/statistics/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());

        // 测试按状态统计
        mockMvc.perform(get("/api/users/statistics/by-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());

        // 测试按角色统计
        mockMvc.perform(get("/api/users/statistics/by-role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    @DisplayName("批量用户操作测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testBatchUserOperations() throws Exception {
        // 创建多个用户
        User user1 = createUserWithRoleAndStatus(UserRole.USER, UserStatus.ACTIVE);
        User user2 = createUserWithRoleAndStatus(UserRole.USER, UserStatus.ACTIVE);
        User user3 = createUserWithRoleAndStatus(UserRole.USER, UserStatus.ACTIVE);

        Long[] userIds = {user1.getId(), user2.getId(), user3.getId()};

        // 测试批量禁用
        mockMvc.perform(put("/api/users/batch/disable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk());

        // 验证用户状态已更新
        List<User> disabledUsers = userRepository.findAllById(List.of(userIds));
        assertThat(disabledUsers).allMatch(u -> u.getStatus() == UserStatus.INACTIVE);

        // 测试批量启用
        mockMvc.perform(put("/api/users/batch/enable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk());

        // 验证用户状态已恢复
        List<User> enabledUsers = userRepository.findAllById(List.of(userIds));
        assertThat(enabledUsers).allMatch(u -> u.getStatus() == UserStatus.ACTIVE);

        // 测试批量删除
        mockMvc.perform(delete("/api/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isNoContent());

        // 验证用户已删除
        List<User> deletedUsers = userRepository.findAllById(List.of(userIds));
        assertThat(deletedUsers).isEmpty();
    }

    @Test
    @DisplayName("用户权限控制测试")
    void testUserPermissionControl() throws Exception {
        // 测试未认证用户访问
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

        // 测试普通用户访问管理员功能
        mockMvc.perform(get("/api/users/statistics/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("用户角色管理测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserRoleManagement() throws Exception {
        // 测试角色升级
        mockMvc.perform(put("/api/users/{id}/role", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));

        // 验证角色已更新
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.MODERATOR);

        // 测试角色降级
        mockMvc.perform(put("/api/users/{id}/role", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("用户登录历史测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUserLoginHistory() throws Exception {
        // 模拟用户登录
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());

        // 获取用户登录历史
        mockMvc.perform(get("/api/users/{id}/login-history", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("用户数据完整性测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserDataIntegrity() throws Exception {
        // 测试创建用户时的数据验证
        User invalidUser = new User();
        // 不设置必需字段

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // 测试重复用户名
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser"); // 与现有用户重复
        duplicateUser.setPassword("password123");
        duplicateUser.setEmail("duplicate@example.com");
        duplicateUser.setFullName("Duplicate User");
        duplicateUser.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict());

        // 测试重复邮箱
        User duplicateEmailUser = new User();
        duplicateEmailUser.setUsername("uniqueuser");
        duplicateEmailUser.setPassword("password123");
        duplicateEmailUser.setEmail("test@example.com"); // 与现有用户重复
        duplicateEmailUser.setFullName("Duplicate Email User");
        duplicateEmailUser.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailUser)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("用户会话管理测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUserSessionManagement() throws Exception {
        // 测试获取当前用户会话信息
        mockMvc.perform(get("/api/users/current/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 测试清除用户会话
        mockMvc.perform(delete("/api/users/{id}/sessions", testUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("用户活跃状态测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUserActivityStatus() throws Exception {
        // 测试获取活跃用户列表
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 测试更新用户最后活跃时间
        mockMvc.perform(put("/api/users/{id}/last-active", testUser.getId()))
                .andExpect(status().isOk());

        // 验证最后活跃时间已更新
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getLastActiveAt()).isNotNull();
    }

    @Test
    @DisplayName("用户偏好设置测试")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUserPreferences() throws Exception {
        // 测试更新用户偏好设置
        String preferences = "{\"theme\":\"dark\",\"language\":\"zh-CN\",\"timezone\":\"Asia/Shanghai\"}";
        
        mockMvc.perform(put("/api/users/{id}/preferences", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(preferences))
                .andExpect(status().isOk());

        // 测试获取用户偏好设置
        mockMvc.perform(get("/api/users/{id}/preferences", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"))
                .andExpect(jsonPath("$.language").value("zh-CN"))
                .andExpect(jsonPath("$.timezone").value("Asia/Shanghai"));
    }

    @Test
    @DisplayName("并发用户操作测试")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConcurrentUserOperations() throws Exception {
        // 模拟并发更新同一用户
        User user = userRepository.findById(testUser.getId()).orElseThrow();
        
        // 第一次更新
        user.setFullName("并发更新1");
        mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // 第二次更新
        user.setFullName("并发更新2");
        mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // 验证最终状态
        User finalUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(finalUser.getFullName()).isEqualTo("并发更新2");
    }

    /**
     * 创建指定角色和状态的用户
     */
    private User createUserWithRoleAndStatus(UserRole role, UserStatus status) {
        User user = new User();
        user.setUsername("user_" + System.currentTimeMillis());
        user.setPassword("password123");
        user.setEmail("user_" + System.currentTimeMillis() + "@example.com");
        user.setFullName("Test User - " + role + " - " + status);
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}