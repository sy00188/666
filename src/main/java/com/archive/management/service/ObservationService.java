package com.archive.management.service;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 观察服务类
 * 使用Micrometer Observation API进行性能监控和链路追踪
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class ObservationService {

    @Autowired
    private ObservationRegistry observationRegistry;

    /**
     * 执行观察操作
     */
    public <T> T observe(String name, Supplier<T> operation) {
        return Observation.createNotStarted(name, observationRegistry)
                .observe(operation);
    }

    /**
     * 执行观察操作（带标签）
     */
    public <T> T observe(String name, String tagKey, String tagValue, Supplier<T> operation) {
        return Observation.createNotStarted(name, observationRegistry)
                .tag(tagKey, tagValue)
                .observe(operation);
    }

    /**
     * 执行观察操作（带多个标签）
     */
    public <T> T observe(String name, String[] tagKeys, String[] tagValues, Supplier<T> operation) {
        Observation.Builder builder = Observation.createNotStarted(name, observationRegistry);
        for (int i = 0; i < tagKeys.length && i < tagValues.length; i++) {
            builder.tag(tagKeys[i], tagValues[i]);
        }
        return builder.observe(operation);
    }

    /**
     * 执行观察操作（无返回值）
     */
    public void observe(String name, Runnable operation) {
        Observation.createNotStarted(name, observationRegistry)
                .observe(operation);
    }

    /**
     * 执行观察操作（无返回值，带标签）
     */
    public void observe(String name, String tagKey, String tagValue, Runnable operation) {
        Observation.createNotStarted(name, observationRegistry)
                .tag(tagKey, tagValue)
                .observe(operation);
    }

    /**
     * 创建观察器
     */
    public Observation createObservation(String name) {
        return Observation.createNotStarted(name, observationRegistry);
    }

    /**
     * 创建带标签的观察器
     */
    public Observation createObservation(String name, String tagKey, String tagValue) {
        return Observation.createNotStarted(name, observationRegistry)
                .tag(tagKey, tagValue);
    }

    /**
     * 创建带多个标签的观察器
     */
    public Observation createObservation(String name, String[] tagKeys, String[] tagValues) {
        Observation.Builder builder = Observation.createNotStarted(name, observationRegistry);
        for (int i = 0; i < tagKeys.length && i < tagValues.length; i++) {
            builder.tag(tagKeys[i], tagValues[i]);
        }
        return builder;
    }

    /**
     * 业务操作观察器
     */
    public <T> T observeBusinessOperation(String operation, String entityType, String entityId, Supplier<T> businessLogic) {
        return Observation.createNotStarted("business.operation", observationRegistry)
                .tag("operation", operation)
                .tag("entity.type", entityType)
                .tag("entity.id", entityId)
                .observe(businessLogic);
    }

    /**
     * 数据库操作观察器
     */
    public <T> T observeDatabaseOperation(String operation, String table, Supplier<T> databaseLogic) {
        return Observation.createNotStarted("database.operation", observationRegistry)
                .tag("operation", operation)
                .tag("table", table)
                .observe(databaseLogic);
    }

    /**
     * HTTP请求观察器
     */
    public <T> T observeHttpRequest(String method, String uri, Supplier<T> requestLogic) {
        return Observation.createNotStarted("http.request", observationRegistry)
                .tag("method", method)
                .tag("uri", uri)
                .observe(requestLogic);
    }

    /**
     * 缓存操作观察器
     */
    public <T> T observeCacheOperation(String operation, String cacheName, String key, Supplier<T> cacheLogic) {
        return Observation.createNotStarted("cache.operation", observationRegistry)
                .tag("operation", operation)
                .tag("cache.name", cacheName)
                .tag("cache.key", key)
                .observe(cacheLogic);
    }

    /**
     * 文件操作观察器
     */
    public <T> T observeFileOperation(String operation, String fileName, Supplier<T> fileLogic) {
        return Observation.createNotStarted("file.operation", observationRegistry)
                .tag("operation", operation)
                .tag("file.name", fileName)
                .observe(fileLogic);
    }

    /**
     * 搜索操作观察器
     */
    public <T> T observeSearchOperation(String searchType, String keyword, Supplier<T> searchLogic) {
        return Observation.createNotStarted("search.operation", observationRegistry)
                .tag("search.type", searchType)
                .tag("keyword", keyword)
                .observe(searchLogic);
    }

    /**
     * 批量操作观察器
     */
    public <T> T observeBatchOperation(String operation, int batchSize, Supplier<T> batchLogic) {
        return Observation.createNotStarted("batch.operation", observationRegistry)
                .tag("operation", operation)
                .tag("batch.size", String.valueOf(batchSize))
                .observe(batchLogic);
    }

    /**
     * 异步操作观察器
     */
    public <T> T observeAsyncOperation(String operation, String asyncType, Supplier<T> asyncLogic) {
        return Observation.createNotStarted("async.operation", observationRegistry)
                .tag("operation", operation)
                .tag("async.type", asyncType)
                .observe(asyncLogic);
    }

    /**
     * 错误处理观察器
     */
    public <T> T observeErrorHandling(String errorType, String errorCode, Supplier<T> errorLogic) {
        return Observation.createNotStarted("error.handling", observationRegistry)
                .tag("error.type", errorType)
                .tag("error.code", errorCode)
                .observe(errorLogic);
    }

    /**
     * 性能监控观察器
     */
    public <T> T observePerformance(String component, String operation, Supplier<T> performanceLogic) {
        return Observation.createNotStarted("performance.monitoring", observationRegistry)
                .tag("component", component)
                .tag("operation", operation)
                .observe(performanceLogic);
    }
}
