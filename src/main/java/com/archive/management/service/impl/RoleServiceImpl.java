package com.archive.management.service.impl;

import com.archive.management.entity.Role;
import com.archive.management.entity.Permission;
import com.archive.management.entity.User;
import com.archive.management.mapper.RoleMapper;
import com.archive.management.mapper.PermissionMapper;
import com.archive.management.mapper.UserMapper;
import com.archive.management.service.RoleService;
import com.archive.management.service.CacheService;
import com.archive.management.exception.BusinessException;
import com.archive.management.exception.ResourceNotFoundException;
import com.archive.management.exception.DuplicateResourceException;
import com.archive.management.util.SecurityUtils;
import com.archive.management.common.util.ValidationUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 提供角色管理的完整功能，包括CRUD操作、权限管理、用户管理、层级管理等
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CacheService cacheService;

    @Override
    public RoleMapper getBaseMapper() {
        return roleMapper;
    }

    // ==================== 缓存键常量 ====================
    
    private static final String ROLE_CACHE_PREFIX = "role:";
    private static final String ROLE_LIST_CACHE_KEY = "role:list";
    private static final String ROLE_ENABLED_CACHE_KEY = "role:enabled";
    private static final String ROLE_PERMISSIONS_CACHE_PREFIX = "role:permissions:";
    private static final String ROLE_USERS_CACHE_PREFIX = "role:users:";
    private static final String ROLE_TREE_CACHE_KEY = "role:tree";
    private static final String ROLE_STATISTICS_CACHE_KEY = "role:statistics";

    // ==================== 角色基础CRUD操作 ====================

    /**
     * 创建角色
     */
    @Override
    @CacheEvict(value = "roleCache", allEntries = true)
    public Role createRole(Role role) {
        log.info("创建角色: {}", role.getRoleName());
        
        // 参数验证
        validateRoleForCreate(role);
        
        // 检查角色编码唯一性
        if (existsByRoleCode(role.getRoleCode())) {
            throw new DuplicateResourceException("角色编码已存在: " + role.getRoleCode());
        }
        
        // 检查角色名称唯一性
        if (existsByRoleName(role.getRoleName())) {
            throw new DuplicateResourceException("角色名称已存在: " + role.getRoleName());
        }
        
        // 设置默认值
        setDefaultValues(role);
        
        // 保存角色
        if (!save(role)) {
            throw new BusinessException("创建角色失败");
        }
        
        log.info("角色创建成功，ID: {}", role.getId());
        return role;
    }

    /**
     * 根据ID获取角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "#roleId")
    public Role getRoleById(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        Role role = getById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("角色不存在，ID: " + roleId);
        }
        
        return role;
    }

    /**
     * 更新角色
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "roleCache", key = "#role.id"),
        @CacheEvict(value = "roleCache", allEntries = true)
    })
    public Role updateRole(Role role) {
        log.info("更新角色: {}", role.getId());
        
        // 参数验证
        validateRoleForUpdate(role);
        
        // 检查角色是否存在
        Role existingRole = getRoleById(role.getId());
        
        // 检查角色编码唯一性（排除自身）
        if (!existingRole.getRoleCode().equals(role.getRoleCode()) && 
            existsByRoleCode(role.getRoleCode())) {
            throw new DuplicateResourceException("角色编码已存在: " + role.getRoleCode());
        }
        
        // 检查角色名称唯一性（排除自身）
        if (!existingRole.getRoleName().equals(role.getRoleName()) && 
            existsByRoleName(role.getRoleName())) {
            throw new DuplicateResourceException("角色名称已存在: " + role.getRoleName());
        }
        
        // 系统角色保护
        if (existingRole.getIsSystem() == 1 && !SecurityUtils.isSuperAdmin()) {
            throw new BusinessException("系统角色不允许修改");
        }
        
        // 更新角色
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(SecurityUtils.getCurrentUserId());
        
        if (!updateById(role)) {
            throw new BusinessException("更新角色失败");
        }
        
        log.info("角色更新成功，ID: {}", role.getId());
        return role;
    }

    /**
     * 删除角色（软删除）
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "roleCache", key = "#roleId"),
        @CacheEvict(value = "roleCache", allEntries = true)
    })
    public boolean deleteRole(Long roleId) {
        log.info("删除角色: {}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        // 系统角色保护
        if (role.getIsSystem() == 1) {
            throw new BusinessException("系统角色不允许删除");
        }
        
        // 检查是否可以删除
        if (!canDeleteRole(roleId)) {
            throw new BusinessException("角色正在使用中，无法删除");
        }
        
        // 执行软删除
        boolean result = removeById(roleId);
        
        if (result) {
            log.info("角色删除成功，ID: {}", roleId);
        } else {
            throw new BusinessException("删除角色失败");
        }
        
        return result;
    }

    /**
     * 批量删除角色
     */
    @Override
    @CacheEvict(value = "roleCache", allEntries = true)
    public boolean batchDeleteRoles(List<Long> roleIds) {
        log.info("批量删除角色: {}", roleIds);
        
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new IllegalArgumentException("角色ID列表不能为空");
        }
        
        // 检查每个角色是否可以删除
        for (Long roleId : roleIds) {
            Role role = getRoleById(roleId);
            if (role.getIsSystem() == 1) {
                throw new BusinessException("系统角色不允许删除: " + role.getRoleName());
            }
            if (!canDeleteRole(roleId)) {
                throw new BusinessException("角色正在使用中，无法删除: " + role.getRoleName());
            }
        }
        
        // 执行批量删除
        boolean result = removeByIds(roleIds);
        
        if (result) {
            log.info("批量删除角色成功，数量: {}", roleIds.size());
        } else {
            throw new BusinessException("批量删除角色失败");
        }
        
        return result;
    }

    // ==================== 角色权限管理 ====================

    /**
     * 分配权限给角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "roleCache", key = "#roleId"),
        @CacheEvict(value = "rolePermissionCache", key = "#roleId"),
        @CacheEvict(value = "roleCache", allEntries = true)
    })
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        log.info("为角色分配权限: roleId={}, permissionIds={}", roleId, permissionIds);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        if (CollectionUtils.isEmpty(permissionIds)) {
            throw new IllegalArgumentException("权限ID列表不能为空");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        // 检查权限是否存在
        List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new BusinessException("部分权限不存在");
        }
        
        // 删除现有权限关联
        roleMapper.deleteRolePermissions(roleId);
        
        // 批量插入新的权限关联
        boolean result = roleMapper.batchInsertRolePermissions(roleId, permissionIds, 
                SecurityUtils.getCurrentUserId(), LocalDateTime.now());
        
        if (result) {
            log.info("角色权限分配成功: roleId={}, 权限数量={}", roleId, permissionIds.size());
        } else {
            throw new BusinessException("角色权限分配失败");
        }
        
        return result;
    }

    /**
     * 移除角色权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "roleCache", key = "#roleId"),
        @CacheEvict(value = "rolePermissionCache", key = "#roleId"),
        @CacheEvict(value = "roleCache", allEntries = true)
    })
    public boolean removePermissions(Long roleId, List<Long> permissionIds) {
        log.info("移除角色权限: roleId={}, permissionIds={}", roleId, permissionIds);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        if (CollectionUtils.isEmpty(permissionIds)) {
            throw new IllegalArgumentException("权限ID列表不能为空");
        }
        
        // 检查角色是否存在
        getRoleById(roleId);
        
        // 删除指定权限关联
        boolean result = roleMapper.deleteRolePermissionsByIds(roleId, permissionIds);
        
        if (result) {
            log.info("角色权限移除成功: roleId={}, 权限数量={}", roleId, permissionIds.size());
        } else {
            throw new BusinessException("角色权限移除失败");
        }
        
        return result;
    }

    /**
     * 获取角色权限列表
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "#roleId")
    public List<String> getRolePermissions(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        getRoleById(roleId);
        
        List<Permission> permissions = permissionMapper.findByRoleId(roleId);
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取角色权限ID列表
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'ids:' + #roleId")
    public List<Long> getRolePermissionIds(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        getRoleById(roleId);
        
        return roleMapper.getRolePermissionIds(roleId);
    }

    /**
     * 获取角色权限编码列表
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'codes:' + #roleId")
    public List<String> getRolePermissionCodes(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        getRoleById(roleId);
        
        return roleMapper.getRolePermissionCodes(roleId);
    }

    /**
     * 检查角色是否拥有权限
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'check:' + #roleId + ':' + #permissionId")
    public boolean checkRolePermission(Long roleId, Long permissionId) {
        if (roleId == null || permissionId == null) {
            return false;
        }
        
        return roleMapper.checkRolePermission(roleId, permissionId);
    }

    /**
     * 检查角色是否拥有权限（通过权限编码）
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'checkCode:' + #roleId + ':' + #permissionCode")
    public boolean checkRolePermissionByCode(Long roleId, String permissionCode) {
        if (roleId == null || !StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        return roleMapper.checkRolePermissionByCode(roleId, permissionCode);
    }

    /**
     * 根据权限ID查找拥有该权限的角色
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'rolesByPermission:' + #permissionId")
    public List<Role> getRolesByPermission(Long permissionId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        
        return roleMapper.findByPermissionId(permissionId);
    }

    /**
     * 根据权限编码查找拥有该权限的角色
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'rolesByPermissionCode:' + #permissionCode")
    public List<Role> getRolesByPermissionCode(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            throw new IllegalArgumentException("权限编码不能为空");
        }
        
        return roleMapper.findByPermissionCode(permissionCode);
    }

    /**
     * 获取角色权限树
     */
    @Override
    @Cacheable(value = "rolePermissionCache", key = "'tree:' + #roleId")
    public List<Map<String, Object>> getRolePermissionTree(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 获取角色权限列表
        List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        
        if (CollectionUtils.isEmpty(permissions)) {
            return new ArrayList<>();
        }
        
        // 构建权限树并转换为Map格式
        List<Permission> permissionTree = buildPermissionTree(permissions);
        return convertPermissionsToMapTree(permissionTree);
    }

    /**
     * 将权限树转换为Map格式
     */
    private List<Map<String, Object>> convertPermissionsToMapTree(List<Permission> permissions) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Permission permission : permissions) {
            Map<String, Object> permissionMap = new HashMap<>();
            permissionMap.put("id", permission.getId());
            permissionMap.put("name", permission.getPermissionName());
            permissionMap.put("code", permission.getPermissionCode());
            permissionMap.put("type", permission.getPermissionType());
            permissionMap.put("parentId", permission.getParentId());
            permissionMap.put("sortOrder", permission.getSortOrder());
            permissionMap.put("status", permission.getStatus());
            
            // 递归处理子权限
            if (!CollectionUtils.isEmpty(permission.getChildren())) {
                permissionMap.put("children", convertPermissionsToMapTree(permission.getChildren()));
            }
            
            result.add(permissionMap);
        }
        return result;
    }

    /**
     * 构建权限树结构
     */
    private List<Permission> buildPermissionTree(List<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return new ArrayList<>();
        }
        
        // 创建权限映射
        Map<Long, Permission> permissionMap = permissions.stream()
                .collect(Collectors.toMap(Permission::getId, permission -> permission));
        
        // 构建树结构
        List<Permission> rootPermissions = new ArrayList<>();
        
        for (Permission permission : permissions) {
            if (permission.getParentId() == null || permission.getParentId() == 0) {
                // 根节点
                rootPermissions.add(permission);
            } else {
                // 子节点
                Permission parent = permissionMap.get(permission.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permission);
                }
            }
        }
        
        // 排序
        sortPermissionTree(rootPermissions);
        
        return rootPermissions;
    }

    /**
     * 权限树排序
     */
    private void sortPermissionTree(List<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return;
        }
        
        // 按排序号和创建时间排序
        permissions.sort((p1, p2) -> {
            int sortCompare = Integer.compare(
                    p1.getSortOrder() != null ? p1.getSortOrder() : 0,
                    p2.getSortOrder() != null ? p2.getSortOrder() : 0
            );
            if (sortCompare != 0) {
                return sortCompare;
            }
            return p1.getCreateTime().compareTo(p2.getCreateTime());
        });
        
        // 递归排序子节点
        for (Permission permission : permissions) {
            if (CollectionUtils.isNotEmpty(permission.getChildren())) {
                sortPermissionTree(permission.getChildren());
            }
        }
    }

    // ==================== 角色用户管理方法 ====================

    /**
     * 获取角色下的用户列表
     */
    @Override
    @Cacheable(value = "roleUserCache", key = "#roleId")
    public List<User> getRoleUsers(Long roleId) {
        log.info("获取角色用户列表: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        return roleMapper.findUsersByRoleId(roleId);
    }

    /**
     * 获取角色用户数量
     */
    @Override
    @Cacheable(value = "roleUserCountCache", key = "#roleId")
    public Long getRoleUserCount(Long roleId) {
        log.info("获取角色用户数量: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        return roleMapper.countUsersByRoleId(roleId);
    }

    /**
     * 获取角色用户ID列表
     */
    @Override
    @Cacheable(value = "roleUserIdsCache", key = "#roleId")
    public List<Long> getRoleUserIds(Long roleId) {
        log.info("获取角色用户ID列表: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        return roleMapper.findUserIdsByRoleId(roleId);
    }

    /**
     * 获取用户的角色列表
     */
    @Override
    @Cacheable(value = "userRoleCache", key = "#userId")
    public List<Role> getUserRoles(Long userId) {
        log.info("获取用户角色列表: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        return roleMapper.findRolesByUserId(userId);
    }

    /**
     * 获取用户的角色ID列表
     */
    @Override
    @Cacheable(value = "userRoleIdsCache", key = "#userId")
    public List<Long> getUserRoleIds(Long userId) {
        log.info("获取用户角色ID列表: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        return roleMapper.findRoleIdsByUserId(userId);
    }

    /**
     * 获取用户的角色编码列表
     */
    @Override
    @Cacheable(value = "userRoleCodesCache", key = "#userId")
    public List<String> getUserRoleCodes(Long userId) {
        log.info("获取用户角色编码列表: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        return roleMapper.findRoleCodesByUserId(userId);
    }

    /**
     * 检查用户是否拥有指定角色
     */
    @Override
    @Cacheable(value = "userHasRoleCache", key = "#userId + ':' + #roleId")
    public boolean checkUserHasRole(Long userId, Long roleId) {
        log.info("检查用户是否拥有角色: userId={}, roleId={}", userId, roleId);
        
        if (userId == null || roleId == null) {
            return false;
        }
        
        return roleMapper.checkUserHasRole(userId, roleId);
    }

    /**
     * 检查用户是否拥有指定角色编码
     */
    @Override
    @Cacheable(value = "userHasRoleCodeCache", key = "#userId + ':' + #roleCode")
    public boolean checkUserHasRoleByCode(Long userId, String roleCode) {
        log.info("检查用户是否拥有角色编码: userId={}, roleCode={}", userId, roleCode);
        
        if (userId == null || !StringUtils.hasText(roleCode)) {
            return false;
        }
        
        return roleMapper.checkUserHasRoleByCode(userId, roleCode);
    }

    /**
     * 为用户分配角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "userRoleCache", key = "#userId"),
        @CacheEvict(value = "userRoleIdsCache", key = "#userId"),
        @CacheEvict(value = "userRoleCodesCache", key = "#userId"),
        @CacheEvict(value = "roleUserCache", allEntries = true),
        @CacheEvict(value = "roleUserCountCache", allEntries = true),
        @CacheEvict(value = "userHasRoleCache", allEntries = true),
        @CacheEvict(value = "userHasRoleCodeCache", allEntries = true)
    })
    public boolean assignUserRoles(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色: userId={}, roleIds={}", userId, roleIds);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new IllegalArgumentException("角色ID列表不能为空");
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在: " + userId);
        }
        
        // 检查角色是否存在
        List<Role> roles = roleMapper.selectBatchIds(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new BusinessException("部分角色不存在");
        }
        
        // 删除现有角色关联
        roleMapper.deleteUserRoles(userId);
        
        // 批量插入新的角色关联
        boolean result = roleMapper.batchInsertUserRoles(userId, roleIds, 
                SecurityUtils.getCurrentUserId(), LocalDateTime.now());
        
        if (result) {
            log.info("用户角色分配成功: userId={}, 角色数量={}", userId, roleIds.size());
        } else {
            throw new BusinessException("用户角色分配失败");
        }
        
        return result;
    }

    /**
     * 移除用户角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "userRoleCache", key = "#userId"),
        @CacheEvict(value = "userRoleIdsCache", key = "#userId"),
        @CacheEvict(value = "userRoleCodesCache", key = "#userId"),
        @CacheEvict(value = "roleUserCache", allEntries = true),
        @CacheEvict(value = "roleUserCountCache", allEntries = true),
        @CacheEvict(value = "userHasRoleCache", allEntries = true),
        @CacheEvict(value = "userHasRoleCodeCache", allEntries = true)
    })
    public boolean removeUserRoles(Long userId, List<Long> roleIds) {
        log.info("移除用户角色: userId={}, roleIds={}", userId, roleIds);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (CollectionUtils.isEmpty(roleIds)) {
            throw new IllegalArgumentException("角色ID列表不能为空");
        }
        
        boolean result = roleMapper.deleteUserRolesByIds(userId, roleIds);
        
        if (result) {
            log.info("用户角色移除成功: userId={}, 角色数量={}", userId, roleIds.size());
        } else {
            throw new BusinessException("用户角色移除失败");
        }
        
        return result;
    }

    /**
     * 清空用户所有角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "userRoleCache", key = "#userId"),
        @CacheEvict(value = "userRoleIdsCache", key = "#userId"),
        @CacheEvict(value = "userRoleCodesCache", key = "#userId"),
        @CacheEvict(value = "roleUserCache", allEntries = true),
        @CacheEvict(value = "roleUserCountCache", allEntries = true),
        @CacheEvict(value = "userHasRoleCache", allEntries = true),
        @CacheEvict(value = "userHasRoleCodeCache", allEntries = true)
    })
    public boolean clearUserRoles(Long userId) {
        log.info("清空用户所有角色: userId={}", userId);
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        boolean result = roleMapper.deleteUserRoles(userId);
        
        if (result) {
            log.info("用户角色清空成功: userId={}", userId);
        } else {
            throw new BusinessException("用户角色清空失败");
        }
        
        return result;
    }

    // ==================== 角色层级管理方法 ====================

    /**
     * 验证角色层级关系
     */
    @Override
    public boolean validateRoleHierarchy(Long parentRoleId, Long childRoleId) {
        log.info("验证角色层级关系: parentRoleId={}, childRoleId={}", parentRoleId, childRoleId);
        
        if (parentRoleId == null || childRoleId == null) {
            throw new IllegalArgumentException("父角色ID和子角色ID不能为空");
        }
        
        if (parentRoleId.equals(childRoleId)) {
            return false; // 不能自己作为自己的父角色
        }
        
        // 检查是否会形成循环引用
        return !isCircularReference(parentRoleId, childRoleId);
    }

    /**
     * 获取角色树
     */
    @Override
    @Cacheable(value = "roleTreeCache", key = "'all'")
    public List<Role> getRoleTree() {
        log.info("获取角色树");
        
        List<Role> allRoles = roleMapper.selectList(null);
        return buildRoleTree(allRoles);
    }

    /**
     * 获取指定角色的子角色
     */
    @Override
    @Cacheable(value = "childRolesCache", key = "#roleId")
    public List<Role> getChildRoles(Long roleId) {
        log.info("获取子角色: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        return roleMapper.findChildRoles(roleId);
    }

    /**
     * 获取指定角色的父角色
     */
    @Override
    @Cacheable(value = "parentRolesCache", key = "#roleId")
    public List<Role> getParentRoles(Long roleId) {
        log.info("获取父角色: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        return roleMapper.findParentRoles(roleId);
    }

    /**
     * 设置角色的父角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "roleTreeCache", allEntries = true),
        @CacheEvict(value = "childRolesCache", allEntries = true),
        @CacheEvict(value = "parentRolesCache", allEntries = true)
    })
    public boolean setParentRole(Long roleId, Long parentRoleId) {
        log.info("设置角色父角色: roleId={}, parentRoleId={}", roleId, parentRoleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 验证层级关系
        if (parentRoleId != null && !validateRoleHierarchy(parentRoleId, roleId)) {
            throw new BusinessException("无效的角色层级关系");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        if (parentRoleId != null) {
            Role parentRole = getRoleById(parentRoleId);
        }
        
        // 更新角色的父角色
        role.setParentId(parentRoleId);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(SecurityUtils.getCurrentUserId());
        
        boolean result = roleMapper.updateById(role) > 0;
        
        if (result) {
            log.info("角色父角色设置成功: roleId={}, parentRoleId={}", roleId, parentRoleId);
        } else {
            throw new BusinessException("角色父角色设置失败");
        }
        
        return result;
    }

    /**
     * 移除角色的父角色关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
        @CacheEvict(value = "roleTreeCache", allEntries = true),
        @CacheEvict(value = "childRolesCache", allEntries = true),
        @CacheEvict(value = "parentRolesCache", allEntries = true)
    })
    public boolean removeParentRole(Long roleId) {
        log.info("移除角色父角色关系: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        // 移除父角色关系
        role.setParentId(null);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(SecurityUtils.getCurrentUserId());
        
        boolean result = roleMapper.updateById(role) > 0;
        
        if (result) {
            log.info("角色父角色关系移除成功: roleId={}", roleId);
        } else {
            throw new BusinessException("角色父角色关系移除失败");
        }
        
        return result;
    }

    /**
     * 获取角色的所有子孙角色
     */
    @Override
    @Cacheable(value = "descendantRolesCache", key = "#roleId")
    public List<Role> getDescendantRoles(Long roleId) {
        log.info("获取角色的所有子孙角色: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        List<Role> descendants = new ArrayList<>();
        collectDescendantRoles(roleId, descendants);
        return descendants;
    }

    /**
     * 获取角色的所有祖先角色
     */
    @Override
    @Cacheable(value = "ancestorRolesCache", key = "#roleId")
    public List<Role> getAncestorRoles(Long roleId) {
        log.info("获取角色的所有祖先角色: roleId={}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        List<Role> ancestors = new ArrayList<>();
        collectAncestorRoles(roleId, ancestors);
        return ancestors;
    }

    /**
     * 检查是否存在循环引用
     */
    private boolean isCircularReference(Long parentRoleId, Long childRoleId) {
        List<Role> ancestors = getAncestorRoles(parentRoleId);
        return ancestors.stream().anyMatch(role -> role.getId().equals(childRoleId));
    }

    /**
     * 构建角色树
     */
    private List<Role> buildRoleTree(List<Role> roles) {
        Map<Long, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));
        
        List<Role> rootRoles = new ArrayList<>();
        for (Role role : roles) {
            if (role.getParentId() == null || role.getParentId() == 0) {
                rootRoles.add(role);
            } else {
                Role parent = roleMap.get(role.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(role);
                }
            }
        }
        
        return sortRoleTree(rootRoles);
    }

    /**
     * 排序角色树
     */
    private List<Role> sortRoleTree(List<Role> roles) {
        roles.sort(Comparator.comparing(Role::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        for (Role role : roles) {
            if (role.getChildren() != null && !role.getChildren().isEmpty()) {
                role.setChildren(sortRoleTree(role.getChildren()));
            }
        }
        return roles;
    }

    /**
     * 递归收集子孙角色
     */
    private void collectDescendantRoles(Long roleId, List<Role> descendants) {
        List<Role> children = getChildRoles(roleId);
        for (Role child : children) {
            descendants.add(child);
            collectDescendantRoles(child.getId(), descendants);
        }
    }

    /**
     * 递归收集祖先角色
     */
    private void collectAncestorRoles(Long roleId, List<Role> ancestors) {
        List<Role> parents = getParentRoles(roleId);
        for (Role parent : parents) {
            ancestors.add(parent);
            collectAncestorRoles(parent.getId(), ancestors);
        }
    }

    // ==================== 角色统计功能 ====================

    /**
     * 统计角色总数
     */
    @Override
    @Cacheable(value = "roleCountCache", key = "'total'")
    public long countRoles() {
        return roleMapper.selectCount(null);
    }

    /**
     * 根据状态统计角色数量
     */
    @Override
    @Cacheable(value = "roleStatusStatsCache", key = "'all'")
    public List<Map<String, Object>> getRoleStatusStatistics() {
        log.info("获取角色状态统计");
        
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        Map<String, Object> activeStat = new HashMap<>();
        activeStat.put("status", "ACTIVE");
        activeStat.put("count", roleMapper.countByStatus(RoleStatus.ACTIVE));
        statistics.add(activeStat);
        
        Map<String, Object> inactiveStat = new HashMap<>();
        inactiveStat.put("status", "INACTIVE");
        inactiveStat.put("count", roleMapper.countByStatus(RoleStatus.INACTIVE));
        statistics.add(inactiveStat);
        
        Map<String, Object> lockedStat = new HashMap<>();
        lockedStat.put("status", "LOCKED");
        lockedStat.put("count", roleMapper.countByStatus(RoleStatus.LOCKED));
        statistics.add(lockedStat);
        
        return statistics;
    }

    /**
     * 根据类型统计角色数量
     */
    @Override
    @Cacheable(value = "roleTypeStatsCache", key = "'all'")
    public List<Map<String, Object>> getRoleTypeStatistics() {
        log.info("获取角色类型统计");
        
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        Map<String, Object> systemStat = new HashMap<>();
        systemStat.put("type", "SYSTEM");
        systemStat.put("count", roleMapper.countByType(RoleType.SYSTEM));
        statistics.add(systemStat);
        
        Map<String, Object> businessStat = new HashMap<>();
        businessStat.put("type", "BUSINESS");
        businessStat.put("count", roleMapper.countByType(RoleType.BUSINESS));
        statistics.add(businessStat);
        
        Map<String, Object> customStat = new HashMap<>();
        customStat.put("type", "CUSTOM");
        customStat.put("count", roleMapper.countByType(RoleType.CUSTOM));
        statistics.add(customStat);
        
        return statistics;
    }

    /**
     * 获取角色使用统计
     */
    @Override
    @Cacheable(value = "roleUsageStatsCache", key = "'all'")
    public Map<String, Object> getRoleUsageStatistics() {
        log.info("获取角色使用统计");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 基础统计
        statistics.put("totalRoles", countRoles());
        statistics.put("activeRoles", roleMapper.countByStatus(RoleStatus.ACTIVE));
        statistics.put("totalUsers", userMapper.selectCount(null));
        
        // 角色分配统计
        List<Map<String, Object>> roleUserStats = roleMapper.getRoleUserStatistics();
        statistics.put("roleUserStats", roleUserStats);
        
        // 权限分配统计
        List<Map<String, Object>> rolePermissionStats = roleMapper.getRolePermissionStatistics();
        statistics.put("rolePermissionStats", rolePermissionStats);
        
        // 最近创建的角色
        List<Role> recentRoles = roleMapper.findRecentCreatedRoles(10);
        statistics.put("recentRoles", recentRoles);
        
        // 使用频率最高的角色
        List<Map<String, Object>> topUsedRoles = roleMapper.getTopUsedRoles(10);
        statistics.put("topUsedRoles", topUsedRoles);
        
        return statistics;
    }

    /**
     * 获取角色层级统计
     */
    @Override
    @Cacheable(value = "roleHierarchyStatsCache", key = "'all'")
    public Map<String, Object> getRoleHierarchyStatistics() {
        log.info("获取角色层级统计");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 根角色数量
        Long rootRoleCount = roleMapper.countRootRoles();
        statistics.put("rootRoleCount", rootRoleCount);
        
        // 叶子角色数量
        Long leafRoleCount = roleMapper.countLeafRoles();
        statistics.put("leafRoleCount", leafRoleCount);
        
        // 最大层级深度
        Integer maxDepth = roleMapper.getMaxHierarchyDepth();
        statistics.put("maxDepth", maxDepth);
        
        // 平均子角色数量
        Double avgChildCount = roleMapper.getAverageChildRoleCount();
        statistics.put("avgChildCount", avgChildCount);
        
        // 层级分布
        List<Map<String, Object>> levelDistribution = roleMapper.getHierarchyLevelDistribution();
        statistics.put("levelDistribution", levelDistribution);
        
        return statistics;
    }

    /**
     * 获取角色权限统计
     */
    @Override
    @Cacheable(value = "rolePermissionStatsCache", key = "'all'")
    public Map<String, Object> getRolePermissionStatistics() {
        log.info("获取角色权限统计");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 总权限数量
        Long totalPermissions = permissionMapper.selectCount(null);
        statistics.put("totalPermissions", totalPermissions);
        
        // 已分配权限数量
        Long assignedPermissions = roleMapper.countAssignedPermissions();
        statistics.put("assignedPermissions", assignedPermissions);
        
        // 未分配权限数量
        statistics.put("unassignedPermissions", totalPermissions - assignedPermissions);
        
        // 权限分配率
        Double assignmentRate = totalPermissions > 0 ? 
                (double) assignedPermissions / totalPermissions * 100 : 0.0;
        statistics.put("assignmentRate", assignmentRate);
        
        // 角色权限分布
        List<Map<String, Object>> rolePermissionDistribution = roleMapper.getRolePermissionDistribution();
        statistics.put("rolePermissionDistribution", rolePermissionDistribution);
        
        // 权限使用频率
        List<Map<String, Object>> permissionUsageFrequency = roleMapper.getPermissionUsageFrequency();
        statistics.put("permissionUsageFrequency", permissionUsageFrequency);
        
        return statistics;
    }

    /**
     * 生成角色报告
     */
    @Override
    public Map<String, Object> generateRoleReport() {
        log.info("生成角色报告");
        
        Map<String, Object> report = new HashMap<>();
        
        // 基础统计
        report.put("basicStats", getRoleStatusStatistics());
        report.put("typeStats", getRoleTypeStatistics());
        report.put("usageStats", getRoleUsageStatistics());
        report.put("hierarchyStats", getRoleHierarchyStatistics());
        report.put("permissionStats", getRolePermissionStatistics());
        
        // 报告生成时间
        report.put("generatedAt", LocalDateTime.now());
        report.put("generatedBy", SecurityUtils.getCurrentUserId());
        
        return report;
    }

    // ==================== 高级功能 ====================

    /**
     * 复制角色
     */
    @Override
    @Transactional
    @CacheEvict(value = {"roleCache", "roleListCache", "roleCountCache"}, allEntries = true)
    public Role copyRole(Long sourceRoleId, String newRoleCode, String newRoleName) {
        log.info("复制角色: sourceRoleId={}, newRoleCode={}, newRoleName={}", 
                sourceRoleId, newRoleCode, newRoleName);
        
        // 获取源角色
        Role sourceRole = getRoleById(sourceRoleId);
        if (sourceRole == null) {
            throw new BusinessException("源角色不存在");
        }
        
        // 检查新角色编码是否已存在
        if (existsByRoleCode(newRoleCode)) {
            throw new BusinessException("角色编码已存在: " + newRoleCode);
        }
        
        // 检查新角色名称是否已存在
        if (existsByRoleName(newRoleName)) {
            throw new BusinessException("角色名称已存在: " + newRoleName);
        }
        
        // 创建新角色
        Role newRole = new Role();
        BeanUtils.copyProperties(sourceRole, newRole);
        newRole.setId(null);
        newRole.setRoleCode(newRoleCode);
        newRole.setRoleName(newRoleName);
        newRole.setCreatedAt(LocalDateTime.now());
        newRole.setUpdatedAt(LocalDateTime.now());
        newRole.setCreatedBy(SecurityUtils.getCurrentUserId());
        newRole.setUpdatedBy(SecurityUtils.getCurrentUserId());
        
        // 保存新角色
        roleMapper.insert(newRole);
        
        // 复制权限关联
        List<Long> sourcePermissionIds = getRolePermissionIds(sourceRoleId);
        if (!sourcePermissionIds.isEmpty()) {
            assignPermissions(newRole.getId(), sourcePermissionIds);
        }
        
        // 复制层级关系（如果源角色有父角色）
        List<Role> parentRoles = getParentRoles(sourceRoleId);
        for (Role parentRole : parentRoles) {
            setParentRole(newRole.getId(), parentRole.getId());
        }
        
        log.info("角色复制成功: newRoleId={}", newRole.getId());
        return newRole;
    }

    /**
     * 导入角色
     */
    @Override
    @Transactional
    @CacheEvict(value = {"roleCache", "roleListCache", "roleCountCache"}, allEntries = true)
    public List<Role> importRoles(List<Role> roles) {
        log.info("导入角色: count={}", roles.size());
        
        List<Role> importedRoles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Role role : roles) {
            try {
                // 验证角色数据
                validateRoleForCreate(role);
                
                // 检查编码是否已存在
                if (existsByRoleCode(role.getRoleCode())) {
                    errors.add("角色编码已存在: " + role.getRoleCode());
                    continue;
                }
                
                // 检查名称是否已存在
                if (existsByRoleName(role.getRoleName())) {
                    errors.add("角色名称已存在: " + role.getRoleName());
                    continue;
                }
                
                // 设置默认值
                setDefaultValues(role);
                
                // 保存角色
                roleMapper.insert(role);
                importedRoles.add(role);
                
            } catch (Exception e) {
                errors.add("导入角色失败: " + role.getRoleCode() + " - " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            log.warn("角色导入部分失败: errors={}", errors);
            throw new BusinessException("部分角色导入失败: " + String.join(", ", errors));
        }
        
        log.info("角色导入成功: count={}", importedRoles.size());
        return importedRoles;
    }

    /**
     * 导出角色
     */
    @Override
    public List<Role> exportRoles(List<Long> roleIds) {
        log.info("导出角色: roleIds={}", roleIds);
        
        if (roleIds == null || roleIds.isEmpty()) {
            // 导出所有角色
            return roleMapper.selectList(null);
        } else {
            // 导出指定角色
            return roleMapper.selectBatchIds(roleIds);
        }
    }

    /**
     * 同步权限
     */
    @Override
    @Transactional
    @CacheEvict(value = {"rolePermissionCache", "userRoleCache"}, allEntries = true)
    public void syncPermissions(Long roleId, List<Long> permissionIds) {
        log.info("同步角色权限: roleId={}, permissionIds={}", roleId, permissionIds);
        
        // 获取当前权限
        List<Long> currentPermissionIds = getRolePermissionIds(roleId);
        
        // 计算需要添加的权限
        List<Long> toAdd = new ArrayList<>(permissionIds);
        toAdd.removeAll(currentPermissionIds);
        
        // 计算需要移除的权限
        List<Long> toRemove = new ArrayList<>(currentPermissionIds);
        toRemove.removeAll(permissionIds);
        
        // 添加新权限
        if (!toAdd.isEmpty()) {
            List<RolePermission> rolePermissions = toAdd.stream()
                    .map(permissionId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permissionId);
                        rp.setCreatedAt(LocalDateTime.now());
                        rp.setCreatedBy(SecurityUtils.getCurrentUserId());
                        return rp;
                    })
                    .collect(Collectors.toList());
            
            rolePermissionMapper.insertBatch(rolePermissions);
        }
        
        // 移除旧权限
        if (!toRemove.isEmpty()) {
            rolePermissionMapper.deleteByRoleIdAndPermissionIds(roleId, toRemove);
        }
        
        log.info("权限同步完成: added={}, removed={}", toAdd.size(), toRemove.size());
    }

    /**
     * 清理无效关联
     */
    @Override
    @Transactional
    @CacheEvict(value = {"rolePermissionCache", "userRoleCache", "roleHierarchyCache"}, allEntries = true)
    public void cleanInvalidAssociations() {
        log.info("清理无效关联");
        
        // 清理无效的角色权限关联
        int deletedRolePermissions = rolePermissionMapper.deleteInvalidAssociations();
        log.info("清理无效角色权限关联: count={}", deletedRolePermissions);
        
        // 清理无效的用户角色关联
        int deletedUserRoles = userRoleMapper.deleteInvalidAssociations();
        log.info("清理无效用户角色关联: count={}", deletedUserRoles);
        
        // 清理无效的角色层级关联
        int deletedRoleHierarchy = roleHierarchyMapper.deleteInvalidAssociations();
        log.info("清理无效角色层级关联: count={}", deletedRoleHierarchy);
        
        log.info("无效关联清理完成");
    }

    /**
     * 验证角色创建参数
     */
    private void validateRoleForCreate(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("角色信息不能为空");
        }
        if (!StringUtils.hasText(role.getRoleName())) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        if (!StringUtils.hasText(role.getRoleCode())) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        if (role.getRoleType() == null) {
            throw new IllegalArgumentException("角色类型不能为空");
        }
        if (role.getRoleLevel() == null) {
            throw new IllegalArgumentException("角色级别不能为空");
        }
    }

    /**
     * 验证角色更新参数
     */
    private void validateRoleForUpdate(Role role) {
        if (role == null || role.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        validateRoleForCreate(role);
    }

    /**
     * 设置角色默认值
     */
    private void setDefaultValues(Role role) {
        if (role.getStatus() == null) {
            role.setStatus(1); // 默认启用
        }
        if (role.getIsSystem() == null) {
            role.setIsSystem(0); // 默认非系统角色
        }
        if (role.getDataScope() == null) {
            role.setDataScope(4); // 默认仅本人数据
        }
        if (role.getSortOrder() == null) {
            role.setSortOrder(roleMapper.getNextSortOrder());
        }
        
        role.setCreateTime(LocalDateTime.now());
        role.setCreateBy(SecurityUtils.getCurrentUserId());
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy(SecurityUtils.getCurrentUserId());
        role.setDeleted(0);
        role.setVersion(1);
    }

    /**
     * 检查角色编码是否存在
     */
    public boolean existsByRoleCode(String roleCode) {
        return roleMapper.countByRoleCode(roleCode) > 0;
    }

    /**
     * 检查角色名称是否存在
     */
    public boolean existsByRoleName(String roleName) {
        return roleMapper.countByRoleName(roleName) > 0;
    }

    /**
     * 检查角色是否可以删除
     */
    @Override
    public boolean canDeleteRole(Long roleId) {
        // 检查是否有用户关联此角色
        long userCount = roleMapper.getRoleUserCount(roleId);
        return userCount == 0;
    }

    // ==================== 角色查询方法 ====================

    /**
     * 根据角色编码获取角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'code:' + #roleCode")
    public Role getRoleByCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        
        return roleMapper.findByRoleCode(roleCode);
    }

    /**
     * 根据角色名称获取角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'name:' + #roleName")
    public Role getRoleByName(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        
        return roleMapper.findByRoleName(roleName);
    }

    /**
     * 获取所有角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'all'")
    public List<Role> getAllRoles() {
        return list(new LambdaQueryWrapper<Role>()
                .orderByAsc(Role::getSortOrder)
                .orderByDesc(Role::getCreateTime));
    }

    /**
     * 获取启用的角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'enabled'")
    public List<Role> getEnabledRoles() {
        return roleMapper.findByStatus(1);
    }

    /**
     * 获取禁用的角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'disabled'")
    public List<Role> getDisabledRoles() {
        return roleMapper.findByStatus(0);
    }

    /**
     * 获取系统角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'system'")
    public List<Role> getSystemRoles() {
        return roleMapper.findSystemRoles();
    }

    /**
     * 获取业务角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'business'")
    public List<Role> getBusinessRoles() {
        return roleMapper.findBusinessRoles();
    }

    /**
     * 获取自定义角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'custom'")
    public List<Role> getCustomRoles() {
        return roleMapper.findCustomRoles();
    }

    /**
     * 根据角色类型获取角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'type:' + #roleType")
    public List<Role> getRolesByType(Integer roleType) {
        if (roleType == null) {
            throw new IllegalArgumentException("角色类型不能为空");
        }
        
        return roleMapper.findByRoleType(roleType);
    }

    /**
     * 根据角色级别获取角色
     */
    @Override
    @Cacheable(value = "roleCache", key = "'level:' + #roleLevel")
    public List<Role> getRolesByLevel(Integer roleLevel) {
        if (roleLevel == null) {
            throw new IllegalArgumentException("角色级别不能为空");
        }
        
        return roleMapper.findByRoleLevel(roleLevel);
    }

    /**
     * 分页查询角色
     */
    @Override
    public IPage<Role> getRolesWithPagination(Page<Role> page, Role queryRole) {
        if (page == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }
        
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (queryRole != null) {
            if (StringUtils.hasText(queryRole.getRoleName())) {
                queryWrapper.like(Role::getRoleName, queryRole.getRoleName());
            }
            if (StringUtils.hasText(queryRole.getRoleCode())) {
                queryWrapper.like(Role::getRoleCode, queryRole.getRoleCode());
            }
            if (queryRole.getStatus() != null) {
                queryWrapper.eq(Role::getStatus, queryRole.getStatus());
            }
            if (queryRole.getRoleType() != null) {
                queryWrapper.eq(Role::getRoleType, queryRole.getRoleType());
            }
            if (queryRole.getRoleLevel() != null) {
                queryWrapper.eq(Role::getRoleLevel, queryRole.getRoleLevel());
            }
            if (queryRole.getIsSystem() != null) {
                queryWrapper.eq(Role::getIsSystem, queryRole.getIsSystem());
            }
        }
        
        // 排序
        queryWrapper.orderByAsc(Role::getSortOrder)
                   .orderByDesc(Role::getCreateTime);
        
        return page(page, queryWrapper);
    }

    /**
     * 搜索角色
     */
    @Override
    public List<Role> searchRoles(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllRoles();
        }
        
        return roleMapper.searchRoles(keyword);
    }

    /**
     * 分页搜索角色
     */
    @Override
    public IPage<Role> searchRolesWithPagination(Page<Role> page, String keyword) {
        if (page == null) {
            throw new IllegalArgumentException("分页参数不能为空");
        }
        
        if (!StringUtils.hasText(keyword)) {
            return getRolesWithPagination(page, null);
        }
        
        return roleMapper.searchRolesWithPagination(page, keyword);
    }

    /**
     * 启用角色
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "roleCache", key = "#roleId"),
        @CacheEvict(value = "roleCache", allEntries = true)
    })
    public boolean enableRole(Long roleId) {
        log.info("启用角色: {}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        if (role.getStatus() == 1) {
            log.warn("角色已经是启用状态: {}", roleId);
            return true;
        }
        
        // 更新状态
        Role updateRole = new Role();
        updateRole.setId(roleId);
        updateRole.setStatus(1);
        updateRole.setUpdateTime(LocalDateTime.now());
        updateRole.setUpdateBy(SecurityUtils.getCurrentUserId());
        
        boolean result = updateById(updateRole);
        
        if (result) {
            log.info("角色启用成功: {}", roleId);
        } else {
            throw new BusinessException("启用角色失败");
        }
        
        return result;
    }

    /**
     * 禁用角色
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "roleCache", key = "#roleId"),
        @CacheEvict(value = "roleCache", allEntries = true)
    })
    public boolean disableRole(Long roleId) {
        log.info("禁用角色: {}", roleId);
        
        if (roleId == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        
        // 检查角色是否存在
        Role role = getRoleById(roleId);
        
        // 系统角色保护
        if (role.getIsSystem() == 1) {
            throw new BusinessException("系统角色不允许禁用");
        }
        
        if (role.getStatus() == 0) {
            log.warn("角色已经是禁用状态: {}", roleId);
            return true;
        }
        
        // 更新状态
        Role updateRole = new Role();
        updateRole.setId(roleId);
        updateRole.setStatus(0);
        updateRole.setUpdateTime(LocalDateTime.now());
        updateRole.setUpdateBy(SecurityUtils.getCurrentUserId());
        
        boolean result = updateById(updateRole);
        
        if (result) {
            log.info("角色禁用成功: {}", roleId);
        } else {
            throw new BusinessException("禁用角色失败");
        }
        
        return result;
    }

    /**
     * 批量更新角色状态
     */
    @Override
    @CacheEvict(value = "roleCache", allEntries = true)
    public boolean batchUpdateStatus(List<Long> roleIds, Integer status) {
        if (CollectionUtils.isEmpty(roleIds)) {
            log.warn("批量更新角色状态失败：角色ID列表为空");
            return false;
        }

        try {
            // 验证状态值
            if (status == null || (status != 0 && status != 1)) {
                log.warn("批量更新角色状态失败：状态值无效 - {}", status);
                throw new BusinessException("状态值无效");
            }

            // 批量更新状态
            int updatedCount = roleMapper.batchUpdateStatus(roleIds, status);
            
            if (updatedCount > 0) {
                log.info("批量更新角色状态成功：更新了 {} 个角色的状态为 {}", updatedCount, status);
                return true;
            } else {
                log.warn("批量更新角色状态失败：没有角色被更新");
                return false;
            }
        } catch (Exception e) {
            log.error("批量更新角色状态异常：roleIds={}, status={}", roleIds, status, e);
            throw new BusinessException("批量更新角色状态失败", e);
        }
    }

    @Override
    public List<Long> getRecommendedPermissions(String roleType) {
        if (!StringUtils.hasText(roleType)) {
            log.warn("获取推荐权限失败：角色类型为空");
            return new ArrayList<>();
        }

        try {
            // 根据角色类型获取推荐的权限ID列表
            List<Long> recommendedPermissions = new ArrayList<>();
            
            switch (roleType.toUpperCase()) {
                case "ADMIN":
                    // 管理员角色推荐所有权限
                    recommendedPermissions = permissionMapper.selectList(null)
                            .stream()
                            .map(Permission::getId)
                            .collect(Collectors.toList());
                    break;
                    
                case "USER":
                    // 普通用户角色推荐基础权限
                    recommendedPermissions = permissionMapper.selectList(
                            new LambdaQueryWrapper<Permission>()
                                    .like(Permission::getPermissionCode, "user:")
                                    .or()
                                    .like(Permission::getPermissionCode, "view:")
                    ).stream()
                            .map(Permission::getId)
                            .collect(Collectors.toList());
                    break;
                    
                case "MANAGER":
                    // 管理者角色推荐管理权限
                    recommendedPermissions = permissionMapper.selectList(
                            new LambdaQueryWrapper<Permission>()
                                    .like(Permission::getPermissionCode, "manage:")
                                    .or()
                                    .like(Permission::getPermissionCode, "user:")
                                    .or()
                                    .like(Permission::getPermissionCode, "view:")
                    ).stream()
                            .map(Permission::getId)
                            .collect(Collectors.toList());
                    break;
                    
                case "GUEST":
                    // 访客角色推荐只读权限
                    recommendedPermissions = permissionMapper.selectList(
                            new LambdaQueryWrapper<Permission>()
                                    .like(Permission::getPermissionCode, "view:")
                    ).stream()
                            .map(Permission::getId)
                            .collect(Collectors.toList());
                    break;
                    
                default:
                    // 未知角色类型，返回基础权限
                    recommendedPermissions = permissionMapper.selectList(
                            new LambdaQueryWrapper<Permission>()
                                    .like(Permission::getPermissionCode, "view:")
                    ).stream()
                            .map(Permission::getId)
                            .collect(Collectors.toList());
                    break;
            }
            
            log.info("获取角色类型 {} 的推荐权限成功：共 {} 个权限", roleType, recommendedPermissions.size());
            return recommendedPermissions;
            
        } catch (Exception e) {
            log.error("获取推荐权限异常：roleType={}", roleType, e);
            return new ArrayList<>();
        }
    }
}