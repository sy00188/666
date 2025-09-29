package com.archive.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志数据传输对象
 * 用于API接口的数据传输
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审计日志数据传输对象")
public class AuditLogDTO {

    @Schema(description = "日志ID", example = "1")
    private Long id;

    @Schema(description = "日志编码", example = "LOG_20240101_001", required = true)
    @NotBlank(message = "日志编码不能为空")
    @Size(min = 5, max = 100, message = "日志编码长度必须在5-100个字符之间")
    private String logCode;

    @Schema(description = "操作类型", example = "1", allowableValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, required = true)
    @NotNull(message = "操作类型不能为空")
    @Min(value = 1, message = "操作类型值必须为1-10")
    @Max(value = 10, message = "操作类型值必须为1-10")
    private Integer operationType;

    @Schema(description = "操作模块", example = "用户管理", required = true)
    @NotBlank(message = "操作模块不能为空")
    @Size(min = 2, max = 50, message = "操作模块长度必须在2-50个字符之间")
    private String operationModule;

    @Schema(description = "操作功能", example = "创建用户", required = true)
    @NotBlank(message = "操作功能不能为空")
    @Size(min = 2, max = 100, message = "操作功能长度必须在2-100个字符之间")
    private String operationFunction;

    @Schema(description = "操作描述", example = "创建了新用户：张三")
    @Size(max = 500, message = "操作描述长度不能超过500个字符")
    private String operationDescription;

    @Schema(description = "操作对象类型", example = "User")
    @Size(max = 50, message = "操作对象类型长度不能超过50个字符")
    private String targetType;

    @Schema(description = "操作对象ID", example = "123")
    @Size(max = 100, message = "操作对象ID长度不能超过100个字符")
    private String targetId;

    @Schema(description = "操作对象名称", example = "张三")
    @Size(max = 200, message = "操作对象名称长度不能超过200个字符")
    private String targetName;

    @Schema(description = "操作前数据")
    private String beforeData;

    @Schema(description = "操作后数据")
    private String afterData;

    @Schema(description = "操作结果", example = "1", allowableValues = {"1", "2", "3"}, required = true)
    @NotNull(message = "操作结果不能为空")
    @Min(value = 1, message = "操作结果值必须为1-3")
    @Max(value = 3, message = "操作结果值必须为1-3")
    private Integer operationResult;

    @Schema(description = "错误信息", example = "用户名已存在")
    @Size(max = 1000, message = "错误信息长度不能超过1000个字符")
    private String errorMessage;

    @Schema(description = "错误代码", example = "USER_EXISTS")
    @Size(max = 50, message = "错误代码长度不能超过50个字符")
    private String errorCode;

    @Schema(description = "操作耗时(毫秒)", example = "150")
    @Min(value = 0, message = "操作耗时不能为负数")
    private Long duration;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人用户名", example = "admin")
    @Size(max = 50, message = "操作人用户名长度不能超过50个字符")
    private String operatorUsername;

    @Schema(description = "操作人姓名", example = "系统管理员")
    @Size(max = 100, message = "操作人姓名长度不能超过100个字符")
    private String operatorName;

    @Schema(description = "操作人类型", example = "1", allowableValues = {"1", "2", "3", "4"})
    @Min(value = 1, message = "操作人类型值必须为1-4")
    @Max(value = 4, message = "操作人类型值必须为1-4")
    private Integer operatorType;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    @Size(max = 45, message = "客户端IP长度不能超过45个字符")
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$", 
             message = "客户端IP格式不正确")
    private String clientIp;

    @Schema(description = "客户端地址", example = "北京市朝阳区")
    @Size(max = 200, message = "客户端地址长度不能超过200个字符")
    private String clientAddress;

    @Schema(description = "用户代理", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    private String userAgent;

    @Schema(description = "浏览器类型", example = "Chrome")
    @Size(max = 50, message = "浏览器类型长度不能超过50个字符")
    private String browserType;

    @Schema(description = "操作系统", example = "Windows 10")
    @Size(max = 50, message = "操作系统长度不能超过50个字符")
    private String operatingSystem;

    @Schema(description = "设备类型", example = "PC")
    @Size(max = 20, message = "设备类型长度不能超过20个字符")
    private String deviceType;

    @Schema(description = "请求URL", example = "/api/users")
    @Size(max = 500, message = "请求URL长度不能超过500个字符")
    private String requestUrl;

    @Schema(description = "请求方法", example = "POST")
    @Size(max = 10, message = "请求方法长度不能超过10个字符")
    private String requestMethod;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "响应状态码", example = "200")
    @Min(value = 100, message = "响应状态码不能小于100")
    @Max(value = 599, message = "响应状态码不能大于599")
    private Integer responseStatus;

    @Schema(description = "响应数据")
    private String responseData;

    @Schema(description = "会话ID", example = "SESSION_123456")
    @Size(max = 100, message = "会话ID长度不能超过100个字符")
    private String sessionId;

    @Schema(description = "请求ID", example = "REQ_123456")
    @Size(max = 100, message = "请求ID长度不能超过100个字符")
    private String requestId;

    @Schema(description = "业务流水号", example = "BIZ_123456")
    @Size(max = 100, message = "业务流水号长度不能超过100个字符")
    private String businessId;

    @Schema(description = "风险等级", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @Min(value = 1, message = "风险等级值必须为1-5")
    @Max(value = 5, message = "风险等级值必须为1-5")
    private Integer riskLevel;

    @Schema(description = "是否敏感操作", example = "false")
    private Boolean sensitiveOperation;

    @Schema(description = "数据分类", example = "1", allowableValues = {"1", "2", "3", "4"})
    @Min(value = 1, message = "数据分类值必须为1-4")
    @Max(value = 4, message = "数据分类值必须为1-4")
    private Integer dataClassification;

    @Schema(description = "合规标签")
    private String complianceLabels;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedProperties;

    @Schema(description = "操作时间", example = "2024-01-01 12:00:00", required = true)
    @NotNull(message = "操作时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "是否已归档", example = "false")
    private Boolean archived;

    @Schema(description = "归档时间", example = "2024-01-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archiveTime;

    @Schema(description = "保留期限(天)", example = "365")
    @Min(value = 1, message = "保留期限必须为正数")
    private Integer retentionDays;

    @Schema(description = "过期时间", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationTime;

    /**
     * 获取操作类型显示文本
     */
    @Schema(description = "操作类型显示文本")
    public String getOperationTypeText() {
        if (operationType == null) {
            return "未知";
        }
        switch (operationType) {
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
                return "上传";
            case 10:
                return "下载";
            default:
                return "其他";
        }
    }

    /**
     * 获取操作结果显示文本
     */
    @Schema(description = "操作结果显示文本")
    public String getOperationResultText() {
        if (operationResult == null) {
            return "未知";
        }
        switch (operationResult) {
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
     * 获取操作人类型显示文本
     */
    @Schema(description = "操作人类型显示文本")
    public String getOperatorTypeText() {
        if (operatorType == null) {
            return "未知";
        }
        switch (operatorType) {
            case 1:
                return "系统用户";
            case 2:
                return "管理员";
            case 3:
                return "普通用户";
            case 4:
                return "系统任务";
            default:
                return "未知";
        }
    }

    /**
     * 获取风险等级显示文本
     */
    @Schema(description = "风险等级显示文本")
    public String getRiskLevelText() {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case 1:
                return "极低";
            case 2:
                return "低";
            case 3:
                return "中";
            case 4:
                return "高";
            case 5:
                return "极高";
            default:
                return "未知";
        }
    }

    /**
     * 获取数据分类显示文本
     */
    @Schema(description = "数据分类显示文本")
    public String getDataClassificationText() {
        if (dataClassification == null) {
            return "未知";
        }
        switch (dataClassification) {
            case 1:
                return "公开";
            case 2:
                return "内部";
            case 3:
                return "机密";
            case 4:
                return "绝密";
            default:
                return "未知";
        }
    }

    /**
     * 判断操作是否成功
     */
    @Schema(description = "操作是否成功")
    public boolean isSuccess() {
        return operationResult != null && operationResult == 1;
    }

    /**
     * 判断操作是否失败
     */
    @Schema(description = "操作是否失败")
    public boolean isFailure() {
        return operationResult != null && operationResult == 2;
    }

    /**
     * 判断是否为敏感操作
     */
    @Schema(description = "是否为敏感操作")
    public boolean isSensitive() {
        return sensitiveOperation != null && sensitiveOperation;
    }

    /**
     * 判断是否为高风险操作
     */
    @Schema(description = "是否为高风险操作")
    public boolean isHighRisk() {
        return riskLevel != null && riskLevel >= 4;
    }

    /**
     * 判断是否已归档
     */
    @Schema(description = "是否已归档")
    public boolean isArchived() {
        return archived != null && archived;
    }

    /**
     * 判断是否已过期
     */
    @Schema(description = "是否已过期")
    public boolean isExpired() {
        return expirationTime != null && LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * 判断是否需要归档
     */
    @Schema(description = "是否需要归档")
    public boolean needsArchive() {
        if (archived != null && archived) {
            return false; // 已归档
        }
        
        if (retentionDays == null || createTime == null) {
            return false;
        }
        
        LocalDateTime archiveDate = createTime.plusDays(retentionDays);
        return LocalDateTime.now().isAfter(archiveDate);
    }

    /**
     * 获取操作耗时显示文本
     */
    @Schema(description = "操作耗时显示文本")
    public String getDurationText() {
        if (duration == null) {
            return "未知";
        }
        
        if (duration < 1000) {
            return duration + "ms";
        } else if (duration < 60000) {
            return String.format("%.2fs", duration / 1000.0);
        } else {
            long minutes = duration / 60000;
            long seconds = (duration % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    /**
     * 获取风险等级颜色
     */
    @Schema(description = "风险等级颜色")
    public String getRiskLevelColor() {
        if (riskLevel == null) {
            return "#666666";
        }
        switch (riskLevel) {
            case 1:
                return "#52c41a"; // 绿色
            case 2:
                return "#1890ff"; // 蓝色
            case 3:
                return "#faad14"; // 橙色
            case 4:
                return "#ff4d4f"; // 红色
            case 5:
                return "#722ed1"; // 紫色
            default:
                return "#666666"; // 灰色
        }
    }

    /**
     * 获取操作结果颜色
     */
    @Schema(description = "操作结果颜色")
    public String getOperationResultColor() {
        if (operationResult == null) {
            return "#666666";
        }
        switch (operationResult) {
            case 1:
                return "#52c41a"; // 绿色 - 成功
            case 2:
                return "#ff4d4f"; // 红色 - 失败
            case 3:
                return "#faad14"; // 橙色 - 部分成功
            default:
                return "#666666"; // 灰色
        }
    }

    /**
     * 判断是否为登录操作
     */
    @Schema(description = "是否为登录操作")
    public boolean isLoginOperation() {
        return operationType != null && operationType == 1;
    }

    /**
     * 判断是否为登出操作
     */
    @Schema(description = "是否为登出操作")
    public boolean isLogoutOperation() {
        return operationType != null && operationType == 2;
    }

    /**
     * 判断是否为数据修改操作
     */
    @Schema(description = "是否为数据修改操作")
    public boolean isDataModificationOperation() {
        return operationType != null && (operationType == 3 || operationType == 4 || operationType == 5);
    }

    /**
     * 判断是否为文件操作
     */
    @Schema(description = "是否为文件操作")
    public boolean isFileOperation() {
        return operationType != null && (operationType == 9 || operationType == 10);
    }

    /**
     * 获取简化的操作描述
     */
    @Schema(description = "简化的操作描述")
    public String getSimpleDescription() {
        if (operationDescription != null && operationDescription.length() > 50) {
            return operationDescription.substring(0, 47) + "...";
        }
        return operationDescription;
    }

    /**
     * 获取客户端信息摘要
     */
    @Schema(description = "客户端信息摘要")
    public String getClientSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (browserType != null) {
            summary.append(browserType);
        }
        
        if (operatingSystem != null) {
            if (summary.length() > 0) {
                summary.append(" / ");
            }
            summary.append(operatingSystem);
        }
        
        if (clientIp != null) {
            if (summary.length() > 0) {
                summary.append(" (");
            }
            summary.append(clientIp);
            if (summary.toString().contains("(")) {
                summary.append(")");
            }
        }
        
        return summary.toString();
    }
}