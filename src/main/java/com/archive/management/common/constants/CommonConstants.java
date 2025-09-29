package com.archive.management.common.constants;

/**
 * 通用常量类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 系统编码
     */
    public static final String SYSTEM_ENCODING = "UTF-8";

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认页面大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大页面大小
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 未授权状态码
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 资源未找到状态码
     */
    public static final int NOT_FOUND_CODE = 404;

    /**
     * 参数错误状态码
     */
    public static final int BAD_REQUEST_CODE = 400;

    /**
     * 系统默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * 超级管理员角色编码
     */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /**
     * 管理员角色编码
     */
    public static final String ADMIN_ROLE = "ADMIN";

    /**
     * 普通用户角色编码
     */
    public static final String USER_ROLE = "USER";

    /**
     * 启用状态
     */
    public static final Integer STATUS_ENABLED = 1;

    /**
     * 禁用状态
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * 删除标记 - 已删除
     */
    public static final Integer DELETED = 1;

    /**
     * 删除标记 - 未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 根部门ID
     */
    public static final Long ROOT_DEPARTMENT_ID = 0L;

    /**
     * 系统用户ID
     */
    public static final Long SYSTEM_USER_ID = 0L;

    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "/static/images/default-avatar.png";

    /**
     * 文件上传路径
     */
    public static final String UPLOAD_PATH = "/uploads/";

    /**
     * 临时文件路径
     */
    public static final String TEMP_PATH = "/temp/";

    /**
     * 日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
}