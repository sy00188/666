package com.archive.management.service;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 性能监控服务类
 * 提供系统性能监控、分析和优化建议
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceMonitoringService {

    private final ArchiveMapper archiveMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MonitoringService monitoringService;

    /**
     * 获取系统概览信息
     */
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // 基础统计
            overview.put("timestamp", System.currentTimeMillis());
            overview.put("systemTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 用户统计
            long totalUsers = userMapper.selectCount(new QueryWrapper<User>().eq("deleted", 0));
            long activeUsers = userMapper.selectCount(new QueryWrapper<User>().eq("deleted", 0).eq("status", 1));
            overview.put("totalUsers", totalUsers);
            overview.put("activeUsers", activeUsers);
            
            // 档案统计
            long totalArchives = archiveMapper.selectCount(new QueryWrapper<Archive>().eq("deleted", 0));
            long activeArchives = archiveMapper.selectCount(new QueryWrapper<Archive>().eq("deleted", 0).eq("status", 1));
            overview.put("totalArchives", totalArchives);
            overview.put("activeArchives", activeArchives);
            
            // 系统资源信息
            Map<String, Object> systemInfo = getSystemResourceInfo();
            overview.put("systemInfo", systemInfo);
            
            // 性能指标
            Map<String, Object> performanceMetrics = getPerformanceMetrics();
            overview.put("performanceMetrics", performanceMetrics);
            
            // 最近活动
            List<Map<String, Object>> recentActivities = getRecentActivities();
            overview.put("recentActivities", recentActivities);
            
        } catch (Exception e) {
            log.error("获取系统概览失败", e);
            overview.put("error", "获取系统概览失败: " + e.getMessage());
        }
        
        return overview;
    }

    /**
     * 获取系统资源信息
     */
    public Map<String, Object> getSystemResourceInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        try {
            // JVM内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
            
            systemInfo.put("heapUsed", heapUsed);
            systemInfo.put("heapMax", heapMax);
            systemInfo.put("heapUsage", (double) heapUsed / heapMax * 100);
            systemInfo.put("nonHeapUsed", nonHeapUsed);
            
            // 线程信息
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();
            
            systemInfo.put("threadCount", threadCount);
            systemInfo.put("peakThreadCount", peakThreadCount);
            
            // 系统负载
            double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
            systemInfo.put("systemLoad", systemLoad);
            
            // 可用处理器数
            int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
            systemInfo.put("availableProcessors", availableProcessors);
            
        } catch (Exception e) {
            log.error("获取系统资源信息失败", e);
            systemInfo.put("error", "获取系统资源信息失败: " + e.getMessage());
        }
        
        return systemInfo;
    }

    /**
     * 获取性能指标
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 从Redis获取缓存的性能数据
            String cacheKey = "performance:metrics:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            Map<String, Object> cachedMetrics = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedMetrics != null) {
                return cachedMetrics;
            }
            
            // 计算性能指标
            metrics.put("responseTime", calculateAverageResponseTime());
            metrics.put("throughput", calculateThroughput());
            metrics.put("errorRate", calculateErrorRate());
            metrics.put("concurrentUsers", monitoringService.getActiveUsers());
            metrics.put("databaseConnections", getDatabaseConnectionCount());
            
            // 缓存性能指标
            redisTemplate.opsForValue().set(cacheKey, metrics, 1, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.error("获取性能指标失败", e);
            metrics.put("error", "获取性能指标失败: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 获取实时性能指标
     */
    public Map<String, Object> getRealtimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            metrics.put("timestamp", System.currentTimeMillis());
            metrics.put("status", "running");
            
            // 实时系统资源
            Map<String, Object> systemInfo = getSystemResourceInfo();
            metrics.put("systemInfo", systemInfo);
            
            // 实时业务指标
            metrics.put("activeUsers", monitoringService.getActiveUsers());
            metrics.put("concurrentOperations", monitoringService.getConcurrentOperations());
            metrics.put("totalUsers", monitoringService.getTotalUsers());
            metrics.put("totalDocuments", monitoringService.getTotalDocuments());
            
            // 实时性能数据
            Map<String, Object> performanceData = getRealtimePerformanceData();
            metrics.put("performance", performanceData);
            
        } catch (Exception e) {
            log.error("获取实时性能指标失败", e);
            metrics.put("error", "获取实时性能指标失败: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 获取详细性能指标
     */
    public Map<String, Object> getDetailedMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 系统资源详细信息
            Map<String, Object> systemResources = getDetailedSystemResources();
            metrics.put("systemResources", systemResources);
            
            // 数据库性能
            Map<String, Object> databaseMetrics = getDatabaseMetrics();
            metrics.put("database", databaseMetrics);
            
            // 缓存性能
            Map<String, Object> cacheMetrics = getCacheMetrics();
            metrics.put("cache", cacheMetrics);
            
            // 业务性能
            Map<String, Object> businessMetrics = getBusinessMetrics();
            metrics.put("business", businessMetrics);
            
            // 网络性能
            Map<String, Object> networkMetrics = getNetworkMetrics();
            metrics.put("network", networkMetrics);
            
        } catch (Exception e) {
            log.error("获取详细性能指标失败", e);
            metrics.put("error", "获取详细性能指标失败: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 生成性能报告
     */
    public Map<String, Object> generatePerformanceReport(String reportType) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            report.put("reportType", reportType);
            report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            switch (reportType) {
                case "summary":
                    report.put("data", getSystemOverview());
                    break;
                case "detailed":
                    report.put("data", getDetailedMetrics());
                    break;
                case "optimization":
                    report.put("data", getOptimizationSuggestions());
                    break;
                default:
                    report.put("data", getSystemOverview());
            }
            
        } catch (Exception e) {
            log.error("生成性能报告失败", e);
            report.put("error", "生成性能报告失败: " + e.getMessage());
        }
        
        return report;
    }

    /**
     * 获取优化建议
     */
    public Map<String, Object> getOptimizationSuggestions() {
        Map<String, Object> suggestions = new HashMap<>();
        
        try {
            List<Map<String, Object>> optimizationList = new ArrayList<>();
            
            // 内存优化建议
            Map<String, Object> memorySuggestion = new HashMap<>();
            memorySuggestion.put("category", "内存优化");
            memorySuggestion.put("title", "JVM内存使用率过高");
            memorySuggestion.put("description", "建议调整JVM堆内存大小或优化内存使用");
            memorySuggestion.put("priority", "high");
            optimizationList.add(memorySuggestion);
            
            // 数据库优化建议
            Map<String, Object> databaseSuggestion = new HashMap<>();
            databaseSuggestion.put("category", "数据库优化");
            databaseSuggestion.put("title", "慢查询优化");
            databaseSuggestion.put("description", "发现多个慢查询，建议添加索引或优化SQL");
            databaseSuggestion.put("priority", "medium");
            optimizationList.add(databaseSuggestion);
            
            // 缓存优化建议
            Map<String, Object> cacheSuggestion = new HashMap<>();
            cacheSuggestion.put("category", "缓存优化");
            cacheSuggestion.put("title", "缓存命中率较低");
            cacheSuggestion.put("description", "建议调整缓存策略或增加缓存容量");
            cacheSuggestion.put("priority", "low");
            optimizationList.add(cacheSuggestion);
            
            suggestions.put("suggestions", optimizationList);
            suggestions.put("totalCount", optimizationList.size());
            
        } catch (Exception e) {
            log.error("获取优化建议失败", e);
            suggestions.put("error", "获取优化建议失败: " + e.getMessage());
        }
        
        return suggestions;
    }

    /**
     * 记录请求性能
     */
    public void recordRequestPerformance(long responseTime) {
        try {
            String cacheKey = "performance:requests:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            
            // 记录响应时间
            redisTemplate.opsForList().leftPush(cacheKey + ":response_times", responseTime);
            redisTemplate.expire(cacheKey + ":response_times", 24, TimeUnit.HOURS);
            
            // 记录请求计数
            redisTemplate.opsForValue().increment(cacheKey + ":count");
            redisTemplate.expire(cacheKey + ":count", 24, TimeUnit.HOURS);
            
            // 记录慢请求
            if (responseTime > 5000) { // 超过5秒的请求
                Map<String, Object> slowRequest = new HashMap<>();
                slowRequest.put("timestamp", System.currentTimeMillis());
                slowRequest.put("responseTime", responseTime);
                redisTemplate.opsForList().leftPush("performance:slow_requests", slowRequest);
                redisTemplate.expire("performance:slow_requests", 7, TimeUnit.DAYS);
            }
            
        } catch (Exception e) {
            log.error("记录请求性能失败", e);
        }
    }

    /**
     * 获取最近活动
     */
    private List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // 这里可以从日志或数据库中获取最近活动
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("type", "user_login");
            activity1.put("description", "用户登录");
            activity1.put("timestamp", LocalDateTime.now().minusMinutes(5));
            activities.add(activity1);
            
            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("type", "document_created");
            activity2.put("description", "创建档案");
            activity2.put("timestamp", LocalDateTime.now().minusMinutes(10));
            activities.add(activity2);
            
        } catch (Exception e) {
            log.error("获取最近活动失败", e);
        }
        
        return activities;
    }

    /**
     * 计算平均响应时间
     */
    private double calculateAverageResponseTime() {
        try {
            String cacheKey = "performance:requests:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            List<Object> responseTimes = redisTemplate.opsForList().range(cacheKey + ":response_times", 0, -1);
            
            if (responseTimes != null && !responseTimes.isEmpty()) {
                double sum = responseTimes.stream().mapToDouble(time -> Double.parseDouble(time.toString())).sum();
                return sum / responseTimes.size();
            }
        } catch (Exception e) {
            log.error("计算平均响应时间失败", e);
        }
        
        return 0.0;
    }

    /**
     * 计算吞吐量
     */
    private double calculateThroughput() {
        try {
            String cacheKey = "performance:requests:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            Object count = redisTemplate.opsForValue().get(cacheKey + ":count");
            
            if (count != null) {
                return Double.parseDouble(count.toString()) / 3600; // 每小时请求数
            }
        } catch (Exception e) {
            log.error("计算吞吐量失败", e);
        }
        
        return 0.0;
    }

    /**
     * 计算错误率
     */
    private double calculateErrorRate() {
        try {
            // 这里可以从错误日志中计算错误率
            return 0.01; // 示例：1%错误率
        } catch (Exception e) {
            log.error("计算错误率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取数据库连接数
     */
    private int getDatabaseConnectionCount() {
        try {
            // 这里可以从数据库连接池获取连接数
            return 10; // 示例连接数
        } catch (Exception e) {
            log.error("获取数据库连接数失败", e);
            return 0;
        }
    }

    /**
     * 获取详细系统资源
     */
    private Map<String, Object> getDetailedSystemResources() {
        Map<String, Object> resources = new HashMap<>();
        
        try {
            // JVM详细信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            resources.put("heapMemory", memoryBean.getHeapMemoryUsage());
            resources.put("nonHeapMemory", memoryBean.getNonHeapMemoryUsage());
            
            // 线程详细信息
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            resources.put("threadCount", threadBean.getThreadCount());
            resources.put("peakThreadCount", threadBean.getPeakThreadCount());
            resources.put("daemonThreadCount", threadBean.getDaemonThreadCount());
            
        } catch (Exception e) {
            log.error("获取详细系统资源失败", e);
        }
        
        return resources;
    }

    /**
     * 获取数据库指标
     */
    private Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 这里可以获取数据库性能指标
            metrics.put("connectionCount", 10);
            metrics.put("activeConnections", 5);
            metrics.put("queryCount", 1000);
            metrics.put("slowQueryCount", 10);
            
        } catch (Exception e) {
            log.error("获取数据库指标失败", e);
        }
        
        return metrics;
    }

    /**
     * 获取缓存指标
     */
    private Map<String, Object> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 这里可以获取缓存性能指标
            metrics.put("hitRate", 0.95);
            metrics.put("missRate", 0.05);
            metrics.put("evictionCount", 100);
            metrics.put("size", 1000);
            
        } catch (Exception e) {
            log.error("获取缓存指标失败", e);
        }
        
        return metrics;
    }

    /**
     * 获取业务指标
     */
    private Map<String, Object> getBusinessMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            metrics.put("totalUsers", monitoringService.getTotalUsers());
            metrics.put("activeUsers", monitoringService.getActiveUsers());
            metrics.put("totalDocuments", monitoringService.getTotalDocuments());
            metrics.put("concurrentOperations", monitoringService.getConcurrentOperations());
            
        } catch (Exception e) {
            log.error("获取业务指标失败", e);
        }
        
        return metrics;
    }

    /**
     * 获取网络指标
     */
    private Map<String, Object> getNetworkMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 这里可以获取网络性能指标
            metrics.put("bytesReceived", 1024000);
            metrics.put("bytesSent", 512000);
            metrics.put("connectionCount", 50);
            
        } catch (Exception e) {
            log.error("获取网络指标失败", e);
        }
        
        return metrics;
    }

    /**
     * 获取实时性能数据
     */
    private Map<String, Object> getRealtimePerformanceData() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            data.put("cpuUsage", getCpuUsage());
            data.put("memoryUsage", getMemoryUsage());
            data.put("diskUsage", getDiskUsage());
            data.put("networkUsage", getNetworkUsage());
            
        } catch (Exception e) {
            log.error("获取实时性能数据失败", e);
        }
        
        return data;
    }

    /**
     * 获取CPU使用率
     */
    private double getCpuUsage() {
        try {
            // 这里可以获取CPU使用率
            return 45.5; // 示例值
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取内存使用率
     */
    private double getMemoryUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long used = memoryBean.getHeapMemoryUsage().getUsed();
            long max = memoryBean.getHeapMemoryUsage().getMax();
            return (double) used / max * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取磁盘使用率
     */
    private double getDiskUsage() {
        try {
            // 这里可以获取磁盘使用率
            return 75.2; // 示例值
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取网络使用率
     */
    private double getNetworkUsage() {
        try {
            // 这里可以获取网络使用率
            return 30.8; // 示例值
        } catch (Exception e) {
            return 0.0;
        }
    }
}
