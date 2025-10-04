package com.archive.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.OperatingSystemMXBean;

/**
 * 性能分析服务类
 * 提供系统性能分析、报告生成和优化建议功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class PerformanceAnalysisService {

    @Autowired
    private QueryOptimizationService queryOptimizationService;

    @Autowired
    private SmartCacheService smartCacheService;

    private final Map<String, PerformanceMetric> performanceMetrics = new HashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    /**
     * 收集性能指标
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void collectPerformanceMetrics() {
        // 收集JVM指标
        collectJVMMetrics();
        
        // 收集系统指标
        collectSystemMetrics();
        
        // 收集应用指标
        collectApplicationMetrics();
        
        // 收集数据库指标
        collectDatabaseMetrics();
        
        // 收集缓存指标
        collectCacheMetrics();
    }

    /**
     * 收集JVM指标
     */
    private void collectJVMMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        // 内存使用情况
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        double heapUsage = (double) heapUsed / heapMax * 100;
        
        // 线程情况
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        
        // GC情况
        long totalGcTime = 0;
        long totalGcCount = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGcTime += gcBean.getCollectionTime();
            totalGcCount += gcBean.getCollectionCount();
        }
        
        PerformanceMetric jvmMetric = new PerformanceMetric();
        jvmMetric.setName("JVM");
        jvmMetric.setTimestamp(LocalDateTime.now());
        jvmMetric.setValue("heapUsage", heapUsage);
        jvmMetric.setValue("threadCount", threadCount);
        jvmMetric.setValue("peakThreadCount", peakThreadCount);
        jvmMetric.setValue("totalGcTime", totalGcTime);
        jvmMetric.setValue("totalGcCount", totalGcCount);
        
        performanceMetrics.put("JVM", jvmMetric);
    }

    /**
     * 收集系统指标
     */
    private void collectSystemMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        // CPU使用率
        double cpuUsage = osBean.getProcessCpuLoad() * 100;
        
        // 系统负载
        double systemLoad = osBean.getSystemLoadAverage();
        
        PerformanceMetric systemMetric = new PerformanceMetric();
        systemMetric.setName("System");
        systemMetric.setTimestamp(LocalDateTime.now());
        systemMetric.setValue("cpuUsage", cpuUsage);
        systemMetric.setValue("systemLoad", systemLoad);
        
        performanceMetrics.put("System", systemMetric);
    }

    /**
     * 收集应用指标
     */
    private void collectApplicationMetrics() {
        // 请求统计
        long requests = totalRequests.get();
        long responseTime = totalResponseTime.get();
        double avgResponseTime = requests > 0 ? (double) responseTime / requests : 0.0;
        
        PerformanceMetric appMetric = new PerformanceMetric();
        appMetric.setName("Application");
        appMetric.setTimestamp(LocalDateTime.now());
        appMetric.setValue("totalRequests", requests);
        appMetric.setValue("avgResponseTime", avgResponseTime);
        
        performanceMetrics.put("Application", appMetric);
    }

    /**
     * 收集数据库指标
     */
    private void collectDatabaseMetrics() {
        Map<String, Object> dbStats = queryOptimizationService.getSlowQueryStatistics();
        
        PerformanceMetric dbMetric = new PerformanceMetric();
        dbMetric.setName("Database");
        dbMetric.setTimestamp(LocalDateTime.now());
        dbMetric.setValue("totalQueries", dbStats.get("totalQueries"));
        dbMetric.setValue("slowQueries", dbStats.get("slowQueries"));
        dbMetric.setValue("slowQueryRate", dbStats.get("slowQueryRate"));
        
        performanceMetrics.put("Database", dbMetric);
    }

    /**
     * 收集缓存指标
     */
    private void collectCacheMetrics() {
        Map<String, Object> cacheStats = smartCacheService.getCacheStatistics();
        
        PerformanceMetric cacheMetric = new PerformanceMetric();
        cacheMetric.setName("Cache");
        cacheMetric.setTimestamp(LocalDateTime.now());
        cacheMetric.setValue("cacheStats", cacheStats);
        
        performanceMetrics.put("Cache", cacheMetric);
    }

    /**
     * 记录请求性能
     */
    public void recordRequestPerformance(long responseTime) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
    }

    /**
     * 生成性能报告
     */
    @Cacheable(value = "performanceReport", key = "#reportType")
    public Map<String, Object> generatePerformanceReport(String reportType) {
        Map<String, Object> report = new HashMap<>();
        
        switch (reportType) {
            case "summary":
                report = generateSummaryReport();
                break;
            case "detailed":
                report = generateDetailedReport();
                break;
            case "optimization":
                report = generateOptimizationReport();
                break;
            default:
                report = generateSummaryReport();
        }
        
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.put("reportType", reportType);
        
        return report;
    }

    /**
     * 生成摘要报告
     */
    private Map<String, Object> generateSummaryReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 系统概览
        Map<String, Object> systemOverview = new HashMap<>();
        systemOverview.put("totalRequests", totalRequests.get());
        systemOverview.put("avgResponseTime", totalRequests.get() > 0 ? 
            (double) totalResponseTime.get() / totalRequests.get() : 0.0);
        
        // 性能指标
        Map<String, Object> metrics = new HashMap<>();
        for (Map.Entry<String, PerformanceMetric> entry : performanceMetrics.entrySet()) {
            metrics.put(entry.getKey(), entry.getValue().getValues());
        }
        
        report.put("systemOverview", systemOverview);
        report.put("metrics", metrics);
        
        return report;
    }

    /**
     * 生成详细报告
     */
    private Map<String, Object> generateDetailedReport() {
        Map<String, Object> report = generateSummaryReport();
        
        // 添加详细指标
        report.put("jvmDetails", getJVMDetails());
        report.put("systemDetails", getSystemDetails());
        report.put("databaseDetails", getDatabaseDetails());
        report.put("cacheDetails", getCacheDetails());
        
        return report;
    }

    /**
     * 生成优化建议报告
     */
    private Map<String, Object> generateOptimizationReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<String> optimizationSuggestions = new ArrayList<>();
        
        // 分析JVM性能
        PerformanceMetric jvmMetric = performanceMetrics.get("JVM");
        if (jvmMetric != null) {
            double heapUsage = (Double) jvmMetric.getValue("heapUsage");
            if (heapUsage > 80) {
                optimizationSuggestions.add("JVM堆内存使用率过高(" + heapUsage + "%)，建议增加堆内存或优化内存使用");
            }
            
            int threadCount = (Integer) jvmMetric.getValue("threadCount");
            if (threadCount > 100) {
                optimizationSuggestions.add("线程数量过多(" + threadCount + ")，建议优化线程池配置");
            }
        }
        
        // 分析系统性能
        PerformanceMetric systemMetric = performanceMetrics.get("System");
        if (systemMetric != null) {
            double cpuUsage = (Double) systemMetric.getValue("cpuUsage");
            if (cpuUsage > 80) {
                optimizationSuggestions.add("CPU使用率过高(" + cpuUsage + "%)，建议优化算法或增加CPU资源");
            }
        }
        
        // 分析数据库性能
        PerformanceMetric dbMetric = performanceMetrics.get("Database");
        if (dbMetric != null) {
            double slowQueryRate = (Double) dbMetric.getValue("slowQueryRate");
            if (slowQueryRate > 10) {
                optimizationSuggestions.add("慢查询比例过高(" + slowQueryRate + "%)，建议优化数据库查询");
            }
        }
        
        // 分析缓存性能
        PerformanceMetric cacheMetric = performanceMetrics.get("Cache");
        if (cacheMetric != null) {
            Map<String, Object> cacheStats = (Map<String, Object>) cacheMetric.getValue("cacheStats");
            for (Map.Entry<String, Object> entry : cacheStats.entrySet()) {
                Map<String, Object> cacheInfo = (Map<String, Object>) entry.getValue();
                double hitRate = (Double) cacheInfo.get("hitRate");
                if (hitRate < 70) {
                    optimizationSuggestions.add("缓存命中率过低(" + hitRate + "%)，建议优化缓存策略");
                }
            }
        }
        
        report.put("optimizationSuggestions", optimizationSuggestions);
        report.put("totalSuggestions", optimizationSuggestions.size());
        
        return report;
    }

    /**
     * 获取JVM详细信息
     */
    private Map<String, Object> getJVMDetails() {
        Map<String, Object> details = new HashMap<>();
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        details.put("heapMemory", memoryBean.getHeapMemoryUsage());
        details.put("nonHeapMemory", memoryBean.getNonHeapMemoryUsage());
        details.put("threadCount", threadBean.getThreadCount());
        details.put("peakThreadCount", threadBean.getPeakThreadCount());
        details.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        
        return details;
    }

    /**
     * 获取系统详细信息
     */
    private Map<String, Object> getSystemDetails() {
        Map<String, Object> details = new HashMap<>();
        
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        details.put("osName", osBean.getName());
        details.put("osVersion", osBean.getVersion());
        details.put("osArch", osBean.getArch());
        details.put("availableProcessors", osBean.getAvailableProcessors());
        details.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        return details;
    }

    /**
     * 获取数据库详细信息
     */
    private Map<String, Object> getDatabaseDetails() {
        return queryOptimizationService.generatePerformanceReport();
    }

    /**
     * 获取缓存详细信息
     */
    private Map<String, Object> getCacheDetails() {
        return smartCacheService.getCacheStatistics();
    }

    /**
     * 性能指标类
     */
    public static class PerformanceMetric {
        private String name;
        private LocalDateTime timestamp;
        private Map<String, Object> values = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(Map<String, Object> values) {
            this.values = values;
        }

        public void setValue(String key, Object value) {
            this.values.put(key, value);
        }

        public Object getValue(String key) {
            return this.values.get(key);
        }
    }
}
