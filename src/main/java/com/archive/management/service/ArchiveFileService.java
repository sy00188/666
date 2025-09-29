package com.archive.management.service;

import com.archive.management.entity.ArchiveFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案文件业务服务接口
 * 定义档案文件管理的核心业务方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public interface ArchiveFileService {

    /**
     * 创建档案文件记录
     * @param archiveFile 档案文件信息
     * @return 创建的档案文件
     */
    ArchiveFile createArchiveFile(ArchiveFile archiveFile);

    /**
     * 根据ID获取档案文件
     * @param id 档案文件ID
     * @return 档案文件信息
     */
    ArchiveFile getArchiveFileById(Long id);

    /**
     * 根据文件名获取档案文件
     * @param fileName 文件名
     * @return 档案文件信息
     */
    ArchiveFile getArchiveFileByName(String fileName);

    /**
     * 根据文件路径获取档案文件
     * @param filePath 文件路径
     * @return 档案文件信息
     */
    ArchiveFile getArchiveFileByPath(String filePath);

    /**
     * 根据MD5获取档案文件
     * @param md5 文件MD5值
     * @return 档案文件信息
     */
    ArchiveFile getArchiveFileByMd5(String md5);

    /**
     * 更新档案文件信息
     * @param archiveFile 档案文件信息
     * @return 更新后的档案文件
     */
    ArchiveFile updateArchiveFile(ArchiveFile archiveFile);

    /**
     * 删除档案文件（软删除）
     * @param id 档案文件ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean deleteArchiveFile(Long id, Long deletedBy);

    /**
     * 批量删除档案文件（软删除）
     * @param ids 档案文件ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchDeleteArchiveFiles(List<Long> ids, Long deletedBy);

    /**
     * 物理删除档案文件
     * @param id 档案文件ID
     * @param deletedBy 删除人ID
     * @return 是否删除成功
     */
    boolean physicalDeleteArchiveFile(Long id, Long deletedBy);

    /**
     * 批量物理删除档案文件
     * @param ids 档案文件ID列表
     * @param deletedBy 删除人ID
     * @return 删除成功的数量
     */
    int batchPhysicalDeleteArchiveFiles(List<Long> ids, Long deletedBy);

    /**
     * 启用档案文件
     * @param id 档案文件ID
     * @param updatedBy 更新人ID
     * @return 是否启用成功
     */
    boolean enableArchiveFile(Long id, Long updatedBy);

    /**
     * 禁用档案文件
     * @param id 档案文件ID
     * @param updatedBy 更新人ID
     * @return 是否禁用成功
     */
    boolean disableArchiveFile(Long id, Long updatedBy);

    /**
     * 批量更新档案文件状态
     * @param ids 档案文件ID列表
     * @param status 新状态
     * @param updatedBy 更新人ID
     * @return 更新成功的数量
     */
    int batchUpdateArchiveFileStatus(List<Long> ids, Integer status, Long updatedBy);

    /**
     * 检查文件名是否存在
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean existsByFileName(String fileName);

    /**
     * 检查文件路径是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean existsByFilePath(String filePath);

    /**
     * 检查MD5是否存在
     * @param md5 MD5值
     * @return 是否存在
     */
    boolean existsByMd5(String md5);

    /**
     * 分页查询档案文件
     * @param page 分页参数
     * @param archiveId 档案ID（可选）
     * @param fileName 文件名（可选）
     * @param fileType 文件类型（可选）
     * @param status 状态（可选）
     * @param uploadedBy 上传人ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页结果
     */
    IPage<ArchiveFile> findArchiveFilesWithPagination(Page<ArchiveFile> page, Long archiveId, 
                                                     String fileName, String fileType, Integer status, 
                                                     Long uploadedBy, LocalDateTime startDate, 
                                                     LocalDateTime endDate);

    /**
     * 根据档案ID查找文件列表
     * @param archiveId 档案ID
     * @return 文件列表
     */
    List<ArchiveFile> findArchiveFilesByArchiveId(Long archiveId);

    /**
     * 根据文件类型查找文件列表
     * @param fileType 文件类型
     * @return 文件列表
     */
    List<ArchiveFile> findArchiveFilesByType(String fileType);

    /**
     * 根据状态查找文件列表
     * @param status 状态
     * @return 文件列表
     */
    List<ArchiveFile> findArchiveFilesByStatus(Integer status);

    /**
     * 根据上传人查找文件列表
     * @param uploadedBy 上传人ID
     * @return 文件列表
     */
    List<ArchiveFile> findArchiveFilesByUploadedBy(Long uploadedBy);

    /**
     * 根据文件大小范围查找文件列表
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @return 文件列表
     */
    List<ArchiveFile> findArchiveFilesBySizeRange(Long minSize, Long maxSize);

    /**
     * 根据上传日期范围查找文件列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 文件列表
     */
    List<ArchiveFile> findArchiveFilesByUploadDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计档案文件总数
     * @return 总数
     */
    long countArchiveFiles();

    /**
     * 根据档案统计文件数量
     * @param archiveId 档案ID
     * @return 数量
     */
    long countArchiveFilesByArchiveId(Long archiveId);

    /**
     * 根据文件类型统计文件数量
     * @param fileType 文件类型
     * @return 数量
     */
    long countArchiveFilesByType(String fileType);

    /**
     * 根据状态统计文件数量
     * @param status 状态
     * @return 数量
     */
    long countArchiveFilesByStatus(Integer status);

    /**
     * 根据上传人统计文件数量
     * @param uploadedBy 上传人ID
     * @return 数量
     */
    long countArchiveFilesByUploadedBy(Long uploadedBy);

    /**
     * 统计文件总大小
     * @return 总大小（字节）
     */
    long getTotalFileSize();

    /**
     * 根据档案统计文件总大小
     * @param archiveId 档案ID
     * @return 总大小（字节）
     */
    long getTotalFileSizeByArchiveId(Long archiveId);

    /**
     * 根据文件类型统计文件总大小
     * @param fileType 文件类型
     * @return 总大小（字节）
     */
    long getTotalFileSizeByType(String fileType);

    /**
     * 获取文件类型统计
     * @return 类型统计结果
     */
    List<Map<String, Object>> getFileTypeStatistics();

    /**
     * 获取文件状态统计
     * @return 状态统计结果
     */
    List<Map<String, Object>> getFileStatusStatistics();

    /**
     * 获取文件大小分布统计
     * @return 大小分布统计结果
     */
    List<Map<String, Object>> getFileSizeDistributionStatistics();

    /**
     * 获取文件上传趋势统计
     * @param days 统计天数
     * @return 趋势统计结果
     */
    List<Map<String, Object>> getFileUploadTrend(int days);

    /**
     * 上传文件
     * @param archiveId 档案ID
     * @param file 文件
     * @param uploadedBy 上传人ID
     * @return 文件信息
     */
    ArchiveFile uploadFile(Long archiveId, MultipartFile file, Long uploadedBy);

    /**
     * 批量上传文件
     * @param archiveId 档案ID
     * @param files 文件列表
     * @param uploadedBy 上传人ID
     * @return 上传结果
     */
    List<ArchiveFile> batchUploadFiles(Long archiveId, List<MultipartFile> files, Long uploadedBy);

    /**
     * 下载文件
     * @param id 文件ID
     * @param userId 用户ID
     * @return 文件下载信息
     */
    Map<String, Object> downloadFile(Long id, Long userId);

    /**
     * 预览文件
     * @param id 文件ID
     * @param userId 用户ID
     * @return 预览信息
     */
    Map<String, Object> previewFile(Long id, Long userId);

    /**
     * 生成文件缩略图
     * @param id 文件ID
     * @return 缩略图信息
     */
    Map<String, Object> generateThumbnail(Long id);

    /**
     * 获取文件信息
     * @param id 文件ID
     * @return 文件详细信息
     */
    Map<String, Object> getFileInfo(Long id);

    /**
     * 检查文件完整性
     * @param id 文件ID
     * @return 检查结果
     */
    Map<String, Object> checkFileIntegrity(Long id);

    /**
     * 修复损坏的文件
     * @param id 文件ID
     * @param repairedBy 修复人ID
     * @return 修复结果
     */
    Map<String, Object> repairCorruptedFile(Long id, Long repairedBy);

    /**
     * 计算文件MD5
     * @param id 文件ID
     * @return MD5值
     */
    String calculateFileMd5(Long id);

    /**
     * 重新计算文件MD5
     * @param id 文件ID
     * @param updatedBy 更新人ID
     * @return 是否重新计算成功
     */
    boolean recalculateFileMd5(Long id, Long updatedBy);

    /**
     * 批量重新计算文件MD5
     * @param ids 文件ID列表
     * @param updatedBy 更新人ID
     * @return 重新计算成功的数量
     */
    int batchRecalculateFileMd5(List<Long> ids, Long updatedBy);

    /**
     * 检测重复文件
     * @return 重复文件列表
     */
    List<Map<String, Object>> detectDuplicateFiles();

    /**
     * 清理重复文件
     * @param keepStrategy 保留策略（latest/oldest/largest/smallest）
     * @param deletedBy 删除人ID
     * @return 清理结果
     */
    Map<String, Object> cleanupDuplicateFiles(String keepStrategy, Long deletedBy);

    /**
     * 移动文件到新位置
     * @param id 文件ID
     * @param newPath 新路径
     * @param updatedBy 更新人ID
     * @return 是否移动成功
     */
    boolean moveFile(Long id, String newPath, Long updatedBy);

    /**
     * 批量移动文件
     * @param filePathMap 文件路径映射（文件ID -> 新路径）
     * @param updatedBy 更新人ID
     * @return 移动成功的数量
     */
    int batchMoveFiles(Map<Long, String> filePathMap, Long updatedBy);

    /**
     * 复制文件
     * @param id 文件ID
     * @param newArchiveId 新档案ID
     * @param newFileName 新文件名
     * @param copiedBy 复制人ID
     * @return 新文件信息
     */
    ArchiveFile copyFile(Long id, Long newArchiveId, String newFileName, Long copiedBy);

    /**
     * 批量复制文件
     * @param ids 文件ID列表
     * @param targetArchiveId 目标档案ID
     * @param copiedBy 复制人ID
     * @return 复制成功的数量
     */
    int batchCopyFiles(List<Long> ids, Long targetArchiveId, Long copiedBy);

    /**
     * 重命名文件
     * @param id 文件ID
     * @param newFileName 新文件名
     * @param updatedBy 更新人ID
     * @return 是否重命名成功
     */
    boolean renameFile(Long id, String newFileName, Long updatedBy);

    /**
     * 压缩文件
     * @param ids 文件ID列表
     * @param compressionType 压缩类型（zip/rar/7z）
     * @param compressedBy 压缩人ID
     * @return 压缩文件信息
     */
    Map<String, Object> compressFiles(List<Long> ids, String compressionType, Long compressedBy);

    /**
     * 解压文件
     * @param id 压缩文件ID
     * @param targetArchiveId 目标档案ID
     * @param extractedBy 解压人ID
     * @return 解压结果
     */
    Map<String, Object> extractFile(Long id, Long targetArchiveId, Long extractedBy);

    /**
     * 转换文件格式
     * @param id 文件ID
     * @param targetFormat 目标格式
     * @param convertedBy 转换人ID
     * @return 转换结果
     */
    Map<String, Object> convertFileFormat(Long id, String targetFormat, Long convertedBy);

    /**
     * 获取文件版本历史
     * @param id 文件ID
     * @return 版本历史列表
     */
    List<Map<String, Object>> getFileVersionHistory(Long id);

    /**
     * 创建文件版本
     * @param id 文件ID
     * @param versionNote 版本说明
     * @param createdBy 创建人ID
     * @return 版本信息
     */
    Map<String, Object> createFileVersion(Long id, String versionNote, Long createdBy);

    /**
     * 恢复文件版本
     * @param id 文件ID
     * @param versionId 版本ID
     * @param restoredBy 恢复人ID
     * @return 是否恢复成功
     */
    boolean restoreFileVersion(Long id, Long versionId, Long restoredBy);

    /**
     * 比较文件版本
     * @param id 文件ID
     * @param version1Id 版本1ID
     * @param version2Id 版本2ID
     * @return 比较结果
     */
    Map<String, Object> compareFileVersions(Long id, Long version1Id, Long version2Id);

    /**
     * 设置文件权限
     * @param id 文件ID
     * @param permissions 权限设置
     * @param updatedBy 更新人ID
     * @return 是否设置成功
     */
    boolean setFilePermissions(Long id, Map<String, Object> permissions, Long updatedBy);

    /**
     * 检查用户文件访问权限
     * @param userId 用户ID
     * @param fileId 文件ID
     * @param action 操作类型（read/write/delete）
     * @return 是否有权限
     */
    boolean checkUserFileAccess(Long userId, Long fileId, String action);

    /**
     * 加密文件
     * @param id 文件ID
     * @param encryptionType 加密类型
     * @param password 密码
     * @param encryptedBy 加密人ID
     * @return 加密结果
     */
    Map<String, Object> encryptFile(Long id, String encryptionType, String password, Long encryptedBy);

    /**
     * 解密文件
     * @param id 文件ID
     * @param password 密码
     * @param decryptedBy 解密人ID
     * @return 解密结果
     */
    Map<String, Object> decryptFile(Long id, String password, Long decryptedBy);

    /**
     * 获取文件访问日志
     * @param id 文件ID
     * @param days 天数
     * @return 访问日志
     */
    List<Map<String, Object>> getFileAccessLogs(Long id, int days);

    /**
     * 增加文件下载次数
     * @param id 文件ID
     * @param userId 用户ID（可选）
     * @return 是否增加成功
     */
    boolean incrementDownloadCount(Long id, Long userId);

    /**
     * 增加文件查看次数
     * @param id 文件ID
     * @param userId 用户ID（可选）
     * @return 是否增加成功
     */
    boolean incrementViewCount(Long id, Long userId);

    /**
     * 获取热门文件
     * @param limit 数量限制
     * @return 文件列表
     */
    List<ArchiveFile> getPopularFiles(int limit);

    /**
     * 获取最新文件
     * @param limit 数量限制
     * @return 文件列表
     */
    List<ArchiveFile> getLatestFiles(int limit);

    /**
     * 获取大文件列表
     * @param minSize 最小大小
     * @param limit 数量限制
     * @return 文件列表
     */
    List<ArchiveFile> getLargeFiles(Long minSize, int limit);

    /**
     * 获取孤立文件列表（没有关联档案的文件）
     * @return 文件列表
     */
    List<ArchiveFile> getOrphanFiles();

    /**
     * 清理孤立文件
     * @param deletedBy 删除人ID
     * @return 清理的数量
     */
    int cleanupOrphanFiles(Long deletedBy);

    /**
     * 清理过期文件
     * @param days 过期天数
     * @param deletedBy 删除人ID
     * @return 清理的数量
     */
    int cleanupExpiredFiles(int days, Long deletedBy);

    /**
     * 备份文件
     * @param ids 文件ID列表
     * @param backupPath 备份路径
     * @return 备份结果
     */
    Map<String, Object> backupFiles(List<Long> ids, String backupPath);

    /**
     * 恢复文件
     * @param backupFile 备份文件
     * @param restoreOptions 恢复选项
     * @return 恢复结果
     */
    Map<String, Object> restoreFiles(String backupFile, Map<String, Object> restoreOptions);

    /**
     * 同步文件系统
     * @param archiveId 档案ID（可选，为空则同步所有）
     * @return 同步结果
     */
    Map<String, Object> syncFileSystem(Long archiveId);

    /**
     * 扫描病毒
     * @param id 文件ID
     * @return 扫描结果
     */
    Map<String, Object> scanVirus(Long id);

    /**
     * 批量扫描病毒
     * @param ids 文件ID列表
     * @return 扫描结果
     */
    Map<String, Object> batchScanVirus(List<Long> ids);

    /**
     * 隔离感染文件
     * @param id 文件ID
     * @param quarantinedBy 隔离人ID
     * @return 是否隔离成功
     */
    boolean quarantineFile(Long id, Long quarantinedBy);

    /**
     * 生成文件报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param reportType 报告类型
     * @return 报告数据
     */
    Map<String, Object> generateFileReport(LocalDateTime startDate, LocalDateTime endDate, String reportType);

    /**
     * 刷新文件缓存
     * @param id 文件ID（可选，为空则刷新所有）
     * @return 是否刷新成功
     */
    boolean refreshFileCache(Long id);

    /**
     * 重建文件索引
     * @param ids 文件ID列表（可选，为空则重建所有）
     * @return 重建结果
     */
    Map<String, Object> rebuildFileIndex(List<Long> ids);
}