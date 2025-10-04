package com.archive.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 数据库性能优化配置类
 * 配置数据库连接池、索引优化、慢查询监控等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableScheduling
@EnableJpaRepositories
@EnableTransactionManagement
@EnableConfigurationProperties(DatabaseOptimizationProperties.class)
public class DatabaseOptimizationConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DatabaseOptimizationProperties properties;

    private final AtomicLong slowQueryCount = new AtomicLong(0);
    private final AtomicLong totalQueryCount = new AtomicLong(0);

    /**
     * 数据库连接池优化配置
     * 注意：这里不重复定义dataSource Bean，而是通过配置属性来优化现有的数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public com.zaxxer.hikari.HikariConfig hikariConfig() {
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setMaximumPoolSize(properties.getConnectionPool().getMaxPoolSize());
        config.setMinimumIdle(properties.getConnectionPool().getMinIdle());
        config.setConnectionTimeout(properties.getConnectionPool().getConnectionTimeout());
        config.setIdleTimeout(properties.getConnectionPool().getIdleTimeout());
        config.setMaxLifetime(properties.getConnectionPool().getMaxLifetime());
        config.setLeakDetectionThreshold(properties.getConnectionPool().getLeakDetectionThreshold());
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("ArchiveManagementPool");
        return config;
    }

    /**
     * 事务管理器配置
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(dataSource);
        transactionManager.setDefaultTimeout(properties.getTransaction().getTimeout());
        return transactionManager;
    }

    /**
     * 定时检查数据库性能
     */
    @Scheduled(fixedRate = 300000) // 每5分钟
    public void checkDatabasePerformance() {
        if (properties.getMonitoring().isEnabled()) {
            analyzeSlowQueries();
            checkConnectionPoolStatus();
            analyzeIndexUsage();
        }
    }

    /**
     * 分析慢查询
     */
    private void analyzeSlowQueries() {
        try {
            // 获取慢查询日志
            String sql = "SHOW PROCESSLIST";
            List<Map<String, Object>> processes = jdbcTemplate.queryForList(sql);
            
            int slowQueryThreshold = properties.getQueryOptimization().getSlowQueryThreshold();
            int slowQueryCount = 0;
            
            for (Map<String, Object> process : processes) {
                Object time = process.get("Time");
                if (time != null && Integer.parseInt(time.toString()) > slowQueryThreshold) {
                    slowQueryCount++;
                    logSlowQuery(process);
                }
            }
            
            this.slowQueryCount.addAndGet(slowQueryCount);
            this.totalQueryCount.addAndGet(processes.size());
            
        } catch (Exception e) {
            System.err.println("分析慢查询时出错: " + e.getMessage());
        }
    }

    /**
     * 记录慢查询
     */
    private void logSlowQuery(Map<String, Object> process) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String user = process.get("User").toString();
        String host = process.get("Host").toString();
        String db = process.get("db") != null ? process.get("db").toString() : "N/A";
        String command = process.get("Command").toString();
        String time = process.get("Time").toString();
        String state = process.get("State") != null ? process.get("State").toString() : "N/A";
        String info = process.get("Info") != null ? process.get("Info").toString() : "N/A";
        
        System.out.println(String.format(
            "[%s] 慢查询检测 - 用户: %s, 主机: %s, 数据库: %s, 命令: %s, 时间: %s秒, 状态: %s, SQL: %s",
            timestamp, user, host, db, command, time, state, info
        ));
    }

    /**
     * 检查连接池状态
     */
    private void checkConnectionPoolStatus() {
        try {
            // 获取连接池状态
            String sql = "SHOW STATUS LIKE 'Threads_connected'";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            int connectedThreads = Integer.parseInt(result.get("Value").toString());
            
            int maxConnections = properties.getConnectionPool().getMaxPoolSize();
            double connectionUsage = (double) connectedThreads / maxConnections * 100;
            
            if (connectionUsage > 80) {
                System.out.println(String.format(
                    "警告: 数据库连接池使用率过高: %.2f%% (%d/%d)",
                    connectionUsage, connectedThreads, maxConnections
                ));
            }
            
        } catch (Exception e) {
            System.err.println("检查连接池状态时出错: " + e.getMessage());
        }
    }

    /**
     * 分析索引使用情况
     */
    private void analyzeIndexUsage() {
        try {
            // 获取索引使用统计
            String sql = "SELECT * FROM information_schema.INNODB_SYS_TABLESTATS";
            List<Map<String, Object>> stats = jdbcTemplate.queryForList(sql);
            
            for (Map<String, Object> stat : stats) {
                String tableName = stat.get("NAME").toString();
                Object rowsRead = stat.get("N_ROWS_READ");
                Object rowsInserted = stat.get("N_ROWS_INSERTED");
                Object rowsUpdated = stat.get("N_ROWS_UPDATED");
                Object rowsDeleted = stat.get("N_ROWS_DELETED");
                
                System.out.println(String.format(
                    "表 %s 统计 - 读取: %s, 插入: %s, 更新: %s, 删除: %s",
                    tableName, rowsRead, rowsInserted, rowsUpdated, rowsDeleted
                ));
            }
            
        } catch (Exception e) {
            System.err.println("分析索引使用情况时出错: " + e.getMessage());
        }
    }

    /**
     * 获取慢查询统计
     */
    public Map<String, Object> getSlowQueryStatistics() {
        return Map.of(
            "slowQueryCount", slowQueryCount.get(),
            "totalQueryCount", totalQueryCount.get(),
            "slowQueryRate", totalQueryCount.get() > 0 ? 
                (double) slowQueryCount.get() / totalQueryCount.get() * 100 : 0.0
        );
    }

    /**
     * 数据库优化配置属性
     */
    @ConfigurationProperties(prefix = "archive.database.optimization")
    public static class DatabaseOptimizationProperties {
        private ConnectionPool connectionPool = new ConnectionPool();
        private Transaction transaction = new Transaction();
        private QueryOptimization queryOptimization = new QueryOptimization();
        private Monitoring monitoring = new Monitoring();

        public ConnectionPool getConnectionPool() {
            return connectionPool;
        }

        public void setConnectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        public QueryOptimization getQueryOptimization() {
            return queryOptimization;
        }

        public void setQueryOptimization(QueryOptimization queryOptimization) {
            this.queryOptimization = queryOptimization;
        }

        public Monitoring getMonitoring() {
            return monitoring;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
        }

        public static class ConnectionPool {
            private int maxPoolSize = 20;
            private int minIdle = 5;
            private long connectionTimeout = 30000;
            private long idleTimeout = 600000;
            private long maxLifetime = 1800000;
            private long leakDetectionThreshold = 60000;

            public int getMaxPoolSize() {
                return maxPoolSize;
            }

            public void setMaxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
            }

            public int getMinIdle() {
                return minIdle;
            }

            public void setMinIdle(int minIdle) {
                this.minIdle = minIdle;
            }

            public long getConnectionTimeout() {
                return connectionTimeout;
            }

            public void setConnectionTimeout(long connectionTimeout) {
                this.connectionTimeout = connectionTimeout;
            }

            public long getIdleTimeout() {
                return idleTimeout;
            }

            public void setIdleTimeout(long idleTimeout) {
                this.idleTimeout = idleTimeout;
            }

            public long getMaxLifetime() {
                return maxLifetime;
            }

            public void setMaxLifetime(long maxLifetime) {
                this.maxLifetime = maxLifetime;
            }

            public long getLeakDetectionThreshold() {
                return leakDetectionThreshold;
            }

            public void setLeakDetectionThreshold(long leakDetectionThreshold) {
                this.leakDetectionThreshold = leakDetectionThreshold;
            }
        }

        public static class Transaction {
            private int timeout = 30;

            public int getTimeout() {
                return timeout;
            }

            public void setTimeout(int timeout) {
                this.timeout = timeout;
            }
        }

        public static class QueryOptimization {
            private int slowQueryThreshold = 1000;
            private boolean enableQueryCache = true;
            private int queryCacheSize = 32;

            public int getSlowQueryThreshold() {
                return slowQueryThreshold;
            }

            public void setSlowQueryThreshold(int slowQueryThreshold) {
                this.slowQueryThreshold = slowQueryThreshold;
            }

            public boolean isEnableQueryCache() {
                return enableQueryCache;
            }

            public void setEnableQueryCache(boolean enableQueryCache) {
                this.enableQueryCache = enableQueryCache;
            }

            public int getQueryCacheSize() {
                return queryCacheSize;
            }

            public void setQueryCacheSize(int queryCacheSize) {
                this.queryCacheSize = queryCacheSize;
            }
        }

        public static class Monitoring {
            private boolean enabled = true;
            private int checkInterval = 300;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getCheckInterval() {
                return checkInterval;
            }

            public void setCheckInterval(int checkInterval) {
                this.checkInterval = checkInterval;
            }
        }
    }
}
