package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 密码重置令牌实体类
 * 对应数据库表：sys_password_reset_token
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_password_reset_token")
public class PasswordResetToken extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 令牌ID
     */
    @TableId(value = "token_id", type = IdType.AUTO)
    private Long tokenId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    /**
     * JWT重置令牌
     */
    @NotBlank(message = "重置令牌不能为空")
    @Size(max = 255, message = "重置令牌长度不能超过255个字符")
    @TableField("token")
    private String token;

    /**
     * 令牌哈希值（用于快速查找）
     */
    @NotBlank(message = "令牌哈希值不能为空")
    @Size(max = 64, message = "令牌哈希值长度不能超过64个字符")
    @TableField("token_hash")
    private String tokenHash;

    /**
     * 过期时间
     */
    @NotNull(message = "过期时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 是否已使用
     * 0-未使用，1-已使用
     */
    @NotNull(message = "使用状态不能为空")
    @Min(value = 0, message = "使用状态值不正确")
    @Max(value = 1, message = "使用状态值不正确")
    @TableField("used")
    private Integer used;

    /**
     * 使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("used_time")
    private LocalDateTime usedTime;

    /**
     * 请求IP
     */
    @Size(max = 50, message = "请求IP长度不能超过50个字符")
    @TableField("client_ip")
    private String clientIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @TableField("user_agent")
    private String userAgent;

    // ==================== 非数据库字段 ====================

    /**
     * 用户信息（关联查询时使用）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 用户名（关联查询时使用）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 真实姓名（关联查询时使用）
     */
    @TableField(exist = false)
    private String realName;

    // ==================== 业务方法 ====================

    /**
     * 判断令牌是否已过期
     * 
     * @return true-已过期，false-未过期
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 判断令牌是否已使用
     * 
     * @return true-已使用，false-未使用
     */
    public boolean isUsed() {
        return used != null && used == 1;
    }

    /**
     * 判断令牌是否有效（未过期且未使用）
     * 
     * @return true-有效，false-无效
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /**
     * 标记令牌为已使用
     */
    public void markAsUsed() {
        this.used = 1;
        this.usedTime = LocalDateTime.now();
    }

    /**
     * 获取令牌剩余有效时间（分钟）
     * 
     * @return 剩余分钟数，如果已过期返回0
     */
    public long getRemainingMinutes() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expireTime).toMinutes();
    }
}