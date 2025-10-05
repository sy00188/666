package com.archive.management.mapper;

import com.archive.management.entity.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限Mapper接口
 * 基于MyBatis-Plus的权限数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据权限编码查找权限
     * 
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0")
    Permission findByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 根据权限编码检查是否存在
     * 
     * @param code 权限编码
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_permission WHERE permission_code = #{code} AND deleted = 0")
    boolean existsByCode(@Param("code") String code);

    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 " +
            "ORDER BY p.sort_order ASC, p.create_time DESC")
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限名称查找权限
     * 
     * @param permissionName 权限名称
     * @return 权限信息
     */
    @Select("SELECT * FROM sys_permission WHERE name = #{permissionName} AND deleted = 0")
    Permission findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * 根据权限路径查找权限
     * 
     * @param permissionPath 权限路径
     * @return 权限信息
     */
    @Select("SELECT * FROM sys_permission WHERE permission_path = #{permissionPath} AND deleted = 0")
    Permission findByPermissionPath(@Param("permissionPath") String permissionPath);

    /**
     * 检查权限编码是否存在
     * 
     * @param permissionCode 权限编码
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0")
    int countByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 检查权限名称是否存在
     * 
     * @param permissionName 权限名称
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE name = #{permissionName} AND deleted = 0")
    int countByPermissionName(@Param("permissionName") String permissionName);

    /**
     * 检查权限路径是否存在
     * 
     * @param permissionPath 权限路径
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE permission_path = #{permissionPath} AND deleted = 0")
    int countByPermissionPath(@Param("permissionPath") String permissionPath);

    /**
     * 根据状态查找权限列表
     * 
     * @param status 权限状态
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE status = #{status} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findByStatus(@Param("status") Integer status);

    /**
     * 根据权限类型查找权限列表
     * 
     * @param permissionType 权限类型
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE permission_type = #{permissionType} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findByPermissionType(@Param("permissionType") Integer permissionType);

    /**
     * 根据父权限ID查找子权限列表
     * 
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查找根权限列表（顶级权限）
     * 
     * @return 根权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findRootPermissions();

    /**
     * 查找启用的权限列表
     * 
     * @return 启用的权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findEnabledPermissions();

    /**
     * 查找禁用的权限列表
     * 
     * @return 禁用的权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE status = 0 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findDisabledPermissions();

    /**
     * 查找菜单类型权限列表
     * 
     * @return 菜单类型权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE permission_type = 1 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findMenuPermissions();

    /**
     * 查找按钮类型权限列表
     * 
     * @return 按钮类型权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE permission_type = 2 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findButtonPermissions();

    /**
     * 查找接口类型权限列表
     * 
     * @return 接口类型权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE permission_type = 3 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findApiPermissions();

    /**
     * 根据角色ID查找权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 " +
            "ORDER BY p.sort_order ASC, p.create_time DESC")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 " +
            "ORDER BY p.sort_order ASC, p.create_time DESC")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限级别查找权限列表
     * 
     * @param level 权限级别
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE level = #{level} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> findByLevel(@Param("level") Integer level);

    /**
     * 根据权限级别范围查找权限列表
     * 
     * @param minLevel 最小级别
     * @param maxLevel 最大级别
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE level BETWEEN #{minLevel} AND #{maxLevel} " +
            "AND deleted = 0 ORDER BY level ASC, sort_order ASC")
    List<Permission> findByLevelBetween(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    /**
     * 根据创建时间范围查找权限列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Permission> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 模糊搜索权限
     * 
     * @param keyword 关键词
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE (permission_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR permission_path LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Permission> searchPermissions(@Param("keyword") String keyword);

    /**
     * 分页模糊搜索权限
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM sys_permission WHERE (permission_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR permission_path LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    IPage<Permission> searchPermissionsWithPagination(Page<Permission> page, @Param("keyword") String keyword);

    /**
     * 根据指定字段分页模糊搜索权限
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @param searchFields 搜索字段列表
     * @return 分页结果
     */
    IPage<Permission> searchPermissionsWithFields(Page<Permission> page, @Param("keyword") String keyword, @Param("searchFields") List<String> searchFields);

    /**
     * 获取权限树结构
     * 
     * @return 权限树列表
     */
    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY parent_id ASC, sort_order ASC, create_time DESC")
    List<Permission> getPermissionTree();

    /**
     * 获取用户权限树结构
     * 
     * @param userId 用户ID
     * @return 用户权限树列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 " +
            "ORDER BY p.parent_id ASC, p.sort_order ASC, p.create_time DESC")
    List<Permission> getUserPermissionTree(@Param("userId") Long userId);

    /**
     * 获取角色权限树结构
     * 
     * @param roleId 角色ID
     * @return 角色权限树列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 " +
            "ORDER BY p.parent_id ASC, p.sort_order ASC, p.create_time DESC")
    List<Permission> getRolePermissionTree(@Param("roleId") Long roleId);

    /**
     * 统计权限总数
     * 
     * @return 权限总数
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE deleted = 0")
    long countPermissions();

    /**
     * 根据状态统计权限数量
     * 
     * @param status 状态
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据权限类型统计权限数量
     * 
     * @param permissionType 权限类型
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE permission_type = #{permissionType} AND deleted = 0")
    long countByPermissionType(@Param("permissionType") Integer permissionType);

    /**
     * 根据权限级别统计权限数量
     * 
     * @param level 权限级别
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE level = #{level} AND deleted = 0")
    long countByLevel(@Param("level") Integer level);

    /**
     * 统计今日新增权限数量
     * 
     * @return 今日新增权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    long countTodayNewPermissions();

    /**
     * 统计本周新增权限数量
     * 
     * @return 本周新增权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) AND deleted = 0")
    long countThisWeekNewPermissions();

    /**
     * 统计本月新增权限数量
     * 
     * @return 本月新增权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE YEAR(create_time) = YEAR(NOW()) " +
            "AND MONTH(create_time) = MONTH(NOW()) AND deleted = 0")
    long countThisMonthNewPermissions();

    /**
     * 批量更新权限状态
     * 
     * @param permissionIds 权限ID列表
     * @param status 状态
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE sys_permission SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='permissionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("permissionIds") List<Long> permissionIds, @Param("status") Integer status);

    /**
     * 批量删除权限（软删除）
     * 
     * @param permissionIds 权限ID列表
     * @param deletedBy 删除人ID
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE sys_permission SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='permissionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeletePermissions(@Param("permissionIds") List<Long> permissionIds, @Param("deletedBy") Long deletedBy);

    /**
     * 更新权限排序
     * 
     * @param permissionId 权限ID
     * @param sortOrder 排序号
     * @return 更新数量
     */
    @Update("UPDATE sys_permission SET sort_order = #{sortOrder}, update_time = NOW() WHERE id = #{permissionId}")
    int updateSortOrder(@Param("permissionId") Long permissionId, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取子权限数量
     * 
     * @param parentId 父权限ID
     * @return 子权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0")
    long getChildrenCount(@Param("parentId") Long parentId);

    /**
     * 根据父权限ID统计权限数量
     * 
     * @param parentId 父权限ID
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0")
    long countByParentId(@Param("parentId") Long parentId);

    /**
     * 获取权限的所有子权限ID
     * 
     * @param parentId 父权限ID
     * @return 子权限ID列表
     */
    @Select("SELECT id FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0")
    List<Long> getChildrenIds(@Param("parentId") Long parentId);

    /**
     * 获取权限的所有祖先权限ID
     * 
     * @param permissionId 权限ID
     * @return 祖先权限ID列表
     */
    @Select("WITH RECURSIVE permission_ancestors AS (" +
            "  SELECT parent_id FROM sys_permission WHERE id = #{permissionId} AND parent_id IS NOT NULL" +
            "  UNION ALL" +
            "  SELECT p.parent_id FROM sys_permission p " +
            "  INNER JOIN permission_ancestors pa ON p.id = pa.parent_id " +
            "  WHERE p.parent_id IS NOT NULL" +
            ") SELECT parent_id FROM permission_ancestors")
    List<Long> getAncestorIds(@Param("permissionId") Long permissionId);

    /**
     * 获取权限的所有后代权限ID
     * 
     * @param parentId 父权限ID
     * @return 后代权限ID列表
     */
    @Select("WITH RECURSIVE permission_descendants AS (" +
            "  SELECT id FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0" +
            "  UNION ALL" +
            "  SELECT p.id FROM sys_permission p " +
            "  INNER JOIN permission_descendants pd ON p.parent_id = pd.id " +
            "  WHERE p.deleted = 0" +
            ") SELECT id FROM permission_descendants")
    List<Long> getDescendantIds(@Param("parentId") Long parentId);

    /**
     * 检查权限是否有子权限
     * 
     * @param permissionId 权限ID
     * @return 是否有子权限
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_permission WHERE parent_id = #{permissionId} AND deleted = 0")
    boolean hasChildren(@Param("permissionId") Long permissionId);

    /**
     * 获取最大排序号
     * 
     * @param parentId 父权限ID
     * @return 最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 获取下一个可用的排序号
     * 
     * @param parentId 父权限ID
     * @return 下一个排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) + 1 FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getNextSortOrder(@Param("parentId") Long parentId);

    /**
     * 清理未使用的权限
     * 
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE sys_permission SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id NOT IN (SELECT DISTINCT permission_id FROM sys_role_permission) " +
            "AND permission_type = 2 AND status = 0 AND deleted = 0")
    int cleanUnusedPermissions(@Param("deletedBy") Long deletedBy);

    /**
     * 获取权限统计信息
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalPermissions, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabledPermissions, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabledPermissions, " +
            "SUM(CASE WHEN permission_type = 1 THEN 1 ELSE 0 END) as menuPermissions, " +
            "SUM(CASE WHEN permission_type = 2 THEN 1 ELSE 0 END) as buttonPermissions, " +
            "SUM(CASE WHEN permission_type = 3 THEN 1 ELSE 0 END) as apiPermissions, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNewPermissions " +
            "FROM sys_permission WHERE deleted = 0")
    Map<String, Object> getPermissionStatistics();

    /**
     * 获取权限级别统计
     * 
     * @return 权限级别统计
     */
    @Select("SELECT level, COUNT(*) as permissionCount " +
            "FROM sys_permission WHERE deleted = 0 " +
            "GROUP BY level ORDER BY level ASC")
    List<Map<String, Object>> getPermissionLevelStatistics();

    /**
     * 获取权限类型统计
     * 
     * @return 权限类型统计
     */
    @Select("SELECT " +
            "CASE permission_type " +
            "  WHEN 1 THEN '菜单权限' " +
            "  WHEN 2 THEN '按钮权限' " +
            "  WHEN 3 THEN '接口权限' " +
            "  ELSE '其他权限' " +
            "END as permissionTypeName, " +
            "COUNT(*) as permissionCount " +
            "FROM sys_permission WHERE deleted = 0 " +
            "GROUP BY permission_type ORDER BY permission_type ASC")
    List<Map<String, Object>> getPermissionTypeStatistics();

    /**
     * 获取权限使用统计
     * 
     * @return 权限使用统计
     */
    @Select("SELECT p.name as permissionName, COUNT(rp.role_id) as roleCount " +
            "FROM sys_permission p LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "LEFT JOIN sys_role r ON rp.role_id = r.id AND r.deleted = 0 " +
            "WHERE p.deleted = 0 GROUP BY p.id, p.name ORDER BY roleCount DESC")
    List<Map<String, Object>> getPermissionUsageStatistics();

    /**
     * 获取权限创建趋势统计
     * 
     * @param days 天数
     * @return 创建趋势统计
     */
    @Select("SELECT DATE(create_time) as createDate, COUNT(*) as permissionCount " +
            "FROM sys_permission WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 GROUP BY DATE(create_time) ORDER BY createDate ASC")
    List<Map<String, Object>> getPermissionCreationTrendStatistics(@Param("days") int days);

    /**
     * 检查用户是否有指定权限代码的权限
     * 
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 匹配的权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.permission_code = #{permissionCode} " +
            "AND p.deleted = 0 AND p.status = 1")
    int checkUserPermissionByCode(@Param("userId") Long userId, @Param("permissionCode") String permissionCode);

    /**
     * 查找用户的菜单权限列表
     * 
     * @param userId 用户ID
     * @return 用户的菜单权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.permission_type = 1 " +
            "AND p.deleted = 0 AND p.status = 1 " +
            "ORDER BY p.sort_order ASC, p.create_time DESC")
    List<Permission> findUserMenuPermissions(@Param("userId") Long userId);
}