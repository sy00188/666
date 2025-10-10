package com.archive.management.service;

import com.archive.management.dto.*;
import com.archive.management.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户业务服务接口
 * 定义用户管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 创建用户
     * @param request 创建用户请求
     * @return 用户响应信息
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 根据ID获取用户响应信息
     * @param id 用户ID
     * @return 用户响应信息
     */
    UserResponse getUserResponseById(Long id);

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据用户名获取用户响应信息
     * @param username 用户名
     * @return 用户响应信息
     */
    UserResponse getUserResponseByUsername(String username);

    /**
     * 根据邮箱获取用户
     * @param email 邮箱
     * @return 用户信息
     */
    User getUserByEmail(String email);

    /**
     * 根据邮箱获取用户响应信息
     * @param email 邮箱
     * @return 用户响应信息
     */
    UserResponse getUserResponseByEmail(String email);

    /**
     * 根据手机号获取用户
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 更新用户信息
     * @param request 更新用户请求
     * @return 用户响应信息
     */
    UserResponse updateUser(UpdateUserRequest request);

    /**
     * 删除用户（软删除）
     * @param id 用户ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id, Long deletedBy);

    /**
     * 批量删除用户（软删除）
     * @param ids 用户ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteUsers(List<Long> ids, Long deletedBy);

    /**
     * 启用用户
     * @param id 用户ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableUser(Long id, Long updatedBy);

    /**
     * 禁用用户
     * @param id 用户ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableUser(Long id, Long updatedBy);

    /**
     * 批量更新用户状态
     * @param ids 用户ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateUserStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 重置密码
     * @param request 重置密码请求
     * @return 是否重置成功
     */
    boolean resetPassword(ResetPasswordRequest request);

    /**
     * 修改密码
     * @param request 修改密码请求
     * @return 是否修改成功
     */
    boolean changePassword(ChangePasswordRequest request);

    /**
     * 验证用户密码
     * @param username 用户名
     * @param password 密码
     * @return 是否验证成功
     */
    boolean validatePassword(String username, String password);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 用户信息（登录成功）或null（登录失败）
     */
    User login(String username, String password);

    /**
     * 用户登出
     * @param id 用户ID
     * @return 是否登出成功
     */
    boolean logout(Long id);

    /**
     * 锁定用户账户
     * @param id 用户ID
     * @param lockReason 锁定原因
     * @param lockUntil 锁定到期时间
     * @param updatedBy 更新人ID
     * @return 是否锁定成功
     */
    boolean lockUser(Long id, String lockReason, LocalDateTime lockUntil, Long updatedBy);

    /**
     * 解锁用户账户
     * @param id 用户ID
     * @param updatedBy 更新人ID
     * @return 是否解锁成功
     */
    boolean unlockUser(Long id, Long updatedBy);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 分页查询用户
     * @param page 分页参数
     * @param username 用户名（可选）
     * @param email 邮箱（可选）
     * @param phone 手机号（可选）
     * @param status 状态（可选）
     * @param departmentId 部门ID（可选）
     * @return 分页结果
     */
    IPage<User> findUsersWithPagination(Page<User> page, String username, String email, 
                                       String phone, Integer status, Long departmentId);

    /**
     * 根据部门ID查找用户列表
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<User> findUsersByDepartment(Long departmentId);

    /**
     * 根据状态查找用户列表
     * @param status 状态
     * @return 用户列表
     */
    List<User> findUsersByStatus(Integer status);

    /**
     * 查找活跃用户列表
     * @param days 天数
     * @return 用户列表
     */
    List<User> findActiveUsers(int days);

    /**
     * 查找非活跃用户列表
     * @param days 天数
     * @return 用户列表
     */
    List<User> findInactiveUsers(int days);

    /**
     * 查找即将过期的用户列表
     * @param days 天数
     * @return 用户列表
     */
    List<User> findUsersExpiringIn(int days);

    /**
     * 查找已过期的用户列表
     * @return 用户列表
     */
    List<User> findExpiredUsers();

    /**
     * 查找被锁定的用户列表
     * @return 用户列表
     */
    List<User> findLockedUsers();

    /**
     * 统计用户总数
     * @return 总数
     */
    long countUsers();

    /**
     * 根据状态统计用户数量
     * @param status 状态
     * @return 数量
     */
    long countUsersByStatus(Integer status);

    /**
     * 根据部门统计用户数量
     * @param departmentId 部门ID
     * @return 数量
     */
    long countUsersByDepartment(Long departmentId);

    /**
     * 获取用户状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getUserStatusStatistics();

    /**
     * 获取用户部门统计
     * @return 部门统计结果
     */
    List<Map<String, Object>> getUserDepartmentStatistics();

    /**
     * 获取用户注册趋势统计
     * @param days 天数
     * @return 注册趋势统计结果
     */
    List<Map<String, Object>> getUserRegistrationTrend(int days);

    /**
     * 根据关键词搜索用户
     * @param keyword 关键词
     * @return 用户列表
     */
    List<User> searchUsers(String keyword);

    /**
     * 导出用户数据
     * @param ids 用户ID列表（可选，为空则导出所有）
     * @return 用户列表
     */
    List<User> exportUsers(List<Long> ids);

    /**
     * 批量导入用户
     * @param users 用户列表
     * @param createdBy 创建人ID
     * @return 导入成功的数量
     */
    int importUsers(List<User> users, Long createdBy);

    /**
     * 同步用户信息（从外部系统）
     * @param externalUserId 外部用户ID
     * @return 同步后的用户信息
     */
    User syncUserFromExternal(String externalUserId);

    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否有指定角色
     * @param userId 用户ID
     * @param role 角色标识
     * @return 是否有角色
     */
    boolean hasRole(Long userId, String role);

    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param assignedBy 分配人ID
     * @return 分配成功的数量
     */
    int assignRolesToUser(Long userId, List<Long> roleIds, Long assignedBy);

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param removedBy 移除人ID
     * @return 移除成功的数量
     */
    int removeRolesFromUser(Long userId, List<Long> roleIds, Long removedBy);

    /**
     * 更新用户最后登录时间
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @param loginIp 登录IP
     * @return 是否更新成功
     */
    boolean updateLastLoginTime(Long id, LocalDateTime lastLoginTime, String loginIp);

    /**
     * 增加用户登录次数
     * @param id 用户ID
     * @return 是否增加成功
     */
    boolean incrementLoginCount(Long id);

    /**
     * 清理过期的用户数据
     * @param days 过期天数
     * @return 清理的数量
     */
    int cleanupExpiredUsers(int days);

    /**
     * 发送用户通知
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 是否发送成功
     */
    boolean sendNotification(Long userId, String title, String content, String type);

    /**
     * 生成用户报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    Map<String, Object> generateUserReport(LocalDateTime startDate, LocalDateTime endDate);

    // ==================== 用户初始化与配置相关方法 ====================

    /**
     * 初始化用户设置
     * @param userId 用户ID
     */
    void initializeUserSettings(Long userId);

    /**
     * 分配默认角色
     * @param userId 用户ID
     */
    void assignDefaultRoles(Long userId);

    /**
     * 创建用户工作空间
     * @param userId 用户ID
     */
    void createUserWorkspace(Long userId);

    // ==================== 登录与会话管理相关方法 ====================

    /**
     * 更新最后登录时间（重载版本）
     * @param userId 用户ID
     * @param loginTime 登录时间
     */
    void updateLastLoginTime(Long userId, LocalDateTime loginTime);

    /**
     * 记录登录历史
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @param userAgent 用户代理
     * @param loginTime 登录时间
     */
    void recordLoginHistory(Long userId, String loginIp, String userAgent, LocalDateTime loginTime);

    /**
     * 检测异常登录
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 是否异常登录
     */
    boolean isAbnormalLogin(Long userId, String loginIp);

    /**
     * 更新在线状态
     * @param userId 用户ID
     * @param online 是否在线
     */
    void updateOnlineStatus(Long userId, boolean online);

    /**
     * 清理过期会话
     * @param userId 用户ID
     */
    void cleanupExpiredSessions(Long userId);

    /**
     * 更新登录统计
     * @param userId 用户ID
     */
    void updateLoginStatistics(Long userId);

    /**
     * 记录登出历史
     * @param userId 用户ID
     * @param logoutReason 登出原因
     * @param logoutTime 登出时间
     */
    void recordLogoutHistory(Long userId, String logoutReason, LocalDateTime logoutTime);

    /**
     * 清理用户会话
     * @param userId 用户ID
     */
    void cleanupUserSessions(Long userId);

    /**
     * 更新在线时长统计
     * @param userId 用户ID
     * @param logoutTime 登出时间
     */
    void updateOnlineTimeStatistics(Long userId, LocalDateTime logoutTime);

    /**
     * 清理所有用户会话
     * @param userId 用户ID
     */
    void cleanupAllUserSessions(Long userId);

    // ==================== 用户数据管理相关方法 ====================

    /**
     * 检测敏感数据变更
     * @param oldData 旧数据
     * @param newData 新数据
     * @return 是否有敏感数据变更
     */
    boolean hasSensitiveDataChanged(com.fasterxml.jackson.databind.JsonNode oldData, com.fasterxml.jackson.databind.JsonNode newData);

    /**
     * 更新用户索引
     * @param userId 用户ID
     */
    void updateUserIndex(Long userId);

    /**
     * 同步用户数据到外部系统
     * @param userId 用户ID
     */
    void syncUserDataToExternalSystems(Long userId);

    /**
     * 归档用户数据
     * @param userId 用户ID
     */
    void archiveUserData(Long userId);

    /**
     * 清理用户权限
     * @param userId 用户ID
     */
    void cleanupUserPermissions(Long userId);

    // ==================== 用户状态与权限相关方法 ====================

    /**
     * 禁用用户会话
     * @param userId 用户ID
     */
    void disableUserSessions(Long userId);

    /**
     * 锁定用户账户
     * @param userId 用户ID
     */
    void lockUserAccount(Long userId);

    /**
     * 激活用户账户
     * @param userId 用户ID
     */
    void activateUserAccount(Long userId);

    /**
     * 更新状态统计
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    void updateStatusStatistics(String oldStatus, String newStatus);

    /**
     * 更新用户权限
     * @param userId 用户ID
     */
    void updateUserPermissions(Long userId);

    /**
     * 检测权限升级
     * @param oldRoles 旧角色
     * @param newRoles 新角色
     * @return 是否有权限升级
     */
    boolean hasPermissionUpgrade(com.fasterxml.jackson.databind.JsonNode oldRoles, com.fasterxml.jackson.databind.JsonNode newRoles);

    /**
     * 记录角色变更历史
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @param oldRoles 旧角色
     * @param newRoles 新角色
     */
    void recordRoleChangeHistory(Long userId, Long operatorId, com.fasterxml.jackson.databind.JsonNode oldRoles, com.fasterxml.jackson.databind.JsonNode newRoles);

    // ==================== 密码与安全相关方法 ====================

    /**
     * 强制登出其他会话
     * @param userId 用户ID
     */
    void forceLogoutOtherSessions(Long userId);

    /**
     * 记录密码变更历史
     * @param userId 用户ID
     * @param changeType 变更类型
     * @param changeIp 变更IP
     */
    void recordPasswordChangeHistory(Long userId, String changeType, String changeIp);

    /**
     * 检测弱密码
     * @param userId 用户ID
     * @return 是否为弱密码
     */
    boolean isWeakPassword(Long userId);

    /**
     * 更新密码策略统计
     * @param userId 用户ID
     */
    void updatePasswordPolicyStatistics(Long userId);

    // ==================== 统计与报告相关方法 ====================

    /**
     * 更新注册统计
     * @param source 注册来源
     */
    void updateRegistrationStatistics(String source);

    /**
     * 更新删除统计
     * @param reason 删除原因
     */
    void updateDeleteStatistics(String reason);
}