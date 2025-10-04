package com.archive.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 性能优化配置类
 * 配置线程池、异步处理、缓存等性能相关设置
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(PerformanceProperties.class)
public class PerformanceConfig implements WebMvcConfigurer {

    private final PerformanceProperties performanceProperties;

    public PerformanceConfig(PerformanceProperties performanceProperties) {
        this.performanceProperties = performanceProperties;
    }

    /**
     * 异步任务执行器
     */
    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(performanceProperties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(performanceProperties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(performanceProperties.getAsync().getQueueCapacity());
        executor.setKeepAliveSeconds(performanceProperties.getAsync().getKeepAliveSeconds());
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 定时任务执行器
     */
    @Bean("scheduledTaskExecutor")
    public Executor scheduledTaskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(performanceProperties.getScheduled().getPoolSize());
        scheduler.setThreadNamePrefix("scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 文件上传执行器
     */
    @Bean("fileUploadExecutor")
    public Executor fileUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(performanceProperties.getFileUpload().getCorePoolSize());
        executor.setMaxPoolSize(performanceProperties.getFileUpload().getMaxPoolSize());
        executor.setQueueCapacity(performanceProperties.getFileUpload().getQueueCapacity());
        executor.setKeepAliveSeconds(performanceProperties.getFileUpload().getKeepAliveSeconds());
        executor.setThreadNamePrefix("file-upload-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 搜索执行器
     */
    @Bean("searchExecutor")
    public Executor searchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(performanceProperties.getSearch().getCorePoolSize());
        executor.setMaxPoolSize(performanceProperties.getSearch().getMaxPoolSize());
        executor.setQueueCapacity(performanceProperties.getSearch().getQueueCapacity());
        executor.setKeepAliveSeconds(performanceProperties.getSearch().getKeepAliveSeconds());
        executor.setThreadNamePrefix("search-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 批量操作执行器
     */
    @Bean("batchOperationExecutor")
    public Executor batchOperationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(performanceProperties.getBatchOperation().getCorePoolSize());
        executor.setMaxPoolSize(performanceProperties.getBatchOperation().getMaxPoolSize());
        executor.setQueueCapacity(performanceProperties.getBatchOperation().getQueueCapacity());
        executor.setKeepAliveSeconds(performanceProperties.getBatchOperation().getKeepAliveSeconds());
        executor.setThreadNamePrefix("batch-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 配置异步支持
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncTaskExecutor());
        configurer.setDefaultTimeout(30000);
    }

    /**
     * 配置CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }

    /**
     * 配置视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/dashboard").setViewName("dashboard");
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PerformanceInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/uploads/**", "/actuator/**");
    }

    /**
     * 性能监控拦截器
     */
    public static class PerformanceInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            request.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }
        
        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
            long startTime = (Long) request.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            if (duration > 1000) { // 超过1秒的请求
                System.out.println("慢请求: " + request.getRequestURI() + " 耗时: " + duration + "ms");
            }
        }
    }

    /**
     * 性能配置属性
     */
    @ConfigurationProperties(prefix = "archive.performance")
    public static class PerformanceProperties {
        private Async async = new Async();
        private Scheduled scheduled = new Scheduled();
        private FileUpload fileUpload = new FileUpload();
        private Search search = new Search();
        private BatchOperation batchOperation = new BatchOperation();

        public Async getAsync() {
            return async;
        }

        public void setAsync(Async async) {
            this.async = async;
        }

        public Scheduled getScheduled() {
            return scheduled;
        }

        public void setScheduled(Scheduled scheduled) {
            this.scheduled = scheduled;
        }

        public FileUpload getFileUpload() {
            return fileUpload;
        }

        public void setFileUpload(FileUpload fileUpload) {
            this.fileUpload = fileUpload;
        }

        public Search getSearch() {
            return search;
        }

        public void setSearch(Search search) {
            this.search = search;
        }

        public BatchOperation getBatchOperation() {
            return batchOperation;
        }

        public void setBatchOperation(BatchOperation batchOperation) {
            this.batchOperation = batchOperation;
        }

        public static class Async {
            private int corePoolSize = 5;
            private int maxPoolSize = 20;
            private int queueCapacity = 100;
            private int keepAliveSeconds = 60;

            public int getCorePoolSize() {
                return corePoolSize;
            }

            public void setCorePoolSize(int corePoolSize) {
                this.corePoolSize = corePoolSize;
            }

            public int getMaxPoolSize() {
                return maxPoolSize;
            }

            public void setMaxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
            }

            public int getQueueCapacity() {
                return queueCapacity;
            }

            public void setQueueCapacity(int queueCapacity) {
                this.queueCapacity = queueCapacity;
            }

            public int getKeepAliveSeconds() {
                return keepAliveSeconds;
            }

            public void setKeepAliveSeconds(int keepAliveSeconds) {
                this.keepAliveSeconds = keepAliveSeconds;
            }
        }

        public static class Scheduled {
            private int poolSize = 5;

            public int getPoolSize() {
                return poolSize;
            }

            public void setPoolSize(int poolSize) {
                this.poolSize = poolSize;
            }
        }

        public static class FileUpload {
            private int corePoolSize = 3;
            private int maxPoolSize = 10;
            private int queueCapacity = 50;
            private int keepAliveSeconds = 60;

            public int getCorePoolSize() {
                return corePoolSize;
            }

            public void setCorePoolSize(int corePoolSize) {
                this.corePoolSize = corePoolSize;
            }

            public int getMaxPoolSize() {
                return maxPoolSize;
            }

            public void setMaxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
            }

            public int getQueueCapacity() {
                return queueCapacity;
            }

            public void setQueueCapacity(int queueCapacity) {
                this.queueCapacity = queueCapacity;
            }

            public int getKeepAliveSeconds() {
                return keepAliveSeconds;
            }

            public void setKeepAliveSeconds(int keepAliveSeconds) {
                this.keepAliveSeconds = keepAliveSeconds;
            }
        }

        public static class Search {
            private int corePoolSize = 5;
            private int maxPoolSize = 15;
            private int queueCapacity = 100;
            private int keepAliveSeconds = 60;

            public int getCorePoolSize() {
                return corePoolSize;
            }

            public void setCorePoolSize(int corePoolSize) {
                this.corePoolSize = corePoolSize;
            }

            public int getMaxPoolSize() {
                return maxPoolSize;
            }

            public void setMaxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
            }

            public int getQueueCapacity() {
                return queueCapacity;
            }

            public void setQueueCapacity(int queueCapacity) {
                this.queueCapacity = queueCapacity;
            }

            public int getKeepAliveSeconds() {
                return keepAliveSeconds;
            }

            public void setKeepAliveSeconds(int keepAliveSeconds) {
                this.keepAliveSeconds = keepAliveSeconds;
            }
        }

        public static class BatchOperation {
            private int corePoolSize = 3;
            private int maxPoolSize = 8;
            private int queueCapacity = 50;
            private int keepAliveSeconds = 60;

            public int getCorePoolSize() {
                return corePoolSize;
            }

            public void setCorePoolSize(int corePoolSize) {
                this.corePoolSize = corePoolSize;
            }

            public int getMaxPoolSize() {
                return maxPoolSize;
            }

            public void setMaxPoolSize(int maxPoolSize) {
                this.maxPoolSize = maxPoolSize;
            }

            public int getQueueCapacity() {
                return queueCapacity;
            }

            public void setQueueCapacity(int queueCapacity) {
                this.queueCapacity = queueCapacity;
            }

            public int getKeepAliveSeconds() {
                return keepAliveSeconds;
            }

            public void setKeepAliveSeconds(int keepAliveSeconds) {
                this.keepAliveSeconds = keepAliveSeconds;
            }
        }
    }
}
