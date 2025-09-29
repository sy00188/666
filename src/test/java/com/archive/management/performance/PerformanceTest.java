package com.archive.management.performance;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.enums.ArchiveType;
import com.archive.management.enums.UserRole;
import com.archive.management.enums.UserStatus;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.ArchiveService;
import com.archive.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 性能测试类
 * 测试系统的性能表现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("系统性能测试")
class PerformanceTest {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private static final int LARGE_DATA_SIZE = 1000;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        archiveRepository.deleteAll();
        userRepository.deleteAll();

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("perftest_user");
        testUser.setPassword("password123");
        testUser.setEmail("perftest@example.com");
        testUser.setFullName("Performance Test User");
        testUser.setRole(UserRole.USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("大量数据创建性能测试")
    @Transactional
    void testBulkDataCreationPerformance() {
        StopWatch stopWatch = new StopWatch("批量数据创建性能测试");
        
        // 测试批量创建用户
        stopWatch.start("创建" + LARGE_DATA_SIZE + "个用户");
        List<User> users = new ArrayList<>();
        for (int i = 0; i < LARGE_DATA_SIZE; i++) {
            User user = new User();
            user.setUsername("bulk_user_" + i);
            user.setPassword("password123");
            user.setEmail("bulk_user_" + i + "@example.com");
            user.setFullName("Bulk User " + i);
            user.setRole(UserRole.USER);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            users.add(user);
        }
        userRepository.saveAll(users);
        stopWatch.stop();

        // 测试批量创建档案
        stopWatch.start("创建" + LARGE_DATA_SIZE + "个档案");
        List<Archive> archives = new ArrayList<>();
        for (int i = 0; i < LARGE_DATA_SIZE; i++) {
            Archive archive = new Archive();
            archive.setTitle("批量档案 " + i);
            archive.setDescription("性能测试档案 " + i);
            archive.setType(ArchiveType.DOCUMENT);
            archive.setStatus(ArchiveStatus.ACTIVE);
            archive.setCreatedBy(testUser.getId());
            archive.setCreatedAt(LocalDateTime.now());
            archives.add(archive);
        }
        archiveRepository.saveAll(archives);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 验证数据创建成功
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(LARGE_DATA_SIZE);
        assertThat(archiveRepository.count()).isGreaterThanOrEqualTo(LARGE_DATA_SIZE);

        // 性能断言（根据实际环境调整）
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(30000); // 30秒内完成
    }

    @Test
    @DisplayName("大量数据查询性能测试")
    void testBulkDataQueryPerformance() {
        // 先创建测试数据
        createTestData(500);

        StopWatch stopWatch = new StopWatch("大量数据查询性能测试");

        // 测试分页查询性能
        stopWatch.start("分页查询用户");
        Pageable pageable = PageRequest.of(0, 50);
        Page<User> userPage = userService.getAllUsers(pageable);
        stopWatch.stop();

        stopWatch.start("分页查询档案");
        Page<Archive> archivePage = archiveService.getAllArchives(pageable);
        stopWatch.stop();

        // 测试搜索查询性能
        stopWatch.start("搜索用户");
        Page<User> searchUsers = userService.searchUsers("bulk", pageable);
        stopWatch.stop();

        stopWatch.start("搜索档案");
        Page<Archive> searchArchives = archiveService.searchArchives("批量", pageable);
        stopWatch.stop();

        // 测试统计查询性能
        stopWatch.start("统计查询");
        long userCount = userService.getTotalUserCount();
        long archiveCount = archiveService.getTotalArchiveCount();
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 验证查询结果
        assertThat(userPage.getContent()).isNotEmpty();
        assertThat(archivePage.getContent()).isNotEmpty();
        assertThat(userCount).isGreaterThan(0);
        assertThat(archiveCount).isGreaterThan(0);

        // 性能断言
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // 5秒内完成
    }

    @Test
    @DisplayName("并发操作性能测试")
    void testConcurrentOperationPerformance() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        StopWatch stopWatch = new StopWatch("并发操作性能测试");

        // 并发创建用户
        stopWatch.start("并发创建用户");
        List<CompletableFuture<User>> userFutures = IntStream.range(0, 100)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    User user = new User();
                    user.setUsername("concurrent_user_" + i + "_" + Thread.currentThread().getId());
                    user.setPassword("password123");
                    user.setEmail("concurrent_" + i + "@example.com");
                    user.setFullName("Concurrent User " + i);
                    user.setRole(UserRole.USER);
                    user.setStatus(UserStatus.ACTIVE);
                    return userService.createUser(user);
                }, executor))
                .toList();

        CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0])).get();
        stopWatch.stop();

        // 并发创建档案
        stopWatch.start("并发创建档案");
        List<CompletableFuture<Archive>> archiveFutures = IntStream.range(0, 100)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    Archive archive = new Archive();
                    archive.setTitle("并发档案 " + i + "_" + Thread.currentThread().getId());
                    archive.setDescription("并发测试档案 " + i);
                    archive.setType(ArchiveType.DOCUMENT);
                    archive.setStatus(ArchiveStatus.ACTIVE);
                    archive.setCreatedBy(testUser.getId());
                    return archiveService.createArchive(archive);
                }, executor))
                .toList();

        CompletableFuture.allOf(archiveFutures.toArray(new CompletableFuture[0])).get();
        stopWatch.stop();

        // 并发查询操作
        stopWatch.start("并发查询操作");
        List<CompletableFuture<Void>> queryFutures = IntStream.range(0, 50)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    Pageable pageable = PageRequest.of(0, 10);
                    userService.getAllUsers(pageable);
                    archiveService.getAllArchives(pageable);
                }, executor))
                .toList();

        CompletableFuture.allOf(queryFutures.toArray(new CompletableFuture[0])).get();
        stopWatch.stop();

        executor.shutdown();
        System.out.println(stopWatch.prettyPrint());

        // 验证并发操作结果
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(100);
        assertThat(archiveRepository.count()).isGreaterThanOrEqualTo(100);

        // 性能断言
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(15000); // 15秒内完成
    }

    @Test
    @DisplayName("内存使用性能测试")
    void testMemoryUsagePerformance() {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存使用
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 创建大量对象
        List<Archive> archives = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Archive archive = new Archive();
            archive.setTitle("内存测试档案 " + i);
            archive.setDescription("测试内存使用情况的档案 " + i);
            archive.setType(ArchiveType.DOCUMENT);
            archive.setStatus(ArchiveStatus.ACTIVE);
            archive.setCreatedBy(testUser.getId());
            archive.setCreatedAt(LocalDateTime.now());
            archives.add(archive);
        }

        // 记录峰值内存使用
        long peakMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 清理对象
        archives.clear();
        System.gc(); // 建议垃圾回收
        
        // 等待垃圾回收
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 记录清理后内存使用
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("初始内存使用: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("峰值内存使用: " + (peakMemory / 1024 / 1024) + " MB");
        System.out.println("最终内存使用: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("内存增长: " + ((peakMemory - initialMemory) / 1024 / 1024) + " MB");

        // 内存使用断言
        long memoryIncrease = peakMemory - initialMemory;
        assertThat(memoryIncrease).isLessThan(500 * 1024 * 1024); // 内存增长不超过500MB
    }

    @Test
    @DisplayName("数据库连接池性能测试")
    void testDatabaseConnectionPoolPerformance() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        StopWatch stopWatch = new StopWatch("数据库连接池性能测试");

        stopWatch.start("高并发数据库操作");
        
        List<CompletableFuture<Void>> futures = IntStream.range(0, 200)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    // 执行数据库操作
                    User user = new User();
                    user.setUsername("pool_test_" + i + "_" + System.currentTimeMillis());
                    user.setPassword("password123");
                    user.setEmail("pool_test_" + i + "@example.com");
                    user.setFullName("Pool Test User " + i);
                    user.setRole(UserRole.USER);
                    user.setStatus(UserStatus.ACTIVE);
                    
                    User savedUser = userRepository.save(user);
                    
                    // 立即查询
                    userRepository.findById(savedUser.getId());
                    
                    // 更新操作
                    savedUser.setFullName("Updated Pool Test User " + i);
                    userRepository.save(savedUser);
                    
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        stopWatch.stop();

        executor.shutdown();
        System.out.println(stopWatch.prettyPrint());

        // 验证操作成功
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(200);

        // 性能断言
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(20000); // 20秒内完成
    }

    @Test
    @DisplayName("缓存性能测试")
    void testCachePerformance() {
        // 创建测试数据
        Archive archive = new Archive();
        archive.setTitle("缓存性能测试档案");
        archive.setDescription("测试缓存性能");
        archive.setType(ArchiveType.DOCUMENT);
        archive.setStatus(ArchiveStatus.ACTIVE);
        archive.setCreatedBy(testUser.getId());
        Archive savedArchive = archiveService.createArchive(archive);

        StopWatch stopWatch = new StopWatch("缓存性能测试");

        // 第一次查询（冷缓存）
        stopWatch.start("首次查询（冷缓存）");
        for (int i = 0; i < 100; i++) {
            archiveService.getArchiveById(savedArchive.getId());
        }
        stopWatch.stop();

        // 第二次查询（热缓存）
        stopWatch.start("重复查询（热缓存）");
        for (int i = 0; i < 100; i++) {
            archiveService.getArchiveById(savedArchive.getId());
        }
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());

        // 缓存性能断言（热缓存应该明显快于冷缓存）
        long coldCacheTime = stopWatch.getTaskInfo()[0].getTimeMillis();
        long hotCacheTime = stopWatch.getTaskInfo()[1].getTimeMillis();
        
        System.out.println("冷缓存时间: " + coldCacheTime + "ms");
        System.out.println("热缓存时间: " + hotCacheTime + "ms");
        
        // 热缓存应该比冷缓存快（允许一定误差）
        assertThat(hotCacheTime).isLessThanOrEqualTo(coldCacheTime + 100);
    }

    @Test
    @DisplayName("分页查询性能测试")
    void testPaginationPerformance() {
        // 创建大量测试数据
        createTestData(2000);

        StopWatch stopWatch = new StopWatch("分页查询性能测试");

        // 测试不同页面大小的性能
        int[] pageSizes = {10, 50, 100, 200};
        
        for (int pageSize : pageSizes) {
            stopWatch.start("页面大小: " + pageSize);
            
            Pageable pageable = PageRequest.of(0, pageSize);
            Page<Archive> archivePage = archiveService.getAllArchives(pageable);
            
            // 访问多页
            for (int page = 0; page < Math.min(10, archivePage.getTotalPages()); page++) {
                pageable = PageRequest.of(page, pageSize);
                archiveService.getAllArchives(pageable);
            }
            
            stopWatch.stop();
        }

        System.out.println(stopWatch.prettyPrint());

        // 性能断言
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(10000); // 10秒内完成
    }

    @Test
    @DisplayName("搜索性能测试")
    void testSearchPerformance() {
        // 创建测试数据
        createTestData(1000);

        StopWatch stopWatch = new StopWatch("搜索性能测试");

        String[] searchTerms = {"测试", "档案", "用户", "批量", "性能"};
        
        for (String term : searchTerms) {
            stopWatch.start("搜索关键词: " + term);
            
            Pageable pageable = PageRequest.of(0, 20);
            
            // 搜索用户
            userService.searchUsers(term, pageable);
            
            // 搜索档案
            archiveService.searchArchives(term, pageable);
            
            stopWatch.stop();
        }

        System.out.println(stopWatch.prettyPrint());

        // 性能断言
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // 5秒内完成
    }

    /**
     * 创建测试数据
     */
    private void createTestData(int count) {
        // 创建用户
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count / 2; i++) {
            User user = new User();
            user.setUsername("bulk_user_" + i);
            user.setPassword("password123");
            user.setEmail("bulk_user_" + i + "@example.com");
            user.setFullName("批量测试用户 " + i);
            user.setRole(UserRole.USER);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            users.add(user);
        }
        userRepository.saveAll(users);

        // 创建档案
        List<Archive> archives = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Archive archive = new Archive();
            archive.setTitle("批量测试档案 " + i);
            archive.setDescription("性能测试档案描述 " + i);
            archive.setType(i % 2 == 0 ? ArchiveType.DOCUMENT : ArchiveType.IMAGE);
            archive.setStatus(i % 3 == 0 ? ArchiveStatus.ARCHIVED : ArchiveStatus.ACTIVE);
            archive.setCreatedBy(testUser.getId());
            archive.setCreatedAt(LocalDateTime.now().minusDays(i % 30));
            archives.add(archive);
        }
        archiveRepository.saveAll(archives);
    }
}