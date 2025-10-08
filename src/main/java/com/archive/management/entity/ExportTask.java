package com.archive.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 导出任务实体
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Data
@Entity
@Table(name = "export_tasks")
public class ExportTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务ID（唯一标识符）
     */
    @Column(nullable = false, unique = true, length = 64)
    private String taskId;
    
    /**
     * 任务名称
     */
    @Column(nullable = false, length = 200)
    private String taskName;
    
    /**
     * 导出类型（archive/borrow/user/custom等）
     */
    @Column(nullable = false, length = 50)
    private String exportType;
    
    /**
     * 导出格式（excel/csv/pdf）
     */
    @Column(nullable = false, length = 20)
    private String format;
    
    /**
     * 任务状态（pending/processing/completed/failed/cancelled）
     */
    @Column(nullable = false, length = 20)
    private String status;
    
    /**
     * 进度百分比（0-100）
     */
    @Column(nullable = false)
    private Integer progress = 0;
    
    /**
     * 总记录数
     */
    private Long totalCount;
    
    /**
     * 已处理记录数
     */
    private Long processedCount = 0L;
    
    /**
     * 文件路径（导出完成后的文件路径）
     */
    @Column(length = 500)
    private String filePath;
    
    /**
     * 文件名
     */
    @Column(length = 200)
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 导出参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 创建人ID
     */
    @Column(nullable = false)
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    @Column(length = 100)
    private String createdByName;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 开始时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    /**
     * 过期时间（文件自动删除时间）
     */
    private LocalDateTime expireAt;
    
    /**
     * 预计剩余时间（毫秒）
     */
    private Long estimatedTime;
    
    /**
     * 是否可暂停
     */
    private Boolean pausable = false;
    
    /**
     * 是否已暂停
     */
    private Boolean paused = false;
    
    /**
     * 优先级（1-10，数字越大优先级越高）
     */
    private Integer priority = 5;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "pending";
        }
        if (progress == null) {
            progress = 0;
        }
        if (processedCount == null) {
            processedCount = 0L;
        }
    }
    
    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("pending", "等待中"),
        PROCESSING("processing", "处理中"),
        COMPLETED("completed", "已完成"),
        FAILED("failed", "失败"),
        CANCELLED("cancelled", "已取消"),
        PAUSED("paused", "已暂停");
        
        private final String code;
        private final String description;
        
        TaskStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

