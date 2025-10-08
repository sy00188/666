package com.archive.management.service;

import com.archive.management.entity.ExportTask;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 导出任务服务接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface ExportTaskService {
    
    /**
     * 创建导出任务
     * 
     * @param taskName 任务名称
     * @param exportType 导出类型
     * @param format 导出格式
     * @param parameters 导出参数
     * @param totalCount 总记录数
     * @return 创建的任务
     */
    ExportTask createTask(
        String taskName,
        String exportType,
        String format,
        Map<String, Object> parameters,
        Long totalCount
    );
    
    /**
     * 根据任务ID查询任务
     */
    ExportTask getTaskById(String taskId);
    
    /**
     * 查询用户的任务列表（分页）
     */
    Page<ExportTask> getUserTasks(Long userId, int page, int size);
    
    /**
     * 查询用户指定状态的任务列表（分页）
     */
    Page<ExportTask> getUserTasksByStatus(Long userId, String status, int page, int size);
    
    /**
     * 更新任务进度
     */
    void updateTaskProgress(String taskId, int progress, long processedCount, Long estimatedTime);
    
    /**
     * 标记任务开始处理
     */
    void startTask(String taskId);
    
    /**
     * 标记任务完成
     */
    void completeTask(String taskId, String filePath, String fileName, long fileSize);
    
    /**
     * 标记任务失败
     */
    void failTask(String taskId, String errorMessage);
    
    /**
     * 取消任务
     */
    void cancelTask(String taskId);
    
    /**
     * 暂停任务
     */
    void pauseTask(String taskId);
    
    /**
     * 恢复任务
     */
    void resumeTask(String taskId);
    
    /**
     * 删除任务
     */
    void deleteTask(String taskId);
    
    /**
     * 清理过期任务
     */
    int cleanExpiredTasks();
    
    /**
     * 获取用户的活动任务数量
     */
    long getUserActiveTaskCount(Long userId);
    
    /**
     * 获取所有活动任务
     */
    List<ExportTask> getActiveTasks();
}

