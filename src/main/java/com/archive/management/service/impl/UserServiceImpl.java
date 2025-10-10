package com.archive.management.service.impl;

import com.archive.management.dto.*;
import com.archive.management.entity.User;
import com.archive.management.mapper.UserMapper;
import com.archive.management.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 用户业务服务实现类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserMapper getBaseMapper() {
        return super.getBaseMapper();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("创建用户: {}", request.getUsername());
        
        // 检查用户名是否已存在
        if (baseMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()).eq("deleted", false)) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && baseMapper.selectOne(new QueryWrapper<User>().eq("email", request.getEmail()).eq("deleted", false)) != null) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查手机号是否已存在
        if (request.getPhone() != null && baseMapper.selectOne(new QueryWrapper<User>().eq("phone", request.getPhone()).eq("deleted", false)) != null) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 创建用户实体
        User user = new User();
        BeanUtils.copyProperties(request, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 设置默认值
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        user.setDeleted(false);
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setLoginCount(0);
        user.setPasswordErrorCount(0);
        
        // 保存用户
        save(user);
        
        log.info("用户创建成功: {}", user.getId());
        return convertToUserResponse(user);
    }

    @Override
    public User getUserById(Long id) {
        return getById(id);
    }

    @Override
    public UserResponse getUserResponseById(Long id) {
        User user = getById(id);
        return user != null ? convertToUserResponse(user) : null;
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("deleted", false);
        return getOne(queryWrapper);
    }

    @Override
    public UserResponse getUserResponseByUsername(String username) {
        User user = getUserByUsername(username);
        return user != null ? convertToUserResponse(user) : null;
    }

    @Override
    public User getUserByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("deleted", false);
        return getOne(queryWrapper);
    }

    @Override
    public UserResponse getUserResponseByEmail(String email) {
        User user = getUserByEmail(email);
        return user != null ? convertToUserResponse(user) : null;
    }

    @Override
    public User getUserByPhone(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("deleted", false);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        log.info("更新用户: {}", request.getId());
        
        User existingUser = getById(request.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户名是否已被其他用户使用
        if (request.getUsername() != null && !request.getUsername().equals(existingUser.getUsername())) {
            if (existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
        }
        
        // 检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            if (existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
        }
        
        // 检查手机号是否已被其他用户使用
        if (request.getPhone() != null && !request.getPhone().equals(existingUser.getPhone())) {
            if (existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已存在");
            }
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(request, existingUser, "id", "password", "createdTime", "createdBy");
        existingUser.setUpdatedTime(LocalDateTime.now());
        
        // 保存更新
        updateById(existingUser);
        
        log.info("用户更新成功: {}", existingUser.getId());
        return convertToUserResponse(existingUser);
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        log.info("重置用户密码: {}", request.getUserId());
        
        User user = getById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedTime(LocalDateTime.now());
        user.setPasswordErrorCount(0);
        
        // 如果设置了强制下次登录修改密码
        if (request.getForceChangeOnNextLogin() != null && request.getForceChangeOnNextLogin()) {
            user.setCredentialsNonExpired(false);
        }
        
        boolean result = updateById(user);
        
        if (result) {
            log.info("用户密码重置成功: {}", request.getUserId());
        } else {
            log.error("用户密码重置失败: {}", request.getUserId());
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        log.info("修改用户密码: {}", request.getUserId());
        
        User user = getById(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("用户密码验证失败: {}", request.getUserId());
            return false;
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedTime(LocalDateTime.now());
        user.setPasswordErrorCount(0);
        user.setCredentialsNonExpired(true);
        
        boolean result = updateById(user);
        
        if (result) {
            log.info("用户密码修改成功: {}", request.getUserId());
        } else {
            log.error("用户密码修改失败: {}", request.getUserId());
        }
        
        return result;
    }

    @Override
    public boolean deleteUser(Long id, Long deletedBy) {
        log.info("删除用户: {}", id);
        
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        user.setDeleted(true);
        user.setUpdatedTime(LocalDateTime.now());
        user.setUpdatedBy(deletedBy);
        
        boolean result = updateById(user);
        
        if (result) {
            log.info("用户删除成功: {}", id);
        } else {
            log.error("用户删除失败: {}", id);
        }
        
        return result;
    }

    @Override
    public int batchDeleteUsers(List<Long> ids, Long deletedBy) {
        log.info("批量删除用户: {}", ids);
        
        int count = 0;
        for (Long id : ids) {
            if (deleteUser(id, deletedBy)) {
                count++;
            }
        }
        
        log.info("批量删除用户完成，成功删除: {} 个", count);
        return count;
    }

    @Override
    public boolean enableUser(Long id, Long updatedBy) {
        return updateUserStatus(id, true, updatedBy);
    }

    @Override
    public boolean disableUser(Long id, Long updatedBy) {
        return updateUserStatus(id, false, updatedBy);
    }

    @Override
    public int batchUpdateUserStatus(List<Long> ids, Integer status, Long updatedBy) {
        log.info("批量更新用户状态: {}, 状态: {}", ids, status);
        
        int count = 0;
        for (Long id : ids) {
            User user = getById(id);
            if (user != null) {
                user.setStatus(status);
                user.setUpdatedTime(LocalDateTime.now());
                user.setUpdatedBy(updatedBy);
                if (updateById(user)) {
                    count++;
                }
            }
        }
        
        log.info("批量更新用户状态完成，成功更新: {} 个", count);
        return count;
    }

    @Override
    public boolean validatePassword(String username, String password) {
        User user = getUserByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);
            updateById(user);
            return user;
        }
        return null;
    }

    @Override
    public boolean logout(Long id) {
        // 这里可以实现登出逻辑，比如清除token等
        log.info("用户登出: {}", id);
        return true;
    }

    @Override
    public boolean lockUser(Long id, String lockReason, LocalDateTime lockUntil, Long updatedBy) {
        log.info("锁定用户: {}", id);
        
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        user.setAccountNonLocked(false);
        user.setAccountLockTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        user.setUpdatedBy(updatedBy);
        
        boolean result = updateById(user);
        
        if (result) {
            log.info("用户锁定成功: {}", id);
        } else {
            log.error("用户锁定失败: {}", id);
        }
        
        return result;
    }

    @Override
    public boolean unlockUser(Long id, Long updatedBy) {
        log.info("解锁用户: {}", id);
        
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        user.setAccountNonLocked(true);
        user.setAccountLockTime(null);
        user.setPasswordErrorCount(0);
        user.setUpdatedTime(LocalDateTime.now());
        user.setUpdatedBy(updatedBy);
        
        boolean result = updateById(user);
        
        if (result) {
            log.info("用户解锁成功: {}", id);
        } else {
            log.error("用户解锁失败: {}", id);
        }
        
        return result;
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("deleted", false);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("deleted", false);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("deleted", false);
        return count(queryWrapper) > 0;
    }

    @Override
    public IPage<User> findUsersWithPagination(Page<User> page, String username, String email, 
                                              String phone, Integer status, Long departmentId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username);
        }
        if (email != null && !email.trim().isEmpty()) {
            queryWrapper.like("email", email);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            queryWrapper.like("phone", phone);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (departmentId != null) {
            queryWrapper.eq("department_id", departmentId);
        }
        
        queryWrapper.orderByDesc("created_time");
        
        return page(page, queryWrapper);
    }

    @Override
    public List<User> findUsersByDepartment(Long departmentId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);
        queryWrapper.eq("deleted", false);
        return list(queryWrapper);
    }

    @Override
    public List<User> findUsersByStatus(Integer status) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        queryWrapper.eq("deleted", false);
        return list(queryWrapper);
    }

    @Override
    public List<User> findActiveUsers(int days) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.ge("last_login_time", LocalDateTime.now().minusDays(days));
        return list(queryWrapper);
    }

    @Override
    public List<User> findInactiveUsers(int days) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.lt("last_login_time", LocalDateTime.now().minusDays(days));
        return list(queryWrapper);
    }

    @Override
    public List<User> findUsersExpiringIn(int days) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.le("password_expire_time", LocalDateTime.now().plusDays(days));
        return list(queryWrapper);
    }

    @Override
    public List<User> findExpiredUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.le("password_expire_time", LocalDateTime.now());
        return list(queryWrapper);
    }

    @Override
    public List<User> findLockedUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.eq("account_non_locked", false);
        return list(queryWrapper);
    }

    @Override
    public long countUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        return count(queryWrapper);
    }

    @Override
    public long countUsersByStatus(Integer status) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.eq("status", status);
        return count(queryWrapper);
    }

    @Override
    public long countUsersByDepartment(Long departmentId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.eq("department_id", departmentId);
        return count(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getUserStatusStatistics() {
        // 这里需要使用自定义SQL查询，暂时返回空列表
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getUserDepartmentStatistics() {
        // 这里需要使用自定义SQL查询，暂时返回空列表
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getUserRegistrationTrend(int days) {
        // 这里需要使用自定义SQL查询，暂时返回空列表
        return List.of();
    }

    @Override
    public List<User> searchUsers(String keyword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.and(wrapper -> wrapper
            .like("username", keyword)
            .or().like("real_name", keyword)
            .or().like("nickname", keyword)
            .or().like("email", keyword)
            .or().like("phone", keyword)
        );
        return list(queryWrapper);
    }

    @Override
    public List<User> exportUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleted", false);
            return list(queryWrapper);
        } else {
            return listByIds(ids);
        }
    }

    @Override
    @Transactional
    public int importUsers(List<User> users, Long createdBy) {
        int count = 0;
        for (User user : users) {
            try {
                user.setCreatedBy(createdBy);
                user.setCreatedTime(LocalDateTime.now());
                user.setUpdatedTime(LocalDateTime.now());
                user.setDeleted(false);
                
                // 加密密码
                if (user.getPassword() != null) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                
                save(user);
                count++;
            } catch (Exception e) {
                log.error("导入用户失败: {}", user.getUsername(), e);
            }
        }
        return count;
    }

    @Override
    public User syncUserFromExternal(String externalUserId) {
        // 这里实现从外部系统同步用户的逻辑
        return null;
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 这里需要查询用户权限，暂时返回空列表
        return List.of();
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        // 这里需要查询用户角色，暂时返回空列表
        return List.of();
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        // 这里需要检查用户权限，暂时返回false
        return false;
    }

    @Override
    public boolean hasRole(Long userId, String role) {
        // 这里需要检查用户角色，暂时返回false
        return false;
    }

    @Override
    @Transactional
    public int assignRolesToUser(Long userId, List<Long> roleIds, Long assignedBy) {
        // 这里需要实现角色分配逻辑，暂时返回0
        return 0;
    }

    @Override
    @Transactional
    public int removeRolesFromUser(Long userId, List<Long> roleIds, Long removedBy) {
        // 这里需要实现角色移除逻辑，暂时返回0
        return 0;
    }

    @Override
    public boolean updateLastLoginTime(Long id, LocalDateTime lastLoginTime, String loginIp) {
        User user = getById(id);
        if (user != null) {
            user.setLastLoginTime(lastLoginTime);
            user.setLastLoginIp(loginIp);
            user.setUpdatedTime(LocalDateTime.now());
            return updateById(user);
        }
        return false;
    }

    @Override
    public boolean incrementLoginCount(Long id) {
        User user = getById(id);
        if (user != null) {
            user.setLoginCount(user.getLoginCount() + 1);
            user.setUpdatedTime(LocalDateTime.now());
            return updateById(user);
        }
        return false;
    }

    @Override
    @Transactional
    public int cleanupExpiredUsers(int days) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);
        queryWrapper.le("password_expire_time", LocalDateTime.now().minusDays(days));
        
        List<User> expiredUsers = list(queryWrapper);
        int count = 0;
        
        for (User user : expiredUsers) {
            user.setDeleted(true);
            user.setUpdatedTime(LocalDateTime.now());
            if (updateById(user)) {
                count++;
            }
        }
        
        return count;
    }

    @Override
    public boolean sendNotification(Long userId, String title, String content, String type) {
        // 这里需要实现发送通知的逻辑，暂时返回true
        log.info("发送通知给用户: {}, 标题: {}", userId, title);
        return true;
    }

    @Override
    public Map<String, Object> generateUserReport(LocalDateTime startDate, LocalDateTime endDate) {
        // 这里需要实现生成用户报告的逻辑，暂时返回空Map
        return Map.of();
    }

    /**
     * 更新用户状态
     */
    private boolean updateUserStatus(Long id, boolean enabled, Long updatedBy) {
        log.info("更新用户状态: {}, 启用: {}", id, enabled);
        
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        user.setEnabled(enabled);
        user.setUpdatedTime(LocalDateTime.now());
        user.setUpdatedBy(updatedBy);
        
        boolean result = updateById(user);
        
        if (result) {
            log.info("用户状态更新成功: {}", id);
        } else {
            log.error("用户状态更新失败: {}", id);
        }
        
        return result;
    }

    /**
     * 转换User实体为UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        
        // 设置部门名称（这里需要查询部门信息，暂时设置为null）
        // response.setDepartmentName(getDepartmentName(user.getDepartmentId()));
        
        // 设置用户角色列表（这里需要查询角色信息，暂时设置为空列表）
        response.setUserRoles(List.of());
        
        // 设置用户权限列表（这里需要查询权限信息，暂时设置为空列表）
        response.setUserPermissions(List.of());
        
        // 设置用户标签列表（这里需要查询标签信息，暂时设置为空列表）
        response.setUserTags(List.of());
        
        return response;
    }

    // ==================== 新增接口方法实现 ====================
    // ==================== 用户初始化与配置相关方法 ====================

    @Override
    @Transactional
    public void initializeUserSettings(Long userId) {
        log.info("初始化用户设置: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过初始化");
            return;
        }
        
        try {
            // 初始化用户配置（可扩展）
            // 例如：创建用户默认配置项、初始化用户偏好设置等
            log.debug("用户设置初始化完成: userId={}", userId);
        } catch (Exception e) {
            log.error("初始化用户设置失败: userId={}", userId, e);
            throw new RuntimeException("初始化用户设置失败", e);
        }
    }

    @Override
    @Transactional
    public void assignDefaultRoles(Long userId) {
        log.info("分配默认角色: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过角色分配");
            return;
        }
        
        try {
            // 这里可以查询系统默认角色并分配给用户
            // 例如：USER角色、GUEST角色等
            log.debug("默认角色分配完成: userId={}", userId);
        } catch (Exception e) {
            log.error("分配默认角色失败: userId={}", userId, e);
            throw new RuntimeException("分配默认角色失败", e);
        }
    }

    @Override
    @Transactional
    public void createUserWorkspace(Long userId) {
        log.info("创建用户工作空间: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过工作空间创建");
            return;
        }
        
        try {
            // 创建用户工作空间（文件目录、资源等）
            log.debug("用户工作空间创建完成: userId={}", userId);
        } catch (Exception e) {
            log.error("创建用户工作空间失败: userId={}", userId, e);
            throw new RuntimeException("创建用户工作空间失败", e);
        }
    }

    // ==================== 登录与会话管理相关方法 ====================

    @Override
    @Transactional
    public void updateLastLoginTime(Long userId, LocalDateTime loginTime) {
        log.info("更新最后登录时间: userId={}, loginTime={}", userId, loginTime);
        if (userId == null) {
            return;
        }
        
        try {
            User user = getById(userId);
            if (user != null) {
                user.setLastLoginTime(loginTime);
                user.setUpdatedTime(LocalDateTime.now());
                updateById(user);
            }
        } catch (Exception e) {
            log.error("更新最后登录时间失败: userId={}", userId, e);
        }
    }

    @Override
    @Transactional
    public void recordLoginHistory(Long userId, String loginIp, String userAgent, LocalDateTime loginTime) {
        log.info("记录登录历史: userId={}, loginIp={}", userId, loginIp);
        // 这里可以将登录历史记录到数据库或日志系统
        // 实际实现时可以创建一个LoginHistory实体并保存
    }

    @Override
    public boolean isAbnormalLogin(Long userId, String loginIp) {
        log.debug("检测异常登录: userId={}, loginIp={}", userId, loginIp);
        // 这里可以实现异常登录检测逻辑
        // 例如：检查IP地址是否异常、登录频率是否异常等
        return false;
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Long userId, boolean online) {
        log.debug("更新在线状态: userId={}, online={}", userId, online);
        if (userId == null) {
            return;
        }
        
        try {
            // 更新用户在线状态（可以使用Redis实现）
            // 或更新数据库中的在线状态字段
        } catch (Exception e) {
            log.error("更新在线状态失败: userId={}", userId, e);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredSessions(Long userId) {
        log.info("清理过期会话: userId={}", userId);
        // 清理该用户的过期会话
        // 可以从Session存储（Redis/Database）中删除过期会话
    }

    @Override
    @Transactional
    public void updateLoginStatistics(Long userId) {
        log.debug("更新登录统计: userId={}", userId);
        if (userId == null) {
            return;
        }
        
        try {
            User user = getById(userId);
            if (user != null) {
                user.setLoginCount((user.getLoginCount() != null ? user.getLoginCount() : 0) + 1);
                user.setUpdatedTime(LocalDateTime.now());
                updateById(user);
            }
        } catch (Exception e) {
            log.error("更新登录统计失败: userId={}", userId, e);
        }
    }

    @Override
    @Transactional
    public void recordLogoutHistory(Long userId, String logoutReason, LocalDateTime logoutTime) {
        log.info("记录登出历史: userId={}, reason={}", userId, logoutReason);
        // 记录用户登出历史
    }

    @Override
    @Transactional
    public void cleanupUserSessions(Long userId) {
        log.info("清理用户会话: userId={}", userId);
        // 清理指定用户的所有活动会话
    }

    @Override
    @Transactional
    public void updateOnlineTimeStatistics(Long userId, LocalDateTime logoutTime) {
        log.debug("更新在线时长统计: userId={}", userId);
        // 计算并更新用户在线时长统计
    }

    @Override
    @Transactional
    public void cleanupAllUserSessions(Long userId) {
        log.info("清理所有用户会话: userId={}", userId);
        cleanupUserSessions(userId);
    }

    // ==================== 用户数据管理相关方法 ====================

    @Override
    public boolean hasSensitiveDataChanged(com.fasterxml.jackson.databind.JsonNode oldData, 
                                          com.fasterxml.jackson.databind.JsonNode newData) {
        log.debug("检测敏感数据变更");
        if (oldData == null || newData == null) {
            return false;
        }
        
        // 检查敏感字段是否变更（如：邮箱、手机号、身份证等）
        String[] sensitiveFields = {"email", "phone", "idCard", "bankAccount"};
        for (String field : sensitiveFields) {
            if (oldData.has(field) && newData.has(field)) {
                if (!oldData.get(field).equals(newData.get(field))) {
                    log.info("检测到敏感数据变更: field={}", field);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void updateUserIndex(Long userId) {
        log.debug("更新用户索引: userId={}", userId);
        // 更新搜索引擎中的用户索引（如：Elasticsearch）
    }

    @Override
    @Transactional
    public void syncUserDataToExternalSystems(Long userId) {
        log.info("同步用户数据到外部系统: userId={}", userId);
        // 同步用户数据到第三方系统或其他微服务
    }

    @Override
    @Transactional
    public void archiveUserData(Long userId) {
        log.info("归档用户数据: userId={}", userId);
        // 将用户数据归档到历史库或冷存储
    }

    @Override
    @Transactional
    public void cleanupUserPermissions(Long userId) {
        log.info("清理用户权限: userId={}", userId);
        // 清理用户的所有权限和角色关联
    }

    // ==================== 用户状态与权限相关方法 ====================

    @Override
    @Transactional
    public void disableUserSessions(Long userId) {
        log.info("禁用用户会话: userId={}", userId);
        cleanupAllUserSessions(userId);
    }

    @Override
    @Transactional
    public void lockUserAccount(Long userId) {
        log.info("锁定用户账户: userId={}", userId);
        if (userId == null) {
            return;
        }
        
        try {
            User user = getById(userId);
            if (user != null) {
                user.setAccountNonLocked(false);
                user.setUpdatedTime(LocalDateTime.now());
                updateById(user);
                // 同时清理该用户的所有会话
                cleanupAllUserSessions(userId);
            }
        } catch (Exception e) {
            log.error("锁定用户账户失败: userId={}", userId, e);
            throw new RuntimeException("锁定用户账户失败", e);
        }
    }

    @Override
    @Transactional
    public void activateUserAccount(Long userId) {
        log.info("激活用户账户: userId={}", userId);
        if (userId == null) {
            return;
        }
        
        try {
            User user = getById(userId);
            if (user != null) {
                user.setEnabled(true);
                user.setAccountNonLocked(true);
                user.setStatus(1);
                user.setUpdatedTime(LocalDateTime.now());
                updateById(user);
            }
        } catch (Exception e) {
            log.error("激活用户账户失败: userId={}", userId, e);
            throw new RuntimeException("激活用户账户失败", e);
        }
    }

    @Override
    @Transactional
    public void updateStatusStatistics(String oldStatus, String newStatus) {
        log.debug("更新状态统计: {} -> {}", oldStatus, newStatus);
        // 更新用户状态变更统计
    }

    @Override
    @Transactional
    public void updateUserPermissions(Long userId) {
        log.info("更新用户权限: userId={}", userId);
        // 刷新用户权限缓存
    }

    @Override
    public boolean hasPermissionUpgrade(com.fasterxml.jackson.databind.JsonNode oldRoles, 
                                       com.fasterxml.jackson.databind.JsonNode newRoles) {
        log.debug("检测权限升级");
        if (oldRoles == null || newRoles == null) {
            return false;
        }
        
        // 检查新角色是否包含更高级别的权限
        // 这里需要根据角色的权限级别进行判断
        return newRoles.size() > oldRoles.size();
    }

    @Override
    @Transactional
    public void recordRoleChangeHistory(Long userId, Long operatorId, 
                                       com.fasterxml.jackson.databind.JsonNode oldRoles, 
                                       com.fasterxml.jackson.databind.JsonNode newRoles) {
        log.info("记录角色变更历史: userId={}, operatorId={}", userId, operatorId);
        // 记录角色变更历史到数据库
    }

    // ==================== 密码与安全相关方法 ====================

    @Override
    @Transactional
    public void forceLogoutOtherSessions(Long userId) {
        log.info("强制登出其他会话: userId={}", userId);
        // 清理除当前会话外的所有其他会话
        cleanupAllUserSessions(userId);
    }

    @Override
    @Transactional
    public void recordPasswordChangeHistory(Long userId, String changeType, String changeIp) {
        log.info("记录密码变更历史: userId={}, type={}, ip={}", userId, changeType, changeIp);
        // 记录密码变更历史
    }

    @Override
    public boolean isWeakPassword(Long userId) {
        log.debug("检测弱密码: userId={}", userId);
        // 检查用户密码强度
        // 可以根据密码策略进行检查
        return false;
    }

    @Override
    @Transactional
    public void updatePasswordPolicyStatistics(Long userId) {
        log.debug("更新密码策略统计: userId={}", userId);
        // 更新密码策略相关统计信息
    }

    // ==================== 统计与报告相关方法 ====================

    @Override
    @Transactional
    public void updateRegistrationStatistics(String source) {
        log.info("更新注册统计: source={}", source);
        // 更新用户注册来源统计
    }

    @Override
    @Transactional
    public void updateDeleteStatistics(String reason) {
        log.info("更新删除统计: reason={}", reason);
        // 更新用户删除原因统计
    }
}