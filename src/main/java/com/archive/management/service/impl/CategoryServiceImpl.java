package com.archive.management.service.impl;

import com.archive.management.entity.Category;
import com.archive.management.mapper.CategoryMapper;
import com.archive.management.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分类业务服务实现类
 * 实现档案分类管理的核心业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;
    
    @Override
    public CategoryMapper getBaseMapper() {
        return categoryMapper;
    }

    // 缓存键前缀
    private static final String CACHE_PREFIX = "category:";
    private static final String CACHE_KEY_CATEGORY = CACHE_PREFIX + "detail:";
    private static final String CACHE_KEY_LIST = CACHE_PREFIX + "list:";
    private static final String CACHE_KEY_TREE = CACHE_PREFIX + "tree";

    /**
     * 创建分类
     * @param category 分类信息
     * @return 创建的分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        log.info("开始创建分类，名称: {}", category.getCategoryName());
        
        try {
            // 验证分类数据
            validateCategoryData(category);
            
            // 设置默认值
            category.setDeleted(0);  // 0-未删除
            category.setCreateTime(LocalDateTime.now());
            category.setUpdateTime(LocalDateTime.now());
            
            // 生成分类编码（如果未提供）
            if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
                category.setCategoryCode(generateCategoryCode(category.getCategoryName()));
            }
            
            // 检查分类编码是否重复
            if (existsByCategoryCode(category.getCategoryCode())) {
                throw new RuntimeException("分类编码已存在: " + category.getCategoryCode());
            }
            
            // 设置层级和路径
            if (category.getParentId() != null && category.getParentId() > 0) {
                Category parent = getCategoryById(category.getParentId());
                category.setCategoryLevel(parent.getCategoryLevel() + 1);
                category.setPath(parent.getCategoryPath() + "/" + category.getCategoryCode());
            } else {
                category.setParentId(0L);
                category.setCategoryLevel(1);
                category.setPath("/" + category.getCategoryCode());
            }
            
            // 保存分类
            boolean saved = save(category);
            if (!saved) {
                throw new RuntimeException("分类保存失败");
            }
            
            log.info("分类创建成功，ID: {}, 编码: {}", category.getCategoryId(), category.getCategoryCode());
            return category;
            
        } catch (Exception e) {
            log.error("创建分类失败，名称: {}", category.getCategoryName(), e);
            throw new RuntimeException("创建分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类信息
     */
    @Override
    @Cacheable(value = "categories", key = "#id")
    public Category getCategoryById(Long id) {
        log.debug("获取分类详情，ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        Category category = getById(id);
        if (category == null || (category.getDeleted() != null && category.getDeleted() == 1)) {
            throw new RuntimeException("分类不存在或已删除，ID: " + id);
        }
        
        return category;
    }

    /**
     * 根据分类编码获取分类
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    @Override
    @Cacheable(value = "categories", key = "'code:' + #categoryCode")
    public Category getCategoryByCode(String categoryCode) {
        log.debug("根据编码获取分类，编码: {}", categoryCode);
        
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("分类编码不能为空");
        }
        
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_code", categoryCode)
                   .eq("deleted", 0);
        
        Category category = getOne(queryWrapper);
        if (category == null) {
            throw new RuntimeException("分类不存在，编码: " + categoryCode);
        }
        
        return category;
    }

    /**
     * 更新分类信息
     * @param category 分类信息
     * @return 更新后的分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Category category) {
        log.info("开始更新分类，ID: {}", category.getCategoryId());
        
        try {
            // 验证分类存在
            Category existingCategory = getCategoryById(category.getCategoryId());
            
            // 验证分类数据
            validateCategoryData(category);
            
            // 检查分类编码是否重复（排除自身）
            if (!existingCategory.getCategoryCode().equals(category.getCategoryCode()) 
                && existsByCategoryCode(category.getCategoryCode())) {
                throw new RuntimeException("分类编码已存在: " + category.getCategoryCode());
            }
            
            // 更新时间
            category.setUpdateTime(LocalDateTime.now());
            
            // 如果父分类发生变化，需要重新计算层级和路径
            if (!Objects.equals(existingCategory.getParentId(), category.getParentId())) {
                updateCategoryHierarchy(category);
            }
            
            // 保存更新
            boolean updated = updateById(category);
            if (!updated) {
                throw new RuntimeException("分类更新失败");
            }
            
            log.info("分类更新成功，ID: {}", category.getCategoryId());
            return category;
            
        } catch (Exception e) {
            log.error("更新分类失败，ID: {}", category.getCategoryId(), e);
            throw new RuntimeException("更新分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除分类（软删除）
     * @param id 分类ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "categories", allEntries = true)
    public boolean deleteCategory(Long id, Long deletedBy) {
        log.info("开始删除分类，ID: {}, 删除人: {}", id, deletedBy);
        
        try {
            // 验证分类存在
            Category category = getCategoryById(id);
            
            // 检查是否有子分类
            if (hasChildCategories(id)) {
                throw new RuntimeException("该分类下存在子分类，无法删除");
            }
            
            // 检查是否有关联的档案
            if (hasRelatedArchives(id)) {
                throw new RuntimeException("该分类下存在档案，无法删除");
            }
            
            // 执行软删除
            UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                        .set("deleted", true)
                        .set("deleted_by", deletedBy)
                        .set("deleted_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now());
            
            boolean deleted = update(updateWrapper);
            if (deleted) {
                log.info("分类删除成功，ID: {}", id);
            } else {
                log.warn("分类删除失败，ID: {}", id);
            }
            
            return deleted;
            
        } catch (Exception e) {
            log.error("删除分类失败，ID: {}", id, e);
            throw new RuntimeException("删除分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取分类树结构
     * @return 分类树
     */
    @Override
    @Cacheable(value = "categories", key = "'tree'")
    public List<Map<String, Object>> getCategoryTree() {
        log.debug("获取分类树结构");
        
        try {
            // 获取所有分类
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleted", 0)
                       .orderByAsc("parent_id", "sort_order", "category_id");
            
            List<Category> allCategories = list(queryWrapper);
            
            // 构建树结构
            return buildCategoryTreeMap(allCategories);
            
        } catch (Exception e) {
            log.error("获取分类树结构失败", e);
            throw new RuntimeException("获取分类树结构失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查分类编码是否存在
     * @param categoryCode 分类编码
     * @return 是否存在
     */
    @Override
    public boolean existsByCategoryCode(String categoryCode) {
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            return false;
        }
        
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_code", categoryCode)
                   .eq("deleted", 0);
        
        return count(queryWrapper) > 0;
    }

    /**
     * 生成分类编码
     * @param categoryName 分类名称
     * @return 分类编码
     */
    @Override
    public String generateCategoryCode(String categoryName) {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return "CAT" + System.currentTimeMillis();
            }
            
            // 简单的拼音转换（这里使用简化版本）
            String code = categoryName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "")
                                    .toUpperCase();
            
            if (code.length() > 10) {
                code = code.substring(0, 10);
            }
            
            // 如果编码已存在，添加数字后缀
            String originalCode = code;
            int suffix = 1;
            while (existsByCategoryCode(code)) {
                code = originalCode + suffix;
                suffix++;
            }
            
            return code;
            
        } catch (Exception e) {
            log.error("生成分类编码失败", e);
            return "CAT" + System.currentTimeMillis();
        }
    }

    /**
     * 验证分类数据
     * @param category 分类信息
     */
    private void validateCategoryData(Category category) {
        List<String> errors = new ArrayList<>();
        
        // 验证必填字段
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            errors.add("分类名称不能为空");
        } else if (category.getCategoryName().length() > 100) {
            errors.add("分类名称长度不能超过100个字符");
        }
        
        if (category.getCategoryCode() != null && category.getCategoryCode().length() > 50) {
            errors.add("分类编码长度不能超过50个字符");
        }
        
        // 验证父分类是否存在
        if (category.getParentId() != null && category.getParentId() > 0) {
            Category parent = getById(category.getParentId());
            if (parent == null || (parent.getDeleted() != null && parent.getDeleted() == 1)) {
                errors.add("指定的父分类不存在");
            }
        }
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("分类数据验证失败: " + String.join(", ", errors));
        }
    }

    /**
     * 更新分类层级结构
     * @param category 分类信息
     */
    private void updateCategoryHierarchy(Category category) {
        if (category.getParentId() != null && category.getParentId() > 0) {
            Category parent = getCategoryById(category.getParentId());
            category.setCategoryLevel(parent.getCategoryLevel() + 1);
            category.setPath(parent.getCategoryPath() + "/" + category.getCategoryCode());
        } else {
            category.setParentId(0L);
            category.setCategoryLevel(1);
            category.setPath("/" + category.getCategoryCode());
        }
    }

    /**
     * 检查是否有子分类
     * @param parentId 父分类ID
     * @return 是否有子分类
     */
    private boolean hasChildCategories(Long parentId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId)
                   .eq("deleted", 0);
        
        return count(queryWrapper) > 0;
    }

    /**
     * 检查是否有关联的档案
     * @param categoryId 分类ID
     * @return 是否有关联档案
     */
    private boolean hasRelatedArchives(Long categoryId) {
        // 这里需要查询档案表，暂时返回false
        // 实际实现中需要注入ArchiveMapper并查询
        return false;
    }

    /**
     * 构建分类树结构（Map格式）
     * @param categories 所有分类
     * @return 分类树
     */
    private List<Map<String, Object>> buildCategoryTreeMap(List<Category> categories) {
        return buildCategoryTreeMap(categories, 0L);
    }

    /**
     * 构建分类树结构（Map格式，递归方法）
     * @param categories 所有分类
     * @param parentId 父分类ID
     * @return 分类树
     */
    private List<Map<String, Object>> buildCategoryTreeMap(List<Category> categories, Long parentId) {
        return categories.stream()
                .filter(category -> Objects.equals(category.getParentId(), parentId))
                .map(category -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", category.getCategoryId());
                    node.put("categoryId", category.getCategoryId());
                    node.put("categoryName", category.getCategoryName());
                    node.put("categoryCode", category.getCategoryCode());
                    node.put("parentId", category.getParentId());
                    node.put("categoryLevel", category.getCategoryLevel());
                    node.put("defaultSecurityLevel", category.getDefaultSecurityLevel());
                    node.put("retentionPeriod", category.getRetentionPeriod());
                    node.put("businessType", category.getBusinessType());
                    node.put("sortOrder", category.getSortOrder());
                    node.put("status", category.getStatus());
                    node.put("remark", category.getRemark());
                    
                    // 递归构建子节点
                    List<Map<String, Object>> children = buildCategoryTreeMap(categories, category.getCategoryId());
                    node.put("children", children);
                    
                    return node;
                })
                .collect(Collectors.toList());
    }
}