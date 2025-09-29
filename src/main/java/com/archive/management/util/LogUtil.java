package com.archive.management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类
 * 提供统一的日志记录功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class LogUtil {

    /** 默认日志记录器 */
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LogUtil.class);
    
    /** 日志级别 */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
    
    /** 业务类型 */
    public enum BusinessType {
        LOGIN("登录"),
        LOGOUT("登出"),
        CREATE("新增"),
        UPDATE("修改"),
        DELETE("删除"),
        QUERY("查询"),
        EXPORT("导出"),
        IMPORT("导入"),
        UPLOAD("上传"),
        DOWNLOAD("下载"),
        BACKUP("备份"),
        RESTORE("恢复"),
        SYNC("同步"),
        AUDIT("审计"),
        OTHER("其他");
        
        private final String description;
        
        BusinessType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }

    // ========== MDC 常量 ==========
    
    /** 请求ID */
    public static final String REQUEST_ID = "requestId";
    
    /** 用户ID */
    public static final String USER_ID = "userId";
    
    /** 用户名 */
    public static final String USERNAME = "username";
    
    /** IP地址 */
    public static final String IP_ADDRESS = "ipAddress";
    
    /** 业务类型 */
    public static final String BUSINESS_TYPE = "businessType";
    
    /** 模块名称 */
    public static final String MODULE_NAME = "moduleName";
    
    /** 方法名称 */
    public static final String METHOD_NAME = "methodName";
    
    /** 执行时间 */
    public static final String EXECUTION_TIME = "executionTime";

    // ========== 基础日志方法 ==========

    /**
     * 记录TRACE级别日志
     * 
     * @param message 消息
     */
    public static void trace(String message) {
        DEFAULT_LOGGER.trace(message);
    }

    /**
     * 记录TRACE级别日志
     * 
     * @param message 消息
     * @param args 参数
     */
    public static void trace(String message, Object... args) {
        DEFAULT_LOGGER.trace(message, args);
    }

    /**
     * 记录DEBUG级别日志
     * 
     * @param message 消息
     */
    public static void debug(String message) {
        DEFAULT_LOGGER.debug(message);
    }

    /**
     * 记录DEBUG级别日志
     * 
     * @param message 消息
     * @param args 参数
     */
    public static void debug(String message, Object... args) {
        DEFAULT_LOGGER.debug(message, args);
    }

    /**
     * 记录INFO级别日志
     * 
     * @param message 消息
     */
    public static void info(String message) {
        DEFAULT_LOGGER.info(message);
    }

    /**
     * 记录INFO级别日志
     * 
     * @param message 消息
     * @param args 参数
     */
    public static void info(String message, Object... args) {
        DEFAULT_LOGGER.info(message, args);
    }

    /**
     * 记录WARN级别日志
     * 
     * @param message 消息
     */
    public static void warn(String message) {
        DEFAULT_LOGGER.warn(message);
    }

    /**
     * 记录WARN级别日志
     * 
     * @param message 消息
     * @param args 参数
     */
    public static void warn(String message, Object... args) {
        DEFAULT_LOGGER.warn(message, args);
    }

    /**
     * 记录WARN级别日志
     * 
     * @param message 消息
     * @param throwable 异常
     */
    public static void warn(String message, Throwable throwable) {
        DEFAULT_LOGGER.warn(message, throwable);
    }

    /**
     * 记录ERROR级别日志
     * 
     * @param message 消息
     */
    public static void error(String message) {
        DEFAULT_LOGGER.error(message);
    }

    /**
     * 记录ERROR级别日志
     * 
     * @param message 消息
     * @param args 参数
     */
    public static void error(String message, Object... args) {
        DEFAULT_LOGGER.error(message, args);
    }

    /**
     * 记录ERROR级别日志
     * 
     * @param message 消息
     * @param throwable 异常
     */
    public static void error(String message, Throwable throwable) {
        DEFAULT_LOGGER.error(message, throwable);
    }

    // ========== 指定Logger的日志方法 ==========

    /**
     * 使用指定Logger记录日志
     * 
     * @param logger 日志记录器
     * @param level 日志级别
     * @param message 消息
     * @param args 参数
     */
    public static void log(Logger logger, LogLevel level, String message, Object... args) {
        switch (level) {
            case TRACE:
                logger.trace(message, args);
                break;
            case DEBUG:
                logger.debug(message, args);
                break;
            case INFO:
                logger.info(message, args);
                break;
            case WARN:
                logger.warn(message, args);
                break;
            case ERROR:
                logger.error(message, args);
                break;
        }
    }

    /**
     * 使用指定Logger记录异常日志
     * 
     * @param logger 日志记录器
     * @param level 日志级别
     * @param message 消息
     * @param throwable 异常
     */
    public static void log(Logger logger, LogLevel level, String message, Throwable throwable) {
        switch (level) {
            case TRACE:
                logger.trace(message, throwable);
                break;
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
        }
    }

    // ========== MDC 操作方法 ==========

    /**
     * 设置请求ID
     * 
     * @param requestId 请求ID
     */
    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }

    /**
     * 生成并设置请求ID
     * 
     * @return 请求ID
     */
    public static String generateAndSetRequestId() {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        setRequestId(requestId);
        return requestId;
    }

    /**
     * 获取请求ID
     * 
     * @return 请求ID
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * 设置用户信息
     * 
     * @param userId 用户ID
     * @param username 用户名
     */
    public static void setUserInfo(String userId, String username) {
        MDC.put(USER_ID, userId);
        MDC.put(USERNAME, username);
    }

    /**
     * 设置IP地址
     * 
     * @param ipAddress IP地址
     */
    public static void setIpAddress(String ipAddress) {
        MDC.put(IP_ADDRESS, ipAddress);
    }

    /**
     * 设置业务类型
     * 
     * @param businessType 业务类型
     */
    public static void setBusinessType(BusinessType businessType) {
        MDC.put(BUSINESS_TYPE, businessType.getDescription());
    }

    /**
     * 设置模块信息
     * 
     * @param moduleName 模块名称
     * @param methodName 方法名称
     */
    public static void setModuleInfo(String moduleName, String methodName) {
        MDC.put(MODULE_NAME, moduleName);
        MDC.put(METHOD_NAME, methodName);
    }

    /**
     * 设置执行时间
     * 
     * @param executionTime 执行时间（毫秒）
     */
    public static void setExecutionTime(long executionTime) {
        MDC.put(EXECUTION_TIME, String.valueOf(executionTime));
    }

    /**
     * 设置MDC属性
     * 
     * @param key 键
     * @param value 值
     */
    public static void setMDC(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * 获取MDC属性
     * 
     * @param key 键
     * @return 值
     */
    public static String getMDC(String key) {
        return MDC.get(key);
    }

    /**
     * 移除MDC属性
     * 
     * @param key 键
     */
    public static void removeMDC(String key) {
        MDC.remove(key);
    }

    /**
     * 清空MDC
     */
    public static void clearMDC() {
        MDC.clear();
    }

    /**
     * 获取MDC副本
     * 
     * @return MDC副本
     */
    public static Map<String, String> getMDCCopy() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 设置MDC上下文
     * 
     * @param contextMap 上下文映射
     */
    public static void setMDCContext(Map<String, String> contextMap) {
        MDC.setContextMap(contextMap);
    }

    // ========== 业务日志方法 ==========

    /**
     * 记录业务操作日志
     * 
     * @param businessType 业务类型
     * @param moduleName 模块名称
     * @param methodName 方法名称
     * @param message 消息
     */
    public static void business(BusinessType businessType, String moduleName, 
                              String methodName, String message) {
        setBusinessType(businessType);
        setModuleInfo(moduleName, methodName);
        info("业务操作: {}", message);
    }

    /**
     * 记录业务操作日志（带参数）
     * 
     * @param businessType 业务类型
     * @param moduleName 模块名称
     * @param methodName 方法名称
     * @param message 消息
     * @param args 参数
     */
    public static void business(BusinessType businessType, String moduleName, 
                              String methodName, String message, Object... args) {
        setBusinessType(businessType);
        setModuleInfo(moduleName, methodName);
        info("业务操作: " + message, args);
    }

    /**
     * 记录登录日志
     * 
     * @param username 用户名
     * @param ipAddress IP地址
     * @param success 是否成功
     * @param message 消息
     */
    public static void login(String username, String ipAddress, boolean success, String message) {
        setUserInfo(null, username);
        setIpAddress(ipAddress);
        setBusinessType(BusinessType.LOGIN);
        
        if (success) {
            info("用户登录成功: {}, IP: {}, 详情: {}", username, ipAddress, message);
        } else {
            warn("用户登录失败: {}, IP: {}, 详情: {}", username, ipAddress, message);
        }
    }

    /**
     * 记录登出日志
     * 
     * @param username 用户名
     * @param ipAddress IP地址
     */
    public static void logout(String username, String ipAddress) {
        setUserInfo(null, username);
        setIpAddress(ipAddress);
        setBusinessType(BusinessType.LOGOUT);
        info("用户登出: {}, IP: {}", username, ipAddress);
    }

    /**
     * 记录操作日志
     * 
     * @param businessType 业务类型
     * @param target 操作目标
     * @param result 操作结果
     */
    public static void operation(BusinessType businessType, String target, String result) {
        setBusinessType(businessType);
        info("操作记录: {} - {}, 结果: {}", businessType.getDescription(), target, result);
    }

    /**
     * 记录异常日志
     * 
     * @param moduleName 模块名称
     * @param methodName 方法名称
     * @param throwable 异常
     */
    public static void exception(String moduleName, String methodName, Throwable throwable) {
        setModuleInfo(moduleName, methodName);
        error("系统异常: 模块[{}] 方法[{}]", moduleName, methodName, throwable);
    }

    /**
     * 记录性能日志
     * 
     * @param moduleName 模块名称
     * @param methodName 方法名称
     * @param executionTime 执行时间（毫秒）
     */
    public static void performance(String moduleName, String methodName, long executionTime) {
        setModuleInfo(moduleName, methodName);
        setExecutionTime(executionTime);
        
        if (executionTime > 5000) {
            warn("性能警告: 模块[{}] 方法[{}] 执行时间: {}ms", moduleName, methodName, executionTime);
        } else if (executionTime > 1000) {
            info("性能监控: 模块[{}] 方法[{}] 执行时间: {}ms", moduleName, methodName, executionTime);
        } else {
            debug("性能监控: 模块[{}] 方法[{}] 执行时间: {}ms", moduleName, methodName, executionTime);
        }
    }

    // ========== 日志构建器 ==========

    /**
     * 日志构建器
     */
    public static class LogBuilder {
        private final Logger logger;
        private final LogLevel level;
        private final Map<String, String> mdcContext;
        private String message;
        private Object[] args;
        private Throwable throwable;

        public LogBuilder(Logger logger, LogLevel level) {
            this.logger = logger;
            this.level = level;
            this.mdcContext = new HashMap<>();
        }

        public LogBuilder message(String message) {
            this.message = message;
            return this;
        }

        public LogBuilder args(Object... args) {
            this.args = args;
            return this;
        }

        public LogBuilder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public LogBuilder requestId(String requestId) {
            this.mdcContext.put(REQUEST_ID, requestId);
            return this;
        }

        public LogBuilder userInfo(String userId, String username) {
            this.mdcContext.put(USER_ID, userId);
            this.mdcContext.put(USERNAME, username);
            return this;
        }

        public LogBuilder ipAddress(String ipAddress) {
            this.mdcContext.put(IP_ADDRESS, ipAddress);
            return this;
        }

        public LogBuilder businessType(BusinessType businessType) {
            this.mdcContext.put(BUSINESS_TYPE, businessType.getDescription());
            return this;
        }

        public LogBuilder moduleInfo(String moduleName, String methodName) {
            this.mdcContext.put(MODULE_NAME, moduleName);
            this.mdcContext.put(METHOD_NAME, methodName);
            return this;
        }

        public LogBuilder mdc(String key, String value) {
            this.mdcContext.put(key, value);
            return this;
        }

        public void log() {
            // 保存当前MDC
            Map<String, String> originalMDC = getMDCCopy();
            
            try {
                // 设置新的MDC
                for (Map.Entry<String, String> entry : mdcContext.entrySet()) {
                    MDC.put(entry.getKey(), entry.getValue());
                }
                
                // 记录日志
                if (throwable != null) {
                    LogUtil.log(logger, level, message, throwable);
                } else if (args != null && args.length > 0) {
                    LogUtil.log(logger, level, message, args);
                } else {
                    LogUtil.log(logger, level, message);
                }
            } finally {
                // 恢复原始MDC
                MDC.clear();
                if (originalMDC != null) {
                    setMDCContext(originalMDC);
                }
            }
        }
    }

    /**
     * 创建日志构建器
     * 
     * @param logger 日志记录器
     * @param level 日志级别
     * @return 日志构建器
     */
    public static LogBuilder builder(Logger logger, LogLevel level) {
        return new LogBuilder(logger, level);
    }

    /**
     * 创建日志构建器（使用默认Logger）
     * 
     * @param level 日志级别
     * @return 日志构建器
     */
    public static LogBuilder builder(LogLevel level) {
        return new LogBuilder(DEFAULT_LOGGER, level);
    }

    // ========== 工具方法 ==========

    /**
     * 获取Logger
     * 
     * @param clazz 类
     * @return Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 获取Logger
     * 
     * @param name 名称
     * @return Logger
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * 格式化当前时间
     * 
     * @return 格式化的时间字符串
     */
    public static String formatCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    /**
     * 获取调用者信息
     * 
     * @return 调用者信息
     */
    public static String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return caller.getClassName() + "." + caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")";
        }
        return "Unknown";
    }

    /**
     * 检查日志级别是否启用
     * 
     * @param logger 日志记录器
     * @param level 日志级别
     * @return 是否启用
     */
    public static boolean isEnabled(Logger logger, LogLevel level) {
        switch (level) {
            case TRACE:
                return logger.isTraceEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            default:
                return false;
        }
    }
}