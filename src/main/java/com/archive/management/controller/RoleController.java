package com.archive.management.controller;

import com.archive.management.entity.Role;
import com.archive.management.service.RoleService;
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
import com.archive.management.util.SecurityUtils;

/**
 * 角色管理控制器
 * 提供角色相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新角色")
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<Map<String, Object>> createRole(@Valid @RequestBody Role role) {
        try {
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "角色创建成功",
                "data", createdRole
            ));
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取角色", description = "根据ID获取角色信息")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> getRoleById(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id) {
        try {
            Role role = roleService.getRoleById(id);
            if (role != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取角色成功",
                    "data", role
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据角色编码获取角色
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据角色编码获取角色", description = "根据角色编码获取角色信息")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> getRoleByCode(
            @Parameter(description = "角色编码") @PathVariable @NotBlank String code) {
        try {
            Role role = roleService.getRoleByCode(code);
            if (role != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取角色成功",
                    "data", role
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据角色编码获取角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据角色名称获取角色
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "根据角色名称获取角色", description = "根据角色名称获取角色信息")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> getRoleByName(
            @Parameter(description = "角色名称") @PathVariable @NotBlank String name) {
        try {
            Role role = roleService.getRoleByName(name);
            if (role != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取角色成功",
                    "data", role
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据角色名称获取角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody Role role) {
        try {
            role.setId(id);
            Role updatedRole = roleService.updateRole(role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "角色更新成功",
                "data", updatedRole
            ));
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除角色（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "软删除角色")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<Map<String, Object>> deleteRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean deleted = roleService.deleteRole(id, deletedBy);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "角色删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "角色删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除角色", description = "批量软删除角色")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<Map<String, Object>> batchDeleteRoles(
            @Parameter(description = "角色ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean result = roleService.batchDeleteRoles(ids, deletedBy);
            int deletedCount = result ? ids.size() : 0;
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除角色成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 启用角色
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用角色", description = "启用角色")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<Map<String, Object>> enableRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean enabled = roleService.enableRole(id, updatedBy);
            if (enabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "角色启用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "角色启用失败"
                ));
            }
        } catch (Exception e) {
            log.error("启用角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "启用角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 禁用角色
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用角色", description = "禁用角色")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<Map<String, Object>> disableRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean disabled = roleService.disableRole(id, updatedBy);
            if (disabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "角色禁用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "角色禁用失败"
                ));
            }
        } catch (Exception e) {
            log.error("禁用角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "禁用角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询角色
     */
    @GetMapping
    @Operation(summary = "分页查询角色", description = "分页查询角色列表")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> findRolesWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "角色编码") @RequestParam(required = false) String code,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "角色类型") @RequestParam(required = false) String type,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<Role> page = new Page<>(current, size);
            IPage<Role> result = roleService.findRolesWithPagination(page, code, name, status, type, SecurityUtils.getCurrentUserId());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询角色成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有角色", description = "获取所有角色列表")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取角色列表成功",
                "data", roles
            ));
        } catch (Exception e) {
            log.error("获取所有角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取角色列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取角色统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取角色统计信息", description = "获取角色统计信息")
    @PreAuthorize("hasAuthority('role:statistics')")
    public ResponseEntity<Map<String, Object>> getRoleStatistics() {
        try {
            long totalRoles = roleService.countRoles();
            long activeRoles = roleService.countRolesByStatus(1);
            long inactiveRoles = roleService.countRolesByStatus(0);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", Map.of(
                    "totalRoles", totalRoles,
                    "activeRoles", activeRoles,
                    "inactiveRoles", inactiveRoles
                )
            ));
        } catch (Exception e) {
            log.error("获取角色统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取角色状态统计
     */
    @GetMapping("/statistics/status")
    @Operation(summary = "获取角色状态统计", description = "获取角色状态统计")
    @PreAuthorize("hasAuthority('role:statistics')")
    public ResponseEntity<Map<String, Object>> getStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = roleService.getRoleStatusStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取状态统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取角色状态统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取状态统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取角色类型统计
     */
    @GetMapping("/statistics/type")
    @Operation(summary = "获取角色类型统计", description = "获取角色类型统计")
    @PreAuthorize("hasAuthority('role:statistics')")
    public ResponseEntity<Map<String, Object>> getTypeStatistics() {
        try {
            List<Map<String, Object>> statistics = roleService.getRoleTypeStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取类型统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取角色类型统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取类型统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查角色编码是否存在
     */
    @GetMapping("/check-code")
    @Operation(summary = "检查角色编码", description = "检查角色编码是否已存在")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> checkCode(
            @Parameter(description = "角色编码") @RequestParam @NotBlank String code) {
        try {
            boolean exists = roleService.existsByRoleCode(code);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查角色编码成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查角色编码失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查角色编码失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查角色名称是否存在
     */
    @GetMapping("/check-name")
    @Operation(summary = "检查角色名称", description = "检查角色名称是否已存在")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> checkName(
            @Parameter(description = "角色名称") @RequestParam @NotBlank String name) {
        try {
            boolean exists = roleService.existsByRoleName(name);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查角色名称成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查角色名称失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查角色名称失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索角色
     */
    @GetMapping("/search")
    @Operation(summary = "搜索角色", description = "根据关键词搜索角色")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> searchRoles(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Role> page = new Page<>(current, size);
            IPage<Role> result = roleService.searchRolesWithPagination(page, keyword);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索角色成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分配权限给角色
     */
    @PostMapping("/{id}/permissions")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    @PreAuthorize("hasAuthority('role:assign-permission')")
    public ResponseEntity<Map<String, Object>> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "权限ID列表") @RequestBody @NotEmpty List<Long> permissionIds,
            @Parameter(description = "分配人ID") @RequestParam @NotNull @Positive Long assignedBy) {
        try {
            boolean assigned = roleService.assignPermissions(id, permissionIds, assignedBy);
            if (assigned) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "权限分配成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "权限分配失败"
                ));
            }
        } catch (Exception e) {
            log.error("分配权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "分配权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 移除角色权限
     */
    @DeleteMapping("/{id}/permissions")
    @Operation(summary = "移除权限", description = "移除角色权限")
    @PreAuthorize("hasAuthority('role:remove-permission')")
    public ResponseEntity<Map<String, Object>> removePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "权限ID列表") @RequestBody @NotEmpty List<Long> permissionIds,
            @Parameter(description = "移除人ID") @RequestParam @NotNull @Positive Long removedBy) {
        try {
            boolean removed = roleService.removePermissions(id, permissionIds, removedBy);
            if (removed) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "权限移除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "权限移除失败"
                ));
            }
        } catch (Exception e) {
            log.error("移除权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "移除权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色权限", description = "获取角色的所有权限")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<Map<String, Object>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long id) {
        try {
            List<String> permissionCodes = roleService.getRolePermissions(id);
            List<Map<String, Object>> permissions = permissionCodes.stream()
                .map(code -> Map.of("code", (Object)code))
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取角色权限成功",
                "data", permissions
            ));
        } catch (Exception e) {
            log.error("获取角色权限失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取角色权限失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出角色
     */
    @PostMapping("/export")
    @Operation(summary = "导出角色", description = "导出角色数据")
    @PreAuthorize("hasAuthority('role:export')")
    public ResponseEntity<Map<String, Object>> exportRoles(
            @Parameter(description = "角色ID列表") @RequestBody(required = false) List<Long> roleIds,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "包含字段") @RequestParam(required = false) List<String> includeFields) {
        try {
            List<Role> roles = roleService.exportRoles(roleIds, SecurityUtils.getCurrentUserId());
            Map<String, Object> result = Map.of("roles", roles);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出角色成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导出角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导出角色失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导入角色
     */
    @PostMapping("/import")
    @Operation(summary = "导入角色", description = "导入角色数据")
    @PreAuthorize("hasAuthority('role:import')")
    public ResponseEntity<Map<String, Object>> importRoles(
            @Parameter(description = "角色数据") @RequestBody List<Role> roles,
            @Parameter(description = "导入模式") @RequestParam(defaultValue = "merge") String importMode,
            @Parameter(description = "导入人ID") @RequestParam @NotNull @Positive Long importedBy) {
        try {
            List<Role> importedRoles = roleService.importRoles(roles, importedBy);
            Map<String, Object> result = Map.of("importedRoles", importedRoles, "count", importedRoles.size());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导入角色成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导入角色失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导入角色失败: " + e.getMessage()
            ));
        }
    }
}