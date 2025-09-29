package com.archive.management.controller;

import com.archive.management.dto.*;
import com.archive.management.entity.User;
import com.archive.management.service.UserService;
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
 * 用户管理控制器
 * 提供用户相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserResponse createdUser = userService.createUser(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户创建成功",
                "data", createdUser
            ));
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户", description = "根据ID获取用户信息")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Map<String, Object>> getUserById(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id) {
        try {
            UserResponse user = userService.getUserResponseById(id);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取用户成功",
                    "data", user
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户信息")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Map<String, Object>> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable @NotBlank String username) {
        try {
            UserResponse user = userService.getUserResponseByUsername(username);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取用户成功",
                    "data", user
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据用户名获取用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据邮箱获取用户
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取用户", description = "根据邮箱获取用户信息")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Map<String, Object>> getUserByEmail(
            @Parameter(description = "邮箱") @PathVariable @NotBlank String email) {
        try {
            UserResponse user = userService.getUserResponseByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取用户成功",
                    "data", user
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据邮箱获取用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        try {
            request.setId(id); // 确保ID一致
            UserResponse updatedUser = userService.updateUser(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户更新成功",
                "data", updatedUser
            ));
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "软删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean deleted = userService.deleteUser(id, deletedBy);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "批量软删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Map<String, Object>> batchDeleteUsers(
            @Parameter(description = "用户ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            int deletedCount = userService.batchDeleteUsers(ids, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除用户成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用用户", description = "启用用户账户")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Map<String, Object>> enableUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean enabled = userService.enableUser(id, updatedBy);
            if (enabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户启用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户启用失败"
                ));
            }
        } catch (Exception e) {
            log.error("启用用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "启用用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用用户", description = "禁用用户账户")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Map<String, Object>> disableUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean disabled = userService.disableUser(id, updatedBy);
            if (disabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户禁用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户禁用失败"
                ));
            }
        } catch (Exception e) {
            log.error("禁用用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "禁用用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 锁定用户
     */
    @PutMapping("/{id}/lock")
    @Operation(summary = "锁定用户", description = "锁定用户账户")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Map<String, Object>> lockUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean locked = userService.lockUser(id, updatedBy);
            if (locked) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户锁定成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户锁定失败"
                ));
            }
        } catch (Exception e) {
            log.error("锁定用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "锁定用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{id}/unlock")
    @Operation(summary = "解锁用户", description = "解锁用户账户")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Map<String, Object>> unlockUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "更新人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean unlocked = userService.unlockUser(id, updatedBy);
            if (unlocked) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "用户解锁成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户解锁失败"
                ));
            }
        } catch (Exception e) {
            log.error("解锁用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "解锁用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "重置用户密码")
    @PreAuthorize("hasAuthority('user:reset-password')")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            // 设置用户ID到请求对象中
            request.setUserId(id);
            boolean reset = userService.resetPassword(request);
            if (reset) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "密码重置成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "密码重置失败"
                ));
            }
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "重置密码失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/change-password")
    @Operation(summary = "修改密码", description = "用户修改密码")
    @PreAuthorize("hasAuthority('user:change-password') or #id == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            // 设置用户ID到请求对象中
            request.setUserId(id);
            boolean changed = userService.changePassword(request);
            if (changed) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "密码修改成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "密码修改失败，请检查旧密码是否正确"
                ));
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "修改密码失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Map<String, Object>> findUsersWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "职位ID") @RequestParam(required = false) Long positionId,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<User> page = new Page<>(current, size);
            IPage<User> result = userService.findUsersWithPagination(page, username, email, phone, 
                realName, status, departmentId, positionId, createdBy, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询用户成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息", description = "获取用户统计信息")
    @PreAuthorize("hasAuthority('user:statistics')")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        try {
            long totalUsers = userService.countUsers();
            long activeUsers = userService.countUsersByStatus(1);
            long inactiveUsers = userService.countUsersByStatus(0);
            long lockedUsers = userService.countUsersByStatus(2);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", Map.of(
                    "totalUsers", totalUsers,
                    "activeUsers", activeUsers,
                    "inactiveUsers", inactiveUsers,
                    "lockedUsers", lockedUsers
                )
            ));
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户状态统计
     */
    @GetMapping("/statistics/status")
    @Operation(summary = "获取用户状态统计", description = "获取用户状态统计")
    @PreAuthorize("hasAuthority('user:statistics')")
    public ResponseEntity<Map<String, Object>> getStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = userService.getStatusStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取状态统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取用户状态统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取状态统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户注册趋势
     */
    @GetMapping("/statistics/registration-trend")
    @Operation(summary = "获取用户注册趋势", description = "获取用户注册趋势统计")
    @PreAuthorize("hasAuthority('user:statistics')")
    public ResponseEntity<Map<String, Object>> getRegistrationTrend(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") int days) {
        try {
            List<Map<String, Object>> trend = userService.getRegistrationTrend(days);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取注册趋势成功",
                "data", trend
            ));
        } catch (Exception e) {
            log.error("获取用户注册趋势失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取注册趋势失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public ResponseEntity<Map<String, Object>> checkUsername(
            @Parameter(description = "用户名") @RequestParam @NotBlank String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查用户名成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查用户名失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查用户名失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @Parameter(description = "邮箱") @RequestParam @NotBlank String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查邮箱成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查邮箱失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查邮箱失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已存在")
    public ResponseEntity<Map<String, Object>> checkPhone(
            @Parameter(description = "手机号") @RequestParam @NotBlank String phone) {
        try {
            boolean exists = userService.existsByPhone(phone);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "检查手机号成功",
                "data", Map.of("exists", exists)
            ));
        } catch (Exception e) {
            log.error("检查手机号失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "检查手机号失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<User> page = new Page<>(current, size);
            IPage<User> result = userService.searchUsers(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索用户成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出用户
     */
    @PostMapping("/export")
    @Operation(summary = "导出用户", description = "导出用户数据")
    @PreAuthorize("hasAuthority('user:export')")
    public ResponseEntity<Map<String, Object>> exportUsers(
            @Parameter(description = "用户ID列表") @RequestBody(required = false) List<Long> userIds,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "包含字段") @RequestParam(required = false) List<String> includeFields) {
        try {
            Map<String, Object> result = userService.exportUsers(userIds, format, includeFields);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出用户成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导出用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导出用户失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导入用户
     */
    @PostMapping("/import")
    @Operation(summary = "导入用户", description = "导入用户数据")
    @PreAuthorize("hasAuthority('user:import')")
    public ResponseEntity<Map<String, Object>> importUsers(
            @Parameter(description = "用户数据") @RequestBody List<User> users,
            @Parameter(description = "导入模式") @RequestParam(defaultValue = "merge") String importMode,
            @Parameter(description = "导入人ID") @RequestParam @NotNull @Positive Long importedBy) {
        try {
            Map<String, Object> result = userService.importUsers(users, importMode, importedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导入用户成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导入用户失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导入用户失败: " + e.getMessage()
            ));
        }
    }
}