package com.archive.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.archive.management.entity.User;

/**
 * 部门实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "department", indexes = {
    @Index(name = "idx_department_code", columnList = "code", unique = true),
    @Index(name = "idx_department_name", columnList = "name"),
    @Index(name = "idx_department_parent_id", columnList = "parent_id"),
    @Index(name = "idx_department_status", columnList = "status"),
    @Index(name = "idx_department_level", columnList = "level")
})
@SQLDelete(sql = "UPDATE department SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 部门编码
     */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "部门编码只能包含大写字母、数字和下划线")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 父部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 部门描述
     */
    @Size(max = 500, message = "部门描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 负责人ID
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * 联系人
     */
    @Size(max = 50, message = "联系人长度不能超过50个字符")
    @Column(name = "contact", length = 50)
    private String contact;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$", message = "联系电话格式不正确")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 部门地址
     */
    @Size(max = 200, message = "部门地址长度不能超过200个字符")
    @Column(name = "address", length = 200)
    private String address;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能为负数")
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 部门状态 (0: 禁用, 1: 启用)
     */
    @NotNull(message = "部门状态不能为空")
    @Min(value = 0, message = "部门状态值不正确")
    @Max(value = 1, message = "部门状态值不正确")
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 部门层级
     */
    @Min(value = 1, message = "部门层级不能小于1")
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /**
     * 部门类型
     */
    @Size(max = 20, message = "部门类型长度不能超过20个字符")
    @Column(name = "type", length = 20)
    private String type;

    /**
     * 部门路径
     */
    @Size(max = 500, message = "部门路径长度不能超过500个字符")
    @Column(name = "path", length = 500)
    private String path;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 创建人ID
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 是否删除 (0: 未删除, 1: 已删除)
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 删除人ID
     */
    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 删除时间
     */
    @Column(name = "delete_time")
    private LocalDateTime deleteTime;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    // 非持久化字段

    /**
     * 父部门名称
     */
    @Transient
    private String parentName;

    /**
     * 负责人信息
     */
    @Transient
    private User manager;

    /**
     * 子部门列表
     */
    @Transient
    private List<Department> children;

    /**
     * 用户数量
     */
    @Transient
    private Long userCount;

    /**
     * 创建人信息
     */
    @Transient
    private User creator;

    /**
     * 更新人信息
     */
    @Transient
    private User updater;

    // 业务方法

    /**
     * 是否为根部门
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否禁用
     */
    public boolean isDisabled() {
        return this.status != null && this.status == 0;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return this.name + " (" + this.code + ")";
    }

    /**
     * 构建部门路径
     */
    public void buildPath(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            this.path = "/" + this.code;
        } else {
            this.path = parentPath + "/" + this.code;
        }
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        this.deleted = true;
        this.deletedBy = deletedBy;
        this.deleteTime = LocalDateTime.now();
    }

    /**
     * 恢复删除的部门
     */
    public void restore() {
        this.deleted = false;
        this.deletedBy = null;
        this.deleteTime = null;
    }

    // 手动添加缺失的setter方法
    public void setSort(Integer sort) {
        this.sortOrder = sort;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    // 添加缺失的setter方法
    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}