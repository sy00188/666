package com.archive.management.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存服务接口
 * 提供系统缓存相关的操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface CacheService {

    /**
     * 设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存并指定过期时间
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param duration 过期时间
     */
    void set(String key, Object value, Duration duration);

    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return 缓存值
     */
    Object get(String key);

    /**
     * 获取缓存并指定类型
     * 
     * @param key 缓存键
     * @param clazz 值类型
     * @param <T> 泛型类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键列表
     * @return 删除成功的数量
     */
    long delete(List<String> keys);

    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存键
     * @param duration 过期时间
     * @return 是否设置成功
     */
    boolean expire(String key, Duration duration);

    /**
     * 获取缓存剩余过期时间
     * 
     * @param key 缓存键
     * @return 剩余过期时间（秒）
     */
    long getExpire(String key);

    /**
     * 根据模式匹配获取所有键
     * 
     * @param pattern 匹配模式
     * @return 匹配的键集合
     */
    Set<String> keys(String pattern);

    /**
     * 清空所有缓存
     */
    void clear();

    /**
     * 清空指定前缀的缓存
     * 
     * @param prefix 缓存键前缀
     * @return 清空的数量
     */
    long clearByPrefix(String prefix);

    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getStats();

    /**
     * 刷新缓存
     * 
     * @param key 缓存键
     * @param value 新值
     * @return 是否刷新成功
     */
    boolean refresh(String key, Object value);

    /**
     * 预热缓存
     * 
     * @param cacheData 预热数据
     * @return 预热成功的数量
     */
    int warmUp(Map<String, Object> cacheData);

    /**
     * 获取缓存大小
     * 
     * @return 缓存项数量
     */
    long size();

    /**
     * 获取缓存内存使用情况
     * 
     * @return 内存使用信息
     */
    Map<String, Object> getMemoryInfo();
}