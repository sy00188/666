package com.archive.management.service;

import com.archive.management.entity.AuditLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志业务服务接口
 * 定义审计日志管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface AuditLogService {

    /**
     * 创建审计日志
     * @param auditLog 审计日志信息
     * @return 创建的审计日志
     */
    AuditLog createAuditLog(AuditLog auditLog);

    /**
     * 批量创建审计日志
     * @param auditLogs 审计日志列表
     * @return 创建成功的数量
     */
    int batchCreateAuditLogs(List<AuditLog> auditLogs);

    /**
     * 记录用户操作日志
     * @param userId 用户ID
     * @param action 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param description 操作描述
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param result 操作结果
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog logUserAction(Long userId, String action, String resourceType, Long resourceId, 
                          String description, String ipAddress, String userAgent, 
                          String result, String details);

    /**
     * 记录系统操作日志
     * @param action 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param description 操作描述
     * @param result 操作结果
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog logSystemAction(String action, String resourceType, Long resourceId, 
                           String description, String result, String details);

    /**
     * 记录登录日志
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param success 是否成功
     * @param failureReason 失败原因（如果失败）
     * @return 审计日志
     */
    AuditLog logLogin(Long userId, String username, String ipAddress, String userAgent, 
                     boolean success, String failureReason);

    /**
     * 记录登出日志
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 审计日志
     */
    AuditLog logLogout(Long userId, String username, String ipAddress, String userAgent);

    /**
     * 记录档案操作日志
     * @param userId 用户ID
     * @param action 操作类型（create/update/delete/view/download）
     * @param archiveId 档案ID
     * @param archiveTitle 档案标题
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog logArchiveAction(Long userId, String action, Long archiveId, String archiveTitle, 
                            String ipAddress, String userAgent, String details);

    /**
     * 记录用户操作（简化版）
     * @param userId 用户ID
     * @param action 操作类型
     * @param resourceId 资源ID
     * @param description 描述
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog recordUserOperation(Long userId, String action, Long resourceId, String description, String details);

    /**
     * 记录档案操作（简化版）
     * @param archiveId 档案ID
     * @param action 操作类型
     * @param userId 用户ID
     * @param description 描述
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog recordArchiveOperation(Long archiveId, String action, Long userId, String description, String details);

    /**
     * 记录操作日志（通用版）
     * @param module 模块
     * @param action 操作
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param description 描述
     * @param result 结果
     * @return 审计日志
     */
    AuditLog recordOperationLog(String module, String action, String resourceType, String resourceId, String description, String result);

    /**
     * 记录文件操作日志
     * @param userId 用户ID
     * @param action 操作类型（upload/download/delete/view）
     * @param fileId 文件ID
     * @param fileName 文件名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog logFileAction(Long userId, String action, Long fileId, String fileName, 
                         String ipAddress, String userAgent, String details);

    /**
     * 记录权限操作日志
     * @param userId 用户ID
     * @param action 操作类型（grant/revoke/modify）
     * @param targetUserId 目标用户ID
     * @param permissionInfo 权限信息
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 审计日志
     */
    AuditLog logPermissionAction(Long userId, String action, Long targetUserId, 
                               String permissionInfo, String ipAddress, String userAgent);

    /**
     * 记录配置变更日志
     * @param userId 用户ID
     * @param configKey 配置键
     * @param oldValue 旧值
     * @param newValue 新值
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 审计日志
     */
    AuditLog logConfigChange(Long userId, String configKey, String oldValue, String newValue, 
                           String ipAddress, String userAgent);

    /**
     * 记录安全事件日志
     * @param userId 用户ID（可选）
     * @param eventType 事件类型
     * @param severity 严重程度
     * @param description 事件描述
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param details 详细信息
     * @return 审计日志
     */
    AuditLog logSecurityEvent(Long userId, String eventType, String severity, 
                            String description, String ipAddress, String userAgent, String details);

    /**
     * 根据ID获取审计日志
     * @param id 日志ID
     * @return 审计日志
     */
    AuditLog getAuditLogById(Long id);

    /**
     * 分页查询审计日志
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param action 操作类型（可选）
     * @param resourceType 资源类型（可选）
     * @param resourceId 资源ID（可选）
     * @param result 操作结果（可选）
     * @param ipAddress IP地址（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<AuditLog> findAuditLogsWithPagination(Page<AuditLog> page, Long userId, String action, 
                                               String resourceType, Long resourceId, String result, 
                                               String ipAddress, LocalDateTime startDate, 
                                               LocalDateTime endDate);

    /**
     * 根据用户ID查找审计日志
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByUserId(Long userId, int limit);

    /**
     * 根据操作类型查找审计日志
     * @param action 操作类型
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByAction(String action, int limit);

    /**
     * 根据资源类型查找审计日志
     * @param resourceType 资源类型
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByResourceType(String resourceType, int limit);

    /**
     * 根据资源ID查找审计日志
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByResource(String resourceType, Long resourceId, int limit);

    /**
     * 根据IP地址查找审计日志
     * @param ipAddress IP地址
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByIpAddress(String ipAddress, int limit);

    /**
     * 根据操作结果查找审计日志
     * @param result 操作结果
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByResult(String result, int limit);

    /**
     * 根据时间范围查找审计日志
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit);

    /**
     * 查找失败的操作日志
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findFailedOperations(int limit);

    /**
     * 查找可疑活动日志
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findSuspiciousActivities(int limit);

    /**
     * 查找高风险操作日志
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AuditLog> findHighRiskOperations(int limit);

    /**
     * 统计审计日志总数
     * @return 总数
     */
    long countAuditLogs();

    /**
     * 根据用户统计日志数量
     * @param userId 用户ID
     * @return 数量
     */
    long countAuditLogsByUserId(Long userId);

    /**
     * 根据操作类型统计日志数量
     * @param action 操作类型
     * @return 数量
     */
    long countAuditLogsByAction(String action);

    /**
     * 根据资源类型统计日志数量
     * @param resourceType 资源类型
     * @return 数量
     */
    long countAuditLogsByResourceType(String resourceType);

    /**
     * 根据操作结果统计日志数量
     * @param result 操作结果
     * @return 数量
     */
    long countAuditLogsByResult(String result);

    /**
     * 根据时间范围统计日志数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 数量
     */
    long countAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计失败操作数量
     * @return 数量
     */
    long countFailedOperations();

    /**
     * 统计可疑活动数量
     * @return 数量
     */
    long countSuspiciousActivities();

    /**
     * 获取操作类型统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getActionStatistics(int days);

    /**
     * 获取用户活动统计
     * @param days 统计天数
     * @param limit 用户数量限制
     * @return 统计结果
     */
    List<Map<String, Object>> getUserActivityStatistics(int days, int limit);

    /**
     * 获取资源访问统计
     * @param resourceType 资源类型
     * @param days 统计天数
     * @param limit 资源数量限制
     * @return 统计结果
     */
    List<Map<String, Object>> getResourceAccessStatistics(String resourceType, int days, int limit);

    /**
     * 获取IP地址统计
     * @param days 统计天数
     * @param limit IP数量限制
     * @return 统计结果
     */
    List<Map<String, Object>> getIpAddressStatistics(int days, int limit);

    /**
     * 获取操作结果统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getOperationResultStatistics(int days);

    /**
     * 获取时间分布统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getTimeDistributionStatistics(int days);

    /**
     * 获取登录统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getLoginStatistics(int days);

    /**
     * 获取档案操作统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getArchiveOperationStatistics(int days);

    /**
     * 获取文件操作统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getFileOperationStatistics(int days);

    /**
     * 获取安全事件统计
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getSecurityEventStatistics(int days);

    /**
     * 检测异常活动
     * @param userId 用户ID（可选）
     * @param hours 检测时间范围（小时）
     * @return 异常活动列表
     */
    List<Map<String, Object>> detectAnomalousActivity(Long userId, int hours);

    /**
     * 检测暴力破解攻击
     * @param hours 检测时间范围（小时）
     * @param threshold 阈值
     * @return 攻击列表
     */
    List<Map<String, Object>> detectBruteForceAttacks(int hours, int threshold);

    /**
     * 检测权限提升
     * @param hours 检测时间范围（小时）
     * @return 权限提升事件列表
     */
    List<Map<String, Object>> detectPrivilegeEscalation(int hours);

    /**
     * 检测数据泄露风险
     * @param hours 检测时间范围（小时）
     * @return 风险事件列表
     */
    List<Map<String, Object>> detectDataLeakageRisk(int hours);

    /**
     * 生成安全报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param reportType 报告类型
     * @return 报告数据
     */
    Map<String, Object> generateSecurityReport(LocalDateTime startDate, LocalDateTime endDate, String reportType);

    /**
     * 生成合规报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param complianceType 合规类型
     * @return 报告数据
     */
    Map<String, Object> generateComplianceReport(LocalDateTime startDate, LocalDateTime endDate, String complianceType);

    /**
     * 导出审计日志
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param format 导出格式（csv/excel/json）
     * @param filters 过滤条件
     * @return 导出结果
     */
    Map<String, Object> exportAuditLogs(LocalDateTime startDate, LocalDateTime endDate, 
                                       String format, Map<String, Object> filters);

    /**
     * 归档审计日志
     * @param beforeDate 归档日期之前的日志
     * @param archivePath 归档路径
     * @return 归档结果
     */
    Map<String, Object> archiveAuditLogs(LocalDateTime beforeDate, String archivePath);

    /**
     * 清理过期审计日志
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    Map<String, Object> cleanupExpiredAuditLogs(int retentionDays);

    /**
     * 压缩审计日志
     * @param beforeDate 压缩日期之前的日志
     * @return 压缩结果
     */
    Map<String, Object> compressAuditLogs(LocalDateTime beforeDate);

    /**
     * 验证日志完整性
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 验证结果
     */
    Map<String, Object> verifyLogIntegrity(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 搜索审计日志
     * @param keyword 关键词
     * @param searchFields 搜索字段
     * @param filters 过滤条件
     * @param page 分页参数
     * @return 搜索结果
     */
    IPage<AuditLog> searchAuditLogs(String keyword, List<String> searchFields, 
                                   Map<String, Object> filters, Page<AuditLog> page);

    /**
     * 获取用户操作轨迹
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 操作轨迹
     */
    List<Map<String, Object>> getUserOperationTrail(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取资源访问轨迹
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 访问轨迹
     */
    List<Map<String, Object>> getResourceAccessTrail(String resourceType, Long resourceId, 
                                                    LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取系统活动概览
     * @param hours 时间范围（小时）
     * @return 活动概览
     */
    Map<String, Object> getSystemActivityOverview(int hours);

    /**
     * 获取实时监控数据
     * @return 监控数据
     */
    Map<String, Object> getRealTimeMonitoringData();

    /**
     * 设置日志告警规则
     * @param ruleName 规则名称
     * @param conditions 告警条件
     * @param actions 告警动作
     * @param createdBy 创建人ID
     * @return 是否设置成功
     */
    boolean setLogAlertRule(String ruleName, Map<String, Object> conditions, 
                          Map<String, Object> actions, Long createdBy);

    /**
     * 检查告警规则
     * @param auditLog 审计日志
     * @return 触发的告警规则列表
     */
    List<Map<String, Object>> checkAlertRules(AuditLog auditLog);

    /**
     * 发送告警通知
     * @param alertRule 告警规则
     * @param auditLog 触发的审计日志
     * @return 是否发送成功
     */
    boolean sendAlertNotification(Map<String, Object> alertRule, AuditLog auditLog);

    /**
     * 获取告警历史
     * @param days 天数
     * @return 告警历史
     */
    List<Map<String, Object>> getAlertHistory(int days);

    /**
     * 刷新日志缓存
     * @return 是否刷新成功
     */
    boolean refreshLogCache();

    /**
     * 重建日志索引
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 重建结果
     */
    Map<String, Object> rebuildLogIndex(LocalDateTime startDate, LocalDateTime endDate);
}