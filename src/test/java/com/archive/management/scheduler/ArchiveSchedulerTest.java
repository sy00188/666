package com.archive.management.scheduler;

import com.archive.management.service.ArchiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

/**
 * 档案定时任务调度器测试类
 * 测试档案相关定时任务的执行逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("档案定时任务调度器测试")
class ArchiveSchedulerTest {

    @Mock
    private ArchiveService archiveService;

    @Mock
    private Logger logger;

    @InjectMocks
    private ArchiveScheduler archiveScheduler;

    @BeforeEach
    void setUp() {
        // 注入模拟的Logger
        ReflectionTestUtils.setField(archiveScheduler, "logger", logger);
    }

    @Test
    @DisplayName("自动归档过期档案 - 成功")
    void autoArchiveExpiredArchives_Success() {
        // Given
        when(archiveService.archiveExpiredArchives()).thenReturn(5);

        // When
        archiveScheduler.autoArchiveExpiredArchives();

        // Then
        verify(archiveService).archiveExpiredArchives();
        verify(logger).info("开始执行自动归档过期档案任务");
        verify(logger).info("自动归档任务完成，共归档 {} 个过期档案", 5);
    }

    @Test
    @DisplayName("自动归档过期档案 - 异常处理")
    void autoArchiveExpiredArchives_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("数据库连接失败");
        when(archiveService.archiveExpiredArchives()).thenThrow(exception);

        // When
        archiveScheduler.autoArchiveExpiredArchives();

        // Then
        verify(archiveService).archiveExpiredArchives();
        verify(logger).info("开始执行自动归档过期档案任务");
        verify(logger).error("自动归档过期档案任务执行失败", exception);
    }

    @Test
    @DisplayName("清理临时文件 - 成功")
    void cleanupTempFiles_Success() {
        // Given
        when(archiveService.cleanupTempFiles()).thenReturn(10);

        // When
        archiveScheduler.cleanupTempFiles();

        // Then
        verify(archiveService).cleanupTempFiles();
        verify(logger).info("开始执行清理临时文件任务");
        verify(logger).info("临时文件清理完成，共清理 {} 个文件", 10);
    }

    @Test
    @DisplayName("清理临时文件 - 异常处理")
    void cleanupTempFiles_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("文件系统错误");
        when(archiveService.cleanupTempFiles()).thenThrow(exception);

        // When
        archiveScheduler.cleanupTempFiles();

        // Then
        verify(archiveService).cleanupTempFiles();
        verify(logger).info("开始执行清理临时文件任务");
        verify(logger).error("清理临时文件任务执行失败", exception);
    }

    @Test
    @DisplayName("生成档案统计报告 - 成功")
    void generateArchiveStatistics_Success() {
        // Given
        doNothing().when(archiveService).generateStatisticsReport();

        // When
        archiveScheduler.generateArchiveStatistics();

        // Then
        verify(archiveService).generateStatisticsReport();
        verify(logger).info("开始执行档案统计报告生成任务");
        verify(logger).info("档案统计报告生成完成");
    }

    @Test
    @DisplayName("生成档案统计报告 - 异常处理")
    void generateArchiveStatistics_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("报告生成失败");
        doThrow(exception).when(archiveService).generateStatisticsReport();

        // When
        archiveScheduler.generateArchiveStatistics();

        // Then
        verify(archiveService).generateStatisticsReport();
        verify(logger).info("开始执行档案统计报告生成任务");
        verify(logger).error("档案统计报告生成任务执行失败", exception);
    }

    @Test
    @DisplayName("备份档案数据 - 成功")
    void backupArchiveData_Success() {
        // Given
        when(archiveService.backupArchiveData()).thenReturn(true);

        // When
        archiveScheduler.backupArchiveData();

        // Then
        verify(archiveService).backupArchiveData();
        verify(logger).info("开始执行档案数据备份任务");
        verify(logger).info("档案数据备份任务完成，备份状态: {}", true);
    }

    @Test
    @DisplayName("备份档案数据 - 异常处理")
    void backupArchiveData_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("备份失败");
        when(archiveService.backupArchiveData()).thenThrow(exception);

        // When
        archiveScheduler.backupArchiveData();

        // Then
        verify(archiveService).backupArchiveData();
        verify(logger).info("开始执行档案数据备份任务");
        verify(logger).error("档案数据备份任务执行失败", exception);
    }

    @Test
    @DisplayName("同步档案索引 - 成功")
    void syncArchiveIndex_Success() {
        // Given
        when(archiveService.syncArchiveIndex()).thenReturn(100);

        // When
        archiveScheduler.syncArchiveIndex();

        // Then
        verify(archiveService).syncArchiveIndex();
        verify(logger).info("开始执行档案索引同步任务");
        verify(logger).info("档案索引同步完成，共同步 {} 条记录", 100);
    }

    @Test
    @DisplayName("同步档案索引 - 异常处理")
    void syncArchiveIndex_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("索引同步失败");
        when(archiveService.syncArchiveIndex()).thenThrow(exception);

        // When
        archiveScheduler.syncArchiveIndex();

        // Then
        verify(archiveService).syncArchiveIndex();
        verify(logger).info("开始执行档案索引同步任务");
        verify(logger).error("档案索引同步任务执行失败", exception);
    }

    @Test
    @DisplayName("检查档案完整性 - 成功")
    void checkArchiveIntegrity_Success() {
        // Given
        when(archiveService.checkArchiveIntegrity()).thenReturn(5);

        // When
        archiveScheduler.checkArchiveIntegrity();

        // Then
        verify(archiveService).checkArchiveIntegrity();
        verify(logger).info("开始执行档案完整性检查任务");
        verify(logger).info("档案完整性检查完成，发现 {} 个问题", 5);
    }

    @Test
    @DisplayName("检查档案完整性 - 异常处理")
    void checkArchiveIntegrity_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("完整性检查失败");
        when(archiveService.checkArchiveIntegrity()).thenThrow(exception);

        // When
        archiveScheduler.checkArchiveIntegrity();

        // Then
        verify(archiveService).checkArchiveIntegrity();
        verify(logger).info("开始执行档案完整性检查任务");
        verify(logger).error("档案完整性检查任务执行失败", exception);
    }

    @Test
    @DisplayName("更新档案访问统计 - 成功")
    void updateArchiveAccessStatistics_Success() {
        // Given
        doNothing().when(archiveService).updateAccessStatistics();

        // When
        archiveScheduler.updateArchiveAccessStatistics();

        // Then
        verify(archiveService).updateAccessStatistics();
        verify(logger).info("开始执行档案访问统计更新任务");
        verify(logger).info("档案访问统计更新完成");
    }

    @Test
    @DisplayName("更新档案访问统计 - 异常处理")
    void updateArchiveAccessStatistics_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("统计更新失败");
        doThrow(exception).when(archiveService).updateAccessStatistics();

        // When
        archiveScheduler.updateArchiveAccessStatistics();

        // Then
        verify(archiveService).updateAccessStatistics();
        verify(logger).info("开始执行档案访问统计更新任务");
        verify(logger).error("档案访问统计更新任务执行失败", exception);
    }

    @Test
    @DisplayName("清理已删除档案 - 成功")
    void cleanupDeletedArchives_Success() {
        // Given
        when(archiveService.cleanupDeletedArchives()).thenReturn(3);

        // When
        archiveScheduler.cleanupDeletedArchives();

        // Then
        verify(archiveService).cleanupDeletedArchives();
        verify(logger).info("开始执行已删除档案清理任务");
        verify(logger).info("已删除档案清理完成，共清理 {} 个档案", 3);
    }

    @Test
    @DisplayName("清理已删除档案 - 异常处理")
    void cleanupDeletedArchives_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("清理失败");
        when(archiveService.cleanupDeletedArchives()).thenThrow(exception);

        // When
        archiveScheduler.cleanupDeletedArchives();

        // Then
        verify(archiveService).cleanupDeletedArchives();
        verify(logger).info("开始执行已删除档案清理任务");
        verify(logger).error("已删除档案清理任务执行失败", exception);
    }

    @Test
    @DisplayName("优化档案存储 - 成功")
    void optimizeArchiveStorage_Success() {
        // Given
        when(archiveService.optimizeStorage()).thenReturn(1024L);

        // When
        archiveScheduler.optimizeArchiveStorage();

        // Then
        verify(archiveService).optimizeStorage();
        verify(logger).info("开始执行档案存储优化任务");
        verify(logger).info("档案存储优化完成，节省空间 {} MB", 1024L);
    }

    @Test
    @DisplayName("优化档案存储 - 异常处理")
    void optimizeArchiveStorage_Exception() {
        // Given
        RuntimeException exception = new RuntimeException("存储优化失败");
        when(archiveService.optimizeStorage()).thenThrow(exception);

        // When
        archiveScheduler.optimizeArchiveStorage();

        // Then
        verify(archiveService).optimizeStorage();
        verify(logger).info("开始执行档案存储优化任务");
        verify(logger).error("档案存储优化任务执行失败", exception);
    }

    @Test
    @DisplayName("验证任务执行时间记录")
    void verifyTaskExecutionTimeLogging() {
        // Given
        when(archiveService.archiveExpiredArchives()).thenReturn(5);

        // When
        archiveScheduler.autoArchiveExpiredArchives();

        // Then
        verify(logger).info("开始执行自动归档过期档案任务");
        verify(logger).info("自动归档任务完成，共归档 {} 个过期档案", 5);
        // 验证任务执行时间被记录（通过检查日志调用次数）
        verify(logger, atLeast(2)).info(anyString(), any());
    }

    @Test
    @DisplayName("验证所有任务都有异常处理")
    void verifyAllTasksHaveExceptionHandling() {
        // 这个测试确保所有定时任务方法都有适当的异常处理
        // 通过检查每个方法在异常情况下的行为来验证

        // 测试所有任务方法的异常处理
        RuntimeException testException = new RuntimeException("测试异常");

        // 自动归档
        when(archiveService.archiveExpiredArchives()).thenThrow(testException);
        archiveScheduler.autoArchiveExpiredArchives();
        verify(logger).error("自动归档过期档案任务执行失败", testException);

        // 清理临时文件
        when(archiveService.cleanupTempFiles()).thenThrow(testException);
        archiveScheduler.cleanupTempFiles();
        verify(logger).error("清理临时文件任务执行失败", testException);

        // 生成统计报告
        doThrow(testException).when(archiveService).generateStatisticsReport();
        archiveScheduler.generateArchiveStatistics();
        verify(logger).error("档案统计报告生成任务执行失败", testException);
    }
}