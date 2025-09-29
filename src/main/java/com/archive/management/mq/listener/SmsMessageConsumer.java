package com.archive.management.mq.listener;

import com.archive.management.entity.SmsMessage;
import com.archive.management.service.SmsService;
import com.archive.management.service.AuditLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SMS消息消费者
 * 负责从RabbitMQ队列消费SMS消息并进行批量处理
 * 
 * @author Archive Management System
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsMessageConsumer {

    // 手动添加log变量以解决Lombok编译问题
    private static final Logger log = LoggerFactory.getLogger(SmsMessageConsumer.class);

    private final SmsService smsService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀
    private static final String SMS_PROCESSING_PREFIX = "sms:processing:";
    private static final String SMS_RETRY_PREFIX = "sms:retry:";
    private static final String SMS_BATCH_STATS_PREFIX = "sms:batch:stats:";

    /**
     * 监听单条SMS消息
     */
    @RabbitListener(queues = "${app.mq.queue.sms:sms.queue}")
    @Transactional
    public void handleSingleSms(Message message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageBody = null;
        SmsMessage smsMessage = null;
        
        try {
            messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            smsMessage = parseSmsMessage(messageData);
            
            log.info("处理单条SMS消息，消息ID: {}, 手机号: {}", 
                smsMessage.getMessageId(), smsMessage.getPhoneNumber());
            
            // 检查是否为定时发送
            if (smsMessage.getSendMode() == SmsMessage.SendMode.SCHEDULED && 
                smsMessage.getScheduledTime() != null && 
                smsMessage.getScheduledTime().isAfter(LocalDateTime.now())) {
                
                log.info("定时SMS消息，延迟处理: {}", smsMessage.getScheduledTime());
                handleScheduledSms(smsMessage);
                return;
            }
            
            // 处理单条SMS发送
            boolean success = processSingleSms(smsMessage);
            
            if (success) {
                updateSmsProcessingStatus(smsMessage.getMessageId(), "SUCCESS", null);
                recordAuditLog(smsMessage, "SMS_SENT", "单条SMS发送成功");
                log.info("单条SMS发送成功，消息ID: {}", smsMessage.getMessageId());
            } else {
                handleSmsFailure(smsMessage, "SMS发送失败");
            }
            
        } catch (Exception e) {
            log.error("处理单条SMS消息异常，消息体: {}", messageBody, e);
            if (smsMessage != null) {
                handleSmsFailure(smsMessage, "处理SMS消息异常: " + e.getMessage());
            }
            handleMessageProcessingError(message, e, "SINGLE_SMS");
        }
    }

    /**
     * 监听批量SMS消息
     */
    @RabbitListener(queues = "${app.mq.queue.sms.batch:sms.batch.queue}")
    @Transactional
    public void handleBatchSms(Message message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String messageBody = null;
        SmsMessage smsMessage = null;
        
        try {
            messageBody = new String(message.getBody());
            JsonNode messageData = objectMapper.readTree(messageBody);
            smsMessage = parseSmsMessage(messageData);
            
            log.info("处理批量SMS消息，消息ID: {}, 手机号数量: {}", 
                smsMessage.getMessageId(), smsMessage.getRecipientCount());
            
            // 检查是否为定时发送
            if (smsMessage.getSendMode() == SmsMessage.SendMode.SCHEDULED && 
                smsMessage.getScheduledTime() != null && 
                smsMessage.getScheduledTime().isAfter(LocalDateTime.now())) {
                
                log.info("定时批量SMS消息，延迟处理: {}", smsMessage.getScheduledTime());
                handleScheduledSms(smsMessage);
                return;
            }
            
            // 处理批量SMS发送
            BatchSmsResult result = processBatchSms(smsMessage);
            
            updateBatchSmsStatistics(smsMessage, result);
            updateSmsProcessingStatus(smsMessage.getMessageId(), 
                result.getTotalCount() == result.getSuccessCount() ? "SUCCESS" : "PARTIAL_SUCCESS", 
                result.getErrorMessage());
            
            recordAuditLog(smsMessage, "BATCH_SMS_SENT", 
                String.format("批量SMS发送完成，成功: %d, 失败: %d", 
                    result.getSuccessCount(), result.getFailureCount()));
            
            log.info("批量SMS发送完成，消息ID: {}, 成功: {}, 失败: {}", 
                smsMessage.getMessageId(), result.getSuccessCount(), result.getFailureCount());
            
        } catch (Exception e) {
            log.error("处理批量SMS消息异常，消息体: {}", messageBody, e);
            if (smsMessage != null) {
                handleSmsFailure(smsMessage, "处理批量SMS消息异常: " + e.getMessage());
            }
            handleMessageProcessingError(message, e, "BATCH_SMS");
        }
    }

    /**
     * 处理单条SMS发送
     */
    private boolean processSingleSms(SmsMessage smsMessage) {
        try {
            // 验证手机号码
            if (!smsService.validatePhoneNumber(smsMessage.getPhoneNumber())) {
                log.warn("手机号码格式无效: {}", smsMessage.getPhoneNumber());
                return false;
            }
            
            // 检查频率限制
            if (!smsService.checkRateLimit(smsMessage.getPhoneNumber(), 
                smsMessage.getMessageType().name())) {
                log.warn("SMS发送频率超限: {}", smsMessage.getPhoneNumber());
                return false;
            }
            
            boolean success;
            
            // 根据消息类型选择发送方式
            switch (smsMessage.getMessageType()) {
                case TEMPLATE:
                    // 转换模板参数类型
                    Map<String, String> templateParams = new HashMap<>();
                    if (smsMessage.getTemplateParams() != null) {
                        smsMessage.getTemplateParams().forEach((key, value) -> 
                            templateParams.put(key, value != null ? value.toString() : ""));
                    }
                    success = smsService.sendTemplateSms(
                        smsMessage.getPhoneNumber(),
                        smsMessage.getTemplateId(),
                        templateParams
                    );
                    break;
                case SINGLE:
                default:
                    success = smsService.sendSms(
                        smsMessage.getPhoneNumber(),
                        smsMessage.getContent()
                    );
                    break;
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("处理单条SMS发送异常，消息ID: {}", smsMessage.getMessageId(), e);
            return false;
        }
    }

    /**
     * 处理批量SMS发送
     */
    private BatchSmsResult processBatchSms(SmsMessage smsMessage) {
        BatchSmsResult result = new BatchSmsResult();
        
        if (CollectionUtils.isEmpty(smsMessage.getPhoneNumbers())) {
            result.setErrorMessage("手机号码列表为空");
            return result;
        }
        
        List<String> validPhoneNumbers = smsMessage.getPhoneNumbers().stream()
            .filter(phone -> smsService.validatePhoneNumber(phone))
            .collect(Collectors.toList());
        
        if (validPhoneNumbers.isEmpty()) {
            result.setErrorMessage("没有有效的手机号码");
            return result;
        }
        
        result.setTotalCount(validPhoneNumbers.size());
        
        // 分批处理，避免一次性发送过多
        int batchSize = 50; // 每批50个
        List<List<String>> batches = partitionList(validPhoneNumbers, batchSize);
        
        for (List<String> batch : batches) {
            try {
                // 使用现有的批量发送方法
                int successCount = smsService.sendBatchSms(batch, smsMessage.getContent());
                result.addSuccessCount(successCount);
                result.addFailureCount(batch.size() - successCount);
                
                // 批次间延迟，避免频率限制
                if (batches.size() > 1) {
                    Thread.sleep(1000); // 1秒延迟
                }
                
            } catch (Exception e) {
                log.error("批量SMS发送异常，批次大小: {}", batch.size(), e);
                result.addFailureCount(batch.size());
                if (result.getErrorMessage() == null) {
                    result.setErrorMessage("批量发送异常: " + e.getMessage());
                }
            }
        }
        
        return result;
    }

    /**
     * 处理定时SMS
     */
    private void handleScheduledSms(SmsMessage smsMessage) {
        try {
            // 将定时消息存储到Redis，等待定时处理
            String key = SMS_PROCESSING_PREFIX + "scheduled:" + smsMessage.getMessageId();
            redisTemplate.opsForValue().set(key, smsMessage, 
                calculateDelaySeconds(smsMessage.getScheduledTime()), TimeUnit.SECONDS);
            
            log.info("定时SMS消息已存储，消息ID: {}, 定时时间: {}", 
                smsMessage.getMessageId(), smsMessage.getScheduledTime());
                
        } catch (Exception e) {
            log.error("处理定时SMS异常，消息ID: {}", smsMessage.getMessageId(), e);
        }
    }

    /**
     * 处理SMS发送失败
     */
    private void handleSmsFailure(SmsMessage smsMessage, String errorMessage) {
        try {
            // 检查是否需要重试
            if (smsMessage.getRetryCount() < smsMessage.getMaxRetryCount()) {
                smsMessage.setRetryCount(smsMessage.getRetryCount() + 1);
                
                // 存储重试消息
                String retryKey = SMS_RETRY_PREFIX + smsMessage.getMessageId() + ":" + smsMessage.getRetryCount();
                redisTemplate.opsForValue().set(retryKey, smsMessage, 
                    calculateRetryDelay(smsMessage.getRetryCount()), TimeUnit.SECONDS);
                
                updateSmsProcessingStatus(smsMessage.getMessageId(), "RETRY_PENDING", errorMessage);
                log.info("SMS消息将重试，消息ID: {}, 重试次数: {}", 
                    smsMessage.getMessageId(), smsMessage.getRetryCount());
            } else {
                updateSmsProcessingStatus(smsMessage.getMessageId(), "FAILED", errorMessage);
                recordAuditLog(smsMessage, "SMS_FAILED", "SMS发送失败，已达最大重试次数: " + errorMessage);
                log.error("SMS发送失败，已达最大重试次数，消息ID: {}", smsMessage.getMessageId());
            }
            
        } catch (Exception e) {
            log.error("处理SMS失败异常，消息ID: {}", smsMessage.getMessageId(), e);
        }
    }

    /**
     * 解析SMS消息
     */
    private SmsMessage parseSmsMessage(JsonNode messageData) throws Exception {
        return objectMapper.treeToValue(messageData, SmsMessage.class);
    }

    /**
     * 更新SMS处理状态
     */
    private void updateSmsProcessingStatus(String messageId, String status, String errorMessage) {
        try {
            String key = SMS_PROCESSING_PREFIX + messageId;
            Map<String, Object> statusInfo = new HashMap<>();
            statusInfo.put("status", status);
            statusInfo.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            if (StringUtils.hasText(errorMessage)) {
                statusInfo.put("errorMessage", errorMessage);
            }
            
            redisTemplate.opsForHash().putAll(key, statusInfo);
            redisTemplate.expire(key, 7, TimeUnit.DAYS); // 保留7天
            
        } catch (Exception e) {
            log.error("更新SMS处理状态异常，消息ID: {}", messageId, e);
        }
    }

    /**
     * 更新批量SMS统计
     */
    private void updateBatchSmsStatistics(SmsMessage smsMessage, BatchSmsResult result) {
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = SMS_BATCH_STATS_PREFIX + today;
            
            redisTemplate.opsForHash().increment(key, "totalBatches", 1);
            redisTemplate.opsForHash().increment(key, "totalMessages", result.getTotalCount());
            redisTemplate.opsForHash().increment(key, "successMessages", result.getSuccessCount());
            redisTemplate.opsForHash().increment(key, "failedMessages", result.getFailureCount());
            
            // 按业务类型统计
            if (smsMessage.getBusinessType() != null) {
                String businessKey = key + ":" + smsMessage.getBusinessType().name();
                redisTemplate.opsForHash().increment(businessKey, "batches", 1);
                redisTemplate.opsForHash().increment(businessKey, "messages", result.getTotalCount());
                redisTemplate.opsForHash().increment(businessKey, "success", result.getSuccessCount());
                redisTemplate.expire(businessKey, 30, TimeUnit.DAYS);
            }
            
            redisTemplate.expire(key, 30, TimeUnit.DAYS); // 保留30天
            
        } catch (Exception e) {
            log.error("更新批量SMS统计异常", e);
        }
    }

    /**
     * 记录审计日志
     */
    private void recordAuditLog(SmsMessage smsMessage, String operation, String description) {
        try {
            if (auditLogService != null) {
                auditLogService.logSystemAction(
                    operation,
                    "SMS_MESSAGE",
                    Long.valueOf(smsMessage.getMessageId().hashCode()),
                    description,
                    "SUCCESS",
                    objectMapper.writeValueAsString(smsMessage.getExtendedProperties())
                );
            }
        } catch (Exception e) {
            log.error("记录SMS审计日志异常，消息ID: {}", smsMessage.getMessageId(), e);
        }
    }

    /**
     * 处理消息处理错误
     */
    private void handleMessageProcessingError(Message message, Exception e, String messageType) {
        try {
            log.error("消息处理失败，类型: {}, 消息: {}", messageType, new String(message.getBody()), e);
            
            // 这里可以实现将失败消息发送到死信队列的逻辑
            // 或者记录到数据库进行人工处理
            
        } catch (Exception ex) {
            log.error("处理消息错误异常", ex);
        }
    }

    /**
     * 计算延迟秒数
     */
    private long calculateDelaySeconds(LocalDateTime scheduledTime) {
        return java.time.Duration.between(LocalDateTime.now(), scheduledTime).getSeconds();
    }

    /**
     * 计算重试延迟（指数退避）
     */
    private long calculateRetryDelay(int retryCount) {
        return Math.min(300, (long) Math.pow(2, retryCount) * 10); // 最大5分钟
    }

    /**
     * 分割列表
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    /**
     * 批量SMS发送结果
     */
    private static class BatchSmsResult {
        private int totalCount = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private String errorMessage;

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public int getSuccessCount() { return successCount; }
        public void addSuccessCount(int count) { this.successCount += count; }

        public int getFailureCount() { return failureCount; }
        public void addFailureCount(int count) { this.failureCount += count; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}