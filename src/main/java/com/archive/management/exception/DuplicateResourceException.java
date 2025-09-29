package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 重复资源异常
 * 用于处理如角色编码、名称等唯一键冲突的业务场景
 */
public class DuplicateResourceException extends BaseException {

    // 基础构造函数
    public DuplicateResourceException() {
        super(ResponseCode.CONFLICT);
    }

    public DuplicateResourceException(String message) {
        super(ResponseCode.CONFLICT, message);
    }

    public DuplicateResourceException(String message, Object data) {
        super(ResponseCode.CONFLICT, message, data);
    }

    public DuplicateResourceException(Throwable cause) {
        super(ResponseCode.CONFLICT, cause);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(ResponseCode.CONFLICT, message, cause);
    }

    public DuplicateResourceException(String message, Object data, Throwable cause) {
        super(ResponseCode.CONFLICT, message, data, cause);
    }

    // 语义化静态工厂
    public static DuplicateResourceException roleCodeExists(String roleCode) {
        return new DuplicateResourceException("角色编码已存在: " + roleCode);
    }

    public static DuplicateResourceException roleNameExists(String roleName) {
        return new DuplicateResourceException("角色名称已存在: " + roleName);
    }

    public static DuplicateResourceException resourceExists(String resourceDesc) {
        return new DuplicateResourceException("资源已存在: " + resourceDesc);
    }
}