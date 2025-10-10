package com.archive.management.mapper;

import com.archive.management.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色关联数据访问层接口
 * 基于MyBatis-Plus实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查找用户角色关联列表
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    @Select("SELECT * FROM user_roles WHERE user_id = #{userId} AND deleted = 0")
    List<UserRole> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查找用户角色关联列表
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    @Select("SELECT * FROM user_roles WHERE role_id = #{roleId} AND deleted = 0")
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID和角色ID查找用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联
     */
    @Select("SELECT * FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId} AND deleted = 0")
    UserRole findByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 检查用户角色关联是否存在
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId} AND deleted = 0")
    boolean existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据状态查找用户角色关联列表
     * @param status 状态
     * @return 用户角色关联列表
     */
    @Select("SELECT * FROM user_roles WHERE status = #{status} AND deleted = 0")
    List<UserRole> findByStatus(@Param("status") Integer status);

    /**
     * 根据分配类型查找用户角色关联列表
     * @param assignType 分配类型
     * @return 用户角色关联列表
     */
    @Select("SELECT * FROM user_roles WHERE assign_type = #{assignType} AND deleted = 0")
    List<UserRole> findByAssignType(@Param("assignType") Integer assignType);

    /**
     * 查找有效期内的用户角色关联列表
     * @param currentTime 当前时间
     * @return 用户角色关联列表
     */
    @Select("SELECT * FROM user_roles WHERE (valid_from IS NULL OR valid_from <= #{currentTime}) " +
            "AND (valid_until IS NULL OR valid_until >= #{currentTime}) AND deleted = 0")
    List<UserRole> findValidUserRoles(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找已过期的用户角色关联列表
     * @param currentTime 当前时间
     * @return 用户角色关联列表
     */
    @Select("SELECT * FROM user_roles WHERE valid_until IS NOT NULL AND valid_until < #{currentTime} AND deleted = 0")
    List<UserRole> findExpiredUserRoles(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 分页查询用户角色关联
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param roleId 角色ID（可选）
     * @param status 状态（可选）
     * @param assignType 分配类型（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM user_roles WHERE deleted = 0" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='roleId != null'> AND role_id = #{roleId}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='assignType != null'> AND assign_type = #{assignType}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    IPage<UserRole> findUserRolesWithPagination(Page<UserRole> page, 
                                               @Param("userId") Long userId,
                                               @Param("roleId") Long roleId,
                                               @Param("status") Integer status,
                                               @Param("assignType") Integer assignType);

    /**
     * 统计用户角色关联总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE deleted = 0")
    long countUserRoles();

    /**
     * 根据状态统计用户角色关联数量
     * @param status 状态
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据分配类型统计用户角色关联数量
     * @param assignType 分配类型
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE assign_type = #{assignType} AND deleted = 0")
    long countByAssignType(@Param("assignType") Integer assignType);

    /**
     * 统计用户的角色数量
     * @param userId 用户ID
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE user_id = #{userId} AND deleted = 0")
    long countRolesByUserId(@Param("userId") Long userId);

    /**
     * 统计角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE role_id = #{roleId} AND deleted = 0")
    long countUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量更新用户角色关联状态
     * @param ids ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE user_roles SET status = #{status}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
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
     * 批量软删除用户角色关联
     * @param ids ID列表
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE user_roles SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
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
     * 根据用户ID软删除所有用户角色关联
     * @param userId 用户ID
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("UPDATE user_roles SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
            "WHERE user_id = #{userId} AND deleted = 0")
    int softDeleteByUserId(@Param("userId") Long userId, 
                          @Param("deletedBy") Long deletedBy,
                          @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 根据角色ID软删除所有用户角色关联
     * @param roleId 角色ID
     * @param deletedBy 删除人ID
     * @param deletedAt 删除时间
     * @return 删除数量
     */
    @Update("UPDATE user_roles SET deleted = 1, deleted_by = #{deletedBy}, deleted_at = #{deletedAt} " +
            "WHERE role_id = #{roleId} AND deleted = 0")
    int softDeleteByRoleId(@Param("roleId") Long roleId, 
                          @Param("deletedBy") Long deletedBy,
                          @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 更新用户角色关联的有效期
     * @param id 用户角色关联ID
     * @param validFrom 有效开始时间
     * @param validUntil 有效结束时间
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("UPDATE user_roles SET valid_from = #{validFrom}, valid_until = #{validUntil}, " +
            "updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
            "WHERE id = #{id} AND deleted = 0")
    int updateValidPeriod(@Param("id") Long id,
                         @Param("validFrom") LocalDateTime validFrom,
                         @Param("validUntil") LocalDateTime validUntil,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量设置用户角色关联过期
     * @param ids ID列表
     * @param expiredAt 过期时间
     * @param updatedBy 更新人ID
     * @param updatedAt 更新时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE user_roles SET valid_until = #{expiredAt}, updated_by = #{updatedBy}, updated_at = #{updatedAt} " +
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
     * 删除无效的用户角色关联
     * 
     * @return 删除数量
     */
    @Delete("DELETE FROM user_roles WHERE " +
            "(user_id NOT IN (SELECT id FROM users WHERE deleted = 0)) OR " +
            "(role_id NOT IN (SELECT id FROM roles WHERE deleted = 0))")
    int deleteInvalidAssociations();
}