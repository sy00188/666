package com.archive.management.monitor;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 系统健康检查指示器
 * 监控数据库、Redis、内存等系统组件的健康状态
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            Health.Builder healthBuilder = Health.up();
            
            // 检查数据库连接
            checkDatabaseHealth(healthBuilder);
            
            // 检查Redis连接
            checkRedisHealth(healthBuilder);
            
            // 检查内存使用情况
            checkMemoryHealth(healthBuilder);
            
            // 检查数据库连接池状态
            checkConnectionPoolHealth(healthBuilder);
            
            return healthBuilder.build();
            
        } catch (Exception e) {
            log.error("系统健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    /**
     * 检查数据库健康状态
     */
    private void checkDatabaseHealth(Health.Builder healthBuilder) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        healthBuilder.withDetail("database", "UP")
                                .withDetail("database.status", "Connected");
                    }
                }
            }
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            healthBuilder.down()
                    .withDetail("database", "DOWN")
                    .withDetail("database.error", e.getMessage());
        }
    }

    /**
     * 检查Redis健康状态
     */
    private void checkRedisHealth(Health.Builder healthBuilder) {
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            String result = (String) redisTemplate.opsForValue().get("health:check");
            if ("ok".equals(result)) {
                healthBuilder.withDetail("redis", "UP")
                        .withDetail("redis.status", "Connected");
            } else {
                healthBuilder.down()
                        .withDetail("redis", "DOWN")
                        .withDetail("redis.error", "Redis response mismatch");
            }
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            healthBuilder.down()
                    .withDetail("redis", "DOWN")
                    .withDetail("redis.error", e.getMessage());
        }
    }

    /**
     * 检查内存健康状态
     */
    private void checkMemoryHealth(Health.Builder healthBuilder) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        healthBuilder.withDetail("memory.used", formatBytes(usedMemory))
                .withDetail("memory.max", formatBytes(maxMemory))
                .withDetail("memory.usage.percent", String.format("%.2f%%", memoryUsagePercent));
        
        if (memoryUsagePercent > 90) {
            healthBuilder.down()
                    .withDetail("memory.status", "CRITICAL")
                    .withDetail("memory.warning", "Memory usage is above 90%");
        } else if (memoryUsagePercent > 80) {
            healthBuilder.up()
                    .withDetail("memory.status", "WARNING")
                    .withDetail("memory.warning", "Memory usage is above 80%");
        } else {
            healthBuilder.withDetail("memory.status", "OK");
        }
    }

    /**
     * 检查数据库连接池健康状态
     */
    private void checkConnectionPoolHealth(Health.Builder healthBuilder) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            
            int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
            int totalConnections = hikariDataSource.getMaximumPoolSize();
            int idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
            
            double poolUsagePercent = (double) activeConnections / totalConnections * 100;
            
            healthBuilder.withDetail("connectionPool.active", activeConnections)
                    .withDetail("connectionPool.idle", idleConnections)
                    .withDetail("connectionPool.total", totalConnections)
                    .withDetail("connectionPool.usage.percent", String.format("%.2f%%", poolUsagePercent));
            
            if (poolUsagePercent > 90) {
                healthBuilder.down()
                        .withDetail("connectionPool.status", "CRITICAL")
                        .withDetail("connectionPool.warning", "Connection pool usage is above 90%");
            } else if (poolUsagePercent > 80) {
                healthBuilder.up()
                        .withDetail("connectionPool.status", "WARNING")
                        .withDetail("connectionPool.warning", "Connection pool usage is above 80%");
            } else {
                healthBuilder.withDetail("connectionPool.status", "OK");
            }
        }
    }

    /**
     * 格式化字节数为可读格式
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
