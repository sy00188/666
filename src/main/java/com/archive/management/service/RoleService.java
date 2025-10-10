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
     * @param roleId 角色ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteRole(Long roleId, Long deletedBy);

    /**
     * 批量删除角色（软删除）
     * @param roleIds 角色ID列表
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean batchDeleteRoles(List<Long> roleIds, Long deletedBy);

    /**
     * 启用角色
     * @param roleId 角色ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableRole(Long roleId, Long updatedBy);

    /**
     * 禁用角色
     * @param roleId 角色ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableRole(Long roleId, Long updatedBy);

    /**
     * 批量更新角色状态
     * @param roleIds 角色ID列表
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean batchUpdateStatus(List<Long> roleIds, Integer status);

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
     * @param userId 用户ID（用于权限过滤）
     * @return 分页结果
     */
    IPage<Role> findRolesWithPagination(Page<Role> page, String roleCode, String roleName, 
                                       Integer status, String roleType, Long userId);

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
     * 获取所有角色
     * @return 角色列表
     */
    List<Role> getAllRoles();

    /**
     * 获取启用的角色
     * @return 角色列表
     */
    List<Role> getEnabledRoles();

    /**
     * 获取禁用的角色
     * @return 角色列表
     */
    List<Role> getDisabledRoles();

    /**
     * 获取系统角色
     * @return 角色列表
     */
    List<Role> getSystemRoles();

    /**
     * 获取系统内置角色
     * @return 角色列表
     */
    List<Role> findBuiltinRoles();

    /**
     * 获取业务角色
     * @return 角色列表
     */
    List<Role> getBusinessRoles();

    /**
     * 获取自定义角色
     * @return 角色列表
     */
    List<Role> findCustomRoles();

    /**
     * 获取自定义角色
     * @return 角色列表
     */
    List<Role> getCustomRoles();

    /**
     * 根据角色类型获取角色
     * @param roleType 角色类型
     * @return 角色列表
     */
    List<Role> getRolesByType(Integer roleType);

    /**
     * 根据角色级别获取角色
     * @param roleLevel 角色级别
     * @return 角色列表
     */
    List<Role> getRolesByLevel(Integer roleLevel);

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
     * 获取角色层级统计
     * @return 层级统计结果
     */
    Map<String, Object> getRoleHierarchyStatistics();

    /**
     * 获取角色权限统计
     * @return 权限统计结果
     */
    Map<String, Object> getRolePermissionStatistics();

    /**
     * 根据关键词搜索角色
     * @param keyword 关键词
     * @param userId 用户ID（用于权限过滤）
     * @return 角色列表
     */
    List<Role> searchRoles(String keyword, Long userId);

    /**
     * 分页查询角色
     * @param page 分页参数
     * @param queryRole 查询条件
     * @return 分页结果
     */
    IPage<Role> getRolesWithPagination(Page<Role> page, Role queryRole);

    /**
     * 分页搜索角色
     * @param page 分页参数
     * @param keyword 关键词
     * @return 分页结果
     */
    IPage<Role> searchRolesWithPagination(Page<Role> page, String keyword);

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
     * @param updatedBy 更新人ID
     * @return 是否分配成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds, Long updatedBy);

    /**
     * 移除角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @param updatedBy 更新人ID
     * @return 是否移除成功
     */
    boolean removePermissions(Long roleId, List<Long> permissionIds, Long updatedBy);

    /**
     * 检查角色是否有指定权限
     * @param roleId 角色ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean checkRolePermissionByCode(Long roleId, String permissionCode);

    /**
     * 获取拥有指定权限的角色列表
     * @param permissionCode 权限编码
     * @return 角色列表
     */
    List<Role> getRolesByPermissionCode(String permissionCode);

    /**
     * 获取角色权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 获取角色权限编码列表
     * @param roleId 角色ID
     * @return 权限编码列表
     */
    List<String> getRolePermissionCodes(Long roleId);

    /**
     * 检查角色是否拥有权限（通过权限ID）
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否拥有权限
     */
    boolean checkRolePermission(Long roleId, Long permissionId);

    /**
     * 根据权限ID查找拥有该权限的角色
     * @param permissionId 权限ID
     * @return 角色列表
     */
    List<Role> getRolesByPermission(Long permissionId);

    /**
     * 获取角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    Long getRoleUserCount(Long roleId);

    /**
     * 获取角色的用户ID列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getRoleUserIds(Long roleId);

    /**
     * 获取角色下的用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<com.archive.management.entity.User> getRoleUsers(Long roleId);

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 获取用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 获取用户的角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);

    /**
     * 检查用户是否拥有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否拥有角色
     */
    boolean checkUserHasRole(Long userId, Long roleId);

    /**
     * 检查用户是否拥有指定角色编码
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否拥有角色
     */
    boolean checkUserHasRoleByCode(Long userId, String roleCode);

    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否分配成功
     */
    boolean assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否移除成功
     */
    boolean removeUserRoles(Long userId, List<Long> roleIds);

    /**
     * 清空用户所有角色
     * @param userId 用户ID
     * @return 是否清空成功
     */
    boolean clearUserRoles(Long userId);

    /**
     * 复制角色
     * @param sourceRoleId 源角色ID
     * @param newRoleCode 新角色编码
     * @param newRoleName 新角色名称
     * @return 新创建的角色
     */
    Role copyRole(Long sourceRoleId, String newRoleCode, String newRoleName);

    /**
     * 导出角色数据
     * @param ids 角色ID列表（可选，为空则导出所有）
     * @param userId 用户ID（用于权限过滤）
     * @return 角色列表
     */
    List<Role> exportRoles(List<Long> ids, Long userId);

    /**
     * 批量导入角色
     * @param roles 角色列表
     * @param userId 用户ID（导入人）
     * @return 导入的角色列表
     */
    List<Role> importRoles(List<Role> roles, Long userId);

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
     * 获取角色树
     * @return 角色树
     */
    List<Role> getRoleTree();

    /**
     * 获取角色的子角色列表
     * @param roleId 父角色ID
     * @return 子角色列表
     */
    List<Role> getChildRoles(Long roleId);

    /**
     * 获取角色的父角色列表
     * @param roleId 角色ID
     * @return 父角色列表
     */
    List<Role> getParentRoles(Long roleId);

    /**
     * 获取角色的父角色
     * @param roleId 角色ID
     * @return 父角色
     */
    Role getParentRole(Long roleId);

    /**
     * 获取角色的所有子孙角色
     * @param roleId 角色ID
     * @return 子孙角色列表
     */
    List<Role> getDescendantRoles(Long roleId);

    /**
     * 获取角色的所有祖先角色
     * @param roleId 角色ID
     * @return 祖先角色列表
     */
    List<Role> getAncestorRoles(Long roleId);

    /**
     * 设置角色的父角色
     * @param roleId 角色ID
     * @param parentRoleId 父角色ID
     * @return 是否设置成功
     */
    boolean setParentRole(Long roleId, Long parentRoleId);

    /**
     * 移除角色的父角色关系
     * @param roleId 角色ID
     * @return 是否移除成功
     */
    boolean removeParentRole(Long roleId);

    /**
     * 获取角色权限树
     * @param roleId 角色ID
     * @return 权限树
     */
    List<Map<String, Object>> getRolePermissionTree(Long roleId);

    /**
     * 同步角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void syncPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 清理无效的角色关联
     */
    void cleanInvalidAssociations();

    /**
     * 生成角色报告
     * @return 报告数据
     */
    Map<String, Object> generateRoleReport();

    /**
     * 获取角色使用统计
     * @return 使用统计结果
     */
    Map<String, Object> getRoleUsageStatistics();

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
     * 获取推荐的角色权限配置
     * @param roleType 角色类型
     * @return 推荐的权限ID列表
     */
    List<Long> getRecommendedPermissions(String roleType);
}