package com.archive.management.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT令牌工具类
 * 提供JWT令牌的生成、解析、验证等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    /**
     * JWT密钥
     */
    @Value("${jwt.secret:archiveManagementSystemSecretKey2024ForJWTTokenGeneration}")
    private String secret;

    /**
     * JWT过期时间（秒）
     */
    @Value("${jwt.expiration:86400}")
    private Long expiration;

    /**
     * 刷新令牌过期时间（秒）
     */
    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    /**
     * JWT令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT令牌头名称
     */
    public static final String HEADER_STRING = "Authorization";

    /**
     * 获取密钥
     * @return 密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期时间
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取签发时间
     * @param token JWT令牌
     * @return 签发时间
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取用户角色
     * @param token JWT令牌
     * @return 用户角色
     */
    @SuppressWarnings("unchecked")
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 从令牌中获取部门ID
     * @param token JWT令牌
     * @return 部门ID
     */
    public Long getDepartmentIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("departmentId", Long.class);
    }

    /**
     * 从令牌中获取指定声明
     * @param token JWT令牌
     * @param claimsResolver 声明解析器
     * @param <T> 返回类型
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT令牌已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT令牌: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("JWT令牌格式错误: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("JWT令牌签名验证失败: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT令牌参数非法: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 检查令牌是否过期
     * @param token JWT令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 检查令牌是否在最后密码重置之前创建
     * @param token JWT令牌
     * @param lastPasswordReset 最后密码重置时间
     * @return 是否在密码重置之前创建
     */
    public Boolean isCreatedBeforeLastPasswordReset(String token, Date lastPasswordReset) {
        try {
            final Date created = getIssuedAtDateFromToken(token);
            return (lastPasswordReset != null && created.before(lastPasswordReset));
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 生成访问令牌
     * @param userDetails 用户详情
     * @param userId 用户ID
     * @param role 用户角色
     * @param departmentId 部门ID
     * @return JWT令牌
     */
    public String generateToken(UserDetails userDetails, Long userId, String role, Long departmentId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("departmentId", departmentId);
        claims.put("type", "access");
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 生成刷新令牌
     * @param userDetails 用户详情
     * @param userId 用户ID
     * @return 刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * 创建令牌
     * @param claims 声明
     * @param subject 主题（用户名）
     * @param expiration 过期时间（秒）
     * @return JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 验证令牌
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证令牌（不需要用户详情）
     * @param token JWT令牌
     * @return 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新令牌
     * @param token 原令牌
     * @param userDetails 用户详情
     * @param userId 用户ID
     * @param role 用户角色
     * @param departmentId 部门ID
     * @return 新令牌
     */
    public String refreshToken(String token, UserDetails userDetails, Long userId, String role, Long departmentId) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + expiration * 1000));
            
            // 更新用户信息
            claims.put("userId", userId);
            claims.put("role", role);
            claims.put("departmentId", departmentId);
            
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("令牌刷新失败: {}", e.getMessage());
            return generateToken(userDetails, userId, role, departmentId);
        }
    }

    /**
     * 从请求头中提取令牌
     * @param authHeader 授权头
     * @return JWT令牌
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 检查令牌类型
     * @param token JWT令牌
     * @return 令牌类型
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否为访问令牌
     * @param token JWT令牌
     * @return 是否为访问令牌
     */
    public Boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    /**
     * 检查是否为刷新令牌
     * @param token JWT令牌
     * @return 是否为刷新令牌
     */
    public Boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    /**
     * 获取令牌剩余有效时间（秒）
     * @param token JWT令牌
     * @return 剩余有效时间
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining / 1000);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 检查令牌是否即将过期（30分钟内）
     * @param token JWT令牌
     * @return 是否即将过期
     */
    public Boolean isTokenExpiringSoon(String token) {
        try {
            Long remainingTime = getTokenRemainingTime(token);
            return remainingTime <= 1800; // 30分钟
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 生成令牌指纹（用于令牌黑名单）
     * @param token JWT令牌
     * @return 令牌指纹
     */
    public String generateTokenFingerprint(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String data = claims.getSubject() + claims.getIssuedAt().getTime();
            return Integer.toHexString(data.hashCode());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析令牌获取用户信息
     * @param token JWT令牌
     * @return 用户信息Map
     */
    public Map<String, Object> parseTokenToUserInfo(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", claims.getSubject());
            userInfo.put("userId", claims.get("userId", Long.class));
            userInfo.put("role", claims.get("role", String.class));
            userInfo.put("departmentId", claims.get("departmentId", Long.class));
            userInfo.put("issuedAt", claims.getIssuedAt());
            userInfo.put("expiration", claims.getExpiration());
            return userInfo;
        } catch (Exception e) {
            logger.error("解析令牌失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取JWT配置信息
     * @return 配置信息
     */
    public Map<String, Object> getJwtConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("expiration", expiration);
        config.put("refreshExpiration", refreshExpiration);
        config.put("tokenPrefix", TOKEN_PREFIX);
        config.put("headerString", HEADER_STRING);
        return config;
    }
}