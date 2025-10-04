package com.archive.management.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 多级缓存配置类
 * 实现本地缓存 + Redis分布式缓存的多级缓存架构
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis模板配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 本地缓存管理器 (Caffeine)
     */
    @Bean
    public CaffeineCacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(2))
                .recordStats());
        return cacheManager;
    }

    /**
     * Redis缓存管理器
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.Builder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .disableCachingNullValues());
        
        // 配置不同缓存的TTL
        Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存 - 30分钟
        cacheConfigurations.put("users", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)));
        
        // 权限缓存 - 1小时
        cacheConfigurations.put("permissions", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)));
        
        // 角色缓存 - 1小时
        cacheConfigurations.put("roles", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)));
        
        // 部门缓存 - 2小时
        cacheConfigurations.put("departments", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2)));
        
        // 档案缓存 - 15分钟
        cacheConfigurations.put("archives", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)));
        
        // 配置缓存 - 2小时
        cacheConfigurations.put("config", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2)));
        
        // 统计缓存 - 5分钟
        cacheConfigurations.put("statistics", org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)));
        
        builder.withInitialCacheConfigurations(cacheConfigurations);
        
        return builder.build();
    }

    /**
     * 复合缓存管理器 - 多级缓存
     */
    @Bean
    @Primary
    public CacheManager cacheManager(CaffeineCacheManager localCacheManager, 
                                   RedisCacheManager redisCacheManager) {
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        compositeCacheManager.setCacheManagers(Arrays.asList(localCacheManager, redisCacheManager));
        compositeCacheManager.setFallbackToNoOpCache(true);
        return compositeCacheManager;
    }

    /**
     * 缓存配置属性
     */
    @Bean
    public CacheProperties cacheProperties() {
        return new CacheProperties();
    }

    /**
     * 缓存统计配置
     */
    @Bean
    public CacheStatisticsConfig cacheStatisticsConfig() {
        return new CacheStatisticsConfig();
    }

    /**
     * 用户信息本地缓存
     */
    @Bean
    public LoadingCache<String, Object> userCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .build(key -> {
                    // 缓存未命中时的加载逻辑
                    return null;
                });
    }

    /**
     * 权限信息本地缓存
     */
    @Bean
    public LoadingCache<String, Object> permissionCache() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .recordStats()
                .build(key -> {
                    // 缓存未命中时的加载逻辑
                    return null;
                });
    }

    /**
     * 档案信息本地缓存
     */
    @Bean
    public LoadingCache<String, Object> archiveCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(2))
                .recordStats()
                .build(key -> {
                    // 缓存未命中时的加载逻辑
                    return null;
                });
    }

    /**
     * 统计信息本地缓存
     */
    @Bean
    public LoadingCache<String, Object> statisticsCache() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(2))
                .expireAfterAccess(Duration.ofMinutes(1))
                .recordStats()
                .build(key -> {
                    // 缓存未命中时的加载逻辑
                    return null;
                });
    }
}