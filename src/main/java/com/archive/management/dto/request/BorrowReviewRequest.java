package com.archive.management.dto.request;

import com.archive.management.enums.BorrowReviewAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 借阅审批请求DTO
 * 封装借阅申请审批时的输入信息
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
@Schema(description = "借阅审批请求")
public class BorrowReviewRequest {

    @Schema(description = "审批动作", example = "APPROVE", allowableValues = {"APPROVE", "REJECT", "REQUEST_MODIFICATION"})
    @NotNull(message = "审批动作不能为空")
    private BorrowReviewAction action;

    @Schema(description = "审批意见", example = "申请材料齐全，同意借阅")
    @Size(max = 500, message = "审批意见不能超过500字符")
    private String reviewComment;

    @Schema(description = "审批备注", example = "需在指定地点使用，不得外借")
    @Size(max = 200, message = "审批备注不能超过200字符")
    private String reviewNote;

    @Schema(description = "批准的借阅天数", example = "30")
    private Integer approvedBorrowDays;

    @Schema(description = "使用限制说明", example = "仅限馆内阅览，不得复印")
    @Size(max = 300, message = "使用限制说明不能超过300字符")
    private String usageRestriction;

    @Schema(description = "特殊要求", example = "需有专人陪同")
    @Size(max = 200, message = "特殊要求不能超过200字符")
    private String specialRequirement;

    @Schema(description = "是否允许复印", example = "false")
    private Boolean allowCopy = false;

    @Schema(description = "允许复印页数", example = "10")
    private Integer allowedCopyPages;

    @Schema(description = "复印限制说明", example = "仅允许复印目录页")
    @Size(max = 200, message = "复印限制说明不能超过200字符")
    private String copyRestriction;

    @Schema(description = "紧急程度", example = "NORMAL", allowableValues = {"LOW", "NORMAL", "HIGH", "URGENT"})
    private String urgencyLevel = "NORMAL";

    @Schema(description = "需要修改的内容", example = "请补充详细的使用目的说明")
    @Size(max = 500, message = "需要修改的内容不能超过500字符")
    private String modificationRequired;

    @Schema(description = "审批优先级", example = "NORMAL", allowableValues = {"LOW", "NORMAL", "HIGH"})
    private String priority = "NORMAL";

    // 构造函数
    public BorrowReviewRequest() {}

    public BorrowReviewRequest(BorrowReviewAction action, String reviewComment) {
        this.action = action;
        this.reviewComment = reviewComment;
    }

    public BorrowReviewRequest(BorrowReviewAction action, String reviewComment, String reviewNote, 
                             Integer approvedBorrowDays) {
        this.action = action;
        this.reviewComment = reviewComment;
        this.reviewNote = reviewNote;
        this.approvedBorrowDays = approvedBorrowDays;
    }

    // Getter和Setter方法
    public BorrowReviewAction getAction() {
        return action;
    }

    public void setAction(BorrowReviewAction action) {
        this.action = action;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public Integer getApprovedBorrowDays() {
        return approvedBorrowDays;
    }

    public void setApprovedBorrowDays(Integer approvedBorrowDays) {
        this.approvedBorrowDays = approvedBorrowDays;
    }

    public String getUsageRestriction() {
        return usageRestriction;
    }

    public void setUsageRestriction(String usageRestriction) {
        this.usageRestriction = usageRestriction;
    }

    public String getSpecialRequirement() {
        return specialRequirement;
    }

    public void setSpecialRequirement(String specialRequirement) {
        this.specialRequirement = specialRequirement;
    }

    public Boolean getAllowCopy() {
        return allowCopy;
    }

    public void setAllowCopy(Boolean allowCopy) {
        this.allowCopy = allowCopy;
    }

    public Integer getAllowedCopyPages() {
        return allowedCopyPages;
    }

    public void setAllowedCopyPages(Integer allowedCopyPages) {
        this.allowedCopyPages = allowedCopyPages;
    }

    public String getCopyRestriction() {
        return copyRestriction;
    }

    public void setCopyRestriction(String copyRestriction) {
        this.copyRestriction = copyRestriction;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getModificationRequired() {
        return modificationRequired;
    }

    public void setModificationRequired(String modificationRequired) {
        this.modificationRequired = modificationRequired;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "BorrowReviewRequest{" +
                "action=" + action +
                ", reviewComment='" + reviewComment + '\'' +
                ", reviewNote='" + reviewNote + '\'' +
                ", approvedBorrowDays=" + approvedBorrowDays +
                ", usageRestriction='" + usageRestriction + '\'' +
                ", specialRequirement='" + specialRequirement + '\'' +
                ", allowCopy=" + allowCopy +
                ", allowedCopyPages=" + allowedCopyPages +
                ", copyRestriction='" + copyRestriction + '\'' +
                ", urgencyLevel='" + urgencyLevel + '\'' +
                ", modificationRequired='" + modificationRequired + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}