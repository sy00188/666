package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 导出登录日志请求DTO
 * 用于导出登录日志时的查询条件和导出配置
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导出登录日志请求")
public class ExportLoginLogRequest {

    @Schema(description = "用户ID列表", example = "[1001, 1002]")
    private List<Long> userIds;

    @Schema(description = "用户名列表", example = "[\"admin\", \"user1\"]")
    private List<String> usernames;

    @Schema(description = "登录状态列表", example = "[1, 0]")
    private List<Integer> statuses;

    @Schema(description = "客户端IP地址列表", example = "[\"192.168.1.100\", \"192.168.1.101\"]")
    private List<String> clientIps;

    @Schema(description = "登录类型列表", example = "[\"WEB\", \"MOBILE\"]")
    private List<String> loginTypes;

    @Schema(description = "登录方式列表", example = "[\"PASSWORD\", \"SMS\"]")
    private List<String> loginMethods;

    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-31 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "关键词搜索", example = "admin")
    @Size(max = 100, message = "关键词长度不能超过100个字符")
    private String keyword;

    @Schema(description = "导出格式", example = "EXCEL", allowableValues = {"EXCEL", "CSV", "PDF"})
    private String exportFormat = "EXCEL";

    @Schema(description = "导出字段列表", example = "[\"username\", \"clientIp\", \"loginTime\", \"status\"]")
    private List<String> exportFields;

    @Schema(description = "是否包含敏感信息", example = "false")
    private Boolean includeSensitiveInfo = false;

    @Schema(description = "最大导出记录数", example = "10000")
    private Integer maxRecords = 10000;

    @Schema(description = "排序字段", example = "loginTime")
    private String sortBy = "loginTime";

    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortOrder = "DESC";

    @Schema(description = "文件名前缀", example = "login_logs")
    @Size(max = 50, message = "文件名前缀长度不能超过50个字符")
    private String fileNamePrefix = "login_logs";

    @Schema(description = "是否压缩文件", example = "false")
    private Boolean compressed = false;

    @Schema(description = "导出备注", example = "月度登录日志导出")
    @Size(max = 200, message = "导出备注长度不能超过200个字符")
    private String exportRemark;
}