package com.archive.management.service.impl;

import com.archive.management.entity.Archive;
import com.archive.management.entity.ArchiveFile;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.ArchiveFileMapper;
import com.archive.management.mapper.CategoryMapper;
import com.archive.management.mapper.UserMapper;
import com.archive.management.service.ArchiveService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 档案业务服务实现类
 * 实现档案管理的核心业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveServiceImpl extends ServiceImpl<ArchiveMapper, Archive> implements ArchiveService {

    private final ArchiveMapper archiveMapper;
    
    @Override
    public ArchiveMapper getBaseMapper() {
        return archiveMapper;
    }
    private final ArchiveFileMapper archiveFileMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "archive:";
    private static final String CACHE_KEY_ARCHIVE = CACHE_PREFIX + "detail:";
    private static final String CACHE_KEY_LIST = CACHE_PREFIX + "list:";
    private static final String CACHE_KEY_STATS = CACHE_PREFIX + "stats:";

    // 档案状态常量
    private static final Integer STATUS_DRAFT = 1;      // 草稿
    private static final Integer STATUS_PENDING = 2;    // 待审核
    private static final Integer STATUS_APPROVED = 3;   // 已审核
    private static final Integer STATUS_PUBLISHED = 4;  // 已发布
    private static final Integer STATUS_ARCHIVED = 5;   // 已归档
    private static final Integer STATUS_REJECTED = 6;   // 已拒绝

    /**
     * 创建档案
     * @param archive 档案信息
     * @return 创建的档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archives", allEntries = true)
    public Archive createArchive(Archive archive) {
        log.info("开始创建档案，标题: {}", archive.getTitle());
        
        try {
            // 验证档案数据
            validateArchiveData(archive);
            
            // 设置默认值
            archive.setStatus(STATUS_DRAFT);
            archive.setViewCount(0L);
            archive.setDownloadCount(0L);
            archive.setVersionNumber(1);
            archive.setDeleted(false);
            archive.setCreateTime(LocalDateTime.now());
            archive.setUpdateTime(LocalDateTime.now());
            
            // 生成档案编号（如果未提供）
            if (archive.getArchiveNumber() == null || archive.getArchiveNumber().trim().isEmpty()) {
                archive.setArchiveNumber(generateArchiveNumber(archive.getCategoryId()));
            }
            
            // 检查档案编号是否重复
            if (existsByArchiveNumber(archive.getArchiveNumber())) {
                throw new RuntimeException("档案编号已存在: " + archive.getArchiveNumber());
            }
            
            // 保存档案
            boolean saved = save(archive);
            if (!saved) {
                throw new RuntimeException("档案保存失败");
            }
            
            log.info("档案创建成功，ID: {}, 编号: {}", archive.getId(), archive.getArchiveNumber());
            return archive;
            
        } catch (Exception e) {
            log.error("创建档案失败，标题: {}", archive.getTitle(), e);
            throw new RuntimeException("创建档案失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取档案
     * @param id 档案ID
     * @return 档案信息
     */
    @Override
    @Cacheable(value = "archives", key = "#id")
    public Archive getArchiveById(Long id) {
        log.debug("获取档案详情，ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        Archive archive = getById(id);
        if (archive == null || archive.getDeleted()) {
            throw new RuntimeException("档案不存在或已删除，ID: " + id);
        }
        
        return archive;
    }

    /**
     * 根据档案编号获取档案
     * @param archiveNumber 档案编号
     * @return 档案信息
     */
    @Override
    @Cacheable(value = "archives", key = "'number:' + #archiveNumber")
    public Archive getArchiveByNumber(String archiveNumber) {
        log.debug("根据编号获取档案，编号: {}", archiveNumber);
        
        if (archiveNumber == null || archiveNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("档案编号不能为空");
        }
        
        Archive archive = archiveMapper.findByArchiveNumber(archiveNumber);
        if (archive == null) {
            throw new RuntimeException("档案不存在，编号: " + archiveNumber);
        }
        
        return archive;
    }

    /**
     * 更新档案信息
     * @param archive 档案信息
     * @return 更新后的档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archives", key = "#archive.id")
    public Archive updateArchive(Archive archive) {
        log.info("开始更新档案，ID: {}", archive.getId());
        
        try {
            // 验证档案存在
            Archive existingArchive = getArchiveById(archive.getId());
            
            // 验证档案数据
            validateArchiveData(archive);
            
            // 检查档案编号是否重复（排除自身）
            if (!existingArchive.getArchiveNumber().equals(archive.getArchiveNumber()) 
                && existsByArchiveNumber(archive.getArchiveNumber())) {
                throw new RuntimeException("档案编号已存在: " + archive.getArchiveNumber());
            }
            
            // 更新时间和版本号
            archive.setUpdateTime(LocalDateTime.now());
            archive.setVersionNumber(existingArchive.getVersionNumber() + 1);
            
            // 保存更新
            boolean updated = updateById(archive);
            if (!updated) {
                throw new RuntimeException("档案更新失败");
            }
            
            log.info("档案更新成功，ID: {}", archive.getId());
            return archive;
            
        } catch (Exception e) {
            log.error("更新档案失败，ID: {}", archive.getId(), e);
            throw new RuntimeException("更新档案失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除档案（软删除）
     * @param id 档案ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archives", key = "#id")
    public boolean deleteArchive(Long id, Long deletedBy) {
        log.info("开始删除档案，ID: {}, 删除人: {}", id, deletedBy);
        
        try {
            // 验证档案存在
            Archive archive = getArchiveById(id);
            
            // 执行软删除
            UpdateWrapper<Archive> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                        .set("deleted", true)
                        .set("deleted_by", deletedBy)
                        .set("deleted_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now());
            
            boolean deleted = update(updateWrapper);
            if (deleted) {
                log.info("档案删除成功，ID: {}", id);
            } else {
                log.warn("档案删除失败，ID: {}", id);
            }
            
            return deleted;
            
        } catch (Exception e) {
            log.error("删除档案失败，ID: {}", id, e);
            throw new RuntimeException("删除档案失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查档案编号是否存在
     * @param archiveNumber 档案编号
     * @return 是否存在
     */
    @Override
    public boolean existsByArchiveNumber(String archiveNumber) {
        if (archiveNumber == null || archiveNumber.trim().isEmpty()) {
            return false;
        }
        
        int count = archiveMapper.countByArchiveNumber(archiveNumber);
        return count > 0;
    }

    /**
     * 生成档案编号
     * @param categoryId 分类ID
     * @return 档案编号
     */
    @Override
    public String generateArchiveNumber(Long categoryId) {
        try {
            String prefix = "ARC";
            
            // 如果有分类，使用分类编码作为前缀
            if (categoryId != null) {
                var category = categoryMapper.selectById(categoryId);
                if (category != null && category.getCategoryCode() != null) {
                    prefix = category.getCategoryCode();
                }
            }
            
            // 生成时间戳部分
            String timestamp = String.valueOf(System.currentTimeMillis());
            String datePart = timestamp.substring(timestamp.length() - 8);
            
            // 生成随机数部分
            Random random = new Random();
            String randomPart = String.format("%03d", random.nextInt(1000));
            
            return prefix + "-" + datePart + "-" + randomPart;
            
        } catch (Exception e) {
            log.error("生成档案编号失败", e);
            return "ARC-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
        }
    }

    /**
     * 验证档案数据
     * @param archive 档案信息
     * @return 验证结果
     */
    @Override
    public Map<String, Object> validateArchiveData(Archive archive) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        // 验证必填字段
        if (archive.getTitle() == null || archive.getTitle().trim().isEmpty()) {
            errors.add("档案标题不能为空");
        } else if (archive.getTitle().length() > 200) {
            errors.add("档案标题长度不能超过200个字符");
        }
        
        if (archive.getArchiveNumber() != null && archive.getArchiveNumber().length() > 50) {
            errors.add("档案编号长度不能超过50个字符");
        }
        
        if (archive.getCategoryId() == null) {
            errors.add("档案分类不能为空");
        }
        
        // 验证分类是否存在
        if (archive.getCategoryId() != null) {
            var category = categoryMapper.selectById(archive.getCategoryId());
            if (category == null || category.getDeleted()) {
                errors.add("指定的档案分类不存在");
            }
        }
        
        // 验证创建人是否存在
        if (archive.getCreatedBy() != null) {
            var user = userMapper.selectById(archive.getCreatedBy());
            if (user == null || user.getDeleted()) {
                errors.add("指定的创建人不存在");
            }
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("档案数据验证失败: " + String.join(", ", errors));
        }
        
        return result;
    }

    // 继续实现其他方法...
    // 由于文件长度限制，其他方法将在后续步骤中实现
}