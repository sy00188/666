package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建部门请求DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "创建部门请求")
public class CreateDepartmentRequest {

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @Schema(description = "部门名称", example = "技术部")
    private String name;

    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    @Schema(description = "部门编码", example = "TECH")
    private String code;

    @Schema(description = "父部门ID", example = "1")
    private Long parentId;

    @Schema(description = "部门类型", example = "BUSINESS")
    private String type;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "负责人ID", example = "1")
    private Long managerId;

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

    @Schema(description = "备注", example = "新成立部门")
    private String remark;

    @NotNull(message = "创建人ID不能为空")
    @Schema(description = "创建人ID", example = "1")
    private Long createBy;
}