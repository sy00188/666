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
            log.info("创建通知模板: name={}, type={}, createdBy={}", templateName, templateType, createdBy);
            
            // 创建通知模板实体
            NotificationTemplate template = new NotificationTemplate();
            template.setTemplateName(templateName);
            template.setTemplateContent(templateContent);
            template.setTemplateType(templateType);
            template.setCreatedBy(createdBy);
            template.setEnabled(true);
            template.setCreateTime(LocalDateTime.now());
            template.setUpdateTime(LocalDateTime.now());
            
            // 调用通知模板服务创建
            NotificationTemplate createdTemplate = notificationTemplateService.createTemplate(template);
            
            if (createdTemplate != null && createdTemplate.getId() != null) {
                log.info("通知模板创建成功，ID：{}", createdTemplate.getId());
                return true;
            } else {
                log.warn("通知模板创建失败");
                return false;
            }
            
        } catch (Exception e) {
            log.error("创建通知模板失败: name={}, type={}", templateName, templateType, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getNotificationTemplate(String templateName) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("获取通知模板: name={}", templateName);
            
            // 通过模板名称获取模板（假设templateName就是templateCode）
            NotificationTemplate template = notificationTemplateService.getTemplateByCode(templateName);
            
            if (template != null) {
                result.put("success", true);
                result.put("templateName", template.getTemplateName());
                result.put("templateContent", template.getTemplateContent());
                result.put("templateType", template.getTemplateType());
                result.put("enabled", template.isEnabled());
                result.put("createTime", template.getCreateTime());
                result.put("updateTime", template.getUpdateTime());
                log.info("通知模板获取成功: {}", templateName);
            } else {
                result.put("success", false);
                result.put("message", "模板不存在");
                log.warn("通知模板不存在: {}", templateName);
            }
            
        } catch (Exception e) {
            log.error("获取通知模板失败: name={}", templateName, e);
            result.put("success", false);
            result.put("message", "获取模板失败：" + e.getMessage());
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
        try {
            if (!StringUtils.hasText(template)) {
                return content;
            }
            
            // 使用通知模板服务渲染模板
            Map<String, Object> variables = new HashMap<>();
            variables.put("content", content);
            variables.put("timestamp", LocalDateTime.now());
            
            String renderedContent = notificationTemplateService.renderTemplate(template, variables);
            
            if (StringUtils.hasText(renderedContent)) {
                log.debug("模板应用成功: template={}", template);
                return renderedContent;
            } else {
                log.warn("模板渲染结果为空: template={}", template);
                return content;
            }
            
        } catch (Exception e) {
            log.error("应用模板失败: template={}", template, e);
            return content; // 失败时返回原内容
        }
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

    // ==================== 新增接口方法实现 ====================
    // ==================== 用户注册与登录相关通知 ====================

    @Override
    @Async
    public boolean sendWelcomeEmail(Long userId) {
        log.info("发送欢迎邮件: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，无法发送欢迎邮件");
            return false;
        }
        
        try {
            String title = "欢迎加入档案管理系统";
            String content = "尊敬的用户，欢迎您加入档案管理系统！我们很高兴为您提供服务。";
            return sendEmailNotification(userId, title, content, "welcome_email");
        } catch (Exception e) {
            log.error("发送欢迎邮件失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean sendAbnormalLoginAlert(Long userId, String loginIp) {
        log.warn("发送异常登录告警: userId={}, loginIp={}", userId, loginIp);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "异常登录告警";
            String content = String.format("检测到您的账户在陌生IP地址 %s 登录，如非本人操作，请立即修改密码。", loginIp);
            sendNotification(userId, title, content, "SECURITY_ALERT");
            return sendEmailNotification(userId, title, content, "abnormal_login_alert");
        } catch (Exception e) {
            log.error("发送异常登录告警失败: userId={}", userId, e);
            return false;
        }
    }

    // ==================== 用户数据变更相关通知 ====================

    @Override
    @Async
    public boolean sendSensitiveDataChangeAlert(Long userId, Long updaterId) {
        log.warn("发送敏感数据变更告警: userId={}, updaterId={}", userId, updaterId);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "敏感信息变更提醒";
            String content = String.format("您的敏感信息已被修改（操作者ID: %d），如非本人操作，请立即联系管理员。", updaterId);
            sendNotification(userId, title, content, "SECURITY_ALERT");
            return sendEmailNotification(userId, title, content, "sensitive_data_alert");
        } catch (Exception e) {
            log.error("发送敏感数据变更告警失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean sendUserDataUpdatedNotification(Long userId, Long updaterId) {
        log.info("发送用户数据更新通知: userId={}, updaterId={}", userId, updaterId);
        if (userId == null || userId.equals(updaterId)) {
            return false; // 自己更新自己的数据，不发送通知
        }
        
        try {
            String title = "个人信息已更新";
            String content = String.format("您的个人信息已被管理员更新（操作者ID: %d）。", updaterId);
            return sendNotification(userId, title, content, "SYSTEM_NOTICE");
        } catch (Exception e) {
            log.error("发送用户数据更新通知失败: userId={}", userId, e);
            return false;
        }
    }

    // ==================== 用户状态与权限变更通知 ====================

    @Override
    @Async
    public boolean sendUserStatusChangedNotification(Long userId, Long operatorId, String oldStatus, String newStatus) {
        log.info("发送用户状态变更通知: userId={}, operatorId={}, {} -> {}", userId, operatorId, oldStatus, newStatus);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "账户状态变更通知";
            String content = String.format("您的账户状态已从 %s 变更为 %s（操作者ID: %d）。", 
                                         oldStatus, newStatus, operatorId);
            return sendNotification(userId, title, content, "ACCOUNT_CHANGE");
        } catch (Exception e) {
            log.error("发送用户状态变更通知失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean sendPermissionUpgradeAlert(Long userId, Long operatorId) {
        log.info("发送权限升级告警: userId={}, operatorId={}", userId, operatorId);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "权限升级通知";
            String content = String.format("您的账户权限已被升级（操作者ID: %d），请谨慎使用新增权限。", operatorId);
            return sendNotification(userId, title, content, "PERMISSION_CHANGE");
        } catch (Exception e) {
            log.error("发送权限升级告警失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean sendUserRoleChangedNotification(Long userId, Long operatorId) {
        log.info("发送用户角色变更通知: userId={}, operatorId={}", userId, operatorId);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "角色变更通知";
            String content = String.format("您的角色已被修改（操作者ID: %d），您的权限可能已发生变化。", operatorId);
            return sendNotification(userId, title, content, "ROLE_CHANGE");
        } catch (Exception e) {
            log.error("发送用户角色变更通知失败: userId={}", userId, e);
            return false;
        }
    }

    // ==================== 密码与安全相关通知 ====================

    @Override
    @Async
    public boolean sendPasswordChangedNotification(Long userId, String changeType) {
        log.info("发送密码变更通知: userId={}, changeType={}", userId, changeType);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "密码修改通知";
            String content = String.format("您的密码已成功修改（变更类型: %s）。如非本人操作，请立即联系管理员。", changeType);
            sendNotification(userId, title, content, "SECURITY_NOTICE");
            return sendEmailNotification(userId, title, content, "password_changed");
        } catch (Exception e) {
            log.error("发送密码变更通知失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean sendWeakPasswordAlert(Long userId) {
        log.warn("发送弱密码告警: userId={}", userId);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "密码安全警告";
            String content = "检测到您的密码强度较弱，建议尽快修改为更安全的密码，以保护您的账户安全。";
            return sendNotification(userId, title, content, "SECURITY_ALERT");
        } catch (Exception e) {
            log.error("发送弱密码告警失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Async
    public boolean sendUserDeletedNotification(Long userId, Long deleterId, String deleteReason) {
        log.info("发送用户删除通知: userId={}, deleterId={}, reason={}", userId, deleterId, deleteReason);
        if (userId == null) {
            return false;
        }
        
        try {
            String title = "账户注销通知";
            String content = String.format("您的账户已被删除（操作者ID: %d，原因: %s）。", deleterId, deleteReason);
            // 用户删除时发送最后通知
            return sendEmailNotification(userId, title, content, "account_deleted");
        } catch (Exception e) {
            log.error("发送用户删除通知失败: userId={}", userId, e);
            return false;
        }
    }

    // ==================== 错误处理通知 ====================

    @Override
    @Async
    public boolean sendMessageProcessingErrorAlert(String messageType, String errorMessage) {
        log.error("发送消息处理错误告警: type={}, error={}", messageType, errorMessage);
        
        try {
            // 发送给系统管理员
            String title = "系统消息处理错误";
            String content = String.format("消息类型: %s\n错误信息: %s\n时间: %s", 
                                         messageType, errorMessage, LocalDateTime.now());
            // 这里应该获取系统管理员ID列表并发送通知
            // 暂时记录日志
            log.error("系统错误告警: {}", content);
            return true;
        } catch (Exception e) {
            log.error("发送消息处理错误告警失败", e);
            return false;
        }
    }
}