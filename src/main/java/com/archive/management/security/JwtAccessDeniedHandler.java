package com.archive.management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT访问拒绝处理器
 * 处理权限不足的访问
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理访问拒绝异常
     * @param request HTTP请求
     * @param response HTTP响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "匿名用户";
        
        logger.warn("用户 {} 尝试访问无权限的资源: {} {}", username, request.getMethod(), request.getRequestURI());
        logger.debug("访问拒绝异常详情: {}", accessDeniedException.getMessage());

        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", 403);
        errorResponse.put("message", "权限不足，无法访问此资源");
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("user", username);

        // 根据请求路径提供更具体的权限说明
        String specificMessage = getSpecificPermissionMessage(request, authentication);
        if (specificMessage != null) {
            errorResponse.put("detail", specificMessage);
        }

        // 提供权限建议
        String suggestion = getPermissionSuggestion(request, authentication);
        if (suggestion != null) {
            errorResponse.put("suggestion", suggestion);
        }

        // 添加调试信息（仅在开发环境）
        if (isDebugMode()) {
            errorResponse.put("exception", accessDeniedException.getClass().getSimpleName());
            errorResponse.put("exceptionMessage", accessDeniedException.getMessage());
            if (authentication != null) {
                errorResponse.put("userAuthorities", authentication.getAuthorities().toString());
            }
        }

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }

    /**
     * 获取具体的权限说明
     * @param request HTTP请求
     * @param authentication 认证信息
     * @return 具体权限说明
     */
    private String getSpecificPermissionMessage(HttpServletRequest request, Authentication authentication) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 根据请求路径分析所需权限
        if (uri.startsWith("/api/admin/")) {
            return "此功能需要管理员权限";
        } else if (uri.startsWith("/api/system/")) {
            return "此功能需要系统管理权限";
        } else if (uri.startsWith("/api/user/") && ("PUT".equals(method) || "DELETE".equals(method))) {
            return "修改或删除用户信息需要用户管理权限";
        } else if (uri.startsWith("/api/archive/")) {
            if ("POST".equals(method)) {
                return "创建档案需要档案管理权限";
            } else if ("PUT".equals(method) || "PATCH".equals(method)) {
                return "修改档案需要档案管理权限";
            } else if ("DELETE".equals(method)) {
                return "删除档案需要档案管理权限";
            } else {
                return "访问档案需要档案查看权限";
            }
        } else if (uri.startsWith("/api/borrow/")) {
            if ("POST".equals(method)) {
                return "创建借阅记录需要借阅管理权限";
            } else if ("PUT".equals(method) || "PATCH".equals(method)) {
                return "修改借阅记录需要借阅管理权限";
            } else {
                return "查看借阅记录需要借阅查看权限";
            }
        } else if (uri.startsWith("/api/category/")) {
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                return "管理分类需要分类管理权限";
            } else {
                return "查看分类需要分类查看权限";
            }
        } else if (uri.startsWith("/api/department/")) {
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                return "管理部门需要部门管理权限";
            } else {
                return "查看部门需要部门查看权限";
            }
        } else if (uri.startsWith("/api/role/")) {
            return "角色管理需要角色管理权限";
        } else if (uri.startsWith("/api/permission/")) {
            return "权限管理需要权限管理权限";
        }

        return "您当前的权限不足以执行此操作";
    }

    /**
     * 获取权限建议
     * @param request HTTP请求
     * @param authentication 认证信息
     * @return 权限建议
     */
    private String getPermissionSuggestion(HttpServletRequest request, Authentication authentication) {
        String uri = request.getRequestURI();
        
        if (uri.startsWith("/api/admin/") || uri.startsWith("/api/system/")) {
            return "请联系系统管理员为您分配相应的管理权限";
        } else if (uri.startsWith("/api/user/") || uri.startsWith("/api/archive/") || 
                  uri.startsWith("/api/borrow/") || uri.startsWith("/api/category/")) {
            return "请联系管理员为您分配相应的功能权限，或检查您的角色配置";
        } else if (uri.startsWith("/api/role/") || uri.startsWith("/api/permission/")) {
            return "权限和角色管理功能仅限系统管理员使用";
        }

        return "请联系管理员检查您的权限配置";
    }

    /**
     * 检查是否为调试模式
     * @return 是否为调试模式
     */
    private boolean isDebugMode() {
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}