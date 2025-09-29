package com.archive.management.service;

import com.archive.management.dto.statistics.ArchiveStatisticsDTO;
import com.archive.management.dto.statistics.BorrowTrendDTO;
import com.archive.management.dto.statistics.UserActivityDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 * 提供各种统计数据查询功能
 *
 * @author System
 * @version 1.0
 * @since 2025-01-20
 */
public interface StatisticsService {

    /**
     * 获取档案统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param groupBy   分组方式
     * @return 档案统计数据
     */
    ArchiveStatisticsDTO getArchiveStatistics(LocalDate startDate, LocalDate endDate, String groupBy);

    /**
     * 获取借阅趋势统计
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 统计粒度（day/week/month）
     * @return 借阅趋势数据列表
     */
    List<BorrowTrendDTO> getBorrowTrend(LocalDate startDate, LocalDate endDate, String granularity);

    /**
     * 获取用户活跃度统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param activityType 活跃类型
     * @return 用户活跃度数据列表
     */
    List<UserActivityDTO> getUserActivity(LocalDate startDate, LocalDate endDate, String activityType);

    /**
     * 获取系统概览统计
     *
     * @return 系统概览数据
     */
    Map<String, Object> getSystemOverview();

    /**
     * 获取档案分类统计
     *
     * @return 分类统计数据
     */
    Map<String, Long> getArchiveCategoryStatistics();

    /**
     * 获取借阅状态统计
     *
     * @return 借阅状态统计数据
     */
    Map<String, Long> getBorrowStatusStatistics();

    /**
     * 获取用户统计
     *
     * @return 用户统计数据
     */
    Map<String, Object> getUserStatistics();

    /**
     * 导出统计报表
     *
     * @param reportType 报表类型
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param format     导出格式
     * @return 下载链接
     */
    String exportStatistics(String reportType, LocalDate startDate, LocalDate endDate, String format);
}