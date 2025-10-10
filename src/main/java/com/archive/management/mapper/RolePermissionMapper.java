package com.archive.management.mapper;

import com.archive.management.entity.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 批量插入角色权限关联
     * 
     * @param rolePermissions 角色权限列表
     * @return 插入数量
     */
    int insertBatch(@Param("rolePermissions") List<RolePermission> rolePermissions);

    /**
     * 根据角色ID和权限ID列表删除关联
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 删除数量
     */
    int deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId, 
                                      @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除无效的角色权限关联
     * 
     * @return 删除数量
     */
    int deleteInvalidAssociations();

    /**
     * 根据角色ID查询权限ID列表
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查询角色ID列表
     * 
     * @param permissionId 权限ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 统计角色拥有的权限数量
     * 
     * @param roleId 角色ID
     * @return 权限数量
     */
    Long countPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查角色权限关联是否存在
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否存在
     */
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Long roleId, 
                                         @Param("permissionId") Long permissionId);
}
