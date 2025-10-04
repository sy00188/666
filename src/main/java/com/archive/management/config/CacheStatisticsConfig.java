package com.archive.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.LoadingCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存统计配置类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableScheduling
public class CacheStatisticsConfig {

    @Autowired
    private CacheManager cacheManager;

    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);

    /**
     * 定时收集缓存统计信息
     */
    @Scheduled(fixedRate = 300000) // 每5分钟
    public void collectCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 收集各缓存的统计信息
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheStats = new HashMap<>();
                
                // 获取缓存大小
                if (cache.getNativeCache() instanceof LoadingCache) {
                    LoadingCache<?, ?> loadingCache = (LoadingCache<?, ?>) cache.getNativeCache();
                    CacheStats stats = loadingCache.stats();
                    
                    cacheStats.put("hitCount", stats.hitCount());
                    cacheStats.put("missCount", stats.missCount());
                    cacheStats.put("hitRate", stats.hitRate());
                    cacheStats.put("missRate", stats.missRate());
                    cacheStats.put("requestCount", stats.requestCount());
                    cacheStats.put("evictionCount", stats.evictionCount());
                    cacheStats.put("averageLoadPenalty", stats.averageLoadPenalty());
                    cacheStats.put("size", loadingCache.size());
                    
                    // 更新全局统计
                    totalHits.addAndGet(stats.hitCount());
                    totalMisses.addAndGet(stats.missCount());
                    totalRequests.addAndGet(stats.requestCount());
                }
                
                statistics.put(cacheName, cacheStats);
            }
        }
        
        // 记录全局统计
        statistics.put("global", Map.of(
            "totalHits", totalHits.get(),
            "totalMisses", totalMisses.get(),
            "totalRequests", totalRequests.get(),
            "globalHitRate", calculateGlobalHitRate()
        ));
        
        // 这里可以将统计信息发送到监控系统
        System.out.println("缓存统计信息: " + statistics);
    }

    /**
     * 计算全局命中率
     */
    private double calculateGlobalHitRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalHits.get() / total;
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.getNativeCache() instanceof LoadingCache) {
                LoadingCache<?, ?> loadingCache = (LoadingCache<?, ?>) cache.getNativeCache();
                CacheStats stats = loadingCache.stats();
                
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("hitCount", stats.hitCount());
                cacheStats.put("missCount", stats.missCount());
                cacheStats.put("hitRate", stats.hitRate());
                cacheStats.put("missRate", stats.missRate());
                cacheStats.put("requestCount", stats.requestCount());
                cacheStats.put("evictionCount", stats.evictionCount());
                cacheStats.put("averageLoadPenalty", stats.averageLoadPenalty());
                cacheStats.put("size", loadingCache.size());
                
                statistics.put(cacheName, cacheStats);
            }
        }
        
        return statistics;
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalHits.set(0);
        totalMisses.set(0);
        totalRequests.set(0);
    }
}
