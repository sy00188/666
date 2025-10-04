package com.archive.management.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.sql.DataSource;
import java.time.LocalDateTime;

/**
 * 数据库配置类
 * 配置数据源、MyBatis-Plus插件等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    /**
     * 主数据源配置
     * 
     * @return HikariDataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // 基本连接信息
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 连接池配置
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // 连接池名称
        config.setPoolName("ArchiveHikariCP");
        
        // 连接测试查询
        config.setConnectionTestQuery("SELECT 1");
        
        // 自动提交
        config.setAutoCommit(true);
        
        // 连接初始化SQL
        config.setConnectionInitSql("SET NAMES utf8mb4");
        
        // 性能优化配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }

    /**
     * MyBatis-Plus插件配置
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setDbType(DbType.MYSQL);
        paginationInterceptor.setOverflow(false);
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 自动填充配置
     * 
     * @return MetaObjectHandler
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }

    /**
     * 自定义元数据处理器
     * 自动填充创建时间、更新时间等字段
     */
    public static class CustomMetaObjectHandler implements MetaObjectHandler {

        /**
         * 插入时自动填充
         * 
         * @param metaObject 元数据对象
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            
            // 创建时间
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "gmtCreate", LocalDateTime.class, now);
            
            // 更新时间
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
            this.strictInsertFill(metaObject, "gmtModified", LocalDateTime.class, now);
            
            // 创建者（如果能获取到当前用户）
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
                this.strictInsertFill(metaObject, "createdBy", Long.class, currentUserId);
                this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
                this.strictInsertFill(metaObject, "updatedBy", Long.class, currentUserId);
            }
            
            // 删除标志
            this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
            this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
            this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
            
            // 状态
            this.strictInsertFill(metaObject, "status", Integer.class, 1);
            
            // 版本号（乐观锁）
            this.strictInsertFill(metaObject, "version", Integer.class, 1);
        }

        /**
         * 更新时自动填充
         * 
         * @param metaObject 元数据对象
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            
            // 更新时间
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);
            this.strictUpdateFill(metaObject, "gmtModified", LocalDateTime.class, now);
            
            // 更新者（如果能获取到当前用户）
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
                this.strictUpdateFill(metaObject, "updatedBy", Long.class, currentUserId);
            }
        }

        /**
         * 获取当前用户ID
         * 
         * @return 用户ID
         */
        private Long getCurrentUserId() {
            try {
                // 从Spring Security上下文获取当前用户
                org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                    
                    Object principal = authentication.getPrincipal();
                    
                    // 如果是自定义的用户详情对象
                    if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                        String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                        // 这里可以根据用户名查询用户ID，或者在UserDetails中直接包含用户ID
                        return parseUserIdFromUsername(username);
                    }
                    
                    // 如果直接是用户ID
                    if (principal instanceof Number) {
                        return ((Number) principal).longValue();
                    }
                    
                    // 如果是字符串形式的用户ID
                    if (principal instanceof String) {
                        try {
                            return Long.parseLong((String) principal);
                        } catch (NumberFormatException e) {
                            // 忽略解析错误
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略获取用户信息时的异常
                System.err.println("获取当前用户ID时发生异常: " + e.getMessage());
            }
            
            return null;
        }

        /**
         * 从用户名解析用户ID
         * 
         * @param username 用户名
         * @return 用户ID
         */
        private Long parseUserIdFromUsername(String username) {
            // 这里可以实现具体的用户名到用户ID的转换逻辑
            // 比如查询数据库、从缓存获取等
            // 为了避免循环依赖，这里暂时返回null
            return null;
        }
    }

    /**
     * SQL性能分析插件（仅开发环境使用）
     * 注意：MyBatis-Plus 3.5.0+ 版本已移除该插件
     */
    /*
    @Bean
    @Profile({"dev", "test"})
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setMaxTime(1000); // SQL执行最大时长，超过则停止运行
        performanceInterceptor.setFormat(true); // 格式化SQL
        return performanceInterceptor;
    }
    */

    /**
     * 数据库健康检查
     * 
     * @param dataSource 数据源
     * @return 健康状态
     */
    public boolean checkDatabaseHealth(DataSource dataSource) {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (Exception e) {
            System.err.println("数据库健康检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取数据库连接池状态
     * 
     * @param dataSource 数据源
     * @return 连接池状态信息
     */
    public String getConnectionPoolStatus(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            com.zaxxer.hikari.HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
            
            return String.format(
                "连接池状态 [活跃连接: %d, 空闲连接: %d, 总连接: %d, 等待线程: %d]",
                poolMXBean.getActiveConnections(),
                poolMXBean.getIdleConnections(),
                poolMXBean.getTotalConnections(),
                poolMXBean.getThreadsAwaitingConnection()
            );
        }
        
        return "无法获取连接池状态信息";
    }
}