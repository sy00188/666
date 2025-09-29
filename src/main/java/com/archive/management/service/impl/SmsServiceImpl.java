package com.archive.management.service.impl;

import com.archive.management.service.SmsService;
import com.archive.management.mq.producer.SmsMessageProducer;
import com.archive.management.entity.SmsMessage;
import com.archive.management.entity.SmsMessage.SmsMessageType;
import com.archive.management.entity.SmsMessage.SendMode;
import com.archive.management.entity.SmsMessage.Priority;
import com.archive.management.entity.SmsMessage.BusinessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * 短信服务实现类
 * 实现短信发送相关的核心业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsMessageProducer smsMessageProducer;

    // 短信配置参数
    @Value("${sms.provider:aliyun}")
    private String smsProvider;

    @Value("${sms.access-key:}")
    private String accessKey;

    @Value("${sms.access-secret:}")
    private String accessSecret;

    @Value("${sms.sign-name:档案管理系统}")
    private String signName;

    @Value("${sms.rate-limit.verification:60}")
    private int verificationRateLimit; // 验证码发送间隔（秒）

    @Value("${sms.rate-limit.notification:300}")
    private int notificationRateLimit; // 通知短信发送间隔（秒）

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    // 手机号码正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // Redis键前缀
    private static final String SMS_RATE_LIMIT_PREFIX = "sms:rate_limit:";
    private static final String SMS_STATISTICS_PREFIX = "sms:statistics:";

    @Override
    public boolean sendSms(String phoneNumber, String content) {
        try {
            // 验证手机号码
            if (!validatePhoneNumber(phoneNumber)) {
                log.warn("手机号码格式无效: {}", phoneNumber);
                return false;
            }

            // 检查短信服务是否启用
            if (!smsEnabled) {
                log.info("短信服务未启用，模拟发送成功: phoneNumber={}, content={}", phoneNumber, content);
                return true;
            }

            // 检查频率限制
            if (!checkRateLimit(phoneNumber, "GENERAL")) {
                log.warn("短信发送频率超限: {}", phoneNumber);
                return false;
            }

            // 构建完整短信内容
            String fullContent = String.format("【%s】%s", signName, content);

            // 调用短信服务商API发送短信
            boolean result = sendSmsInternal(phoneNumber, fullContent);

            if (result) {
                // 更新发送统计
                updateSmsStatistics("GENERAL", true);
                // 设置频率限制
                setRateLimit(phoneNumber, "GENERAL", notificationRateLimit);
                log.info("短信发送成功: phoneNumber={}", phoneNumber);
            } else {
                updateSmsStatistics("GENERAL", false);
                log.error("短信发送失败: phoneNumber={}", phoneNumber);
            }

            return result;
        } catch (Exception e) {
            log.error("发送短信异常: phoneNumber={}", phoneNumber, e);
            updateSmsStatistics("GENERAL", false);
            return false;
        }
    }

    @Override
    public boolean sendTemplateSms(String phoneNumber, String templateCode, Map<String, String> templateParams) {
        try {
            // 验证手机号码
            if (!validatePhoneNumber(phoneNumber)) {
                log.warn("手机号码格式无效: {}", phoneNumber);
                return false;
            }

            // 检查短信服务是否启用
            if (!smsEnabled) {
                log.info("短信服务未启用，模拟模板短信发送成功: phoneNumber={}, templateCode={}", phoneNumber, templateCode);
                return true;
            }

            // 检查频率限制
            if (!checkRateLimit(phoneNumber, "TEMPLATE")) {
                log.warn("模板短信发送频率超限: {}", phoneNumber);
                return false;
            }

            // 调用短信服务商API发送模板短信
            boolean result = sendTemplateSmsInternal(phoneNumber, templateCode, templateParams);

            if (result) {
                updateSmsStatistics("TEMPLATE", true);
                setRateLimit(phoneNumber, "TEMPLATE", notificationRateLimit);
                log.info("模板短信发送成功: phoneNumber={}, templateCode={}", phoneNumber, templateCode);
            } else {
                updateSmsStatistics("TEMPLATE", false);
                log.error("模板短信发送失败: phoneNumber={}, templateCode={}", phoneNumber, templateCode);
            }

            return result;
        } catch (Exception e) {
            log.error("发送模板短信异常: phoneNumber={}, templateCode={}", phoneNumber, templateCode, e);
            updateSmsStatistics("TEMPLATE", false);
            return false;
        }
    }

    @Override
    public int sendBatchSms(List<String> phoneNumbers, String content) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String phoneNumber : phoneNumbers) {
            if (sendSms(phoneNumber, content)) {
                successCount++;
            }
        }

        log.info("批量短信发送完成: 总数={}, 成功={}", phoneNumbers.size(), successCount);
        return successCount;
    }

    @Override
    public boolean sendVerificationCode(String phoneNumber, String code) {
        try {
            // 验证手机号码
            if (!validatePhoneNumber(phoneNumber)) {
                log.warn("手机号码格式无效: {}", phoneNumber);
                return false;
            }

            // 检查验证码发送频率限制
            if (!checkRateLimit(phoneNumber, "VERIFICATION")) {
                log.warn("验证码发送频率超限: {}", phoneNumber);
                return false;
            }

            // 构建验证码短信内容
            String content = String.format("您的验证码是：%s，5分钟内有效，请勿泄露给他人。", code);

            // 发送短信
            boolean result = sendSms(phoneNumber, content);

            if (result) {
                // 设置验证码发送频率限制
                setRateLimit(phoneNumber, "VERIFICATION", verificationRateLimit);
                updateSmsStatistics("VERIFICATION", true);
                log.info("验证码短信发送成功: phoneNumber={}", phoneNumber);
            } else {
                updateSmsStatistics("VERIFICATION", false);
            }

            return result;
        } catch (Exception e) {
            log.error("发送验证码短信异常: phoneNumber={}", phoneNumber, e);
            updateSmsStatistics("VERIFICATION", false);
            return false;
        }
    }

    @Override
    public boolean sendNotificationSms(String phoneNumber, String title, String content) {
        try {
            // 构建通知短信内容
            String smsContent = StringUtils.hasText(title) ? 
                    String.format("%s：%s", title, content) : content;

            // 限制短信长度
            if (smsContent.length() > 70) {
                smsContent = smsContent.substring(0, 67) + "...";
            }

            return sendSms(phoneNumber, smsContent);
        } catch (Exception e) {
            log.error("发送通知短信异常: phoneNumber={}, title={}", phoneNumber, title, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> checkSmsStatus(String messageId) {
        Map<String, Object> result = new HashMap<>();
        
        if (!StringUtils.hasText(messageId)) {
            result.put("success", false);
            result.put("message", "消息ID不能为空");
            return result;
        }

        try {
            // 这里应该调用短信服务商的状态查询API
            // 暂时返回模拟数据
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("status", "DELIVERED");
            result.put("deliveryTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.info("查询短信状态: messageId={}", messageId);
        } catch (Exception e) {
            log.error("查询短信状态异常: messageId={}", messageId, e);
            result.put("success", false);
            result.put("message", "查询短信状态失败");
        }

        return result;
    }

    @Override
    public Map<String, Object> getSmsStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 从Redis获取统计数据
            String generalKey = SMS_STATISTICS_PREFIX + "GENERAL:" + today;
            String templateKey = SMS_STATISTICS_PREFIX + "TEMPLATE:" + today;
            String verificationKey = SMS_STATISTICS_PREFIX + "VERIFICATION:" + today;

            Map<Object, Object> generalStats = redisTemplate.opsForHash().entries(generalKey);
            Map<Object, Object> templateStats = redisTemplate.opsForHash().entries(templateKey);
            Map<Object, Object> verificationStats = redisTemplate.opsForHash().entries(verificationKey);

            statistics.put("general", generalStats);
            statistics.put("template", templateStats);
            statistics.put("verification", verificationStats);
            
            // 计算总计
            int totalSent = getStatValue(generalStats, "sent") + 
                           getStatValue(templateStats, "sent") + 
                           getStatValue(verificationStats, "sent");
            int totalSuccess = getStatValue(generalStats, "success") + 
                              getStatValue(templateStats, "success") + 
                              getStatValue(verificationStats, "success");

            statistics.put("total", Map.of(
                "sent", totalSent,
                "success", totalSuccess,
                "failed", totalSent - totalSuccess,
                "successRate", totalSent > 0 ? (double) totalSuccess / totalSent * 100 : 0
            ));

            log.info("获取短信统计数据: startDate={}, endDate={}", startDate, endDate);
        } catch (Exception e) {
            log.error("获取短信统计数据异常", e);
            statistics.put("error", "获取统计数据失败");
        }

        return statistics;
    }

    @Override
    public boolean validatePhoneNumber(String phoneNumber) {
        return StringUtils.hasText(phoneNumber) && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    @Override
    public boolean checkRateLimit(String phoneNumber, String smsType) {
        try {
            String key = SMS_RATE_LIMIT_PREFIX + smsType + ":" + phoneNumber;
            return !redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查短信发送频率限制异常: phoneNumber={}, smsType={}", phoneNumber, smsType, e);
            return true; // 异常时允许发送
        }
    }

    @Override
    public Map<String, Object> getSmsBalance() {
        Map<String, Object> balance = new HashMap<>();
        
        try {
            // 这里应该调用短信服务商的余额查询API
            // 暂时返回模拟数据
            balance.put("success", true);
            balance.put("balance", 10000);
            balance.put("unit", "条");
            balance.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            log.info("查询短信余额");
        } catch (Exception e) {
            log.error("查询短信余额异常", e);
            balance.put("success", false);
            balance.put("message", "查询余额失败");
        }

        return balance;
    }

    /**
     * 内部短信发送方法
     */
    private boolean sendSmsInternal(String phoneNumber, String content) {
        try {
            // 这里应该根据配置的短信服务商调用相应的API
            // 目前返回模拟成功结果
            log.info("调用短信服务商API发送短信: phoneNumber={}, content={}", phoneNumber, content);
            
            // 模拟API调用延迟
            Thread.sleep(100);
            
            return true;
        } catch (Exception e) {
            log.error("调用短信服务商API异常", e);
            return false;
        }
    }

    /**
     * 内部模板短信发送方法
     */
    private boolean sendTemplateSmsInternal(String phoneNumber, String templateCode, Map<String, String> templateParams) {
        try {
            // 这里应该根据配置的短信服务商调用相应的模板短信API
            log.info("调用短信服务商模板API: phoneNumber={}, templateCode={}, params={}", 
                    phoneNumber, templateCode, templateParams);
            
            // 模拟API调用延迟
            Thread.sleep(100);
            
            return true;
        } catch (Exception e) {
            log.error("调用短信服务商模板API异常", e);
            return false;
        }
    }

    /**
     * 设置频率限制
     */
    private void setRateLimit(String phoneNumber, String smsType, int seconds) {
        try {
            String key = SMS_RATE_LIMIT_PREFIX + smsType + ":" + phoneNumber;
            redisTemplate.opsForValue().set(key, "1", seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置短信发送频率限制异常: phoneNumber={}, smsType={}", phoneNumber, smsType, e);
        }
    }

    /**
     * 更新短信发送统计
     */
    private void updateSmsStatistics(String smsType, boolean success) {
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = SMS_STATISTICS_PREFIX + smsType + ":" + today;
            
            redisTemplate.opsForHash().increment(key, "sent", 1);
            if (success) {
                redisTemplate.opsForHash().increment(key, "success", 1);
            } else {
                redisTemplate.opsForHash().increment(key, "failed", 1);
            }
            
            // 设置过期时间为7天
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("更新短信发送统计异常: smsType={}, success={}", smsType, success, e);
        }
    }

    /**
     * 获取统计值
     */
    private int getStatValue(Map<Object, Object> stats, String key) {
        Object value = stats.get(key);
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }

    // ==================== 异步方法实现 ====================

    @Override
    public CompletableFuture<String> sendSmsAsync(String phoneNumber, String content, 
                                                 Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                SmsMessage.BusinessType businessTypeEnum = SmsMessage.BusinessType.valueOf(businessType.toUpperCase());
                
                smsMessageProducer.sendSingleSmsMessage(phoneNumber, content, businessTypeEnum, 
                                                      businessId, operatorId, "System");
                
                log.info("异步发送单条短信成功 - 手机号: {}, 消息ID: {}", phoneNumber, messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送单条短信失败 - 手机号: {}", phoneNumber, e);
                throw new RuntimeException("异步发送单条短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<String> sendTemplateSmsAsync(String phoneNumber, String templateId, 
                                                         Map<String, String> templateParams,
                                                         Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                SmsMessage.BusinessType businessTypeEnum = SmsMessage.BusinessType.valueOf(businessType.toUpperCase());
                
                // 转换Map<String, String>为Map<String, Object>
                Map<String, Object> templateParamsObj = new HashMap<>(templateParams);
                
                smsMessageProducer.sendTemplateSmsMessage(phoneNumber, templateId, templateParamsObj, 
                                                        businessTypeEnum, businessId, operatorId, "System");
                
                log.info("异步发送模板短信成功 - 手机号: {}, 模板ID: {}, 消息ID: {}", phoneNumber, templateId, messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送模板短信失败 - 手机号: {}, 模板ID: {}", phoneNumber, templateId, e);
                throw new RuntimeException("异步发送模板短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<String> sendBatchSmsAsync(List<String> phoneNumbers, String content,
                                                      Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                SmsMessage.BusinessType businessTypeEnum = SmsMessage.BusinessType.valueOf(businessType.toUpperCase());
                
                smsMessageProducer.sendBatchSmsMessage(phoneNumbers, content, businessTypeEnum, 
                                                     businessId, operatorId, "System");
                
                log.info("异步发送批量短信成功 - 接收者数量: {}, 消息ID: {}", phoneNumbers.size(), messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送批量短信失败 - 接收者数量: {}", phoneNumbers.size(), e);
                throw new RuntimeException("异步发送批量短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<String> sendHighPrioritySmsAsync(String phoneNumber, String content,
                                                             Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                
                // 转换业务类型枚举
                BusinessType businessTypeEnum = BusinessType.valueOf(businessType.toUpperCase());
                
                SmsMessage smsMessage = SmsMessage.builder()
                        .messageId(messageId)
                        .messageType(SmsMessageType.SINGLE)
                        .sendMode(SendMode.IMMEDIATE)
                        .phoneNumber(phoneNumber)
                        .content(content)
                        .priority(Priority.HIGH)
                        .businessType(businessTypeEnum)
                        .businessId(businessId)
                        .operatorId(operatorId)
                        .createTime(LocalDateTime.now())
                        .retryCount(0)
                        .maxRetryCount(3)
                        .build();

                smsMessageProducer.sendHighPrioritySmsMessage(smsMessage);
                
                log.info("异步发送高优先级短信成功 - 手机号: {}, 消息ID: {}", phoneNumber, messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送高优先级短信失败 - 手机号: {}", phoneNumber, e);
                throw new RuntimeException("发送高优先级短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<String> sendScheduledSmsAsync(String phoneNumber, String content, 
                                                          LocalDateTime scheduledTime,
                                                          Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                
                // 转换业务类型枚举
                BusinessType businessTypeEnum = BusinessType.valueOf(businessType.toUpperCase());
                
                SmsMessage smsMessage = SmsMessage.builder()
                        .messageId(messageId)
                        .messageType(SmsMessageType.SINGLE)
                        .sendMode(SendMode.SCHEDULED)
                        .phoneNumber(phoneNumber)
                        .content(content)
                        .priority(Priority.NORMAL)
                        .businessType(businessTypeEnum)
                        .businessId(businessId)
                        .operatorId(operatorId)
                        .createTime(LocalDateTime.now())
                        .scheduledTime(scheduledTime)
                        .retryCount(0)
                        .maxRetryCount(3)
                        .build();

                smsMessageProducer.sendScheduledSmsMessage(smsMessage, scheduledTime);
                
                log.info("异步发送定时短信成功 - 手机号: {}, 定时时间: {}, 消息ID: {}", phoneNumber, scheduledTime, messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送定时短信失败 - 手机号: {}, 定时时间: {}", phoneNumber, scheduledTime, e);
                throw new RuntimeException("发送定时短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<String> sendScheduledTemplateSmsAsync(String phoneNumber, String templateId,
                                                                  Map<String, String> templateParams,
                                                                  LocalDateTime scheduledTime,
                                                                  Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                
                // 转换业务类型枚举
                BusinessType businessTypeEnum = BusinessType.valueOf(businessType.toUpperCase());
                
                // 转换模板参数类型
                Map<String, Object> templateParamsObj = new HashMap<>(templateParams);
                
                SmsMessage smsMessage = SmsMessage.builder()
                        .messageId(messageId)
                        .messageType(SmsMessageType.TEMPLATE)
                        .sendMode(SendMode.SCHEDULED)
                        .phoneNumber(phoneNumber)
                        .templateId(templateId)
                        .templateParams(templateParamsObj)
                        .priority(Priority.NORMAL)
                        .businessType(businessTypeEnum)
                        .businessId(businessId)
                        .operatorId(operatorId)
                        .createTime(LocalDateTime.now())
                        .scheduledTime(scheduledTime)
                        .retryCount(0)
                        .maxRetryCount(3)
                        .build();

                smsMessageProducer.sendScheduledSmsMessage(smsMessage, scheduledTime);
                
                log.info("异步发送定时模板短信成功 - 手机号: {}, 模板ID: {}, 定时时间: {}, 消息ID: {}", 
                        phoneNumber, templateId, scheduledTime, messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送定时模板短信失败 - 手机号: {}, 模板ID: {}, 定时时间: {}", 
                        phoneNumber, templateId, scheduledTime, e);
                throw new RuntimeException("发送定时模板短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<String> sendScheduledBatchSmsAsync(List<String> phoneNumbers, String content,
                                                               LocalDateTime scheduledTime,
                                                               Long operatorId, String businessType, String businessId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                
                // 转换业务类型枚举
                BusinessType businessTypeEnum = BusinessType.valueOf(businessType.toUpperCase());
                
                SmsMessage smsMessage = SmsMessage.builder()
                        .messageId(messageId)
                        .messageType(SmsMessageType.BATCH)
                        .sendMode(SendMode.SCHEDULED)
                        .phoneNumbers(phoneNumbers)
                        .content(content)
                        .priority(Priority.NORMAL)
                        .businessType(businessTypeEnum)
                        .businessId(businessId)
                        .operatorId(operatorId)
                        .createTime(LocalDateTime.now())
                        .scheduledTime(scheduledTime)
                        .retryCount(0)
                        .maxRetryCount(3)
                        .build();

                smsMessageProducer.sendScheduledSmsMessage(smsMessage, scheduledTime);
                
                log.info("异步发送定时批量短信成功 - 手机号数量: {}, 定时时间: {}, 消息ID: {}", 
                        phoneNumbers.size(), scheduledTime, messageId);
                return messageId;
            } catch (Exception e) {
                log.error("异步发送定时批量短信失败 - 手机号数量: {}, 定时时间: {}", 
                        phoneNumbers.size(), scheduledTime, e);
                throw new RuntimeException("发送定时批量短信失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<Map<String, Object>> getAsyncSmsStatus(String messageId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 这里应该从数据库或缓存中查询消息状态
                // 目前返回模拟数据
                Map<String, Object> status = new HashMap<>();
                status.put("messageId", messageId);
                status.put("status", "SENT");
                status.put("createTime", LocalDateTime.now());
                status.put("sentTime", LocalDateTime.now());
                
                log.info("查询异步短信状态成功 - 消息ID: {}", messageId);
                return status;
            } catch (Exception e) {
                log.error("查询异步短信状态失败 - 消息ID: {}", messageId, e);
                throw new RuntimeException("查询异步短信状态失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> cancelScheduledSms(String messageId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 这里应该实现取消定时短信的逻辑
                // 可能需要从延迟队列中移除消息或标记为已取消
                log.info("取消定时短信成功 - 消息ID: {}", messageId);
                return true;
            } catch (Exception e) {
                log.error("取消定时短信失败 - 消息ID: {}", messageId, e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> retrySms(String messageId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 这里应该从数据库中查询原始消息信息，然后重新发送
                // 目前返回模拟结果
                log.info("重试发送短信成功 - 消息ID: {}", messageId);
                return true;
            } catch (Exception e) {
                log.error("重试发送短信失败 - 消息ID: {}", messageId, e);
                return false;
            }
        });
    }


}