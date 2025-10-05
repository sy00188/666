package com.archive.management.service.impl;

import com.archive.management.config.WeChatConfig;
import com.archive.management.dto.*;
import com.archive.management.entity.User;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.WeChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 微信登录服务实现类
 * 支持真实微信OAuth和模拟模式
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Service
public class WeChatServiceImpl implements WeChatService {
    
    @Autowired
    private WeChatConfig weChatConfig;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String REDIS_KEY_PREFIX = "wechat:login:";
    private static final String REDIS_STATE_PREFIX = "wechat:state:";
    
    @Override
    public WeChatLoginResponse generateLoginQRCode() {
        if (!weChatConfig.getEnabled()) {
            throw new RuntimeException("微信登录功能未启用");
        }
        
        WeChatLoginResponse response = new WeChatLoginResponse();
        
        // 生成唯一的state标识
        String state = UUID.randomUUID().toString().replace("-", "");
        response.setState(state);
        response.setMockMode(weChatConfig.getMockMode());
        
        // 设置过期时间
        Long expiresIn = weChatConfig.getQrcodeExpireMinutes() * 60L;
        response.setExpiresIn(expiresIn);
        
        if (weChatConfig.getMockMode()) {
            // 模拟模式
            response.setMessage("模拟模式：请输入微信ID进行登录测试");
            log.info("生成微信登录state（模拟模式）: {}", state);
        } else {
            // 真实模式：构建微信二维码URL
            String qrcodeUrl = buildWeChatQRCodeUrl(state);
            response.setQrcodeUrl(qrcodeUrl);
            response.setMessage("请使用微信扫描二维码登录");
            log.info("生成微信登录二维码: state={}, url={}", state, qrcodeUrl);
        }
        
        // 在Redis中保存state（用于后续验证）
        String redisKey = REDIS_STATE_PREFIX + state;
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("state", state);
        stateData.put("status", "pending");
        stateData.put("createTime", LocalDateTime.now().toString());
        redisTemplate.opsForValue().set(redisKey, stateData, expiresIn, TimeUnit.SECONDS);
        
        // 在数据库中记录
        saveLoginStateToDb(state, "pending", expiresIn);
        
        return response;
    }
    
    @Override
    public WeChatUserInfo handleCallback(WeChatCallbackRequest request) {
        log.info("处理微信回调: state={}, code={}", request.getState(), request.getCode());
        
        // 验证state
        if (!validateState(request.getState())) {
            throw new RuntimeException("无效的登录状态或已过期");
        }
        
        // 检查是否有错误
        if (request.getError() != null) {
            log.error("微信授权失败: error={}, description={}", request.getError(), request.getErrorDescription());
            throw new RuntimeException("微信授权失败: " + request.getErrorDescription());
        }
        
        try {
            // 1. 使用code换取access_token
            String accessTokenUrl = String.format(
                "%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                WeChatConfig.WeChatApi.ACCESS_TOKEN_URL,
                weChatConfig.getAppId(),
                weChatConfig.getAppSecret(),
                request.getCode()
            );
            
            String accessTokenResponse = restTemplate.getForObject(accessTokenUrl, String.class);
            JsonNode accessTokenNode = objectMapper.readTree(accessTokenResponse);
            
            if (accessTokenNode.has("errcode")) {
                String errMsg = accessTokenNode.get("errmsg").asText();
                log.error("获取access_token失败: {}", errMsg);
                throw new RuntimeException("获取微信access_token失败: " + errMsg);
            }
            
            String accessToken = accessTokenNode.get("access_token").asText();
            String openid = accessTokenNode.get("openid").asText();
            String refreshToken = accessTokenNode.has("refresh_token") ? accessTokenNode.get("refresh_token").asText() : null;
            
            // 2. 使用access_token获取用户信息
            String userInfoUrl = String.format(
                "%s?access_token=%s&openid=%s",
                WeChatConfig.WeChatApi.USER_INFO_URL,
                accessToken,
                openid
            );
            
            String userInfoResponse = restTemplate.getForObject(userInfoUrl, String.class);
            WeChatUserInfo userInfo = objectMapper.readValue(userInfoResponse, WeChatUserInfo.class);
            
            log.info("获取微信用户信息成功: openid={}, nickname={}", userInfo.getOpenid(), userInfo.getNickname());
            
            // 3. 更新登录状态
            saveLoginState(request.getState(), userInfo, null);
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("处理微信回调异常", e);
            throw new RuntimeException("处理微信回调失败: " + e.getMessage());
        }
    }
    
    @Override
    public WeChatUserInfo handleMockLogin(WeChatLoginRequest request) {
        log.info("处理模拟微信登录: state={}, mockWechatId={}", request.getState(), request.getMockWechatId());
        
        // 验证state
        if (!validateState(request.getState())) {
            throw new RuntimeException("无效的登录状态或已过期");
        }
        
        // 创建模拟的微信用户信息
        WeChatUserInfo userInfo = new WeChatUserInfo();
        userInfo.setOpenid("mock_" + request.getMockWechatId());
        userInfo.setNickname(request.getMockNickname() != null ? request.getMockNickname() : "模拟用户_" + request.getMockWechatId());
        userInfo.setHeadimgurl("https://via.placeholder.com/150");
        userInfo.setSex(1);
        userInfo.setCountry("中国");
        userInfo.setProvince("陕西");
        userInfo.setCity("咸阳");
        
        // 更新登录状态
        saveLoginState(request.getState(), userInfo, null);
        
        log.info("模拟微信登录成功: openid={}", userInfo.getOpenid());
        
        return userInfo;
    }
    
    @Override
    public Map<String, Object> checkLoginStatus(String state) {
        Map<String, Object> result = new HashMap<>();
        
        // 先从Redis获取
        String redisKey = REDIS_STATE_PREFIX + state;
        @SuppressWarnings("unchecked")
        Map<String, Object> stateData = (Map<String, Object>) redisTemplate.opsForValue().get(redisKey);
        
        if (stateData == null) {
            // Redis中没有，从数据库获取
            stateData = getLoginStateFromDb(state);
        }
        
        if (stateData == null) {
            result.put("status", "expired");
            result.put("message", "登录状态已过期");
            return result;
        }
        
        String status = (String) stateData.get("status");
        result.put("status", status);
        
        if ("success".equals(status)) {
            result.put("message", "登录成功");
            result.put("openid", stateData.get("openid"));
            result.put("nickname", stateData.get("nickname"));
            result.put("avatar", stateData.get("avatar"));
            result.put("userId", stateData.get("userId"));
        } else if ("pending".equals(status)) {
            result.put("message", "等待扫码");
        } else if ("binding_required".equals(status)) {
            result.put("message", "需要绑定账号");
            result.put("openid", stateData.get("openid"));
            result.put("nickname", stateData.get("nickname"));
            result.put("avatar", stateData.get("avatar"));
        } else {
            result.put("message", "登录失败");
        }
        
        return result;
    }
    
    @Override
    public User findUserByOpenid(String openid) {
        return userRepository.findByWechatOpenid(openid).orElse(null);
    }
    
    @Override
    @Transactional
    public boolean bindWeChatToUser(Long userId, WeChatUserInfo weChatUserInfo) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查该OpenID是否已被其他用户绑定
            User existingUser = findUserByOpenid(weChatUserInfo.getOpenid());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException("该微信账号已被其他用户绑定");
            }
            
            // 绑定微信信息
            user.setWechatOpenid(weChatUserInfo.getOpenid());
            user.setWechatUnionid(weChatUserInfo.getUnionid());
            user.setWechatNickname(weChatUserInfo.getNickname());
            user.setWechatAvatar(weChatUserInfo.getHeadimgurl());
            user.setWechatBindingTime(LocalDateTime.now());
            
            userRepository.save(user);
            
            log.info("微信账号绑定成功: userId={}, openid={}", userId, weChatUserInfo.getOpenid());
            return true;
            
        } catch (Exception e) {
            log.error("绑定微信账号失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public User createUserWithWeChat(WeChatBindRequest bindRequest, WeChatUserInfo weChatUserInfo) {
        try {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(bindRequest.getNewUsername()).isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 创建新用户
            User user = new User();
            user.setUsername(bindRequest.getNewUsername());
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 随机密码
            user.setEmail(bindRequest.getEmail());
            user.setPhone(bindRequest.getPhone());
            user.setRealName(bindRequest.getRealName() != null ? bindRequest.getRealName() : weChatUserInfo.getNickname());
            user.setStatus(1); // 启用状态
            user.setCreateTime(LocalDateTime.now());
            
            // 绑定微信信息
            user.setWechatOpenid(weChatUserInfo.getOpenid());
            user.setWechatUnionid(weChatUserInfo.getUnionid());
            user.setWechatNickname(weChatUserInfo.getNickname());
            user.setWechatAvatar(weChatUserInfo.getHeadimgurl());
            user.setWechatBindingTime(LocalDateTime.now());
            
            // 分配默认角色（普通用户）
            user.setRoleId(2L);
            
            userRepository.save(user);
            
            log.info("创建新用户并绑定微信成功: username={}, openid={}", user.getUsername(), weChatUserInfo.getOpenid());
            return user;
            
        } catch (Exception e) {
            log.error("创建用户并绑定微信失败", e);
            throw new RuntimeException("创建用户失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean unbindWeChat(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setWechatOpenid(null);
            user.setWechatUnionid(null);
            user.setWechatNickname(null);
            user.setWechatAvatar(null);
            user.setWechatBindingTime(null);
            
            userRepository.save(user);
            
            log.info("解绑微信账号成功: userId={}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("解绑微信账号失败", e);
            return false;
        }
    }
    
    @Override
    public void saveLoginState(String state, WeChatUserInfo weChatUserInfo, Long userId) {
        // 保存到Redis
        String redisKey = REDIS_STATE_PREFIX + state;
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("state", state);
        stateData.put("status", userId != null ? "success" : "binding_required");
        stateData.put("openid", weChatUserInfo.getOpenid());
        stateData.put("unionid", weChatUserInfo.getUnionid());
        stateData.put("nickname", weChatUserInfo.getNickname());
        stateData.put("avatar", weChatUserInfo.getHeadimgurl());
        stateData.put("userId", userId);
        stateData.put("updateTime", LocalDateTime.now().toString());
        
        Long expiresIn = weChatConfig.getStateExpireMinutes() * 60L;
        redisTemplate.opsForValue().set(redisKey, stateData, expiresIn, TimeUnit.SECONDS);
        
        // 更新数据库
        updateLoginStateInDb(state, weChatUserInfo, userId);
    }
    
    @Override
    public Map<String, Object> getLoginState(String state) {
        return checkLoginStatus(state);
    }
    
    @Override
    public void logOAuthLogin(Long userId, WeChatUserInfo weChatUserInfo, String ipAddress, String status, String errorMessage) {
        try {
            String sql = "INSERT INTO oauth_login_log (user_id, provider, openid, unionid, login_time, ip_address, status, error_message) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql,
                userId,
                "wechat",
                weChatUserInfo != null ? weChatUserInfo.getOpenid() : null,
                weChatUserInfo != null ? weChatUserInfo.getUnionid() : null,
                LocalDateTime.now(),
                ipAddress,
                status,
                errorMessage
            );
            
            log.debug("OAuth登录日志记录成功: userId={}, status={}", userId, status);
            
        } catch (Exception e) {
            log.error("记录OAuth登录日志失败", e);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 构建微信二维码URL
     */
    private String buildWeChatQRCodeUrl(String state) {
        try {
            String encodedRedirectUri = URLEncoder.encode(weChatConfig.getRedirectUri(), "UTF-8");
            return String.format(
                "%s?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s#wechat_redirect",
                WeChatConfig.WeChatApi.QRCONNECT_URL,
                weChatConfig.getAppId(),
                encodedRedirectUri,
                weChatConfig.getScope(),
                state
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("构建微信二维码URL失败", e);
        }
    }
    
    /**
     * 验证state是否有效
     */
    private boolean validateState(String state) {
        if (state == null || state.isEmpty()) {
            return false;
        }
        
        String redisKey = REDIS_STATE_PREFIX + state;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }
    
    /**
     * 保存登录状态到数据库
     */
    private void saveLoginStateToDb(String state, String status, Long expiresIn) {
        try {
            LocalDateTime expiresTime = LocalDateTime.now().plusSeconds(expiresIn);
            String sql = "INSERT INTO wechat_login_state (state, status, expires_time, create_time) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, state, status, expiresTime, LocalDateTime.now());
        } catch (Exception e) {
            log.error("保存登录状态到数据库失败", e);
        }
    }
    
    /**
     * 从数据库获取登录状态
     */
    private Map<String, Object> getLoginStateFromDb(String state) {
        try {
            String sql = "SELECT * FROM wechat_login_state WHERE state = ? AND expires_time > NOW()";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, state);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("从数据库获取登录状态失败", e);
            return null;
        }
    }
    
    /**
     * 更新数据库中的登录状态
     */
    private void updateLoginStateInDb(String state, WeChatUserInfo weChatUserInfo, Long userId) {
        try {
            String status = userId != null ? "success" : "binding_required";
            String sql = "UPDATE wechat_login_state SET status = ?, openid = ?, unionid = ?, nickname = ?, avatar = ?, user_id = ?, update_time = ? WHERE state = ?";
            jdbcTemplate.update(sql,
                status,
                weChatUserInfo.getOpenid(),
                weChatUserInfo.getUnionid(),
                weChatUserInfo.getNickname(),
                weChatUserInfo.getHeadimgurl(),
                userId,
                LocalDateTime.now(),
                state
            );
        } catch (Exception e) {
            log.error("更新数据库登录状态失败", e);
        }
    }
}

