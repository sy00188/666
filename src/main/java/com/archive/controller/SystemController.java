package com.archive.controller;

import com.archive.service.SystemService;
import com.archive.dto.request.*;
import com.archive.dto.response.*;
import com.archive.management.common.Result;
import com.archive.management.common.PageResult;
import com.archive.management.common.PermissionTreeNode;
import com.archive.annotation.RequirePermission;
import com.archive.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 * 提供部门、角色、权限、配置管理等API接口
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统管理", description = "系统管理相关API接口")
public class SystemController {

    private static final Logger log = LoggerFactory.getLogger(SystemController.class);
    private final SystemService systemService;

    // ==================== 部门管理 ====================

    @PostMapping("/departments")
    @Operation(summary = "创建部门", description = "创建新的部门信息")
    @PreAuthorize("hasAuthority('system:department:create')")
    @RequirePermission("system:department:create")
    @OperationLog(module = "系统管理", operation = "创建部门")
    public Result<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request) {
        try {
            log.info("创建部门请求: {}", request);
            DepartmentResponse response = systemService.createDepartment(request);
            log.info("部门创建成功，部门ID: {}", response.getId());
            return Result.success(response, "部门创建成功");
        } catch (Exception e) {
            log.error("创建部门失败: {}", e.getMessage(), e);
            return Result.failure("创建部门失败: " + e.getMessage());
        }
    }

    @PutMapping("/departments/{departmentId}")
    @Operation(summary = "更新部门", description = "更新指定部门的信息")
    @PreAuthorize("hasAuthority('system:department:update')")
    @RequirePermission("system:department:update")
    @OperationLog(module = "系统管理", operation = "更新部门")
    public Result<DepartmentResponse> updateDepartment(
            @Parameter(description = "部门ID") @PathVariable @NotNull @Positive Long departmentId,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        try {
            log.info("更新部门请求，部门ID: {}, 请求参数: {}", departmentId, request);
            DepartmentResponse response = systemService.updateDepartment(departmentId, request);
            log.info("部门更新成功，部门ID: {}", departmentId);
            return Result.success(response, "部门更新成功");
        } catch (Exception e) {
            log.error("更新部门失败，部门ID: {}, 错误: {}", departmentId, e.getMessage(), e);
            return Result.failure("更新部门失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/departments/{departmentId}")
    @Operation(summary = "删除部门", description = "删除指定的部门")
    @PreAuthorize("hasAuthority('system:department:delete')")
    @RequirePermission("system:department:delete")
    @OperationLog(module = "系统管理", operation = "删除部门")
    public Result<Void> deleteDepartment(
            @Parameter(description = "部门ID") @PathVariable @NotNull @Positive Long departmentId) {
        try {
            log.info("删除部门请求，部门ID: {}", departmentId);
            systemService.deleteDepartment(departmentId);
            log.info("部门删除成功，部门ID: {}", departmentId);
            return Result.success("部门删除成功");
        } catch (Exception e) {
            log.error("删除部门失败，部门ID: {}, 错误: {}", departmentId, e.getMessage(), e);
            return Result.failure("删除部门失败: " + e.getMessage());
        }
    }

    @GetMapping("/departments/{departmentId}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详细信息")
    @PreAuthorize("hasAuthority('system:department:view')")
    @RequirePermission("system:department:view")
    public Result<DepartmentResponse> getDepartmentById(
            @Parameter(description = "部门ID") @PathVariable @NotNull @Positive Long departmentId) {
        try {
            log.info("获取部门详情请求，部门ID: {}", departmentId);
            DepartmentResponse response = systemService.getDepartmentById(departmentId);
            return Result.success(response, "获取部门详情成功");
        } catch (Exception e) {
            log.error("获取部门详情失败，部门ID: {}, 错误: {}", departmentId, e.getMessage(), e);
            return Result.failure("获取部门详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/departments")
    @Operation(summary = "查询部门列表", description = "分页查询部门列表")
    @PreAuthorize("hasAuthority('system:department:view')")
    @RequirePermission("system:department:view")
    public Result<PageResult<DepartmentResponse>> queryDepartments(
            @Valid DepartmentQueryRequest request) {
        try {
            log.info("查询部门列表请求: {}", request);
            PageResult<DepartmentResponse> result = systemService.queryDepartments(request);
            return Result.success(result, "查询部门列表成功");
        } catch (Exception e) {
            log.error("查询部门列表失败: {}", e.getMessage(), e);
            return Result.failure("查询部门列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/departments/tree")
    @Operation(summary = "获取部门树", description = "获取部门的树形结构")
    @PreAuthorize("hasAuthority('system:department:view')")
    @RequirePermission("system:department:view")
    public Result<List<DepartmentResponse>> getDepartmentTree() {
        try {
            log.info("获取部门树请求");
            List<DepartmentResponse> tree = systemService.getDepartmentTree();
            return Result.success(tree, "获取部门树成功");
        } catch (Exception e) {
            log.error("获取部门树失败: {}", e.getMessage(), e);
            return Result.failure("获取部门树失败: " + e.getMessage());
        }
    }

    @GetMapping("/departments/{departmentId}/users")
    @Operation(summary = "获取部门用户", description = "获取指定部门下的所有用户")
    @PreAuthorize("hasAuthority('system:department:view')")
    @RequirePermission("system:department:view")
    public Result<List<UserResponse>> getDepartmentUsers(
            @Parameter(description = "部门ID") @PathVariable @NotNull @Positive Long departmentId) {
        try {
            log.info("获取部门用户请求，部门ID: {}", departmentId);
            List<UserResponse> users = systemService.getDepartmentUsers(departmentId);
            return Result.success(users, "获取部门用户成功");
        } catch (Exception e) {
            log.error("获取部门用户失败，部门ID: {}, 错误: {}", departmentId, e.getMessage(), e);
            return Result.failure("获取部门用户失败: " + e.getMessage());
        }
    }

    // ==================== 角色管理 ====================

    @PostMapping("/roles")
    @Operation(summary = "创建角色", description = "创建新的角色")
    @PreAuthorize("hasAuthority('system:role:create')")
    @RequirePermission("system:role:create")
    @OperationLog(module = "系统管理", operation = "创建角色")
    public Result<RoleResponse> createRole(
            @Valid @RequestBody RoleCreateRequest request) {
        try {
            log.info("创建角色请求: {}", request);
            RoleResponse response = systemService.createRole(request);
            log.info("角色创建成功，角色ID: {}", response.getId());
            return Result.success(response, "角色创建成功");
        } catch (Exception e) {
            log.error("创建角色失败: {}", e.getMessage(), e);
            return Result.failure("创建角色失败: " + e.getMessage());
        }
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "更新角色", description = "更新指定角色的信息")
    @PreAuthorize("hasAuthority('system:role:update')")
    @RequirePermission("system:role:update")
    @OperationLog(module = "系统管理", operation = "更新角色")
    public Result<RoleResponse> updateRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long roleId,
            @Valid @RequestBody RoleUpdateRequest request) {
        try {
            log.info("更新角色请求，角色ID: {}, 请求参数: {}", roleId, request);
            RoleResponse response = systemService.updateRole(roleId, request);
            log.info("角色更新成功，角色ID: {}", roleId);
            return Result.success(response, "角色更新成功");
        } catch (Exception e) {
            log.error("更新角色失败，角色ID: {}, 错误: {}", roleId, e.getMessage(), e);
            return Result.failure("更新角色失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "删除角色", description = "删除指定的角色")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @RequirePermission("system:role:delete")
    @OperationLog(module = "系统管理", operation = "删除角色")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long roleId) {
        try {
            log.info("删除角色请求，角色ID: {}", roleId);
            systemService.deleteRole(roleId);
            log.info("角色删除成功，角色ID: {}", roleId);
            return Result.success("角色删除成功");
        } catch (Exception e) {
            log.error("删除角色失败，角色ID: {}, 错误: {}", roleId, e.getMessage(), e);
            return Result.failure("删除角色失败: " + e.getMessage());
        }
    }

    @GetMapping("/roles/{roleId}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @PreAuthorize("hasAuthority('system:role:view')")
    @RequirePermission("system:role:view")
    public Result<RoleResponse> getRoleById(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long roleId) {
        try {
            log.info("获取角色详情请求，角色ID: {}", roleId);
            RoleResponse response = systemService.getRoleById(roleId);
            return Result.success(response, "获取角色详情成功");
        } catch (Exception e) {
            log.error("获取角色详情失败，角色ID: {}, 错误: {}", roleId, e.getMessage(), e);
            return Result.failure("获取角色详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/roles")
    @Operation(summary = "查询角色列表", description = "分页查询角色列表")
    @PreAuthorize("hasAuthority('system:role:view')")
    @RequirePermission("system:role:view")
    public Result<PageResult<RoleResponse>> queryRoles(
            @Valid RoleQueryRequest request) {
        try {
            log.info("查询角色列表请求: {}", request);
            PageResult<RoleResponse> result = systemService.queryRoles(request);
            return Result.success(result, "查询角色列表成功");
        } catch (Exception e) {
            log.error("查询角色列表失败: {}", e.getMessage(), e);
            return Result.failure("查询角色列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/roles/available")
    @Operation(summary = "获取可用角色", description = "获取所有可用的角色列表")
    @PreAuthorize("hasAuthority('system:role:view')")
    @RequirePermission("system:role:view")
    public Result<List<RoleResponse>> getAllAvailableRoles() {
        try {
            log.info("获取可用角色列表请求");
            List<RoleResponse> roles = systemService.getAllAvailableRoles();
            return Result.success(roles, "获取可用角色列表成功");
        } catch (Exception e) {
            log.error("获取可用角色列表失败: {}", e.getMessage(), e);
            return Result.failure("获取可用角色列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    @PreAuthorize("hasAuthority('system:role:permission')")
    @RequirePermission("system:role:permission")
    @OperationLog(module = "系统管理", operation = "角色分配权限")
    public Result<Void> assignPermissionsToRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long roleId,
            @RequestBody @Valid List<@NotNull @Positive Long> permissionIds) {
        try {
            log.info("角色分配权限请求，角色ID: {}, 权限IDs: {}", roleId, permissionIds);
            systemService.assignPermissionsToRole(roleId, permissionIds);
            log.info("角色权限分配成功，角色ID: {}", roleId);
            return Result.success("角色权限分配成功");
        } catch (Exception e) {
            log.error("角色权限分配失败，角色ID: {}, 错误: {}", roleId, e.getMessage(), e);
            return Result.failure("角色权限分配失败: " + e.getMessage());
        }
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限")
    @PreAuthorize("hasAuthority('system:role:view')")
    @RequirePermission("system:role:view")
    public Result<List<PermissionResponse>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull @Positive Long roleId) {
        try {
            log.info("获取角色权限请求，角色ID: {}", roleId);
            List<PermissionResponse> permissions = systemService.getRolePermissions(roleId);
            return Result.success(permissions, "获取角色权限成功");
        } catch (Exception e) {
            log.error("获取角色权限失败，角色ID: {}, 错误: {}", roleId, e.getMessage(), e);
            return Result.failure("获取角色权限失败: " + e.getMessage());
        }
    }

    // ==================== 权限管理 ====================

    @PostMapping("/permissions")
    @Operation(summary = "创建权限", description = "创建新的权限")
    @PreAuthorize("hasAuthority('system:permission:create')")
    @RequirePermission("system:permission:create")
    @OperationLog(module = "系统管理", operation = "创建权限")
    public Result<PermissionResponse> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {
        try {
            log.info("创建权限请求: {}", request);
            PermissionResponse response = systemService.createPermission(request);
            log.info("权限创建成功，权限ID: {}", response.getId());
            return Result.success(response, "权限创建成功");
        } catch (Exception e) {
            log.error("创建权限失败: {}", e.getMessage(), e);
            return Result.failure("创建权限失败: " + e.getMessage());
        }
    }

    @PutMapping("/permissions/{permissionId}")
    @Operation(summary = "更新权限", description = "更新指定权限的信息")
    @PreAuthorize("hasAuthority('system:permission:update')")
    @RequirePermission("system:permission:update")
    @OperationLog(module = "系统管理", operation = "更新权限")
    public Result<PermissionResponse> updatePermission(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long permissionId,
            @Valid @RequestBody PermissionUpdateRequest request) {
        try {
            log.info("更新权限请求，权限ID: {}, 请求参数: {}", permissionId, request);
            PermissionResponse response = systemService.updatePermission(permissionId, request);
            log.info("权限更新成功，权限ID: {}", permissionId);
            return Result.success(response, "权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败，权限ID: {}, 错误: {}", permissionId, e.getMessage(), e);
            return Result.failure("更新权限失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/permissions/{permissionId}")
    @Operation(summary = "删除权限", description = "删除指定的权限")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    @RequirePermission("system:permission:delete")
    @OperationLog(module = "系统管理", operation = "删除权限")
    public Result<Void> deletePermission(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long permissionId) {
        try {
            log.info("删除权限请求，权限ID: {}", permissionId);
            systemService.deletePermission(permissionId);
            log.info("权限删除成功，权限ID: {}", permissionId);
            return Result.success("权限删除成功");
        } catch (Exception e) {
            log.error("删除权限失败，权限ID: {}, 错误: {}", permissionId, e.getMessage(), e);
            return Result.failure("删除权限失败: " + e.getMessage());
        }
    }

    @GetMapping("/permissions/{permissionId}")
    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
    @PreAuthorize("hasAuthority('system:permission:view')")
    @RequirePermission("system:permission:view")
    public Result<PermissionResponse> getPermissionById(
            @Parameter(description = "权限ID") @PathVariable @NotNull @Positive Long permissionId) {
        try {
            log.info("获取权限详情请求，权限ID: {}", permissionId);
            PermissionResponse response = systemService.getPermissionById(permissionId);
            return Result.success(response, "获取权限详情成功");
        } catch (Exception e) {
            log.error("获取权限详情失败，权限ID: {}, 错误: {}", permissionId, e.getMessage(), e);
            return Result.failure("获取权限详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/permissions")
    @Operation(summary = "查询权限列表", description = "分页查询权限列表")
    @PreAuthorize("hasAuthority('system:permission:view')")
    @RequirePermission("system:permission:view")
    public Result<PageResult<PermissionResponse>> queryPermissions(
            @Valid PermissionQueryRequest request) {
        try {
            log.info("查询权限列表请求: {}", request);
            PageResult<PermissionResponse> result = systemService.queryPermissions(request);
            return Result.success(result, "查询权限列表成功");
        } catch (Exception e) {
            log.error("查询权限列表失败: {}", e.getMessage(), e);
            return Result.failure("查询权限列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/permissions/tree")
    @Operation(summary = "获取权限树", description = "获取权限的树形结构")
    @PreAuthorize("hasAuthority('system:permission:view')")
    @RequirePermission("system:permission:view")
    public Result<List<PermissionTreeNode>> getPermissionTree() {
        try {
            log.info("获取权限树请求");
            Result<List<PermissionTreeNode>> result = systemService.getPermissionTree();
            return result;
        } catch (Exception e) {
            log.error("获取权限树失败: {}", e.getMessage(), e);
            return Result.failure("获取权限树失败: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    @PreAuthorize("hasAuthority('system:permission:view')")
    @RequirePermission("system:permission:view")
    public Result<List<PermissionResponse>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long userId) {
        try {
            log.info("获取用户权限请求，用户ID: {}", userId);
            List<PermissionResponse> permissions = systemService.getUserPermissions(userId);
            return Result.success(permissions, "获取用户权限成功");
        } catch (Exception e) {
            log.error("获取用户权限失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return Result.failure("获取用户权限失败: " + e.getMessage());
        }
    }

    // ==================== 系统配置管理 ====================

    @PostMapping("/configs")
    @Operation(summary = "创建配置", description = "创建新的系统配置")
    @PreAuthorize("hasAuthority('system:config:create')")
    @RequirePermission("system:config:create")
    @OperationLog(module = "系统管理", operation = "创建系统配置")
    public Result<ConfigResponse> createSystemConfig(
            @Valid @RequestBody ConfigCreateRequest request) {
        try {
            log.info("创建系统配置请求: {}", request);
            ConfigResponse response = systemService.createSystemConfig(request);
            log.info("系统配置创建成功，配置ID: {}", response.getId());
            return Result.success(response, "系统配置创建成功");
        } catch (Exception e) {
            log.error("创建系统配置失败: {}", e.getMessage(), e);
            return Result.failure("创建系统配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/configs/{configId}")
    @Operation(summary = "更新配置", description = "更新指定的系统配置")
    @PreAuthorize("hasAuthority('system:config:update')")
    @RequirePermission("system:config:update")
    @OperationLog(module = "系统管理", operation = "更新系统配置")
    public Result<ConfigResponse> updateSystemConfig(
            @Parameter(description = "配置ID") @PathVariable @NotNull @Positive Long configId,
            @Valid @RequestBody ConfigUpdateRequest request) {
        try {
            log.info("更新系统配置请求，配置ID: {}, 请求参数: {}", configId, request);
            ConfigResponse response = systemService.updateSystemConfig(configId, request);
            log.info("系统配置更新成功，配置ID: {}", configId);
            return Result.success(response, "系统配置更新成功");
        } catch (Exception e) {
            log.error("更新系统配置失败，配置ID: {}, 错误: {}", configId, e.getMessage(), e);
            return Result.failure("更新系统配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/configs/{configId}")
    @Operation(summary = "删除配置", description = "删除指定的系统配置")
    @PreAuthorize("hasAuthority('system:config:delete')")
    @RequirePermission("system:config:delete")
    @OperationLog(module = "系统管理", operation = "删除系统配置")
    public Result<Void> deleteSystemConfig(
            @Parameter(description = "配置ID") @PathVariable @NotNull @Positive Long configId) {
        try {
            log.info("删除系统配置请求，配置ID: {}", configId);
            systemService.deleteSystemConfig(configId);
            log.info("系统配置删除成功，配置ID: {}", configId);
            return Result.success("系统配置删除成功");
        } catch (Exception e) {
            log.error("删除系统配置失败，配置ID: {}, 错误: {}", configId, e.getMessage(), e);
            return Result.failure("删除系统配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/configs/value/{configKey}")
    @Operation(summary = "获取配置值", description = "根据配置键获取配置值")
    @PreAuthorize("hasAuthority('system:config:view')")
    @RequirePermission("system:config:view")
    public Result<String> getConfigValue(
            @Parameter(description = "配置键") @PathVariable String configKey) {
        try {
            log.info("获取配置值请求，配置键: {}", configKey);
            String value = systemService.getConfigValue(configKey);
            return Result.success(value, "获取配置值成功");
        } catch (Exception e) {
            log.error("获取配置值失败，配置键: {}, 错误: {}", configKey, e.getMessage(), e);
            return Result.failure("获取配置值失败: " + e.getMessage());
        }
    }

    @GetMapping("/configs/key/{configKey}")
    @Operation(summary = "获取配置详情", description = "根据配置键获取配置详细信息")
    @PreAuthorize("hasAuthority('system:config:view')")
    @RequirePermission("system:config:view")
    public Result<ConfigResponse> getConfigByKey(
            @Parameter(description = "配置键") @PathVariable String configKey) {
        try {
            log.info("获取配置详情请求，配置键: {}", configKey);
            ConfigResponse response = systemService.getConfigByKey(configKey);
            return Result.success(response, "获取配置详情成功");
        } catch (Exception e) {
            log.error("获取配置详情失败，配置键: {}, 错误: {}", configKey, e.getMessage(), e);
            return Result.failure("获取配置详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/configs")
    @Operation(summary = "查询配置列表", description = "分页查询系统配置列表")
    @PreAuthorize("hasAuthority('system:config:view')")
    @RequirePermission("system:config:view")
    public Result<PageResult<ConfigResponse>> querySystemConfigs(
            @Valid ConfigQueryRequest request) {
        try {
            log.info("查询系统配置列表请求: {}", request);
            PageResult<ConfigResponse> result = systemService.querySystemConfigs(request);
            return Result.success(result, "查询系统配置列表成功");
        } catch (Exception e) {
            log.error("查询系统配置列表失败: {}", e.getMessage(), e);
            return Result.failure("查询系统配置列表失败: " + e.getMessage());
        }
    }

    @PutMapping("/configs/batch")
    @Operation(summary = "批量更新配置", description = "批量更新系统配置")
    @PreAuthorize("hasAuthority('system:config:update')")
    @RequirePermission("system:config:update")
    @OperationLog(module = "系统管理", operation = "批量更新系统配置")
    public Result<Void> batchUpdateConfigs(
            @RequestBody @Valid Map<String, String> configs) {
        try {
            log.info("批量更新系统配置请求，配置数量: {}", configs.size());
            systemService.batchUpdateConfigs(configs);
            log.info("批量更新系统配置成功");
            return Result.success("批量更新系统配置成功");
        } catch (Exception e) {
            log.error("批量更新系统配置失败: {}", e.getMessage(), e);
            return Result.failure("批量更新系统配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/configs/group/{group}")
    @Operation(summary = "获取分组配置", description = "获取指定分组的所有配置")
    @PreAuthorize("hasAuthority('system:config:view')")
    @RequirePermission("system:config:view")
    public Result<List<ConfigResponse>> getConfigsByGroup(
            @Parameter(description = "配置分组") @PathVariable String group) {
        try {
            log.info("获取分组配置请求，分组: {}", group);
            List<ConfigResponse> configs = systemService.getConfigsByGroup(group);
            return Result.success(configs, "获取分组配置成功");
        } catch (Exception e) {
            log.error("获取分组配置失败，分组: {}, 错误: {}", group, e.getMessage(), e);
            return Result.failure("获取分组配置失败: " + e.getMessage());
        }
    }

    // ==================== 系统统计 ====================

    @GetMapping("/statistics")
    @Operation(summary = "获取系统统计", description = "获取系统整体统计信息")
    @PreAuthorize("hasAuthority('system:statistics:view')")
    @RequirePermission("system:statistics:view")
    public Result<SystemService.SystemStatistics> getSystemStatistics() {
        try {
            log.info("获取系统统计请求");
            SystemService.SystemStatistics statistics = systemService.getSystemStatistics();
            return Result.success(statistics, "获取系统统计成功");
        } catch (Exception e) {
            log.error("获取系统统计失败: {}", e.getMessage(), e);
            return Result.failure("获取系统统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/user-activity")
    @Operation(summary = "获取用户活动统计", description = "获取指定天数内的用户活动统计")
    @PreAuthorize("hasAuthority('system:statistics:view')")
    @RequirePermission("system:statistics:view")
    public Result<List<SystemService.UserActivityStatistics>> getUserActivityStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") int days) {
        try {
            log.info("获取用户活动统计请求，天数: {}", days);
            List<SystemService.UserActivityStatistics> statistics = 
                systemService.getUserActivityStatistics(days);
            return Result.success(statistics, "获取用户活动统计成功");
        } catch (Exception e) {
            log.error("获取用户活动统计失败，天数: {}, 错误: {}", days, e.getMessage(), e);
            return Result.failure("获取用户活动统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/department")
    @Operation(summary = "获取部门统计", description = "获取各部门的统计信息")
    @PreAuthorize("hasAuthority('system:statistics:view')")
    @RequirePermission("system:statistics:view")
    public Result<List<SystemService.DepartmentStatistics>> getDepartmentStatistics() {
        try {
            log.info("获取部门统计请求");
            List<SystemService.DepartmentStatistics> statistics = 
                systemService.getDepartmentStatistics();
            return Result.success(statistics, "获取部门统计成功");
        } catch (Exception e) {
            log.error("获取部门统计失败: {}", e.getMessage(), e);
            return Result.failure("获取部门统计失败: " + e.getMessage());
        }
    }
}