package com.archive.management.interceptor;

import com.archive.management.util.LogUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 日志拦截器
 * 记录请求日志，设置请求ID和用户信息到MDC
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String IP_ADDRESS_KEY = "ipAddress";
    private static final String USER_AGENT_KEY = "userAgent";
    private static final String START_TIME_KEY = "startTime";

    /**
     * 请求处理前
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 是否继续处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "");
        
        // 获取客户端IP
        String ipAddress = getClientIpAddress(request);
        
        // 获取用户代理
        String userAgent = request.getHeader("User-Agent");
        
        // 获取用户ID（从认证信息中获取，如果已登录）
        String userId = getCurrentUserId(request);
        
        // 设置MDC
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(IP_ADDRESS_KEY, ipAddress);
        MDC.put(USER_AGENT_KEY, userAgent);
        MDC.put(START_TIME_KEY, String.valueOf(System.currentTimeMillis()));
        
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        }
        
        // 记录请求开始日志
        LogUtil.info("请求开始 - {} {} - IP: {} - User-Agent: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                ipAddress, 
                userAgent);
        
        return true;
    }

    /**
     * 请求处理后，视图渲染前
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @param modelAndView 模型和视图
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // 可以在这里添加一些后处理逻辑
    }

    /**
     * 请求完成后
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @param ex 异常（如果有）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            // 计算请求耗时
            String startTimeStr = MDC.get(START_TIME_KEY);
            long duration = 0;
            if (startTimeStr != null) {
                long startTime = Long.parseLong(startTimeStr);
                duration = System.currentTimeMillis() - startTime;
            }
            
            // 记录请求完成日志
            if (ex != null) {
                LogUtil.error("请求异常 - {} {} - 状态码: {} - 耗时: {}ms - 异常: {}", 
                        request.getMethod(), 
                        request.getRequestURI(), 
                        response.getStatus(), 
                        duration, 
                        ex.getMessage(), 
                        ex);
            } else {
                LogUtil.info("请求完成 - {} {} - 状态码: {} - 耗时: {}ms", 
                        request.getMethod(), 
                        request.getRequestURI(), 
                        response.getStatus(), 
                        duration);
            }
            
            // 记录访问日志
            recordAccessLog(request, response, duration, ex);
            
        } finally {
            // 清理MDC
            MDC.clear();
        }
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
        
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        String httpClientIp = request.getHeader("HTTP_CLIENT_IP");
        if (httpClientIp != null && !httpClientIp.isEmpty() && !"unknown".equalsIgnoreCase(httpClientIp)) {
            return httpClientIp;
        }
        
        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 获取当前用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID
     */
    private String getCurrentUserId(HttpServletRequest request) {
        try {
            // 从Spring Security上下文中获取用户信息
            org.springframework.security.core.Authentication authentication = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                Object principal = authentication.getPrincipal();
                
                // 如果是UserDetails实现类
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                }
                
                // 如果是字符串
                if (principal instanceof String) {
                    return (String) principal;
                }
                
                // 如果是自定义用户对象，需要根据实际情况调整
                // 例如：if (principal instanceof CustomUserDetails) {
                //     return ((CustomUserDetails) principal).getUserId();
                // }
            }
        } catch (Exception e) {
            LogUtil.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * 记录访问日志
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param duration 请求耗时
     * @param ex 异常（如果有）
     */
    private void recordAccessLog(HttpServletRequest request, HttpServletResponse response, 
                                long duration, Exception ex) {
        try {
            // 使用专门的访问日志记录器
            org.slf4j.Logger accessLogger = org.slf4j.LoggerFactory.getLogger("ACCESS");
            
            String logMessage = String.format(
                    "ACCESS - %s %s - IP: %s - User: %s - Status: %d - Duration: %dms - Size: %d - Referer: %s - UserAgent: %s",
                    request.getMethod(),
                    request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""),
                    MDC.get(IP_ADDRESS_KEY),
                    MDC.get(USER_ID_KEY) != null ? MDC.get(USER_ID_KEY) : "-",
                    response.getStatus(),
                    duration,
                    response.getBufferSize(),
                    request.getHeader("Referer") != null ? request.getHeader("Referer") : "-",
                    MDC.get(USER_AGENT_KEY) != null ? MDC.get(USER_AGENT_KEY) : "-"
            );
            
            if (ex != null) {
                accessLogger.error(logMessage + " - Exception: " + ex.getMessage());
            } else {
                accessLogger.info(logMessage);
            }
            
        } catch (Exception e) {
            LogUtil.warn("记录访问日志失败: {}", e.getMessage());
        }
    }

    /**
     * 判断是否需要记录请求体
     * 
     * @param request HTTP请求
     * @return 是否需要记录
     */
    private boolean shouldLogRequestBody(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // 只记录JSON和表单数据
        return contentType.contains("application/json") || 
               contentType.contains("application/x-www-form-urlencoded");
    }

    /**
     * 判断是否为静态资源请求
     * 
     * @param request HTTP请求
     * @return 是否为静态资源
     */
    private boolean isStaticResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains(".css") || uri.contains(".js") || uri.contains(".png") || 
               uri.contains(".jpg") || uri.contains(".jpeg") || uri.contains(".gif") || 
               uri.contains(".ico") || uri.contains(".svg") || uri.contains(".woff") || 
               uri.contains(".ttf") || uri.contains(".eot");
    }
}