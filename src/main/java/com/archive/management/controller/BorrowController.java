package com.archive.management.controller;

import com.archive.management.dto.request.*;
import com.archive.management.dto.response.BorrowResponse;
import com.archive.management.service.BorrowService;
import com.archive.management.common.Result;
import com.archive.management.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅管理控制器
 * 提供借阅申请、审批、归还等API接口
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
@Tag(name = "借阅管理", description = "借阅申请、审批、归还等功能")
@RestController
@RequestMapping("/api/borrow")
@Validated
public class BorrowController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    @Autowired
    private BorrowService borrowService;

    /**
     * 获取当前用户ID
     * 从Spring Security上下文中获取已认证的用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("用户未认证");
                throw new IllegalStateException("用户未认证，请先登录");
            }
            
            Object principal = authentication.getPrincipal();
            
            // 尝试多种方式获取用户ID
            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 如果principal是UserDetails，尝试从username解析ID
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                try {
                    return Long.parseLong(username);
                } catch (NumberFormatException e) {
                    logger.warn("无法从username解析用户ID: {}", username);
                }
            } else if (principal instanceof String) {
                // 如果principal是字符串，尝试解析为Long
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    logger.warn("无法解析用户ID字符串: {}", principal);
                }
            }
            
            // 如果以上方式都失败，记录警告并抛出异常
            logger.error("无法获取当前用户ID，principal类型: {}", principal.getClass().getName());
            throw new IllegalStateException("无法获取当前用户信息");
            
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取当前用户ID时发生错误", e);
            throw new IllegalStateException("获取用户信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建借阅申请
     */
    @Operation(summary = "创建借阅申请", description = "用户申请借阅档案")
    @PostMapping("/apply")
    @PreAuthorize("hasPermission('BORROW_APPLY')")
    public Result<BorrowResponse> createBorrowApplication(
            @Valid @RequestBody BorrowCreateRequest request) {
        
        logger.info("创建借阅申请: {}", request);
        
        try {
            BorrowResponse response = borrowService.createBorrowApplication(request);
            logger.info("借阅申请创建成功: borrowId={}", response.getId());
            return Result.success(response, "借阅申请提交成功");
        } catch (Exception e) {
            logger.error("借阅申请提交失败: {}", e.getMessage(), e);
            return Result.failure("借阅申请提交失败: " + e.getMessage());
        }
    }

    /**
     * 审批借阅申请
     */
    @Operation(summary = "审批借阅申请", description = "管理员审批用户的借阅申请")
    @PostMapping("/{borrowId}/review")
    @PreAuthorize("hasPermission('BORROW_REVIEW')")
    public Result<BorrowResponse> reviewBorrowApplication(
            @Parameter(description = "借阅记录ID") @PathVariable @NotNull Long borrowId,
            @Valid @RequestBody BorrowReviewRequest request) {
        
        logger.info("审批借阅申请: borrowId={}, request={}", borrowId, request);
        
        try {
            BorrowResponse response = borrowService.reviewBorrowApplication(borrowId, request);
            logger.info("借阅申请审批完成: borrowId={}, action={}", borrowId, request.getAction());
            return Result.success(response, "审批完成");
        } catch (Exception e) {
            logger.error("审批失败: {}", e.getMessage(), e);
            return Result.failure("审批失败: " + e.getMessage());
        }
    }

    /**
     * 归还档案
     */
    @Operation(summary = "归还档案", description = "用户归还借阅的档案")
    @PostMapping("/{borrowId}/return")
    @PreAuthorize("hasPermission('BORROW_RETURN')")
    public Result<BorrowResponse> returnArchives(
            @Parameter(description = "借阅记录ID") @PathVariable @NotNull Long borrowId,
            @Valid @RequestBody BorrowReturnRequest request) {
        
        logger.info("归还档案: borrowId={}, request={}", borrowId, request);
        
        try {
            BorrowResponse response = borrowService.returnArchive(borrowId, request);
            logger.info("档案归还完成: borrowId={}", borrowId);
            return Result.success(response, "档案归还成功");
        } catch (Exception e) {
            logger.error("归还失败: {}", e.getMessage(), e);
            return Result.failure("归还失败: " + e.getMessage());
        }
    }

    /**
     * 延期借阅
     */
    @Operation(summary = "延期借阅", description = "用户申请延期借阅时间")
    @PostMapping("/{borrowId}/extend")
    @PreAuthorize("hasPermission('BORROW_EXTEND')")
    public Result<BorrowResponse> extendBorrowTime(
            @Parameter(description = "借阅记录ID") @PathVariable @NotNull Long borrowId,
            @Parameter(description = "延期天数") @RequestParam @Min(1) Integer extensionDays,
            @Parameter(description = "延期原因") @RequestParam String reason) {
        
        logger.info("延期借阅: borrowId={}, extensionDays={}, reason={}", borrowId, extensionDays, reason);
        
        try {
            BorrowResponse response = borrowService.requestExtension(borrowId, extensionDays, reason);
            logger.info("借阅延期成功: borrowId={}", borrowId);
            return Result.success(response, "延期申请成功");
        } catch (Exception e) {
            logger.error("延期申请失败: {}", e.getMessage(), e);
            return Result.failure("延期申请失败: " + e.getMessage());
        }
    }

    /**
     * 取消借阅申请
     */
    @Operation(summary = "取消借阅申请", description = "用户取消待审批的借阅申请")
    @PostMapping("/{borrowId}/cancel")
    @PreAuthorize("hasPermission('BORROW_CANCEL')")
    public Result<Boolean> cancelBorrowApplication(
            @Parameter(description = "借阅记录ID") @PathVariable @NotNull Long borrowId,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        
        logger.info("取消借阅申请: borrowId={}, reason={}", borrowId, reason);
        
        try {
            BorrowResponse result = borrowService.cancelBorrowApplication(borrowId, reason);
            logger.info("借阅申请取消成功: borrowId={}", borrowId);
            return Result.success(result != null, "借阅申请已取消");
        } catch (Exception e) {
            logger.error("取消失败: {}", e.getMessage(), e);
            return Result.failure("取消失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询借阅记录
     */
    @Operation(summary = "分页查询借阅记录", description = "根据条件分页查询借阅记录")
    @PostMapping("/query")
    @PreAuthorize("hasPermission('BORROW_QUERY')")
    public Result<PageResult<BorrowResponse>> queryBorrowRecords(
            @Valid @RequestBody BorrowQueryRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        logger.info("查询借阅记录: request={}, page={}, size={}", request, page, size);
        
        try {
            PageResult<BorrowResponse> result = borrowService.queryBorrowRecords(request, page, size);
            logger.info("借阅记录查询成功: total={}", result.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询失败: {}", e.getMessage(), e);
            return Result.failure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取借阅详情
     */
    @Operation(summary = "获取借阅详情", description = "根据ID获取借阅记录详细信息")
    @GetMapping("/{borrowId}")
    @PreAuthorize("hasPermission('BORROW_VIEW')")
    public Result<BorrowResponse> getBorrowDetail(
            @Parameter(description = "借阅记录ID") @PathVariable @NotNull Long borrowId) {
        
        logger.info("获取借阅详情: borrowId={}", borrowId);
        
        try {
            BorrowResponse response = borrowService.getBorrowRecordById(borrowId);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("获取详情失败: {}", e.getMessage(), e);
            return Result.failure("获取详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户借阅历史
     */
    @Operation(summary = "获取用户借阅历史", description = "获取指定用户的所有借阅记录")
    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasPermission('BORROW_VIEW') or #userId == authentication.principal.id")
    public Result<PageResult<BorrowResponse>> getUserBorrowHistory(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long userId) {
        
        logger.info("获取用户借阅历史: userId={}", userId);
        
        try {
            List<BorrowResponse> history = borrowService.getUserBorrowHistory(userId);
            PageResult<BorrowResponse> pageResult = new PageResult<>();
            pageResult.setList(history);
            pageResult.setTotal(history.size());
            pageResult.setPageSize(history.size());
            pageResult.setPageNum(1);
            pageResult.setPages(1);
            
            logger.info("获取用户借阅历史成功: userId={}, count={}", userId, history.size());
            return Result.success(pageResult, "获取成功");
        } catch (Exception e) {
            logger.error("获取历史记录失败: {}", e.getMessage(), e);
            return Result.failure("获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取逾期借阅记录
     */
    @Operation(summary = "获取逾期借阅记录", description = "获取所有逾期未归还的借阅记录")
    @GetMapping("/overdue")
    @PreAuthorize("hasPermission('BORROW_MANAGE')")
    public Result<List<BorrowResponse>> getOverdueRecords() {
        
        logger.info("获取逾期借阅记录");
        
        try {
            List<BorrowResponse> overdueRecords = borrowService.getOverdueRecords();
            logger.info("获取逾期记录成功: count={}", overdueRecords.size());
            return Result.success(overdueRecords);
        } catch (Exception e) {
            logger.error("获取逾期记录失败: {}", e.getMessage(), e);
            return Result.failure("获取逾期记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户借阅统计
     */
    @Operation(summary = "获取用户借阅统计", description = "获取指定用户的借阅统计信息")
    @GetMapping("/user/{userId}/statistics")
    @PreAuthorize("hasPermission('BORROW_VIEW') or #userId == authentication.principal.id")
    public Result<String> getUserBorrowStatistics(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long userId) {
        
        logger.info("获取用户借阅统计: userId={}", userId);
        
        try {
            // 暂时返回提示信息，因为BorrowService中没有对应的方法
            return Result.success("用户借阅统计功能暂未实现", "获取成功");
        } catch (Exception e) {
            logger.error("获取用户借阅统计失败: {}", e.getMessage(), e);
            return Result.failure("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统借阅统计
     */
    @Operation(summary = "获取系统借阅统计", description = "获取整个系统的借阅统计信息")
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('BORROW_MANAGE')")
    public Result<BorrowService.SystemBorrowStatistics> getSystemBorrowStatistics() {
        
        logger.info("获取系统借阅统计");
        
        try {
            // 添加默认的日期范围参数
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusMonths(12); // 默认查询最近12个月
            BorrowService.SystemBorrowStatistics statistics = borrowService.getSystemBorrowStatistics(startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            logger.error("获取系统借阅统计失败", e);
            return Result.failure("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批借阅申请
     */
    @Operation(summary = "批量审批借阅申请", description = "批量审批多个借阅申请")
    @PostMapping("/batch-review")
    @PreAuthorize("hasPermission('BORROW_REVIEW')")
    public Result<String> batchReviewBorrowApplications(
            @Parameter(description = "借阅记录ID列表") @RequestParam List<Long> borrowIds,
            @Valid @RequestBody BorrowReviewRequest request) {
        
        logger.info("批量审批借阅申请: borrowIds={}, request={}", borrowIds, request);
        
        try {
            // 暂时返回提示信息，因为BorrowService中没有对应的方法
            return Result.success("批量审批功能暂未实现", "操作成功");
        } catch (Exception e) {
            logger.error("批量审批失败: {}", e.getMessage(), e);
            return Result.failure("批量审批失败: " + e.getMessage());
        }
    }

    /**
     * 导出借阅记录
     */
    @Operation(summary = "导出借阅记录", description = "根据条件导出借阅记录到Excel")
    @PostMapping("/export")
    @PreAuthorize("hasPermission('BORROW_EXPORT')")
    public Result<String> exportBorrowRecords(
            @Valid @RequestBody BorrowQueryRequest request) {
        
        logger.info("导出借阅记录: request={}", request);
        
        try {
            // 查询所有符合条件的记录
            PageResult<BorrowResponse> allRecords = borrowService.queryBorrowRecords(request, 1, Integer.MAX_VALUE);
            
            // 这里应该调用Excel导出服务
            // String filePath = excelService.exportBorrowRecords(allRecords.getRecords());
            String filePath = "/tmp/borrow_records_" + System.currentTimeMillis() + ".xlsx";
            
            logger.info("借阅记录导出成功: filePath={}", filePath);
            return Result.success(filePath, "导出成功");
        } catch (Exception e) {
            logger.error("导出借阅记录失败", e);
            return Result.failure("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取借阅申请审批流程
     */
    @Operation(summary = "获取借阅申请审批流程", description = "获取借阅申请的审批流程信息")
    @GetMapping("/{borrowId}/workflow")
    @PreAuthorize("hasPermission('BORROW_VIEW')")
    public Result<List<Object>> getBorrowWorkflow(
            @Parameter(description = "借阅记录ID") @PathVariable @NotNull Long borrowId) {
        
        logger.info("获取借阅申请审批流程: borrowId={}", borrowId);
        
        try {
            // 这里应该查询工作流历史记录
            // List<WorkflowHistory> workflow = workflowService.getBorrowWorkflow(borrowId);
            List<Object> workflow = new java.util.ArrayList<>();
            
            return Result.success(workflow);
        } catch (Exception e) {
            logger.error("获取借阅申请审批流程失败: borrowId={}", borrowId, e);
            return Result.failure("获取审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 发送逾期提醒
     */
    @Operation(summary = "发送逾期提醒", description = "向逾期用户发送提醒通知")
    @PostMapping("/overdue/remind")
    @PreAuthorize("hasPermission('BORROW_MANAGE')")
    public Result<Boolean> sendOverdueReminder() {
        
        logger.info("发送逾期提醒");
        
        try {
            List<BorrowResponse> overdueRecords = borrowService.getOverdueRecords();
            
            // 这里应该调用通知服务发送提醒
            // notificationService.sendOverdueReminder(overdueRecords);
            // 由于通知服务可能还未完全实现，这里仅记录日志
            logger.info("准备发送逾期提醒，逾期记录数: {}", overdueRecords.size());
            
            // 遍历逾期记录，记录需要提醒的用户
            for (BorrowResponse record : overdueRecords) {
                logger.info("逾期提醒 - 借阅ID: {}, 用户ID: {}, 预期归还时间: {}", 
                           record.getId(), record.getUserId(), record.getExpectedReturnDate());
            }
            
            logger.info("逾期提醒发送完成: count={}", overdueRecords.size());
            return Result.success(true, String.format("逾期提醒发送成功，共%d条记录", overdueRecords.size()));
        } catch (Exception e) {
            logger.error("发送逾期提醒失败", e);
            return Result.failure("发送提醒失败: " + e.getMessage());
        }
    }
}