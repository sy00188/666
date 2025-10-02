package com.archive.management.service;

import com.archive.management.dto.LoginRequest;
import com.archive.management.dto.LoginResponse;
import com.archive.management.entity.User;
import com.archive.management.mapper.UserMapper;
import com.archive.management.service.impl.AuthServiceImpl;
import com.archive.management.security.JwtTokenUtil;
import com.archive.management.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        loginRequest.setClientIp("127.0.0.1");
    }

    @Test
    void testLogin_Success() {
        // 准备测试数据
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn("test-jwt-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            // 执行测试
            LoginResponse response = authService.login(loginRequest);

            // 验证结果
            assertNotNull(response);
            assertEquals("test-jwt-token", response.getToken());
            assertEquals("testuser", response.getUsername());

            // 验证方法调用
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userMapper).selectByUsername("testuser");
            verify(jwtTokenUtil).generateToken(testUser);
        }
    }

    @Test
    void testLogin_InvalidCredentials() {
        // 准备测试数据
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        // 执行测试并验证异常
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testIsUserLocked_NotLocked() {
        // 准备测试数据
        testUser.setStatus(1);
        testUser.setLockTime(null);

        // 执行测试
        boolean result = authService.isUserLocked(testUser);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testIsUserLocked_StatusLocked() {
        // 准备测试数据
        testUser.setStatus(0);

        // 执行测试
        boolean result = authService.isUserLocked(testUser);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsUserLocked_TimeLocked() {
        // 准备测试数据
        testUser.setStatus(1);
        testUser.setLockTime(LocalDateTime.now().minusMinutes(10)); // 10分钟前锁定

        // 执行测试
        boolean result = authService.isUserLocked(testUser);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsUserExpired_NotExpired() {
        // 准备测试数据
        testUser.setExpireTime(LocalDateTime.now().plusDays(30));

        // 执行测试
        boolean result = authService.isUserExpired(testUser);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testIsUserExpired_Expired() {
        // 准备测试数据
        testUser.setExpireTime(LocalDateTime.now().minusDays(1));

        // 执行测试
        boolean result = authService.isUserExpired(testUser);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testValidatePasswordStrength_Strong() {
        // 执行测试
        boolean result = authService.validatePasswordStrength("StrongPass123!");

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testValidatePasswordStrength_Weak() {
        // 执行测试
        boolean result = authService.validatePasswordStrength("weak");

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testGeneratePasswordResetToken_Success() {
        // 准备测试数据
        when(userMapper.selectByEmail("test@example.com")).thenReturn(testUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        String token = authService.generatePasswordResetToken("test@example.com");

        // 验证结果
        assertNotNull(token);
        assertTrue(token.startsWith("reset_token_"));
        assertTrue(token.contains("1")); // 用户ID

        // 验证方法调用
        verify(userMapper).selectByEmail("test@example.com");
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testValidatePasswordResetToken_Valid() {
        // 准备测试数据
        String token = "reset_token_1234567890_1";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("password_reset_token:1")).thenReturn(token);

        // 执行测试
        boolean result = authService.validatePasswordResetToken(token);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testValidatePasswordResetToken_Invalid() {
        // 准备测试数据
        String token = "invalid_token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // 执行测试
        boolean result = authService.validatePasswordResetToken(token);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testResetPassword_Success() {
        // 准备测试数据
        String token = "reset_token_1234567890_1";
        String newPassword = "newPassword123!";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("password_reset_token:1")).thenReturn(token);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // 执行测试
        boolean result = authService.resetPassword(token, newPassword);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(userMapper).selectById(1L);
        verify(passwordEncoder).encode(newPassword);
        verify(userMapper).updateById(testUser);
        verify(redisTemplate).delete("password_reset_token:1");
    }

    @Test
    void testChangePassword_Success() {
        // 准备测试数据
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123!";
        
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // 执行测试
        boolean result = authService.changePassword(1L, oldPassword, newPassword);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(userMapper).selectById(1L);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userMapper).updateById(testUser);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // 准备测试数据
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123!";
        
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);

        // 执行测试
        boolean result = authService.changePassword(1L, oldPassword, newPassword);

        // 验证结果
        assertFalse(result);

        // 验证方法调用
        verify(userMapper).selectById(1L);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void testRecordLoginLog_Success() {
        // 准备测试数据
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        authService.recordLoginLog("testuser", "127.0.0.1", true, "登录成功");

        // 验证方法调用
        verify(userMapper).selectByUsername("testuser");
        verify(valueOperations).set(anyString(), any(Map.class), anyLong(), any());
    }

    @Test
    void testRecordLoginLog_UserNotFound() {
        // 准备测试数据
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // 执行测试（不应该抛出异常）
        assertDoesNotThrow(() -> {
            authService.recordLoginLog("nonexistent", "127.0.0.1", false, "用户不存在");
        });

        // 验证方法调用
        verify(userMapper).selectByUsername("nonexistent");
        verify(redisTemplate, never()).opsForValue();
    }
}
