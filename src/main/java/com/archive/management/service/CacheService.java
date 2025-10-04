package com.archive.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存服务类
 * 提供多级缓存的统一访问接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LoadingCache<String, Object> userCache;

    @Autowired
    private LoadingCache<String, Object> permissionCache;

    @Autowired
    private LoadingCache<String, Object> archiveCache;

    @Autowired
    private LoadingCache<String, Object> statisticsCache;

    /**
     * 获取缓存值
     */
    public <T> T get(String cacheName, String key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return type.cast(wrapper.get());
            }
        }
        return null;
    }

    /**
     * 设置缓存值
     */
    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * 设置缓存值（带TTL）
     */
    public void put(String cacheName, String key, Object value, long ttl, TimeUnit timeUnit) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
        
        // 同时设置Redis缓存
        redisTemplate.opsForValue().set(cacheName + ":" + key, value, ttl, timeUnit);
    }

    /**
     * 删除缓存
     */
    public void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
        
        // 同时删除Redis缓存
        redisTemplate.delete(cacheName + ":" + key);
    }

    /**
     * 清空缓存
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 获取或计算缓存值
     */
    public <T> T getOrCompute(String cacheName, String key, Function<String, T> computeFunction, Class<T> type) {
        T value = get(cacheName, key, type);
        if (value == null) {
            value = computeFunction.apply(key);
            if (value != null) {
                put(cacheName, key, value);
            }
        }
        return value;
    }

    /**
     * 获取或计算缓存值（带TTL）
     */
    public <T> T getOrCompute(String cacheName, String key, Function<String, T> computeFunction, 
                             Class<T> type, long ttl, TimeUnit timeUnit) {
        T value = get(cacheName, key, type);
        if (value == null) {
            value = computeFunction.apply(key);
            if (value != null) {
                put(cacheName, key, value, ttl, timeUnit);
            }
        }
        return value;
    }

    /**
     * 用户缓存操作
     */
    public <T> T getUser(String key, Class<T> type) {
        return type.cast(userCache.get(key));
    }

    public void putUser(String key, Object value) {
        userCache.put(key, value);
    }

    public void evictUser(String key) {
        userCache.invalidate(key);
    }

    public void clearUserCache() {
        userCache.invalidateAll();
    }

    /**
     * 权限缓存操作
     */
    public <T> T getPermission(String key, Class<T> type) {
        return type.cast(permissionCache.get(key));
    }

    public void putPermission(String key, Object value) {
        permissionCache.put(key, value);
    }

    public void evictPermission(String key) {
        permissionCache.invalidate(key);
    }

    public void clearPermissionCache() {
        permissionCache.invalidateAll();
    }

    /**
     * 档案缓存操作
     */
    public <T> T getArchive(String key, Class<T> type) {
        return type.cast(archiveCache.get(key));
    }

    public void putArchive(String key, Object value) {
        archiveCache.put(key, value);
    }

    public void evictArchive(String key) {
        archiveCache.invalidate(key);
    }

    public void clearArchiveCache() {
        archiveCache.invalidateAll();
    }

    /**
     * 统计缓存操作
     */
    public <T> T getStatistics(String key, Class<T> type) {
        return type.cast(statisticsCache.get(key));
    }

    public void putStatistics(String key, Object value) {
        statisticsCache.put(key, value);
    }

    public void evictStatistics(String key) {
        statisticsCache.invalidate(key);
    }

    public void clearStatisticsCache() {
        statisticsCache.invalidateAll();
    }

    /**
     * 预热缓存
     */
    public void warmUpCache(String cacheName, String key, Object value) {
        put(cacheName, key, value);
    }

    /**
     * 批量预热缓存
     */
    public void warmUpCache(String cacheName, java.util.Map<String, Object> data) {
        data.forEach((k, v) -> put(cacheName, k, v));
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("用户缓存统计: ").append(userCache.stats()).append("\n");
        stats.append("权限缓存统计: ").append(permissionCache.stats()).append("\n");
        stats.append("档案缓存统计: ").append(archiveCache.stats()).append("\n");
        stats.append("统计缓存统计: ").append(statisticsCache.stats()).append("\n");
        return stats.toString();
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.get(key) != null;
        }
        return false;
    }

    /**
     * 获取缓存大小
     */
    public long getCacheSize(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.getNativeCache().size();
        }
        return 0;
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        
        // 清空本地缓存
        userCache.invalidateAll();
        permissionCache.invalidateAll();
        archiveCache.invalidateAll();
        statisticsCache.invalidateAll();
    }
}