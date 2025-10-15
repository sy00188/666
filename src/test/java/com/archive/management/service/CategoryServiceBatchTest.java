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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * CategoryService 批量操作测试类
 * 测试分类的批量操作功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("CategoryService 批量操作测试")
class CategoryServiceBatchTest extends BaseTest {

    @MockBean
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryMapper);
    }

    // ==================== 批量状态更新测试 ====================

    @Test
    @DisplayName("批量更新分类状态 - 成功场景")
    void shouldBatchUpdateCategoryStatusSuccessfully() {
        // Given
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L);
        Integer newStatus = 1;
        Long updatedBy = 100L;
        
        setupCategoriesForBatchUpdate(categoryIds);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        int result = categoryService.batchUpdateCategoryStatus(categoryIds, newStatus, updatedBy);
        
        // Then
        assertThat(result).isEqualTo(3);
        then(categoryMapper).should(times(3)).updateById(any(Category.class));
    }

    @Test
    @DisplayName("批量移动分类 - 成功场景")
    void shouldBatchMoveCategoriesSuccessfully() {
        // Given
        List<Long> categoryIds = Arrays.asList(1L, 2L);
        Long newParentId = 3L;
        Long updatedBy = 100L;
        
        setupCategoriesForBatchMove(categoryIds, newParentId);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        int result = categoryService.batchMoveCategories(categoryIds, newParentId, updatedBy);
        
        // Then
        assertThat(result).isEqualTo(2);
        then(categoryMapper).should(times(2)).updateById(any(Category.class));
    }

    // ==================== 批量导入导出测试 ====================

    @Test
    @DisplayName("批量导入分类 - 成功场景")
    void shouldImportCategoriesSuccessfully() {
        // Given
        List<Category> categories = CategoryTestDataFactory.createCategoryList(5);
        Long createdBy = 100L;
        
        given(categoryMapper.existsByCategoryCode(any())).willReturn(false);
        given(categoryMapper.insert(any(Category.class))).willReturn(1);
        
        // When
        int result = categoryService.importCategories(categories, createdBy);
        
        // Then
        assertThat(result).isEqualTo(5);
        then(categoryMapper).should(times(5)).insert(any(Category.class));
    }

    @Test
    @DisplayName("导出分类数据 - 成功场景")
    void shouldExportCategoriesSuccessfully() {
        // Given
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L);
        List<Category> expectedCategories = CategoryTestDataFactory.createCategoryList(3);
        
        given(categoryMapper.selectBatchIds(categoryIds)).willReturn(expectedCategories);
        
        // When
        List<Category> result = categoryService.exportCategories(categoryIds);
        
        // Then
        assertThat(result).hasSize(3);
        then(categoryMapper).should().selectBatchIds(categoryIds);
    }

    // ==================== 批量复制测试 ====================

    @Test
    @DisplayName("批量复制分类 - 成功场景")
    void shouldBatchCopyCategoriesSuccessfully() {
        // Given
        List<Long> sourceCategoryIds = Arrays.asList(1L, 2L);
        Long targetParentId = 3L;
        Long createdBy = 100L;
        
        setupCategoriesForBatchCopy(sourceCategoryIds);
        given(categoryMapper.insert(any(Category.class))).willReturn(1);
        
        // When
        int result = categoryService.batchCopyCategories(sourceCategoryIds, targetParentId, createdBy);
        
        // Then
        assertThat(result).isEqualTo(2);
        then(categoryMapper).should(times(2)).insert(any(Category.class));
    }

    // ==================== 辅助方法 ====================

    private void setupCategoriesForBatchUpdate(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Category category = CategoryTestDataFactory.createCategory("TEST" + (i + 1), "分类" + (i + 1));
            category.setId(categoryIds.get(i));
            given(categoryMapper.selectById(categoryIds.get(i))).willReturn(category);
        }
    }

    private void setupCategoriesForBatchMove(List<Long> categoryIds, Long newParentId) {
        Category newParent = CategoryTestDataFactory.createCategory("PARENT", "新父分类");
        newParent.setId(newParentId);
        newParent.setLevel(1);
        given(categoryMapper.selectById(newParentId)).willReturn(newParent);
        
        for (Long categoryId : categoryIds) {
            Category category = CategoryTestDataFactory.createCategory("CHILD" + categoryId, "子分类" + categoryId);
            category.setId(categoryId);
            category.setLevel(2);
            given(categoryMapper.selectById(categoryId)).willReturn(category);
        }
    }

    private void setupCategoriesForBatchCopy(List<Long> sourceCategoryIds) {
        for (Long categoryId : sourceCategoryIds) {
            Category category = CategoryTestDataFactory.createCategory("SOURCE" + categoryId, "源分类" + categoryId);
            category.setId(categoryId);
            given(categoryMapper.selectById(categoryId)).willReturn(category);
        }
    }
}
