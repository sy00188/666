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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * CategoryService 树结构操作测试类
 * 测试分类的树形结构相关操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("CategoryService 树结构操作测试")
class CategoryServiceTreeTest extends BaseTest {

    @MockBean
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryMapper);
    }

    // ==================== 树结构构建测试 ====================

    @Test
    @DisplayName("构建分类树 - 成功场景")
    void shouldBuildCategoryTreeSuccessfully() {
        // Given
        List<Category> categories = createTestCategoryHierarchy();
        given(categoryMapper.findEnabledCategories()).willReturn(categories);
        
        // When
        List<Map<String, Object>> result = categoryService.getCategoryTree();
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2); // 2个根节点
        
        Map<String, Object> firstRoot = result.get(0);
        assertThat(firstRoot.get("categoryCode")).isEqualTo("ROOT_001");
        assertThat(firstRoot.get("children")).isNotNull();
    }

    @Test
    @DisplayName("获取分类路径 - 成功场景")
    void shouldGetCategoryPathSuccessfully() {
        // Given
        Long categoryId = 3L;
        setupCategoryHierarchyMocks();
        
        // When
        String result = categoryService.getCategoryPath(categoryId);
        
        // Then
        assertThat(result).isEqualTo("根分类/子分类/孙分类");
    }

    @Test
    @DisplayName("验证分类层级关系 - 无循环依赖")
    void shouldValidateHierarchyWithoutCircularDependency() {
        // Given
        Long parentId = 1L;
        Long childId = 2L;
        setupNonCircularHierarchy();
        
        // When
        boolean result = categoryService.validateCategoryHierarchy(parentId, childId);
        
        // Then
        assertThat(result).isFalse(); // false表示无循环依赖
    }

    @Test
    @DisplayName("验证分类层级关系 - 存在循环依赖")
    void shouldDetectCircularDependency() {
        // Given
        Long parentId = 1L;
        Long childId = 2L;
        setupCircularHierarchy();
        
        // When
        boolean result = categoryService.validateCategoryHierarchy(parentId, childId);
        
        // Then
        assertThat(result).isTrue(); // true表示存在循环依赖
    }

    // ==================== 移动分类测试 ====================

    @Test
    @DisplayName("移动分类 - 成功场景")
    void shouldMoveCategorySuccessfully() {
        // Given
        Long categoryId = 2L;
        Long newParentId = 3L;
        Long updatedBy = 100L;
        
        Category category = CategoryTestDataFactory.createCategory("CHILD_001", "子分类");
        category.setId(categoryId);
        category.setParentId(1L);
        category.setLevel(2);
        
        given(categoryMapper.selectById(categoryId)).willReturn(category);
        given(categoryMapper.selectById(newParentId)).willReturn(createParentCategory());
        given(categoryMapper.updateById(any(Category.class))).willReturn(1);
        
        // When
        boolean result = categoryService.moveCategory(categoryId, newParentId, updatedBy);
        
        // Then
        assertThat(result).isTrue();
        then(categoryMapper).should().updateById(any(Category.class));
    }

    // ==================== 辅助方法 ====================

    private List<Category> createTestCategoryHierarchy() {
        List<Category> categories = new ArrayList<>();
        
        // 根分类1
        Category root1 = CategoryTestDataFactory.createCategory("ROOT_001", "根分类1");
        root1.setId(1L);
        root1.setParentId(0L);
        root1.setLevel(1);
        categories.add(root1);
        
        // 根分类2
        Category root2 = CategoryTestDataFactory.createCategory("ROOT_002", "根分类2");
        root2.setId(2L);
        root2.setParentId(0L);
        root2.setLevel(1);
        categories.add(root2);
        
        // 子分类
        Category child1 = CategoryTestDataFactory.createCategory("CHILD_001", "子分类1");
        child1.setId(3L);
        child1.setParentId(1L);
        child1.setLevel(2);
        categories.add(child1);
        
        return categories;
    }

    private void setupCategoryHierarchyMocks() {
        Category grandParent = CategoryTestDataFactory.createCategory("ROOT", "根分类");
        grandParent.setId(1L);
        grandParent.setParentId(0L);
        
        Category parent = CategoryTestDataFactory.createCategory("PARENT", "子分类");
        parent.setId(2L);
        parent.setParentId(1L);
        
        Category child = CategoryTestDataFactory.createCategory("CHILD", "孙分类");
        child.setId(3L);
        child.setParentId(2L);
        
        given(categoryMapper.selectById(1L)).willReturn(grandParent);
        given(categoryMapper.selectById(2L)).willReturn(parent);
        given(categoryMapper.selectById(3L)).willReturn(child);
    }

    private void setupNonCircularHierarchy() {
        // 设置正常的层级关系，无循环依赖
        given(categoryMapper.getAllParentCategoryIds(2L)).willReturn(Arrays.asList(0L));
    }

    private void setupCircularHierarchy() {
        // 设置循环依赖的层级关系
        given(categoryMapper.getAllParentCategoryIds(2L)).willReturn(Arrays.asList(1L, 0L));
    }

    private Category createParentCategory() {
        Category parent = CategoryTestDataFactory.createCategory("PARENT", "父分类");
        parent.setId(3L);
        parent.setLevel(1);
        return parent;
    }
}
