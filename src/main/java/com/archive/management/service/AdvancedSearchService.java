package com.archive.management.service;

import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 高级搜索服务类
 * 提供全文搜索、高级过滤器、搜索结果排序等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedSearchService {

    private final ArchiveMapper archiveMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MonitoringService monitoringService;

    /**
     * 高级搜索档案
     */
    public IPage<Archive> advancedSearchArchives(Map<String, Object> searchCriteria, Page<Archive> page) {
        try {
            // 记录搜索操作
            monitoringService.recordDocumentSearched();
            
            // 构建搜索条件
            QueryWrapper<Archive> queryWrapper = buildArchiveSearchQuery(searchCriteria);
            
            // 执行搜索
            return archiveMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            log.error("高级搜索档案失败", e);
            monitoringService.recordError("advanced_search", e.getMessage());
            throw new RuntimeException("高级搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 高级搜索用户
     */
    public IPage<User> advancedSearchUsers(Map<String, Object> searchCriteria, Page<User> page) {
        try {
            // 记录搜索操作
            monitoringService.recordDocumentSearched();
            
            // 构建搜索条件
            QueryWrapper<User> queryWrapper = buildUserSearchQuery(searchCriteria);
            
            // 执行搜索
            return userMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            log.error("高级搜索用户失败", e);
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
            
            // 构建全文搜索条件
            QueryWrapper<Archive> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("description", keyword)
                .or()
                .like("archive_number", keyword)
            );
            queryWrapper.eq("deleted", 0); // 只搜索未删除的档案
            queryWrapper.last("LIMIT " + limit);
            
            // 执行搜索
            return archiveMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("全文搜索档案失败", e);
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
            
            // 构建全文搜索条件
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword)
                .or()
                .like("real_name", keyword)
                .or()
                .like("email", keyword)
            );
            queryWrapper.eq("deleted", 0); // 只搜索未删除的用户
            queryWrapper.last("LIMIT " + limit);
            
            // 执行搜索
            return userMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("全文搜索用户失败", e);
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
            String cacheKey = "search:suggestions:" + type + ":" + keyword;
            List<String> cachedSuggestions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedSuggestions != null) {
                return cachedSuggestions;
            }
            
            if ("archive".equals(type)) {
                // 档案搜索建议
                QueryWrapper<Archive> queryWrapper = new QueryWrapper<>();
                queryWrapper.like("title", keyword);
                queryWrapper.eq("deleted", 0);
                queryWrapper.last("LIMIT 10");
                queryWrapper.select("title");
                
                List<Archive> archives = archiveMapper.selectList(queryWrapper);
                for (Archive archive : archives) {
                    suggestions.add(archive.getTitle());
                }
            } else if ("user".equals(type)) {
                // 用户搜索建议
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.like("username", keyword);
                queryWrapper.eq("deleted", 0);
                queryWrapper.last("LIMIT 10");
                queryWrapper.select("username");
                
                List<User> users = userMapper.selectList(queryWrapper);
                for (User user : users) {
                    suggestions.add(user.getUsername());
                }
            }
            
            // 缓存搜索结果
            if (!suggestions.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, suggestions, 5, TimeUnit.MINUTES);
            }
            
        } catch (Exception e) {
            log.error("获取搜索建议失败", e);
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
            String cacheKey = "search:statistics";
            Map<String, Object> cachedStats = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedStats != null) {
                return cachedStats;
            }
            
            // 总档案数
            QueryWrapper<Archive> archiveWrapper = new QueryWrapper<>();
            archiveWrapper.eq("deleted", 0);
            long totalArchives = archiveMapper.selectCount(archiveWrapper);
            statistics.put("totalArchives", totalArchives);
            
            // 活跃档案数
            QueryWrapper<Archive> activeArchiveWrapper = new QueryWrapper<>();
            activeArchiveWrapper.eq("deleted", 0).eq("status", 1);
            long activeArchives = archiveMapper.selectCount(activeArchiveWrapper);
            statistics.put("activeArchives", activeArchives);
            
            // 总用户数
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("deleted", 0);
            long totalUsers = userMapper.selectCount(userWrapper);
            statistics.put("totalUsers", totalUsers);
            
            // 活跃用户数
            QueryWrapper<User> activeUserWrapper = new QueryWrapper<>();
            activeUserWrapper.eq("deleted", 0).eq("status", 1);
            long activeUsers = userMapper.selectCount(activeUserWrapper);
            statistics.put("activeUsers", activeUsers);
            
            // 最近创建的档案数
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            QueryWrapper<Archive> recentArchiveWrapper = new QueryWrapper<>();
            recentArchiveWrapper.eq("deleted", 0).ge("create_time", oneWeekAgo);
            long recentArchives = archiveMapper.selectCount(recentArchiveWrapper);
            statistics.put("recentArchives", recentArchives);
            
            // 缓存统计结果
            redisTemplate.opsForValue().set(cacheKey, statistics, 10, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            log.error("获取搜索统计失败", e);
            monitoringService.recordError("search_statistics", e.getMessage());
        }
        
        return statistics;
    }

    /**
     * 构建档案搜索条件
     */
    private QueryWrapper<Archive> buildArchiveSearchQuery(Map<String, Object> criteria) {
        QueryWrapper<Archive> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：未删除
        queryWrapper.eq("deleted", 0);
        
        // 标题搜索
        if (criteria.containsKey("title") && StringUtils.hasText(criteria.get("title").toString())) {
            queryWrapper.like("title", criteria.get("title").toString());
        }
        
        // 编号搜索
        if (criteria.containsKey("code") && StringUtils.hasText(criteria.get("code").toString())) {
            queryWrapper.like("archive_number", criteria.get("code").toString());
        }
        
        // 描述搜索
        if (criteria.containsKey("description") && StringUtils.hasText(criteria.get("description").toString())) {
            queryWrapper.like("description", criteria.get("description").toString());
        }
        
        // 状态过滤
        if (criteria.containsKey("status") && criteria.get("status") != null) {
            queryWrapper.eq("status", criteria.get("status"));
        }
        
        // 创建时间范围
        if (criteria.containsKey("createTimeStart") && criteria.get("createTimeStart") != null) {
            queryWrapper.ge("create_time", criteria.get("createTimeStart"));
        }
        
        if (criteria.containsKey("createTimeEnd") && criteria.get("createTimeEnd") != null) {
            queryWrapper.le("create_time", criteria.get("createTimeEnd"));
        }
        
        // 创建用户过滤
        if (criteria.containsKey("createUserId") && criteria.get("createUserId") != null) {
            queryWrapper.eq("created_by", criteria.get("createUserId"));
        }
        
        // 关键词全文搜索
        if (criteria.containsKey("keyword") && StringUtils.hasText(criteria.get("keyword").toString())) {
            String keyword = criteria.get("keyword").toString();
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("description", keyword)
                .or()
                .like("archive_number", keyword)
            );
        }
        
        // 分类过滤
        if (criteria.containsKey("categoryId") && criteria.get("categoryId") != null) {
            queryWrapper.eq("category_id", criteria.get("categoryId"));
        }
        
        // 安全级别过滤
        if (criteria.containsKey("securityLevel") && StringUtils.hasText(criteria.get("securityLevel").toString())) {
            queryWrapper.eq("security_level", criteria.get("securityLevel"));
        }
        
        return queryWrapper;
    }

    /**
     * 构建用户搜索条件
     */
    private QueryWrapper<User> buildUserSearchQuery(Map<String, Object> criteria) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        // 基础条件：未删除
        queryWrapper.eq("deleted", 0);
        
        // 用户名搜索
        if (criteria.containsKey("username") && StringUtils.hasText(criteria.get("username").toString())) {
            queryWrapper.like("username", criteria.get("username").toString());
        }
        
        // 真实姓名搜索
        if (criteria.containsKey("realName") && StringUtils.hasText(criteria.get("realName").toString())) {
            queryWrapper.like("real_name", criteria.get("realName").toString());
        }
        
        // 邮箱搜索
        if (criteria.containsKey("email") && StringUtils.hasText(criteria.get("email").toString())) {
            queryWrapper.like("email", criteria.get("email").toString());
        }
        
        // 手机号搜索
        if (criteria.containsKey("phone") && StringUtils.hasText(criteria.get("phone").toString())) {
            queryWrapper.like("phone", criteria.get("phone").toString());
        }
        
        // 状态过滤
        if (criteria.containsKey("status") && criteria.get("status") != null) {
            queryWrapper.eq("status", criteria.get("status"));
        }
        
        // 创建时间范围
        if (criteria.containsKey("createTimeStart") && criteria.get("createTimeStart") != null) {
            queryWrapper.ge("create_time", criteria.get("createTimeStart"));
        }
        
        if (criteria.containsKey("createTimeEnd") && criteria.get("createTimeEnd") != null) {
            queryWrapper.le("create_time", criteria.get("createTimeEnd"));
        }
        
        // 关键词全文搜索
        if (criteria.containsKey("keyword") && StringUtils.hasText(criteria.get("keyword").toString())) {
            String keyword = criteria.get("keyword").toString();
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword)
                .or()
                .like("real_name", keyword)
                .or()
                .like("email", keyword)
            );
        }
        
        // 部门过滤
        if (criteria.containsKey("departmentId") && criteria.get("departmentId") != null) {
            queryWrapper.eq("department_id", criteria.get("departmentId"));
        }
        
        return queryWrapper;
    }

    /**
     * 搜索历史记录
     */
    public void recordSearchHistory(String keyword, String type, Long userId) {
        try {
            String cacheKey = "search:history:" + userId + ":" + type;
            List<String> history = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            
            if (history == null) {
                history = new ArrayList<>();
            }
            
            // 添加新的搜索关键词
            if (!history.contains(keyword)) {
                history.add(0, keyword); // 添加到列表开头
                
                // 限制历史记录数量
                if (history.size() > 20) {
                    history = history.subList(0, 20);
                }
                
                // 保存到缓存
                redisTemplate.opsForValue().set(cacheKey, history, 7, TimeUnit.DAYS);
            }
            
            log.info("记录搜索历史: {} - 类型: {} - 用户: {}", keyword, type, userId);
        } catch (Exception e) {
            log.error("记录搜索历史失败", e);
            monitoringService.recordError("search_history", e.getMessage());
        }
    }

    /**
     * 获取搜索历史
     */
    public List<String> getSearchHistory(Long userId, String type) {
        List<String> history = new ArrayList<>();
        
        try {
            String cacheKey = "search:history:" + userId + ":" + type;
            List<String> cachedHistory = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedHistory != null) {
                history = cachedHistory;
            }
        } catch (Exception e) {
            log.error("获取搜索历史失败", e);
            monitoringService.recordError("search_history", e.getMessage());
        }
        
        return history;
    }

    /**
     * 清除搜索历史
     */
    public void clearSearchHistory(Long userId, String type) {
        try {
            String cacheKey = "search:history:" + userId + ":" + type;
            redisTemplate.delete(cacheKey);
            log.info("清除搜索历史: 用户 {} - 类型: {}", userId, type);
        } catch (Exception e) {
            log.error("清除搜索历史失败", e);
            monitoringService.recordError("search_history", e.getMessage());
        }
    }

    /**
     * 获取热门搜索关键词
     */
    public List<String> getHotSearchKeywords(String type) {
        List<String> hotKeywords = new ArrayList<>();
        
        try {
            String cacheKey = "search:hot:" + type;
            List<String> cachedKeywords = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedKeywords != null) {
                return cachedKeywords;
            }
            
            // 这里可以实现基于搜索频率的热门关键词统计
            // 暂时返回示例数据
            if ("archive".equals(type)) {
                hotKeywords.add("年度报告");
                hotKeywords.add("会议纪要");
                hotKeywords.add("合同文件");
                hotKeywords.add("财务报表");
                hotKeywords.add("人事档案");
            } else if ("user".equals(type)) {
                hotKeywords.add("管理员");
                hotKeywords.add("财务");
                hotKeywords.add("人事");
                hotKeywords.add("技术");
                hotKeywords.add("销售");
            }
            
            // 缓存热门关键词
            if (!hotKeywords.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, hotKeywords, 1, TimeUnit.HOURS);
            }
            
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败", e);
            monitoringService.recordError("hot_keywords", e.getMessage());
        }
        
        return hotKeywords;
    }

    /**
     * 搜索相似档案
     */
    public List<Archive> findSimilarArchives(Long archiveId, int limit) {
        List<Archive> similarArchives = new ArrayList<>();
        
        try {
            // 获取目标档案
            Archive targetArchive = archiveMapper.selectById(archiveId);
            if (targetArchive == null) {
                return similarArchives;
            }
            
            // 基于分类和关键词搜索相似档案
            QueryWrapper<Archive> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("deleted", 0);
            queryWrapper.ne("id", archiveId);
            
            // 同分类的档案
            if (targetArchive.getCategoryId() != null) {
                queryWrapper.eq("category_id", targetArchive.getCategoryId());
            }
            
            // 相似标题的档案
            if (StringUtils.hasText(targetArchive.getTitle())) {
                String[] titleWords = targetArchive.getTitle().split("\\s+");
                if (titleWords.length > 0) {
                    queryWrapper.and(wrapper -> {
                        for (String word : titleWords) {
                            if (word.length() > 2) {
                                wrapper.like("title", word).or();
                            }
                        }
                        return wrapper;
                    });
                }
            }
            
            queryWrapper.last("LIMIT " + limit);
            similarArchives = archiveMapper.selectList(queryWrapper);
            
        } catch (Exception e) {
            log.error("搜索相似档案失败", e);
            monitoringService.recordError("similar_archives", e.getMessage());
        }
        
        return similarArchives;
    }
}