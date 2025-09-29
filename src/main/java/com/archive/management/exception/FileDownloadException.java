package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 文件下载异常类
 * 用于处理文件下载过程中的各种异常情况
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class FileDownloadException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     */
    public FileDownloadException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     */
    public FileDownloadException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     * @param data 数据
     */
    public FileDownloadException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param cause 原因
     */
    public FileDownloadException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     * @param cause 原因
     */
    public FileDownloadException(ResponseCode responseCode, String message, Throwable cause) {
        super(responseCode, message, cause);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     * @param data 数据
     * @param cause 原因
     */
    public FileDownloadException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param message 错误消息
     */
    public FileDownloadException(String message) {
        super(ResponseCode.FILE_DOWNLOAD_FAILED, message);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param fileName 文件名
     * @param message 错误消息
     */
    public FileDownloadException(String fileName, String message) {
        super(ResponseCode.FILE_DOWNLOAD_FAILED, String.format("文件 [%s] 下载失败: %s", fileName, message));
        this.fileName = fileName;
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param message 错误消息
     */
    public FileDownloadException(String fileName, String filePath, String message) {
        super(ResponseCode.FILE_DOWNLOAD_FAILED, String.format("文件 [%s] (路径: %s) 下载失败: %s", fileName, filePath, message));
        this.fileName = fileName;
        this.filePath = filePath;
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public FileDownloadException(String message, Throwable cause) {
        super(ResponseCode.FILE_DOWNLOAD_FAILED, message, cause);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param fileName 文件名
     * @param message 错误消息
     * @param cause 原因
     */
    public FileDownloadException(String fileName, String message, Throwable cause) {
        super(ResponseCode.FILE_DOWNLOAD_FAILED, String.format("文件 [%s] 下载失败: %s", fileName, message), cause);
        this.fileName = fileName;
    }

    // 常用静态方法

    /**
     * 文件不存在异常
     * 
     * @param fileName 文件名
     * @return FileDownloadException
     */
    public static FileDownloadException fileNotFound(String fileName) {
        return new FileDownloadException(fileName, "文件不存在");
    }

    /**
     * 文件不存在异常
     * 
     * @param fileName 文件名
     * @param filePath 文件路径
     * @return FileDownloadException
     */
    public static FileDownloadException fileNotFound(String fileName, String filePath) {
        return new FileDownloadException(fileName, filePath, "文件不存在");
    }

    /**
     * 文件访问权限不足异常
     * 
     * @param fileName 文件名
     * @return FileDownloadException
     */
    public static FileDownloadException accessDenied(String fileName) {
        return new FileDownloadException(fileName, "文件访问权限不足");
    }

    /**
     * 文件读取失败异常
     * 
     * @param fileName 文件名
     * @param cause 原因
     * @return FileDownloadException
     */
    public static FileDownloadException readFailed(String fileName, Throwable cause) {
        return new FileDownloadException(fileName, "文件读取失败", cause);
    }

    /**
     * 网络传输失败异常
     * 
     * @param fileName 文件名
     * @param cause 原因
     * @return FileDownloadException
     */
    public static FileDownloadException networkError(String fileName, Throwable cause) {
        return new FileDownloadException(fileName, "网络传输失败", cause);
    }

    /**
     * 磁盘空间不足异常
     * 
     * @param fileName 文件名
     * @return FileDownloadException
     */
    public static FileDownloadException diskSpaceInsufficient(String fileName) {
        return new FileDownloadException(fileName, "磁盘空间不足");
    }

    /**
     * 文件损坏异常
     * 
     * @param fileName 文件名
     * @return FileDownloadException
     */
    public static FileDownloadException fileCorrupted(String fileName) {
        return new FileDownloadException(fileName, "文件已损坏");
    }

    // Getter and Setter methods

    /**
     * 获取文件名
     * 
     * @return 文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名
     * 
     * @param fileName 文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取文件路径
     * 
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置文件路径
     * 
     * @param filePath 文件路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 获取错误类型
     * 
     * @return 错误类型
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * 设置错误类型
     * 
     * @param errorType 错误类型
     */
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return String.format("FileDownloadException{fileName='%s', filePath='%s', errorType='%s', message='%s'}", 
                           fileName, filePath, errorType, getMessage());
    }
}