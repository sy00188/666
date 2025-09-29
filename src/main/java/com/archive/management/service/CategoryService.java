package com.archive.management.service;

import com.archive.management.entity.Category;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分类业务服务接口
 * 定义分类管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface CategoryService {

    /**
     * 创建分类
     * @param category 分类信息
     * @return 创建的分类
     */
    Category createCategory(Category category);

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类信息
     */
    Category getCategoryById(Long id);

    /**
     * 根据分类编码获取分类
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    Category getCategoryByCode(String categoryCode);

    /**
     * 根据分类名称获取分类
     * @param categoryName 分类名称
     * @return 分类信息
     */
    Category getCategoryByName(String categoryName);

    /**
     * 更新分类信息
     * @param category 分类信息
     * @return 更新后的分类
     */
    Category updateCategory(Category category);

    /**
     * 删除分类（软删除）
     * @param id 分类ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Long id, Long deletedBy);

    /**
     * 批量删除分类（软删除）
     * @param ids 分类ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteCategories(List<Long> ids, Long deletedBy);

    /**
     * 启用分类
     * @param id 分类ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableCategory(Long id, Long updatedBy);

    /**
     * 禁用分类
     * @param id 分类ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableCategory(Long id, Long updatedBy);

    /**
     * 批量更新分类状态
     * @param ids 分类ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateCategoryStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 检查分类编码是否存在
     * @param categoryCode 分类编码
     * @return 是否存在
     */
    boolean existsByCategoryCode(String categoryCode);

    /**
     * 检查分类名称是否存在
     * @param categoryName 分类名称
     * @return 是否存在
     */
    boolean existsByCategoryName(String categoryName);

    /**
     * 分页查询分类
     * @param page 分页参数
     * @param categoryCode 分类编码（可选）
     * @param categoryName 分类名称（可选）
     * @param status 状态（可选）
     * @param parentId 父分类ID（可选）
     * @return 分页结果
     */
    IPage<Category> findCategoriesWithPagination(Page<Category> page, String categoryCode, 
                                                String categoryName, Integer status, Long parentId);

    /**
     * 根据状态查找分类列表
     * @param status 状态
     * @return 分类列表
     */
    List<Category> findCategoriesByStatus(Integer status);

    /**
     * 根据父分类ID查找子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> findCategoriesByParentId(Long parentId);

    /**
     * 获取所有启用的分类
     * @return 分类列表
     */
    List<Category> findEnabledCategories();

    /**
     * 获取根分类列表（顶级分类）
     * @return 分类列表
     */
    List<Category> findRootCategories();

    /**
     * 根据层级查找分类列表
     * @param level 层级
     * @return 分类列表
     */
    List<Category> findCategoriesByLevel(Integer level);

    /**
     * 统计分类总数
     * @return 总数
     */
    long countCategories();

    /**
     * 根据状态统计分类数量
     * @param status 状态
     * @return 数量
     */
    long countCategoriesByStatus(Integer status);

    /**
     * 根据父分类统计子分类数量
     * @param parentId 父分类ID
     * @return 数量
     */
    long countCategoriesByParentId(Long parentId);

    /**
     * 根据层级统计分类数量
     * @param level 层级
     * @return 数量
     */
    long countCategoriesByLevel(Integer level);

    /**
     * 获取分类状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getCategoryStatusStatistics();

    /**
     * 获取分类层级统计
     * @return 层级统计结果
     */
    List<Map<String, Object>> getCategoryLevelStatistics();

    /**
     * 根据关键词搜索分类
     * @param keyword 关键词
     * @return 分类列表
     */
    List<Category> searchCategories(String keyword);

    /**
     * 构建分类树结构
     * @param categories 分类列表
     * @return 分类树
     */
    List<Map<String, Object>> buildCategoryTree(List<Category> categories);

    /**
     * 获取完整分类树
     * @return 分类树
     */
    List<Map<String, Object>> getCategoryTree();

    /**
     * 获取启用分类树
     * @return 启用分类树
     */
    List<Map<String, Object>> getEnabledCategoryTree();

    /**
     * 获取指定分类的子树
     * @param parentId 父分类ID
     * @return 分类子树
     */
    List<Map<String, Object>> getCategorySubTree(Long parentId);

    /**
     * 验证分类层级关系
     * @param parentId 父分类ID
     * @param childId 子分类ID
     * @return 是否存在循环依赖
     */
    boolean validateCategoryHierarchy(Long parentId, Long childId);

    /**
     * 获取分类的所有子分类ID
     * @param parentId 父分类ID
     * @return 子分类ID列表
     */
    List<Long> getAllChildCategoryIds(Long parentId);

    /**
     * 获取分类的所有父分类ID
     * @param childId 子分类ID
     * @return 父分类ID列表
     */
    List<Long> getAllParentCategoryIds(Long childId);

    /**
     * 获取分类路径
     * @param categoryId 分类ID
     * @return 分类路径字符串
     */
    String getCategoryPath(Long categoryId);

    /**
     * 获取分类层级
     * @param categoryId 分类ID
     * @return 分类层级
     */
    int getCategoryLevel(Long categoryId);

    /**
     * 移动分类到新的父分类下
     * @param categoryId 分类ID
     * @param newParentId 新父分类ID
     * @param updatedBy 更新人ID
     * @return 是否移动成功
     */
    boolean moveCategory(Long categoryId, Long newParentId, Long updatedBy);

    /**
     * 批量移动分类
     * @param categoryIds 分类ID列表
     * @param newParentId 新父分类ID
     * @param updatedBy 更新人ID
     * @return 移动成功的数量
     */
    int batchMoveCategories(List<Long> categoryIds, Long newParentId, Long updatedBy);

    /**
     * 复制分类
     * @param sourceCategoryId 源分类ID
     * @param newCategoryCode 新分类编码
     * @param newCategoryName 新分类名称
     * @param parentId 父分类ID
     * @param createdBy 创建人ID
     * @return 新创建的分类
     */
    Category copyCategory(Long sourceCategoryId, String newCategoryCode, 
                         String newCategoryName, Long parentId, Long createdBy);

    /**
     * 批量复制分类
     * @param sourceCategoryIds 源分类ID列表
     * @param targetParentId 目标父分类ID
     * @param createdBy 创建人ID
     * @return 复制成功的数量
     */
    int batchCopyCategories(List<Long> sourceCategoryIds, Long targetParentId, Long createdBy);

    /**
     * 合并分类
     * @param sourceCategoryId 源分类ID
     * @param targetCategoryId 目标分类ID
     * @param mergedBy 合并人ID
     * @return 是否合并成功
     */
    boolean mergeCategories(Long sourceCategoryId, Long targetCategoryId, Long mergedBy);

    /**
     * 拆分分类
     * @param sourceCategoryId 源分类ID
     * @param newCategoryInfo 新分类信息列表
     * @param splitBy 拆分人ID
     * @return 拆分结果
     */
    List<Category> splitCategory(Long sourceCategoryId, List<Map<String, Object>> newCategoryInfo, Long splitBy);

    /**
     * 导出分类数据
     * @param ids 分类ID列表（可选，为空则导出所有）
     * @return 分类列表
     */
    List<Category> exportCategories(List<Long> ids);

    /**
     * 批量导入分类
     * @param categories 分类列表
     * @param createdBy 创建人ID
     * @return 导入成功的数量
     */
    int importCategories(List<Category> categories, Long createdBy);

    /**
     * 检查分类是否可以删除
     * @param categoryId 分类ID
     * @return 是否可以删除
     */
    boolean canDeleteCategory(Long categoryId);

    /**
     * 获取分类依赖信息
     * @param categoryId 分类ID
     * @return 依赖信息
     */
    Map<String, Object> getCategoryDependencies(Long categoryId);

    /**
     * 批量更新分类排序
     * @param categoryOrders 分类排序映射（分类ID -> 排序值）
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateCategorySort(Map<Long, Integer> categoryOrders, Long updatedBy);

    /**
     * 获取分类使用统计
     * @param days 统计天数
     * @return 使用统计结果
     */
    List<Map<String, Object>> getCategoryUsageStatistics(int days);

    /**
     * 获取热门分类
     * @param limit 数量限制
     * @return 分类列表
     */
    List<Category> getPopularCategories(int limit);

    /**
     * 获取最新分类
     * @param limit 数量限制
     * @return 分类列表
     */
    List<Category> getLatestCategories(int limit);

    /**
     * 获取分类档案数量统计
     * @return 统计结果
     */
    List<Map<String, Object>> getCategoryArchiveCountStatistics();

    /**
     * 获取空分类列表（没有档案的分类）
     * @return 分类列表
     */
    List<Category> getEmptyCategories();

    /**
     * 清理空分类
     * @param deletedBy 删除人ID
     * @return 清理的数量
     */
    int cleanupEmptyCategories(Long deletedBy);

    /**
     * 重新计算分类层级
     * @param categoryId 分类ID（可选，为空则重新计算所有）
     * @return 重新计算的数量
     */
    int recalculateCategoryLevels(Long categoryId);

    /**
     * 重新计算分类路径
     * @param categoryId 分类ID（可选，为空则重新计算所有）
     * @return 重新计算的数量
     */
    int recalculateCategoryPaths(Long categoryId);

    /**
     * 生成分类编码
     * @param parentId 父分类ID
     * @param categoryName 分类名称
     * @return 分类编码
     */
    String generateCategoryCode(Long parentId, String categoryName);

    /**
     * 验证分类数据
     * @param category 分类信息
     * @return 验证结果
     */
    Map<String, Object> validateCategoryData(Category category);

    /**
     * 获取分类访问记录
     * @param categoryId 分类ID
     * @param days 天数
     * @return 访问记录
     */
    List<Map<String, Object>> getCategoryAccessLogs(Long categoryId, int days);

    /**
     * 设置分类图标
     * @param categoryId 分类ID
     * @param iconUrl 图标URL
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setCategoryIcon(Long categoryId, String iconUrl, Long updatedBy);

    /**
     * 设置分类颜色
     * @param categoryId 分类ID
     * @param color 颜色值
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setCategoryColor(Long categoryId, String color, Long updatedBy);

    /**
     * 获取分类配置
     * @param categoryId 分类ID
     * @return 配置信息
     */
    Map<String, Object> getCategoryConfig(Long categoryId);

    /**
     * 更新分类配置
     * @param categoryId 分类ID
     * @param config 配置信息
     * @param updatedBy 更新人ID
     * @return 是否更新成功
     */
    boolean updateCategoryConfig(Long categoryId, Map<String, Object> config, Long updatedBy);

    /**
     * 生成分类报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    Map<String, Object> generateCategoryReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 刷新分类缓存
     * @param categoryId 分类ID（可选，为空则刷新所有）
     * @return 是否刷新成功
     */
    boolean refreshCategoryCache(Long categoryId);

    /**
     * 同步分类数据
     * @param source 同步源
     * @param updatedBy 更新人ID
     * @return 同步成功的数量
     */
    int syncCategories(String source, Long updatedBy);

    /**
     * 备份分类数据
     * @param categoryIds 分类ID列表
     * @param backupPath 备份路径
     * @return 备份结果
     */
    Map<String, Object> backupCategories(List<Long> categoryIds, String backupPath);

    /**
     * 恢复分类数据
     * @param backupFile 备份文件
     * @param restoreOptions 恢复选项
     * @return 恢复结果
     */
    Map<String, Object> restoreCategories(String backupFile, Map<String, Object> restoreOptions);
}