package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.service.BatchOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 批量操作控制器
 * 提供批量导入导出、批量状态更新等API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Tag(name = "批量操作", description = "批量操作相关接口")
public class BatchOperationController {

    private final BatchOperationService batchOperationService;

    /**
     * 批量更新档案状态
     */
    @PostMapping("/archives/status")
    @Operation(summary = "批量更新档案状态", description = "批量更新档案状态")
    @PreAuthorize("hasAuthority('archive:update')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchUpdateArchiveStatus(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> archiveIds = (List<Long>) request.get("archiveIds");
            Integer newStatus = (Integer) request.get("status");
            
            Map<String, Object> result = batchOperationService.batchUpdateArchiveStatus(archiveIds, newStatus);
            return ResponseEntity.ok(ApiResponse.success(result, "批量更新成功"));
        } catch (Exception e) {
            log.error("批量更新档案状态失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("批量更新失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除档案
     */
    @PostMapping("/archives/delete")
    @Operation(summary = "批量删除档案", description = "批量软删除档案")
    @PreAuthorize("hasAuthority('archive:delete')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteArchives(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> archiveIds = (List<Long>) request.get("archiveIds");
            Long deletedBy = Long.valueOf(request.get("deletedBy").toString());
            
            Map<String, Object> result = batchOperationService.batchDeleteArchives(archiveIds, deletedBy);
            return ResponseEntity.ok(ApiResponse.success(result, "批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除档案失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("批量删除失败: " + e.getMessage()));
        }
    }

    /**
     * 批量更新用户状态
     */
    @PostMapping("/users/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchUpdateUserStatus(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            Integer newStatus = (Integer) request.get("status");
            
            Map<String, Object> result = batchOperationService.batchUpdateUserStatus(userIds, newStatus);
            return ResponseEntity.ok(ApiResponse.success(result, "批量更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量更新失败: " + e.getMessage()));
        }
    }

    /**
     * 批量导入档案
     */
    @PostMapping("/archives/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchImportArchives(
            @RequestParam("file") MultipartFile file,
            @RequestParam("createUserId") Long createUserId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("文件不能为空"));
            }
            
            CompletableFuture<Map<String, Object>> future = batchOperationService.batchImportArchives(file, createUserId);
            Map<String, Object> result = future.get(); // 等待异步操作完成
            
            return ResponseEntity.ok(ApiResponse.success(result, "批量导入成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量导入失败: " + e.getMessage()));
        }
    }

    /**
     * 批量导出档案
     */
    @PostMapping("/archives/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> batchExportArchives(
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "excel") String format) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> archiveIds = (List<Long>) request.get("archiveIds");
            
            byte[] fileContent = batchOperationService.batchExportArchives(archiveIds, format);
            
            String contentType = "excel".equalsIgnoreCase(format) ? 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : 
                "text/csv";
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=archives." + format)
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量导出用户
     */
    @PostMapping("/users/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> batchExportUsers(
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "excel") String format) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            
            byte[] fileContent = batchOperationService.batchExportUsers(userIds, format);
            
            String contentType = "excel".equalsIgnoreCase(format) ? 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : 
                "text/csv";
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=users." + format)
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量分配角色
     */
    @PostMapping("/users/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchAssignRoles(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            @SuppressWarnings("unchecked")
            List<Long> roleIds = (List<Long>) request.get("roleIds");
            
            Map<String, Object> result = batchOperationService.batchAssignRoles(userIds, roleIds);
            return ResponseEntity.ok(ApiResponse.success(result, "批量分配角色成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量分配角色失败: " + e.getMessage()));
        }
    }

    /**
     * 批量分配权限
     */
    @PostMapping("/users/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchAssignPermissions(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            @SuppressWarnings("unchecked")
            List<Long> permissionIds = (List<Long>) request.get("permissionIds");
            
            Map<String, Object> result = batchOperationService.batchAssignPermissions(userIds, permissionIds);
            return ResponseEntity.ok(ApiResponse.success(result, "批量分配权限成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量分配权限失败: " + e.getMessage()));
        }
    }

    /**
     * 获取批量操作模板
     */
    @GetMapping("/template/archives")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getArchiveImportTemplate() {
        try {
            // 生成导入模板
            byte[] template = generateArchiveImportTemplate();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=archive_import_template.xlsx")
                    .body(template);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用户导入模板
     */
    @GetMapping("/template/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getUserImportTemplate() {
        try {
            // 生成用户导入模板
            byte[] template = generateUserImportTemplate();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=user_import_template.xlsx")
                    .body(template);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 生成档案导入模板
     */
    private byte[] generateArchiveImportTemplate() {
        // 这里需要实现Excel模板生成逻辑
        return "档案导入模板".getBytes();
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/users/import")
    @Operation(summary = "批量导入用户", description = "从Excel文件批量导入用户")
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchImportUsers(
            @Parameter(description = "导入文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "创建人ID") @RequestParam("createUserId") Long createUserId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("文件不能为空"));
            }
            
            CompletableFuture<Map<String, Object>> future = batchOperationService.batchImportUsers(file, createUserId);
            Map<String, Object> result = future.get(); // 等待异步操作完成
            
            return ResponseEntity.ok(ApiResponse.success(result, "批量导入成功"));
        } catch (Exception e) {
            log.error("批量导入用户失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("批量导入失败: " + e.getMessage()));
        }
    }

    /**
     * 获取批量操作进度
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "获取批量操作进度", description = "获取批量操作的进度信息")
    @PreAuthorize("hasAuthority('batch:view')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBatchOperationProgress(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        try {
            // 这里可以从Redis或数据库中获取任务进度
            Map<String, Object> progress = new HashMap<>();
            progress.put("taskId", taskId);
            progress.put("status", "completed");
            progress.put("progress", 100);
            progress.put("message", "批量操作已完成");
            
            return ResponseEntity.ok(ApiResponse.success(progress, "获取进度成功"));
        } catch (Exception e) {
            log.error("获取批量操作进度失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取进度失败: " + e.getMessage()));
        }
    }

    /**
     * 取消批量操作
     */
    @PostMapping("/cancel/{taskId}")
    @Operation(summary = "取消批量操作", description = "取消正在进行的批量操作")
    @PreAuthorize("hasAuthority('batch:cancel')")
    public ResponseEntity<ApiResponse<String>> cancelBatchOperation(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        try {
            // 这里可以实现取消批量操作的逻辑
            return ResponseEntity.ok(ApiResponse.success("操作已取消", "批量操作已取消"));
        } catch (Exception e) {
            log.error("取消批量操作失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("取消失败: " + e.getMessage()));
        }
    }

    /**
     * 批量操作历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "获取批量操作历史", description = "获取批量操作的历史记录")
    @PreAuthorize("hasAuthority('batch:view')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBatchOperationHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            // 这里可以从数据库中获取批量操作历史
            List<Map<String, Object>> history = new ArrayList<>();
            
            Map<String, Object> record = new HashMap<>();
            record.put("id", "1");
            record.put("operation", "批量导入档案");
            record.put("status", "completed");
            record.put("createTime", "2024-01-20 10:00:00");
            record.put("operator", "admin");
            record.put("totalCount", 100);
            record.put("successCount", 95);
            record.put("failedCount", 5);
            history.add(record);
            
            return ResponseEntity.ok(ApiResponse.success(history, "获取历史记录成功"));
        } catch (Exception e) {
            log.error("获取批量操作历史失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取历史失败: " + e.getMessage()));
        }
    }
}
