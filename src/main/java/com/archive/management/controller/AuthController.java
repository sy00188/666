package com.archive.management.controller;

import com.archive.management.dto.LoginRequest;
import com.archive.management.dto.LoginResponse;
import com.archive.management.entity.User;
import com.archive.management.security.CustomUserPrincipal;
import com.archive.management.service.UserService;
import com.archive.management.service.CaptchaService;
import com.archive.management.security.JwtTokenUtil;
import com.archive.management.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出、令牌刷新等认证相关操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户通过用户名和密码进行登录认证，支持验证码验证")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "登录请求") @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        try {
            logger.info("用户登录尝试: {}, IP: {}", loginRequest.getUsername(), clientIp);
            
            // 1. 验证码验证
            if (!captchaService.validateCaptcha(loginRequest.getCaptchaId(), loginRequest.getCaptcha())) {
                logger.warn("验证码验证失败: 用户={}, 验证码ID={}", loginRequest.getUsername(), loginRequest.getCaptchaId());
                response.put("code", 400);
                response.put("message", "验证码错误或已过期");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2. 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 获取认证后的用户信息
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            User user = userService.findByUsername(loginRequest.getUsername());
            
            if (user == null) {
                throw new BadCredentialsException("用户不存在");
            }

            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new DisabledException("用户已被禁用");
            }

            // 生成JWT令牌
            String accessToken = jwtTokenUtil.generateToken(userPrincipal);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userPrincipal);
            
            // 获取JWT配置信息
            Map<String, Object> jwtConfig = jwtTokenUtil.getJwtConfig();
            Long expiresIn = (Long) jwtConfig.get("expiration");

            // 更新用户登录信息
            userService.updateLastLoginTime(user.getId(), clientIp);

            // 记录登录日志
            LogUtil.logLogin(user.getId(), user.getUsername(), clientIp, "登录成功");

            // 构建响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(expiresIn);
        
        // 设置用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userPrincipal.getId());
        userInfo.put("username", userPrincipal.getUsername());
        userInfo.put("email", userPrincipal.getEmail());
        userInfo.put("realName", userPrincipal.getRealName());
        userInfo.put("phone", userPrincipal.getPhone());
        userInfo.put("departmentId", userPrincipal.getDepartmentId());
        userInfo.put("roleId", userPrincipal.getRoleId());
        userInfo.put("roleName", userPrincipal.getRoleName());
        loginResponse.setUserInfo(userInfo);

        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("data", loginResponse);


            
            logger.info("用户登录成功: {}, IP: {}", loginRequest.getUsername(), clientIp);
            
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("用户登录失败 - 凭据错误: {}, IP: {}", loginRequest.getUsername(), clientIp);
            LogUtil.login(loginRequest.getUsername(), clientIp, false, "用户名或密码错误");
            
            response.put("code", 401);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(response);
            
        } catch (DisabledException e) {
            logger.warn("用户登录失败 - 账户被禁用: {}, IP: {}", loginRequest.getUsername(), clientIp);
            LogUtil.login(loginRequest.getUsername(), clientIp, false, "账户已被禁用");
            
            response.put("code", 403);
            response.put("message", "账户已被禁用");
            return ResponseEntity.status(403).body(response);
            
        } catch (Exception e) {
            logger.error("用户登录异常: {}, IP: {}, 错误: {}", loginRequest.getUsername(), clientIp, e.getMessage(), e);
            LogUtil.login(loginRequest.getUsername(), clientIp, false, "系统异常: " + e.getMessage());
            
            response.put("code", 500);
            response.put("message", "登录失败，请稍后重试");
            return ResponseEntity.status(500).body(response);
        } finally {
            // 无论登录成功与否，都清除验证码
            if (loginRequest.getCaptchaId() != null) {
                captchaService.removeCaptcha(loginRequest.getCaptchaId());
            }
        }
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户登出系统")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIpAddress(request);
        
        try {
            // 从请求头中获取令牌
            String authHeader = request.getHeader(JwtTokenUtil.HEADER_STRING);
            if (authHeader != null && authHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
                String token = jwtTokenUtil.extractTokenFromHeader(authHeader);
                String username = jwtTokenUtil.getUsernameFromToken(token);
                
                logger.info("用户登出: {}, IP: {}", username, clientIp);
                LogUtil.login(username, clientIp, true, "用户登出");
            }
            
            response.put("code", 200);
            response.put("message", "登出成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("用户登出异常, IP: {}, 错误: {}", clientIp, e.getMessage(), e);
            
            response.put("code", 500);
            response.put("message", "登出失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Parameter(description = "刷新令牌") @RequestParam String refreshToken,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIpAddress(request);
        
        try {
            // 验证刷新令牌
            if (!jwtTokenUtil.validateToken(refreshToken) || !jwtTokenUtil.isRefreshToken(refreshToken)) {
                response.put("code", 401);
                response.put("message", "无效的刷新令牌");
                return ResponseEntity.status(401).body(response);
            }
            
            // 从令牌中获取用户信息
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
            
            User user = userService.findByUsername(username);
            if (user == null || !user.getId().equals(userId)) {
                response.put("code", 401);
                response.put("message", "用户信息不匹配");
                return ResponseEntity.status(401).body(response);
            }
            
            // 生成新的访问令牌
            CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);
            String newAccessToken = jwtTokenUtil.generateToken(
                userPrincipal, 
                user.getId(), 
                user.getRole().getName(),
                user.getDepartmentId()
            );
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", newAccessToken);
            tokenData.put("tokenType", "Bearer");
            tokenData.put("expiresIn", jwtTokenUtil.getJwtConfig().get("expiration"));
            
            response.put("code", 200);
            response.put("message", "令牌刷新成功");
            response.put("data", tokenData);
            
            logger.info("令牌刷新成功: {}, IP: {}", username, clientIp);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("令牌刷新异常, IP: {}, 错误: {}", clientIp, e.getMessage(), e);
            
            response.put("code", 500);
            response.put("message", "令牌刷新失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供有效的访问令牌"));
            }
            
            token = token.substring(7); // 移除 "Bearer " 前缀
            
            // 验证token并获取用户信息
            if (!jwtTokenUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "访问令牌已过期或无效"));
            }
            
            String username = jwtTokenUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "用户不存在"));
            }
            
            // 检查用户状态
            if (!"ACTIVE".equals(user.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "用户账户已被禁用"));
            }
            
            Map<String, Object> userInfo = convertToUserInfo(user);
            
            // 记录操作日志
            logger.info("用户 {} 获取个人信息成功，IP: {}", username, getClientIpAddress(request));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取用户信息成功",
                "data", userInfo
            ));
            
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户权限信息
     */
    @GetMapping("/permissions")
    @Operation(summary = "获取当前用户权限信息", description = "获取当前登录用户的权限列表")
    public ResponseEntity<Map<String, Object>> getCurrentUserPermissions(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "未提供有效的访问令牌"));
            }
            
            token = token.substring(7); // 移除 "Bearer " 前缀
            
            // 验证token并获取用户信息
            if (!jwtTokenUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "访问令牌已过期或无效"));
            }
            
            String username = jwtTokenUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "用户不存在"));
            }
            
            // 获取用户角色和权限
            Map<String, Object> permissionInfo = new HashMap<>();
            permissionInfo.put("userId", user.getId());
            permissionInfo.put("username", user.getUsername());
            permissionInfo.put("roles", userService.getUserRoles(user.getId()));
            permissionInfo.put("permissions", userService.getUserPermissions(user.getId()));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取权限信息成功",
                "data", permissionInfo
            ));
            
        } catch (Exception e) {
            logger.error("获取用户权限信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "获取权限信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 转换用户信息
     */
    private Map<String, Object> convertToUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("realName", user.getRealName());
        userInfo.put("department", user.getDepartment());
        userInfo.put("position", user.getPosition());
        userInfo.put("status", user.getStatus());
        userInfo.put("createTime", user.getCreateTime());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        return userInfo;
    }
}