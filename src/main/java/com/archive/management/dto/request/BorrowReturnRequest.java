package com.archive.management.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅归还请求DTO
 * 封装档案归还时的输入信息
 * 
 * @author System
 * @version 1.0
 * @since 2024-01-01
 */
@Schema(description = "借阅归还请求")
public class BorrowReturnRequest {

    @Schema(description = "实际归还时间", example = "2024-01-15 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualReturnTime;

    @Schema(description = "归还说明", example = "档案完好无损，按时归还")
    @Size(max = 500, message = "归还说明不能超过500字符")
    private String returnNote;

    @Schema(description = "档案状态检查", example = "GOOD", allowableValues = {"GOOD", "DAMAGED", "MISSING_PAGES", "OTHER"})
    private String archiveCondition = "GOOD";

    @Schema(description = "损坏描述", example = "第3页有轻微折痕")
    @Size(max = 300, message = "损坏描述不能超过300字符")
    private String damageDescription;

    @Schema(description = "缺失页面列表", example = "[3, 5, 7]")
    private List<Integer> missingPages;

    @Schema(description = "复印页数", example = "10")
    private Integer copiedPages;

    @Schema(description = "复印说明", example = "已复印第1-10页用于研究")
    @Size(max = 200, message = "复印说明不能超过200字符")
    private String copyNote;

    @Schema(description = "使用情况反馈", example = "档案内容对研究很有帮助")
    @Size(max = 500, message = "使用情况反馈不能超过500字符")
    private String usageFeedback;

    @Schema(description = "是否建议其他用户", example = "true")
    private Boolean recommendToOthers = false;

    @Schema(description = "推荐理由", example = "内容详实，对相关研究很有价值")
    @Size(max = 200, message = "推荐理由不能超过200字符")
    private String recommendReason;

    @Schema(description = "归还地点", example = "档案馆一楼服务台")
    @Size(max = 100, message = "归还地点不能超过100字符")
    private String returnLocation;

    @Schema(description = "接收人员", example = "李四")
    @Size(max = 50, message = "接收人员不能超过50字符")
    private String receivedBy;

    @Schema(description = "是否需要延期", example = "false")
    private Boolean needExtension = false;

    @Schema(description = "延期原因", example = "研究尚未完成，需要继续使用")
    @Size(max = 300, message = "延期原因不能超过300字符")
    private String extensionReason;

    @Schema(description = "申请延期天数", example = "15")
    private Integer extensionDays;

    @Schema(description = "紧急归还", example = "false")
    private Boolean emergencyReturn = false;

    @Schema(description = "紧急归还原因", example = "档案需要紧急调用")
    @Size(max = 200, message = "紧急归还原因不能超过200字符")
    private String emergencyReason;

    @Schema(description = "附件文件列表", example = "[\"receipt.pdf\", \"damage_photo.jpg\"]")
    private List<String> attachmentFiles;

    @Schema(description = "特殊说明", example = "档案使用过程中发现的问题")
    @Size(max = 500, message = "特殊说明不能超过500字符")
    private String specialNote;

    // 构造函数
    public BorrowReturnRequest() {}

    public BorrowReturnRequest(LocalDateTime actualReturnTime, String returnNote, String archiveCondition) {
        this.actualReturnTime = actualReturnTime;
        this.returnNote = returnNote;
        this.archiveCondition = archiveCondition;
    }

    // Getter和Setter方法
    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    public String getReturnNote() {
        return returnNote;
    }

    public void setReturnNote(String returnNote) {
        this.returnNote = returnNote;
    }

    public String getArchiveCondition() {
        return archiveCondition;
    }

    public void setArchiveCondition(String archiveCondition) {
        this.archiveCondition = archiveCondition;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public List<Integer> getMissingPages() {
        return missingPages;
    }

    public void setMissingPages(List<Integer> missingPages) {
        this.missingPages = missingPages;
    }

    public Integer getCopiedPages() {
        return copiedPages;
    }

    public void setCopiedPages(Integer copiedPages) {
        this.copiedPages = copiedPages;
    }

    public String getCopyNote() {
        return copyNote;
    }

    public void setCopyNote(String copyNote) {
        this.copyNote = copyNote;
    }

    public String getUsageFeedback() {
        return usageFeedback;
    }

    public void setUsageFeedback(String usageFeedback) {
        this.usageFeedback = usageFeedback;
    }

    public Boolean getRecommendToOthers() {
        return recommendToOthers;
    }

    public void setRecommendToOthers(Boolean recommendToOthers) {
        this.recommendToOthers = recommendToOthers;
    }

    public String getRecommendReason() {
        return recommendReason;
    }

    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }

    public String getReturnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(String returnLocation) {
        this.returnLocation = returnLocation;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Boolean getNeedExtension() {
        return needExtension;
    }

    public void setNeedExtension(Boolean needExtension) {
        this.needExtension = needExtension;
    }

    public String getExtensionReason() {
        return extensionReason;
    }

    public void setExtensionReason(String extensionReason) {
        this.extensionReason = extensionReason;
    }

    public Integer getExtensionDays() {
        return extensionDays;
    }

    public void setExtensionDays(Integer extensionDays) {
        this.extensionDays = extensionDays;
    }

    public Boolean getEmergencyReturn() {
        return emergencyReturn;
    }

    public void setEmergencyReturn(Boolean emergencyReturn) {
        this.emergencyReturn = emergencyReturn;
    }

    public String getEmergencyReason() {
        return emergencyReason;
    }

    public void setEmergencyReason(String emergencyReason) {
        this.emergencyReason = emergencyReason;
    }

    public List<String> getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(List<String> attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }

    public String getSpecialNote() {
        return specialNote;
    }

    public void setSpecialNote(String specialNote) {
        this.specialNote = specialNote;
    }

    @Override
    public String toString() {
        return "BorrowReturnRequest{" +
                "actualReturnTime=" + actualReturnTime +
                ", returnNote='" + returnNote + '\'' +
                ", archiveCondition='" + archiveCondition + '\'' +
                ", damageDescription='" + damageDescription + '\'' +
                ", missingPages=" + missingPages +
                ", copiedPages=" + copiedPages +
                ", copyNote='" + copyNote + '\'' +
                ", usageFeedback='" + usageFeedback + '\'' +
                ", recommendToOthers=" + recommendToOthers +
                ", recommendReason='" + recommendReason + '\'' +
                ", returnLocation='" + returnLocation + '\'' +
                ", receivedBy='" + receivedBy + '\'' +
                ", needExtension=" + needExtension +
                ", extensionReason='" + extensionReason + '\'' +
                ", extensionDays=" + extensionDays +
                ", emergencyReturn=" + emergencyReturn +
                ", emergencyReason='" + emergencyReason + '\'' +
                ", attachmentFiles=" + attachmentFiles +
                ", specialNote='" + specialNote + '\'' +
                '}';
    }
}