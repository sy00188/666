package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @TableField("role_id")
    private Long roleId;

    /**
     * 分配类型：1-直接分配，2-继承分配，3-临时分配
     */
    @NotNull(message = "分配类型不能为空")
    @Min(value = 1, message = "分配类型值不正确")
    @Max(value = 3, message = "分配类型值不正确")
    @TableField("assign_type")
    private Integer assignType;

    /**
     * 生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 状态：1-正常，2-暂停，3-过期
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值不正确")
    @Max(value = 3, message = "状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 分配原因
     */
    @Size(max = 500, message = "分配原因长度不能超过500个字符")
    @TableField("assign_reason")
    private String assignReason;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    /**
     * 创建者ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;

    // ==================== 非数据库字段 ====================

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 角色信息（非数据库字段）
     */
    @TableField(exist = false)
    private Role role;

    /**
     * 创建者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String createByName;

    /**
     * 更新者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String updateByName;

    // ==================== 业务方法 ====================

    /**
     * 检查是否为直接分配
     */
    public boolean isDirectAssign() {
        return this.assignType != null && this.assignType == 1;
    }

    /**
     * 检查是否为继承分配
     */
    public boolean isInheritAssign() {
        return this.assignType != null && this.assignType == 2;
    }

    /**
     * 检查是否为临时分配
     */
    public boolean isTemporaryAssign() {
        return this.assignType != null && this.assignType == 3;
    }

    /**
     * 检查状态是否正常
     */
    public boolean isNormal() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查是否暂停
     */
    public boolean isSuspended() {
        return this.status != null && this.status == 2;
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return this.status != null && this.status == 3;
    }

    /**
     * 检查是否在有效期内
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查生效时间
        if (this.effectiveTime != null && now.isBefore(this.effectiveTime)) {
            return false;
        }
        
        // 检查过期时间
        if (this.expireTime != null && now.isAfter(this.expireTime)) {
            return false;
        }
        
        return this.isNormal();
    }

    /**
     * 获取分配类型描述
     */
    public String getAssignTypeDesc() {
        if (this.assignType == null) {
            return "未知";
        }
        switch (this.assignType) {
            case 1:
                return "直接分配";
            case 2:
                return "继承分配";
            case 3:
                return "临时分配";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (this.status == null) {
            return "未知";
        }
        switch (this.status) {
            case 1:
                return "正常";
            case 2:
                return "暂停";
            case 3:
                return "过期";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否即将过期（7天内）
     */
    public boolean isExpiringSoon() {
        if (this.expireTime == null) {
            return false;
        }
        LocalDateTime sevenDaysLater = LocalDateTime.now().plusDays(7);
        return this.expireTime.isBefore(sevenDaysLater);
    }

    /**
     * 获取剩余有效天数
     */
    public long getRemainingDays() {
        if (this.expireTime == null) {
            return -1; // 永久有效
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.expireTime)) {
            return 0; // 已过期
        }
        return java.time.Duration.between(now, this.expireTime).toDays();
    }

    /**
     * 延长有效期
     */
    public void extendExpireTime(int days) {
        if (this.expireTime == null) {
            this.expireTime = LocalDateTime.now().plusDays(days);
        } else {
            this.expireTime = this.expireTime.plusDays(days);
        }
    }

    /**
     * 暂停角色分配
     */
    public void suspend() {
        this.status = 2;
    }

    /**
     * 恢复角色分配
     */
    public void resume() {
        this.status = 1;
    }

    /**
     * 标记为过期
     */
    public void markExpired() {
        this.status = 3;
    }
}