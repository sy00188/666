package com.archive.management.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;

import com.archive.management.entity.SmsMessage;

/**
 * 短信服务接口
 * 负责处理短信发送相关的业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface SmsService {

    /**
     * 发送单条短信
     * 
     * @param phoneNumber 手机号码
     * @param content 短信内容
     * @return 是否发送成功
     */
    boolean sendSms(String phoneNumber, String content);

    /**
     * 发送模板短信
     * 
     * @param phoneNumber 手机号码
     * @param templateCode 模板代码
     * @param templateParams 模板参数
     * @return 是否发送成功
     */
    boolean sendTemplateSms(String phoneNumber, String templateCode, Map<String, String> templateParams);

    /**
     * 批量发送短信
     * 
     * @param phoneNumbers 手机号码列表
     * @param content 短信内容
     * @return 发送成功的数量
     */
    int sendBatchSms(List<String> phoneNumbers, String content);

    /**
     * 发送验证码短信
     * 
     * @param phoneNumber 手机号码
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phoneNumber, String code);

    /**
     * 发送通知短信
     * 
     * @param phoneNumber 手机号码
     * @param title 通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean sendNotificationSms(String phoneNumber, String title, String content);

    /**
     * 检查短信发送状态
     * 
     * @param messageId 消息ID
     * @return 发送状态信息
     */
    Map<String, Object> checkSmsStatus(String messageId);

    /**
     * 获取短信发送统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getSmsStatistics(String startDate, String endDate);

    /**
     * 验证手机号码格式
     * 
     * @param phoneNumber 手机号码
     * @return 是否有效
     */
    boolean validatePhoneNumber(String phoneNumber);

    /**
     * 检查短信发送频率限制
     * 
     * @param phoneNumber 手机号码
     * @param smsType 短信类型
     * @return 是否允许发送
     */
    boolean checkRateLimit(String phoneNumber, String smsType);

    /**
     * 获取短信余额
     * 
     * @return 余额信息
     */
    Map<String, Object> getSmsBalance();

    // ==================== 异步批量发送方法 ====================

    /**
     * 异步发送单条短信
     * 
     * @param phoneNumber 手机号码
     * @param content 短信内容
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含消息ID
     */
    CompletableFuture<String> sendSmsAsync(String phoneNumber, String content, 
                                          Long operatorId, String businessType, String businessId);

    /**
     * 异步发送模板短信
     * 
     * @param phoneNumber 手机号码
     * @param templateId 模板ID
     * @param templateParams 模板参数
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含消息ID
     */
    CompletableFuture<String> sendTemplateSmsAsync(String phoneNumber, String templateId, 
                                                  Map<String, String> templateParams,
                                                  Long operatorId, String businessType, String businessId);

    /**
     * 异步批量发送短信
     * 
     * @param phoneNumbers 手机号码列表
     * @param content 短信内容
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含批量消息ID
     */
    CompletableFuture<String> sendBatchSmsAsync(List<String> phoneNumbers, String content,
                                               Long operatorId, String businessType, String businessId);

    /**
     * 异步发送高优先级短信
     * 
     * @param phoneNumber 手机号码
     * @param content 短信内容
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含消息ID
     */
    CompletableFuture<String> sendHighPrioritySmsAsync(String phoneNumber, String content,
                                                      Long operatorId, String businessType, String businessId);

    /**
     * 异步发送定时短信
     * 
     * @param phoneNumber 手机号码
     * @param content 短信内容
     * @param scheduledTime 定时发送时间
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含消息ID
     */
    CompletableFuture<String> sendScheduledSmsAsync(String phoneNumber, String content, 
                                                   LocalDateTime scheduledTime,
                                                   Long operatorId, String businessType, String businessId);

    /**
     * 异步发送定时模板短信
     * 
     * @param phoneNumber 手机号码
     * @param templateId 模板ID
     * @param templateParams 模板参数
     * @param scheduledTime 定时发送时间
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含消息ID
     */
    CompletableFuture<String> sendScheduledTemplateSmsAsync(String phoneNumber, String templateId,
                                                           Map<String, String> templateParams,
                                                           LocalDateTime scheduledTime,
                                                           Long operatorId, String businessType, String businessId);

    /**
     * 异步批量发送定时短信
     * 
     * @param phoneNumbers 手机号码列表
     * @param content 短信内容
     * @param scheduledTime 定时发送时间
     * @param operatorId 操作员ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 异步结果，包含批量消息ID
     */
    CompletableFuture<String> sendScheduledBatchSmsAsync(List<String> phoneNumbers, String content,
                                                        LocalDateTime scheduledTime,
                                                        Long operatorId, String businessType, String businessId);

    /**
     * 查询异步短信发送状态
     * 
     * @param messageId 消息ID
     * @return 发送状态信息
     */
    CompletableFuture<Map<String, Object>> getAsyncSmsStatus(String messageId);

    /**
     * 取消定时短信
     * 
     * @param messageId 消息ID
     * @return 是否取消成功
     */
    CompletableFuture<Boolean> cancelScheduledSms(String messageId);

    /**
     * 重试失败的短信
     * 
     * @param messageId 消息ID
     * @return 重试结果
     */
    CompletableFuture<Boolean> retrySms(String messageId);
}