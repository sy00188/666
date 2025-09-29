package com.archive.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案文件数据传输对象
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
@Schema(description = "档案文件数据传输对象")
public class ArchiveFileDTO {

    @Schema(description = "文件ID", example = "1")
    private Long id;

    @Schema(description = "档案ID", example = "1", required = true)
    @NotNull(message = "档案ID不能为空")
    private Long archiveId;

    @Schema(description = "档案标题", example = "重要文档")
    private String archiveTitle;

    @Schema(description = "文件编码", example = "FILE_20240101_001", required = true)
    @NotBlank(message = "文件编码不能为空")
    @Size(min = 5, max = 100, message = "文件编码长度必须在5-100个字符之间")
    private String fileCode;

    @Schema(description = "文件名称", example = "重要文档.pdf", required = true)
    @NotBlank(message = "文件名称不能为空")
    @Size(min = 1, max = 255, message = "文件名称长度必须在1-255个字符之间")
    private String fileName;

    @Schema(description = "原始文件名", example = "original_document.pdf")
    @Size(max = 255, message = "原始文件名长度不能超过255个字符")
    private String originalFileName;

    @Schema(description = "文件类型", example = "pdf")
    @Size(max = 50, message = "文件类型长度不能超过50个字符")
    private String fileType;

    @Schema(description = "文件扩展名", example = ".pdf")
    @Size(max = 20, message = "文件扩展名长度不能超过20个字符")
    private String fileExtension;

    @Schema(description = "MIME类型", example = "application/pdf")
    @Size(max = 100, message = "MIME类型长度不能超过100个字符")
    private String mimeType;

    @Schema(description = "文件大小(字节)", example = "1024000")
    @Min(value = 0, message = "文件大小不能为负数")
    private Long fileSize;

    // 手动添加缺失的getter方法
    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    @Schema(description = "文件MD5值", example = "d41d8cd98f00b204e9800998ecf8427e")
    @Size(max = 32, message = "MD5值长度必须为32个字符")
    @Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "MD5值格式不正确")
    private String fileMd5;

    @Schema(description = "文件SHA1值", example = "da39a3ee5e6b4b0d3255bfef95601890afd80709")
    @Size(max = 40, message = "SHA1值长度必须为40个字符")
    @Pattern(regexp = "^[a-fA-F0-9]{40}$", message = "SHA1值格式不正确")
    private String fileSha1;

    @Schema(description = "文件存储路径", example = "/archives/2024/01/file.pdf")
    @Size(max = 500, message = "文件存储路径长度不能超过500个字符")
    private String storagePath;

    @Schema(description = "文件存储类型", example = "1", allowableValues = {"1", "2", "3", "4"})
    @Min(value = 1, message = "存储类型值必须为1-4")
    @Max(value = 4, message = "存储类型值必须为1-4")
    private Integer storageType;

    @Schema(description = "存储服务器", example = "server1")
    @Size(max = 100, message = "存储服务器长度不能超过100个字符")
    private String storageServer;

    @Schema(description = "存储桶名称", example = "archive-bucket")
    @Size(max = 100, message = "存储桶名称长度不能超过100个字符")
    private String bucketName;

    @Schema(description = "文件访问URL", example = "https://example.com/files/document.pdf")
    @Size(max = 500, message = "文件访问URL长度不能超过500个字符")
    private String accessUrl;

    @Schema(description = "文件下载URL", example = "https://example.com/download/document.pdf")
    @Size(max = 500, message = "文件下载URL长度不能超过500个字符")
    private String downloadUrl;

    @Schema(description = "文件预览URL", example = "https://example.com/preview/document.pdf")
    @Size(max = 500, message = "文件预览URL长度不能超过500个字符")
    private String previewUrl;

    @Schema(description = "缩略图URL", example = "https://example.com/thumbnails/document.jpg")
    @Size(max = 500, message = "缩略图URL长度不能超过500个字符")
    private String thumbnailUrl;

    @Schema(description = "文件版本号", example = "1.0")
    @Size(max = 20, message = "文件版本号长度不能超过20个字符")
    private String version;

    @Schema(description = "是否为主文件", example = "true")
    private Boolean isPrimary;

    @Schema(description = "文件状态", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @NotNull(message = "文件状态不能为空")
    @Min(value = 1, message = "文件状态值必须为1-5")
    @Max(value = 5, message = "文件状态值必须为1-5")
    private Integer status;

    @Schema(description = "是否加密", example = "false")
    private Boolean encrypted;

    @Schema(description = "加密算法", example = "AES-256")
    @Size(max = 50, message = "加密算法长度不能超过50个字符")
    private String encryptionAlgorithm;

    @Schema(description = "是否压缩", example = "false")
    private Boolean compressed;

    @Schema(description = "压缩算法", example = "ZIP")
    @Size(max = 50, message = "压缩算法长度不能超过50个字符")
    private String compressionAlgorithm;

    @Schema(description = "压缩率", example = "0.75")
    @DecimalMin(value = "0.0", message = "压缩率不能小于0")
    @DecimalMax(value = "1.0", message = "压缩率不能大于1")
    private Double compressionRatio;

    @Schema(description = "病毒扫描状态", example = "1", allowableValues = {"1", "2", "3", "4"})
    @Min(value = 1, message = "病毒扫描状态值必须为1-4")
    @Max(value = 4, message = "病毒扫描状态值必须为1-4")
    private Integer virusScanStatus;

    @Schema(description = "病毒扫描时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime virusScanTime;

    @Schema(description = "病毒扫描结果", example = "clean")
    @Size(max = 200, message = "病毒扫描结果长度不能超过200个字符")
    private String virusScanResult;

    @Schema(description = "文件标签")
    private List<String> tags;

    @Schema(description = "文件关键词")
    private List<String> keywords;

    @Schema(description = "文件描述", example = "重要的业务文档")
    @Size(max = 1000, message = "文件描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "文件内容摘要", example = "本文档包含...")
    @Size(max = 2000, message = "文件内容摘要长度不能超过2000个字符")
    private String contentSummary;

    @Schema(description = "文件元数据")
    private Map<String, Object> metadata;

    @Schema(description = "文件属性")
    private Map<String, Object> properties;

    @Schema(description = "下载次数", example = "10")
    @Min(value = 0, message = "下载次数不能为负数")
    private Long downloadCount;

    @Schema(description = "查看次数", example = "50")
    @Min(value = 0, message = "查看次数不能为负数")
    private Long viewCount;

    @Schema(description = "最后访问时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

    @Schema(description = "最后下载时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDownloadTime;

    @Schema(description = "排序号", example = "1")
    @Min(value = 0, message = "排序号不能为负数")
    private Integer sortOrder;

    @Schema(description = "备注", example = "重要文件，请妥善保管")
    @Size(max = 500, message = "备注长度不能超过500个字符")
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

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    /**
     * 获取存储类型显示文本
     */
    @Schema(description = "存储类型显示文本")
    public String getStorageTypeText() {
        if (storageType == null) {
            return "未知";
        }
        switch (storageType) {
            case 1:
                return "本地存储";
            case 2:
                return "云存储";
            case 3:
                return "分布式存储";
            case 4:
                return "归档存储";
            default:
                return "未知";
        }
    }

    /**
     * 获取文件状态显示文本
     */
    @Schema(description = "文件状态显示文本")
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "正常";
            case 2:
                return "处理中";
            case 3:
                return "已损坏";
            case 4:
                return "已删除";
            case 5:
                return "已归档";
            default:
                return "未知";
        }
    }

    /**
     * 获取病毒扫描状态显示文本
     */
    @Schema(description = "病毒扫描状态显示文本")
    public String getVirusScanStatusText() {
        if (virusScanStatus == null) {
            return "未扫描";
        }
        switch (virusScanStatus) {
            case 1:
                return "未扫描";
            case 2:
                return "扫描中";
            case 3:
                return "安全";
            case 4:
                return "发现病毒";
            default:
                return "未知";
        }
    }

    /**
     * 判断文件是否可用
     */
    @Schema(description = "文件是否可用")
    public boolean isAvailable() {
        return status != null && status == 1 && (deleted == null || !deleted);
    }

    /**
     * 判断文件是否安全
     */
    @Schema(description = "文件是否安全")
    public boolean isSafe() {
        return virusScanStatus != null && virusScanStatus == 3;
    }

    /**
     * 判断文件是否可以下载
     */
    @Schema(description = "文件是否可以下载")
    public boolean isDownloadable() {
        return isAvailable() && isSafe();
    }

    /**
     * 判断文件是否可以预览
     */
    @Schema(description = "文件是否可以预览")
    public boolean isPreviewable() {
        if (!isAvailable()) {
            return false;
        }
        
        if (fileType == null) {
            return false;
        }
        
        // 支持预览的文件类型
        String[] previewableTypes = {"pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
                                   "jpg", "jpeg", "png", "gif", "bmp", "svg", "mp4", "avi", "mov"};
        
        for (String type : previewableTypes) {
            if (type.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 判断是否为图片文件
     */
    @Schema(description = "是否为图片文件")
    public boolean isImage() {
        if (fileType == null) {
            return false;
        }
        
        String[] imageTypes = {"jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico"};
        
        for (String type : imageTypes) {
            if (type.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 判断是否为视频文件
     */
    @Schema(description = "是否为视频文件")
    public boolean isVideo() {
        if (fileType == null) {
            return false;
        }
        
        String[] videoTypes = {"mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v"};
        
        for (String type : videoTypes) {
            if (type.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 判断是否为音频文件
     */
    @Schema(description = "是否为音频文件")
    public boolean isAudio() {
        if (fileType == null) {
            return false;
        }
        
        String[] audioTypes = {"mp3", "wav", "flac", "aac", "ogg", "wma", "m4a"};
        
        for (String type : audioTypes) {
            if (type.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 判断是否为文档文件
     */
    @Schema(description = "是否为文档文件")
    public boolean isDocument() {
        if (fileType == null) {
            return false;
        }
        
        String[] documentTypes = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf"};
        
        for (String type : documentTypes) {
            if (type.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        
        return false;
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
     * 获取文件图标
     */
    @Schema(description = "文件图标")
    public String getFileIcon() {
        if (fileType == null) {
            return "file";
        }
        
        if (isImage()) {
            return "image";
        } else if (isVideo()) {
            return "video";
        } else if (isAudio()) {
            return "audio";
        } else if (isDocument()) {
            return "document";
        } else if ("zip".equalsIgnoreCase(fileType) || "rar".equalsIgnoreCase(fileType) || 
                  "7z".equalsIgnoreCase(fileType)) {
            return "archive";
        } else {
            return "file";
        }
    }

    /**
     * 判断文件是否需要病毒扫描
     */
    @Schema(description = "文件是否需要病毒扫描")
    public boolean needsVirusScan() {
        return virusScanStatus == null || virusScanStatus == 1;
    }

    /**
     * 判断文件是否正在扫描病毒
     */
    @Schema(description = "文件是否正在扫描病毒")
    public boolean isVirusScanning() {
        return virusScanStatus != null && virusScanStatus == 2;
    }

    /**
     * 判断文件是否发现病毒
     */
    @Schema(description = "文件是否发现病毒")
    public boolean hasVirus() {
        return virusScanStatus != null && virusScanStatus == 4;
    }

    /**
     * 获取文件完整路径
     */
    @Schema(description = "文件完整路径")
    public String getFullPath() {
        if (storagePath == null) {
            return fileName;
        }
        
        if (storagePath.endsWith("/")) {
            return storagePath + fileName;
        } else {
            return storagePath + "/" + fileName;
        }
    }

    /**
     * 判断是否为加密文件
     */
    @Schema(description = "是否为加密文件")
    public boolean isEncrypted() {
        return encrypted != null && encrypted;
    }

    /**
     * 判断是否为压缩文件
     */
    @Schema(description = "是否为压缩文件")
    public boolean isCompressed() {
        return compressed != null && compressed;
    }

    /**
     * 获取压缩率百分比
     */
    @Schema(description = "压缩率百分比")
    public String getCompressionRatioPercentage() {
        if (compressionRatio == null) {
            return "0%";
        }
        
        return String.format("%.1f%%", compressionRatio * 100);
    }
}