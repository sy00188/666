package com.archive.management.repository;

import com.archive.management.entity.Backup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 备份Repository
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface BackupRepository extends JpaRepository<Backup, Long> {

    /**
     * 根据备份类型查询
     */
    List<Backup> findByBackupTypeAndDeletedFalseOrderByCreatedAtDesc(String backupType);

    /**
     * 根据状态查询
     */
    List<Backup> findByStatusAndDeletedFalseOrderByCreatedAtDesc(String status);

    /**
     * 查询未删除的所有备份
     */
    List<Backup> findByDeletedFalseOrderByCreatedAtDesc();

    /**
     * 根据时间范围查询
     */
    @Query("SELECT b FROM Backup b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate AND b.deleted = false ORDER BY b.createdAt DESC")
    List<Backup> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);

    /**
     * 查询过期的备份
     */
    @Query("SELECT b FROM Backup b WHERE b.expiryTime < :now AND b.deleted = false")
    List<Backup> findExpiredBackups(@Param("now") LocalDateTime now);

    /**
     * 查询指定操作人的备份
     */
    List<Backup> findByOperatorIdAndDeletedFalseOrderByCreatedAtDesc(Long operatorId);

    /**
     * 统计备份总数
     */
    @Query("SELECT COUNT(b) FROM Backup b WHERE b.deleted = false")
    long countAll();

    /**
     * 统计指定类型的备份数
     */
    long countByBackupTypeAndDeletedFalse(String backupType);

    /**
     * 统计指定状态的备份数
     */
    long countByStatusAndDeletedFalse(String status);

    /**
     * 获取总备份大小
     */
    @Query("SELECT COALESCE(SUM(b.fileSize), 0) FROM Backup b WHERE b.deleted = false")
    long getTotalBackupSize();

    /**
     * 查询最近的成功备份
     */
    @Query("SELECT b FROM Backup b WHERE b.status = 'completed' AND b.backupType = :backupType AND b.deleted = false ORDER BY b.createdAt DESC")
    List<Backup> findLatestSuccessfulBackups(@Param("backupType") String backupType);
}

