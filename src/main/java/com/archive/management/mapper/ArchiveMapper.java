package com.archive.management.mapper;

import com.archive.management.entity.Archive;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 档案Mapper接口
 * 基于MyBatis-Plus的档案数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface ArchiveMapper extends BaseMapper<Archive> {

    /**
     * 根据档案编号查找档案
     * 
     * @param archiveNumber 档案编号
     * @return 档案信息
     */
    @Select("SELECT * FROM archive WHERE archive_number = #{archiveNumber} AND deleted = 0")
    Archive findByArchiveNumber(@Param("archiveNumber") String archiveNumber);

    /**
     * 根据档案标题查找档案
     * 
     * @param title 档案标题
     * @return 档案信息
     */
    @Select("SELECT * FROM archive WHERE title = #{title} AND deleted = 0")
    Archive findByTitle(@Param("title") String title);

    /**
     * 检查档案编号是否存在
     * 
     * @param archiveNumber 档案编号
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE archive_number = #{archiveNumber} AND deleted = 0")
    int countByArchiveNumber(@Param("archiveNumber") String archiveNumber);

    /**
     * 检查档案标题是否存在
     * 
     * @param title 档案标题
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE title = #{title} AND deleted = 0")
    int countByTitle(@Param("title") String title);

    /**
     * 根据分类ID查找档案列表
     * 
     * @param categoryId 分类ID
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE category_id = #{categoryId} AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据状态查找档案列表
     * 
     * @param status 档案状态
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findByStatus(@Param("status") Integer status);

    /**
     * 根据档案类型查找档案列表
     * 
     * @param archiveType 档案类型
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE archive_type = #{archiveType} AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findByArchiveType(@Param("archiveType") Integer archiveType);

    /**
     * 根据保密级别查找档案列表
     * 
     * @param securityLevel 保密级别
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE security_level = #{securityLevel} AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findBySecurityLevel(@Param("securityLevel") Integer securityLevel);

    /**
     * 根据创建人查找档案列表
     * 
     * @param createdBy 创建人ID
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE created_by = #{createdBy} AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findByCreatedBy(@Param("createdBy") Long createdBy);

    /**
     * 根据归档时间范围查找档案列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE archive_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY archive_time DESC")
    List<Archive> findByArchiveTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 根据创建时间范围查找档案列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 根据保存期限查找档案列表
     * 
     * @param retentionPeriod 保存期限
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE retention_period = #{retentionPeriod} AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> findByRetentionPeriod(@Param("retentionPeriod") Integer retentionPeriod);

    /**
     * 查找即将到期的档案列表
     * 
     * @param days 天数
     * @return 即将到期的档案列表
     */
    @Select("SELECT * FROM archive WHERE expiry_date IS NOT NULL " +
            "AND expiry_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 ORDER BY expiry_date ASC")
    List<Archive> findExpiringArchives(@Param("days") int days);

    /**
     * 查找已过期的档案列表
     * 
     * @return 已过期的档案列表
     */
    @Select("SELECT * FROM archive WHERE expiry_date IS NOT NULL " +
            "AND expiry_date < NOW() AND deleted = 0 ORDER BY expiry_date ASC")
    List<Archive> findExpiredArchives();

    /**
     * 查找热门档案列表（按查看次数排序）
     * 
     * @param limit 限制数量
     * @return 热门档案列表
     */
    @Select("SELECT * FROM archive WHERE deleted = 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Archive> findPopularArchives(@Param("limit") int limit);

    /**
     * 查找最新档案列表
     * 
     * @param limit 限制数量
     * @return 最新档案列表
     */
    @Select("SELECT * FROM archive WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Archive> findLatestArchives(@Param("limit") int limit);

    /**
     * 查找最近归档的档案列表
     * 
     * @param limit 限制数量
     * @return 最近归档的档案列表
     */
    @Select("SELECT * FROM archive WHERE archive_time IS NOT NULL " +
            "AND deleted = 0 ORDER BY archive_time DESC LIMIT #{limit}")
    List<Archive> findRecentlyArchivedArchives(@Param("limit") int limit);

    /**
     * 模糊搜索档案
     * 
     * @param keyword 关键词
     * @return 档案列表
     */
    @Select("SELECT * FROM archive WHERE (archive_number LIKE CONCAT('%', #{keyword}, '%') " +
            "OR title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR keywords LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Archive> searchArchives(@Param("keyword") String keyword);

    /**
     * 分页模糊搜索档案
     * 
     * @param page 分页对象
     * @param keyword 关键词
     * @return 分页结果
     */
    @Select("SELECT * FROM archive WHERE (archive_number LIKE CONCAT('%', #{keyword}, '%') " +
            "OR title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR keywords LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY create_time DESC")
    IPage<Archive> searchArchivesWithPagination(Page<Archive> page, @Param("keyword") String keyword);

    /**
     * 高级搜索档案
     * 
     * @param params 搜索参数
     * @return 档案列表
     */
    @Select("<script>" +
            "SELECT * FROM archive WHERE deleted = 0 " +
            "<if test='archiveNumber != null and archiveNumber != \"\"'>" +
            "AND archive_number LIKE CONCAT('%', #{archiveNumber}, '%') " +
            "</if>" +
            "<if test='title != null and title != \"\"'>" +
            "AND title LIKE CONCAT('%', #{title}, '%') " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND category_id = #{categoryId} " +
            "</if>" +
            "<if test='archiveType != null'>" +
            "AND archive_type = #{archiveType} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='securityLevel != null'>" +
            "AND security_level = #{securityLevel} " +
            "</if>" +
            "<if test='createdBy != null'>" +
            "AND created_by = #{createdBy} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND create_time <= #{endTime} " +
            "</if>" +
            "<if test='archiveStartTime != null'>" +
            "AND archive_time >= #{archiveStartTime} " +
            "</if>" +
            "<if test='archiveEndTime != null'>" +
            "AND archive_time <= #{archiveEndTime} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Archive> advancedSearchArchives(@Param("archiveNumber") String archiveNumber,
                                        @Param("title") String title,
                                        @Param("categoryId") Long categoryId,
                                        @Param("archiveType") Integer archiveType,
                                        @Param("status") Integer status,
                                        @Param("securityLevel") Integer securityLevel,
                                        @Param("createdBy") Long createdBy,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("archiveStartTime") LocalDateTime archiveStartTime,
                                        @Param("archiveEndTime") LocalDateTime archiveEndTime);

    /**
     * 统计档案总数
     * 
     * @return 档案总数
     */
    @Select("SELECT COUNT(*) FROM archive WHERE deleted = 0")
    long countArchives();

    /**
     * 根据状态统计档案数量
     * 
     * @param status 状态
     * @return 档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE status = #{status} AND deleted = 0")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据档案类型统计档案数量
     * 
     * @param archiveType 档案类型
     * @return 档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE archive_type = #{archiveType} AND deleted = 0")
    long countByArchiveType(@Param("archiveType") Integer archiveType);

    /**
     * 根据分类统计档案数量
     * 
     * @param categoryId 分类ID
     * @return 档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE category_id = #{categoryId} AND deleted = 0")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据保密级别统计档案数量
     * 
     * @param securityLevel 保密级别
     * @return 档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE security_level = #{securityLevel} AND deleted = 0")
    long countBySecurityLevel(@Param("securityLevel") Integer securityLevel);

    /**
     * 根据创建人统计档案数量
     * 
     * @param createdBy 创建人ID
     * @return 档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE created_by = #{createdBy} AND deleted = 0")
    long countByCreatedBy(@Param("createdBy") Long createdBy);

    /**
     * 统计今日新增档案数量
     * 
     * @return 今日新增档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    long countTodayNewArchives();

    /**
     * 统计本周新增档案数量
     * 
     * @return 本周新增档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) AND deleted = 0")
    long countThisWeekNewArchives();

    /**
     * 统计本月新增档案数量
     * 
     * @return 本月新增档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE YEAR(create_time) = YEAR(NOW()) " +
            "AND MONTH(create_time) = MONTH(NOW()) AND deleted = 0")
    long countThisMonthNewArchives();

    /**
     * 统计今日归档数量
     * 
     * @return 今日归档数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE DATE(archive_time) = CURDATE() AND deleted = 0")
    long countTodayArchivedArchives();

    /**
     * 统计即将到期档案数量
     * 
     * @param days 天数
     * @return 即将到期档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE expiry_date IS NOT NULL " +
            "AND expiry_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0")
    long countExpiringArchives(@Param("days") int days);

    /**
     * 统计已过期档案数量
     * 
     * @return 已过期档案数量
     */
    @Select("SELECT COUNT(*) FROM archive WHERE expiry_date IS NOT NULL " +
            "AND expiry_date < NOW() AND deleted = 0")
    long countExpiredArchives();

    /**
     * 批量更新档案状态
     * 
     * @param archiveIds 档案ID列表
     * @param status 状态
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE archive SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='archiveIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("archiveIds") List<Long> archiveIds, @Param("status") Integer status);

    /**
     * 批量删除档案（软删除）
     * 
     * @param archiveIds 档案ID列表
     * @param deletedBy 删除人ID
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE archive SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='archiveIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteArchives(@Param("archiveIds") List<Long> archiveIds, @Param("deletedBy") Long deletedBy);

    /**
     * 批量归档
     * 
     * @param archiveIds 档案ID列表
     * @param archivedBy 归档人ID
     * @return 归档数量
     */
    @Update("<script>" +
            "UPDATE archive SET status = 2, archive_time = NOW(), archived_by = #{archivedBy}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='archiveIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchArchiveArchives(@Param("archiveIds") List<Long> archiveIds, @Param("archivedBy") Long archivedBy);

    /**
     * 增加查看次数
     * 
     * @param archiveId 档案ID
     * @return 更新数量
     */
    @Update("UPDATE archive SET view_count = view_count + 1, last_view_time = NOW() WHERE id = #{archiveId}")
    int incrementViewCount(@Param("archiveId") Long archiveId);

    /**
     * 增加下载次数
     * 
     * @param archiveId 档案ID
     * @return 更新数量
     */
    @Update("UPDATE archive SET download_count = download_count + 1, last_download_time = NOW() WHERE id = #{archiveId}")
    int incrementDownloadCount(@Param("archiveId") Long archiveId);

    /**
     * 更新档案大小
     * 
     * @param archiveId 档案ID
     * @param fileSize 文件大小
     * @return 更新数量
     */
    @Update("UPDATE archive SET file_size = #{fileSize}, update_time = NOW() WHERE id = #{archiveId}")
    int updateFileSize(@Param("archiveId") Long archiveId, @Param("fileSize") Long fileSize);

    /**
     * 更新档案存储路径
     * 
     * @param archiveId 档案ID
     * @param storagePath 存储路径
     * @return 更新数量
     */
    @Update("UPDATE archive SET storage_path = #{storagePath}, update_time = NOW() WHERE id = #{archiveId}")
    int updateStoragePath(@Param("archiveId") Long archiveId, @Param("storagePath") String storagePath);

    /**
     * 更新档案到期时间
     * 
     * @param archiveId 档案ID
     * @param expiryDate 到期时间
     * @return 更新数量
     */
    @Update("UPDATE archive SET expiry_date = #{expiryDate}, update_time = NOW() WHERE id = #{archiveId}")
    int updateExpiryDate(@Param("archiveId") Long archiveId, @Param("expiryDate") LocalDateTime expiryDate);

    /**
     * 获取档案统计信息
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalArchives, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as draftArchives, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as publishedArchives, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as archivedArchives, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as deletedArchives, " +
            "SUM(CASE WHEN archive_type = 1 THEN 1 ELSE 0 END) as documentArchives, " +
            "SUM(CASE WHEN archive_type = 2 THEN 1 ELSE 0 END) as imageArchives, " +
            "SUM(CASE WHEN archive_type = 3 THEN 1 ELSE 0 END) as videoArchives, " +
            "SUM(CASE WHEN archive_type = 4 THEN 1 ELSE 0 END) as audioArchives, " +
            "SUM(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNewArchives, " +
            "SUM(view_count) as totalViewCount, " +
            "SUM(download_count) as totalDownloadCount, " +
            "SUM(file_size) as totalFileSize " +
            "FROM archive WHERE deleted = 0")
    Map<String, Object> getArchiveStatistics();

    /**
     * 获取档案类型统计
     * 
     * @return 档案类型统计
     */
    @Select("SELECT " +
            "CASE archive_type " +
            "  WHEN 1 THEN '文档档案' " +
            "  WHEN 2 THEN '图片档案' " +
            "  WHEN 3 THEN '视频档案' " +
            "  WHEN 4 THEN '音频档案' " +
            "  ELSE '其他档案' " +
            "END as archiveTypeName, " +
            "COUNT(*) as archiveCount, " +
            "SUM(file_size) as totalSize " +
            "FROM archive WHERE deleted = 0 " +
            "GROUP BY archive_type ORDER BY archive_type ASC")
    List<Map<String, Object>> getArchiveTypeStatistics();

    /**
     * 获取档案状态统计
     * 
     * @return 档案状态统计
     */
    @Select("SELECT " +
            "CASE status " +
            "  WHEN 0 THEN '草稿' " +
            "  WHEN 1 THEN '已发布' " +
            "  WHEN 2 THEN '已归档' " +
            "  WHEN 3 THEN '已删除' " +
            "  ELSE '未知状态' " +
            "END as statusName, " +
            "COUNT(*) as archiveCount " +
            "FROM archive WHERE deleted = 0 " +
            "GROUP BY status ORDER BY status ASC")
    List<Map<String, Object>> getArchiveStatusStatistics();

    /**
     * 获取档案分类统计
     * 
     * @return 档案分类统计
     */
    @Select("SELECT c.name as categoryName, COUNT(a.id) as archiveCount, SUM(a.file_size) as totalSize " +
            "FROM archive a LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 AND c.deleted = 0 " +
            "GROUP BY a.category_id, c.name ORDER BY archiveCount DESC")
    List<Map<String, Object>> getArchiveCategoryStatistics();

    /**
     * 获取档案保密级别统计
     * 
     * @return 档案保密级别统计
     */
    @Select("SELECT " +
            "CASE security_level " +
            "  WHEN 1 THEN '公开' " +
            "  WHEN 2 THEN '内部' " +
            "  WHEN 3 THEN '机密' " +
            "  WHEN 4 THEN '绝密' " +
            "  ELSE '未分级' " +
            "END as securityLevelName, " +
            "COUNT(*) as archiveCount " +
            "FROM archive WHERE deleted = 0 " +
            "GROUP BY security_level ORDER BY security_level ASC")
    List<Map<String, Object>> getArchiveSecurityLevelStatistics();

    /**
     * 获取档案创建趋势统计
     * 
     * @param days 天数
     * @return 创建趋势统计
     */
    @Select("SELECT DATE(create_time) as createDate, COUNT(*) as archiveCount, SUM(file_size) as totalSize " +
            "FROM archive WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 GROUP BY DATE(create_time) ORDER BY createDate ASC")
    List<Map<String, Object>> getArchiveCreationTrendStatistics(@Param("days") int days);

    /**
     * 获取档案归档趋势统计
     * 
     * @param days 天数
     * @return 归档趋势统计
     */
    @Select("SELECT DATE(archive_time) as archiveDate, COUNT(*) as archiveCount " +
            "FROM archive WHERE archive_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND archive_time IS NOT NULL AND deleted = 0 " +
            "GROUP BY DATE(archive_time) ORDER BY archiveDate ASC")
    List<Map<String, Object>> getArchiveArchivingTrendStatistics(@Param("days") int days);

    /**
     * 获取热门档案统计
     * 
     * @param limit 限制数量
     * @return 热门档案统计
     */
    @Select("SELECT archive_number, title, view_count, download_count " +
            "FROM archive WHERE deleted = 0 " +
            "ORDER BY (view_count * 0.7 + download_count * 0.3) DESC LIMIT #{limit}")
    List<Map<String, Object>> getPopularArchiveStatistics(@Param("limit") int limit);

    /**
     * 获取用户档案统计
     * 
     * @return 用户档案统计
     */
    @Select("SELECT u.username, u.real_name, COUNT(a.id) as archiveCount, SUM(a.file_size) as totalSize " +
            "FROM archive a LEFT JOIN sys_user u ON a.created_by = u.id " +
            "WHERE a.deleted = 0 AND u.deleted = 0 " +
            "GROUP BY a.created_by, u.username, u.real_name ORDER BY archiveCount DESC")
    List<Map<String, Object>> getUserArchiveStatistics();

    /**
     * 获取档案存储统计
     * 
     * @return 档案存储统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalArchives, " +
            "SUM(file_size) as totalSize, " +
            "AVG(file_size) as avgSize, " +
            "MAX(file_size) as maxSize, " +
            "MIN(file_size) as minSize " +
            "FROM archive WHERE deleted = 0 AND file_size IS NOT NULL")
    Map<String, Object> getArchiveStorageStatistics();

    /**
     * 清理过期档案
     * 
     * @param deletedBy 删除人ID
     * @return 清理数量
     */
    @Update("UPDATE archive SET deleted = 1, deleted_by = #{deletedBy}, delete_time = NOW() " +
            "WHERE expiry_date IS NOT NULL AND expiry_date < NOW() " +
            "AND status != 3 AND deleted = 0")
    int cleanExpiredArchives(@Param("deletedBy") Long deletedBy);

    /**
     * 自动归档符合条件的档案
     * 
     * @param archivedBy 归档人ID
     * @param days 天数（创建后多少天自动归档）
     * @return 归档数量
     */
    @Update("UPDATE archive SET status = 2, archive_time = NOW(), archived_by = #{archivedBy}, update_time = NOW() " +
            "WHERE status = 1 AND create_time <= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0")
    int autoArchiveOldArchives(@Param("archivedBy") Long archivedBy, @Param("days") int days);

    /**
     * 获取档案文件扩展名统计
     * 
     * @return 文件扩展名统计
     */
    @Select("SELECT " +
            "SUBSTRING_INDEX(file_name, '.', -1) as fileExtension, " +
            "COUNT(*) as fileCount, " +
            "SUM(file_size) as totalSize " +
            "FROM archive WHERE deleted = 0 AND file_name IS NOT NULL " +
            "GROUP BY SUBSTRING_INDEX(file_name, '.', -1) " +
            "ORDER BY fileCount DESC")
    List<Map<String, Object>> getArchiveFileExtensionStatistics();
}