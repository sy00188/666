package com.archive.management.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证码响应DTO
 * 用于返回验证码生成结果
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Schema(description = "验证码响应")
public class CaptchaResponse {

    @Schema(description = "验证码ID", example = "captcha_123456789")
    @JsonProperty("captchaId")
    private String captchaId;

    @Schema(description = "验证码图片（base64格式）", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    @JsonProperty("captchaImage")
    private String captchaImage;

    @Schema(description = "验证码过期时间（秒）", example = "300")
    @JsonProperty("expiresIn")
    private Long expiresIn;

    public CaptchaResponse() {
    }

    public CaptchaResponse(String captchaId, String captchaImage, Long expiresIn) {
        this.captchaId = captchaId;
        this.captchaImage = captchaImage;
        this.expiresIn = expiresIn;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaImage() {
        return captchaImage;
    }

    public void setCaptchaImage(String captchaImage) {
        this.captchaImage = captchaImage;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "CaptchaResponse{" +
                "captchaId='" + captchaId + '\'' +
                ", captchaImage='" + (captchaImage != null ? captchaImage.substring(0, Math.min(50, captchaImage.length())) + "..." : "null") + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}