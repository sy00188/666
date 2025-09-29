package com.archive.management.controller;

import com.archive.management.entity.ArchiveFile;
import com.archive.management.service.ArchiveFileService;
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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案文件管理控制器
 * 提供档案文件相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/archive-files")
@RequiredArgsConstructor
@Validated
@Tag(name = "档案文件管理", description = "档案文件管理相关接口")
public class ArchiveFileController {

    private final ArchiveFileService archiveFileService;

    /**
     * 创建档案文件记录
     */
    @PostMapping
    @Operation(summary = "创建档案文件记录", description = "创建新的档案文件记录")
    @PreAuthorize("hasAuthority('archive_file:create')")
    public ResponseEntity<Map<String, Object>> createArchiveFile(@Valid @RequestBody ArchiveFile archiveFile) {
        try {
            ArchiveFile createdFile = archiveFileService.createArchiveFile(archiveFile);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "档案文件记录创建成功",
                "data", createdFile
            ));
        } catch (Exception e) {
            log.error("创建档案文件记录失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建档案文件记录失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取档案文件
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取档案文件", description = "根据ID获取档案文件信息")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> getArchiveFileById(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id) {
        try {
            ArchiveFile archiveFile = archiveFileService.getArchiveFileById(id);
            if (archiveFile != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取档案文件成功",
                    "data", archiveFile
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取档案文件失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据档案ID获取文件列表
     */
    @GetMapping("/archive/{archiveId}")
    @Operation(summary = "根据档案ID获取文件列表", description = "根据档案ID获取关联的文件列表")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> getFilesByArchiveId(
            @Parameter(description = "档案ID") @PathVariable @NotNull @Positive Long archiveId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "包含已删除文件") @RequestParam(defaultValue = "false") boolean includeDeleted) {
        try {
            Page<ArchiveFile> page = new Page<>(current, size);
            IPage<ArchiveFile> result = archiveFileService.getFilesByArchiveId(archiveId, includeDeleted, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取档案文件列表成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("根据档案ID获取文件列表失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取文件列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新档案文件
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新档案文件", description = "更新档案文件信息")
    @PreAuthorize("hasAuthority('archive_file:update')")
    public ResponseEntity<Map<String, Object>> updateArchiveFile(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody ArchiveFile archiveFile) {
        try {
            archiveFile.setId(id);
            ArchiveFile updatedFile = archiveFileService.updateArchiveFile(archiveFile);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "档案文件更新成功",
                "data", updatedFile
            ));
        } catch (Exception e) {
            log.error("更新档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新档案文件失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除档案文件（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除档案文件", description = "软删除档案文件")
    @PreAuthorize("hasAuthority('archive_file:delete')")
    public ResponseEntity<Map<String, Object>> deleteArchiveFile(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean deleted = archiveFileService.deleteArchiveFile(id, deletedBy);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "档案文件删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "档案文件删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("删除档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除档案文件失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除档案文件
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除档案文件", description = "批量软删除档案文件")
    @PreAuthorize("hasAuthority('archive_file:delete')")
    public ResponseEntity<Map<String, Object>> batchDeleteArchiveFiles(
            @Parameter(description = "档案文件ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            int deletedCount = archiveFileService.batchDeleteArchiveFiles(ids, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除档案文件成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除档案文件失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传档案文件")
    @PreAuthorize("hasAuthority('archive_file:upload')")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @Parameter(description = "档案ID") @RequestParam @NotNull @Positive Long archiveId,
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述") @RequestParam(required = false) String description,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType,
            @Parameter(description = "上传人ID") @RequestParam @NotNull @Positive Long uploadedBy) {
        try {
            ArchiveFile uploadedFile = archiveFileService.uploadFile(archiveId, file, description, fileType, uploadedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文件上传成功",
                "data", uploadedFile
            ));
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "文件上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传档案文件")
    @PreAuthorize("hasAuthority('archive_file:upload')")
    public ResponseEntity<Map<String, Object>> batchUploadFiles(
            @Parameter(description = "档案ID") @RequestParam @NotNull @Positive Long archiveId,
            @Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "上传人ID") @RequestParam @NotNull @Positive Long uploadedBy) {
        try {
            List<ArchiveFile> uploadedFiles = archiveFileService.batchUploadFiles(archiveId, files, uploadedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量文件上传成功",
                "data", uploadedFiles
            ));
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量文件上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载文件", description = "下载档案文件")
    @PreAuthorize("hasAuthority('archive_file:download')")
    public void downloadFile(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "下载人ID") @RequestParam @NotNull @Positive Long downloadedBy,
            HttpServletResponse response) {
        try {
            archiveFileService.downloadFile(id, downloadedBy, response);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 预览文件
     */
    @GetMapping("/{id}/preview")
    @Operation(summary = "预览文件", description = "预览档案文件")
    @PreAuthorize("hasAuthority('archive_file:preview')")
    public ResponseEntity<Map<String, Object>> previewFile(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "预览人ID") @RequestParam @NotNull @Positive Long previewedBy) {
        try {
            Map<String, Object> previewData = archiveFileService.previewFile(id, previewedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文件预览成功",
                "data", previewData
            ));
        } catch (Exception e) {
            log.error("文件预览失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "文件预览失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查文件权限
     */
    @GetMapping("/{id}/permission")
    @Operation(summary = "检查文件权限", description = "检查用户对文件的权限")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> checkFilePermission(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "权限类型") @RequestParam @NotBlank String permissionType) {
        try {
            boolean hasPermission = archiveFileService.checkFilePermission(id, userId, permissionType);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "权限检查完成",
                "data", Map.of("hasPermission", hasPermission)
            ));
        } catch (Exception e) {
            log.error("检查文件权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "权限检查失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取文件版本列表
     */
    @GetMapping("/{id}/versions")
    @Operation(summary = "获取文件版本列表", description = "获取文件的所有版本")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> getFileVersions(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ArchiveFile> page = new Page<>(current, size);
            IPage<ArchiveFile> result = archiveFileService.getFileVersions(id, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取文件版本列表成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取文件版本列表失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取文件版本列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 创建文件版本
     */
    @PostMapping("/{id}/versions")
    @Operation(summary = "创建文件版本", description = "为文件创建新版本")
    @PreAuthorize("hasAuthority('archive_file:version')")
    public ResponseEntity<Map<String, Object>> createFileVersion(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "新版本文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "版本说明") @RequestParam(required = false) String versionNote,
            @Parameter(description = "创建人ID") @RequestParam @NotNull @Positive Long createdBy) {
        try {
            ArchiveFile newVersion = archiveFileService.createFileVersion(id, file, versionNote, createdBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文件版本创建成功",
                "data", newVersion
            ));
        } catch (Exception e) {
            log.error("创建文件版本失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建文件版本失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 恢复文件版本
     */
    @PutMapping("/{id}/versions/{versionId}/restore")
    @Operation(summary = "恢复文件版本", description = "恢复到指定版本")
    @PreAuthorize("hasAuthority('archive_file:version')")
    public ResponseEntity<Map<String, Object>> restoreFileVersion(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "版本ID") @PathVariable @NotNull @Positive Long versionId,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            boolean restored = archiveFileService.restoreFileVersion(id, versionId, operatedBy);
            if (restored) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文件版本恢复成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "文件版本恢复失败"
                ));
            }
        } catch (Exception e) {
            log.error("恢复文件版本失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "恢复文件版本失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 病毒扫描
     */
    @PostMapping("/{id}/scan")
    @Operation(summary = "病毒扫描", description = "对文件进行病毒扫描")
    @PreAuthorize("hasAuthority('archive_file:scan')")
    public ResponseEntity<Map<String, Object>> scanFile(
            @Parameter(description = "档案文件ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "扫描人ID") @RequestParam @NotNull @Positive Long scannedBy) {
        try {
            Map<String, Object> scanResult = archiveFileService.scanFile(id, scannedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文件扫描完成",
                "data", scanResult
            ));
        } catch (Exception e) {
            log.error("文件病毒扫描失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "文件扫描失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量病毒扫描
     */
    @PostMapping("/scan/batch")
    @Operation(summary = "批量病毒扫描", description = "批量对文件进行病毒扫描")
    @PreAuthorize("hasAuthority('archive_file:scan')")
    public ResponseEntity<Map<String, Object>> batchScanFiles(
            @Parameter(description = "档案文件ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "扫描人ID") @RequestParam @NotNull @Positive Long scannedBy) {
        try {
            List<Map<String, Object>> scanResults = archiveFileService.batchScanFiles(ids, scannedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量文件扫描完成",
                "data", scanResults
            ));
        } catch (Exception e) {
            log.error("批量文件病毒扫描失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量文件扫描失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询档案文件
     */
    @GetMapping
    @Operation(summary = "分页查询档案文件", description = "分页查询档案文件列表")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> findArchiveFilesWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "档案ID") @RequestParam(required = false) Long archiveId,
            @Parameter(description = "文件名") @RequestParam(required = false) String fileName,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType,
            @Parameter(description = "文件状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "上传人ID") @RequestParam(required = false) Long uploadedBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<ArchiveFile> page = new Page<>(current, size);
            IPage<ArchiveFile> result = archiveFileService.findArchiveFilesWithPagination(page, archiveId, fileName, 
                fileType, status, uploadedBy, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询档案文件成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询档案文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询档案文件失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取文件统计信息", description = "获取档案文件统计信息")
    @PreAuthorize("hasAuthority('archive_file:statistics')")
    public ResponseEntity<Map<String, Object>> getFileStatistics() {
        try {
            long totalFiles = archiveFileService.countFiles();
            long totalSize = archiveFileService.getTotalFileSize();
            Map<String, Long> typeStatistics = archiveFileService.getFileTypeStatistics();
            Map<String, Long> statusStatistics = archiveFileService.getFileStatusStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", Map.of(
                    "totalFiles", totalFiles,
                    "totalSize", totalSize,
                    "typeStatistics", typeStatistics,
                    "statusStatistics", statusStatistics
                )
            ));
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文件", description = "根据关键词搜索档案文件")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> searchFiles(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ArchiveFile> page = new Page<>(current, size);
            IPage<ArchiveFile> result = archiveFileService.searchFiles(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索文件成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索文件失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 高级搜索文件
     */
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索文件", description = "高级搜索档案文件")
    @PreAuthorize("hasAuthority('archive_file:read')")
    public ResponseEntity<Map<String, Object>> advancedSearchFiles(
            @Parameter(description = "搜索条件") @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ArchiveFile> page = new Page<>(current, size);
            IPage<ArchiveFile> result = archiveFileService.advancedSearchFiles(searchCriteria, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "高级搜索文件成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("高级搜索文件失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "高级搜索文件失败: " + e.getMessage()
            ));
        }
    }
}