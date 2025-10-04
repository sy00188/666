package com.archive.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 查询性能优化服务类
 * 提供慢查询识别、优化建议和性能分析功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class QueryOptimizationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);
    private final List<SlowQueryInfo> slowQueryHistory = new ArrayList<>();

    /**
     * 分析慢查询
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void analyzeSlowQueries() {
        try {
            // 获取当前正在执行的查询
            String sql = "SHOW PROCESSLIST";
            List<Map<String, Object>> processes = jdbcTemplate.queryForList(sql);
            
            int slowQueryThreshold = 1000; // 1秒
            int currentSlowQueries = 0;
            
            for (Map<String, Object> process : processes) {
                Object time = process.get("Time");
                if (time != null && Integer.parseInt(time.toString()) > slowQueryThreshold) {
                    currentSlowQueries++;
                    recordSlowQuery(process);
                }
            }
            
            totalQueries.addAndGet(processes.size());
            slowQueries.addAndGet(currentSlowQueries);
            
        } catch (Exception e) {
            System.err.println("分析慢查询时出错: " + e.getMessage());
        }
    }

    /**
     * 记录慢查询信息
     */
    private void recordSlowQuery(Map<String, Object> process) {
        SlowQueryInfo slowQuery = new SlowQueryInfo();
        slowQuery.setTimestamp(LocalDateTime.now());
        slowQuery.setUser(process.get("User").toString());
        slowQuery.setHost(process.get("Host").toString());
        slowQuery.setDatabase(process.get("db") != null ? process.get("db").toString() : "N/A");
        slowQuery.setCommand(process.get("Command").toString());
        slowQuery.setTime(Integer.parseInt(process.get("Time").toString()));
        slowQuery.setState(process.get("State") != null ? process.get("State").toString() : "N/A");
        slowQuery.setInfo(process.get("Info") != null ? process.get("Info").toString() : "N/A");
        
        synchronized (slowQueryHistory) {
            slowQueryHistory.add(slowQuery);
            // 只保留最近100条记录
            if (slowQueryHistory.size() > 100) {
                slowQueryHistory.remove(0);
            }
        }
    }

    /**
     * 获取慢查询统计
     */
    public Map<String, Object> getSlowQueryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQueries", totalQueries.get());
        stats.put("slowQueries", slowQueries.get());
        stats.put("slowQueryRate", totalQueries.get() > 0 ? 
            (double) slowQueries.get() / totalQueries.get() * 100 : 0.0);
        stats.put("recentSlowQueries", getRecentSlowQueries(10));
        return stats;
    }

    /**
     * 获取最近的慢查询
     */
    public List<SlowQueryInfo> getRecentSlowQueries(int limit) {
        synchronized (slowQueryHistory) {
            int size = slowQueryHistory.size();
            int start = Math.max(0, size - limit);
            return new ArrayList<>(slowQueryHistory.subList(start, size));
        }
    }

    /**
     * 分析查询性能
     */
    @Cacheable(value = "queryAnalysis", key = "#sql")
    public QueryAnalysisResult analyzeQuery(String sql) {
        QueryAnalysisResult result = new QueryAnalysisResult();
        result.setSql(sql);
        result.setTimestamp(LocalDateTime.now());
        
        try {
            // 执行EXPLAIN分析
            String explainSql = "EXPLAIN " + sql;
            List<Map<String, Object>> explainResult = jdbcTemplate.queryForList(explainSql);
            
            result.setExplainResult(explainResult);
            result.setOptimizationSuggestions(generateOptimizationSuggestions(explainResult));
            
        } catch (Exception e) {
            result.setError("分析查询时出错: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 生成优化建议
     */
    private List<String> generateOptimizationSuggestions(List<Map<String, Object>> explainResult) {
        List<String> suggestions = new ArrayList<>();
        
        for (Map<String, Object> row : explainResult) {
            String type = row.get("type").toString();
            String key = row.get("key") != null ? row.get("key").toString() : "";
            String extra = row.get("Extra") != null ? row.get("Extra").toString() : "";
            String rows = row.get("rows") != null ? row.get("rows").toString() : "0";
            
            // 检查全表扫描
            if ("ALL".equals(type)) {
                suggestions.add("检测到全表扫描，建议添加索引");
            }
            
            // 检查临时表
            if (extra.contains("Using temporary")) {
                suggestions.add("使用了临时表，建议优化查询或添加索引");
            }
            
            // 检查文件排序
            if (extra.contains("Using filesort")) {
                suggestions.add("使用了文件排序，建议添加ORDER BY索引");
            }
            
            // 检查大量行扫描
            try {
                long rowCount = Long.parseLong(rows);
                if (rowCount > 10000) {
                    suggestions.add("扫描了大量行(" + rowCount + ")，建议优化查询条件");
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        return suggestions;
    }

    /**
     * 获取索引使用统计
     */
    public Map<String, Object> getIndexUsageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取表统计信息
            String sql = "SELECT * FROM information_schema.INNODB_SYS_TABLESTATS";
            List<Map<String, Object>> tableStats = jdbcTemplate.queryForList(sql);
            
            stats.put("tableStats", tableStats);
            
            // 获取索引统计信息
            String indexSql = "SELECT * FROM information_schema.INNODB_SYS_INDEXES";
            List<Map<String, Object>> indexStats = jdbcTemplate.queryForList(indexSql);
            
            stats.put("indexStats", indexStats);
            
        } catch (Exception e) {
            stats.put("error", "获取索引统计信息时出错: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * 生成性能报告
     */
    public Map<String, Object> generatePerformanceReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 基本统计
        report.put("slowQueryStats", getSlowQueryStatistics());
        report.put("indexStats", getIndexUsageStatistics());
        
        // 性能指标
        Map<String, Object> performanceMetrics = new HashMap<>();
        performanceMetrics.put("totalQueries", totalQueries.get());
        performanceMetrics.put("slowQueries", slowQueries.get());
        performanceMetrics.put("slowQueryRate", totalQueries.get() > 0 ? 
            (double) slowQueries.get() / totalQueries.get() * 100 : 0.0);
        
        report.put("performanceMetrics", performanceMetrics);
        
        // 优化建议
        List<String> optimizationSuggestions = new ArrayList<>();
        if (slowQueries.get() > 0) {
            optimizationSuggestions.add("检测到慢查询，建议优化查询语句");
        }
        if (totalQueries.get() > 0 && (double) slowQueries.get() / totalQueries.get() > 0.1) {
            optimizationSuggestions.add("慢查询比例较高，建议全面检查数据库性能");
        }
        
        report.put("optimizationSuggestions", optimizationSuggestions);
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return report;
    }

    /**
     * 慢查询信息类
     */
    public static class SlowQueryInfo {
        private LocalDateTime timestamp;
        private String user;
        private String host;
        private String database;
        private String command;
        private int time;
        private String state;
        private String info;

        // Getters and Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    /**
     * 查询分析结果类
     */
    public static class QueryAnalysisResult {
        private String sql;
        private LocalDateTime timestamp;
        private List<Map<String, Object>> explainResult;
        private List<String> optimizationSuggestions;
        private String error;

        // Getters and Setters
        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public List<Map<String, Object>> getExplainResult() {
            return explainResult;
        }

        public void setExplainResult(List<Map<String, Object>> explainResult) {
            this.explainResult = explainResult;
        }

        public List<String> getOptimizationSuggestions() {
            return optimizationSuggestions;
        }

        public void setOptimizationSuggestions(List<String> optimizationSuggestions) {
            this.optimizationSuggestions = optimizationSuggestions;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
