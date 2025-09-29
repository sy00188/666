package com.archive.management.service.impl;

import com.archive.management.dto.response.CaptchaResponse;
import com.archive.management.service.CaptchaService;
import com.archive.management.util.CaptchaGenerator;
import com.archive.management.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * 基于Redis实现验证码的生成、存储和验证
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    // Redis键前缀
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    // 验证码过期时间（秒）
    private static final long CAPTCHA_EXPIRE_TIME = 300; // 5分钟
    
    // 验证码长度
    private static final int CAPTCHA_LENGTH = 4;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public CaptchaResponse generateCaptcha() {
        try {
            // 生成验证码ID
            String captchaId = IdUtil.generateCaptchaId();
            
            // 生成验证码图片和文本
            CaptchaGenerator.CaptchaResult captchaResult = CaptchaGenerator.generateCaptcha();
            
            // 存储验证码到Redis（不区分大小写）
            String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
            redisTemplate.opsForValue().set(redisKey, captchaResult.getCode().toUpperCase(), CAPTCHA_EXPIRE_TIME, TimeUnit.SECONDS);
            
            logger.info("生成验证码成功，captchaId: {}", captchaId);
            
            return new CaptchaResponse(captchaId, captchaResult.getBase64Image(), CAPTCHA_EXPIRE_TIME);
            
        } catch (Exception e) {
            logger.error("生成验证码失败", e);
            throw new RuntimeException("生成验证码失败", e);
        }
    }

    @Override
    public boolean validateCaptcha(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            logger.warn("验证码ID或验证码为空");
            return false;
        }

        try {
            String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
            String storedCode = (String) redisTemplate.opsForValue().get(redisKey);
            
            if (storedCode == null) {
                logger.warn("验证码不存在或已过期，captchaId: {}", captchaId);
                return false;
            }
            
            // 不区分大小写比较
            boolean isValid = storedCode.equalsIgnoreCase(captchaCode.trim());
            
            if (isValid) {
                // 验证成功后立即删除验证码（一次性使用）
                redisTemplate.delete(redisKey);
                logger.info("验证码验证成功，captchaId: {}", captchaId);
            } else {
                logger.warn("验证码验证失败，captchaId: {}, 输入: {}, 期望: {}", captchaId, captchaCode, storedCode);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("验证验证码时发生异常，captchaId: {}", captchaId, e);
            return false;
        }
    }

    @Override
    public void removeCaptcha(String captchaId) {
        if (!StringUtils.hasText(captchaId)) {
            return;
        }

        try {
            String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
            redisTemplate.delete(redisKey);
            logger.debug("删除验证码，captchaId: {}", captchaId);
        } catch (Exception e) {
            logger.error("删除验证码失败，captchaId: {}", captchaId, e);
        }
    }

    @Override
    public boolean existsCaptcha(String captchaId) {
        if (!StringUtils.hasText(captchaId)) {
            return false;
        }

        try {
            String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            logger.error("检查验证码存在性失败，captchaId: {}", captchaId, e);
            return false;
        }
    }

    @Override
    public void cleanExpiredCaptcha() {
        // Redis会自动清理过期的键，这里可以实现额外的清理逻辑
        logger.debug("清理过期验证码（Redis自动处理）");
    }

    /**
     * 获取验证码剩余过期时间
     * 
     * @param captchaId 验证码ID
     * @return 剩余时间（秒），-1表示不存在或已过期
     */
    public long getCaptchaExpireTime(String captchaId) {
        if (!StringUtils.hasText(captchaId)) {
            return -1;
        }

        try {
            String redisKey = CAPTCHA_KEY_PREFIX + captchaId;
            return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("获取验证码过期时间失败，captchaId: {}", captchaId, e);
            return -1;
        }
    }
}