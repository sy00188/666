package com.archive.management.mapper;

import com.archive.management.entity.LoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志Mapper接口
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 根据用户ID查询登录日志
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC")
    List<LoginLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户名查询登录日志
     */
    @Select("SELECT * FROM sys_login_log WHERE username = #{username} ORDER BY login_time DESC")
    List<LoginLog> selectByUsername(@Param("username") String username);

    /**
     * 根据登录状态查询日志
     */
    @Select("SELECT * FROM sys_login_log WHERE status = #{status} ORDER BY login_time DESC")
    List<LoginLog> selectByStatus(@Param("status") Integer status);

    /**
     * 根据客户端IP查询登录日志
     */
    @Select("SELECT * FROM sys_login_log WHERE client_ip = #{clientIp} ORDER BY login_time DESC")
    List<LoginLog> selectByClientIp(@Param("clientIp") String clientIp);

    /**
     * 根据登录类型查询日志
     */
    @Select("SELECT * FROM sys_login_log WHERE login_type = #{loginType} ORDER BY login_time DESC")
    List<LoginLog> selectByLoginType(@Param("loginType") String loginType);

    /**
     * 查询指定时间范围内的登录日志
     */
    @Select("SELECT * FROM sys_login_log WHERE login_time BETWEEN #{startTime} AND #{endTime} ORDER BY login_time DESC")
    List<LoginLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户最近的登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT #{limit}")
    List<LoginLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询用户最后一次成功登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} AND status = 1 ORDER BY login_time DESC LIMIT 1")
    LoginLog selectLastSuccessLogin(@Param("userId") Long userId);

    /**
     * 查询用户最后一次登录记录（无论成功失败）
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT 1")
    LoginLog selectLastLogin(@Param("userId") Long userId);

    /**
     * 统计用户登录失败次数（指定时间范围内）
     */
    @Select("SELECT COUNT(*) FROM sys_login_log WHERE user_id = #{userId} AND status = 0 AND login_time >= #{startTime}")
    int countFailedLoginsSince(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计IP登录失败次数（指定时间范围内）
     */
    @Select("SELECT COUNT(*) FROM sys_login_log WHERE client_ip = #{clientIp} AND status = 0 AND login_time >= #{startTime}")
    int countFailedLoginsByIpSince(@Param("clientIp") String clientIp, @Param("startTime") LocalDateTime startTime);

    /**
     * 分页查询登录日志（带搜索条件）
     */
    IPage<LoginLog> selectPageWithConditions(Page<LoginLog> page,
                                           @Param("userId") Long userId,
                                           @Param("username") String username,
                                           @Param("clientIp") String clientIp,
                                           @Param("loginType") String loginType,
                                           @Param("status") Integer status,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计登录成功率
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as failed_count " +
            "FROM sys_login_log WHERE login_time >= #{startTime}")
    Map<String, Object> getLoginStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计用户登录统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as failed_count " +
            "FROM sys_login_log WHERE user_id = #{userId} AND login_time >= #{startTime}")
    Map<String, Object> getUserLoginStatistics(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取登录活跃用户统计
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM sys_login_log WHERE status = 1 AND login_time >= #{startTime}")
    int getActiveUserCount(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取每日登录统计
     */
    @Select("SELECT " +
            "DATE(login_time) as login_date, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as failed_count, " +
            "COUNT(DISTINCT user_id) as unique_users " +
            "FROM sys_login_log " +
            "WHERE login_time >= #{startTime} " +
            "GROUP BY DATE(login_time) " +
            "ORDER BY login_date DESC")
    List<Map<String, Object>> getDailyLoginStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取登录类型统计
     */
    @Select("SELECT " +
            "login_type, " +
            "COUNT(*) as count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM sys_login_log " +
            "WHERE login_time >= #{startTime} " +
            "GROUP BY login_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getLoginTypeStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取浏览器统计
     */
    @Select("SELECT " +
            "browser, " +
            "COUNT(*) as count " +
            "FROM sys_login_log " +
            "WHERE browser IS NOT NULL AND login_time >= #{startTime} " +
            "GROUP BY browser " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getBrowserStatistics(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 获取操作系统统计
     */
    @Select("SELECT " +
            "os, " +
            "COUNT(*) as count " +
            "FROM sys_login_log " +
            "WHERE os IS NOT NULL AND login_time >= #{startTime} " +
            "GROUP BY os " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getOsStatistics(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 获取地理位置统计
     */
    @Select("SELECT " +
            "client_location, " +
            "COUNT(*) as count " +
            "FROM sys_login_log " +
            "WHERE client_location IS NOT NULL AND login_time >= #{startTime} " +
            "GROUP BY client_location " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getLocationStatistics(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 获取异常登录记录（失败次数较多的IP）
     */
    @Select("SELECT " +
            "client_ip, " +
            "COUNT(*) as failed_count, " +
            "MAX(login_time) as last_attempt " +
            "FROM sys_login_log " +
            "WHERE status = 0 AND login_time >= #{startTime} " +
            "GROUP BY client_ip " +
            "HAVING failed_count >= #{threshold} " +
            "ORDER BY failed_count DESC")
    List<Map<String, Object>> getSuspiciousIps(@Param("startTime") LocalDateTime startTime, @Param("threshold") int threshold);

    /**
     * 删除指定时间之前的日志记录
     */
    @Delete("DELETE FROM sys_login_log WHERE login_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 清理用户的历史登录记录（保留最近N条）
     */
    @Delete("DELETE FROM sys_login_log WHERE user_id = #{userId} AND log_id NOT IN " +
            "(SELECT log_id FROM (SELECT log_id FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT #{keepCount}) t)")
    int cleanupUserLoginHistory(@Param("userId") Long userId, @Param("keepCount") int keepCount);

    /**
     * 获取在线用户数量（最近N分钟内有成功登录的用户）
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM sys_login_log WHERE status = 1 AND login_time >= #{sinceTime}")
    int getOnlineUserCount(@Param("sinceTime") LocalDateTime sinceTime);
}