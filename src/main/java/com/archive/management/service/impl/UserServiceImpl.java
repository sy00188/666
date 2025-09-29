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
}