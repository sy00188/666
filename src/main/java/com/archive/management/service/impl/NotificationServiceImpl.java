package com.archive.management.service.impl;

import com.archive.management.entity.Notification;
import com.archive.management.mapper.NotificationMapper;
import com.archive.management.service.NotificationService;
import com.archive.management.service.SmsService;
import com.archive.management.util.EmailUtil;
import com.archive.management.websocket.NotificationWebSocketHandler;
import com.archive.management.constant.BusinessConstants;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 通知服务实现类
 * 实现通知相关的核心业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private SmsService smsService;

    @Override
    public boolean sendNotification(Long userId, String title, String content, String type) {
        try {
            Notification notification = buildNotification(userId, title, content, type);
            return sendNotificationInternal(notification);
        } catch (Exception e) {
            log.error("发送通知失败: userId={}, title={}, type={}", userId, title, type, e);
            return false;
        }
    }

    @Override
    public int sendBatchNotification(List<Long> userIds, String title, String content, String type) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        try {
            for (Long userId : userIds) {
                if (sendNotification(userId, title, content, type)) {
                    successCount++;
                }
            }
            log.info("批量通知发送完成: 成功{}/总共{}", successCount, userIds.size());
        } catch (Exception e) {
            log.error("批量发送通知失败", e);
        }
        return successCount;
    }

    @Override
    public boolean sendBroadcastNotification(String title, String content, String type, String targetType) {
        try {
            Notification notification = buildBroadcastNotification(title, content, type, targetType);
            return sendBroadcastNotificationInternal(notification);
        } catch (Exception e) {
            log.error("发送广播通知失败: title={}, type={}, targetType={}", title, type, targetType, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserNotifications(Long userId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<Notification> pageObj = new Page<>(page, size);
            QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
            
            queryWrapper.eq("user_id", userId)
                       .eq("deleted", 0)
                       .orderByDesc("priority")
                       .orderByDesc("create_time");

            IPage<Notification> pageResult = notificationMapper.selectPage(pageObj, queryWrapper);
            
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("current", pageResult.getCurrent());
            result.put("size", pageResult.getSize());
            result.put("pages", pageResult.getPages());
            
        } catch (Exception e) {
            log.error("获取用户通知列表失败: userId={}", userId, e);
            result.put("records", List.of());
            result.put("total", 0L);
            result.put("current", (long) page);
            result.put("size", (long) size);
            result.put("pages", 0L);
        }
        return result;
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.warn("通知不存在: notificationId={}", notificationId);
                return false;
            }

            // 检查权限（只能标记自己的通知或广播通知）
            if (notification.getUserId() != null && !notification.getUserId().equals(userId)) {
                log.warn("无权限标记通知为已读: notificationId={}, userId={}", notificationId, userId);
                return false;
            }

            // 如果已经是已读状态，直接返回成功
            if (BusinessConstants.NotificationStatus.READ.equals(notification.getStatus())) {
                return true;
            }

            // 标记为已读
            notification.setStatus(BusinessConstants.NotificationStatus.READ);
            notification.setReadTime(LocalDateTime.now());
            notification.setUpdateTime(LocalDateTime.now());

            int result = notificationMapper.updateById(notification);
            if (result > 0) {
                // 通过WebSocket更新未读数量
                updateUnreadCountAsync(userId);
                log.info("通知标记为已读成功: notificationId={}, userId={}", notificationId, userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("标记通知为已读失败: notificationId={}, userId={}", notificationId, userId, e);
            return false;
        }
    }

    @Override
    public int markBatchAsRead(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        try {
            for (Long notificationId : notificationIds) {
                if (markAsRead(notificationId, userId)) {
                    successCount++;
                }
            }
            log.info("批量标记通知为已读完成: 成功{}/总共{}", successCount, notificationIds.size());
        } catch (Exception e) {
            log.error("批量标记通知为已读失败: userId={}", userId, e);
        }
        return successCount;
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.warn("通知不存在: notificationId={}", notificationId);
                return false;
            }

            // 检查权限（只能删除自己的通知）
            if (notification.getUserId() != null && !notification.getUserId().equals(userId)) {
                log.warn("无权限删除通知: notificationId={}, userId={}", notificationId, userId);
                return false;
            }

            // 逻辑删除
            notification.setDeleted(1);
            notification.setUpdateTime(LocalDateTime.now());

            int result = notificationMapper.updateById(notification);
            if (result > 0) {
                // 通过WebSocket更新未读数量
                updateUnreadCountAsync(userId);
                log.info("删除通知成功: notificationId={}, userId={}", notificationId, userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除通知失败: notificationId={}, userId={}", notificationId, userId, e);
            return false;
        }
    }

    @Override
    public long getUnreadCount(Long userId) {
        try {
            QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("status", BusinessConstants.NotificationStatus.UNREAD)
                       .eq("deleted", 0);

            return notificationMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("获取未读通知数量失败: userId={}", userId, e);
            return 0L;
        }
    }

    @Override
    public boolean sendEmailNotification(Long userId, String title, String content, String template) {
        try {
            // 这里需要根据userId获取用户邮箱，暂时使用占位符
            // String userEmail = userService.getUserEmail(userId);
            String userEmail = "user@example.com"; // 占位符，需要实际的用户服务

            String emailContent = StringUtils.hasText(template) ? 
                    applyTemplate(content, template) : buildEmailContent(title, content);

            boolean result = emailUtil.sendSimpleEmail(userEmail, title, emailContent);
            
            if (result) {
                log.info("邮件通知发送成功: userId={}, email={}", userId, userEmail);
            } else {
                log.error("邮件通知发送失败: userId={}, email={}", userId, userEmail);
            }
            
            return result;
        } catch (Exception e) {
            log.error("发送邮件通知异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean sendSmsNotification(Long userId, String content, String template) {
        try {
            // 这里需要根据userId获取用户手机号，暂时使用占位符
            // String userPhone = userService.getUserPhone(userId);
            String userPhone = "13800138000"; // 占位符，需要实际的用户服务

            String smsContent = StringUtils.hasText(template) ? 
                    applyTemplate(content, template) : buildSmsContent(content);

            // 使用SmsService发送短信
            boolean result = smsService.sendSms(userPhone, smsContent);

            if (result) {
                log.info("短信通知发送成功: userId={}, phone={}", userId, userPhone);
            } else {
                log.error("短信通知发送失败: userId={}, phone={}", userId, userPhone);
            }
            
            return result;
        } catch (Exception e) {
            log.error("发送短信通知异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean createNotificationTemplate(String templateName, String templateContent, String templateType, Long createdBy) {
        try {
            // TODO: 实现通知模板创建逻辑
            // 这里需要创建NotificationTemplate实体和相应的Mapper
            log.info("创建通知模板: name={}, type={}, createdBy={}", templateName, templateType, createdBy);
            return true; // 占位符实现
        } catch (Exception e) {
            log.error("创建通知模板失败: name={}, type={}", templateName, templateType, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getNotificationTemplate(String templateName) {
        Map<String, Object> result = new HashMap<>();
        try {
            // TODO: 实现获取通知模板逻辑
            result.put("templateName", templateName);
            result.put("templateContent", "模板内容占位符");
            result.put("templateType", "EMAIL");
            log.info("获取通知模板: name={}", templateName);
        } catch (Exception e) {
            log.error("获取通知模板失败: name={}", templateName, e);
        }
        return result;
    }

    @Override
    public Map<String, Object> getNotificationStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
            queryWrapper.between("create_time", startDate, endDate)
                       .eq("deleted", 0);

            // 总通知数
            Long totalCount = notificationMapper.selectCount(queryWrapper);
            result.put("totalCount", totalCount);

            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            typeStats.put("SYSTEM", getCountByType(startDate, endDate, BusinessConstants.NotificationType.SYSTEM));
            typeStats.put("BUSINESS", getCountByType(startDate, endDate, BusinessConstants.NotificationType.BUSINESS));
            typeStats.put("WARNING", getCountByType(startDate, endDate, BusinessConstants.NotificationType.WARNING));
            typeStats.put("ERROR", getCountByType(startDate, endDate, BusinessConstants.NotificationType.ERROR));
            result.put("typeStatistics", typeStats);

            // 已读/未读统计
            Long readCount = getCountByStatus(startDate, endDate, BusinessConstants.NotificationStatus.READ);
            Long unreadCount = getCountByStatus(startDate, endDate, BusinessConstants.NotificationStatus.UNREAD);
            result.put("readCount", readCount);
            result.put("unreadCount", unreadCount);

            log.info("获取通知统计成功: startDate={}, endDate={}, total={}", startDate, endDate, totalCount);
        } catch (Exception e) {
            log.error("获取通知统计失败: startDate={}, endDate={}", startDate, endDate, e);
        }
        return result;
    }

    @Override
    public int cleanExpiredNotifications(int days) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
            QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
            queryWrapper.lt("create_time", expireTime)
                       .eq("status", BusinessConstants.NotificationStatus.READ);

            int count = notificationMapper.delete(queryWrapper);
            log.info("清理过期通知完成: 清理{}天前的通知，共清理{}条", days, count);
            return count;
        } catch (Exception e) {
            log.error("清理过期通知失败: days={}", days, e);
            return 0;
        }
    }

    @Override
    public boolean sendSystemAlert(String alertType, String alertContent, String severity) {
        try {
            String title = String.format("系统告警 - %s", alertType);
            String content = String.format("[%s] %s", severity, alertContent);
            
            // 系统告警通常发送给管理员，这里需要获取管理员用户列表
            // List<Long> adminUserIds = userService.getAdminUserIds();
            // 暂时使用占位符
            List<Long> adminUserIds = List.of(1L); // 占位符
            
            return sendBatchNotification(adminUserIds, title, content, 
                    String.valueOf(BusinessConstants.NotificationType.ERROR)) > 0;
        } catch (Exception e) {
            log.error("发送系统告警失败: alertType={}, severity={}", alertType, severity, e);
            return false;
        }
    }

    @Override
    public boolean sendUserOperationNotification(Long userId, String operationType, String operationContent) {
        try {
            String title = String.format("用户操作通知 - %s", operationType);
            return sendNotification(userId, title, operationContent, 
                    String.valueOf(BusinessConstants.NotificationType.BUSINESS));
        } catch (Exception e) {
            log.error("发送用户操作通知失败: userId={}, operationType={}", userId, operationType, e);
            return false;
        }
    }

    @Override
    public boolean sendArchiveNotification(Long userId, Long archiveId, String operationType, String operationContent) {
        try {
            String title = String.format("档案操作通知 - %s", operationType);
            String content = String.format("档案ID: %d, %s", archiveId, operationContent);
            return sendNotification(userId, title, content, 
                    String.valueOf(BusinessConstants.NotificationType.BUSINESS));
        } catch (Exception e) {
            log.error("发送档案通知失败: userId={}, archiveId={}, operationType={}", userId, archiveId, operationType, e);
            return false;
        }
    }

    @Override
    public boolean sendBorrowNotification(Long userId, Long borrowId, String notificationType, String content) {
        try {
            String title = String.format("借阅通知 - %s", notificationType);
            String fullContent = String.format("借阅ID: %d, %s", borrowId, content);
            return sendNotification(userId, title, fullContent, 
                    String.valueOf(BusinessConstants.NotificationType.BUSINESS));
        } catch (Exception e) {
            log.error("发送借阅通知失败: userId={}, borrowId={}, notificationType={}", userId, borrowId, notificationType, e);
            return false;
        }
    }

    @Override
    public boolean sendApprovalNotification(Long userId, Long approvalId, String approvalType, String approvalStatus) {
        try {
            String title = String.format("审批通知 - %s", approvalType);
            String content = String.format("审批ID: %d, 状态: %s", approvalId, approvalStatus);
            return sendNotification(userId, title, content, 
                    String.valueOf(BusinessConstants.NotificationType.BUSINESS));
        } catch (Exception e) {
            log.error("发送审批通知失败: userId={}, approvalId={}, approvalType={}", userId, approvalId, approvalType, e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建通知对象
     */
    private Notification buildNotification(Long userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(Integer.valueOf(type));
        notification.setStatus(BusinessConstants.NotificationStatus.UNREAD);
        notification.setPriority(1); // 默认普通优先级
        notification.setCreateTime(LocalDateTime.now());
        notification.setEmailNotify(1); // 默认启用邮件通知
        notification.setSmsNotify(0); // 默认不启用短信通知
        notification.setWebsocketNotify(1); // 默认启用WebSocket通知
        notification.setDeleted(0);
        return notification;
    }

    /**
     * 构建广播通知对象
     */
    private Notification buildBroadcastNotification(String title, String content, String type, String targetType) {
        Notification notification = new Notification();
        notification.setUserId(null); // 广播通知userId为null
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(Integer.valueOf(type));
        notification.setStatus(BusinessConstants.NotificationStatus.UNREAD);
        notification.setPriority(2); // 广播通知默认高优先级
        notification.setCreateTime(LocalDateTime.now());
        notification.setEmailNotify(0); // 广播通知默认不启用邮件
        notification.setSmsNotify(0); // 广播通知默认不启用短信
        notification.setWebsocketNotify(1); // 广播通知启用WebSocket
        notification.setBusinessType(targetType); // 使用businessType存储目标类型
        notification.setDeleted(0);
        return notification;
    }

    /**
     * 内部发送通知方法
     */
    private boolean sendNotificationInternal(Notification notification) {
        try {
            // 保存到数据库
            int result = notificationMapper.insert(notification);
            if (result <= 0) {
                log.error("保存通知到数据库失败: {}", notification);
                return false;
            }

            // 异步发送各种通知
            sendAsyncNotifications(notification);

            log.info("通知发送成功: notificationId={}, userId={}, type={}", 
                    notification.getNotificationId(), notification.getUserId(), notification.getNotificationType());
            return true;
        } catch (Exception e) {
            log.error("发送通知失败: {}", notification, e);
            return false;
        }
    }

    /**
     * 内部发送广播通知方法
     */
    private boolean sendBroadcastNotificationInternal(Notification notification) {
        try {
            // 保存到数据库
            int result = notificationMapper.insert(notification);
            if (result <= 0) {
                log.error("保存广播通知到数据库失败: {}", notification);
                return false;
            }

            // 发送WebSocket广播通知
            if (notification.needWebsocketNotify()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        webSocketHandler.broadcastNotification(notification);
                    } catch (Exception e) {
                        log.error("发送WebSocket广播通知失败: notificationId={}", notification.getNotificationId(), e);
                    }
                });
            }

            log.info("广播通知发送成功: notificationId={}, type={}", 
                    notification.getNotificationId(), notification.getNotificationType());
            return true;
        } catch (Exception e) {
            log.error("发送广播通知失败: {}", notification, e);
            return false;
        }
    }

    /**
     * 异步发送各种类型的通知
     */
    private void sendAsyncNotifications(Notification notification) {
        CompletableFuture.runAsync(() -> {
            try {
                // WebSocket通知
                if (notification.needWebsocketNotify()) {
                    if (notification.getUserId() != null) {
                        webSocketHandler.sendNotificationToUser(String.valueOf(notification.getUserId()), notification);
                    } else {
                        webSocketHandler.broadcastNotification(notification);
                    }
                }

                // 邮件通知
                if (notification.needEmailNotify()) {
                    sendEmailNotificationInternal(notification);
                }

                // 短信通知
                if (notification.needSmsNotify()) {
                    sendSmsNotificationInternal(notification);
                }
            } catch (Exception e) {
                log.error("异步发送通知失败: notificationId={}", notification.getNotificationId(), e);
            }
        });
    }

    /**
     * 内部邮件通知发送
     */
    private boolean sendEmailNotificationInternal(Notification notification) {
        try {
            // 这里需要根据userId获取用户邮箱
            String userEmail = "user@example.com"; // 占位符
            String content = buildEmailContent(notification.getTitle(), notification.getContent());
            return emailUtil.sendSimpleEmail(userEmail, notification.getTitle(), content);
        } catch (Exception e) {
            log.error("发送邮件通知失败: notificationId={}", notification.getNotificationId(), e);
            return false;
        }
    }

    /**
     * 内部短信通知发送
     */
    private boolean sendSmsNotificationInternal(Notification notification) {
        try {
            // 这里需要根据userId获取用户手机号
            String userPhone = "13800138000"; // 占位符，需要实际的用户服务获取
            String content = buildSmsContent(notification.getContent());
            
            // 使用SmsService发送短信
            return smsService.sendNotificationSms(userPhone, notification.getTitle(), content);
        } catch (Exception e) {
            log.error("发送短信通知失败: notificationId={}", notification.getNotificationId(), e);
            return false;
        }
    }

    /**
     * 异步更新未读数量
     */
    private void updateUnreadCountAsync(Long userId) {
        CompletableFuture.runAsync(() -> {
            try {
                // 这里需要实现WebSocketHandler的updateUnreadCount方法
                // webSocketHandler.updateUnreadCount(userId);
                log.debug("更新用户未读通知数量: userId={}", userId);
            } catch (Exception e) {
                log.error("更新未读通知数量失败: userId={}", userId, e);
            }
        });
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String title, String content) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("尊敬的用户，您有一条新的通知：\n\n");
        emailContent.append("标题：").append(title).append("\n");
        emailContent.append("内容：").append(content).append("\n");
        emailContent.append("时间：").append(LocalDateTime.now()).append("\n\n");
        emailContent.append("请及时查看处理。\n\n");
        emailContent.append("此邮件由系统自动发送，请勿回复。");
        return emailContent.toString();
    }

    /**
     * 构建短信内容
     */
    private String buildSmsContent(String content) {
        // 短信内容需要简洁
        return String.format("【档案管理系统】%s", 
                content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }

    /**
     * 应用模板
     */
    private String applyTemplate(String content, String template) {
        // TODO: 实现模板应用逻辑
        return content; // 占位符实现
    }

    /**
     * 按类型统计通知数量
     */
    private Long getCountByType(LocalDateTime startDate, LocalDateTime endDate, Integer type) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startDate, endDate)
                   .eq("notification_type", type)
                   .eq("deleted", 0);
        return notificationMapper.selectCount(queryWrapper);
    }

    /**
     * 按状态统计通知数量
     */
    private Long getCountByStatus(LocalDateTime startDate, LocalDateTime endDate, Integer status) {
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startDate, endDate)
                   .eq("status", status)
                   .eq("deleted", 0);
        return notificationMapper.selectCount(queryWrapper);
    }
}