package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库表：arc_notification
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("arc_notification")
public class Notification extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    /**
     * 接收用户ID（为空表示系统广播通知）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知类型：1-系统通知，2-业务通知，3-警告通知，4-错误通知
     */
    @NotNull(message = "通知类型不能为空")
    @Min(value = 1, message = "通知类型值不正确")
    @Max(value = 4, message = "通知类型值不正确")
    @TableField("notification_type")
    private Integer notificationType;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 200, message = "通知标题长度不能超过200个字符")
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    @Size(max = 2000, message = "通知内容长度不能超过2000个字符")
    @TableField("content")
    private String content;

    /**
     * 通知状态：0-未读，1-已读
     */
    @NotNull(message = "通知状态不能为空")
    @Min(value = 0, message = "通知状态值不正确")
    @Max(value = 1, message = "通知状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 是否需要邮件通知：0-否，1-是
     */
    @TableField("email_notify")
    private Integer emailNotify;

    /**
     * 是否需要短信通知：0-否，1-是
     */
    @TableField("sms_notify")
    private Integer smsNotify;

    /**
     * 是否需要WebSocket推送：0-否，1-是
     */
    @TableField("websocket_notify")
    private Integer websocketNotify;

    /**
     * 关联业务ID（可选）
     */
    @Size(max = 100, message = "关联业务ID长度不能超过100个字符")
    @TableField("business_id")
    private String businessId;

    /**
     * 关联业务类型（可选）
     */
    @Size(max = 50, message = "关联业务类型长度不能超过50个字符")
    @TableField("business_type")
    private String businessType;

    /**
     * 优先级：1-低，2-中，3-高，4-紧急
     */
    @Min(value = 1, message = "优先级值不正确")
    @Max(value = 4, message = "优先级值不正确")
    @TableField("priority")
    private Integer priority;

    /**
     * 过期时间（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 扩展数据（JSON格式）
     */
    @TableField("extra_data")
    private String extraData;

    // ==================== 业务方法 ====================

    /**
     * 判断通知是否已读
     * 
     * @return 是否已读
     */
    public boolean isRead() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断通知是否过期
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        if (this.expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expireTime);
    }

    /**
     * 判断是否为系统广播通知
     * 
     * @return 是否为广播通知
     */
    public boolean isBroadcast() {
        return this.userId == null;
    }

    /**
     * 获取通知类型描述
     * 
     * @return 类型描述
     */
    public String getNotificationTypeDesc() {
        if (this.notificationType == null) {
            return "未知";
        }
        switch (this.notificationType) {
            case 1:
                return "系统通知";
            case 2:
                return "业务通知";
            case 3:
                return "警告通知";
            case 4:
                return "错误通知";
            default:
                return "未知";
        }
    }

    /**
     * 获取优先级描述
     * 
     * @return 优先级描述
     */
    public String getPriorityDesc() {
        if (this.priority == null) {
            return "普通";
        }
        switch (this.priority) {
            case 1:
                return "低";
            case 2:
                return "中";
            case 3:
                return "高";
            case 4:
                return "紧急";
            default:
                return "普通";
        }
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.status = 1;
        this.readTime = LocalDateTime.now();
    }

    /**
     * 检查是否需要邮件通知
     * 
     * @return 是否需要邮件通知
     */
    public boolean needEmailNotify() {
        return this.emailNotify != null && this.emailNotify == 1;
    }

    /**
     * 检查是否需要短信通知
     * 
     * @return 是否需要短信通知
     */
    public boolean needSmsNotify() {
        return this.smsNotify != null && this.smsNotify == 1;
    }

    /**
     * 检查是否需要WebSocket推送
     * 
     * @return 是否需要WebSocket推送
     */
    public boolean needWebsocketNotify() {
        return this.websocketNotify != null && this.websocketNotify == 1;
    }
}