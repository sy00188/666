package com.archive.management.mapper;

import com.archive.management.entity.AuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志数据访问层接口
 * 基于MyBatis-Plus实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 根据用户ID查找审计日志列表
     * @param userId 用户ID
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<AuditLog> findByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查找审计日志列表
     * @param operationType 操作类型
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE operation_type = #{operationType} ORDER BY created_at DESC")
    List<AuditLog> findByOperationType(@Param("operationType") String operationType);

    /**
     * 根据模块名称查找审计日志列表
     * @param moduleName 模块名称
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE module_name = #{moduleName} ORDER BY created_at DESC")
    List<AuditLog> findByModuleName(@Param("moduleName") String moduleName);

    /**
     * 根据操作结果查找审计日志列表
     * @param operationResult 操作结果
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE operation_result = #{operationResult} ORDER BY created_at DESC")
    List<AuditLog> findByOperationResult(@Param("operationResult") Integer operationResult);

    /**
     * 根据IP地址查找审计日志列表
     * @param ipAddress IP地址
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE ip_address = #{ipAddress} ORDER BY created_at DESC")
    List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * 根据用户代理查找审计日志列表
     * @param userAgent 用户代理
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE user_agent = #{userAgent} ORDER BY created_at DESC")
    List<AuditLog> findByUserAgent(@Param("userAgent") String userAgent);

    /**
     * 根据时间范围查找审计日志列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<AuditLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 根据风险等级查找审计日志列表
     * @param riskLevel 风险等级
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE risk_level = #{riskLevel} ORDER BY created_at DESC")
    List<AuditLog> findByRiskLevel(@Param("riskLevel") Integer riskLevel);

    /**
     * 根据关联实体查找审计日志列表
     * @param relatedEntityType 关联实体类型
     * @param relatedEntityId 关联实体ID
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE related_entity_type = #{relatedEntityType} " +
            "AND related_entity_id = #{relatedEntityId} ORDER BY created_at DESC")
    List<AuditLog> findByRelatedEntity(@Param("relatedEntityType") String relatedEntityType,
                                      @Param("relatedEntityId") Long relatedEntityId);

    /**
     * 分页查询审计日志
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param operationType 操作类型（可选）
     * @param moduleName 模块名称（可选）
     * @param operationResult 操作结果（可选）
     * @param riskLevel 风险等级（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM audit_logs WHERE 1=1" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='operationType != null and operationType != \"\"'> AND operation_type = #{operationType}</if>" +
            "<if test='moduleName != null and moduleName != \"\"'> AND module_name = #{moduleName}</if>" +
            "<if test='operationResult != null'> AND operation_result = #{operationResult}</if>" +
            "<if test='riskLevel != null'> AND risk_level = #{riskLevel}</if>" +
            "<if test='startTime != null'> AND created_at >= #{startTime}</if>" +
            "<if test='endTime != null'> AND created_at <= #{endTime}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    IPage<AuditLog> findAuditLogsWithPagination(Page<AuditLog> page,
                                               @Param("userId") Long userId,
                                               @Param("operationType") String operationType,
                                               @Param("moduleName") String moduleName,
                                               @Param("operationResult") Integer operationResult,
                                               @Param("riskLevel") Integer riskLevel,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计审计日志总数
     * @return 总数
     */
    @Select("SELECT COUNT(*) FROM audit_logs")
    long countAuditLogs();

    /**
     * 根据操作类型统计审计日志数量
     * @param operationType 操作类型
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE operation_type = #{operationType}")
    long countByOperationType(@Param("operationType") String operationType);

    /**
     * 根据模块名称统计审计日志数量
     * @param moduleName 模块名称
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE module_name = #{moduleName}")
    long countByModuleName(@Param("moduleName") String moduleName);

    /**
     * 根据操作结果统计审计日志数量
     * @param operationResult 操作结果
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE operation_result = #{operationResult}")
    long countByOperationResult(@Param("operationResult") Integer operationResult);

    /**
     * 根据风险等级统计审计日志数量
     * @param riskLevel 风险等级
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE risk_level = #{riskLevel}")
    long countByRiskLevel(@Param("riskLevel") Integer riskLevel);

    /**
     * 统计用户的操作次数
     * @param userId 用户ID
     * @return 操作次数
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE user_id = #{userId}")
    long countOperationsByUserId(@Param("userId") Long userId);

    /**
     * 统计时间范围内的审计日志数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE created_at BETWEEN #{startTime} AND #{endTime}")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime, 
                         @Param("endTime") LocalDateTime endTime);

    /**
     * 获取操作类型统计
     * @return 操作类型统计结果
     */
    @Select("SELECT operation_type, COUNT(*) as count FROM audit_logs GROUP BY operation_type ORDER BY count DESC")
    List<Map<String, Object>> getOperationTypeStatistics();

    /**
     * 获取模块名称统计
     * @return 模块名称统计结果
     */
    @Select("SELECT module_name, COUNT(*) as count FROM audit_logs GROUP BY module_name ORDER BY count DESC")
    List<Map<String, Object>> getModuleNameStatistics();

    /**
     * 获取操作结果统计
     * @return 操作结果统计结果
     */
    @Select("SELECT operation_result, COUNT(*) as count FROM audit_logs GROUP BY operation_result")
    List<Map<String, Object>> getOperationResultStatistics();

    /**
     * 获取风险等级统计
     * @return 风险等级统计结果
     */
    @Select("SELECT risk_level, COUNT(*) as count FROM audit_logs GROUP BY risk_level ORDER BY risk_level")
    List<Map<String, Object>> getRiskLevelStatistics();

    /**
     * 获取用户操作统计（Top N）
     * @param limit 限制数量
     * @return 用户操作统计结果
     */
    @Select("SELECT user_id, COUNT(*) as count FROM audit_logs WHERE user_id IS NOT NULL " +
            "GROUP BY user_id ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserOperationStatistics(@Param("limit") Integer limit);

    /**
     * 获取IP地址统计（Top N）
     * @param limit 限制数量
     * @return IP地址统计结果
     */
    @Select("SELECT ip_address, COUNT(*) as count FROM audit_logs WHERE ip_address IS NOT NULL " +
            "GROUP BY ip_address ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> getIpAddressStatistics(@Param("limit") Integer limit);

    /**
     * 获取每日操作统计
     * @param days 天数
     * @return 每日操作统计结果
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM audit_logs " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date DESC")
    List<Map<String, Object>> getDailyOperationStatistics(@Param("days") Integer days);

    /**
     * 获取每小时操作统计
     * @param hours 小时数
     * @return 每小时操作统计结果
     */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') as hour, COUNT(*) as count FROM audit_logs " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') ORDER BY hour DESC")
    List<Map<String, Object>> getHourlyOperationStatistics(@Param("hours") Integer hours);

    /**
     * 查找异常操作日志（高风险等级）
     * @param riskLevel 风险等级阈值
     * @param limit 限制数量
     * @return 异常操作日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE risk_level >= #{riskLevel} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> findAbnormalOperations(@Param("riskLevel") Integer riskLevel, 
                                         @Param("limit") Integer limit);

    /**
     * 查找失败操作日志
     * @param limit 限制数量
     * @return 失败操作日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE operation_result = 0 " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> findFailedOperations(@Param("limit") Integer limit);

    /**
     * 根据关键词搜索审计日志
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 审计日志列表
     */
    @Select("SELECT * FROM audit_logs WHERE " +
            "operation_description LIKE CONCAT('%', #{keyword}, '%') OR " +
            "operation_details LIKE CONCAT('%', #{keyword}, '%') OR " +
            "error_message LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> searchByKeyword(@Param("keyword") String keyword, 
                                  @Param("limit") Integer limit);

    /**
     * 清理过期的审计日志
     * @param expiredTime 过期时间
     * @return 清理数量
     */
    @Delete("DELETE FROM audit_logs WHERE created_at < #{expiredTime}")
    int cleanExpiredLogs(@Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 批量插入审计日志
     * @param auditLogs 审计日志列表
     * @return 插入数量
     */
    @Insert("<script>" +
            "INSERT INTO audit_logs (user_id, operation_type, module_name, operation_description, " +
            "operation_details, operation_result, error_message, ip_address, user_agent, " +
            "risk_level, related_entity_type, related_entity_id, tags, created_at) VALUES " +
            "<foreach collection='auditLogs' item='log' separator=','>" +
            "(#{log.userId}, #{log.operationType}, #{log.moduleName}, #{log.operationDescription}, " +
            "#{log.operationDetails}, #{log.operationResult}, #{log.errorMessage}, #{log.ipAddress}, " +
            "#{log.userAgent}, #{log.riskLevel}, #{log.relatedEntityType}, #{log.relatedEntityId}, " +
            "#{log.tags}, #{log.createdAt})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("auditLogs") List<AuditLog> auditLogs);
}