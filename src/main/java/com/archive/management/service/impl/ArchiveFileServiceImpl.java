package com.archive.management.service.impl;

import com.archive.management.entity.ArchiveFile;
import com.archive.management.mapper.ArchiveFileMapper;
import com.archive.management.service.ArchiveFileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 档案文件业务服务实现类
 * 实现档案文件管理的核心业务逻辑
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveFileServiceImpl extends ServiceImpl<ArchiveFileMapper, ArchiveFile> implements ArchiveFileService {

    private final ArchiveFileMapper archiveFileMapper;
    
    @Override
    public ArchiveFileMapper getBaseMapper() {
        return archiveFileMapper;
    }

    @Value("${file.upload.path:/data/archive/files}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types:pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,jpeg,png,gif}")
    private String allowedTypes;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "archive_file:";

    /**
     * 上传档案文件
     * @param archiveId 档案ID
     * @param file 上传的文件
     * @param uploadedBy 上传人ID
     * @return 文件信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archive_files", key = "#archiveId")
    public ArchiveFile uploadFile(Long archiveId, MultipartFile file, Long uploadedBy) {
        log.info("开始上传档案文件，档案ID: {}, 文件名: {}", archiveId, file.getOriginalFilename());
        
        try {
            // 验证文件
            validateFile(file);
            
            // 生成文件信息
            ArchiveFile archiveFile = new ArchiveFile();
            archiveFile.setArchiveId(archiveId);
            archiveFile.setOriginalName(file.getOriginalFilename());
            archiveFile.setFileSize(file.getSize());
            archiveFile.setFileType(getFileExtension(file.getOriginalFilename()));
            archiveFile.setMimeType(file.getContentType());
            archiveFile.setUploadedBy(uploadedBy);
            archiveFile.setDeleted(false);
            archiveFile.setCreateTime(LocalDateTime.now());
            archiveFile.setUpdateTime(LocalDateTime.now());
            
            // 生成存储路径和文件名
            String storagePath = generateStoragePath(archiveId);
            String fileName = generateFileName(file.getOriginalFilename());
            archiveFile.setStoragePath(storagePath);
            archiveFile.setFileName(fileName);
            
            // 创建目录
            Path directoryPath = Paths.get(uploadPath, storagePath);
            Files.createDirectories(directoryPath);
            
            // 保存文件
            Path filePath = directoryPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            
            // 设置访问URL
            archiveFile.setAccessUrl("/api/files/" + archiveId + "/" + fileName);
            
            // 保存文件记录
            boolean saved = save(archiveFile);
            if (!saved) {
                // 删除已上传的文件
                Files.deleteIfExists(filePath);
                throw new RuntimeException("文件记录保存失败");
            }
            
            log.info("档案文件上传成功，ID: {}, 路径: {}", archiveFile.getId(), filePath);
            return archiveFile;
            
        } catch (Exception e) {
            log.error("上传档案文件失败，档案ID: {}, 文件名: {}", archiveId, file.getOriginalFilename(), e);
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据档案ID获取文件列表
     * @param archiveId 档案ID
     * @return 文件列表
     */
    @Override
    @Cacheable(value = "archive_files", key = "#archiveId")
    public List<ArchiveFile> getFilesByArchiveId(Long archiveId) {
        log.debug("获取档案文件列表，档案ID: {}", archiveId);
        
        if (archiveId == null) {
            throw new IllegalArgumentException("档案ID不能为空");
        }
        
        QueryWrapper<ArchiveFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("archive_id", archiveId)
                   .eq("deleted", false)
                   .orderByDesc("create_time");
        
        return list(queryWrapper);
    }

    /**
     * 根据ID获取文件信息
     * @param id 文件ID
     * @return 文件信息
     */
    @Override
    @Cacheable(value = "archive_files", key = "'detail:' + #id")
    public ArchiveFile getFileById(Long id) {
        log.debug("获取文件详情，ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        
        ArchiveFile archiveFile = getById(id);
        if (archiveFile == null || archiveFile.getDeleted()) {
            throw new RuntimeException("文件不存在或已删除，ID: " + id);
        }
        
        return archiveFile;
    }

    /**
     * 删除文件
     * @param id 文件ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archive_files", allEntries = true)
    public boolean deleteFile(Long id, Long deletedBy) {
        log.info("开始删除档案文件，ID: {}, 删除人: {}", id, deletedBy);
        
        try {
            // 验证文件存在
            ArchiveFile archiveFile = getFileById(id);
            
            // 执行软删除
            UpdateWrapper<ArchiveFile> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                        .set("deleted", true)
                        .set("deleted_by", deletedBy)
                        .set("deleted_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now());
            
            boolean deleted = update(updateWrapper);
            
            // 删除物理文件（可选，根据业务需求决定）
            if (deleted) {
                try {
                    Path filePath = Paths.get(uploadPath, archiveFile.getStoragePath(), archiveFile.getFileName());
                    Files.deleteIfExists(filePath);
                    log.info("物理文件删除成功: {}", filePath);
                } catch (Exception e) {
                    log.warn("删除物理文件失败: {}", e.getMessage());
                }
                
                log.info("档案文件删除成功，ID: {}", id);
            } else {
                log.warn("档案文件删除失败，ID: {}", id);
            }
            
            return deleted;
            
        } catch (Exception e) {
            log.error("删除档案文件失败，ID: {}", id, e);
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件下载路径
     * @param id 文件ID
     * @return 文件路径
     */
    @Override
    public String getFileDownloadPath(Long id) {
        log.debug("获取文件下载路径，ID: {}", id);
        
        ArchiveFile archiveFile = getFileById(id);
        Path filePath = Paths.get(uploadPath, archiveFile.getStoragePath(), archiveFile.getFileName());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在: " + filePath);
        }
        
        return filePath.toString();
    }

    /**
     * 更新文件信息
     * @param archiveFile 文件信息
     * @return 更新后的文件信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archive_files", key = "#archiveFile.id")
    public ArchiveFile updateFile(ArchiveFile archiveFile) {
        log.info("开始更新文件信息，ID: {}", archiveFile.getId());
        
        try {
            // 验证文件存在
            ArchiveFile existingFile = getFileById(archiveFile.getId());
            
            // 更新时间
            archiveFile.setUpdateTime(LocalDateTime.now());
            
            // 保存更新
            boolean updated = updateById(archiveFile);
            if (!updated) {
                throw new RuntimeException("文件信息更新失败");
            }
            
            log.info("文件信息更新成功，ID: {}", archiveFile.getId());
            return archiveFile;
            
        } catch (Exception e) {
            log.error("更新文件信息失败，ID: {}", archiveFile.getId(), e);
            throw new RuntimeException("更新文件信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除文件
     * @param archiveId 档案ID
     * @param deletedBy 删除人ID
     * @return 删除的文件数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "archive_files", key = "#archiveId")
    public int deleteFilesByArchiveId(Long archiveId, Long deletedBy) {
        log.info("开始批量删除档案文件，档案ID: {}, 删除人: {}", archiveId, deletedBy);
        
        try {
            // 获取要删除的文件列表
            List<ArchiveFile> files = getFilesByArchiveId(archiveId);
            
            if (files.isEmpty()) {
                return 0;
            }
            
            // 批量软删除
            UpdateWrapper<ArchiveFile> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("archive_id", archiveId)
                        .eq("deleted", false)
                        .set("deleted", true)
                        .set("deleted_by", deletedBy)
                        .set("deleted_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now());
            
            boolean updated = update(updateWrapper);
            
            if (updated) {
                // 删除物理文件
                for (ArchiveFile file : files) {
                    try {
                        Path filePath = Paths.get(uploadPath, file.getStoragePath(), file.getFileName());
                        Files.deleteIfExists(filePath);
                    } catch (Exception e) {
                        log.warn("删除物理文件失败: {}", e.getMessage());
                    }
                }
                
                log.info("批量删除档案文件成功，档案ID: {}, 删除数量: {}", archiveId, files.size());
                return files.size();
            } else {
                log.warn("批量删除档案文件失败，档案ID: {}", archiveId);
                return 0;
            }
            
        } catch (Exception e) {
            log.error("批量删除档案文件失败，档案ID: {}", archiveId, e);
            throw new RuntimeException("批量删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证上传文件
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // 检查文件类型
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (fileExtension == null || !isAllowedFileType(fileExtension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        }
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return null;
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 检查是否为允许的文件类型
     * @param fileExtension 文件扩展名
     * @return 是否允许
     */
    private boolean isAllowedFileType(String fileExtension) {
        if (fileExtension == null || allowedTypes == null) {
            return false;
        }
        
        String[] types = allowedTypes.toLowerCase().split(",");
        return Arrays.asList(types).contains(fileExtension.toLowerCase());
    }

    /**
     * 生成存储路径
     * @param archiveId 档案ID
     * @return 存储路径
     */
    private String generateStoragePath(Long archiveId) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + archiveId;
    }

    /**
     * 生成文件名
     * @param originalFileName 原始文件名
     * @return 新文件名
     */
    private String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        if (extension != null) {
            return timestamp + "_" + randomStr + "." + extension;
        } else {
            return timestamp + "_" + randomStr;
        }
    }
}