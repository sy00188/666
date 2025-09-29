package com.archive.management.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件存储服务接口
 * 提供文件上传、下载、删除等核心功能
 * 
 * @author Archive Management System
 * @version 1.0.0
 */
public interface FileStorageService {

    /**
     * 上传文件
     * 
     * @param file 要上传的文件
     * @param objectName 对象名称（文件在存储中的路径）
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadFile(MultipartFile file, String objectName) throws Exception;

    /**
     * 上传文件流
     * 
     * @param inputStream 文件输入流
     * @param objectName 对象名称
     * @param contentType 文件内容类型
     * @param size 文件大小
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType, long size) throws Exception;

    /**
     * 下载文件
     * 
     * @param objectName 对象名称
     * @return 文件输入流
     * @throws Exception 下载失败时抛出异常
     */
    InputStream downloadFile(String objectName) throws Exception;

    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     * @return 删除是否成功
     * @throws Exception 删除失败时抛出异常
     */
    boolean deleteFile(String objectName) throws Exception;

    /**
     * 批量删除文件
     * 
     * @param objectNames 对象名称列表
     * @return 删除结果列表（true表示成功，false表示失败）
     * @throws Exception 删除失败时抛出异常
     */
    List<Boolean> deleteFiles(List<String> objectNames) throws Exception;

    /**
     * 检查文件是否存在
     * 
     * @param objectName 对象名称
     * @return 文件是否存在
     * @throws Exception 检查失败时抛出异常
     */
    boolean fileExists(String objectName) throws Exception;

    /**
     * 获取文件信息
     * 
     * @param objectName 对象名称
     * @return 文件信息对象
     * @throws Exception 获取失败时抛出异常
     */
    FileInfo getFileInfo(String objectName) throws Exception;

    /**
     * 获取文件访问URL
     * 
     * @param objectName 对象名称
     * @return 文件访问URL
     * @throws Exception 获取失败时抛出异常
     */
    String getFileUrl(String objectName) throws Exception;

    /**
     * 获取预签名上传URL
     * 
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 预签名上传URL
     * @throws Exception 获取失败时抛出异常
     */
    String getPresignedUploadUrl(String objectName, int expiry) throws Exception;

    /**
     * 获取预签名下载URL
     * 
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 预签名下载URL
     * @throws Exception 获取失败时抛出异常
     */
    String getPresignedDownloadUrl(String objectName, int expiry) throws Exception;

    /**
     * 列出指定前缀的文件
     * 
     * @param prefix 文件前缀
     * @param maxKeys 最大返回数量
     * @return 文件信息列表
     * @throws Exception 列出失败时抛出异常
     */
    List<FileInfo> listFiles(String prefix, int maxKeys) throws Exception;

    /**
     * 复制文件
     * 
     * @param sourceObjectName 源对象名称
     * @param targetObjectName 目标对象名称
     * @return 复制是否成功
     * @throws Exception 复制失败时抛出异常
     */
    boolean copyFile(String sourceObjectName, String targetObjectName) throws Exception;

    /**
     * 移动文件
     * 
     * @param sourceObjectName 源对象名称
     * @param targetObjectName 目标对象名称
     * @return 移动是否成功
     * @throws Exception 移动失败时抛出异常
     */
    boolean moveFile(String sourceObjectName, String targetObjectName) throws Exception;

    /**
     * 文件信息内部类
     */
    class FileInfo {
        private String objectName;
        private long size;
        private String etag;
        private String lastModified;
        private String contentType;

        // 构造函数
        public FileInfo() {}

        public FileInfo(String objectName, long size, String etag, String lastModified, String contentType) {
            this.objectName = objectName;
            this.size = size;
            this.etag = etag;
            this.lastModified = lastModified;
            this.contentType = contentType;
        }

        // Getter和Setter方法
        public String getObjectName() {
            return objectName;
        }

        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String toString() {
            return "FileInfo{" +
                    "objectName='" + objectName + '\'' +
                    ", size=" + size +
                    ", etag='" + etag + '\'' +
                    ", lastModified='" + lastModified + '\'' +
                    ", contentType='" + contentType + '\'' +
                    '}';
        }
    }
}