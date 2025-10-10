package com.archive.management.service;

import com.archive.management.entity.Archive;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案业务服务接口
 * 定义档案管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface ArchiveService {

    /**
     * 创建档案
     * @param archive 档案信息
     * @return 创建的档案
     */
    Archive createArchive(Archive archive);

    /**
     * 根据ID获取档案
     * @param id 档案ID
     * @return 档案信息
     */
    Archive getArchiveById(Long id);

    /**
     * 根据档案编号获取档案
     * @param archiveNumber 档案编号
     * @return 档案信息
     */
    Archive getArchiveByNumber(String archiveNumber);

    /**
     * 更新档案信息
     * @param archive 档案信息
     * @return 更新后的档案
     */
    Archive updateArchive(Archive archive);

    /**
     * 删除档案（软删除）
     * @param id 档案ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteArchive(Long id, Long deletedBy);

    /**
     * 批量删除档案（软删除）
     * @param ids 档案ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteArchives(List<Long> ids, Long deletedBy);

    /**
     * 归档档案
     * @param id 档案ID
     * @param archivedBy 归档人ID
     * @return 是否归档成功
     */
    boolean archiveDocument(Long id, Long archivedBy);

    /**
     * 取消归档
     * @param id 档案ID
     * @param updatedBy 更新人ID
     * @return 是否取消归档成功
     */
    boolean unarchiveDocument(Long id, Long updatedBy);

    /**
     * 批量归档档案
     * @param ids 档案ID列表
     * @param archivedBy 归档人ID
     * @return 归档成功的数量
     */
    int batchArchiveDocuments(List<Long> ids, Long archivedBy);

    /**
     * 发布档案
     * @param id 档案ID
     * @param publishedBy 发布人ID
     * @return 是否发布成功
     */
    boolean publishArchive(Long id, Long publishedBy);

    /**
     * 取消发布档案
     * @param id 档案ID
     * @param updatedBy 更新人ID
     * @return 是否取消发布成功
     */
    boolean unpublishArchive(Long id, Long updatedBy);

    /**
     * 批量更新档案状态
     * @param ids 档案ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateArchiveStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 检查档案编号是否存在
     * @param archiveNumber 档案编号
     * @return 是否存在
     */
    boolean existsByArchiveNumber(String archiveNumber);

    /**
     * 分页查询档案
     * @param page 分页参数
     * @param archiveNumber 档案编号（可选）
     * @param title 标题（可选）
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param createdBy 创建人ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<Archive> findArchivesWithPagination(Page<Archive> page, String archiveNumber, 
                                             String title, Long categoryId, Integer status, 
                                             Long createdBy, LocalDateTime startDate, 
                                             LocalDateTime endDate);

    /**
     * 根据分类ID查找档案列表
     * @param categoryId 分类ID
     * @return 档案列表
     */
    List<Archive> findArchivesByCategoryId(Long categoryId);

    /**
     * 根据状态查找档案列表
     * @param status 状态
     * @return 档案列表
     */
    List<Archive> findArchivesByStatus(Integer status);

    /**
     * 根据创建人查找档案列表
     * @param createdBy 创建人ID
     * @return 档案列表
     */
    List<Archive> findArchivesByCreatedBy(Long createdBy);

    /**
     * 根据标题搜索档案
     * @param title 标题关键词
     * @return 档案列表
     */
    List<Archive> searchArchivesByTitle(String title);

    /**
     * 根据内容搜索档案
     * @param content 内容关键词
     * @return 档案列表
     */
    List<Archive> searchArchivesByContent(String content);

    /**
     * 全文搜索档案
     * @param keyword 关键词
     * @return 档案列表
     */
    List<Archive> fullTextSearchArchives(String keyword);

    /**
     * 高级搜索档案
     * @param searchParams 搜索参数
     * @return 档案列表
     */
    List<Archive> advancedSearchArchives(Map<String, Object> searchParams);

    /**
     * 获取热门档案
     * @param limit 数量限制
     * @return 档案列表
     */
    List<Archive> getPopularArchives(int limit);

    /**
     * 获取最新档案
     * @param limit 数量限制
     * @return 档案列表
     */
    List<Archive> getLatestArchives(int limit);

    /**
     * 获取推荐档案
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 档案列表
     */
    List<Archive> getRecommendedArchives(Long userId, int limit);

    /**
     * 统计档案总数
     * @return 总数
     */
    long countArchives();

    /**
     * 根据分类统计档案数量
     * @param categoryId 分类ID
     * @return 数量
     */
    long countArchivesByCategory(Long categoryId);

    /**
     * 根据状态统计档案数量
     * @param status 状态
     * @return 数量
     */
    long countArchivesByStatus(Integer status);

    /**
     * 根据创建人统计档案数量
     * @param createdBy 创建人ID
     * @return 数量
     */
    long countArchivesByCreatedBy(Long createdBy);

    /**
     * 根据日期范围统计档案数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 数量
     */
    long countArchivesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取档案分类统计
     * @return 分类统计结果
     */
    List<Map<String, Object>> getArchiveCategoryStatistics();

    /**
     * 获取档案状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getArchiveStatusStatistics();

    /**
     * 获取档案创建趋势统计
     * @param days 统计天数
     * @return 趋势统计结果
     */
    List<Map<String, Object>> getArchiveCreationTrend(int days);

    /**
     * 获取档案访问统计
     * @param days 统计天数
     * @return 访问统计结果
     */
    List<Map<String, Object>> getArchiveAccessStatistics(int days);

    /**
     * 增加档案查看次数
     * @param id 档案ID
     * @param userId 用户ID（可选）
     * @return 是否增加成功
     */
    boolean incrementViewCount(Long id, Long userId);

    /**
     * 增加档案下载次数
     * @param id 档案ID
     * @param userId 用户ID（可选）
     * @return 是否增加成功
     */
    boolean incrementDownloadCount(Long id, Long userId);

    /**
     * 获取档案访问记录
     * @param archiveId 档案ID
     * @param days 天数
     * @return 访问记录
     */
    List<Map<String, Object>> getArchiveAccessLogs(Long archiveId, int days);

    /**
     * 上传档案文件
     * @param archiveId 档案ID
     * @param file 文件
     * @param uploadedBy 上传人ID
     * @return 文件信息
     */
    Map<String, Object> uploadArchiveFile(Long archiveId, MultipartFile file, Long uploadedBy);

    /**
     * 批量上传档案文件
     * @param archiveId 档案ID
     * @param files 文件列表
     * @param uploadedBy 上传人ID
     * @return 上传结果
     */
    List<Map<String, Object>> batchUploadArchiveFiles(Long archiveId, List<MultipartFile> files, Long uploadedBy);

    /**
     * 删除档案文件
     * @param archiveId 档案ID
     * @param fileId 文件ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteArchiveFile(Long archiveId, Long fileId, Long deletedBy);

    /**
     * 获取档案文件列表
     * @param archiveId 档案ID
     * @return 文件列表
     */
    List<Map<String, Object>> getArchiveFiles(Long archiveId);

    /**
     * 下载档案文件
     * @param archiveId 档案ID
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 文件下载信息
     */
    Map<String, Object> downloadArchiveFile(Long archiveId, Long fileId, Long userId);

    /**
     * 预览档案文件
     * @param archiveId 档案ID
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 预览信息
     */
    Map<String, Object> previewArchiveFile(Long archiveId, Long fileId, Long userId);

    /**
     * 生成档案缩略图
     * @param archiveId 档案ID
     * @param fileId 文件ID
     * @return 缩略图信息
     */
    Map<String, Object> generateArchiveThumbnail(Long archiveId, Long fileId);

    /**
     * 检查用户是否有档案访问权限
     * @param userId 用户ID
     * @param archiveId 档案ID
     * @return 是否有权限
     */
    boolean checkUserArchiveAccess(Long userId, Long archiveId);

    /**
     * 检查用户是否有档案编辑权限
     * @param userId 用户ID
     * @param archiveId 档案ID
     * @return 是否有权限
     */
    boolean checkUserArchiveEditAccess(Long userId, Long archiveId);

    /**
     * 检查用户是否有档案删除权限
     * @param userId 用户ID
     * @param archiveId 档案ID
     * @return 是否有权限
     */
    boolean checkUserArchiveDeleteAccess(Long userId, Long archiveId);

    /**
     * 获取用户可访问的档案列表
     * @param userId 用户ID
     * @param page 分页参数
     * @return 档案列表
     */
    IPage<Archive> getUserAccessibleArchives(Long userId, Page<Archive> page);

    /**
     * 复制档案
     * @param sourceArchiveId 源档案ID
     * @param newTitle 新标题
     * @param newArchiveNumber 新档案编号
     * @param categoryId 分类ID
     * @param createdBy 创建人ID
     * @return 新创建的档案
     */
    Archive copyArchive(Long sourceArchiveId, String newTitle, String newArchiveNumber, 
                       Long categoryId, Long createdBy);

    /**
     * 移动档案到新分类
     * @param archiveId 档案ID
     * @param newCategoryId 新分类ID
     * @param updatedBy 更新人ID
     * @return 是否移动成功
     */
    boolean moveArchiveToCategory(Long archiveId, Long newCategoryId, Long updatedBy);

    /**
     * 批量移动档案到新分类
     * @param archiveIds 档案ID列表
     * @param newCategoryId 新分类ID
     * @param updatedBy 更新人ID
     * @return 移动成功的数量
     */
    int batchMoveArchivesToCategory(List<Long> archiveIds, Long newCategoryId, Long updatedBy);

    /**
     * 导出档案数据
     * @param ids 档案ID列表（可选，为空则导出所有）
     * @param format 导出格式（excel/pdf/csv）
     * @return 导出文件信息
     */
    Map<String, Object> exportArchives(List<Long> ids, String format);

    /**
     * 批量导入档案
     * @param file 导入文件
     * @param categoryId 默认分类ID
     * @param createdBy 创建人ID
     * @return 导入结果
     */
    Map<String, Object> importArchives(MultipartFile file, Long categoryId, Long createdBy);

    /**
     * 生成档案编号
     * @param categoryId 分类ID
     * @return 档案编号
     */
    String generateArchiveNumber(Long categoryId);

    /**
     * 验证档案数据
     * @param archive 档案信息
     * @return 验证结果
     */
    Map<String, Object> validateArchiveData(Archive archive);

    /**
     * 获取档案版本历史
     * @param archiveId 档案ID
     * @return 版本历史列表
     */
    List<Map<String, Object>> getArchiveVersionHistory(Long archiveId);

    /**
     * 创建档案版本
     * @param archiveId 档案ID
     * @param versionNote 版本说明
     * @param createdBy 创建人ID
     * @return 版本信息
     */
    Map<String, Object> createArchiveVersion(Long archiveId, String versionNote, Long createdBy);

    /**
     * 恢复档案版本
     * @param archiveId 档案ID
     * @param versionId 版本ID
     * @param restoredBy 恢复人ID
     * @return 是否恢复成功
     */
    boolean restoreArchiveVersion(Long archiveId, Long versionId, Long restoredBy);

    /**
     * 比较档案版本
     * @param archiveId 档案ID
     * @param version1Id 版本1ID
     * @param version2Id 版本2ID
     * @return 比较结果
     */
    Map<String, Object> compareArchiveVersions(Long archiveId, Long version1Id, Long version2Id);

    /**
     * 设置档案标签
     * @param archiveId 档案ID
     * @param tags 标签列表
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setArchiveTags(Long archiveId, List<String> tags, Long updatedBy);

    /**
     * 添加档案标签
     * @param archiveId 档案ID
     * @param tag 标签
     * @param updatedBy 更新人ID
     * @return 是否添加成功
     */
    boolean addArchiveTag(Long archiveId, String tag, Long updatedBy);

    /**
     * 移除档案标签
     * @param archiveId 档案ID
     * @param tag 标签
     * @param updatedBy 更新人ID
     * @return 是否移除成功
     */
    boolean removeArchiveTag(Long archiveId, String tag, Long updatedBy);

    /**
     * 根据标签搜索档案
     * @param tags 标签列表
     * @return 档案列表
     */
    List<Archive> searchArchivesByTags(List<String> tags);

    /**
     * 获取热门标签
     * @param limit 数量限制
     * @return 标签列表
     */
    List<Map<String, Object>> getPopularTags(int limit);

    /**
     * 设置档案评分
     * @param archiveId 档案ID
     * @param userId 用户ID
     * @param rating 评分
     * @return 是否设置成功
     */
    boolean setArchiveRating(Long archiveId, Long userId, Integer rating);

    /**
     * 获取档案平均评分
     * @param archiveId 档案ID
     * @return 平均评分
     */
    Double getArchiveAverageRating(Long archiveId);

    /**
     * 添加档案评论
     * @param archiveId 档案ID
     * @param userId 用户ID
     * @param comment 评论内容
     * @return 评论信息
     */
    Map<String, Object> addArchiveComment(Long archiveId, Long userId, String comment);

    /**
     * 获取档案评论列表
     * @param archiveId 档案ID
     * @param page 分页参数
     * @return 评论列表
     */
    IPage<Map<String, Object>> getArchiveComments(Long archiveId, Page<Map<String, Object>> page);

    /**
     * 删除档案评论
     * @param archiveId 档案ID
     * @param commentId 评论ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteArchiveComment(Long archiveId, Long commentId, Long deletedBy);

    /**
     * 收藏档案
     * @param archiveId 档案ID
     * @param userId 用户ID
     * @return 是否收藏成功
     */
    boolean favoriteArchive(Long archiveId, Long userId);

    /**
     * 取消收藏档案
     * @param archiveId 档案ID
     * @param userId 用户ID
     * @return 是否取消收藏成功
     */
    boolean unfavoriteArchive(Long archiveId, Long userId);

    /**
     * 获取用户收藏的档案列表
     * @param userId 用户ID
     * @param page 分页参数
     * @return 档案列表
     */
    IPage<Archive> getUserFavoriteArchives(Long userId, Page<Archive> page);

    /**
     * 检查档案是否被用户收藏
     * @param archiveId 档案ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    boolean isArchiveFavorited(Long archiveId, Long userId);

    /**
     * 生成档案报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param reportType 报告类型
     * @return 报告数据
     */
    Map<String, Object> generateArchiveReport(LocalDateTime startDate, LocalDateTime endDate, String reportType);

    /**
     * 清理过期档案
     * @param days 过期天数
     * @return 清理的数量
     */
    int cleanupExpiredArchives(int days);

    /**
     * 备份档案数据
     * @param archiveIds 档案ID列表
     * @param backupPath 备份路径
     * @return 备份结果
     */
    Map<String, Object> backupArchives(List<Long> archiveIds, String backupPath);

    /**
     * 恢复档案数据
     * @param backupFile 备份文件
     * @param restoreOptions 恢复选项
     * @return 恢复结果
     */
    Map<String, Object> restoreArchives(String backupFile, Map<String, Object> restoreOptions);

    /**
     * 刷新档案缓存
     * @param archiveId 档案ID（可选，为空则刷新所有）
     * @return 是否刷新成功
     */
    boolean refreshArchiveCache(Long archiveId);

    /**
     * 重建档案索引
     * @param archiveIds 档案ID列表（可选，为空则重建所有）
     * @return 重建结果
     */
    Map<String, Object> rebuildArchiveIndex(List<Long> archiveIds);

    // ==================== 档案消息监听器专用方法 ====================
    
    /**
     * 初始化档案索引
     * @param archiveId 档案ID
     */
    void initializeArchiveIndex(Long archiveId);
    
    /**
     * 设置档案默认权限
     * @param archiveId 档案ID
     */
    void setupDefaultPermissions(Long archiveId);
    
    /**
     * 触发档案工作流
     * @param archiveId 档案ID
     */
    void triggerArchiveWorkflow(Long archiveId);
    
    /**
     * 更新档案索引
     * @param archiveId 档案ID
     */
    void updateArchiveIndex(Long archiveId);
    
    /**
     * 检查是否需要重新审批
     * @param archiveId 档案ID
     * @param oldData 旧数据
     * @param newData 新数据
     * @return 是否需要重新审批
     */
    boolean needsReapproval(Long archiveId, com.fasterxml.jackson.databind.JsonNode oldData, com.fasterxml.jackson.databind.JsonNode newData);
    
    /**
     * 清理档案索引
     * @param archiveId 档案ID
     */
    void cleanupArchiveIndex(Long archiveId);
    
    /**
     * 清理档案文件
     * @param archiveId 档案ID
     */
    void cleanupArchiveFiles(Long archiveId);
    
    /**
     * 更新档案状态
     * @param archiveId 档案ID
     * @param status 状态
     */
    void updateArchiveStatus(Long archiveId, String status);
    
    /**
     * 移动档案到归档存储
     * @param archiveId 档案ID
     * @param storagePath 存储路径
     */
    void moveToArchiveStorage(Long archiveId, String storagePath);
    
    /**
     * 设置发布权限
     * @param archiveId 档案ID
     * @param publishLevel 发布级别
     */
    void setupPublishPermissions(Long archiveId, String publishLevel);
    
    /**
     * 更新档案统计信息
     * @param archiveId 档案ID
     */
    void updateArchiveStatistics(Long archiveId);
    
    /**
     * 触发状态变更工作流
     * @param archiveId 档案ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    void triggerStatusChangeWorkflow(Long archiveId, String oldStatus, String newStatus);
    
    /**
     * 更新状态变更相关权限
     * @param archiveId 档案ID
     * @param newStatus 新状态
     */
    void updatePermissionsForStatusChange(Long archiveId, String newStatus);
    
    /**
     * 更新状态统计信息
     * @param archiveId 档案ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    void updateStatusStatistics(Long archiveId, String oldStatus, String newStatus);
    
    /**
     * 更新权限缓存
     * @param archiveId 档案ID
     */
    void updatePermissionCache(Long archiveId);
    
    /**
     * 记录权限变更历史
     * @param archiveId 档案ID
     * @param operatorId 操作人ID
     * @param changeType 变更类型
     * @param changes 变更内容
     */
    void recordPermissionChangeHistory(Long archiveId, Long operatorId, String changeType, com.fasterxml.jackson.databind.JsonNode changes);
    
    /**
     * 更新删除统计信息
     * @param archiveId 档案ID
     */
    void updateDeleteStatistics(Long archiveId);
}