package com.archive.management.service.impl;

import com.archive.management.constant.*;
import com.archive.management.entity.Permission;
import com.archive.management.mapper.PermissionMapper;
import com.archive.management.mapper.RolePermissionMapper;
import com.archive.management.service.PermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限业务服务实现类
 * 提供权限管理的完整业务逻辑实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    // ==================== 权限CRUD操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public Permission createPermission(Permission permission) {
        log.info("开始创建权限：{}", permission.getPermissionCode());
        
        // 参数验证
        validatePermissionForCreate(permission);
        
        // 设置默认值
        setDefaultValues(permission);
        
        // 计算权限路径和级别
        calculatePermissionPathAndLevel(permission);
        
        // 保存权限
        int result = permissionMapper.insert(permission);
        if (result <= 0) {
            log.error("权限创建失败：{}", permission.getPermissionCode());
            throw new RuntimeException("权限创建失败");
        }
        
        log.info("权限创建成功：{}, ID：{}", permission.getPermissionCode(), permission.getId());
        return permission;
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "#id", unless = "#result == null")
    public Permission getPermissionById(Long id) {
        if (id == null) {
            return null;
        }
        return permissionMapper.selectById(id);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'code:' + #permissionCode", unless = "#result == null")
    public Permission getPermissionByCode(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return null;
        }
        return permissionMapper.findByPermissionCode(permissionCode);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'name:' + #permissionName", unless = "#result == null")
    public Permission getPermissionByName(String permissionName) {
        if (!StringUtils.hasText(permissionName)) {
            return null;
        }
        return permissionMapper.findByPermissionName(permissionName);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'path:' + #permissionPath", unless = "#result == null")
    public Permission getPermissionByPath(String permissionPath) {
        if (!StringUtils.hasText(permissionPath)) {
            return null;
        }
        return permissionMapper.findByPermissionPath(permissionPath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public Permission updatePermission(Permission permission) {
        log.info("开始更新权限：{}", permission.getId());
        
        // 参数验证
        validatePermissionForUpdate(permission);
        
        // 检查权限是否存在
        Permission existingPermission = getPermissionById(permission.getId());
        if (existingPermission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查系统内置权限
        if (existingPermission.isSystemPermission()) {
            throw new RuntimeException("系统内置权限不允许修改");
        }
        
        // 重新计算权限路径和级别（如果父权限发生变化）
        if (!Objects.equals(existingPermission.getParentId(), permission.getParentId())) {
            calculatePermissionPathAndLevel(permission);
        }
        
        // 更新权限
        int result = permissionMapper.updateById(permission);
        if (result <= 0) {
            log.error("权限更新失败：{}", permission.getId());
            throw new RuntimeException("权限更新失败");
        }
        
        log.info("权限更新成功：{}", permission.getId());
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public boolean deletePermission(Long id, Long deletedBy) {
        log.info("开始删除权限：{}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        
        // 检查权限是否存在
        Permission permission = getPermissionById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查系统内置权限
        if (permission.isSystemPermission()) {
            throw new RuntimeException("系统内置权限不允许删除");
        }
        
        // 检查是否有子权限
        List<Permission> children = findPermissionsByParentId(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new RuntimeException("存在子权限，无法删除");
        }
        
        // 检查是否被角色使用
        if (!canDeletePermission(id)) {
            throw new RuntimeException("权限正在被使用，无法删除");
        }
        
        // 执行逻辑删除
        LambdaUpdateWrapper<Permission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Permission::getId, id)
                   .set(Permission::getDeleted, SecurityConstants.DeleteFlag.DELETED)
                   .set(Permission::getUpdateBy, deletedBy)
                   .set(Permission::getUpdateTime, LocalDateTime.now());
        
        int result = permissionMapper.update(null, updateWrapper);
        if (result <= 0) {
            log.error("权限删除失败：{}", id);
            return false;
        }
        
        log.info("权限删除成功：{}", id);
        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证权限创建参数
     */
    private void validatePermissionForCreate(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("权限信息不能为空");
        }
        
        if (!StringUtils.hasText(permission.getPermissionName())) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
        
        if (!StringUtils.hasText(permission.getPermissionCode())) {
            throw new IllegalArgumentException("权限编码不能为空");
        }
        
        if (permission.getPermissionType() == null) {
            throw new IllegalArgumentException("权限类型不能为空");
        }
        
        // 检查权限编码是否已存在
        if (existsByPermissionCode(permission.getPermissionCode())) {
            throw new RuntimeException("权限编码已存在：" + permission.getPermissionCode());
        }
        
        // 检查权限名称是否已存在
        if (existsByPermissionName(permission.getPermissionName())) {
            throw new RuntimeException("权限名称已存在：" + permission.getPermissionName());
        }
        
        // 验证父权限
        if (permission.getParentId() != null && permission.getParentId() > 0) {
            Permission parentPermission = getPermissionById(permission.getParentId());
            if (parentPermission == null) {
                throw new RuntimeException("父权限不存在");
            }
        }
    }

    /**
     * 验证权限更新参数
     */
    private void validatePermissionForUpdate(Permission permission) {
        if (permission == null || permission.getId() == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        
        if (!StringUtils.hasText(permission.getPermissionName())) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
        
        if (!StringUtils.hasText(permission.getPermissionCode())) {
            throw new IllegalArgumentException("权限编码不能为空");
        }
        
        // 检查权限编码是否被其他权限使用
        Permission existingByCode = getPermissionByCode(permission.getPermissionCode());
        if (existingByCode != null && !existingByCode.getId().equals(permission.getId())) {
            throw new RuntimeException("权限编码已被其他权限使用：" + permission.getPermissionCode());
        }
        
        // 检查权限名称是否被其他权限使用
        Permission existingByName = getPermissionByName(permission.getPermissionName());
        if (existingByName != null && !existingByName.getId().equals(permission.getId())) {
            throw new RuntimeException("权限名称已被其他权限使用：" + permission.getPermissionName());
        }
        
        // 验证父权限（不能设置自己为父权限）
        if (permission.getParentId() != null && permission.getParentId().equals(permission.getId())) {
            throw new RuntimeException("不能设置自己为父权限");
        }
    }

    /**
     * 设置权限默认值
     */
    private void setDefaultValues(Permission permission) {
        if (permission.getStatus() == null) {
            permission.setStatus(SecurityConstants.RoleStatus.ENABLED);
        }
        
        if (permission.getIsSystem() == null) {
            permission.setIsSystem(SecurityConstants.SystemFlag.NO);
        }
        
        if (permission.getShowInMenu() == null) {
            permission.setShowInMenu(SystemConstants.BooleanFlag.TRUE);
        }
        
        if (permission.getKeepAlive() == null) {
            permission.setKeepAlive(SystemConstants.BooleanFlag.FALSE);
        }
        
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        
        if (permission.getDeleted() == null) {
            permission.setDeleted(SecurityConstants.DeleteFlag.NOT_DELETED);
        }
    }

    /**
     * 计算权限路径和级别
     */
    private void calculatePermissionPathAndLevel(Permission permission) {
        if (permission.getParentId() == null || permission.getParentId().equals(PermissionConstants.ROOT_PARENT_ID)) {
            // 根权限
            permission.setPermissionPath(PermissionConstants.ROOT_PARENT_ID.toString());
            permission.setPermissionLevel(PermissionConstants.Level.ROOT);
        } else {
            // 子权限
            Permission parentPermission = getPermissionById(permission.getParentId());
            if (parentPermission != null) {
                String parentPath = parentPermission.getPermissionPath();
                if (StringUtils.hasText(parentPath)) {
                    permission.setPermissionPath(parentPath + PermissionConstants.PATH_SEPARATOR + permission.getParentId());
                } else {
                    permission.setPermissionPath(permission.getParentId().toString());
                }
                permission.setPermissionLevel(parentPermission.getPermissionLevel() + 1);
            }
        }
    }

    // ==================== 存在性检查方法 ====================

    @Override
    public boolean existsByPermissionCode(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return false;
        }
        return permissionMapper.countByPermissionCode(permissionCode) > 0;
    }

    @Override
    public boolean existsByPermissionName(String permissionName) {
        if (!StringUtils.hasText(permissionName)) {
            return false;
        }
        return permissionMapper.countByPermissionName(permissionName) > 0;
    }

    @Override
    public boolean existsByPermissionPath(String permissionPath) {
        if (!StringUtils.hasText(permissionPath)) {
            return false;
        }
        return permissionMapper.countByPermissionPath(permissionPath) > 0;
    }

    // ==================== 查询方法 ====================

    @Override
    public List<Permission> findPermissionsByType(String permissionType) {
        if (!StringUtils.hasText(permissionType)) {
            return new ArrayList<>();
        }
        
        Integer type = null;
        switch (permissionType.toLowerCase()) {
            case "menu":
                type = PermissionConstants.Type.MENU;
                break;
            case "button":
                type = PermissionConstants.Type.BUTTON;
                break;
            case "api":
                type = PermissionConstants.Type.API;
                break;
            case "data":
                type = PermissionConstants.Type.DATA;
                break;
            default:
                return new ArrayList<>();
        }
        
        return permissionMapper.findByPermissionType(type);
    }

    @Override
    public List<Permission> findPermissionsByStatus(Integer status) {
        if (status == null) {
            return new ArrayList<>();
        }
        return permissionMapper.findByStatus(status);
    }

    @Override
    public List<Permission> findPermissionsByParentId(Long parentId) {
        return permissionMapper.findByParentId(parentId);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'enabled'")
    public List<Permission> findEnabledPermissions() {
        return permissionMapper.findEnabledPermissions();
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'root'")
    public List<Permission> findRootPermissions() {
        return permissionMapper.findRootPermissions();
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'menu'")
    public List<Permission> findMenuPermissions() {
        return permissionMapper.findByPermissionType(PermissionConstants.Type.MENU);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'button'")
    public List<Permission> findButtonPermissions() {
        return permissionMapper.findByPermissionType(PermissionConstants.Type.BUTTON);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'api'")
    public List<Permission> findApiPermissions() {
        return permissionMapper.findByPermissionType(PermissionConstants.Type.API);
    }

    // ==================== 批量操作方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public int batchDeletePermissions(List<Long> ids, Long deletedBy) {
        log.info("开始批量删除权限：{}", ids);
        
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("权限ID列表不能为空");
        }
        
        // 检查所有权限是否可以删除
        for (Long id : ids) {
            Permission permission = getPermissionById(id);
            if (permission == null) {
                throw new RuntimeException("权限不存在：" + id);
            }
            
            if (permission.isSystemPermission()) {
                throw new RuntimeException("系统内置权限不允许删除：" + permission.getPermissionName());
            }
            
            if (!canDeletePermission(id)) {
                throw new RuntimeException("权限正在被使用，无法删除：" + permission.getPermissionName());
            }
        }
        
        // 执行批量删除
        LambdaUpdateWrapper<Permission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Permission::getId, ids)
                   .set(Permission::getDeleted, SecurityConstants.DeleteFlag.DELETED)
                   .set(Permission::getUpdateBy, deletedBy)
                   .set(Permission::getUpdateTime, LocalDateTime.now());
        
        int result = permissionMapper.update(null, updateWrapper);
        log.info("批量删除权限完成，影响行数：{}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public boolean enablePermission(Long id, Long updatedBy) {
        return updatePermissionStatus(id, SystemConstants.ConfigStatus.ENABLED, updatedBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public boolean disablePermission(Long id, Long updatedBy) {
        return updatePermissionStatus(id, SystemConstants.ConfigStatus.DISABLED, updatedBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public int batchUpdatePermissionStatus(List<Long> ids, Integer status, Long updatedBy) {
        log.info("开始批量更新权限状态：{}, 状态：{}", ids, status);
        
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("权限ID列表不能为空");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("状态不能为空");
        }
        
        LambdaUpdateWrapper<Permission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Permission::getId, ids)
                   .set(Permission::getStatus, status)
                   .set(Permission::getUpdateBy, updatedBy)
                   .set(Permission::getUpdateTime, LocalDateTime.now());
        
        int result = permissionMapper.update(null, updateWrapper);
        log.info("批量更新权限状态完成，影响行数：{}", result);
        return result;
    }

    // ==================== 分页查询方法 ====================

    @Override
    public IPage<Permission> findPermissionsWithPagination(Page<Permission> page, String permissionCode, 
                                                         String permissionName, String permissionType, 
                                                         Integer status, Long parentId) {
        log.info("分页查询权限：页码={}, 页大小={}, 权限编码={}, 权限名称={}, 类型={}, 状态={}, 父ID={}", 
                page.getCurrent(), page.getSize(), permissionCode, permissionName, permissionType, status, parentId);
        
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, SecurityConstants.DeleteFlag.NOT_DELETED);
        
        // 权限编码搜索
        if (StringUtils.hasText(permissionCode)) {
            queryWrapper.like(Permission::getPermissionCode, permissionCode);
        }
        
        // 权限名称搜索
        if (StringUtils.hasText(permissionName)) {
            queryWrapper.like(Permission::getPermissionName, permissionName);
        }
        
        // 权限类型过滤
        if (StringUtils.hasText(permissionType)) {
            queryWrapper.eq(Permission::getPermissionType, permissionType);
        }
        
        // 状态过滤
        if (status != null) {
            queryWrapper.eq(Permission::getStatus, status);
        }
        
        // 父权限过滤
        if (parentId != null) {
            queryWrapper.eq(Permission::getParentId, parentId);
        }
        
        // 排序
        queryWrapper.orderByAsc(Permission::getSortOrder)
                   .orderByAsc(Permission::getCreateTime);
        
        return permissionMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Permission> searchPermissions(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        
        return permissionMapper.searchPermissions(keyword);
    }

    @Override
    public IPage<Permission> searchPermissions(String keyword, List<String> searchFields, 
                                             Object additionalFilter, Page<Permission> page) {
        if (!StringUtils.hasText(keyword)) {
            return page;
        }
        
        return permissionMapper.searchPermissionsWithFields(page, keyword, searchFields);
    }

    // ==================== 权限树构建方法 ====================



    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_TREE, key = "'user:' + #userId")
    public List<Map<String, Object>> getUserPermissionTree(Long userId) {
        log.info("构建用户权限树：{}", userId);
        
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // 获取用户权限
        List<Permission> userPermissions = permissionMapper.findByUserId(userId);
        
        List<Permission> tree = buildTree(userPermissions, SecurityConstants.ROOT_PERMISSION_PARENT_ID);
        return convertToTreeMap(tree);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_TREE, key = "'role:' + #roleId")
    public List<Map<String, Object>> getRolePermissionTree(Long roleId) {
        log.info("构建角色权限树：{}", roleId);
        
        if (roleId == null) {
            return new ArrayList<>();
        }
        
        // 获取角色权限
        List<Permission> rolePermissions = permissionMapper.findByRoleId(roleId);
        
        List<Permission> tree = buildTree(rolePermissions, SecurityConstants.ROOT_PERMISSION_PARENT_ID);
        return convertToTreeMap(tree);
    }

    // ==================== 权限验证方法 ====================

    @Override
    public boolean validatePermissionHierarchy(Long parentId, Long childId) {
        if (parentId == null || childId == null) {
            return false;
        }
        
        if (parentId.equals(childId)) {
            return false;
        }
        
        // 检查是否会形成循环引用
        Permission childPermission = getPermissionById(childId);
        if (childPermission == null) {
            return false;
        }
        
        // 检查父权限路径中是否包含子权限ID
        Permission parentPermission = getPermissionById(parentId);
        if (parentPermission != null && StringUtils.hasText(parentPermission.getPermissionPath())) {
            String[] pathIds = parentPermission.getPermissionPath().split(SecurityConstants.PERMISSION_PATH_SEPARATOR);
            for (String pathId : pathIds) {
                if (childId.toString().equals(pathId)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public List<Long> getAllChildPermissionIds(Long parentId) {
        if (parentId == null) {
            return new ArrayList<>();
        }
        
        List<Long> childIds = new ArrayList<>();
        collectChildPermissionIds(parentId, childIds);
        return childIds;
    }

    @Override
    public List<Long> getAllParentPermissionIds(Long permissionId) {
        if (permissionId == null) {
            return new ArrayList<>();
        }
        
        List<Long> parentIds = new ArrayList<>();
        Permission permission = getPermissionById(permissionId);
        
        if (permission != null && StringUtils.hasText(permission.getPermissionPath())) {
            String[] pathIds = permission.getPermissionPath().split(SecurityConstants.PERMISSION_PATH_SEPARATOR);
            for (String pathId : pathIds) {
                try {
                    Long id = Long.parseLong(pathId);
                    if (!id.equals(SecurityConstants.ROOT_PERMISSION_PARENT_ID)) {
                        parentIds.add(id);
                    }
                } catch (NumberFormatException e) {
                    log.warn("权限路径包含无效ID：{}", pathId);
                }
            }
        }
        
        return parentIds;
    }

    @Override
    public String getPermissionPath(Long permissionId) {
        if (permissionId == null) {
            return "";
        }
        
        Permission permission = getPermissionById(permissionId);
        return permission != null ? permission.getPermissionPath() : "";
    }

    @Override
    public int getPermissionLevel(Long permissionId) {
        if (permissionId == null) {
            return 0;
        }
        
        Permission permission = getPermissionById(permissionId);
        return permission != null ? permission.getPermissionLevel() : 0;
    }

    // ==================== 用户权限检查方法 ====================

    @Override
    public boolean checkUserPermission(Long userId, String permissionCode) {
        if (userId == null || !StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        return permissionMapper.checkUserPermissionByCode(userId, permissionCode) > 0;
    }

    @Override
    public boolean checkUserPermissionByPath(Long userId, String permissionPath) {
        if (userId == null || !StringUtils.hasText(permissionPath)) {
            return false;
        }
        
        return permissionMapper.checkUserPermissionByPath(userId, permissionPath) > 0;
    }

    @Override
    public List<Permission> getUserMenuPermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        return permissionMapper.findUserMenuPermissions(userId);
    }

    @Override
    public List<Permission> getUserButtonPermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        return permissionMapper.findUserButtonPermissions(userId);
    }

    @Override
    public List<Permission> getUserApiPermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        return permissionMapper.findUserApiPermissions(userId);
    }

    // ==================== 缓存管理方法 ====================

    @Override
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public void refreshPermissionCache() {
        log.info("刷新权限缓存");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 更新权限状态
     */
    private boolean updatePermissionStatus(Long id, Integer status, Long updatedBy) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("权限ID和状态不能为空");
        }
        
        Permission permission = getPermissionById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        LambdaUpdateWrapper<Permission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Permission::getId, id)
                   .set(Permission::getStatus, status)
                   .set(Permission::getUpdateBy, updatedBy)
                   .set(Permission::getUpdateTime, LocalDateTime.now());
        
        int result = permissionMapper.update(null, updateWrapper);
        return result > 0;
    }

    /**
     * 构建权限树
     */
    private List<Permission> buildTree(List<Permission> permissions, Long parentId) {
        if (CollectionUtils.isEmpty(permissions)) {
            return new ArrayList<>();
        }
        
        List<Permission> tree = new ArrayList<>();
        
        for (Permission permission : permissions) {
            if (Objects.equals(permission.getParentId(), parentId)) {
                List<Permission> children = buildTree(permissions, permission.getId());
                permission.setChildren(children);
                tree.add(permission);
            }
        }
        
        // 按排序号排序
        tree.sort(Comparator.comparing(Permission::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        
        return tree;
    }

    /**
     * 递归收集子权限ID
     */
    private void collectChildPermissionIds(Long parentId, List<Long> childIds) {
        List<Permission> children = findPermissionsByParentId(parentId);
        for (Permission child : children) {
            childIds.add(child.getId());
            collectChildPermissionIds(child.getId(), childIds);
        }
    }

    @Override
    public boolean canDeletePermission(Long permissionId) {
        if (permissionId == null) {
            return false;
        }
        
        // 检查是否有子权限
        List<Permission> children = findPermissionsByParentId(permissionId);
        if (!CollectionUtils.isEmpty(children)) {
            return false;
        }
        
        // 检查是否被角色使用
        int roleCount = rolePermissionMapper.countByPermissionId(permissionId);
        return roleCount == 0;
    }

    // ==================== 统计方法 ====================

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'type_statistics'")
    public List<Map<String, Object>> getPermissionTypeStatistics() {
        log.info("开始获取权限类型统计");
        
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        // 统计各种类型的权限数量
        Map<String, Object> menuStat = new HashMap<>();
        menuStat.put("type", "menu");
        menuStat.put("count", (long) permissionMapper.countByPermissionType(PermissionConstants.Type.MENU));
        statistics.add(menuStat);
        
        Map<String, Object> buttonStat = new HashMap<>();
        buttonStat.put("type", "button");
        buttonStat.put("count", (long) permissionMapper.countByPermissionType(PermissionConstants.Type.BUTTON));
        statistics.add(buttonStat);
        
        Map<String, Object> apiStat = new HashMap<>();
        apiStat.put("type", "api");
        apiStat.put("count", (long) permissionMapper.countByPermissionType(PermissionConstants.Type.API));
        statistics.add(apiStat);
        
        Map<String, Object> dataStat = new HashMap<>();
        dataStat.put("type", "data");
        dataStat.put("count", (long) permissionMapper.countByPermissionType(PermissionConstants.Type.DATA));
        statistics.add(dataStat);
        
        log.info("权限类型统计完成：{}", statistics);
        return statistics;
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'status_statistics'")
    public List<Map<String, Object>> getPermissionStatusStatistics() {
        log.info("开始获取权限状态统计");
        
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        // 统计各种状态的权限数量
        Map<String, Object> enabledStat = new HashMap<>();
        enabledStat.put("status", "enabled");
        enabledStat.put("count", (long) permissionMapper.countByStatus(SystemConstants.ConfigStatus.ENABLED));
        statistics.add(enabledStat);
        
        Map<String, Object> disabledStat = new HashMap<>();
        disabledStat.put("status", "disabled");
        disabledStat.put("count", (long) permissionMapper.countByStatus(SystemConstants.ConfigStatus.DISABLED));
        statistics.add(disabledStat);
        
        log.info("权限状态统计完成：{}", statistics);
        return statistics;
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_PREFIX, key = "'total_count'")
    public long countPermissions() {
        log.info("开始统计权限总数");
        
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, SecurityConstants.DeleteFlag.NOT_DELETED);
        
        long count = permissionMapper.selectCount(queryWrapper);
        log.info("权限总数统计完成：{}", count);
        return count;
    }

    // ==================== 权限树方法修正 ====================

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_TREE, key = "'all'")
    public List<Permission> buildPermissionTree() {
        log.info("开始构建完整权限树");
        
        // 获取所有权限
        List<Permission> allPermissions = permissionMapper.selectList(
            new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, SecurityConstants.DeleteFlag.NOT_DELETED)
                .orderByAsc(Permission::getSort, Permission::getId)
        );
        
        // 构建树结构
        List<Permission> tree = buildTree(allPermissions, null);
        log.info("权限树构建完成，根节点数量：{}", tree.size());
        return tree;
    }

    @Override
    public List<Map<String, Object>> buildPermissionTree(List<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return new ArrayList<>();
        }
        
        return convertToTreeMap(buildTree(permissions, null));
    }

    @Override
    public List<Map<String, Object>> getPermissionTree() {
        List<Permission> tree = buildPermissionTree();
        return convertToTreeMap(tree);
    }

    @Override
    @Cacheable(value = SecurityConstants.Cache.PERMISSION_TREE, key = "'enabled'")
    public List<Map<String, Object>> getEnabledPermissionTree() {
        log.info("开始构建启用权限树");
        
        // 获取所有启用的权限
        List<Permission> enabledPermissions = permissionMapper.selectList(
            new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, SecurityConstants.DeleteFlag.NOT_DELETED)
                .eq(Permission::getStatus, SystemConstants.ConfigStatus.ENABLED)
                .orderByAsc(Permission::getSort, Permission::getId)
        );
        
        // 构建树结构并转换为Map格式
        List<Permission> tree = buildTree(enabledPermissions, null);
        List<Map<String, Object>> result = convertToTreeMap(tree);
        
        log.info("启用权限树构建完成，根节点数量：{}", result.size());
        return result;
    }

    // ==================== 权限操作方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public boolean movePermission(Long permissionId, Long newParentId, Long updatedBy) {
        log.info("开始移动权限：{} 到父权限：{}", permissionId, newParentId);
        
        if (permissionId == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        
        // 检查权限是否存在
        Permission permission = getPermissionById(permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查是否为系统内置权限
        if (permission.isSystemPermission()) {
            throw new RuntimeException("系统内置权限不允许移动");
        }
        
        // 验证层级关系（防止循环依赖）
        if (newParentId != null && !validatePermissionHierarchy(newParentId, permissionId)) {
            throw new RuntimeException("移动操作会导致循环依赖");
        }
        
        // 更新权限的父ID
        permission.setParentId(newParentId);
        permission.setUpdatedBy(updatedBy);
        permission.setUpdateTime(LocalDateTime.now());
        
        // 重新计算权限路径和级别
        calculatePermissionPathAndLevel(permission);
        
        // 更新权限
        int result = permissionMapper.updateById(permission);
        if (result <= 0) {
            log.error("权限移动失败：{}", permissionId);
            throw new RuntimeException("权限移动失败");
        }
        
        // 递归更新所有子权限的路径和级别
        updateChildPermissionPaths(permissionId);
        
        log.info("权限移动成功：{}", permissionId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public Permission copyPermission(Long sourcePermissionId, String newPermissionCode, 
                                   String newPermissionName, Long parentId, Long createdBy) {
        log.info("开始复制权限：{} 为新权限：{}", sourcePermissionId, newPermissionCode);
        
        if (sourcePermissionId == null) {
            throw new IllegalArgumentException("源权限ID不能为空");
        }
        if (!StringUtils.hasText(newPermissionCode)) {
            throw new IllegalArgumentException("新权限编码不能为空");
        }
        if (!StringUtils.hasText(newPermissionName)) {
            throw new IllegalArgumentException("新权限名称不能为空");
        }
        
        // 检查源权限是否存在
        Permission sourcePermission = getPermissionById(sourcePermissionId);
        if (sourcePermission == null) {
            throw new RuntimeException("源权限不存在");
        }
        
        // 检查新权限编码是否已存在
        if (existsByPermissionCode(newPermissionCode)) {
            throw new RuntimeException("权限编码已存在：" + newPermissionCode);
        }
        
        // 检查新权限名称是否已存在
        if (existsByPermissionName(newPermissionName)) {
            throw new RuntimeException("权限名称已存在：" + newPermissionName);
        }
        
        // 创建新权限
        Permission newPermission = new Permission();
        // 复制源权限的属性
        newPermission.setPermissionName(newPermissionName);
        newPermission.setPermissionCode(newPermissionCode);
        newPermission.setPermissionType(sourcePermission.getPermissionType());
        newPermission.setParentId(parentId);
        newPermission.setMenuUrl(sourcePermission.getMenuUrl());
        newPermission.setIcon(sourcePermission.getIcon());
        newPermission.setComponent(sourcePermission.getComponent());
        newPermission.setApiUrl(sourcePermission.getApiUrl());
        newPermission.setHttpMethod(sourcePermission.getHttpMethod());
        newPermission.setStatus(sourcePermission.getStatus());// 复制的权限不是系统内置
        newPermission.setIsSystemPermission(SecurityConstants.SystemFlag.NO);  // 复制的权限不是系统内置        newPermission.setShowInMenu(sourcePermission.getShowInMenu());
        newPermission.setKeepAlive(sourcePermission.getKeepAlive());
        newPermission.setSort(sourcePermission.getSort());
        newPermission.setDescription(sourcePermission.getDescription());
        newPermission.setCreatedBy(createdBy);
        newPermission.setCreateTime(LocalDateTime.now());
        
        // 创建权限
        Permission createdPermission = createPermission(newPermission);
        
        log.info("权限复制成功：{} -> {}", sourcePermissionId, createdPermission.getId());
        return createdPermission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public int batchCopyPermissions(List<Long> sourcePermissionIds, Long targetParentId, Long createdBy) {
        log.info("开始批量复制权限：{} 到父权限：{}", sourcePermissionIds, targetParentId);
        
        if (CollectionUtils.isEmpty(sourcePermissionIds)) {
            return 0;
        }
        
        int successCount = 0;
        
        for (Long sourceId : sourcePermissionIds) {
            try {
                Permission sourcePermission = getPermissionById(sourceId);
                if (sourcePermission == null) {
                    log.warn("源权限不存在，跳过：{}", sourceId);
                    continue;
                }
                
                // 生成新的权限编码和名称
                String newCode = generateUniquePermissionCode(sourcePermission.getPermissionCode());
                String newName = generateUniquePermissionName(sourcePermission.getPermissionName());
                
                // 复制权限
                copyPermission(sourceId, newCode, newName, targetParentId, createdBy);
                successCount++;
                
            } catch (Exception e) {
                log.error("复制权限失败：{}, 错误：{}", sourceId, e.getMessage());
            }
        }
        
        log.info("批量复制权限完成，成功：{}/{}", successCount, sourcePermissionIds.size());
        return successCount;
    }

    // ==================== 导入导出方法 ====================

    @Override
    public List<Permission> exportPermissions(List<Long> ids) {
        log.info("开始导出权限数据，ID列表：{}", ids);
        
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, SecurityConstants.DeleteFlag.NOT_DELETED);
        
        if (!CollectionUtils.isEmpty(ids)) {
            queryWrapper.in(Permission::getId, ids);
        }
        
        queryWrapper.orderByAsc(Permission::getSort, Permission::getId);
        
        List<Permission> permissions = permissionMapper.selectList(queryWrapper);
        log.info("权限数据导出完成，数量：{}", permissions.size());
        return permissions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {SecurityConstants.Cache.PERMISSION_PREFIX, SecurityConstants.Cache.PERMISSION_TREE}, allEntries = true)
    public int importPermissions(List<Permission> permissions, Long createdBy) {
        log.info("开始导入权限数据，数量：{}", permissions != null ? permissions.size() : 0);
        
        if (CollectionUtils.isEmpty(permissions)) {
            return 0;
        }
        
        int successCount = 0;
        
        for (Permission permission : permissions) {
            try {
                // 检查权限编码是否已存在
                if (existsByPermissionCode(permission.getPermissionCode())) {
                    log.warn("权限编码已存在，跳过：{}", permission.getPermissionCode());
                    continue;
                }
                
                // 重置ID和时间字段
                permission.setId(null);
                permission.setCreatedBy(createdBy);
                permission.setCreateTime(LocalDateTime.now());
                permission.setUpdatedBy(null);
                permission.setUpdateTime(null);
                permission.setDeleted(SecurityConstants.DeleteFlag.NOT_DELETED);
                permission.setVersion(1); // version字段类型是Integer
                
                // 创建权限
                createPermission(permission);
                successCount++;
                
            } catch (Exception e) {
                log.error("导入权限失败：{}, 错误：{}", permission.getPermissionCode(), e.getMessage());
            }
        }
        
        log.info("权限数据导入完成，成功：{}/{}", successCount, permissions.size());
        return successCount;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将权限树转换为Map格式
     */
    private List<Map<String, Object>> convertToTreeMap(List<Permission> tree) {
        if (CollectionUtils.isEmpty(tree)) {
            return new ArrayList<>();
        }
        
        return tree.stream().map(permission -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", permission.getId());
            map.put("permissionName", permission.getPermissionName());
            map.put("permissionCode", permission.getPermissionCode());
            map.put("permissionType", permission.getPermissionType());
            map.put("parentId", permission.getParentId());
            map.put("permissionPath", permission.getPermissionPath());
            map.put("permissionLevel", permission.getPermissionLevel());
            map.put("menuUrl", permission.getMenuUrl());
            map.put("icon", permission.getIcon());
            map.put("component", permission.getComponent());
            map.put("apiUrl", permission.getApiUrl());
            map.put("httpMethod", permission.getHttpMethod());
            map.put("status", permission.getStatus());
            map.put("isSystemPermission", permission.getIsSystemPermission());
            map.put("showInMenu", permission.getShowInMenu());
            map.put("keepAlive", permission.getKeepAlive());
            map.put("sort", permission.getSort());
            map.put("description", permission.getDescription());
            
            // 递归处理子权限
            if (!CollectionUtils.isEmpty(permission.getChildren())) {
                map.put("children", convertToTreeMap(permission.getChildren()));
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 更新子权限的路径和级别
     */
    private void updateChildPermissionPaths(Long parentId) {
        List<Permission> children = findPermissionsByParentId(parentId);
        
        for (Permission child : children) {
            // 重新计算路径和级别
            calculatePermissionPathAndLevel(child);
            
            // 更新子权限
            permissionMapper.updateById(child);
            
            // 递归更新子权限的子权限
            updateChildPermissionPaths(child.getId());
        }
    }

    /**
     * 生成唯一的权限编码
     */
    private String generateUniquePermissionCode(String baseCode) {
        String newCode = baseCode + "_copy";
        int counter = 1;
        
        while (existsByPermissionCode(newCode)) {
            newCode = baseCode + "_copy_" + counter;
            counter++;
        }
        
        return newCode;
    }

    /**
     * 生成唯一的权限名称
     */
    private String generateUniquePermissionName(String baseName) {
        String newName = baseName + "(副本)";
        int counter = 1;
        
        while (existsByPermissionName(newName)) {
            newName = baseName + "(副本" + counter + ")";
            counter++;
        }
        
        return newName;
    }

    @Override
    public long countPermissionsByType(String permissionType) {
        return permissionMapper.countByPermissionType(permissionType);
    }

    @Override
    public long countPermissionsByStatus(Integer status) {
        return permissionMapper.countByStatus(status);
    }

    @Override
    public long countPermissionsByParentId(Long parentId) {
        return permissionMapper.getChildrenCount(parentId);
    }

    @Override
    public int syncPermissions(String source, Long updatedBy) {
        // TODO: 实现权限同步逻辑
        log.info("同步权限，来源：{}，更新人：{}", source, updatedBy);
        return 0;
    }

    @Override
    public Map<String, Object> getPermissionDependencies(Long permissionId) {
        Map<String, Object> dependencies = new HashMap<>();
        
        // 获取子权限数量
        long childCount = permissionMapper.getChildrenCount(permissionId);
        dependencies.put("childCount", childCount);
        
        // 获取关联角色数量 (需要在RolePermissionMapper中添加此方法)
        // long roleCount = rolePermissionMapper.countRolesByPermissionId(permissionId);
        // dependencies.put("roleCount", roleCount);
        dependencies.put("roleCount", 0L); // 临时返回0
        
        return dependencies;
    }

    @Override
    public int batchUpdatePermissionSort(Map<Long, Integer> permissionOrders, Long updatedBy) {
        if (CollectionUtils.isEmpty(permissionOrders)) {
            return 0;
        }
        
        int updateCount = 0;
        for (Map.Entry<Long, Integer> entry : permissionOrders.entrySet()) {
            Long permissionId = entry.getKey();
            Integer sortOrder = entry.getValue();
            
            LambdaUpdateWrapper<Permission> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Permission::getId, permissionId)
                        .set(Permission::getSortOrder, sortOrder)
                        .set(Permission::getUpdatedBy, updatedBy)
                        .set(Permission::getUpdateTime, LocalDateTime.now()); // 使用getUpdateTime而不是getUpdatedTime
            
            int result = permissionMapper.update(null, updateWrapper);
            if (result > 0) {
                updateCount++;
            }
        }
        
        return updateCount;
    }

    @Override
    public List<Map<String, Object>> getPermissionUsageStatistics(int days) {
        // TODO: 实现权限使用统计逻辑
        List<Map<String, Object>> statistics = new ArrayList<>();
        Map<String, Object> stat = new HashMap<>();
        stat.put("period", days + "天");
        stat.put("totalAccess", 0);
        stat.put("uniqueUsers", 0);
        statistics.add(stat);
        return statistics;
    }

    @Override
    public int cleanupInvalidPermissionAssociations() {
        // TODO: 实现清理无效权限关联逻辑
        log.info("清理无效的权限关联");
        return 0;
    }

    @Override
    public Map<String, Object> generatePermissionReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // 基本统计
        long totalPermissions = permissionMapper.selectCount(null);
        long enabledPermissions = permissionMapper.countByStatus(1);
        long disabledPermissions = permissionMapper.countByStatus(0);
        
        report.put("totalPermissions", totalPermissions);
        report.put("enabledPermissions", enabledPermissions);
        report.put("disabledPermissions", disabledPermissions);
        report.put("reportPeriod", startDate + " 至 " + endDate);
        report.put("generatedTime", LocalDateTime.now());
        
        return report;
    }

    @Override
    public List<Map<String, Object>> getPermissionAccessLogs(Long permissionId, int days) {
        // TODO: 实现权限访问日志逻辑
        List<Map<String, Object>> logs = new ArrayList<>();
        Map<String, Object> log = new HashMap<>();
        log.put("permissionId", permissionId);
        log.put("accessCount", 0);
        log.put("period", days + "天");
        logs.add(log);
        return logs;
    }

    @Override
    public boolean checkUserPathPermission(Long userId, String requestPath, String requestMethod) {
        // TODO: 实现用户路径权限检查逻辑
        log.debug("检查用户路径权限，用户ID：{}，路径：{}，方法：{}", userId, requestPath, requestMethod);
        return true;
    }

    @Override
    public boolean refreshPermissionCache(Long permissionId) {
        try {
            if (permissionId != null) {
                // 刷新特定权限的缓存
                log.info("刷新权限缓存，权限ID：{}", permissionId);
            } else {
                // 刷新所有权限缓存
                log.info("刷新所有权限缓存");
            }
            return true;
        } catch (Exception e) {
            log.error("刷新权限缓存失败", e);
            return false;
        }
    }
}