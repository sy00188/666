package com.archive.management.mq.listener;

import com.archive.management.service.SystemConfigService;
import com.archive.management.service.AuditLogService;
import com.archive.management.service.NotificationService;
import com.archive.management.service.CacheService;
import com.archive.management.service.BackupService;
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
 * 系统消息监听器
 * 负责处理系统相关的消息队列消息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageListener {

    private final SystemConfigService systemConfigService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final CacheService cacheService;
    private final BackupService backupService;
    private final ObjectMapper objectMapper;

    /**
     * 监听系统配置变更消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.config.changed:system.config.changed.queue}")
    @Transactional
    public void handleSystemConfigChanged(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long configId = messageData.get("configId").asLong();
            Long operatorId = messageData.get("operatorId").asLong();
            String configKey = messageData.get("configKey").asText();
            String oldValue = messageData.has("oldValue") ? messageData.get("oldValue").asText() : null;
            String newValue = messageData.get("newValue").asText();
            
            log.info("处理系统配置变更消息，配置ID: {}, 配置键: {}, 操作者ID: {}", configId, configKey, operatorId);
            
            // 处理系统配置变更后的业务逻辑
            processSystemConfigChanged(configId, operatorId, configKey, oldValue, newValue);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(operatorId, "SYSTEM_CONFIG_CHANGED", configId, 
                String.format("系统配置变更消息处理完成，配置键: %s，旧值: %s，新值: %s", 
                    configKey, oldValue, newValue), null);
            
            log.info("系统配置变更消息处理完成，配置ID: {}", configId);
            
        } catch (Exception e) {
            log.error("处理系统配置变更消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_CONFIG_CHANGED");
        }
    }

    /**
     * 监听系统启动消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.startup:system.startup.queue}")
    @Transactional
    public void handleSystemStartup(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String instanceId = messageData.get("instanceId").asText();
            String version = messageData.has("version") ? messageData.get("version").asText() : "unknown";
            LocalDateTime startupTime = messageData.has("startupTime") ? 
                LocalDateTime.parse(messageData.get("startupTime").asText()) : LocalDateTime.now();
            
            log.info("处理系统启动消息，实例ID: {}, 版本: {}", instanceId, version);
            
            // 处理系统启动后的业务逻辑
            processSystemStartup(instanceId, version, startupTime);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(null, "SYSTEM_STARTUP", null, 
                String.format("系统启动消息处理完成，实例ID: %s，版本: %s", instanceId, version), null);
            
            log.info("系统启动消息处理完成，实例ID: {}", instanceId);
            
        } catch (Exception e) {
            log.error("处理系统启动消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_STARTUP");
        }
    }

    /**
     * 监听系统关闭消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.shutdown:system.shutdown.queue}")
    @Transactional
    public void handleSystemShutdown(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String instanceId = messageData.get("instanceId").asText();
            String shutdownReason = messageData.has("shutdownReason") ? 
                messageData.get("shutdownReason").asText() : "NORMAL";
            LocalDateTime shutdownTime = messageData.has("shutdownTime") ? 
                LocalDateTime.parse(messageData.get("shutdownTime").asText()) : LocalDateTime.now();
            
            log.info("处理系统关闭消息，实例ID: {}, 关闭原因: {}", instanceId, shutdownReason);
            
            // 处理系统关闭后的业务逻辑
            processSystemShutdown(instanceId, shutdownReason, shutdownTime);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(null, "SYSTEM_SHUTDOWN", null, 
                String.format("系统关闭消息处理完成，实例ID: %s，关闭原因: %s", instanceId, shutdownReason), null);
            
            log.info("系统关闭消息处理完成，实例ID: {}", instanceId);
            
        } catch (Exception e) {
            log.error("处理系统关闭消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_SHUTDOWN");
        }
    }

    /**
     * 监听系统备份消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.backup:system.backup.queue}")
    @Transactional
    public void handleSystemBackup(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String backupType = messageData.get("backupType").asText();
            Long operatorId = messageData.has("operatorId") ? messageData.get("operatorId").asLong() : null;
            String backupPath = messageData.has("backupPath") ? messageData.get("backupPath").asText() : null;
            boolean isScheduled = messageData.has("isScheduled") ? messageData.get("isScheduled").asBoolean() : false;
            
            log.info("处理系统备份消息，备份类型: {}, 操作者ID: {}, 是否定时: {}", backupType, operatorId, isScheduled);
            
            // 处理系统备份后的业务逻辑
            processSystemBackup(backupType, operatorId, backupPath, isScheduled);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(operatorId, "SYSTEM_BACKUP", null, 
                String.format("系统备份消息处理完成，备份类型: %s，备份路径: %s", backupType, backupPath), null);
            
            log.info("系统备份消息处理完成，备份类型: {}", backupType);
            
        } catch (Exception e) {
            log.error("处理系统备份消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_BACKUP");
        }
    }

    /**
     * 监听系统恢复消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.restore:system.restore.queue}")
    @Transactional
    public void handleSystemRestore(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String restoreType = messageData.get("restoreType").asText();
            Long operatorId = messageData.get("operatorId").asLong();
            String backupPath = messageData.get("backupPath").asText();
            LocalDateTime backupTime = messageData.has("backupTime") ? 
                LocalDateTime.parse(messageData.get("backupTime").asText()) : null;
            
            log.info("处理系统恢复消息，恢复类型: {}, 操作者ID: {}, 备份路径: {}", restoreType, operatorId, backupPath);
            
            // 处理系统恢复后的业务逻辑
            processSystemRestore(restoreType, operatorId, backupPath, backupTime);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(operatorId, "SYSTEM_RESTORE", null, 
                String.format("系统恢复消息处理完成，恢复类型: %s，备份路径: %s", restoreType, backupPath), null);
            
            log.info("系统恢复消息处理完成，恢复类型: {}", restoreType);
            
        } catch (Exception e) {
            log.error("处理系统恢复消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_RESTORE");
        }
    }

    /**
     * 监听系统监控告警消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.alert:system.alert.queue}")
    @Transactional
    public void handleSystemAlert(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String alertType = messageData.get("alertType").asText();
            String alertLevel = messageData.get("alertLevel").asText();
            String alertMessage = messageData.get("alertMessage").asText();
            String source = messageData.has("source") ? messageData.get("source").asText() : "SYSTEM";
            JsonNode alertData = messageData.has("alertData") ? messageData.get("alertData") : null;
            
            log.info("处理系统监控告警消息，告警类型: {}, 告警级别: {}, 来源: {}", alertType, alertLevel, source);
            
            // 处理系统监控告警后的业务逻辑
            processSystemAlert(alertType, alertLevel, alertMessage, source, alertData);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(null, "SYSTEM_ALERT", null, 
                String.format("系统监控告警消息处理完成，告警类型: %s，告警级别: %s，告警信息: %s", 
                    alertType, alertLevel, alertMessage), null);
            
            log.info("系统监控告警消息处理完成，告警类型: {}", alertType);
            
        } catch (Exception e) {
            log.error("处理系统监控告警消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_ALERT");
        }
    }

    /**
     * 监听系统维护消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.maintenance:system.maintenance.queue}")
    @Transactional
    public void handleSystemMaintenance(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String maintenanceType = messageData.get("maintenanceType").asText();
            Long operatorId = messageData.get("operatorId").asLong();
            String maintenanceAction = messageData.get("maintenanceAction").asText();
            LocalDateTime scheduledTime = messageData.has("scheduledTime") ? 
                LocalDateTime.parse(messageData.get("scheduledTime").asText()) : null;
            
            log.info("处理系统维护消息，维护类型: {}, 操作者ID: {}, 维护动作: {}", 
                maintenanceType, operatorId, maintenanceAction);
            
            // 处理系统维护后的业务逻辑
            processSystemMaintenance(maintenanceType, operatorId, maintenanceAction, scheduledTime);
            
            // 记录审计日志
            auditLogService.recordSystemOperation(operatorId, "SYSTEM_MAINTENANCE", null, 
                String.format("系统维护消息处理完成，维护类型: %s，维护动作: %s", maintenanceType, maintenanceAction), null);
            
            log.info("系统维护消息处理完成，维护类型: {}", maintenanceType);
            
        } catch (Exception e) {
            log.error("处理系统维护消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_MAINTENANCE");
        }
    }

    /**
     * 监听系统性能监控消息
     */
    @RabbitListener(queues = "${app.mq.queue.system.performance:system.performance.queue}")
    @Transactional
    public void handleSystemPerformance(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            String metricType = messageData.get("metricType").asText();
            Double metricValue = messageData.get("metricValue").asDouble();
            String instanceId = messageData.has("instanceId") ? messageData.get("instanceId").asText() : null;
            LocalDateTime timestamp = messageData.has("timestamp") ? 
                LocalDateTime.parse(messageData.get("timestamp").asText()) : LocalDateTime.now();
            
            log.info("处理系统性能监控消息，指标类型: {}, 指标值: {}, 实例ID: {}", 
                metricType, metricValue, instanceId);
            
            // 处理系统性能监控后的业务逻辑
            processSystemPerformance(metricType, metricValue, instanceId, timestamp);
            
            log.debug("系统性能监控消息处理完成，指标类型: {}", metricType);
            
        } catch (Exception e) {
            log.error("处理系统性能监控消息失败", e);
            handleMessageProcessingError(message, e, "SYSTEM_PERFORMANCE");
        }
    }

    /**
     * 处理系统配置变更后的业务逻辑
     */
    private void processSystemConfigChanged(Long configId, Long operatorId, String configKey, String oldValue, String newValue) {
        try {
            // 1. 清理相关缓存
            cacheService.clearSystemConfigCache(configKey);
            
            // 2. 通知相关服务配置变更
            systemConfigService.notifyConfigChange(configKey, oldValue, newValue);
            
            // 3. 检查是否需要重启服务
            if (systemConfigService.requiresServiceRestart(configKey)) {
                notificationService.sendServiceRestartAlert(configKey, operatorId);
            }
            
            // 4. 备份配置变更
            systemConfigService.backupConfigChange(configId, operatorId, oldValue, newValue);
            
            // 5. 发送配置变更通知
            notificationService.sendConfigChangedNotification(configKey, operatorId, oldValue, newValue);
            
            log.debug("系统配置变更后处理完成，配置键: {}", configKey);
            
        } catch (Exception e) {
            log.error("处理系统配置变更后业务逻辑失败，配置键: {}", configKey, e);
            throw e;
        }
    }

    /**
     * 处理系统启动后的业务逻辑
     */
    private void processSystemStartup(String instanceId, String version, LocalDateTime startupTime) {
        try {
            // 1. 注册实例信息
            systemConfigService.registerInstance(instanceId, version, startupTime);
            
            // 2. 初始化系统配置
            systemConfigService.initializeSystemConfigs();
            
            // 3. 启动定时任务
            systemConfigService.startScheduledTasks();
            
            // 4. 检查系统健康状态
            systemConfigService.performHealthCheck();
            
            // 5. 发送启动通知
            notificationService.sendSystemStartupNotification(instanceId, version);
            
            // 6. 清理过期数据
            systemConfigService.cleanupExpiredData();
            
            log.debug("系统启动后处理完成，实例ID: {}", instanceId);
            
        } catch (Exception e) {
            log.error("处理系统启动后业务逻辑失败，实例ID: {}", instanceId, e);
            throw e;
        }
    }

    /**
     * 处理系统关闭后的业务逻辑
     */
    private void processSystemShutdown(String instanceId, String shutdownReason, LocalDateTime shutdownTime) {
        try {
            // 1. 注销实例信息
            systemConfigService.unregisterInstance(instanceId, shutdownTime);
            
            // 2. 停止定时任务
            systemConfigService.stopScheduledTasks();
            
            // 3. 清理临时数据
            cacheService.clearAllTemporaryData();
            
            // 4. 保存运行状态
            systemConfigService.saveRuntimeState(instanceId, shutdownReason);
            
            // 5. 发送关闭通知
            notificationService.sendSystemShutdownNotification(instanceId, shutdownReason);
            
            log.debug("系统关闭后处理完成，实例ID: {}", instanceId);
            
        } catch (Exception e) {
            log.error("处理系统关闭后业务逻辑失败，实例ID: {}", instanceId, e);
            throw e;
        }
    }

    /**
     * 处理系统备份后的业务逻辑
     */
    private void processSystemBackup(String backupType, Long operatorId, String backupPath, boolean isScheduled) {
        try {
            // 1. 执行备份操作
            String backupId = backupService.performBackup(backupType, backupPath, operatorId);
            
            // 2. 验证备份完整性
            boolean isValid = backupService.validateBackup(backupId);
            
            // 3. 更新备份记录
            backupService.updateBackupRecord(backupId, isValid);
            
            // 4. 清理过期备份
            if (isScheduled) {
                backupService.cleanupExpiredBackups(backupType);
            }
            
            // 5. 发送备份结果通知
            notificationService.sendBackupResultNotification(backupType, operatorId, isValid, backupPath);
            
            // 6. 更新备份统计
            backupService.updateBackupStatistics(backupType, isValid);
            
            log.debug("系统备份后处理完成，备份类型: {}", backupType);
            
        } catch (Exception e) {
            log.error("处理系统备份后业务逻辑失败，备份类型: {}", backupType, e);
            throw e;
        }
    }

    /**
     * 处理系统恢复后的业务逻辑
     */
    private void processSystemRestore(String restoreType, Long operatorId, String backupPath, LocalDateTime backupTime) {
        try {
            // 1. 执行恢复操作
            String restoreId = backupService.performRestore(restoreType, backupPath, operatorId);
            
            // 2. 验证恢复结果
            boolean isSuccessful = backupService.validateRestore(restoreId);
            
            // 3. 更新恢复记录
            backupService.updateRestoreRecord(restoreId, isSuccessful);
            
            // 4. 重新初始化系统
            if (isSuccessful) {
                systemConfigService.reinitializeSystem();
            }
            
            // 5. 发送恢复结果通知
            notificationService.sendRestoreResultNotification(restoreType, operatorId, isSuccessful, backupPath);
            
            // 6. 更新恢复统计
            backupService.updateRestoreStatistics(restoreType, isSuccessful);
            
            log.debug("系统恢复后处理完成，恢复类型: {}", restoreType);
            
        } catch (Exception e) {
            log.error("处理系统恢复后业务逻辑失败，恢复类型: {}", restoreType, e);
            throw e;
        }
    }

    /**
     * 处理系统监控告警后的业务逻辑
     */
    private void processSystemAlert(String alertType, String alertLevel, String alertMessage, String source, JsonNode alertData) {
        try {
            // 1. 记录告警信息
            systemConfigService.recordAlert(alertType, alertLevel, alertMessage, source, alertData);
            
            // 2. 根据告警级别执行相应操作
            switch (alertLevel) {
                case "CRITICAL":
                    systemConfigService.handleCriticalAlert(alertType, alertMessage, alertData);
                    break;
                case "HIGH":
                    systemConfigService.handleHighAlert(alertType, alertMessage, alertData);
                    break;
                case "MEDIUM":
                    systemConfigService.handleMediumAlert(alertType, alertMessage, alertData);
                    break;
                case "LOW":
                    systemConfigService.handleLowAlert(alertType, alertMessage, alertData);
                    break;
            }
            
            // 3. 发送告警通知
            notificationService.sendAlertNotification(alertType, alertLevel, alertMessage, source);
            
            // 4. 更新告警统计
            systemConfigService.updateAlertStatistics(alertType, alertLevel);
            
            // 5. 检查是否需要自动处理
            if (systemConfigService.shouldAutoHandle(alertType)) {
                systemConfigService.autoHandleAlert(alertType, alertData);
            }
            
            log.debug("系统监控告警后处理完成，告警类型: {}", alertType);
            
        } catch (Exception e) {
            log.error("处理系统监控告警后业务逻辑失败，告警类型: {}", alertType, e);
            throw e;
        }
    }

    /**
     * 处理系统维护后的业务逻辑
     */
    private void processSystemMaintenance(String maintenanceType, Long operatorId, String maintenanceAction, LocalDateTime scheduledTime) {
        try {
            // 1. 执行维护操作
            String maintenanceId = systemConfigService.performMaintenance(maintenanceType, maintenanceAction, operatorId);
            
            // 2. 记录维护历史
            systemConfigService.recordMaintenanceHistory(maintenanceId, maintenanceType, maintenanceAction, operatorId);
            
            // 3. 检查维护结果
            boolean isSuccessful = systemConfigService.validateMaintenanceResult(maintenanceId);
            
            // 4. 发送维护结果通知
            notificationService.sendMaintenanceResultNotification(maintenanceType, operatorId, isSuccessful);
            
            // 5. 更新维护统计
            systemConfigService.updateMaintenanceStatistics(maintenanceType, isSuccessful);
            
            // 6. 如果是定时维护，安排下次维护
            if (scheduledTime != null) {
                systemConfigService.scheduleNextMaintenance(maintenanceType, scheduledTime);
            }
            
            log.debug("系统维护后处理完成，维护类型: {}", maintenanceType);
            
        } catch (Exception e) {
            log.error("处理系统维护后业务逻辑失败，维护类型: {}", maintenanceType, e);
            throw e;
        }
    }

    /**
     * 处理系统性能监控后的业务逻辑
     */
    private void processSystemPerformance(String metricType, Double metricValue, String instanceId, LocalDateTime timestamp) {
        try {
            // 1. 存储性能指标
            systemConfigService.storePerformanceMetric(metricType, metricValue, instanceId, timestamp);
            
            // 2. 检查性能阈值
            if (systemConfigService.exceedsThreshold(metricType, metricValue)) {
                notificationService.sendPerformanceThresholdAlert(metricType, metricValue, instanceId);
            }
            
            // 3. 更新性能统计
            systemConfigService.updatePerformanceStatistics(metricType, metricValue, instanceId);
            
            // 4. 检查是否需要自动优化
            if (systemConfigService.shouldAutoOptimize(metricType, metricValue)) {
                systemConfigService.performAutoOptimization(metricType, instanceId);
            }
            
            log.debug("系统性能监控后处理完成，指标类型: {}", metricType);
            
        } catch (Exception e) {
            log.error("处理系统性能监控后业务逻辑失败，指标类型: {}", metricType, e);
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