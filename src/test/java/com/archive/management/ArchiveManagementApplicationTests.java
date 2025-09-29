package com.archive.management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Spring Boot应用主测试类
 * 测试应用启动和基础配置
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@SpringBootTest(classes = ArchiveManagementApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ArchiveManagementApplicationTests {

    /**
     * 测试应用上下文加载
     */
    @Test
    void contextLoads() {
        // 此测试验证Spring应用上下文能够成功加载
        // 如果上下文加载失败，测试将抛出异常
    }

    /**
     * 测试应用启动
     */
    @Test
    void applicationStarts() {
        // 验证应用能够正常启动
        // 这个测试会启动整个Spring Boot应用
    }
}