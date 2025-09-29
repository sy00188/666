package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门树形响应DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "部门树形响应")
public class DepartmentTreeResponse {

    @Schema(description = "部门ID", example = "1")
    private Long id;

    @Schema(description = "部门名称", example = "技术部")
    private String name;

    @Schema(description = "部门编码", example = "TECH")
    private String code;

    @Schema(description = "父部门ID", example = "1")
    private Long parentId;

    @Schema(description = "部门状态", example = "1")
    private Integer status;

    @Schema(description = "部门状态描述", example = "启用")
    private String statusName;

    @Schema(description = "部门级别", example = "2")
    private Integer level;

    @Schema(description = "部门类型", example = "BUSINESS")
    private String type;

    @Schema(description = "部门类型描述", example = "业务部门")
    private String typeName;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "负责人ID", example = "1")
    private Long managerId;

    @Schema(description = "负责人姓名", example = "张三")
    private String managerName;

    @Schema(description = "联系人", example = "张三")
    private String contact;

    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "tech@company.com")
    private String email;

    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;

    @Schema(description = "部门描述", example = "负责公司技术研发工作")
    private String description;

    @Schema(description = "部门路径", example = "/1/2/3")
    private String path;

    @Schema(description = "备注", example = "新成立部门")
    private String remark;

    @Schema(description = "用户数量", example = "10")
    private Long userCount;

    @Schema(description = "总用户数量（包含子部门）", example = "25")
    private Long totalUserCount;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "是否为根部门", example = "false")
    private Boolean isRoot;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;

    @Schema(description = "是否有子部门", example = "true")
    private Boolean hasChildren;

    @Schema(description = "显示名称", example = "技术部")
    private String displayName;

    @Schema(description = "子部门列表")
    private List<DepartmentTreeResponse> children;
}