package com.archive.management.scheduler;

import com.archive.management.entity.Archive;
import com.archive.management.entity.ArchiveFile;
import com.archive.management.entity.AuditLog;
import com.archive.management.service.ArchiveService;
import com.archive.management.service.ArchiveFileService;
import com.archive.management.service.AuditLogService;
import com.archive.management.service.SystemConfigService;
import com.archive.management.mq.producer.ArchiveMessageProducer;
import com.archive.management.mq.producer.SystemMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 档案定时任务调度器
 * 负责处理档案相关的定时任务，包括自动归档、清理、备份、监控等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveScheduler {

    private final ArchiveService archiveService;
    private final ArchiveFileService archiveFileService;
    private final AuditLogService auditLogService;
    private final SystemConfigService systemConfigService;
    private final ArchiveMessageProducer archiveMessageProducer;
    private final SystemMessageProducer systemMessageProducer;

    /**
     * 自动归档任务
     * 每天凌晨2点执行，将符合条件的档案进行自动归档
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void autoArchiveTask() {
        log.info("开始执行自动归档任务");
        
        try {
            // 获取自动归档配置
            Integer autoArchiveDays = systemConfigService.getIntValue("auto.archive.days", 30);
            Boolean autoArchiveEnabled = systemConfigService.getBooleanValue("auto.archive.enabled", true);
            
            if (!autoArchiveEnabled) {
                log.info("自动归档功能已禁用，跳过任务");
                return;
            }
            
            // 查找需要归档的档案
            LocalDateTime archiveDate = LocalDateTime.now().minusDays(autoArchiveDays);
            List<Archive> archivesToProcess = archiveService.findArchivesForAutoArchive(archiveDate);
            
            log.info("找到 {} 个档案需要自动归档", archivesToProcess.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (Archive archive : archivesToProcess) {
                try {
                    // 执行归档操作
                    archiveService.performAutoArchive(archive.getId());
                    
                    // 发送归档消息
                    archiveMessageProducer.sendArchiveMessage(archive.getId(), "AUTO_ARCHIVE", 
                        Map.of("archiveDate", archiveDate.toString()));
                    
                    successCount++;
                    log.debug("档案 {} 自动归档成功", archive.getId());
                    
                } catch (Exception e) {
                    failureCount++;
                    log.error("档案 {} 自动归档失败: {}", archive.getId(), e.getMessage(), e);
                    
                    // 记录审计日志
                    auditLogService.recordOperationLog("SYSTEM", "AUTO_ARCHIVE", 
                        "Archive", archive.getId().toString(), "FAILURE", 
                        "自动归档失败: " + e.getMessage());
                }
            }
            
            // 发送任务完成通知
            systemMessageProducer.sendSystemMessage("AUTO_ARCHIVE_COMPLETE", 
                Map.of("totalCount", archivesToProcess.size(), 
                       "successCount", successCount, 
                       "failureCount", failureCount));
            
            log.info("自动归档任务完成，成功: {}, 失败: {}", successCount, failureCount);
            
        } catch (Exception e) {
            log.error("自动归档任务执行失败", e);
            systemMessageProducer.sendSystemMessage("AUTO_ARCHIVE_ERROR", 
                Map.of("error", e.getMessage()));
        }
    }

    /**
     * 档案文件清理任务
     * 每天凌晨3点执行，清理过期的临时文件和垃圾文件
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredFilesTask() {
        log.info("开始执行档案文件清理任务");
        
        try {
            // 获取清理配置
            Integer tempFileRetentionDays = systemConfigService.getIntValue("temp.file.retention.days", 7);
            Integer deletedFileRetentionDays = systemConfigService.getIntValue("deleted.file.retention.days", 30);
            Boolean cleanupEnabled = systemConfigService.getBooleanValue("file.cleanup.enabled", true);
            
            if (!cleanupEnabled) {
                log.info("文件清理功能已禁用，跳过任务");
                return;
            }
            
            // 清理临时文件
            LocalDateTime tempFileExpireDate = LocalDateTime.now().minusDays(tempFileRetentionDays);
            List<ArchiveFile> tempFilesToClean = archiveFileService.findExpiredTempFiles(tempFileExpireDate);
            
            // 清理已删除文件
            LocalDateTime deletedFileExpireDate = LocalDateTime.now().minusDays(deletedFileRetentionDays);
            List<ArchiveFile> deletedFilesToClean = archiveFileService.findExpiredDeletedFiles(deletedFileExpireDate);
            
            int tempFilesCleanedCount = 0;
            int deletedFilesCleanedCount = 0;
            long totalSpaceFreed = 0;
            
            // 异步清理临时文件
            CompletableFuture<Void> tempFileCleanup = CompletableFuture.runAsync(() -> {
                for (ArchiveFile file : tempFilesToClean) {
                    try {
                        long fileSize = file.getFileSize();
                        archiveFileService.permanentlyDeleteFile(file.getId());
                        tempFilesCleanedCount++;
                        totalSpaceFreed += fileSize;
                        
                        log.debug("清理临时文件: {}", file.getFileName());
                    } catch (Exception e) {
                        log.error("清理临时文件失败: {}", file.getFileName(), e);
                    }
                }
            });
            
            // 异步清理已删除文件
            CompletableFuture<Void> deletedFileCleanup = CompletableFuture.runAsync(() -> {
                for (ArchiveFile file : deletedFilesToClean) {
                    try {
                        long fileSize = file.getFileSize();
                        archiveFileService.permanentlyDeleteFile(file.getId());
                        deletedFilesCleanedCount++;
                        totalSpaceFreed += fileSize;
                        
                        log.debug("清理已删除文件: {}", file.getFileName());
                    } catch (Exception e) {
                        log.error("清理已删除文件失败: {}", file.getFileName(), e);
                    }
                }
            });
            
            // 等待清理任务完成
            CompletableFuture.allOf(tempFileCleanup, deletedFileCleanup).join();
            
            // 发送清理完成通知
            systemMessageProducer.sendSystemMessage("FILE_CLEANUP_COMPLETE", 
                Map.of("tempFilesCount", tempFilesCleanedCount,
                       "deletedFilesCount", deletedFilesCleanedCount,
                       "totalSpaceFreed", totalSpaceFreed));
            
            log.info("文件清理任务完成，清理临时文件: {}, 清理已删除文件: {}, 释放空间: {} bytes", 
                tempFilesCleanedCount, deletedFilesCleanedCount, totalSpaceFreed);
            
        } catch (Exception e) {
            log.error("文件清理任务执行失败", e);
            systemMessageProducer.sendSystemMessage("FILE_CLEANUP_ERROR", 
                Map.of("error", e.getMessage()));
        }
    }

    /**
     * 系统监控任务
     * 每5分钟执行一次，监控系统状态和性能指标
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void systemMonitoringTask() {
        log.debug("执行系统监控任务");
        
        try {
            // 检查系统资源使用情况
            Map<String, Object> systemMetrics = collectSystemMetrics();
            
            // 检查关键阈值
            checkSystemThresholds(systemMetrics);
            
            // 发送监控数据
            systemMessageProducer.sendSystemMessage("SYSTEM_MONITORING", systemMetrics);
            
        } catch (Exception e) {
            log.error("系统监控任务执行失败", e);
        }
    }

    /**
     * 审计日志清理任务
     * 每周日凌晨4点执行，清理过期的审计日志
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    @Transactional
    public void cleanupAuditLogsTask() {
        log.info("开始执行审计日志清理任务");
        
        try {
            // 获取审计日志保留配置
            Integer auditLogRetentionDays = systemConfigService.getIntValue("audit.log.retention.days", 90);
            Boolean auditLogCleanupEnabled = systemConfigService.getBooleanValue("audit.log.cleanup.enabled", true);
            
            if (!auditLogCleanupEnabled) {
                log.info("审计日志清理功能已禁用，跳过任务");
                return;
            }
            
            LocalDateTime expireDate = LocalDateTime.now().minusDays(auditLogRetentionDays);
            
            // 分批清理审计日志
            int batchSize = 1000;
            int totalCleaned = 0;
            
            while (true) {
                List<AuditLog> logsToClean = auditLogService.findExpiredLogs(expireDate, batchSize);
                
                if (logsToClean.isEmpty()) {
                    break;
                }
                
                // 批量删除
                auditLogService.batchDeleteLogs(logsToClean.stream()
                    .map(AuditLog::getId)
                    .toList());
                
                totalCleaned += logsToClean.size();
                
                log.debug("已清理 {} 条审计日志", logsToClean.size());
                
                // 避免长时间占用数据库连接
                Thread.sleep(100);
            }
            
            // 记录清理结果
            auditLogService.recordOperationLog("SYSTEM", "AUDIT_LOG_CLEANUP", 
                "AuditLog", "BATCH", "SUCCESS", 
                String.format("清理了 %d 条过期审计日志", totalCleaned));
            
            log.info("审计日志清理任务完成，共清理 {} 条记录", totalCleaned);
            
        } catch (Exception e) {
            log.error("审计日志清理任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "AUDIT_LOG_CLEANUP", 
                "AuditLog", "BATCH", "FAILURE", 
                "审计日志清理失败: " + e.getMessage());
        }
    }

    /**
     * 数据备份任务
     * 每天凌晨1点执行，备份重要数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dataBackupTask() {
        log.info("开始执行数据备份任务");
        
        try {
            // 获取备份配置
            Boolean backupEnabled = systemConfigService.getBooleanValue("data.backup.enabled", true);
            String backupPath = systemConfigService.getStringValue("data.backup.path", "/backup");
            
            if (!backupEnabled) {
                log.info("数据备份功能已禁用，跳过任务");
                return;
            }
            
            // 执行数据库备份
            String backupFileName = "archive_backup_" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql";
            
            // 这里应该调用实际的备份服务
            // backupService.performDatabaseBackup(backupPath + "/" + backupFileName);
            
            // 发送备份完成通知
            systemMessageProducer.sendSystemMessage("DATA_BACKUP_COMPLETE", 
                Map.of("backupFile", backupFileName, 
                       "backupPath", backupPath,
                       "backupTime", LocalDateTime.now().toString()));
            
            log.info("数据备份任务完成，备份文件: {}", backupFileName);
            
        } catch (Exception e) {
            log.error("数据备份任务执行失败", e);
            systemMessageProducer.sendSystemMessage("DATA_BACKUP_ERROR", 
                Map.of("error", e.getMessage()));
        }
    }

    /**
     * 收集系统指标
     */
    private Map<String, Object> collectSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        // 获取档案统计信息
        long totalArchives = archiveService.getTotalArchiveCount();
        long totalFiles = archiveFileService.getTotalFileCount();
        long totalFileSize = archiveFileService.getTotalFileSize();
        
        return Map.of(
            "memoryUsagePercent", memoryUsagePercent,
            "usedMemory", usedMemory,
            "totalMemory", totalMemory,
            "maxMemory", maxMemory,
            "totalArchives", totalArchives,
            "totalFiles", totalFiles,
            "totalFileSize", totalFileSize,
            "timestamp", LocalDateTime.now().toString()
        );
    }

    /**
     * 检查系统阈值
     */
    private void checkSystemThresholds(Map<String, Object> metrics) {
        // 检查内存使用率
        double memoryUsagePercent = (Double) metrics.get("memoryUsagePercent");
        double memoryThreshold = systemConfigService.getDoubleValue("system.memory.threshold", 80.0);
        
        if (memoryUsagePercent > memoryThreshold) {
            log.warn("内存使用率过高: {}%", memoryUsagePercent);
            systemMessageProducer.sendSystemMessage("HIGH_MEMORY_USAGE_ALERT", 
                Map.of("memoryUsage", memoryUsagePercent, "threshold", memoryThreshold));
        }
        
        // 检查磁盘空间
        long totalFileSize = (Long) metrics.get("totalFileSize");
        long diskSpaceThreshold = systemConfigService.getLongValue("system.disk.threshold", 1024L * 1024 * 1024 * 10); // 10GB
        
        if (totalFileSize > diskSpaceThreshold) {
            log.warn("磁盘使用空间过大: {} bytes", totalFileSize);
            systemMessageProducer.sendSystemMessage("HIGH_DISK_USAGE_ALERT", 
                Map.of("diskUsage", totalFileSize, "threshold", diskSpaceThreshold));
        }
    }

    /**
     * 健康检查任务
     * 每分钟执行一次，检查系统各组件健康状态
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void healthCheckTask() {
        try {
            // 检查数据库连接
            boolean dbHealthy = archiveService.checkDatabaseHealth();
            
            // 检查消息队列连接
            boolean mqHealthy = archiveMessageProducer.checkConnectionHealth();
            
            // 检查文件系统
            boolean fsHealthy = archiveFileService.checkFileSystemHealth();
            
            Map<String, Object> healthStatus = Map.of(
                "database", dbHealthy,
                "messageQueue", mqHealthy,
                "fileSystem", fsHealthy,
                "overall", dbHealthy && mqHealthy && fsHealthy,
                "timestamp", LocalDateTime.now().toString()
            );
            
            // 如果有组件不健康，发送告警
            if (!dbHealthy || !mqHealthy || !fsHealthy) {
                systemMessageProducer.sendSystemMessage("HEALTH_CHECK_ALERT", healthStatus);
                log.warn("系统健康检查发现问题: {}", healthStatus);
            }
            
        } catch (Exception e) {
            log.error("健康检查任务执行失败", e);
        }
    }
}