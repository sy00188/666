package com.archive.management.service.impl;

import com.archive.management.config.MinioConfig;
import com.archive.management.service.FileStorageService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 文件存储服务实现类
 * 基于MinIO实现文件存储功能
 * 
 * @author Archive Management System
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 初始化存储桶
     */
    @PostConstruct
    public void initBucket() {
        try {
            String bucketName = minioConfig.getBucketName();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶成功: {}", bucketName);
            } else {
                log.info("存储桶已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("初始化存储桶失败", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try (InputStream inputStream = file.getInputStream()) {
            return uploadFile(inputStream, objectName, file.getContentType(), file.getSize());
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件输入流不能为空");
        }
        
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try {
            // 构建上传参数
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1);
            
            if (StringUtils.hasText(contentType)) {
                builder.contentType(contentType);
            }

            // 执行上传
            ObjectWriteResponse response = minioClient.putObject(builder.build());
            
            log.info("文件上传成功: {} -> {}", objectName, response.etag());
            
            // 返回文件访问URL
            return getFileUrl(objectName);
            
        } catch (Exception e) {
            log.error("文件上传失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            
            log.info("文件下载成功: {}", objectName);
            return inputStream;
            
        } catch (Exception e) {
            log.error("文件下载失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            
            log.info("文件删除成功: {}", objectName);
            return true;
            
        } catch (Exception e) {
            log.error("文件删除失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Boolean> deleteFiles(List<String> objectNames) throws Exception {
        if (objectNames == null || objectNames.isEmpty()) {
            throw new IllegalArgumentException("对象名称列表不能为空");
        }

        List<Boolean> results = new ArrayList<>();
        
        try {
            // 构建删除对象列表
            List<DeleteObject> deleteObjects = new ArrayList<>();
            for (String objectName : objectNames) {
                if (StringUtils.hasText(objectName)) {
                    deleteObjects.add(new DeleteObject(objectName));
                }
            }

            // 批量删除
            Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .objects(deleteObjects)
                            .build());

            // 处理删除结果
            for (String objectName : objectNames) {
                results.add(true); // 默认成功
            }
            
            // 检查删除错误
            for (Result<DeleteError> result : deleteResults) {
                DeleteError error = result.get();
                String errorObjectName = error.objectName();
                int index = objectNames.indexOf(errorObjectName);
                if (index >= 0) {
                    results.set(index, false);
                    log.error("文件删除失败: {}, 错误: {}", errorObjectName, error.message());
                }
            }
            
            log.info("批量删除文件完成，总数: {}, 成功: {}", 
                    objectNames.size(), results.stream().mapToInt(b -> b ? 1 : 0).sum());
            
            return results;
            
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage(), e);
            throw new Exception("批量删除文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            
            return true;
            
        } catch (Exception e) {
            // 文件不存在时会抛出异常
            if (e.getMessage().contains("NoSuchKey") || e.getMessage().contains("Not Found")) {
                return false;
            }
            log.error("检查文件是否存在失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("检查文件是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileInfo getFileInfo(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            
            FileInfo fileInfo = new FileInfo();
            fileInfo.setObjectName(objectName);
            fileInfo.setSize(stat.size());
            fileInfo.setEtag(stat.etag());
            fileInfo.setLastModified(stat.lastModified().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            fileInfo.setContentType(stat.contentType());
            
            log.info("获取文件信息成功: {}", objectName);
            return fileInfo;
            
        } catch (Exception e) {
            log.error("获取文件信息失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String objectName) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        return minioConfig.getFileUrlPrefix() + objectName;
    }

    @Override
    public String getPresignedUploadUrl(String objectName, int expiry) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        
        if (expiry <= 0) {
            expiry = 3600; // 默认1小时
        }

        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.PUT)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
            
            log.info("获取预签名上传URL成功: {}", objectName);
            return url;
            
        } catch (Exception e) {
            log.error("获取预签名上传URL失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("获取预签名上传URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedDownloadUrl(String objectName, int expiry) throws Exception {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("对象名称不能为空");
        }
        
        if (expiry <= 0) {
            expiry = 3600; // 默认1小时
        }

        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
            
            log.info("获取预签名下载URL成功: {}", objectName);
            return url;
            
        } catch (Exception e) {
            log.error("获取预签名下载URL失败: {}, 错误: {}", objectName, e.getMessage(), e);
            throw new Exception("获取预签名下载URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileInfo> listFiles(String prefix, int maxKeys) throws Exception {
        if (maxKeys <= 0) {
            maxKeys = 1000; // 默认最大1000个
        }

        List<FileInfo> fileInfos = new ArrayList<>();
        
        try {
            ListObjectsArgs.Builder builder = ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .maxKeys(maxKeys);
            
            if (StringUtils.hasText(prefix)) {
                builder.prefix(prefix);
            }

            Iterable<Result<Item>> results = minioClient.listObjects(builder.build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                
                FileInfo fileInfo = new FileInfo();
                fileInfo.setObjectName(item.objectName());
                fileInfo.setSize(item.size());
                fileInfo.setEtag(item.etag());
                fileInfo.setLastModified(item.lastModified().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                // Note: ListObjects不返回contentType，需要单独获取
                
                fileInfos.add(fileInfo);
            }
            
            log.info("列出文件成功，前缀: {}, 数量: {}", prefix, fileInfos.size());
            return fileInfos;
            
        } catch (Exception e) {
            log.error("列出文件失败，前缀: {}, 错误: {}", prefix, e.getMessage(), e);
            throw new Exception("列出文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean copyFile(String sourceObjectName, String targetObjectName) throws Exception {
        if (!StringUtils.hasText(sourceObjectName)) {
            throw new IllegalArgumentException("源对象名称不能为空");
        }
        
        if (!StringUtils.hasText(targetObjectName)) {
            throw new IllegalArgumentException("目标对象名称不能为空");
        }

        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(targetObjectName)
                    .source(CopySource.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(sourceObjectName)
                            .build())
                    .build());
            
            log.info("文件复制成功: {} -> {}", sourceObjectName, targetObjectName);
            return true;
            
        } catch (Exception e) {
            log.error("文件复制失败: {} -> {}, 错误: {}", sourceObjectName, targetObjectName, e.getMessage(), e);
            throw new Exception("文件复制失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean moveFile(String sourceObjectName, String targetObjectName) throws Exception {
        if (!StringUtils.hasText(sourceObjectName)) {
            throw new IllegalArgumentException("源对象名称不能为空");
        }
        
        if (!StringUtils.hasText(targetObjectName)) {
            throw new IllegalArgumentException("目标对象名称不能为空");
        }

        try {
            // 先复制文件
            boolean copySuccess = copyFile(sourceObjectName, targetObjectName);
            
            if (copySuccess) {
                // 删除源文件
                boolean deleteSuccess = deleteFile(sourceObjectName);
                
                if (deleteSuccess) {
                    log.info("文件移动成功: {} -> {}", sourceObjectName, targetObjectName);
                    return true;
                } else {
                    log.warn("文件移动部分成功，复制成功但删除源文件失败: {} -> {}", sourceObjectName, targetObjectName);
                    return false;
                }
            } else {
                log.error("文件移动失败，复制失败: {} -> {}", sourceObjectName, targetObjectName);
                return false;
            }
            
        } catch (Exception e) {
            log.error("文件移动失败: {} -> {}, 错误: {}", sourceObjectName, targetObjectName, e.getMessage(), e);
            throw new Exception("文件移动失败: " + e.getMessage(), e);
        }
    }
}