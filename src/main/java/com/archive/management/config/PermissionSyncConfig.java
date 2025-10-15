package com.archive.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限同步配置类
 * 读取permissions.yml配置文件
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "")
public class PermissionSyncConfig {

    private List<PermissionConfig> permissions = new ArrayList<>();

    /**
     * 权限配置项
     */
    @Data
    public static class PermissionConfig {
        private String code;
        private String name;
        private String description;
        private String type;
        private String path;
        private String icon;
        private Integer sort;
        private String parentCode;
    }
}

