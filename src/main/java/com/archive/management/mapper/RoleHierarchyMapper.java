package com.archive.management.mapper;

import com.archive.management.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色层级Mapper接口（占位符）
 * 用于处理角色层级关系的数据访问
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Mapper
public interface RoleHierarchyMapper extends BaseMapper<Role> {

    /**
     * 删除无效的角色层级关联
     * 清理父角色或子角色已被删除的层级关系
     * 
     * @return 删除数量
     */
    @Delete("UPDATE roles SET parent_id = NULL WHERE " +
            "parent_id IS NOT NULL AND " +
            "parent_id NOT IN (SELECT id FROM roles WHERE deleted = 0)")
    int deleteInvalidAssociations();

    /**
     * 检查是否存在循环引用
     * 
     * @param roleId 角色ID
     * @param potentialParentId 潜在父角色ID
     * @return 是否存在循环引用
     */
    @Select("WITH RECURSIVE role_hierarchy AS (" +
            "  SELECT id, parent_id FROM roles WHERE id = #{potentialParentId} AND deleted = 0" +
            "  UNION ALL" +
            "  SELECT r.id, r.parent_id FROM roles r" +
            "  INNER JOIN role_hierarchy rh ON r.id = rh.parent_id" +
            "  WHERE r.deleted = 0" +
            ")" +
            "SELECT COUNT(*) > 0 FROM role_hierarchy WHERE id = #{roleId}")
    boolean hasCircularReference(@Param("roleId") Long roleId, 
                                 @Param("potentialParentId") Long potentialParentId);
}

