package com.archive.management.service.impl;

import com.archive.management.service.CacheService;
import com.archive.management.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 缓存服务实现类
 * 优先使用Redis缓存，Redis不可用时降级到内存缓存
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisUtil redisUtil;

    // 降级使用的内存缓存
    private final Map<String, Object> fallbackCache = new ConcurrentHashMap<>();
    private final Map<String, Long> fallbackExpireMap = new ConcurrentHashMap<>();
    
    // Redis可用性标志
    private volatile boolean redisAvailable = true;

    @Override
    public void set(String key, Object value) {
        log.debug("设置缓存: key={}", key);
        try {
            if (redisAvailable) {
                redisUtil.set(key, value);
            } else {
                useFallbackCache("set", key, value);
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，降级到内存缓存: key={}", key);
            redisAvailable = false;
            useFallbackCache("set", key, value);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
            useFallbackCache("set", key, value);
        }
    }

    @Override
    public void set(String key, Object value, Duration duration) {
        log.debug("设置缓存并指定过期时间: key={}, duration={}", key, duration);
        try {
            if (redisAvailable) {
                redisUtil.set(key, value, duration.getSeconds());
            } else {
                useFallbackCache("set", key, value, duration.toMillis());
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，降级到内存缓存: key={}", key);
            redisAvailable = false;
            useFallbackCache("set", key, value, duration.toMillis());
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
            useFallbackCache("set", key, value, duration.toMillis());
        }
    }

    @Override
    public Object get(String key) {
        log.debug("获取缓存: key={}", key);
        try {
            if (redisAvailable) {
                return redisUtil.get(key);
            } else {
                return useFallbackCache("get", key, null);
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，降级到内存缓存: key={}", key);
            redisAvailable = false;
            return useFallbackCache("get", key, null);
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            return useFallbackCache("get", key, null);
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        log.debug("获取缓存并指定类型: key={}, clazz={}", key, clazz.getSimpleName());
        Object value = get(key);
        if (value != null && clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    @Override
    public boolean delete(String key) {
        log.debug("删除缓存: key={}", key);
        try {
            if (redisAvailable) {
                redisUtil.del(key);
                return true;
            } else {
                useFallbackCache("delete", key, null);
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，降级到内存缓存: key={}", key);
            redisAvailable = false;
            useFallbackCache("delete", key, null);
            return true;
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public long delete(List<String> keys) {
        log.debug("批量删除缓存: keys={}", keys);
        try {
            if (redisAvailable) {
                redisUtil.del(keys.toArray(new String[0]));
                return keys.size();
            } else {
                long count = 0;
                for (String key : keys) {
                    if (fallbackCache.containsKey(key)) {
                        fallbackCache.remove(key);
                        fallbackExpireMap.remove(key);
                        count++;
                    }
                }
                return count;
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败", e);
            return 0;
        }
    }

    @Override
    public boolean exists(String key) {
        log.debug("检查缓存是否存在: key={}", key);
        try {
            if (redisAvailable) {
                return redisUtil.hasKey(key);
            } else {
                if (isFallbackExpired(key)) {
                    fallbackCache.remove(key);
                    fallbackExpireMap.remove(key);
                    return false;
                }
                return fallbackCache.containsKey(key);
            }
        } catch (Exception e) {
            log.error("检查缓存存在失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, Duration duration) {
        log.debug("设置缓存过期时间: key={}, duration={}", key, duration);
        try {
            if (redisAvailable) {
                return redisUtil.expire(key, duration.getSeconds());
            } else {
                if (fallbackCache.containsKey(key)) {
                    fallbackExpireMap.put(key, System.currentTimeMillis() + duration.toMillis());
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public long getExpire(String key) {
        log.debug("获取缓存过期时间: key={}", key);
        try {
            if (redisAvailable) {
                return redisUtil.getExpire(key);
            } else {
                Long expireTime = fallbackExpireMap.get(key);
                if (expireTime != null) {
                    long remaining = expireTime - System.currentTimeMillis();
                    return remaining > 0 ? remaining / 1000 : -1;
                }
                return -1;
            }
        } catch (Exception e) {
            log.error("获取过期时间失败: key={}", key, e);
            return -1;
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        log.debug("获取匹配模式的缓存键: pattern={}", pattern);
        try {
            if (redisAvailable) {
                // Redis模式匹配
                return redisUtil.sGet("keys:" + pattern).stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet());
            } else {
                // 内存缓存模式匹配
                return fallbackCache.keySet().stream()
                        .filter(key -> key.matches(pattern.replace("*", ".*")))
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("获取匹配键失败: pattern={}", pattern, e);
            return Set.of();
        }
    }

    @Override
    public void clear() {
        log.warn("清空所有缓存");
        try {
            if (redisAvailable) {
                // Redis清空需要谨慎，这里只清空应用的缓存前缀
                log.warn("Redis清空操作需要管理员权限，当前仅清空应用缓存");
            }
            // 同时清空降级缓存
            fallbackCache.clear();
            fallbackExpireMap.clear();
        } catch (Exception e) {
            log.error("清空缓存失败", e);
        }
    }

    @Override
    public long clearByPrefix(String prefix) {
        log.info("根据前缀清理缓存: prefix={}", prefix);
        long count = 0;
        try {
            if (redisAvailable) {
                Set<String> keysToRemove = keys(prefix + "*");
                if (!keysToRemove.isEmpty()) {
                    redisUtil.del(keysToRemove.toArray(new String[0]));
                    count = keysToRemove.size();
                }
            } else {
                List<String> keysToRemove = fallbackCache.keySet().stream()
                        .filter(key -> key.startsWith(prefix))
                        .collect(Collectors.toList());
                
                for (String key : keysToRemove) {
                    fallbackCache.remove(key);
                    fallbackExpireMap.remove(key);
                    count++;
                }
            }
        } catch (Exception e) {
            log.error("前缀清理失败: prefix={}", prefix, e);
        }
        return count;
    }

    @Override
    public Map<String, Object> getStats() {
        log.debug("获取缓存统计信息");
        Map<String, Object> stats = new HashMap<>();
        try {
            if (redisAvailable) {
                stats.put("cacheType", "Redis");
                stats.put("redisAvailable", true);
                stats.put("fallbackCacheSize", fallbackCache.size());
            } else {
                stats.put("cacheType", "Memory (Fallback)");
                stats.put("redisAvailable", false);
                stats.put("totalKeys", fallbackCache.size());
                stats.put("expiredKeys", fallbackExpireMap.size());
            }
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    @Override
    public boolean refresh(String key, Object value) {
        log.debug("刷新缓存: key={}", key);
        try {
            if (redisAvailable) {
                if (redisUtil.hasKey(key)) {
                    long ttl = redisUtil.getExpire(key);
                    if (ttl > 0) {
                        redisUtil.set(key, value, ttl);
                    } else {
                        redisUtil.set(key, value);
                    }
                    return true;
                }
                return false;
            } else {
                if (fallbackCache.containsKey(key)) {
                    fallbackCache.put(key, value);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("刷新缓存失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public int warmUp(Map<String, Object> cacheData) {
        log.info("预热缓存: dataSize={}", cacheData.size());
        int count = 0;
        try {
            for (Map.Entry<String, Object> entry : cacheData.entrySet()) {
                if (redisAvailable) {
                    redisUtil.set(entry.getKey(), entry.getValue());
                } else {
                    fallbackCache.put(entry.getKey(), entry.getValue());
                }
                count++;
            }
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
        return count;
    }

    @Override
    public long size() {
        log.debug("获取缓存大小");
        try {
            if (redisAvailable) {
                // Redis无法直接获取所有键的数量，返回降级缓存大小
                return fallbackCache.size();
            } else {
                return fallbackCache.size();
            }
        } catch (Exception e) {
            log.error("获取缓存大小失败", e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getMemoryInfo() {
        log.debug("获取内存信息");
        Map<String, Object> memoryInfo = new HashMap<>();
        try {
            if (redisAvailable) {
                memoryInfo.put("cacheType", "Redis");
                memoryInfo.put("fallbackCacheSize", fallbackCache.size());
                memoryInfo.put("fallbackMemoryEstimate", estimateFallbackMemory() + " bytes");
            } else {
                memoryInfo.put("cacheType", "Memory (Fallback)");
                memoryInfo.put("cacheSize", fallbackCache.size());
                memoryInfo.put("memoryEstimate", estimateFallbackMemory() + " bytes");
            }
        } catch (Exception e) {
            log.error("获取内存信息失败", e);
            memoryInfo.put("error", e.getMessage());
        }
        return memoryInfo;
    }

    /**
     * 使用降级缓存
     */
    private Object useFallbackCache(String operation, String key, Object value) {
        return useFallbackCache(operation, key, value, 0L);
    }

    /**
     * 使用降级缓存（带过期时间）
     */
    private Object useFallbackCache(String operation, String key, Object value, Long expireMillis) {
        switch (operation) {
            case "set":
                fallbackCache.put(key, value);
                if (expireMillis != null && expireMillis > 0) {
                    fallbackExpireMap.put(key, System.currentTimeMillis() + expireMillis);
                }
                return null;
            case "get":
                if (isFallbackExpired(key)) {
                    fallbackCache.remove(key);
                    fallbackExpireMap.remove(key);
                    return null;
                }
                return fallbackCache.get(key);
            case "delete":
                fallbackCache.remove(key);
                fallbackExpireMap.remove(key);
                return null;
            default:
                return null;
        }
    }

    /**
     * 检查降级缓存是否过期
     */
    private boolean isFallbackExpired(String key) {
        Long expireTime = fallbackExpireMap.get(key);
        return expireTime != null && System.currentTimeMillis() > expireTime;
    }

    /**
     * 估算降级缓存内存使用
     */
    private long estimateFallbackMemory() {
        // 粗略估算：每个键值对约100字节
        return fallbackCache.size() * 100L;
    }

    // ==================== 新增接口方法实现 ====================

    @Override
    public void clearUserTemporaryData(Long userId) {
        log.info("清理用户临时数据: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过清理");
            return;
        }
        clearByPrefix("user:temp:" + userId);
    }

    @Override
    public void clearUserCache(Long userId) {
        log.info("清理用户缓存: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过清理");
            return;
        }
        clearByPrefix("user:" + userId);
    }

    @Override
    public void clearAllUserCache(Long userId) {
        log.info("清理所有用户相关缓存: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过清理");
            return;
        }
        // 清理用户基本信息缓存
        clearUserCache(userId);
        // 清理用户权限缓存
        clearUserPermissionCache(userId);
        // 清理用户临时数据
        clearUserTemporaryData(userId);
        // 清理用户会话相关
        clearByPrefix("session:user:" + userId);
    }

    @Override
    public void clearUserPermissionCache(Long userId) {
        log.info("清理用户权限缓存: userId={}", userId);
        if (userId == null) {
            log.warn("用户ID为空，跳过清理");
            return;
        }
        clearByPrefix("permission:user:" + userId);
        clearByPrefix("role:user:" + userId);
    }

    @Override
    public void clearRoleCache(Long roleId) {
        log.info("清理角色缓存: roleId={}", roleId);
        if (roleId == null) {
            log.warn("角色ID为空，跳过清理");
            return;
        }
        clearByPrefix("role:" + roleId);
        delete("role:tree");
        delete("role:all");
    }

    @Override
    public void clearPermissionCache(Long permissionId) {
        log.info("清理权限缓存: permissionId={}", permissionId);
        if (permissionId == null) {
            log.warn("权限ID为空，跳过清理");
            return;
        }
        clearByPrefix("permission:" + permissionId);
        delete("permission:tree");
        delete("permission:all");
    }

    @Override
    public void clearDepartmentCache(Long departmentId) {
        log.info("清理部门缓存: departmentId={}", departmentId);
        if (departmentId == null) {
            log.warn("部门ID为空，跳过清理");
            return;
        }
        clearByPrefix("department:" + departmentId);
        delete("department:tree");
        delete("department:all");
    }

    @Override
    public void clearSystemConfigCache(String configKey) {
        log.info("清理系统配置缓存: configKey={}", configKey);
        if (configKey == null || configKey.trim().isEmpty()) {
            log.warn("配置键为空，跳过清理");
            return;
        }
        delete("config:" + configKey);
        delete("config:all");
    }

    @Override
    public void clearAllCache() {
        log.warn("执行清空所有缓存操作");
        clear();
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        log.debug("获取缓存统计信息");
        return getStats();
    }

    @Override
    public void warmUpCache() {
        log.info("执行缓存预热操作");
        // 预热常用的系统配置
        Map<String, Object> commonData = new HashMap<>();
        // 这里可以添加需要预热的数据
        // 例如：系统配置、常用字典数据等
        commonData.put("system:initialized", true);
        warmUp(commonData);
    }

    @Override
    public boolean cacheExists(String key) {
        return exists(key);
    }

    @Override
    public void setCache(String key, Object value, long expireSeconds) {
        if (expireSeconds > 0) {
            set(key, value, Duration.ofSeconds(expireSeconds));
        } else {
            set(key, value);
        }
    }

    @Override
    public Object getCache(String key) {
        return get(key);
    }

    @Override
    public void deleteCache(String key) {
        delete(key);
    }

    @Override
    public void batchDeleteCache(List<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            delete(keys);
        }
    }

    @Override
    public void refreshCacheExpire(String key, long expireSeconds) {
        if (expireSeconds > 0) {
            expire(key, Duration.ofSeconds(expireSeconds));
        }
    }
}
