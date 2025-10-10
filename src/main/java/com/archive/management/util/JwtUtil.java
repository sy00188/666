package com.archive.management.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT令牌
 * 
 * 注意：这是一个简化版本的JWT工具类，用于快速编译通过
 * 在生产环境中，建议使用成熟的JWT库（如jjwt）
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:archive-management-secret-key-2024}")
    private String secret;

    @Value("${jwt.expiration:86400}") // 默认24小时
    private Long expiration;

    /**
     * 生成Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("username", username);
            claims.put("exp", Instant.now().getEpochSecond() + expiration);
            
            // 简化的Token生成（仅用于编译通过）
            String payload = Base64.getEncoder().encodeToString(
                claims.toString().getBytes(StandardCharsets.UTF_8)
            );
            
            String signature = generateSignature(payload);
            return payload + "." + signature;
            
        } catch (Exception e) {
            throw new RuntimeException("生成Token失败", e);
        }
    }

    /**
     * 验证Token
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return false;
            }

            String payload = parts[0];
            String signature = parts[1];

            // 验证签名
            String expectedSignature = generateSignature(payload);
            if (!signature.equals(expectedSignature)) {
                return false;
            }

            // 验证过期时间
            String decodedPayload = new String(
                Base64.getDecoder().decode(payload),
                StandardCharsets.UTF_8
            );
            
            // 简化的过期验证（实际应解析JSON）
            return !isExpired(decodedPayload);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 1) {
                return null;
            }

            String payload = new String(
                Base64.getDecoder().decode(parts[0]),
                StandardCharsets.UTF_8
            );

            // 简化的解析（实际应使用JSON解析）
            int userIdIndex = payload.indexOf("userId=");
            if (userIdIndex > 0) {
                String userIdStr = payload.substring(userIdIndex + 7);
                int endIndex = userIdStr.indexOf(",");
                if (endIndex > 0) {
                    userIdStr = userIdStr.substring(0, endIndex);
                }
                return Long.parseLong(userIdStr.trim());
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 1) {
                return null;
            }

            String payload = new String(
                Base64.getDecoder().decode(parts[0]),
                StandardCharsets.UTF_8
            );

            // 简化的解析
            int usernameIndex = payload.indexOf("username=");
            if (usernameIndex > 0) {
                String usernameStr = payload.substring(usernameIndex + 9);
                int endIndex = usernameStr.indexOf(",");
                if (endIndex > 0) {
                    usernameStr = usernameStr.substring(0, endIndex);
                }
                return usernameStr.trim();
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 刷新Token
     * 
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("Token无效，无法刷新");
        }

        Long userId = getUserIdFromToken(token);
        String username = getUsernameFromToken(token);

        if (userId == null || username == null) {
            throw new RuntimeException("无法从Token中提取用户信息");
        }

        return generateToken(userId, username);
    }

    /**
     * 生成签名
     * 
     * @param data 数据
     * @return 签名
     */
    private String generateSignature(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((data + secret).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 检查是否过期
     * 
     * @param payload 载荷
     * @return 是否过期
     */
    private boolean isExpired(String payload) {
        try {
            int expIndex = payload.indexOf("exp=");
            if (expIndex > 0) {
                String expStr = payload.substring(expIndex + 4);
                int endIndex = expStr.indexOf(",");
                if (endIndex < 0) {
                    endIndex = expStr.indexOf("}");
                }
                if (endIndex > 0) {
                    expStr = expStr.substring(0, endIndex);
                }
                long exp = Long.parseLong(expStr.trim());
                return Instant.now().getEpochSecond() > exp;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}

