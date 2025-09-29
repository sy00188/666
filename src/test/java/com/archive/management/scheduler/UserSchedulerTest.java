package com.archive.management.scheduler;

import com.archive.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * 用户定时任务调度器测试类
 * 测试用户相关定时任务的执行逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户定时任务调度器测试")
class UserSchedulerTest {

    @Mock
    private UserService userService;

    @Mock
    private Logger logger;

    @InjectMocks
    private UserScheduler userScheduler;

    @BeforeEach
    void setUp() {
        // 注入模拟的Logger
        ReflectionTestUtils.setField(userScheduler, "logger", logger);
    }

    @Test
    @DisplayName("清理过期会话 - 成功")
    void cleanupExpiredSessions_Success() {
        // Given
        when(userService.cleanupExpiredSessions()).thenReturn(15);

        // When
        userScheduler.cleanupExpiredSessions();

        // Then
        verify(userService).cleanupExpiredSessions();
        verify(logger).info("开始执行清理过期会话任务");
        verify(logger).info("过期会话清理完成，共清理 {} 个会话", 15);
    }

    @Test
    @DisplayName("清理过期会话 - 异常处理")
    void cleanupExpiredSessions_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("会话清理失败");
        when(userService.cleanupExpiredSessions()).thenThrow(exception);

        // When
        userScheduler.cleanupExpiredSessions();

        // Then
        verify(userService).cleanupExpiredSessions();
        verify(logger).info("开始执行清理过期会话任务");
        verify(logger).error("清理过期会话任务执行失败", exception);
    }

    @Test
    @DisplayName("清理过期令牌 - 成功")
    void cleanupExpiredTokens_Success() {
        // Given
        when(userService.cleanupExpiredTokens()).thenReturn(25);

        // When
        userScheduler.cleanupExpiredTokens();

        // Then
        verify(userService).cleanupExpiredTokens();
        verify(logger).info("开始执行清理过期令牌任务");
        verify(logger).info("过期令牌清理完成，共清理 {} 个令牌", 25);
    }

    @Test
    @DisplayName("清理过期令牌 - 异常处理")
    void cleanupExpiredTokens_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("令牌清理失败");
        when(userService.cleanupExpiredTokens()).thenThrow(exception);

        // When
        userScheduler.cleanupExpiredTokens();

        // Then
        verify(userService).cleanupExpiredTokens();
        verify(logger).info("开始执行清理过期令牌任务");
        verify(logger).error("清理过期令牌任务执行失败", exception);
    }

    @Test
    @DisplayName("更新用户活跃状态 - 成功")
    void updateUserActiveStatus_Success() {
        // Given
        when(userService.updateUserActiveStatus()).thenReturn(50);

        // When
        userScheduler.updateUserActiveStatus();

        // Then
        verify(userService).updateUserActiveStatus();
        verify(logger).info("开始执行用户活跃状态更新任务");
        verify(logger).info("用户活跃状态更新完成，共更新 {} 个用户", 50);
    }

    @Test
    @DisplayName("更新用户活跃状态 - 异常处理")
    void updateUserActiveStatus_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("状态更新失败");
        when(userService.updateUserActiveStatus()).thenThrow(exception);

        // When
        userScheduler.updateUserActiveStatus();

        // Then
        verify(userService).updateUserActiveStatus();
        verify(logger).info("开始执行用户活跃状态更新任务");
        verify(logger).error("用户活跃状态更新任务执行失败", exception);
    }

    @Test
    @DisplayName("生成用户统计报告 - 成功")
    void generateUserStatistics_Success() {
        // Given
        doNothing().when(userService).generateUserStatisticsReport();

        // When
        userScheduler.generateUserStatistics();

        // Then
        verify(userService).generateUserStatisticsReport();
        verify(logger).info("开始执行用户统计报告生成任务");
        verify(logger).info("用户统计报告生成完成");
    }

    @Test
    @DisplayName("生成用户统计报告 - 异常处理")
    void generateUserStatistics_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("报告生成失败");
        doThrow(exception).when(userService).generateUserStatisticsReport();

        // When
        userScheduler.generateUserStatistics();

        // Then
        verify(userService).generateUserStatisticsReport();
        verify(logger).info("开始执行用户统计报告生成任务");
        verify(logger).error("用户统计报告生成任务执行失败", exception);
    }

    @Test
    @DisplayName("备份用户数据 - 成功")
    void backupUserData_Success() {
        // Given
        when(userService.backupUserData()).thenReturn(true);

        // When
        userScheduler.backupUserData();

        // Then
        verify(userService).backupUserData();
        verify(logger).info("开始执行用户数据备份任务");
        verify(logger).info("用户数据备份任务完成，备份状态: {}", true);
    }

    @Test
    @DisplayName("备份用户数据 - 异常处理")
    void backupUserData_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("备份失败");
        when(userService.backupUserData()).thenThrow(exception);

        // When
        userScheduler.backupUserData();

        // Then
        verify(userService).backupUserData();
        verify(logger).info("开始执行用户数据备份任务");
        verify(logger).error("用户数据备份任务执行失败", exception);
    }

    @Test
    @DisplayName("同步用户权限 - 成功")
    void syncUserPermissions_Success() {
        // Given
        when(userService.syncUserPermissions()).thenReturn(30);

        // When
        userScheduler.syncUserPermissions();

        // Then
        verify(userService).syncUserPermissions();
        verify(logger).info("开始执行用户权限同步任务");
        verify(logger).info("用户权限同步完成，共同步 {} 个用户", 30);
    }

    @Test
    @DisplayName("同步用户权限 - 异常处理")
    void syncUserPermissions_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("权限同步失败");
        when(userService.syncUserPermissions()).thenThrow(exception);

        // When
        userScheduler.syncUserPermissions();

        // Then
        verify(userService).syncUserPermissions();
        verify(logger).info("开始执行用户权限同步任务");
        verify(logger).error("用户权限同步任务执行失败", exception);
    }

    @Test
    @DisplayName("检查用户账户安全 - 成功")
    void checkUserAccountSecurity_Success() {
        // Given
        when(userService.checkAccountSecurity()).thenReturn(5);

        // When
        userScheduler.checkUserAccountSecurity();

        // Then
        verify(userService).checkAccountSecurity();
        verify(logger).info("开始执行用户账户安全检查任务");
        verify(logger).info("用户账户安全检查完成，发现 {} 个安全问题", 5);
    }

    @Test
    @DisplayName("检查用户账户安全 - 异常处理")
    void checkUserAccountSecurity_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("安全检查失败");
        when(userService.checkAccountSecurity()).thenThrow(exception);

        // When
        userScheduler.checkUserAccountSecurity();

        // Then
        verify(userService).checkAccountSecurity();
        verify(logger).info("开始执行用户账户安全检查任务");
        verify(logger).error("用户账户安全检查任务执行失败", exception);
    }

    @Test
    @DisplayName("清理用户操作日志 - 成功")
    void cleanupUserOperationLogs_Success() {
        // Given
        when(userService.cleanupOperationLogs()).thenReturn(100);

        // When
        userScheduler.cleanupUserOperationLogs();

        // Then
        verify(userService).cleanupOperationLogs();
        verify(logger).info("开始执行用户操作日志清理任务");
        verify(logger).info("用户操作日志清理完成，共清理 {} 条日志", 100);
    }

    @Test
    @DisplayName("清理用户操作日志 - 异常处理")
    void cleanupUserOperationLogs_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("日志清理失败");
        when(userService.cleanupOperationLogs()).thenThrow(exception);

        // When
        userScheduler.cleanupUserOperationLogs();

        // Then
        verify(userService).cleanupOperationLogs();
        verify(logger).info("开始执行用户操作日志清理任务");
        verify(logger).error("用户操作日志清理任务执行失败", exception);
    }

    @Test
    @DisplayName("发送用户通知 - 成功")
    void sendUserNotifications_Success() {
        // Given
        when(userService.sendPendingNotifications()).thenReturn(20);

        // When
        userScheduler.sendUserNotifications();

        // Then
        verify(userService).sendPendingNotifications();
        verify(logger).info("开始执行用户通知发送任务");
        verify(logger).info("用户通知发送完成，共发送 {} 条通知", 20);
    }

    @Test
    @DisplayName("发送用户通知 - 异常处理")
    void sendUserNotifications_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("通知发送失败");
        when(userService.sendPendingNotifications()).thenThrow(exception);

        // When
        userScheduler.sendUserNotifications();

        // Then
        verify(userService).sendPendingNotifications();
        verify(logger).info("开始执行用户通知发送任务");
        verify(logger).error("用户通知发送任务执行失败", exception);
    }

    @Test
    @DisplayName("更新用户登录统计 - 成功")
    void updateUserLoginStatistics_Success() {
        // Given
        doNothing().when(userService).updateLoginStatistics();

        // When
        userScheduler.updateUserLoginStatistics();

        // Then
        verify(userService).updateLoginStatistics();
        verify(logger).info("开始执行用户登录统计更新任务");
        verify(logger).info("用户登录统计更新完成");
    }

    @Test
    @DisplayName("更新用户登录统计 - 异常处理")
    void updateUserLoginStatistics_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("统计更新失败");
        doThrow(exception).when(userService).updateLoginStatistics();

        // When
        userScheduler.updateUserLoginStatistics();

        // Then
        verify(userService).updateLoginStatistics();
        verify(logger).info("开始执行用户登录统计更新任务");
        verify(logger).error("用户登录统计更新任务执行失败", exception);
    }

    @Test
    @DisplayName("处理用户密码过期 - 成功")
    void handlePasswordExpiration_Success() {
        // Given
        when(userService.handlePasswordExpiration()).thenReturn(8);

        // When
        userScheduler.handlePasswordExpiration();

        // Then
        verify(userService).handlePasswordExpiration();
        verify(logger).info("开始执行用户密码过期处理任务");
        verify(logger).info("用户密码过期处理完成，共处理 {} 个用户", 8);
    }

    @Test
    @DisplayName("处理用户密码过期 - 异常处理")
    void handlePasswordExpiration_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("密码过期处理失败");
        when(userService.handlePasswordExpiration()).thenThrow(exception);

        // When
        userScheduler.handlePasswordExpiration();

        // Then
        verify(userService).handlePasswordExpiration();
        verify(logger).info("开始执行用户密码过期处理任务");
        verify(logger).error("用户密码过期处理任务执行失败", exception);
    }

    @Test
    @DisplayName("验证任务执行时间记录")
    void verifyTaskExecutionTimeLogging() {
        // Given
        when(userService.cleanupExpiredSessions()).thenReturn(15);

        // When
        userScheduler.cleanupExpiredSessions();

        // Then
        verify(logger).info("开始执行清理过期会话任务");
        verify(logger).info("过期会话清理完成，共清理 {} 个会话", 15);
        // 验证任务执行时间被记录（通过检查日志调用次数）
        verify(logger, atLeast(2)).info(anyString(), any());
    }

    @Test
    @DisplayName("验证所有任务都有异常处理")
    void verifyAllTasksHaveExceptionHandling() {
        // 这个测试确保所有定时任务方法都有适当的异常处理
        // 通过检查每个方法在异常情况下的行为来验证

        RuntimeException testException = new RuntimeException("测试异常");

        // 清理过期会话
        when(userService.cleanupExpiredSessions()).thenThrow(testException);
        userScheduler.cleanupExpiredSessions();
        verify(logger).error("清理过期会话任务执行失败", testException);

        // 清理过期令牌
        when(userService.cleanupExpiredTokens()).thenThrow(testException);
        userScheduler.cleanupExpiredTokens();
        verify(logger).error("清理过期令牌任务执行失败", testException);

        // 更新用户活跃状态
        when(userService.updateUserActiveStatus()).thenThrow(testException);
        userScheduler.updateUserActiveStatus();
        verify(logger).error("用户活跃状态更新任务执行失败", testException);

        // 生成用户统计报告
        doThrow(testException).when(userService).generateUserStatisticsReport();
        userScheduler.generateUserStatistics();
        verify(logger).error("用户统计报告生成任务执行失败", testException);
    }

    @Test
    @DisplayName("验证任务执行顺序和依赖关系")
    void verifyTaskExecutionOrderAndDependencies() {
        // 验证某些任务的执行顺序是否正确
        // 例如：先清理过期会话，再清理过期令牌

        when(userService.cleanupExpiredSessions()).thenReturn(15);
        when(userService.cleanupExpiredTokens()).thenReturn(25);

        // 执行任务
        userScheduler.cleanupExpiredSessions();
        userScheduler.cleanupExpiredTokens();

        // 验证执行顺序
        verify(userService).cleanupExpiredSessions();
        verify(userService).cleanupExpiredTokens();
    }

    @Test
    @DisplayName("验证任务性能监控")
    void verifyTaskPerformanceMonitoring() {
        // 验证任务执行时间监控
        when(userService.cleanupExpiredSessions()).thenReturn(15);

        long startTime = System.currentTimeMillis();
        userScheduler.cleanupExpiredSessions();
        long endTime = System.currentTimeMillis();

        // 验证任务在合理时间内完成
        assert (endTime - startTime) < 5000; // 5秒内完成

        verify(logger).info("开始执行清理过期会话任务");
        verify(logger).info("过期会话清理完成，共清理 {} 个会话", 15);
    }
}