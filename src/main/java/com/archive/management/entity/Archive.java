package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("arc_archive")
public class Archive extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 档案ID
     */
    @TableId(value = "archive_id", type = IdType.AUTO)
    private Long archiveId;

    /**
     * 档案编号（唯一）
     */
    @NotBlank(message = "档案编号不能为空")
    @Size(max = 50, message = "档案编号长度不能超过50个字符")
    @TableField("archive_no")
    private String archiveNo;

    /**
     * 档案标题
     */
    @NotBlank(message = "档案标题不能为空")
    @Size(max = 200, message = "档案标题长度不能超过200个字符")
    @TableField("title")
    private String title;

    /**
     * 档案分类ID
     */
    @NotNull(message = "档案分类不能为空")
    @TableField("category_id")
    private Long categoryId;

    /**
     * 密级：1-公开，2-内部，3-机密，4-绝密
     */
    @NotNull(message = "密级不能为空")
    @Min(value = 1, message = "密级值不正确")
    @Max(value = 4, message = "密级值不正确")
    @TableField("security_level")
    private Integer securityLevel;

    /**
     * 关联业务ID
     */
    @Size(max = 100, message = "关联业务ID长度不能超过100个字符")
    @TableField("business_id")
    private String businessId;

    /**
     * 关键词
     */
    @Size(max = 500, message = "关键词长度不能超过500个字符")
    @TableField("keywords")
    private String keywords;

    /**
     * 摘要
     */
    @TableField("abstract")
    private String abstractContent;

    /**
     * 元数据JSON
     */
    @TableField("metadata_json")
    private String metadataJson;

    /**
     * 文件数量
     */
    @Min(value = 0, message = "文件数量不能为负数")
    @TableField("file_count")
    private Integer fileCount;

    /**
     * 总文件大小（字节）
     */
    @Min(value = 0, message = "总文件大小不能为负数")
    @TableField("total_size")
    private Long totalSize;

    /**
     * 保存期限
     */
    @Size(max = 20, message = "保存期限长度不能超过20个字符")
    @TableField("retention_period")
    private String retentionPeriod;

    /**
     * 到期日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("expiry_date")
    private LocalDate expiryDate;

    /**
     * 状态：1-待审核，2-已归档，3-已借出，4-审核驳回，5-已销毁
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值不正确")
    @Max(value = 5, message = "状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 提交人ID
     */
    @NotNull(message = "提交人不能为空")
    @TableField("submit_user_id")
    private Long submitUserId;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
     * 归档人ID
     */
    @TableField("archive_user_id")
    private Long archiveUserId;

    /**
     * 归档时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("archive_time")
    private LocalDateTime archiveTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

    // ========== 非持久化字段 ==========

    /**
     * 分类信息
     */
    @TableField(exist = false)
    private Category category;

    /**
     * 档案文件列表
     */
    @TableField(exist = false)
    private List<ArchiveFile> files;

    /**
     * 关键词列表
     */
    @TableField(exist = false)
    private List<String> keywordList;

    /**
     * 提交人姓名
     */
    @TableField(exist = false)
    private String submitUserName;

    /**
     * 归档人姓名
     */
    @TableField(exist = false)
    private String archiveUserName;

    /**
     * 分类名称
     */
    @TableField(exist = false)
    private String categoryName;

    // ========== 业务方法 ==========

    /**
     * 是否待审核
     */
    public boolean isPendingReview() {
        return status != null && status == 1;
    }

    /**
     * 是否已归档
     */
    public boolean isArchived() {
        return status != null && status == 2;
    }

    /**
     * 是否已借出
     */
    public boolean isBorrowed() {
        return status != null && status == 3;
    }

    /**
     * 是否审核驳回
     */
    public boolean isRejected() {
        return status != null && status == 4;
    }

    /**
     * 是否已销毁
     */
    public boolean isDestroyed() {
        return status != null && status == 5;
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * 获取密级描述
     */
    public String getSecurityLevelDesc() {
        if (securityLevel == null) {
            return "未知";
        }
        switch (securityLevel) {
            case 1: return "公开";
            case 2: return "内部";
            case 3: return "机密";
            case 4: return "绝密";
            default: return "未知";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1: return "待审核";
            case 2: return "已归档";
            case 3: return "已借出";
            case 4: return "审核驳回";
            case 5: return "已销毁";
            default: return "未知";
        }
    }

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedTotalSize() {
        if (totalSize == null || totalSize == 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = totalSize.doubleValue();
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 解析关键词为列表
     */
    public List<String> parseKeywordsToList() {
        if (keywords == null || keywords.trim().isEmpty()) {
            return List.of();
        }
        return List.of(keywords.split("[,，;；\\s]+"));
    }

    /**
     * 设置关键词列表
     */
    public void setKeywordList(List<String> keywordList) {
        this.keywordList = keywordList;
        if (keywordList != null && !keywordList.isEmpty()) {
            this.keywords = String.join(",", keywordList);
        }
    }

    // ========== Getter/Setter for ID compatibility ==========

    /**
     * 获取档案ID（兼容性方法）
     */
    public Long getId() {
        return this.archiveId;
    }

    /**
     * 设置档案ID（兼容性方法）
     */
    public void setId(Long id) {
        this.archiveId = id;
    }
}