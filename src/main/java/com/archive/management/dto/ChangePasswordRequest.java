package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * 修改密码请求DTO
 * 用于用户修改密码接口的请求参数
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "旧密码", example = "oldPassword123", required = true)
    @NotBlank(message = "旧密码不能为空")
    @Size(min = 6, max = 100, message = "旧密码长度必须在6-100个字符之间")
    private String oldPassword;

    @Schema(description = "新密码", example = "newPassword123", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$", 
             message = "新密码必须包含至少一个大写字母、一个小写字母和一个数字")
    private String newPassword;

    @Schema(description = "确认新密码", example = "newPassword123", required = true)
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;

    /**
     * 验证新密码和确认新密码是否一致
     */
    @AssertTrue(message = "新密码和确认新密码不一致")
    public boolean isNewPasswordMatching() {
        if (newPassword == null || confirmNewPassword == null) {
            return false;
        }
        return newPassword.equals(confirmNewPassword);
    }

    /**
     * 验证新密码和旧密码是否不同
     */
    @AssertTrue(message = "新密码不能与旧密码相同")
    public boolean isPasswordDifferent() {
        if (oldPassword == null || newPassword == null) {
            return true; // 让其他验证处理空值情况
        }
        return !oldPassword.equals(newPassword);
    }
}