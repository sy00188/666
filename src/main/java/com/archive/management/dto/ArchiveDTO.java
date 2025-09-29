package com.archive.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案数据传输对象
 * 用于API接口的数据传输
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "档案数据传输对象")
public class ArchiveDTO {

    @Schema(description = "档案ID", example = "1")
    public Long id;

    @Schema(description = "档案编号", example = "ARC-2024-001", required = true)
    @NotBlank(message = "档案编号不能为空")
    @Size(min = 5, max = 50, message = "档案编号长度必须在5-50个字符之间")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "档案编号只能包含大写字母、数字和横线")
    private String archiveNumber;

    @Schema(description = "档案标题", example = "重要文件档案", required = true)
    @NotBlank(message = "档案标题不能为空")
    @Size(min = 2, max = 200, message = "档案标题长度必须在2-200个字符之间")
    public String title;

    @Schema(description = "档案副标题", example = "2024年度重要文件")
    @Size(max = 200, message = "档案副标题长度不能超过200个字符")
    private String subtitle;

    @Schema(description = "档案描述", example = "包含2024年度所有重要文件的档案")
    @Size(max = 2000, message = "档案描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "档案摘要", example = "重要文件档案摘要")
    @Size(max = 1000, message = "档案摘要长度不能超过1000个字符")
    private String summary;

    @Schema(description = "档案类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @NotNull(message = "档案类型不能为空")
    @Min(value = 1, message = "档案类型值必须为1-5")
    @Max(value = 5, message = "档案类型值必须为1-5")
    private Integer archiveType;

    @Schema(description = "分类ID", example = "1", required = true)
    @NotNull(message = "分类ID不能为空")
    @Min(value = 1, message = "分类ID必须为正数")
    private Long categoryId;

    @Schema(description = "分类名称", example = "重要文件")
    private String categoryName;

    @Schema(description = "分类路径", example = "文档管理/重要文件")
    private String categoryPath;

    @Schema(description = "档案状态", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @NotNull(message = "档案状态不能为空")
    @Min(value = 1, message = "档案状态值必须为1-5")
    @Max(value = 5, message = "档案状态值必须为1-5")
    private Integer status;

    @Schema(description = "保密级别", example = "1", allowableValues = {"1", "2", "3", "4"})
    @NotNull(message = "保密级别不能为空")
    @Min(value = 1, message = "保密级别值必须为1-4")
    @Max(value = 4, message = "保密级别值必须为1-4")
    private Integer securityLevel;

    @Schema(description = "重要程度", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "重要程度值必须为1-5")
    @Max(value = 5, message = "重要程度值必须为1-5")
    private Integer importance;

    @Schema(description = "紧急程度", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "紧急程度值必须为1-5")
    @Max(value = 5, message = "紧急程度值必须为1-5")
    private Integer urgency;

    @Schema(description = "档案来源", example = "内部创建")
    @Size(max = 100, message = "档案来源长度不能超过100个字符")
    private String source;

    @Schema(description = "档案作者", example = "张三")
    @Size(max = 100, message = "档案作者长度不能超过100个字符")
    private String author;

    @Schema(description = "档案关键词")
    private List<String> keywords;

    @Schema(description = "档案标签")
    private List<String> tags;

    @Schema(description = "档案语言", example = "zh-CN")
    @Size(max = 10, message = "档案语言长度不能超过10个字符")
    private String language;

    @Schema(description = "档案版本", example = "1.0")
    @Size(max = 20, message = "档案版本长度不能超过20个字符")
    private String version;

    @Schema(description = "档案格式", example = "PDF")
    @Size(max = 20, message = "档案格式长度不能超过20个字符")
    private String format;

    @Schema(description = "档案大小(字节)", example = "1024000")
    @Min(value = 0, message = "档案大小不能为负数")
    private Long fileSize;

    @Schema(description = "档案页数", example = "10")
    @Min(value = 0, message = "档案页数不能为负数")
    private Integer pageCount;

    @Schema(description = "档案文件数量", example = "5")
    @Min(value = 0, message = "档案文件数量不能为负数")
    private Integer fileCount;

    @Schema(description = "档案评分", example = "4.5")
    @DecimalMin(value = "0.0", message = "档案评分不能小于0")
    @DecimalMax(value = "5.0", message = "档案评分不能大于5")
    private BigDecimal rating;

    @Schema(description = "评分次数", example = "10")
    @Min(value = 0, message = "评分次数不能为负数")
    private Integer ratingCount;

    @Schema(description = "浏览次数", example = "100")
    @Min(value = 0, message = "浏览次数不能为负数")
    private Long viewCount;

    @Schema(description = "下载次数", example = "50")
    @Min(value = 0, message = "下载次数不能为负数")
    private Long downloadCount;

    @Schema(description = "收藏次数", example = "20")
    @Min(value = 0, message = "收藏次数不能为负数")
    private Long favoriteCount;

    @Schema(description = "分享次数", example = "15")
    @Min(value = 0, message = "分享次数不能为负数")
    private Long shareCount;

    @Schema(description = "评论次数", example = "8")
    @Min(value = 0, message = "评论次数不能为负数")
    private Long commentCount;

    @Schema(description = "档案日期", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate archiveDate;

    @Schema(description = "生效日期", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期", example = "2025-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Schema(description = "保存期限(年)", example = "10")
    @Min(value = 0, message = "保存期限不能为负数")
    private Integer retentionPeriod;

    @Schema(description = "是否永久保存", example = "false")
    private Boolean permanentRetention;

    @Schema(description = "存储位置", example = "服务器A/档案库/2024")
    @Size(max = 200, message = "存储位置长度不能超过200个字符")
    private String storageLocation;

    @Schema(description = "物理位置", example = "档案室A-001")
    @Size(max = 100, message = "物理位置长度不能超过100个字符")
    private String physicalLocation;

    @Schema(description = "是否已归档", example = "true")
    private Boolean archived;

    @Schema(description = "归档时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archivedTime;

    @Schema(description = "归档人ID", example = "1")
    private Long archivedBy;

    @Schema(description = "归档人姓名", example = "张三")
    private String archivedByName;

    @Schema(description = "是否已发布", example = "true")
    private Boolean published;

    @Schema(description = "发布时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedTime;

    @Schema(description = "发布人ID", example = "1")
    private Long publishedBy;

    @Schema(description = "发布人姓名", example = "张三")
    private String publishedByName;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "是否公开", example = "false")
    private Boolean publicAccess;

    @Schema(description = "访问权限配置")
    private String accessPermissions;

    @Schema(description = "编辑权限配置")
    private String editPermissions;

    @Schema(description = "下载权限配置")
    private String downloadPermissions;

    @Schema(description = "备注", example = "重要档案，请妥善保管")
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建人姓名", example = "系统管理员")
    private String createdByName;

    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "更新人ID", example = "1")
    private Long updatedBy;

    @Schema(description = "更新人姓名", example = "系统管理员")
    private String updatedByName;

    @Schema(description = "版本号", example = "1")
    @Min(value = 0, message = "版本号不能为负数")
    private Integer versionNumber;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "档案文件列表")
    private List<ArchiveFileDTO> files;

    @Schema(description = "相关档案列表")
    private List<ArchiveDTO> relatedArchives;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    /**
     * 获取档案类型显示文本
     */
    @Schema(description = "档案类型显示文本")
    public String getArchiveTypeText() {
        if (archiveType == null) {
            return "未知";
        }
        switch (archiveType) {
            case 1:
                return "文档档案";
            case 2:
                return "图片档案";
            case 3:
                return "音频档案";
            case 4:
                return "视频档案";
            case 5:
                return "其他档案";
            default:
                return "未知";
        }
    }

    /**
     * 获取档案状态显示文本
     */
    @Schema(description = "档案状态显示文本")
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "草稿";
            case 2:
                return "待审核";
            case 3:
                return "已发布";
            case 4:
                return "已归档";
            case 5:
                return "已下架";
            default:
                return "未知";
        }
    }

    /**
     * 获取保密级别显示文本
     */
    @Schema(description = "保密级别显示文本")
    public String getSecurityLevelText() {
        if (securityLevel == null) {
            return "未设置";
        }
        switch (securityLevel) {
            case 1:
                return "公开";
            case 2:
                return "内部";
            case 3:
                return "机密";
            case 4:
                return "绝密";
            default:
                return "未设置";
        }
    }

    /**
     * 获取重要程度显示文本
     */
    @Schema(description = "重要程度显示文本")
    public String getImportanceText() {
        if (importance == null) {
            return "未设置";
        }
        switch (importance) {
            case 1:
                return "很低";
            case 2:
                return "低";
            case 3:
                return "中等";
            case 4:
                return "高";
            case 5:
                return "很高";
            default:
                return "未设置";
        }
    }

    /**
     * 获取紧急程度显示文本
     */
    @Schema(description = "紧急程度显示文本")
    public String getUrgencyText() {
        if (urgency == null) {
            return "未设置";
        }
        switch (urgency) {
            case 1:
                return "很低";
            case 2:
                return "低";
            case 3:
                return "中等";
            case 4:
                return "高";
            case 5:
                return "很高";
            default:
                return "未设置";
        }
    }

    /**
     * 判断档案是否可用
     */
    @Schema(description = "档案是否可用")
    public boolean isAvailable() {
        return enabled != null && enabled && (deleted == null || !deleted);
    }

    /**
     * 判断档案是否已过期
     */
    @Schema(description = "档案是否已过期")
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * 判断档案是否在有效期内
     */
    @Schema(description = "档案是否在有效期内")
    public boolean isEffective() {
        LocalDate now = LocalDate.now();
        boolean afterEffective = effectiveDate == null || !effectiveDate.isAfter(now);
        boolean beforeExpiry = expiryDate == null || !expiryDate.isBefore(now);
        return afterEffective && beforeExpiry;
    }

    /**
     * 判断档案是否可以编辑
     */
    @Schema(description = "档案是否可以编辑")
    public boolean isEditable() {
        return status != null && (status == 1 || status == 2); // 草稿或待审核状态可编辑
    }

    /**
     * 判断档案是否可以删除
     */
    @Schema(description = "档案是否可以删除")
    public boolean isDeletable() {
        return status != null && status != 4; // 已归档状态不可删除
    }

    /**
     * 判断档案是否可以下载
     */
    @Schema(description = "档案是否可以下载")
    public boolean isDownloadable() {
        return isAvailable() && (status == 3 || status == 4); // 已发布或已归档状态可下载
    }

    /**
     * 获取格式化的文件大小
     */
    @Schema(description = "格式化的文件大小")
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取平均评分
     */
    @Schema(description = "平均评分")
    public BigDecimal getAverageRating() {
        if (rating == null || ratingCount == null || ratingCount == 0) {
            return BigDecimal.ZERO;
        }
        return rating;
    }
}