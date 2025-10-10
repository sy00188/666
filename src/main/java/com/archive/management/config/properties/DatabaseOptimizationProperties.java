package com.archive.management.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库优化配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "archive.database.optimization")
public class DatabaseOptimizationProperties {

    /**
     * 连接池配置
     */
    private ConnectionPool connectionPool = new ConnectionPool();

    /**
     * 事务配置
     */
    private Transaction transaction = new Transaction();

    /**
     * 查询优化配置
     */
    private QueryOptimization queryOptimization = new QueryOptimization();

    /**
     * 监控配置
     */
    private Monitoring monitoring = new Monitoring();

    /**
     * 连接池配置
     */
    @Data
    public static class ConnectionPool {
        /** 最大连接池大小 */
        private Integer maxPoolSize = 20;
        
        /** 最小空闲连接数 */
        private Integer minIdle = 5;
        
        /** 连接超时时间(毫秒) */
        private Long connectionTimeout = 30000L;
        
        /** 空闲超时时间(毫秒) */
        private Long idleTimeout = 600000L;
        
        /** 最大生命周期(毫秒) */
        private Long maxLifetime = 1800000L;
        
        /** 泄漏检测阈值(毫秒) */
        private Long leakDetectionThreshold = 60000L;
    }

    /**
     * 事务配置
     */
    @Data
    public static class Transaction {
        /** 事务超时时间(秒) */
        private Integer timeout = 30;
    }

    /**
     * 查询优化配置
     */
    @Data
    public static class QueryOptimization {
        /** 慢查询阈值(毫秒) */
        private Long slowQueryThreshold = 1000L;
        
        /** 是否启用查询缓存 */
        private Boolean enableQueryCache = true;
        
        /** 查询缓存大小 */
        private Integer queryCacheSize = 32;
    }

    /**
     * 监控配置
     */
    @Data
    public static class Monitoring {
        /** 是否启用监控 */
        private Boolean enabled = true;
        
        /** 检查间隔(秒) */
        private Integer checkInterval = 300;
    }
}

