package com.archive.management.service.impl;

import com.archive.management.dto.LoginRequest;
import com.archive.management.dto.LoginResponse;
import com.archive.management.entity.User;
import com.archive.management.mapper.UserMapper;
import com.archive.management.service.AuthService;
import com.archive.management.service.AuditLogService;
import com.archive.management.security.JwtTokenUtil;
import com.archive.management.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 提供用户登录、登出、令牌管理等核心认证功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditLogService auditLogService;

    @Value("${app.jwt.expiration:86400}")
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    @Value("${app.security.max-login-attempts:5}")
    private Integer maxLoginAttempts;

    @Value("${app.security.lock-duration:1800}")
    private Integer lockDuration;

    private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";
    private static final String BLACKLIST_TOKEN_KEY = "blacklist_token:";
    private static final String REFRESH_TOKEN_KEY = "refresh_token:";

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String clientIp) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());
        
        try {
            // 检查用户是否被锁定
            User user = getUserByUsername(loginRequest.getUsername());
            if (user != null && isUserLocked(user)) {
                log.warn("用户账户被锁定: {}", loginRequest.getUsername());
                recordLoginLog(loginRequest.getUsername(), clientIp, false, "账户被锁定");
                throw new LockedException("账户已被锁定，请稍后再试");
            }

            // 验证用户凭据
            user = validateCredentials(loginRequest.getUsername(), loginRequest.getPassword());
            if (user == null) {
                recordLoginLog(loginRequest.getUsername(), clientIp, false, "用户名或密码错误");
                throw new BadCredentialsException("用户名或密码错误");
            }
            
            // 生成JWT令牌
            String accessToken = jwtTokenUtil.generateToken(user.getUsername());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());
            
            // 缓存刷新令牌
            cacheRefreshToken(user.getUsername(), refreshToken);
            
            // 更新用户登录信息
             updateLastLoginInfo(user.getUserId(), clientIp);
             
             // 记录登录成功日志
             recordLoginLog(user.getUsername(), clientIp, true, "登录成功");
             
             // 构建登录响应
             LoginResponse response = new LoginResponse();
             response.setAccessToken(accessToken);
             response.setRefreshToken(refreshToken);
             response.setTokenType("Bearer");
             response.setExpiresIn(jwtExpiration);
             response.setUser(buildUserInfoMap(user));
            
            log.info("用户登录成功: {}", user.getUsername());
            return response;
            
        } catch (AuthenticationException e) {
            log.error("用户登录失败: {}, 原因: {}", loginRequest.getUsername(), e.getMessage());
            recordLoginLog(loginRequest.getUsername(), clientIp, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户登录异常: {}, 原因: {}", loginRequest.getUsername(), e.getMessage());
            recordLoginLog(loginRequest.getUsername(), clientIp, false, "系统异常");
            throw new RuntimeException("登录失败，请稍后重试", e);
        }
    }

    @Override
    @Transactional
    public boolean logout(String token, String clientIp) {
        log.info("用户登出请求");
        
        try {
            if (!StringUtils.hasText(token)) {
                log.warn("登出失败：令牌为空");
                return false;
            }

            // 验证令牌
            if (!jwtTokenUtil.validateToken(token)) {
                log.warn("登出失败：无效令牌");
                return false;
            }

            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            // 将令牌加入黑名单
            blacklistToken(token);
            
            // 清除刷新令牌
            clearRefreshToken(username);
            
            // 记录登出日志
            recordLoginLog(username, clientIp, true, "用户登出");
            
            log.info("用户登出成功: {}", username);
            return true;
            
        } catch (Exception e) {
            log.error("用户登出异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken, String clientIp) {
        log.info("刷新令牌请求");
        
        try {
            if (!StringUtils.hasText(refreshToken)) {
                throw new IllegalArgumentException("刷新令牌不能为空");
            }

            // 验证刷新令牌
            if (!jwtTokenUtil.validateToken(refreshToken)) {
                throw new IllegalArgumentException("无效的刷新令牌");
            }

            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            
            // 检查缓存中的刷新令牌
            String cachedRefreshToken = getCachedRefreshToken(username);
            if (!refreshToken.equals(cachedRefreshToken)) {
                throw new IllegalArgumentException("刷新令牌不匹配");
            }

            // 生成新的访问令牌
            String newAccessToken = jwtTokenUtil.generateToken(username);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(username);
            
            // 更新缓存中的刷新令牌
            cacheRefreshToken(username, newRefreshToken);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", jwtExpiration);
            
            log.info("令牌刷新成功: {}", username);
            return result;
            
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("刷新令牌失败", e);
        }
    }

    @Override
    public Map<String, Object> getCurrentUserInfo(String token) {
        log.info("获取当前用户信息");
        
        try {
            if (!StringUtils.hasText(token)) {
                throw new IllegalArgumentException("令牌不能为空");
            }

            if (!jwtTokenUtil.validateToken(token)) {
                throw new IllegalArgumentException("无效令牌");
            }

            String username = jwtTokenUtil.getUsernameFromToken(token);
            User user = getUserByUsername(username);
            
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }

            return buildUserInfoMap(user);
            
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            throw new RuntimeException("获取用户信息失败", e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("验证令牌");
        
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }

            // 检查令牌是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return false;
            }

            return jwtTokenUtil.validateToken(token);
            
        } catch (Exception e) {
            log.error("验证令牌失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public User validateCredentials(String username, String password) {
        log.debug("验证用户凭据: {}", username);
        
        try {
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return null;
            }

            User user = getUserByUsername(username);
            if (user == null) {
                return null;
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return null;
            }

            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() != 1) {
                return null;
            }

            return user;
            
        } catch (Exception e) {
            log.error("验证用户凭据失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isUserLocked(User user) {
        log.debug("检查用户是否被锁定: {}", user.getUsername());
        
        try {
            // TODO: 实现用户锁定检查逻辑
            // 可以基于登录失败次数、锁定时间等进行判断
            return false;
            
        } catch (Exception e) {
            log.error("检查用户锁定状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isUserExpired(User user) {
        log.debug("检查用户是否已过期: {}", user.getUsername());
        
        try {
            // TODO: 实现用户过期检查逻辑
            // 可以基于用户的有效期字段进行判断
            return false;
            
        } catch (Exception e) {
            log.error("检查用户过期状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void updateLastLoginInfo(Long userId, String clientIp) {
        log.debug("更新用户最后登录信息: userId={}, clientIp={}", userId, clientIp);
        
        try {
             User user = new User();
             user.setUserId(userId);
             user.setLastLoginTime(LocalDateTime.now());
             user.setLastLoginIp(clientIp);
             userMapper.updateById(user);
            
        } catch (Exception e) {
            log.error("更新用户登录信息失败: {}", e.getMessage());
        }
    }

    @Override
    public void recordLoginLog(String username, String clientIp, boolean success, String message) {
        log.debug("记录登录日志: username={}, success={}", username, success);
        
        try {
            // TODO: 实现登录日志记录逻辑
            // 可以调用LoginLogService或直接操作数据库
            log.info("登录日志: 用户={}, IP={}, 成功={}, 消息={}", username, clientIp, success, message);
            
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> buildUserInfoMap(User user) {
        log.debug("构建用户信息Map: {}", user.getUsername());
        
        Map<String, Object> userInfo = new HashMap<>();
         userInfo.put("id", user.getUserId());
         userInfo.put("username", user.getUsername());
         userInfo.put("email", user.getEmail());
         userInfo.put("realName", user.getRealName());
         userInfo.put("phone", user.getPhone());
         userInfo.put("departmentId", user.getDepartmentId());
         userInfo.put("roleId", user.getRoles() != null && !user.getRoles().isEmpty() ? user.getRoles().get(0).getId() : null);
         userInfo.put("status", user.getStatus());
         userInfo.put("createTime", user.getCreateTime());
         userInfo.put("lastLoginTime", user.getLastLoginTime());
         userInfo.put("lastLoginIp", user.getLastLoginIp());
        
        return userInfo;
    }

    @Override
    public boolean checkPasswordStrength(String password) {
        log.debug("检查密码强度");
        
        try {
            if (!StringUtils.hasText(password)) {
                return false;
            }

            // TODO: 实现密码强度检查逻辑
            // 可以检查长度、复杂度等
            return password.length() >= 8;
            
        } catch (Exception e) {
            log.error("检查密码强度失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String generatePasswordResetToken(String email) {
        log.info("生成密码重置令牌: {}", email);
        
        try {
            // TODO: 实现密码重置令牌生成逻辑
            // 可以生成临时令牌并设置过期时间
            return "reset_token_" + System.currentTimeMillis();
            
        } catch (Exception e) {
            log.error("生成密码重置令牌失败: {}", e.getMessage());
            throw new RuntimeException("生成密码重置令牌失败", e);
        }
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        log.debug("验证密码重置令牌");
        
        try {
            // TODO: 实现密码重置令牌验证逻辑
            return StringUtils.hasText(token);
            
        } catch (Exception e) {
            log.error("验证密码重置令牌失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        log.info("重置密码");
        
        try {
            // TODO: 实现密码重置逻辑
            // 验证令牌，更新用户密码
            return true;
            
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码: userId={}", userId);
        
        try {
            // TODO: 实现密码修改逻辑
            // 验证旧密码，更新新密码
            return true;
            
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return false;
        }
    }

    // 私有辅助方法

    private User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    private void cacheRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_KEY + username;
        redisTemplate.opsForValue().set(key, refreshToken, refreshExpiration, TimeUnit.SECONDS);
    }

    private String getCachedRefreshToken(String username) {
        String key = REFRESH_TOKEN_KEY + username;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    private void clearRefreshToken(String username) {
        String key = REFRESH_TOKEN_KEY + username;
        redisTemplate.delete(key);
    }

    private void blacklistToken(String token) {
        String key = BLACKLIST_TOKEN_KEY + token;
        Long remainingTime = jwtTokenUtil.getTokenRemainingTime(token);
        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", remainingTime, TimeUnit.SECONDS);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_TOKEN_KEY + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}