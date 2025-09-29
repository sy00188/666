package com.archive.management.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅申请请求DTO
 * 封装用户提交借阅申请时的输入信息
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
@Schema(description = "借阅申请请求")
public class BorrowCreateRequest {

    @Schema(description = "档案ID列表", example = "[1, 2, 3]")
    @NotNull(message = "档案ID列表不能为空")
    @Size(min = 1, max = 10, message = "单次申请档案数量应在1-10之间")
    private List<Long> archiveIds;

    @Schema(description = "借阅目的", example = "学术研究")
    @NotBlank(message = "借阅目的不能为空")
    @Size(max = 200, message = "借阅目的不能超过200字符")
    private String purpose;

    @Schema(description = "预计归还日期", example = "2024-02-01 10:00:00")
    @NotNull(message = "预计归还日期不能为空")
    @Future(message = "预计归还日期必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedReturnDate;

    @Schema(description = "借阅期限（天数）", example = "30")
    @NotNull(message = "借阅期限不能为空")
    private Integer borrowDays;

    @Schema(description = "使用地点", example = "图书馆阅览室")
    @Size(max = 100, message = "使用地点不能超过100字符")
    private String usageLocation;

    @Schema(description = "联系电话", example = "13800138000")
    @Size(max = 20, message = "联系电话不能超过20字符")
    private String contactPhone;

    @Schema(description = "紧急联系人", example = "张三")
    @Size(max = 50, message = "紧急联系人不能超过50字符")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话", example = "13900139000")
    @Size(max = 20, message = "紧急联系人电话不能超过20字符")
    private String emergencyContactPhone;

    @Schema(description = "申请说明", example = "用于毕业论文研究，需要查阅相关历史资料")
    @Size(max = 500, message = "申请说明不能超过500字符")
    private String applicationNote;

    @Schema(description = "是否需要复印", example = "false")
    private Boolean needCopy = false;

    @Schema(description = "复印页数", example = "10")
    private Integer copyPages;

    @Schema(description = "复印说明", example = "仅复印第1-10页")
    @Size(max = 200, message = "复印说明不能超过200字符")
    private String copyNote;

    // 构造函数
    public BorrowCreateRequest() {}

    public BorrowCreateRequest(List<Long> archiveIds, String purpose, LocalDateTime expectedReturnDate, 
                             Integer borrowDays, String usageLocation) {
        this.archiveIds = archiveIds;
        this.purpose = purpose;
        this.expectedReturnDate = expectedReturnDate;
        this.borrowDays = borrowDays;
        this.usageLocation = usageLocation;
    }

    // Getter和Setter方法
    public List<Long> getArchiveIds() {
        return archiveIds;
    }

    public void setArchiveIds(List<Long> archiveIds) {
        this.archiveIds = archiveIds;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDateTime getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public Integer getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }

    public String getUsageLocation() {
        return usageLocation;
    }

    public void setUsageLocation(String usageLocation) {
        this.usageLocation = usageLocation;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getApplicationNote() {
        return applicationNote;
    }

    public void setApplicationNote(String applicationNote) {
        this.applicationNote = applicationNote;
    }

    public Boolean getNeedCopy() {
        return needCopy;
    }

    public void setNeedCopy(Boolean needCopy) {
        this.needCopy = needCopy;
    }

    public Integer getCopyPages() {
        return copyPages;
    }

    public void setCopyPages(Integer copyPages) {
        this.copyPages = copyPages;
    }

    public String getCopyNote() {
        return copyNote;
    }

    public void setCopyNote(String copyNote) {
        this.copyNote = copyNote;
    }

    @Override
    public String toString() {
        return "BorrowCreateRequest{" +
                "archiveIds=" + archiveIds +
                ", purpose='" + purpose + '\'' +
                ", expectedReturnDate=" + expectedReturnDate +
                ", borrowDays=" + borrowDays +
                ", usageLocation='" + usageLocation + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                ", emergencyContactPhone='" + emergencyContactPhone + '\'' +
                ", applicationNote='" + applicationNote + '\'' +
                ", needCopy=" + needCopy +
                ", copyPages=" + copyPages +
                ", copyNote='" + copyNote + '\'' +
                '}';
    }
}