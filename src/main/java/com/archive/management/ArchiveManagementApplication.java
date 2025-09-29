package com.archive.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 档案管理系统主启动类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {"com.archive.management", "com.archivesystem"})
@EnableJpaRepositories(basePackages = "com.archive.management.repository")
@EntityScan(basePackages = "com.archive.management.entity")
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@EnableConfigurationProperties
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ArchiveManagementApplication {

    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("spring.application.name", "archive-management-system");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("user.timezone", "Asia/Shanghai");
        
        // 启动应用
        SpringApplication application = new SpringApplication(ArchiveManagementApplication.class);
        
        // 设置默认配置文件
        application.setAdditionalProfiles("dev");
        
        // 启动横幅
        application.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);
        
        // 运行应用
        application.run(args);
    }
}