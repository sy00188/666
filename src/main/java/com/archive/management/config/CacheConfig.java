package com.archive.management.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 * 配置Redis缓存管理器、RedisTemplate等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;

    @Value("${cache.default.ttl:3600}")
    private long defaultCacheTtl;

    /**
     * Redis连接工厂
     * 
     * @return Redis连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.setPassword(redisPassword);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(true);
        
        return factory;
    }

    /**
     * Redis模板配置
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置JSON序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 配置字符串序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置key和hashKey的序列化器
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 设置value和hashValue的序列化器
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(jackson2JsonRedisSerializer);

        // 启用事务支持
        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 字符串Redis模板
     * 
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        org.springframework.data.redis.core.StringRedisTemplate template = new org.springframework.data.redis.core.StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * 缓存管理器
     * 
     * @param connectionFactory Redis连接工厂
     * @return 缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置JSON序列化器
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultCacheTtl))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "archive:cache:" + cacheName + ":");

        // 特定缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存 - 30分钟
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 权限缓存 - 1小时
        cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 角色缓存 - 1小时
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 档案分类缓存 - 2小时
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 系统配置缓存 - 4小时
        cacheConfigurations.put("systemConfig", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // 字典数据缓存 - 6小时
        cacheConfigurations.put("dictData", defaultConfig.entryTtl(Duration.ofHours(6)));
        
        // 部门组织缓存 - 2小时
        cacheConfigurations.put("departments", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 档案统计缓存 - 15分钟
        cacheConfigurations.put("archiveStats", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 热门搜索缓存 - 1小时
        cacheConfigurations.put("hotSearch", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 验证码缓存 - 5分钟
        cacheConfigurations.put("captcha", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 短期缓存 - 5分钟
        cacheConfigurations.put("shortTerm", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 长期缓存 - 24小时
        cacheConfigurations.put("longTerm", defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * 创建Jackson序列化器
     * 
     * @return Jackson2JsonRedisSerializer
     */
    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }

    /**
     * 缓存键生成器
     * 
     * @return 键生成器
     */
    @Bean
    public org.springframework.cache.interceptor.KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");
            
            if (params.length > 0) {
                for (Object param : params) {
                    if (param != null) {
                        sb.append(param.toString()).append(":");
                    } else {
                        sb.append("null:");
                    }
                }
                // 移除最后一个冒号
                sb.setLength(sb.length() - 1);
            }
            
            return sb.toString();
        };
    }

    /**
     * 缓存错误处理器
     * 
     * @return 错误处理器
     */
    @Bean
    public org.springframework.cache.interceptor.CacheErrorHandler cacheErrorHandler() {
        return new org.springframework.cache.interceptor.SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                // 记录缓存获取错误，但不抛出异常
                System.err.println("缓存获取错误 - Cache: " + cache.getName() + ", Key: " + key + ", Error: " + exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                // 记录缓存存储错误，但不抛出异常
                System.err.println("缓存存储错误 - Cache: " + cache.getName() + ", Key: " + key + ", Error: " + exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                // 记录缓存清除错误，但不抛出异常
                System.err.println("缓存清除错误 - Cache: " + cache.getName() + ", Key: " + key + ", Error: " + exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                // 记录缓存清空错误，但不抛出异常
                System.err.println("缓存清空错误 - Cache: " + cache.getName() + ", Error: " + exception.getMessage());
            }
        };
    }

    /**
     * 缓存解析器
     * 
     * @param cacheManager 缓存管理器
     * @return 缓存解析器
     */
    @Bean
    public org.springframework.cache.interceptor.CacheResolver cacheResolver(CacheManager cacheManager) {
        org.springframework.cache.interceptor.SimpleCacheResolver resolver = new org.springframework.cache.interceptor.SimpleCacheResolver();
        resolver.setCacheManager(cacheManager);
        return resolver;
    }
}