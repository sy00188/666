package com.archive.management.mapper;

import com.archive.management.entity.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色权限关联数据访问层接口
 * 基于MyBatis-Plus实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查找角色权限关联列表
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE role_id = #{roleId} AND deleted = 0")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查找角色权限关联列表
     * @param permissionId 权限ID
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE permission_id = #{permissionId} AND deleted = 0")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID和权限ID查找角色权限关联
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 角色权限关联
     */
    @Select("SELECT * FROM role_permissions WHERE role_id = #{roleId} AND permission_id = #{permissionId} AND deleted = 0")
    RolePermission findByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 检查角色权限关联是否存在
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM role_permissions WHERE role_id = #{roleId} AND permission_id = #{permissionId} AND deleted = 0")
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 根据状态查找角色权限关联列表
     * @param status 状态
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE status = #{status} AND deleted = 0")
    List<RolePermission> findByStatus(@Param("status") Integer status);

    /**
     * 根据授权类型查找角色权限关联列表
     * @param grantType 授权类型
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE grant_type = #{grantType} AND deleted = 0")
    List<RolePermission> findByGrantType(@Param("grantType") Integer grantType);

    /**
     * 查找有效期内的角色权限关联列表
     * @param currentTime 当前时间
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE (valid_from IS NULL OR valid_from <= #{currentTime}) " +
            "AND (valid_until IS NULL OR valid_until >= #{currentTime}) AND deleted = 0")
    List<RolePermission> findValidRolePermissions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找已过期的角色权限关联列表
     * @param currentTime 当前时间
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE valid_until IS NOT NULL AND valid_until < #{currentTime} AND deleted = 0")
    List<RolePermission> findExpiredRolePermissions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据范围值查找角色权限关联列表
     * @param scopeValue 范围值
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM role_permissions WHERE scope_value = #{scopeValue} AND deleted = 0")
    List<RolePermission> findByScopeValue(@Param("scopeValue") String scopeValue);

    /**
     * 分页查询角色权限关联
     * @param page 分页参数
     * @param roleId 角色ID（可选）
     * @param permissionId 权限ID（可选）
     * @param status 状态（可选）
     * @param grantType 授权类型（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM role_permissions WHERE deleted = 0" +
            "<if test='roleId != null'> AND role_id = #{roleId}</if>" +
            "<if test='permissionId != null'> AND permission_id = #{permissionId}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='grantType != null'> AND grant_type = #{grantType}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    IPage<RolePermission> findRolePermissionsWithPagination(Page<RolePermission> page, 
                                                           @Param("roleId") Long roleId,
                                                           @Param("permissionId") Long permissionId,
                                                           @Param("status") Integer status,
                                                           @Param("grantType") Integer grantType);

    /**
     * 统计角色权限关联总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM role_permissions WHERE deleted = 0")
    long countRolePermissions();

    /**
     * 根据状态统计角色权限关联数量
     * @param status 状态
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM role_permissions WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据授权类型统计角色权限关联数量
     * @param grantType 授权类型
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM role_permissions WHERE grant_type = #{grantType} AND deleted = 0")
    long countByGrantType(@Param("grantType") Integer grantType);

    /**
     * 统计角色的权限数量
     * @param roleId 角色ID
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM role_permissions WHERE role_id = #{roleId} AND deleted = 0")
    long countPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计权限的角色数量
     * @param permissionId 权限ID
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM role_permissions WHERE permission_id = #{permissionId} AND deleted = 0")
    long countRolesByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 批量更新角色权限关联状态
     * @param ids ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE role_permissions SET status = #{status}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, 
                         @Param("status") Integer status,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量软删除角色权限关联
     * @param ids ID列表
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE role_permissions SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids, 
                       @Param("deletedBy") Long deletedBy,
                       @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 根据角色ID软删除所有角色权限关联
     * @param roleId 角色ID
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("UPDATE role_permissions SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
            "WHERE role_id = #{roleId} AND deleted = 0")
    int softDeleteByRoleId(@Param("roleId") Long roleId, 
                          @Param("deletedBy") Long deletedBy,
                          @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 根据权限ID软删除所有角色权限关联
     * @param permissionId 权限ID
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("UPDATE role_permissions SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
            "WHERE permission_id = #{permissionId} AND deleted = 0")
    int softDeleteByPermissionId(@Param("permissionId") Long permissionId, 
                                @Param("deletedBy") Long deletedBy,
                                @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 更新角色权限关联的有效期
     * @param id 角色权限关联ID
     * @param validFrom 有效开始时间
     * @param validUntil 有效结束时间
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE role_permissions SET valid_from = #{validFrom}, valid_until = #{validUntil}, " +
            "updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id = #{id} AND deleted = 0")
    int updateValidPeriod(@Param("id") Long id,
                         @Param("validFrom") LocalDateTime validFrom,
                         @Param("validUntil") LocalDateTime validUntil,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新角色权限关联的范围值
     * @param id 角色权限关联ID
     * @param scopeValue 范围值
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE role_permissions SET scope_value = #{scopeValue}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id = #{id} AND deleted = 0")
    int updateScopeValue(@Param("id") Long id,
                        @Param("scopeValue") String scopeValue,
                        @Param("updatedBy") Long updatedBy,
                        @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量设置角色权限关联过期
     * @param ids ID列表
     * @param expiredAt 过期时间
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE role_permissions SET valid_until = #{expiredAt}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchSetExpired(@Param("ids") List<Long> ids,
                       @Param("expiredAt") LocalDateTime expiredAt,
                       @Param("updatedBy") Long updatedBy,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 根据角色ID和权限类型查找角色权限关联列表
     * @param roleId 角色ID
     * @param permissionType 权限类型
     * @return 角色权限关联列表
     */
    @Select("SELECT rp.* FROM role_permissions rp " +
            "JOIN permissions p ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.permission_type = #{permissionType} " +
            "AND rp.deleted = 0 AND p.deleted = 0")
    List<RolePermission> findByRoleIdAndPermissionType(@Param("roleId") Long roleId, 
                                                      @Param("permissionType") Integer permissionType);

    /**
     * 根据用户ID查找所有有效的角色权限关联（通过用户角色关联）
     * @param userId 用户ID
     * @param currentTime 当前时间
     * @return 角色权限关联列表
     */
    @Select("SELECT DISTINCT rp.* FROM role_permissions rp " +
            "JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "AND (rp.valid_from IS NULL OR rp.valid_from <= #{currentTime}) " +
            "AND (rp.valid_until IS NULL OR rp.valid_until >= #{currentTime}) " +
            "AND (ur.valid_from IS NULL OR ur.valid_from <= #{currentTime}) " +
            "AND (ur.valid_until IS NULL OR ur.valid_until >= #{currentTime}) " +
            "AND rp.deleted = 0 AND ur.deleted = 0 " +
            "AND rp.status = 1 AND ur.status = 1")
    List<RolePermission> findValidRolePermissionsByUserId(@Param("userId") Long userId, 
                                                         @Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据权限ID统计角色数量（别名方法，与countRolesByPermissionId功能相同）
     * @param permissionId 权限ID
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM role_permissions WHERE permission_id = #{permissionId} AND deleted = 0")
    int countByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     * @param rolePermissions 角色权限关联列表
     * @return 插入数量
     */
    @Insert("<script>" +
            "INSERT INTO role_permissions (role_id, permission_id, grant_type, scope_value, " +
            "valid_from, valid_until, status, created_by, created_at) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roleId}, #{item.permissionId}, #{item.grantType}, #{item.scopeValue}, " +
            "#{item.validFrom}, #{item.validUntil}, #{item.status}, #{item.createdBy}, #{item.createdAt})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<RolePermission> rolePermissions);

    /**
     * 根据角色ID和权限ID列表删除角色权限关联
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 删除数量
     */
    @Delete("<script>" +
            "DELETE FROM role_permissions WHERE role_id = #{roleId} AND permission_id IN " +
            "<foreach collection='permissionIds' item='permissionId' open='(' separator=',' close=')'>" +
            "#{permissionId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除无效的关联（清理过期或无效的角色权限关联）
     * @return 删除数量
     */
    @Delete("DELETE FROM role_permissions WHERE " +
            "(valid_until IS NOT NULL AND valid_until < NOW()) OR " +
            "role_id NOT IN (SELECT id FROM sys_role WHERE deleted = 0) OR " +
            "permission_id NOT IN (SELECT id FROM sys_permission WHERE deleted = 0)")
    int deleteInvalidAssociations();
}