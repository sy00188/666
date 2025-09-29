package com.archive.management.repository;

import com.archive.management.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问层接口
 * 提供角色实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    /**
     * 根据角色编码查找角色
     * @param code 角色编码
     * @return 角色信息
     */
    Optional<Role> findByCode(String code);

    /**
     * 根据角色名称查找角色
     * @param name 角色名称
     * @return 角色信息
     */
    Optional<Role> findByName(String name);

    /**
     * 检查角色编码是否存在
     * @param code 角色编码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查角色名称是否存在
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据角色状态查找角色列表
     * @param status 角色状态
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据角色类型查找角色列表
     * @param type 角色类型
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByType(String type, Pageable pageable);

    /**
     * 根据数据权限范围查找角色列表
     * @param dataScope 数据权限范围
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByDataScope(String dataScope, Pageable pageable);

    /**
     * 模糊查询角色（根据名称、描述）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE %:keyword% OR r.description LIKE %:keyword%")
    Page<Role> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据角色名称模糊查询
     * @param name 角色名称
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByNameContaining(String name, Pageable pageable);

    /**
     * 查找指定时间范围内创建的角色
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据排序号排序查找角色
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 查找所有启用状态的角色
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 1 ORDER BY r.sortOrder ASC")
    List<Role> findAllActiveRoles();

    /**
     * 根据用户ID查找用户拥有的角色
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r " +
           "JOIN UserRole ur ON r.id = ur.roleId " +
           "WHERE ur.userId = :userId AND r.status = 1")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据权限ID查找拥有该权限的角色
     * @param permissionId 权限ID
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r " +
           "JOIN RolePermission rp ON r.id = rp.roleId " +
           "WHERE rp.permissionId = :permissionId AND r.status = 1")
    List<Role> findRolesByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据权限编码查找拥有该权限的角色
     * @param permissionCode 权限编码
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r " +
           "JOIN RolePermission rp ON r.id = rp.roleId " +
           "JOIN Permission p ON rp.permissionId = p.id " +
           "WHERE p.code = :permissionCode AND r.status = 1")
    List<Role> findRolesByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查找系统内置角色
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.isSystem = true ORDER BY r.sortOrder ASC")
    List<Role> findSystemRoles();

    /**
     * 查找非系统内置角色
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.isSystem = false ORDER BY r.sortOrder ASC")
    List<Role> findCustomRoles();

    /**
     * 统计角色总数
     * @return 角色总数
     */
    @Query("SELECT COUNT(r) FROM Role r")
    long countAllRoles();

    /**
     * 统计指定状态的角色数量
     * @param status 角色状态
     * @return 角色数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定类型的角色数量
     * @param type 角色类型
     * @return 角色数量
     */
    long countByType(String type);

    /**
     * 统计指定数据权限范围的角色数量
     * @param dataScope 数据权限范围
     * @return 角色数量
     */
    long countByDataScope(String dataScope);

    /**
     * 统计系统内置角色数量
     * @return 角色数量
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.isSystem = true")
    long countSystemRoles();

    /**
     * 统计自定义角色数量
     * @return 角色数量
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.isSystem = false")
    long countCustomRoles();

    /**
     * 统计指定时间范围内创建的角色数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 角色数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取指定角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.roleId = :roleId")
    long getUserCount(@Param("roleId") Long roleId);

    /**
     * 获取指定角色的权限数量
     * @param roleId 角色ID
     * @return 权限数量
     */
    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.roleId = :roleId")
    long getPermissionCount(@Param("roleId") Long roleId);

    /**
     * 检查角色是否有用户
     * @param roleId 角色ID
     * @return 是否有用户
     */
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.roleId = :roleId")
    boolean hasUsers(@Param("roleId") Long roleId);

    /**
     * 检查角色是否有权限
     * @param roleId 角色ID
     * @return 是否有权限
     */
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.roleId = :roleId")
    boolean hasPermissions(@Param("roleId") Long roleId);

    /**
     * 根据角色ID列表批量查询角色
     * @param roleIds 角色ID列表
     * @return 角色列表
     */
    List<Role> findByIdIn(List<Long> roleIds);

    /**
     * 批量更新角色状态
     * @param roleIds 角色ID列表
     * @param status 新状态
     */
    @Query("UPDATE Role r SET r.status = :status WHERE r.id IN :roleIds")
    void batchUpdateStatus(@Param("roleIds") List<Long> roleIds, @Param("status") Integer status);

    /**
     * 更新角色的排序号
     * @param roleId 角色ID
     * @param sortOrder 新排序号
     */
    @Query("UPDATE Role r SET r.sortOrder = :sortOrder WHERE r.id = :roleId")
    void updateSortOrder(@Param("roleId") Long roleId, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取最大排序号
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(r.sortOrder), 0) FROM Role r")
    Integer getMaxSortOrder();

    /**
     * 软删除角色（更新删除标记）
     * @param roleId 角色ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE Role r SET r.deleted = true, r.deletedBy = :deletedBy, r.deleteTime = :deleteTime WHERE r.id = :roleId")
    void softDeleteRole(@Param("roleId") Long roleId, 
                       @Param("deletedBy") Long deletedBy, 
                       @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的角色
     * @param pageable 分页参数
     * @return 角色分页列表
     */
    Page<Role> findByDeletedFalse(Pageable pageable);

    /**
     * 根据角色编码查找未删除的角色
     * @param code 角色编码
     * @return 角色信息
     */
    Optional<Role> findByCodeAndDeletedFalse(String code);

    /**
     * 根据角色名称查找未删除的角色
     * @param name 角色名称
     * @return 角色信息
     */
    Optional<Role> findByNameAndDeletedFalse(String name);

    /**
     * 查找未删除的启用角色
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.deleted = false AND r.status = 1 ORDER BY r.sortOrder ASC")
    List<Role> findActiveRolesNotDeleted();

    /**
     * 按状态统计角色数量
     * @return 状态统计结果
     */
    @Query("SELECT r.status as status, COUNT(r) as count FROM Role r GROUP BY r.status ORDER BY status")
    List<Object[]> countRolesByStatus();

    /**
     * 按类型统计角色数量
     * @return 类型统计结果
     */
    @Query("SELECT r.type as type, COUNT(r) as count FROM Role r GROUP BY r.type ORDER BY type")
    List<Object[]> countRolesByType();

    /**
     * 按数据权限范围统计角色数量
     * @return 数据权限范围统计结果
     */
    @Query("SELECT r.dataScope as dataScope, COUNT(r) as count FROM Role r GROUP BY r.dataScope ORDER BY dataScope")
    List<Object[]> countRolesByDataScope();

    /**
     * 获取角色权限统计信息
     * @return 角色权限统计结果
     */
    @Query("SELECT r.id, r.name, r.code, COUNT(rp.permissionId) as permissionCount, COUNT(ur.userId) as userCount " +
           "FROM Role r " +
           "LEFT JOIN RolePermission rp ON r.id = rp.roleId " +
           "LEFT JOIN UserRole ur ON r.id = ur.roleId " +
           "WHERE r.deleted = false " +
           "GROUP BY r.id, r.name, r.code " +
           "ORDER BY r.sortOrder")
    List<Object[]> getRoleStatistics();

    /**
     * 查找拥有指定权限的所有角色
     * @param permissionCodes 权限编码列表
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r " +
           "JOIN RolePermission rp ON r.id = rp.roleId " +
           "JOIN Permission p ON rp.permissionId = p.id " +
           "WHERE p.code IN :permissionCodes AND r.status = 1 AND r.deleted = false")
    List<Role> findRolesByPermissionCodes(@Param("permissionCodes") List<String> permissionCodes);

    /**
     * 查找指定用户可以分配的角色（基于数据权限）
     * @param userId 用户ID
     * @param userDataScope 用户数据权限范围
     * @return 角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 1 AND r.deleted = false " +
           "AND (r.dataScope = 'ALL' OR r.dataScope = :userDataScope OR r.dataScope = 'SELF') " +
           "ORDER BY r.sortOrder ASC")
    List<Role> findAssignableRoles(@Param("userId") Long userId, @Param("userDataScope") String userDataScope);

    /**
     * 查找管理员角色
     * @return 管理员角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.code LIKE '%ADMIN%' AND r.status = 1 AND r.deleted = false ORDER BY r.sortOrder ASC")
    List<Role> findAdminRoles();

    /**
     * 查找普通用户角色
     * @return 普通用户角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.code NOT LIKE '%ADMIN%' AND r.status = 1 AND r.deleted = false ORDER BY r.sortOrder ASC")
    List<Role> findUserRoles();

    /**
     * 根据部门ID查找该部门用户拥有的所有角色
     * @param departmentId 部门ID
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r " +
           "JOIN UserRole ur ON r.id = ur.roleId " +
           "JOIN User u ON ur.userId = u.id " +
           "WHERE u.departmentId = :departmentId AND r.status = 1 AND r.deleted = false")
    List<Role> findRolesByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 查找有档案管理权限的角色
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r " +
           "JOIN RolePermission rp ON r.id = rp.roleId " +
           "JOIN Permission p ON rp.permissionId = p.id " +
           "WHERE p.code LIKE 'ARCHIVE_%' AND r.status = 1 AND r.deleted = false")
    List<Role> findArchiveManagementRoles();

    /**
     * 查找有系统管理权限的角色
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r " +
           "JOIN RolePermission rp ON r.id = rp.roleId " +
           "JOIN Permission p ON rp.permissionId = p.id " +
           "WHERE p.code LIKE 'SYSTEM_%' AND r.status = 1 AND r.deleted = false")
    List<Role> findSystemManagementRoles();

    /**
     * 检查角色编码是否已被其他角色使用（排除指定角色）
     * @param code 角色编码
     * @param excludeId 排除的角色ID
     * @return 是否已被使用
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.code = :code AND r.id != :excludeId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 检查角色名称是否已被其他角色使用（排除指定角色）
     * @param name 角色名称
     * @param excludeId 排除的角色ID
     * @return 是否已被使用
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = :name AND r.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 获取角色层级关系（如果有父子关系）
     * @return 角色层级统计结果
     */
    @Query("SELECT r.id, r.name, r.code, r.type, COUNT(ur.userId) as userCount " +
           "FROM Role r " +
           "LEFT JOIN UserRole ur ON r.id = ur.roleId " +
           "WHERE r.deleted = false " +
           "GROUP BY r.id, r.name, r.code, r.type " +
           "ORDER BY r.type, r.sortOrder")
    List<Object[]> getRoleHierarchy();
}