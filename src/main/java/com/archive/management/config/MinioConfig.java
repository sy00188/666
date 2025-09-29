package com.archive.management.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MinIO配置类
 * 提供MinIO客户端配置和连接管理
 * 
 * @author Archive Management System
 * @version 1.0.0
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "archive.minio")
@Data
@Validated
public class MinioConfig {

    /**
     * MinIO服务器地址
     */
    @NotBlank(message = "MinIO服务器地址不能为空")
    private String endpoint = "http://localhost:9000";

    /**
     * MinIO访问密钥
     */
    @NotBlank(message = "MinIO访问密钥不能为空")
    private String accessKey = "minioadmin";

    /**
     * MinIO秘密密钥
     */
    @NotBlank(message = "MinIO秘密密钥不能为空")
    private String secretKey = "minioadmin";

    /**
     * 默认存储桶名称
     */
    @NotBlank(message = "默认存储桶名称不能为空")
    private String bucketName = "archive-files";

    /**
     * 连接超时时间（毫秒）
     */
    @NotNull(message = "连接超时时间不能为空")
    private Long connectTimeout = 10000L;

    /**
     * 写入超时时间（毫秒）
     */
    @NotNull(message = "写入超时时间不能为空")
    private Long writeTimeout = 60000L;

    /**
     * 读取超时时间（毫秒）
     */
    @NotNull(message = "读取超时时间不能为空")
    private Long readTimeout = 10000L;

    /**
     * 创建MinIO客户端Bean
     * 
     * @return MinioClient实例
     */
    @Bean
    public MinioClient minioClient() {
        try {
            log.info("正在初始化MinIO客户端，服务器地址: {}", endpoint);
            
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // 设置超时时间
            client.setTimeout(connectTimeout, writeTimeout, readTimeout);
            
            log.info("MinIO客户端初始化成功");
            return client;
            
        } catch (Exception e) {
            log.error("MinIO客户端初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }

    /**
     * 检查MinIO连接状态
     * 
     * @return 连接是否正常
     */
    public boolean checkConnection() {
        try {
            MinioClient client = minioClient();
            // 尝试列出存储桶来测试连接
            client.listBuckets();
            log.info("MinIO连接检查成功");
            return true;
        } catch (Exception e) {
            log.error("MinIO连接检查失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取文件访问URL前缀
     * 
     * @return URL前缀
     */
    public String getFileUrlPrefix() {
        return endpoint + "/" + bucketName + "/";
    }
}