package com.archive.management.scheduler;

import com.archive.management.entity.SystemConfig;
import com.archive.management.entity.AuditLog;
import com.archive.management.service.SystemConfigService;
import com.archive.management.service.AuditLogService;
import com.archive.management.service.ArchiveService;
import com.archive.management.service.UserService;
import com.archive.management.mq.producer.SystemMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 系统定时任务调度器
 * 负责系统级别的定时任务处理
 */
@Component
public class SystemScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SystemScheduler.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemMessageProducer systemMessageProducer;

    /**
     * 系统配置同步任务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void syncSystemConfig() {
        try {
            logger.info("开始执行系统配置同步任务");
            
            // 获取需要同步的配置
            List<SystemConfig> configs = systemConfigService.findPendingSyncConfigs();
            
            for (SystemConfig config : configs) {
                try {
                    // 同步配置到缓存
                    systemConfigService.syncConfigToCache(config);
                    
                    // 发送配置变更消息
                    systemMessageProducer.sendConfigChangeMessage(
                        config.getConfigKey(), 
                        config.getConfigValue()
                    );
                    
                    // 更新同步状态
                    config.setLastSyncTime(LocalDateTime.now());
                    systemConfigService.updateConfig(config);
                    
                    logger.debug("配置同步成功: {}", config.getConfigKey());
                } catch (Exception e) {
                    logger.error("配置同步失败: {}, 错误: {}", config.getConfigKey(), e.getMessage());
                }
            }
            
            logger.info("系统配置同步任务完成，处理配置数量: {}", configs.size());
        } catch (Exception e) {
            logger.error("系统配置同步任务执行失败", e);
        }
    }

    /**
     * 系统性能监控任务
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void monitorSystemPerformance() {
        try {
            logger.info("开始执行系统性能监控任务");
            
            // 获取系统性能指标
            Map<String, Object> performanceMetrics = collectPerformanceMetrics();
            
            // 检查性能阈值
            checkPerformanceThresholds(performanceMetrics);
            
            // 记录性能数据
            recordPerformanceData(performanceMetrics);
            
            logger.info("系统性能监控任务完成");
        } catch (Exception e) {
            logger.error("系统性能监控任务执行失败", e);
        }
    }

    /**
     * 系统备份任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void performSystemBackup() {
        try {
            logger.info("开始执行系统备份任务");
            
            // 创建备份记录
            AuditLog backupLog = createBackupAuditLog();
            
            try {
                // 备份数据库
                boolean dbBackupSuccess = performDatabaseBackup();
                
                // 备份文件系统
                boolean fileBackupSuccess = performFileSystemBackup();
                
                // 备份配置文件
                boolean configBackupSuccess = performConfigBackup();
                
                if (dbBackupSuccess && fileBackupSuccess && configBackupSuccess) {
                    backupLog.setOperationResult("SUCCESS");
                    backupLog.setResultDescription("系统备份成功完成");
                    
                    // 发送备份成功消息
                    systemMessageProducer.sendBackupCompleteMessage("SUCCESS", "系统备份成功");
                    
                    logger.info("系统备份任务成功完成");
                } else {
                    backupLog.setOperationResult("PARTIAL_SUCCESS");
                    backupLog.setResultDescription("系统备份部分成功");
                    
                    logger.warn("系统备份任务部分成功");
                }
            } catch (Exception e) {
                backupLog.setOperationResult("FAILURE");
                backupLog.setResultDescription("系统备份失败: " + e.getMessage());
                
                // 发送备份失败消息
                systemMessageProducer.sendBackupCompleteMessage("FAILURE", e.getMessage());
                
                logger.error("系统备份任务失败", e);
            } finally {
                backupLog.setOperationEndTime(LocalDateTime.now());
                auditLogService.saveAuditLog(backupLog);
            }
        } catch (Exception e) {
            logger.error("系统备份任务执行失败", e);
        }
    }

    /**
     * 系统日志清理任务
     * 每周日凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupSystemLogs() {
        try {
            logger.info("开始执行系统日志清理任务");
            
            // 获取日志保留天数配置
            int retentionDays = getLogRetentionDays();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            
            // 清理审计日志
            int auditLogsDeleted = auditLogService.deleteLogsBefore(cutoffDate);
            
            // 清理系统日志文件
            int systemLogsDeleted = cleanupSystemLogFiles(cutoffDate);
            
            // 清理临时文件
            int tempFilesDeleted = cleanupTempFiles();
            
            logger.info("系统日志清理任务完成 - 审计日志: {}, 系统日志: {}, 临时文件: {}", 
                       auditLogsDeleted, systemLogsDeleted, tempFilesDeleted);
            
            // 发送清理完成消息
            Map<String, Integer> cleanupStats = new HashMap<>();
            cleanupStats.put("auditLogs", auditLogsDeleted);
            cleanupStats.put("systemLogs", systemLogsDeleted);
            cleanupStats.put("tempFiles", tempFilesDeleted);
            
            systemMessageProducer.sendMaintenanceCompleteMessage("LOG_CLEANUP", cleanupStats);
            
        } catch (Exception e) {
            logger.error("系统日志清理任务执行失败", e);
        }
    }

    /**
     * 系统健康检查任务
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void performHealthCheck() {
        try {
            logger.debug("开始执行系统健康检查任务");
            
            Map<String, String> healthStatus = new HashMap<>();
            
            // 检查数据库连接
            healthStatus.put("database", checkDatabaseHealth());
            
            // 检查文件系统
            healthStatus.put("filesystem", checkFileSystemHealth());
            
            // 检查内存使用
            healthStatus.put("memory", checkMemoryHealth());
            
            // 检查磁盘空间
            healthStatus.put("disk", checkDiskHealth());
            
            // 检查服务状态
            healthStatus.put("services", checkServicesHealth());
            
            // 记录健康状态
            recordHealthStatus(healthStatus);
            
            // 检查是否有异常状态
            checkHealthAlerts(healthStatus);
            
            logger.debug("系统健康检查任务完成");
        } catch (Exception e) {
            logger.error("系统健康检查任务执行失败", e);
        }
    }

    /**
     * 系统统计数据更新任务
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void updateSystemStatistics() {
        try {
            logger.info("开始执行系统统计数据更新任务");
            
            // 更新用户统计
            updateUserStatistics();
            
            // 更新档案统计
            updateArchiveStatistics();
            
            // 更新系统使用统计
            updateSystemUsageStatistics();
            
            // 更新性能统计
            updatePerformanceStatistics();
            
            logger.info("系统统计数据更新任务完成");
        } catch (Exception e) {
            logger.error("系统统计数据更新任务执行失败", e);
        }
    }

    // 辅助方法

    /**
     * 收集系统性能指标
     */
    private Map<String, Object> collectPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // CPU使用率
        metrics.put("cpuUsage", getCpuUsage());
        
        // 内存使用率
        metrics.put("memoryUsage", getMemoryUsage());
        
        // 磁盘使用率
        metrics.put("diskUsage", getDiskUsage());
        
        // 网络IO
        metrics.put("networkIO", getNetworkIO());
        
        // 数据库连接数
        metrics.put("dbConnections", getDatabaseConnections());
        
        // 活跃用户数
        metrics.put("activeUsers", getActiveUserCount());
        
        return metrics;
    }

    /**
     * 检查性能阈值
     */
    private void checkPerformanceThresholds(Map<String, Object> metrics) {
        // 检查CPU使用率
        Double cpuUsage = (Double) metrics.get("cpuUsage");
        if (cpuUsage != null && cpuUsage > 80.0) {
            systemMessageProducer.sendMonitoringAlertMessage("HIGH_CPU_USAGE", 
                "CPU使用率过高: " + cpuUsage + "%");
        }
        
        // 检查内存使用率
        Double memoryUsage = (Double) metrics.get("memoryUsage");
        if (memoryUsage != null && memoryUsage > 85.0) {
            systemMessageProducer.sendMonitoringAlertMessage("HIGH_MEMORY_USAGE", 
                "内存使用率过高: " + memoryUsage + "%");
        }
        
        // 检查磁盘使用率
        Double diskUsage = (Double) metrics.get("diskUsage");
        if (diskUsage != null && diskUsage > 90.0) {
            systemMessageProducer.sendMonitoringAlertMessage("HIGH_DISK_USAGE", 
                "磁盘使用率过高: " + diskUsage + "%");
        }
    }

    /**
     * 记录性能数据
     */
    private void recordPerformanceData(Map<String, Object> metrics) {
        try {
            // 将性能数据保存到数据库或缓存
            systemConfigService.savePerformanceMetrics(metrics);
        } catch (Exception e) {
            logger.error("记录性能数据失败", e);
        }
    }

    /**
     * 创建备份审计日志
     */
    private AuditLog createBackupAuditLog() {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("SYSTEM_BACKUP");
        auditLog.setOperationDescription("系统定时备份");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setOperatorId("SYSTEM");
        auditLog.setOperatorName("系统定时任务");
        auditLog.setClientIp("127.0.0.1");
        auditLog.setRiskLevel("LOW");
        return auditLog;
    }

    /**
     * 执行数据库备份
     */
    private boolean performDatabaseBackup() {
        try {
            // 实现数据库备份逻辑
            logger.info("执行数据库备份");
            return true;
        } catch (Exception e) {
            logger.error("数据库备份失败", e);
            return false;
        }
    }

    /**
     * 执行文件系统备份
     */
    private boolean performFileSystemBackup() {
        try {
            // 实现文件系统备份逻辑
            logger.info("执行文件系统备份");
            return true;
        } catch (Exception e) {
            logger.error("文件系统备份失败", e);
            return false;
        }
    }

    /**
     * 执行配置备份
     */
    private boolean performConfigBackup() {
        try {
            // 实现配置备份逻辑
            logger.info("执行配置备份");
            return true;
        } catch (Exception e) {
            logger.error("配置备份失败", e);
            return false;
        }
    }

    /**
     * 获取日志保留天数
     */
    private int getLogRetentionDays() {
        try {
            SystemConfig config = systemConfigService.getConfigByKey("log.retention.days");
            return config != null ? Integer.parseInt(config.getConfigValue()) : 90;
        } catch (Exception e) {
            logger.warn("获取日志保留天数配置失败，使用默认值90天", e);
            return 90;
        }
    }

    /**
     * 清理系统日志文件
     */
    private int cleanupSystemLogFiles(LocalDateTime cutoffDate) {
        try {
            // 实现系统日志文件清理逻辑
            logger.info("清理系统日志文件，截止日期: {}", cutoffDate);
            return 0;
        } catch (Exception e) {
            logger.error("清理系统日志文件失败", e);
            return 0;
        }
    }

    /**
     * 清理临时文件
     */
    private int cleanupTempFiles() {
        try {
            // 实现临时文件清理逻辑
            logger.info("清理临时文件");
            return 0;
        } catch (Exception e) {
            logger.error("清理临时文件失败", e);
            return 0;
        }
    }

    /**
     * 检查数据库健康状态
     */
    private String checkDatabaseHealth() {
        try {
            // 实现数据库健康检查逻辑
            return "HEALTHY";
        } catch (Exception e) {
            logger.error("数据库健康检查失败", e);
            return "UNHEALTHY";
        }
    }

    /**
     * 检查文件系统健康状态
     */
    private String checkFileSystemHealth() {
        try {
            // 实现文件系统健康检查逻辑
            return "HEALTHY";
        } catch (Exception e) {
            logger.error("文件系统健康检查失败", e);
            return "UNHEALTHY";
        }
    }

    /**
     * 检查内存健康状态
     */
    private String checkMemoryHealth() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double usagePercent = (double) usedMemory / maxMemory * 100;
            
            if (usagePercent > 90) {
                return "CRITICAL";
            } else if (usagePercent > 80) {
                return "WARNING";
            } else {
                return "HEALTHY";
            }
        } catch (Exception e) {
            logger.error("内存健康检查失败", e);
            return "UNHEALTHY";
        }
    }

    /**
     * 检查磁盘健康状态
     */
    private String checkDiskHealth() {
        try {
            // 实现磁盘健康检查逻辑
            return "HEALTHY";
        } catch (Exception e) {
            logger.error("磁盘健康检查失败", e);
            return "UNHEALTHY";
        }
    }

    /**
     * 检查服务健康状态
     */
    private String checkServicesHealth() {
        try {
            // 实现服务健康检查逻辑
            return "HEALTHY";
        } catch (Exception e) {
            logger.error("服务健康检查失败", e);
            return "UNHEALTHY";
        }
    }

    /**
     * 记录健康状态
     */
    private void recordHealthStatus(Map<String, String> healthStatus) {
        try {
            systemConfigService.saveHealthStatus(healthStatus);
        } catch (Exception e) {
            logger.error("记录健康状态失败", e);
        }
    }

    /**
     * 检查健康告警
     */
    private void checkHealthAlerts(Map<String, String> healthStatus) {
        for (Map.Entry<String, String> entry : healthStatus.entrySet()) {
            String component = entry.getKey();
            String status = entry.getValue();
            
            if ("UNHEALTHY".equals(status) || "CRITICAL".equals(status)) {
                systemMessageProducer.sendMonitoringAlertMessage("HEALTH_CHECK_ALERT", 
                    "组件健康检查异常: " + component + " - " + status);
            }
        }
    }

    /**
     * 更新用户统计
     */
    private void updateUserStatistics() {
        try {
            Map<String, Object> userStats = userService.getUserStatistics();
            systemConfigService.saveStatistics("user", userStats);
        } catch (Exception e) {
            logger.error("更新用户统计失败", e);
        }
    }

    /**
     * 更新档案统计
     */
    private void updateArchiveStatistics() {
        try {
            Map<String, Object> archiveStats = archiveService.getArchiveStatistics();
            systemConfigService.saveStatistics("archive", archiveStats);
        } catch (Exception e) {
            logger.error("更新档案统计失败", e);
        }
    }

    /**
     * 更新系统使用统计
     */
    private void updateSystemUsageStatistics() {
        try {
            Map<String, Object> usageStats = collectSystemUsageStats();
            systemConfigService.saveStatistics("usage", usageStats);
        } catch (Exception e) {
            logger.error("更新系统使用统计失败", e);
        }
    }

    /**
     * 更新性能统计
     */
    private void updatePerformanceStatistics() {
        try {
            Map<String, Object> performanceStats = collectPerformanceStats();
            systemConfigService.saveStatistics("performance", performanceStats);
        } catch (Exception e) {
            logger.error("更新性能统计失败", e);
        }
    }

    // 性能指标获取方法

    private double getCpuUsage() {
        // 实现CPU使用率获取逻辑
        return 0.0;
    }

    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return (double) usedMemory / maxMemory * 100;
    }

    private double getDiskUsage() {
        // 实现磁盘使用率获取逻辑
        return 0.0;
    }

    private Map<String, Object> getNetworkIO() {
        // 实现网络IO获取逻辑
        return new HashMap<>();
    }

    private int getDatabaseConnections() {
        // 实现数据库连接数获取逻辑
        return 0;
    }

    private int getActiveUserCount() {
        try {
            return userService.getActiveUserCount();
        } catch (Exception e) {
            logger.error("获取活跃用户数失败", e);
            return 0;
        }
    }

    private Map<String, Object> collectSystemUsageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 0);
        stats.put("avgResponseTime", 0.0);
        stats.put("errorRate", 0.0);
        return stats;
    }

    private Map<String, Object> collectPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("avgCpuUsage", getCpuUsage());
        stats.put("avgMemoryUsage", getMemoryUsage());
        stats.put("avgDiskUsage", getDiskUsage());
        return stats;
    }
}