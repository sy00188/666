package com.archive.management.service;

import com.archive.management.BaseTest;
import com.archive.management.entity.Category;
import com.archive.management.factory.CategoryTestDataFactory;
import com.archive.management.mapper.CategoryMapper;
import com.archive.management.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * CategoryService 统计功能测试类
 * 测试分类的统计和高级功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("CategoryService 统计功能测试")
class CategoryServiceStatisticsTest extends BaseTest {

    @MockBean
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryMapper);
    }

    // ==================== 统计功能测试 ====================

    @Test
    @DisplayName("统计分类总数 - 成功场景")
    void shouldCountCategoriesSuccessfully() {
        // Given
        given(categoryMapper.selectCount(any())).willReturn(150L);
        
        // When
        long result = categoryService.countCategories();
        
        // Then
        assertThat(result).isEqualTo(150L);
        then(categoryMapper).should().selectCount(any());
    }

    @Test
    @DisplayName("根据状态统计分类数量 - 成功场景")
    void shouldCountCategoriesByStatusSuccessfully() {
        // Given
        Integer status = 1;
        given(categoryMapper.countByStatus(status)).willReturn(80L);
        
        // When
        long result = categoryService.countCategoriesByStatus(status);
        
        // Then
        assertThat(result).isEqualTo(80L);
        then(categoryMapper).should().countByStatus(status);
    }

    @Test
    @DisplayName("获取分类状态统计 - 成功场景")
    void shouldGetCategoryStatusStatisticsSuccessfully() {
        // Given
        List<Map<String, Object>> expectedStats = createStatusStatistics();
        given(categoryMapper.getCategoryStatusStatistics()).willReturn(expectedStats);
        
        // When
        List<Map<String, Object>> result = categoryService.getCategoryStatusStatistics();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("status")).isEqualTo(1);
        assertThat(result.get(0).get("count")).isEqualTo(80L);
    }

    @Test
    @DisplayName("获取分类层级统计 - 成功场景")
    void shouldGetCategoryLevelStatisticsSuccessfully() {
        // Given
        List<Map<String, Object>> expectedStats = createLevelStatistics();
        given(categoryMapper.getCategoryLevelStatistics()).willReturn(expectedStats);
        
        // When
        List<Map<String, Object>> result = categoryService.getCategoryLevelStatistics();
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).get("level")).isEqualTo(1);
        assertThat(result.get(0).get("count")).isEqualTo(10L);
    }

    // ==================== 搜索功能测试 ====================

    @Test
    @DisplayName("根据关键词搜索分类 - 成功场景")
    void shouldSearchCategoriesSuccessfully() {
        // Given
        String keyword = "测试";
        List<Category> expectedCategories = CategoryTestDataFactory.createCategoryList(3);
        given(categoryMapper.searchByKeyword(keyword)).willReturn(expectedCategories);
        
        // When
        List<Category> result = categoryService.searchCategories(keyword);
        
        // Then
        assertThat(result).hasSize(3);
        then(categoryMapper).should().searchByKeyword(keyword);
    }

    // ==================== 热门分类测试 ====================

    @Test
    @DisplayName("获取热门分类 - 成功场景")
    void shouldGetPopularCategoriesSuccessfully() {
        // Given
        int limit = 10;
        List<Category> expectedCategories = CategoryTestDataFactory.createCategoryList(limit);
        given(categoryMapper.findPopularCategories(limit)).willReturn(expectedCategories);
        
        // When
        List<Category> result = categoryService.getPopularCategories(limit);
        
        // Then
        assertThat(result).hasSize(limit);
        then(categoryMapper).should().findPopularCategories(limit);
    }

    @Test
    @DisplayName("获取最新分类 - 成功场景")
    void shouldGetLatestCategoriesSuccessfully() {
        // Given
        int limit = 5;
        List<Category> expectedCategories = CategoryTestDataFactory.createCategoryList(limit);
        given(categoryMapper.findLatestCategories(limit)).willReturn(expectedCategories);
        
        // When
        List<Category> result = categoryService.getLatestCategories(limit);
        
        // Then
        assertThat(result).hasSize(limit);
        then(categoryMapper).should().findLatestCategories(limit);
    }

    // ==================== 报告生成测试 ====================

    @Test
    @DisplayName("生成分类报告 - 成功场景")
    void shouldGenerateCategoryReportSuccessfully() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        Map<String, Object> expectedReport = createReportData();
        
        given(categoryMapper.generateReport(startDate, endDate)).willReturn(expectedReport);
        
        // When
        Map<String, Object> result = categoryService.generateCategoryReport(startDate, endDate);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get("totalCategories")).isEqualTo(150L);
        assertThat(result.get("activeCategories")).isEqualTo(120L);
    }

    // ==================== 缓存管理测试 ====================

    @Test
    @DisplayName("刷新分类缓存 - 成功场景")
    void shouldRefreshCategoryCacheSuccessfully() {
        // Given
        Long categoryId = 1L;
        
        // When
        boolean result = categoryService.refreshCategoryCache(categoryId);
        
        // Then
        assertThat(result).isTrue();
    }

    // ==================== 辅助方法 ====================

    private List<Map<String, Object>> createStatusStatistics() {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        Map<String, Object> enabledStat = new HashMap<>();
        enabledStat.put("status", 1);
        enabledStat.put("count", 80L);
        enabledStat.put("percentage", 80.0);
        stats.add(enabledStat);
        
        Map<String, Object> disabledStat = new HashMap<>();
        disabledStat.put("status", 0);
        disabledStat.put("count", 20L);
        disabledStat.put("percentage", 20.0);
        stats.add(disabledStat);
        
        return stats;
    }

    private List<Map<String, Object>> createLevelStatistics() {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        for (int level = 1; level <= 3; level++) {
            Map<String, Object> levelStat = new HashMap<>();
            levelStat.put("level", level);
            levelStat.put("count", (long) (10 * level));
            stats.add(levelStat);
        }
        
        return stats;
    }

    private Map<String, Object> createReportData() {
        Map<String, Object> report = new HashMap<>();
        report.put("totalCategories", 150L);
        report.put("activeCategories", 120L);
        report.put("inactiveCategories", 30L);
        report.put("averageLevel", 2.5);
        report.put("maxLevel", 5);
        return report;
    }
}
