package com.archive.management.service;

import com.archive.management.entity.Notification;
import com.archive.management.mapper.NotificationMapper;
import com.archive.management.service.impl.NotificationServiceImpl;
import com.archive.management.util.EmailUtil;
import com.archive.management.websocket.NotificationWebSocketHandler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通知服务单元测试类
 * 测试通知管理业务逻辑的各种场景
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("通知服务测试")
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testNotification = new Notification();
        testNotification.setNotificationId(1L);
        testNotification.setUserId(100L);
        testNotification.setTitle("测试通知");
        testNotification.setContent("这是一条测试通知内容");
        testNotification.setNotificationType(1); // 系统通知
        testNotification.setStatus(0); // 未读
        testNotification.setCreateTime(LocalDateTime.now());

        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("发送通知 - 成功")
    void sendNotification_Success() {
        // Given
        Long userId = 100L;
        String title = "测试通知";
        String content = "测试内容";
        String type = "SYSTEM";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendNotification(userId, title, content, type);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, times(1)).sendNotificationToUser(eq(userId.toString()), any(Notification.class));
    }

    @Test
    @DisplayName("发送通知 - 数据库插入失败")
    void sendNotification_DatabaseInsertFailed() {
        // Given
        Long userId = 100L;
        String title = "测试通知";
        String content = "测试内容";
        String type = "SYSTEM";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(0);

        // When
        boolean result = notificationService.sendNotification(userId, title, content, type);

        // Then
        assertFalse(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, never()).sendNotificationToUser(anyString(), any(Notification.class));
    }

    @Test
    @DisplayName("批量发送通知 - 成功")
    void sendBatchNotification_Success() {
        // Given
        List<Long> userIds = Arrays.asList(100L, 101L, 102L);
        String title = "批量通知";
        String content = "批量通知内容";
        String type = "SYSTEM";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        int result = notificationService.sendBatchNotification(userIds, title, content, type);

        // Then
        assertEquals(3, result);
        verify(notificationMapper, times(3)).insert(any(Notification.class));
        verify(webSocketHandler, times(3)).sendNotificationToUser(anyString(), any(Notification.class));
    }

    @Test
    @DisplayName("广播通知 - 成功")
    void sendBroadcastNotification_Success() {
        // Given
        String title = "广播通知";
        String content = "广播通知内容";
        String type = "SYSTEM";
        String targetType = "ALL_USERS";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendBroadcastNotification(title, content, type, targetType);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
    }

    @Test
    @DisplayName("获取用户通知列表 - 成功")
    void getUserNotifications_Success() {
        // Given
        Long userId = 100L;
        int page = 0;
        int size = 10;
        
        Page<Notification> mockPage = new Page<>(page + 1, size);
        IPage<Notification> mockResult = mock(IPage.class);
        List<Notification> notifications = Arrays.asList(testNotification);
        
        when(mockResult.getRecords()).thenReturn(notifications);
        when(notificationMapper.selectUserNotifications(any(Page.class), eq(userId), 
                isNull(), isNull(), isNull(), isNull(), any(LocalDateTime.class))).thenReturn(mockResult);

        // When
        Map<String, Object> result = notificationService.getUserNotifications(userId, page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("notifications"));
        @SuppressWarnings("unchecked")
        List<Notification> resultNotifications = (List<Notification>) result.get("notifications");
        assertEquals(1, resultNotifications.size());
        assertEquals(testNotification.getNotificationId(), resultNotifications.get(0).getNotificationId());
    }

    @Test
    @DisplayName("标记通知为已读 - 成功")
    void markAsRead_Success() {
        // Given
        Long notificationId = 1L;
        Long userId = 100L;

        when(notificationMapper.batchMarkAsRead(eq(userId), eq(Arrays.asList(notificationId)), any(LocalDateTime.class))).thenReturn(1);

        // When
        boolean result = notificationService.markAsRead(notificationId, userId);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).batchMarkAsRead(eq(userId), eq(Arrays.asList(notificationId)), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("批量标记通知为已读 - 成功")
    void markBatchAsRead_Success() {
        // Given
        List<Long> notificationIds = Arrays.asList(1L, 2L, 3L);
        Long userId = 100L;

        when(notificationMapper.batchMarkAsRead(eq(userId), eq(notificationIds), any(LocalDateTime.class))).thenReturn(3);

        // When
        int result = notificationService.markBatchAsRead(notificationIds, userId);

        // Then
        assertEquals(3, result);
        verify(notificationMapper, times(1)).batchMarkAsRead(eq(userId), eq(notificationIds), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("删除通知 - 成功")
    void deleteNotification_Success() {
        // Given
        Long notificationId = 1L;
        Long userId = 100L;

        when(notificationMapper.deleteById(notificationId)).thenReturn(1);

        // When
        boolean result = notificationService.deleteNotification(notificationId, userId);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).deleteById(notificationId);
    }

    @Test
    @DisplayName("获取未读通知数量 - 成功")
    void getUnreadCount_Success() {
        // Given
        Long userId = 100L;
        Long expectedCount = 5L;

        when(notificationMapper.countUnreadNotifications(userId)).thenReturn(expectedCount);

        // When
        long result = notificationService.getUnreadCount(userId);

        // Then
        assertEquals(expectedCount.longValue(), result);
        verify(notificationMapper, times(1)).countUnreadNotifications(userId);
    }

    @Test
    @DisplayName("发送邮件通知 - 成功")
    void sendEmailNotification_Success() {
        // Given
        Long userId = 100L;
        String title = "测试邮件";
        String content = "邮件内容";
        String template = "default";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendEmailNotification(userId, title, content, template);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
    }

    @Test
    @DisplayName("发送短信通知 - 成功")
    void sendSmsNotification_Success() {
        // Given
        Long userId = 100L;
        String content = "短信内容";
        String template = "default";

        when(smsService.sendSms(anyString(), eq(content))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendSmsNotification(userId, content, template);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
    }

    @Test
    @DisplayName("发送短信通知 - 失败")
    void sendSmsNotification_Failed() {
        // Given
        Long userId = 100L;
        String content = "短信内容";
        String template = "default";

        when(smsService.sendSms(anyString(), eq(content))).thenReturn(false);

        // When
        boolean result = notificationService.sendSmsNotification(userId, content, template);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("获取通知统计信息 - 成功")
    void getNotificationStatistics_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Map<String, Object> expectedStats = new HashMap<>();
        expectedStats.put("totalCount", 10);
        expectedStats.put("unreadCount", 3);

        when(notificationMapper.countUnreadNotifications(any())).thenReturn(3L);

        // When
        Map<String, Object> result = notificationService.getNotificationStatistics(startDate, endDate);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("清理过期通知 - 成功")
    void cleanExpiredNotifications_Success() {
        // Given
        int days = 30;
        int deletedCount = 5;

        when(notificationMapper.deleteExpiredNotifications(any(LocalDateTime.class))).thenReturn(deletedCount);

        // When
        int result = notificationService.cleanExpiredNotifications(days);

        // Then
        assertEquals(deletedCount, result);
        verify(notificationMapper, times(1)).deleteExpiredNotifications(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("发送用户操作通知 - 成功")
    void sendUserOperationNotification_Success() {
        // Given
        Long userId = 100L;
        String operationType = "LOGIN";
        String operationContent = "用户登录成功";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendUserOperationNotification(userId, operationType, operationContent);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, times(1)).sendNotificationToUser(eq(userId.toString()), any(Notification.class));
    }

    @Test
    @DisplayName("发送档案通知 - 成功")
    void sendArchiveNotification_Success() {
        // Given
        Long userId = 100L;
        Long archiveId = 200L;
        String operationType = "CREATED";
        String operationContent = "档案创建成功";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendArchiveNotification(userId, archiveId, operationType, operationContent);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, times(1)).sendNotificationToUser(eq(userId.toString()), any(Notification.class));
    }

    @Test
    @DisplayName("发送借阅通知 - 成功")
    void sendBorrowNotification_Success() {
        // Given
        Long userId = 100L;
        Long borrowId = 300L;
        String notificationType = "APPROVED";
        String content = "借阅申请已批准";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendBorrowNotification(userId, borrowId, notificationType, content);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, times(1)).sendNotificationToUser(eq(userId.toString()), any(Notification.class));
    }

    @Test
    @DisplayName("发送审批通知 - 成功")
    void sendApprovalNotification_Success() {
        // Given
        Long userId = 100L;
        Long approvalId = 400L;
        String approvalType = "ARCHIVE_BORROW";
        String approvalStatus = "PENDING";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendApprovalNotification(userId, approvalId, approvalType, approvalStatus);

        // Then
        assertTrue(result);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, times(1)).sendNotificationToUser(eq(userId.toString()), any(Notification.class));
    }
}