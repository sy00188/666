package com.archive.management.service;

import com.archive.management.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SMS服务单元测试类
 * 测试短信发送、验证码、限流等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SMS服务测试")
class SmsServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SmsServiceImpl smsService;

    @BeforeEach
    void setUp() {
        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 设置SMS配置属性
        ReflectionTestUtils.setField(smsService, "smsProvider", "aliyun");
        ReflectionTestUtils.setField(smsService, "accessKeyId", "test_access_key");
        ReflectionTestUtils.setField(smsService, "accessKeySecret", "test_access_secret");
        ReflectionTestUtils.setField(smsService, "signName", "档案管理系统");
        ReflectionTestUtils.setField(smsService, "rateLimitPerMinute", 10);
        ReflectionTestUtils.setField(smsService, "rateLimitPerHour", 100);
        ReflectionTestUtils.setField(smsService, "rateLimitPerDay", 1000);
    }

    @Test
    @DisplayName("发送普通短信 - 成功")
    void sendSms_Success() {
        // Given
        String phoneNumber = "13800138000";
        String content = "您的验证码是123456，5分钟内有效。";

        when(valueOperations.get(anyString())).thenReturn(null); // 无限流记录

        // When
        boolean result = smsService.sendSms(phoneNumber, content);

        // Then
        assertTrue(result);
        verify(valueOperations, times(3)).increment(anyString()); // 分钟、小时、天限流计数
        verify(redisTemplate, times(3)).expire(anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("发送普通短信 - 手机号格式错误")
    void sendSms_InvalidPhoneNumber() {
        // Given
        String invalidPhoneNumber = "123456";
        String content = "测试内容";

        // When
        boolean result = smsService.sendSms(invalidPhoneNumber, content);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("发送普通短信 - 触发限流")
    void sendSms_RateLimited() {
        // Given
        String phoneNumber = "13800138000";
        String content = "测试内容";

        when(valueOperations.get(contains("minute"))).thenReturn(10L); // 已达分钟限流

        // When
        boolean result = smsService.sendSms(phoneNumber, content);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("发送模板短信 - 成功")
    void sendTemplateSms_Success() {
        // Given
        String phoneNumber = "13800138000";
        String templateCode = "SMS_001";
        Map<String, String> templateParams = Map.of("code", "123456", "product", "档案管理系统");

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean result = smsService.sendTemplateSms(phoneNumber, templateCode, templateParams);

        // Then
        assertTrue(result);
        verify(valueOperations, times(3)).increment(anyString());
    }

    @Test
    @DisplayName("批量发送短信 - 成功")
    void sendBatchSms_Success() {
        // Given
        List<String> phoneNumbers = Arrays.asList("13800138000", "13800138001", "13800138002");
        String content = "批量短信内容";

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        int result = smsService.sendBatchSms(phoneNumbers, content);

        // Then
        assertEquals(3, result);
        verify(valueOperations, times(9)).increment(anyString()); // 3个号码 * 3种限流
    }

    @Test
    @DisplayName("批量发送短信 - 部分成功")
    void sendBatchSms_PartialSuccess() {
        // Given
        List<String> phoneNumbers = Arrays.asList("13800138000", "invalid", "13800138002");
        String content = "批量短信内容";

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        int result = smsService.sendBatchSms(phoneNumbers, content);

        // Then
        assertEquals(2, result); // 只有2个有效号码
        verify(valueOperations, times(6)).increment(anyString()); // 2个有效号码 * 3种限流
    }

    @Test
    @DisplayName("发送验证码短信 - 成功")
    void sendVerificationCode_Success() {
        // Given
        String phoneNumber = "13800138000";
        String code = "123456";

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean result = smsService.sendVerificationCode(phoneNumber, code);

        // Then
        assertTrue(result);
        verify(valueOperations, times(1)).set(contains("verification"), eq(code), eq(300L), eq(TimeUnit.SECONDS));
        verify(valueOperations, times(3)).increment(anyString());
    }

    @Test
    @DisplayName("发送通知短信 - 成功")
    void sendNotificationSms_Success() {
        // Given
        String phoneNumber = "13800138000";
        String title = "档案借阅通知";
        String content = "您有新的档案借阅申请待处理";

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean result = smsService.sendNotificationSms(phoneNumber, title, content);

        // Then
        assertTrue(result);
        verify(valueOperations, times(3)).increment(anyString());
    }

    @Test
    @DisplayName("检查短信状态 - 成功")
    void checkSmsStatus_Success() {
        // Given
        String messageId = "SMS_123456789";

        // When
        Map<String, Object> result = smsService.checkSmsStatus(messageId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
    }

    @Test
    @DisplayName("获取短信统计信息 - 成功")
    void getSmsStatistics_Success() {
        // Given
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";

        // When
        Map<String, Object> result = smsService.getSmsStatistics(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("totalSent"));
    }

    @Test
    @DisplayName("验证手机号格式 - 有效号码")
    void validatePhoneNumber_Valid() {
        // Given
        String validPhoneNumber = "13800138000";

        // When
        boolean result = smsService.validatePhoneNumber(validPhoneNumber);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证手机号格式 - 无效号码")
    void validatePhoneNumber_Invalid() {
        // Given
        String[] invalidPhoneNumbers = {"123456", "1380013800a", "12345678901", ""};

        // When & Then
        for (String phoneNumber : invalidPhoneNumbers) {
            boolean result = smsService.validatePhoneNumber(phoneNumber);
            assertFalse(result, "手机号 " + phoneNumber + " 应该被判定为无效");
        }
    }

    @Test
    @DisplayName("检查限流状态 - 未限流")
    void checkRateLimit_NotLimited() {
        // Given
        String phoneNumber = "13800138000";
        String smsType = "GENERAL";

        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // When
        boolean result = smsService.checkRateLimit(phoneNumber, smsType);

        // Then
        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey(contains("sms:rate_limit:"));
    }

    @Test
    @DisplayName("检查限流状态 - 分钟限流")
    void checkRateLimit_MinuteLimited() {
        // Given
        String phoneNumber = "13800138000";
        String smsType = "GENERAL";

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        boolean result = smsService.checkRateLimit(phoneNumber, smsType);

        // Then
        assertFalse(result);
        verify(redisTemplate, times(1)).hasKey(contains("sms:rate_limit:"));
    }

    @Test
    @DisplayName("检查限流状态 - 小时限流")
    void checkRateLimit_HourLimited() {
        // Given
        String phoneNumber = "13800138000";
        String smsType = "GENERAL";

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        boolean result = smsService.checkRateLimit(phoneNumber, smsType);

        // Then
        assertFalse(result);
        verify(redisTemplate, times(1)).hasKey(contains("sms:rate_limit:"));
    }

    @Test
    @DisplayName("检查限流状态 - 天限流")
    void checkRateLimit_DayLimited() {
        // Given
        String phoneNumber = "13800138000";
        String smsType = "GENERAL";

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When
        boolean result = smsService.checkRateLimit(phoneNumber, smsType);

        // Then
        assertFalse(result);
        verify(redisTemplate, times(1)).hasKey(contains("sms:rate_limit:"));
    }

    @Test
    @DisplayName("获取短信余额 - 成功")
    void getSmsBalance_Success() {
        // When
        Map<String, Object> result = smsService.getSmsBalance();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("balance"));
    }

    @Test
    @DisplayName("发送短信 - 空内容")
    void sendSms_EmptyContent() {
        // Given
        String phoneNumber = "13800138000";
        String emptyContent = "";

        // When
        boolean result = smsService.sendSms(phoneNumber, emptyContent);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("发送短信 - 空手机号")
    void sendSms_EmptyPhoneNumber() {
        // Given
        String emptyPhoneNumber = "";
        String content = "测试内容";

        // When
        boolean result = smsService.sendSms(emptyPhoneNumber, content);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("发送验证码 - 验证码为空")
    void sendVerificationCode_EmptyCode() {
        // Given
        String phoneNumber = "13800138000";
        String emptyCode = "";

        // When
        boolean result = smsService.sendVerificationCode(phoneNumber, emptyCode);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("发送验证码 - 手机号无效")
    void sendVerificationCode_InvalidPhoneNumber() {
        // Given
        String invalidPhoneNumber = "123456";
        String code = "123456";

        // When
        boolean result = smsService.sendVerificationCode(invalidPhoneNumber, code);

        // Then
        assertFalse(result);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }
}