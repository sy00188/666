package com.archive.management.mq.producer;

import com.archive.management.dto.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户消息生产者
 * 负责发送用户相关的消息队列消息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
public class UserMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(UserMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mq.exchange.user:user.exchange}")
    private String userExchange;

    @Value("${app.mq.routing-key.user.created:user.created}")
    private String userCreatedRoutingKey;

    @Value("${app.mq.routing-key.user.updated:user.updated}")
    private String userUpdatedRoutingKey;

    @Value("${app.mq.routing-key.user.deleted:user.deleted}")
    private String userDeletedRoutingKey;

    @Value("${app.mq.routing-key.user.login:user.login}")
    private String userLoginRoutingKey;

    @Value("${app.mq.routing-key.user.logout:user.logout}")
    private String userLogoutRoutingKey;

    @Value("${app.mq.routing-key.user.password.changed:user.password.changed}")
    private String passwordChangedRoutingKey;

    @Value("${app.mq.routing-key.user.status.changed:user.status.changed}")
    private String statusChangedRoutingKey;

    @Value("${app.mq.routing-key.user.role.assigned:user.role.assigned}")
    private String roleAssignedRoutingKey;

    @Value("${app.mq.routing-key.user.role.removed:user.role.removed}")
    private String roleRemovedRoutingKey;

    @Value("${app.mq.routing-key.user.notification:user.notification}")
    private String userNotificationRoutingKey;

    /**
     * 发送用户创建消息
     */
    public void sendUserCreatedMessage(UserDTO user) {
        try {
            Map<String, Object> messageData = createMessageData("USER_CREATED", user);
            sendMessage(userExchange, userCreatedRoutingKey, messageData, "用户创建");
            log.info("发送用户创建消息成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("发送用户创建消息失败，用户ID: {}", user.getId(), e);
        }
    }

    /**
     * 发送用户更新消息
     */
    public void sendUserUpdatedMessage(UserDTO user, UserDTO oldUser) {
        try {
            Map<String, Object> messageData = createMessageData("USER_UPDATED", user);
            messageData.put("oldData", oldUser);
            sendMessage(userExchange, userUpdatedRoutingKey, messageData, "用户更新");
            log.info("发送用户更新消息成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        } catch (Exception e) {
            log.error("发送用户更新消息失败，用户ID: {}", user.getId(), e);
        }
    }

    /**
     * 发送用户删除消息
     */
    public void sendUserDeletedMessage(Long userId, String username, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_DELETED");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, userDeletedRoutingKey, messageData, "用户删除");
            log.info("发送用户删除消息成功，用户ID: {}, 用户名: {}", userId, username);
        } catch (Exception e) {
            log.error("发送用户删除消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送用户登录消息
     */
    public void sendUserLoginMessage(Long userId, String username, String loginIp, String userAgent) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_LOGIN");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("loginIp", loginIp);
            messageData.put("userAgent", userAgent);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, userLoginRoutingKey, messageData, "用户登录");
            log.info("发送用户登录消息成功，用户ID: {}, 用户名: {}, IP: {}", userId, username, loginIp);
        } catch (Exception e) {
            log.error("发送用户登录消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送用户登出消息
     */
    public void sendUserLogoutMessage(Long userId, String username, String logoutReason) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_LOGOUT");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("logoutReason", logoutReason);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, userLogoutRoutingKey, messageData, "用户登出");
            log.info("发送用户登出消息成功，用户ID: {}, 用户名: {}, 登出原因: {}", userId, username, logoutReason);
        } catch (Exception e) {
            log.error("发送用户登出消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送密码修改消息
     */
    public void sendPasswordChangedMessage(Long userId, String username, String changeType, String changeIp) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "PASSWORD_CHANGED");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("changeType", changeType); // SELF_CHANGE, ADMIN_RESET, FORCE_RESET
            messageData.put("changeIp", changeIp);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, passwordChangedRoutingKey, messageData, "密码修改");
            log.info("发送密码修改消息成功，用户ID: {}, 用户名: {}, 修改类型: {}", userId, username, changeType);
        } catch (Exception e) {
            log.error("发送密码修改消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送用户状态变更消息
     */
    public void sendUserStatusChangedMessage(Long userId, String username, String oldStatus, String newStatus, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_STATUS_CHANGED");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("oldStatus", oldStatus);
            messageData.put("newStatus", newStatus);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, statusChangedRoutingKey, messageData, "用户状态变更");
            log.info("发送用户状态变更消息成功，用户ID: {}, 用户名: {}, 状态变更: {} -> {}", 
                    userId, username, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("发送用户状态变更消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送角色分配消息
     */
    public void sendRoleAssignedMessage(Long userId, String username, Long roleId, String roleName, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "ROLE_ASSIGNED");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("roleId", roleId);
            messageData.put("roleName", roleName);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, roleAssignedRoutingKey, messageData, "角色分配");
            log.info("发送角色分配消息成功，用户ID: {}, 用户名: {}, 角色: {}", userId, username, roleName);
        } catch (Exception e) {
            log.error("发送角色分配消息失败，用户ID: {}, 角色ID: {}", userId, roleId, e);
        }
    }

    /**
     * 发送角色移除消息
     */
    public void sendRoleRemovedMessage(Long userId, String username, Long roleId, String roleName, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "ROLE_REMOVED");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("roleId", roleId);
            messageData.put("roleName", roleName);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, roleRemovedRoutingKey, messageData, "角色移除");
            log.info("发送角色移除消息成功，用户ID: {}, 用户名: {}, 角色: {}", userId, username, roleName);
        } catch (Exception e) {
            log.error("发送角色移除消息失败，用户ID: {}, 角色ID: {}", userId, roleId, e);
        }
    }

    /**
     * 发送用户通知消息
     */
    public void sendUserNotificationMessage(Long userId, String notificationType, String title, String content, Map<String, Object> extraData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_NOTIFICATION");
            messageData.put("userId", userId);
            messageData.put("notificationType", notificationType);
            messageData.put("title", title);
            messageData.put("content", content);
            messageData.put("extraData", extraData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, userNotificationRoutingKey, messageData, "用户通知");
            log.info("发送用户通知消息成功，用户ID: {}, 通知类型: {}, 标题: {}", userId, notificationType, title);
        } catch (Exception e) {
            log.error("发送用户通知消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 发送批量用户操作消息
     */
    public void sendBatchUserMessage(String batchType, Object batchData, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "BATCH_USER_OPERATION");
            messageData.put("batchType", batchType);
            messageData.put("batchData", batchData);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, "user.batch.operation", messageData, "批量用户操作");
            log.info("发送批量用户操作消息成功，批量类型: {}, 操作人ID: {}", batchType, operatorId);
        } catch (Exception e) {
            log.error("发送批量用户操作消息失败，批量类型: {}", batchType, e);
        }
    }

    /**
     * 发送用户统计消息
     */
    public void sendUserStatisticsMessage(String statisticsType, Map<String, Object> statisticsData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_STATISTICS");
            messageData.put("statisticsType", statisticsType);
            messageData.put("statisticsData", statisticsData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, "user.statistics", messageData, "用户统计");
            log.info("发送用户统计消息成功，统计类型: {}", statisticsType);
        } catch (Exception e) {
            log.error("发送用户统计消息失败，统计类型: {}", statisticsType, e);
        }
    }

    /**
     * 发送用户安全事件消息
     */
    public void sendUserSecurityEventMessage(Long userId, String username, String eventType, String eventDescription, 
                                           String sourceIp, String riskLevel) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "USER_SECURITY_EVENT");
            messageData.put("userId", userId);
            messageData.put("username", username);
            messageData.put("securityEventType", eventType);
            messageData.put("eventDescription", eventDescription);
            messageData.put("sourceIp", sourceIp);
            messageData.put("riskLevel", riskLevel);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(userExchange, "user.security.event", messageData, "用户安全事件");
            log.info("发送用户安全事件消息成功，用户ID: {}, 事件类型: {}, 风险等级: {}", userId, eventType, riskLevel);
        } catch (Exception e) {
            log.error("发送用户安全事件消息失败，用户ID: {}", userId, e);
        }
    }

    /**
     * 创建消息数据
     */
    private Map<String, Object> createMessageData(String eventType, Object data) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("eventType", eventType);
        messageData.put("data", data);
        messageData.put("timestamp", LocalDateTime.now());
        messageData.put("messageId", UUID.randomUUID().toString());
        return messageData;
    }

    /**
     * 发送消息
     */
    private void sendMessage(String exchange, String routingKey, Object messageData, String messageType) {
        try {
            // 创建消息属性
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setContentEncoding("UTF-8");
            properties.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            properties.setPriority(0);
            properties.setMessageId(UUID.randomUUID().toString());
            properties.setTimestamp(new java.util.Date());
            properties.setHeader("messageType", messageType);
            properties.setHeader("source", "archive-management-system");

            // 序列化消息内容
            String messageJson = objectMapper.writeValueAsString(messageData);
            Message message = new Message(messageJson.getBytes(), properties);

            // 发送消息
            rabbitTemplate.send(exchange, routingKey, message);
            log.debug("消息发送成功 - 交换机: {}, 路由键: {}, 消息类型: {}", exchange, routingKey, messageType);

        } catch (JsonProcessingException e) {
            log.error("消息序列化失败 - 消息类型: {}", messageType, e);
            throw new RuntimeException("消息序列化失败", e);
        } catch (Exception e) {
            log.error("消息发送失败 - 交换机: {}, 路由键: {}, 消息类型: {}", exchange, routingKey, messageType, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
}