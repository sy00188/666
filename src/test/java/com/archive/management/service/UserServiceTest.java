package com.archive.management.service;

import com.archive.management.dto.UserDTO;
import com.archive.management.entity.Role;
import com.archive.management.entity.User;
import com.archive.management.enums.UserStatus;
import com.archive.management.exception.BusinessException;
import com.archive.management.exception.UserNotFoundException;
import com.archive.management.repository.RoleRepository;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试类
 * 测试用户管理业务逻辑的各种场景
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // 初始化测试角色
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");
        testRole.setDescription("普通用户");

        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);

        // 初始化测试用户DTO
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFullName("测试用户");
        testUserDTO.setPhone("13800138000");
        testUserDTO.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        UserDTO createDTO = new UserDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("new@example.com");
        createDTO.setPassword("password123");
        createDTO.setFullName("新用户");
        createDTO.setPhone("13900139000");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.createUser(createDTO);

        // Then
        assertNotNull(result);
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void createUser_UsernameExists() {
        // Given
        UserDTO createDTO = new UserDTO();
        createDTO.setUsername("existinguser");
        createDTO.setEmail("new@example.com");
        createDTO.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.createUser(createDTO);
        });

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户 - 邮箱已存在")
    void createUser_EmailExists() {
        // Given
        UserDTO createDTO = new UserDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("existing@example.com");
        createDTO.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.createUser(createDTO);
        });

        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("根据ID获取用户 - 成功")
    void getUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(1L);
        });

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("根据用户名获取用户 - 成功")
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("根据用户名获取用户 - 用户不存在")
    void getUserByUsername_NotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void updateUser_Success() {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setId(1L);
        updateDTO.setFullName("更新后的姓名");
        updateDTO.setPhone("13700137000");
        updateDTO.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("updated@example.com", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更新用户 - 用户不存在")
    void updateUser_NotFound() {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setFullName("更新后的姓名");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1L, updateDTO);
        });

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("更新用户 - 邮箱已被其他用户使用")
    void updateUser_EmailExistsForOtherUser() {
        // Given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("existing@example.com", 1L)).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.updateUser(1L, updateDTO);
        });

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("existing@example.com", 1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void deleteUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When
        assertDoesNotThrow(() -> {
            userService.deleteUser(1L);
        });

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("删除用户 - 用户不存在")
    void deleteUser_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository).findById(1L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("分页获取用户列表 - 成功")
    void getUserList_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // When
        Page<UserDTO> result = userService.getUserList(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("根据状态获取用户列表 - 成功")
    void getUsersByStatus_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(users);

        // When
        List<UserDTO> result = userService.getUsersByStatus(UserStatus.ACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        assertEquals(UserStatus.ACTIVE, result.get(0).getStatus());

        verify(userRepository).findByStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("搜索用户 - 成功")
    void searchUsers_Success() {
        // Given
        String keyword = "test";
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByUsernameContainingOrFullNameContainingOrEmailContaining(
                keyword, keyword, keyword)).thenReturn(users);

        // When
        List<UserDTO> result = userService.searchUsers(keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());

        verify(userRepository).findByUsernameContainingOrFullNameContainingOrEmailContaining(
                keyword, keyword, keyword);
    }

    @Test
    @DisplayName("激活用户 - 成功")
    void activateUser_Success() {
        // Given
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.activateUser(1L);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("停用用户 - 成功")
    void deactivateUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = userService.deactivateUser(1L);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("重置密码 - 成功")
    void resetPassword_Success() {
        // Given
        String newPassword = "newPassword123";
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        assertDoesNotThrow(() -> {
            userService.resetPassword(1L, newPassword);
        });

        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更改密码 - 成功")
    void changePassword_Success() {
        // Given
        String oldPassword = "oldPassword";
        String newPassword = "newPassword123";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        assertDoesNotThrow(() -> {
            userService.changePassword(1L, oldPassword, newPassword);
        });

        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更改密码 - 旧密码错误")
    void changePassword_WrongOldPassword() {
        // Given
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.changePassword(1L, oldPassword, newPassword);
        });

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("统计用户数量 - 成功")
    void countUsers_Success() {
        // Given
        when(userRepository.count()).thenReturn(100L);

        // When
        long result = userService.countUsers();

        // Then
        assertEquals(100L, result);
        verify(userRepository).count();
    }

    @Test
    @DisplayName("根据状态统计用户数量 - 成功")
    void countUsersByStatus_Success() {
        // Given
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(80L);

        // When
        long result = userService.countUsersByStatus(UserStatus.ACTIVE);

        // Then
        assertEquals(80L, result);
        verify(userRepository).countByStatus(UserStatus.ACTIVE);
    }
}