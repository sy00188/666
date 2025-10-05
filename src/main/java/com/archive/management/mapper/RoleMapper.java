package com.archive.management.mapper;

import com.archive.management.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 角色Mapper接口
 * 基于MyBatis-Plus的角色数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码检查是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    boolean existsByCode(@Param("roleCode") String roleCode);

    /**
     * 根据参数统计角色数量
     */
    int countByParams(@Param("params") Map<String, Object> params);

    /**
     * 根据参数分页查询角色
     */
    List<Role> selectByParams(@Param("params") Map<String, Object> params, 
                             @Param("offset") int offset, 
                             @Param("limit") Integer limit);

    /**
     * 查询所有启用的角色
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<Role> selectAllEnabled();

    /**
     * 根据角色编码查找角色
     * 
     * @param roleCode 角色编码
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    Role findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据角色名称查找角色
     * 
     * @param roleName 角色名称
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE name = #{roleName} AND deleted = 0")
    Role findByRoleName(@Param("roleName") String roleName);

    /**
     * 检查角色编码是否存在
     * 
     * @param roleCode 角色编码
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    int countByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 检查角色名称是否存在
     * 
     * @param roleName 角色名称
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE name = #{roleName} AND deleted = 0")
    int countByRoleName(@Param("roleName") String roleName);

    /**
     * 根据状态查找角色列表
     * 
     * @param status 角色状态
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE status = #{status} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findByStatus(@Param("status") Integer status);

    /**
     * 根据角色类型查找角色列表
     * 
     * @param roleType 角色类型
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_type = #{roleType} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findByRoleType(@Param("roleType") Integer roleType);

    /**
     * 根据角色级别查找角色列表
     * 
     * @param roleLevel 角色级别
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_level = #{roleLevel} AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findByRoleLevel(@Param("roleLevel") Integer roleLevel);

    /**
     * 查找启用的角色列表
     * 
     * @return 启用的角色列表
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findEnabledRoles();

    /**
     * 查找禁用的角色列表
     * 
     * @return 禁用的角色列表
     */
    @Select("SELECT * FROM sys_role WHERE status = 0 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findDisabledRoles();

    /**
     * 查找系统角色列表
     * 
     * @return 系统角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_type = 1 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findSystemRoles();

    /**
     * 查找业务角色列表
     * 
     * @return 业务角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_type = 2 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findBusinessRoles();

    /**
     * 查找自定义角色列表
     * 
     * @return 自定义角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_type = 3 AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> findCustomRoles();

    /**
     * 根据用户ID查找角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 " +
            "ORDER BY r.sort_order ASC, r.create_time DESC")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限ID查找角色列表
     * 
     * @param permissionId 权限ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_role_permission rp ON r.id = rp.role_id " +
            "WHERE rp.permission_id = #{permissionId} AND r.deleted = 0 " +
            "ORDER BY r.sort_order ASC, r.create_time DESC")
    List<Role> findByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色级别范围查找角色列表
     * 
     * @param minLevel 最小级别
     * @param maxLevel 最大级别
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_level BETWEEN #{minLevel} AND #{maxLevel} " +
            "AND deleted = 0 ORDER BY role_level ASC, sort_order ASC")
    List<Role> findByRoleLevelBetween(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    /**
     * 根据创建时间范围查找角色列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Role> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 模糊搜索角色
     * 
     * @param keyword 关键词
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE (role_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    List<Role> searchRoles(@Param("keyword") String keyword);

    /**
     * 分页模糊搜索角色
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM sys_role WHERE (role_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY sort_order ASC, create_time DESC")
    IPage<Role> searchRolesWithPagination(Page<Role> page, @Param("keyword") String keyword);

    /**
     * 统计角色总数
     * 
     * @return 角色总数
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE deleted = 0")
    long countRoles();

    /**
     * 根据状态统计角色数量
     * 
     * @param status 状态
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据角色类型统计角色数量
     * 
     * @param roleType 角色类型
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE role_type = #{roleType} AND deleted = 0")
    long countByRoleType(@Param("roleType") Integer roleType);

    /**
     * 根据角色级别统计角色数量
     * 
     * @param roleLevel 角色级别
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE role_level = #{roleLevel} AND deleted = 0")
    long countByRoleLevel(@Param("roleLevel") Integer roleLevel);

    /**
     * 统计今日新增角色数量
     * 
     * @return 今日新增角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    long countTodayNewRoles();

    /**
     * 统计本周新增角色数量
     * 
     * @return 本周新增角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) AND deleted = 0")
    long countThisWeekNewRoles();

    /**
     * 统计本月新增角色数量
     * 
     * @return 本月新增角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE YEAR(create_time) = YEAR(NOW()) " +
            "AND MONTH(create_time) = MONTH(NOW()) AND deleted = 0")
    long countThisMonthNewRoles();

    /**
     * 批量更新角色状态
     * 
     * @param roleIds 角色ID列表
     * @param status 状态
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE sys_role SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("roleIds") List<Long> roleIds, @Param("status") Integer status);

    /**
     * 批量删除角色（软删除）
     * 
     * @param roleIds 角色ID列表
     * @param deletedBy 删除人ID
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE sys_role SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteRoles(@Param("roleIds") List<Long> roleIds, @Param("deletedBy") Long deletedBy);

    /**
     * 更新角色排序
     * 
     * @param roleId 角色ID
     * @param sortOrder 排序号
     * @return 更新数量
     */
    @Update("UPDATE sys_role SET sort_order = #{sortOrder}, update_time = NOW() WHERE id = #{roleId}")
    int updateSortOrder(@Param("roleId") Long roleId, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取角色的所有权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1 AND p.deleted = 0")
    List<String> getRolePermissions(@Param("roleId") Long roleId);

    /**
     * 获取角色的用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user_role ur " +
            "INNER JOIN sys_user u ON ur.user_id = u.id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    long getRoleUserCount(@Param("roleId") Long roleId);

    /**
     * 检查角色是否有指定权限
     * 
     * @param roleId 角色ID
     * @param permission 权限标识
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.permission_code = #{permission} " +
            "AND p.status = 1 AND p.deleted = 0")
    int countRolePermission(@Param("roleId") Long roleId, @Param("permission") String permission);

    /**
     * 获取最大排序号
     * 
     * @return 最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM sys_role WHERE deleted = 0")
    Integer getMaxSortOrder();

    /**
     * 获取下一个可用的排序号
     * 
     * @return 下一个排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) + 1 FROM sys_role WHERE deleted = 0")
    Integer getNextSortOrder();

    /**
     * 根据角色级别获取上级角色列表
     * 
     * @param roleLevel 角色级别
     * @return 上级角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_level < #{roleLevel} AND status = 1 AND deleted = 0 " +
            "ORDER BY role_level DESC, sort_order ASC")
    List<Role> getSuperiorRoles(@Param("roleLevel") Integer roleLevel);

    /**
     * 根据角色级别获取下级角色列表
     * 
     * @param roleLevel 角色级别
     * @return 下级角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_level > #{roleLevel} AND status = 1 AND deleted = 0 " +
            "ORDER BY role_level ASC, sort_order ASC")
    List<Role> getSubordinateRoles(@Param("roleLevel") Integer roleLevel);

    /**
     * 获取同级角色列表
     * 
     * @param roleLevel 角色级别
     * @param excludeRoleId 排除的角色ID
     * @return 同级角色列表
     */
    @Select("SELECT * FROM sys_role WHERE role_level = #{roleLevel} AND id != #{excludeRoleId} " +
            "AND status = 1 AND deleted = 0 ORDER BY sort_order ASC")
    List<Role> getSameLevelRoles(@Param("roleLevel") Integer roleLevel, @Param("excludeRoleId") Long excludeRoleId);

    /**
     * 清理未使用的角色
     * 
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE sys_role SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id NOT IN (SELECT DISTINCT role_id FROM sys_user_role) " +
            "AND role_type = 3 AND status = 0 AND deleted = 0")
    int cleanUnusedRoles(@Param("deletedBy") Long deletedBy);

    /**
     * 获取角色统计信息
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalRoles, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabledRoles, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabledRoles, " +
            "SUM(CASE WHEN role_type = 1 THEN 1 ELSE 0 END) as systemRoles, " +
            "SUM(CASE WHEN role_type = 2 THEN 1 ELSE 0 END) as businessRoles, " +
            "SUM(CASE WHEN role_type = 3 THEN 1 ELSE 0 END) as customRoles, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNewRoles " +
            "FROM sys_role WHERE deleted = 0")
    Map<String, Object> getRoleStatistics();

    /**
     * 获取角色级别统计
     * 
     * @return 角色级别统计
     */
    @Select("SELECT role_level, COUNT(*) as roleCount " +
            "FROM sys_role WHERE deleted = 0 " +
            "GROUP BY role_level ORDER BY role_level ASC")
    List<Map<String, Object>> getRoleLevelStatistics();

    /**
     * 获取角色权限统计
     * 
     * @return 角色权限统计
     */
    @Select("SELECT r.name as roleName, COUNT(rp.permission_id) as permissionCount " +
            "FROM sys_role r LEFT JOIN sys_role_permission rp ON r.id = rp.role_id " +
            "LEFT JOIN sys_permission p ON rp.permission_id = p.id AND p.deleted = 0 " +
            "WHERE r.deleted = 0 GROUP BY r.id, r.name ORDER BY permissionCount DESC")
    List<Map<String, Object>> getRolePermissionStatistics();

    /**
     * 获取角色用户分布统计
     * 
     * @return 角色用户分布统计
     */
    @Select("SELECT r.name as roleName, COUNT(ur.user_id) as userCount " +
            "FROM sys_role r LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "LEFT JOIN sys_user u ON ur.user_id = u.id AND u.deleted = 0 " +
            "WHERE r.deleted = 0 GROUP BY r.id, r.name ORDER BY userCount DESC")
    List<Map<String, Object>> getRoleUserDistributionStatistics();

    /**
     * 获取角色创建趋势统计
     * 
     * @param days 天数
     * @return 创建趋势统计
     */
    @Select("SELECT DATE(create_time) as createDate, COUNT(*) as roleCount " +
            "FROM sys_role WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 GROUP BY DATE(create_time) ORDER BY createDate ASC")
    List<Map<String, Object>> getRoleCreationTrendStatistics(@Param("days") int days);

    /**
     * 删除角色的所有权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissions(@Param("roleId") Long roleId);

    /**
     * 批量插入角色权限关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='permissionIds' item='permissionId' separator=','>" +
            "(#{roleId}, #{permissionId})" +
            "</foreach>" +
            "</script>")
    void batchInsertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 根据权限ID统计关联的角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission rp " +
            "INNER JOIN sys_role r ON rp.role_id = r.id " +
            "WHERE rp.permission_id = #{permissionId} AND r.deleted = 0")
    long countByPermissionId(@Param("permissionId") Long permissionId);
}