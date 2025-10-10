package com.archive.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * SMS消息实体类
 * 用于RabbitMQ队列中的SMS消息传输
 * 
 * @author Archive Management System
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 消息类型：SINGLE(单条发送), BATCH(批量发送), TEMPLATE(模板发送)
     */
    private SmsMessageType messageType;

    /**
     * 发送方式：IMMEDIATE(立即发送), SCHEDULED(定时发送)
     */
    private SendMode sendMode;

    /**
     * 单个手机号（单条发送时使用）
     */
    private String phoneNumber;

    /**
     * 多个手机号（批量发送时使用）
     */
    private List<String> phoneNumbers;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 短信模板ID（模板发送时使用）
     */
    private String templateId;

    /**
     * 模板参数（模板发送时使用）
     */
    private Map<String, Object> templateParams;

    /**
     * 发送优先级：HIGH(高), NORMAL(普通), LOW(低)
     */
    private Priority priority;

    /**
     * 业务类型：NOTIFICATION(通知), VERIFICATION(验证码), MARKETING(营销), SYSTEM(系统)
     */
    private BusinessType businessType;

    /**
     * 业务关联ID（如用户ID、订单ID等）
     */
    private String businessId;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员名称
     */
    private String operatorName;

    /**
     * 消息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 计划发送时间（定时发送时使用）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 消息来源系统
     */
    private String sourceSystem;

    /**
     * 消息版本
     */
    private String version;

    /**
     * SMS消息类型枚举
     */
    public enum SmsMessageType {
        SINGLE("单条发送"),
        BATCH("批量发送"),
        TEMPLATE("模板发送");

        private final String description;

        SmsMessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 发送模式枚举
     */
    public enum SendMode {
        IMMEDIATE("立即发送"),
        SCHEDULED("定时发送");

        private final String description;

        SendMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        HIGH(1, "高优先级"),
        NORMAL(2, "普通优先级"),
        LOW(3, "低优先级");

        private final int level;
        private final String description;

        Priority(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 业务类型枚举
     */
    public enum BusinessType {
        NOTIFICATION("通知类"),
        VERIFICATION("验证码"),
        MARKETING("营销类"),
        SYSTEM("系统类");

        private final String description;

        BusinessType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 获取接收者数量
     */
    public int getRecipientCount() {
        if (messageType == SmsMessageType.SINGLE) {
            return phoneNumber != null ? 1 : 0;
        } else if (messageType == SmsMessageType.BATCH) {
            return phoneNumbers != null ? phoneNumbers.size() : 0;
        }
        return 0;
    }

    /**
     * 检查是否为批量消息
     */
    public boolean isBatchMessage() {
        return messageType == SmsMessageType.BATCH && phoneNumbers != null && phoneNumbers.size() > 1;
    }

    /**
     * 检查是否为定时消息
     */
    public boolean isScheduledMessage() {
        return sendMode == SendMode.SCHEDULED && scheduledTime != null;
    }

    /**
     * 检查是否需要重试
     */
    public boolean needsRetry() {
        return retryCount != null && maxRetryCount != null && retryCount < maxRetryCount;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        if (retryCount == null) {
            retryCount = 0;
        }
        retryCount++;
    }

    /**
     * 验证消息有效性
     */
    public boolean isValid() {
        // 基本字段验证
        if (messageId == null || messageType == null || sendMode == null) {
            return false;
        }

        // 根据消息类型验证
        switch (messageType) {
            case SINGLE:
                return phoneNumber != null && !phoneNumber.trim().isEmpty() && 
                       content != null && !content.trim().isEmpty();
            case BATCH:
                return phoneNumbers != null && !phoneNumbers.isEmpty() && 
                       content != null && !content.trim().isEmpty();
            case TEMPLATE:
                return templateId != null && !templateId.trim().isEmpty() &&
                       ((phoneNumber != null && !phoneNumber.trim().isEmpty()) || 
                        (phoneNumbers != null && !phoneNumbers.isEmpty()));
            default:
                return false;
        }
    }

    // 手动添加getter方法以解决Lombok编译问题
    public String getMessageId() {
        return messageId;
    }

    public SendMode getSendMode() {
        return sendMode;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public String getContent() {
        return content;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public SmsMessageType getMessageType() {
        return messageType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Map<String, Object> getTemplateParams() {
        return templateParams;
    }

    public Priority getPriority() {
        return priority;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Map<String, Object> getExtendedProperties() {
        return extendedProperties;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getVersion() {
        return version;
    }

    // 手动添加setter方法
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}