package com.archive.management.service;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.UserMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 高级搜索服务测试类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
class AdvancedSearchServiceTest {

    @Mock
    private ArchiveMapper archiveMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private AdvancedSearchService advancedSearchService;

    private Archive testArchive;
    private User testUser;
    private Map<String, Object> searchCriteria;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testArchive = new Archive();
        testArchive.setId(1L);
        testArchive.setTitle("测试档案");
        testArchive.setArchiveNumber("TEST-001");
        testArchive.setDescription("这是一个测试档案");
        testArchive.setStatus(1);
        testArchive.setDeleted(0);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);
        testUser.setDeleted(0);

        searchCriteria = new HashMap<>();
        searchCriteria.put("title", "测试");
        searchCriteria.put("status", 1);

        // 设置Redis模板模拟
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testAdvancedSearchArchives() {
        // 准备测试数据
        Page<Archive> page = new Page<>(1, 10);
        List<Archive> archives = new ArrayList<>();
        archives.add(testArchive);
        
        IPage<Archive> expectedPage = new Page<>(1, 10, 1);
        expectedPage.setRecords(archives);

        when(archiveMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        IPage<Archive> result = advancedSearchService.advancedSearchArchives(searchCriteria, page);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("测试档案", result.getRecords().get(0).getTitle());

        // 验证方法调用
        verify(archiveMapper).selectPage(any(Page.class), any());
        verify(monitoringService).recordDocumentSearched();
    }

    @Test
    void testAdvancedSearchUsers() {
        // 准备测试数据
        Page<User> page = new Page<>(1, 10);
        List<User> users = new ArrayList<>();
        users.add(testUser);
        
        IPage<User> expectedPage = new Page<>(1, 10, 1);
        expectedPage.setRecords(users);

        when(userMapper.selectPage(any(Page.class), any())).thenReturn(expectedPage);

        // 执行测试
        IPage<User> result = advancedSearchService.advancedSearchUsers(searchCriteria, page);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("testuser", result.getRecords().get(0).getUsername());

        // 验证方法调用
        verify(userMapper).selectPage(any(Page.class), any());
        verify(monitoringService).recordDocumentSearched();
    }

    @Test
    void testFullTextSearchArchives() {
        // 准备测试数据
        List<Archive> archives = new ArrayList<>();
        archives.add(testArchive);

        when(archiveMapper.selectList(any())).thenReturn(archives);

        // 执行测试
        List<Archive> result = advancedSearchService.fullTextSearchArchives("测试", 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试档案", result.get(0).getTitle());

        // 验证方法调用
        verify(archiveMapper).selectList(any());
        verify(monitoringService).recordDocumentSearched();
    }

    @Test
    void testFullTextSearchUsers() {
        // 准备测试数据
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(userMapper.selectList(any())).thenReturn(users);

        // 执行测试
        List<User> result = advancedSearchService.fullTextSearchUsers("test", 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());

        // 验证方法调用
        verify(userMapper).selectList(any());
        verify(monitoringService).recordDocumentSearched();
    }

    @Test
    void testGetSearchSuggestions() {
        // 准备测试数据
        List<Archive> archives = new ArrayList<>();
        archives.add(testArchive);

        when(archiveMapper.selectList(any())).thenReturn(archives);

        // 执行测试
        List<String> result = advancedSearchService.getSearchSuggestions("测试", "archive");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试档案", result.get(0));

        // 验证方法调用
        verify(archiveMapper).selectList(any());
    }

    @Test
    void testGetSearchStatistics() {
        // 准备测试数据
        when(archiveMapper.selectCount(any())).thenReturn(10L);
        when(userMapper.selectCount(any())).thenReturn(5L);

        // 执行测试
        Map<String, Object> result = advancedSearchService.getSearchStatistics();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("totalArchives"));
        assertTrue(result.containsKey("totalUsers"));

        // 验证方法调用
        verify(archiveMapper, atLeastOnce()).selectCount(any());
        verify(userMapper, atLeastOnce()).selectCount(any());
    }

    @Test
    void testRecordSearchHistory() {
        // 执行测试
        advancedSearchService.recordSearchHistory("测试", "archive", 1L);

        // 验证方法调用
        verify(redisTemplate).opsForValue();
    }

    @Test
    void testGetSearchHistory() {
        // 准备测试数据
        List<String> history = new ArrayList<>();
        history.add("测试搜索1");
        history.add("测试搜索2");

        when(valueOperations.get(anyString())).thenReturn(history);

        // 执行测试
        List<String> result = advancedSearchService.getSearchHistory(1L, "archive");

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证方法调用
        verify(valueOperations).get(anyString());
    }

    @Test
    void testClearSearchHistory() {
        // 执行测试
        advancedSearchService.clearSearchHistory(1L, "archive");

        // 验证方法调用
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void testGetHotSearchKeywords() {
        // 执行测试
        List<String> result = advancedSearchService.getHotSearchKeywords("archive");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    void testFindSimilarArchives() {
        // 准备测试数据
        List<Archive> similarArchives = new ArrayList<>();
        similarArchives.add(testArchive);

        when(archiveMapper.selectById(anyLong())).thenReturn(testArchive);
        when(archiveMapper.selectList(any())).thenReturn(similarArchives);

        // 执行测试
        List<Archive> result = advancedSearchService.findSimilarArchives(1L, 5);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试档案", result.get(0).getTitle());

        // 验证方法调用
        verify(archiveMapper).selectById(anyLong());
        verify(archiveMapper).selectList(any());
    }

    @Test
    void testAdvancedSearchArchivesWithException() {
        // 准备异常情况
        when(archiveMapper.selectPage(any(Page.class), any())).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            advancedSearchService.advancedSearchArchives(searchCriteria, new Page<>(1, 10));
        });

        // 验证错误记录
        verify(monitoringService).recordError(anyString(), anyString());
    }
}
