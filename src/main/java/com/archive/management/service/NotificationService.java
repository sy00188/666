package com.archive.management.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 负责处理系统通知相关的业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface NotificationService {

    /**
     * 发送系统通知
     * 
     * @param userId 用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 是否发送成功
     */
    boolean sendNotification(Long userId, String title, String content, String type);

    /**
     * 批量发送通知
     * 
     * @param userIds 用户ID列表
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @return 发送成功的数量
     */
    int sendBatchNotification(List<Long> userIds, String title, String content, String type);

    /**
     * 发送广播通知
     * 
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param targetType 目标类型（ALL_USERS, ADMIN_USERS等）
     * @return 是否发送成功
     */
    boolean sendBroadcastNotification(String title, String content, String type, String targetType);

    /**
     * 获取用户通知列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 通知列表
     */
    Map<String, Object> getUserNotifications(Long userId, int page, int size);

    /**
     * 标记通知为已读
     * 
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否标记成功
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记通知为已读
     * 
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @return 标记成功的数量
     */
    int markBatchAsRead(List<Long> notificationIds, Long userId);

    /**
     * 删除通知
     * 
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 获取未读通知数量
     * 
     * @param userId 用户ID
     * @return 未读通知数量
     */
    long getUnreadCount(Long userId);

    /**
     * 发送邮件通知
     * 
     * @param userId 用户ID
     * @param title 邮件标题
     * @param content 邮件内容
     * @param template 邮件模板
     * @return 是否发送成功
     */
    boolean sendEmailNotification(Long userId, String title, String content, String template);

    /**
     * 发送短信通知
     * 
     * @param userId 用户ID
     * @param content 短信内容
     * @param template 短信模板
     * @return 是否发送成功
     */
    boolean sendSmsNotification(Long userId, String content, String template);

    /**
     * 创建通知模板
     * 
     * @param templateName 模板名称
     * @param templateContent 模板内容
     * @param templateType 模板类型
     * @param createdBy 创建人ID
     * @return 是否创建成功
     */
    boolean createNotificationTemplate(String templateName, String templateContent, String templateType, Long createdBy);

    /**
     * 获取通知模板
     * 
     * @param templateName 模板名称
     * @return 模板内容
     */
    Map<String, Object> getNotificationTemplate(String templateName);

    /**
     * 获取通知统计信息
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getNotificationStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 清理过期通知
     * 
     * @param days 保留天数
     * @return 清理的数量
     */
    int cleanExpiredNotifications(int days);

    /**
     * 发送系统告警通知
     * 
     * @param alertType 告警类型
     * @param alertContent 告警内容
     * @param severity 严重程度
     * @return 是否发送成功
     */
    boolean sendSystemAlert(String alertType, String alertContent, String severity);

    /**
     * 发送用户操作通知
     * 
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param operationContent 操作内容
     * @return 是否发送成功
     */
    boolean sendUserOperationNotification(Long userId, String operationType, String operationContent);

    /**
     * 发送档案相关通知
     * 
     * @param userId 用户ID
     * @param archiveId 档案ID
     * @param operationType 操作类型
     * @param operationContent 操作内容
     * @return 是否发送成功
     */
    boolean sendArchiveNotification(Long userId, Long archiveId, String operationType, String operationContent);

    /**
     * 发送借阅相关通知
     * 
     * @param userId 用户ID
     * @param borrowId 借阅ID
     * @param notificationType 通知类型
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean sendBorrowNotification(Long userId, Long borrowId, String notificationType, String content);

    /**
     * 发送审批相关通知
     * 
     * @param userId 用户ID
     * @param approvalId 审批ID
     * @param approvalType 审批类型
     * @param approvalStatus 审批状态
     * @return 是否发送成功
     */
    boolean sendApprovalNotification(Long userId, Long approvalId, String approvalType, String approvalStatus);
}