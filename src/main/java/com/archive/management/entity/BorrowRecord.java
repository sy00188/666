package com.archive.management.entity;

import com.archive.management.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 借阅记录实体类
 * 对应数据库表：arc_borrow
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("arc_borrow")
public class BorrowRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借阅ID
     */
    @TableId(value = "borrow_id", type = IdType.AUTO)
    private Long borrowId;

    /**
     * 借阅编号
     */
    @TableField("borrow_no")
    private String borrowNo;

    /**
     * 档案ID
     */
    @TableField("archive_id")
    private Long archiveId;

    /**
     * 申请人ID
     */
    @TableField("apply_user_id")
    private Long applyUserId;

    /**
     * 借阅用途
     */
    @TableField("purpose")
    private String purpose;

    /**
     * 期望借阅天数
     */
    @TableField("expected_days")
    private Integer expectedDays;

    /**
     * 实际批准天数
     */
    @TableField("actual_days")
    private Integer actualDays;

    /**
     * 借阅开始时间
     */
    @TableField("borrow_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowStartTime;

    /**
     * 借阅结束时间
     */
    @TableField("borrow_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowEndTime;

    /**
     * 归还时间
     */
    @TableField("return_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 状态(1:待审批 2:已批准 3:已拒绝 4:借阅中 5:已归还 6:已逾期)
     */
    @TableField("status")
    private Integer status;

    /**
     * 审批人ID
     */
    @TableField("approve_user_id")
    private Long approveUserId;

    /**
     * 审批时间
     */
    @TableField("approve_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    /**
     * 审批备注
     */
    @TableField("approve_remark")
    private String approveRemark;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    // 构造函数
    public BorrowRecord() {
        super();
        this.expectedDays = 7;
        this.status = 1; // 默认待审批
        this.applyTime = LocalDateTime.now();
    }

    public BorrowRecord(String borrowNo, Long archiveId, Long applyUserId, String purpose) {
        this();
        this.borrowNo = borrowNo;
        this.archiveId = archiveId;
        this.applyUserId = applyUserId;
        this.purpose = purpose;
    }

    // 业务方法

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待审批";
            case 2:
                return "已批准";
            case 3:
                return "已拒绝";
            case 4:
                return "借阅中";
            case 5:
                return "已归还";
            case 6:
                return "已逾期";
            default:
                return "未知状态";
        }
    }

    /**
     * 检查是否已审批
     */
    public boolean isApproved() {
        return status != null && status == 2;
    }

    /**
     * 检查是否已拒绝
     */
    public boolean isRejected() {
        return status != null && status == 3;
    }

    /**
     * 检查是否正在借阅中
     */
    public boolean isBorrowing() {
        return status != null && status == 4;
    }

    /**
     * 检查是否已归还
     */
    public boolean isReturned() {
        return status != null && status == 5;
    }

    /**
     * 检查是否已逾期
     */
    public boolean isOverdue() {
        return status != null && status == 6;
    }

    /**
     * 检查是否待审批
     */
    public boolean isPending() {
        return status != null && status == 1;
    }

    /**
     * 计算已借阅天数
     */
    public long getBorrowedDays() {
        if (borrowStartTime == null) {
            return 0;
        }
        LocalDateTime endTime = returnTime != null ? returnTime : LocalDateTime.now();
        return ChronoUnit.DAYS.between(borrowStartTime, endTime);
    }

    /**
     * 计算剩余借阅天数
     */
    public long getRemainingDays() {
        if (borrowEndTime == null || returnTime != null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(borrowEndTime)) {
            return 0; // 已逾期
        }
        return ChronoUnit.DAYS.between(now, borrowEndTime);
    }

    /**
     * 计算逾期天数
     */
    public long getOverdueDays() {
        if (borrowEndTime == null) {
            return 0;
        }
        LocalDateTime checkTime = returnTime != null ? returnTime : LocalDateTime.now();
        if (checkTime.isBefore(borrowEndTime) || checkTime.isEqual(borrowEndTime)) {
            return 0; // 未逾期
        }
        return ChronoUnit.DAYS.between(borrowEndTime, checkTime);
    }

    /**
     * 检查是否需要提醒归还
     */
    public boolean needReturnReminder() {
        if (borrowEndTime == null || !isBorrowing()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = borrowEndTime.minusDays(1); // 提前1天提醒
        return now.isAfter(reminderTime) && now.isBefore(borrowEndTime);
    }

    /**
     * 审批通过
     */
    public void approve(Long approveUserId, Integer actualDays, String approveRemark) {
        this.status = 2;
        this.approveUserId = approveUserId;
        this.actualDays = actualDays;
        this.approveRemark = approveRemark;
        this.approveTime = LocalDateTime.now();
    }

    /**
     * 审批拒绝
     */
    public void reject(Long approveUserId, String approveRemark) {
        this.status = 3;
        this.approveUserId = approveUserId;
        this.approveRemark = approveRemark;
        this.approveTime = LocalDateTime.now();
    }

    /**
     * 开始借阅
     */
    public void startBorrow() {
        if (!isApproved()) {
            throw new IllegalStateException("只有已审批的借阅申请才能开始借阅");
        }
        this.status = 4;
        this.borrowStartTime = LocalDateTime.now();
        if (actualDays != null) {
            this.borrowEndTime = borrowStartTime.plusDays(actualDays);
        }
    }

    /**
     * 归还档案
     */
    public void returnArchive() {
        if (!isBorrowing()) {
            throw new IllegalStateException("只有借阅中的档案才能归还");
        }
        this.returnTime = LocalDateTime.now();
        // 检查是否逾期
        if (borrowEndTime != null && returnTime.isAfter(borrowEndTime)) {
            this.status = 6; // 逾期归还
        } else {
            this.status = 5; // 正常归还
        }
    }

    /**
     * 标记为逾期
     */
    public void markOverdue() {
        if (isBorrowing() && borrowEndTime != null && LocalDateTime.now().isAfter(borrowEndTime)) {
            this.status = 6;
        }
    }

    /**
     * 生成借阅编号
     */
    public static String generateBorrowNo() {
        return "BR" + System.currentTimeMillis();
    }

    // 删除手动编写的toString()和equals()方法，让Lombok自动生成
}