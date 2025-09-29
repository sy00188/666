package com.archive.management.controller;

import com.archive.management.dto.UserDTO;
import com.archive.management.enums.UserStatus;
import com.archive.management.service.UserService;
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
 * 用户控制器集成测试类
 * 测试用户管理REST API接口的各种场景
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@WebMvcTest(UserController.class)
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFullName("测试用户");
        testUserDTO.setPhone("13800138000");
        testUserDTO.setStatus(UserStatus.ACTIVE);
        testUserDTO.setCreatedAt(LocalDateTime.now());
        testUserDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        // Given
        UserDTO createDTO = new UserDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("new@example.com");
        createDTO.setPassword("password123");
        createDTO.setFullName("新用户");
        createDTO.setPhone("13900139000");

        when(userService.createUser(any(UserDTO.class))).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("创建用户 - 参数验证失败")
    @WithMockUser(roles = "ADMIN")
    void createUser_ValidationFailed() throws Exception {
        // Given
        UserDTO invalidDTO = new UserDTO();
        // 缺少必要字段

        // When & Then
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("根据ID获取用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound() throws Exception {
        // Given
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("用户不存在"));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("根据用户名获取用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getUserByUsername_Success() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(get("/api/users/username/testuser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @DisplayName("更新用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void updateUser_Success() throws Exception {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setFullName("更新后的姓名");
        updateDTO.setPhone("13700137000");
        updateDTO.setEmail("updated@example.com");

        when(userService.updateUser(eq(1L), any(UserDTO.class)))
                .thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(userService).updateUser(eq(1L), any(UserDTO.class));
    }

    @Test
    @DisplayName("删除用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户删除成功"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("分页获取用户列表 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getUserList_Success() throws Exception {
        // Given
        List<UserDTO> users = Arrays.asList(testUserDTO);
        Page<UserDTO> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

        when(userService.getUserList(any())).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(userService).getUserList(any());
    }

    @Test
    @DisplayName("根据状态获取用户列表 - 成功")
    @WithMockUser(roles = "ADMIN")
    void getUsersByStatus_Success() throws Exception {
        // Given
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getUsersByStatus(UserStatus.ACTIVE)).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/status/ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

        verify(userService).getUsersByStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("搜索用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void searchUsers_Success() throws Exception {
        // Given
        String keyword = "test";
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.searchUsers(keyword)).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(userService).searchUsers(keyword);
    }

    @Test
    @DisplayName("激活用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void activateUser_Success() throws Exception {
        // Given
        testUserDTO.setStatus(UserStatus.ACTIVE);
        when(userService.activateUser(1L)).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(patch("/api/users/1/activate")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.message").value("用户激活成功"));

        verify(userService).activateUser(1L);
    }

    @Test
    @DisplayName("停用用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_Success() throws Exception {
        // Given
        testUserDTO.setStatus(UserStatus.INACTIVE);
        when(userService.deactivateUser(1L)).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(patch("/api/users/1/deactivate")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.message").value("用户停用成功"));

        verify(userService).deactivateUser(1L);
    }

    @Test
    @DisplayName("重置密码 - 成功")
    @WithMockUser(roles = "ADMIN")
    void resetPassword_Success() throws Exception {
        // Given
        String newPassword = "newPassword123";
        doNothing().when(userService).resetPassword(1L, newPassword);

        // When & Then
        mockMvc.perform(patch("/api/users/1/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"" + newPassword + "\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        verify(userService).resetPassword(1L, newPassword);
    }

    @Test
    @DisplayName("更改密码 - 成功")
    @WithMockUser
    void changePassword_Success() throws Exception {
        // Given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword123";
        doNothing().when(userService).changePassword(1L, oldPassword, newPassword);

        // When & Then
        mockMvc.perform(patch("/api/users/1/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"oldPassword\":\"" + oldPassword + "\",\"newPassword\":\"" + newPassword + "\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        verify(userService).changePassword(1L, oldPassword, newPassword);
    }

    @Test
    @DisplayName("统计用户数量 - 成功")
    @WithMockUser(roles = "ADMIN")
    void countUsers_Success() throws Exception {
        // Given
        when(userService.countUsers()).thenReturn(100L);

        // When & Then
        mockMvc.perform(get("/api/users/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));

        verify(userService).countUsers();
    }

    @Test
    @DisplayName("根据状态统计用户数量 - 成功")
    @WithMockUser(roles = "ADMIN")
    void countUsersByStatus_Success() throws Exception {
        // Given
        when(userService.countUsersByStatus(UserStatus.ACTIVE)).thenReturn(80L);

        // When & Then
        mockMvc.perform(get("/api/users/count/status/ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(80));

        verify(userService).countUsersByStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("未授权访问 - 返回401")
    void unauthorizedAccess_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("权限不足 - 返回403")
    @WithMockUser(roles = "USER")
    void insufficientPermission_Returns403() throws Exception {
        // Given
        UserDTO createDTO = new UserDTO();
        createDTO.setUsername("newuser");

        // When & Then
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, never()).createUser(any(UserDTO.class));
    }

    @Test
    @DisplayName("批量删除用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void batchDeleteUsers_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(userService).batchDeleteUsers(ids);

        // When & Then
        mockMvc.perform(delete("/api/users/batch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量删除成功"));

        verify(userService).batchDeleteUsers(ids);
    }

    @Test
    @DisplayName("批量激活用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void batchActivateUsers_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(userService).batchActivateUsers(ids);

        // When & Then
        mockMvc.perform(patch("/api/users/batch/activate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量激活成功"));

        verify(userService).batchActivateUsers(ids);
    }

    @Test
    @DisplayName("批量停用用户 - 成功")
    @WithMockUser(roles = "ADMIN")
    void batchDeactivateUsers_Success() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(userService).batchDeactivateUsers(ids);

        // When & Then
        mockMvc.perform(patch("/api/users/batch/deactivate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量停用成功"));

        verify(userService).batchDeactivateUsers(ids);
    }

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    @WithMockUser(username = "testuser")
    void getCurrentUser_Success() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(get("/api/users/current"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @DisplayName("更新当前用户信息 - 成功")
    @WithMockUser(username = "testuser")
    void updateCurrentUser_Success() throws Exception {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setFullName("更新后的姓名");
        updateDTO.setPhone("13700137000");

        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);
        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(put("/api/users/current")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(userService).getUserByUsername("testuser");
        verify(userService).updateUser(eq(1L), any(UserDTO.class));
    }
}