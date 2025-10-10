package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 * 映射角色与权限的多对多关系
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@TableName("sys_role_permission")
public class RolePermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 权限ID
     */
    private Long permissionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 更新人ID
     */
    private Long updatedBy;
    
    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer deleted;

    // ==================== 额外的 Setter 方法（用于兼容性） ====================

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}
