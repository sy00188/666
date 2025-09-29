package com.archive.management.service;

import com.archive.management.dto.LoginRequest;
import com.archive.management.dto.LoginResponse;
import com.archive.management.entity.User;

import java.util.Map;

/**
 * 认证服务接口
 * 定义用户认证相关的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest, String clientIp);

    /**
     * 用户登出
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 是否登出成功
     */
    boolean logout(String token, String clientIp);

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @param clientIp 客户端IP
     * @return 新的令牌信息
     */
    Map<String, Object> refreshToken(String refreshToken, String clientIp);

    /**
     * 获取当前用户信息
     * @param token 访问令牌
     * @return 用户信息
     */
    Map<String, Object> getCurrentUserInfo(String token);

    /**
     * 验证令牌有效性
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 验证用户凭据
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，验证失败返回null
     */
    User validateCredentials(String username, String password);

    /**
     * 检查用户是否被锁定
     * @param user 用户对象
     * @return 是否被锁定
     */
    boolean isUserLocked(User user);

    /**
     * 检查用户是否已过期
     * @param user 用户对象
     * @return 是否已过期
     */
    boolean isUserExpired(User user);

    /**
     * 更新用户最后登录信息
     * @param userId 用户ID
     * @param clientIp 客户端IP
     */
    void updateLastLoginInfo(Long userId, String clientIp);

    /**
     * 记录登录日志
     * @param username 用户名
     * @param clientIp 客户端IP
     * @param success 是否成功
     * @param message 消息
     */
    void recordLoginLog(String username, String clientIp, boolean success, String message);

    /**
     * 生成用户信息Map
     * @param user 用户对象
     * @return 用户信息Map
     */
    Map<String, Object> buildUserInfoMap(User user);

    /**
     * 检查密码强度
     * @param password 密码
     * @return 是否符合强度要求
     */
    boolean checkPasswordStrength(String password);

    /**
     * 生成密码重置令牌
     * @param email 邮箱
     * @return 重置令牌
     */
    String generatePasswordResetToken(String email);

    /**
     * 验证密码重置令牌
     * @param token 重置令牌
     * @return 是否有效
     */
    boolean validatePasswordResetToken(String token);

    /**
     * 重置密码
     * @param token 重置令牌
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(String token, String newPassword);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}