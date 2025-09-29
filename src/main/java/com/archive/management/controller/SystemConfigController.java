package com.archive.management.controller;

import com.archive.management.entity.SystemConfig;
import com.archive.management.service.SystemConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 * 提供系统配置相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/system-configs")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统配置管理", description = "系统配置管理相关接口")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 创建系统配置
     */
    @PostMapping
    @Operation(summary = "创建系统配置", description = "创建新的系统配置项")
    @PreAuthorize("hasAuthority('system_config:create')")
    public ResponseEntity<Map<String, Object>> createSystemConfig(@Valid @RequestBody SystemConfig systemConfig) {
        try {
            SystemConfig createdConfig = systemConfigService.createSystemConfig(systemConfig);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统配置创建成功",
                "data", createdConfig
            ));
        } catch (Exception e) {
            log.error("创建系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量创建系统配置
     */
    @PostMapping("/batch")
    @Operation(summary = "批量创建系统配置", description = "批量创建系统配置项")
    @PreAuthorize("hasAuthority('system_config:create')")
    public ResponseEntity<Map<String, Object>> batchCreateSystemConfigs(
            @Parameter(description = "系统配置列表") @RequestBody @NotEmpty List<SystemConfig> systemConfigs) {
        try {
            int createdCount = systemConfigService.batchCreateSystemConfigs(systemConfigs);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量创建系统配置成功",
                "data", Map.of("createdCount", createdCount)
            ));
        } catch (Exception e) {
            log.error("批量创建系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量创建系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取系统配置
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取系统配置", description = "根据ID获取系统配置详情")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> getSystemConfigById(
            @Parameter(description = "系统配置ID") @PathVariable @NotNull @Positive Long id) {
        try {
            SystemConfig systemConfig = systemConfigService.getSystemConfigById(id);
            if (systemConfig != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取系统配置成功",
                    "data", systemConfig
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据配置键获取系统配置
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据配置键获取系统配置", description = "根据配置键获取系统配置")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> getSystemConfigByKey(
            @Parameter(description = "配置键") @PathVariable @NotBlank String configKey) {
        try {
            SystemConfig systemConfig = systemConfigService.getSystemConfigByKey(configKey);
            if (systemConfig != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取系统配置成功",
                    "data", systemConfig
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据配置键获取系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据配置名称获取系统配置
     */
    @GetMapping("/name/{configName}")
    @Operation(summary = "根据配置名称获取系统配置", description = "根据配置名称获取系统配置")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> getSystemConfigByName(
            @Parameter(description = "配置名称") @PathVariable @NotBlank String configName) {
        try {
            SystemConfig systemConfig = systemConfigService.getSystemConfigByName(configName);
            if (systemConfig != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取系统配置成功",
                    "data", systemConfig
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据配置名称获取系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "获取配置值", description = "根据配置键获取配置值")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> getConfigValue(
            @Parameter(description = "配置键") @PathVariable @NotBlank String configKey,
            @Parameter(description = "默认值") @RequestParam(required = false) String defaultValue) {
        try {
            String configValue = systemConfigService.getConfigValue(configKey, defaultValue);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取配置值成功",
                "data", Map.of("configValue", configValue)
            ));
        } catch (Exception e) {
            log.error("获取配置值失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取配置值失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取解密后的配置值
     */
    @GetMapping("/decrypted-value/{configKey}")
    @Operation(summary = "获取解密后的配置值", description = "根据配置键获取解密后的配置值")
    @PreAuthorize("hasAuthority('system_config:decrypt')")
    public ResponseEntity<Map<String, Object>> getDecryptedConfigValue(
            @Parameter(description = "配置键") @PathVariable @NotBlank String configKey,
            @Parameter(description = "默认值") @RequestParam(required = false) String defaultValue) {
        try {
            String decryptedValue = systemConfigService.getDecryptedConfigValue(configKey, defaultValue);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取解密配置值成功",
                "data", Map.of("decryptedValue", decryptedValue)
            ));
        } catch (Exception e) {
            log.error("获取解密配置值失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取解密配置值失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据分组获取系统配置
     */
    @GetMapping("/group/{configGroup}")
    @Operation(summary = "根据分组获取系统配置", description = "根据分组获取系统配置列表")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> getSystemConfigsByGroup(
            @Parameter(description = "配置分组") @PathVariable @NotBlank String configGroup) {
        try {
            List<SystemConfig> systemConfigs = systemConfigService.getSystemConfigsByGroup(configGroup);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取分组系统配置成功",
                "data", systemConfigs
            ));
        } catch (Exception e) {
            log.error("根据分组获取系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取分组系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新系统配置", description = "更新系统配置信息")
    @PreAuthorize("hasAuthority('system_config:update')")
    public ResponseEntity<Map<String, Object>> updateSystemConfig(
            @Parameter(description = "系统配置ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody SystemConfig systemConfig) {
        try {
            systemConfig.setId(id);
            SystemConfig updatedConfig = systemConfigService.updateSystemConfig(systemConfig);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统配置更新成功",
                "data", updatedConfig
            ));
        } catch (Exception e) {
            log.error("更新系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量更新系统配置
     */
    @PutMapping("/batch")
    @Operation(summary = "批量更新系统配置", description = "批量更新系统配置")
    @PreAuthorize("hasAuthority('system_config:update')")
    public ResponseEntity<Map<String, Object>> batchUpdateSystemConfigs(
            @Parameter(description = "系统配置列表") @RequestBody @NotEmpty List<SystemConfig> systemConfigs) {
        try {
            int updatedCount = systemConfigService.batchUpdateSystemConfigs(systemConfigs);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量更新系统配置成功",
                "data", Map.of("updatedCount", updatedCount)
            ));
        } catch (Exception e) {
            log.error("批量更新系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量更新系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新配置值
     */
    @PutMapping("/value/{configKey}")
    @Operation(summary = "更新配置值", description = "根据配置键更新配置值")
    @PreAuthorize("hasAuthority('system_config:update')")
    public ResponseEntity<Map<String, Object>> updateConfigValue(
            @Parameter(description = "配置键") @PathVariable @NotBlank String configKey,
            @Parameter(description = "配置值") @RequestParam @NotBlank String configValue,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean updated = systemConfigService.updateConfigValue(configKey, configValue, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", updated,
                "message", updated ? "配置值更新成功" : "配置值更新失败",
                "data", Map.of("updated", updated)
            ));
        } catch (Exception e) {
            log.error("更新配置值失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新配置值失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新加密配置值
     */
    @PutMapping("/encrypted-value/{configKey}")
    @Operation(summary = "更新加密配置值", description = "根据配置键更新加密配置值")
    @PreAuthorize("hasAuthority('system_config:encrypt')")
    public ResponseEntity<Map<String, Object>> updateEncryptedConfigValue(
            @Parameter(description = "配置键") @PathVariable @NotBlank String configKey,
            @Parameter(description = "配置值") @RequestParam @NotBlank String configValue,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long updatedBy) {
        try {
            boolean updated = systemConfigService.updateEncryptedConfigValue(configKey, configValue, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", updated,
                "message", updated ? "加密配置值更新成功" : "加密配置值更新失败",
                "data", Map.of("updated", updated)
            ));
        } catch (Exception e) {
            log.error("更新加密配置值失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新加密配置值失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统配置", description = "删除系统配置")
    @PreAuthorize("hasAuthority('system_config:delete')")
    public ResponseEntity<Map<String, Object>> deleteSystemConfig(
            @Parameter(description = "系统配置ID") @PathVariable @NotNull @Positive Long id) {
        try {
            boolean deleted = systemConfigService.deleteSystemConfig(id);
            return ResponseEntity.ok(Map.of(
                "success", deleted,
                "message", deleted ? "系统配置删除成功" : "系统配置删除失败",
                "data", Map.of("deleted", deleted)
            ));
        } catch (Exception e) {
            log.error("删除系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除系统配置
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除系统配置", description = "批量删除系统配置")
    @PreAuthorize("hasAuthority('system_config:delete')")
    public ResponseEntity<Map<String, Object>> batchDeleteSystemConfigs(
            @Parameter(description = "系统配置ID列表") @RequestBody @NotEmpty List<Long> ids) {
        try {
            int deletedCount = systemConfigService.batchDeleteSystemConfigs(ids);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除系统配置成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询系统配置
     */
    @GetMapping
    @Operation(summary = "分页查询系统配置", description = "分页查询系统配置列表")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> findSystemConfigsWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "配置键") @RequestParam(required = false) String configKey,
            @Parameter(description = "配置名称") @RequestParam(required = false) String configName,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled) {
        try {
            Page<SystemConfig> page = new Page<>(current, size);
            IPage<SystemConfig> result = systemConfigService.findSystemConfigsWithPagination(page, configGroup, 
                configKey, configName, enabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询系统配置成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取系统配置统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取系统配置统计信息", description = "获取系统配置统计信息")
    @PreAuthorize("hasAuthority('system_config:statistics')")
    public ResponseEntity<Map<String, Object>> getSystemConfigStatistics() {
        try {
            long totalConfigs = systemConfigService.countSystemConfigs();
            long enabledConfigs = systemConfigService.countEnabledSystemConfigs();
            long disabledConfigs = systemConfigService.countDisabledSystemConfigs();
            Map<String, Long> groupStats = systemConfigService.getConfigGroupStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取系统配置统计信息成功",
                "data", Map.of(
                    "totalConfigs", totalConfigs,
                    "enabledConfigs", enabledConfigs,
                    "disabledConfigs", disabledConfigs,
                    "groupStats", groupStats
                )
            ));
        } catch (Exception e) {
            log.error("获取系统配置统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导入系统配置
     */
    @PostMapping("/import")
    @Operation(summary = "导入系统配置", description = "从文件导入系统配置")
    @PreAuthorize("hasAuthority('system_config:import')")
    public ResponseEntity<Map<String, Object>> importSystemConfigs(
            @Parameter(description = "配置文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "导入格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "是否覆盖") @RequestParam(defaultValue = "false") boolean overwrite,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long importedBy) {
        try {
            Map<String, Object> importResult = systemConfigService.importSystemConfigs(file, format, overwrite, importedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统配置导入成功",
                "data", importResult
            ));
        } catch (Exception e) {
            log.error("导入系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导入系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出系统配置
     */
    @GetMapping("/export")
    @Operation(summary = "导出系统配置", description = "导出系统配置到文件")
    @PreAuthorize("hasAuthority('system_config:export')")
    public void exportSystemConfigs(
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "配置分组") @RequestParam(required = false) String configGroup,
            @Parameter(description = "是否包含敏感信息") @RequestParam(defaultValue = "false") boolean includeSensitive,
            HttpServletResponse response) {
        try {
            systemConfigService.exportSystemConfigs(format, configGroup, includeSensitive, response);
        } catch (Exception e) {
            log.error("导出系统配置失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 备份系统配置
     */
    @PostMapping("/backup")
    @Operation(summary = "备份系统配置", description = "备份系统配置")
    @PreAuthorize("hasAuthority('system_config:backup')")
    public ResponseEntity<Map<String, Object>> backupSystemConfigs(
            @Parameter(description = "备份位置") @RequestParam(required = false) String backupLocation,
            @Parameter(description = "备份格式") @RequestParam(defaultValue = "json") String backupFormat,
            @Parameter(description = "是否包含敏感信息") @RequestParam(defaultValue = "false") boolean includeSensitive,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            Map<String, Object> backupResult = systemConfigService.backupSystemConfigs(backupLocation, backupFormat, 
                includeSensitive, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统配置备份成功",
                "data", backupResult
            ));
        } catch (Exception e) {
            log.error("备份系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "备份系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 恢复系统配置
     */
    @PostMapping("/restore")
    @Operation(summary = "恢复系统配置", description = "从备份恢复系统配置")
    @PreAuthorize("hasAuthority('system_config:restore')")
    public ResponseEntity<Map<String, Object>> restoreSystemConfigs(
            @Parameter(description = "备份文件") @RequestParam("file") MultipartFile backupFile,
            @Parameter(description = "恢复策略") @RequestParam(defaultValue = "merge") String restoreStrategy,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            Map<String, Object> restoreResult = systemConfigService.restoreSystemConfigs(backupFile, restoreStrategy, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统配置恢复成功",
                "data", restoreResult
            ));
        } catch (Exception e) {
            log.error("恢复系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "恢复系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 监控配置变更
     */
    @GetMapping("/changes")
    @Operation(summary = "监控配置变更", description = "获取配置变更历史")
    @PreAuthorize("hasAuthority('system_config:monitor')")
    public ResponseEntity<Map<String, Object>> getConfigChanges(
            @Parameter(description = "配置键") @RequestParam(required = false) String configKey,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Map<String, Object>> page = new Page<>(current, size);
            IPage<Map<String, Object>> result = systemConfigService.getConfigChanges(configKey, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取配置变更历史成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取配置变更历史失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取配置变更历史失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 验证配置完整性
     */
    @PostMapping("/validate")
    @Operation(summary = "验证配置完整性", description = "验证系统配置的完整性和有效性")
    @PreAuthorize("hasAuthority('system_config:validate')")
    public ResponseEntity<Map<String, Object>> validateSystemConfigs(
            @Parameter(description = "验证类型") @RequestParam(defaultValue = "all") String validationType) {
        try {
            Map<String, Object> validationResult = systemConfigService.validateSystemConfigs(validationType);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统配置验证完成",
                "data", validationResult
            ));
        } catch (Exception e) {
            log.error("验证系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "验证系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索系统配置
     */
    @GetMapping("/search")
    @Operation(summary = "搜索系统配置", description = "根据关键词搜索系统配置")
    @PreAuthorize("hasAuthority('system_config:read')")
    public ResponseEntity<Map<String, Object>> searchSystemConfigs(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<SystemConfig> page = new Page<>(current, size);
            IPage<SystemConfig> result = systemConfigService.searchSystemConfigs(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索系统配置成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索系统配置失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索系统配置失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新配置缓存", description = "刷新系统配置缓存")
    @PreAuthorize("hasAuthority('system_config:cache')")
    public ResponseEntity<Map<String, Object>> refreshConfigCache(
            @Parameter(description = "配置键") @RequestParam(required = false) String configKey) {
        try {
            boolean refreshed = systemConfigService.refreshConfigCache(configKey);
            return ResponseEntity.ok(Map.of(
                "success", refreshed,
                "message", refreshed ? "配置缓存刷新成功" : "配置缓存刷新失败",
                "data", Map.of("refreshed", refreshed)
            ));
        } catch (Exception e) {
            log.error("刷新配置缓存失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "刷新配置缓存失败: " + e.getMessage()
            ));
        }
    }
}