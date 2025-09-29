package com.archive.management.controller;

import com.archive.management.common.ApiResponse;
import com.archive.management.dto.response.CaptchaResponse;
import com.archive.management.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码控制器
 * 提供验证码生成、验证和管理功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "验证码管理", description = "验证码生成、验证和管理相关接口")
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码
     * 
     * @return 验证码响应信息
     */
    @GetMapping("/captcha")
    @Operation(summary = "生成验证码", description = "生成图形验证码，返回验证码ID和base64图片")
    public ResponseEntity<ApiResponse<CaptchaResponse>> generateCaptcha() {
        log.info("生成验证码请求");
        
        try {
            CaptchaResponse captchaResponse = captchaService.generateCaptcha();
            log.info("验证码生成成功，ID: {}", captchaResponse.getCaptchaId());
            
            return ResponseEntity.ok(ApiResponse.success(captchaResponse, "验证码生成成功"));
        } catch (Exception e) {
            log.error("验证码生成失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("验证码生成失败"));
        }
    }

    /**
     * 验证验证码
     * 
     * @param captchaId 验证码ID
     * @param captcha 验证码内容
     * @return 验证结果
     */
    @PostMapping("/captcha/validate")
    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    public ResponseEntity<ApiResponse<Boolean>> validateCaptcha(
            @Parameter(description = "验证码ID", required = true)
            @RequestParam String captchaId,
            @Parameter(description = "验证码内容", required = true)
            @RequestParam String captcha) {
        
        log.info("验证验证码请求，ID: {}", captchaId);
        
        try {
            boolean isValid = captchaService.validateCaptcha(captchaId, captcha);
            
            if (isValid) {
                log.info("验证码验证成功，ID: {}", captchaId);
                return ResponseEntity.ok(ApiResponse.success(true, "验证码验证成功"));
            } else {
                log.warn("验证码验证失败，ID: {}", captchaId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("验证码验证失败"));
            }
        } catch (Exception e) {
            log.error("验证码验证异常，ID: {}", captchaId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("验证码验证异常"));
        }
    }

    /**
     * 检查验证码是否存在
     * 
     * @param captchaId 验证码ID
     * @return 是否存在
     */
    @GetMapping("/captcha/exists")
    @Operation(summary = "检查验证码是否存在", description = "检查指定ID的验证码是否存在且未过期")
    public ResponseEntity<ApiResponse<Boolean>> checkCaptchaExists(
            @Parameter(description = "验证码ID", required = true)
            @RequestParam String captchaId) {
        
        log.info("检查验证码是否存在，ID: {}", captchaId);
        
        try {
            boolean exists = captchaService.existsCaptcha(captchaId);
            
            return ResponseEntity.ok(ApiResponse.success(exists, 
                    exists ? "验证码存在" : "验证码不存在或已过期"));
        } catch (Exception e) {
            log.error("检查验证码存在性异常，ID: {}", captchaId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("检查验证码存在性异常"));
        }
    }
}