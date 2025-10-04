package com.archive.management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置属性类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
@ConfigurationProperties(prefix = "archive.cache")
public class CacheProperties {

    /**
     * 默认缓存配置
     */
    private CacheConfig defaultConfig = new CacheConfig();

    /**
     * 各缓存的具体配置
     */
    private Map<String, CacheConfig> caches = new HashMap<>();

    /**
     * 缓存统计配置
     */
    private Statistics statistics = new Statistics();

    public CacheConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(CacheConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public Map<String, CacheConfig> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, CacheConfig> caches) {
        this.caches = caches;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * 缓存配置
     */
    public static class CacheConfig {
        private Duration ttl = Duration.ofMinutes(30);
        private long maximumSize = 1000;
        private Duration expireAfterWrite = Duration.ofMinutes(30);
        private Duration expireAfterAccess = Duration.ofMinutes(10);
        private boolean recordStats = true;
        private boolean enableMetrics = true;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }

        public Duration getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Duration expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public Duration getExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(Duration expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }

        public boolean isRecordStats() {
            return recordStats;
        }

        public void setRecordStats(boolean recordStats) {
            this.recordStats = recordStats;
        }

        public boolean isEnableMetrics() {
            return enableMetrics;
        }

        public void setEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
        }
    }

    /**
     * 统计配置
     */
    public static class Statistics {
        private boolean enabled = true;
        private Duration reportInterval = Duration.ofMinutes(5);
        private boolean enableMetrics = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getReportInterval() {
            return reportInterval;
        }

        public void setReportInterval(Duration reportInterval) {
            this.reportInterval = reportInterval;
        }

        public boolean isEnableMetrics() {
            return enableMetrics;
        }

        public void setEnableMetrics(boolean enableMetrics) {
            this.enableMetrics = enableMetrics;
        }
    }
}
