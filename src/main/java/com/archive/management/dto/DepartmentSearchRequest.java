package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门搜索请求DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "部门搜索请求")
public class DepartmentSearchRequest {

    @Schema(description = "部门名称（模糊查询）", example = "技术")
    private String name;

    @Schema(description = "部门编码（模糊查询）", example = "TECH")
    private String code;

    @Schema(description = "父部门ID", example = "1")
    private Long parentId;

    @Schema(description = "部门状态", example = "1")
    private Integer status;

    @Schema(description = "部门级别", example = "2")
    private Integer level;

    @Schema(description = "部门类型", example = "BUSINESS")
    private String type;

    @Schema(description = "负责人ID", example = "1")
    private Long managerId;

    @Schema(description = "联系人（模糊查询）", example = "张")
    private String contact;

    @Schema(description = "联系电话", example = "138")
    private String phone;

    @Schema(description = "邮箱", example = "tech")
    private String email;

    @Schema(description = "关键词（名称或描述）", example = "技术")
    private String keyword;

    @Schema(description = "创建开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建结束时间", example = "2024-12-31T23:59:59")
    private LocalDateTime createTimeEnd;

    @Schema(description = "创建人ID", example = "1")
    private Long createBy;

    @Schema(description = "是否包含已删除", example = "false")
    private Boolean includeDeleted = false;

    @Schema(description = "排序字段", example = "createTime")
    private String sortBy;

    @Schema(description = "排序方向", example = "desc")
    private String sortDirection;
}