package com.archive.management.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 借阅趋势数据传输对象
 *
 * @author System
 * @version 1.0
 * @since 2025-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "借阅趋势数据")
public class BorrowTrendDTO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "日期标签（用于显示）")
    private String dateLabel;

    @Schema(description = "借阅申请数量")
    private Long borrowApplications;

    @Schema(description = "借阅批准数量")
    private Long borrowApprovals;

    @Schema(description = "借阅拒绝数量")
    private Long borrowRejections;

    @Schema(description = "实际借出数量")
    private Long actualBorrows;

    @Schema(description = "归还数量")
    private Long returns;

    @Schema(description = "逾期数量")
    private Long overdue;

    @Schema(description = "批准率（百分比）")
    private Double approvalRate;

    @Schema(description = "归还率（百分比）")
    private Double returnRate;

    @Schema(description = "逾期率（百分比）")
    private Double overdueRate;
}