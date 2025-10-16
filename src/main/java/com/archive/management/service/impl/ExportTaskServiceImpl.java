package com.archive.management.service.impl;

import com.archive.management.entity.ExportTask;
import com.archive.management.repository.ExportTaskRepository;
import com.archive.management.service.ExportTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 导出任务服务实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportTaskServiceImpl implements ExportTaskService {
    
    private final ExportTaskRepository exportTaskRepository;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_ACTIVE_TASKS_PER_USER = 5; // 每个用户最多同时5个活动任务
    private static final int DEFAULT_EXPIRE_DAYS = 7; // 默认7天后过期
    
    @Override
    @Transactional
    public ExportTask createTask(
            String taskName,
            String exportType,
            String format,
            Map<String, Object> parameters,
            Long totalCount) {
        
        // 获取当前用户
        Long currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        
        // 检查用户的活动任务数量
        long activeTaskCount = exportTaskRepository.countActiveTasksByUser(currentUserId);
        if (activeTaskCount >= MAX_ACTIVE_TASKS_PER_USER) {
            throw new RuntimeException(
                String.format("您已有 %d 个导出任务正在进行中，请等待完成后再创建新任务", activeTaskCount)
            );
        }
        
        // 创建任务
        ExportTask task = new ExportTask();
        task.setTaskId(generateTaskId());
        task.setTaskName(taskName);
        task.setExportType(exportType);
        task.setFormat(format);
        task.setStatus(ExportTask.TaskStatus.PENDING.getCode());
        task.setProgress(0);
        task.setTotalCount(totalCount);
        task.setProcessedCount(0L);
        task.setCreatedBy(currentUserId);
        task.setCreatedByName(currentUserName);
        task.setCreatedAt(LocalDateTime.now());
        task.setExpireAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRE_DAYS));
        task.setPausable(totalCount > 10000); // 大于1万条记录的任务支持暂停
        
        // 序列化参数
        if (parameters != null && !parameters.isEmpty()) {
            try {
                task.setParameters(objectMapper.writeValueAsString(parameters));
            } catch (JsonProcessingException e) {
                log.error("序列化导出参数失败", e);
                throw new RuntimeException("导出参数格式错误");
            }
        }
        
        // 保存任务
        ExportTask savedTask = exportTaskRepository.save(task);
        
        log.info("创建导出任务成功: taskId={}, type={}, format={}, totalCount={}",
            savedTask.getTaskId(), exportType, format, totalCount);
        
        return savedTask;
    }
    
    @Override
    public ExportTask getTaskById(String taskId) {
        return exportTaskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new RuntimeException("导出任务不存在: " + taskId));
    }
    
    @Override
    public Page<ExportTask> getUserTasks(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return exportTaskRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    public Page<ExportTask> getUserTasksByStatus(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return exportTaskRepository.findByStatusAndCreatedByOrderByCreatedAtDesc(status, userId, pageable);
    }
    
    @Override
    @Transactional
    public void updateTaskProgress(String taskId, int progress, long processedCount, Long estimatedTime) {
        ExportTask task = getTaskById(taskId);
        task.setProgress(progress);
        task.setProcessedCount(processedCount);
        task.setEstimatedTime(estimatedTime);
        exportTaskRepository.save(task);
        
        log.debug("更新任务进度: taskId={}, progress={}%, processed={}/{}",
            taskId, progress, processedCount, task.getTotalCount());
    }
    
    @Override
    @Transactional
    public void startTask(String taskId) {
        ExportTask task = getTaskById(taskId);
        
        if (!ExportTask.TaskStatus.PENDING.getCode().equals(task.getStatus()) &&
            !ExportTask.TaskStatus.PAUSED.getCode().equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许启动: " + task.getStatus());
        }
        
        task.setStatus(ExportTask.TaskStatus.PROCESSING.getCode());
        task.setStartedAt(LocalDateTime.now());
        task.setPaused(false);
        exportTaskRepository.save(task);
        
        log.info("开始处理导出任务: taskId={}", taskId);
    }
    
    @Override
    @Transactional
    public void completeTask(String taskId, String filePath, String fileName, long fileSize) {
        ExportTask task = getTaskById(taskId);
        
        task.setStatus(ExportTask.TaskStatus.COMPLETED.getCode());
        task.setProgress(100);
        task.setFilePath(filePath);
        task.setFileName(fileName);
        task.setFileSize(fileSize);
        task.setCompletedAt(LocalDateTime.now());
        task.setEstimatedTime(0L);
        exportTaskRepository.save(task);
        
        log.info("导出任务完成: taskId={}, file={}, size={} bytes",
            taskId, fileName, fileSize);
    }
    
    @Override
    @Transactional
    public void failTask(String taskId, String errorMessage) {
        ExportTask task = getTaskById(taskId);
        
        task.setStatus(ExportTask.TaskStatus.FAILED.getCode());
        task.setErrorMessage(errorMessage);
        task.setCompletedAt(LocalDateTime.now());
        exportTaskRepository.save(task);
        
        log.error("导出任务失败: taskId={}, error={}", taskId, errorMessage);
    }
    
    @Override
    @Transactional
    public void cancelTask(String taskId) {
        ExportTask task = getTaskById(taskId);
        
        // 只有等待中和处理中的任务可以取消
        if (!ExportTask.TaskStatus.PENDING.getCode().equals(task.getStatus()) &&
            !ExportTask.TaskStatus.PROCESSING.getCode().equals(task.getStatus()) &&
            !ExportTask.TaskStatus.PAUSED.getCode().equals(task.getStatus())) {
            throw new RuntimeException("任务状态不允许取消: " + task.getStatus());
        }
        
        task.setStatus(ExportTask.TaskStatus.CANCELLED.getCode());
        task.setCompletedAt(LocalDateTime.now());
        exportTaskRepository.save(task);
        
        log.info("取消导出任务: taskId={}", taskId);
    }
    
    @Override
    @Transactional
    public void pauseTask(String taskId) {
        ExportTask task = getTaskById(taskId);
        
        if (!task.getPausable()) {
            throw new RuntimeException("该任务不支持暂停");
        }
        
        if (!ExportTask.TaskStatus.PROCESSING.getCode().equals(task.getStatus())) {
            throw new RuntimeException("只有处理中的任务可以暂停");
        }
        
        task.setStatus(ExportTask.TaskStatus.PAUSED.getCode());
        task.setPaused(true);
        exportTaskRepository.save(task);
        
        log.info("暂停导出任务: taskId={}", taskId);
    }
    
    @Override
    @Transactional
    public void resumeTask(String taskId) {
        ExportTask task = getTaskById(taskId);
        
        if (!ExportTask.TaskStatus.PAUSED.getCode().equals(task.getStatus())) {
            throw new RuntimeException("只有已暂停的任务可以恢复");
        }
        
        task.setStatus(ExportTask.TaskStatus.PENDING.getCode());
        task.setPaused(false);
        exportTaskRepository.save(task);
        
        log.info("恢复导出任务: taskId={}", taskId);
    }
    
    @Override
    @Transactional
    public void deleteTask(String taskId) {
        ExportTask task = getTaskById(taskId);
        
        // 只有已完成、失败、取消的任务可以删除
        if (ExportTask.TaskStatus.PROCESSING.getCode().equals(task.getStatus()) ||
            ExportTask.TaskStatus.PENDING.getCode().equals(task.getStatus())) {
            throw new RuntimeException("进行中的任务不能删除，请先取消任务");
        }
        
        exportTaskRepository.delete(task);
        log.info("删除导出任务: taskId={}", taskId);
    }
    
    @Override
    @Transactional
    public int cleanExpiredTasks() {
        List<ExportTask> expiredTasks = exportTaskRepository.findExpiredTasks(LocalDateTime.now());
        
        int deletedCount = 0;
        for (ExportTask task : expiredTasks) {
            // 删除文件
            if (StringUtils.hasText(task.getFilePath())) {
                try {
                    File file = new File(task.getFilePath());
                    if (file.exists() && file.delete()) {
                        log.info("任务文件删除成功: {}", task.getFilePath());
                    }
                } catch (Exception e) {
                    log.error("删除任务文件失败: {}", task.getFilePath(), e);
                }
            }
            exportTaskRepository.delete(task);
            deletedCount++;
        }
        
        if (deletedCount > 0) {
            log.info("清理过期导出任务: {} 个", deletedCount);
        }
        
        return deletedCount;
    }
    
    @Override
    public long getUserActiveTaskCount(Long userId) {
        return exportTaskRepository.countActiveTasksByUser(userId);
    }
    
    @Override
    public List<ExportTask> getActiveTasks() {
        return exportTaskRepository.findActiveTasks();
    }
    
    /**
     * 生成唯一任务ID
     */
    private String generateTaskId() {
        return "EXPORT_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            // 根据实际的用户对象获取ID
            Long userId = getCurrentUserId();
            return 1L; // 临时返回默认值
        }
        throw new RuntimeException("未登录或登录已过期");
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "系统";
    }
}

