package com.archive.management.controller;

import com.archive.management.entity.Category;
import com.archive.management.service.CategoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分类管理控制器
 * 提供分类相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "分类管理", description = "分类管理相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建新分类")
    @PreAuthorize("hasAuthority('category:create')")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "分类创建成功",
                "data", createdCategory
            ));
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类", description = "根据ID获取分类信息")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            if (category != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取分类成功",
                    "data", category
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据分类编码获取分类
     */
    @GetMapping("/code/{categoryCode}")
    @Operation(summary = "根据分类编码获取分类", description = "根据分类编码获取分类信息")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> getCategoryByCode(
            @Parameter(description = "分类编码") @PathVariable @NotBlank String categoryCode) {
        try {
            Category category = categoryService.getCategoryByCode(categoryCode);
            if (category != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取分类成功",
                    "data", category
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("根据分类编码获取分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新分类信息")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody Category category) {
        try {
            category.setId(id);
            Category updatedCategory = categoryService.updateCategory(category);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "分类更新成功",
                "data", updatedCategory
            ));
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除分类（软删除）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "软删除分类")
    @PreAuthorize("hasAuthority('category:delete')")
    public ResponseEntity<Map<String, Object>> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            boolean deleted = categoryService.deleteCategory(id, deletedBy);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "分类删除失败"
                ));
            }
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分类", description = "批量软删除分类")
    @PreAuthorize("hasAuthority('category:delete')")
    public ResponseEntity<Map<String, Object>> batchDeleteCategories(
            @Parameter(description = "分类ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "删除人ID") @RequestParam @NotNull @Positive Long deletedBy) {
        try {
            int deletedCount = categoryService.batchDeleteCategories(ids, deletedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量删除分类成功",
                "data", Map.of("deletedCount", deletedCount)
            ));
        } catch (Exception e) {
            log.error("批量删除分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量删除分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 启用分类
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用分类", description = "启用分类")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<Map<String, Object>> enableCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            boolean enabled = categoryService.enableCategory(id, operatedBy);
            if (enabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类启用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "分类启用失败"
                ));
            }
        } catch (Exception e) {
            log.error("启用分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "启用分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 禁用分类
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用分类", description = "禁用分类")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<Map<String, Object>> disableCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            boolean disabled = categoryService.disableCategory(id, operatedBy);
            if (disabled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类禁用成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "分类禁用失败"
                ));
            }
        } catch (Exception e) {
            log.error("禁用分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "禁用分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量更新分类状态
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新分类状态", description = "批量更新分类状态")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<Map<String, Object>> batchUpdateCategoryStatus(
            @Parameter(description = "分类ID列表") @RequestBody @NotEmpty List<Long> ids,
            @Parameter(description = "状态") @RequestParam @NotNull Integer status,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            int updatedCount = categoryService.batchUpdateCategoryStatus(ids, status, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量更新分类状态成功",
                "data", Map.of("updatedCount", updatedCount)
            ));
        } catch (Exception e) {
            log.error("批量更新分类状态失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量更新分类状态失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页查询分类
     */
    @GetMapping
    @Operation(summary = "分页查询分类", description = "分页查询分类列表")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> findCategoriesWithPagination(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类编码") @RequestParam(required = false) String categoryCode,
            @Parameter(description = "分类名称") @RequestParam(required = false) String categoryName,
            @Parameter(description = "父分类ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "分类级别") @RequestParam(required = false) Integer level,
            @Parameter(description = "创建人ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Page<Category> page = new Page<>(current, size);
            IPage<Category> result = categoryService.findCategoriesWithPagination(page, categoryCode, categoryName, 
                parentId, status, level, createdBy, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "查询分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("分页查询分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "查询分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据父分类获取子分类
     */
    @GetMapping("/parent/{parentId}")
    @Operation(summary = "根据父分类获取子分类", description = "根据父分类ID获取子分类列表")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> getCategoriesByParent(
            @Parameter(description = "父分类ID") @PathVariable @NotNull @Positive Long parentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "包含禁用分类") @RequestParam(defaultValue = "false") boolean includeDisabled) {
        try {
            Page<Category> page = new Page<>(current, size);
            IPage<Category> result = categoryService.getCategoriesByParent(parentId, includeDisabled, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取子分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("根据父分类获取子分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取子分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取根分类
     */
    @GetMapping("/root")
    @Operation(summary = "获取根分类", description = "获取根分类列表")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> getRootCategories(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "包含禁用分类") @RequestParam(defaultValue = "false") boolean includeDisabled) {
        try {
            Page<Category> page = new Page<>(current, size);
            IPage<Category> result = categoryService.getRootCategories(includeDisabled, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取根分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取根分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取根分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分类统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取分类统计信息", description = "获取分类统计信息")
    @PreAuthorize("hasAuthority('category:statistics')")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        try {
            long totalCategories = categoryService.countCategories();
            long enabledCategories = categoryService.countCategoriesByStatus(1);
            long disabledCategories = categoryService.countCategoriesByStatus(0);
            long rootCategories = categoryService.countRootCategories();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", Map.of(
                    "totalCategories", totalCategories,
                    "enabledCategories", enabledCategories,
                    "disabledCategories", disabledCategories,
                    "rootCategories", rootCategories
                )
            ));
        } catch (Exception e) {
            log.error("获取分类统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分类状态统计
     */
    @GetMapping("/statistics/status")
    @Operation(summary = "获取分类状态统计", description = "获取分类状态统计")
    @PreAuthorize("hasAuthority('category:statistics')")
    public ResponseEntity<Map<String, Object>> getStatusStatistics() {
        try {
            List<Map<String, Object>> statistics = categoryService.getStatusStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取状态统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取分类状态统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取状态统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分类级别统计
     */
    @GetMapping("/statistics/level")
    @Operation(summary = "获取分类级别统计", description = "获取分类级别统计")
    @PreAuthorize("hasAuthority('category:statistics')")
    public ResponseEntity<Map<String, Object>> getLevelStatistics() {
        try {
            List<Map<String, Object>> statistics = categoryService.getLevelStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取级别统计成功",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("获取分类级别统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取级别统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 搜索分类
     */
    @GetMapping("/search")
    @Operation(summary = "搜索分类", description = "根据关键词搜索分类")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> searchCategories(
            @Parameter(description = "关键词") @RequestParam @NotBlank String keyword,
            @Parameter(description = "搜索字段") @RequestParam(required = false) List<String> searchFields,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Category> page = new Page<>(current, size);
            IPage<Category> result = categoryService.searchCategories(keyword, searchFields, null, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "搜索分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("搜索分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "搜索分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 高级搜索分类
     */
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索分类", description = "高级搜索分类")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> advancedSearchCategories(
            @Parameter(description = "搜索条件") @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Category> page = new Page<>(current, size);
            IPage<Category> result = categoryService.advancedSearchCategories(searchCriteria, page);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "高级搜索分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("高级搜索分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "高级搜索分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 构建分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "构建分类树", description = "构建分类树结构")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> buildCategoryTree(
            @Parameter(description = "根分类ID") @RequestParam(required = false) Long rootId,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "10") int maxDepth,
            @Parameter(description = "包含禁用分类") @RequestParam(defaultValue = "false") boolean includeDisabled) {
        try {
            List<Map<String, Object>> tree = categoryService.buildCategoryTree(rootId, maxDepth, includeDisabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "构建分类树成功",
                "data", tree
            ));
        } catch (Exception e) {
            log.error("构建分类树失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "构建分类树失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分类路径
     */
    @GetMapping("/{id}/path")
    @Operation(summary = "获取分类路径", description = "获取分类的完整路径")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<Map<String, Object>> getCategoryPath(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "路径分隔符") @RequestParam(defaultValue = "/") String separator) {
        try {
            String path = categoryService.getCategoryPath(id, separator);
            List<Category> pathCategories = categoryService.getCategoryPathList(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取分类路径成功",
                "data", Map.of(
                    "path", path,
                    "pathCategories", pathCategories
                )
            ));
        } catch (Exception e) {
            log.error("获取分类路径失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取分类路径失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 移动分类
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "移动分类", description = "移动分类到新的父分类下")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<Map<String, Object>> moveCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "新父分类ID") @RequestParam(required = false) Long newParentId,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            boolean moved = categoryService.moveCategory(id, newParentId, operatedBy);
            if (moved) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "分类移动成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "分类移动失败"
                ));
            }
        } catch (Exception e) {
            log.error("移动分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "移动分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 复制分类
     */
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制分类", description = "复制分类")
    @PreAuthorize("hasAuthority('category:create')")
    public ResponseEntity<Map<String, Object>> copyCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "新父分类ID") @RequestParam(required = false) Long newParentId,
            @Parameter(description = "新分类名称") @RequestParam(required = false) String newCategoryName,
            @Parameter(description = "复制子分类") @RequestParam(defaultValue = "false") boolean copyChildren,
            @Parameter(description = "操作人ID") @RequestParam @NotNull @Positive Long operatedBy) {
        try {
            Category copiedCategory = categoryService.copyCategory(id, newParentId, newCategoryName, copyChildren, operatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "分类复制成功",
                "data", copiedCategory
            ));
        } catch (Exception e) {
            log.error("复制分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "复制分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出分类
     */
    @PostMapping("/export")
    @Operation(summary = "导出分类", description = "导出分类数据")
    @PreAuthorize("hasAuthority('category:export')")
    public ResponseEntity<Map<String, Object>> exportCategories(
            @Parameter(description = "分类ID列表") @RequestBody(required = false) List<Long> categoryIds,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format,
            @Parameter(description = "包含字段") @RequestParam(required = false) List<String> includeFields,
            @Parameter(description = "包含子分类") @RequestParam(defaultValue = "false") boolean includeChildren) {
        try {
            Map<String, Object> result = categoryService.exportCategories(categoryIds, format, includeFields, includeChildren);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导出分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导出分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导出分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导入分类
     */
    @PostMapping("/import")
    @Operation(summary = "导入分类", description = "导入分类数据")
    @PreAuthorize("hasAuthority('category:import')")
    public ResponseEntity<Map<String, Object>> importCategories(
            @Parameter(description = "分类数据") @RequestBody List<Category> categories,
            @Parameter(description = "导入模式") @RequestParam(defaultValue = "merge") String importMode,
            @Parameter(description = "导入人ID") @RequestParam @NotNull @Positive Long importedBy) {
        try {
            Map<String, Object> result = categoryService.importCategories(categories, importMode, importedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "导入分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("导入分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "导入分类失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 同步分类
     */
    @PostMapping("/sync")
    @Operation(summary = "同步分类", description = "同步分类数据")
    @PreAuthorize("hasAuthority('category:sync')")
    public ResponseEntity<Map<String, Object>> syncCategories(
            @Parameter(description = "同步源") @RequestParam @NotBlank String syncSource,
            @Parameter(description = "同步配置") @RequestBody(required = false) Map<String, Object> syncConfig,
            @Parameter(description = "同步人ID") @RequestParam @NotNull @Positive Long syncedBy) {
        try {
            Map<String, Object> result = categoryService.syncCategories(syncSource, syncConfig, syncedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "同步分类成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("同步分类失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "同步分类失败: " + e.getMessage()
            ));
        }
    }
}