package com.archive.management.mapper;

import com.archive.management.entity.BorrowRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 借阅记录Mapper接口
 * 基于MyBatis-Plus的借阅记录数据访问层
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Mapper
@Repository
public interface BorrowMapper extends BaseMapper<BorrowRecord> {

    /**
     * 根据借阅编号查找借阅记录
     * 
     * @param borrowNumber 借阅编号
     * @return 借阅记录信息
     */
    @Select("SELECT * FROM borrow_record WHERE borrow_number = #{borrowNumber} AND deleted = 0")
    BorrowRecord findByBorrowNumber(@Param("borrowNumber") String borrowNumber);

    /**
     * 根据借阅人ID查找借阅记录列表
     * 
     * @param borrowerId 借阅人ID
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE borrower_id = #{borrowerId} AND deleted = 0 ORDER BY create_time DESC")
    List<BorrowRecord> findByBorrowerId(@Param("borrowerId") Long borrowerId);

    /**
     * 根据档案ID查找借阅记录列表
     * 
     * @param archiveId 档案ID
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE archive_id = #{archiveId} AND deleted = 0 ORDER BY create_time DESC")
    List<BorrowRecord> findByArchiveId(@Param("archiveId") Long archiveId);

    /**
     * 根据借阅状态查找借阅记录列表
     * 
     * @param status 借阅状态
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<BorrowRecord> findByStatus(@Param("status") String status);

    /**
     * 根据审批人ID查找借阅记录列表
     * 
     * @param approverId 审批人ID
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE approver_id = #{approverId} AND deleted = 0 ORDER BY create_time DESC")
    List<BorrowRecord> findByApproverId(@Param("approverId") Long approverId);

    /**
     * 查找待审批的借阅记录列表
     * 
     * @return 待审批的借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE status = 'PENDING_APPROVAL' AND deleted = 0 ORDER BY create_time ASC")
    List<BorrowRecord> findPendingApprovalRecords();

    /**
     * 查找已借出但未归还的记录
     * 
     * @return 已借出但未归还的记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE status = 'BORROWED' AND deleted = 0 ORDER BY borrow_time ASC")
    List<BorrowRecord> findBorrowedRecords();

    /**
     * 查找逾期未归还的记录
     * 
     * @return 逾期未归还的记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE status = 'BORROWED' AND expected_return_time < NOW() AND deleted = 0 ORDER BY expected_return_time ASC")
    List<BorrowRecord> findOverdueRecords();

    /**
     * 根据申请时间范围查找借阅记录列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE apply_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY apply_time DESC")
    List<BorrowRecord> findByApplyTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 根据借出时间范围查找借阅记录列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE borrow_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY borrow_time DESC")
    List<BorrowRecord> findByBorrowTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 根据归还时间范围查找借阅记录列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM borrow_record WHERE actual_return_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY actual_return_time DESC")
    List<BorrowRecord> findByReturnTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计借阅记录数量
     * 
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalRecords, " +
            "SUM(CASE WHEN status = 'PENDING_APPROVAL' THEN 1 ELSE 0 END) as pendingRecords, " +
            "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approvedRecords, " +
            "SUM(CASE WHEN status = 'BORROWED' THEN 1 ELSE 0 END) as borrowedRecords, " +
            "SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) as returnedRecords, " +
            "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejectedRecords, " +
            "SUM(CASE WHEN status = 'BORROWED' AND expected_return_time < NOW() THEN 1 ELSE 0 END) as overdueRecords " +
            "FROM borrow_record WHERE deleted = 0")
    Map<String, Object> getBorrowStatistics();

    /**
     * 分页查询借阅记录
     * 
     * @param page 分页参数
     * @param borrowerId 借阅人ID（可选）
     * @param archiveId 档案ID（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM borrow_record WHERE deleted = 0 " +
            "<if test='borrowerId != null'> AND borrower_id = #{borrowerId} </if>" +
            "<if test='archiveId != null'> AND archive_id = #{archiveId} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "<if test='startTime != null'> AND apply_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND apply_time <= #{endTime} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<BorrowRecord> findBorrowRecordsPage(Page<BorrowRecord> page,
                                             @Param("borrowerId") Long borrowerId,
                                             @Param("archiveId") Long archiveId,
                                             @Param("status") String status,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 检查借阅编号是否存在
     * 
     * @param borrowNumber 借阅编号
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE borrow_number = #{borrowNumber} AND deleted = 0")
    int countByBorrowNumber(@Param("borrowNumber") String borrowNumber);

    /**
     * 检查用户是否有未归还的借阅记录
     * 
     * @param borrowerId 借阅人ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE borrower_id = #{borrowerId} AND status = 'BORROWED' AND deleted = 0")
    int countUnreturnedByBorrowerId(@Param("borrowerId") Long borrowerId);

    /**
     * 检查档案是否被借阅中
     * 
     * @param archiveId 档案ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE archive_id = #{archiveId} AND status = 'BORROWED' AND deleted = 0")
    int countBorrowedByArchiveId(@Param("archiveId") Long archiveId);

    /**
     * 统计借阅记录总数
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE deleted = 0")
    Long countTotal();
}