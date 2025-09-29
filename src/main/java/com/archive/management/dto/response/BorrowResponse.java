package com.archive.management.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅响应DTO
 * 封装借阅记录信息的输出数据
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
@Schema(description = "借阅记录响应")
public class BorrowResponse {

    @Schema(description = "借阅记录ID", example = "1")
    private Long id;

    @Schema(description = "借阅申请编号", example = "BR202401001")
    private String borrowNumber;

    @Schema(description = "借阅用户信息")
    private BorrowerInfo borrower;

    @Schema(description = "借阅档案列表")
    private List<BorrowedArchiveInfo> archives;

    @Schema(description = "借阅目的", example = "学术研究")
    private String purpose;

    @Schema(description = "借阅状态", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "BORROWED", "RETURNED", "OVERDUE", "CANCELLED"})
    private String status;

    @Schema(description = "申请时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    @Schema(description = "审批时间", example = "2024-01-02 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    @Schema(description = "审批人信息")
    private ReviewerInfo reviewer;

    @Schema(description = "审批意见", example = "同意借阅，请按时归还")
    private String reviewComment;

    @Schema(description = "预计归还日期", example = "2024-01-15 17:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedReturnTime;

    @Schema(description = "实际借出时间", example = "2024-01-03 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualBorrowTime;

    @Schema(description = "实际归还时间", example = "2024-01-14 16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualReturnTime;

    @Schema(description = "是否逾期", example = "false")
    private Boolean isOverdue;

    @Schema(description = "逾期天数", example = "0")
    private Integer overdueDays;

    @Schema(description = "借阅天数", example = "11")
    private Integer borrowDays;

    @Schema(description = "延期次数", example = "0")
    private Integer extensionCount;

    @Schema(description = "最后延期时间", example = "2024-01-10 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastExtensionTime;

    @Schema(description = "归还状态", example = "GOOD", allowableValues = {"GOOD", "DAMAGED", "MISSING_PAGES", "OTHER"})
    private String returnCondition;

    @Schema(description = "损坏描述", example = "第3页有轻微折痕")
    private String damageDescription;

    @Schema(description = "使用反馈", example = "档案内容对研究很有帮助")
    private String usageFeedback;

    @Schema(description = "复印页数", example = "10")
    private Integer copiedPages;

    @Schema(description = "借阅费用", example = "50.00")
    private Double borrowFee;

    @Schema(description = "押金金额", example = "200.00")
    private Double depositAmount;

    @Schema(description = "押金状态", example = "RETURNED", allowableValues = {"PAID", "RETURNED", "DEDUCTED"})
    private String depositStatus;

    @Schema(description = "违约金", example = "0.00")
    private Double penaltyFee;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-14 16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "特殊借阅申请")
    private String remark;

    /**
     * 借阅人信息内部类
     */
    @Schema(description = "借阅人信息")
    public static class BorrowerInfo {
        @Schema(description = "用户ID", example = "1")
        private Long userId;

        @Schema(description = "用户名", example = "zhangsan")
        private String username;

        @Schema(description = "真实姓名", example = "张三")
        private String realName;

        @Schema(description = "部门", example = "历史研究所")
        private String department;

        @Schema(description = "联系电话", example = "13800138000")
        private String phone;

        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private String email;

        // 构造函数
        public BorrowerInfo() {}

        public BorrowerInfo(Long userId, String username, String realName, String department) {
            this.userId = userId;
            this.username = username;
            this.realName = realName;
            this.department = department;
        }

        // Getter和Setter方法
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * 借阅档案信息内部类
     */
    @Schema(description = "借阅档案信息")
    public static class BorrowedArchiveInfo {
        @Schema(description = "档案ID", example = "1")
        private Long archiveId;

        @Schema(description = "档案编号", example = "ARC202401001")
        private String archiveNumber;

        @Schema(description = "档案标题", example = "重要历史文件")
        private String title;

        @Schema(description = "档案分类", example = "历史档案")
        private String category;

        @Schema(description = "安全等级", example = "CONFIDENTIAL")
        private String securityLevel;

        @Schema(description = "借阅状态", example = "BORROWED")
        private String status;

        @Schema(description = "借出时间", example = "2024-01-03 09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime borrowTime;

        @Schema(description = "归还时间", example = "2024-01-14 16:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime returnTime;

        // 构造函数
        public BorrowedArchiveInfo() {}

        public BorrowedArchiveInfo(Long archiveId, String archiveNumber, String title, String category) {
            this.archiveId = archiveId;
            this.archiveNumber = archiveNumber;
            this.title = title;
            this.category = category;
        }

        // Getter和Setter方法
        public Long getArchiveId() { return archiveId; }
        public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }
        public String getArchiveNumber() { return archiveNumber; }
        public void setArchiveNumber(String archiveNumber) { this.archiveNumber = archiveNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSecurityLevel() { return securityLevel; }
        public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getBorrowTime() { return borrowTime; }
        public void setBorrowTime(LocalDateTime borrowTime) { this.borrowTime = borrowTime; }
        public LocalDateTime getReturnTime() { return returnTime; }
        public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }
    }

    /**
     * 审批人信息内部类
     */
    @Schema(description = "审批人信息")
    public static class ReviewerInfo {
        @Schema(description = "审批人ID", example = "2")
        private Long reviewerId;

        @Schema(description = "审批人姓名", example = "李四")
        private String reviewerName;

        @Schema(description = "审批人部门", example = "档案管理部")
        private String department;

        // 构造函数
        public ReviewerInfo() {}

        public ReviewerInfo(Long reviewerId, String reviewerName, String department) {
            this.reviewerId = reviewerId;
            this.reviewerName = reviewerName;
            this.department = department;
        }

        // Getter和Setter方法
        public Long getReviewerId() { return reviewerId; }
        public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
        public String getReviewerName() { return reviewerName; }
        public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }

    // 构造函数
    public BorrowResponse() {}

    public BorrowResponse(Long id, String borrowNumber, String purpose, String status) {
        this.id = id;
        this.borrowNumber = borrowNumber;
        this.purpose = purpose;
        this.status = status;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBorrowNumber() { return borrowNumber; }
    public void setBorrowNumber(String borrowNumber) { this.borrowNumber = borrowNumber; }
    public BorrowerInfo getBorrower() { return borrower; }
    public void setBorrower(BorrowerInfo borrower) { this.borrower = borrower; }
    public List<BorrowedArchiveInfo> getArchives() { return archives; }
    public void setArchives(List<BorrowedArchiveInfo> archives) { this.archives = archives; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
    public ReviewerInfo getReviewer() { return reviewer; }
    public void setReviewer(ReviewerInfo reviewer) { this.reviewer = reviewer; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getExpectedReturnTime() { return expectedReturnTime; }
    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    public LocalDateTime getActualBorrowTime() { return actualBorrowTime; }
    public void setActualBorrowTime(LocalDateTime actualBorrowTime) { this.actualBorrowTime = actualBorrowTime; }
    public LocalDateTime getActualReturnTime() { return actualReturnTime; }
    public void setActualReturnTime(LocalDateTime actualReturnTime) { this.actualReturnTime = actualReturnTime; }
    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
    public Integer getOverdueDays() { return overdueDays; }
    public void setOverdueDays(Integer overdueDays) { this.overdueDays = overdueDays; }
    public Integer getBorrowDays() { return borrowDays; }
    public void setBorrowDays(Integer borrowDays) { this.borrowDays = borrowDays; }
    public Integer getExtensionCount() { return extensionCount; }
    public void setExtensionCount(Integer extensionCount) { this.extensionCount = extensionCount; }
    public LocalDateTime getLastExtensionTime() { return lastExtensionTime; }
    public void setLastExtensionTime(LocalDateTime lastExtensionTime) { this.lastExtensionTime = lastExtensionTime; }
    public String getReturnCondition() { return returnCondition; }
    public void setReturnCondition(String returnCondition) { this.returnCondition = returnCondition; }
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
    public String getUsageFeedback() { return usageFeedback; }
    public void setUsageFeedback(String usageFeedback) { this.usageFeedback = usageFeedback; }
    public Integer getCopiedPages() { return copiedPages; }
    public void setCopiedPages(Integer copiedPages) { this.copiedPages = copiedPages; }
    public Double getBorrowFee() { return borrowFee; }
    public void setBorrowFee(Double borrowFee) { this.borrowFee = borrowFee; }
    public Double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(Double depositAmount) { this.depositAmount = depositAmount; }
    public String getDepositStatus() { return depositStatus; }
    public void setDepositStatus(String depositStatus) { this.depositStatus = depositStatus; }
    public Double getPenaltyFee() { return penaltyFee; }
    public void setPenaltyFee(Double penaltyFee) { this.penaltyFee = penaltyFee; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "BorrowResponse{" +
                "id=" + id +
                ", borrowNumber='" + borrowNumber + '\'' +
                ", borrower=" + borrower +
                ", archives=" + archives +
                ", purpose='" + purpose + '\'' +
                ", status='" + status + '\'' +
                ", applyTime=" + applyTime +
                ", reviewTime=" + reviewTime +
                ", reviewer=" + reviewer +
                ", reviewComment='" + reviewComment + '\'' +
                ", expectedReturnTime=" + expectedReturnTime +
                ", actualBorrowTime=" + actualBorrowTime +
                ", actualReturnTime=" + actualReturnTime +
                ", isOverdue=" + isOverdue +
                ", overdueDays=" + overdueDays +
                ", borrowDays=" + borrowDays +
                ", extensionCount=" + extensionCount +
                ", lastExtensionTime=" + lastExtensionTime +
                ", returnCondition='" + returnCondition + '\'' +
                ", damageDescription='" + damageDescription + '\'' +
                ", usageFeedback='" + usageFeedback + '\'' +
                ", copiedPages=" + copiedPages +
                ", borrowFee=" + borrowFee +
                ", depositAmount=" + depositAmount +
                ", depositStatus='" + depositStatus + '\'' +
                ", penaltyFee=" + penaltyFee +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}