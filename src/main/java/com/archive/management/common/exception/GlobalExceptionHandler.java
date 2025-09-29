package com.archive.management.common.exception;

import com.archive.management.common.enums.ResponseCode;
import com.archive.management.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage(), e.getData())
                .traceId(getTraceId(request));
    }

    /**
     * 参数验证异常处理 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证异常: {}", e.getMessage());
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage)
                .traceId(getTraceId(request));
    }

    /**
     * 参数绑定异常处理 - @ModelAttribute
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定异常: {}", e.getMessage());
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage)
                .traceId(getTraceId(request));
    }

    /**
     * 约束违反异常处理 - @Validated
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束违反异常: {}", e.getMessage());
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage)
                .traceId(getTraceId(request));
    }

    /**
     * 缺少请求参数异常处理
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数异常: {}", e.getMessage());
        String errorMessage = String.format("缺少必需的请求参数: %s", e.getParameterName());
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage)
                .traceId(getTraceId(request));
    }

    /**
     * 参数类型不匹配异常处理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配异常: {}", e.getMessage());
        String errorMessage = String.format("参数 %s 类型不匹配", e.getName());
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), errorMessage)
                .traceId(getTraceId(request));
    }

    /**
     * HTTP消息不可读异常处理
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("HTTP消息不可读异常: {}", e.getMessage());
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), "请求体格式错误")
                .traceId(getTraceId(request));
    }

    /**
     * 请求方法不支持异常处理
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持异常: {}", e.getMessage());
        return Result.error(ResponseCode.METHOD_NOT_ALLOWED.getCode(), "请求方法不支持")
                .traceId(getTraceId(request));
    }

    /**
     * 媒体类型不支持异常处理
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Object> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("媒体类型不支持异常: {}", e.getMessage());
        return Result.error(415, "媒体类型不支持")
                .traceId(getTraceId(request));
    }

    /**
     * 文件上传大小超限异常处理
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限异常: {}", e.getMessage());
        return Result.error(ResponseCode.PAYLOAD_TOO_LARGE.getCode(), "文件大小超出限制")
                .traceId(getTraceId(request));
    }

    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Object> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404异常: {}", e.getMessage());
        return Result.error(ResponseCode.NOT_FOUND.getCode(), "请求的资源不存在")
                .traceId(getTraceId(request));
    }

    /**
     * 认证异常处理
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证异常: {}", e.getMessage());
        if (e instanceof BadCredentialsException) {
            return Result.error(ResponseCode.INVALID_CREDENTIALS)
                    .traceId(getTraceId(request));
        }
        return Result.error(ResponseCode.UNAUTHORIZED)
                .traceId(getTraceId(request));
    }

    /**
     * 授权异常处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("授权异常: {}", e.getMessage());
        return Result.error(ResponseCode.FORBIDDEN)
                .traceId(getTraceId(request));
    }

    /**
     * 数据完整性违反异常处理
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性违反异常: {}", e.getMessage(), e);
        return Result.error(ResponseCode.CONFLICT.getCode(), "数据操作冲突，请检查数据完整性")
                .traceId(getTraceId(request));
    }

    /**
     * SQL异常处理
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("SQL异常: {}", e.getMessage(), e);
        return Result.error(ResponseCode.SYSTEM_ERROR.getCode(), "数据库操作异常")
                .traceId(getTraceId(request));
    }

    /**
     * 空指针异常处理
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {}", e.getMessage(), e);
        return Result.error(ResponseCode.SYSTEM_ERROR.getCode(), "系统内部错误")
                .traceId(getTraceId(request));
    }

    /**
     * 非法参数异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常: {}", e.getMessage());
        return Result.error(ResponseCode.PARAM_ERROR.getCode(), e.getMessage())
                .traceId(getTraceId(request));
    }

    /**
     * 非法状态异常处理
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Object> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.warn("非法状态异常: {}", e.getMessage());
        return Result.error(ResponseCode.CONFLICT.getCode(), e.getMessage())
                .traceId(getTraceId(request));
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(ResponseCode.SYSTEM_ERROR)
                .traceId(getTraceId(request));
    }

    /**
     * 获取请求追踪ID
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("X-Request-Id");
        }
        return traceId;
    }
}