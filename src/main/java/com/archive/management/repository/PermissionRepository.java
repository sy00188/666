package com.archive.management.repository;

import com.archive.management.entity.Permission;
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
 * 权限数据访问层接口
 * 提供权限实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    /**
     * 根据权限编码查找权限
     * @param code 权限编码
     * @return 权限信息
     */
    Optional<Permission> findByCode(String code);

    /**
     * 根据权限名称查找权限
     * @param name 权限名称
     * @return 权限信息
     */
    Optional<Permission> findByName(String name);

    /**
     * 检查权限编码是否存在
     * @param code 权限编码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查权限名称是否存在
     * @param name 权限名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据父权限ID查找子权限列表
     * @param parentId 父权限ID
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByParentId(Long parentId, Pageable pageable);

    /**
     * 查找所有根权限（父权限ID为空）
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByParentIdIsNull(Pageable pageable);

    /**
     * 根据权限状态查找权限列表
     * @param status 权限状态
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据权限类型查找权限列表
     * @param type 权限类型
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByType(String type, Pageable pageable);

    /**
     * 根据权限级别查找权限列表
     * @param level 权限级别
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByLevel(Integer level, Pageable pageable);

    /**
     * 模糊查询权限（根据名称、描述）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    @Query("SELECT p FROM Permission p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Permission> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据权限名称模糊查询
     * @param name 权限名称
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByNameContaining(String name, Pageable pageable);

    /**
     * 查找指定时间范围内创建的权限
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据排序号排序查找权限
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 查找指定父权限下的所有子权限（按排序号排序）
     * @param parentId 父权限ID
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId = :parentId ORDER BY p.sortOrder ASC")
    List<Permission> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找所有根权限（按排序号排序）
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId IS NULL ORDER BY p.sortOrder ASC")
    List<Permission> findAllRootPermissions();

    /**
     * 查找指定权限的所有祖先权限
     * @param permissionId 权限ID
     * @return 祖先权限列表
     */
    @Query(value = "WITH RECURSIVE permission_ancestors AS (" +
           "  SELECT id, parent_id, name, code, level FROM permission WHERE id = :permissionId " +
           "  UNION ALL " +
           "  SELECT p.id, p.parent_id, p.name, p.code, p.level " +
           "  FROM permission p " +
           "  INNER JOIN permission_ancestors pa ON p.id = pa.parent_id " +
           ") " +
           "SELECT * FROM permission_ancestors WHERE id != :permissionId ORDER BY level", 
           nativeQuery = true)
    List<Permission> findAncestors(@Param("permissionId") Long permissionId);

    /**
     * 查找指定权限的所有后代权限
     * @param permissionId 权限ID
     * @return 后代权限列表
     */
    @Query(value = "WITH RECURSIVE permission_descendants AS (" +
           "  SELECT id, parent_id, name, code, level FROM permission WHERE parent_id = :permissionId " +
           "  UNION ALL " +
           "  SELECT p.id, p.parent_id, p.name, p.code, p.level " +
           "  FROM permission p " +
           "  INNER JOIN permission_descendants pd ON p.parent_id = pd.id " +
           ") " +
           "SELECT * FROM permission_descendants ORDER BY level, sort_order", 
           nativeQuery = true)
    List<Permission> findDescendants(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID查找角色拥有的权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "WHERE rp.roleId = :roleId AND p.status = 1")
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找用户拥有的权限
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "JOIN UserRole ur ON rp.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND p.status = 1")
    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据权限编码列表查找权限
     * @param codes 权限编码列表
     * @return 权限列表
     */
    List<Permission> findByCodeIn(List<String> codes);

    /**
     * 查找所有启用状态的权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.status = 1 ORDER BY p.sortOrder ASC")
    List<Permission> findAllActivePermissions();

    /**
     * 查找系统内置权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.isSystem = true ORDER BY p.sortOrder ASC")
    List<Permission> findSystemPermissions();

    /**
     * 查找非系统内置权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.isSystem = false ORDER BY p.sortOrder ASC")
    List<Permission> findCustomPermissions();

    /**
     * 查找菜单类型权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.type = 'MENU' AND p.status = 1 ORDER BY p.sortOrder ASC")
    List<Permission> findMenuPermissions();

    /**
     * 查找按钮类型权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.type = 'BUTTON' AND p.status = 1 ORDER BY p.sortOrder ASC")
    List<Permission> findButtonPermissions();

    /**
     * 查找API类型权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.type = 'API' AND p.status = 1 ORDER BY p.sortOrder ASC")
    List<Permission> findApiPermissions();

    /**
     * 统计权限总数
     * @return 权限总数
     */
    @Query("SELECT COUNT(p) FROM Permission p")
    long countAllPermissions();

    /**
     * 统计指定状态的权限数量
     * @param status 权限状态
     * @return 权限数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定类型的权限数量
     * @param type 权限类型
     * @return 权限数量
     */
    long countByType(String type);

    /**
     * 统计指定级别的权限数量
     * @param level 权限级别
     * @return 权限数量
     */
    long countByLevel(Integer level);

    /**
     * 统计指定父权限下的子权限数量
     * @param parentId 父权限ID
     * @return 子权限数量
     */
    long countByParentId(Long parentId);

    /**
     * 统计根权限数量
     * @return 根权限数量
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.parentId IS NULL")
    long countRootPermissions();

    /**
     * 统计系统内置权限数量
     * @return 权限数量
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.isSystem = true")
    long countSystemPermissions();

    /**
     * 统计自定义权限数量
     * @return 权限数量
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.isSystem = false")
    long countCustomPermissions();

    /**
     * 统计指定时间范围内创建的权限数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 权限数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取指定权限的角色数量
     * @param permissionId 权限ID
     * @return 角色数量
     */
    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    long getRoleCount(@Param("permissionId") Long permissionId);

    /**
     * 检查权限是否有子权限
     * @param permissionId 权限ID
     * @return 是否有子权限
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.parentId = :permissionId")
    boolean hasChildren(@Param("permissionId") Long permissionId);

    /**
     * 检查权限是否被角色使用
     * @param permissionId 权限ID
     * @return 是否被使用
     */
    @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp WHERE rp.permissionId = :permissionId")
    boolean isUsedByRoles(@Param("permissionId") Long permissionId);

    /**
     * 根据权限ID列表批量查询权限
     * @param permissionIds 权限ID列表
     * @return 权限列表
     */
    List<Permission> findByIdIn(List<Long> permissionIds);

    /**
     * 批量更新权限状态
     * @param permissionIds 权限ID列表
     * @param status 新状态
     */
    @Query("UPDATE Permission p SET p.status = :status WHERE p.id IN :permissionIds")
    void batchUpdateStatus(@Param("permissionIds") List<Long> permissionIds, @Param("status") Integer status);

    /**
     * 更新权限的排序号
     * @param permissionId 权限ID
     * @param sortOrder 新排序号
     */
    @Query("UPDATE Permission p SET p.sortOrder = :sortOrder WHERE p.id = :permissionId")
    void updateSortOrder(@Param("permissionId") Long permissionId, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取指定父权限下的最大排序号
     * @param parentId 父权限ID
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM Permission p WHERE p.parentId = :parentId")
    Integer getMaxSortOrderByParent(@Param("parentId") Long parentId);

    /**
     * 获取根权限的最大排序号
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM Permission p WHERE p.parentId IS NULL")
    Integer getMaxSortOrderForRoot();

    /**
     * 软删除权限（更新删除标记）
     * @param permissionId 权限ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE Permission p SET p.deleted = true, p.deletedBy = :deletedBy, p.deleteTime = :deleteTime WHERE p.id = :permissionId")
    void softDeletePermission(@Param("permissionId") Long permissionId, 
                             @Param("deletedBy") Long deletedBy, 
                             @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的权限
     * @param pageable 分页参数
     * @return 权限分页列表
     */
    Page<Permission> findByDeletedFalse(Pageable pageable);

    /**
     * 根据权限编码查找未删除的权限
     * @param code 权限编码
     * @return 权限信息
     */
    Optional<Permission> findByCodeAndDeletedFalse(String code);

    /**
     * 根据权限名称查找未删除的权限
     * @param name 权限名称
     * @return 权限信息
     */
    Optional<Permission> findByNameAndDeletedFalse(String name);

    /**
     * 查找未删除的根权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId IS NULL AND p.deleted = false ORDER BY p.sortOrder ASC")
    List<Permission> findRootPermissionsNotDeleted();

    /**
     * 查找指定父权限下未删除的子权限
     * @param parentId 父权限ID
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId = :parentId AND p.deleted = false ORDER BY p.sortOrder ASC")
    List<Permission> findChildrenNotDeleted(@Param("parentId") Long parentId);

    /**
     * 按级别统计权限数量
     * @return 级别统计结果
     */
    @Query("SELECT p.level as level, COUNT(p) as count FROM Permission p GROUP BY p.level ORDER BY level")
    List<Object[]> countPermissionsByLevel();

    /**
     * 按状态统计权限数量
     * @return 状态统计结果
     */
    @Query("SELECT p.status as status, COUNT(p) as count FROM Permission p GROUP BY p.status ORDER BY status")
    List<Object[]> countPermissionsByStatus();

    /**
     * 按类型统计权限数量
     * @return 类型统计结果
     */
    @Query("SELECT p.type as type, COUNT(p) as count FROM Permission p GROUP BY p.type ORDER BY type")
    List<Object[]> countPermissionsByType();

    /**
     * 获取权限树结构（包含角色数量）
     * @return 权限树统计结果
     */
    @Query("SELECT p.id, p.name, p.parentId, p.level, COUNT(rp.roleId) as roleCount " +
           "FROM Permission p LEFT JOIN RolePermission rp ON p.id = rp.permissionId " +
           "WHERE p.deleted = false " +
           "GROUP BY p.id, p.name, p.parentId, p.level " +
           "ORDER BY p.level, p.sortOrder")
    List<Object[]> getPermissionTreeWithRoleCount();

    /**
     * 根据URL路径查找权限
     * @param path URL路径
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.path = :path AND p.status = 1")
    List<Permission> findByPath(@Param("path") String path);

    /**
     * 根据URL路径模糊查找权限
     * @param pathPattern URL路径模式
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.path LIKE :pathPattern AND p.status = 1")
    List<Permission> findByPathLike(@Param("pathPattern") String pathPattern);

    /**
     * 检查权限编码是否已被其他权限使用（排除指定权限）
     * @param code 权限编码
     * @param excludeId 排除的权限ID
     * @return 是否已被使用
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.code = :code AND p.id != :excludeId")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 检查权限名称是否已被其他权限使用（排除指定权限）
     * @param name 权限名称
     * @param excludeId 排除的权限ID
     * @return 是否已被使用
     */
    @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.name = :name AND p.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 查找档案管理相关权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.code LIKE 'ARCHIVE_%' AND p.status = 1 AND p.deleted = false ORDER BY p.sortOrder")
    List<Permission> findArchivePermissions();

    /**
     * 查找系统管理相关权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.code LIKE 'SYSTEM_%' AND p.status = 1 AND p.deleted = false ORDER BY p.sortOrder")
    List<Permission> findSystemManagementPermissions();

    /**
     * 查找用户管理相关权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.code LIKE 'USER_%' AND p.status = 1 AND p.deleted = false ORDER BY p.sortOrder")
    List<Permission> findUserPermissions();

    /**
     * 获取权限使用统计
     * @return 权限使用统计结果
     */
    @Query("SELECT p.id, p.name, p.code, p.type, COUNT(rp.roleId) as roleCount " +
           "FROM Permission p " +
           "LEFT JOIN RolePermission rp ON p.id = rp.permissionId " +
           "WHERE p.deleted = false " +
           "GROUP BY p.id, p.name, p.code, p.type " +
           "ORDER BY roleCount DESC, p.sortOrder")
    List<Object[]> getPermissionUsageStatistics();

    /**
     * 查找未被任何角色使用的权限
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p " +
           "WHERE p.deleted = false " +
           "AND NOT EXISTS (SELECT 1 FROM RolePermission rp WHERE rp.permissionId = p.id) " +
           "ORDER BY p.sortOrder")
    List<Permission> findUnusedPermissions();

    /**
     * 查找被多个角色使用的权限
     * @param minRoleCount 最小角色数量
     * @return 权限列表
     */
    @Query("SELECT p FROM Permission p " +
           "WHERE p.deleted = false " +
           "AND (SELECT COUNT(rp.roleId) FROM RolePermission rp WHERE rp.permissionId = p.id) >= :minRoleCount " +
           "ORDER BY p.sortOrder")
    List<Permission> findPermissionsUsedByMultipleRoles(@Param("minRoleCount") int minRoleCount);
}