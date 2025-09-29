package com.archive.management.mq.producer;

import com.archive.management.dto.AuditLogDTO;
import com.archive.management.dto.SystemConfigDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 系统消息生产者
 * 负责发送系统相关的消息队列消息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mq.exchange.system:system.exchange}")
    private String systemExchange;

    @Value("${app.mq.routing-key.system.config.changed:system.config.changed}")
    private String configChangedRoutingKey;

    @Value("${app.mq.routing-key.system.audit.log:system.audit.log}")
    private String auditLogRoutingKey;

    @Value("${app.mq.routing-key.system.backup:system.backup}")
    private String systemBackupRoutingKey;

    @Value("${app.mq.routing-key.system.maintenance:system.maintenance}")
    private String systemMaintenanceRoutingKey;

    @Value("${app.mq.routing-key.system.alert:system.alert}")
    private String systemAlertRoutingKey;

    @Value("${app.mq.routing-key.system.performance:system.performance}")
    private String systemPerformanceRoutingKey;

    @Value("${app.mq.routing-key.system.security:system.security}")
    private String systemSecurityRoutingKey;

    @Value("${app.mq.routing-key.system.cleanup:system.cleanup}")
    private String systemCleanupRoutingKey;

    @Value("${app.mq.routing-key.system.notification:system.notification}")
    private String systemNotificationRoutingKey;

    /**
     * 发送系统配置变更消息
     */
    public void sendConfigChangedMessage(SystemConfigDTO config, SystemConfigDTO oldConfig, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "CONFIG_CHANGED");
            messageData.put("config", config);
            messageData.put("oldConfig", oldConfig);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, configChangedRoutingKey, messageData, "系统配置变更");
            log.info("发送系统配置变更消息成功，配置键: {}, 操作人ID: {}", config.getConfigKey(), operatorId);
        } catch (Exception e) {
            log.error("发送系统配置变更消息失败，配置键: {}", config.getConfigKey(), e);
        }
    }

    /**
     * 发送审计日志消息
     */
    public void sendAuditLogMessage(AuditLogDTO auditLog) {
        try {
            Map<String, Object> messageData = createMessageData("AUDIT_LOG", auditLog);
            sendMessage(systemExchange, auditLogRoutingKey, messageData, "审计日志");
            log.debug("发送审计日志消息成功，操作类型: {}, 操作人ID: {}", auditLog.getOperationType(), auditLog.getOperatorId());
        } catch (Exception e) {
            log.error("发送审计日志消息失败，操作类型: {}", auditLog.getOperationType(), e);
        }
    }

    /**
     * 发送系统备份消息
     */
    public void sendSystemBackupMessage(String backupType, String backupPath, String backupStatus, Map<String, Object> backupInfo) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_BACKUP");
            messageData.put("backupType", backupType);
            messageData.put("backupPath", backupPath);
            messageData.put("backupStatus", backupStatus);
            messageData.put("backupInfo", backupInfo);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemBackupRoutingKey, messageData, "系统备份");
            log.info("发送系统备份消息成功，备份类型: {}, 备份状态: {}", backupType, backupStatus);
        } catch (Exception e) {
            log.error("发送系统备份消息失败，备份类型: {}", backupType, e);
        }
    }

    /**
     * 发送系统维护消息
     */
    public void sendSystemMaintenanceMessage(String maintenanceType, String maintenanceStatus, 
                                           LocalDateTime startTime, LocalDateTime endTime, String description) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_MAINTENANCE");
            messageData.put("maintenanceType", maintenanceType);
            messageData.put("maintenanceStatus", maintenanceStatus);
            messageData.put("startTime", startTime);
            messageData.put("endTime", endTime);
            messageData.put("description", description);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemMaintenanceRoutingKey, messageData, "系统维护");
            log.info("发送系统维护消息成功，维护类型: {}, 维护状态: {}", maintenanceType, maintenanceStatus);
        } catch (Exception e) {
            log.error("发送系统维护消息失败，维护类型: {}", maintenanceType, e);
        }
    }

    /**
     * 发送系统告警消息
     */
    public void sendSystemAlertMessage(String alertType, String alertLevel, String alertTitle, 
                                     String alertContent, Map<String, Object> alertData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_ALERT");
            messageData.put("alertType", alertType);
            messageData.put("alertLevel", alertLevel);
            messageData.put("alertTitle", alertTitle);
            messageData.put("alertContent", alertContent);
            messageData.put("alertData", alertData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemAlertRoutingKey, messageData, "系统告警");
            log.warn("发送系统告警消息成功，告警类型: {}, 告警级别: {}, 告警标题: {}", alertType, alertLevel, alertTitle);
        } catch (Exception e) {
            log.error("发送系统告警消息失败，告警类型: {}", alertType, e);
        }
    }

    /**
     * 发送系统性能监控消息
     */
    public void sendSystemPerformanceMessage(String metricType, Map<String, Object> performanceData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_PERFORMANCE");
            messageData.put("metricType", metricType);
            messageData.put("performanceData", performanceData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemPerformanceRoutingKey, messageData, "系统性能监控");
            log.debug("发送系统性能监控消息成功，指标类型: {}", metricType);
        } catch (Exception e) {
            log.error("发送系统性能监控消息失败，指标类型: {}", metricType, e);
        }
    }

    /**
     * 发送系统安全事件消息
     */
    public void sendSystemSecurityMessage(String securityEventType, String riskLevel, String eventDescription, 
                                        String sourceIp, Map<String, Object> securityData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_SECURITY");
            messageData.put("securityEventType", securityEventType);
            messageData.put("riskLevel", riskLevel);
            messageData.put("eventDescription", eventDescription);
            messageData.put("sourceIp", sourceIp);
            messageData.put("securityData", securityData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemSecurityRoutingKey, messageData, "系统安全事件");
            log.warn("发送系统安全事件消息成功，事件类型: {}, 风险等级: {}", securityEventType, riskLevel);
        } catch (Exception e) {
            log.error("发送系统安全事件消息失败，事件类型: {}", securityEventType, e);
        }
    }

    /**
     * 发送系统清理消息
     */
    public void sendSystemCleanupMessage(String cleanupType, String cleanupStatus, Map<String, Object> cleanupResult) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_CLEANUP");
            messageData.put("cleanupType", cleanupType);
            messageData.put("cleanupStatus", cleanupStatus);
            messageData.put("cleanupResult", cleanupResult);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemCleanupRoutingKey, messageData, "系统清理");
            log.info("发送系统清理消息成功，清理类型: {}, 清理状态: {}", cleanupType, cleanupStatus);
        } catch (Exception e) {
            log.error("发送系统清理消息失败，清理类型: {}", cleanupType, e);
        }
    }

    /**
     * 发送系统通知消息
     */
    public void sendSystemNotificationMessage(String notificationType, String title, String content, 
                                            String targetType, Object targetData, String priority) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_NOTIFICATION");
            messageData.put("notificationType", notificationType);
            messageData.put("title", title);
            messageData.put("content", content);
            messageData.put("targetType", targetType); // ALL_USERS, ADMIN_USERS, SPECIFIC_USERS
            messageData.put("targetData", targetData);
            messageData.put("priority", priority);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, systemNotificationRoutingKey, messageData, "系统通知");
            log.info("发送系统通知消息成功，通知类型: {}, 目标类型: {}, 优先级: {}", notificationType, targetType, priority);
        } catch (Exception e) {
            log.error("发送系统通知消息失败，通知类型: {}", notificationType, e);
        }
    }

    /**
     * 发送系统启动消息
     */
    public void sendSystemStartupMessage(String applicationName, String version, Map<String, Object> startupInfo) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_STARTUP");
            messageData.put("applicationName", applicationName);
            messageData.put("version", version);
            messageData.put("startupInfo", startupInfo);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, "system.startup", messageData, "系统启动");
            log.info("发送系统启动消息成功，应用名称: {}, 版本: {}", applicationName, version);
        } catch (Exception e) {
            log.error("发送系统启动消息失败，应用名称: {}", applicationName, e);
        }
    }

    /**
     * 发送系统关闭消息
     */
    public void sendSystemShutdownMessage(String applicationName, String shutdownReason, Map<String, Object> shutdownInfo) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_SHUTDOWN");
            messageData.put("applicationName", applicationName);
            messageData.put("shutdownReason", shutdownReason);
            messageData.put("shutdownInfo", shutdownInfo);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, "system.shutdown", messageData, "系统关闭");
            log.info("发送系统关闭消息成功，应用名称: {}, 关闭原因: {}", applicationName, shutdownReason);
        } catch (Exception e) {
            log.error("发送系统关闭消息失败，应用名称: {}", applicationName, e);
        }
    }

    /**
     * 发送系统健康检查消息
     */
    public void sendSystemHealthCheckMessage(String healthStatus, Map<String, Object> healthData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "SYSTEM_HEALTH_CHECK");
            messageData.put("healthStatus", healthStatus);
            messageData.put("healthData", healthData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(systemExchange, "system.health.check", messageData, "系统健康检查");
            log.debug("发送系统健康检查消息成功，健康状态: {}", healthStatus);
        } catch (Exception e) {
            log.error("发送系统健康检查消息失败，健康状态: {}", healthStatus, e);
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