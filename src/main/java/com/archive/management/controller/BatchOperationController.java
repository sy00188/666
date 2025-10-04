package com.archive.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import com.archive.management.service.BatchOperationService;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.dto.ApiResponse;

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
@RestController
@RequestMapping("/api/batch")
public class BatchOperationController {

    @Autowired
    private BatchOperationService batchOperationService;

    /**
     * 批量更新档案状态
     */
    @PostMapping("/archives/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchUpdateArchiveStatus(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> archiveIds = (List<Long>) request.get("archiveIds");
            String statusStr = (String) request.get("status");
            ArchiveStatus newStatus = ArchiveStatus.valueOf(statusStr);
            
            Map<String, Object> result = batchOperationService.batchUpdateArchiveStatus(archiveIds, newStatus);
            return ResponseEntity.ok(ApiResponse.success(result, "批量更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量更新失败: " + e.getMessage()));
        }
    }

    /**
     * 批量删除档案
     */
    @PostMapping("/archives/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteArchives(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> archiveIds = (List<Long>) request.get("archiveIds");
            
            Map<String, Object> result = batchOperationService.batchDeleteArchives(archiveIds);
            return ResponseEntity.ok(ApiResponse.success(result, "批量删除成功"));
        } catch (Exception e) {
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
     * 生成用户导入模板
     */
    private byte[] generateUserImportTemplate() {
        // 这里需要实现用户Excel模板生成逻辑
        return "用户导入模板".getBytes();
    }
}
