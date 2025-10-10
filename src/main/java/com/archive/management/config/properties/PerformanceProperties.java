package com.archive.management.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 性能配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "archive.performance")
public class PerformanceProperties {

    /**
     * 异步配置
     */
    private Async async = new Async();

    /**
     * 定时任务配置
     */
    private Scheduled scheduled = new Scheduled();

    /**
     * 文件上传配置
     */
    private FileUpload fileUpload = new FileUpload();

    /**
     * 搜索配置
     */
    private Search search = new Search();

    /**
     * 批量操作配置
     */
    private BatchOperation batchOperation = new BatchOperation();

    /**
     * 异步配置
     */
    @Data
    public static class Async {
        /** 核心线程池大小 */
        private Integer corePoolSize = 5;
        
        /** 最大线程池大小 */
        private Integer maxPoolSize = 20;
        
        /** 队列容量 */
        private Integer queueCapacity = 100;
        
        /** 保持活跃时间(秒) */
        private Integer keepAliveSeconds = 60;
    }

    /**
     * 定时任务配置
     */
    @Data
    public static class Scheduled {
        /** 线程池大小 */
        private Integer poolSize = 5;
    }

    /**
     * 文件上传配置
     */
    @Data
    public static class FileUpload {
        /** 核心线程池大小 */
        private Integer corePoolSize = 3;
        
        /** 最大线程池大小 */
        private Integer maxPoolSize = 10;
        
        /** 队列容量 */
        private Integer queueCapacity = 50;
        
        /** 保持活跃时间(秒) */
        private Integer keepAliveSeconds = 60;
    }

    /**
     * 搜索配置
     */
    @Data
    public static class Search {
        /** 核心线程池大小 */
        private Integer corePoolSize = 5;
        
        /** 最大线程池大小 */
        private Integer maxPoolSize = 15;
        
        /** 队列容量 */
        private Integer queueCapacity = 100;
        
        /** 保持活跃时间(秒) */
        private Integer keepAliveSeconds = 60;
    }

    /**
     * 批量操作配置
     */
    @Data
    public static class BatchOperation {
        /** 核心线程池大小 */
        private Integer corePoolSize = 3;
        
        /** 最大线程池大小 */
        private Integer maxPoolSize = 8;
        
        /** 队列容量 */
        private Integer queueCapacity = 50;
        
        /** 保持活跃时间(秒) */
        private Integer keepAliveSeconds = 60;
    }
}

