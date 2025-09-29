package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新用户请求DTO
 * 用于用户更新接口的请求参数
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @Schema(description = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正数")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "昵称", example = "管理员")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    @Min(value = 0, message = "性别值必须为0、1或2")
    @Max(value = 2, message = "性别值必须为0、1或2")
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01T00:00:00")
    private LocalDateTime birthday;

    @Schema(description = "部门ID", example = "1")
    @Positive(message = "部门ID必须为正数")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    @Size(max = 50, message = "工号长度不能超过50个字符")
    private String employeeNumber;

    @Schema(description = "入职日期", example = "2024-01-01T00:00:00")
    private LocalDateTime hireDate;

    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1", "2"})
    @Min(value = 0, message = "用户状态值必须为0、1或2")
    @Max(value = 2, message = "用户状态值必须为0、1或2")
    private Integer status;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "角色ID列表", example = "[1, 2]")
    private List<Long> roleIds;

    @Schema(description = "权限ID列表", example = "[1, 2, 3]")
    private List<Long> permissionIds;

    @Schema(description = "用户标签列表", example = "[\"VIP\", \"重要客户\"]")
    private List<String> tags;

    @Schema(description = "备注", example = "系统管理员账户")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "扩展属性")
    private Object extendedProperties;

    @Schema(description = "版本号", example = "1")
    @NotNull(message = "版本号不能为空")
    @Min(value = 0, message = "版本号不能为负数")
    private Long version;
}