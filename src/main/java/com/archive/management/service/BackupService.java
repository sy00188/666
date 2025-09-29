package com.archive.management.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 备份服务接口
 * 提供系统数据备份和恢复相关的操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface BackupService {

    /**
     * 创建数据库备份
     * 
     * @param backupName 备份名称
     * @param description 备份描述
     * @param operatorId 操作人ID
     * @return 备份ID
     */
    Long createDatabaseBackup(String backupName, String description, Long operatorId);

    /**
     * 创建文件备份
     * 
     * @param backupName 备份名称
     * @param filePaths 文件路径列表
     * @param description 备份描述
     * @param operatorId 操作人ID
     * @return 备份ID
     */
    Long createFileBackup(String backupName, List<String> filePaths, String description, Long operatorId);

    /**
     * 创建完整系统备份
     * 
     * @param backupName 备份名称
     * @param description 备份描述
     * @param operatorId 操作人ID
     * @return 备份ID
     */
    Long createFullBackup(String backupName, String description, Long operatorId);

    /**
     * 创建增量备份
     * 
     * @param backupName 备份名称
     * @param baseBackupId 基础备份ID
     * @param description 备份描述
     * @param operatorId 操作人ID
     * @return 备份ID
     */
    Long createIncrementalBackup(String backupName, Long baseBackupId, String description, Long operatorId);

    /**
     * 恢复数据库备份
     * 
     * @param backupId 备份ID
     * @param operatorId 操作人ID
     * @return 是否恢复成功
     */
    boolean restoreDatabaseBackup(Long backupId, Long operatorId);

    /**
     * 恢复文件备份
     * 
     * @param backupId 备份ID
     * @param targetPath 目标路径
     * @param operatorId 操作人ID
     * @return 是否恢复成功
     */
    boolean restoreFileBackup(Long backupId, String targetPath, Long operatorId);

    /**
     * 恢复完整系统备份
     * 
     * @param backupId 备份ID
     * @param operatorId 操作人ID
     * @return 是否恢复成功
     */
    boolean restoreFullBackup(Long backupId, Long operatorId);

    /**
     * 删除备份
     * 
     * @param backupId 备份ID
     * @param operatorId 操作人ID
     * @return 是否删除成功
     */
    boolean deleteBackup(Long backupId, Long operatorId);

    /**
     * 获取备份列表
     * 
     * @param backupType 备份类型
     * @param page 页码
     * @param size 页大小
     * @return 备份列表
     */
    Map<String, Object> getBackupList(String backupType, int page, int size);

    /**
     * 获取备份详情
     * 
     * @param backupId 备份ID
     * @return 备份详情
     */
    Map<String, Object> getBackupDetail(Long backupId);

    /**
     * 验证备份完整性
     * 
     * @param backupId 备份ID
     * @return 验证结果
     */
    Map<String, Object> validateBackup(Long backupId);

    /**
     * 获取备份统计信息
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getBackupStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 清理过期备份
     * 
     * @param retentionDays 保留天数
     * @return 清理的备份数量
     */
    int cleanExpiredBackups(int retentionDays);

    /**
     * 创建定时备份任务
     * 
     * @param taskName 任务名称
     * @param backupType 备份类型
     * @param cronExpression cron表达式
     * @param operatorId 操作人ID
     * @return 任务ID
     */
    Long createScheduledBackupTask(String taskName, String backupType, String cronExpression, Long operatorId);

    /**
     * 停止定时备份任务
     * 
     * @param taskId 任务ID
     * @param operatorId 操作人ID
     * @return 是否停止成功
     */
    boolean stopScheduledBackupTask(Long taskId, Long operatorId);

    /**
     * 获取备份任务列表
     * 
     * @return 任务列表
     */
    List<Map<String, Object>> getScheduledBackupTasks();

    /**
     * 导出备份到外部存储
     * 
     * @param backupId 备份ID
     * @param storageType 存储类型（如：OSS、FTP等）
     * @param storageConfig 存储配置
     * @param operatorId 操作人ID
     * @return 是否导出成功
     */
    boolean exportBackup(Long backupId, String storageType, Map<String, Object> storageConfig, Long operatorId);

    /**
     * 从外部存储导入备份
     * 
     * @param backupName 备份名称
     * @param storageType 存储类型
     * @param storageConfig 存储配置
     * @param operatorId 操作人ID
     * @return 备份ID
     */
    Long importBackup(String backupName, String storageType, Map<String, Object> storageConfig, Long operatorId);

    /**
     * 获取备份进度
     * 
     * @param backupId 备份ID
     * @return 进度信息
     */
    Map<String, Object> getBackupProgress(Long backupId);

    /**
     * 取消正在进行的备份
     * 
     * @param backupId 备份ID
     * @param operatorId 操作人ID
     * @return 是否取消成功
     */
    boolean cancelBackup(Long backupId, Long operatorId);
}