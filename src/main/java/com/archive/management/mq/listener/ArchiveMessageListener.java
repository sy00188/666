package com.archive.management.mq.listener;

import com.archive.management.dto.ArchiveDTO;
import com.archive.management.service.ArchiveService;
import com.archive.management.service.AuditLogService;
import com.archive.management.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 档案消息监听器
 * 负责处理档案相关的消息队列消息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveMessageListener {

    private final ArchiveService archiveService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * 监听档案创建消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.created:archive.created.queue}")
    @Transactional
    public void handleArchiveCreated(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long creatorId = messageData.get("creatorId").asLong();
            String eventType = messageData.get("eventType").asText();
            
            log.info("处理档案创建消息，档案ID: {}, 创建者ID: {}", archiveId, creatorId);
            
            // 处理档案创建后的业务逻辑
            processArchiveCreated(archiveId, creatorId);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_CREATED", creatorId, 
                "档案创建消息处理完成", null);
            
            log.info("档案创建消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案创建消息失败", e);
            // 可以考虑将失败消息发送到死信队列
            handleMessageProcessingError(message, e, "ARCHIVE_CREATED");
        }
    }

    /**
     * 监听档案更新消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.updated:archive.updated.queue}")
    @Transactional
    public void handleArchiveUpdated(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long updaterId = messageData.get("updaterId").asLong();
            JsonNode oldDataNode = messageData.get("oldData");
            JsonNode newDataNode = messageData.get("newData");
            
            log.info("处理档案更新消息，档案ID: {}, 更新者ID: {}", archiveId, updaterId);
            
            // 处理档案更新后的业务逻辑
            processArchiveUpdated(archiveId, updaterId, oldDataNode, newDataNode);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_UPDATED", updaterId, 
                "档案更新消息处理完成", null);
            
            log.info("档案更新消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案更新消息失败", e);
            handleMessageProcessingError(message, e, "ARCHIVE_UPDATED");
        }
    }

    /**
     * 监听档案删除消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.deleted:archive.deleted.queue}")
    @Transactional
    public void handleArchiveDeleted(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long deleterId = messageData.get("deleterId").asLong();
            String deleteReason = messageData.has("deleteReason") ? messageData.get("deleteReason").asText() : null;
            
            log.info("处理档案删除消息，档案ID: {}, 删除者ID: {}", archiveId, deleterId);
            
            // 处理档案删除后的业务逻辑
            processArchiveDeleted(archiveId, deleterId, deleteReason);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_DELETED", deleterId, 
                "档案删除消息处理完成，删除原因: " + deleteReason, null);
            
            log.info("档案删除消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案删除消息失败", e);
            handleMessageProcessingError(message, e, "ARCHIVE_DELETED");
        }
    }

    /**
     * 监听档案归档消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.archived:archive.archived.queue}")
    @Transactional
    public void handleArchiveArchived(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long operatorId = messageData.get("operatorId").asLong();
            String archiveLocation = messageData.has("archiveLocation") ? messageData.get("archiveLocation").asText() : null;
            
            log.info("处理档案归档消息，档案ID: {}, 操作者ID: {}", archiveId, operatorId);
            
            // 处理档案归档后的业务逻辑
            processArchiveArchived(archiveId, operatorId, archiveLocation);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_ARCHIVED", operatorId, 
                "档案归档消息处理完成，归档位置: " + archiveLocation, null);
            
            log.info("档案归档消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案归档消息失败", e);
            handleMessageProcessingError(message, e, "ARCHIVE_ARCHIVED");
        }
    }

    /**
     * 监听档案发布消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.published:archive.published.queue}")
    @Transactional
    public void handleArchivePublished(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long publisherId = messageData.get("publisherId").asLong();
            String publishScope = messageData.has("publishScope") ? messageData.get("publishScope").asText() : "PUBLIC";
            
            log.info("处理档案发布消息，档案ID: {}, 发布者ID: {}, 发布范围: {}", archiveId, publisherId, publishScope);
            
            // 处理档案发布后的业务逻辑
            processArchivePublished(archiveId, publisherId, publishScope);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_PUBLISHED", publisherId, 
                "档案发布消息处理完成，发布范围: " + publishScope, null);
            
            log.info("档案发布消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案发布消息失败", e);
            handleMessageProcessingError(message, e, "ARCHIVE_PUBLISHED");
        }
    }

    /**
     * 监听档案状态变更消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.status.changed:archive.status.changed.queue}")
    @Transactional
    public void handleArchiveStatusChanged(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long operatorId = messageData.get("operatorId").asLong();
            String oldStatus = messageData.get("oldStatus").asText();
            String newStatus = messageData.get("newStatus").asText();
            String changeReason = messageData.has("changeReason") ? messageData.get("changeReason").asText() : null;
            
            log.info("处理档案状态变更消息，档案ID: {}, 操作者ID: {}, 状态变更: {} -> {}", 
                archiveId, operatorId, oldStatus, newStatus);
            
            // 处理档案状态变更后的业务逻辑
            processArchiveStatusChanged(archiveId, operatorId, oldStatus, newStatus, changeReason);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_STATUS_CHANGED", operatorId, 
                String.format("档案状态变更消息处理完成，状态变更: %s -> %s，变更原因: %s", 
                    oldStatus, newStatus, changeReason), null);
            
            log.info("档案状态变更消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案状态变更消息失败", e);
            handleMessageProcessingError(message, e, "ARCHIVE_STATUS_CHANGED");
        }
    }

    /**
     * 监听档案权限变更消息
     */
    @RabbitListener(queues = "${app.mq.queue.archive.permission.changed:archive.permission.changed.queue}")
    @Transactional
    public void handleArchivePermissionChanged(Message message) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            
            Long archiveId = messageData.get("archiveId").asLong();
            Long operatorId = messageData.get("operatorId").asLong();
            String permissionType = messageData.get("permissionType").asText();
            JsonNode permissionData = messageData.get("permissionData");
            
            log.info("处理档案权限变更消息，档案ID: {}, 操作者ID: {}, 权限类型: {}", 
                archiveId, operatorId, permissionType);
            
            // 处理档案权限变更后的业务逻辑
            processArchivePermissionChanged(archiveId, operatorId, permissionType, permissionData);
            
            // 记录审计日志
            auditLogService.recordArchiveOperation(archiveId, "ARCHIVE_PERMISSION_CHANGED", operatorId, 
                "档案权限变更消息处理完成，权限类型: " + permissionType, null);
            
            log.info("档案权限变更消息处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案权限变更消息失败", e);
            handleMessageProcessingError(message, e, "ARCHIVE_PERMISSION_CHANGED");
        }
    }

    /**
     * 处理档案创建后的业务逻辑
     */
    private void processArchiveCreated(Long archiveId, Long creatorId) {
        try {
            // 1. 初始化档案索引
            archiveService.initializeArchiveIndex(archiveId);
            
            // 2. 设置默认权限
            archiveService.setupDefaultPermissions(archiveId, creatorId);
            
            // 3. 发送创建通知
            notificationService.sendArchiveCreatedNotification(archiveId, creatorId);
            
            // 4. 触发相关工作流
            archiveService.triggerArchiveWorkflow(archiveId, "CREATED");
            
            log.debug("档案创建后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案创建后业务逻辑失败，档案ID: {}", archiveId, e);
            throw e;
        }
    }

    /**
     * 处理档案更新后的业务逻辑
     */
    private void processArchiveUpdated(Long archiveId, Long updaterId, JsonNode oldData, JsonNode newData) {
        try {
            // 1. 更新档案索引
            archiveService.updateArchiveIndex(archiveId);
            
            // 2. 检查是否需要重新审核
            if (archiveService.needsReapproval(oldData, newData)) {
                archiveService.triggerReapproval(archiveId, updaterId);
            }
            
            // 3. 发送更新通知
            notificationService.sendArchiveUpdatedNotification(archiveId, updaterId);
            
            // 4. 更新相关统计
            archiveService.updateArchiveStatistics(archiveId);
            
            log.debug("档案更新后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案更新后业务逻辑失败，档案ID: {}", archiveId, e);
            throw e;
        }
    }

    /**
     * 处理档案删除后的业务逻辑
     */
    private void processArchiveDeleted(Long archiveId, Long deleterId, String deleteReason) {
        try {
            // 1. 清理档案索引
            archiveService.cleanupArchiveIndex(archiveId);
            
            // 2. 清理相关文件
            archiveService.cleanupArchiveFiles(archiveId);
            
            // 3. 发送删除通知
            notificationService.sendArchiveDeletedNotification(archiveId, deleterId, deleteReason);
            
            // 4. 更新统计信息
            archiveService.updateDeleteStatistics(archiveId);
            
            log.debug("档案删除后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案删除后业务逻辑失败，档案ID: {}", archiveId, e);
            throw e;
        }
    }

    /**
     * 处理档案归档后的业务逻辑
     */
    private void processArchiveArchived(Long archiveId, Long operatorId, String archiveLocation) {
        try {
            // 1. 更新档案状态
            archiveService.updateArchiveStatus(archiveId, "ARCHIVED");
            
            // 2. 移动文件到归档存储
            archiveService.moveToArchiveStorage(archiveId, archiveLocation);
            
            // 3. 发送归档通知
            notificationService.sendArchiveArchivedNotification(archiveId, operatorId);
            
            // 4. 更新归档统计
            archiveService.updateArchiveStatistics(archiveId);
            
            log.debug("档案归档后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案归档后业务逻辑失败，档案ID: {}", archiveId, e);
            throw e;
        }
    }

    /**
     * 处理档案发布后的业务逻辑
     */
    private void processArchivePublished(Long archiveId, Long publisherId, String publishScope) {
        try {
            // 1. 更新档案状态
            archiveService.updateArchiveStatus(archiveId, "PUBLISHED");
            
            // 2. 设置发布权限
            archiveService.setupPublishPermissions(archiveId, publishScope);
            
            // 3. 发送发布通知
            notificationService.sendArchivePublishedNotification(archiveId, publisherId, publishScope);
            
            // 4. 更新发布统计
            archiveService.updatePublishStatistics(archiveId);
            
            log.debug("档案发布后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案发布后业务逻辑失败，档案ID: {}", archiveId, e);
            throw e;
        }
    }

    /**
     * 处理档案状态变更后的业务逻辑
     */
    private void processArchiveStatusChanged(Long archiveId, Long operatorId, String oldStatus, String newStatus, String changeReason) {
        try {
            // 1. 触发状态变更工作流
            archiveService.triggerStatusChangeWorkflow(archiveId, oldStatus, newStatus);
            
            // 2. 更新相关权限
            archiveService.updatePermissionsForStatusChange(archiveId, newStatus);
            
            // 3. 发送状态变更通知
            notificationService.sendArchiveStatusChangedNotification(archiveId, operatorId, oldStatus, newStatus);
            
            // 4. 更新统计信息
            archiveService.updateStatusStatistics(archiveId, oldStatus, newStatus);
            
            log.debug("档案状态变更后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案状态变更后业务逻辑失败，档案ID: {}", archiveId, e);
            throw e;
        }
    }

    /**
     * 处理档案权限变更后的业务逻辑
     */
    private void processArchivePermissionChanged(Long archiveId, Long operatorId, String permissionType, JsonNode permissionData) {
        try {
            // 1. 更新权限缓存
            archiveService.updatePermissionCache(archiveId);
            
            // 2. 发送权限变更通知
            notificationService.sendArchivePermissionChangedNotification(archiveId, operatorId, permissionType);
            
            // 3. 记录权限变更历史
            archiveService.recordPermissionChangeHistory(archiveId, operatorId, permissionType, permissionData);
            
            log.debug("档案权限变更后处理完成，档案ID: {}", archiveId);
            
        } catch (Exception e) {
            log.error("处理档案权限变更后业务逻辑失败，档案ID: {}", archiveId, e);
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