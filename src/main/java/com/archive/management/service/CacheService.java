package com.archive.management.service;

/**
 * 缓存服务接口
 * 负责处理系统缓存相关的业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface CacheService {

    /**
     * 清理用户临时数据
     * 
     * @param userId 用户ID
     */
    void clearUserTemporaryData(Long userId);

    /**
     * 清理用户缓存
     * 
     * @param userId 用户ID
     */
    void clearUserCache(Long userId);

    /**
     * 清理所有用户缓存
     * 
     * @param userId 用户ID
     */
    void clearAllUserCache(Long userId);

    /**
     * 清理用户权限缓存
     * 
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);

    /**
     * 清理角色缓存
     * 
     * @param roleId 角色ID
     */
    void clearRoleCache(Long roleId);

    /**
     * 清理权限缓存
     * 
     * @param permissionId 权限ID
     */
    void clearPermissionCache(Long permissionId);

    /**
     * 清理部门缓存
     * 
     * @param departmentId 部门ID
     */
    void clearDepartmentCache(Long departmentId);

    /**
     * 清理系统配置缓存
     * 
     * @param configKey 配置键
     */
    void clearSystemConfigCache(String configKey);

    /**
     * 清理所有缓存
     */
    void clearAllCache();

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    java.util.Map<String, Object> getCacheStatistics();

    /**
     * 预热缓存
     */
    void warmUpCache();

    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean cacheExists(String key);

    /**
     * 设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    void setCache(String key, Object value, long expireSeconds);

    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return 缓存值
     */
    Object getCache(String key);

    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    void deleteCache(String key);

    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键列表
     */
    void batchDeleteCache(java.util.List<String> keys);

    /**
     * 刷新缓存过期时间
     * 
     * @param key 缓存键
     * @param expireSeconds 过期时间（秒）
     */
    void refreshCacheExpire(String key, long expireSeconds);
}
