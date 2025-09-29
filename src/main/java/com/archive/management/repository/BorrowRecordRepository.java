package com.archive.management.repository;

import com.archive.management.entity.BorrowRecord;
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
 * 借阅记录数据访问层接口
 * 提供借阅记录实体的数据库操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    /**
     * 根据借阅编号查找借阅记录
     * @param borrowNumber 借阅编号
     * @return 借阅记录信息
     */
    Optional<BorrowRecord> findByBorrowNumber(String borrowNumber);

    /**
     * 检查借阅编号是否存在
     * @param borrowNumber 借阅编号
     * @return 是否存在
     */
    boolean existsByBorrowNumber(String borrowNumber);

    /**
     * 根据借阅人查找借阅记录列表
     * @param borrowerId 借阅人ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByBorrowerId(Long borrowerId, Pageable pageable);

    /**
     * 根据档案ID查找借阅记录列表
     * @param archiveId 档案ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByArchiveId(Long archiveId, Pageable pageable);

    /**
     * 根据借阅状态查找借阅记录列表
     * @param status 借阅状态
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据审批人查找借阅记录列表
     * @param approverId 审批人ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByApproverId(Long approverId, Pageable pageable);

    /**
     * 查找指定用户的当前借阅记录（未归还）
     * @param borrowerId 借阅人ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowerId = :borrowerId AND br.status = 1")
    Page<BorrowRecord> findCurrentBorrowsByUser(@Param("borrowerId") Long borrowerId, Pageable pageable);

    /**
     * 查找指定档案的当前借阅记录（未归还）
     * @param archiveId 档案ID
     * @return 借阅记录信息
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.archiveId = :archiveId AND br.status = 1")
    Optional<BorrowRecord> findCurrentBorrowByArchive(@Param("archiveId") Long archiveId);

    /**
     * 查找待审批的借阅申请
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 0 ORDER BY br.applyTime ASC")
    Page<BorrowRecord> findPendingApprovals(Pageable pageable);

    /**
     * 查找已逾期的借阅记录
     * @param currentTime 当前时间
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 1 AND br.expectedReturnTime < :currentTime")
    Page<BorrowRecord> findOverdueBorrows(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * 查找即将到期的借阅记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 1 AND br.expectedReturnTime BETWEEN :startTime AND :endTime")
    Page<BorrowRecord> findExpiringBorrows(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime, 
                                         Pageable pageable);

    /**
     * 查找指定时间范围内的借阅申请
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByApplyTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定时间范围内的归还记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByActualReturnTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 查找指定用户的借阅历史
     * @param borrowerId 借阅人ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowerId = :borrowerId ORDER BY br.applyTime DESC")
    Page<BorrowRecord> findBorrowHistoryByUser(@Param("borrowerId") Long borrowerId, Pageable pageable);

    /**
     * 查找指定档案的借阅历史
     * @param archiveId 档案ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.archiveId = :archiveId ORDER BY br.applyTime DESC")
    Page<BorrowRecord> findBorrowHistoryByArchive(@Param("archiveId") Long archiveId, Pageable pageable);

    /**
     * 统计借阅记录总数
     * @return 借阅记录总数
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br")
    long countAllBorrowRecords();

    /**
     * 统计指定状态的借阅记录数量
     * @param status 借阅状态
     * @return 借阅记录数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定用户的借阅次数
     * @param borrowerId 借阅人ID
     * @return 借阅次数
     */
    long countByBorrowerId(Long borrowerId);

    /**
     * 统计指定档案的借阅次数
     * @param archiveId 档案ID
     * @return 借阅次数
     */
    long countByArchiveId(Long archiveId);

    /**
     * 统计指定时间范围内的借阅申请数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 借阅申请数量
     */
    long countByApplyTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计当前借阅中的记录数量
     * @return 借阅中记录数量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 1")
    long countCurrentBorrows();

    /**
     * 统计逾期借阅记录数量
     * @param currentTime 当前时间
     * @return 逾期记录数量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 1 AND br.expectedReturnTime < :currentTime")
    long countOverdueBorrows(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计待审批的借阅申请数量
     * @return 待审批申请数量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 0")
    long countPendingApprovals();

    /**
     * 检查用户是否有未归还的借阅记录
     * @param borrowerId 借阅人ID
     * @return 是否有未归还记录
     */
    @Query("SELECT COUNT(br) > 0 FROM BorrowRecord br WHERE br.borrowerId = :borrowerId AND br.status = 1")
    boolean hasUnreturnedBorrows(@Param("borrowerId") Long borrowerId);

    /**
     * 检查档案是否正在被借阅
     * @param archiveId 档案ID
     * @return 是否正在被借阅
     */
    @Query("SELECT COUNT(br) > 0 FROM BorrowRecord br WHERE br.archiveId = :archiveId AND br.status = 1")
    boolean isArchiveBorrowed(@Param("archiveId") Long archiveId);

    /**
     * 检查用户是否已申请借阅指定档案（待审批或已借阅）
     * @param borrowerId 借阅人ID
     * @param archiveId 档案ID
     * @return 是否已申请
     */
    @Query("SELECT COUNT(br) > 0 FROM BorrowRecord br WHERE br.borrowerId = :borrowerId AND br.archiveId = :archiveId AND br.status IN (0, 1)")
    boolean hasAppliedForArchive(@Param("borrowerId") Long borrowerId, @Param("archiveId") Long archiveId);

    /**
     * 查找热门借阅档案（按借阅次数排序）
     * @param pageable 分页参数
     * @return 档案借阅统计结果
     */
    @Query("SELECT br.archiveId, COUNT(br) as borrowCount FROM BorrowRecord br GROUP BY br.archiveId ORDER BY borrowCount DESC")
    Page<Object[]> findPopularArchives(Pageable pageable);

    /**
     * 查找活跃借阅用户（按借阅次数排序）
     * @param pageable 分页参数
     * @return 用户借阅统计结果
     */
    @Query("SELECT br.borrowerId, COUNT(br) as borrowCount FROM BorrowRecord br GROUP BY br.borrowerId ORDER BY borrowCount DESC")
    Page<Object[]> findActiveBorrowers(Pageable pageable);

    /**
     * 根据借阅记录ID列表批量查询
     * @param recordIds 借阅记录ID列表
     * @return 借阅记录列表
     */
    List<BorrowRecord> findByIdIn(List<Long> recordIds);

    /**
     * 批量更新借阅记录状态
     * @param recordIds 借阅记录ID列表
     * @param status 新状态
     */
    @Query("UPDATE BorrowRecord br SET br.status = :status WHERE br.id IN :recordIds")
    void batchUpdateStatus(@Param("recordIds") List<Long> recordIds, @Param("status") Integer status);

    /**
     * 更新借阅记录的审批信息
     * @param recordId 借阅记录ID
     * @param approverId 审批人ID
     * @param approveTime 审批时间
     * @param approveRemark 审批备注
     * @param status 新状态
     */
    @Query("UPDATE BorrowRecord br SET br.approverId = :approverId, br.approveTime = :approveTime, br.approveRemark = :approveRemark, br.status = :status WHERE br.id = :recordId")
    void updateApprovalInfo(@Param("recordId") Long recordId,
                           @Param("approverId") Long approverId,
                           @Param("approveTime") LocalDateTime approveTime,
                           @Param("approveRemark") String approveRemark,
                           @Param("status") Integer status);

    /**
     * 更新借阅记录的归还信息
     * @param recordId 借阅记录ID
     * @param actualReturnTime 实际归还时间
     * @param returnRemark 归还备注
     * @param status 新状态
     */
    @Query("UPDATE BorrowRecord br SET br.actualReturnTime = :actualReturnTime, br.returnRemark = :returnRemark, br.status = :status WHERE br.id = :recordId")
    void updateReturnInfo(@Param("recordId") Long recordId,
                         @Param("actualReturnTime") LocalDateTime actualReturnTime,
                         @Param("returnRemark") String returnRemark,
                         @Param("status") Integer status);

    /**
     * 更新借阅记录的延期信息
     * @param recordId 借阅记录ID
     * @param newExpectedReturnTime 新的预期归还时间
     * @param extensionReason 延期原因
     * @param extensionCount 延期次数
     */
    @Query("UPDATE BorrowRecord br SET br.expectedReturnTime = :newExpectedReturnTime, br.extensionReason = :extensionReason, br.extensionCount = :extensionCount WHERE br.id = :recordId")
    void updateExtensionInfo(@Param("recordId") Long recordId,
                            @Param("newExpectedReturnTime") LocalDateTime newExpectedReturnTime,
                            @Param("extensionReason") String extensionReason,
                            @Param("extensionCount") Integer extensionCount);

    /**
     * 按月份统计借阅数量
     * @param year 年份
     * @return 月份统计结果
     */
    @Query("SELECT MONTH(br.applyTime) as month, COUNT(br) as count FROM BorrowRecord br WHERE YEAR(br.applyTime) = :year GROUP BY MONTH(br.applyTime) ORDER BY month")
    List<Object[]> countBorrowsByMonth(@Param("year") int year);

    /**
     * 按状态统计借阅记录数量
     * @return 状态统计结果
     */
    @Query("SELECT br.status as status, COUNT(br) as count FROM BorrowRecord br GROUP BY br.status ORDER BY status")
    List<Object[]> countBorrowsByStatus();

    /**
     * 统计用户借阅行为
     * @param borrowerId 借阅人ID
     * @return 借阅行为统计
     */
    @Query("SELECT COUNT(br) as totalBorrows, " +
           "SUM(CASE WHEN br.status = 2 THEN 1 ELSE 0 END) as returnedBorrows, " +
           "SUM(CASE WHEN br.status = 1 AND br.expectedReturnTime < CURRENT_TIMESTAMP THEN 1 ELSE 0 END) as overdueBorrows " +
           "FROM BorrowRecord br WHERE br.borrowerId = :borrowerId")
    Object[] getBorrowStatsByUser(@Param("borrowerId") Long borrowerId);

    /**
     * 统计档案借阅情况
     * @param archiveId 档案ID
     * @return 档案借阅统计
     */
    @Query("SELECT COUNT(br) as totalBorrows, " +
           "COUNT(DISTINCT br.borrowerId) as uniqueBorrowers, " +
           "AVG(CASE WHEN br.actualReturnTime IS NOT NULL THEN DATEDIFF(br.actualReturnTime, br.borrowTime) ELSE NULL END) as avgBorrowDays " +
           "FROM BorrowRecord br WHERE br.archiveId = :archiveId")
    Object[] getBorrowStatsByArchive(@Param("archiveId") Long archiveId);
}