package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 用户未找到异常（ResourceNotFoundException的别名）
 * 为了兼容性保留此类
 */
public class UserNotFoundException extends ResourceNotFoundException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UserNotFoundException(ResponseCode responseCode) {
        super(responseCode);
    }
    
    public UserNotFoundException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }
    
    /**
     * 根据用户ID创建异常
     */
    public static UserNotFoundException byId(Long userId) {
        return new UserNotFoundException("用户未找到，ID: " + userId);
    }
    
    /**
     * 根据用户名创建异常
     */
    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("用户未找到，用户名: " + username);
    }
}


