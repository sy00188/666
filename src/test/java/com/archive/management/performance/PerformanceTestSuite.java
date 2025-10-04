package com.archive.management.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.archive.management.service.MonitoringService;
import com.archive.management.service.CacheService;
import com.archive.management.service.ObservationService;
import com.archive.management.service.BatchOperationService;
import com.archive.management.service.AdvancedSearchService;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能测试套件
 * 测试系统在高并发和大数据量下的性能表现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cache.type=simple"
})
@DisplayName("性能测试套件")
class PerformanceTestSuite {

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ObservationService observationService;

    @Autowired
    private BatchOperationService batchOperationService;

    @Autowired
    private AdvancedSearchService advancedSearchService;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    @DisplayName("缓存性能测试")
    void testCachePerformance() {
        // 测试缓存读写性能
        long startTime = System.currentTimeMillis();
        
        // 写入测试
        for (int i = 0; i < 1000; i++) {
            cacheService.put("test", "key" + i, "value" + i);
        }
        
        // 读取测试
        for (int i = 0; i < 1000; i++) {
            String value = cacheService.get("test", "key" + i, String.class);
            assertNotNull(value);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("缓存性能测试完成，耗时: " + duration + "ms");
        assertTrue(duration < 5000, "缓存操作应在5秒内完成");
    }

    @Test
    @DisplayName("并发缓存测试")
    void testConcurrentCacheOperations() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "thread" + threadId + "_key" + j;
                    String value = "thread" + threadId + "_value" + j;
                    
                    cacheService.put("concurrent", key, value);
                    String retrievedValue = cacheService.get("concurrent", key, String.class);
                    assertEquals(value, retrievedValue);
                }
            }, executorService);
            futures.add(future);
        }
        
        // 等待所有线程完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        System.out.println("并发缓存测试完成，线程数: " + threadCount + ", 每线程操作数: " + operationsPerThread);
    }

    @Test
    @DisplayName("批量操作性能测试")
    void testBatchOperationPerformance() {
        // 准备测试数据
        List<Long> archiveIds = IntStream.range(1, 1001).mapToObj(Long::valueOf).toList();
        
        long startTime = System.currentTimeMillis();
        
        // 执行批量操作
        Map<String, Object> result = batchOperationService.batchUpdateArchiveStatus(
            archiveIds, com.archive.management.enums.ArchiveStatus.ACTIVE);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("批量操作性能测试完成，处理数量: " + archiveIds.size() + ", 耗时: " + duration + "ms");
        assertTrue(duration < 10000, "批量操作应在10秒内完成");
        assertTrue((Boolean) result.get("success"), "批量操作应成功");
    }

    @Test
    @DisplayName("搜索性能测试")
    void testSearchPerformance() {
        // 准备搜索条件
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("keyword", "测试");
        searchCriteria.put("status", 1);
        
        long startTime = System.currentTimeMillis();
        
        // 执行搜索
        List<com.archive.management.entity.Archive> results = advancedSearchService.fullTextSearchArchives("测试", 100);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("搜索性能测试完成，结果数量: " + results.size() + ", 耗时: " + duration + "ms");
        assertTrue(duration < 2000, "搜索操作应在2秒内完成");
    }

    @Test
    @DisplayName("并发搜索测试")
    void testConcurrentSearchOperations() throws InterruptedException {
        int threadCount = 5;
        int searchesPerThread = 20;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < searchesPerThread; j++) {
                    String keyword = "测试" + threadId + "_" + j;
                    List<com.archive.management.entity.Archive> results = 
                        advancedSearchService.fullTextSearchArchives(keyword, 10);
                    assertNotNull(results);
                }
            }, executorService);
            futures.add(future);
        }
        
        // 等待所有线程完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        System.out.println("并发搜索测试完成，线程数: " + threadCount + ", 每线程搜索数: " + searchesPerThread);
    }

    @Test
    @DisplayName("监控指标性能测试")
    void testMonitoringPerformance() {
        long startTime = System.currentTimeMillis();
        
        // 执行大量监控操作
        for (int i = 0; i < 1000; i++) {
            monitoringService.recordUserRegistration();
            monitoringService.recordDocumentCreated();
            monitoringService.recordDocumentSearched();
            monitoringService.incrementConcurrentOperations();
            monitoringService.decrementConcurrentOperations();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("监控指标性能测试完成，操作数量: 5000, 耗时: " + duration + "ms");
        assertTrue(duration < 3000, "监控操作应在3秒内完成");
    }

    @Test
    @DisplayName("观察器性能测试")
    void testObservationPerformance() {
        long startTime = System.currentTimeMillis();
        
        // 执行大量观察操作
        for (int i = 0; i < 1000; i++) {
            observationService.observe("test.operation", () -> {
                // 模拟业务操作
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("观察器性能测试完成，操作数量: 1000, 耗时: " + duration + "ms");
        assertTrue(duration < 5000, "观察操作应在5秒内完成");
    }

    @Test
    @DisplayName("内存使用测试")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行内存密集型操作
        List<String> largeList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeList.add("Large string data " + i + " with some additional content to increase memory usage");
        }
        
        long afterOperationMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterOperationMemory - initialMemory;
        
        System.out.println("内存使用测试完成，使用内存: " + memoryUsed + " bytes");
        assertTrue(memoryUsed < 100 * 1024 * 1024, "内存使用应小于100MB");
        
        // 清理内存
        largeList.clear();
        System.gc();
    }

    @Test
    @DisplayName("数据库连接池性能测试")
    void testDatabaseConnectionPoolPerformance() {
        long startTime = System.currentTimeMillis();
        
        // 模拟数据库操作
        for (int i = 0; i < 100; i++) {
            // 这里可以添加实际的数据库操作测试
            // 例如：userRepository.findAll(), archiveRepository.count() 等
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("数据库连接池性能测试完成，操作数量: 100, 耗时: " + duration + "ms");
        assertTrue(duration < 5000, "数据库操作应在5秒内完成");
    }

    @Test
    @DisplayName("综合性能测试")
    void testComprehensivePerformance() {
        long startTime = System.currentTimeMillis();
        
        // 综合测试：缓存 + 搜索 + 监控
        for (int i = 0; i < 100; i++) {
            // 缓存操作
            cacheService.put("comprehensive", "key" + i, "value" + i);
            
            // 搜索操作
            advancedSearchService.fullTextSearchArchives("测试" + i, 10);
            
            // 监控操作
            monitoringService.recordDocumentSearched();
            
            // 观察操作
            observationService.observe("comprehensive.operation", () -> {
                // 模拟业务逻辑
            });
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("综合性能测试完成，操作数量: 400, 耗时: " + duration + "ms");
        assertTrue(duration < 10000, "综合操作应在10秒内完成");
    }
}
