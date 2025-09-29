package com.archive.management.service.impl;

import com.archive.management.common.PageResult;
import com.archive.management.constant.BusinessConstants;
import com.archive.management.dto.request.BorrowCreateRequest;
import com.archive.management.dto.request.BorrowQueryRequest;
import com.archive.management.dto.request.BorrowReviewRequest;
import com.archive.management.dto.request.BorrowReturnRequest;
import com.archive.management.dto.response.BorrowResponse;
import com.archive.management.entity.Archive;
import com.archive.management.entity.BorrowRecord;
import com.archive.management.entity.User;
import com.archive.management.mapper.ArchiveMapper;
import com.archive.management.mapper.AuditLogMapper;

import com.archive.management.mapper.BorrowMapper;
import com.archive.management.mapper.UserMapper;
import com.archive.management.service.BorrowService;
import com.archive.management.util.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 借阅服务实现类
 */
@Service
@Transactional
public class BorrowServiceImpl extends ServiceImpl<BorrowMapper, BorrowRecord> implements BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowServiceImpl.class);

    @Autowired
    private BorrowMapper borrowMapper;

    @Override
    public BorrowMapper getBaseMapper() {
        return borrowMapper;
    }

    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Override
    public BorrowResponse createBorrowApplication(BorrowCreateRequest request) {
        logger.info("创建借阅申请: {}", request);

        // 获取当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 生成借阅编号
        String borrowNumber = generateBorrowNumber();
        
        // 创建借阅记录
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setBorrowNo(borrowNumber);
        borrowRecord.setApplyUserId(currentUserId);
        borrowRecord.setPurpose(request.getPurpose());
        borrowRecord.setExpectedDays(request.getBorrowDays());
        borrowRecord.setStatus(BusinessConstants.BorrowStatus.APPLYING);
        borrowRecord.setApplyTime(LocalDateTime.now());
        
        // 保存借阅记录
        save(borrowRecord);
        
        logger.info("借阅申请创建成功，借阅编号: {}", borrowNumber);
        return convertToBorrowResponse(borrowRecord);
    }

    @Override
    public BorrowResponse reviewBorrowApplication(Long borrowId, BorrowReviewRequest request) {
        logger.info("审批借阅申请: borrowId={}, request={}", borrowId, request);

        BorrowRecord borrowRecord = getById(borrowId);
        if (borrowRecord == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        // 获取当前用户ID作为审批人
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 更新审批信息
        borrowRecord.setApproveUserId(currentUserId);
        borrowRecord.setApproveTime(LocalDateTime.now());
        borrowRecord.setApproveRemark(request.getReviewComment());
        
        // 根据审批动作设置状态
        if ("APPROVE".equals(request.getAction().toString())) {
            borrowRecord.setStatus(BusinessConstants.BorrowStatus.APPROVED);
        } else {
            borrowRecord.setStatus(BusinessConstants.BorrowStatus.REJECTED);
        }
        
        updateById(borrowRecord);
        
        logger.info("借阅申请审批完成: borrowId={}, status={}", borrowId, borrowRecord.getStatus());
        return convertToBorrowResponse(borrowRecord);
    }

    @Override
    public BorrowResponse returnArchive(Long borrowId, BorrowReturnRequest request) {
        logger.info("归还档案: borrowId={}, request={}", borrowId, request);

        BorrowRecord borrowRecord = getById(borrowId);
        if (borrowRecord == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        // 更新归还信息
        borrowRecord.setReturnTime(request.getActualReturnTime() != null ? 
            request.getActualReturnTime() : LocalDateTime.now());
        borrowRecord.setStatus(BusinessConstants.BorrowStatus.RETURNED);
        borrowRecord.setRemark(request.getReturnNote());
        
        updateById(borrowRecord);
        
        logger.info("档案归还完成: borrowId={}", borrowId);
        return convertToBorrowResponse(borrowRecord);
    }

    @Override
    public BorrowResponse requestExtension(Long borrowId, Integer extensionDays, String reason) {
        logger.info("申请延期: borrowId={}, extensionDays={}, reason={}", borrowId, extensionDays, reason);
        
        BorrowRecord record = getById(borrowId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        // 简化实现：直接更新预期归还时间
        if (record.getBorrowEndTime() != null) {
            record.setBorrowEndTime(record.getBorrowEndTime().plusDays(extensionDays));
            updateById(record);
        }
        
        return convertToBorrowResponse(record);
    }

    @Override
    public List<BorrowResponse> getUserBorrowHistory(Long userId) {
        logger.info("获取用户借阅历史: userId={}", userId);
        
        QueryWrapper<BorrowRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("apply_user_id", userId);
        queryWrapper.orderByDesc("apply_time");
        
        List<BorrowRecord> records = list(queryWrapper);
        return records.stream()
                .map(this::convertToBorrowResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowResponse cancelBorrowApplication(Long borrowId, String reason) {
        logger.info("取消借阅申请: borrowId={}, reason={}", borrowId, reason);
        
        BorrowRecord record = getById(borrowId);
        if (record == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        
        // 只有待审批状态才能取消
        if (!BusinessConstants.BorrowStatus.APPLYING.equals(record.getStatus())) {
            throw new RuntimeException("只有待审批状态的申请才能取消");
        }
        
        record.setStatus(BusinessConstants.BorrowStatus.REJECTED);
        record.setRemark(reason);
        updateById(record);
        
        return convertToBorrowResponse(record);
    }

    @Override
    public BorrowResponse getBorrowRecordById(Long borrowId) {
        logger.info("获取借阅记录详情: borrowId={}", borrowId);

        BorrowRecord borrowRecord = getById(borrowId);
        if (borrowRecord == null) {
            throw new RuntimeException("借阅记录不存在");
        }

        return convertToBorrowResponse(borrowRecord);
    }

    @Override
    public PageResult<BorrowResponse> queryBorrowRecords(BorrowQueryRequest request, int page, int size) {
        logger.info("查询借阅记录: request={}, page={}, size={}", request, page, size);

        Page<BorrowRecord> pageParam = new Page<>(page, size);
        QueryWrapper<BorrowRecord> queryWrapper = buildBorrowQueryWrapper(request);
        
        IPage<BorrowRecord> borrowPage = page(pageParam, queryWrapper);
        
        // 转换为响应DTO
        List<BorrowResponse> responseList = borrowPage.getRecords().stream()
                .map(this::convertToBorrowResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(page, size, borrowPage.getTotal(), responseList);
    }

    // 移除不存在的方法
    // public PageResult<BorrowResponse> getUserBorrowRecords(Long userId, int page, int size) {

    @Override
    public BorrowService.SystemBorrowStatistics getSystemBorrowStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("获取系统借阅统计: startDate={}, endDate={}", startDate, endDate);

        QueryWrapper<BorrowRecord> queryWrapper = new QueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge("apply_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("apply_time", endDate);
        }

        List<BorrowRecord> records = list(queryWrapper);
        
        // 统计各种状态的数量
        int totalApplications = records.size();
        int approvedApplications = (int) records.stream().filter(r -> 
            BusinessConstants.BorrowStatus.APPROVED.equals(r.getStatus())).count();
        int rejectedApplications = (int) records.stream().filter(r -> 
            BusinessConstants.BorrowStatus.REJECTED.equals(r.getStatus())).count();
        int currentBorrowings = (int) records.stream().filter(r -> 
            BusinessConstants.BorrowStatus.BORROWED.equals(r.getStatus())).count();
        int overdueRecords = (int) records.stream().filter(r -> 
            BusinessConstants.BorrowStatus.OVERDUE.equals(r.getStatus())).count();
        int returnedRecords = (int) records.stream().filter(r -> 
            BusinessConstants.BorrowStatus.RETURNED.equals(r.getStatus())).count();
        
        double approvalRate = totalApplications > 0 ? 
            (double) approvedApplications / totalApplications * 100 : 0.0;

        SystemBorrowStatistics statistics = new SystemBorrowStatistics();
        statistics.setTotalApplications(totalApplications);
        statistics.setApprovedApplications(approvedApplications);
        statistics.setRejectedApplications(rejectedApplications);
        statistics.setCurrentBorrowings(currentBorrowings);
        statistics.setOverdueRecords(overdueRecords);
        statistics.setReturnedRecords(returnedRecords);
        statistics.setApprovalRate(approvalRate);

        return statistics;
    }

    /**
     * 生成借阅编号
     */
    private String generateBorrowNumber() {
        return "BR" + System.currentTimeMillis();
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<BorrowRecord> buildBorrowQueryWrapper(BorrowQueryRequest request) {
        QueryWrapper<BorrowRecord> queryWrapper = new QueryWrapper<>();
        
        if (request.getBorrowId() != null) {
            queryWrapper.eq("borrow_id", request.getBorrowId());
        }
        if (request.getApplicantId() != null) {
            queryWrapper.eq("apply_user_id", request.getApplicantId());
        }
        if (request.getArchiveId() != null) {
            queryWrapper.eq("archive_id", request.getArchiveId());
        }
        
        queryWrapper.orderByDesc("apply_time");
        return queryWrapper;
    }

    /**
     * 转换为响应DTO
     */
    private BorrowResponse convertToBorrowResponse(BorrowRecord borrowRecord) {
        BorrowResponse response = new BorrowResponse();
        response.setId(borrowRecord.getBorrowId());
        response.setBorrowNumber(borrowRecord.getBorrowNo());
        response.setPurpose(borrowRecord.getPurpose());
        response.setApplyTime(borrowRecord.getApplyTime());
        response.setReviewTime(borrowRecord.getApproveTime());
        response.setReviewComment(borrowRecord.getApproveRemark());
        
        // 设置状态字符串
        if (borrowRecord.getStatus() != null) {
            switch (borrowRecord.getStatus()) {
                case 0:
                    response.setStatus("PENDING");
                    break;
                case 1:
                    response.setStatus("APPROVED");
                    break;
                case 2:
                    response.setStatus("BORROWED");
                    break;
                case 3:
                    response.setStatus("RETURNED");
                    break;
                case 4:
                    response.setStatus("OVERDUE");
                    break;
                case 5:
                    response.setStatus("REJECTED");
                    break;
                default:
                    response.setStatus("UNKNOWN");
            }
        }
        
        return response;
    }
}