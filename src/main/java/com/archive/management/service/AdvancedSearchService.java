package com.archive.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 高级搜索服务类
 * 提供全文搜索、高级过滤器、搜索结果排序等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Service
public class AdvancedSearchService {

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MonitoringService monitoringService;

    /**
     * 高级搜索档案
     */
    public Page<Archive> advancedSearchArchives(Map<String, Object> searchCriteria, Pageable pageable) {
        try {
            // 记录搜索操作
            monitoringService.recordDocumentSearched();
            
            // 构建搜索条件
            Specification<Archive> spec = buildArchiveSearchSpecification(searchCriteria);
            
            // 执行搜索
            return archiveRepository.findAll(spec, pageable);
        } catch (Exception e) {
            monitoringService.recordError("advanced_search", e.getMessage());
            throw new RuntimeException("高级搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 高级搜索用户
     */
    public Page<User> advancedSearchUsers(Map<String, Object> searchCriteria, Pageable pageable) {
        try {
            // 记录搜索操作
            monitoringService.recordDocumentSearched();
            
            // 构建搜索条件
            Specification<User> spec = buildUserSearchSpecification(searchCriteria);
            
            // 执行搜索
            return userRepository.findAll(spec, pageable);
        } catch (Exception e) {
            monitoringService.recordError("advanced_search", e.getMessage());
            throw new RuntimeException("高级搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 全文搜索档案
     */
    public List<Archive> fullTextSearchArchives(String keyword, int limit) {
        try {
            // 记录搜索操作
            monitoringService.recordDocumentSearched();
            
            // 执行全文搜索
            List<Archive> results = archiveRepository.findByTitleContainingOrDescriptionContaining(
                keyword, keyword);
            
            // 限制结果数量
            if (results.size() > limit) {
                results = results.subList(0, limit);
            }
            
            return results;
        } catch (Exception e) {
            monitoringService.recordError("fulltext_search", e.getMessage());
            throw new RuntimeException("全文搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 全文搜索用户
     */
    public List<User> fullTextSearchUsers(String keyword, int limit) {
        try {
            // 记录搜索操作
            monitoringService.recordDocumentSearched();
            
            // 执行全文搜索
            List<User> results = userRepository.findByUsernameContainingOrRealNameContainingOrEmailContaining(
                keyword, keyword, keyword);
            
            // 限制结果数量
            if (results.size() > limit) {
                results = results.subList(0, limit);
            }
            
            return results;
        } catch (Exception e) {
            monitoringService.recordError("fulltext_search", e.getMessage());
            throw new RuntimeException("全文搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 智能搜索建议
     */
    public List<String> getSearchSuggestions(String keyword, String type) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            if ("archive".equals(type)) {
                // 档案搜索建议
                List<Archive> archives = archiveRepository.findTop10ByTitleContaining(keyword);
                for (Archive archive : archives) {
                    suggestions.add(archive.getTitle());
                }
            } else if ("user".equals(type)) {
                // 用户搜索建议
                List<User> users = userRepository.findTop10ByUsernameContaining(keyword);
                for (User user : users) {
                    suggestions.add(user.getUsername());
                }
            }
        } catch (Exception e) {
            monitoringService.recordError("search_suggestions", e.getMessage());
        }
        
        return suggestions;
    }

    /**
     * 搜索统计
     */
    public Map<String, Object> getSearchStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 总档案数
            long totalArchives = archiveRepository.count();
            statistics.put("totalArchives", totalArchives);
            
            // 活跃档案数
            long activeArchives = archiveRepository.countByStatus(1);
            statistics.put("activeArchives", activeArchives);
            
            // 总用户数
            long totalUsers = userRepository.count();
            statistics.put("totalUsers", totalUsers);
            
            // 活跃用户数
            long activeUsers = userRepository.countByStatus(1);
            statistics.put("activeUsers", activeUsers);
            
            // 最近创建的档案数
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            long recentArchives = archiveRepository.countByCreateTimeAfter(oneWeekAgo);
            statistics.put("recentArchives", recentArchives);
            
        } catch (Exception e) {
            monitoringService.recordError("search_statistics", e.getMessage());
        }
        
        return statistics;
    }

    /**
     * 构建档案搜索条件
     */
    private Specification<Archive> buildArchiveSearchSpecification(Map<String, Object> criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 标题搜索
            if (criteria.containsKey("title") && StringUtils.hasText(criteria.get("title").toString())) {
                predicates.add(cb.like(cb.lower(root.get("title")), 
                    "%" + criteria.get("title").toString().toLowerCase() + "%"));
            }
            
            // 编号搜索
            if (criteria.containsKey("code") && StringUtils.hasText(criteria.get("code").toString())) {
                predicates.add(cb.like(cb.lower(root.get("code")), 
                    "%" + criteria.get("code").toString().toLowerCase() + "%"));
            }
            
            // 描述搜索
            if (criteria.containsKey("description") && StringUtils.hasText(criteria.get("description").toString())) {
                predicates.add(cb.like(cb.lower(root.get("description")), 
                    "%" + criteria.get("description").toString().toLowerCase() + "%"));
            }
            
            // 状态过滤
            if (criteria.containsKey("status") && criteria.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), criteria.get("status")));
            }
            
            // 创建时间范围
            if (criteria.containsKey("createTimeStart") && criteria.get("createTimeStart") != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), 
                    (LocalDateTime) criteria.get("createTimeStart")));
            }
            
            if (criteria.containsKey("createTimeEnd") && criteria.get("createTimeEnd") != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), 
                    (LocalDateTime) criteria.get("createTimeEnd")));
            }
            
            // 创建用户过滤
            if (criteria.containsKey("createUserId") && criteria.get("createUserId") != null) {
                predicates.add(cb.equal(root.get("createUser").get("id"), criteria.get("createUserId")));
            }
            
            // 关键词全文搜索
            if (criteria.containsKey("keyword") && StringUtils.hasText(criteria.get("keyword").toString())) {
                String keyword = criteria.get("keyword").toString().toLowerCase();
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), "%" + keyword + "%");
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), "%" + keyword + "%");
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), "%" + keyword + "%");
                predicates.add(cb.or(titlePredicate, descriptionPredicate, codePredicate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 构建用户搜索条件
     */
    private Specification<User> buildUserSearchSpecification(Map<String, Object> criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户名搜索
            if (criteria.containsKey("username") && StringUtils.hasText(criteria.get("username").toString())) {
                predicates.add(cb.like(cb.lower(root.get("username")), 
                    "%" + criteria.get("username").toString().toLowerCase() + "%"));
            }
            
            // 真实姓名搜索
            if (criteria.containsKey("realName") && StringUtils.hasText(criteria.get("realName").toString())) {
                predicates.add(cb.like(cb.lower(root.get("realName")), 
                    "%" + criteria.get("realName").toString().toLowerCase() + "%"));
            }
            
            // 邮箱搜索
            if (criteria.containsKey("email") && StringUtils.hasText(criteria.get("email").toString())) {
                predicates.add(cb.like(cb.lower(root.get("email")), 
                    "%" + criteria.get("email").toString().toLowerCase() + "%"));
            }
            
            // 手机号搜索
            if (criteria.containsKey("phone") && StringUtils.hasText(criteria.get("phone").toString())) {
                predicates.add(cb.like(root.get("phone"), "%" + criteria.get("phone").toString() + "%"));
            }
            
            // 状态过滤
            if (criteria.containsKey("status") && criteria.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), criteria.get("status")));
            }
            
            // 创建时间范围
            if (criteria.containsKey("createTimeStart") && criteria.get("createTimeStart") != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), 
                    (LocalDateTime) criteria.get("createTimeStart")));
            }
            
            if (criteria.containsKey("createTimeEnd") && criteria.get("createTimeEnd") != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), 
                    (LocalDateTime) criteria.get("createTimeEnd")));
            }
            
            // 关键词全文搜索
            if (criteria.containsKey("keyword") && StringUtils.hasText(criteria.get("keyword").toString())) {
                String keyword = criteria.get("keyword").toString().toLowerCase();
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), "%" + keyword + "%");
                Predicate realNamePredicate = cb.like(cb.lower(root.get("realName")), "%" + keyword + "%");
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), "%" + keyword + "%");
                predicates.add(cb.or(usernamePredicate, realNamePredicate, emailPredicate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 搜索历史记录
     */
    public void recordSearchHistory(String keyword, String type, Long userId) {
        try {
            // 这里可以实现搜索历史记录功能
            // 可以存储到数据库或缓存中
            System.out.println("记录搜索历史: " + keyword + ", 类型: " + type + ", 用户: " + userId);
        } catch (Exception e) {
            monitoringService.recordError("search_history", e.getMessage());
        }
    }

    /**
     * 获取搜索历史
     */
    public List<String> getSearchHistory(Long userId, String type) {
        List<String> history = new ArrayList<>();
        
        try {
            // 这里可以从数据库或缓存中获取搜索历史
            // 示例实现
            history.add("示例搜索1");
            history.add("示例搜索2");
            history.add("示例搜索3");
        } catch (Exception e) {
            monitoringService.recordError("search_history", e.getMessage());
        }
        
        return history;
    }

    /**
     * 清除搜索历史
     */
    public void clearSearchHistory(Long userId, String type) {
        try {
            // 这里可以清除用户的搜索历史
            System.out.println("清除搜索历史: 用户 " + userId + ", 类型: " + type);
        } catch (Exception e) {
            monitoringService.recordError("search_history", e.getMessage());
        }
    }
}
