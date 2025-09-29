package com.archive.management.controller;

import com.archive.management.entity.Archive;
import com.archive.management.service.ArchiveService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案管理控制器
 * 提供档案相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/archives")
@RequiredArgsConstructor
@Validated
@Tag(name = "档案管理", description = "档案管理相关接口")
public class ArchiveController {

    private final ArchiveService archiveService;

    /**
     * 创建档案
     */
    @PostMapping
    @Operation(summary = "创建档案", description = "创建新档案")
    @PreAuthorize("hasAuthority('archive:create')")
    public ResponseEntity<Map<String, Object>> createArchive(@Valid @RequestBody Archive archive) {
        try {
            Archive createdArchive = archiveService.createArchive(archive);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "档案创建成功",
                "data", createdArchive
            ));
        } catch (Exception e) {
            log.error("创建档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取档案
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取档案", description = "根据ID获取档案信息")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> getArchiveById(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id) {
        try {
            Archive archive = archiveService.getArchiveById(id);
            if (archive != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取档案成功",
                    "data", archive
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据档案编号获取档案
     */
    @GetMapping("/number/{archiveNumber}")
    @Operation(summary = "根据档案编号获取档案", description = "根据档案编号获取档案信息")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> getArchiveByNumber(
            @Parameter(description = "档案编号") @PathVariable @NotBlank String archiveNumber) {
        try {
            Archive archive = archiveService.getArchiveByNumber(archiveNumber);
            if (archive != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取档案成功",
                    "data", archive
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据档案编号获取档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新档案
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新档案", description = "更新档案信息")
    @PreAuthorize("hasAuthority('archive:update')")
    public ResponseEntity<Map<String, Object>> updateArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody Archive archive) {
        try {
            archive.setId(id);
            Archive updatedArchive = archiveService.updateArchive(archive);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "档案更新成功",
                "data", updatedArchive
            ));
        } catch (Exception e) {
            log.error("更新档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除档案（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除档案", description = "软删除档案")
    @PreAuthorize("hasAuthority('archive:delete')")
    public ResponseEntity<Map<String, Object>> deleteArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean deleted = archiveService.deleteArchive(id, deletedBy);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("删除档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除档案
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除档案", description = "批量软删除档案")
    @PreAuthorize("hasAuthority('archive:delete')")
    public ResponseEntity<Map<String, Object>> batchDeleteArchives(
            @Parameter(description = "档案ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            int deletedCount = archiveService.batchDeleteArchives(ids, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除档案成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 归档档案
     */
    @PutMapping("/{id}/archive")
    @Operation(summary = "归档档案", description = "将档案设置为归档状态")
    @PreAuthorize("hasAuthority('archive:archive')")
    public ResponseEntity<Map<String, Object>> archiveDocument(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "归档人ID") @RequestParam @NotNull @Positive Long archivedBy,
            @Parameter(description = "归档备注") @RequestParam(required = false) String archiveNote) {
        try {
            boolean archived = archiveService.archiveDocument(id, archivedBy, archiveNote);
            if (archived) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案归档成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案归档失败"
                ));
            }
        } catch (Exception e) {
            log.error("归档档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "归档档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发布档案
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布档案", description = "发布档案")
    @PreAuthorize("hasAuthority('archive:publish')")
    public ResponseEntity<Map<String, Object>> publishArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "发布人ID") @RequestParam @NotNull @Positive Long publishedBy,
            @Parameter(description = "发布备注") @RequestParam(required = false) String publishNote) {
        try {
            boolean published = archiveService.publishArchive(id, publishedBy, publishNote);
            if (published) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案发布成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案发布失败"
                ));
            }
        } catch (Exception e) {
            log.error("发布档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发布档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 撤销发布档案
     */
    @PutMapping("/{id}/unpublish")
    @Operation(summary = "撤销发布档案", description = "撤销发布档案")
    @PreAuthorize("hasAuthority('archive:unpublish')")
    public ResponseEntity<Map<String, Object>> unpublishArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "撤销人ID") @RequestParam @NotNull @Positive Long unpublishedBy,
            @Parameter(description = "撤销备注") @RequestParam(required = false) String unpublishNote) {
        try {
            boolean unpublished = archiveService.unpublishArchive(id, unpublishedBy, unpublishNote);
            if (unpublished) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案撤销发布成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案撤销发布失败"
                ));
            }
        } catch (Exception e) {
            log.error("撤销发布档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "撤销发布档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询档案
     */
    @GetMapping
    @Operation(summary = "分页查询档案", description = "分页查询档案列表")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> findArchivesWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "档案编号") @RequestParam(required = false) String archiveNumber,
            @Parameter(description = "档案标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "档案级别") @RequestParam(required = false) String level,
            @Parameter(description = "保密级别") @RequestParam(required = false) String securityLevel,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<Archive> page = new Page<>(current, size);
            IPage<Archive> result = archiveService.findArchivesWithPagination(page, archiveNumber, title, 
                categoryId, status, level, securityLevel, createdBy, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据分类获取档案
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据分类获取档案", description = "根据分类ID获取档案列表")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> getArchivesByCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "包含子分类") @RequestParam(defaultValue = "false") boolean includeSubCategories) {
        try {
            Page<Archive> page = new Page<>(current, size);
            IPage<Archive> result = archiveService.getArchivesByCategory(categoryId, includeSubCategories, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取分类档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("根据分类获取档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取分类档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据创建人获取档案
     */
    @GetMapping("/creator/{createdBy}")
    @Operation(summary = "根据创建人获取档案", description = "根据创建人ID获取档案列表")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> getArchivesByCreator(
            @Parameter(description = "创建人ID") @PathVariable @NotNull @Positive Long createdBy,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Archive> page = new Page<>(current, size);
            IPage<Archive> result = archiveService.getArchivesByCreator(createdBy, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取创建人档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("根据创建人获取档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取创建人档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取档案统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取档案统计信息", description = "获取档案统计信息")
    @PreAuthorize("hasAuthority('archive:statistics')")
    public ResponseEntity<Map<String, Object>> getArchiveStatistics() {
        try {
            long totalArchives = archiveService.countArchives();
            long publishedArchives = archiveService.countArchivesByStatus(1);
            long archivedArchives = archiveService.countArchivesByStatus(2);
            long draftArchives = archiveService.countArchivesByStatus(0);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", Map.of(
                    "totalArchives", totalArchives,
                    "publishedArchives", publishedArchives,
                    "archivedArchives", archivedArchives,
                    "draftArchives", draftArchives
                )
            ));
        } catch (Exception e) {
            log.error("获取档案统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取档案状态统计
     */
    @GetMapping("/statistics/status")
    @Operation(summary = "获取档案状态统计", description = "获取档案状态统计")
    @PreAuthorize("hasAuthority('archive:statistics')")
    public ResponseEntity<Map<String, Object>> getStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = archiveService.getStatusStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取状态统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取档案状态统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取状态统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取档案分类统计
     */
    @GetMapping("/statistics/category")
    @Operation(summary = "获取档案分类统计", description = "获取档案分类统计")
    @PreAuthorize("hasAuthority('archive:statistics')")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        try {
            List<Map<String, Object>> statistics = archiveService.getCategoryStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取分类统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取档案分类统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取分类统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索档案
     */
    @GetMapping("/search")
    @Operation(summary = "搜索档案", description = "根据关键词搜索档案")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> searchArchives(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Archive> page = new Page<>(current, size);
            IPage<Archive> result = archiveService.searchArchives(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 高级搜索档案
     */
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索档案", description = "高级搜索档案")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> advancedSearchArchives(
            @Parameter(description = "搜索条件") @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Archive> page = new Page<>(current, size);
            IPage<Archive> result = archiveService.advancedSearchArchives(searchCriteria, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "高级搜索档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("高级搜索档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "高级搜索档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 上传档案文件
     */
    @PostMapping("/{id}/upload")
    @Operation(summary = "上传档案文件", description = "上传档案文件")
    @PreAuthorize("hasAuthority('archive:upload')")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "上传人ID") @RequestParam @NotNull @Positive Long uploadedBy,
            @Parameter(description = "文件描述") @RequestParam(required = false) String description) {
        try {
            Map<String, Object> result = archiveService.uploadFile(id, file, uploadedBy, description);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文件上传成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("上传档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "文件上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 下载档案文件
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载档案文件", description = "下载档案文件")
    @PreAuthorize("hasAuthority('archive:download')")
    public ResponseEntity<Map<String, Object>> downloadFile(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "文件ID") @RequestParam @NotNull @Positive Long fileId,
            @Parameter(description = "下载人ID") @RequestParam @NotNull @Positive Long downloadedBy) {
        try {
            Map<String, Object> result = archiveService.downloadFile(id, fileId, downloadedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文件下载成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("下载档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "文件下载失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查档案访问权限
     */
    @GetMapping("/{id}/check-permission")
    @Operation(summary = "检查档案访问权限", description = "检查用户对档案的访问权限")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> checkArchivePermission(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "权限类型") @RequestParam @NotBlank String permissionType) {
        try {
            boolean hasPermission = archiveService.checkArchivePermission(id, userId, permissionType);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "权限检查成功",
                "data", Map.of("hasPermission", hasPermission)
            ));
        } catch (Exception e) {
            log.error("检查档案访问权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "权限检查失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 添加档案标签
     */
    @PostMapping("/{id}/tags")
    @Operation(summary = "添加档案标签", description = "为档案添加标签")
    @PreAuthorize("hasAuthority('archive:update')")
    public ResponseEntity<Map<String, Object>> addArchiveTags(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "标签列表") @RequestBody @NotEmpty List<String> tags,
            @Parameter(description = "添加人ID") @RequestParam @NotNull @Positive Long addedBy) {
        try {
            boolean added = archiveService.addArchiveTags(id, tags, addedBy);
            if (added) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "标签添加成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "标签添加失败"
                ));
            }
        } catch (Exception e) {
            log.error("添加档案标签失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "标签添加失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 移除档案标签
     */
    @DeleteMapping("/{id}/tags")
    @Operation(summary = "移除档案标签", description = "移除档案标签")
    @PreAuthorize("hasAuthority('archive:update')")
    public ResponseEntity<Map<String, Object>> removeArchiveTags(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "标签列表") @RequestBody @NotEmpty List<String> tags,
            @Parameter(description = "移除人ID") @RequestParam @NotNull @Positive Long removedBy) {
        try {
            boolean removed = archiveService.removeArchiveTags(id, tags, removedBy);
            if (removed) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "标签移除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "标签移除失败"
                ));
            }
        } catch (Exception e) {
            log.error("移除档案标签失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "标签移除失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 档案评分
     */
    @PostMapping("/{id}/rating")
    @Operation(summary = "档案评分", description = "为档案评分")
    @PreAuthorize("hasAuthority('archive:rate')")
    public ResponseEntity<Map<String, Object>> rateArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "评分") @RequestParam @NotNull BigDecimal rating,
            @Parameter(description = "评分人ID") @RequestParam @NotNull @Positive Long ratedBy,
            @Parameter(description = "评分备注") @RequestParam(required = false) String ratingNote) {
        try {
            boolean rated = archiveService.rateArchive(id, rating, ratedBy, ratingNote);
            if (rated) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案评分成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案评分失败"
                ));
            }
        } catch (Exception e) {
            log.error("档案评分失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "档案评分失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 添加档案评论
     */
    @PostMapping("/{id}/comments")
    @Operation(summary = "添加档案评论", description = "为档案添加评论")
    @PreAuthorize("hasAuthority('archive:comment')")
    public ResponseEntity<Map<String, Object>> addArchiveComment(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "评论内容") @RequestParam @NotBlank String content,
            @Parameter(description = "评论人ID") @RequestParam @NotNull @Positive Long commentedBy,
            @Parameter(description = "父评论ID") @RequestParam(required = false) Long parentCommentId) {
        try {
            Map<String, Object> result = archiveService.addArchiveComment(id, content, commentedBy, parentCommentId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "评论添加成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("添加档案评论失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "评论添加失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取档案评论
     */
    @GetMapping("/{id}/comments")
    @Operation(summary = "获取档案评论", description = "获取档案评论列表")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<Map<String, Object>> getArchiveComments(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Map<String, Object>> page = new Page<>(current, size);
            IPage<Map<String, Object>> result = archiveService.getArchiveComments(id, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取评论成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取档案评论失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取评论失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 备份档案
     */
    @PostMapping("/{id}/backup")
    @Operation(summary = "备份档案", description = "备份档案数据")
    @PreAuthorize("hasAuthority('archive:backup')")
    public ResponseEntity<Map<String, Object>> backupArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "备份类型") @RequestParam(defaultValue = "full") String backupType,
            @Parameter(description = "备份人ID") @RequestParam @NotNull @Positive Long backedUpBy) {
        try {
            Map<String, Object> result = archiveService.backupArchive(id, backupType, backedUpBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "档案备份成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("备份档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "档案备份失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 恢复档案
     */
    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复档案", description = "从备份恢复档案")
    @PreAuthorize("hasAuthority('archive:restore')")
    public ResponseEntity<Map<String, Object>> restoreArchive(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "备份ID") @RequestParam @NotNull @Positive Long backupId,
            @Parameter(description = "恢复人ID") @RequestParam @NotNull @Positive Long restoredBy) {
        try {
            boolean restored = archiveService.restoreArchive(id, backupId, restoredBy);
            if (restored) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案恢复成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案恢复失败"
                ));
            }
        } catch (Exception e) {
            log.error("恢复档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "档案恢复失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出档案
     */
    @PostMapping("/export")
    @Operation(summary = "导出档案", description = "导出档案数据")
    @PreAuthorize("hasAuthority('archive:export')")
    public ResponseEntity<Map<String, Object>> exportArchives(
            @Parameter(description = "档案ID列表") @RequestBody(required = false) List<Long> archiveIds,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "包含字段") @RequestParam(required = false) List<String> includeFields,
            @Parameter(description = "包含文件") @RequestParam(defaultValue = "false") boolean includeFiles) {
        try {
            Map<String, Object> result = archiveService.exportArchives(archiveIds, format, includeFields, includeFiles);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导出档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导出档案失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导入档案
     */
    @PostMapping("/import")
    @Operation(summary = "导入档案", description = "导入档案数据")
    @PreAuthorize("hasAuthority('archive:import')")
    public ResponseEntity<Map<String, Object>> importArchives(
            @Parameter(description = "档案数据") @RequestBody List<Archive> archives,
            @Parameter(description = "导入模式") @RequestParam(defaultValue = "merge") String importMode,
            @Parameter(description = "导入人ID") @RequestParam @NotNull @Positive Long importedBy) {
        try {
            Map<String, Object> result = archiveService.importArchives(archives, importMode, importedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导入档案成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导入档案失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导入档案失败: " + e.getMessage()
            ));
        }
    }
}