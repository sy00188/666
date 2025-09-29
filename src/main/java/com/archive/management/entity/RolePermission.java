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
 * 角色权限关联实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("role_permission")
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限ID
     */
    @NotNull(message = "权限ID不能为空")
    @TableField("permission_id")
    private Long permissionId;

    /**
     * 授权类型：1-直接授权，2-继承授权，3-临时授权
     */
    @NotNull(message = "授权类型不能为空")
    @Min(value = 1, message = "授权类型值不正确")
    @Max(value = 3, message = "授权类型值不正确")
    @TableField("grant_type")
    private Integer grantType;

    /**
     * 权限范围：1-全部，2-部门，3-个人，4-自定义
     */
    @NotNull(message = "权限范围不能为空")
    @Min(value = 1, message = "权限范围值不正确")
    @Max(value = 4, message = "权限范围值不正确")
    @TableField("permission_scope")
    private Integer permissionScope;

    /**
     * 范围值（部门ID、用户ID等，用逗号分隔）
     */
    @Size(max = 1000, message = "范围值长度不能超过1000个字符")
    @TableField("scope_value")
    private String scopeValue;

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
     * 授权原因
     */
    @Size(max = 500, message = "授权原因长度不能超过500个字符")
    @TableField("grant_reason")
    private String grantReason;

    /**
     * 权限条件（JSON格式）
     */
    @TableField("permission_condition")
    private String permissionCondition;

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
     * 角色信息（非数据库字段）
     */
    @TableField(exist = false)
    private Role role;

    /**
     * 权限信息（非数据库字段）
     */
    @TableField(exist = false)
    private Permission permission;

    /**
     * 范围值列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<String> scopeValueList;

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
     * 检查是否为直接授权
     */
    public boolean isDirectGrant() {
        return this.grantType != null && this.grantType == 1;
    }

    /**
     * 检查是否为继承授权
     */
    public boolean isInheritGrant() {
        return this.grantType != null && this.grantType == 2;
    }

    /**
     * 检查是否为临时授权
     */
    public boolean isTemporaryGrant() {
        return this.grantType != null && this.grantType == 3;
    }

    /**
     * 检查权限范围是否为全部
     */
    public boolean isFullScope() {
        return this.permissionScope != null && this.permissionScope == 1;
    }

    /**
     * 检查权限范围是否为部门
     */
    public boolean isDepartmentScope() {
        return this.permissionScope != null && this.permissionScope == 2;
    }

    /**
     * 检查权限范围是否为个人
     */
    public boolean isPersonalScope() {
        return this.permissionScope != null && this.permissionScope == 3;
    }

    /**
     * 检查权限范围是否为自定义
     */
    public boolean isCustomScope() {
        return this.permissionScope != null && this.permissionScope == 4;
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
     * 获取授权类型描述
     */
    public String getGrantTypeDesc() {
        if (this.grantType == null) {
            return "未知";
        }
        switch (this.grantType) {
            case 1:
                return "直接授权";
            case 2:
                return "继承授权";
            case 3:
                return "临时授权";
            default:
                return "未知";
        }
    }

    /**
     * 获取权限范围描述
     */
    public String getPermissionScopeDesc() {
        if (this.permissionScope == null) {
            return "未知";
        }
        switch (this.permissionScope) {
            case 1:
                return "全部";
            case 2:
                return "部门";
            case 3:
                return "个人";
            case 4:
                return "自定义";
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
     * 解析范围值字符串为列表
     */
    public java.util.List<String> parseScopeValueToList() {
        if (this.scopeValue == null || this.scopeValue.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(this.scopeValue.split(","));
    }

    /**
     * 检查指定值是否在权限范围内
     */
    public boolean isInScope(String value) {
        if (this.isFullScope()) {
            return true;
        }
        
        if (this.scopeValue == null || this.scopeValue.trim().isEmpty()) {
            return false;
        }
        
        java.util.List<String> scopeList = this.parseScopeValueToList();
        return scopeList.contains(value);
    }

    /**
     * 添加范围值
     */
    public void addScopeValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        
        java.util.List<String> scopeList = this.parseScopeValueToList();
        if (!scopeList.contains(value)) {
            scopeList.add(value);
            this.scopeValue = String.join(",", scopeList);
        }
    }

    /**
     * 移除范围值
     */
    public void removeScopeValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        
        java.util.List<String> scopeList = this.parseScopeValueToList();
        scopeList.remove(value);
        this.scopeValue = String.join(",", scopeList);
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
     * 暂停权限授权
     */
    public void suspend() {
        this.status = 2;
    }

    /**
     * 恢复权限授权
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