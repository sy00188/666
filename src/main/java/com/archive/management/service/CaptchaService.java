package com.archive.management.service;

import com.archive.management.dto.response.CaptchaResponse;

/**
 * 验证码服务接口
 * 提供验证码生成、验证和管理功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public interface CaptchaService {

    /**
     * 生成验证码
     * 
     * @return CaptchaResponse 包含验证码ID、图片和过期时间
     */
    CaptchaResponse generateCaptcha();

    /**
     * 验证验证码
     * 
     * @param captchaId 验证码ID
     * @param captchaCode 用户输入的验证码
     * @return true 验证成功，false 验证失败
     */
    boolean validateCaptcha(String captchaId, String captchaCode);

    /**
     * 删除验证码（验证后清理）
     * 
     * @param captchaId 验证码ID
     */
    void removeCaptcha(String captchaId);

    /**
     * 检查验证码是否存在
     * 
     * @param captchaId 验证码ID
     * @return true 存在，false 不存在或已过期
     */
    boolean existsCaptcha(String captchaId);

    /**
     * 清理过期的验证码（可选，Redis会自动过期）
     */
    void cleanExpiredCaptcha();
}