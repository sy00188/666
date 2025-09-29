package com.archive.management.controller;

import com.archive.management.entity.Permission;
import com.archive.management.service.PermissionService;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 * 提供权限相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Validated
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 创建权限
     */
    @PostMapping
    @Operation(summary = "创建权限", description = "创建新权限")
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<Map<String, Object>> createPermission(@Valid @RequestBody Permission permission) {
        try {
            Permission createdPermission = permissionService.createPermission(permission);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "权限创建成功",
                "data", createdPermission
            ));
        } catch (Exception e) {
            log.error("创建权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取权限", description = "根据ID获取权限信息")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> getPermissionById(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long id) {
        try {
            Permission permission = permissionService.getPermissionById(id);
            if (permission != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取权限成功",
                    "data", permission
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据权限编码获取权限
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据权限编码获取权限", description = "根据权限编码获取权限信息")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> getPermissionByCode(
            @Parameter(description = "权限编码") @PathVariable @NotBlank String code) {
        try {
            Permission permission = permissionService.getPermissionByCode(code);
            if (permission != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取权限成功",
                    "data", permission
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据权限编码获取权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据权限名称获取权限
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "根据权限名称获取权限", description = "根据权限名称获取权限信息")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> getPermissionByName(
            @Parameter(description = "权限名称") @PathVariable @NotBlank String name) {
        try {
            Permission permission = permissionService.getPermissionByName(name);
            if (permission != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取权限成功",
                    "data", permission
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据权限名称获取权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新权限", description = "更新权限信息")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<Map<String, Object>> updatePermission(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody Permission permission) {
        try {
            permission.setId(id);
            Permission updatedPermission = permissionService.updatePermission(permission);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "权限更新成功",
                "data", updatedPermission
            ));
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除权限（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "软删除权限")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<Map<String, Object>> deletePermission(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean deleted = permissionService.deletePermission(id, deletedBy);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "权限删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "权限删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除权限", description = "批量软删除权限")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<Map<String, Object>> batchDeletePermissions(
            @Parameter(description = "权限ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            int deletedCount = permissionService.batchDeletePermissions(ids, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除权限成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 启用权限
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用权限", description = "启用权限")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<Map<String, Object>> enablePermission(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean enabled = permissionService.enablePermission(id, updatedBy);
            if (enabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "权限启用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "权限启用失败"
                ));
            }
        } catch (Exception e) {
            log.error("启用权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "启用权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 禁用权限
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用权限", description = "禁用权限")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<Map<String, Object>> disablePermission(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean disabled = permissionService.disablePermission(id, updatedBy);
            if (disabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "权限禁用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "权限禁用失败"
                ));
            }
        } catch (Exception e) {
            log.error("禁用权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "禁用权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询权限
     */
    @GetMapping
    @Operation(summary = "分页查询权限", description = "分页查询权限列表")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> findPermissionsWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "权限编码") @RequestParam(required = false) String code,
            @Parameter(description = "权限名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "权限类型") @RequestParam(required = false) String type,
            @Parameter(description = "父权限ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<Permission> page = new Page<>(current, size);
            IPage<Permission> result = permissionService.findPermissionsWithPagination(page, code, name, 
                status, type, parentId, createdBy, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询权限成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取所有权限
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有权限", description = "获取所有权限列表")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> getAllPermissions() {
        try {
            List<Permission> permissions = permissionService.getAllPermissions();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取权限列表成功",
                "data", permissions
            ));
        } catch (Exception e) {
            log.error("获取所有权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取权限列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取权限树结构")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> getPermissionTree(
            @Parameter(description = "根节点ID") @RequestParam(required = false) Long rootId,
            @Parameter(description = "包含禁用的权限") @RequestParam(defaultValue = "false") boolean includeDisabled) {
        try {
            List<Map<String, Object>> tree = permissionService.buildPermissionTree(rootId, includeDisabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取权限树成功",
                "data", tree
            ));
        } catch (Exception e) {
            log.error("获取权限树失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取权限树失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取子权限
     */
    @GetMapping("/{id}/children")
    @Operation(summary = "获取子权限", description = "获取指定权限的子权限")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> getChildPermissions(
            @Parameter(description = "父权限ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "包含禁用的权限") @RequestParam(defaultValue = "false") boolean includeDisabled) {
        try {
            List<Permission> children = permissionService.getChildPermissions(id, includeDisabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取子权限成功",
                "data", children
            ));
        } catch (Exception e) {
            log.error("获取子权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取子权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取权限统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取权限统计信息", description = "获取权限统计信息")
    @PreAuthorize("hasAuthority('permission:statistics')")
    public ResponseEntity<Map<String, Object>> getPermissionStatistics() {
        try {
            long totalPermissions = permissionService.countPermissions();
            long activePermissions = permissionService.countPermissionsByStatus(1);
            long inactivePermissions = permissionService.countPermissionsByStatus(0);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", Map.of(
                    "totalPermissions", totalPermissions,
                    "activePermissions", activePermissions,
                    "inactivePermissions", inactivePermissions
                )
            ));
        } catch (Exception e) {
            log.error("获取权限统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取权限状态统计
     */
    @GetMapping("/statistics/status")
    @Operation(summary = "获取权限状态统计", description = "获取权限状态统计")
    @PreAuthorize("hasAuthority('permission:statistics')")
    public ResponseEntity<Map<String, Object>> getStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = permissionService.getStatusStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取状态统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取权限状态统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取状态统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取权限类型统计
     */
    @GetMapping("/statistics/type")
    @Operation(summary = "获取权限类型统计", description = "获取权限类型统计")
    @PreAuthorize("hasAuthority('permission:statistics')")
    public ResponseEntity<Map<String, Object>> getTypeStatistics() {
        try {
            List<Map<String, Object>> statistics = permissionService.getTypeStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取类型统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取权限类型统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取类型统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查权限编码是否存在
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查权限编码", description = "检查权限编码是否已存在")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> checkCode(
            @Parameter(description = "权限编码") @RequestParam @NotBlank String code) {
        try {
            boolean exists = permissionService.existsByCode(code);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查权限编码成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查权限编码失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查权限编码失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查权限名称是否存在
     */
    @GetMapping("/check-name")
    @Operation(summary = "检查权限名称", description = "检查权限名称是否已存在")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> checkName(
            @Parameter(description = "权限名称") @RequestParam @NotBlank String name) {
        try {
            boolean exists = permissionService.existsByName(name);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查权限名称成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查权限名称失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查权限名称失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索权限
     */
    @GetMapping("/search")
    @Operation(summary = "搜索权限", description = "根据关键词搜索权限")
    @PreAuthorize("hasAuthority('permission:read')")
    public ResponseEntity<Map<String, Object>> searchPermissions(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Permission> page = new Page<>(current, size);
            IPage<Permission> result = permissionService.searchPermissions(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索权限成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出权限
     */
    @PostMapping("/export")
    @Operation(summary = "导出权限", description = "导出权限数据")
    @PreAuthorize("hasAuthority('permission:export')")
    public ResponseEntity<Map<String, Object>> exportPermissions(
            @Parameter(description = "权限ID列表") @RequestBody(required = false) List<Long> permissionIds,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "包含字段") @RequestParam(required = false) List<String> includeFields) {
        try {
            Map<String, Object> result = permissionService.exportPermissions(permissionIds, format, includeFields);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出权限成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导出权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导出权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导入权限
     */
    @PostMapping("/import")
    @Operation(summary = "导入权限", description = "导入权限数据")
    @PreAuthorize("hasAuthority('permission:import')")
    public ResponseEntity<Map<String, Object>> importPermissions(
            @Parameter(description = "权限数据") @RequestBody List<Permission> permissions,
            @Parameter(description = "导入模式") @RequestParam(defaultValue = "merge") String importMode,
            @Parameter(description = "导入人ID") @RequestParam @NotNull @Positive Long importedBy) {
        try {
            Map<String, Object> result = permissionService.importPermissions(permissions, importMode, importedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导入权限成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导入权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导入权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 同步权限
     */
    @PostMapping("/sync")
    @Operation(summary = "同步权限", description = "同步权限数据")
    @PreAuthorize("hasAuthority('permission:sync')")
    public ResponseEntity<Map<String, Object>> syncPermissions(
            @Parameter(description = "同步源") @RequestParam @NotBlank String source,
            @Parameter(description = "同步配置") @RequestBody Map<String, Object> syncConfig,
            @Parameter(description = "同步人ID") @RequestParam @NotNull @Positive Long syncBy) {
        try {
            Map<String, Object> result = permissionService.syncPermissions(source, syncConfig, syncBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "同步权限成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("同步权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "同步权限失败: " + e.getMessage()
            ));
        }
    }
}