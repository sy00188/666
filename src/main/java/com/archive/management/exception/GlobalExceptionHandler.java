package com.archive.management.exception;

import com.archive.management.common.Result;
import com.archive.management.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准化的错误响应
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.BUSINESS_ERROR.getCode(), e.getMessage()));
    }

    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Result<Void>> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.warn("用户不存在: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(ResultCode.USER_NOT_FOUND.getCode(), e.getMessage()));
    }

    /**
     * 处理档案不存在异常
     */
    @ExceptionHandler(ArchiveNotFoundException.class)
    public ResponseEntity<Result<Void>> handleArchiveNotFoundException(ArchiveNotFoundException e, HttpServletRequest request) {
        log.warn("档案不存在: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(ResultCode.ARCHIVE_NOT_FOUND.getCode(), e.getMessage()));
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证失败: {} - {}", request.getRequestURI(), e.getMessage());
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_VALID_ERROR.getCode(), "参数验证失败: " + errorMessage));
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败: {} - {}", request.getRequestURI(), e.getMessage());
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_VALID_ERROR.getCode(), "参数绑定失败: " + errorMessage));
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束违反: {} - {}", request.getRequestURI(), e.getMessage());
        
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_VALID_ERROR.getCode(), "约束违反: " + errorMessage));
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败: {} - {}", request.getRequestURI(), e.getMessage());
        
        String message = "认证失败";
        if (e instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        } else if (e instanceof DisabledException) {
            message = "账户已被禁用";
        } else if (e instanceof LockedException) {
            message = "账户已被锁定";
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(ResultCode.UNAUTHORIZED.getCode(), message));
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问被拒绝: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(ResultCode.FORBIDDEN.getCode(), "访问被拒绝，权限不足"));
    }

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        log.error("数据库访问异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.DATABASE_ERROR.getCode(), "数据库操作失败"));
    }

    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Result<Void>> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("SQL异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.DATABASE_ERROR.getCode(), "数据库操作失败"));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.FILE_SIZE_EXCEEDED.getCode(), "文件大小超出限制"));
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("HTTP消息不可读: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_ERROR.getCode(), "请求参数格式错误"));
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.error(ResultCode.METHOD_NOT_ALLOWED.getCode(), "请求方法不支持"));
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_ERROR.getCode(), "缺少必要参数: " + e.getParameterName()));
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_ERROR.getCode(), "参数类型错误: " + e.getName()));
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求路径不存在: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(ResultCode.NOT_FOUND.getCode(), "请求路径不存在"));
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误"));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数: {} - {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest()
                .body(Result.error(ResultCode.PARAM_ERROR.getCode(), "参数错误: " + e.getMessage()));
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统运行异常"));
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误"));
    }
}