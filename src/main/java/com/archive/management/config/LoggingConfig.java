package com.archive.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * 日志配置类
 * 配置应用程序的日志记录策略和格式
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@ConfigurationProperties(prefix = "logging")
public class LoggingConfig {

    @Autowired
    private Environment environment;

    /**
     * 日志级别
     */
    private String level = "INFO";

    /**
     * 日志文件路径
     */
    private String filePath = "logs/archive-management.log";

    /**
     * 日志文件最大大小
     */
    private String maxFileSize = "10MB";

    /**
     * 日志文件保留天数
     */
    private int maxHistory = 30;

    /**
     * 控制台日志模式
     */
    private String consolePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";

    /**
     * 文件日志模式
     */
    private String filePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";

    /**
     * 是否启用文件日志
     */
    private boolean enableFileLogging = true;

    /**
     * 是否启用控制台日志
     */
    private boolean enableConsoleLogging = true;

    /**
     * 根日志记录器名称
     */
    private String rootLoggerName = "com.archive.management";

    /**
     * 初始化日志配置
     */
    @PostConstruct
    public void initLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // 配置根日志记录器
        configureRootLogger(loggerContext);
        
        // 配置应用程序日志记录器
        configureApplicationLogger(loggerContext);
        
        // 配置第三方库日志级别
        configureThirdPartyLoggers(loggerContext);
        
        // 根据环境配置特定设置
        configureEnvironmentSpecificSettings(loggerContext);
    }

    /**
     * 配置根日志记录器
     * 
     * @param loggerContext 日志上下文
     */
    private void configureRootLogger(LoggerContext loggerContext) {
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(ch.qos.logback.classic.Level.valueOf(level.toUpperCase()));
        
        // 清除现有的appender
        rootLogger.detachAndStopAllAppenders();
        
        // 添加控制台appender
        if (enableConsoleLogging) {
            ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
                createConsoleAppender(loggerContext);
            rootLogger.addAppender(consoleAppender);
        }
        
        // 添加文件appender
        if (enableFileLogging) {
            RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = 
                createFileAppender(loggerContext);
            rootLogger.addAppender(fileAppender);
        }
    }

    /**
     * 配置应用程序日志记录器
     * 
     * @param loggerContext 日志上下文
     */
    private void configureApplicationLogger(LoggerContext loggerContext) {
        Logger appLogger = loggerContext.getLogger(rootLoggerName);
        appLogger.setLevel(ch.qos.logback.classic.Level.valueOf(level.toUpperCase()));
        appLogger.setAdditive(false);
        
        // 添加控制台appender
        if (enableConsoleLogging) {
            ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
                createConsoleAppender(loggerContext);
            appLogger.addAppender(consoleAppender);
        }
        
        // 添加文件appender
        if (enableFileLogging) {
            RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = 
                createFileAppender(loggerContext);
            appLogger.addAppender(fileAppender);
        }
    }

    /**
     * 创建控制台appender
     * 
     * @param loggerContext 日志上下文
     * @return ConsoleAppender
     */
    private ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> createConsoleAppender(
            LoggerContext loggerContext) {
        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> consoleAppender = 
            new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(consolePattern);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        return consoleAppender;
    }

    /**
     * 创建文件appender
     * 
     * @param loggerContext 日志上下文
     * @return RollingFileAppender
     */
    private RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> createFileAppender(
            LoggerContext loggerContext) {
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> fileAppender = 
            new RollingFileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(filePath);
        
        // 配置滚动策略
        SizeAndTimeBasedRollingPolicy<ch.qos.logback.classic.spi.ILoggingEvent> rollingPolicy = 
            new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(filePath + ".%d{yyyy-MM-dd}.%i.gz");
        rollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
        rollingPolicy.setMaxHistory(maxHistory);
        rollingPolicy.setTotalSizeCap(FileSize.valueOf("1GB"));
        rollingPolicy.start();
        
        fileAppender.setRollingPolicy(rollingPolicy);
        
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(filePattern);
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        
        return fileAppender;
    }

    /**
     * 配置第三方库日志级别
     * 
     * @param loggerContext 日志上下文
     */
    private void configureThirdPartyLoggers(LoggerContext loggerContext) {
        // Spring框架日志
        loggerContext.getLogger("org.springframework").setLevel(ch.qos.logback.classic.Level.WARN);
        loggerContext.getLogger("org.springframework.web").setLevel(ch.qos.logback.classic.Level.INFO);
        loggerContext.getLogger("org.springframework.security").setLevel(ch.qos.logback.classic.Level.INFO);
        
        // Hibernate日志
        loggerContext.getLogger("org.hibernate").setLevel(ch.qos.logback.classic.Level.WARN);
        loggerContext.getLogger("org.hibernate.SQL").setLevel(ch.qos.logback.classic.Level.DEBUG);
        loggerContext.getLogger("org.hibernate.type.descriptor.sql.BasicBinder")
            .setLevel(ch.qos.logback.classic.Level.TRACE);
        
        // MyBatis日志
        loggerContext.getLogger("org.mybatis").setLevel(ch.qos.logback.classic.Level.WARN);
        
        // Apache Commons日志
        loggerContext.getLogger("org.apache.commons").setLevel(ch.qos.logback.classic.Level.WARN);
        
        // Tomcat日志
        loggerContext.getLogger("org.apache.catalina").setLevel(ch.qos.logback.classic.Level.WARN);
        loggerContext.getLogger("org.apache.coyote").setLevel(ch.qos.logback.classic.Level.WARN);
        
        // Netty日志
        loggerContext.getLogger("io.netty").setLevel(ch.qos.logback.classic.Level.WARN);
        
        // Jackson日志
        loggerContext.getLogger("com.fasterxml.jackson").setLevel(ch.qos.logback.classic.Level.WARN);
    }

    /**
     * 根据环境配置特定设置
     * 
     * @param loggerContext 日志上下文
     */
    private void configureEnvironmentSpecificSettings(LoggerContext loggerContext) {
        String[] activeProfiles = environment.getActiveProfiles();
        
        for (String profile : activeProfiles) {
            switch (profile.toLowerCase()) {
                case "dev":
                case "development":
                    configureDevelopmentLogging(loggerContext);
                    break;
                case "test":
                case "testing":
                    configureTestLogging(loggerContext);
                    break;
                case "prod":
                case "production":
                    configureProductionLogging(loggerContext);
                    break;
                default:
                    // 默认配置已在上面设置
                    break;
            }
        }
    }

    /**
     * 配置开发环境日志
     * 
     * @param loggerContext 日志上下文
     */
    private void configureDevelopmentLogging(LoggerContext loggerContext) {
        // 开发环境启用DEBUG级别
        Logger appLogger = loggerContext.getLogger(rootLoggerName);
        appLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        
        // 启用SQL日志
        loggerContext.getLogger("org.hibernate.SQL").setLevel(ch.qos.logback.classic.Level.DEBUG);
        loggerContext.getLogger("org.hibernate.type.descriptor.sql.BasicBinder")
            .setLevel(ch.qos.logback.classic.Level.TRACE);
    }

    /**
     * 配置测试环境日志
     * 
     * @param loggerContext 日志上下文
     */
    private void configureTestLogging(LoggerContext loggerContext) {
        // 测试环境使用INFO级别，减少日志输出
        Logger appLogger = loggerContext.getLogger(rootLoggerName);
        appLogger.setLevel(ch.qos.logback.classic.Level.INFO);
        
        // 禁用文件日志，只使用控制台
        this.enableFileLogging = false;
    }

    /**
     * 配置生产环境日志
     * 
     * @param loggerContext 日志上下文
     */
    private void configureProductionLogging(LoggerContext loggerContext) {
        // 生产环境使用WARN级别，减少日志量
        Logger appLogger = loggerContext.getLogger(rootLoggerName);
        appLogger.setLevel(ch.qos.logback.classic.Level.WARN);
        
        // 禁用控制台日志，只使用文件
        this.enableConsoleLogging = false;
        
        // 增加文件保留天数
        this.maxHistory = 90;
    }

    // Getter 和 Setter 方法

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public String getConsolePattern() {
        return consolePattern;
    }

    public void setConsolePattern(String consolePattern) {
        this.consolePattern = consolePattern;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public boolean isEnableFileLogging() {
        return enableFileLogging;
    }

    public void setEnableFileLogging(boolean enableFileLogging) {
        this.enableFileLogging = enableFileLogging;
    }

    public boolean isEnableConsoleLogging() {
        return enableConsoleLogging;
    }

    public void setEnableConsoleLogging(boolean enableConsoleLogging) {
        this.enableConsoleLogging = enableConsoleLogging;
    }

    public String getRootLoggerName() {
        return rootLoggerName;
    }

    public void setRootLoggerName(String rootLoggerName) {
        this.rootLoggerName = rootLoggerName;
    }
}