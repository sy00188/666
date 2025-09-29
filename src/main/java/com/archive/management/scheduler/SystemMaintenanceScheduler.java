package com.archive.management.scheduler;

import com.archive.management.service.SystemConfigService;
import com.archive.management.service.AuditLogService;
import com.archive.management.mq.producer.SystemMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 系统维护定时任务调度器
 * 负责处理系统维护相关的定时任务，包括数据库优化、缓存清理、日志管理等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMaintenanceScheduler {

    private final SystemConfigService systemConfigService;
    private final AuditLogService auditLogService;
    private final SystemMessageProducer systemMessageProducer;
    private final DataSource dataSource;

    /**
     * 数据库优化任务
     * 每周日凌晨2点执行，优化数据库性能
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void databaseOptimizationTask() {
        log.info("开始执行数据库优化任务");
        
        try {
            Boolean dbOptimizationEnabled = systemConfigService.getBooleanValue("db.optimization.enabled", true);
            
            if (!dbOptimizationEnabled) {
                log.info("数据库优化功能已禁用，跳过任务");
                return;
            }
            
            List<String> optimizationResults = new ArrayList<>();
            
            // 执行表分析和优化
            String[] tablesToOptimize = {
                "archive", "archive_file", "user", "role", "permission", 
                "audit_log", "system_config", "category"
            };
            
            try (Connection connection = dataSource.getConnection()) {
                for (String tableName : tablesToOptimize) {
                    try {
                        // 分析表
                        String analyzeQuery = "ANALYZE TABLE " + tableName;
                        try (PreparedStatement stmt = connection.prepareStatement(analyzeQuery)) {
                            stmt.execute();
                            optimizationResults.add("分析表 " + tableName + " 完成");
                        }
                        
                        // 优化表
                        String optimizeQuery = "OPTIMIZE TABLE " + tableName;
                        try (PreparedStatement stmt = connection.prepareStatement(optimizeQuery)) {
                            stmt.execute();
                            optimizationResults.add("优化表 " + tableName + " 完成");
                        }
                        
                        log.debug("表 {} 优化完成", tableName);
                        
                    } catch (Exception e) {
                        log.error("优化表 {} 失败: {}", tableName, e.getMessage(), e);
                        optimizationResults.add("优化表 " + tableName + " 失败: " + e.getMessage());
                    }
                }
                
                // 更新表统计信息
                updateTableStatistics(connection, optimizationResults);
                
                // 检查索引使用情况
                checkIndexUsage(connection, optimizationResults);
                
            }
            
            // 记录优化结果
            auditLogService.recordOperationLog("SYSTEM", "DATABASE_OPTIMIZATION", 
                "Database", "ALL_TABLES", "SUCCESS", 
                String.join("; ", optimizationResults));
            
            // 发送优化完成通知
            systemMessageProducer.sendSystemMessage("DATABASE_OPTIMIZATION_COMPLETE", 
                Map.of("results", optimizationResults, 
                       "timestamp", LocalDateTime.now().toString()));
            
            log.info("数据库优化任务完成，优化了 {} 个表", tablesToOptimize.length);
            
        } catch (Exception e) {
            log.error("数据库优化任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "DATABASE_OPTIMIZATION", 
                "Database", "ALL_TABLES", "FAILURE", 
                "数据库优化失败: " + e.getMessage());
        }
    }

    /**
     * 缓存清理任务
     * 每天凌晨3点执行，清理过期缓存
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cacheCleanupTask() {
        log.info("开始执行缓存清理任务");
        
        try {
            Boolean cacheCleanupEnabled = systemConfigService.getBooleanValue("cache.cleanup.enabled", true);
            
            if (!cacheCleanupEnabled) {
                log.info("缓存清理功能已禁用，跳过任务");
                return;
            }
            
            // 清理应用缓存
            long applicationCacheCleared = clearApplicationCache();
            
            // 清理系统临时文件
            long tempFilesCleared = clearSystemTempFiles();
            
            // 清理日志缓存
            long logCacheCleared = clearLogCache();
            
            Map<String, Object> cleanupResults = Map.of(
                "applicationCacheCleared", applicationCacheCleared,
                "tempFilesCleared", tempFilesCleared,
                "logCacheCleared", logCacheCleared,
                "totalCleared", applicationCacheCleared + tempFilesCleared + logCacheCleared,
                "timestamp", LocalDateTime.now().toString()
            );
            
            // 发送清理完成通知
            systemMessageProducer.sendSystemMessage("CACHE_CLEANUP_COMPLETE", cleanupResults);
            
            log.info("缓存清理任务完成，清理缓存项: {}, 临时文件: {}, 日志缓存: {}", 
                applicationCacheCleared, tempFilesCleared, logCacheCleared);
            
        } catch (Exception e) {
            log.error("缓存清理任务执行失败", e);
            systemMessageProducer.sendSystemMessage("CACHE_CLEANUP_ERROR", 
                Map.of("error", e.getMessage()));
        }
    }

    /**
     * 日志轮转任务
     * 每天凌晨4点执行，轮转应用日志文件
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void logRotationTask() {
        log.info("开始执行日志轮转任务");
        
        try {
            Boolean logRotationEnabled = systemConfigService.getBooleanValue("log.rotation.enabled", true);
            String logDirectory = systemConfigService.getStringValue("log.directory", "logs");
            Integer maxLogFiles = systemConfigService.getIntValue("log.rotation.max.files", 30);
            Long maxLogSizeMB = systemConfigService.getLongValue("log.rotation.max.size.mb", 100L);
            
            if (!logRotationEnabled) {
                log.info("日志轮转功能已禁用，跳过任务");
                return;
            }
            
            Path logPath = Paths.get(logDirectory);
            if (!Files.exists(logPath)) {
                log.warn("日志目录不存在: {}", logDirectory);
                return;
            }
            
            List<String> rotationResults = new ArrayList<>();
            
            // 轮转应用日志
            rotateLogFiles(logPath, "application", maxLogFiles, maxLogSizeMB, rotationResults);
            
            // 轮转错误日志
            rotateLogFiles(logPath, "error", maxLogFiles, maxLogSizeMB, rotationResults);
            
            // 轮转访问日志
            rotateLogFiles(logPath, "access", maxLogFiles, maxLogSizeMB, rotationResults);
            
            // 压缩旧日志文件
            compressOldLogFiles(logPath, rotationResults);
            
            // 记录轮转结果
            auditLogService.recordOperationLog("SYSTEM", "LOG_ROTATION", 
                "LogFiles", logDirectory, "SUCCESS", 
                String.join("; ", rotationResults));
            
            log.info("日志轮转任务完成，处理结果: {}", rotationResults.size());
            
        } catch (Exception e) {
            log.error("日志轮转任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "LOG_ROTATION", 
                "LogFiles", "ALL", "FAILURE", 
                "日志轮转失败: " + e.getMessage());
        }
    }

    /**
     * 性能监控任务
     * 每15分钟执行一次，监控系统性能指标
     */
    @Scheduled(fixedRate = 900000) // 15分钟
    public void performanceMonitoringTask() {
        try {
            Boolean performanceMonitoringEnabled = systemConfigService.getBooleanValue("performance.monitoring.enabled", true);
            
            if (!performanceMonitoringEnabled) {
                return;
            }
            
            // 收集性能指标
            Map<String, Object> performanceMetrics = collectPerformanceMetrics();
            
            // 检查性能阈值
            checkPerformanceThresholds(performanceMetrics);
            
            // 发送性能数据
            systemMessageProducer.sendSystemMessage("PERFORMANCE_METRICS", performanceMetrics);
            
            log.debug("性能监控数据收集完成");
            
        } catch (Exception e) {
            log.error("性能监控任务执行失败", e);
        }
    }

    /**
     * 系统更新检查任务
     * 每天凌晨8点执行，检查系统更新
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void systemUpdateCheckTask() {
        log.info("开始执行系统更新检查任务");
        
        try {
            Boolean updateCheckEnabled = systemConfigService.getBooleanValue("system.update.check.enabled", true);
            
            if (!updateCheckEnabled) {
                log.info("系统更新检查功能已禁用，跳过任务");
                return;
            }
            
            // 检查应用版本更新
            Map<String, Object> updateInfo = checkForUpdates();
            
            if ((Boolean) updateInfo.get("hasUpdates")) {
                // 发送更新通知
                systemMessageProducer.sendSystemMessage("SYSTEM_UPDATE_AVAILABLE", updateInfo);
                
                log.info("发现系统更新: {}", updateInfo.get("latestVersion"));
            } else {
                log.debug("系统已是最新版本");
            }
            
        } catch (Exception e) {
            log.error("系统更新检查任务执行失败", e);
        }
    }

    /**
     * 磁盘空间监控任务
     * 每小时执行一次，监控磁盘空间使用情况
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void diskSpaceMonitoringTask() {
        try {
            Boolean diskMonitoringEnabled = systemConfigService.getBooleanValue("disk.monitoring.enabled", true);
            
            if (!diskMonitoringEnabled) {
                return;
            }
            
            // 检查各个重要目录的磁盘使用情况
            Map<String, Object> diskUsage = checkDiskUsage();
            
            // 检查磁盘空间阈值
            checkDiskSpaceThresholds(diskUsage);
            
            log.debug("磁盘空间监控完成");
            
        } catch (Exception e) {
            log.error("磁盘空间监控任务执行失败", e);
        }
    }

    // 私有辅助方法

    private void updateTableStatistics(Connection connection, List<String> results) {
        try {
            String query = "SELECT table_name, table_rows, data_length, index_length " +
                          "FROM information_schema.tables " +
                          "WHERE table_schema = DATABASE()";
            
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    long tableRows = rs.getLong("table_rows");
                    long dataLength = rs.getLong("data_length");
                    long indexLength = rs.getLong("index_length");
                    
                    results.add(String.format("表 %s: 行数=%d, 数据大小=%d, 索引大小=%d", 
                        tableName, tableRows, dataLength, indexLength));
                }
            }
        } catch (Exception e) {
            log.error("更新表统计信息失败", e);
        }
    }

    private void checkIndexUsage(Connection connection, List<String> results) {
        try {
            String query = "SELECT table_name, index_name, cardinality " +
                          "FROM information_schema.statistics " +
                          "WHERE table_schema = DATABASE() AND cardinality < 10";
            
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String indexName = rs.getString("index_name");
                    long cardinality = rs.getLong("cardinality");
                    
                    results.add(String.format("低效索引: %s.%s (基数=%d)", 
                        tableName, indexName, cardinality));
                }
            }
        } catch (Exception e) {
            log.error("检查索引使用情况失败", e);
        }
    }

    private long clearApplicationCache() {
        // 这里应该实现具体的应用缓存清理逻辑
        // 例如清理 Redis 缓存、本地缓存等
        return 0;
    }

    private long clearSystemTempFiles() {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempPath = Paths.get(tempDir);
            
            return Files.walk(tempPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant()
                            .isBefore(LocalDateTime.now().minusDays(1).atZone(
                                java.time.ZoneId.systemDefault()).toInstant());
                    } catch (IOException e) {
                        return false;
                    }
                })
                .mapToLong(path -> {
                    try {
                        long size = Files.size(path);
                        Files.delete(path);
                        return size;
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
                
        } catch (Exception e) {
            log.error("清理系统临时文件失败", e);
            return 0;
        }
    }

    private long clearLogCache() {
        // 这里应该实现日志缓存清理逻辑
        return 0;
    }

    private void rotateLogFiles(Path logPath, String logType, int maxFiles, long maxSizeMB, List<String> results) {
        try {
            String logFileName = logType + ".log";
            Path logFile = logPath.resolve(logFileName);
            
            if (!Files.exists(logFile)) {
                return;
            }
            
            long fileSizeMB = Files.size(logFile) / (1024 * 1024);
            
            if (fileSizeMB > maxSizeMB) {
                // 轮转日志文件
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String rotatedFileName = logType + "_" + timestamp + ".log";
                Path rotatedFile = logPath.resolve(rotatedFileName);
                
                Files.move(logFile, rotatedFile);
                Files.createFile(logFile);
                
                results.add(String.format("轮转日志文件: %s -> %s", logFileName, rotatedFileName));
            }
            
            // 清理旧的日志文件
            cleanupOldLogFiles(logPath, logType, maxFiles, results);
            
        } catch (Exception e) {
            log.error("轮转日志文件失败: {}", logType, e);
        }
    }

    private void cleanupOldLogFiles(Path logPath, String logType, int maxFiles, List<String> results) {
        try {
            List<Path> logFiles = Files.list(logPath)
                .filter(path -> path.getFileName().toString().startsWith(logType + "_"))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .toList();
            
            if (logFiles.size() > maxFiles) {
                for (int i = maxFiles; i < logFiles.size(); i++) {
                    Files.delete(logFiles.get(i));
                    results.add("删除旧日志文件: " + logFiles.get(i).getFileName());
                }
            }
            
        } catch (Exception e) {
            log.error("清理旧日志文件失败: {}", logType, e);
        }
    }

    private void compressOldLogFiles(Path logPath, List<String> results) {
        // 这里应该实现日志文件压缩逻辑
        // 例如使用 gzip 压缩超过一定时间的日志文件
    }

    private Map<String, Object> collectPerformanceMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        return Map.of(
            "cpuUsage", getCpuUsage(),
            "memoryUsage", (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100,
            "diskUsage", getDiskUsage(),
            "networkIO", getNetworkIO(),
            "databaseConnections", getDatabaseConnectionCount(),
            "activeThreads", Thread.activeCount(),
            "timestamp", LocalDateTime.now().toString()
        );
    }

    private void checkPerformanceThresholds(Map<String, Object> metrics) {
        double cpuThreshold = systemConfigService.getDoubleValue("performance.cpu.threshold", 80.0);
        double memoryThreshold = systemConfigService.getDoubleValue("performance.memory.threshold", 85.0);
        double diskThreshold = systemConfigService.getDoubleValue("performance.disk.threshold", 90.0);
        
        double cpuUsage = (Double) metrics.get("cpuUsage");
        double memoryUsage = (Double) metrics.get("memoryUsage");
        double diskUsage = (Double) metrics.get("diskUsage");
        
        if (cpuUsage > cpuThreshold) {
            systemMessageProducer.sendSystemMessage("HIGH_CPU_USAGE_ALERT", 
                Map.of("cpuUsage", cpuUsage, "threshold", cpuThreshold));
        }
        
        if (memoryUsage > memoryThreshold) {
            systemMessageProducer.sendSystemMessage("HIGH_MEMORY_USAGE_ALERT", 
                Map.of("memoryUsage", memoryUsage, "threshold", memoryThreshold));
        }
        
        if (diskUsage > diskThreshold) {
            systemMessageProducer.sendSystemMessage("HIGH_DISK_USAGE_ALERT", 
                Map.of("diskUsage", diskUsage, "threshold", diskThreshold));
        }
    }

    private Map<String, Object> checkForUpdates() {
        // 这里应该实现实际的更新检查逻辑
        // 例如检查远程版本服务器
        return Map.of(
            "hasUpdates", false,
            "currentVersion", "1.0.0",
            "latestVersion", "1.0.0",
            "updateDescription", "无可用更新"
        );
    }

    private Map<String, Object> checkDiskUsage() {
        File[] roots = File.listRoots();
        Map<String, Object> diskUsage = new java.util.HashMap<>();
        
        for (File root : roots) {
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = (double) usedSpace / totalSpace * 100;
            
            diskUsage.put(root.getPath(), Map.of(
                "totalSpace", totalSpace,
                "freeSpace", freeSpace,
                "usedSpace", usedSpace,
                "usagePercent", usagePercent
            ));
        }
        
        return diskUsage;
    }

    private void checkDiskSpaceThresholds(Map<String, Object> diskUsage) {
        double diskThreshold = systemConfigService.getDoubleValue("disk.usage.threshold", 85.0);
        
        for (Map.Entry<String, Object> entry : diskUsage.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) entry.getValue();
            double usagePercent = (Double) usage.get("usagePercent");
            
            if (usagePercent > diskThreshold) {
                systemMessageProducer.sendSystemMessage("DISK_SPACE_ALERT", 
                    Map.of("path", entry.getKey(), 
                           "usagePercent", usagePercent, 
                           "threshold", diskThreshold));
            }
        }
    }

    // 这些方法需要根据实际环境实现
    private double getCpuUsage() {
        return 0.0; // 实际实现需要使用 JMX 或其他方式获取 CPU 使用率
    }

    private double getDiskUsage() {
        return 0.0; // 实际实现需要计算磁盘使用率
    }

    private Map<String, Object> getNetworkIO() {
        return Map.of("bytesIn", 0L, "bytesOut", 0L); // 实际实现需要获取网络 IO 统计
    }

    private int getDatabaseConnectionCount() {
        return 0; // 实际实现需要从连接池获取活跃连接数
    }
}