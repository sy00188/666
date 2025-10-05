package com.archive.management.service;

import com.archive.management.dto.*;
import com.archive.management.entity.User;

/**
 * 微信登录服务接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public interface WeChatService {
    
    /**
     * 生成微信登录二维码
     * 
     * @return 微信登录响应（包含二维码URL或模拟登录信息）
     */
    WeChatLoginResponse generateLoginQRCode();
    
    /**
     * 处理微信回调
     * 
     * @param request 微信回调请求
     * @return 微信用户信息
     */
    WeChatUserInfo handleCallback(WeChatCallbackRequest request);
    
    /**
     * 处理模拟登录
     * 
     * @param request 微信登录请求
     * @return 微信用户信息
     */
    WeChatUserInfo handleMockLogin(WeChatLoginRequest request);
    
    /**
     * 检查登录状态
     * 
     * @param state 登录状态标识
     * @return 登录状态信息（status: pending/success/expired/error）
     */
    java.util.Map<String, Object> checkLoginStatus(String state);
    
    /**
     * 通过OpenID查找用户
     * 
     * @param openid 微信OpenID
     * @return 用户对象，未找到返回null
     */
    User findUserByOpenid(String openid);
    
    /**
     * 绑定微信账号到现有用户
     * 
     * @param userId 用户ID
     * @param weChatUserInfo 微信用户信息
     * @return 是否绑定成功
     */
    boolean bindWeChatToUser(Long userId, WeChatUserInfo weChatUserInfo);
    
    /**
     * 创建新用户并绑定微信
     * 
     * @param bindRequest 绑定请求
     * @param weChatUserInfo 微信用户信息
     * @return 新创建的用户
     */
    User createUserWithWeChat(WeChatBindRequest bindRequest, WeChatUserInfo weChatUserInfo);
    
    /**
     * 解绑微信账号
     * 
     * @param userId 用户ID
     * @return 是否解绑成功
     */
    boolean unbindWeChat(Long userId);
    
    /**
     * 保存登录状态
     * 
     * @param state 状态标识
     * @param weChatUserInfo 微信用户信息
     * @param userId 用户ID（可选）
     */
    void saveLoginState(String state, WeChatUserInfo weChatUserInfo, Long userId);
    
    /**
     * 获取登录状态信息
     * 
     * @param state 状态标识
     * @return 登录状态信息
     */
    java.util.Map<String, Object> getLoginState(String state);
    
    /**
     * 记录OAuth登录日志
     * 
     * @param userId 用户ID
     * @param weChatUserInfo 微信用户信息
     * @param ipAddress IP地址
     * @param status 登录状态
     * @param errorMessage 错误信息
     */
    void logOAuthLogin(Long userId, WeChatUserInfo weChatUserInfo, String ipAddress, String status, String errorMessage);
}

