package com.archive.management.service;

import com.archive.management.dto.request.BorrowCreateRequest;
import com.archive.management.dto.request.BorrowQueryRequest;
import com.archive.management.dto.request.BorrowReviewRequest;
import com.archive.management.dto.request.BorrowReturnRequest;
import com.archive.management.dto.response.BorrowResponse;
import com.archive.management.common.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅管理服务接口
 * 提供档案借阅申请、审批、归还等核心业务功能
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
public interface BorrowService {

    /**
     * 创建借阅申请
     * 用户提交档案借阅申请，系统进行初步验证
     * 
     * @param request 借阅申请请求
     * @return 借阅记录响应
     * @throws IllegalArgumentException 当请求参数无效时
     * @throws IllegalStateException 当档案状态不允许借阅时
     */
    BorrowResponse createBorrowApplication(BorrowCreateRequest request);

    /**
     * 审批借阅申请
     * 管理员或有权限的用户对借阅申请进行审批
     * 
     * @param borrowId 借阅记录ID
     * @param request 审批请求
     * @return 更新后的借阅记录响应
     * @throws IllegalArgumentException 当借阅记录不存在时
     * @throws IllegalStateException 当借阅状态不允许审批时
     */
    BorrowResponse reviewBorrowApplication(Long borrowId, BorrowReviewRequest request);

    /**
     * 档案归还
     * 借阅人归还档案，管理员确认归还
     * 
     * @param borrowId 借阅记录ID
     * @param request 归还请求
     * @return 更新后的借阅记录响应
     * @throws IllegalArgumentException 当借阅记录不存在时
     * @throws IllegalStateException 当借阅状态不允许归还时
     */
    BorrowResponse returnArchive(Long borrowId, BorrowReturnRequest request);

    /**
     * 延期申请
     * 借阅人申请延长借阅期限
     * 
     * @param borrowId 借阅记录ID
     * @param extensionDays 延期天数
     * @param reason 延期原因
     * @return 更新后的借阅记录响应
     */
    BorrowResponse requestExtension(Long borrowId, Integer extensionDays, String reason);

    /**
     * 分页查询借阅记录
     * 根据条件分页查询借阅记录
     * 
     * @param request 查询请求
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<BorrowResponse> queryBorrowRecords(BorrowQueryRequest request, int page, int size);

    /**
     * 根据ID获取借阅记录详情
     * 
     * @param borrowId 借阅记录ID
     * @return 借阅记录响应
     * @throws IllegalArgumentException 当借阅记录不存在时
     */
    BorrowResponse getBorrowRecordById(Long borrowId);

    /**
     * 获取用户的借阅记录
     * 
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    List<BorrowResponse> getUserBorrowHistory(Long userId);

    /**
     * 取消借阅申请
     * 
     * @param borrowId 借阅记录ID
     * @param reason 取消原因
     * @return 更新后的借阅记录响应
     */
    BorrowResponse cancelBorrowApplication(Long borrowId, String reason);

    /**
     * 获取系统借阅统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 系统借阅统计
     */
    SystemBorrowStatistics getSystemBorrowStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取逾期借阅记录
     * 
     * @return 逾期借阅记录列表
     */
    List<BorrowResponse> getOverdueRecords();

    /**
     * 系统借阅统计信息内部类
     */
    class SystemBorrowStatistics {
        private int totalApplications;       // 总申请数
        private int approvedApplications;    // 已批准申请数
        private int rejectedApplications;    // 已拒绝申请数
        private int currentBorrowings;       // 当前借阅数
        private int overdueRecords;          // 逾期记录数
        private int returnedRecords;         // 已归还记录数
        private double approvalRate;         // 审批通过率

        // 构造函数
        public SystemBorrowStatistics() {}

        public SystemBorrowStatistics(int totalApplications, int approvedApplications, 
                                    int rejectedApplications, int currentBorrowings,
                                    int overdueRecords, int returnedRecords, double approvalRate) {
            this.totalApplications = totalApplications;
            this.approvedApplications = approvedApplications;
            this.rejectedApplications = rejectedApplications;
            this.currentBorrowings = currentBorrowings;
            this.overdueRecords = overdueRecords;
            this.returnedRecords = returnedRecords;
            this.approvalRate = approvalRate;
        }

        // Getter和Setter方法
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

        public int getApprovedApplications() { return approvedApplications; }
        public void setApprovedApplications(int approvedApplications) { this.approvedApplications = approvedApplications; }

        public int getRejectedApplications() { return rejectedApplications; }
        public void setRejectedApplications(int rejectedApplications) { this.rejectedApplications = rejectedApplications; }

        public int getCurrentBorrowings() { return currentBorrowings; }
        public void setCurrentBorrowings(int currentBorrowings) { this.currentBorrowings = currentBorrowings; }

        public int getOverdueRecords() { return overdueRecords; }
        public void setOverdueRecords(int overdueRecords) { this.overdueRecords = overdueRecords; }

        public int getReturnedRecords() { return returnedRecords; }
        public void setReturnedRecords(int returnedRecords) { this.returnedRecords = returnedRecords; }

        public double getApprovalRate() { return approvalRate; }
        public void setApprovalRate(double approvalRate) { this.approvalRate = approvalRate; }
    }
}