package com.archive.management.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis配置类
 * 配置Redis连接、序列化器、缓存管理器等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeout:5000}")
    private int timeout;

    @Value("${spring.redis.lettuce.pool.max-active:20}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-idle:10}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle:5}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-wait:-1}")
    private long maxWait;

    @Value("${spring.redis.lettuce.shutdown-timeout:100}")
    private long shutdownTimeout;

    /**
     * 配置Lettuce客户端资源
     */
    @Bean(destroyMethod = "shutdown")
    public ClientResources clientResources() {
        return DefaultClientResources.create();
    }

    /**
     * 配置Redis连接工厂
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis单机配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        redisConfig.setDatabase(database);
        if (password != null && !password.trim().isEmpty()) {
            redisConfig.setPassword(password);
        }

        // 连接池配置
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofMinutes(1).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofMinutes(1).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);

        // Lettuce客户端配置
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .clientResources(clientResources())
                .commandTimeout(Duration.ofMillis(timeout))
                .shutdownTimeout(Duration.ofMillis(shutdownTimeout))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * 配置Jackson2JsonRedisSerializer
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(new JavaTimeModule());
        
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    /**
     * 配置RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = jackson2JsonRedisSerializer();
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 开启事务支持
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置字符串RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认1小时过期
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(jackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 不同缓存的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存 - 30分钟
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 档案缓存 - 2小时
        cacheConfigurations.put("archive", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 系统配置缓存 - 24小时
        cacheConfigurations.put("system-config", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // 权限缓存 - 1小时
        cacheConfigurations.put("permission", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 审计日志缓存 - 10分钟
        cacheConfigurations.put("audit-log", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 短期缓存 - 5分钟
        cacheConfigurations.put("short-term", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 长期缓存 - 12小时
        cacheConfigurations.put("long-term", defaultConfig.entryTtl(Duration.ofHours(12)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();
    }

    /**
     * 检查Redis连接健康状态
     */
    public boolean checkRedisHealth(RedisTemplate<String, Object> redisTemplate) {
        try {
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "OK";
            
            // 测试写入
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
            
            // 测试读取
            String result = (String) redisTemplate.opsForValue().get(testKey);
            
            // 清理测试数据
            redisTemplate.delete(testKey);
            
            return testValue.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Redis连接信息
     */
    public String getRedisConnectionInfo() {
        return String.format("Redis连接信息 - 主机: %s, 端口: %d, 数据库: %d", host, port, database);
    }

    /**
     * 清理所有缓存
     */
    public void clearAllCache(CacheManager cacheManager) {
        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats(CacheManager cacheManager) {
        Map<String, Object> stats = new HashMap<>();
        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                stats.put(cacheName, "缓存存在");
            }
        });
        return stats;
    }
}