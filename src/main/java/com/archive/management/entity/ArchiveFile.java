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
 * 档案文件实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("archive_file")
public class ArchiveFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 档案ID
     */
    @NotNull(message = "档案ID不能为空")
    @TableField("archive_id")
    private Long archiveId;

    /**
     * 文件名称
     */
    @NotBlank(message = "文件名称不能为空")
    @Size(min = 1, max = 255, message = "文件名称长度必须在1-255个字符之间")
    @TableField("file_name")
    private String fileName;

    /**
     * 文件原始名称
     */
    @NotBlank(message = "文件原始名称不能为空")
    @Size(min = 1, max = 255, message = "文件原始名称长度必须在1-255个字符之间")
    @TableField("original_name")
    private String originalName;

    /**
     * 文件扩展名
     */
    @Size(max = 20, message = "文件扩展名长度不能超过20个字符")
    @TableField("file_extension")
    private String fileExtension;

    /**
     * 文件MIME类型
     */
    @Size(max = 100, message = "文件MIME类型长度不能超过100个字符")
    @TableField("mime_type")
    private String mimeType;

    /**
     * 文件大小（字节）
     */
    @NotNull(message = "文件大小不能为空")
    @Min(value = 0, message = "文件大小不能为负数")
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件MD5值
     */
    @Size(max = 32, message = "文件MD5值长度不能超过32个字符")
    @TableField("file_md5")
    private String fileMd5;

    /**
     * 文件SHA1值
     */
    @Size(max = 40, message = "文件SHA1值长度不能超过40个字符")
    @TableField("file_sha1")
    private String fileSha1;

    /**
     * 文件存储路径
     */
    @NotBlank(message = "文件存储路径不能为空")
    @Size(min = 1, max = 500, message = "文件存储路径长度必须在1-500个字符之间")
    @TableField("storage_path")
    private String storagePath;

    /**
     * 文件相对路径
     */
    @Size(max = 500, message = "文件相对路径长度不能超过500个字符")
    @TableField("relative_path")
    private String relativePath;

    /**
     * 文件访问URL
     */
    @Size(max = 1000, message = "文件访问URL长度不能超过1000个字符")
    @TableField("access_url")
    private String accessUrl;

    /**
     * 文件缩略图路径
     */
    @Size(max = 500, message = "文件缩略图路径长度不能超过500个字符")
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 文件缩略图URL
     */
    @Size(max = 1000, message = "文件缩略图URL长度不能超过1000个字符")
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 文件类型：1-主文件，2-附件，3-缩略图，4-预览图
     */
    @NotNull(message = "文件类型不能为空")
    @Min(value = 1, message = "文件类型值不正确")
    @Max(value = 4, message = "文件类型值不正确")
    @TableField("file_type")
    private Integer fileType;

    /**
     * 文件状态：1-正常，2-损坏，3-丢失，4-已删除
     */
    @NotNull(message = "文件状态不能为空")
    @Min(value = 1, message = "文件状态值不正确")
    @Max(value = 4, message = "文件状态值不正确")
    @TableField("status")
    private Integer status;

    /**
     * 存储类型：1-本地存储，2-阿里云OSS，3-腾讯云COS，4-七牛云，5-AWS S3
     */
    @NotNull(message = "存储类型不能为空")
    @Min(value = 1, message = "存储类型值不正确")
    @Max(value = 5, message = "存储类型值不正确")
    @TableField("storage_type")
    private Integer storageType;

    /**
     * 存储桶名称
     */
    @Size(max = 100, message = "存储桶名称长度不能超过100个字符")
    @TableField("bucket_name")
    private String bucketName;

    /**
     * 存储区域
     */
    @Size(max = 50, message = "存储区域长度不能超过50个字符")
    @TableField("storage_region")
    private String storageRegion;

    /**
     * 文件宽度（像素，用于图片和视频）
     */
    @Min(value = 0, message = "文件宽度不能为负数")
    @TableField("width")
    private Integer width;

    /**
     * 文件高度（像素，用于图片和视频）
     */
    @Min(value = 0, message = "文件高度不能为负数")
    @TableField("height")
    private Integer height;

    /**
     * 文件时长（秒，用于音频和视频）
     */
    @Min(value = 0, message = "文件时长不能为负数")
    @TableField("duration")
    private Integer duration;

    /**
     * 文件比特率（用于音频和视频）
     */
    @Min(value = 0, message = "文件比特率不能为负数")
    @TableField("bitrate")
    private Integer bitrate;

    /**
     * 文件帧率（用于视频）
     */
    @Min(value = 0, message = "文件帧率不能为负数")
    @TableField("frame_rate")
    private Integer frameRate;

    /**
     * 文件编码格式
     */
    @Size(max = 50, message = "文件编码格式长度不能超过50个字符")
    @TableField("encoding")
    private String encoding;

    /**
     * 文件压缩率
     */
    @DecimalMin(value = "0.0", message = "文件压缩率不能为负数")
    @DecimalMax(value = "100.0", message = "文件压缩率不能超过100")
    @TableField("compression_ratio")
    private java.math.BigDecimal compressionRatio;

    /**
     * 文件质量评分（1-10分）
     */
    @DecimalMin(value = "1.0", message = "文件质量评分不能小于1.0")
    @DecimalMax(value = "10.0", message = "文件质量评分不能大于10.0")
    @TableField("quality_score")
    private java.math.BigDecimal qualityScore;

    /**
     * 文件下载次数
     */
    @Min(value = 0, message = "文件下载次数不能为负数")
    @TableField("download_count")
    private Long downloadCount;

    /**
     * 文件访问次数
     */
    @Min(value = 0, message = "文件访问次数不能为负数")
    @TableField("access_count")
    private Long accessCount;

    /**
     * 最后访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_access_time")
    private LocalDateTime lastAccessTime;

    /**
     * 文件上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 文件过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 是否公开访问：0-否，1-是
     */
    @TableField("is_public")
    private Integer isPublic;

    /**
     * 是否允许下载：0-否，1-是
     */
    @TableField("allow_download")
    private Integer allowDownload;

    /**
     * 是否需要水印：0-否，1-是
     */
    @TableField("need_watermark")
    private Integer needWatermark;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能为负数")
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 文件描述
     */
    @Size(max = 1000, message = "文件描述长度不能超过1000个字符")
    @TableField("description")
    private String description;

    /**
     * 文件标签（用逗号分隔）
     */
    @Size(max = 500, message = "文件标签长度不能超过500个字符")
    @TableField("tags")
    private String tags;

    /**
     * 文件元数据（JSON格式）
     */
    @TableField("metadata")
    private String metadata;

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
     * 档案信息（非数据库字段）
     */
    @TableField(exist = false)
    private Archive archive;

    /**
     * 文件标签列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<String> tagList;

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
     * 检查是否为主文件
     */
    public boolean isMainFile() {
        return this.fileType != null && this.fileType == 1;
    }

    /**
     * 检查是否为附件
     */
    public boolean isAttachment() {
        return this.fileType != null && this.fileType == 2;
    }

    /**
     * 检查是否为缩略图
     */
    public boolean isThumbnail() {
        return this.fileType != null && this.fileType == 3;
    }

    /**
     * 检查是否为预览图
     */
    public boolean isPreview() {
        return this.fileType != null && this.fileType == 4;
    }

    /**
     * 检查文件状态是否正常
     */
    public boolean isNormal() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查文件是否损坏
     */
    public boolean isDamaged() {
        return this.status != null && this.status == 2;
    }

    /**
     * 检查文件是否丢失
     */
    public boolean isMissing() {
        return this.status != null && this.status == 3;
    }

    /**
     * 检查文件是否已删除
     */
    public boolean isDeleted() {
        return this.status != null && this.status == 4;
    }

    /**
     * 检查是否为本地存储
     */
    public boolean isLocalStorage() {
        return this.storageType != null && this.storageType == 1;
    }

    /**
     * 检查是否为云存储
     */
    public boolean isCloudStorage() {
        return this.storageType != null && this.storageType > 1;
    }

    /**
     * 检查是否公开访问
     */
    public boolean isPublic() {
        return this.isPublic != null && this.isPublic == 1;
    }

    /**
     * 检查是否允许下载
     */
    public boolean isAllowDownload() {
        return this.allowDownload != null && this.allowDownload == 1;
    }

    /**
     * 检查是否需要水印
     */
    public boolean isNeedWatermark() {
        return this.needWatermark != null && this.needWatermark == 1;
    }

    /**
     * 检查文件是否过期
     */
    public boolean isExpired() {
        return this.expireTime != null && this.expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 检查是否为图片文件
     */
    public boolean isImageFile() {
        if (this.mimeType == null) {
            return false;
        }
        return this.mimeType.startsWith("image/");
    }

    /**
     * 检查是否为音频文件
     */
    public boolean isAudioFile() {
        if (this.mimeType == null) {
            return false;
        }
        return this.mimeType.startsWith("audio/");
    }

    /**
     * 检查是否为视频文件
     */
    public boolean isVideoFile() {
        if (this.mimeType == null) {
            return false;
        }
        return this.mimeType.startsWith("video/");
    }

    /**
     * 检查是否为文档文件
     */
    public boolean isDocumentFile() {
        if (this.mimeType == null) {
            return false;
        }
        return this.mimeType.startsWith("application/") || this.mimeType.startsWith("text/");
    }

    /**
     * 获取文件类型描述
     */
    public String getFileTypeDesc() {
        if (this.fileType == null) {
            return "未知";
        }
        switch (this.fileType) {
            case 1:
                return "主文件";
            case 2:
                return "附件";
            case 3:
                return "缩略图";
            case 4:
                return "预览图";
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
                return "损坏";
            case 3:
                return "丢失";
            case 4:
                return "已删除";
            default:
                return "未知";
        }
    }

    /**
     * 获取存储类型描述
     */
    public String getStorageTypeDesc() {
        if (this.storageType == null) {
            return "未知";
        }
        switch (this.storageType) {
            case 1:
                return "本地存储";
            case 2:
                return "阿里云OSS";
            case 3:
                return "腾讯云COS";
            case 4:
                return "七牛云";
            case 5:
                return "AWS S3";
            default:
                return "未知";
        }
    }

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (this.fileSize == null || this.fileSize == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = this.fileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取格式化的时长
     */
    public String getFormattedDuration() {
        if (this.duration == null || this.duration == 0) {
            return "00:00:00";
        }
        
        int hours = this.duration / 3600;
        int minutes = (this.duration % 3600) / 60;
        int seconds = this.duration % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 获取文件分辨率字符串
     */
    public String getResolution() {
        if (this.width == null || this.height == null) {
            return null;
        }
        return this.width + "x" + this.height;
    }

    /**
     * 增加下载次数
     */
    public void increaseDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    /**
     * 增加访问次数
     */
    public void increaseAccessCount() {
        this.accessCount = (this.accessCount == null ? 0 : this.accessCount) + 1;
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 解析标签字符串为列表
     */
    public java.util.List<String> parseTagsToList() {
        if (this.tags == null || this.tags.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(this.tags.split(","));
    }

    /**
     * 生成文件访问URL
     */
    public String generateAccessUrl(String baseUrl) {
        if (this.isLocalStorage()) {
            return baseUrl + "/files/" + this.relativePath;
        } else {
            return this.accessUrl;
        }
    }

    /**
     * 生成缩略图URL
     */
    public String generateThumbnailUrl(String baseUrl) {
        if (this.thumbnailPath == null) {
            return null;
        }
        if (this.isLocalStorage()) {
            return baseUrl + "/thumbnails/" + this.thumbnailPath;
        } else {
            return this.thumbnailUrl;
        }
    }
}