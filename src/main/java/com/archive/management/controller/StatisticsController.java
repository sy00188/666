package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.dto.statistics.ArchiveStatisticsDTO;
import com.archive.management.dto.statistics.BorrowTrendDTO;
import com.archive.management.dto.statistics.UserActivityDTO;
import com.archive.management.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计报表控制器
 * 提供各种统计数据和报表功能
 *
 * @author System
 * @version 1.0
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/system/statistics")
@RequiredArgsConstructor
@Tag(name = "统计报表", description = "统计报表相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取档案统计数据
     */
    @GetMapping("/archives")
    @Operation(summary = "获取档案统计数据", description = "获取档案数量、分类等统计信息")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<ArchiveStatisticsDTO> getArchiveStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "分组方式") @RequestParam(defaultValue = "category") String groupBy) {
        
        log.info("获取档案统计数据，开始日期：{}，结束日期：{}，分组方式：{}", startDate, endDate, groupBy);
        
        ArchiveStatisticsDTO statistics = statisticsService.getArchiveStatistics(startDate, endDate, groupBy);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取借阅趋势统计
     */
    @GetMapping("/borrow-trend")
    @Operation(summary = "获取借阅趋势统计", description = "获取指定时间段内的借阅趋势数据")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<List<BorrowTrendDTO>> getBorrowTrend(
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "统计粒度") @RequestParam(defaultValue = "day") String granularity) {
        
        log.info("获取借阅趋势统计，开始日期：{}，结束日期：{}，统计粒度：{}", startDate, endDate, granularity);
        
        List<BorrowTrendDTO> trendData = statisticsService.getBorrowTrend(startDate, endDate, granularity);
        return ApiResponse.success(trendData);
    }

    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/user-activity")
    @Operation(summary = "获取用户活跃度统计", description = "获取用户活跃度相关统计数据")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<List<UserActivityDTO>> getUserActivity(
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "login") String activityType) {
        
        log.info("获取用户活跃度统计，开始日期：{}，结束日期：{}，活跃类型：{}", startDate, endDate, activityType);
        
        List<UserActivityDTO> activityData = statisticsService.getUserActivity(startDate, endDate, activityType);
        return ApiResponse.success(activityData);
    }

    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "获取系统概览统计", description = "获取系统整体统计数据")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<Map<String, Object>> getSystemOverview() {
        log.info("获取系统概览统计");
        
        Map<String, Object> overview = statisticsService.getSystemOverview();
        return ApiResponse.success(overview);
    }

    /**
     * 获取档案分类统计
     */
    @GetMapping("/archives/category")
    @Operation(summary = "获取档案分类统计", description = "获取各分类档案数量统计")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<Map<String, Long>> getArchiveCategoryStatistics() {
        log.info("获取档案分类统计");
        
        Map<String, Long> categoryStats = statisticsService.getArchiveCategoryStatistics();
        return ApiResponse.success(categoryStats);
    }

    /**
     * 获取借阅状态统计
     */
    @GetMapping("/borrows/status")
    @Operation(summary = "获取借阅状态统计", description = "获取各状态借阅记录数量统计")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<Map<String, Long>> getBorrowStatusStatistics() {
        log.info("获取借阅状态统计");
        
        Map<String, Long> statusStats = statisticsService.getBorrowStatusStatistics();
        return ApiResponse.success(statusStats);
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户统计", description = "获取用户相关统计数据")
    @PreAuthorize("hasAuthority('statistics:view')")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        log.info("获取用户统计");
        
        Map<String, Object> userStats = statisticsService.getUserStatistics();
        return ApiResponse.success(userStats);
    }

    /**
     * 导出统计报表
     */
    @PostMapping("/export")
    @Operation(summary = "导出统计报表", description = "导出指定类型的统计报表")
    @PreAuthorize("hasAuthority('statistics:export')")
    public ApiResponse<String> exportStatistics(
            @Parameter(description = "报表类型") @RequestParam String reportType,
            @Parameter(description = "开始日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format) {
        
        log.info("导出统计报表，类型：{}，开始日期：{}，结束日期：{}，格式：{}", 
                reportType, startDate, endDate, format);
        
        String downloadUrl = statisticsService.exportStatistics(reportType, startDate, endDate, format);
        return ApiResponse.success(downloadUrl);
    }
}