package com.archive.management.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Map;

/**
 * 缓存服务接口
 * 提供多级缓存的统一访问接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public interface CacheService {

    /**
     * 获取缓存值
     */
    <T> T get(String cacheName, String key, Class<T> type);

    /**
     * 设置缓存值
     */
    void put(String cacheName, String key, Object value);

    /**
     * 设置缓存值（带TTL）
     */
    void put(String cacheName, String key, Object value, long ttl, TimeUnit timeUnit);

    /**
     * 删除缓存
     */
    void evict(String cacheName, String key);

    /**
     * 清空缓存
     */
    void clear(String cacheName);

    /**
     * 获取或计算缓存值
     */
    <T> T getOrCompute(String cacheName, String key, Function<String, T> computeFunction, Class<T> type);

    /**
     * 获取或计算缓存值（带TTL）
     */
    <T> T getOrCompute(String cacheName, String key, Function<String, T> computeFunction, 
                      Class<T> type, long ttl, TimeUnit timeUnit);

    /**
     * 用户缓存操作
     */
    <T> T getUser(String key, Class<T> type);
    void putUser(String key, Object value);
    void evictUser(String key);
    void clearUserCache();

    /**
     * 权限缓存操作
     */
    <T> T getPermission(String key, Class<T> type);
    void putPermission(String key, Object value);
    void evictPermission(String key);
    void clearPermissionCache();

    /**
     * 档案缓存操作
     */
    <T> T getArchive(String key, Class<T> type);
    void putArchive(String key, Object value);
    void evictArchive(String key);
    void clearArchiveCache();

    /**
     * 统计缓存操作
     */
    <T> T getStatistics(String key, Class<T> type);
    void putStatistics(String key, Object value);
    void evictStatistics(String key);
    void clearStatisticsCache();

    /**
     * 预热缓存
     */
    void warmUpCache(String cacheName, String key, Object value);

    /**
     * 批量预热缓存
     */
    void warmUpCache(String cacheName, Map<String, Object> data);

    /**
     * 获取缓存统计信息
     */
    String getCacheStats();

    /**
     * 检查缓存是否存在
     */
    boolean exists(String cacheName, String key);

    /**
     * 获取缓存大小
     */
    long getCacheSize(String cacheName);

    /**
     * 清空所有缓存
     */
    void clearAllCaches();
}
