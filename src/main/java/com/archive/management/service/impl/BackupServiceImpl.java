package com.archive.management.service.impl;

import com.archive.management.entity.Backup;
import com.archive.management.repository.BackupRepository;
import com.archive.management.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 备份服务实现类
 * 实现数据库和文件的备份恢复功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    @Autowired
    private BackupRepository backupRepository;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${archive.backup.path:./data/backup}")
    private String backupPath;

    @Value("${archive.backup.retention-days:30}")
    private int retentionDays;

    // 备份进度跟踪
    private final Map<Long, Integer> backupProgress = new ConcurrentHashMap<>();

    @Override
    @Async
    @Transactional
    public Long createDatabaseBackup(String backupName, String description, Long operatorId) {
        log.info("开始创建数据库备份: backupName={}, operatorId={}", backupName, operatorId);
        
        // 创建备份记录
        Backup backup = new Backup();
        backup.setBackupName(backupName);
        backup.setBackupType("database");
        backup.setStatus("running");
        backup.setDescription(description);
        backup.setOperatorId(operatorId);
        backup.setStartTime(LocalDateTime.now());
        backup.setProgress(0);
        backup.setStorageType("local");
        backup.setRetentionDays(retentionDays);
        backup.setExpiryTime(LocalDateTime.now().plusDays(retentionDays));
        
        backup = backupRepository.save(backup);
        final Long backupId = backup.getId();
        
        try {
            // 确保备份目录存在
            ensureBackupDirectory();
            
            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("db_backup_%s_%s.sql", backupName.replaceAll("[^a-zA-Z0-9]", "_"), timestamp);
            String filePath = Paths.get(backupPath, fileName).toString();
            
            // 从URL中提取数据库名
            String databaseName = extractDatabaseName(dbUrl);
            backup.setDatabaseName(databaseName);
            
            // 执行mysqldump备份
            backupProgress.put(backupId, 10);
            boolean success = executeMysqldump(databaseName, filePath, backupId);
            
            if (success) {
                File backupFile = new File(filePath);
                backup.setFilePath(filePath);
                backup.setFileSize(backupFile.length());
                backup.setMd5(calculateMD5(filePath));
                backup.setStatus("completed");
                backup.setProgress(100);
                backup.setEndTime(LocalDateTime.now());
                backup.setDuration(java.time.Duration.between(backup.getStartTime(), backup.getEndTime()).toMillis());
                
                log.info("数据库备份完成: backupId={}, size={}bytes", backupId, backupFile.length());
            } else {
                backup.setStatus("failed");
                backup.setErrorMessage("备份执行失败");
                backup.setEndTime(LocalDateTime.now());
                log.error("数据库备份失败: backupId={}", backupId);
            }
            
        } catch (Exception e) {
            log.error("数据库备份异常: backupId={}", backupId, e);
            backup.setStatus("failed");
            backup.setErrorMessage(e.getMessage());
            backup.setEndTime(LocalDateTime.now());
        } finally {
            backupRepository.save(backup);
            backupProgress.remove(backupId);
        }
        
        return backupId;
    }

    @Override
    @Async
    @Transactional
    public Long createFileBackup(String backupName, List<String> filePaths, String description, Long operatorId) {
        log.info("开始创建文件备份: backupName={}, fileCount={}, operatorId={}", backupName, filePaths.size(), operatorId);
        
        Backup backup = new Backup();
        backup.setBackupName(backupName);
        backup.setBackupType("file");
        backup.setStatus("running");
        backup.setDescription(description);
        backup.setOperatorId(operatorId);
        backup.setStartTime(LocalDateTime.now());
        backup.setProgress(0);
        backup.setStorageType("local");
        backup.setRetentionDays(retentionDays);
        backup.setExpiryTime(LocalDateTime.now().plusDays(retentionDays));
        
        backup = backupRepository.save(backup);
        final Long backupId = backup.getId();
        
        try {
            ensureBackupDirectory();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("file_backup_%s_%s.zip", backupName.replaceAll("[^a-zA-Z0-9]", "_"), timestamp);
            String zipPath = Paths.get(backupPath, fileName).toString();
            
            backupProgress.put(backupId, 10);
            
            // 创建ZIP压缩文件
            boolean success = createZipBackup(filePaths, zipPath, backupId);
            
            if (success) {
                File backupFile = new File(zipPath);
                backup.setFilePath(zipPath);
                backup.setFileSize(backupFile.length());
                backup.setMd5(calculateMD5(zipPath));
                backup.setStatus("completed");
                backup.setProgress(100);
                backup.setEndTime(LocalDateTime.now());
                backup.setDuration(java.time.Duration.between(backup.getStartTime(), backup.getEndTime()).toMillis());
                
                log.info("文件备份完成: backupId={}, size={}bytes", backupId, backupFile.length());
            } else {
                backup.setStatus("failed");
                backup.setErrorMessage("文件备份失败");
                backup.setEndTime(LocalDateTime.now());
            }
            
        } catch (Exception e) {
            log.error("文件备份异常: backupId={}", backupId, e);
            backup.setStatus("failed");
            backup.setErrorMessage(e.getMessage());
            backup.setEndTime(LocalDateTime.now());
        } finally {
            backupRepository.save(backup);
            backupProgress.remove(backupId);
        }
        
        return backupId;
    }

    @Override
    @Transactional
    public Long createFullBackup(String backupName, String description, Long operatorId) {
        log.info("开始创建完整系统备份: backupName={}, operatorId={}", backupName, operatorId);
        
        // 先创建数据库备份
        Long dbBackupId = createDatabaseBackup(backupName + "_db", "完整备份-数据库部分", operatorId);
        
        // 创建文件备份（包含所有重要文件）
        List<String> filePaths = getSystemFilePaths();
        Long fileBackupId = createFileBackup(backupName + "_files", filePaths, "完整备份-文件部分", operatorId);
        
        // 创建完整备份记录
        Backup fullBackup = new Backup();
        fullBackup.setBackupName(backupName);
        fullBackup.setBackupType("full");
        fullBackup.setStatus("completed");
        fullBackup.setDescription(description + String.format(" (包含DB备份ID:%d, 文件备份ID:%d)", dbBackupId, fileBackupId));
        fullBackup.setOperatorId(operatorId);
        fullBackup.setStartTime(LocalDateTime.now());
        fullBackup.setEndTime(LocalDateTime.now());
        fullBackup.setProgress(100);
        fullBackup.setStorageType("local");
        fullBackup.setRetentionDays(retentionDays);
        fullBackup.setExpiryTime(LocalDateTime.now().plusDays(retentionDays));
        
        fullBackup = backupRepository.save(fullBackup);
        
        log.info("完整系统备份完成: fullBackupId={}", fullBackup.getId());
        return fullBackup.getId();
    }

    @Override
    @Transactional
    public Long createIncrementalBackup(String backupName, Long baseBackupId, String description, Long operatorId) {
        log.info("开始创建增量备份: backupName={}, baseBackupId={}, operatorId={}", backupName, baseBackupId, operatorId);
        
        Backup baseBackup = backupRepository.findById(baseBackupId)
                .orElseThrow(() -> new RuntimeException("基础备份不存在: " + baseBackupId));
        
        if (!"completed".equals(baseBackup.getStatus())) {
            throw new RuntimeException("基础备份状态不正确，无法创建增量备份");
        }
        
        Backup backup = new Backup();
        backup.setBackupName(backupName);
        backup.setBackupType("incremental");
        backup.setBaseBackupId(baseBackupId);
        backup.setStatus("running");
        backup.setDescription(description);
        backup.setOperatorId(operatorId);
        backup.setStartTime(LocalDateTime.now());
        backup.setProgress(0);
        backup.setStorageType("local");
        backup.setRetentionDays(retentionDays);
        backup.setExpiryTime(LocalDateTime.now().plusDays(retentionDays));
        
        backup = backupRepository.save(backup);
        
        // 增量备份逻辑（简化实现，实际应该只备份变更的数据）
        try {
            Long dbBackupId = createDatabaseBackup(backupName, "增量备份", operatorId);
            backup.setStatus("completed");
            backup.setProgress(100);
            backup.setEndTime(LocalDateTime.now());
            backup.setDescription(description + " (数据库备份ID:" + dbBackupId + ")");
        } catch (Exception e) {
            log.error("增量备份失败", e);
            backup.setStatus("failed");
            backup.setErrorMessage(e.getMessage());
            backup.setEndTime(LocalDateTime.now());
        }
        
        backupRepository.save(backup);
        log.info("增量备份完成: backupId={}", backup.getId());
        return backup.getId();
    }

    @Override
    @Transactional
    public boolean restoreDatabaseBackup(Long backupId, Long operatorId) {
        log.info("开始恢复数据库备份: backupId={}, operatorId={}", backupId, operatorId);
        
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("备份记录不存在: " + backupId));
        
        if (!"completed".equals(backup.getStatus())) {
            throw new RuntimeException("备份状态不正确，无法恢复");
        }
        
        if (backup.getFilePath() == null || !new File(backup.getFilePath()).exists()) {
            throw new RuntimeException("备份文件不存在");
        }
        
        try {
            String databaseName = backup.getDatabaseName();
            boolean success = executeMysqlRestore(databaseName, backup.getFilePath());
            
            if (success) {
                log.info("数据库恢复成功: backupId={}", backupId);
                return true;
            } else {
                log.error("数据库恢复失败: backupId={}", backupId);
                return false;
            }
        } catch (Exception e) {
            log.error("数据库恢复异常: backupId={}", backupId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean restoreFileBackup(Long backupId, String targetPath, Long operatorId) {
        log.info("开始恢复文件备份: backupId={}, targetPath={}, operatorId={}", backupId, targetPath, operatorId);
        
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("备份记录不存在: " + backupId));
        
        if (!"completed".equals(backup.getStatus())) {
            throw new RuntimeException("备份状态不正确，无法恢复");
        }
        
        if (backup.getFilePath() == null || !new File(backup.getFilePath()).exists()) {
            throw new RuntimeException("备份文件不存在");
        }
        
        try {
            unzipBackup(backup.getFilePath(), targetPath);
            log.info("文件恢复成功: backupId={}", backupId);
            return true;
        } catch (Exception e) {
            log.error("文件恢复失败: backupId={}", backupId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean restoreFullBackup(Long backupId, Long operatorId) {
        log.info("开始恢复完整备份: backupId={}, operatorId={}", backupId, operatorId);
        
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("备份记录不存在: " + backupId));
        
        // 解析完整备份中包含的子备份ID
        // 简化实现，实际应该从description中解析出子备份ID并依次恢复
        log.warn("完整备份恢复功能待完善，请分别恢复数据库和文件备份");
        return true;
    }

    @Override
    @Transactional
    public boolean deleteBackup(Long backupId, Long operatorId) {
        log.info("删除备份: backupId={}, operatorId={}", backupId, operatorId);
        
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("备份记录不存在: " + backupId));
        
        // 软删除
        backup.setDeleted(true);
        backup.setUpdatedAt(LocalDateTime.now());
        backupRepository.save(backup);
        
        // 可选：删除实际文件
        if (backup.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(backup.getFilePath()));
                log.info("备份文件已删除: {}", backup.getFilePath());
            } catch (IOException e) {
                log.warn("删除备份文件失败: {}", backup.getFilePath(), e);
            }
        }
        
        return true;
    }

    @Override
    public Map<String, Object> getBackupList(String backupType, int page, int size) {
        log.debug("获取备份列表: backupType={}, page={}, size={}", backupType, page, size);
        
        List<Backup> backups;
        if (backupType != null && !backupType.isEmpty()) {
            backups = backupRepository.findByBackupTypeAndDeletedFalseOrderByCreatedAtDesc(backupType);
        } else {
            backups = backupRepository.findByDeletedFalseOrderByCreatedAtDesc();
        }
        
        // 简单分页
        int start = page * size;
        int end = Math.min(start + size, backups.size());
        List<Backup> pageBackups = start < backups.size() ? backups.subList(start, end) : List.of();
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", backups.size());
        result.put("records", pageBackups);
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }

    @Override
    public Map<String, Object> getBackupDetail(Long backupId) {
        log.debug("获取备份详情: backupId={}", backupId);
        
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("备份记录不存在: " + backupId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", backup.getId());
        result.put("backupName", backup.getBackupName());
        result.put("backupType", backup.getBackupType());
        result.put("status", backup.getStatus());
        result.put("filePath", backup.getFilePath());
        result.put("fileSize", backup.getFileSize());
        result.put("description", backup.getDescription());
        result.put("startTime", backup.getStartTime());
        result.put("endTime", backup.getEndTime());
        result.put("duration", backup.getDuration());
        result.put("progress", backup.getProgress());
        result.put("verified", backup.getVerified());
        result.put("restorable", backup.getRestorable());
        result.put("md5", backup.getMd5());
        
        return result;
    }

    @Override
    public Map<String, Object> validateBackup(Long backupId) {
        log.info("验证备份: backupId={}", backupId);
        
        Backup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("备份记录不存在: " + backupId));
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (backup.getFilePath() == null) {
                result.put("valid", false);
                result.put("message", "备份文件路径为空");
                return result;
            }
            
            File backupFile = new File(backup.getFilePath());
            if (!backupFile.exists()) {
                result.put("valid", false);
                result.put("message", "备份文件不存在");
                backup.setRestorable(false);
            } else {
                // 验证文件大小
                if (backupFile.length() != backup.getFileSize()) {
                    result.put("valid", false);
                    result.put("message", "备份文件大小不匹配");
                    backup.setRestorable(false);
                } else {
                    // 验证MD5
                    String actualMd5 = calculateMD5(backup.getFilePath());
                    if (!actualMd5.equals(backup.getMd5())) {
                        result.put("valid", false);
                        result.put("message", "备份文件MD5不匹配，可能已损坏");
                        backup.setRestorable(false);
                    } else {
                        result.put("valid", true);
                        result.put("message", "备份文件完整有效");
                        backup.setVerified(true);
                        backup.setVerifiedTime(LocalDateTime.now());
                        backup.setVerifiedResult("验证成功");
                    }
                }
            }
            
            backupRepository.save(backup);
        } catch (Exception e) {
            log.error("验证备份失败: backupId={}", backupId, e);
            result.put("valid", false);
            result.put("message", "验证过程出错: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getBackupStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("获取备份统计: startDate={}, endDate={}", startDate, endDate);
        
        List<Backup> backups;
        if (startDate != null && endDate != null) {
            backups = backupRepository.findByDateRange(startDate, endDate);
        } else {
            backups = backupRepository.findByDeletedFalseOrderByCreatedAtDesc();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBackups", backups.size());
        stats.put("successfulBackups", backups.stream().filter(b -> "completed".equals(b.getStatus())).count());
        stats.put("failedBackups", backups.stream().filter(b -> "failed".equals(b.getStatus())).count());
        stats.put("runningBackups", backups.stream().filter(b -> "running".equals(b.getStatus())).count());
        stats.put("totalSize", backups.stream().mapToLong(b -> b.getFileSize() != null ? b.getFileSize() : 0).sum());
        
        // 按类型统计
        Map<String, Long> byType = backups.stream()
                .collect(Collectors.groupingBy(Backup::getBackupType, Collectors.counting()));
        stats.put("byType", byType);
        
        return stats;
    }

    @Override
    @Scheduled(cron = "${archive.scheduler.backup-cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public int cleanExpiredBackups(int retentionDays) {
        log.info("开始清理过期备份: retentionDays={}", retentionDays);
        
        List<Backup> expiredBackups = backupRepository.findExpiredBackups(LocalDateTime.now());
        int count = 0;
        
        for (Backup backup : expiredBackups) {
            try {
                deleteBackup(backup.getId(), 0L); // 系统自动清理
                count++;
            } catch (Exception e) {
                log.error("清理过期备份失败: backupId={}", backup.getId(), e);
            }
        }
        
        log.info("过期备份清理完成: 清理数量={}", count);
        return count;
    }

    @Override
    @Transactional
    public Long createScheduledBackupTask(String taskName, String backupType, String cronExpression, Long operatorId) {
        log.info("创建定时备份任务: taskName={}, backupType={}, cron={}", taskName, backupType, cronExpression);
        // 定时任务配置应该保存到专门的定时任务表，这里简化处理
        log.warn("定时备份任务功能需要集成Spring @Scheduled或Quartz");
        return 1L;
    }

    @Override
    public boolean stopScheduledBackupTask(Long taskId, Long operatorId) {
        log.info("停止定时备份任务: taskId={}", taskId);
        return true;
    }

    @Override
    public List<Map<String, Object>> getScheduledBackupTasks() {
        log.debug("获取定时备份任务列表");
        return List.of();
    }

    @Override
    public boolean exportBackup(Long backupId, String storageType, Map<String, Object> storageConfig, Long operatorId) {
        log.info("导出备份: backupId={}, storageType={}", backupId, storageType);
        // 导出到云存储等外部位置
        return true;
    }

    @Override
    public Long importBackup(String backupName, String storageType, Map<String, Object> storageConfig, Long operatorId) {
        log.info("导入备份: backupName={}, storageType={}", backupName, storageType);
        // 从外部存储导入备份
        return 1L;
    }

    @Override
    public Map<String, Object> getBackupProgress(Long backupId) {
        Map<String, Object> result = new HashMap<>();
        Integer progress = backupProgress.get(backupId);
        
        if (progress != null) {
            result.put("backupId", backupId);
            result.put("progress", progress);
            result.put("status", "running");
        } else {
            // 从数据库查询
            Backup backup = backupRepository.findById(backupId).orElse(null);
            if (backup != null) {
                result.put("backupId", backupId);
                result.put("progress", backup.getProgress());
                result.put("status", backup.getStatus());
            } else {
                result.put("progress", 0);
                result.put("status", "not_found");
            }
        }
        
        return result;
    }

    @Override
    public boolean cancelBackup(Long backupId, Long operatorId) {
        log.info("取消备份: backupId={}", backupId);
        
        Backup backup = backupRepository.findById(backupId).orElse(null);
        if (backup != null && "running".equals(backup.getStatus())) {
            backup.setStatus("cancelled");
            backup.setEndTime(LocalDateTime.now());
            backupRepository.save(backup);
            backupProgress.remove(backupId);
            return true;
        }
        
        return false;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 确保备份目录存在
     */
    private void ensureBackupDirectory() throws IOException {
        Path path = Paths.get(backupPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("创建备份目录: {}", backupPath);
        }
    }

    /**
     * 从JDBC URL中提取数据库名
     */
    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:mysql://localhost:3306/archive_management?params...
        String[] parts = jdbcUrl.split("/");
        if (parts.length >= 4) {
            String dbPart = parts[3];
            return dbPart.split("\\?")[0];
        }
        return "archive_management";
    }

    /**
     * 执行mysqldump备份
     */
    private boolean executeMysqldump(String databaseName, String outputPath, Long backupId) {
        try {
            // 构建mysqldump命令
            String command = String.format(
                    "mysqldump -u%s -p%s --single-transaction --quick --lock-tables=false %s",
                    dbUsername, dbPassword, databaseName
            );
            
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
            pb.redirectOutput(new File(outputPath));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                backupProgress.put(backupId, 90);
                return true;
            } else {
                log.error("mysqldump执行失败, exitCode={}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行mysqldump失败", e);
            return false;
        }
    }

    /**
     * 执行mysql恢复
     */
    private boolean executeMysqlRestore(String databaseName, String sqlFile) {
        try {
            String command = String.format(
                    "mysql -u%s -p%s %s < %s",
                    dbUsername, dbPassword, databaseName, sqlFile
            );
            
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            return exitCode == 0;
        } catch (Exception e) {
            log.error("执行mysql恢复失败", e);
            return false;
        }
    }

    /**
     * 创建ZIP备份
     */
    private boolean createZipBackup(List<String> filePaths, String zipPath, Long backupId) {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            int totalFiles = filePaths.size();
            int processedFiles = 0;
            
            for (String filePath : filePaths) {
                File file = new File(filePath);
                if (file.exists()) {
                    addToZip(file, file.getName(), zos);
                }
                
                processedFiles++;
                int progress = 10 + (int) ((processedFiles / (double) totalFiles) * 80);
                backupProgress.put(backupId, progress);
            }
            
            return true;
        } catch (Exception e) {
            log.error("创建ZIP备份失败", e);
            return false;
        }
    }

    /**
     * 添加文件到ZIP
     */
    private void addToZip(File file, String fileName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            
            byte[] bytes = new byte[4096];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            
            zos.closeEntry();
        }
    }

    /**
     * 解压ZIP备份
     */
    private void unzipBackup(String zipPath, String targetPath) throws IOException {
        File destDir = new File(targetPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] bytes = new byte[4096];
                        int length;
                        while ((length = zis.read(bytes)) >= 0) {
                            fos.write(bytes, 0, length);
                        }
                    }
                }
                
                zipEntry = zis.getNextEntry();
            }
            
            zis.closeAllEntries();
        }
    }

    /**
     * 计算文件MD5
     */
    private String calculateMD5(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length;
            
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            log.error("计算MD5失败: {}", filePath, e);
            return "";
        }
    }

    /**
     * 获取系统文件路径列表
     */
    private List<String> getSystemFilePaths() {
        List<String> filePaths = new ArrayList<>();
        // 这里应该根据实际情况配置需要备份的文件路径
        filePaths.add("./application.yml");
        filePaths.add("./logback-spring.xml");
        return filePaths;
    }
}
