package com.archive.management.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 用于处理JWT令牌的验证和用户认证
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * 执行过滤逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求中提取JWT令牌
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // 验证令牌格式和有效性
                if (jwtTokenUtil.validateToken(jwt)) {
                    // 检查令牌类型，只处理访问令牌
                    if (jwtTokenUtil.isAccessToken(jwt)) {
                        // 从令牌中获取用户名
                        String username = jwtTokenUtil.getUsernameFromToken(jwt);
                        
                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            // 加载用户详情
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            // 验证令牌与用户详情的匹配性
                            if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                                // 创建认证对象
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                
                                // 设置认证详情
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                
                                // 将认证对象设置到安全上下文中
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                
                                logger.debug("用户 {} 认证成功", username);
                                
                                // 检查令牌是否即将过期，如果是则在响应头中添加提示
                                if (jwtTokenUtil.isTokenExpiringSoon(jwt)) {
                                    response.setHeader("X-Token-Expiring", "true");
                                    response.setHeader("X-Token-Remaining", 
                                        String.valueOf(jwtTokenUtil.getTokenRemainingTime(jwt)));
                                    logger.debug("令牌即将过期，用户: {}", username);
                                }
                            } else {
                                logger.warn("JWT令牌验证失败，用户: {}", username);
                            }
                        }
                    } else {
                        logger.warn("收到非访问令牌，令牌类型: {}", jwtTokenUtil.getTokenType(jwt));
                    }
                } else {
                    logger.warn("JWT令牌无效或已过期");
                }
            }
        } catch (Exception e) {
            logger.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            // 清除可能存在的认证信息
            SecurityContextHolder.clearContext();
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 从Authorization头中获取令牌
        String bearerToken = request.getHeader(JwtTokenUtil.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
            return jwtTokenUtil.extractTokenFromHeader(bearerToken);
        }
        
        // 从请求参数中获取令牌（用于某些特殊场景，如文件下载）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }

    /**
     * 判断是否应该跳过过滤
     * @param request HTTP请求
     * @return 是否跳过
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 跳过登录、注册等公开接口
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/webjars/") ||
               path.equals("/favicon.ico") ||
               path.equals("/") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/actuator/health");
    }
}