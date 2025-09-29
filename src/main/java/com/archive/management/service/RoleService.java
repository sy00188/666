package com.archive.management.service;

import com.archive.management.entity.Role;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 角色业务服务接口
 * 定义角色管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface RoleService {

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建的角色
     */
    Role createRole(Role role);

    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    Role getRoleById(Long id);

    /**
     * 根据角色编码获取角色
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Role getRoleByCode(String roleCode);

    /**
     * 根据角色名称获取角色
     * @param roleName 角色名称
     * @return 角色信息
     */
    Role getRoleByName(String roleName);

    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 更新后的角色
     */
    Role updateRole(Role role);

    /**
     * 删除角色（软删除）
     * @param id 角色ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteRole(Long id, Long deletedBy);

    /**
     * 批量删除角色（软删除）
     * @param ids 角色ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteRoles(List<Long> ids, Long deletedBy);

    /**
     * 启用角色
     * @param id 角色ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableRole(Long id, Long updatedBy);

    /**
     * 禁用角色
     * @param id 角色ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableRole(Long id, Long updatedBy);

    /**
     * 批量更新角色状态
     * @param ids 角色ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateRoleStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 检查角色编码是否存在
     * @param roleCode 角色编码
     * @return 是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色名称是否存在
     * @param roleName 角色名称
     * @return 是否存在
     */
    boolean existsByRoleName(String roleName);

    /**
     * 分页查询角色
     * @param page 分页参数
     * @param roleCode 角色编码（可选）
     * @param roleName 角色名称（可选）
     * @param status 状态（可选）
     * @param roleType 角色类型（可选）
     * @return 分页结果
     */
    IPage<Role> findRolesWithPagination(Page<Role> page, String roleCode, String roleName, 
                                       Integer status, String roleType);

    /**
     * 根据状态查找角色列表
     * @param status 状态
     * @return 角色列表
     */
    List<Role> findRolesByStatus(Integer status);

    /**
     * 根据角色类型查找角色列表
     * @param roleType 角色类型
     * @return 角色列表
     */
    List<Role> findRolesByType(String roleType);

    /**
     * 获取所有启用的角色
     * @return 角色列表
     */
    List<Role> findEnabledRoles();

    /**
     * 获取系统内置角色
     * @return 角色列表
     */
    List<Role> findBuiltinRoles();

    /**
     * 获取自定义角色
     * @return 角色列表
     */
    List<Role> findCustomRoles();

    /**
     * 统计角色总数
     * @return 总数
     */
    long countRoles();

    /**
     * 根据状态统计角色数量
     * @param status 状态
     * @return 数量
     */
    long countRolesByStatus(Integer status);

    /**
     * 根据角色类型统计角色数量
     * @param roleType 角色类型
     * @return 数量
     */
    long countRolesByType(String roleType);

    /**
     * 获取角色状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getRoleStatusStatistics();

    /**
     * 获取角色类型统计
     * @return 类型统计结果
     */
    List<Map<String, Object>> getRoleTypeStatistics();

    /**
     * 根据关键词搜索角色
     * @param keyword 关键词
     * @return 角色列表
     */
    List<Role> searchRoles(String keyword);

    /**
     * 获取角色的权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> getRolePermissions(Long roleId);

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @param assignedBy 分配人ID
     * @return 分配成功的数量
     */
    int assignPermissionsToRole(Long roleId, List<Long> permissionIds, Long assignedBy);

    /**
     * 移除角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @param removedBy 移除人ID
     * @return 移除成功的数量
     */
    int removePermissionsFromRole(Long roleId, List<Long> permissionIds, Long removedBy);

    /**
     * 检查角色是否有指定权限
     * @param roleId 角色ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long roleId, String permission);

    /**
     * 获取拥有指定权限的角色列表
     * @param permission 权限标识
     * @return 角色列表
     */
    List<Role> findRolesByPermission(String permission);

    /**
     * 获取角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    long countUsersByRole(Long roleId);

    /**
     * 获取角色的用户列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getUserIdsByRole(Long roleId);

    /**
     * 复制角色
     * @param sourceRoleId 源角色ID
     * @param newRoleCode 新角色编码
     * @param newRoleName 新角色名称
     * @param createdBy 创建人ID
     * @return 新创建的角色
     */
    Role copyRole(Long sourceRoleId, String newRoleCode, String newRoleName, Long createdBy);

    /**
     * 导出角色数据
     * @param ids 角色ID列表（可选，为空则导出所有）
     * @return 角色列表
     */
    List<Role> exportRoles(List<Long> ids);

    /**
     * 批量导入角色
     * @param roles 角色列表
     * @param createdBy 创建人ID
     * @return 导入成功的数量
     */
    int importRoles(List<Role> roles, Long createdBy);

    /**
     * 验证角色层级关系
     * @param parentRoleId 父角色ID
     * @param childRoleId 子角色ID
     * @return 是否存在循环依赖
     */
    boolean validateRoleHierarchy(Long parentRoleId, Long childRoleId);

    /**
     * 获取角色层级树
     * @return 角色层级树
     */
    List<Map<String, Object>> getRoleHierarchyTree();

    /**
     * 获取角色的子角色列表
     * @param parentRoleId 父角色ID
     * @return 子角色列表
     */
    List<Role> getChildRoles(Long parentRoleId);

    /**
     * 获取角色的父角色
     * @param roleId 角色ID
     * @return 父角色
     */
    Role getParentRole(Long roleId);

    /**
     * 设置角色的父角色
     * @param roleId 角色ID
     * @param parentRoleId 父角色ID
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setParentRole(Long roleId, Long parentRoleId, Long updatedBy);

    /**
     * 移除角色的父角色关系
     * @param roleId 角色ID
     * @param updatedBy 更新人ID
     * @return 是否移除成功
     */
    boolean removeParentRole(Long roleId, Long updatedBy);

    /**
     * 获取角色权限树
     * @param roleId 角色ID
     * @return 权限树
     */
    List<Map<String, Object>> getRolePermissionTree(Long roleId);

    /**
     * 同步角色权限（从模板角色）
     * @param targetRoleId 目标角色ID
     * @param templateRoleId 模板角色ID
     * @param updatedBy 更新人ID
     * @return 同步成功的权限数量
     */
    int syncRolePermissions(Long targetRoleId, Long templateRoleId, Long updatedBy);

    /**
     * 清理无效的角色关联
     * @return 清理的数量
     */
    int cleanupInvalidRoleAssociations();

    /**
     * 生成角色报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    Map<String, Object> generateRoleReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取角色使用统计
     * @param days 统计天数
     * @return 使用统计结果
     */
    List<Map<String, Object>> getRoleUsageStatistics(int days);

    /**
     * 检查角色是否可以删除
     * @param roleId 角色ID
     * @return 是否可以删除
     */
    boolean canDeleteRole(Long roleId);

    /**
     * 获取角色依赖信息
     * @param roleId 角色ID
     * @return 依赖信息
     */
    Map<String, Object> getRoleDependencies(Long roleId);

    /**
     * 批量更新角色排序
     * @param roleOrders 角色排序映射（角色ID -> 排序值）
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateRoleSort(Map<Long, Integer> roleOrders, Long updatedBy);

    /**
     * 获取推荐的角色权限配置
     * @param roleType 角色类型
     * @return 推荐的权限ID列表
     */
    List<Long> getRecommendedPermissions(String roleType);
}