package com.archive.management.mq.producer;

import com.archive.management.dto.ArchiveDTO;
import com.archive.management.dto.ArchiveFileDTO;
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
 * 档案消息生产者
 * 负责发送档案相关的消息队列消息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
public class ArchiveMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(ArchiveMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mq.exchange.archive:archive.exchange}")
    private String archiveExchange;

    @Value("${app.mq.routing-key.archive.created:archive.created}")
    private String archiveCreatedRoutingKey;

    @Value("${app.mq.routing-key.archive.updated:archive.updated}")
    private String archiveUpdatedRoutingKey;

    @Value("${app.mq.routing-key.archive.deleted:archive.deleted}")
    private String archiveDeletedRoutingKey;

    @Value("${app.mq.routing-key.archive.published:archive.published}")
    private String archivePublishedRoutingKey;

    @Value("${app.mq.routing-key.archive.archived:archive.archived}")
    private String archiveArchivedRoutingKey;

    @Value("${app.mq.routing-key.archive.file.uploaded:archive.file.uploaded}")
    private String fileUploadedRoutingKey;

    @Value("${app.mq.routing-key.archive.file.deleted:archive.file.deleted}")
    private String fileDeletedRoutingKey;

    @Value("${app.mq.routing-key.archive.backup:archive.backup}")
    private String archiveBackupRoutingKey;

    @Value("${app.mq.routing-key.archive.restore:archive.restore}")
    private String archiveRestoreRoutingKey;

    @Value("${app.mq.routing-key.archive.index:archive.index}")
    private String archiveIndexRoutingKey;

    /**
     * 发送档案创建消息
     */
    public void sendArchiveCreatedMessage(ArchiveDTO archive) {
        try {
            Map<String, Object> messageData = createMessageData("ARCHIVE_CREATED", archive);
            sendMessage(archiveExchange, archiveCreatedRoutingKey, messageData, "ARCHIVE_CREATED");
            log.info("发送档案创建消息成功，档案ID: {}, 档案标题: {}", archive.id, archive.title);
        } catch (Exception e) {
            log.error("发送档案创建消息失败，档案ID: {}", archive.id, e);
        }
    }

    /**
     * 发送档案更新消息
     */
    public void sendArchiveUpdatedMessage(ArchiveDTO archive, ArchiveDTO oldArchive) {
        try {
            Map<String, Object> messageData = createMessageData("ARCHIVE_UPDATED", 
                Map.of("archive", archive, "oldArchive", oldArchive));
            sendMessage(archiveExchange, archiveUpdatedRoutingKey, messageData, "ARCHIVE_UPDATED");
            log.info("发送档案更新消息成功，档案ID: {}, 档案标题: {}", archive.id, archive.title);
        } catch (Exception e) {
            log.error("发送档案更新消息失败，档案ID: {}", archive.id, e);
        }
    }

    /**
     * 发送档案删除消息
     */
    public void sendArchiveDeletedMessage(Long archiveId, String archiveTitle, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "ARCHIVE_DELETED");
            messageData.put("archiveId", archiveId);
            messageData.put("archiveTitle", archiveTitle);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(archiveExchange, archiveDeletedRoutingKey, messageData, "档案删除");
            log.info("发送档案删除消息成功，档案ID: {}, 档案标题: {}", archiveId, archiveTitle);
        } catch (Exception e) {
            log.error("发送档案删除消息失败，档案ID: {}", archiveId, e);
        }
    }

    /**
     * 发送档案发布消息
     */
    public void sendArchivePublishedMessage(ArchiveDTO archive) {
        try {
            Map<String, Object> messageData = createMessageData("ARCHIVE_PUBLISHED", archive);
            sendMessage(archiveExchange, archivePublishedRoutingKey, messageData, "ARCHIVE_PUBLISHED");
            log.info("发送档案发布消息成功，档案ID: {}, 档案标题: {}", archive.id, archive.title);
        } catch (Exception e) {
            log.error("发送档案发布消息失败，档案ID: {}", archive.id, e);
        }
    }

    /**
     * 发送档案归档消息
     */
    public void sendArchiveArchivedMessage(ArchiveDTO archive) {
        try {
            Map<String, Object> messageData = createMessageData("ARCHIVE_ARCHIVED", archive);
            sendMessage(archiveExchange, archiveArchivedRoutingKey, messageData, "ARCHIVE_ARCHIVED");
            log.info("发送档案归档消息成功，档案ID: {}, 档案标题: {}", archive.id, archive.title);
        } catch (Exception e) {
            log.error("发送档案归档消息失败，档案ID: {}", archive.id, e);
        }
    }

    /**
     * 发送文件上传消息
     */
    public void sendFileUploadedMessage(ArchiveFileDTO archiveFile) {
        try {
            Map<String, Object> messageData = createMessageData("FILE_UPLOADED", archiveFile);
            sendMessage(archiveExchange, fileUploadedRoutingKey, messageData, "文件上传");
            log.info("发送文件上传消息成功，文件ID: {}, 文件名: {}", archiveFile.getId(), archiveFile.getFileName());
        } catch (Exception e) {
            log.error("发送文件上传消息失败，文件ID: {}", archiveFile.getId(), e);
        }
    }

    /**
     * 发送文件删除消息
     */
    public void sendFileDeletedMessage(Long fileId, String fileName, Long archiveId, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "FILE_DELETED");
            messageData.put("fileId", fileId);
            messageData.put("fileName", fileName);
            messageData.put("archiveId", archiveId);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(archiveExchange, fileDeletedRoutingKey, messageData, "文件删除");
            log.info("发送文件删除消息成功，文件ID: {}, 文件名: {}", fileId, fileName);
        } catch (Exception e) {
            log.error("发送文件删除消息失败，文件ID: {}", fileId, e);
        }
    }

    /**
     * 发送档案备份消息
     */
    public void sendArchiveBackupMessage(Long archiveId, String backupPath, String backupType) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "ARCHIVE_BACKUP");
            messageData.put("archiveId", archiveId);
            messageData.put("backupPath", backupPath);
            messageData.put("backupType", backupType);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(archiveExchange, archiveBackupRoutingKey, messageData, "档案备份");
            log.info("发送档案备份消息成功，档案ID: {}, 备份路径: {}", archiveId, backupPath);
        } catch (Exception e) {
            log.error("发送档案备份消息失败，档案ID: {}", archiveId, e);
        }
    }

    /**
     * 发送档案恢复消息
     */
    public void sendArchiveRestoreMessage(Long archiveId, String backupPath, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "ARCHIVE_RESTORE");
            messageData.put("archiveId", archiveId);
            messageData.put("backupPath", backupPath);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(archiveExchange, archiveRestoreRoutingKey, messageData, "档案恢复");
            log.info("发送档案恢复消息成功，档案ID: {}, 备份路径: {}", archiveId, backupPath);
        } catch (Exception e) {
            log.error("发送档案恢复消息失败，档案ID: {}", archiveId, e);
        }
    }

    /**
     * 发送档案索引消息
     */
    public void sendArchiveIndexMessage(ArchiveDTO archive, String indexAction) {
        try {
            Map<String, Object> messageData = createMessageData("ARCHIVE_INDEX", 
                Map.of("archive", archive, "indexAction", indexAction));
            sendMessage(archiveExchange, archiveIndexRoutingKey, messageData, "ARCHIVE_INDEX");
            log.info("发送档案索引消息成功，档案ID: {}, 索引操作: {}", archive.id, indexAction);
        } catch (Exception e) {
            log.error("发送档案索引消息失败，档案ID: {}", archive.id, e);
        }
    }

    /**
     * 发送批量档案处理消息
     */
    public void sendBatchArchiveMessage(String batchType, Object batchData, Long operatorId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "BATCH_ARCHIVE_PROCESS");
            messageData.put("batchType", batchType);
            messageData.put("batchData", batchData);
            messageData.put("operatorId", operatorId);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(archiveExchange, "archive.batch.process", messageData, "批量档案处理");
            log.info("发送批量档案处理消息成功，批量类型: {}, 操作人ID: {}", batchType, operatorId);
        } catch (Exception e) {
            log.error("发送批量档案处理消息失败，批量类型: {}", batchType, e);
        }
    }

    /**
     * 发送档案统计消息
     */
    public void sendArchiveStatisticsMessage(String statisticsType, Map<String, Object> statisticsData) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("eventType", "ARCHIVE_STATISTICS");
            messageData.put("statisticsType", statisticsType);
            messageData.put("statisticsData", statisticsData);
            messageData.put("timestamp", LocalDateTime.now());
            messageData.put("messageId", UUID.randomUUID().toString());

            sendMessage(archiveExchange, "archive.statistics", messageData, "档案统计");
            log.info("发送档案统计消息成功，统计类型: {}", statisticsType);
        } catch (Exception e) {
            log.error("发送档案统计消息失败，统计类型: {}", statisticsType, e);
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

    /**
     * 发送延迟消息
     */
    public void sendDelayedMessage(String exchange, String routingKey, Object messageData, 
                                   String messageType, long delayMillis) {
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
            properties.setHeader("x-delay", delayMillis); // 延迟时间

            // 序列化消息内容
            String messageJson = objectMapper.writeValueAsString(messageData);
            Message message = new Message(messageJson.getBytes(), properties);

            // 发送延迟消息
            rabbitTemplate.send(exchange, routingKey, message);
            log.info("延迟消息发送成功 - 交换机: {}, 路由键: {}, 消息类型: {}, 延迟: {}ms", 
                    exchange, routingKey, messageType, delayMillis);

        } catch (JsonProcessingException e) {
            log.error("延迟消息序列化失败 - 消息类型: {}", messageType, e);
            throw new RuntimeException("延迟消息序列化失败", e);
        } catch (Exception e) {
            log.error("延迟消息发送失败 - 交换机: {}, 路由键: {}, 消息类型: {}", exchange, routingKey, messageType, e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }

    /**
     * 发送事务消息
     */
    public void sendTransactionalMessage(String exchange, String routingKey, Object messageData, String messageType) {
        try {
            rabbitTemplate.execute(channel -> {
                try {
                    // 开启事务
                    channel.txSelect();
                    
                    // 发送消息
                    sendMessage(exchange, routingKey, messageData, messageType);
                    
                    // 提交事务
                    channel.txCommit();
                    log.info("事务消息发送成功 - 交换机: {}, 路由键: {}, 消息类型: {}", exchange, routingKey, messageType);
                    
                } catch (Exception e) {
                    // 回滚事务
                    channel.txRollback();
                    log.error("事务消息发送失败，已回滚 - 交换机: {}, 路由键: {}, 消息类型: {}", 
                            exchange, routingKey, messageType, e);
                    throw new RuntimeException("事务消息发送失败", e);
                }
                return null;
            });
            
        } catch (Exception e) {
            log.error("事务消息处理失败 - 交换机: {}, 路由键: {}, 消息类型: {}", exchange, routingKey, messageType, e);
            throw new RuntimeException("事务消息处理失败", e);
        }
    }
}