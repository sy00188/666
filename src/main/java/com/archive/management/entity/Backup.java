package com.archive.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 备份记录实体类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_backups")
public class Backup {

    /**
     * 备份ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 备份名称
     */
    @Column(nullable = false, length = 200)
    private String backupName;

    /**
     * 备份类型: database, file, full, incremental
     */
    @Column(nullable = false, length = 50)
    private String backupType;

    /**
     * 备份文件路径
     */
    @Column(length = 500)
    private String filePath;

    /**
     * 备份文件大小（字节）
     */
    private Long fileSize;

    /**
     * 基础备份ID（增量备份时使用）
     */
    private Long baseBackupId;

    /**
     * 备份状态: pending, running, completed, failed, deleted
     */
    @Column(nullable = false, length = 50)
    private String status;

    /**
     * 备份描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 备份耗时（毫秒）
     */
    private Long duration;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    @Column(length = 100)
    private String operatorName;

    /**
     * 错误信息（备份失败时）
     */
    @Column(length = 1000)
    private String errorMessage;

    /**
     * 是否已验证
     */
    private Boolean verified;

    /**
     * 验证时间
     */
    private LocalDateTime verifiedTime;

    /**
     * 验证结果
     */
    @Column(length = 500)
    private String verifiedResult;

    /**
     * 存储类型: local, oss, s3
     */
    @Column(length = 50)
    private String storageType;

    /**
     * 存储配置（JSON）
     */
    @Column(length = 1000)
    private String storageConfig;

    /**
     * 备份进度（0-100）
     */
    private Integer progress;

    /**
     * 是否可恢复
     */
    private Boolean restorable;

    /**
     * 保留天数
     */
    private Integer retentionDays;

    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;

    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 备份文件MD5
     */
    @Column(length = 32)
    private String md5;

    /**
     * 备份文件SHA256
     */
    @Column(length = 64)
    private String sha256;

    /**
     * 是否加密
     */
    private Boolean encrypted;

    /**
     * 备份包含的表（JSON数组）
     */
    @Column(length = 2000)
    private String includedTables;

    /**
     * 备份的数据库名称
     */
    @Column(length = 100)
    private String databaseName;

    /**
     * 是否删除
     */
    private Boolean deleted;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deleted == null) {
            deleted = false;
        }
        if (verified == null) {
            verified = false;
        }
        if (restorable == null) {
            restorable = true;
        }
        if (encrypted == null) {
            encrypted = false;
        }
        if (progress == null) {
            progress = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

