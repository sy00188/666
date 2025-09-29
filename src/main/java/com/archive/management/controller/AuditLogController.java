package com.archive.management.controller;

import com.archive.management.entity.AuditLog;
import com.archive.management.service.AuditLogService;
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
 * 审计日志管理控制器
 * 提供审计日志相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "审计日志管理", description = "审计日志管理相关接口")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 创建审计日志
     */
    @PostMapping
    @Operation(summary = "创建审计日志", description = "创建新的审计日志记录")
    @PreAuthorize("hasAuthority('audit_log:create')")
    public ResponseEntity<Map<String, Object>> createAuditLog(@Valid @RequestBody AuditLog auditLog) {
        try {
            AuditLog createdLog = auditLogService.createAuditLog(auditLog);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审计日志创建成功",
                "data", createdLog
            ));
        } catch (Exception e) {
            log.error("创建审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量创建审计日志
     */
    @PostMapping("/batch")
    @Operation(summary = "批量创建审计日志", description = "批量创建审计日志记录")
    @PreAuthorize("hasAuthority('audit_log:create')")
    public ResponseEntity<Map<String, Object>> batchCreateAuditLogs(
            @Parameter(description = "审计日志列表") @RequestBody @NotEmpty List<AuditLog> auditLogs) {
        try {
            int createdCount = auditLogService.batchCreateAuditLogs(auditLogs);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量创建审计日志成功",
                "data", Map.of("createdCount", createdCount)
            ));
        } catch (Exception e) {
            log.error("批量创建审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量创建审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 记录用户登录日志
     */
    @PostMapping("/login")
    @Operation(summary = "记录用户登录日志", description = "记录用户登录操作日志")
    @PreAuthorize("hasAuthority('audit_log:create')")
    public ResponseEntity<Map<String, Object>> logUserLogin(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "IP地址") @RequestParam @NotBlank String ipAddress,
            @Parameter(description = "用户代理") @RequestParam(required = false) String userAgent,
            @Parameter(description = "登录结果") @RequestParam @NotBlank String loginResult) {
        try {
            AuditLog loginLog = auditLogService.logUserLogin(userId, ipAddress, userAgent, loginResult);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户登录日志记录成功",
                "data", loginLog
            ));
        } catch (Exception e) {
            log.error("记录用户登录日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "记录登录日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 记录用户登出日志
     */
    @PostMapping("/logout")
    @Operation(summary = "记录用户登出日志", description = "记录用户登出操作日志")
    @PreAuthorize("hasAuthority('audit_log:create')")
    public ResponseEntity<Map<String, Object>> logUserLogout(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "IP地址") @RequestParam @NotBlank String ipAddress,
            @Parameter(description = "登出原因") @RequestParam(required = false) String logoutReason) {
        try {
            AuditLog logoutLog = auditLogService.logUserLogout(userId, ipAddress, logoutReason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户登出日志记录成功",
                "data", logoutLog
            ));
        } catch (Exception e) {
            log.error("记录用户登出日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "记录登出日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 记录档案操作日志
     */
    @PostMapping("/archive-operation")
    @Operation(summary = "记录档案操作日志", description = "记录档案相关操作日志")
    @PreAuthorize("hasAuthority('audit_log:create')")
    public ResponseEntity<Map<String, Object>> logArchiveOperation(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "档案ID") @RequestParam @NotNull @Positive Long archiveId,
            @Parameter(description = "操作类型") @RequestParam @NotBlank String operationType,
            @Parameter(description = "操作描述") @RequestParam(required = false) String operationDescription,
            @Parameter(description = "IP地址") @RequestParam @NotBlank String ipAddress) {
        try {
            AuditLog operationLog = auditLogService.logArchiveOperation(userId, archiveId, operationType, 
                operationDescription, ipAddress);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "档案操作日志记录成功",
                "data", operationLog
            ));
        } catch (Exception e) {
            log.error("记录档案操作日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "记录档案操作日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 记录系统操作日志
     */
    @PostMapping("/system-operation")
    @Operation(summary = "记录系统操作日志", description = "记录系统相关操作日志")
    @PreAuthorize("hasAuthority('audit_log:create')")
    public ResponseEntity<Map<String, Object>> logSystemOperation(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "操作类型") @RequestParam @NotBlank String operationType,
            @Parameter(description = "操作描述") @RequestParam(required = false) String operationDescription,
            @Parameter(description = "IP地址") @RequestParam @NotBlank String ipAddress,
            @Parameter(description = "操作结果") @RequestParam(required = false) String operationResult) {
        try {
            AuditLog systemLog = auditLogService.logSystemOperation(userId, operationType, operationDescription, 
                ipAddress, operationResult);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "系统操作日志记录成功",
                "data", systemLog
            ));
        } catch (Exception e) {
            log.error("记录系统操作日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "记录系统操作日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取审计日志
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取审计日志", description = "根据ID获取审计日志详情")
    @PreAuthorize("hasAuthority('audit_log:read')")
    public ResponseEntity<Map<String, Object>> getAuditLogById(
            @Parameter(description = "审计日志ID") @PathVariable @NotNull @Positive Long id) {
        try {
            AuditLog auditLog = auditLogService.getAuditLogById(id);
            if (auditLog != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取审计日志成功",
                    "data", auditLog
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据用户ID查询审计日志
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询审计日志", description = "根据用户ID分页查询审计日志")
    @PreAuthorize("hasAuthority('audit_log:read')")
    public ResponseEntity<Map<String, Object>> getAuditLogsByUserId(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<AuditLog> page = new Page<>(current, size);
            IPage<AuditLog> result = auditLogService.getAuditLogsByUserId(userId, startDate, endDate, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询用户审计日志成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("根据用户ID查询审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询用户审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据操作类型查询审计日志
     */
    @GetMapping("/operation-type/{operationType}")
    @Operation(summary = "根据操作类型查询审计日志", description = "根据操作类型分页查询审计日志")
    @PreAuthorize("hasAuthority('audit_log:read')")
    public ResponseEntity<Map<String, Object>> getAuditLogsByOperationType(
            @Parameter(description = "操作类型") @PathVariable @NotBlank String operationType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<AuditLog> page = new Page<>(current, size);
            IPage<AuditLog> result = auditLogService.getAuditLogsByOperationType(operationType, startDate, endDate, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询操作类型审计日志成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("根据操作类型查询审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询操作类型审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询审计日志
     */
    @GetMapping
    @Operation(summary = "分页查询审计日志", description = "分页查询审计日志列表")
    @PreAuthorize("hasAuthority('audit_log:read')")
    public ResponseEntity<Map<String, Object>> findAuditLogsWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "IP地址") @RequestParam(required = false) String ipAddress,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<AuditLog> page = new Page<>(current, size);
            IPage<AuditLog> result = auditLogService.findAuditLogsWithPagination(page, userId, operationType, 
                ipAddress, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询审计日志成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取审计日志统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取审计日志统计信息", description = "获取审计日志统计信息")
    @PreAuthorize("hasAuthority('audit_log:statistics')")
    public ResponseEntity<Map<String, Object>> getAuditLogStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            long totalLogs = auditLogService.countAuditLogs(startDate, endDate);
            Map<String, Long> operationTypeStats = auditLogService.getOperationTypeStatistics(startDate, endDate);
            Map<String, Long> userStats = auditLogService.getUserOperationStatistics(startDate, endDate);
            Map<String, Long> dailyStats = auditLogService.getDailyOperationStatistics(startDate, endDate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取审计日志统计信息成功",
                "data", Map.of(
                    "totalLogs", totalLogs,
                    "operationTypeStats", operationTypeStats,
                    "userStats", userStats,
                    "dailyStats", dailyStats
                )
            ));
        } catch (Exception e) {
            log.error("获取审计日志统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分析用户行为
     */
    @GetMapping("/analysis/user-behavior")
    @Operation(summary = "分析用户行为", description = "分析用户行为模式")
    @PreAuthorize("hasAuthority('audit_log:analysis')")
    public ResponseEntity<Map<String, Object>> analyzeUserBehavior(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Map<String, Object> behaviorAnalysis = auditLogService.analyzeUserBehavior(userId, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户行为分析完成",
                "data", behaviorAnalysis
            ));
        } catch (Exception e) {
            log.error("分析用户行为失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "用户行为分析失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 检测异常操作
     */
    @GetMapping("/analysis/anomaly-detection")
    @Operation(summary = "检测异常操作", description = "检测异常操作行为")
    @PreAuthorize("hasAuthority('audit_log:analysis')")
    public ResponseEntity<Map<String, Object>> detectAnomalousOperations(
            @Parameter(description = "检测时间范围（小时）") @RequestParam(defaultValue = "24") int timeRangeHours,
            @Parameter(description = "异常阈值") @RequestParam(defaultValue = "5") int anomalyThreshold) {
        try {
            List<Map<String, Object>> anomalies = auditLogService.detectAnomalousOperations(timeRangeHours, anomalyThreshold);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "异常操作检测完成",
                "data", anomalies
            ));
        } catch (Exception e) {
            log.error("检测异常操作失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "异常操作检测失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    @Operation(summary = "导出审计日志", description = "导出审计日志到文件")
    @PreAuthorize("hasAuthority('audit_log:export')")
    public void exportAuditLogs(
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response) {
        try {
            auditLogService.exportAuditLogs(format, userId, operationType, startDate, endDate, response);
        } catch (Exception e) {
            log.error("导出审计日志失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 归档审计日志
     */
    @PostMapping("/archive")
    @Operation(summary = "归档审计日志", description = "归档指定时间范围的审计日志")
    @PreAuthorize("hasAuthority('audit_log:archive')")
    public ResponseEntity<Map<String, Object>> archiveAuditLogs(
            @Parameter(description = "归档截止日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime archiveBeforeDate,
            @Parameter(description = "归档位置") @RequestParam(required = false) String archiveLocation,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            Map<String, Object> archiveResult = auditLogService.archiveAuditLogs(archiveBeforeDate, archiveLocation, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审计日志归档成功",
                "data", archiveResult
            ));
        } catch (Exception e) {
            log.error("归档审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "归档审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 清理过期审计日志
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期审计日志", description = "清理过期的审计日志")
    @PreAuthorize("hasAuthority('audit_log:cleanup')")
    public ResponseEntity<Map<String, Object>> cleanupExpiredAuditLogs(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "365") int retentionDays,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            int cleanedCount = auditLogService.cleanupExpiredAuditLogs(retentionDays, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "过期审计日志清理成功",
                "data", Map.of("cleanedCount", cleanedCount)
            ));
        } catch (Exception e) {
            log.error("清理过期审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "清理过期审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 压缩审计日志
     */
    @PostMapping("/compress")
    @Operation(summary = "压缩审计日志", description = "压缩指定时间范围的审计日志")
    @PreAuthorize("hasAuthority('audit_log:compress')")
    public ResponseEntity<Map<String, Object>> compressAuditLogs(
            @Parameter(description = "压缩截止日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime compressBeforeDate,
            @Parameter(description = "压缩算法") @RequestParam(defaultValue = "gzip") String compressionAlgorithm,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            Map<String, Object> compressResult = auditLogService.compressAuditLogs(compressBeforeDate, compressionAlgorithm, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审计日志压缩成功",
                "data", compressResult
            ));
        } catch (Exception e) {
            log.error("压缩审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "压缩审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 验证审计日志完整性
     */
    @PostMapping("/verify")
    @Operation(summary = "验证审计日志完整性", description = "验证审计日志的完整性和一致性")
    @PreAuthorize("hasAuthority('audit_log:verify')")
    public ResponseEntity<Map<String, Object>> verifyAuditLogIntegrity(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "验证类型") @RequestParam(defaultValue = "full") String verificationType) {
        try {
            Map<String, Object> verificationResult = auditLogService.verifyAuditLogIntegrity(startDate, endDate, verificationType);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审计日志完整性验证完成",
                "data", verificationResult
            ));
        } catch (Exception e) {
            log.error("验证审计日志完整性失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "验证审计日志完整性失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索审计日志
     */
    @GetMapping("/search")
    @Operation(summary = "搜索审计日志", description = "根据关键词搜索审计日志")
    @PreAuthorize("hasAuthority('audit_log:read')")
    public ResponseEntity<Map<String, Object>> searchAuditLogs(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AuditLog> page = new Page<>(current, size);
            IPage<AuditLog> result = auditLogService.searchAuditLogs(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索审计日志成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索审计日志失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 高级搜索审计日志
     */
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索审计日志", description = "高级搜索审计日志")
    @PreAuthorize("hasAuthority('audit_log:read')")
    public ResponseEntity<Map<String, Object>> advancedSearchAuditLogs(
            @Parameter(description = "搜索条件") @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AuditLog> page = new Page<>(current, size);
            IPage<AuditLog> result = auditLogService.advancedSearchAuditLogs(searchCriteria, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "高级搜索审计日志成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("高级搜索审计日志失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "高级搜索审计日志失败: " + e.getMessage()
            ));
        }
    }
}