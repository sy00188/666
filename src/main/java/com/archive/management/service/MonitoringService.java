package com.archive.management.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控服务类
 * 提供各种业务指标的监控和统计
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class MonitoringService {

    @Autowired
    private MeterRegistry meterRegistry;

    // 业务指标计数器
    private final Counter userRegistrationCounter;
    private final Counter documentCreatedCounter;
    private final Counter documentDownloadedCounter;
    private final Counter userLoginCounter;
    private final Counter userLogoutCounter;
    private final Counter documentSearchedCounter;
    private final Counter documentUpdatedCounter;
    private final Counter documentDeletedCounter;
    private final Counter batchOperationCounter;
    private final Counter errorCounter;

    // 性能指标计时器
    private final Timer userRegistrationTimer;
    private final Timer documentCreationTimer;
    private final Timer documentSearchTimer;
    private final Timer documentDownloadTimer;
    private final Timer userLoginTimer;
    private final Timer batchOperationTimer;

    // 系统指标
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicLong totalDocuments = new AtomicLong(0);
    private final AtomicLong totalUsers = new AtomicLong(0);
    private final AtomicInteger concurrentOperations = new AtomicInteger(0);

    public MonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化业务指标计数器
        this.userRegistrationCounter = Counter.builder("archive.user.registration")
                .description("用户注册总数")
                .register(meterRegistry);

        this.documentCreatedCounter = Counter.builder("archive.document.created")
                .description("档案创建总数")
                .register(meterRegistry);

        this.documentDownloadedCounter = Counter.builder("archive.document.downloaded")
                .description("档案下载总数")
                .register(meterRegistry);

        this.userLoginCounter = Counter.builder("archive.user.login")
                .description("用户登录总数")
                .register(meterRegistry);

        this.userLogoutCounter = Counter.builder("archive.user.logout")
                .description("用户登出总数")
                .register(meterRegistry);

        this.documentSearchedCounter = Counter.builder("archive.document.searched")
                .description("档案搜索总数")
                .register(meterRegistry);

        this.documentUpdatedCounter = Counter.builder("archive.document.updated")
                .description("档案更新总数")
                .register(meterRegistry);

        this.documentDeletedCounter = Counter.builder("archive.document.deleted")
                .description("档案删除总数")
                .register(meterRegistry);

        this.batchOperationCounter = Counter.builder("archive.batch.operation")
                .description("批量操作总数")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("archive.error.count")
                .description("错误总数")
                .register(meterRegistry);

        // 初始化性能指标计时器
        this.userRegistrationTimer = Timer.builder("archive.user.registration.time")
                .description("用户注册耗时")
                .register(meterRegistry);

        this.documentCreationTimer = Timer.builder("archive.document.creation.time")
                .description("档案创建耗时")
                .register(meterRegistry);

        this.documentSearchTimer = Timer.builder("archive.document.search.time")
                .description("档案搜索耗时")
                .register(meterRegistry);

        this.documentDownloadTimer = Timer.builder("archive.document.download.time")
                .description("档案下载耗时")
                .register(meterRegistry);

        this.userLoginTimer = Timer.builder("archive.user.login.time")
                .description("用户登录耗时")
                .register(meterRegistry);

        this.batchOperationTimer = Timer.builder("archive.batch.operation.time")
                .description("批量操作耗时")
                .register(meterRegistry);

        // 注册系统指标
        Gauge.builder("archive.system.active.users")
                .description("当前活跃用户数")
                .register(meterRegistry, activeUsers, AtomicInteger::get);

        Gauge.builder("archive.system.total.documents")
                .description("系统总档案数")
                .register(meterRegistry, totalDocuments, AtomicLong::get);

        Gauge.builder("archive.system.total.users")
                .description("系统总用户数")
                .register(meterRegistry, totalUsers, AtomicLong::get);

        Gauge.builder("archive.system.concurrent.operations")
                .description("当前并发操作数")
                .register(meterRegistry, concurrentOperations, AtomicInteger::get);
    }

    /**
     * 记录用户注册
     */
    public void recordUserRegistration() {
        userRegistrationCounter.increment();
    }

    /**
     * 记录用户注册耗时
     */
    public void recordUserRegistrationTime(Runnable operation) {
        userRegistrationTimer.record(operation);
    }

    /**
     * 记录档案创建
     */
    public void recordDocumentCreated() {
        documentCreatedCounter.increment();
        totalDocuments.incrementAndGet();
    }

    /**
     * 记录档案创建耗时
     */
    public void recordDocumentCreationTime(Runnable operation) {
        documentCreationTimer.record(operation);
    }

    /**
     * 记录档案下载
     */
    public void recordDocumentDownloaded() {
        documentDownloadedCounter.increment();
    }

    /**
     * 记录档案下载耗时
     */
    public void recordDocumentDownloadTime(Runnable operation) {
        documentDownloadTimer.record(operation);
    }

    /**
     * 记录用户登录
     */
    public void recordUserLogin() {
        userLoginCounter.increment();
        activeUsers.incrementAndGet();
    }

    /**
     * 记录用户登录耗时
     */
    public void recordUserLoginTime(Runnable operation) {
        userLoginTimer.record(operation);
    }

    /**
     * 记录用户登出
     */
    public void recordUserLogout() {
        userLogoutCounter.increment();
        activeUsers.decrementAndGet();
    }

    /**
     * 记录档案搜索
     */
    public void recordDocumentSearched() {
        documentSearchedCounter.increment();
    }

    /**
     * 记录档案搜索耗时
     */
    public void recordDocumentSearchTime(Runnable operation) {
        documentSearchTimer.record(operation);
    }

    /**
     * 记录档案更新
     */
    public void recordDocumentUpdated() {
        documentUpdatedCounter.increment();
    }

    /**
     * 记录档案删除
     */
    public void recordDocumentDeleted() {
        documentDeletedCounter.increment();
        totalDocuments.decrementAndGet();
    }

    /**
     * 记录批量操作
     */
    public void recordBatchOperation() {
        batchOperationCounter.increment();
    }

    /**
     * 记录批量操作耗时
     */
    public void recordBatchOperationTime(Runnable operation) {
        batchOperationTimer.record(operation);
    }

    /**
     * 记录错误
     */
    public void recordError() {
        errorCounter.increment();
    }

    /**
     * 记录错误（带标签）
     */
    public void recordError(String errorType, String errorMessage) {
        Counter.builder("archive.error.count")
                .tag("type", errorType)
                .tag("message", errorMessage)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 增加并发操作数
     */
    public void incrementConcurrentOperations() {
        concurrentOperations.incrementAndGet();
    }

    /**
     * 减少并发操作数
     */
    public void decrementConcurrentOperations() {
        concurrentOperations.decrementAndGet();
    }

    /**
     * 更新总用户数
     */
    public void updateTotalUsers(long count) {
        totalUsers.set(count);
    }

    /**
     * 更新总档案数
     */
    public void updateTotalDocuments(long count) {
        totalDocuments.set(count);
    }

    /**
     * 获取当前活跃用户数
     */
    public int getActiveUsers() {
        return activeUsers.get();
    }

    /**
     * 获取总用户数
     */
    public long getTotalUsers() {
        return totalUsers.get();
    }

    /**
     * 获取总档案数
     */
    public long getTotalDocuments() {
        return totalDocuments.get();
    }

    /**
     * 获取当前并发操作数
     */
    public int getConcurrentOperations() {
        return concurrentOperations.get();
    }

    /**
     * 记录自定义指标
     */
    public void recordCustomMetric(String metricName, String description, double value) {
        Gauge.builder(metricName)
                .description(description)
                .register(meterRegistry, () -> value);
    }

    /**
     * 记录自定义计数器
     */
    public void recordCustomCounter(String counterName, String description) {
        Counter.builder(counterName)
                .description(description)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录自定义计时器
     */
    public void recordCustomTimer(String timerName, String description, Runnable operation) {
        Timer.builder(timerName)
                .description(description)
                .register(meterRegistry)
                .record(operation);
    }
}
