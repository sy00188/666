package com.archive.management.mapper;

import com.archive.management.entity.OperationLog;
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
 * 操作日志Mapper接口
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 根据用户ID查询操作日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<OperationLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户名查询操作日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE username = #{username} ORDER BY create_time DESC")
    List<OperationLog> selectByUsername(@Param("username") String username);

    /**
     * 根据操作类型查询日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE operation_type = #{operationType} ORDER BY create_time DESC")
    List<OperationLog> selectByOperationType(@Param("operationType") String operationType);

    /**
     * 根据目标类型查询日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE target_type = #{targetType} ORDER BY create_time DESC")
    List<OperationLog> selectByTargetType(@Param("targetType") String targetType);

    /**
     * 根据目标类型和目标ID查询日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE target_type = #{targetType} AND target_id = #{targetId} ORDER BY create_time DESC")
    List<OperationLog> selectByTargetTypeAndId(@Param("targetType") String targetType, @Param("targetId") String targetId);

    /**
     * 根据操作状态查询日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE status = #{status} ORDER BY create_time DESC")
    List<OperationLog> selectByStatus(@Param("status") Integer status);

    /**
     * 根据客户端IP查询操作日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE client_ip = #{clientIp} ORDER BY create_time DESC")
    List<OperationLog> selectByClientIp(@Param("clientIp") String clientIp);

    /**
     * 查询指定时间范围内的操作日志
     */
    @Select("SELECT * FROM sys_operation_log WHERE create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time DESC")
    List<OperationLog> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户最近的操作记录
     */
    @Select("SELECT * FROM sys_operation_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<OperationLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询执行时间超过阈值的慢操作
     */
    @Select("SELECT * FROM sys_operation_log WHERE execution_time > #{threshold} ORDER BY execution_time DESC")
    List<OperationLog> selectSlowOperations(@Param("threshold") Integer threshold);

    /**
     * 查询失败的操作记录
     */
    @Select("SELECT * FROM sys_operation_log WHERE status = 0 ORDER BY create_time DESC")
    List<OperationLog> selectFailedOperations();

    /**
     * 分页查询操作日志（带搜索条件）
     */
    IPage<OperationLog> selectPageWithConditions(Page<OperationLog> page,
                                                @Param("userId") Long userId,
                                                @Param("username") String username,
                                                @Param("operationType") String operationType,
                                                @Param("targetType") String targetType,
                                                @Param("targetId") String targetId,
                                                @Param("clientIp") String clientIp,
                                                @Param("status") Integer status,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作成功率
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as failed_count " +
            "FROM sys_operation_log WHERE create_time >= #{startTime}")
    Map<String, Object> getOperationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计用户操作统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as failed_count, " +
            "AVG(execution_time) as avg_execution_time " +
            "FROM sys_operation_log WHERE user_id = #{userId} AND create_time >= #{startTime}")
    Map<String, Object> getUserOperationStatistics(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取操作类型统计
     */
    @Select("SELECT " +
            "operation_type, " +
            "COUNT(*) as count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "AVG(execution_time) as avg_execution_time " +
            "FROM sys_operation_log " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY operation_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getOperationTypeStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取目标类型统计
     */
    @Select("SELECT " +
            "target_type, " +
            "COUNT(*) as count " +
            "FROM sys_operation_log " +
            "WHERE target_type IS NOT NULL AND create_time >= #{startTime} " +
            "GROUP BY target_type " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getTargetTypeStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取每日操作统计
     */
    @Select("SELECT " +
            "DATE(create_time) as operation_date, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as failed_count, " +
            "COUNT(DISTINCT user_id) as unique_users, " +
            "AVG(execution_time) as avg_execution_time " +
            "FROM sys_operation_log " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY operation_date DESC")
    List<Map<String, Object>> getDailyOperationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取每小时操作统计
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') as operation_hour, " +
            "COUNT(*) as count " +
            "FROM sys_operation_log " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY operation_hour DESC")
    List<Map<String, Object>> getHourlyOperationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取活跃用户统计
     */
    @Select("SELECT " +
            "user_id, " +
            "username, " +
            "COUNT(*) as operation_count, " +
            "MAX(create_time) as last_operation_time " +
            "FROM sys_operation_log " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY user_id, username " +
            "ORDER BY operation_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getActiveUserStatistics(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 获取请求方法统计
     */
    @Select("SELECT " +
            "request_method, " +
            "COUNT(*) as count, " +
            "AVG(execution_time) as avg_execution_time " +
            "FROM sys_operation_log " +
            "WHERE request_method IS NOT NULL AND create_time >= #{startTime} " +
            "GROUP BY request_method " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getRequestMethodStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取异常操作记录（失败次数较多的用户或IP）
     */
    @Select("SELECT " +
            "user_id, " +
            "username, " +
            "client_ip, " +
            "COUNT(*) as failed_count, " +
            "MAX(create_time) as last_failed_time " +
            "FROM sys_operation_log " +
            "WHERE status = 0 AND create_time >= #{startTime} " +
            "GROUP BY user_id, username, client_ip " +
            "HAVING failed_count >= #{threshold} " +
            "ORDER BY failed_count DESC")
    List<Map<String, Object>> getSuspiciousOperations(@Param("startTime") LocalDateTime startTime, @Param("threshold") int threshold);

    /**
     * 获取性能统计（平均执行时间最长的操作）
     */
    @Select("SELECT " +
            "operation_type, " +
            "COUNT(*) as count, " +
            "AVG(execution_time) as avg_execution_time, " +
            "MAX(execution_time) as max_execution_time, " +
            "MIN(execution_time) as min_execution_time " +
            "FROM sys_operation_log " +
            "WHERE execution_time IS NOT NULL AND create_time >= #{startTime} " +
            "GROUP BY operation_type " +
            "ORDER BY avg_execution_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getPerformanceStatistics(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 获取错误统计
     */
    @Select("SELECT " +
            "error_msg, " +
            "COUNT(*) as count, " +
            "MAX(create_time) as last_occurrence " +
            "FROM sys_operation_log " +
            "WHERE status = 0 AND error_msg IS NOT NULL AND create_time >= #{startTime} " +
            "GROUP BY error_msg " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getErrorStatistics(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 删除指定时间之前的日志记录
     */
    @Delete("DELETE FROM sys_operation_log WHERE create_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 清理用户的历史操作记录（保留最近N条）
     */
    @Delete("DELETE FROM sys_operation_log WHERE user_id = #{userId} AND log_id NOT IN " +
            "(SELECT log_id FROM (SELECT log_id FROM sys_operation_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{keepCount}) t)")
    int cleanupUserOperationHistory(@Param("userId") Long userId, @Param("keepCount") int keepCount);

    /**
     * 统计指定时间段内的操作总数
     */
    @Select("SELECT COUNT(*) FROM sys_operation_log WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    int countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取最近的系统操作记录
     */
    @Select("SELECT * FROM sys_operation_log ORDER BY create_time DESC LIMIT #{limit}")
    List<OperationLog> selectRecentOperations(@Param("limit") int limit);
}