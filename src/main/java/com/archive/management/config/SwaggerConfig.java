package com.archive.management.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger API文档配置类
 * 配置OpenAPI 3.0文档生成
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
public class SwaggerConfig {

    @Value("${swagger.title:档案管理系统API}")
    private String title;

    @Value("${swagger.description:档案管理系统RESTful API文档}")
    private String description;

    @Value("${swagger.version:1.0.0}")
    private String version;

    @Value("${swagger.contact.name:Archive Management Team}")
    private String contactName;

    @Value("${swagger.contact.email:admin@archive.com}")
    private String contactEmail;

    @Value("${swagger.contact.url:https://www.archive.com}")
    private String contactUrl;

    @Value("${swagger.license.name:Apache 2.0}")
    private String licenseName;

    @Value("${swagger.license.url:https://www.apache.org/licenses/LICENSE-2.0}")
    private String licenseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${swagger.server.url:http://localhost}")
    private String serverUrl;

    /**
     * OpenAPI配置
     * 
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * API信息配置
     * 
     * @return API信息
     */
    private Info apiInfo() {
        return new Info()
                .title(title)
                .description(description)
                .version(version)
                .contact(new Contact()
                        .name(contactName)
                        .email(contactEmail)
                        .url(contactUrl))
                .license(new License()
                        .name(licenseName)
                        .url(licenseUrl));
    }

    /**
     * 服务器列表配置
     * 
     * @return 服务器列表
     */
    private List<Server> serverList() {
        Server devServer = new Server()
                .url(serverUrl + ":" + serverPort)
                .description("开发环境");

        Server testServer = new Server()
                .url("https://test-api.archive.com")
                .description("测试环境");

        Server prodServer = new Server()
                .url("https://api.archive.com")
                .description("生产环境");

        return Arrays.asList(devServer, testServer, prodServer);
    }

    /**
     * 安全组件配置
     * 
     * @return 安全组件
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT认证token"))
                .addSecuritySchemes("ApiKey", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-KEY")
                        .description("API密钥认证"));
    }

    /**
     * 安全要求配置
     * 
     * @return 安全要求
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer")
                .addList("ApiKey");
    }

    /**
     * 用户管理API分组
     * 
     * @return 用户管理API组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/api/users/**", "/api/auth/**", "/api/roles/**", "/api/permissions/**")
                .build();
    }

    /**
     * 档案管理API分组
     * 
     * @return 档案管理API组
     */
    @Bean
    public GroupedOpenApi archiveApi() {
        return GroupedOpenApi.builder()
                .group("档案管理")
                .pathsToMatch("/api/archives/**", "/api/categories/**", "/api/files/**")
                .build();
    }

    /**
     * 系统管理API分组
     * 
     * @return 系统管理API组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .pathsToMatch("/api/system/**", "/api/config/**", "/api/logs/**", "/api/monitor/**")
                .build();
    }

    /**
     * 统计报表API分组
     * 
     * @return 统计报表API组
     */
    @Bean
    public GroupedOpenApi statisticsApi() {
        return GroupedOpenApi.builder()
                .group("统计报表")
                .pathsToMatch("/api/statistics/**", "/api/reports/**", "/api/dashboard/**")
                .build();
    }

    /**
     * 工作流API分组
     * 
     * @return 工作流API组
     */
    @Bean
    public GroupedOpenApi workflowApi() {
        return GroupedOpenApi.builder()
                .group("工作流管理")
                .pathsToMatch("/api/workflow/**", "/api/tasks/**", "/api/approvals/**")
                .build();
    }

    /**
     * 消息通知API分组
     * 
     * @return 消息通知API组
     */
    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("消息通知")
                .pathsToMatch("/api/notifications/**", "/api/messages/**", "/api/announcements/**")
                .build();
    }

    /**
     * 数据字典API分组
     * 
     * @return 数据字典API组
     */
    @Bean
    public GroupedOpenApi dictionaryApi() {
        return GroupedOpenApi.builder()
                .group("数据字典")
                .pathsToMatch("/api/dict/**", "/api/dictionaries/**")
                .build();
    }

    /**
     * 部门组织API分组
     * 
     * @return 部门组织API组
     */
    @Bean
    public GroupedOpenApi departmentApi() {
        return GroupedOpenApi.builder()
                .group("部门组织")
                .pathsToMatch("/api/departments/**", "/api/organizations/**")
                .build();
    }

    /**
     * 搜索API分组
     * 
     * @return 搜索API组
     */
    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
                .group("搜索功能")
                .pathsToMatch("/api/search/**", "/api/elasticsearch/**")
                .build();
    }

    /**
     * 公共API分组（无需认证）
     * 
     * @return 公共API组
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("公共接口")
                .pathsToMatch("/api/public/**", "/api/captcha/**", "/api/health/**")
                .build();
    }

    /**
     * 完整API分组
     * 
     * @return 完整API组
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("完整API")
                .pathsToMatch("/api/**")
                .build();
    }
}