package com.archive.management.constant;

/**
 * 权限系统相关常量
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
public class PermissionConstants {

    /**
     * 权限类型常量
     */
    public static final class Type {
        /** 菜单权限 */
        public static final Integer MENU = 1;
        /** 按钮权限 */
        public static final Integer BUTTON = 2;
        /** 接口权限 */
        public static final Integer API = 3;
        /** 数据权限 */
        public static final Integer DATA = 4;
    }

    /**
     * 权限类型描述常量
     */
    public static final class TypeDesc {
        /** 菜单权限描述 */
        public static final String MENU = "菜单权限";
        /** 按钮权限描述 */
        public static final String BUTTON = "按钮权限";
        /** 接口权限描述 */
        public static final String API = "接口权限";
        /** 数据权限描述 */
        public static final String DATA = "数据权限";
    }

    /**
     * 权限状态常量
     */
    public static final class Status {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 权限级别常量
     */
    public static final class Level {
        /** 根级别 */
        public static final Integer ROOT = 0;
        /** 一级菜单 */
        public static final Integer LEVEL_1 = 1;
        /** 二级菜单 */
        public static final Integer LEVEL_2 = 2;
        /** 三级菜单 */
        public static final Integer LEVEL_3 = 3;
        /** 四级菜单 */
        public static final Integer LEVEL_4 = 4;
        /** 五级菜单 */
        public static final Integer LEVEL_5 = 5;
    }

    /**
     * 根权限父ID
     */
    public static final Long ROOT_PARENT_ID = 0L;

    /**
     * 权限代码分隔符
     */
    public static final String CODE_SEPARATOR = ":";

    /**
     * 权限路径分隔符
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * 默认权限级别
     */
    public static final Integer DEFAULT_LEVEL = 1;

    /**
     * 系统内置权限代码前缀
     */
    public static final String SYSTEM_PREFIX = "system";

    /**
     * 业务权限代码前缀
     */
    public static final String BUSINESS_PREFIX = "business";

    /**
     * 权限代码常量
     */
    public static final class Code {
        // 系统管理权限
        /** 系统管理 */
        public static final String SYSTEM_MANAGE = "system:manage";
        /** 用户管理 */
        public static final String USER_MANAGE = "system:user:manage";
        /** 用户查询 */
        public static final String USER_QUERY = "system:user:query";
        /** 用户新增 */
        public static final String USER_ADD = "system:user:add";
        /** 用户编辑 */
        public static final String USER_EDIT = "system:user:edit";
        /** 用户删除 */
        public static final String USER_DELETE = "system:user:delete";
        /** 用户导入 */
        public static final String USER_IMPORT = "system:user:import";
        /** 用户导出 */
        public static final String USER_EXPORT = "system:user:export";
        /** 重置密码 */
        public static final String USER_RESET_PASSWORD = "system:user:reset:password";

        // 角色管理权限
        /** 角色管理 */
        public static final String ROLE_MANAGE = "system:role:manage";
        /** 角色查询 */
        public static final String ROLE_QUERY = "system:role:query";
        /** 角色新增 */
        public static final String ROLE_ADD = "system:role:add";
        /** 角色编辑 */
        public static final String ROLE_EDIT = "system:role:edit";
        /** 角色删除 */
        public static final String ROLE_DELETE = "system:role:delete";
        /** 角色权限分配 */
        public static final String ROLE_ASSIGN_PERMISSION = "system:role:assign:permission";

        // 权限管理权限
        /** 权限管理 */
        public static final String PERMISSION_MANAGE = "system:permission:manage";
        /** 权限查询 */
        public static final String PERMISSION_QUERY = "system:permission:query";
        /** 权限新增 */
        public static final String PERMISSION_ADD = "system:permission:add";
        /** 权限编辑 */
        public static final String PERMISSION_EDIT = "system:permission:edit";
        /** 权限删除 */
        public static final String PERMISSION_DELETE = "system:permission:delete";

        // 系统配置权限
        /** 系统配置管理 */
        public static final String CONFIG_MANAGE = "system:config:manage";
        /** 系统配置查询 */
        public static final String CONFIG_QUERY = "system:config:query";
        /** 系统配置编辑 */
        public static final String CONFIG_EDIT = "system:config:edit";
        /** 系统配置重置 */
        public static final String CONFIG_RESET = "system:config:reset";

        // 日志管理权限
        /** 日志管理 */
        public static final String LOG_MANAGE = "system:log:manage";
        /** 日志查询 */
        public static final String LOG_QUERY = "system:log:query";
        /** 日志删除 */
        public static final String LOG_DELETE = "system:log:delete";
        /** 日志导出 */
        public static final String LOG_EXPORT = "system:log:export";

        // 档案管理权限
        /** 档案管理 */
        public static final String ARCHIVE_MANAGE = "business:archive:manage";
        /** 档案查询 */
        public static final String ARCHIVE_QUERY = "business:archive:query";
        /** 档案新增 */
        public static final String ARCHIVE_ADD = "business:archive:add";
        /** 档案编辑 */
        public static final String ARCHIVE_EDIT = "business:archive:edit";
        /** 档案删除 */
        public static final String ARCHIVE_DELETE = "business:archive:delete";
        /** 档案导入 */
        public static final String ARCHIVE_IMPORT = "business:archive:import";
        /** 档案导出 */
        public static final String ARCHIVE_EXPORT = "business:archive:export";
        /** 档案借阅 */
        public static final String ARCHIVE_BORROW = "business:archive:borrow";
        /** 档案归还 */
        public static final String ARCHIVE_RETURN = "business:archive:return";
        /** 档案审批 */
        public static final String ARCHIVE_APPROVE = "business:archive:approve";

        // 借阅管理权限
        /** 借阅管理 */
        public static final String BORROW_MANAGE = "business:borrow:manage";
        /** 借阅查询 */
        public static final String BORROW_QUERY = "business:borrow:query";
        /** 借阅申请 */
        public static final String BORROW_APPLY = "business:borrow:apply";
        /** 借阅审批 */
        public static final String BORROW_APPROVE = "business:borrow:approve";
        /** 借阅归还 */
        public static final String BORROW_RETURN = "business:borrow:return";
        /** 借阅延期 */
        public static final String BORROW_EXTEND = "business:borrow:extend";

        // 统计报表权限
        /** 统计报表 */
        public static final String REPORT_MANAGE = "business:report:manage";
        /** 档案统计 */
        public static final String REPORT_ARCHIVE = "business:report:archive";
        /** 借阅统计 */
        public static final String REPORT_BORROW = "business:report:borrow";
        /** 用户统计 */
        public static final String REPORT_USER = "business:report:user";
    }

    /**
     * 权限名称常量
     */
    public static final class Name {
        // 系统管理权限名称
        /** 系统管理 */
        public static final String SYSTEM_MANAGE = "系统管理";
        /** 用户管理 */
        public static final String USER_MANAGE = "用户管理";
        /** 角色管理 */
        public static final String ROLE_MANAGE = "角色管理";
        /** 权限管理 */
        public static final String PERMISSION_MANAGE = "权限管理";
        /** 系统配置 */
        public static final String CONFIG_MANAGE = "系统配置";
        /** 日志管理 */
        public static final String LOG_MANAGE = "日志管理";

        // 业务管理权限名称
        /** 档案管理 */
        public static final String ARCHIVE_MANAGE = "档案管理";
        /** 借阅管理 */
        public static final String BORROW_MANAGE = "借阅管理";
        /** 统计报表 */
        public static final String REPORT_MANAGE = "统计报表";
    }

    /**
     * 权限图标常量
     */
    public static final class Icon {
        /** 系统管理图标 */
        public static final String SYSTEM = "system";
        /** 用户管理图标 */
        public static final String USER = "user";
        /** 角色管理图标 */
        public static final String ROLE = "role";
        /** 权限管理图标 */
        public static final String PERMISSION = "permission";
        /** 配置管理图标 */
        public static final String CONFIG = "config";
        /** 日志管理图标 */
        public static final String LOG = "log";
        /** 档案管理图标 */
        public static final String ARCHIVE = "archive";
        /** 借阅管理图标 */
        public static final String BORROW = "borrow";
        /** 统计报表图标 */
        public static final String REPORT = "report";
    }

    /**
     * HTTP方法常量
     */
    public static final class HttpMethod {
        /** GET请求 */
        public static final String GET = "GET";
        /** POST请求 */
        public static final String POST = "POST";
        /** PUT请求 */
        public static final String PUT = "PUT";
        /** DELETE请求 */
        public static final String DELETE = "DELETE";
        /** PATCH请求 */
        public static final String PATCH = "PATCH";
        /** 所有方法 */
        public static final String ALL = "*";
    }

    /**
     * 缓存相关常量
     */
    public static final class Cache {
        /** 权限缓存前缀 */
        public static final String PERMISSION_PREFIX = "permission:";
        /** 用户权限缓存前缀 */
        public static final String USER_PERMISSION_PREFIX = "user:permission:";
        /** 权限树缓存键 */
        public static final String PERMISSION_TREE = "permission:tree";
        /** 权限列表缓存键 */
        public static final String PERMISSION_LIST = "permission:list";
        /** 权限映射缓存键 */
        public static final String PERMISSION_MAP = "permission:map";
    }

    private PermissionConstants() {
        // 工具类，禁止实例化
    }
}