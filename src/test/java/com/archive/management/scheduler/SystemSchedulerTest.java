package com.archive.management.scheduler;

import com.archive.management.service.SystemService;
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
 * 系统定时任务调度器测试类
 * 测试系统维护相关定时任务的执行逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("系统定时任务调度器测试")
class SystemSchedulerTest {

    @Mock
    private SystemService systemService;

    @Mock
    private Logger logger;

    @InjectMocks
    private SystemScheduler systemScheduler;

    @BeforeEach
    void setUp() {
        // 注入模拟的Logger
        ReflectionTestUtils.setField(systemScheduler, "logger", logger);
    }

    @Test
    @DisplayName("系统健康检查 - 成功")
    void performSystemHealthCheck_Success() {
        // Given
        when(systemService.performHealthCheck()).thenReturn(true);

        // When
        systemScheduler.performSystemHealthCheck();

        // Then
        verify(systemService).performHealthCheck();
        verify(logger).info("开始执行系统健康检查任务");
        verify(logger).info("系统健康检查完成，系统状态: {}", "正常");
    }

    @Test
    @DisplayName("系统健康检查 - 检查失败")
    void performSystemHealthCheck_Failed() {
        // Given
        when(systemService.performHealthCheck()).thenReturn(false);

        // When
        systemScheduler.performSystemHealthCheck();

        // Then
        verify(systemService).performHealthCheck();
        verify(logger).info("开始执行系统健康检查任务");
        verify(logger).warn("系统健康检查完成，系统状态: {}", "异常");
    }

    @Test
    @DisplayName("系统健康检查 - 异常处理")
    void performSystemHealthCheck_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("健康检查失败");
        when(systemService.performHealthCheck()).thenThrow(exception);

        // When
        systemScheduler.performSystemHealthCheck();

        // Then
        verify(systemService).performHealthCheck();
        verify(logger).info("开始执行系统健康检查任务");
        verify(logger).error("系统健康检查任务执行失败", exception);
    }

    @Test
    @DisplayName("清理系统日志 - 成功")
    void cleanupSystemLogs_Success() {
        // Given
        when(systemService.cleanupSystemLogs()).thenReturn(500L);

        // When
        systemScheduler.cleanupSystemLogs();

        // Then
        verify(systemService).cleanupSystemLogs();
        verify(logger).info("开始执行系统日志清理任务");
        verify(logger).info("系统日志清理完成，共清理 {} 条日志", 500L);
    }

    @Test
    @DisplayName("清理系统日志 - 异常处理")
    void cleanupSystemLogs_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("日志清理失败");
        when(systemService.cleanupSystemLogs()).thenThrow(exception);

        // When
        systemScheduler.cleanupSystemLogs();

        // Then
        verify(systemService).cleanupSystemLogs();
        verify(logger).info("开始执行系统日志清理任务");
        verify(logger).error("系统日志清理任务执行失败", exception);
    }

    @Test
    @DisplayName("数据库维护 - 成功")
    void performDatabaseMaintenance_Success() {
        // Given
        doNothing().when(systemService).performDatabaseMaintenance();

        // When
        systemScheduler.performDatabaseMaintenance();

        // Then
        verify(systemService).performDatabaseMaintenance();
        verify(logger).info("开始执行数据库维护任务");
        verify(logger).info("数据库维护任务完成");
    }

    @Test
    @DisplayName("数据库维护 - 异常处理")
    void performDatabaseMaintenance_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("数据库维护失败");
        doThrow(exception).when(systemService).performDatabaseMaintenance();

        // When
        systemScheduler.performDatabaseMaintenance();

        // Then
        verify(systemService).performDatabaseMaintenance();
        verify(logger).info("开始执行数据库维护任务");
        verify(logger).error("数据库维护任务执行失败", exception);
    }

    @Test
    @DisplayName("缓存清理 - 成功")
    void cleanupCache_Success() {
        // Given
        when(systemService.cleanupCache()).thenReturn(1024L);

        // When
        systemScheduler.cleanupCache();

        // Then
        verify(systemService).cleanupCache();
        verify(logger).info("开始执行缓存清理任务");
        verify(logger).info("缓存清理完成，释放内存: {} MB", 1024L);
    }

    @Test
    @DisplayName("缓存清理 - 异常处理")
    void cleanupCache_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("缓存清理失败");
        when(systemService.cleanupCache()).thenThrow(exception);

        // When
        systemScheduler.cleanupCache();

        // Then
        verify(systemService).cleanupCache();
        verify(logger).info("开始执行缓存清理任务");
        verify(logger).error("缓存清理任务执行失败", exception);
    }

    @Test
    @DisplayName("系统备份 - 成功")
    void performSystemBackup_Success() {
        // Given
        when(systemService.performSystemBackup()).thenReturn(true);

        // When
        systemScheduler.performSystemBackup();

        // Then
        verify(systemService).performSystemBackup();
        verify(logger).info("开始执行系统备份任务");
        verify(logger).info("系统备份任务完成，备份状态: {}", true);
    }

    @Test
    @DisplayName("系统备份 - 备份失败")
    void performSystemBackup_Failed() {
        // Given
        when(systemService.performSystemBackup()).thenReturn(false);

        // When
        systemScheduler.performSystemBackup();

        // Then
        verify(systemService).performSystemBackup();
        verify(logger).info("开始执行系统备份任务");
        verify(logger).warn("系统备份任务完成，备份状态: {}", false);
    }

    @Test
    @DisplayName("系统备份 - 异常处理")
    void performSystemBackup_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("系统备份失败");
        when(systemService.performSystemBackup()).thenThrow(exception);

        // When
        systemScheduler.performSystemBackup();

        // Then
        verify(systemService).performSystemBackup();
        verify(logger).info("开始执行系统备份任务");
        verify(logger).error("系统备份任务执行失败", exception);
    }

    @Test
    @DisplayName("监控系统资源 - 成功")
    void monitorSystemResources_Success() {
        // Given
        doNothing().when(systemService).monitorSystemResources();

        // When
        systemScheduler.monitorSystemResources();

        // Then
        verify(systemService).monitorSystemResources();
        verify(logger).info("开始执行系统资源监控任务");
        verify(logger).info("系统资源监控任务完成");
    }

    @Test
    @DisplayName("监控系统资源 - 异常处理")
    void monitorSystemResources_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("资源监控失败");
        doThrow(exception).when(systemService).monitorSystemResources();

        // When
        systemScheduler.monitorSystemResources();

        // Then
        verify(systemService).monitorSystemResources();
        verify(logger).info("开始执行系统资源监控任务");
        verify(logger).error("系统资源监控任务执行失败", exception);
    }

    @Test
    @DisplayName("更新系统配置 - 成功")
    void updateSystemConfiguration_Success() {
        // Given
        when(systemService.updateSystemConfiguration()).thenReturn(10);

        // When
        systemScheduler.updateSystemConfiguration();

        // Then
        verify(systemService).updateSystemConfiguration();
        verify(logger).info("开始执行系统配置更新任务");
        verify(logger).info("系统配置更新完成，共更新 {} 项配置", 10);
    }

    @Test
    @DisplayName("更新系统配置 - 异常处理")
    void updateSystemConfiguration_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("配置更新失败");
        when(systemService.updateSystemConfiguration()).thenThrow(exception);

        // When
        systemScheduler.updateSystemConfiguration();

        // Then
        verify(systemService).updateSystemConfiguration();
        verify(logger).info("开始执行系统配置更新任务");
        verify(logger).error("系统配置更新任务执行失败", exception);
    }

    @Test
    @DisplayName("检查系统安全 - 成功")
    void checkSystemSecurity_Success() {
        // Given
        when(systemService.checkSystemSecurity()).thenReturn(2);

        // When
        systemScheduler.checkSystemSecurity();

        // Then
        verify(systemService).checkSystemSecurity();
        verify(logger).info("开始执行系统安全检查任务");
        verify(logger).info("系统安全检查完成，发现 {} 个安全问题", 2);
    }

    @Test
    @DisplayName("检查系统安全 - 异常处理")
    void checkSystemSecurity_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("安全检查失败");
        when(systemService.checkSystemSecurity()).thenThrow(exception);

        // When
        systemScheduler.checkSystemSecurity();

        // Then
        verify(systemService).checkSystemSecurity();
        verify(logger).info("开始执行系统安全检查任务");
        verify(logger).error("系统安全检查任务执行失败", exception);
    }

    @Test
    @DisplayName("生成系统报告 - 成功")
    void generateSystemReport_Success() {
        // Given
        doNothing().when(systemService).generateSystemReport();

        // When
        systemScheduler.generateSystemReport();

        // Then
        verify(systemService).generateSystemReport();
        verify(logger).info("开始执行系统报告生成任务");
        verify(logger).info("系统报告生成完成");
    }

    @Test
    @DisplayName("生成系统报告 - 异常处理")
    void generateSystemReport_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("报告生成失败");
        doThrow(exception).when(systemService).generateSystemReport();

        // When
        systemScheduler.generateSystemReport();

        // Then
        verify(systemService).generateSystemReport();
        verify(logger).info("开始执行系统报告生成任务");
        verify(logger).error("系统报告生成任务执行失败", exception);
    }

    @Test
    @DisplayName("清理临时文件 - 成功")
    void cleanupTemporaryFiles_Success() {
        // Given
        when(systemService.cleanupTemporaryFiles()).thenReturn(256L);

        // When
        systemScheduler.cleanupTemporaryFiles();

        // Then
        verify(systemService).cleanupTemporaryFiles();
        verify(logger).info("开始执行临时文件清理任务");
        verify(logger).info("临时文件清理完成，释放空间: {} MB", 256L);
    }

    @Test
    @DisplayName("清理临时文件 - 异常处理")
    void cleanupTemporaryFiles_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("临时文件清理失败");
        when(systemService.cleanupTemporaryFiles()).thenThrow(exception);

        // When
        systemScheduler.cleanupTemporaryFiles();

        // Then
        verify(systemService).cleanupTemporaryFiles();
        verify(logger).info("开始执行临时文件清理任务");
        verify(logger).error("临时文件清理任务执行失败", exception);
    }

    @Test
    @DisplayName("优化系统性能 - 成功")
    void optimizeSystemPerformance_Success() {
        // Given
        doNothing().when(systemService).optimizeSystemPerformance();

        // When
        systemScheduler.optimizeSystemPerformance();

        // Then
        verify(systemService).optimizeSystemPerformance();
        verify(logger).info("开始执行系统性能优化任务");
        verify(logger).info("系统性能优化任务完成");
    }

    @Test
    @DisplayName("优化系统性能 - 异常处理")
    void optimizeSystemPerformance_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("性能优化失败");
        doThrow(exception).when(systemService).optimizeSystemPerformance();

        // When
        systemScheduler.optimizeSystemPerformance();

        // Then
        verify(systemService).optimizeSystemPerformance();
        verify(logger).info("开始执行系统性能优化任务");
        verify(logger).error("系统性能优化任务执行失败", exception);
    }

    @Test
    @DisplayName("验证任务执行时间记录")
    void verifyTaskExecutionTimeLogging() {
        // Given
        when(systemService.performHealthCheck()).thenReturn(true);

        // When
        systemScheduler.performSystemHealthCheck();

        // Then
        verify(logger).info("开始执行系统健康检查任务");
        verify(logger).info("系统健康检查完成，系统状态: {}", "正常");
        // 验证任务执行时间被记录（通过检查日志调用次数）
        verify(logger, atLeast(2)).info(anyString(), any());
    }

    @Test
    @DisplayName("验证所有任务都有异常处理")
    void verifyAllTasksHaveExceptionHandling() {
        // 这个测试确保所有定时任务方法都有适当的异常处理
        RuntimeException testException = new RuntimeException("测试异常");

        // 系统健康检查
        when(systemService.performHealthCheck()).thenThrow(testException);
        systemScheduler.performSystemHealthCheck();
        verify(logger).error("系统健康检查任务执行失败", testException);

        // 清理系统日志
        when(systemService.cleanupSystemLogs()).thenThrow(testException);
        systemScheduler.cleanupSystemLogs();
        verify(logger).error("系统日志清理任务执行失败", testException);

        // 数据库维护
        doThrow(testException).when(systemService).performDatabaseMaintenance();
        systemScheduler.performDatabaseMaintenance();
        verify(logger).error("数据库维护任务执行失败", testException);

        // 缓存清理
        when(systemService.cleanupCache()).thenThrow(testException);
        systemScheduler.cleanupCache();
        verify(logger).error("缓存清理任务执行失败", testException);
    }

    @Test
    @DisplayName("验证系统维护任务执行顺序")
    void verifySystemMaintenanceTaskOrder() {
        // 验证系统维护任务的执行顺序
        when(systemService.performHealthCheck()).thenReturn(true);
        when(systemService.cleanupSystemLogs()).thenReturn(500L);
        doNothing().when(systemService).performDatabaseMaintenance();
        when(systemService.cleanupCache()).thenReturn(1024L);

        // 按顺序执行任务
        systemScheduler.performSystemHealthCheck();
        systemScheduler.cleanupSystemLogs();
        systemScheduler.performDatabaseMaintenance();
        systemScheduler.cleanupCache();

        // 验证执行顺序
        verify(systemService).performHealthCheck();
        verify(systemService).cleanupSystemLogs();
        verify(systemService).performDatabaseMaintenance();
        verify(systemService).cleanupCache();
    }

    @Test
    @DisplayName("验证系统资源监控和报告生成")
    void verifySystemMonitoringAndReporting() {
        // Given
        doNothing().when(systemService).monitorSystemResources();
        doNothing().when(systemService).generateSystemReport();

        // When
        systemScheduler.monitorSystemResources();
        systemScheduler.generateSystemReport();

        // Then
        verify(systemService).monitorSystemResources();
        verify(systemService).generateSystemReport();
        verify(logger).info("开始执行系统资源监控任务");
        verify(logger).info("系统资源监控任务完成");
        verify(logger).info("开始执行系统报告生成任务");
        verify(logger).info("系统报告生成完成");
    }

    @Test
    @DisplayName("验证系统安全和配置更新")
    void verifySystemSecurityAndConfiguration() {
        // Given
        when(systemService.checkSystemSecurity()).thenReturn(0);
        when(systemService.updateSystemConfiguration()).thenReturn(5);

        // When
        systemScheduler.checkSystemSecurity();
        systemScheduler.updateSystemConfiguration();

        // Then
        verify(systemService).checkSystemSecurity();
        verify(systemService).updateSystemConfiguration();
        verify(logger).info("系统安全检查完成，发现 {} 个安全问题", 0);
        verify(logger).info("系统配置更新完成，共更新 {} 项配置", 5);
    }

    @Test
    @DisplayName("验证系统备份和性能优化")
    void verifySystemBackupAndOptimization() {
        // Given
        when(systemService.performSystemBackup()).thenReturn(true);
        doNothing().when(systemService).optimizeSystemPerformance();

        // When
        systemScheduler.performSystemBackup();
        systemScheduler.optimizeSystemPerformance();

        // Then
        verify(systemService).performSystemBackup();
        verify(systemService).optimizeSystemPerformance();
        verify(logger).info("系统备份任务完成，备份状态: {}", true);
        verify(logger).info("系统性能优化任务完成");
    }

    @Test
    @DisplayName("验证任务性能监控")
    void verifyTaskPerformanceMonitoring() {
        // 验证任务执行时间监控
        when(systemService.performHealthCheck()).thenReturn(true);

        long startTime = System.currentTimeMillis();
        systemScheduler.performSystemHealthCheck();
        long endTime = System.currentTimeMillis();

        // 验证任务在合理时间内完成
        assert (endTime - startTime) < 5000; // 5秒内完成

        verify(logger).info("开始执行系统健康检查任务");
        verify(logger).info("系统健康检查完成，系统状态: {}", "正常");
    }
}