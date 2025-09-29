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
}