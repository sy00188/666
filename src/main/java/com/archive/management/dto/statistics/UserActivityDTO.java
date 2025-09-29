package com.archive.management.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户活跃度数据传输对象
 *
 * @author System
 * @version 1.0
 * @since 2025-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户活跃度数据")
public class UserActivityDTO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "日期标签（用于显示）")
    private String dateLabel;

    @Schema(description = "活跃用户数")
    private Long activeUsers;

    @Schema(description = "新注册用户数")
    private Long newUsers;

    @Schema(description = "登录次数")
    private Long loginCount;

    @Schema(description = "操作次数")
    private Long operationCount;

    @Schema(description = "档案访问次数")
    private Long archiveAccessCount;

    @Schema(description = "文件下载次数")
    private Long fileDownloadCount;

    @Schema(description = "平均在线时长（分钟）")
    private Double avgOnlineMinutes;

    @Schema(description = "活跃度评分")
    private Double activityScore;
}