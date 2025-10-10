package com.archive.management.service;

import com.archive.management.entity.Permission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限业务服务接口
 * 定义权限管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface PermissionService {

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建的权限
     */
    Permission createPermission(Permission permission);

    /**
     * 根据ID获取权限
     * @param id 权限ID
     * @return 权限信息
     */
    Permission getPermissionById(Long id);

    /**
     * 根据权限编码获取权限
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    Permission getPermissionByCode(String permissionCode);

    /**
     * 根据权限名称获取权限
     * @param permissionName 权限名称
     * @return 权限信息
     */
    Permission getPermissionByName(String permissionName);

    /**
     * 根据权限路径获取权限
     * @param permissionPath 权限路径
     * @return 权限信息
     */
    Permission getPermissionByPath(String permissionPath);

    /**
     * 更新权限信息
     * @param permission 权限信息
     * @return 更新后的权限
     */
    Permission updatePermission(Permission permission);

    /**
     * 删除权限（软删除）
     * @param id 权限ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deletePermission(Long id, Long deletedBy);

    /**
     * 批量删除权限（软删除）
     * @param ids 权限ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeletePermissions(List<Long> ids, Long deletedBy);

    /**
     * 启用权限
     * @param id 权限ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enablePermission(Long id, Long updatedBy);

    /**
     * 禁用权限
     * @param id 权限ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disablePermission(Long id, Long updatedBy);

    /**
     * 批量更新权限状态
     * @param ids 权限ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdatePermissionStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 检查权限编码是否存在
     * @param permissionCode 权限编码
     * @return 是否存在
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 检查权限名称是否存在
     * @param permissionName 权限名称
     * @return 是否存在
     */
    boolean existsByPermissionName(String permissionName);

    /**
     * 检查权限路径是否存在
     * @param permissionPath 权限路径
     * @return 是否存在
     */
    boolean existsByPermissionPath(String permissionPath);

    /**
     * 分页查询权限
     * @param page 分页参数
     * @param permissionCode 权限编码（可选）
     * @param permissionName 权限名称（可选）
     * @param permissionType 权限类型（可选）
     * @param status 状态（可选）
     * @param parentId 父权限ID（可选）
     * @return 分页结果
     */
    IPage<Permission> findPermissionsWithPagination(Page<Permission> page, String permissionCode, 
                                                   String permissionName, String permissionType, 
                                                   Integer status, Long parentId);

    /**
     * 根据权限类型查找权限列表
     * @param permissionType 权限类型
     * @return 权限列表
     */
    List<Permission> findPermissionsByType(String permissionType);

    /**
     * 根据状态查找权限列表
     * @param status 状态
     * @return 权限列表
     */
    List<Permission> findPermissionsByStatus(Integer status);

    /**
     * 根据父权限ID查找子权限列表
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<Permission> findPermissionsByParentId(Long parentId);

    /**
     * 获取所有启用的权限
     * @return 权限列表
     */
    List<Permission> findEnabledPermissions();

    /**
     * 获取根权限列表（顶级权限）
     * @return 权限列表
     */
    List<Permission> findRootPermissions();

    /**
     * 获取菜单类型权限
     * @return 权限列表
     */
    List<Permission> findMenuPermissions();

    /**
     * 获取按钮类型权限
     * @return 权限列表
     */
    List<Permission> findButtonPermissions();

    /**
     * 获取API类型权限
     * @return 权限列表
     */
    List<Permission> findApiPermissions();

    /**
     * 统计权限总数
     * @return 总数
     */
    long countPermissions();

    /**
     * 根据权限类型统计权限数量
     * @param permissionType 权限类型
     * @return 数量
     */
    long countPermissionsByType(String permissionType);

    /**
     * 根据状态统计权限数量
     * @param status 状态
     * @return 数量
     */
    long countPermissionsByStatus(Integer status);

    /**
     * 根据父权限统计子权限数量
     * @param parentId 父权限ID
     * @return 数量
     */
    long countPermissionsByParentId(Long parentId);

    /**
     * 获取权限类型统计
     * @return 类型统计结果
     */
    List<Map<String, Object>> getPermissionTypeStatistics();

    /**
     * 获取权限状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getPermissionStatusStatistics();

    /**
     * 根据关键词搜索权限
     * @param keyword 关键词
     * @return 权限列表
     */
    List<Permission> searchPermissions(String keyword);

    /**
     * 根据关键词和搜索字段搜索权限（分页）
     * @param keyword 关键词
     * @param searchFields 搜索字段列表
     * @param additionalFilter 额外过滤条件
     * @param page 分页参数
     * @return 分页权限列表
     */
    IPage<Permission> searchPermissions(String keyword, List<String> searchFields, Object additionalFilter, Page<Permission> page);

    /**
     * 构建权限树结构
     * @param permissions 权限列表
     * @return 权限树
     */
    List<Map<String, Object>> buildPermissionTree(List<Permission> permissions);

    /**
     * 获取完整权限树
     * @return 权限树
     */
    List<Map<String, Object>> getPermissionTree();

    /**
     * 获取启用权限树
     * @return 启用权限树
     */
    List<Map<String, Object>> getEnabledPermissionTree();

    /**
     * 获取用户权限树
     * @param userId 用户ID
     * @return 用户权限树
     */
    List<Map<String, Object>> getUserPermissionTree(Long userId);

    /**
     * 获取角色权限树
     * @param roleId 角色ID
     * @return 角色权限树
     */
    List<Map<String, Object>> getRolePermissionTree(Long roleId);

    /**
     * 验证权限层级关系
     * @param parentId 父权限ID
     * @param childId 子权限ID
     * @return 是否存在循环依赖
     */
    boolean validatePermissionHierarchy(Long parentId, Long childId);

    /**
     * 获取权限的所有子权限ID
     * @param parentId 父权限ID
     * @return 子权限ID列表
     */
    List<Long> getAllChildPermissionIds(Long parentId);

    /**
     * 获取权限的所有父权限ID
     * @param childId 子权限ID
     * @return 父权限ID列表
     */
    List<Long> getAllParentPermissionIds(Long childId);

    /**
     * 获取权限路径
     * @param permissionId 权限ID
     * @return 权限路径字符串
     */
    String getPermissionPath(Long permissionId);

    /**
     * 获取权限层级
     * @param permissionId 权限ID
     * @return 权限层级
     */
    int getPermissionLevel(Long permissionId);

    /**
     * 移动权限到新的父权限下
     * @param permissionId 权限ID
     * @param newParentId 新父权限ID
     * @param updatedBy 更新人ID
     * @return 是否移动成功
     */
    boolean movePermission(Long permissionId, Long newParentId, Long updatedBy);

    /**
     * 复制权限
     * @param sourcePermissionId 源权限ID
     * @param newPermissionCode 新权限编码
     * @param newPermissionName 新权限名称
     * @param parentId 父权限ID
     * @param createdBy 创建人ID
     * @return 新创建的权限
     */
    Permission copyPermission(Long sourcePermissionId, String newPermissionCode, 
                             String newPermissionName, Long parentId, Long createdBy);

    /**
     * 批量复制权限
     * @param sourcePermissionIds 源权限ID列表
     * @param targetParentId 目标父权限ID
     * @param createdBy 创建人ID
     * @return 复制成功的数量
     */
    int batchCopyPermissions(List<Long> sourcePermissionIds, Long targetParentId, Long createdBy);

    /**
     * 导出权限数据
     * @param ids 权限ID列表（可选，为空则导出所有）
     * @return 权限列表
     */
    List<Permission> exportPermissions(List<Long> ids);

    /**
     * 批量导入权限
     * @param permissions 权限列表
     * @param createdBy 创建人ID
     * @return 导入成功的数量
     */
    int importPermissions(List<Permission> permissions, Long createdBy);

    /**
     * 同步权限菜单（从配置文件或注解）
     * @param source 同步源（config/annotation）
     * @param updatedBy 更新人ID
     * @return 同步成功的数量
     */
    int syncPermissions(String source, Long updatedBy);

    /**
     * 检查权限是否可以删除
     * @param permissionId 权限ID
     * @return 是否可以删除
     */
    boolean canDeletePermission(Long permissionId);

    /**
     * 获取权限依赖信息
     * @param permissionId 权限ID
     * @return 依赖信息
     */
    Map<String, Object> getPermissionDependencies(Long permissionId);

    /**
     * 批量更新权限排序
     * @param permissionOrders 权限排序映射（权限ID -> 排序值）
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdatePermissionSort(Map<Long, Integer> permissionOrders, Long updatedBy);

    /**
     * 获取权限使用统计
     * @param days 统计天数
     * @return 使用统计结果
     */
    List<Map<String, Object>> getPermissionUsageStatistics(int days);

    /**
     * 清理无效的权限关联
     * @return 清理的数量
     */
    int cleanupInvalidPermissionAssociations();

    /**
     * 生成权限报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    Map<String, Object> generatePermissionReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取权限访问日志
     * @param permissionId 权限ID
     * @param days 天数
     * @return 访问日志
     */
    List<Map<String, Object>> getPermissionAccessLogs(Long permissionId, int days);

    /**
     * 检查用户是否有访问权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean checkUserPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有访问路径权限
     * @param userId 用户ID
     * @param requestPath 请求路径
     * @param requestMethod 请求方法
     * @return 是否有权限
     */
    boolean checkUserPathPermission(Long userId, String requestPath, String requestMethod);

    /**
     * 获取用户可访问的菜单权限
     * @param userId 用户ID
     * @return 菜单权限列表
     */
    List<Permission> getUserMenuPermissions(Long userId);

    /**
     * 获取用户可访问的按钮权限
     * @param userId 用户ID
     * @return 按钮权限列表
     */
    List<Permission> getUserButtonPermissions(Long userId);

    /**
     * 获取用户可访问的API权限
     * @param userId 用户ID
     * @return API权限列表
     */
    List<Permission> getUserApiPermissions(Long userId);

    /**
     * 刷新权限缓存
     * @param permissionId 权限ID（可选，为空则刷新所有）
     * @return 是否刷新成功
     */
    boolean refreshPermissionCache(Long permissionId);

    /**
     * 检查用户是否有指定路径的权限
     * @param userId 用户ID
     * @param permissionPath 权限路径
     * @return 是否有权限
     */
    boolean checkUserPermissionByPath(Long userId, String permissionPath);

    /**
     * 刷新权限缓存（无参数版本）
     */
    void refreshPermissionCache();

    /**
     * 构建完整权限树（无参数版本）
     * @return 权限列表
     */
    List<Permission> buildPermissionTree();
}