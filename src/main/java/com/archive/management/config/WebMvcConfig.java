package com.archive.management.config;

import com.archive.management.interceptor.LoggingInterceptor;
import com.archive.management.interceptor.RequestLimitInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Web MVC配置类
 * 配置拦截器、消息转换器、CORS等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private RequestLimitInterceptor requestLimitInterceptor;

    /**
     * 配置拦截器
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志拦截器
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                );

        // 请求限制拦截器
        registry.addInterceptor(requestLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/public/**"
                );
    }

    /**
     * 配置CORS跨域
     * 
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源处理
     * 
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // 上传文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        // Swagger UI
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");

        // API文档
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

    /**
     * 配置视图控制器
     * 
     * @param registry 视图控制器注册器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 默认首页
        registry.addViewController("/").setViewName("redirect:/doc.html");
        registry.addViewController("/index").setViewName("redirect:/doc.html");
    }

    /**
     * 配置消息转换器
     * 
     * @param converters 消息转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
    }

    /**
     * 配置Jackson消息转换器
     * 
     * @return MappingJackson2HttpMessageConverter
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    /**
     * 配置ObjectMapper
     * 
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 设置日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        
        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 忽略空值属性
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
    }

    /**
     * 配置内容协商
     * 
     * @param configurer 内容协商配置器
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .favorPathExtension(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("xml", org.springframework.http.MediaType.APPLICATION_XML);
    }

    /**
     * 配置路径匹配
     * 
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 设置URL后缀模式匹配
        configurer.setUseSuffixPatternMatch(false);
        
        // 设置尾部斜杠匹配
        configurer.setUseTrailingSlashMatch(true);
        
        // 设置URL解码
        configurer.setUrlDecode(true);
    }

    /**
     * 配置异步支持
     * 
     * @param configurer 异步支持配置器
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间（毫秒）
        configurer.setDefaultTimeout(30000);
        
        // 设置异步请求任务执行器
        configurer.setTaskExecutor(asyncTaskExecutor());
    }

    /**
     * 异步任务执行器
     * 
     * @return TaskExecutor
     */
    @Bean
    public org.springframework.core.task.TaskExecutor asyncTaskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = 
                new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        return executor;
    }

    /**
     * 配置默认Servlet处理
     * 
     * @param configurer 默认Servlet处理配置器
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}