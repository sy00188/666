package com.archive.management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * 重置密码请求DTO
 * 用于管理员重置用户密码接口的请求参数
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Schema(description = "用户ID", example = "1", required = true)
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正数")
    private Long userId;

    @Schema(description = "新密码", example = "newPassword123", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$", 
             message = "新密码必须包含至少一个大写字母、一个小写字母和一个数字")
    private String newPassword;

    @Schema(description = "确认新密码", example = "newPassword123", required = true)
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;

    @Schema(description = "是否强制用户下次登录时修改密码", example = "true")
    @Builder.Default
    private Boolean forceChangeOnNextLogin = true;

    @Schema(description = "重置原因", example = "用户忘记密码")
    @Size(max = 500, message = "重置原因长度不能超过500个字符")
    private String reason;

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
}