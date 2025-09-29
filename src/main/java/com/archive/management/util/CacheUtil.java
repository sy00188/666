package com.archive.management.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 * 提供Redis缓存操作的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class CacheUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ========== 基础操作 ==========

    /**
     * 设置缓存
     * 
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean set(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置缓存（带过期时间）
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        if (StringUtil.isEmpty(key) || timeout <= 0) {
            return false;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置缓存（带过期时间，使用Duration）
     * 
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 是否成功
     */
    public boolean set(String key, Object value, Duration duration) {
        if (StringUtil.isEmpty(key) || duration == null || duration.isNegative()) {
            return false;
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, duration);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取缓存
     * 
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取缓存（指定类型）
     * 
     * @param key 键
     * @param clazz 类型
     * @param <T> 泛型类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        
        try {
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            
            // 如果是字符串类型，尝试JSON反序列化
            if (value instanceof String && !String.class.equals(clazz)) {
                return JsonUtil.fromJson((String) value, clazz);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 删除缓存
     * 
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(String key) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 批量删除缓存
     * 
     * @param keys 键集合
     * @return 删除的数量
     */
    public long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        if (StringUtil.isEmpty(key) || timeout <= 0) {
            return false;
        }
        
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取过期时间
     * 
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public long getExpire(String key) {
        if (StringUtil.isEmpty(key)) {
            return -2;
        }
        
        try {
            Long expire = redisTemplate.getExpire(key);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            return -2;
        }
    }

    // ========== 字符串操作 ==========

    /**
     * 设置字符串缓存
     * 
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean setString(String key, String value) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置字符串缓存（带过期时间）
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public boolean setString(String key, String value, long timeout, TimeUnit unit) {
        if (StringUtil.isEmpty(key) || timeout <= 0) {
            return false;
        }
        
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取字符串缓存
     * 
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 递增
     * 
     * @param key 键
     * @param delta 增量
     * @return 递增后的值
     */
    public long increment(String key, long delta) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long result = stringRedisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 递增（默认增量为1）
     * 
     * @param key 键
     * @return 递增后的值
     */
    public long increment(String key) {
        return increment(key, 1);
    }

    /**
     * 递减
     * 
     * @param key 键
     * @param delta 减量
     * @return 递减后的值
     */
    public long decrement(String key, long delta) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long result = stringRedisTemplate.opsForValue().decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 递减（默认减量为1）
     * 
     * @param key 键
     * @return 递减后的值
     */
    public long decrement(String key) {
        return decrement(key, 1);
    }

    // ========== Hash操作 ==========

    /**
     * 设置Hash字段
     * 
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return 是否成功
     */
    public boolean hSet(String key, String field, Object value) {
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(field)) {
            return false;
        }
        
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Hash字段值
     * 
     * @param key 键
     * @param field 字段
     * @return 值
     */
    public Object hGet(String key, String field) {
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(field)) {
            return null;
        }
        
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取Hash所有字段和值
     * 
     * @param key 键
     * @return 字段和值的映射
     */
    public Map<Object, Object> hGetAll(String key) {
        if (StringUtil.isEmpty(key)) {
            return new HashMap<>();
        }
        
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 批量设置Hash字段
     * 
     * @param key 键
     * @param map 字段和值的映射
     * @return 是否成功
     */
    public boolean hSetAll(String key, Map<String, Object> map) {
        if (StringUtil.isEmpty(key) || map == null || map.isEmpty()) {
            return false;
        }
        
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除Hash字段
     * 
     * @param key 键
     * @param fields 字段
     * @return 删除的字段数量
     */
    public long hDelete(String key, Object... fields) {
        if (StringUtil.isEmpty(key) || fields == null || fields.length == 0) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.opsForHash().delete(key, fields);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 判断Hash字段是否存在
     * 
     * @param key 键
     * @param field 字段
     * @return 是否存在
     */
    public boolean hExists(String key, String field) {
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(field)) {
            return false;
        }
        
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            return false;
        }
    }

    // ========== List操作 ==========

    /**
     * 从左侧推入列表
     * 
     * @param key 键
     * @param value 值
     * @return 列表长度
     */
    public long lPush(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.opsForList().leftPush(key, value);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 从右侧推入列表
     * 
     * @param key 键
     * @param value 值
     * @return 列表长度
     */
    public long rPush(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.opsForList().rightPush(key, value);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 从左侧弹出列表元素
     * 
     * @param key 键
     * @return 元素值
     */
    public Object lPop(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从右侧弹出列表元素
     * 
     * @param key 键
     * @return 元素值
     */
    public Object rPop(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取列表长度
     * 
     * @param key 键
     * @return 列表长度
     */
    public long lSize(String key) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取列表范围内的元素
     * 
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        if (StringUtil.isEmpty(key)) {
            return new ArrayList<>();
        }
        
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ========== Set操作 ==========

    /**
     * 添加Set元素
     * 
     * @param key 键
     * @param values 值
     * @return 添加的元素数量
     */
    public long sAdd(String key, Object... values) {
        if (StringUtil.isEmpty(key) || values == null || values.length == 0) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 移除Set元素
     * 
     * @param key 键
     * @param values 值
     * @return 移除的元素数量
     */
    public long sRemove(String key, Object... values) {
        if (StringUtil.isEmpty(key) || values == null || values.length == 0) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 判断Set是否包含元素
     * 
     * @param key 键
     * @param value 值
     * @return 是否包含
     */
    public boolean sIsMember(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Set所有元素
     * 
     * @param key 键
     * @return 元素集合
     */
    public Set<Object> sMembers(String key) {
        if (StringUtil.isEmpty(key)) {
            return new HashSet<>();
        }
        
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    /**
     * 获取Set大小
     * 
     * @param key 键
     * @return Set大小
     */
    public long sSize(String key) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ========== ZSet操作 ==========

    /**
     * 添加ZSet元素
     * 
     * @param key 键
     * @param value 值
     * @param score 分数
     * @return 是否成功
     */
    public boolean zAdd(String key, Object value, double score) {
        if (StringUtil.isEmpty(key)) {
            return false;
        }
        
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 移除ZSet元素
     * 
     * @param key 键
     * @param values 值
     * @return 移除的元素数量
     */
    public long zRemove(String key, Object... values) {
        if (StringUtil.isEmpty(key) || values == null || values.length == 0) {
            return 0;
        }
        
        try {
            Long count = redisTemplate.opsForZSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取ZSet元素分数
     * 
     * @param key 键
     * @param value 值
     * @return 分数
     */
    public Double zScore(String key, Object value) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取ZSet范围内的元素（按分数升序）
     * 
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        if (StringUtil.isEmpty(key)) {
            return new LinkedHashSet<>();
        }
        
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    /**
     * 获取ZSet范围内的元素（按分数降序）
     * 
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        if (StringUtil.isEmpty(key)) {
            return new LinkedHashSet<>();
        }
        
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    /**
     * 获取ZSet大小
     * 
     * @param key 键
     * @return ZSet大小
     */
    public long zSize(String key) {
        if (StringUtil.isEmpty(key)) {
            return 0;
        }
        
        try {
            Long size = redisTemplate.opsForZSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ========== 分布式锁 ==========

    /**
     * 获取分布式锁
     * 
     * @param lockKey 锁键
     * @param requestId 请求ID
     * @param expireTime 过期时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        if (StringUtil.isEmpty(lockKey) || StringUtil.isEmpty(requestId) || expireTime <= 0) {
            return false;
        }
        
        try {
            String result = stringRedisTemplate.execute(
                new DefaultRedisScript<>("return redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2], 'NX')", String.class),
                Collections.singletonList(lockKey),
                requestId,
                String.valueOf(expireTime)
            );
            return "OK".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁键
     * @param requestId 请求ID
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String requestId) {
        if (StringUtil.isEmpty(lockKey) || StringUtil.isEmpty(requestId)) {
            return false;
        }
        
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(lockKey),
                requestId
            );
            return Long.valueOf(1).equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 批量操作 ==========

    /**
     * 批量获取缓存
     * 
     * @param keys 键集合
     * @return 值列表
     */
    public List<Object> multiGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 批量设置缓存
     * 
     * @param map 键值映射
     * @return 是否成功
     */
    public boolean multiSet(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        
        try {
            redisTemplate.opsForValue().multiSet(map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 模式匹配 ==========

    /**
     * 根据模式获取键
     * 
     * @param pattern 模式
     * @return 键集合
     */
    public Set<String> keys(String pattern) {
        if (StringUtil.isEmpty(pattern)) {
            return new HashSet<>();
        }
        
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    /**
     * 根据模式删除键
     * 
     * @param pattern 模式
     * @return 删除的数量
     */
    public long deleteByPattern(String pattern) {
        if (StringUtil.isEmpty(pattern)) {
            return 0;
        }
        
        try {
            Set<String> keys = keys(pattern);
            if (keys.isEmpty()) {
                return 0;
            }
            return delete(keys);
        } catch (Exception e) {
            return 0;
        }
    }
}