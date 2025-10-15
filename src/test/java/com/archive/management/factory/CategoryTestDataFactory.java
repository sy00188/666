package com.archive.management.factory;

import com.archive.management.entity.Category;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类测试数据工厂
 * 提供便捷的测试数据创建方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class CategoryTestDataFactory {

    /**
     * 创建默认分类
     */
    public static Category defaultCategory() {
        return createCategory("DEFAULT", "默认分类");
    }

    /**
     * 创建分类
     */
    public static Category createCategory(String code, String name) {
        Category category = new Category();
        category.setCategoryCode(code);
        category.setCategoryName(name);
        category.setDescription("描述：" + name);
        category.setStatus(1);
        category.setLevel(1);
        category.setParentId(0L);
        category.setSortOrder(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        return category;
    }

    /**
     * 创建分类列表
     */
    public static List<Category> createCategoryList(int count) {
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            categories.add(createCategory(
                "TEST_" + String.format("%03d", i),
                "测试分类_" + i
            ));
        }
        return categories;
    }

    /**
     * 创建已启用的分类
     */
    public static Category enabledCategory() {
        Category category = defaultCategory();
        category.setStatus(1);
        return category;
    }

    /**
     * 创建已禁用的分类
     */
    public static Category disabledCategory() {
        Category category = defaultCategory();
        category.setCategoryCode("DISABLED");
        category.setCategoryName("禁用分类");
        category.setStatus(0);
        return category;
    }

    /**
     * 创建子分类
     */
    public static Category childCategory(Long parentId, int level) {
        Category category = defaultCategory();
        category.setParentId(parentId);
        category.setLevel(level);
        category.setCategoryCode("CHILD_" + parentId);
        category.setCategoryName("子分类_" + parentId);
        return category;
    }
}

