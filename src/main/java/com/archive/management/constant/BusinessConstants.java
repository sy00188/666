package com.archive.management.constant;

/**
 * 业务相关常量
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
public class BusinessConstants {

    /**
     * 档案状态常量
     */
    public static final class ArchiveStatus {
        /** 草稿 */
        public static final Integer DRAFT = 0;
        /** 正常 */
        public static final Integer NORMAL = 1;
        /** 借出 */
        public static final Integer BORROWED = 2;
        /** 归档 */
        public static final Integer ARCHIVED = 3;
        /** 销毁 */
        public static final Integer DESTROYED = 4;
    }

    /**
     * 借阅状态常量
     */
    public static final class BorrowStatus {
        /** 申请中 */
        public static final Integer APPLYING = 0;
        /** 已批准 */
        public static final Integer APPROVED = 1;
        /** 借出中 */
        public static final Integer BORROWED = 2;
        /** 已归还 */
        public static final Integer RETURNED = 3;
        /** 逾期 */
        public static final Integer OVERDUE = 4;
        /** 已拒绝 */
        public static final Integer REJECTED = 5;
    }

    /**
     * 审批状态常量
     */
    public static final class ApprovalStatus {
        /** 待审批 */
        public static final Integer PENDING = 0;
        /** 已通过 */
        public static final Integer APPROVED = 1;
        /** 已拒绝 */
        public static final Integer REJECTED = 2;
        /** 已撤回 */
        public static final Integer WITHDRAWN = 3;
    }

    /**
     * 文件类型常量
     */
    public static final class FileType {
        /** 文档 */
        public static final Integer DOCUMENT = 1;
        /** 图片 */
        public static final Integer IMAGE = 2;
        /** 视频 */
        public static final Integer VIDEO = 3;
        /** 音频 */
        public static final Integer AUDIO = 4;
        /** 其他 */
        public static final Integer OTHER = 5;
    }

    /**
     * 文件状态常量
     */
    public static final class FileStatus {
        /** 上传中 */
        public static final Integer UPLOADING = 0;
        /** 正常 */
        public static final Integer NORMAL = 1;
        /** 损坏 */
        public static final Integer CORRUPTED = 2;
        /** 已删除 */
        public static final Integer DELETED = 3;
    }

    /**
     * 操作类型常量
     */
    public static final class OperationType {
        /** 创建 */
        public static final String CREATE = "CREATE";
        /** 更新 */
        public static final String UPDATE = "UPDATE";
        /** 删除 */
        public static final String DELETE = "DELETE";
        /** 查询 */
        public static final String SELECT = "SELECT";
        /** 导入 */
        public static final String IMPORT = "IMPORT";
        /** 导出 */
        public static final String EXPORT = "EXPORT";
        /** 登录 */
        public static final String LOGIN = "LOGIN";
        /** 登出 */
        public static final String LOGOUT = "LOGOUT";
        /** 借阅 */
        public static final String BORROW = "BORROW";
        /** 归还 */
        public static final String RETURN = "RETURN";
        /** 审批 */
        public static final String APPROVE = "APPROVE";
    }

    /**
     * 通知类型常量
     */
    public static final class NotificationType {
        /** 系统通知 */
        public static final Integer SYSTEM = 1;
        /** 业务通知 */
        public static final Integer BUSINESS = 2;
        /** 警告通知 */
        public static final Integer WARNING = 3;
        /** 错误通知 */
        public static final Integer ERROR = 4;
    }

    /**
     * 通知状态常量
     */
    public static final class NotificationStatus {
        /** 未读 */
        public static final Integer UNREAD = 0;
        /** 已读 */
        public static final Integer READ = 1;
    }

    /**
     * 默认值常量
     */
    public static final class DefaultValue {
        /** 默认页码 */
        public static final Integer DEFAULT_PAGE_NUM = 1;
        /** 默认页面大小 */
        public static final Integer DEFAULT_PAGE_SIZE = 10;
        /** 最大页面大小 */
        public static final Integer MAX_PAGE_SIZE = 1000;
        /** 默认排序字段 */
        public static final String DEFAULT_SORT_FIELD = "createTime";
        /** 默认排序方向 */
        public static final String DEFAULT_SORT_ORDER = "desc";
        /** 默认借阅天数 */
        public static final Integer DEFAULT_BORROW_DAYS = 30;
        /** 最大借阅天数 */
        public static final Integer MAX_BORROW_DAYS = 365;
    }

    /**
     * 文件大小限制常量（字节）
     */
    public static final class FileSize {
        /** 1KB */
        public static final Long KB = 1024L;
        /** 1MB */
        public static final Long MB = 1024 * KB;
        /** 1GB */
        public static final Long GB = 1024 * MB;
        /** 默认文件大小限制 100MB */
        public static final Long DEFAULT_MAX_SIZE = 100 * MB;
        /** 图片文件大小限制 10MB */
        public static final Long IMAGE_MAX_SIZE = 10 * MB;
        /** 文档文件大小限制 50MB */
        public static final Long DOCUMENT_MAX_SIZE = 50 * MB;
    }

    /**
     * 正则表达式常量
     */
    public static final class Regex {
        /** 手机号正则 */
        public static final String MOBILE = "^1[3-9]\\d{9}$";
        /** 邮箱正则 */
        public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        /** 身份证号正则 */
        public static final String ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        /** 档案编号正则 */
        public static final String ARCHIVE_CODE = "^[A-Z]{2}\\d{4}-\\d{6}$";
    }

    /**
     * 缓存过期时间常量（秒）
     */
    public static final class CacheExpire {
        /** 5分钟 */
        public static final Long FIVE_MINUTES = 5 * 60L;
        /** 30分钟 */
        public static final Long THIRTY_MINUTES = 30 * 60L;
        /** 1小时 */
        public static final Long ONE_HOUR = 60 * 60L;
        /** 1天 */
        public static final Long ONE_DAY = 24 * 60 * 60L;
        /** 7天 */
        public static final Long ONE_WEEK = 7 * 24 * 60 * 60L;
    }

    private BusinessConstants() {
        // 工具类，禁止实例化
    }
}