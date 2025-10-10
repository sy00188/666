package com.archive.management.util;

import com.archive.management.entity.User;
import com.archive.management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 安全工具类
 * 提供获取当前认证用户信息的静态方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    
    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        SecurityUtils.userService = userService;
    }

    /**
     * 获取当前认证信息
     * @return Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户名
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username != null && userService != null) {
            try {
                User user = userService.getUserByUsername(username);
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                logger.error("获取当前用户ID失败: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 获取当前用户对象
     * @return 用户对象
     */
    public static User getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null && userService != null) {
            try {
                return userService.getUserByUsername(username);
            } catch (Exception e) {
                logger.error("获取当前用户对象失败: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 检查当前用户是否已认证
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否有指定角色
     * @param role 角色名称
     * @return 是否有该角色
     */
    public static boolean hasRole(String role) {
        if (!isAuthenticated() || userService == null) {
            return false;
        }
        
        try {
            Long userId = getCurrentUserId();
            if (userId != null) {
                List<String> userRoles = userService.getUserRoles(userId);
                return userRoles != null && userRoles.contains(role);
            }
        } catch (Exception e) {
            logger.error("检查用户角色失败: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 检查当前用户是否有指定权限
     * @param permission 权限名称
     * @return 是否有该权限
     */
    public static boolean hasPermission(String permission) {
        if (!isAuthenticated() || userService == null) {
            return false;
        }
        
        try {
            Long userId = getCurrentUserId();
            if (userId != null) {
                return userService.hasPermission(userId, permission);
            }
        } catch (Exception e) {
            logger.error("检查用户权限失败: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 检查当前用户是否为超级管理员
     * @return 是否为超级管理员
     */
    public static boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN") || hasRole("ADMIN") || hasRole("超级管理员");
    }

    /**
     * 检查当前用户是否有档案访问权限（根据密级）
     * @param securityLevel 档案密级
     * @return 是否有访问权限
     */
    public static boolean hasArchivePermission(String securityLevel) {
        if (!isAuthenticated()) {
            return false;
        }
        
        // 管理员有所有权限
        if (hasRole("ADMIN")) {
            return true;
        }
        
        // 根据密级检查权限
        switch (securityLevel) {
            case "PUBLIC":
                return true; // 公开档案所有人都可以访问
            case "INTERNAL":
                return hasPermission("VIEW_INTERNAL_ARCHIVE");
            case "CONFIDENTIAL":
                return hasPermission("VIEW_CONFIDENTIAL_ARCHIVE");
            case "SECRET":
                return hasPermission("VIEW_SECRET_ARCHIVE");
            case "TOP_SECRET":
                return hasPermission("VIEW_TOP_SECRET_ARCHIVE");
            default:
                return false;
        }
    }

    /**
     * 获取当前请求的IP地址
     * @return IP地址
     */
    public static String getCurrentUserIP() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return getClientIpAddress(request);
            }
        } catch (Exception e) {
            logger.error("获取用户IP地址失败: {}", e.getMessage(), e);
        }
        return "unknown";
    }

    /**
     * 获取当前请求的User-Agent
     * @return User-Agent
     */
    public static String getCurrentUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            logger.error("获取用户代理失败: {}", e.getMessage(), e);
        }
        return "unknown";
    }

    /**
     * 从请求中获取客户端IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    private static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        if (xForwardedProto != null && !xForwardedProto.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedProto)) {
            return xForwardedProto;
        }

        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }

        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 清除当前安全上下文
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}