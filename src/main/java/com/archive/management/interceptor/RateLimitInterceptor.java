package com.archive.management.interceptor;

import com.archive.management.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 请求限制拦截器
 * 基于Redis实现的分布式限流
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 默认限流配置
    private static final int DEFAULT_LIMIT = 100; // 默认每分钟100次请求
    private static final int DEFAULT_WINDOW = 60; // 默认时间窗口60秒

    // 不同接口的限流配置
    private static final Map<String, RateLimitConfig> RATE_LIMIT_CONFIGS = new HashMap<>();

    static {
        // 登录接口限流：每分钟5次
        RATE_LIMIT_CONFIGS.put("/api/auth/login", new RateLimitConfig(5, 60));
        
        // 注册接口限流：每小时3次
        RATE_LIMIT_CONFIGS.put("/api/auth/register", new RateLimitConfig(3, 3600));
        
        // 密码重置接口限流：每小时5次
        RATE_LIMIT_CONFIGS.put("/api/auth/reset-password", new RateLimitConfig(5, 3600));
        
        // 文件上传接口限流：每分钟10次
        RATE_LIMIT_CONFIGS.put("/api/files/upload", new RateLimitConfig(10, 60));
        
        // 搜索接口限流：每分钟30次
        RATE_LIMIT_CONFIGS.put("/api/search", new RateLimitConfig(30, 60));
        
        // API接口限流：每分钟50次
        RATE_LIMIT_CONFIGS.put("/api/", new RateLimitConfig(50, 60));
    }

    /**
     * 请求处理前进行限流检查
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 是否继续处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // 获取客户端标识
        String clientId = getClientId(request);
        
        // 获取请求路径
        String requestPath = request.getRequestURI();
        
        // 获取限流配置
        RateLimitConfig config = getRateLimitConfig(requestPath);
        
        // 执行限流检查
        if (!checkRateLimit(clientId, requestPath, config)) {
            // 限流触发，返回429状态码
            handleRateLimitExceeded(response, config);
            return false;
        }
        
        return true;
    }

    /**
     * 获取客户端标识
     * 
     * @param request HTTP请求
     * @return 客户端标识
     */
    private String getClientId(HttpServletRequest request) {
        // 优先使用用户ID（如果已登录）
        String userId = getCurrentUserId(request);
        if (userId != null) {
            return "user:" + userId;
        }
        
        // 使用IP地址作为标识
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress;
    }

    /**
     * 获取当前用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID
     */
    private String getCurrentUserId(HttpServletRequest request) {
        try {
            org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                }
                
                if (principal instanceof String) {
                    return (String) principal;
                }
            }
        } catch (Exception e) {
            LogUtil.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 获取限流配置
     * 
     * @param requestPath 请求路径
     * @return 限流配置
     */
    private RateLimitConfig getRateLimitConfig(String requestPath) {
        // 精确匹配
        RateLimitConfig config = RATE_LIMIT_CONFIGS.get(requestPath);
        if (config != null) {
            return config;
        }
        
        // 前缀匹配
        for (Map.Entry<String, RateLimitConfig> entry : RATE_LIMIT_CONFIGS.entrySet()) {
            if (requestPath.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 返回默认配置
        return new RateLimitConfig(DEFAULT_LIMIT, DEFAULT_WINDOW);
    }

    /**
     * 检查限流
     * 
     * @param clientId 客户端标识
     * @param requestPath 请求路径
     * @param config 限流配置
     * @return 是否通过限流检查
     */
    private boolean checkRateLimit(String clientId, String requestPath, RateLimitConfig config) {
        try {
            // 构建Redis键
            String key = buildRedisKey(clientId, requestPath, config.getWindowSeconds());
            
            // 获取当前计数
            String countStr = (String) redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
            
            if (currentCount >= config.getLimit()) {
                // 记录限流日志
                LogUtil.warn("限流触发 - 客户端: {} - 路径: {} - 当前计数: {} - 限制: {}/{}", 
                        clientId, requestPath, currentCount, config.getLimit(), config.getWindowSeconds());
                return false;
            }
            
            // 增加计数
            redisTemplate.opsForValue().increment(key);
            
            // 设置过期时间（如果是新键）
            if (currentCount == 0) {
                redisTemplate.expire(key, config.getWindowSeconds(), TimeUnit.SECONDS);
            }
            
            return true;
            
        } catch (Exception e) {
            LogUtil.error("限流检查异常: {}", e.getMessage(), e);
            // 异常情况下允许通过，避免影响正常业务
            return true;
        }
    }

    /**
     * 构建Redis键
     * 
     * @param clientId 客户端标识
     * @param requestPath 请求路径
     * @param windowSeconds 时间窗口
     * @return Redis键
     */
    private String buildRedisKey(String clientId, String requestPath, int windowSeconds) {
        long currentWindow = System.currentTimeMillis() / (windowSeconds * 1000L);
        return String.format("rate_limit:%s:%s:%d", clientId, requestPath.replaceAll("/", "_"), currentWindow);
    }

    /**
     * 处理限流超出
     * 
     * @param response HTTP响应
     * @param config 限流配置
     */
    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitConfig config) 
            throws IOException {
        
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType("application/json;charset=UTF-8");
        
        // 构建响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("message", "请求过于频繁，请稍后再试");
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        
        // 添加限流信息
        Map<String, Object> rateLimitInfo = new HashMap<>();
        rateLimitInfo.put("limit", config.getLimit());
        rateLimitInfo.put("windowSeconds", config.getWindowSeconds());
        rateLimitInfo.put("retryAfter", config.getWindowSeconds());
        result.put("rateLimitInfo", rateLimitInfo);
        
        // 设置响应头
        response.setHeader("X-RateLimit-Limit", String.valueOf(config.getLimit()));
        response.setHeader("X-RateLimit-Window", String.valueOf(config.getWindowSeconds()));
        response.setHeader("Retry-After", String.valueOf(config.getWindowSeconds()));
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(result));
        response.getWriter().flush();
    }

    /**
     * 获取剩余请求次数
     * 
     * @param clientId 客户端标识
     * @param requestPath 请求路径
     * @param config 限流配置
     * @return 剩余请求次数
     */
    public int getRemainingRequests(String clientId, String requestPath, RateLimitConfig config) {
        try {
            String key = buildRedisKey(clientId, requestPath, config.getWindowSeconds());
            String countStr = (String) redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
            return Math.max(0, config.getLimit() - currentCount);
        } catch (Exception e) {
            LogUtil.error("获取剩余请求次数异常: {}", e.getMessage(), e);
            return config.getLimit();
        }
    }

    /**
     * 清除限流记录
     * 
     * @param clientId 客户端标识
     * @param requestPath 请求路径
     */
    public void clearRateLimit(String clientId, String requestPath) {
        try {
            // 清除所有时间窗口的记录
            String pattern = String.format("rate_limit:%s:%s:*", clientId, requestPath.replaceAll("/", "_"));
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            LogUtil.info("清除限流记录 - 客户端: {} - 路径: {}", clientId, requestPath);
        } catch (Exception e) {
            LogUtil.error("清除限流记录异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 限流配置类
     */
    public static class RateLimitConfig {
        private final int limit;
        private final int windowSeconds;

        public RateLimitConfig(int limit, int windowSeconds) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }

        public int getLimit() {
            return limit;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        @Override
        public String toString() {
            return String.format("RateLimitConfig{limit=%d, windowSeconds=%d}", limit, windowSeconds);
        }
    }
}