package com.archive.management.repository;

import com.archive.management.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 档案数据访问层接口
 * 提供档案实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long>, JpaSpecificationExecutor<Archive> {

    /**
     * 根据档案编号查找档案
     * @param archiveNumber 档案编号
     * @return 档案信息
     */
    Optional<Archive> findByArchiveNumber(String archiveNumber);

    /**
     * 检查档案编号是否存在
     * @param archiveNumber 档案编号
     * @return 是否存在
     */
    boolean existsByArchiveNumber(String archiveNumber);

    /**
     * 根据分类ID查找档案列表
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 根据档案状态查找档案列表
     * @param status 档案状态
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据保密等级查找档案列表
     * @param securityLevel 保密等级
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findBySecurityLevel(Integer securityLevel, Pageable pageable);

    /**
     * 根据存储位置查找档案列表
     * @param storageLocation 存储位置
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByStorageLocation(String storageLocation, Pageable pageable);

    /**
     * 根据创建人查找档案列表
     * @param createdBy 创建人ID
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * 模糊查询档案（根据标题、描述、关键词）
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    @Query("SELECT a FROM Archive a WHERE a.title LIKE %:keyword% OR a.description LIKE %:keyword% OR a.keywords LIKE %:keyword%")
    Page<Archive> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据档案标题模糊查询
     * @param title 档案标题
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByTitleContaining(String title, Pageable pageable);

    /**
     * 查找指定时间范围内创建的档案
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内的档案（按档案日期）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByArchiveDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 查找可借阅的档案
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    @Query("SELECT a FROM Archive a WHERE a.borrowable = true AND a.status = 1")
    Page<Archive> findBorrowableArchives(Pageable pageable);

    /**
     * 查找当前被借阅的档案
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    @Query("SELECT DISTINCT a FROM Archive a JOIN BorrowRecord br ON a.id = br.archiveId WHERE br.status = 1")
    Page<Archive> findCurrentlyBorrowedArchives(Pageable pageable);

    /**
     * 查找热门档案（按借阅次数排序）
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    @Query("SELECT a FROM Archive a ORDER BY a.borrowCount DESC")
    Page<Archive> findPopularArchives(Pageable pageable);

    /**
     * 查找最近更新的档案
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByOrderByUpdateTimeDesc(Pageable pageable);

    /**
     * 统计档案总数
     * @return 档案总数
     */
    @Query("SELECT COUNT(a) FROM Archive a")
    long countAllArchives();

    /**
     * 统计指定状态的档案数量
     * @param status 档案状态
     * @return 档案数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定分类的档案数量
     * @param categoryId 分类ID
     * @return 档案数量
     */
    long countByCategoryId(Long categoryId);

    /**
     * 统计指定保密等级的档案数量
     * @param securityLevel 保密等级
     * @return 档案数量
     */
    long countBySecurityLevel(Integer securityLevel);

    /**
     * 统计指定时间范围内创建的档案数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 档案数量
     */
    long countByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计可借阅的档案数量
     * @return 档案数量
     */
    @Query("SELECT COUNT(a) FROM Archive a WHERE a.borrowable = true AND a.status = 1")
    long countBorrowableArchives();

    /**
     * 统计当前被借阅的档案数量
     * @return 档案数量
     */
    @Query("SELECT COUNT(DISTINCT a) FROM Archive a JOIN BorrowRecord br ON a.id = br.archiveId WHERE br.status = 1")
    long countCurrentlyBorrowedArchives();

    /**
     * 根据分类ID列表查找档案
     * @param categoryIds 分类ID列表
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    /**
     * 根据档案ID列表批量查询档案
     * @param archiveIds 档案ID列表
     * @return 档案列表
     */
    List<Archive> findByIdIn(List<Long> archiveIds);

    /**
     * 查找指定用户有权限访问的档案
     * @param userId 用户ID
     * @param securityLevels 用户可访问的保密等级列表
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    @Query("SELECT a FROM Archive a WHERE a.securityLevel IN :securityLevels")
    Page<Archive> findAccessibleArchives(@Param("userId") Long userId, 
                                       @Param("securityLevels") List<Integer> securityLevels, 
                                       Pageable pageable);

    /**
     * 更新档案借阅次数
     * @param archiveId 档案ID
     */
    @Query("UPDATE Archive a SET a.borrowCount = a.borrowCount + 1 WHERE a.id = :archiveId")
    void incrementBorrowCount(@Param("archiveId") Long archiveId);

    /**
     * 更新档案查看次数
     * @param archiveId 档案ID
     */
    @Query("UPDATE Archive a SET a.viewCount = a.viewCount + 1 WHERE a.id = :archiveId")
    void incrementViewCount(@Param("archiveId") Long archiveId);

    /**
     * 批量更新档案状态
     * @param archiveIds 档案ID列表
     * @param status 新状态
     */
    @Query("UPDATE Archive a SET a.status = :status WHERE a.id IN :archiveIds")
    void batchUpdateStatus(@Param("archiveIds") List<Long> archiveIds, @Param("status") Integer status);

    /**
     * 批量更新档案分类
     * @param archiveIds 档案ID列表
     * @param categoryId 新分类ID
     */
    @Query("UPDATE Archive a SET a.categoryId = :categoryId WHERE a.id IN :archiveIds")
    void batchUpdateCategory(@Param("archiveIds") List<Long> archiveIds, @Param("categoryId") Long categoryId);

    /**
     * 软删除档案（更新删除标记）
     * @param archiveId 档案ID
     * @param deletedBy 删除人ID
     * @param deleteTime 删除时间
     */
    @Query("UPDATE Archive a SET a.deleted = true, a.deletedBy = :deletedBy, a.deleteTime = :deleteTime WHERE a.id = :archiveId")
    void softDeleteArchive(@Param("archiveId") Long archiveId, 
                          @Param("deletedBy") Long deletedBy, 
                          @Param("deleteTime") LocalDateTime deleteTime);

    /**
     * 查找未删除的档案
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    Page<Archive> findByDeletedFalse(Pageable pageable);

    /**
     * 根据档案编号查找未删除的档案
     * @param archiveNumber 档案编号
     * @return 档案信息
     */
    Optional<Archive> findByArchiveNumberAndDeletedFalse(String archiveNumber);

    /**
     * 查找即将到期的档案（保存期限）
     * @param expirationDate 到期日期阈值
     * @param pageable 分页参数
     * @return 档案分页列表
     */
    @Query("SELECT a FROM Archive a WHERE a.retentionPeriod IS NOT NULL AND a.retentionPeriod <= :expirationDate")
    Page<Archive> findExpiringArchives(@Param("expirationDate") LocalDateTime expirationDate, Pageable pageable);

    /**
     * 按年份统计档案数量
     * @return 年份统计结果
     */
    @Query("SELECT YEAR(a.archiveDate) as year, COUNT(a) as count FROM Archive a GROUP BY YEAR(a.archiveDate) ORDER BY year DESC")
    List<Object[]> countArchivesByYear();

    /**
     * 按分类统计档案数量
     * @return 分类统计结果
     */
    @Query("SELECT c.name as categoryName, COUNT(a) as count FROM Archive a JOIN Category c ON a.categoryId = c.id GROUP BY a.categoryId, c.name ORDER BY count DESC")
    List<Object[]> countArchivesByCategory();

    /**
     * 按保密等级统计档案数量
     * @return 保密等级统计结果
     */
    @Query("SELECT a.securityLevel as level, COUNT(a) as count FROM Archive a GROUP BY a.securityLevel ORDER BY level")
    List<Object[]> countArchivesBySecurityLevel();
}