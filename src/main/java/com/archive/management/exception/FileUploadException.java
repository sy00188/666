package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 文件上传异常类
 * 用于处理文件上传过程中的各种异常情况
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class FileUploadException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     */
    public FileUploadException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     */
    public FileUploadException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     * @param data 数据
     */
    public FileUploadException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param cause 原因
     */
    public FileUploadException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 错误消息
     * @param cause 原因
     */
    public FileUploadException(ResponseCode responseCode, String message, Throwable cause) {
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
    public FileUploadException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param message 错误消息
     */
    public FileUploadException(String message) {
        super(ResponseCode.FILE_UPLOAD_FAILED, message);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param fileName 文件名
     * @param message 错误消息
     */
    public FileUploadException(String fileName, String message) {
        super(ResponseCode.FILE_UPLOAD_FAILED, String.format("文件 [%s] 上传失败: %s", fileName, message));
        this.fileName = fileName;
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param message 错误消息
     */
    public FileUploadException(String fileName, Long fileSize, String message) {
        super(ResponseCode.FILE_UPLOAD_FAILED, String.format("文件 [%s] (大小: %d bytes) 上传失败: %s", fileName, fileSize, message));
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public FileUploadException(String message, Throwable cause) {
        super(ResponseCode.FILE_UPLOAD_FAILED, message, cause);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param fileName 文件名
     * @param message 错误消息
     * @param cause 原因
     */
    public FileUploadException(String fileName, String message, Throwable cause) {
        super(ResponseCode.FILE_UPLOAD_FAILED, String.format("文件 [%s] 上传失败: %s", fileName, message), cause);
        this.fileName = fileName;
    }

    // 常用静态方法

    /**
     * 文件大小超限异常
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param maxSize 最大允许大小
     * @return FileUploadException
     */
    public static FileUploadException fileSizeExceeded(String fileName, Long fileSize, Long maxSize) {
        FileUploadException exception = new FileUploadException(fileName, fileSize, 
            String.format("文件大小超出限制，当前: %d bytes, 最大允许: %d bytes", fileSize, maxSize));
        exception.setErrorType("FILE_SIZE_EXCEEDED");
        return exception;
    }

    /**
     * 文件类型不支持异常
     * 
     * @param fileName 文件名
     * @param fileType 文件类型
     * @return FileUploadException
     */
    public static FileUploadException unsupportedFileType(String fileName, String fileType) {
        FileUploadException exception = new FileUploadException(fileName, 
            String.format("不支持的文件类型: %s", fileType));
        exception.setErrorType("UNSUPPORTED_FILE_TYPE");
        return exception;
    }

    /**
     * 文件为空异常
     * 
     * @param fileName 文件名
     * @return FileUploadException
     */
    public static FileUploadException emptyFile(String fileName) {
        FileUploadException exception = new FileUploadException(fileName, "文件为空");
        exception.setErrorType("EMPTY_FILE");
        return exception;
    }

    /**
     * 文件名非法异常
     * 
     * @param fileName 文件名
     * @return FileUploadException
     */
    public static FileUploadException invalidFileName(String fileName) {
        FileUploadException exception = new FileUploadException(fileName, "文件名包含非法字符");
        exception.setErrorType("INVALID_FILE_NAME");
        return exception;
    }

    /**
     * 存储空间不足异常
     * 
     * @param fileName 文件名
     * @return FileUploadException
     */
    public static FileUploadException insufficientStorage(String fileName) {
        FileUploadException exception = new FileUploadException(fileName, "存储空间不足");
        exception.setErrorType("INSUFFICIENT_STORAGE");
        return exception;
    }

    /**
     * 文件已存在异常
     * 
     * @param fileName 文件名
     * @return FileUploadException
     */
    public static FileUploadException fileExists(String fileName) {
        FileUploadException exception = new FileUploadException(fileName, "文件已存在");
        exception.setErrorType("FILE_EXISTS");
        return exception;
    }

    /**
     * 上传超时异常
     * 
     * @param fileName 文件名
     * @return FileUploadException
     */
    public static FileUploadException uploadTimeout(String fileName) {
        FileUploadException exception = new FileUploadException(fileName, "上传超时");
        exception.setErrorType("UPLOAD_TIMEOUT");
        return exception;
    }

    /**
     * IO异常
     * 
     * @param fileName 文件名
     * @param cause 原因
     * @return FileUploadException
     */
    public static FileUploadException ioError(String fileName, Throwable cause) {
        FileUploadException exception = new FileUploadException(fileName, "文件读写异常", cause);
        exception.setErrorType("IO_ERROR");
        return exception;
    }

    // Getter 和 Setter 方法

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return "FileUploadException{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", errorType='" + errorType + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}