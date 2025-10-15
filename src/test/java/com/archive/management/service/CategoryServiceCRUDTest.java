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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * CategoryService CRUD操作测试类
 * 测试分类的创建、读取、更新、删除等基础操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("CategoryService CRUD操作测试")
class CategoryServiceCRUDTest extends BaseTest {

    @MockBean
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryMapper);
    }

    // ==================== 创建分类测试 ====================

    @Test
    @DisplayName("创建分类 - 成功场景")
    void shouldCreateCategorySuccessfully() {
        // Given
        Category category = CategoryTestDataFactory.createCategory("TEST001", "测试分类");
        given(categoryMapper.insert(any(Category.class))).willReturn(1);
        given(categoryMapper.existsByCategoryCode("TEST001")).willReturn(false);
        given(categoryMapper.existsByCategoryName("测试分类")).willReturn(false);
        
        // When
        Category result = categoryService.createCategory(category);
        
        // Then
        then(categoryMapper).should().insert(any(Category.class));
        assertThat(result).isNotNull();
        assertThat(result.getCategoryCode()).isEqualTo("TEST001");
        assertThat(result.getCategoryName()).isEqualTo("测试分类");
        assertThat(result.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("创建分类 - 分类编码已存在")
    void shouldThrowExceptionWhenCategoryCodeExists() {
        // Given
        Category category = CategoryTestDataFactory.createCategory("EXIST001", "已存在分类");
        given(categoryMapper.existsByCategoryCode("EXIST001")).willReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(category))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("分类编码已存在");
    }

    @Test
    @DisplayName("创建分类 - 分类名称已存在")
    void shouldThrowExceptionWhenCategoryNameExists() {
        // Given
        Category category = CategoryTestDataFactory.createCategory("NEW001", "已存在名称");
        given(categoryMapper.existsByCategoryCode("NEW001")).willReturn(false);
        given(categoryMapper.existsByCategoryName("已存在名称")).willReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(category))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("分类名称已存在");
    }

    @Test
    @DisplayName("创建分类 - 参数为空")
    void shouldThrowExceptionWhenCategoryIsNull() {
        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("分类信息不能为空");
    }

    // ==================== 查询分类测试 ====================

    @Test
    @DisplayName("根据ID获取分类 - 成功场景")
    void shouldGetCategoryByIdSuccessfully() {
        // Given
        Long categoryId = 1L;
        Category expectedCategory = CategoryTestDataFactory.createCategory("TEST001", "测试分类");
        expectedCategory.setId(categoryId);
        given(categoryMapper.selectById(categoryId)).willReturn(expectedCategory);
        
        // When
        Category result = categoryService.getCategoryById(categoryId);
        
        // Then
        then(categoryMapper).should().selectById(categoryId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getCategoryCode()).isEqualTo("TEST001");
    }

    @Test
    @DisplayName("根据ID获取分类 - ID为空")
    void shouldReturnNullWhenIdIsNull() {
        // When
        Category result = categoryService.getCategoryById(null);
        
        // Then
        assertThat(result).isNull();
        then(categoryMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("根据分类编码获取分类 - 成功场景")
    void shouldGetCategoryByCodeSuccessfully() {
        // Given
        String categoryCode = "TEST001";
        Category expectedCategory = CategoryTestDataFactory.createCategory(categoryCode, "测试分类");
        given(categoryMapper.findByCategoryCode(categoryCode)).willReturn(expectedCategory);
        
        // When
        Category result = categoryService.getCategoryByCode(categoryCode);
        
        // Then
        then(categoryMapper).should().findByCategoryCode(categoryCode);
        assertThat(result).isNotNull();
        assertThat(result.getCategoryCode()).isEqualTo(categoryCode);
    }

    @Test
    @DisplayName("根据分类名称获取分类 - 成功场景")
    void shouldGetCategoryByNameSuccessfully() {
        // Given
        String categoryName = "测试分类";
        Category expectedCategory = CategoryTestDataFactory.createCategory("TEST001", categoryName);
        given(categoryMapper.findByCategoryName(categoryName)).willReturn(expectedCategory);
        
        // When
        Category result = categoryService.getCategoryByName(categoryName);
        
        // Then
        then(categoryMapper).should().findByCategoryName(categoryName);
        assertThat(result).isNotNull();
        assertThat(result.getCategoryName()).isEqualTo(categoryName);
    }

    // ==================== 更新分类测试 ====================

    @Test
    @DisplayName("更新分类 - 成功场景")
    void shouldUpdateCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        Category existingCategory = CategoryTestDataFactory.createCategory("TEST001", "原始分类");
        existingCategory.setId(categoryId);
        
        Category updateCategory = CategoryTestDataFactory.createCategory("TEST001", "更新后分类");
        updateCategory.setId(categoryId);
        updateCategory.setDescription("更新后的描述");
        
        given(categoryMapper.selectById(categoryId)).willReturn(existingCategory);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        Category result = categoryService.updateCategory(updateCategory);
        
        // Then
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        then(categoryMapper).should().updateById(categoryCaptor.capture());
        
        Category capturedCategory = categoryCaptor.getValue();
        assertThat(capturedCategory.getCategoryName()).isEqualTo("更新后分类");
        assertThat(capturedCategory.getDescription()).isEqualTo("更新后的描述");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("更新分类 - 分类不存在")
    void shouldThrowExceptionWhenUpdateNonExistentCategory() {
        // Given
        Long categoryId = 999L;
        Category updateCategory = CategoryTestDataFactory.createCategory("TEST001", "不存在分类");
        updateCategory.setId(categoryId);
        
        given(categoryMapper.selectById(categoryId)).willReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(updateCategory))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("分类不存在");
    }

    // ==================== 删除分类测试 ====================

    @Test
    @DisplayName("删除分类 - 成功场景")
    void shouldDeleteCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        Long deletedBy = 100L;
        Category existingCategory = CategoryTestDataFactory.createCategory("TEST001", "待删除分类");
        existingCategory.setId(categoryId);
        
        given(categoryMapper.selectById(categoryId)).willReturn(existingCategory);
        given(categoryMapper.countByParentId(categoryId)).willReturn(0L);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        boolean result = categoryService.deleteCategory(categoryId, deletedBy);
        
        // Then
        assertThat(result).isTrue();
        then(categoryMapper).should().updateById(any(Category.class));
    }

    @Test
    @DisplayName("删除分类 - 存在子分类")
    void shouldThrowExceptionWhenCategoryHasChildren() {
        // Given
        Long categoryId = 1L;
        Long deletedBy = 100L;
        Category existingCategory = CategoryTestDataFactory.createCategory("TEST001", "父分类");
        existingCategory.setId(categoryId);
        
        given(categoryMapper.selectById(categoryId)).willReturn(existingCategory);
        given(categoryMapper.countByParentId(categoryId)).willReturn(2L);
        
        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId, deletedBy))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("存在子分类，无法删除");
    }

    @Test
    @DisplayName("删除分类 - ID为空")
    void shouldThrowExceptionWhenDeleteWithNullId() {
        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(null, 100L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("分类ID不能为空");
    }

    // ==================== 批量删除分类测试 ====================

    @Test
    @DisplayName("批量删除分类 - 成功场景")
    void shouldBatchDeleteCategoriesSuccessfully() {
        // Given
        List<Long> categoryIds = Arrays.asList(1L, 2L, 3L);
        Long deletedBy = 100L;
        
        Category category1 = CategoryTestDataFactory.createCategory("TEST001", "分类1");
        category1.setId(1L);
        Category category2 = CategoryTestDataFactory.createCategory("TEST002", "分类2");
        category2.setId(2L);
        Category category3 = CategoryTestDataFactory.createCategory("TEST003", "分类3");
        category3.setId(3L);
        
        given(categoryMapper.selectById(1L)).willReturn(category1);
        given(categoryMapper.selectById(2L)).willReturn(category2);
        given(categoryMapper.selectById(3L)).willReturn(category3);
        given(categoryMapper.countByParentId(any())).willReturn(0L);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        int result = categoryService.batchDeleteCategories(categoryIds, deletedBy);
        
        // Then
        assertThat(result).isEqualTo(3);
        then(categoryMapper).should(times(3)).updateById(any(Category.class));
    }

    @Test
    @DisplayName("批量删除分类 - ID列表为空")
    void shouldThrowExceptionWhenBatchDeleteWithEmptyIds() {
        // When & Then
        assertThatThrownBy(() -> categoryService.batchDeleteCategories(Arrays.asList(), 100L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("分类ID列表不能为空");
    }

    // ==================== 状态管理测试 ====================

    @Test
    @DisplayName("启用分类 - 成功场景")
    void shouldEnableCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        Long updatedBy = 100L;
        Category existingCategory = CategoryTestDataFactory.disabledCategory();
        existingCategory.setId(categoryId);
        
        given(categoryMapper.selectById(categoryId)).willReturn(existingCategory);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        boolean result = categoryService.enableCategory(categoryId, updatedBy);
        
        // Then
        assertThat(result).isTrue();
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        then(categoryMapper).should().updateById(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("禁用分类 - 成功场景")
    void shouldDisableCategorySuccessfully() {
        // Given
        Long categoryId = 1L;
        Long updatedBy = 100L;
        Category existingCategory = CategoryTestDataFactory.enabledCategory();
        existingCategory.setId(categoryId);
        
        given(categoryMapper.selectById(categoryId)).willReturn(existingCategory);
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        boolean result = categoryService.disableCategory(categoryId, updatedBy);
        
        // Then
        assertThat(result).isTrue();
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        then(categoryMapper).should().updateById(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getStatus()).isEqualTo(0);
    }
}
