package com.archive.management.mapper;

import com.archive.management.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 部门Mapper接口
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据部门代码查询部门
     */
    @Select("SELECT * FROM sys_department WHERE dept_code = #{deptCode} AND deleted = 0")
    Department selectByDeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据部门名称查询部门
     */
    @Select("SELECT * FROM sys_department WHERE dept_name = #{deptName} AND deleted = 0")
    Department selectByDeptName(@Param("deptName") String deptName);

    /**
     * 根据父部门ID查询子部门列表
     */
    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Department> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询根部门列表（父部门ID为null或0）
     */
    @Select("SELECT * FROM sys_department WHERE (parent_id IS NULL OR parent_id = 0) AND deleted = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Department> selectRootDepartments();

    /**
     * 根据部门级别查询部门
     */
    @Select("SELECT * FROM sys_department WHERE dept_level = #{deptLevel} AND deleted = 0 ORDER BY sort_order ASC")
    List<Department> selectByDeptLevel(@Param("deptLevel") Integer deptLevel);

    /**
     * 根据部门状态查询部门
     */
    @Select("SELECT * FROM sys_department WHERE status = #{status} AND deleted = 0 ORDER BY sort_order ASC")
    List<Department> selectByStatus(@Param("status") Integer status);

    /**
     * 查询启用的部门列表
     */
    @Select("SELECT * FROM sys_department WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Department> selectEnabledDepartments();

    /**
     * 根据负责人ID查询部门
     */
    @Select("SELECT * FROM sys_department WHERE leader_id = #{leaderId} AND deleted = 0")
    List<Department> selectByLeaderId(@Param("leaderId") Long leaderId);

    /**
     * 根据创建人查询部门
     */
    @Select("SELECT * FROM sys_department WHERE create_by = #{createBy} AND deleted = 0 ORDER BY create_time DESC")
    List<Department> selectByCreateBy(@Param("createBy") String createBy);

    /**
     * 分页查询部门（带搜索条件）
     */
    IPage<Department> selectPageWithConditions(Page<Department> page,
                                             @Param("deptName") String deptName,
                                             @Param("deptCode") String deptCode,
                                             @Param("parentId") Long parentId,
                                             @Param("status") Integer status,
                                             @Param("deptLevel") Integer deptLevel,
                                             @Param("leaderId") Long leaderId);

    /**
     * 检查部门代码是否唯一
     */
    @Select("SELECT COUNT(*) FROM sys_department WHERE dept_code = #{deptCode} AND dept_id != #{excludeId} AND deleted = 0")
    int checkDeptCodeUnique(@Param("deptCode") String deptCode, @Param("excludeId") Long excludeId);

    /**
     * 检查部门名称在同级部门中是否唯一
     */
    @Select("SELECT COUNT(*) FROM sys_department WHERE dept_name = #{deptName} AND parent_id = #{parentId} AND dept_id != #{excludeId} AND deleted = 0")
    int checkDeptNameUniqueInSameLevel(@Param("deptName") String deptName, @Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    /**
     * 获取部门的最大排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 更新部门排序
     */
    @Update("UPDATE sys_department SET sort_order = #{sortOrder}, update_time = NOW() WHERE dept_id = #{deptId}")
    int updateSortOrder(@Param("deptId") Long deptId, @Param("sortOrder") Integer sortOrder);

    /**
     * 软删除部门及其子部门
     */
    @Update("UPDATE sys_department SET deleted = 1, update_time = NOW() WHERE dept_id = #{deptId} OR parent_id = #{deptId}")
    int softDeleteWithChildren(@Param("deptId") Long deptId);

    /**
     * 更新部门状态
     */
    @Update("UPDATE sys_department SET status = #{status}, update_time = NOW() WHERE dept_id = #{deptId}")
    int updateStatus(@Param("deptId") Long deptId, @Param("status") Integer status);

    /**
     * 批量更新部门状态
     */
    @Update("<script>" +
            "UPDATE sys_department SET status = #{status}, update_time = NOW() " +
            "WHERE dept_id IN " +
            "<foreach collection='deptIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("deptIds") List<Long> deptIds, @Param("status") Integer status);

    /**
     * 获取部门树形结构（递归查询）
     */
    List<Department> selectDepartmentTree(@Param("parentId") Long parentId);

    /**
     * 获取部门的所有父级部门ID
     */
    @Select("WITH RECURSIVE dept_hierarchy AS (" +
            "SELECT dept_id, parent_id, dept_name, 0 as level FROM sys_department WHERE dept_id = #{deptId} AND deleted = 0 " +
            "UNION ALL " +
            "SELECT d.dept_id, d.parent_id, d.dept_name, dh.level + 1 " +
            "FROM sys_department d " +
            "INNER JOIN dept_hierarchy dh ON d.dept_id = dh.parent_id " +
            "WHERE d.deleted = 0" +
            ") " +
            "SELECT dept_id FROM dept_hierarchy WHERE dept_id != #{deptId}")
    List<Long> selectParentDeptIds(@Param("deptId") Long deptId);

    /**
     * 获取部门的所有子级部门ID
     */
    @Select("WITH RECURSIVE dept_hierarchy AS (" +
            "SELECT dept_id, parent_id, dept_name, 0 as level FROM sys_department WHERE dept_id = #{deptId} AND deleted = 0 " +
            "UNION ALL " +
            "SELECT d.dept_id, d.parent_id, d.dept_name, dh.level + 1 " +
            "FROM sys_department d " +
            "INNER JOIN dept_hierarchy dh ON d.parent_id = dh.dept_id " +
            "WHERE d.deleted = 0" +
            ") " +
            "SELECT dept_id FROM dept_hierarchy WHERE dept_id != #{deptId}")
    List<Long> selectChildDeptIds(@Param("deptId") Long deptId);

    /**
     * 统计部门数量
     */
    @Select("SELECT COUNT(*) FROM sys_department WHERE deleted = 0")
    int countDepartments();

    /**
     * 统计启用的部门数量
     */
    @Select("SELECT COUNT(*) FROM sys_department WHERE status = 1 AND deleted = 0")
    int countEnabledDepartments();

    /**
     * 统计各级别部门数量
     */
    @Select("SELECT " +
            "dept_level, " +
            "COUNT(*) as count " +
            "FROM sys_department " +
            "WHERE deleted = 0 " +
            "GROUP BY dept_level " +
            "ORDER BY dept_level")
    List<Map<String, Object>> countByDeptLevel();

    /**
     * 统计各状态部门数量
     */
    @Select("SELECT " +
            "status, " +
            "COUNT(*) as count " +
            "FROM sys_department " +
            "WHERE deleted = 0 " +
            "GROUP BY status")
    List<Map<String, Object>> countByStatus();

    /**
     * 获取部门创建统计（按月）
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m') as month, " +
            "COUNT(*) as count " +
            "FROM sys_department " +
            "WHERE deleted = 0 AND create_time >= #{startTime} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Map<String, Object>> getDepartmentCreateStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询最近创建的部门
     */
    @Select("SELECT * FROM sys_department WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Department> selectRecentCreated(@Param("limit") int limit);

    /**
     * 查询最近更新的部门
     */
    @Select("SELECT * FROM sys_department WHERE deleted = 0 AND update_time IS NOT NULL ORDER BY update_time DESC LIMIT #{limit}")
    List<Department> selectRecentUpdated(@Param("limit") int limit);

    /**
     * 根据关键字搜索部门（部门名称或代码）
     */
    @Select("SELECT * FROM sys_department " +
            "WHERE (dept_name LIKE CONCAT('%', #{keyword}, '%') OR dept_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 " +
            "ORDER BY dept_level ASC, sort_order ASC")
    List<Department> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取部门路径（从根部门到当前部门的完整路径）
     */
    @Select("WITH RECURSIVE dept_path AS (" +
            "SELECT dept_id, parent_id, dept_name, dept_code, 0 as level, " +
            "CAST(dept_name AS CHAR(1000)) as path " +
            "FROM sys_department WHERE dept_id = #{deptId} AND deleted = 0 " +
            "UNION ALL " +
            "SELECT d.dept_id, d.parent_id, d.dept_name, d.dept_code, dp.level + 1, " +
            "CONCAT(d.dept_name, ' > ', dp.path) " +
            "FROM sys_department d " +
            "INNER JOIN dept_path dp ON d.dept_id = dp.parent_id " +
            "WHERE d.deleted = 0" +
            ") " +
            "SELECT path FROM dept_path ORDER BY level DESC LIMIT 1")
    String getDepartmentPath(@Param("deptId") Long deptId);

    /**
     * 检查部门是否有子部门
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_department WHERE parent_id = #{deptId} AND deleted = 0")
    boolean hasChildren(@Param("deptId") Long deptId);

    /**
     * 检查部门是否有用户
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_user WHERE dept_id = #{deptId} AND deleted = 0")
    boolean hasUsers(@Param("deptId") Long deptId);

    /**
     * 获取部门用户数量统计
     */
    @Select("SELECT " +
            "d.dept_id, " +
            "d.dept_name, " +
            "d.dept_code, " +
            "COUNT(u.user_id) as user_count " +
            "FROM sys_department d " +
            "LEFT JOIN sys_user u ON d.dept_id = u.dept_id AND u.deleted = 0 " +
            "WHERE d.deleted = 0 " +
            "GROUP BY d.dept_id, d.dept_name, d.dept_code " +
            "ORDER BY user_count DESC")
    List<Map<String, Object>> getDepartmentUserStatistics();

    /**
     * 查询指定部门及其所有子部门
     */
    @Select("WITH RECURSIVE dept_tree AS (" +
            "SELECT dept_id, parent_id, dept_name, dept_code, dept_level, 0 as depth " +
            "FROM sys_department WHERE dept_id = #{deptId} AND deleted = 0 " +
            "UNION ALL " +
            "SELECT d.dept_id, d.parent_id, d.dept_name, d.dept_code, d.dept_level, dt.depth + 1 " +
            "FROM sys_department d " +
            "INNER JOIN dept_tree dt ON d.parent_id = dt.dept_id " +
            "WHERE d.deleted = 0" +
            ") " +
            "SELECT * FROM dept_tree ORDER BY depth, dept_level")
    List<Department> selectDepartmentWithChildren(@Param("deptId") Long deptId);
}