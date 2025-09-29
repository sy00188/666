package com.archive.management.mq.listener;

import com.archive.management.dto.UserDTO;
import com.archive.management.service.UserService;
import com.archive.management.service.AuditLogService;
import com.archive.management.service.NotificationService;
import com.archive.management.service.CacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户消息监听器
 * 负责处理用户相关的消息队列消息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserMessageListener {

    private final UserService userService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    /**
     * 监听用户注册消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.registered:user.registered.queue}")
    @Transactional
    public void handleUserRegistered(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            String registrationSource = messageData.has("registrationSource") ? 
                messageData.get("registrationSource").asText() : "WEB";
            
            log.info("处理用户注册消息，用户ID: {}, 注册来源: {}", userId, registrationSource);
            
            // 处理用户注册后的业务逻辑
            processUserRegistered(userId, registrationSource);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_REGISTERED", userId, 
                "用户注册消息处理完成，注册来源: " + registrationSource, null);
            
            log.info("用户注册消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户注册消息失败", e);
            handleMessageProcessingError(message, e, "USER_REGISTERED");
        }
    }

    /**
     * 监听用户登录消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.login:user.login.queue}")
    @Transactional
    public void handleUserLogin(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            String loginIp = messageData.has("loginIp") ? messageData.get("loginIp").asText() : null;
            String userAgent = messageData.has("userAgent") ? messageData.get("userAgent").asText() : null;
            LocalDateTime loginTime = messageData.has("loginTime") ? 
                LocalDateTime.parse(messageData.get("loginTime").asText()) : LocalDateTime.now();
            
            log.info("处理用户登录消息，用户ID: {}, 登录IP: {}", userId, loginIp);
            
            // 处理用户登录后的业务逻辑
            processUserLogin(userId, loginIp, userAgent, loginTime);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_LOGIN", userId, 
                String.format("用户登录消息处理完成，登录IP: %s", loginIp), null);
            
            log.info("用户登录消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户登录消息失败", e);
            handleMessageProcessingError(message, e, "USER_LOGIN");
        }
    }

    /**
     * 监听用户登出消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.logout:user.logout.queue}")
    @Transactional
    public void handleUserLogout(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            String logoutReason = messageData.has("logoutReason") ? 
                messageData.get("logoutReason").asText() : "NORMAL";
            LocalDateTime logoutTime = messageData.has("logoutTime") ? 
                LocalDateTime.parse(messageData.get("logoutTime").asText()) : LocalDateTime.now();
            
            log.info("处理用户登出消息，用户ID: {}, 登出原因: {}", userId, logoutReason);
            
            // 处理用户登出后的业务逻辑
            processUserLogout(userId, logoutReason, logoutTime);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_LOGOUT", userId, 
                "用户登出消息处理完成，登出原因: " + logoutReason, null);
            
            log.info("用户登出消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户登出消息失败", e);
            handleMessageProcessingError(message, e, "USER_LOGOUT");
        }
    }

    /**
     * 监听用户信息更新消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.updated:user.updated.queue}")
    @Transactional
    public void handleUserUpdated(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            Long updaterId = messageData.get("updaterId").asLong();
            JsonNode oldDataNode = messageData.get("oldData");
            JsonNode newDataNode = messageData.get("newData");
            
            log.info("处理用户信息更新消息，用户ID: {}, 更新者ID: {}", userId, updaterId);
            
            // 处理用户信息更新后的业务逻辑
            processUserUpdated(userId, updaterId, oldDataNode, newDataNode);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_UPDATED", updaterId, 
                "用户信息更新消息处理完成", null);
            
            log.info("用户信息更新消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户信息更新消息失败", e);
            handleMessageProcessingError(message, e, "USER_UPDATED");
        }
    }

    /**
     * 监听用户状态变更消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.status.changed:user.status.changed.queue}")
    @Transactional
    public void handleUserStatusChanged(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            Long operatorId = messageData.get("operatorId").asLong();
            String oldStatus = messageData.get("oldStatus").asText();
            String newStatus = messageData.get("newStatus").asText();
            String changeReason = messageData.has("changeReason") ? 
                messageData.get("changeReason").asText() : null;
            
            log.info("处理用户状态变更消息，用户ID: {}, 操作者ID: {}, 状态变更: {} -> {}", 
                userId, operatorId, oldStatus, newStatus);
            
            // 处理用户状态变更后的业务逻辑
            processUserStatusChanged(userId, operatorId, oldStatus, newStatus, changeReason);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_STATUS_CHANGED", operatorId, 
                String.format("用户状态变更消息处理完成，状态变更: %s -> %s，变更原因: %s", 
                    oldStatus, newStatus, changeReason), null);
            
            log.info("用户状态变更消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户状态变更消息失败", e);
            handleMessageProcessingError(message, e, "USER_STATUS_CHANGED");
        }
    }

    /**
     * 监听用户角色变更消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.role.changed:user.role.changed.queue}")
    @Transactional
    public void handleUserRoleChanged(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            Long operatorId = messageData.get("operatorId").asLong();
            JsonNode oldRolesNode = messageData.get("oldRoles");
            JsonNode newRolesNode = messageData.get("newRoles");
            
            log.info("处理用户角色变更消息，用户ID: {}, 操作者ID: {}", userId, operatorId);
            
            // 处理用户角色变更后的业务逻辑
            processUserRoleChanged(userId, operatorId, oldRolesNode, newRolesNode);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_ROLE_CHANGED", operatorId, 
                "用户角色变更消息处理完成", null);
            
            log.info("用户角色变更消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户角色变更消息失败", e);
            handleMessageProcessingError(message, e, "USER_ROLE_CHANGED");
        }
    }

    /**
     * 监听用户密码变更消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.password.changed:user.password.changed.queue}")
    @Transactional
    public void handleUserPasswordChanged(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            String changeType = messageData.has("changeType") ? 
                messageData.get("changeType").asText() : "SELF_CHANGE";
            String changeIp = messageData.has("changeIp") ? messageData.get("changeIp").asText() : null;
            
            log.info("处理用户密码变更消息，用户ID: {}, 变更类型: {}", userId, changeType);
            
            // 处理用户密码变更后的业务逻辑
            processUserPasswordChanged(userId, changeType, changeIp);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_PASSWORD_CHANGED", userId, 
                "用户密码变更消息处理完成，变更类型: " + changeType, null);
            
            log.info("用户密码变更消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户密码变更消息失败", e);
            handleMessageProcessingError(message, e, "USER_PASSWORD_CHANGED");
        }
    }

    /**
     * 监听用户删除消息
     */
    @RabbitListener(queues = "${app.mq.queue.user.deleted:user.deleted.queue}")
    @Transactional
    public void handleUserDeleted(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long userId = messageData.get("userId").asLong();
            Long deleterId = messageData.get("deleterId").asLong();
            String deleteReason = messageData.has("deleteReason") ? 
                messageData.get("deleteReason").asText() : null;
            
            log.info("处理用户删除消息，用户ID: {}, 删除者ID: {}", userId, deleterId);
            
            // 处理用户删除后的业务逻辑
            processUserDeleted(userId, deleterId, deleteReason);
            
            // 记录审计日志
            auditLogService.recordUserOperation(userId, "USER_DELETED", deleterId, 
                "用户删除消息处理完成，删除原因: " + deleteReason, null);
            
            log.info("用户删除消息处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户删除消息失败", e);
            handleMessageProcessingError(message, e, "USER_DELETED");
        }
    }

    /**
     * 处理用户注册后的业务逻辑
     */
    private void processUserRegistered(Long userId, String registrationSource) {
        try {
            // 1. 初始化用户配置
            userService.initializeUserSettings(userId);
            
            // 2. 分配默认角色
            userService.assignDefaultRoles(userId);
            
            // 3. 发送欢迎邮件
            notificationService.sendWelcomeEmail(userId);
            
            // 4. 创建用户工作空间
            userService.createUserWorkspace(userId);
            
            // 5. 记录注册统计
            userService.updateRegistrationStatistics(registrationSource);
            
            log.debug("用户注册后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户注册后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户登录后的业务逻辑
     */
    private void processUserLogin(Long userId, String loginIp, String userAgent, LocalDateTime loginTime) {
        try {
            // 1. 更新最后登录时间
            userService.updateLastLoginTime(userId, loginTime);
            
            // 2. 记录登录历史
            userService.recordLoginHistory(userId, loginIp, userAgent, loginTime);
            
            // 3. 检查异常登录
            if (userService.isAbnormalLogin(userId, loginIp)) {
                notificationService.sendAbnormalLoginAlert(userId, loginIp);
            }
            
            // 4. 更新用户在线状态
            userService.updateOnlineStatus(userId, true);
            
            // 5. 清理过期会话
            userService.cleanupExpiredSessions(userId);
            
            // 6. 更新登录统计
            userService.updateLoginStatistics(userId);
            
            log.debug("用户登录后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户登录后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户登出后的业务逻辑
     */
    private void processUserLogout(Long userId, String logoutReason, LocalDateTime logoutTime) {
        try {
            // 1. 更新用户在线状态
            userService.updateOnlineStatus(userId, false);
            
            // 2. 记录登出历史
            userService.recordLogoutHistory(userId, logoutReason, logoutTime);
            
            // 3. 清理用户会话
            userService.cleanupUserSessions(userId);
            
            // 4. 清理临时数据
            cacheService.clearUserTemporaryData(userId);
            
            // 5. 更新在线时长统计
            userService.updateOnlineTimeStatistics(userId, logoutTime);
            
            log.debug("用户登出后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户登出后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户信息更新后的业务逻辑
     */
    private void processUserUpdated(Long userId, Long updaterId, JsonNode oldData, JsonNode newData) {
        try {
            // 1. 清理用户缓存
            cacheService.clearUserCache(userId);
            
            // 2. 检查敏感信息变更
            if (userService.hasSensitiveDataChanged(oldData, newData)) {
                notificationService.sendSensitiveDataChangeAlert(userId, updaterId);
            }
            
            // 3. 更新用户索引
            userService.updateUserIndex(userId);
            
            // 4. 同步到相关系统
            userService.syncUserDataToExternalSystems(userId);
            
            // 5. 发送更新通知
            if (!userId.equals(updaterId)) {
                notificationService.sendUserDataUpdatedNotification(userId, updaterId);
            }
            
            log.debug("用户信息更新后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户信息更新后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户状态变更后的业务逻辑
     */
    private void processUserStatusChanged(Long userId, Long operatorId, String oldStatus, String newStatus, String changeReason) {
        try {
            // 1. 根据新状态执行相应操作
            switch (newStatus) {
                case "DISABLED":
                    userService.disableUserSessions(userId);
                    break;
                case "LOCKED":
                    userService.lockUserAccount(userId);
                    break;
                case "ACTIVE":
                    userService.activateUserAccount(userId);
                    break;
            }
            
            // 2. 清理用户缓存
            cacheService.clearUserCache(userId);
            
            // 3. 发送状态变更通知
            notificationService.sendUserStatusChangedNotification(userId, operatorId, oldStatus, newStatus);
            
            // 4. 更新统计信息
            userService.updateStatusStatistics(oldStatus, newStatus);
            
            log.debug("用户状态变更后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户状态变更后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户角色变更后的业务逻辑
     */
    private void processUserRoleChanged(Long userId, Long operatorId, JsonNode oldRoles, JsonNode newRoles) {
        try {
            // 1. 清理权限缓存
            cacheService.clearUserPermissionCache(userId);
            
            // 2. 更新用户权限
            userService.updateUserPermissions(userId);
            
            // 3. 检查权限升级
            if (userService.hasPermissionUpgrade(oldRoles, newRoles)) {
                notificationService.sendPermissionUpgradeAlert(userId, operatorId);
            }
            
            // 4. 发送角色变更通知
            notificationService.sendUserRoleChangedNotification(userId, operatorId);
            
            // 5. 记录角色变更历史
            userService.recordRoleChangeHistory(userId, operatorId, oldRoles, newRoles);
            
            log.debug("用户角色变更后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户角色变更后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户密码变更后的业务逻辑
     */
    private void processUserPasswordChanged(Long userId, String changeType, String changeIp) {
        try {
            // 1. 强制登出所有会话（除当前会话）
            userService.forceLogoutOtherSessions(userId);
            
            // 2. 记录密码变更历史
            userService.recordPasswordChangeHistory(userId, changeType, changeIp);
            
            // 3. 发送密码变更通知
            notificationService.sendPasswordChangedNotification(userId, changeType);
            
            // 4. 检查密码安全性
            if (userService.isWeakPassword(userId)) {
                notificationService.sendWeakPasswordAlert(userId);
            }
            
            // 5. 更新密码策略统计
            userService.updatePasswordPolicyStatistics(userId);
            
            log.debug("用户密码变更后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户密码变更后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理用户删除后的业务逻辑
     */
    private void processUserDeleted(Long userId, Long deleterId, String deleteReason) {
        try {
            // 1. 清理用户所有会话
            userService.cleanupAllUserSessions(userId);
            
            // 2. 清理用户缓存
            cacheService.clearAllUserCache(userId);
            
            // 3. 处理用户数据归档
            userService.archiveUserData(userId);
            
            // 4. 清理用户权限
            userService.cleanupUserPermissions(userId);
            
            // 5. 发送删除通知
            notificationService.sendUserDeletedNotification(userId, deleterId, deleteReason);
            
            // 6. 更新删除统计
            userService.updateDeleteStatistics(deleteReason);
            
            log.debug("用户删除后处理完成，用户ID: {}", userId);
            
        } catch (Exception e) {
            log.error("处理用户删除后业务逻辑失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * 处理消息处理错误
     */
    private void handleMessageProcessingError(Message message, Exception e, String messageType) {
        try {
            // 记录错误日志
            log.error("消息处理失败，消息类型: {}, 错误信息: {}", messageType, e.getMessage());
            
            // 可以将失败消息发送到死信队列或错误处理队列
            // deadLetterService.sendToDeadLetter(message, e, messageType);
            
            // 发送错误告警
            notificationService.sendMessageProcessingErrorAlert(messageType, e.getMessage());
            
        } catch (Exception ex) {
            log.error("处理消息错误时发生异常", ex);
        }
    }
}