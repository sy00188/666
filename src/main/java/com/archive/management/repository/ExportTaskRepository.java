package com.archive.management.repository;

import com.archive.management.entity.ExportTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 导出任务数据仓库
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTask, Long> {
    
    /**
     * 根据任务ID查找任务
     */
    Optional<ExportTask> findByTaskId(String taskId);
    
    /**
     * 根据创建人查询任务列表（分页）
     */
    Page<ExportTask> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);
    
    /**
     * 根据状态查询任务列表
     */
    List<ExportTask> findByStatus(String status);
    
    /**
     * 根据状态和创建人查询任务列表
     */
    Page<ExportTask> findByStatusAndCreatedByOrderByCreatedAtDesc(
        String status, Long createdBy, Pageable pageable
    );
    
    /**
     * 查询指定用户的进行中任务数量
     */
    @Query("SELECT COUNT(t) FROM ExportTask t WHERE t.createdBy = :userId AND t.status IN ('pending', 'processing')")
    long countActiveTasksByUser(@Param("userId") Long userId);
    
    /**
     * 查询所有进行中的任务
     */
    @Query("SELECT t FROM ExportTask t WHERE t.status IN ('pending', 'processing') ORDER BY t.priority DESC, t.createdAt ASC")
    List<ExportTask> findActiveTasks();
    
    /**
     * 查询已过期的任务（需要清理）
     */
    @Query("SELECT t FROM ExportTask t WHERE t.status = 'completed' AND t.expireAt < :now")
    List<ExportTask> findExpiredTasks(@Param("now") LocalDateTime now);
    
    /**
     * 删除已过期的任务
     */
    @Modifying
    @Query("DELETE FROM ExportTask t WHERE t.status = 'completed' AND t.expireAt < :now")
    int deleteExpiredTasks(@Param("now") LocalDateTime now);
    
    /**
     * 更新任务进度
     */
    @Modifying
    @Query("UPDATE ExportTask t SET t.progress = :progress, t.processedCount = :processedCount, t.estimatedTime = :estimatedTime WHERE t.taskId = :taskId")
    int updateProgress(
        @Param("taskId") String taskId,
        @Param("progress") Integer progress,
        @Param("processedCount") Long processedCount,
        @Param("estimatedTime") Long estimatedTime
    );
    
    /**
     * 取消用户的所有待处理任务
     */
    @Modifying
    @Query("UPDATE ExportTask t SET t.status = 'cancelled' WHERE t.createdBy = :userId AND t.status = 'pending'")
    int cancelPendingTasksByUser(@Param("userId") Long userId);
}

