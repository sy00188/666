package com.archive.management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点
 * 处理未认证用户的访问
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证异常
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authException 认证异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        logger.warn("未认证用户尝试访问受保护资源: {} {}", request.getMethod(), request.getRequestURI());
        logger.debug("认证异常详情: {}", authException.getMessage());

        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", 401);
        errorResponse.put("message", "未认证或认证已过期，请重新登录");
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("path", request.getRequestURI());

        // 根据请求路径和异常类型提供更具体的错误信息
        String specificMessage = getSpecificErrorMessage(request, authException);
        if (specificMessage != null) {
            errorResponse.put("detail", specificMessage);
        }

        // 添加调试信息（仅在开发环境）
        if (isDebugMode()) {
            errorResponse.put("exception", authException.getClass().getSimpleName());
            errorResponse.put("exceptionMessage", authException.getMessage());
        }

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }

    /**
     * 获取具体的错误信息
     * @param request HTTP请求
     * @param authException 认证异常
     * @return 具体错误信息
     */
    private String getSpecificErrorMessage(HttpServletRequest request, AuthenticationException authException) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 根据请求路径提供具体建议
        if (uri.startsWith("/api/admin/")) {
            return "访问管理功能需要管理员权限，请使用管理员账户登录";
        } else if (uri.startsWith("/api/user/")) {
            return "访问用户功能需要登录，请先进行身份认证";
        } else if (uri.startsWith("/api/archive/")) {
            return "访问档案功能需要相应权限，请确认您的账户权限";
        } else if (uri.startsWith("/api/borrow/")) {
            return "访问借阅功能需要登录，请先进行身份认证";
        }

        // 根据HTTP方法提供建议
        if ("POST".equals(method)) {
            return "执行此操作需要身份认证，请先登录";
        } else if ("PUT".equals(method) || "PATCH".equals(method)) {
            return "修改操作需要身份认证，请先登录";
        } else if ("DELETE".equals(method)) {
            return "删除操作需要身份认证，请先登录";
        }

        // 检查是否是令牌相关的问题
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "请求头中缺少认证令牌，请在Authorization头中提供有效的JWT令牌";
        } else if (!authHeader.startsWith("Bearer ")) {
            return "认证令牌格式错误，请使用Bearer Token格式";
        } else {
            return "认证令牌无效或已过期，请重新获取令牌";
        }
    }

    /**
     * 检查是否为调试模式
     * @return 是否为调试模式
     */
    private boolean isDebugMode() {
        // 这里可以根据配置文件或环境变量来判断
        // 暂时返回false，生产环境不应该暴露异常详情
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}