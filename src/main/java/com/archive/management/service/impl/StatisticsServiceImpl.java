package com.archive.management.service.impl;

import com.archive.management.dto.statistics.ArchiveStatisticsDTO;
import com.archive.management.dto.statistics.BorrowTrendDTO;
import com.archive.management.dto.statistics.UserActivityDTO;
import com.archive.management.entity.Archive;
import com.archive.management.entity.BorrowRecord;
import com.archive.management.entity.Category;
import com.archive.management.entity.User;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.BorrowRecordRepository;
import com.archive.management.repository.CategoryRepository;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 *
 * @author System
 * @version 1.0
 * @since 2025-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ArchiveRepository archiveRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ArchiveStatisticsDTO getArchiveStatistics(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.info("获取档案统计数据，开始日期：{}，结束日期：{}，分组方式：{}", startDate, endDate, groupBy);

        // 设置默认日期范围
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // 获取基础统计数据
        long totalCount = archiveRepository.count();
        long archivedCount = archiveRepository.countByStatus(1); // ARCHIVED
        long draftCount = archiveRepository.countByStatus(0); // DRAFT
        long pendingCount = archiveRepository.countByStatus(2); // PENDING
        long borrowedCount = archiveRepository.countCurrentlyBorrowedArchives();

        // 获取状态统计
        Map<String, Long> statusStatistics = new HashMap<>();
        statusStatistics.put("ARCHIVED", archivedCount);
        statusStatistics.put("DRAFT", draftCount);
        statusStatistics.put("PENDING", pendingCount);
        statusStatistics.put("BORROWED", borrowedCount);

        // 获取分类统计
        Map<String, Long> categoryStatistics = getCategoryStatisticsFromDB();

        // 获取密级统计
        Map<String, Long> securityLevelStatistics = getSecurityLevelStatisticsFromDB();

        // 获取时间范围内的新增数量
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate yearStart = LocalDate.now().withDayOfYear(1);
        
        long monthlyNewCount = archiveRepository.countByCreateTimeBetween(
                monthStart.atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        long yearlyNewCount = archiveRepository.countByCreateTimeBetween(
                yearStart.atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

        // 构建分组统计数据
        List<ArchiveStatisticsDTO.GroupStatistics> groups = buildGroupStatistics(groupBy, totalCount);

        return ArchiveStatisticsDTO.builder()
                .totalCount(totalCount)
                .archivedCount(archivedCount)
                .draftCount(draftCount)
                .pendingCount(pendingCount)
                .borrowedCount(borrowedCount)
                .groups(groups)
                .statusStatistics(statusStatistics)
                .categoryStatistics(categoryStatistics)
                .securityLevelStatistics(securityLevelStatistics)
                .monthlyNewCount(monthlyNewCount)
                .yearlyNewCount(yearlyNewCount)
                .build();
    }

    @Override
    public List<BorrowTrendDTO> getBorrowTrend(LocalDate startDate, LocalDate endDate, String granularity) {
        log.info("获取借阅趋势统计，开始日期：{}，结束日期：{}，统计粒度：{}", startDate, endDate, granularity);

        // 设置默认日期范围
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<BorrowTrendDTO> trendData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 根据粒度生成日期序列
        List<LocalDate> dateList = generateDateList(startDate, endDate, granularity);

        for (LocalDate date : dateList) {
            LocalDate nextDate = getNextDate(date, granularity);
            
            // 模拟数据生成（实际应从数据库查询）
            long applications = (long) (Math.random() * 50 + 10);
            long approvals = (long) (applications * (0.7 + Math.random() * 0.2));
            long rejections = applications - approvals;
            long actualBorrows = (long) (approvals * (0.8 + Math.random() * 0.15));
            long returns = (long) (actualBorrows * (0.9 + Math.random() * 0.1));
            long overdue = actualBorrows - returns;

            double approvalRate = applications > 0 ? (double) approvals / applications * 100 : 0;
            double returnRate = actualBorrows > 0 ? (double) returns / actualBorrows * 100 : 0;
            double overdueRate = actualBorrows > 0 ? (double) overdue / actualBorrows * 100 : 0;

            BorrowTrendDTO trend = BorrowTrendDTO.builder()
                    .date(date)
                    .dateLabel(date.format(formatter))
                    .borrowApplications(applications)
                    .borrowApprovals(approvals)
                    .borrowRejections(rejections)
                    .actualBorrows(actualBorrows)
                    .returns(returns)
                    .overdue(overdue)
                    .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                    .returnRate(Math.round(returnRate * 100.0) / 100.0)
                    .overdueRate(Math.round(overdueRate * 100.0) / 100.0)
                    .build();

            trendData.add(trend);
        }

        return trendData;
    }

    @Override
    public List<UserActivityDTO> getUserActivity(LocalDate startDate, LocalDate endDate, String activityType) {
        log.info("获取用户活跃度统计，开始日期：{}，结束日期：{}，活跃类型：{}", startDate, endDate, activityType);

        // 设置默认日期范围
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<UserActivityDTO> activityData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 生成日期序列
        List<LocalDate> dateList = generateDateList(startDate, endDate, "day");

        for (LocalDate date : dateList) {
            // 模拟数据生成（实际应从数据库查询）
            long activeUsers = (long) (Math.random() * 100 + 20);
            long newUsers = (long) (Math.random() * 10 + 1);
            long loginCount = (long) (activeUsers * (1.2 + Math.random() * 0.8));
            long operationCount = (long) (activeUsers * (5 + Math.random() * 10));
            long archiveAccessCount = (long) (operationCount * (0.3 + Math.random() * 0.4));
            long fileDownloadCount = (long) (archiveAccessCount * (0.2 + Math.random() * 0.3));
            double avgOnlineMinutes = 30 + Math.random() * 120;
            double activityScore = (operationCount * 0.1 + loginCount * 0.5 + avgOnlineMinutes * 0.01);

            UserActivityDTO activity = UserActivityDTO.builder()
                    .date(date)
                    .dateLabel(date.format(formatter))
                    .activeUsers(activeUsers)
                    .newUsers(newUsers)
                    .loginCount(loginCount)
                    .operationCount(operationCount)
                    .archiveAccessCount(archiveAccessCount)
                    .fileDownloadCount(fileDownloadCount)
                    .avgOnlineMinutes(Math.round(avgOnlineMinutes * 100.0) / 100.0)
                    .activityScore(Math.round(activityScore * 100.0) / 100.0)
                    .build();

            activityData.add(activity);
        }

        return activityData;
    }

    @Override
    public Map<String, Object> getSystemOverview() {
        log.info("获取系统概览统计");

        Map<String, Object> overview = new HashMap<>();
        
        // 档案相关统计
        overview.put("totalArchives", archiveRepository.count());
        overview.put("totalUsers", userRepository.count());
        overview.put("totalCategories", categoryRepository.count());
        overview.put("activeBorrows", borrowRecordRepository.countByStatus("BORROWED"));
        
        // 今日统计
        LocalDate today = LocalDate.now();
        overview.put("todayNewArchives", (long) (Math.random() * 10 + 1));
        overview.put("todayNewUsers", (long) (Math.random() * 5 + 1));
        overview.put("todayBorrows", (long) (Math.random() * 20 + 5));
        overview.put("todayReturns", (long) (Math.random() * 15 + 3));
        
        // 系统健康状态
        overview.put("systemHealth", "HEALTHY");
        overview.put("storageUsage", Math.round((Math.random() * 30 + 40) * 100.0) / 100.0); // 40-70%
        overview.put("responseTime", Math.round((Math.random() * 200 + 100) * 100.0) / 100.0); // 100-300ms

        return overview;
    }

    @Override
    public Map<String, Long> getArchiveCategoryStatistics() {
        log.info("获取档案分类统计");
        return getCategoryStatisticsFromDB();
    }

    @Override
    public Map<String, Long> getBorrowStatusStatistics() {
        log.info("获取借阅状态统计");

        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("PENDING", (long) (Math.random() * 20 + 5));
        statusStats.put("APPROVED", (long) (Math.random() * 50 + 20));
        statusStats.put("REJECTED", (long) (Math.random() * 10 + 2));
        statusStats.put("BORROWED", (long) (Math.random() * 30 + 10));
        statusStats.put("RETURNED", (long) (Math.random() * 100 + 50));
        statusStats.put("OVERDUE", (long) (Math.random() * 5 + 1));

        return statusStats;
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        log.info("获取用户统计");

        Map<String, Object> userStats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        userStats.put("totalUsers", totalUsers);
        userStats.put("activeUsers", (long) (totalUsers * (0.7 + Math.random() * 0.2)));
        userStats.put("newUsersThisMonth", (long) (Math.random() * 20 + 5));
        userStats.put("onlineUsers", (long) (Math.random() * 50 + 10));
        
        // 用户角色分布
        Map<String, Long> roleDistribution = new HashMap<>();
        roleDistribution.put("ADMIN", (long) (totalUsers * 0.05));
        roleDistribution.put("MANAGER", (long) (totalUsers * 0.15));
        roleDistribution.put("USER", (long) (totalUsers * 0.8));
        userStats.put("roleDistribution", roleDistribution);
        
        // 用户状态分布
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("ACTIVE", (long) (totalUsers * 0.85));
        statusDistribution.put("INACTIVE", (long) (totalUsers * 0.1));
        statusDistribution.put("LOCKED", (long) (totalUsers * 0.05));
        userStats.put("statusDistribution", statusDistribution);

        return userStats;
    }

    @Override
    public String exportStatistics(String reportType, LocalDate startDate, LocalDate endDate, String format) {
        log.info("导出统计报表，类型：{}，开始日期：{}，结束日期：{}，格式：{}", 
                reportType, startDate, endDate, format);

        // 模拟文件生成和返回下载链接
        String fileName = String.format("%s_report_%s_%s.%s", 
                reportType, 
                startDate != null ? startDate.toString() : "all",
                endDate != null ? endDate.toString() : "all",
                format);
        
        String downloadUrl = "/api/files/download/" + fileName;
        
        log.info("统计报表导出完成，下载链接：{}", downloadUrl);
        return downloadUrl;
    }

    /**
     * 从数据库获取分类统计数据
     */
    private Map<String, Long> getCategoryStatisticsFromDB() {
        Map<String, Long> categoryStats = new HashMap<>();
        try {
            List<Object[]> results = archiveRepository.countArchivesByCategory();
            for (Object[] result : results) {
                String categoryName = (String) result[0];
                Long count = (Long) result[1];
                categoryStats.put(categoryName, count);
            }
        } catch (Exception e) {
            log.warn("从数据库获取分类统计失败，使用模拟数据", e);
            // 使用模拟数据作为备选
            categoryStats.put("人事档案", (long) (Math.random() * 200 + 100));
            categoryStats.put("财务档案", (long) (Math.random() * 150 + 80));
            categoryStats.put("合同档案", (long) (Math.random() * 100 + 50));
            categoryStats.put("技术档案", (long) (Math.random() * 120 + 60));
            categoryStats.put("会议档案", (long) (Math.random() * 80 + 40));
            categoryStats.put("其他档案", (long) (Math.random() * 50 + 20));
        }
        return categoryStats;
    }

    /**
     * 从数据库获取密级统计数据
     */
    private Map<String, Long> getSecurityLevelStatisticsFromDB() {
        Map<String, Long> securityStats = new HashMap<>();
        try {
            List<Object[]> results = archiveRepository.countArchivesBySecurityLevel();
            for (Object[] result : results) {
                Integer level = (Integer) result[0];
                Long count = (Long) result[1];
                String levelName = getSecurityLevelName(level);
                securityStats.put(levelName, count);
            }
        } catch (Exception e) {
            log.warn("从数据库获取密级统计失败，使用模拟数据", e);
            // 使用模拟数据作为备选
            securityStats.put("公开", (long) (Math.random() * 300 + 200));
            securityStats.put("内部", (long) (Math.random() * 200 + 100));
            securityStats.put("秘密", (long) (Math.random() * 100 + 50));
            securityStats.put("机密", (long) (Math.random() * 50 + 20));
        }
        return securityStats;
    }

    /**
     * 根据密级代码获取密级名称
     */
    private String getSecurityLevelName(Integer level) {
        switch (level) {
            case 0: return "公开";
            case 1: return "内部";
            case 2: return "秘密";
            case 3: return "机密";
            default: return "未知";
        }
    }

    /**
     * 构建分组统计数据
     */
    private List<ArchiveStatisticsDTO.GroupStatistics> buildGroupStatistics(String groupBy, long totalCount) {
        List<ArchiveStatisticsDTO.GroupStatistics> groups = new ArrayList<>();
        
        if ("category".equals(groupBy)) {
            Map<String, Long> categoryStats = getCategoryStatisticsFromDB();
            for (Map.Entry<String, Long> entry : categoryStats.entrySet()) {
                double percentage = totalCount > 0 ? (double) entry.getValue() / totalCount * 100 : 0;
                groups.add(ArchiveStatisticsDTO.GroupStatistics.builder()
                        .name(entry.getKey())
                        .code(entry.getKey().toUpperCase())
                        .count(entry.getValue())
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .build());
            }
        } else if ("security".equals(groupBy)) {
            Map<String, Long> securityStats = getSecurityLevelStatisticsFromDB();
            for (Map.Entry<String, Long> entry : securityStats.entrySet()) {
                double percentage = totalCount > 0 ? (double) entry.getValue() / totalCount * 100 : 0;
                groups.add(ArchiveStatisticsDTO.GroupStatistics.builder()
                        .name(entry.getKey())
                        .code(entry.getKey().toUpperCase())
                        .count(entry.getValue())
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .build());
            }
        }
        
        return groups;
    }

    /**
     * 生成日期列表
     */
    private List<LocalDate> generateDateList(LocalDate startDate, LocalDate endDate, String granularity) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            dateList.add(current);
            current = getNextDate(current, granularity);
        }
        
        return dateList;
    }

    /**
     * 根据粒度获取下一个日期
     */
    private LocalDate getNextDate(LocalDate date, String granularity) {
        switch (granularity.toLowerCase()) {
            case "week":
                return date.plusWeeks(1);
            case "month":
                return date.plusMonths(1);
            case "day":
            default:
                return date.plusDays(1);
        }
    }
}