package com.archive.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知模板实体类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板代码（唯一标识）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String templateCode;

    /**
     * 模板名称
     */
    @Column(nullable = false, length = 200)
    private String templateName;

    /**
     * 模板类型: email, sms, system, wechat
     */
    @Column(nullable = false, length = 50)
    private String templateType;

    /**
     * 模板内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 标题模板
     */
    @Column(length = 500)
    private String titleTemplate;

    /**
     * 模板变量（JSON数组）
     */
    @Column(length = 2000)
    private String variables;

    /**
     * 模板描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 优先级: low, normal, high, urgent
     */
    @Column(length = 50)
    private String priority;

    /**
     * 发送渠道配置（JSON）
     */
    @Column(length = 1000)
    private String channelConfig;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

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
        if (enabled == null) {
            enabled = true;
        }
        if (priority == null) {
            priority = "normal";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
