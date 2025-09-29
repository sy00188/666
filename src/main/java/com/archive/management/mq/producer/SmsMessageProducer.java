package com.archive.management.mq.producer;

import com.archive.management.entity.SmsMessage;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SMS消息生产者
 * 负责将SMS消息发送到RabbitMQ队列进行异步处理
 * 
 * @author Archive Management System
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class SmsMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(SmsMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mq.exchange.sms:sms.exchange}")
    private String smsExchange;

    @Value("${app.mq.routing-key.sms.single:sms.single}")
    private String smsSingleRoutingKey;

    @Value("${app.mq.routing-key.sms.batch:sms.batch}")
    private String smsBatchRoutingKey;

    @Value("${app.mq.routing-key.sms.template:sms.template}")
    private String smsTemplateRoutingKey;

    /**
     * 发送单条SMS消息
     */
    public void sendSingleSmsMessage(String phoneNumber, String content, 
                                   SmsMessage.BusinessType businessType, 
                                   String businessId, Long operatorId, String operatorName) {
        try {
            SmsMessage smsMessage = SmsMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(SmsMessage.SmsMessageType.SINGLE)
                    .sendMode(SmsMessage.SendMode.IMMEDIATE)
                    .phoneNumber(phoneNumber)
                    .content(content)
                    .priority(SmsMessage.Priority.NORMAL)
                    .businessType(businessType)
                    .businessId(businessId)
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .createTime(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetryCount(3)
                    .sourceSystem("archive-management-system")
                    .version("1.0")
                    .build();

            sendMessage(smsExchange, smsSingleRoutingKey, smsMessage, "SMS_SINGLE");
            log.info("单条SMS消息发送成功 - 手机号: {}, 业务ID: {}, 消息ID: {}", 
                    phoneNumber, businessId, smsMessage.getMessageId());

        } catch (Exception e) {
            log.error("单条SMS消息发送失败 - 手机号: {}, 业务ID: {}", phoneNumber, businessId, e);
            throw new RuntimeException("单条SMS消息发送失败", e);
        }
    }

    /**
     * 发送批量SMS消息
     */
    public void sendBatchSmsMessage(List<String> phoneNumbers, String content, 
                                  SmsMessage.BusinessType businessType, 
                                  String businessId, Long operatorId, String operatorName) {
        try {
            SmsMessage smsMessage = SmsMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(SmsMessage.SmsMessageType.BATCH)
                    .sendMode(SmsMessage.SendMode.IMMEDIATE)
                    .phoneNumbers(phoneNumbers)
                    .content(content)
                    .priority(SmsMessage.Priority.NORMAL)
                    .businessType(businessType)
                    .businessId(businessId)
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .createTime(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetryCount(3)
                    .sourceSystem("archive-management-system")
                    .version("1.0")
                    .build();

            sendMessage(smsExchange, smsBatchRoutingKey, smsMessage, "SMS_BATCH");
            log.info("批量SMS消息发送成功 - 接收者数量: {}, 业务ID: {}, 消息ID: {}", 
                    phoneNumbers.size(), businessId, smsMessage.getMessageId());

        } catch (Exception e) {
            log.error("批量SMS消息发送失败 - 接收者数量: {}, 业务ID: {}", 
                    phoneNumbers != null ? phoneNumbers.size() : 0, businessId, e);
            throw new RuntimeException("批量SMS消息发送失败", e);
        }
    }

    /**
     * 发送模板SMS消息
     */
    public void sendTemplateSmsMessage(String phoneNumber, String templateId, 
                                     Map<String, Object> templateParams,
                                     SmsMessage.BusinessType businessType, 
                                     String businessId, Long operatorId, String operatorName) {
        try {
            SmsMessage smsMessage = SmsMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(SmsMessage.SmsMessageType.TEMPLATE)
                    .sendMode(SmsMessage.SendMode.IMMEDIATE)
                    .phoneNumber(phoneNumber)
                    .templateId(templateId)
                    .templateParams(templateParams)
                    .priority(SmsMessage.Priority.NORMAL)
                    .businessType(businessType)
                    .businessId(businessId)
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .createTime(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetryCount(3)
                    .sourceSystem("archive-management-system")
                    .version("1.0")
                    .build();

            sendMessage(smsExchange, smsTemplateRoutingKey, smsMessage, "SMS_TEMPLATE");
            log.info("模板SMS消息发送成功 - 手机号: {}, 模板ID: {}, 业务ID: {}, 消息ID: {}", 
                    phoneNumber, templateId, businessId, smsMessage.getMessageId());

        } catch (Exception e) {
            log.error("模板SMS消息发送失败 - 手机号: {}, 模板ID: {}, 业务ID: {}", 
                    phoneNumber, templateId, businessId, e);
            throw new RuntimeException("模板SMS消息发送失败", e);
        }
    }

    /**
     * 发送批量模板SMS消息
     */
    public void sendBatchTemplateSmsMessage(List<String> phoneNumbers, String templateId, 
                                          Map<String, Object> templateParams,
                                          SmsMessage.BusinessType businessType, 
                                          String businessId, Long operatorId, String operatorName) {
        try {
            SmsMessage smsMessage = SmsMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(SmsMessage.SmsMessageType.TEMPLATE)
                    .sendMode(SmsMessage.SendMode.IMMEDIATE)
                    .phoneNumbers(phoneNumbers)
                    .templateId(templateId)
                    .templateParams(templateParams)
                    .priority(SmsMessage.Priority.NORMAL)
                    .businessType(businessType)
                    .businessId(businessId)
                    .operatorId(operatorId)
                    .operatorName(operatorName)
                    .createTime(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetryCount(3)
                    .sourceSystem("archive-management-system")
                    .version("1.0")
                    .build();

            sendMessage(smsExchange, smsBatchRoutingKey, smsMessage, "SMS_BATCH_TEMPLATE");
            log.info("批量模板SMS消息发送成功 - 接收者数量: {}, 模板ID: {}, 业务ID: {}, 消息ID: {}", 
                    phoneNumbers.size(), templateId, businessId, smsMessage.getMessageId());

        } catch (Exception e) {
            log.error("批量模板SMS消息发送失败 - 接收者数量: {}, 模板ID: {}, 业务ID: {}", 
                    phoneNumbers != null ? phoneNumbers.size() : 0, templateId, businessId, e);
            throw new RuntimeException("批量模板SMS消息发送失败", e);
        }
    }

    /**
     * 发送定时SMS消息
     */
    public void sendScheduledSmsMessage(SmsMessage smsMessage, LocalDateTime scheduledTime) {
        try {
            smsMessage.setSendMode(SmsMessage.SendMode.SCHEDULED);
            smsMessage.setScheduledTime(scheduledTime);
            
            if (smsMessage.getMessageId() == null) {
                smsMessage.setMessageId(UUID.randomUUID().toString());
            }
            if (smsMessage.getCreateTime() == null) {
                smsMessage.setCreateTime(LocalDateTime.now());
            }

            // 计算延迟时间（毫秒）
            long delayMillis = java.time.Duration.between(LocalDateTime.now(), scheduledTime).toMillis();
            
            if (delayMillis > 0) {
                String routingKey = determineRoutingKey(smsMessage);
                sendDelayedMessage(smsExchange, routingKey, smsMessage, "SMS_SCHEDULED", delayMillis);
                log.info("定时SMS消息发送成功 - 计划时间: {}, 延迟: {}ms, 消息ID: {}", 
                        scheduledTime, delayMillis, smsMessage.getMessageId());
            } else {
                // 如果计划时间已过，立即发送
                String routingKey = determineRoutingKey(smsMessage);
                sendMessage(smsExchange, routingKey, smsMessage, "SMS_IMMEDIATE");
                log.info("定时SMS消息立即发送 - 计划时间已过, 消息ID: {}", smsMessage.getMessageId());
            }

        } catch (Exception e) {
            log.error("定时SMS消息发送失败 - 计划时间: {}, 消息ID: {}", 
                    scheduledTime, smsMessage.getMessageId(), e);
            throw new RuntimeException("定时SMS消息发送失败", e);
        }
    }

    /**
     * 发送高优先级SMS消息
     */
    public void sendHighPrioritySmsMessage(SmsMessage smsMessage) {
        try {
            smsMessage.setPriority(SmsMessage.Priority.HIGH);
            
            if (smsMessage.getMessageId() == null) {
                smsMessage.setMessageId(UUID.randomUUID().toString());
            }
            if (smsMessage.getCreateTime() == null) {
                smsMessage.setCreateTime(LocalDateTime.now());
            }

            String routingKey = determineRoutingKey(smsMessage);
            sendMessage(smsExchange, routingKey, smsMessage, "SMS_HIGH_PRIORITY");
            log.info("高优先级SMS消息发送成功 - 优先级: {}, 消息ID: {}", 
                    smsMessage.getPriority(), smsMessage.getMessageId());

        } catch (Exception e) {
            log.error("高优先级SMS消息发送失败 - 消息ID: {}", smsMessage.getMessageId(), e);
            throw new RuntimeException("高优先级SMS消息发送失败", e);
        }
    }

    /**
     * 重新发送失败的SMS消息
     */
    public void retrySmsMessage(SmsMessage smsMessage) {
        try {
            if (!smsMessage.needsRetry()) {
                log.warn("SMS消息已达到最大重试次数 - 消息ID: {}, 重试次数: {}/{}", 
                        smsMessage.getMessageId(), smsMessage.getRetryCount(), smsMessage.getMaxRetryCount());
                return;
            }

            smsMessage.incrementRetryCount();
            String routingKey = determineRoutingKey(smsMessage);
            sendMessage(smsExchange, routingKey, smsMessage, "SMS_RETRY");
            log.info("SMS消息重试发送成功 - 消息ID: {}, 重试次数: {}/{}", 
                    smsMessage.getMessageId(), smsMessage.getRetryCount(), smsMessage.getMaxRetryCount());

        } catch (Exception e) {
            log.error("SMS消息重试发送失败 - 消息ID: {}, 重试次数: {}", 
                    smsMessage.getMessageId(), smsMessage.getRetryCount(), e);
            throw new RuntimeException("SMS消息重试发送失败", e);
        }
    }

    /**
     * 根据消息类型确定路由键
     */
    private String determineRoutingKey(SmsMessage smsMessage) {
        switch (smsMessage.getMessageType()) {
            case SINGLE:
                return smsSingleRoutingKey;
            case BATCH:
                return smsBatchRoutingKey;
            case TEMPLATE:
                return smsMessage.isBatchMessage() ? smsBatchRoutingKey : smsTemplateRoutingKey;
            default:
                return smsSingleRoutingKey;
        }
    }

    /**
     * 创建消息数据
     */
    private Map<String, Object> createMessageData(String eventType, SmsMessage smsMessage) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("eventType", eventType);
        messageData.put("smsMessage", smsMessage);
        messageData.put("timestamp", LocalDateTime.now());
        messageData.put("messageId", smsMessage.getMessageId());
        return messageData;
    }

    /**
     * 发送消息到RabbitMQ
     */
    private void sendMessage(String exchange, String routingKey, SmsMessage smsMessage, String messageType) {
        try {
            // 创建消息属性
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setContentEncoding("UTF-8");
            properties.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            properties.setPriority(smsMessage.getPriority().getLevel());
            properties.setMessageId(smsMessage.getMessageId());
            properties.setTimestamp(new java.util.Date());
            properties.setHeader("messageType", messageType);
            properties.setHeader("businessType", smsMessage.getBusinessType().name());
            properties.setHeader("priority", smsMessage.getPriority().name());
            properties.setHeader("source", "archive-management-system");
            properties.setHeader("version", smsMessage.getVersion());

            // 创建消息数据
            Map<String, Object> messageData = createMessageData(messageType, smsMessage);

            // 序列化消息内容
            String messageJson = objectMapper.writeValueAsString(messageData);
            Message message = new Message(messageJson.getBytes(), properties);

            // 发送消息
            rabbitTemplate.send(exchange, routingKey, message);
            log.debug("SMS消息发送成功 - 交换机: {}, 路由键: {}, 消息类型: {}, 消息ID: {}", 
                    exchange, routingKey, messageType, smsMessage.getMessageId());

        } catch (JsonProcessingException e) {
            log.error("SMS消息序列化失败 - 消息类型: {}, 消息ID: {}", messageType, smsMessage.getMessageId(), e);
            throw new RuntimeException("SMS消息序列化失败", e);
        } catch (Exception e) {
            log.error("SMS消息发送失败 - 交换机: {}, 路由键: {}, 消息类型: {}, 消息ID: {}", 
                    exchange, routingKey, messageType, smsMessage.getMessageId(), e);
            throw new RuntimeException("SMS消息发送失败", e);
        }
    }

    /**
     * 发送延迟消息
     */
    private void sendDelayedMessage(String exchange, String routingKey, SmsMessage smsMessage, 
                                  String messageType, long delayMillis) {
        try {
            // 创建消息属性
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setContentEncoding("UTF-8");
            properties.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
            properties.setPriority(smsMessage.getPriority().getLevel());
            properties.setMessageId(smsMessage.getMessageId());
            properties.setTimestamp(new java.util.Date());
            properties.setHeader("messageType", messageType);
            properties.setHeader("businessType", smsMessage.getBusinessType().name());
            properties.setHeader("priority", smsMessage.getPriority().name());
            properties.setHeader("source", "archive-management-system");
            properties.setHeader("version", smsMessage.getVersion());
            properties.setHeader("x-delay", delayMillis); // 延迟消息头

            // 创建消息数据
            Map<String, Object> messageData = createMessageData(messageType, smsMessage);

            // 序列化消息内容
            String messageJson = objectMapper.writeValueAsString(messageData);
            Message message = new Message(messageJson.getBytes(), properties);

            // 发送延迟消息
            rabbitTemplate.send(exchange, routingKey, message);
            log.debug("SMS延迟消息发送成功 - 交换机: {}, 路由键: {}, 延迟: {}ms, 消息ID: {}", 
                    exchange, routingKey, delayMillis, smsMessage.getMessageId());

        } catch (JsonProcessingException e) {
            log.error("SMS延迟消息序列化失败 - 消息类型: {}, 消息ID: {}", messageType, smsMessage.getMessageId(), e);
            throw new RuntimeException("SMS延迟消息序列化失败", e);
        } catch (Exception e) {
            log.error("SMS延迟消息发送失败 - 交换机: {}, 路由键: {}, 延迟: {}ms, 消息ID: {}", 
                    exchange, routingKey, delayMillis, smsMessage.getMessageId(), e);
            throw new RuntimeException("SMS延迟消息发送失败", e);
        }
    }
}