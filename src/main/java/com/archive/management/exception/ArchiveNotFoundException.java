package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 档案未找到异常类
 * 当请求的档案不存在时抛出此异常
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class ArchiveNotFoundException extends BusinessException {

    /**
     * 默认构造函数
     */
    public ArchiveNotFoundException() {
        super(ResponseCode.ARCHIVE_NOT_FOUND, "档案不存在");
    }

    /**
     * 构造函数
     *
     * @param message 自定义错误消息
     */
    public ArchiveNotFoundException(String message) {
        super(ResponseCode.ARCHIVE_NOT_FOUND, message);
    }

    /**
     * 构造函数
     *
     * @param archiveId 档案ID
     */
    public ArchiveNotFoundException(Long archiveId) {
        super(ResponseCode.ARCHIVE_NOT_FOUND, "档案不存在，ID: " + archiveId);
    }

    /**
     * 构造函数
     *
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public ArchiveNotFoundException(String message, Throwable cause) {
        super(ResponseCode.ARCHIVE_NOT_FOUND, message, cause);
    }

    /**
     * 构造函数
     *
     * @param archiveId 档案ID
     * @param cause 原因异常
     */
    public ArchiveNotFoundException(Long archiveId, Throwable cause) {
        super(ResponseCode.ARCHIVE_NOT_FOUND, "档案不存在，ID: " + archiveId, cause);
    }

    /**
     * 静态工厂方法 - 根据ID创建异常
     *
     * @param archiveId 档案ID
     * @return ArchiveNotFoundException实例
     */
    public static ArchiveNotFoundException byId(Long archiveId) {
        return new ArchiveNotFoundException(archiveId);
    }

    /**
     * 静态工厂方法 - 根据标题创建异常
     *
     * @param title 档案标题
     * @return ArchiveNotFoundException实例
     */
    public static ArchiveNotFoundException byTitle(String title) {
        return new ArchiveNotFoundException("档案不存在，标题: " + title);
    }

    /**
     * 静态工厂方法 - 根据编号创建异常
     *
     * @param archiveNumber 档案编号
     * @return ArchiveNotFoundException实例
     */
    public static ArchiveNotFoundException byNumber(String archiveNumber) {
        return new ArchiveNotFoundException("档案不存在，编号: " + archiveNumber);
    }
}