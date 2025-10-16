package com.archive.management.controller;

import com.archive.management.entity.ExportTask;
import com.archive.management.service.ExportTaskService;
import com.archive.management.service.MultiFormatExportService;
import com.archive.management.service.StreamExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 导出控制器
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "导出管理", description = "数据导出相关接口")
public class ExportController {
    
    private final ExportTaskService exportTaskService;
    private final StreamExportService streamExportService;
    private final MultiFormatExportService multiFormatExportService;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建导出任务
     */
    @PostMapping("/tasks")
    @Operation(summary = "创建导出任务", description = "创建异步导出任务，支持大数据量导出")
    public ResponseEntity<Map<String, Object>> createExportTask(@RequestBody Map<String, Object> request) {
        try {
            String taskName = (String) request.get("taskName");
            String exportType = (String) request.get("exportType");
            String format = (String) request.get("format");
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            Long totalCount = request.containsKey("totalCount") ? 
                Long.valueOf(request.get("totalCount").toString()) : 0L;
            
            ExportTask task = exportTaskService.createTask(
                taskName, exportType, format, parameters, totalCount
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("taskId", task.getTaskId());
            response.put("message", "导出任务创建成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("创建导出任务失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "创建导出任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 查询导出任务列表
     */
    @GetMapping("/tasks")
    @Operation(summary = "查询导出任务列表", description = "分页查询当前用户的导出任务")
    public ResponseEntity<Map<String, Object>> getExportTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        try {
            // 从Spring Security上下文获取当前用户ID
            Long userId = getCurrentUserId();
            
            Page<ExportTask> tasks;
            if (status != null && !status.isEmpty()) {
                tasks = exportTaskService.getUserTasksByStatus(userId, status, page, size);
            } else {
                tasks = exportTaskService.getUserTasks(userId, page, size);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tasks.getContent());
            response.put("total", tasks.getTotalElements());
            response.put("page", tasks.getNumber());
            response.put("size", tasks.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("查询导出任务失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询导出任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 查询导出任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "查询导出任务详情", description = "根据任务ID查询任务详情和进度")
    public ResponseEntity<Map<String, Object>> getExportTask(@PathVariable String taskId) {
        try {
            ExportTask task = exportTaskService.getTaskById(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", task);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("查询导出任务失败: taskId={}", taskId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询导出任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 取消导出任务
     */
    @PostMapping("/tasks/{taskId}/cancel")
    @Operation(summary = "取消导出任务", description = "取消正在进行的导出任务")
    public ResponseEntity<Map<String, Object>> cancelExportTask(@PathVariable String taskId) {
        try {
            exportTaskService.cancelTask(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务已取消");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("取消导出任务失败: taskId={}", taskId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "取消任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 暂停导出任务
     */
    @PostMapping("/tasks/{taskId}/pause")
    @Operation(summary = "暂停导出任务", description = "暂停正在进行的大型导出任务")
    public ResponseEntity<Map<String, Object>> pauseExportTask(@PathVariable String taskId) {
        try {
            exportTaskService.pauseTask(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务已暂停");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("暂停导出任务失败: taskId={}", taskId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "暂停任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 恢复导出任务
     */
    @PostMapping("/tasks/{taskId}/resume")
    @Operation(summary = "恢复导出任务", description = "恢复已暂停的导出任务")
    public ResponseEntity<Map<String, Object>> resumeExportTask(@PathVariable String taskId) {
        try {
            exportTaskService.resumeTask(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务已恢复");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("恢复导出任务失败: taskId={}", taskId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "恢复任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 下载导出文件
     */
    @GetMapping("/tasks/{taskId}/download")
    @Operation(summary = "下载导出文件", description = "下载已完成的导出文件")
    public void downloadExportFile(@PathVariable String taskId, HttpServletResponse response) {
        try {
            ExportTask task = exportTaskService.getTaskById(taskId);
            
            if (!"completed".equals(task.getStatus())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "任务未完成");
                return;
            }
            
            // 从文件存储读取文件并返回
            String filePath = task.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件路径不存在");
                return;
            }
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return;
            }
            
            // 设置响应头
            String fileName = URLEncoder.encode(task.getFileName(), StandardCharsets.UTF_8);
            response.setContentType(getContentType(task.getFormat()));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename*=UTF-8''" + fileName);
            response.setContentLength((int) file.length());
            
            // 读取文件并写入响应
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                
                log.info("文件下载成功: taskId={}, fileName={}", taskId, task.getFileName());
            }
            
            // 示例：返回文件内容
            // try (OutputStream out = response.getOutputStream()) {
            //     Files.copy(Paths.get(task.getFilePath()), out);
            // }
            
            log.info("导出文件下载: taskId={}, fileName={}", taskId, task.getFileName());
            
        } catch (Exception e) {
            log.error("下载导出文件失败: taskId={}", taskId, e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "下载失败: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("发送错误响应失败", ioException);
            }
        }
    }
    
    /**
     * 删除导出任务
     */
    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "删除导出任务", description = "删除已完成或失败的导出任务")
    public ResponseEntity<Map<String, Object>> deleteExportTask(@PathVariable String taskId) {
        try {
            exportTaskService.deleteTask(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "任务已删除");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除导出任务失败: taskId={}", taskId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "删除任务失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 快速导出（小数据量）
     */
    @PostMapping("/quick")
    @Operation(summary = "快速导出", description = "同步导出小数据量（< 1000条），立即返回文件")
    public void quickExport(@RequestBody Map<String, Object> request, HttpServletResponse response) {
        try {
            List<Map<String, Object>> data = (List<Map<String, Object>>) request.get("data");
            List<String> headers = (List<String>) request.get("headers");
            List<String> fieldNames = (List<String>) request.get("fieldNames");
            String format = (String) request.getOrDefault("format", "excel");
            String fileName = (String) request.getOrDefault("fileName", "export");
            
            // 限制数据量
            if (data.size() > 1000) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "数据量过大，请使用异步导出");
                return;
            }
            
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setContentType(getContentType(format));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename*=UTF-8''" + encodedFileName + getFileExtension(format));
            
            // 导出数据
            try (OutputStream out = response.getOutputStream()) {
                if ("pdf".equals(format)) {
                    multiFormatExportService.exportToMultipleFormats(
                        data, headers, fieldNames, fileName, List.of("pdf"), out
                    );
                } else if ("csv".equals(format)) {
                    multiFormatExportService.exportToMultipleFormats(
                        data, headers, fieldNames, fileName, List.of("csv"), out
                    );
                } else {
                    // 默认Excel
                    multiFormatExportService.exportToMultipleFormats(
                        data, headers, fieldNames, fileName, List.of("excel"), out
                    );
                }
            }
            
            log.info("快速导出完成: format={}, dataSize={}", format, data.size());
            
        } catch (Exception e) {
            log.error("快速导出失败", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "导出失败: " + e.getMessage());
            } catch (IOException ioException) {
                log.error("发送错误响应失败", ioException);
            }
        }
    }
    
    /**
     * 获取内容类型
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "excel":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv":
                return "text/csv";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String format) {
        switch (format.toLowerCase()) {
            case "excel":
                return ".xlsx";
            case "csv":
                return ".csv";
            case "pdf":
                return ".pdf";
            default:
                return ".dat";
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("用户未认证");
            }
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                // 假设用户名就是用户ID的字符串形式
                return Long.parseLong(userDetails.getUsername());
            } else if (principal instanceof String) {
                return Long.parseLong((String) principal);
            } else {
                throw new IllegalStateException("无法获取用户信息");
            }
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            throw new IllegalStateException("获取用户信息失败: " + e.getMessage(), e);
        }
    }
}

