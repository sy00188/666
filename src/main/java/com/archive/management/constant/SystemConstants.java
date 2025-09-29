package com.archive.management.constant;

/**
 * 系统相关常量
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
public class SystemConstants {

    /**
     * 系统配置类型常量
     */
    public static final class ConfigType {
        /** 字符串 */
        public static final Integer STRING = 1;
        /** 数字 */
        public static final Integer NUMBER = 2;
        /** 布尔值 */
        public static final Integer BOOLEAN = 3;
        /** JSON */
        public static final Integer JSON = 4;
        /** 数组 */
        public static final Integer ARRAY = 5;
        /** 文件路径 */
        public static final Integer FILE_PATH = 6;
        /** URL */
        public static final Integer URL = 7;
        /** 邮箱 */
        public static final Integer EMAIL = 8;
        /** 密码 */
        public static final Integer PASSWORD = 9;
        /** 其他 */
        public static final Integer OTHER = 10;
    }

    /**
     * 系统配置状态常量
     */
    public static final class ConfigStatus {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 系统配置分组常量
     */
    public static final class ConfigGroup {
        /** 系统基础配置 */
        public static final String SYSTEM_BASIC = "system.basic";
        /** 安全配置 */
        public static final String SECURITY = "security";
        /** 文件配置 */
        public static final String FILE = "file";
        /** 缓存配置 */
        public static final String CACHE = "cache";
        /** 数据库配置 */
        public static final String DATABASE = "database";
        /** 邮件配置 */
        public static final String EMAIL = "email";
        /** 短信配置 */
        public static final String SMS = "sms";
        /** 日志配置 */
        public static final String LOG = "log";
        /** 监控配置 */
        public static final String MONITOR = "monitor";
        /** 备份配置 */
        public static final String BACKUP = "backup";
    }

    /**
     * 布尔值标识常量
     */
    public static final class BooleanFlag {
        /** 否/假 */
        public static final Integer FALSE = 0;
        /** 是/真 */
        public static final Integer TRUE = 1;
    }

    /**
     * 系统环境常量
     */
    public static final class Environment {
        /** 开发环境 */
        public static final String DEV = "dev";
        /** 测试环境 */
        public static final String TEST = "test";
        /** 预生产环境 */
        public static final String STAGING = "staging";
        /** 生产环境 */
        public static final String PROD = "prod";
    }

    /**
     * 数据类型常量
     */
    public static final class DataType {
        /** 字符串 */
        public static final String STRING = "string";
        /** 整数 */
        public static final String INT = "int";
        /** 长整数 */
        public static final String LONG = "long";
        /** 双精度浮点数 */
        public static final String DOUBLE = "double";
        /** 布尔值 */
        public static final String BOOLEAN = "boolean";
        /** JSON对象 */
        public static final String JSON = "json";
        /** 数组 */
        public static final String ARRAY = "array";
    }

    /**
     * 系统监控状态常量
     */
    public static final class MonitorStatus {
        /** 正常 */
        public static final String NORMAL = "NORMAL";
        /** 警告 */
        public static final String WARNING = "WARNING";
        /** 错误 */
        public static final String ERROR = "ERROR";
        /** 未知 */
        public static final String UNKNOWN = "UNKNOWN";
    }

    /**
     * 备份类型常量
     */
    public static final class BackupType {
        /** 全量备份 */
        public static final Integer FULL = 1;
        /** 增量备份 */
        public static final Integer INCREMENTAL = 2;
        /** 差异备份 */
        public static final Integer DIFFERENTIAL = 3;
    }

    /**
     * 备份状态常量
     */
    public static final class BackupStatus {
        /** 备份中 */
        public static final Integer BACKING_UP = 0;
        /** 备份成功 */
        public static final Integer SUCCESS = 1;
        /** 备份失败 */
        public static final Integer FAILED = 2;
        /** 已取消 */
        public static final Integer CANCELLED = 3;
    }

    /**
     * 日志级别常量
     */
    public static final class LogLevel {
        /** 调试 */
        public static final String DEBUG = "DEBUG";
        /** 信息 */
        public static final String INFO = "INFO";
        /** 警告 */
        public static final String WARN = "WARN";
        /** 错误 */
        public static final String ERROR = "ERROR";
    }

    /**
     * 系统默认配置键常量
     */
    public static final class DefaultConfigKey {
        /** 系统名称 */
        public static final String SYSTEM_NAME = "system.name";
        /** 系统版本 */
        public static final String SYSTEM_VERSION = "system.version";
        /** 系统描述 */
        public static final String SYSTEM_DESCRIPTION = "system.description";
        /** 系统Logo */
        public static final String SYSTEM_LOGO = "system.logo";
        /** 系统主题 */
        public static final String SYSTEM_THEME = "system.theme";
        /** 默认语言 */
        public static final String DEFAULT_LANGUAGE = "system.language.default";
        /** 时区 */
        public static final String TIMEZONE = "system.timezone";
        /** 日期格式 */
        public static final String DATE_FORMAT = "system.date.format";
        /** 时间格式 */
        public static final String TIME_FORMAT = "system.time.format";
        /** 文件上传路径 */
        public static final String FILE_UPLOAD_PATH = "file.upload.path";
        /** 文件访问URL前缀 */
        public static final String FILE_ACCESS_URL = "file.access.url";
        /** 最大文件大小 */
        public static final String MAX_FILE_SIZE = "file.max.size";
        /** 允许的文件类型 */
        public static final String ALLOWED_FILE_TYPES = "file.allowed.types";
        /** 密码最小长度 */
        public static final String PASSWORD_MIN_LENGTH = "security.password.min.length";
        /** 密码复杂度要求 */
        public static final String PASSWORD_COMPLEXITY = "security.password.complexity";
        /** 登录失败最大次数 */
        public static final String LOGIN_MAX_ATTEMPTS = "security.login.max.attempts";
        /** 账户锁定时间 */
        public static final String ACCOUNT_LOCK_TIME = "security.account.lock.time";
        /** Session超时时间 */
        public static final String SESSION_TIMEOUT = "security.session.timeout";
    }

    /**
     * HTTP状态码常量
     */
    public static final class HttpStatus {
        /** 成功 */
        public static final Integer SUCCESS = 200;
        /** 未授权 */
        public static final Integer UNAUTHORIZED = 401;
        /** 禁止访问 */
        public static final Integer FORBIDDEN = 403;
        /** 未找到 */
        public static final Integer NOT_FOUND = 404;
        /** 服务器内部错误 */
        public static final Integer INTERNAL_SERVER_ERROR = 500;
    }

    /**
     * 响应消息常量
     */
    public static final class ResponseMessage {
        /** 操作成功 */
        public static final String SUCCESS = "操作成功";
        /** 操作失败 */
        public static final String FAILED = "操作失败";
        /** 参数错误 */
        public static final String PARAM_ERROR = "参数错误";
        /** 数据不存在 */
        public static final String DATA_NOT_FOUND = "数据不存在";
        /** 权限不足 */
        public static final String PERMISSION_DENIED = "权限不足";
        /** 系统错误 */
        public static final String SYSTEM_ERROR = "系统错误";
    }

    private SystemConstants() {
        // 工具类，禁止实例化
    }
}