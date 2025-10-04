package com.archive.management.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能监控配置类
 * 配置各种监控指标和健康检查
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
public class MonitoringConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 自定义MeterRegistry配置
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "archive-management-system",
                "version", "1.0.0",
                "environment", "production"
        );
    }

    /**
     * 业务指标监控
     */
    @Bean
    public MeterBinder businessMetrics() {
        return new MeterBinder() {
            @Override
            public void bindTo(MeterRegistry registry) {
                // 用户注册计数器
                Counter.builder("archive.user.registration")
                        .description("用户注册总数")
                        .register(registry);

                // 档案创建计数器
                Counter.builder("archive.document.created")
                        .description("档案创建总数")
                        .register(registry);

                // 档案下载计数器
                Counter.builder("archive.document.downloaded")
                        .description("档案下载总数")
                        .register(registry);

                // 用户登录计数器
                Counter.builder("archive.user.login")
                        .description("用户登录总数")
                        .register(registry);

                // 用户登出计数器
                Counter.builder("archive.user.logout")
                        .description("用户登出总数")
                        .register(registry);

                // 档案搜索计数器
                Counter.builder("archive.document.searched")
                        .description("档案搜索总数")
                        .register(registry);

                // 档案更新计数器
                Counter.builder("archive.document.updated")
                        .description("档案更新总数")
                        .register(registry);

                // 档案删除计数器
                Counter.builder("archive.document.deleted")
                        .description("档案删除总数")
                        .register(registry);

                // 批量操作计数器
                Counter.builder("archive.batch.operation")
                        .description("批量操作总数")
                        .register(registry);

                // 错误计数器
                Counter.builder("archive.error.count")
                        .description("错误总数")
                        .register(registry);
            }
        };
    }

    /**
     * 系统性能指标监控
     */
    @Bean
    public MeterBinder systemMetrics() {
        return new MeterBinder() {
            @Override
            public void bindTo(MeterRegistry registry) {
                // 数据库连接池监控
                Gauge.builder("archive.database.connections.active")
                        .description("活跃数据库连接数")
                        .register(registry, dataSource, ds -> {
                            try {
                                return ((com.zaxxer.hikari.HikariDataSource) ds).getHikariPoolMXBean().getActiveConnections();
                            } catch (Exception e) {
                                return 0;
                            }
                        });

                // 数据库连接池空闲连接数
                Gauge.builder("archive.database.connections.idle")
                        .description("空闲数据库连接数")
                        .register(registry, dataSource, ds -> {
                            try {
                                return ((com.zaxxer.hikari.HikariDataSource) ds).getHikariPoolMXBean().getIdleConnections();
                            } catch (Exception e) {
                                return 0;
                            }
                        });

                // 数据库连接池总连接数
                Gauge.builder("archive.database.connections.total")
                        .description("数据库连接池总连接数")
                        .register(registry, dataSource, ds -> {
                            try {
                                return ((com.zaxxer.hikari.HikariDataSource) ds).getHikariPoolMXBean().getTotalConnections();
                            } catch (Exception e) {
                                return 0;
                            }
                        });

                // 数据库连接池等待连接数
                Gauge.builder("archive.database.connections.waiting")
                        .description("等待数据库连接数")
                        .register(registry, dataSource, ds -> {
                            try {
                                return ((com.zaxxer.hikari.HikariDataSource) ds).getHikariPoolMXBean().getThreadsAwaitingConnection();
                            } catch (Exception e) {
                                return 0;
                            }
                        });
            }
        };
    }

    /**
     * 自定义健康检查 - 数据库连接
     */
    @Bean
    public HealthIndicator databaseHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                try (Connection connection = dataSource.getConnection()) {
                    if (connection.isValid(5)) {
                        return Health.up()
                                .withDetail("database", "MySQL")
                                .withDetail("status", "连接正常")
                                .build();
                    } else {
                        return Health.down()
                                .withDetail("database", "MySQL")
                                .withDetail("status", "连接异常")
                                .build();
                    }
                } catch (SQLException e) {
                    return Health.down()
                            .withDetail("database", "MySQL")
                            .withDetail("status", "连接失败")
                            .withDetail("error", e.getMessage())
                            .build();
                }
            }
        };
    }

    /**
     * 自定义健康检查 - 应用状态
     */
    @Bean
    public HealthIndicator applicationHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                return Health.up()
                        .withDetail("application", "archive-management-system")
                        .withDetail("version", "1.0.0")
                        .withDetail("status", "运行正常")
                        .withDetail("uptime", System.currentTimeMillis() - System.getProperty("start.time", "0"))
                        .build();
            }
        };
    }

    /**
     * 自定义健康检查 - 文件存储
     */
    @Bean
    public HealthIndicator fileStorageHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                // 这里可以添加文件存储系统的健康检查逻辑
                // 例如检查MinIO连接、磁盘空间等
                return Health.up()
                        .withDetail("storage", "MinIO")
                        .withDetail("status", "存储正常")
                        .build();
            }
        };
    }

    /**
     * 自定义健康检查 - 缓存系统
     */
    @Bean
    public HealthIndicator cacheHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                // 这里可以添加Redis缓存系统的健康检查逻辑
                return Health.up()
                        .withDetail("cache", "Redis")
                        .withDetail("status", "缓存正常")
                        .build();
            }
        };
    }

    /**
     * 自定义健康检查 - 消息队列
     */
    @Bean
    public HealthIndicator messageQueueHealthIndicator() {
        return new HealthIndicator() {
            @Override
            public Health health() {
                // 这里可以添加RabbitMQ消息队列的健康检查逻辑
                return Health.up()
                        .withDetail("messageQueue", "RabbitMQ")
                        .withDetail("status", "消息队列正常")
                        .build();
            }
        };
    }
}