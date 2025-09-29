package com.archive.management.dto.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 档案统计数据传输对象
 *
 * @author System
 * @version 1.0
 * @since 2025-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "档案统计数据")
public class ArchiveStatisticsDTO {

    @Schema(description = "总档案数量")
    private Long totalCount;

    @Schema(description = "已归档数量")
    private Long archivedCount;

    @Schema(description = "草稿数量")
    private Long draftCount;

    @Schema(description = "待审核数量")
    private Long pendingCount;

    @Schema(description = "借出中数量")
    private Long borrowedCount;

    @Schema(description = "分组统计数据")
    private List<GroupStatistics> groups;

    @Schema(description = "按状态统计")
    private Map<String, Long> statusStatistics;

    @Schema(description = "按分类统计")
    private Map<String, Long> categoryStatistics;

    @Schema(description = "按密级统计")
    private Map<String, Long> securityLevelStatistics;

    @Schema(description = "本月新增数量")
    private Long monthlyNewCount;

    @Schema(description = "本年新增数量")
    private Long yearlyNewCount;

    /**
     * 分组统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分组统计数据")
    public static class GroupStatistics {
        
        @Schema(description = "分组名称")
        private String name;
        
        @Schema(description = "分组代码")
        private String code;
        
        @Schema(description = "数量")
        private Long count;
        
        @Schema(description = "百分比")
        private Double percentage;
    }
}