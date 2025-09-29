package com.archive.management.exception;

import com.archive.management.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准化的错误响应
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理基础异常（统一异常处理）
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException e, HttpServletRequest request) {
        logger.warn("业务异常: {} - 请求路径: {} - 响应码: {}", e.getMessage(), request.getRequestURI(), e.getResponseCode().getCode());
        
        // 使用正确的ApiResponse.error方法
        ApiResponse<Object> response = ApiResponse.error(e.getResponseCode().getCode(), e.getMessage());
        
        // 根据响应码确定HTTP状态码
        HttpStatus httpStatus = determineHttpStatus(e.getResponseCode().getCode());
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * 根据响应码确定HTTP状态码
     */
    private HttpStatus determineHttpStatus(int code) {
        if (code >= 1000 && code < 2000) {
            return HttpStatus.BAD_REQUEST; // 参数错误
        } else if (code >= 2000 && code < 3000) {
            return HttpStatus.UNAUTHORIZED; // 认证错误
        } else if (code >= 3000 && code < 4000) {
            return HttpStatus.FORBIDDEN; // 权限错误
        } else if (code >= 4000 && code < 5000) {
            return HttpStatus.NOT_FOUND; // 资源错误
        } else if (code >= 5000 && code < 6000) {
            return HttpStatus.CONFLICT; // 业务错误
        } else if (code >= 6000 && code < 7000) {
            return HttpStatus.INTERNAL_SERVER_ERROR; // 系统错误
        } else if (code >= 7000 && code < 8000) {
            return HttpStatus.SERVICE_UNAVAILABLE; // 数据库错误
        } else if (code >= 8000 && code < 9000) {
            return HttpStatus.BAD_GATEWAY; // 外部服务错误
        } else if (code >= 9000 && code < 10000) {
            return HttpStatus.INSUFFICIENT_STORAGE; // 文件操作错误
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR; // 默认
        }
    }

    /**
     * 处理业务异常（保持向后兼容）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        // 如果BusinessException已经继承BaseException，则委托给BaseException处理器
        if (e instanceof BaseException) {
            return handleBaseException((BaseException) e, request);
        }
        
        // 兼容旧版本BusinessException
        ApiResponse<Object> response = ApiResponse.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.warn("认证异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        String message = "认证失败";
        if (e instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        }
        
        ApiResponse<Object> response = ApiResponse.error(401, message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("权限不足异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(403, "权限不足，无法访问该资源");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 处理参数验证异常（@Valid注解）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.warn("参数验证异常 - 请求路径: {}", request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> response = ApiResponse.error(400, "参数验证失败");
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e, HttpServletRequest request) {
        logger.warn("绑定异常 - 请求路径: {}", request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> response = ApiResponse.error(400, "参数绑定失败");
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理约束违反异常（@Validated注解）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        logger.warn("约束违反异常 - 请求路径: {}", request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        
        ApiResponse<Object> response = ApiResponse.error(400, "参数约束违反");
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        logger.warn("参数类型不匹配异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        String message = String.format("参数 '%s' 的值 '%s' 类型不正确，期望类型为 %s", 
                e.getName(), e.getValue(), e.getRequiredType().getSimpleName());
        
        ApiResponse<Object> response = ApiResponse.error(400, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        logger.warn("资源未找到异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(404, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理数据完整性违反异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        logger.error("数据完整性违反异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        String message = "数据操作失败，可能存在重复数据或违反约束条件";
        if (e.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，不能重复添加";
        } else if (e.getMessage().contains("foreign key constraint")) {
            message = "数据关联约束违反，无法执行操作";
        }
        
        ApiResponse<Object> response = ApiResponse.error(409, message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(400, "参数错误: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        logger.warn("非法状态异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(400, "操作状态错误: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error(500, "系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
        logger.error("未知异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error(500, "系统发生未知错误，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException e, HttpServletRequest request) {
        logger.warn("文件上传异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(400, "文件上传失败: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理文件下载异常
     */
    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileDownloadException(FileDownloadException e, HttpServletRequest request) {
        logger.warn("文件下载异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI());
        
        ApiResponse<Object> response = ApiResponse.error(400, "文件下载失败: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleDatabaseException(DatabaseException e, HttpServletRequest request) {
        logger.error("数据库异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error(503, "数据库操作异常，请稍后重试");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 处理外部服务异常
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleExternalServiceException(ExternalServiceException e, HttpServletRequest request) {
        logger.error("外部服务异常: {} - 请求路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        ApiResponse<Object> response = ApiResponse.error(502, "外部服务异常: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
}