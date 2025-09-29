package com.archive.management.dto.request;

import com.archive.management.enums.BorrowStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅查询请求DTO
 * 封装借阅记录查询的条件参数
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
@Schema(description = "借阅查询请求")
public class BorrowQueryRequest {

    @Schema(description = "借阅记录ID", example = "1")
    private Long borrowId;

    @Schema(description = "申请人ID", example = "1")
    private Long applicantId;

    @Schema(description = "申请人用户名", example = "zhangsan")
    @Size(max = 50, message = "申请人用户名不能超过50字符")
    private String applicantUsername;

    @Schema(description = "申请人真实姓名", example = "张三")
    @Size(max = 50, message = "申请人真实姓名不能超过50字符")
    private String applicantRealName;

    @Schema(description = "档案ID", example = "1")
    private Long archiveId;

    @Schema(description = "档案标题", example = "重要文件")
    @Size(max = 200, message = "档案标题不能超过200字符")
    private String archiveTitle;

    @Schema(description = "档案编号", example = "ARCH-2024-001")
    @Size(max = 50, message = "档案编号不能超过50字符")
    private String archiveNumber;

    @Schema(description = "借阅状态列表", example = "[\"PENDING\", \"APPROVED\"]")
    private List<BorrowStatus> statusList;

    @Schema(description = "借阅目的", example = "学术研究")
    @Size(max = 200, message = "借阅目的不能超过200字符")
    private String purpose;

    @Schema(description = "使用地点", example = "图书馆")
    @Size(max = 100, message = "使用地点不能超过100字符")
    private String usageLocation;

    @Schema(description = "申请开始日期", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applicationStartDate;

    @Schema(description = "申请结束日期", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applicationEndDate;

    @Schema(description = "预计归还开始日期", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedReturnStartDate;

    @Schema(description = "预计归还结束日期", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedReturnEndDate;

    @Schema(description = "实际归还开始日期", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualReturnStartDate;

    @Schema(description = "实际归还结束日期", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualReturnEndDate;

    @Schema(description = "是否逾期", example = "true")
    private Boolean isOverdue;

    @Schema(description = "是否延期", example = "false")
    private Boolean hasExtension;

    @Schema(description = "审批人ID", example = "2")
    private Long reviewerId;

    @Schema(description = "审批人用户名", example = "admin")
    @Size(max = 50, message = "审批人用户名不能超过50字符")
    private String reviewerUsername;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "档案管理部")
    @Size(max = 100, message = "部门名称不能超过100字符")
    private String departmentName;

    @Schema(description = "是否需要复印", example = "true")
    private Boolean needCopy;

    @Schema(description = "排序字段", example = "applicationDate")
    @Size(max = 50, message = "排序字段不能超过50字符")
    private String sortBy = "applicationDate";

    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";

    // 构造函数
    public BorrowQueryRequest() {}

    // Getter和Setter方法
    public Long getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantUsername() {
        return applicantUsername;
    }

    public void setApplicantUsername(String applicantUsername) {
        this.applicantUsername = applicantUsername;
    }

    public String getApplicantRealName() {
        return applicantRealName;
    }

    public void setApplicantRealName(String applicantRealName) {
        this.applicantRealName = applicantRealName;
    }

    public Long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
    }

    public String getArchiveTitle() {
        return archiveTitle;
    }

    public void setArchiveTitle(String archiveTitle) {
        this.archiveTitle = archiveTitle;
    }

    public String getArchiveNumber() {
        return archiveNumber;
    }

    public void setArchiveNumber(String archiveNumber) {
        this.archiveNumber = archiveNumber;
    }

    public List<BorrowStatus> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<BorrowStatus> statusList) {
        this.statusList = statusList;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getUsageLocation() {
        return usageLocation;
    }

    public void setUsageLocation(String usageLocation) {
        this.usageLocation = usageLocation;
    }

    public LocalDateTime getApplicationStartDate() {
        return applicationStartDate;
    }

    public void setApplicationStartDate(LocalDateTime applicationStartDate) {
        this.applicationStartDate = applicationStartDate;
    }

    public LocalDateTime getApplicationEndDate() {
        return applicationEndDate;
    }

    public void setApplicationEndDate(LocalDateTime applicationEndDate) {
        this.applicationEndDate = applicationEndDate;
    }

    public LocalDateTime getExpectedReturnStartDate() {
        return expectedReturnStartDate;
    }

    public void setExpectedReturnStartDate(LocalDateTime expectedReturnStartDate) {
        this.expectedReturnStartDate = expectedReturnStartDate;
    }

    public LocalDateTime getExpectedReturnEndDate() {
        return expectedReturnEndDate;
    }

    public void setExpectedReturnEndDate(LocalDateTime expectedReturnEndDate) {
        this.expectedReturnEndDate = expectedReturnEndDate;
    }

    public LocalDateTime getActualReturnStartDate() {
        return actualReturnStartDate;
    }

    public void setActualReturnStartDate(LocalDateTime actualReturnStartDate) {
        this.actualReturnStartDate = actualReturnStartDate;
    }

    public LocalDateTime getActualReturnEndDate() {
        return actualReturnEndDate;
    }

    public void setActualReturnEndDate(LocalDateTime actualReturnEndDate) {
        this.actualReturnEndDate = actualReturnEndDate;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

    public Boolean getHasExtension() {
        return hasExtension;
    }

    public void setHasExtension(Boolean hasExtension) {
        this.hasExtension = hasExtension;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Boolean getNeedCopy() {
        return needCopy;
    }

    public void setNeedCopy(Boolean needCopy) {
        this.needCopy = needCopy;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "BorrowQueryRequest{" +
                "borrowId=" + borrowId +
                ", applicantId=" + applicantId +
                ", applicantUsername='" + applicantUsername + '\'' +
                ", applicantRealName='" + applicantRealName + '\'' +
                ", archiveId=" + archiveId +
                ", archiveTitle='" + archiveTitle + '\'' +
                ", archiveNumber='" + archiveNumber + '\'' +
                ", statusList=" + statusList +
                ", purpose='" + purpose + '\'' +
                ", usageLocation='" + usageLocation + '\'' +
                ", applicationStartDate=" + applicationStartDate +
                ", applicationEndDate=" + applicationEndDate +
                ", expectedReturnStartDate=" + expectedReturnStartDate +
                ", expectedReturnEndDate=" + expectedReturnEndDate +
                ", actualReturnStartDate=" + actualReturnStartDate +
                ", actualReturnEndDate=" + actualReturnEndDate +
                ", isOverdue=" + isOverdue +
                ", hasExtension=" + hasExtension +
                ", reviewerId=" + reviewerId +
                ", reviewerUsername='" + reviewerUsername + '\'' +
                ", departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", needCopy=" + needCopy +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}