package com.archive.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.archive.management.service.AdvancedSearchService;
import com.archive.management.dto.ApiResponse;
import com.archive.management.entity.Archive;
import com.archive.management.entity.User;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 高级搜索控制器
 * 提供全文搜索、高级过滤器、搜索建议等API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@RestController
@RequestMapping("/api/search")
public class AdvancedSearchController {

    @Autowired
    private AdvancedSearchService advancedSearchService;

    /**
     * 高级搜索档案
     */
    @PostMapping("/archives/advanced")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<Archive>>> advancedSearchArchives(
            @RequestBody Map<String, Object> searchCriteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            // 构建分页和排序
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 执行高级搜索
            Page<Archive> results = advancedSearchService.advancedSearchArchives(searchCriteria, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 高级搜索用户
     */
    @PostMapping("/users/advanced")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> advancedSearchUsers(
            @RequestBody Map<String, Object> searchCriteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            // 构建分页和排序
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 执行高级搜索
            Page<User> results = advancedSearchService.advancedSearchUsers(searchCriteria, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 全文搜索档案
     */
    @GetMapping("/archives/fulltext")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<Archive>>> fullTextSearchArchives(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Archive> results = advancedSearchService.fullTextSearchArchives(keyword, limit);
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<User>>> simpleSearchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 构建搜索条件
            Map<String, Object> searchCriteria = new HashMap<>();
            if (username != null) searchCriteria.put("username", username);
            if (realName != null) searchCriteria.put("realName", realName);
            if (email != null) searchCriteria.put("email", email);
            if (status != null) searchCriteria.put("status", status);
            
            // 构建分页
            Pageable pageable = PageRequest.of(page, size);
            
            // 执行搜索
            Page<User> results = advancedSearchService.advancedSearchUsers(searchCriteria, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(results, "搜索成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }
}
