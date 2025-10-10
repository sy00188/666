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

    // ==================== 用户注册与登录相关通知 ====================

    /**
     * 发送欢迎邮件
     * 
     * @param userId 用户ID
     * @return 是否发送成功
     */
    boolean sendWelcomeEmail(Long userId);

    /**
     * 发送异常登录告警
     * 
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 是否发送成功
     */
    boolean sendAbnormalLoginAlert(Long userId, String loginIp);

    // ==================== 用户数据变更相关通知 ====================

    /**
     * 发送敏感数据变更告警
     * 
     * @param userId 用户ID
     * @param updaterId 更新者ID
     * @return 是否发送成功
     */
    boolean sendSensitiveDataChangeAlert(Long userId, Long updaterId);

    /**
     * 发送用户数据更新通知
     * 
     * @param userId 用户ID
     * @param updaterId 更新者ID
     * @return 是否发送成功
     */
    boolean sendUserDataUpdatedNotification(Long userId, Long updaterId);

    // ==================== 用户状态与权限变更通知 ====================

    /**
     * 发送用户状态变更通知
     * 
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @return 是否发送成功
     */
    boolean sendUserStatusChangedNotification(Long userId, Long operatorId, String oldStatus, String newStatus);

    /**
     * 发送权限升级告警
     * 
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @return 是否发送成功
     */
    boolean sendPermissionUpgradeAlert(Long userId, Long operatorId);

    /**
     * 发送用户角色变更通知
     * 
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @return 是否发送成功
     */
    boolean sendUserRoleChangedNotification(Long userId, Long operatorId);

    // ==================== 密码与安全相关通知 ====================

    /**
     * 发送密码变更通知
     * 
     * @param userId 用户ID
     * @param changeType 变更类型
     * @return 是否发送成功
     */
    boolean sendPasswordChangedNotification(Long userId, String changeType);

    /**
     * 发送弱密码告警
     * 
     * @param userId 用户ID
     * @return 是否发送成功
     */
    boolean sendWeakPasswordAlert(Long userId);

    /**
     * 发送用户删除通知
     * 
     * @param userId 用户ID
     * @param deleterId 删除者ID
     * @param deleteReason 删除原因
     * @return 是否发送成功
     */
    boolean sendUserDeletedNotification(Long userId, Long deleterId, String deleteReason);

    // ==================== 错误处理通知 ====================

    /**
     * 发送消息处理错误告警
     * 
     * @param messageType 消息类型
     * @param errorMessage 错误信息
     * @return 是否发送成功
     */
    boolean sendMessageProcessingErrorAlert(String messageType, String errorMessage);

    // ==================== 档案相关通知 ====================
    
    /**
     * 发送档案创建通知
     * 
     * @param archiveId 档案ID
     * @param creatorId 创建人ID
     * @return 是否发送成功
     */
    boolean sendArchiveCreatedNotification(Long archiveId, Long creatorId);
    
    /**
     * 发送档案更新通知
     * 
     * @param archiveId 档案ID
     * @param updaterId 更新人ID
     * @param needsReapproval 是否需要重新审批
     * @return 是否发送成功
     */
    boolean sendArchiveUpdatedNotification(Long archiveId, Long updaterId, boolean needsReapproval);
    
    /**
     * 发送档案删除通知
     * 
     * @param archiveId 档案ID
     * @param deleterId 删除人ID
     * @param reason 删除原因
     * @return 是否发送成功
     */
    boolean sendArchiveDeletedNotification(Long archiveId, Long deleterId, String reason);
    
    /**
     * 发送档案归档通知
     * 
     * @param archiveId 档案ID
     * @param operatorId 操作人ID
     * @return 是否发送成功
     */
    boolean sendArchiveArchivedNotification(Long archiveId, Long operatorId);
    
    /**
     * 发送档案发布通知
     * 
     * @param archiveId 档案ID
     * @param publisherId 发布人ID
     * @param publishLevel 发布级别
     * @return 是否发送成功
     */
    boolean sendArchivePublishedNotification(Long archiveId, Long publisherId, String publishLevel);
    
    /**
     * 发送档案状态变更通知
     * 
     * @param archiveId 档案ID
     * @param operatorId 操作人ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @return 是否发送成功
     */
    boolean sendArchiveStatusChangedNotification(Long archiveId, Long operatorId, String oldStatus, String newStatus);
    
    /**
     * 发送档案权限变更通知
     * 
     * @param archiveId 档案ID
     * @param operatorId 操作人ID
     * @param changeType 变更类型
     * @return 是否发送成功
     */
    boolean sendArchivePermissionChangedNotification(Long archiveId, Long operatorId, String changeType);
}