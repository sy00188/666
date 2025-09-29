package com.archive.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志实体类
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @Size(max = 100, message = "操作用户名长度不能超过100个字符")
    @TableField("username")
    private String username;

    /**
     * 操作类型：1-登录，2-登出，3-创建，4-更新，5-删除，6-查询，7-导入，8-导出，9-审批，10-其他
     */
    @NotNull(message = "操作类型不能为空")
    @Min(value = 1, message = "操作类型值不正确")
    @Max(value = 10, message = "操作类型值不正确")
    @TableField("operation_type")
    private Integer operationType;

    /**
     * 操作模块
     */
    @NotBlank(message = "操作模块不能为空")
    @Size(min = 1, max = 100, message = "操作模块长度必须在1-100个字符之间")
    @TableField("module")
    private String module;

    /**
     * 操作功能
     */
    @NotBlank(message = "操作功能不能为空")
    @Size(min = 1, max = 100, message = "操作功能长度必须在1-100个字符之间")
    @TableField("function")
    private String function;

    /**
     * 操作描述
     */
    @NotBlank(message = "操作描述不能为空")
    @Size(min = 1, max = 500, message = "操作描述长度必须在1-500个字符之间")
    @TableField("description")
    private String description;

    /**
     * 请求方法
     */
    @Size(max = 10, message = "请求方法长度不能超过10个字符")
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求URL
     */
    @Size(max = 1000, message = "请求URL长度不能超过1000个字符")
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 请求体
     */
    @TableField("request_body")
    private String requestBody;

    /**
     * 响应状态码
     */
    @TableField("response_status")
    private Integer responseStatus;

    /**
     * 响应结果
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * 操作结果：1-成功，2-失败，3-部分成功
     */
    @NotNull(message = "操作结果不能为空")
    @Min(value = 1, message = "操作结果值不正确")
    @Max(value = 3, message = "操作结果值不正确")
    @TableField("operation_result")
    private Integer operationResult;

    /**
     * 错误信息
     */
    @Size(max = 2000, message = "错误信息长度不能超过2000个字符")
    @TableField("error_message")
    private String errorMessage;

    /**
     * 执行时长（毫秒）
     */
    @Min(value = 0, message = "执行时长不能为负数")
    @TableField("execution_time")
    private Long executionTime;

    /**
     * 客户端IP地址
     */
    @Size(max = 50, message = "客户端IP地址长度不能超过50个字符")
    @TableField("client_ip")
    private String clientIp;

    /**
     * 客户端地址
     */
    @Size(max = 200, message = "客户端地址长度不能超过200个字符")
    @TableField("client_address")
    private String clientAddress;

    /**
     * 用户代理
     */
    @Size(max = 1000, message = "用户代理长度不能超过1000个字符")
    @TableField("user_agent")
    private String userAgent;

    /**
     * 浏览器类型
     */
    @Size(max = 100, message = "浏览器类型长度不能超过100个字符")
    @TableField("browser_type")
    private String browserType;

    /**
     * 操作系统
     */
    @Size(max = 100, message = "操作系统长度不能超过100个字符")
    @TableField("operating_system")
    private String operatingSystem;

    /**
     * 设备类型：1-PC，2-移动端，3-平板，4-其他
     */
    @Min(value = 1, message = "设备类型值不正确")
    @Max(value = 4, message = "设备类型值不正确")
    @TableField("device_type")
    private Integer deviceType;

    /**
     * 会话ID
     */
    @Size(max = 100, message = "会话ID长度不能超过100个字符")
    @TableField("session_id")
    private String sessionId;

    /**
     * 业务对象ID
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * 业务对象类型
     */
    @Size(max = 100, message = "业务对象类型长度不能超过100个字符")
    @TableField("business_type")
    private String businessType;

    /**
     * 业务对象名称
     */
    @Size(max = 200, message = "业务对象名称长度不能超过200个字符")
    @TableField("business_name")
    private String businessName;

    /**
     * 操作前数据
     */
    @TableField("old_data")
    private String oldData;

    /**
     * 操作后数据
     */
    @TableField("new_data")
    private String newData;

    /**
     * 风险级别：1-低，2-中，3-高，4-极高
     */
    @Min(value = 1, message = "风险级别值不正确")
    @Max(value = 4, message = "风险级别值不正确")
    @TableField("risk_level")
    private Integer riskLevel;

    /**
     * 是否敏感操作：0-否，1-是
     */
    @TableField("is_sensitive")
    private Integer isSensitive;

    /**
     * 是否需要审批：0-否，1-是
     */
    @TableField("need_approval")
    private Integer needApproval;

    /**
     * 审批状态：1-待审批，2-已通过，3-已拒绝，4-无需审批
     */
    @Min(value = 1, message = "审批状态值不正确")
    @Max(value = 4, message = "审批状态值不正确")
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Size(max = 100, message = "审批人姓名长度不能超过100个字符")
    @TableField("approver_name")
    private String approverName;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @Size(max = 1000, message = "审批意见长度不能超过1000个字符")
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 标签（用逗号分隔）
     */
    @Size(max = 500, message = "标签长度不能超过500个字符")
    @TableField("tags")
    private String tags;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField("extra_info")
    private String extraInfo;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ==================== 非数据库字段 ====================

    /**
     * 用户信息（非数据库字段）
     */
    @TableField(exist = false)
    private User user;

    /**
     * 审批人信息（非数据库字段）
     */
    @TableField(exist = false)
    private User approver;

    /**
     * 标签列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<String> tagList;

    // ==================== 业务方法 ====================

    /**
     * 检查操作是否为登录
     */
    public boolean isLoginOperation() {
        return this.operationType != null && this.operationType == 1;
    }

    /**
     * 检查操作是否为登出
     */
    public boolean isLogoutOperation() {
        return this.operationType != null && this.operationType == 2;
    }

    /**
     * 检查操作是否为创建
     */
    public boolean isCreateOperation() {
        return this.operationType != null && this.operationType == 3;
    }

    /**
     * 检查操作是否为更新
     */
    public boolean isUpdateOperation() {
        return this.operationType != null && this.operationType == 4;
    }

    /**
     * 检查操作是否为删除
     */
    public boolean isDeleteOperation() {
        return this.operationType != null && this.operationType == 5;
    }

    /**
     * 检查操作是否为查询
     */
    public boolean isQueryOperation() {
        return this.operationType != null && this.operationType == 6;
    }

    /**
     * 检查操作是否为导入
     */
    public boolean isImportOperation() {
        return this.operationType != null && this.operationType == 7;
    }

    /**
     * 检查操作是否为导出
     */
    public boolean isExportOperation() {
        return this.operationType != null && this.operationType == 8;
    }

    /**
     * 检查操作是否为审批
     */
    public boolean isApprovalOperation() {
        return this.operationType != null && this.operationType == 9;
    }

    /**
     * 检查操作结果是否成功
     */
    public boolean isSuccess() {
        return this.operationResult != null && this.operationResult == 1;
    }

    /**
     * 检查操作结果是否失败
     */
    public boolean isFailed() {
        return this.operationResult != null && this.operationResult == 2;
    }

    /**
     * 检查操作结果是否部分成功
     */
    public boolean isPartialSuccess() {
        return this.operationResult != null && this.operationResult == 3;
    }

    /**
     * 检查是否为敏感操作
     */
    public boolean isSensitive() {
        return this.isSensitive != null && this.isSensitive == 1;
    }

    /**
     * 检查是否需要审批
     */
    public boolean isNeedApproval() {
        return this.needApproval != null && this.needApproval == 1;
    }

    /**
     * 检查审批状态是否为待审批
     */
    public boolean isPendingApproval() {
        return this.approvalStatus != null && this.approvalStatus == 1;
    }

    /**
     * 检查审批状态是否为已通过
     */
    public boolean isApproved() {
        return this.approvalStatus != null && this.approvalStatus == 2;
    }

    /**
     * 检查审批状态是否为已拒绝
     */
    public boolean isRejected() {
        return this.approvalStatus != null && this.approvalStatus == 3;
    }

    /**
     * 检查是否无需审批
     */
    public boolean isNoApprovalNeeded() {
        return this.approvalStatus != null && this.approvalStatus == 4;
    }

    /**
     * 检查设备类型是否为PC
     */
    public boolean isPcDevice() {
        return this.deviceType != null && this.deviceType == 1;
    }

    /**
     * 检查设备类型是否为移动端
     */
    public boolean isMobileDevice() {
        return this.deviceType != null && this.deviceType == 2;
    }

    /**
     * 检查设备类型是否为平板
     */
    public boolean isTabletDevice() {
        return this.deviceType != null && this.deviceType == 3;
    }

    /**
     * 获取操作类型描述
     */
    public String getOperationTypeDesc() {
        if (this.operationType == null) {
            return "未知";
        }
        switch (this.operationType) {
            case 1:
                return "登录";
            case 2:
                return "登出";
            case 3:
                return "创建";
            case 4:
                return "更新";
            case 5:
                return "删除";
            case 6:
                return "查询";
            case 7:
                return "导入";
            case 8:
                return "导出";
            case 9:
                return "审批";
            case 10:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 获取操作结果描述
     */
    public String getOperationResultDesc() {
        if (this.operationResult == null) {
            return "未知";
        }
        switch (this.operationResult) {
            case 1:
                return "成功";
            case 2:
                return "失败";
            case 3:
                return "部分成功";
            default:
                return "未知";
        }
    }

    /**
     * 获取风险级别描述
     */
    public String getRiskLevelDesc() {
        if (this.riskLevel == null) {
            return "未知";
        }
        switch (this.riskLevel) {
            case 1:
                return "低";
            case 2:
                return "中";
            case 3:
                return "高";
            case 4:
                return "极高";
            default:
                return "未知";
        }
    }

    /**
     * 获取设备类型描述
     */
    public String getDeviceTypeDesc() {
        if (this.deviceType == null) {
            return "未知";
        }
        switch (this.deviceType) {
            case 1:
                return "PC";
            case 2:
                return "移动端";
            case 3:
                return "平板";
            case 4:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 获取审批状态描述
     */
    public String getApprovalStatusDesc() {
        if (this.approvalStatus == null) {
            return "未知";
        }
        switch (this.approvalStatus) {
            case 1:
                return "待审批";
            case 2:
                return "已通过";
            case 3:
                return "已拒绝";
            case 4:
                return "无需审批";
            default:
                return "未知";
        }
    }

    /**
     * 获取格式化的执行时长
     */
    public String getFormattedExecutionTime() {
        if (this.executionTime == null) {
            return "0ms";
        }
        
        if (this.executionTime < 1000) {
            return this.executionTime + "ms";
        } else if (this.executionTime < 60000) {
            return String.format("%.2fs", this.executionTime / 1000.0);
        } else {
            long minutes = this.executionTime / 60000;
            long seconds = (this.executionTime % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    /**
     * 解析标签字符串为列表
     */
    public java.util.List<String> parseTagsToList() {
        if (this.tags == null || this.tags.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(this.tags.split(","));
    }

    /**
     * 检查是否包含指定标签
     */
    public boolean hasTag(String tag) {
        java.util.List<String> tagList = this.parseTagsToList();
        return tagList.contains(tag);
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        java.util.List<String> tagList = this.parseTagsToList();
        if (!tagList.contains(tag)) {
            tagList.add(tag);
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * 移除标签
     */
    public void removeTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        java.util.List<String> tagList = this.parseTagsToList();
        tagList.remove(tag);
        this.tags = String.join(",", tagList);
    }

    /**
     * 检查是否为高风险操作
     */
    public boolean isHighRisk() {
        return this.riskLevel != null && this.riskLevel >= 3;
    }

    /**
     * 检查是否为慢操作（执行时间超过5秒）
     */
    public boolean isSlowOperation() {
        return this.executionTime != null && this.executionTime > 5000;
    }
}