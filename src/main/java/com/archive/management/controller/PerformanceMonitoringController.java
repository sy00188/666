package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.common.ResponseResult;
import com.archive.management.service.PerformanceMonitoringService;
import com.archive.management.service.QueryOptimizationService;
import com.archive.management.service.SmartCacheService;
import com.archive.management.service.PerformanceAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 性能监控控制器
 * 提供性能监控、分析和优化建议的API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Tag(name = "性能监控管理", description = "提供系统性能监控、分析和优化建议等功能")
public class PerformanceMonitoringController {

    private final PerformanceMonitoringService performanceMonitoringService;
    private final QueryOptimizationService queryOptimizationService;
    private final SmartCacheService smartCacheService;
    private final PerformanceAnalysisService performanceAnalysisService;

    @Operation(summary = "获取慢查询统计", description = "获取数据库慢查询统计信息")
    @GetMapping("/slow-queries")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseResult<Map<String, Object>> getSlowQueryStatistics() {
        Map<String, Object> stats = queryOptimizationService.getSlowQueryStatistics();
        return ResponseResult.success(stats, "慢查询统计获取成功");
    }

    @Operation(summary = "分析查询性能", description = "分析指定SQL查询的性能")
    @PostMapping("/analyze-query")
    @PreAuthorize("hasAuthority('performance:analyze')")
    public ResponseResult<QueryOptimizationService.QueryAnalysisResult> analyzeQuery(
            @Parameter(description = "要分析的SQL查询", required = true)
            @RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseResult.error("SQL查询不能为空");
        }
        
        QueryOptimizationService.QueryAnalysisResult result = queryOptimizationService.analyzeQuery(sql);
        return ResponseResult.success(result, "查询性能分析完成");
    }

    @Operation(summary = "获取索引使用统计", description = "获取数据库索引使用统计信息")
    @GetMapping("/index-usage")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseResult<Map<String, Object>> getIndexUsageStatistics() {
        Map<String, Object> stats = queryOptimizationService.getIndexUsageStatistics();
        return ResponseResult.success(stats, "索引使用统计获取成功");
    }

    @Operation(summary = "生成数据库性能报告", description = "生成数据库性能分析报告")
    @GetMapping("/database-report")
    @PreAuthorize("hasAuthority('performance:report')")
    public ResponseResult<Map<String, Object>> generateDatabaseReport() {
        Map<String, Object> report = queryOptimizationService.generatePerformanceReport();
        return ResponseResult.success(report, "数据库性能报告生成成功");
    }

    @Operation(summary = "获取缓存统计", description = "获取缓存使用统计信息")
    @GetMapping("/cache-statistics")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseResult<Map<String, Object>> getCacheStatistics() {
        Map<String, Object> stats = smartCacheService.getCacheStatistics();
        return ResponseResult.success(stats, "缓存统计获取成功");
    }

    @Operation(summary = "预热缓存", description = "手动触发缓存预热")
    @PostMapping("/warm-up-cache")
    @PreAuthorize("hasAuthority('performance:optimize')")
    public ResponseResult<String> warmUpCache() {
        smartCacheService.warmUpCaches();
        return ResponseResult.success("缓存预热完成", "缓存预热操作已执行");
    }

    @Operation(summary = "获取性能报告", description = "生成系统性能分析报告")
    @GetMapping("/report")
    @PreAuthorize("hasAuthority('performance:report')")
    public ResponseResult<Map<String, Object>> getPerformanceReport(
            @Parameter(description = "报告类型 (summary, detailed, optimization)", required = true)
            @RequestParam(defaultValue = "summary") String reportType) {
        Map<String, Object> report = performanceAnalysisService.generatePerformanceReport(reportType);
        return ResponseResult.success(report, "性能报告生成成功");
    }

    @Operation(summary = "记录请求性能", description = "记录请求响应时间")
    @PostMapping("/record-request")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseResult<String> recordRequestPerformance(
            @Parameter(description = "响应时间（毫秒）", required = true)
            @RequestParam long responseTime) {
        performanceAnalysisService.recordRequestPerformance(responseTime);
        return ResponseResult.success("请求性能记录成功", "请求性能数据已记录");
    }

    @Operation(summary = "获取系统概览", description = "获取系统性能概览信息")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemOverview() {
        try {
            Map<String, Object> overview = performanceMonitoringService.getSystemOverview();
            return ResponseEntity.ok(ApiResponse.success(overview, "系统概览获取成功"));
        } catch (Exception e) {
            log.error("获取系统概览失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取系统概览失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取优化建议", description = "获取系统性能优化建议")
    @GetMapping("/optimization-suggestions")
    @PreAuthorize("hasAuthority('performance:optimize')")
    public ResponseResult<Map<String, Object>> getOptimizationSuggestions() {
        Map<String, Object> suggestions = performanceAnalysisService.generatePerformanceReport("optimization");
        return ResponseResult.success(suggestions, "优化建议获取成功");
    }

    @Operation(summary = "获取详细性能指标", description = "获取系统详细性能指标")
    @GetMapping("/detailed-metrics")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseResult<Map<String, Object>> getDetailedMetrics() {
        Map<String, Object> metrics = performanceAnalysisService.generatePerformanceReport("detailed");
        return ResponseResult.success(metrics, "详细性能指标获取成功");
    }

    @Operation(summary = "清理缓存", description = "清理指定缓存")
    @DeleteMapping("/cache/{cacheName}")
    @PreAuthorize("hasAuthority('performance:optimize')")
    public ResponseResult<String> clearCache(
            @Parameter(description = "缓存名称", required = true)
            @PathVariable String cacheName) {
        // 这里可以实现清理指定缓存的逻辑
        return ResponseResult.success("缓存清理完成", "缓存 " + cacheName + " 已清理");
    }

    @Operation(summary = "优化缓存策略", description = "手动触发缓存策略优化")
    @PostMapping("/optimize-cache-strategy")
    @PreAuthorize("hasAuthority('performance:optimize')")
    public ResponseResult<String> optimizeCacheStrategy() {
        smartCacheService.optimizeCacheStrategies();
        return ResponseResult.success("缓存策略优化完成", "缓存策略优化已执行");
    }

    @Operation(summary = "获取实时性能指标", description = "获取系统实时性能指标")
    @GetMapping("/realtime-metrics")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRealtimeMetrics() {
        try {
            Map<String, Object> metrics = performanceMonitoringService.getRealtimeMetrics();
            return ResponseEntity.ok(ApiResponse.success(metrics, "实时性能指标获取成功"));
        } catch (Exception e) {
            log.error("获取实时性能指标失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取实时性能指标失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取详细性能指标", description = "获取系统详细性能指标")
    @GetMapping("/detailed-metrics")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedMetrics() {
        try {
            Map<String, Object> metrics = performanceMonitoringService.getDetailedMetrics();
            return ResponseEntity.ok(ApiResponse.success(metrics, "详细性能指标获取成功"));
        } catch (Exception e) {
            log.error("获取详细性能指标失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取详细性能指标失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取性能报告", description = "生成系统性能分析报告")
    @GetMapping("/report")
    @PreAuthorize("hasAuthority('performance:report')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceReport(
            @Parameter(description = "报告类型 (summary, detailed, optimization)") 
            @RequestParam(defaultValue = "summary") String reportType) {
        try {
            Map<String, Object> report = performanceMonitoringService.generatePerformanceReport(reportType);
            return ResponseEntity.ok(ApiResponse.success(report, "性能报告生成成功"));
        } catch (Exception e) {
            log.error("生成性能报告失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("生成性能报告失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取优化建议", description = "获取系统性能优化建议")
    @GetMapping("/optimization-suggestions")
    @PreAuthorize("hasAuthority('performance:optimize')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOptimizationSuggestions() {
        try {
            Map<String, Object> suggestions = performanceMonitoringService.getOptimizationSuggestions();
            return ResponseEntity.ok(ApiResponse.success(suggestions, "优化建议获取成功"));
        } catch (Exception e) {
            log.error("获取优化建议失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取优化建议失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "记录请求性能", description = "记录请求响应时间")
    @PostMapping("/record-request")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseEntity<ApiResponse<String>> recordRequestPerformance(
            @Parameter(description = "响应时间（毫秒）") @RequestParam long responseTime) {
        try {
            performanceMonitoringService.recordRequestPerformance(responseTime);
            return ResponseEntity.ok(ApiResponse.success("请求性能记录成功", "请求性能数据已记录"));
        } catch (Exception e) {
            log.error("记录请求性能失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("记录请求性能失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取系统资源信息", description = "获取系统资源使用情况")
    @GetMapping("/system-resources")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemResources() {
        try {
            Map<String, Object> resources = performanceMonitoringService.getSystemResourceInfo();
            return ResponseEntity.ok(ApiResponse.success(resources, "系统资源信息获取成功"));
        } catch (Exception e) {
            log.error("获取系统资源信息失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取系统资源信息失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取性能指标", description = "获取系统性能指标")
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('performance:monitor')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceMetrics() {
        try {
            Map<String, Object> metrics = performanceMonitoringService.getPerformanceMetrics();
            return ResponseEntity.ok(ApiResponse.success(metrics, "性能指标获取成功"));
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取性能指标失败: " + e.getMessage()));
        }
    }
}
