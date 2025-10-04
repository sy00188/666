package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.service.AdvancedSearchService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 高级搜索控制器测试类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
class AdvancedSearchControllerTest {

    @Mock
    private AdvancedSearchService advancedSearchService;

    @InjectMocks
    private AdvancedSearchController advancedSearchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Archive testArchive;
    private User testUser;
    private Map<String, Object> searchCriteria;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(advancedSearchController).build();
        objectMapper = new ObjectMapper();

        // 初始化测试数据
        testArchive = new Archive();
        testArchive.setId(1L);
        testArchive.setTitle("测试档案");
        testArchive.setArchiveNumber("TEST-001");
        testArchive.setDescription("这是一个测试档案");
        testArchive.setStatus(1);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);

        searchCriteria = new HashMap<>();
        searchCriteria.put("title", "测试");
        searchCriteria.put("status", 1);
    }

    @Test
    void testAdvancedSearchArchives() throws Exception {
        // 准备测试数据
        List<Archive> archives = new ArrayList<>();
        archives.add(testArchive);
        
        Page<Archive> page = new Page<>(1, 10, 1);
        page.setRecords(archives);
        
        IPage<Archive> expectedPage = page;

        when(advancedSearchService.advancedSearchArchives(any(Map.class), any(Page.class))).thenReturn(expectedPage);

        // 执行测试
        ResponseEntity<ApiResponse<IPage<Archive>>> response = advancedSearchController.advancedSearchArchives(
            searchCriteria, 1, 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getRecords().size());
        assertEquals("测试档案", response.getBody().getData().getRecords().get(0).getTitle());

        // 验证方法调用
        verify(advancedSearchService).advancedSearchArchives(any(Map.class), any(Page.class));
    }

    @Test
    void testAdvancedSearchUsers() throws Exception {
        // 准备测试数据
        List<User> users = new ArrayList<>();
        users.add(testUser);
        
        Page<User> page = new Page<>(1, 10, 1);
        page.setRecords(users);
        
        IPage<User> expectedPage = page;

        when(advancedSearchService.advancedSearchUsers(any(Map.class), any(Page.class))).thenReturn(expectedPage);

        // 执行测试
        ResponseEntity<ApiResponse<IPage<User>>> response = advancedSearchController.advancedSearchUsers(
            searchCriteria, 1, 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getRecords().size());
        assertEquals("testuser", response.getBody().getData().getRecords().get(0).getUsername());

        // 验证方法调用
        verify(advancedSearchService).advancedSearchUsers(any(Map.class), any(Page.class));
    }

    @Test
    void testFullTextSearchArchives() throws Exception {
        // 准备测试数据
        List<Archive> archives = new ArrayList<>();
        archives.add(testArchive);

        when(advancedSearchService.fullTextSearchArchives("测试", 10)).thenReturn(archives);

        // 执行测试
        ResponseEntity<ApiResponse<List<Archive>>> response = advancedSearchController.fullTextSearchArchives(
            "测试", 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("测试档案", response.getBody().getData().get(0).getTitle());

        // 验证方法调用
        verify(advancedSearchService).fullTextSearchArchives("测试", 10);
    }

    @Test
    void testFullTextSearchUsers() throws Exception {
        // 准备测试数据
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(advancedSearchService.fullTextSearchUsers("test", 10)).thenReturn(users);

        // 执行测试
        ResponseEntity<ApiResponse<List<User>>> response = advancedSearchController.fullTextSearchUsers(
            "test", 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("testuser", response.getBody().getData().get(0).getUsername());

        // 验证方法调用
        verify(advancedSearchService).fullTextSearchUsers("test", 10);
    }

    @Test
    void testGetSearchSuggestions() throws Exception {
        // 准备测试数据
        List<String> suggestions = new ArrayList<>();
        suggestions.add("测试档案");
        suggestions.add("测试文档");

        when(advancedSearchService.getSearchSuggestions("测试", "archive")).thenReturn(suggestions);

        // 执行测试
        ResponseEntity<ApiResponse<List<String>>> response = advancedSearchController.getSearchSuggestions(
            "测试", "archive");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("测试档案", response.getBody().getData().get(0));

        // 验证方法调用
        verify(advancedSearchService).getSearchSuggestions("测试", "archive");
    }

    @Test
    void testGetSearchStatistics() throws Exception {
        // 准备测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalArchives", 100);
        statistics.put("activeArchives", 80);
        statistics.put("totalUsers", 50);
        statistics.put("activeUsers", 45);

        when(advancedSearchService.getSearchStatistics()).thenReturn(statistics);

        // 执行测试
        ResponseEntity<ApiResponse<Map<String, Object>>> response = advancedSearchController.getSearchStatistics();

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(100, response.getBody().getData().get("totalArchives"));
        assertEquals(80, response.getBody().getData().get("activeArchives"));

        // 验证方法调用
        verify(advancedSearchService).getSearchStatistics();
    }

    @Test
    void testRecordSearchHistory() throws Exception {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("keyword", "测试");
        request.put("type", "archive");
        request.put("userId", 1L);

        // 执行测试
        ResponseEntity<ApiResponse<String>> response = advancedSearchController.recordSearchHistory(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // 验证方法调用
        verify(advancedSearchService).recordSearchHistory("测试", "archive", 1L);
    }

    @Test
    void testGetSearchHistory() throws Exception {
        // 准备测试数据
        List<String> history = new ArrayList<>();
        history.add("测试搜索1");
        history.add("测试搜索2");

        when(advancedSearchService.getSearchHistory(1L, "archive")).thenReturn(history);

        // 执行测试
        ResponseEntity<ApiResponse<List<String>>> response = advancedSearchController.getSearchHistory(1L, "archive");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("测试搜索1", response.getBody().getData().get(0));

        // 验证方法调用
        verify(advancedSearchService).getSearchHistory(1L, "archive");
    }

    @Test
    void testClearSearchHistory() throws Exception {
        // 执行测试
        ResponseEntity<ApiResponse<String>> response = advancedSearchController.clearSearchHistory(1L, "archive");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        // 验证方法调用
        verify(advancedSearchService).clearSearchHistory(1L, "archive");
    }

    @Test
    void testGetHotSearchKeywords() throws Exception {
        // 准备测试数据
        List<String> hotKeywords = new ArrayList<>();
        hotKeywords.add("年度报告");
        hotKeywords.add("会议纪要");

        when(advancedSearchService.getHotSearchKeywords("archive")).thenReturn(hotKeywords);

        // 执行测试
        ResponseEntity<ApiResponse<List<String>>> response = advancedSearchController.getHotSearchKeywords("archive");

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("年度报告", response.getBody().getData().get(0));

        // 验证方法调用
        verify(advancedSearchService).getHotSearchKeywords("archive");
    }

    @Test
    void testFindSimilarArchives() throws Exception {
        // 准备测试数据
        List<Archive> similarArchives = new ArrayList<>();
        similarArchives.add(testArchive);

        when(advancedSearchService.findSimilarArchives(1L, 5)).thenReturn(similarArchives);

        // 执行测试
        ResponseEntity<ApiResponse<List<Archive>>> response = advancedSearchController.findSimilarArchives(1L, 5);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("测试档案", response.getBody().getData().get(0).getTitle());

        // 验证方法调用
        verify(advancedSearchService).findSimilarArchives(1L, 5);
    }

    @Test
    void testAdvancedSearchArchivesWithException() throws Exception {
        // 准备异常情况
        when(advancedSearchService.advancedSearchArchives(any(Map.class), any(Page.class)))
            .thenThrow(new RuntimeException("搜索失败"));

        // 执行测试
        ResponseEntity<ApiResponse<IPage<Archive>>> response = advancedSearchController.advancedSearchArchives(
            searchCriteria, 1, 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("搜索失败"));
    }

    @Test
    void testFullTextSearchArchivesWithException() throws Exception {
        // 准备异常情况
        when(advancedSearchService.fullTextSearchArchives("测试", 10))
            .thenThrow(new RuntimeException("全文搜索失败"));

        // 执行测试
        ResponseEntity<ApiResponse<List<Archive>>> response = advancedSearchController.fullTextSearchArchives(
            "测试", 10);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("全文搜索失败"));
    }
}
