package com.archive.management.repository;

import com.archive.management.entity.Department;
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
 * 部门数据访问层接口
 * 提供部门实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    /**
     * 根据部门编码查找部门
     * @param code 部门编码
     * @return 部门信息
     */
    Optional<Department> findByCode(String code);

    /**
     * 根据部门名称查找部门
     * @param name 部门名称
     * @return 部门信息
     */
    Optional<Department> findByName(String name);

    /**
     * 检查部门编码是否存在
     * @param code 部门编码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查部门名称是否存在（同级别下）
     * @param name 部门名称
     * @param parentId 父部门ID
     * @return 是否存在
     */
    boolean existsByNameAndParentId(String name, Long parentId);

    /**
     * 检查部门名称是否存在（根部门）
     * @param name 部门名称
     * @return 是否存在
     */
    boolean existsByNameAndParentIdIsNull(String name);

    /**
     * 检查部门编码是否存在（排除指定ID）
     * @param code 部门编码
     * @param id 排除的部门ID
     * @return 是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * 检查部门名称是否存在（排除指定ID）
     * @param name 部门名称
     * @param id 排除的部门ID
     * @return 是否存在
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 查找未删除的部门
     * @return 部门列表
     */
    List<Department> findByDeletedFalse();

    /**
     * 根据父部门ID查找子部门列表
     * @param parentId 父部门ID
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByParentId(Long parentId, Pageable pageable);

    /**
     * 查找所有根部门（父部门ID为空）
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByParentIdIsNull(Pageable pageable);

    /**
     * 根据部门状态查找部门列表
     * @param status 部门状态
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据部门级别查找部门列表
     * @param level 部门级别
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByLevel(Integer level, Pageable pageable);

    /**
     * 根据部门类型查找部门列表
     * @param type 部门类型
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByType(String type, Pageable pageable);

    /**
     * 模糊查询部门（根据名称、描述）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    @Query("SELECT d FROM Department d WHERE d.name LIKE %:keyword% OR d.description LIKE %:keyword%")
    Page<Department> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据部门名称模糊查询
     * @param name 部门名称
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByNameContaining(String name, Pageable pageable);

    /**
     * 查找指定时间范围内创建的部门
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据排序号排序查找部门
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByOrderBySortOrderAsc(Pageable pageable);

    /**
     * 查找指定父部门下的所有子部门（按排序号排序）
     * @param parentId 父部门ID
     * @return 部门列表
     */
    @Query("SELECT d FROM Department d WHERE d.parentId = :parentId ORDER BY d.sortOrder ASC")
    List<Department> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找所有根部门（按排序号排序）
     * @return 部门列表
     */
    @Query("SELECT d FROM Department d WHERE d.parentId IS NULL ORDER BY d.sortOrder ASC")
    List<Department> findAllRootDepartments();

    /**
     * 查找指定部门的所有祖先部门
     * @param departmentId 部门ID
     * @return 祖先部门列表
     */
    @Query(value = "WITH RECURSIVE department_ancestors AS (" +
           "  SELECT id, parent_id, name, code, level FROM department WHERE id = :departmentId " +
           "  UNION ALL " +
           "  SELECT d.id, d.parent_id, d.name, d.code, d.level " +
           "  FROM department d " +
           "  INNER JOIN department_ancestors da ON d.id = da.parent_id " +
           ") " +
           "SELECT * FROM department_ancestors WHERE id != :departmentId ORDER BY level", 
           nativeQuery = true)
    List<Department> findAncestors(@Param("departmentId") Long departmentId);

    /**
     * 查找指定部门的所有后代部门
     * @param departmentId 部门ID
     * @return 后代部门列表
     */
    @Query(value = "WITH RECURSIVE department_descendants AS (" +
           "  SELECT id, parent_id, name, code, level FROM department WHERE parent_id = :departmentId " +
           "  UNION ALL " +
           "  SELECT d.id, d.parent_id, d.name, d.code, d.level " +
           "  FROM department d " +
           "  INNER JOIN department_descendants dd ON d.parent_id = dd.id " +
           ") " +
           "SELECT * FROM department_descendants ORDER BY level, sort_order", 
           nativeQuery = true)
    List<Department> findDescendants(@Param("departmentId") Long departmentId);

    /**
     * 根据负责人ID查找部门
     * @param managerId 负责人ID
     * @return 部门列表
     */
    List<Department> findByManagerId(Long managerId);

    /**
     * 根据联系人查找部门
     * @param contact 联系人
     * @return 部门列表
     */
    List<Department> findByContact(String contact);

    /**
     * 根据联系电话查找部门
     * @param phone 联系电话
     * @return 部门信息
     */
    Optional<Department> findByPhone(String phone);

    /**
     * 根据邮箱查找部门
     * @param email 邮箱
     * @return 部门信息
     */
    Optional<Department> findByEmail(String email);

    /**
     * 统计部门总数
     * @return 部门总数
     */
    @Query("SELECT COUNT(d) FROM Department d")
    long countAllDepartments();

    /**
     * 统计指定状态的部门数量
     * @param status 部门状态
     * @return 部门数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定级别的部门数量
     * @param level 部门级别
     * @return 部门数量
     */
    long countByLevel(Integer level);

    /**
     * 统计指定类型的部门数量
     * @param type 部门类型
     * @return 部门数量
     */
    long countByType(String type);

    /**
     * 统计指定父部门下的子部门数量
     * @param parentId 父部门ID
     * @return 子部门数量
     */
    long countByParentId(Long parentId);

    /**
     * 统计根部门数量
     * @return 根部门数量
     */
    @Query("SELECT COUNT(d) FROM Department d WHERE d.parentId IS NULL")
    long countRootDepartments();

    /**
     * 统计指定时间范围内创建的部门数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 部门数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 检查部门是否有子部门
     * @param departmentId 部门ID
     * @return 是否有子部门
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.parentId = :departmentId")
    boolean hasChildren(@Param("departmentId") Long departmentId);

    /**
     * 检查部门是否有用户
     * @param departmentId 部门ID
     * @return 是否有用户
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.departmentId = :departmentId")
    boolean hasUsers(@Param("departmentId") Long departmentId);

    /**
     * 获取指定部门下的用户数量
     * @param departmentId 部门ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.departmentId = :departmentId")
    long getUserCount(@Param("departmentId") Long departmentId);

    /**
     * 获取指定部门及其所有子部门下的用户数量
     * @param departmentId 部门ID
     * @return 用户数量
     */
    @Query(value = "WITH RECURSIVE department_tree AS (" +
           "  SELECT id FROM department WHERE id = :departmentId " +
           "  UNION ALL " +
           "  SELECT d.id FROM department d " +
           "  INNER JOIN department_tree dt ON d.parent_id = dt.id " +
           ") " +
           "SELECT COUNT(u.id) FROM user u " +
           "WHERE u.department_id IN (SELECT id FROM department_tree)", 
           nativeQuery = true)
    long getTotalUserCount(@Param("departmentId") Long departmentId);

    /**
     * 根据部门ID列表批量查询部门
     * @param departmentIds 部门ID列表
     * @return 部门列表
     */
    List<Department> findByIdIn(List<Long> departmentIds);

    /**
     * 批量更新部门状态
     * @param departmentIds 部门ID列表
     * @param status 新状态
     */
    @Query("UPDATE Department d SET d.status = :status WHERE d.id IN :departmentIds")
    void batchUpdateStatus(@Param("departmentIds") List<Long> departmentIds, @Param("status") Integer status);

    /**
     * 更新部门的排序号
     * @param departmentId 部门ID
     * @param sortOrder 新排序号
     */
    @Query("UPDATE Department d SET d.sortOrder = :sortOrder WHERE d.id = :departmentId")
    void updateSortOrder(@Param("departmentId") Long departmentId, @Param("sortOrder") Integer sortOrder);

    /**
     * 更新部门负责人
     * @param departmentId 部门ID
     * @param managerId 新负责人ID
     */
    @Query("UPDATE Department d SET d.managerId = :managerId WHERE d.id = :departmentId")
    void updateManager(@Param("departmentId") Long departmentId, @Param("managerId") Long managerId);

    /**
     * 获取指定父部门下的最大排序号
     * @param parentId 父部门ID
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(d.sortOrder), 0) FROM Department d WHERE d.parentId = :parentId")
    Integer getMaxSortOrderByParent(@Param("parentId") Long parentId);

    /**
     * 获取根部门的最大排序号
     * @return 最大排序号
     */
    @Query("SELECT COALESCE(MAX(d.sortOrder), 0) FROM Department d WHERE d.parentId IS NULL")
    Integer getMaxSortOrderForRoot();

    /**
     * 软删除部门（更新删除标记）
     * @param departmentId 部门ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE Department d SET d.deleted = true, d.deletedBy = :deletedBy, d.deleteTime = :deleteTime WHERE d.id = :departmentId")
    void softDeleteDepartment(@Param("departmentId") Long departmentId, 
                             @Param("deletedBy") Long deletedBy, 
                             @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的部门
     * @param pageable 分页参数
     * @return 部门分页列表
     */
    Page<Department> findByDeletedFalse(Pageable pageable);

    /**
     * 根据部门编码查找未删除的部门
     * @param code 部门编码
     * @return 部门信息
     */
    Optional<Department> findByCodeAndDeletedFalse(String code);

    /**
     * 根据部门名称查找未删除的部门
     * @param name 部门名称
     * @return 部门信息
     */
    Optional<Department> findByNameAndDeletedFalse(String name);

    /**
     * 查找未删除的根部门
     * @return 部门列表
     */
    @Query("SELECT d FROM Department d WHERE d.parentId IS NULL AND d.deleted = false ORDER BY d.sortOrder ASC")
    List<Department> findRootDepartmentsNotDeleted();

    /**
     * 查找指定父部门下未删除的子部门
     * @param parentId 父部门ID
     * @return 部门列表
     */
    @Query("SELECT d FROM Department d WHERE d.parentId = :parentId AND d.deleted = false ORDER BY d.sortOrder ASC")
    List<Department> findChildrenNotDeleted(@Param("parentId") Long parentId);

    /**
     * 按级别统计部门数量
     * @return 级别统计结果
     */
    @Query("SELECT d.level as level, COUNT(d) as count FROM Department d GROUP BY d.level ORDER BY level")
    List<Object[]> countDepartmentsByLevel();

    /**
     * 按状态统计部门数量
     * @return 状态统计结果
     */
    @Query("SELECT d.status as status, COUNT(d) as count FROM Department d GROUP BY d.status ORDER BY status")
    List<Object[]> countDepartmentsByStatus();

    /**
     * 按类型统计部门数量
     * @return 类型统计结果
     */
    @Query("SELECT d.type as type, COUNT(d) as count FROM Department d GROUP BY d.type ORDER BY type")
    List<Object[]> countDepartmentsByType();

    /**
     * 获取部门树结构（包含用户数量）
     * @return 部门树统计结果
     */
    @Query("SELECT d.id, d.name, d.parentId, d.level, COUNT(u.id) as userCount " +
           "FROM Department d LEFT JOIN User u ON d.id = u.departmentId " +
           "WHERE d.deleted = false " +
           "GROUP BY d.id, d.name, d.parentId, d.level " +
           "ORDER BY d.level, d.sortOrder")
    List<Object[]> getDepartmentTreeWithUserCount();

    /**
     * 查找有权限访问指定档案的部门列表
     * @param archiveId 档案ID
     * @return 部门列表
     */
    @Query("SELECT DISTINCT d FROM Department d " +
           "JOIN User u ON d.id = u.departmentId " +
           "JOIN UserRole ur ON u.id = ur.userId " +
           "JOIN Role r ON ur.roleId = r.id " +
           "JOIN RolePermission rp ON r.id = rp.roleId " +
           "JOIN Permission p ON rp.permissionId = p.id " +
           "WHERE p.code = 'ARCHIVE_VIEW' OR p.code = 'ARCHIVE_MANAGE'")
    List<Department> findDepartmentsWithArchiveAccess(@Param("archiveId") Long archiveId);

    /**
     * 查找指定用户所在部门的所有上级部门
     * @param userId 用户ID
     * @return 上级部门列表
     */
    @Query(value = "WITH RECURSIVE parent_departments AS (" +
           "  SELECT d.id, d.parent_id, d.name, d.code, d.level " +
           "  FROM department d " +
           "  JOIN user u ON d.id = u.department_id " +
           "  WHERE u.id = :userId " +
           "  UNION ALL " +
           "  SELECT d.id, d.parent_id, d.name, d.code, d.level " +
           "  FROM department d " +
           "  INNER JOIN parent_departments pd ON d.id = pd.parent_id " +
           ") " +
           "SELECT * FROM parent_departments ORDER BY level", 
           nativeQuery = true)
    List<Department> findParentDepartmentsByUserId(@Param("userId") Long userId);

    /**
     * 查找部门管理员列表
     * @param departmentId 部门ID
     * @return 管理员用户列表
     */
    @Query("SELECT u FROM User u " +
           "JOIN UserRole ur ON u.id = ur.userId " +
           "JOIN Role r ON ur.roleId = r.id " +
           "WHERE u.departmentId = :departmentId AND r.code = 'DEPT_ADMIN'")
    List<Object[]> findDepartmentAdmins(@Param("departmentId") Long departmentId);
}