package com.archive.management.constant;

/**
 * 系统配置相关常量
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
public class ConfigConstants {

    /**
     * 配置类型常量
     */
    public static final class Type {
        /** 字符串类型 */
        public static final Integer STRING = 1;
        /** 数字类型 */
        public static final Integer NUMBER = 2;
        /** 布尔类型 */
        public static final Integer BOOLEAN = 3;
        /** JSON类型 */
        public static final Integer JSON = 4;
        /** 数组类型 */
        public static final Integer ARRAY = 5;
        /** 文件路径类型 */
        public static final Integer FILE_PATH = 6;
        /** URL类型 */
        public static final Integer URL = 7;
        /** 邮箱类型 */
        public static final Integer EMAIL = 8;
        /** 密码类型 */
        public static final Integer PASSWORD = 9;
        /** 其他类型 */
        public static final Integer OTHER = 10;
    }

    /**
     * 配置类型描述常量
     */
    public static final class TypeDesc {
        /** 字符串类型描述 */
        public static final String STRING = "字符串";
        /** 数字类型描述 */
        public static final String NUMBER = "数字";
        /** 布尔类型描述 */
        public static final String BOOLEAN = "布尔值";
        /** JSON类型描述 */
        public static final String JSON = "JSON";
        /** 数组类型描述 */
        public static final String ARRAY = "数组";
        /** 文件路径类型描述 */
        public static final String FILE_PATH = "文件路径";
        /** URL类型描述 */
        public static final String URL = "URL";
        /** 邮箱类型描述 */
        public static final String EMAIL = "邮箱";
        /** 密码类型描述 */
        public static final String PASSWORD = "密码";
        /** 其他类型描述 */
        public static final String OTHER = "其他";
    }

    /**
     * 配置状态常量
     */
    public static final class Status {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 配置分组常量
     */
    public static final class Group {
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
        /** 界面配置 */
        public static final String UI = "ui";
        /** 业务配置 */
        public static final String BUSINESS = "business";
    }

    /**
     * 系统配置键常量
     */
    public static final class Key {
        // 系统基础配置
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
        /** 系统维护模式 */
        public static final String MAINTENANCE_MODE = "system.maintenance.mode";
        /** 维护提示信息 */
        public static final String MAINTENANCE_MESSAGE = "system.maintenance.message";

        // 安全配置
        /** 密码最小长度 */
        public static final String PASSWORD_MIN_LENGTH = "security.password.min.length";
        /** 密码最大长度 */
        public static final String PASSWORD_MAX_LENGTH = "security.password.max.length";
        /** 密码复杂度要求 */
        public static final String PASSWORD_COMPLEXITY = "security.password.complexity";
        /** 密码过期天数 */
        public static final String PASSWORD_EXPIRE_DAYS = "security.password.expire.days";
        /** 登录失败最大次数 */
        public static final String LOGIN_MAX_ATTEMPTS = "security.login.max.attempts";
        /** 账户锁定时间（分钟） */
        public static final String ACCOUNT_LOCK_TIME = "security.account.lock.time";
        /** Session超时时间（分钟） */
        public static final String SESSION_TIMEOUT = "security.session.timeout";
        /** 是否启用验证码 */
        public static final String CAPTCHA_ENABLED = "security.captcha.enabled";
        /** 验证码类型 */
        public static final String CAPTCHA_TYPE = "security.captcha.type";
        /** 是否启用双因子认证 */
        public static final String TWO_FACTOR_ENABLED = "security.two.factor.enabled";

        // 文件配置
        /** 文件上传路径 */
        public static final String FILE_UPLOAD_PATH = "file.upload.path";
        /** 文件访问URL前缀 */
        public static final String FILE_ACCESS_URL = "file.access.url";
        /** 最大文件大小（MB） */
        public static final String MAX_FILE_SIZE = "file.max.size";
        /** 允许的文件类型 */
        public static final String ALLOWED_FILE_TYPES = "file.allowed.types";
        /** 图片最大尺寸 */
        public static final String IMAGE_MAX_SIZE = "file.image.max.size";
        /** 是否启用文件压缩 */
        public static final String FILE_COMPRESS_ENABLED = "file.compress.enabled";
        /** 文件存储类型 */
        public static final String FILE_STORAGE_TYPE = "file.storage.type";

        // 缓存配置
        /** 缓存类型 */
        public static final String CACHE_TYPE = "cache.type";
        /** Redis主机 */
        public static final String REDIS_HOST = "cache.redis.host";
        /** Redis端口 */
        public static final String REDIS_PORT = "cache.redis.port";
        /** Redis密码 */
        public static final String REDIS_PASSWORD = "cache.redis.password";
        /** Redis数据库 */
        public static final String REDIS_DATABASE = "cache.redis.database";
        /** 缓存默认过期时间 */
        public static final String CACHE_DEFAULT_EXPIRE = "cache.default.expire";

        // 邮件配置
        /** SMTP服务器 */
        public static final String SMTP_HOST = "email.smtp.host";
        /** SMTP端口 */
        public static final String SMTP_PORT = "email.smtp.port";
        /** 邮箱用户名 */
        public static final String EMAIL_USERNAME = "email.username";
        /** 邮箱密码 */
        public static final String EMAIL_PASSWORD = "email.password";
        /** 发件人名称 */
        public static final String EMAIL_FROM_NAME = "email.from.name";
        /** 是否启用SSL */
        public static final String EMAIL_SSL_ENABLED = "email.ssl.enabled";

        // 业务配置
        /** 默认借阅天数 */
        public static final String DEFAULT_BORROW_DAYS = "business.borrow.default.days";
        /** 最大借阅天数 */
        public static final String MAX_BORROW_DAYS = "business.borrow.max.days";
        /** 最大借阅数量 */
        public static final String MAX_BORROW_COUNT = "business.borrow.max.count";
        /** 是否启用借阅审批 */
        public static final String BORROW_APPROVAL_ENABLED = "business.borrow.approval.enabled";
        /** 逾期提醒天数 */
        public static final String OVERDUE_REMIND_DAYS = "business.overdue.remind.days";
        /** 是否启用自动归档 */
        public static final String AUTO_ARCHIVE_ENABLED = "business.auto.archive.enabled";
        /** 档案保存年限 */
        public static final String ARCHIVE_RETENTION_YEARS = "business.archive.retention.years";

        // 界面配置
        /** 默认页面大小 */
        public static final String DEFAULT_PAGE_SIZE = "ui.page.size.default";
        /** 最大页面大小 */
        public static final String MAX_PAGE_SIZE = "ui.page.size.max";
        /** 是否显示页脚 */
        public static final String SHOW_FOOTER = "ui.show.footer";
        /** 是否显示面包屑 */
        public static final String SHOW_BREADCRUMB = "ui.show.breadcrumb";
        /** 默认主题色 */
        public static final String DEFAULT_THEME_COLOR = "ui.theme.color.default";
    }

    /**
     * 配置默认值常量
     */
    public static final class DefaultValue {
        // 系统基础配置默认值
        /** 系统名称默认值 */
        public static final String SYSTEM_NAME = "档案管理系统";
        /** 系统版本默认值 */
        public static final String SYSTEM_VERSION = "1.0.0";
        /** 默认语言 */
        public static final String DEFAULT_LANGUAGE = "zh_CN";
        /** 默认时区 */
        public static final String TIMEZONE = "Asia/Shanghai";
        /** 默认日期格式 */
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        /** 默认时间格式 */
        public static final String TIME_FORMAT = "HH:mm:ss";

        // 安全配置默认值
        /** 密码最小长度默认值 */
        public static final String PASSWORD_MIN_LENGTH = "8";
        /** 密码最大长度默认值 */
        public static final String PASSWORD_MAX_LENGTH = "20";
        /** 登录失败最大次数默认值 */
        public static final String LOGIN_MAX_ATTEMPTS = "5";
        /** 账户锁定时间默认值（分钟） */
        public static final String ACCOUNT_LOCK_TIME = "30";
        /** Session超时时间默认值（分钟） */
        public static final String SESSION_TIMEOUT = "30";

        // 文件配置默认值
        /** 最大文件大小默认值（MB） */
        public static final String MAX_FILE_SIZE = "10";
        /** 允许的文件类型默认值 */
        public static final String ALLOWED_FILE_TYPES = "jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt";

        // 业务配置默认值
        /** 默认借阅天数 */
        public static final String DEFAULT_BORROW_DAYS = "30";
        /** 最大借阅天数 */
        public static final String MAX_BORROW_DAYS = "90";
        /** 最大借阅数量 */
        public static final String MAX_BORROW_COUNT = "5";
        /** 逾期提醒天数 */
        public static final String OVERDUE_REMIND_DAYS = "3";
        /** 档案保存年限 */
        public static final String ARCHIVE_RETENTION_YEARS = "30";

        // 界面配置默认值
        /** 默认页面大小 */
        public static final String DEFAULT_PAGE_SIZE = "20";
        /** 最大页面大小 */
        public static final String MAX_PAGE_SIZE = "100";
        /** 默认主题色 */
        public static final String DEFAULT_THEME_COLOR = "#1890ff";
    }

    /**
     * 配置验证规则常量
     */
    public static final class Validation {
        /** 配置键最小长度 */
        public static final Integer KEY_MIN_LENGTH = 2;
        /** 配置键最大长度 */
        public static final Integer KEY_MAX_LENGTH = 100;
        /** 配置名称最小长度 */
        public static final Integer NAME_MIN_LENGTH = 2;
        /** 配置名称最大长度 */
        public static final Integer NAME_MAX_LENGTH = 100;
        /** 配置值最大长度 */
        public static final Integer VALUE_MAX_LENGTH = 2000;
        /** 配置描述最大长度 */
        public static final Integer DESCRIPTION_MAX_LENGTH = 500;
        /** 配置键正则表达式 */
        public static final String KEY_PATTERN = "^[a-zA-Z][a-zA-Z0-9._]*$";
    }

    /**
     * 缓存相关常量
     */
    public static final class Cache {
        /** 配置缓存前缀 */
        public static final String CONFIG_PREFIX = "config:";
        /** 配置列表缓存键 */
        public static final String CONFIG_LIST = "config:list";
        /** 配置映射缓存键 */
        public static final String CONFIG_MAP = "config:map";
        /** 配置分组缓存前缀 */
        public static final String CONFIG_GROUP_PREFIX = "config:group:";
    }

    /**
     * 配置操作类型常量
     */
    public static final class Operation {
        /** 查询配置 */
        public static final String QUERY = "QUERY";
        /** 更新配置 */
        public static final String UPDATE = "UPDATE";
        /** 重置配置 */
        public static final String RESET = "RESET";
        /** 导入配置 */
        public static final String IMPORT = "IMPORT";
        /** 导出配置 */
        public static final String EXPORT = "EXPORT";
        /** 备份配置 */
        public static final String BACKUP = "BACKUP";
        /** 恢复配置 */
        public static final String RESTORE = "RESTORE";
    }

    private ConfigConstants() {
        // 工具类，禁止实例化
    }
}