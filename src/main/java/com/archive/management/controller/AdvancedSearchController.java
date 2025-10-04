package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.service.AdvancedSearchService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 高级搜索控制器
 * 提供全文搜索、高级过滤器、搜索建议等API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "高级搜索", description = "高级搜索相关接口")
public class AdvancedSearchController {

    private final AdvancedSearchService advancedSearchService;

    /**
     * 高级搜索档案
     */
    @PostMapping("/archives/advanced")
    @Operation(summary = "高级搜索档案", description = "根据复杂条件搜索档案")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<ApiResponse<IPage<Archive>>> advancedSearchArchives(
            @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Archive> page = new Page<>(current, size);
            IPage<Archive> results = advancedSearchService.advancedSearchArchives(searchCriteria, page);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            log.error("高级搜索档案失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 高级搜索用户
     */
    @PostMapping("/users/advanced")
    @Operation(summary = "高级搜索用户", description = "根据复杂条件搜索用户")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<IPage<User>>> advancedSearchUsers(
            @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<User> page = new Page<>(current, size);
            IPage<User> results = advancedSearchService.advancedSearchUsers(searchCriteria, page);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            log.error("高级搜索用户失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 全文搜索档案
     */
    @GetMapping("/archives/fulltext")
    @Operation(summary = "全文搜索档案", description = "根据关键词全文搜索档案")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<ApiResponse<List<Archive>>> fullTextSearchArchives(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "结果数量限制") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Archive> results = advancedSearchService.fullTextSearchArchives(keyword, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            log.error("全文搜索档案失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 全文搜索用户
     */
    @GetMapping("/users/fulltext")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> fullTextSearchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<User> results = advancedSearchService.fullTextSearchUsers(keyword, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam String type) {
        try {
            List<String> suggestions = advancedSearchService.getSearchSuggestions(keyword, type);
            return ResponseEntity.ok(ApiResponse.success(suggestions, "获取建议成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取建议失败: " + e.getMessage()));
        }
    }

    /**
     * 获取搜索统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchStatistics() {
        try {
            Map<String, Object> statistics = advancedSearchService.getSearchStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取统计成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }

    /**
     * 记录搜索历史
     */
    @PostMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> recordSearchHistory(
            @RequestBody Map<String, Object> request) {
        try {
            String keyword = (String) request.get("keyword");
            String type = (String) request.get("type");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            advancedSearchService.recordSearchHistory(keyword, type, userId);
            return ResponseEntity.ok(ApiResponse.success("记录成功", "搜索历史已记录"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取搜索历史
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<String>>> getSearchHistory(
            @RequestParam Long userId,
            @RequestParam String type) {
        try {
            List<String> history = advancedSearchService.getSearchHistory(userId, type);
            return ResponseEntity.ok(ApiResponse.success(history, "获取历史成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取历史失败: " + e.getMessage()));
        }
    }

    /**
     * 清除搜索历史
     */
    @DeleteMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> clearSearchHistory(
            @RequestParam Long userId,
            @RequestParam String type) {
        try {
            advancedSearchService.clearSearchHistory(userId, type);
            return ResponseEntity.ok(ApiResponse.success("清除成功", "搜索历史已清除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("清除失败: " + e.getMessage()));
        }
    }

    /**
     * 快速搜索档案
     */
    @GetMapping("/archives/quick")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<Archive>>> quickSearchArchives(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Archive> results = advancedSearchService.fullTextSearchArchives(keyword, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "快速搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("快速搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 快速搜索用户
     */
    @GetMapping("/users/quick")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> quickSearchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<User> results = advancedSearchService.fullTextSearchUsers(keyword, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "快速搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("快速搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索档案（简单条件）
     */
    @GetMapping("/archives/simple")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<Archive>>> simpleSearchArchives(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 构建搜索条件
            Map<String, Object> searchCriteria = new HashMap<>();
            if (title != null) searchCriteria.put("title", title);
            if (code != null) searchCriteria.put("code", code);
            if (description != null) searchCriteria.put("description", description);
            if (status != null) searchCriteria.put("status", status);
            
            // 构建分页
            Pageable pageable = PageRequest.of(page, size);
            
            // 执行搜索
            Page<Archive> results = advancedSearchService.advancedSearchArchives(searchCriteria, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索用户（简单条件）
     */
    @GetMapping("/users/simple")
    @Operation(summary = "简单搜索用户", description = "根据简单条件搜索用户")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<IPage<User>>> simpleSearchUsers(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            // 构建搜索条件
            Map<String, Object> searchCriteria = new HashMap<>();
            if (username != null) searchCriteria.put("username", username);
            if (realName != null) searchCriteria.put("realName", realName);
            if (email != null) searchCriteria.put("email", email);
            if (status != null) searchCriteria.put("status", status);
            
            // 构建分页
            Page<User> page = new Page<>(current, size);
            
            // 执行搜索
            IPage<User> results = advancedSearchService.advancedSearchUsers(searchCriteria, page);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            log.error("简单搜索用户失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门搜索关键词", description = "获取热门搜索关键词")
    @PreAuthorize("hasAuthority('search:view')")
    public ResponseEntity<ApiResponse<List<String>>> getHotSearchKeywords(
            @Parameter(description = "搜索类型") @RequestParam String type) {
        try {
            List<String> hotKeywords = advancedSearchService.getHotSearchKeywords(type);
            return ResponseEntity.ok(ApiResponse.success(hotKeywords, "获取热门关键词成功"));
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("获取热门关键词失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索相似档案
     */
    @GetMapping("/archives/similar/{archiveId}")
    @Operation(summary = "搜索相似档案", description = "根据档案ID搜索相似档案")
    @PreAuthorize("hasAuthority('archive:read')")
    public ResponseEntity<ApiResponse<List<Archive>>> findSimilarArchives(
            @Parameter(description = "档案ID") @PathVariable Long archiveId,
            @Parameter(description = "结果数量限制") @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Archive> similarArchives = advancedSearchService.findSimilarArchives(archiveId, limit);
            return ResponseEntity.ok(ApiResponse.success(similarArchives, "搜索相似档案成功"));
        } catch (Exception e) {
            log.error("搜索相似档案失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索相似档案失败: " + e.getMessage()));
        }
    }
}
