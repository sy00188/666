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
import java.util.List;
import java.util.ArrayList;

/**
 * 档案分类实体类
 * 对应数据库表：arc_category
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("arc_category")
public class Category extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /**
     * 父分类ID（0表示顶级分类）
     */
    @NotNull(message = "父分类ID不能为空")
    @Min(value = 0, message = "父分类ID不能为负数")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 100, message = "分类名称长度必须在2-100个字符之间")
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类编码（唯一）
     */
    @NotBlank(message = "分类编码不能为空")
    @Size(min = 2, max = 50, message = "分类编码长度必须在2-50个字符之间")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "分类编码只能包含字母、数字、下划线和连字符")
    @TableField("category_code")
    private String categoryCode;

    /**
     * 分类层级（从1开始）
     */
    @NotNull(message = "分类层级不能为空")
    @Min(value = 1, message = "分类层级必须大于0")
    @Max(value = 10, message = "分类层级不能超过10级")
    @TableField("category_level")
    private Integer categoryLevel;

    /**
     * 默认密级：1-公开，2-内部，3-机密，4-绝密
     */
    @NotNull(message = "默认密级不能为空")
    @Min(value = 1, message = "默认密级值不正确")
    @Max(value = 4, message = "默认密级值不正确")
    @TableField("default_security_level")
    private Integer defaultSecurityLevel;

    /**
     * 保管期限（年）
     */
    @Min(value = 1, message = "保管期限必须大于0")
    @TableField("retention_period")
    private Integer retentionPeriod;

    /**
     * 业务类型
     */
    @Size(max = 50, message = "业务类型长度不能超过50个字符")
    @TableField("business_type")
    private String businessType;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能为负数")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：1-启用，0-禁用
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

    // ==================== 非持久化字段 ====================

    /**
     * 父分类对象
     */
    @TableField(exist = false)
    private Category parent;

    /**
     * 子分类列表
     */
    @TableField(exist = false)
    private List<Category> children;

    /**
     * 分类路径（用于显示层级关系）
     */
    @TableField(exist = false)
    private String path;

    /**
     * 完整名称（包含父级名称）
     */
    @TableField(exist = false)
    private String fullName;

    /**
     * 档案数量
     */
    @TableField(exist = false)
    private Long archiveCount;

    /**
     * 子分类数量
     */
    @TableField(exist = false)
    private Integer childCount;

    // ==================== 业务方法 ====================

    /**
     * 是否为顶级分类
     */
    public boolean isTopLevel() {
        return this.parentId != null && this.parentId.equals(0L);
    }

    /**
     * 是否为叶子节点（没有子分类）
     */
    public boolean isLeaf() {
        return this.children == null || this.children.isEmpty();
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status.equals(1);
    }

    /**
     * 是否禁用
     */
    public boolean isDisabled() {
        return this.status != null && this.status.equals(0);
    }

    /**
     * 获取分类层级路径
     */
    public String getCategoryPath() {
        if (this.path != null) {
            return this.path;
        }
        
        if (this.parent != null) {
            if (this.parent.getCategoryPath() != null) {
                this.path = this.parent.getCategoryPath() + "/" + this.categoryCode;
            } else {
                this.path = this.parent.categoryCode + "/" + this.categoryCode;
            }
        } else {
            this.path = this.categoryCode;
        }
        
        return this.path;
    }

    /**
     * 获取完整分类名称
     */
    public String getFullCategoryName() {
        if (this.fullName != null) {
            return this.fullName;
        }
        
        if (this.parent != null) {
            if (this.parent.getFullCategoryName() != null) {
                this.fullName = this.parent.getFullCategoryName() + " > " + this.categoryName;
            } else {
                this.fullName = this.parent.categoryName + " > " + this.categoryName;
            }
        } else {
            this.fullName = this.categoryName;
        }
        
        return this.fullName;
    }

    /**
     * 添加子分类
     */
    public void addChild(Category child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * 移除子分类
     */
    public void removeChild(Category child) {
        if (this.children != null) {
            this.children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * 获取所有祖先分类ID
     */
    public List<Long> getAncestorIds() {
        List<Long> ancestorIds = new ArrayList<>();
        Category current = this.getParent();
        while (current != null) {
            ancestorIds.add(current.getCategoryId());
            current = current.getParent();
        }
        return ancestorIds;
    }

    /**
     * 获取所有后代分类ID
     */
    public List<Long> getDescendantIds() {
        List<Long> descendantIds = new ArrayList<>();
        if (this.getChildren() != null) {
            for (Category child : this.getChildren()) {
                descendantIds.add(child.getCategoryId());
                descendantIds.addAll(child.getDescendantIds());
            }
        }
        return descendantIds;
    }

    /**
     * 判断是否可以删除
     */
    public boolean canDelete() {
        // 有子分类时不能删除
        return this.children == null || this.children.isEmpty();
    }

    /**
     * 判断是否可以移动到指定父分类
     */
    public boolean canMoveTo(Long newParentId) {
        // 不能移动到自己或自己的子分类下
        if (newParentId == null) {
            return true;
        }
        if (newParentId.equals(this.categoryId)) {
            return false;
        }
        List<Long> descendantIds = getDescendantIds();
        return !descendantIds.contains(newParentId);
    }

    /**
     * 重置计算字段
     */
    public void resetCalculatedFields() {
        this.parent = null;
        this.children = null;
        this.path = null;
        this.fullName = null;
        this.archiveCount = null;
        this.childCount = null;
    }

    // 手动添加必要的getter和setter方法
    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Integer getCategoryLevel() {
        return categoryLevel;
    }

    public void setCategoryLevel(Integer categoryLevel) {
        this.categoryLevel = categoryLevel;
    }

    public Integer getDefaultSecurityLevel() {
        return defaultSecurityLevel;
    }

    public void setDefaultSecurityLevel(Integer defaultSecurityLevel) {
        this.defaultSecurityLevel = defaultSecurityLevel;
    }

    public Integer getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(Integer retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getArchiveCount() {
        return archiveCount;
    }

    public void setArchiveCount(Long archiveCount) {
        this.archiveCount = archiveCount;
    }

    public Integer getChildCount() {
        return childCount;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }

    /**
     * 获取密级描述
     */
    public String getSecurityLevelDesc() {
        if (defaultSecurityLevel == null) {
            return "未设置";
        }
        switch (defaultSecurityLevel) {
            case 1: return "公开";
            case 2: return "内部";
            case 3: return "秘密";
            case 4: return "机密";
            default: return "未知";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        return status != null && status == 1 ? "启用" : "禁用";
    }
}