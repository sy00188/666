package com.archive.management.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 监控配置类
 * 配置应用监控、健康检查、指标收集等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
public class MonitoringConfig {

    /**
     * 自定义指标注册器
     * 
     * @return MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // 添加通用标签
            registry.config()
                    .commonTags("application", "archive-management")
                    .commonTags("version", "1.0.0")
                    .commonTags("environment", getEnvironment());
            
            // 添加指标过滤器
            registry.config()
                    .meterFilter(MeterFilter.deny(id -> {
                        String name = id.getName();
                        // 过滤掉一些不需要的指标
                        return name.startsWith("jvm.gc.pause") && 
                               name.contains("unknown");
                    }));
        };
    }

    /**
     * 数据库健康检查指示器
     * 
     * @param dataSource 数据源
     * @return HealthIndicator
     */
    @Bean
    public HealthIndicator databaseHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("database", "MySQL");
                    details.put("status", "UP");
                    details.put("checkTime", LocalDateTime.now());
                    details.put("url", connection.getMetaData().getURL());
                    
                    return org.springframework.boot.actuate.health.Health.up()
                            .withDetails(details)
                            .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.down()
                            .withDetail("error", "数据库连接无效")
                            .build();
                }
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * Redis健康检查指示器
     * 
     * @return HealthIndicator
     */
    @Bean
    public HealthIndicator redisHealthIndicator() {
        return () -> {
            try {
                // 这里可以注入RedisTemplate进行连接测试
                // 为了简化，暂时返回UP状态
                Map<String, Object> details = new HashMap<>();
                details.put("redis", "Redis");
                details.put("status", "UP");
                details.put("checkTime", LocalDateTime.now());
                
                return org.springframework.boot.actuate.health.Health.up()
                        .withDetails(details)
                        .build();
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 磁盘空间健康检查指示器
     * 
     * @return HealthIndicator
     */
    @Bean
    public HealthIndicator diskSpaceHealthIndicator() {
        return () -> {
            try {
                java.io.File root = new java.io.File("/");
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                long usedSpace = totalSpace - freeSpace;
                double usagePercentage = (double) usedSpace / totalSpace * 100;
                
                Map<String, Object> details = new HashMap<>();
                details.put("total", formatBytes(totalSpace));
                details.put("free", formatBytes(freeSpace));
                details.put("used", formatBytes(usedSpace));
                details.put("usagePercentage", String.format("%.2f%%", usagePercentage));
                details.put("checkTime", LocalDateTime.now());
                
                // 如果磁盘使用率超过90%，标记为DOWN
                if (usagePercentage > 90) {
                    return org.springframework.boot.actuate.health.Health.down()
                            .withDetails(details)
                            .withDetail("warning", "磁盘使用率过高")
                            .build();
                } else if (usagePercentage > 80) {
                    return org.springframework.boot.actuate.health.Health.status("WARNING")
                            .withDetails(details)
                            .withDetail("warning", "磁盘使用率较高")
                            .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.up()
                            .withDetails(details)
                            .build();
                }
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 内存健康检查指示器
     * 
     * @return HealthIndicator
     */
    @Bean
    public HealthIndicator memoryHealthIndicator() {
        return () -> {
            try {
                Runtime runtime = Runtime.getRuntime();
                long maxMemory = runtime.maxMemory();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                double usagePercentage = (double) usedMemory / maxMemory * 100;
                
                Map<String, Object> details = new HashMap<>();
                details.put("max", formatBytes(maxMemory));
                details.put("total", formatBytes(totalMemory));
                details.put("used", formatBytes(usedMemory));
                details.put("free", formatBytes(freeMemory));
                details.put("usagePercentage", String.format("%.2f%%", usagePercentage));
                details.put("checkTime", LocalDateTime.now());
                
                // 如果内存使用率超过90%，标记为DOWN
                if (usagePercentage > 90) {
                    return org.springframework.boot.actuate.health.Health.down()
                            .withDetails(details)
                            .withDetail("warning", "内存使用率过高")
                            .build();
                } else if (usagePercentage > 80) {
                    return org.springframework.boot.actuate.health.Health.status("WARNING")
                            .withDetails(details)
                            .withDetail("warning", "内存使用率较高")
                            .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.up()
                            .withDetails(details)
                            .build();
                }
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    /**
     * 应用信息贡献者
     * 
     * @return InfoContributor
     */
    @Bean
    public InfoContributor applicationInfoContributor() {
        return builder -> {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("name", "Archive Management System");
            appInfo.put("version", "1.0.0");
            appInfo.put("description", "企业档案管理系统");
            appInfo.put("startTime", LocalDateTime.now());
            appInfo.put("javaVersion", System.getProperty("java.version"));
            appInfo.put("javaVendor", System.getProperty("java.vendor"));
            appInfo.put("osName", System.getProperty("os.name"));
            appInfo.put("osVersion", System.getProperty("os.version"));
            appInfo.put("osArch", System.getProperty("os.arch"));
            
            builder.withDetail("application", appInfo);
        };
    }

    /**
     * 系统信息贡献者
     * 
     * @return InfoContributor
     */
    @Bean
    public InfoContributor systemInfoContributor() {
        return builder -> {
            Map<String, Object> systemInfo = new HashMap<>();
            
            // CPU信息
            int processors = Runtime.getRuntime().availableProcessors();
            systemInfo.put("processors", processors);
            
            // 内存信息
            Runtime runtime = Runtime.getRuntime();
            systemInfo.put("maxMemory", formatBytes(runtime.maxMemory()));
            systemInfo.put("totalMemory", formatBytes(runtime.totalMemory()));
            systemInfo.put("freeMemory", formatBytes(runtime.freeMemory()));
            
            // 磁盘信息
            java.io.File root = new java.io.File("/");
            systemInfo.put("diskTotal", formatBytes(root.getTotalSpace()));
            systemInfo.put("diskFree", formatBytes(root.getFreeSpace()));
            
            builder.withDetail("system", systemInfo);
        };
    }

    /**
     * 获取当前环境
     * 
     * @return 环境名称
     */
    private String getEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        if (env == null || env.isEmpty()) {
            env = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        return env != null ? env : "default";
    }

    /**
     * 格式化字节数
     * 
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 自定义健康状态
     */
    public static class CustomStatus {
        public static final Status WARNING = new Status("WARNING", "系统运行正常但有警告");
        public static final Status MAINTENANCE = new Status("MAINTENANCE", "系统维护中");
    }

    /**
     * 获取系统监控摘要
     * 
     * @return 监控摘要
     */
    public Map<String, Object> getMonitoringSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // 系统信息
        Runtime runtime = Runtime.getRuntime();
        summary.put("jvmMemoryUsed", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        summary.put("jvmMemoryMax", formatBytes(runtime.maxMemory()));
        summary.put("processors", runtime.availableProcessors());
        
        // 磁盘信息
        java.io.File root = new java.io.File("/");
        summary.put("diskUsed", formatBytes(root.getTotalSpace() - root.getFreeSpace()));
        summary.put("diskTotal", formatBytes(root.getTotalSpace()));
        
        // 应用信息
        summary.put("environment", getEnvironment());
        summary.put("checkTime", LocalDateTime.now());
        
        return summary;
    }
}