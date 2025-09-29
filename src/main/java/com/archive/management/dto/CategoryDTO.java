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

/**
 * 分类数据传输对象
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
@Schema(description = "分类数据传输对象")
public class CategoryDTO {

    @Schema(description = "分类ID", example = "1")
    private Long id;

    @Schema(description = "分类编码", example = "DOC_MGMT", required = true)
    @NotBlank(message = "分类编码不能为空")
    @Size(min = 2, max = 50, message = "分类编码长度必须在2-50个字符之间")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "分类编码只能包含大写字母、数字和下划线")
    private String categoryCode;

    @Schema(description = "分类名称", example = "文档管理", required = true)
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 100, message = "分类名称长度必须在2-100个字符之间")
    private String categoryName;

    @Schema(description = "分类描述", example = "用于管理各类文档档案")
    @Size(max = 500, message = "分类描述长度不能超过500个字符")
    private String description;

    @Schema(description = "分类类型", example = "1", allowableValues = {"1", "2", "3", "4"})
    @NotNull(message = "分类类型不能为空")
    @Min(value = 1, message = "分类类型值必须为1-4")
    @Max(value = 4, message = "分类类型值必须为1-4")
    private Integer categoryType;

    @Schema(description = "父分类ID", example = "0")
    @Min(value = 0, message = "父分类ID不能为负数")
    private Long parentId;

    @Schema(description = "父分类名称", example = "根分类")
    private String parentCategoryName;

    @Schema(description = "分类路径", example = "0,1,2")
    private String categoryPath;

    @Schema(description = "分类层级", example = "1")
    @Min(value = 1, message = "分类层级必须为正数")
    @Max(value = 10, message = "分类层级不能超过10")
    private Integer categoryLevel;

    @Schema(description = "排序号", example = "1")
    @Min(value = 0, message = "排序号不能为负数")
    private Integer sortOrder;

    @Schema(description = "分类图标", example = "folder")
    @Size(max = 50, message = "分类图标长度不能超过50个字符")
    private String icon;

    @Schema(description = "分类颜色", example = "#1890ff")
    @Size(max = 20, message = "分类颜色长度不能超过20个字符")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "分类颜色必须为有效的十六进制颜色值")
    private String color;

    @Schema(description = "分类封面图片", example = "https://example.com/cover.jpg")
    @Size(max = 500, message = "分类封面图片URL长度不能超过500个字符")
    private String coverImage;

    @Schema(description = "分类标签")
    private List<String> tags;

    @Schema(description = "分类关键词")
    private List<String> keywords;

    @Schema(description = "是否系统内置", example = "false")
    @NotNull(message = "系统内置标识不能为空")
    private Boolean systemBuiltIn;

    @Schema(description = "是否启用", example = "true")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @Schema(description = "是否显示", example = "true")
    private Boolean visible;

    @Schema(description = "是否允许上传", example = "true")
    private Boolean allowUpload;

    @Schema(description = "是否允许下载", example = "true")
    private Boolean allowDownload;

    @Schema(description = "最大文件大小(MB)", example = "100")
    @Min(value = 0, message = "最大文件大小不能为负数")
    private Integer maxFileSize;

    @Schema(description = "允许的文件类型")
    private List<String> allowedFileTypes;

    @Schema(description = "存储配置")
    private String storageConfig;

    @Schema(description = "权限配置")
    private String permissionConfig;

    @Schema(description = "审核配置")
    private String auditConfig;

    @Schema(description = "备注", example = "文档管理分类")
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

    @Schema(description = "版本号", example = "1")
    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    @Schema(description = "档案数量", example = "50")
    private Long archiveCount;

    @Schema(description = "文件数量", example = "100")
    private Long fileCount;

    @Schema(description = "总文件大小(字节)", example = "1024000")
    private Long totalFileSize;

    @Schema(description = "子分类数量", example = "5")
    private Long childrenCount;

    @Schema(description = "子分类列表")
    private List<CategoryDTO> children;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    /**
     * 获取分类类型显示文本
     */
    @Schema(description = "分类类型显示文本")
    public String getCategoryTypeText() {
        if (categoryType == null) {
            return "未知";
        }
        switch (categoryType) {
            case 1:
                return "文档分类";
            case 2:
                return "媒体分类";
            case 3:
                return "数据分类";
            case 4:
                return "其他分类";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否为根分类
     */
    @Schema(description = "是否为根分类")
    public boolean isRootCategory() {
        return parentId == null || parentId == 0;
    }

    /**
     * 判断是否有子分类
     */
    @Schema(description = "是否有子分类")
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 判断分类是否可用
     */
    @Schema(description = "分类是否可用")
    public boolean isAvailable() {
        return enabled != null && enabled && (deleted == null || !deleted);
    }

    /**
     * 判断是否可以删除
     */
    @Schema(description = "是否可以删除")
    public boolean isDeletable() {
        return (systemBuiltIn == null || !systemBuiltIn) && 
               (archiveCount == null || archiveCount == 0) &&
               (childrenCount == null || childrenCount == 0);
    }

    /**
     * 判断是否可以编辑
     */
    @Schema(description = "是否可以编辑")
    public boolean isEditable() {
        return systemBuiltIn == null || !systemBuiltIn;
    }

    /**
     * 判断是否可以上传文件
     */
    @Schema(description = "是否可以上传文件")
    public boolean canUpload() {
        return isAvailable() && (allowUpload == null || allowUpload);
    }

    /**
     * 判断是否可以下载文件
     */
    @Schema(description = "是否可以下载文件")
    public boolean canDownload() {
        return isAvailable() && (allowDownload == null || allowDownload);
    }

    /**
     * 判断是否显示
     */
    @Schema(description = "是否显示")
    public boolean isVisible() {
        return visible == null || visible;
    }

    /**
     * 获取分类层级深度
     */
    @Schema(description = "分类层级深度")
    public int getDepth() {
        if (categoryPath == null || categoryPath.trim().isEmpty()) {
            return 0;
        }
        return categoryPath.split(",").length - 1;
    }

    /**
     * 判断是否为指定分类的子分类
     */
    @Schema(description = "是否为指定分类的子分类")
    public boolean isChildOf(Long parentCategoryId) {
        if (categoryPath == null || parentCategoryId == null) {
            return false;
        }
        return categoryPath.contains("," + parentCategoryId + ",") || 
               categoryPath.startsWith(parentCategoryId + ",");
    }

    /**
     * 判断是否为指定分类的父分类
     */
    @Schema(description = "是否为指定分类的父分类")
    public boolean isParentOf(String childCategoryPath) {
        if (id == null || childCategoryPath == null) {
            return false;
        }
        return childCategoryPath.contains("," + id + ",") || 
               childCategoryPath.startsWith(id + ",");
    }

    /**
     * 判断文件类型是否允许
     */
    @Schema(description = "文件类型是否允许")
    public boolean isFileTypeAllowed(String fileType) {
        if (allowedFileTypes == null || allowedFileTypes.isEmpty()) {
            return true; // 如果没有限制，则允许所有类型
        }
        return allowedFileTypes.contains(fileType.toLowerCase());
    }

    /**
     * 判断文件大小是否允许
     */
    @Schema(description = "文件大小是否允许")
    public boolean isFileSizeAllowed(long fileSizeInBytes) {
        if (maxFileSize == null || maxFileSize <= 0) {
            return true; // 如果没有限制，则允许任意大小
        }
        long maxSizeInBytes = maxFileSize * 1024L * 1024L; // 转换为字节
        return fileSizeInBytes <= maxSizeInBytes;
    }

    /**
     * 获取格式化的总文件大小
     */
    @Schema(description = "格式化的总文件大小")
    public String getFormattedTotalFileSize() {
        if (totalFileSize == null || totalFileSize == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = totalFileSize.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取完整分类路径名称
     */
    @Schema(description = "完整分类路径名称")
    public String getFullCategoryPath() {
        if (categoryPath == null || categoryPath.trim().isEmpty()) {
            return categoryName;
        }
        
        // 这里简化处理，实际应该根据路径查询各级分类名称
        return categoryName; // 实际实现中需要根据categoryPath构建完整路径
    }
}