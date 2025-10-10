package com.archive.management.interceptor;

import com.archive.management.util.JwtUtil;
import com.archive.management.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证拦截器
 * 用于验证JWT Token和用户身份
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 白名单路径（不需要认证）
     */
    private static final String[] WHITE_LIST = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/captcha",
        "/api/auth/refresh",
        "/api/actuator/**",
        "/api/doc.html",
        "/api/swagger-ui/**",
        "/api/v3/api-docs/**"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        String requestURI = request.getRequestURI();
        
        // 检查是否在白名单中
        if (isInWhiteList(requestURI)) {
            return true;
        }

        // 如果JwtUtil未配置，跳过认证
        if (jwtUtil == null) {
            return true;
        }

        // 获取Token
        String token = getTokenFromRequest(request);
        
        if (!StringUtils.hasText(token)) {
            sendUnauthorizedResponse(response, "未提供认证令牌");
            return false;
        }

        try {
            // 验证Token
            if (!jwtUtil.validateToken(token)) {
                sendUnauthorizedResponse(response, "认证令牌无效或已过期");
                return false;
            }

            // Token有效，继续处理
            return true;
            
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "认证验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isInWhiteList(String requestURI) {
        for (String pattern : WHITE_LIST) {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                if (requestURI.startsWith(prefix)) {
                    return true;
                }
            } else if (requestURI.equals(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 优先从Header中获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 其次从请求参数中获取
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("timestamp", System.currentTimeMillis());
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

