package com.archive.management.config;

import com.archive.management.interceptor.AuthenticationInterceptor;
import com.archive.management.interceptor.LoggingInterceptor;
import com.archive.management.interceptor.RateLimitInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Web配置类
 * 配置跨域、拦截器、消息转换器、静态资源等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Value("${static.resource.path:/static/**}")
    private String staticResourcePath;

    @Value("${static.resource.location:classpath:/static/}")
    private String staticResourceLocation;

    @Value("${upload.resource.path:/uploads/**}")
    private String uploadResourcePath;

    @Value("${upload.resource.location:file:./uploads/}")
    private String uploadResourceLocation;

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    /**
     * 配置CORS过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(Arrays.asList(allowedMethods));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志拦截器 - 拦截所有请求
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 限流拦截器 - 拦截API请求
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .order(2);

        // 认证拦截器 - 拦截需要认证的请求
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/public/**",
                    "/api/health/**",
                    "/api/docs/**"
                )
                .order(3);
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源
        registry.addResourceHandler(staticResourcePath)
                .addResourceLocations(staticResourceLocation)
                .setCachePeriod(3600)
                .resourceChain(true);

        // 上传文件资源
        registry.addResourceHandler(uploadResourcePath)
                .addResourceLocations(uploadResourceLocation)
                .setCachePeriod(3600)
                .resourceChain(true);

        // Swagger UI 资源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);

        // API文档资源
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .resourceChain(false);
    }

    /**
     * 配置视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 首页重定向
        registry.addViewController("/").setViewName("redirect:/index.html");
        
        // API文档页面
        registry.addViewController("/docs").setViewName("redirect:/swagger-ui/index.html");
        registry.addViewController("/api-docs").setViewName("redirect:/swagger-ui/index.html");
    }

    /**
     * 配置消息转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
    }

    /**
     * 配置Jackson消息转换器
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    /**
     * 配置ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 设置日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        
        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 忽略空值
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
    }

    /**
     * 配置内容协商
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(true)
                .parameterName("format")
                .ignoreAcceptHeader(false)
                .useRegisteredExtensionsOnly(false)
                .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("xml", org.springframework.http.MediaType.APPLICATION_XML);
    }

    /**
     * 配置路径匹配
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
                .setUseTrailingSlashMatch(true)
                .setUseSuffixPatternMatch(false);
    }

    /**
     * 配置异步支持
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30000); // 30秒超时
        configurer.setTaskExecutor(asyncTaskExecutor());
    }

    /**
     * 异步任务执行器
     */
    @Bean
    public org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor asyncTaskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = 
            new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // 拒绝策略
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }

    /**
     * 获取Web配置信息
     */
    public String getWebConfigInfo() {
        return String.format(
            "Web配置信息 - 允许的源: %s, 允许的方法: %s, 静态资源路径: %s",
            String.join(",", allowedOrigins),
            String.join(",", allowedMethods),
            staticResourcePath
        );
    }

    /**
     * 检查CORS配置
     */
    public boolean checkCorsConfig(String origin, String method) {
        return Arrays.asList(allowedOrigins).contains(origin) && 
               Arrays.asList(allowedMethods).contains(method.toUpperCase());
    }
}