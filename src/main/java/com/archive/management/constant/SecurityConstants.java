package com.archive.management.constant;

/**
 * 安全相关常量
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
public class SecurityConstants {

    /**
     * 权限类型常量
     */
    public static final class PermissionType {
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
     * 权限状态常量
     */
    public static final class PermissionStatus {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 角色类型常量
     */
    public static final class RoleType {
        /** 系统角色 */
        public static final Integer SYSTEM = 1;
        /** 业务角色 */
        public static final Integer BUSINESS = 2;
        /** 自定义角色 */
        public static final Integer CUSTOM = 3;
    }

    /**
     * 角色状态常量
     */
    public static final class RoleStatus {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 数据权限范围常量
     */
    public static final class DataScope {
        /** 全部数据 */
        public static final Integer ALL = 1;
        /** 部门及以下数据 */
        public static final Integer DEPT_AND_CHILD = 2;
        /** 部门数据 */
        public static final Integer DEPT_ONLY = 3;
        /** 仅本人数据 */
        public static final Integer SELF_ONLY = 4;
        /** 自定义数据 */
        public static final Integer CUSTOM = 5;
    }

    /**
     * 系统内置标识常量
     */
    public static final class SystemFlag {
        /** 非系统内置 */
        public static final Integer NO = 0;
        /** 系统内置 */
        public static final Integer YES = 1;
    }

    /**
     * 逻辑删除标识常量
     */
    public static final class DeleteFlag {
        /** 未删除 */
        public static final Integer NOT_DELETED = 0;
        /** 已删除 */
        public static final Integer DELETED = 1;
    }

    /**
     * 缓存相关常量
     */
    public static final class Cache {
        /** 权限缓存前缀 */
        public static final String PERMISSION_PREFIX = "permission:";
        /** 角色缓存前缀 */
        public static final String ROLE_PREFIX = "role:";
        /** 用户权限缓存前缀 */
        public static final String USER_PERMISSION_PREFIX = "user:permission:";
        /** 用户角色缓存前缀 */
        public static final String USER_ROLE_PREFIX = "user:role:";
        /** 权限树缓存键 */
        public static final String PERMISSION_TREE = "permission:tree";
        /** 角色权限缓存前缀 */
        public static final String ROLE_PERMISSION_PREFIX = "role:permission:";
    }

    /**
     * 超级管理员角色代码
     */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /**
     * 管理员角色代码
     */
    public static final String ADMIN_ROLE = "ADMIN";

    /**
     * 默认权限级别
     */
    public static final Integer DEFAULT_PERMISSION_LEVEL = 1;

    /**
     * 根权限父ID
     */
    public static final Long ROOT_PERMISSION_PARENT_ID = 0L;

    /**
     * 默认角色级别
     */
    public static final Integer DEFAULT_ROLE_LEVEL = 1;

    /**
     * 最大角色级别
     */
    public static final Integer MAX_ROLE_LEVEL = 999;

    /**
     * 权限代码分隔符
     */
    public static final String PERMISSION_CODE_SEPARATOR = ":";

    /**
     * 权限路径分隔符
     */
    public static final String PERMISSION_PATH_SEPARATOR = "/";

    private SecurityConstants() {
        // 工具类，禁止实例化
    }
}