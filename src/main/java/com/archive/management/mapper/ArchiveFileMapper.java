package com.archive.management.mapper;

import com.archive.management.entity.ArchiveFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案文件Mapper接口
 * 基于MyBatis-Plus的档案文件数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface ArchiveFileMapper extends BaseMapper<ArchiveFile> {

    /**
     * 根据文件名查找档案文件
     * 
     * @param fileName 文件名
     * @return 档案文件信息
     */
    @Select("SELECT * FROM archive_file WHERE file_name = #{fileName} AND deleted = 0")
    ArchiveFile findByFileName(@Param("fileName") String fileName);

    /**
     * 根据文件路径查找档案文件
     * 
     * @param filePath 文件路径
     * @return 档案文件信息
     */
    @Select("SELECT * FROM archive_file WHERE file_path = #{filePath} AND deleted = 0")
    ArchiveFile findByFilePath(@Param("filePath") String filePath);

    /**
     * 根据文件MD5查找档案文件
     * 
     * @param fileMd5 文件MD5
     * @return 档案文件信息
     */
    @Select("SELECT * FROM archive_file WHERE file_md5 = #{fileMd5} AND deleted = 0")
    ArchiveFile findByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据档案ID查找档案文件列表
     * 
     * @param archiveId 档案ID
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE archive_id = #{archiveId} AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByArchiveId(@Param("archiveId") Long archiveId);

    /**
     * 根据文件类型查找档案文件列表
     * 
     * @param fileType 文件类型
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE file_type = #{fileType} AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByFileType(@Param("fileType") Integer fileType);

    /**
     * 根据文件扩展名查找档案文件列表
     * 
     * @param fileExtension 文件扩展名
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE file_extension = #{fileExtension} AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByFileExtension(@Param("fileExtension") String fileExtension);

    /**
     * 根据状态查找档案文件列表
     * 
     * @param status 状态
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByStatus(@Param("status") Integer status);

    /**
     * 根据存储类型查找档案文件列表
     * 
     * @param storageType 存储类型
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE storage_type = #{storageType} AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByStorageType(@Param("storageType") Integer storageType);

    /**
     * 根据文件大小范围查找档案文件列表
     * 
     * @param minSize 最小文件大小
     * @param maxSize 最大文件大小
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE file_size BETWEEN #{minSize} AND #{maxSize} " +
            "AND deleted = 0 ORDER BY file_size DESC")
    List<ArchiveFile> findByFileSizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

    /**
     * 根据创建时间范围查找档案文件列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 根据上传人查找档案文件列表
     * 
     * @param uploadedBy 上传人ID
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE uploaded_by = #{uploadedBy} AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findByUploadedBy(@Param("uploadedBy") Long uploadedBy);

    /**
     * 模糊搜索档案文件
     * 
     * @param keyword 关键词
     * @return 档案文件列表
     */
    @Select("SELECT * FROM archive_file WHERE (file_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR original_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> searchArchiveFiles(@Param("keyword") String keyword);

    /**
     * 分页模糊搜索档案文件
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM archive_file WHERE (file_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR original_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    IPage<ArchiveFile> searchArchiveFilesWithPagination(Page<ArchiveFile> page, @Param("keyword") String keyword);

    /**
     * 查找重复文件（相同MD5）
     * 
     * @return 重复文件列表
     */
    @Select("SELECT * FROM archive_file WHERE file_md5 IN (" +
            "  SELECT file_md5 FROM archive_file WHERE deleted = 0 " +
            "  GROUP BY file_md5 HAVING COUNT(*) > 1" +
            ") AND deleted = 0 ORDER BY file_md5, create_time DESC")
    List<ArchiveFile> findDuplicateFiles();

    /**
     * 查找大文件
     * 
     * @param minSize 最小文件大小
     * @return 大文件列表
     */
    @Select("SELECT * FROM archive_file WHERE file_size >= #{minSize} AND deleted = 0 ORDER BY file_size DESC")
    List<ArchiveFile> findLargeFiles(@Param("minSize") Long minSize);

    /**
     * 查找小文件
     * 
     * @param maxSize 最大文件大小
     * @return 小文件列表
     */
    @Select("SELECT * FROM archive_file WHERE file_size <= #{maxSize} AND deleted = 0 ORDER BY file_size ASC")
    List<ArchiveFile> findSmallFiles(@Param("maxSize") Long maxSize);

    /**
     * 查找最近上传的文件
     * 
     * @param days 天数
     * @return 最近上传的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findRecentUploadedFiles(@Param("days") int days);

    /**
     * 查找最近下载的文件
     * 
     * @param days 天数
     * @return 最近下载的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE last_download_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 ORDER BY last_download_time DESC")
    List<ArchiveFile> findRecentDownloadedFiles(@Param("days") int days);

    /**
     * 查找热门下载文件
     * 
     * @param limit 限制数量
     * @return 热门下载文件列表
     */
    @Select("SELECT * FROM archive_file WHERE download_count > 0 AND deleted = 0 " +
            "ORDER BY download_count DESC, last_download_time DESC LIMIT #{limit}")
    List<ArchiveFile> findPopularDownloadFiles(@Param("limit") int limit);

    /**
     * 查找未下载的文件
     * 
     * @return 未下载的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE download_count = 0 AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findNeverDownloadedFiles();

    /**
     * 查找长时间未访问的文件
     * 
     * @param days 天数
     * @return 长时间未访问的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE (last_download_time IS NULL OR last_download_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)) " +
            "AND create_time < DATE_SUB(NOW(), INTERVAL #{days} DAY) AND deleted = 0 ORDER BY create_time ASC")
    List<ArchiveFile> findLongTimeNotAccessedFiles(@Param("days") int days);

    /**
     * 查找损坏的文件
     * 
     * @return 损坏的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE status = 3 AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findCorruptedFiles();

    /**
     * 查找处理中的文件
     * 
     * @return 处理中的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE status = 2 AND deleted = 0 ORDER BY create_time DESC")
    List<ArchiveFile> findProcessingFiles();

    /**
     * 查找待处理的文件
     * 
     * @return 待处理的文件列表
     */
    @Select("SELECT * FROM archive_file WHERE status = 0 AND deleted = 0 ORDER BY create_time ASC")
    List<ArchiveFile> findPendingFiles();

    /**
     * 统计档案文件总数
     * 
     * @return 档案文件总数
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE deleted = 0")
    long countArchiveFiles();

    /**
     * 根据状态统计档案文件数量
     * 
     * @param status 状态
     * @return 档案文件数量
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据文件类型统计档案文件数量
     * 
     * @param fileType 文件类型
     * @return 档案文件数量
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE file_type = #{fileType} AND deleted = 0")
    long countByFileType(@Param("fileType") Integer fileType);

    /**
     * 根据存储类型统计档案文件数量
     * 
     * @param storageType 存储类型
     * @return 档案文件数量
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE storage_type = #{storageType} AND deleted = 0")
    long countByStorageType(@Param("storageType") Integer storageType);

    /**
     * 统计总文件大小
     * 
     * @return 总文件大小
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM archive_file WHERE deleted = 0")
    long getTotalFileSize();

    /**
     * 根据文件类型统计总文件大小
     * 
     * @param fileType 文件类型
     * @return 总文件大小
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM archive_file WHERE file_type = #{fileType} AND deleted = 0")
    long getTotalFileSizeByType(@Param("fileType") Integer fileType);

    /**
     * 统计今日上传文件数量
     * 
     * @return 今日上传文件数量
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    long countTodayUploadedFiles();

    /**
     * 统计本周上传文件数量
     * 
     * @return 本周上传文件数量
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) AND deleted = 0")
    long countThisWeekUploadedFiles();

    /**
     * 统计本月上传文件数量
     * 
     * @return 本月上传文件数量
     */
    @Select("SELECT COUNT(*) FROM archive_file WHERE YEAR(create_time) = YEAR(NOW()) " +
            "AND MONTH(create_time) = MONTH(NOW()) AND deleted = 0")
    long countThisMonthUploadedFiles();

    /**
     * 统计今日下载次数
     * 
     * @return 今日下载次数
     */
    @Select("SELECT COALESCE(SUM(download_count), 0) FROM archive_file " +
            "WHERE DATE(last_download_time) = CURDATE() AND deleted = 0")
    long countTodayDownloads();

    /**
     * 统计总下载次数
     * 
     * @return 总下载次数
     */
    @Select("SELECT COALESCE(SUM(download_count), 0) FROM archive_file WHERE deleted = 0")
    long getTotalDownloadCount();

    /**
     * 批量更新文件状态
     * 
     * @param fileIds 文件ID列表
     * @param status 状态
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE archive_file SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='fileIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("fileIds") List<Long> fileIds, @Param("status") Integer status);

    /**
     * 批量删除文件（软删除）
     * 
     * @param fileIds 文件ID列表
     * @param deletedBy 删除人ID
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE archive_file SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='fileIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteFiles(@Param("fileIds") List<Long> fileIds, @Param("deletedBy") Long deletedBy);

    /**
     * 增加文件下载次数
     * 
     * @param fileId 文件ID
     * @return 更新数量
     */
    @Update("UPDATE archive_file SET download_count = download_count + 1, " +
            "last_download_time = NOW(), update_time = NOW() WHERE id = #{fileId}")
    int incrementDownloadCount(@Param("fileId") Long fileId);

    /**
     * 更新文件访问时间
     * 
     * @param fileId 文件ID
     * @return 更新数量
     */
    @Update("UPDATE archive_file SET last_access_time = NOW(), update_time = NOW() WHERE id = #{fileId}")
    int updateLastAccessTime(@Param("fileId") Long fileId);

    /**
     * 更新文件状态
     * 
     * @param fileId 文件ID
     * @param status 状态
     * @return 更新数量
     */
    @Update("UPDATE archive_file SET status = #{status}, update_time = NOW() WHERE id = #{fileId}")
    int updateFileStatus(@Param("fileId") Long fileId, @Param("status") Integer status);

    /**
     * 更新文件路径
     * 
     * @param fileId 文件ID
     * @param filePath 文件路径
     * @return 更新数量
     */
    @Update("UPDATE archive_file SET file_path = #{filePath}, update_time = NOW() WHERE id = #{fileId}")
    int updateFilePath(@Param("fileId") Long fileId, @Param("filePath") String filePath);

    /**
     * 更新文件描述
     * 
     * @param fileId 文件ID
     * @param description 描述
     * @return 更新数量
     */
    @Update("UPDATE archive_file SET description = #{description}, update_time = NOW() WHERE id = #{fileId}")
    int updateFileDescription(@Param("fileId") Long fileId, @Param("description") String description);

    /**
     * 更新文件标签
     * 
     * @param fileId 文件ID
     * @param tags 标签
     * @return 更新数量
     */
    @Update("UPDATE archive_file SET tags = #{tags}, update_time = NOW() WHERE id = #{fileId}")
    int updateFileTags(@Param("fileId") Long fileId, @Param("tags") String tags);

    /**
     * 清理临时文件
     * 
     * @param hours 小时数
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE archive_file SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE status = 0 AND create_time < DATE_SUB(NOW(), INTERVAL #{hours} HOUR) AND deleted = 0")
    int cleanTempFiles(@Param("hours") int hours, @Param("deletedBy") Long deletedBy);

    /**
     * 清理损坏文件
     * 
     * @param days 天数
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE archive_file SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE status = 3 AND create_time < DATE_SUB(NOW(), INTERVAL #{days} DAY) AND deleted = 0")
    int cleanCorruptedFiles(@Param("days") int days, @Param("deletedBy") Long deletedBy);

    /**
     * 清理长时间未访问的文件
     * 
     * @param days 天数
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE archive_file SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE (last_access_time IS NULL OR last_access_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)) " +
            "AND create_time < DATE_SUB(NOW(), INTERVAL #{days} DAY) AND download_count = 0 AND deleted = 0")
    int cleanUnusedFiles(@Param("days") int days, @Param("deletedBy") Long deletedBy);

    /**
     * 获取文件统计信息
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalFiles, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as normalFiles, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pendingFiles, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as processingFiles, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as corruptedFiles, " +
            "SUM(CASE WHEN file_type = 1 THEN 1 ELSE 0 END) as documentFiles, " +
            "SUM(CASE WHEN file_type = 2 THEN 1 ELSE 0 END) as imageFiles, " +
            "SUM(CASE WHEN file_type = 3 THEN 1 ELSE 0 END) as videoFiles, " +
            "SUM(CASE WHEN file_type = 4 THEN 1 ELSE 0 END) as audioFiles, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayUploadedFiles, " +
            "SUM(file_size) as totalFileSize, " +
            "SUM(download_count) as totalDownloadCount " +
            "FROM archive_file WHERE deleted = 0")
    Map<String, Object> getFileStatistics();

    /**
     * 获取文件类型统计
     * 
     * @return 文件类型统计
     */
    @Select("SELECT " +
            "CASE file_type " +
            "  WHEN 1 THEN '文档文件' " +
            "  WHEN 2 THEN '图片文件' " +
            "  WHEN 3 THEN '视频文件' " +
            "  WHEN 4 THEN '音频文件' " +
            "  ELSE '其他文件' " +
            "END as fileTypeName, " +
            "COUNT(*) as fileCount, " +
            "SUM(file_size) as totalSize, " +
            "SUM(download_count) as totalDownloads " +
            "FROM archive_file WHERE deleted = 0 " +
            "GROUP BY file_type ORDER BY file_type ASC")
    List<Map<String, Object>> getFileTypeStatistics();

    /**
     * 获取文件扩展名统计
     * 
     * @return 文件扩展名统计
     */
    @Select("SELECT file_extension, COUNT(*) as fileCount, SUM(file_size) as totalSize " +
            "FROM archive_file WHERE deleted = 0 " +
            "GROUP BY file_extension ORDER BY fileCount DESC")
    List<Map<String, Object>> getFileExtensionStatistics();

    /**
     * 获取存储类型统计
     * 
     * @return 存储类型统计
     */
    @Select("SELECT " +
            "CASE storage_type " +
            "  WHEN 1 THEN '本地存储' " +
            "  WHEN 2 THEN '云存储' " +
            "  WHEN 3 THEN '网络存储' " +
            "  ELSE '其他存储' " +
            "END as storageTypeName, " +
            "COUNT(*) as fileCount, " +
            "SUM(file_size) as totalSize " +
            "FROM archive_file WHERE deleted = 0 " +
            "GROUP BY storage_type ORDER BY storage_type ASC")
    List<Map<String, Object>> getStorageTypeStatistics();

    /**
     * 获取文件大小分布统计
     * 
     * @return 文件大小分布统计
     */
    @Select("SELECT " +
            "CASE " +
            "  WHEN file_size < 1024 THEN '< 1KB' " +
            "  WHEN file_size < 1048576 THEN '1KB - 1MB' " +
            "  WHEN file_size < 10485760 THEN '1MB - 10MB' " +
            "  WHEN file_size < 104857600 THEN '10MB - 100MB' " +
            "  WHEN file_size < 1073741824 THEN '100MB - 1GB' " +
            "  ELSE '> 1GB' " +
            "END as sizeRange, " +
            "COUNT(*) as fileCount, " +
            "SUM(file_size) as totalSize " +
            "FROM archive_file WHERE deleted = 0 " +
            "GROUP BY " +
            "CASE " +
            "  WHEN file_size < 1024 THEN 1 " +
            "  WHEN file_size < 1048576 THEN 2 " +
            "  WHEN file_size < 10485760 THEN 3 " +
            "  WHEN file_size < 104857600 THEN 4 " +
            "  WHEN file_size < 1073741824 THEN 5 " +
            "  ELSE 6 " +
            "END " +
            "ORDER BY " +
            "CASE " +
            "  WHEN file_size < 1024 THEN 1 " +
            "  WHEN file_size < 1048576 THEN 2 " +
            "  WHEN file_size < 10485760 THEN 3 " +
            "  WHEN file_size < 104857600 THEN 4 " +
            "  WHEN file_size < 1073741824 THEN 5 " +
            "  ELSE 6 " +
            "END")
    List<Map<String, Object>> getFileSizeDistributionStatistics();

    /**
     * 获取文件上传趋势统计
     * 
     * @param days 天数
     * @return 上传趋势统计
     */
    @Select("SELECT DATE(create_time) as uploadDate, COUNT(*) as fileCount, SUM(file_size) as totalSize " +
            "FROM archive_file WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 GROUP BY DATE(create_time) ORDER BY uploadDate ASC")
    List<Map<String, Object>> getFileUploadTrendStatistics(@Param("days") int days);

    /**
     * 获取文件下载趋势统计
     * 
     * @param days 天数
     * @return 下载趋势统计
     */
    @Select("SELECT DATE(last_download_time) as downloadDate, COUNT(*) as fileCount, SUM(download_count) as totalDownloads " +
            "FROM archive_file WHERE last_download_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 GROUP BY DATE(last_download_time) ORDER BY downloadDate ASC")
    List<Map<String, Object>> getFileDownloadTrendStatistics(@Param("days") int days);

    /**
     * 获取热门文件统计
     * 
     * @param limit 限制数量
     * @return 热门文件统计
     */
    @Select("SELECT file_name, original_name, download_count, file_size, last_download_time " +
            "FROM archive_file WHERE download_count > 0 AND deleted = 0 " +
            "ORDER BY download_count DESC, last_download_time DESC LIMIT #{limit}")
    List<Map<String, Object>> getPopularFileStatistics(@Param("limit") int limit);

    /**
     * 获取最大文件统计
     * 
     * @param limit 限制数量
     * @return 最大文件统计
     */
    @Select("SELECT file_name, original_name, file_size, download_count, create_time " +
            "FROM archive_file WHERE deleted = 0 " +
            "ORDER BY file_size DESC LIMIT #{limit}")
    List<Map<String, Object>> getLargestFileStatistics(@Param("limit") int limit);

    /**
     * 获取用户上传统计
     * 
     * @return 用户上传统计
     */
    @Select("SELECT uploaded_by, COUNT(*) as fileCount, SUM(file_size) as totalSize " +
            "FROM archive_file WHERE deleted = 0 " +
            "GROUP BY uploaded_by ORDER BY fileCount DESC")
    List<Map<String, Object>> getUserUploadStatistics();

    /**
     * 获取重复文件统计
     * 
     * @return 重复文件统计
     */
    @Select("SELECT file_md5, COUNT(*) as duplicateCount, SUM(file_size) as wastedSize " +
            "FROM archive_file WHERE deleted = 0 " +
            "GROUP BY file_md5 HAVING COUNT(*) > 1 " +
            "ORDER BY duplicateCount DESC, wastedSize DESC")
    List<Map<String, Object>> getDuplicateFileStatistics();
}