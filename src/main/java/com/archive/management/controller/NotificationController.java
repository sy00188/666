package com.archive.management.controller;

import com.archive.management.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器
 * 提供通知相关的REST API接口
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
@Tag(name = "通知管理", description = "通知管理相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送通知给指定用户")
    @PreAuthorize("hasAuthority('notification:send')")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "通知标题") @RequestParam @NotBlank String title,
            @Parameter(description = "通知内容") @RequestParam @NotBlank String content,
            @Parameter(description = "通知类型") @RequestParam @NotBlank String type) {
        try {
            boolean result = notificationService.sendNotification(userId, title, content, type);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "通知发送成功" : "通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量发送通知
     */
    @PostMapping("/send/batch")
    @Operation(summary = "批量发送通知", description = "批量发送通知给多个用户")
    @PreAuthorize("hasAuthority('notification:send')")
    public ResponseEntity<Map<String, Object>> sendBatchNotification(
            @Parameter(description = "用户ID列表") @RequestBody @NotNull List<Long> userIds,
            @Parameter(description = "通知标题") @RequestParam @NotBlank String title,
            @Parameter(description = "通知内容") @RequestParam @NotBlank String content,
            @Parameter(description = "通知类型") @RequestParam @NotBlank String type) {
        try {
            int result = notificationService.sendBatchNotification(userIds, title, content, type);
            return ResponseEntity.ok(Map.of(
                "success", result > 0,
                "message", "批量发送通知完成",
                "data", Map.of("successCount", result)
            ));
        } catch (Exception e) {
            log.error("批量发送通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量发送通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送广播通知
     */
    @PostMapping("/send/broadcast")
    @Operation(summary = "发送广播通知", description = "发送广播通知给所有用户")
    @PreAuthorize("hasAuthority('notification:broadcast')")
    public ResponseEntity<Map<String, Object>> sendBroadcastNotification(
            @Parameter(description = "通知标题") @RequestParam @NotBlank String title,
            @Parameter(description = "通知内容") @RequestParam @NotBlank String content,
            @Parameter(description = "通知类型") @RequestParam @NotBlank String type,
            @Parameter(description = "目标类型") @RequestParam @NotBlank String targetType) {
        try {
            boolean result = notificationService.sendBroadcastNotification(title, content, type, targetType);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "广播通知发送成功" : "广播通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送广播通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送广播通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户通知列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户通知列表", description = "分页获取用户的通知列表")
    @PreAuthorize("hasAuthority('notification:read') or #userId == authentication.principal.userId")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> result = notificationService.getUserNotifications(userId, page, size);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取通知列表成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取用户通知列表失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取通知列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读状态")
    @PreAuthorize("hasAuthority('notification:read')")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @Parameter(description = "通知ID") @PathVariable @NotNull @Positive Long notificationId,
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId) {
        try {
            boolean result = notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "标记已读成功" : "标记已读失败"
            ));
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "标记已读失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量标记通知为已读
     */
    @PutMapping("/read/batch")
    @Operation(summary = "批量标记通知为已读", description = "批量标记多个通知为已读状态")
    @PreAuthorize("hasAuthority('notification:read')")
    public ResponseEntity<Map<String, Object>> markBatchAsRead(
            @Parameter(description = "通知ID列表") @RequestBody @NotNull List<Long> notificationIds,
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId) {
        try {
            int result = notificationService.markBatchAsRead(notificationIds, userId);
            return ResponseEntity.ok(Map.of(
                "success", result > 0,
                "message", "批量标记已读完成",
                "data", Map.of("successCount", result)
            ));
        } catch (Exception e) {
            log.error("批量标记通知为已读失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量标记已读失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定的通知")
    @PreAuthorize("hasAuthority('notification:delete')")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable @NotNull @Positive Long notificationId,
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId) {
        try {
            boolean result = notificationService.deleteNotification(notificationId, userId);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "删除通知成功" : "删除通知失败"
            ));
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count/{userId}")
    @Operation(summary = "获取未读通知数量", description = "获取用户的未读通知数量")
    @PreAuthorize("hasAuthority('notification:read') or #userId == authentication.principal.userId")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @Parameter(description = "用户ID") @PathVariable @NotNull @Positive Long userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取未读数量成功",
                "data", Map.of("unreadCount", count)
            ));
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取未读数量失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送邮件通知
     */
    @PostMapping("/send/email")
    @Operation(summary = "发送邮件通知", description = "发送邮件通知给指定用户")
    @PreAuthorize("hasAuthority('notification:email')")
    public ResponseEntity<Map<String, Object>> sendEmailNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "邮件标题") @RequestParam @NotBlank String title,
            @Parameter(description = "邮件内容") @RequestParam @NotBlank String content,
            @Parameter(description = "邮件模板") @RequestParam(required = false) String template) {
        try {
            boolean result = notificationService.sendEmailNotification(userId, title, content, template);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "邮件通知发送成功" : "邮件通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送邮件通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送邮件通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送短信通知
     */
    @PostMapping("/send/sms")
    @Operation(summary = "发送短信通知", description = "发送短信通知给指定用户")
    @PreAuthorize("hasAuthority('notification:sms')")
    public ResponseEntity<Map<String, Object>> sendSmsNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "短信内容") @RequestParam @NotBlank String content,
            @Parameter(description = "短信模板") @RequestParam(required = false) String template) {
        try {
            boolean result = notificationService.sendSmsNotification(userId, content, template);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "短信通知发送成功" : "短信通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送短信通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送短信通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 创建通知模板
     */
    @PostMapping("/templates")
    @Operation(summary = "创建通知模板", description = "创建新的通知模板")
    @PreAuthorize("hasAuthority('notification:template:create')")
    public ResponseEntity<Map<String, Object>> createNotificationTemplate(
            @Parameter(description = "模板名称") @RequestParam @NotBlank String templateName,
            @Parameter(description = "模板内容") @RequestParam @NotBlank String templateContent,
            @Parameter(description = "模板类型") @RequestParam @NotBlank String templateType,
            @Parameter(description = "创建人ID") @RequestParam @NotNull @Positive Long createdBy) {
        try {
            boolean result = notificationService.createNotificationTemplate(templateName, templateContent, templateType, createdBy);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "模板创建成功" : "模板创建失败"
            ));
        } catch (Exception e) {
            log.error("创建通知模板失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建模板失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取通知模板
     */
    @GetMapping("/templates/{templateName}")
    @Operation(summary = "获取通知模板", description = "根据模板名称获取模板信息")
    @PreAuthorize("hasAuthority('notification:template:read')")
    public ResponseEntity<Map<String, Object>> getNotificationTemplate(
            @Parameter(description = "模板名称") @PathVariable @NotBlank String templateName) {
        try {
            Map<String, Object> result = notificationService.getNotificationTemplate(templateName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取模板成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取通知模板失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取模板失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取通知统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取通知统计信息", description = "获取指定时间范围内的通知统计信息")
    @PreAuthorize("hasAuthority('notification:statistics')")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        try {
            Map<String, Object> result = notificationService.getNotificationStatistics(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取统计信息成功",
                "data", result
            ));
        } catch (Exception e) {
            log.error("获取通知统计信息失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 清理过期通知
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期通知", description = "清理指定天数之前的过期通知")
    @PreAuthorize("hasAuthority('notification:cleanup')")
    public ResponseEntity<Map<String, Object>> cleanExpiredNotifications(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") int days) {
        try {
            int result = notificationService.cleanExpiredNotifications(days);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "清理过期通知完成",
                "data", Map.of("cleanedCount", result)
            ));
        } catch (Exception e) {
            log.error("清理过期通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "清理过期通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送系统告警通知
     */
    @PostMapping("/send/system-alert")
    @Operation(summary = "发送系统告警通知", description = "发送系统告警通知")
    @PreAuthorize("hasAuthority('notification:system-alert')")
    public ResponseEntity<Map<String, Object>> sendSystemAlert(
            @Parameter(description = "告警类型") @RequestParam @NotBlank String alertType,
            @Parameter(description = "告警内容") @RequestParam @NotBlank String alertContent,
            @Parameter(description = "严重程度") @RequestParam @NotBlank String severity) {
        try {
            boolean result = notificationService.sendSystemAlert(alertType, alertContent, severity);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "系统告警发送成功" : "系统告警发送失败"
            ));
        } catch (Exception e) {
            log.error("发送系统告警通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送系统告警失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送用户操作通知
     */
    @PostMapping("/send/user-operation")
    @Operation(summary = "发送用户操作通知", description = "发送用户操作相关通知")
    @PreAuthorize("hasAuthority('notification:user-operation')")
    public ResponseEntity<Map<String, Object>> sendUserOperationNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "操作类型") @RequestParam @NotBlank String operationType,
            @Parameter(description = "操作内容") @RequestParam @NotBlank String operationContent) {
        try {
            boolean result = notificationService.sendUserOperationNotification(userId, operationType, operationContent);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "用户操作通知发送成功" : "用户操作通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送用户操作通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送用户操作通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送档案相关通知
     */
    @PostMapping("/send/archive")
    @Operation(summary = "发送档案相关通知", description = "发送档案操作相关通知")
    @PreAuthorize("hasAuthority('notification:archive')")
    public ResponseEntity<Map<String, Object>> sendArchiveNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "档案ID") @RequestParam @NotNull @Positive Long archiveId,
            @Parameter(description = "操作类型") @RequestParam @NotBlank String operationType,
            @Parameter(description = "操作内容") @RequestParam @NotBlank String operationContent) {
        try {
            boolean result = notificationService.sendArchiveNotification(userId, archiveId, operationType, operationContent);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "档案通知发送成功" : "档案通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送档案相关通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送档案通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送借阅相关通知
     */
    @PostMapping("/send/borrow")
    @Operation(summary = "发送借阅相关通知", description = "发送借阅操作相关通知")
    @PreAuthorize("hasAuthority('notification:borrow')")
    public ResponseEntity<Map<String, Object>> sendBorrowNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "借阅ID") @RequestParam @NotNull @Positive Long borrowId,
            @Parameter(description = "通知类型") @RequestParam @NotBlank String notificationType,
            @Parameter(description = "通知内容") @RequestParam @NotBlank String content) {
        try {
            boolean result = notificationService.sendBorrowNotification(userId, borrowId, notificationType, content);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "借阅通知发送成功" : "借阅通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送借阅相关通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送借阅通知失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 发送审批相关通知
     */
    @PostMapping("/send/approval")
    @Operation(summary = "发送审批相关通知", description = "发送审批操作相关通知")
    @PreAuthorize("hasAuthority('notification:approval')")
    public ResponseEntity<Map<String, Object>> sendApprovalNotification(
            @Parameter(description = "用户ID") @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "审批ID") @RequestParam @NotNull @Positive Long approvalId,
            @Parameter(description = "审批类型") @RequestParam @NotBlank String approvalType,
            @Parameter(description = "审批状态") @RequestParam @NotBlank String approvalStatus) {
        try {
            boolean result = notificationService.sendApprovalNotification(userId, approvalId, approvalType, approvalStatus);
            return ResponseEntity.ok(Map.of(
                "success", result,
                "message", result ? "审批通知发送成功" : "审批通知发送失败"
            ));
        } catch (Exception e) {
            log.error("发送审批相关通知失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发送审批通知失败: " + e.getMessage()
            ));
        }
    }
}