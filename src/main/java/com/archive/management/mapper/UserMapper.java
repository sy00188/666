package com.archive.management.mapper;

import com.archive.management.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 * 基于MyBatis-Plus的用户数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据部门ID查询用户列表
     */
    @Select("SELECT * FROM sys_user WHERE department_id = #{departmentId} AND deleted = 0 ORDER BY create_time DESC")
    List<User> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据角色ID统计用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    long countByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    List<User> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计总用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0")
    Long countTotal();

    /**
     * 统计活跃用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE status = 1 AND deleted = 0")
    Long countActive();

    /**
     * 统计在线用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE online_status = 1 AND deleted = 0")
    Long countOnline();

    /**
     * 查询用户活动统计
     */
    List<Map<String, Object>> selectActivityStatistics(@Param("days") int days);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0")
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查找用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    User findByPhone(@Param("phone") String phone);

    /**
     * 根据员工编号查找用户
     * 
     * @param employeeId 员工编号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE employee_id = #{employeeId} AND deleted = 0")
    User findByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email} AND deleted = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    int countByPhone(@Param("phone") String phone);

    /**
     * 检查员工编号是否存在
     * 
     * @param employeeId 员工编号
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE employee_id = #{employeeId} AND deleted = 0")
    int countByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 根据状态查找用户列表
     * 
     * @param status 用户状态
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByStatus(@Param("status") Integer status);

    /**
     * 根据用户类型查找用户列表
     * 
     * @param userType 用户类型
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE user_type = #{userType} AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByUserType(@Param("userType") Integer userType);

    /**
     * 根据部门ID查找用户列表
     * 
     * @param departmentId 部门ID
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE department_id = #{departmentId} AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据角色ID查找用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0 " +
            "ORDER BY u.create_time DESC")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 查找启用的用户列表
     * 
     * @return 启用的用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = 1 AND deleted = 0 ORDER BY create_time DESC")
    List<User> findEnabledUsers();

    /**
     * 查找禁用的用户列表
     * 
     * @return 禁用的用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<User> findDisabledUsers();

    /**
     * 查找锁定的用户列表
     * 
     * @return 锁定的用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = 2 AND deleted = 0 ORDER BY create_time DESC")
    List<User> findLockedUsers();

    /**
     * 查找在线用户列表
     * 
     * @return 在线用户列表
     */
    @Select("SELECT * FROM sys_user WHERE online_status = 1 AND deleted = 0 ORDER BY last_login_time DESC")
    List<User> findOnlineUsers();

    /**
     * 查找最近登录的用户列表
     * 
     * @param days 天数
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE last_login_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 ORDER BY last_login_time DESC")
    List<User> findRecentLoginUsers(@Param("days") int days);

    /**
     * 查找长时间未登录的用户列表
     * 
     * @param days 天数
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE (last_login_time IS NULL OR last_login_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)) " +
            "AND deleted = 0 ORDER BY last_login_time ASC")
    List<User> findLongTimeNoLoginUsers(@Param("days") int days);

    /**
     * 查找密码即将过期的用户列表
     * 
     * @param days 天数
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE password_expire_time IS NOT NULL " +
            "AND password_expire_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 ORDER BY password_expire_time ASC")
    List<User> findPasswordExpiringSoonUsers(@Param("days") int days);

    /**
     * 查找密码已过期的用户列表
     * 
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE password_expire_time IS NOT NULL " +
            "AND password_expire_time < NOW() AND deleted = 0 ORDER BY password_expire_time ASC")
    List<User> findPasswordExpiredUsers();

    /**
     * 根据创建时间范围查找用户列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<User> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 根据最后登录时间范围查找用户列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE last_login_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY last_login_time DESC")
    List<User> findByLastLoginTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 模糊搜索用户
     * 
     * @param keyword 关键词
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR employee_id LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * 分页模糊搜索用户
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM sys_user WHERE (username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR email LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR employee_id LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    IPage<User> searchUsersWithPagination(Page<User> page, @Param("keyword") String keyword);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0")
    long countUsers();

    /**
     * 根据状态统计用户数量
     * 
     * @param status 状态
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据用户类型统计用户数量
     * 
     * @param userType 用户类型
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE user_type = #{userType} AND deleted = 0")
    long countByUserType(@Param("userType") Integer userType);

    /**
     * 根据部门统计用户数量
     * 
     * @param departmentId 部门ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE department_id = #{departmentId} AND deleted = 0")
    long countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 统计今日新增用户数量
     * 
     * @return 今日新增用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    long countTodayNewUsers();

    /**
     * 统计本周新增用户数量
     * 
     * @return 本周新增用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) AND deleted = 0")
    long countThisWeekNewUsers();

    /**
     * 统计本月新增用户数量
     * 
     * @return 本月新增用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE YEAR(create_time) = YEAR(NOW()) " +
            "AND MONTH(create_time) = MONTH(NOW()) AND deleted = 0")
    long countThisMonthNewUsers();

    /**
     * 统计在线用户数量
     * 
     * @return 在线用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE online_status = 1 AND deleted = 0")
    long countOnlineUsers();

    /**
     * 批量更新用户状态
     * 
     * @param userIds 用户ID列表
     * @param status 状态
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE sys_user SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);

    /**
     * 批量删除用户（软删除）
     * 
     * @param userIds 用户ID列表
     * @param deletedBy 删除人ID
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE sys_user SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteUsers(@Param("userIds") List<Long> userIds, @Param("deletedBy") Long deletedBy);

    /**
     * 更新用户最后登录信息
     * 
     * @param userId 用户ID
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     * @return 更新数量
     */
    @Update("UPDATE sys_user SET last_login_time = #{loginTime}, last_login_ip = #{loginIp}, " +
            "online_status = 1, update_time = NOW() WHERE id = #{userId}")
    int updateLastLoginInfo(@Param("userId") Long userId, 
                           @Param("loginTime") LocalDateTime loginTime, 
                           @Param("loginIp") String loginIp);

    /**
     * 更新用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @param passwordExpireTime 密码过期时间
     * @return 更新数量
     */
    @Update("UPDATE sys_user SET password = #{newPassword}, password_expire_time = #{passwordExpireTime}, " +
            "password_update_time = NOW(), update_time = NOW() WHERE id = #{userId}")
    int updatePassword(@Param("userId") Long userId, 
                      @Param("newPassword") String newPassword, 
                      @Param("passwordExpireTime") LocalDateTime passwordExpireTime);

    /**
     * 增加登录失败次数
     * 
     * @param userId 用户ID
     * @return 更新数量
     */
    @Update("UPDATE sys_user SET login_fail_count = login_fail_count + 1, " +
            "last_login_fail_time = NOW(), update_time = NOW() WHERE id = #{userId}")
    int incrementLoginFailCount(@Param("userId") Long userId);

    /**
     * 重置登录失败次数
     * 
     * @param userId 用户ID
     * @return 更新数量
     */
    @Update("UPDATE sys_user SET login_fail_count = 0, last_login_fail_time = NULL, " +
            "update_time = NOW() WHERE id = #{userId}")
    int resetLoginFailCount(@Param("userId") Long userId);

    /**
     * 更新用户头像
     * 
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新数量
     */
    @Update("UPDATE sys_user SET avatar = #{avatarUrl}, update_time = NOW() WHERE id = #{userId}")
    int updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    /**
     * 更新用户在线状态
     * 
     * @param userId 用户ID
     * @param onlineStatus 在线状态
     * @return 更新数量
     */
    @Update("UPDATE sys_user SET online_status = #{onlineStatus}, update_time = NOW() WHERE id = #{userId}")
    int updateOnlineStatus(@Param("userId") Long userId, @Param("onlineStatus") Integer onlineStatus);

    /**
     * 获取用户的所有权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.deleted = 0")
    List<String> getUserPermissions(@Param("userId") Long userId);

    /**
     * 获取用户的所有角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0")
    List<String> getUserRoles(@Param("userId") Long userId);

    /**
     * 检查用户是否有指定权限
     * 
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.permission_code = #{permission} " +
            "AND p.status = 1 AND p.deleted = 0")
    int countUserPermission(@Param("userId") Long userId, @Param("permission") String permission);

    /**
     * 检查用户是否有指定角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.role_code = #{roleCode} " +
            "AND r.status = 1 AND r.deleted = 0")
    int countUserRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);

    /**
     * 清理过期的用户会话
     * 
     * @return 清理数量
     */
    @Update("UPDATE sys_user SET online_status = 0 WHERE last_login_time < DATE_SUB(NOW(), INTERVAL 30 MINUTE)")
    int cleanExpiredSessions();

    /**
     * 清理长时间未登录的用户
     * 
     * @param days 天数
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE sys_user SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE (last_login_time IS NULL OR last_login_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)) " +
            "AND status = 0 AND deleted = 0")
    int cleanLongTimeNoLoginUsers(@Param("days") int days, @Param("deletedBy") Long deletedBy);

    /**
     * 获取用户统计信息
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalUsers, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabledUsers, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabledUsers, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as lockedUsers, " +
            "SUM(CASE WHEN online_status = 1 THEN 1 ELSE 0 END) as onlineUsers, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNewUsers " +
            "FROM sys_user WHERE deleted = 0")
    Map<String, Object> getUserStatistics();

    /**
     * 获取部门用户统计
     * 
     * @return 部门用户统计
     */
    @Select("SELECT d.name as departmentName, COUNT(u.id) as userCount " +
            "FROM sys_department d LEFT JOIN sys_user u ON d.id = u.department_id AND u.deleted = 0 " +
            "WHERE d.deleted = 0 GROUP BY d.id, d.name ORDER BY userCount DESC")
    List<Map<String, Object>> getDepartmentUserStatistics();

    /**
     * 获取角色用户统计
     * 
     * @return 角色用户统计
     */
    @Select("SELECT r.name as roleName, COUNT(ur.user_id) as userCount " +
            "FROM sys_role r LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "LEFT JOIN sys_user u ON ur.user_id = u.id AND u.deleted = 0 " +
            "WHERE r.deleted = 0 GROUP BY r.id, r.name ORDER BY userCount DESC")
    List<Map<String, Object>> getRoleUserStatistics();
}