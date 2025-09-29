package com.archive.management.constant;

/**
 * 角色系统相关常量
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
public class RoleConstants {

    /**
     * 角色类型常量
     */
    public static final class Type {
        /** 系统角色 */
        public static final Integer SYSTEM = 1;
        /** 业务角色 */
        public static final Integer BUSINESS = 2;
        /** 自定义角色 */
        public static final Integer CUSTOM = 3;
    }

    /**
     * 角色类型描述常量
     */
    public static final class TypeDesc {
        /** 系统角色描述 */
        public static final String SYSTEM = "系统角色";
        /** 业务角色描述 */
        public static final String BUSINESS = "业务角色";
        /** 自定义角色描述 */
        public static final String CUSTOM = "自定义角色";
    }

    /**
     * 角色状态常量
     */
    public static final class Status {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
    }

    /**
     * 数据权限范围常量
     */
    public static final class DataScope {
        /** 全部数据权限 */
        public static final Integer ALL = 1;
        /** 部门及以下数据权限 */
        public static final Integer DEPT_AND_CHILD = 2;
        /** 部门数据权限 */
        public static final Integer DEPT_ONLY = 3;
        /** 仅本人数据权限 */
        public static final Integer SELF_ONLY = 4;
        /** 自定义数据权限 */
        public static final Integer CUSTOM = 5;
    }

    /**
     * 数据权限范围描述常量
     */
    public static final class DataScopeDesc {
        /** 全部数据权限描述 */
        public static final String ALL = "全部数据权限";
        /** 部门及以下数据权限描述 */
        public static final String DEPT_AND_CHILD = "部门及以下数据权限";
        /** 部门数据权限描述 */
        public static final String DEPT_ONLY = "部门数据权限";
        /** 仅本人数据权限描述 */
        public static final String SELF_ONLY = "仅本人数据权限";
        /** 自定义数据权限描述 */
        public static final String CUSTOM = "自定义数据权限";
    }

    /**
     * 角色级别常量
     */
    public static final class Level {
        /** 超级管理员级别 */
        public static final Integer SUPER_ADMIN = 0;
        /** 系统管理员级别 */
        public static final Integer SYSTEM_ADMIN = 1;
        /** 部门管理员级别 */
        public static final Integer DEPT_ADMIN = 2;
        /** 普通用户级别 */
        public static final Integer NORMAL_USER = 3;
        /** 访客级别 */
        public static final Integer GUEST = 4;
    }

    /**
     * 默认角色级别
     */
    public static final Integer DEFAULT_LEVEL = Level.NORMAL_USER;

    /**
     * 系统内置角色代码常量
     */
    public static final class Code {
        /** 超级管理员角色代码 */
        public static final String SUPER_ADMIN = "super_admin";
        /** 系统管理员角色代码 */
        public static final String SYSTEM_ADMIN = "system_admin";
        /** 档案管理员角色代码 */
        public static final String ARCHIVE_ADMIN = "archive_admin";
        /** 部门管理员角色代码 */
        public static final String DEPT_ADMIN = "dept_admin";
        /** 档案员角色代码 */
        public static final String ARCHIVIST = "archivist";
        /** 借阅员角色代码 */
        public static final String BORROWER = "borrower";
        /** 审批员角色代码 */
        public static final String APPROVER = "approver";
        /** 普通用户角色代码 */
        public static final String NORMAL_USER = "normal_user";
        /** 访客角色代码 */
        public static final String GUEST = "guest";
    }

    /**
     * 系统内置角色名称常量
     */
    public static final class Name {
        /** 超级管理员角色名称 */
        public static final String SUPER_ADMIN = "超级管理员";
        /** 系统管理员角色名称 */
        public static final String SYSTEM_ADMIN = "系统管理员";
        /** 档案管理员角色名称 */
        public static final String ARCHIVE_ADMIN = "档案管理员";
        /** 部门管理员角色名称 */
        public static final String DEPT_ADMIN = "部门管理员";
        /** 档案员角色名称 */
        public static final String ARCHIVIST = "档案员";
        /** 借阅员角色名称 */
        public static final String BORROWER = "借阅员";
        /** 审批员角色名称 */
        public static final String APPROVER = "审批员";
        /** 普通用户角色名称 */
        public static final String NORMAL_USER = "普通用户";
        /** 访客角色名称 */
        public static final String GUEST = "访客";
    }

    /**
     * 角色描述常量
     */
    public static final class Description {
        /** 超级管理员描述 */
        public static final String SUPER_ADMIN = "拥有系统所有权限的超级管理员";
        /** 系统管理员描述 */
        public static final String SYSTEM_ADMIN = "负责系统配置和用户管理的系统管理员";
        /** 档案管理员描述 */
        public static final String ARCHIVE_ADMIN = "负责档案管理和维护的档案管理员";
        /** 部门管理员描述 */
        public static final String DEPT_ADMIN = "负责部门内档案管理的部门管理员";
        /** 档案员描述 */
        public static final String ARCHIVIST = "负责档案录入和整理的档案员";
        /** 借阅员描述 */
        public static final String BORROWER = "可以借阅档案的借阅员";
        /** 审批员描述 */
        public static final String APPROVER = "负责借阅申请审批的审批员";
        /** 普通用户描述 */
        public static final String NORMAL_USER = "系统普通用户";
        /** 访客描述 */
        public static final String GUEST = "系统访客用户";
    }

    /**
     * 角色权限分配类型常量
     */
    public static final class PermissionAssignType {
        /** 直接分配 */
        public static final Integer DIRECT = 1;
        /** 继承分配 */
        public static final Integer INHERIT = 2;
        /** 临时分配 */
        public static final Integer TEMPORARY = 3;
    }

    /**
     * 角色权限操作类型常量
     */
    public static final class PermissionOperation {
        /** 授权 */
        public static final String GRANT = "GRANT";
        /** 撤销 */
        public static final String REVOKE = "REVOKE";
        /** 更新 */
        public static final String UPDATE = "UPDATE";
    }

    /**
     * 缓存相关常量
     */
    public static final class Cache {
        /** 角色缓存前缀 */
        public static final String ROLE_PREFIX = "role:";
        /** 用户角色缓存前缀 */
        public static final String USER_ROLE_PREFIX = "user:role:";
        /** 角色权限缓存前缀 */
        public static final String ROLE_PERMISSION_PREFIX = "role:permission:";
        /** 角色列表缓存键 */
        public static final String ROLE_LIST = "role:list";
        /** 角色映射缓存键 */
        public static final String ROLE_MAP = "role:map";
        /** 角色树缓存键 */
        public static final String ROLE_TREE = "role:tree";
    }

    /**
     * 角色排序常量
     */
    public static final class Sort {
        /** 按级别排序 */
        public static final String BY_LEVEL = "level";
        /** 按创建时间排序 */
        public static final String BY_CREATE_TIME = "create_time";
        /** 按更新时间排序 */
        public static final String BY_UPDATE_TIME = "update_time";
        /** 按角色名称排序 */
        public static final String BY_NAME = "name";
        /** 按角色代码排序 */
        public static final String BY_CODE = "code";
    }

    /**
     * 角色查询类型常量
     */
    public static final class QueryType {
        /** 查询所有角色 */
        public static final String ALL = "ALL";
        /** 查询启用角色 */
        public static final String ENABLED = "ENABLED";
        /** 查询禁用角色 */
        public static final String DISABLED = "DISABLED";
        /** 查询系统角色 */
        public static final String SYSTEM = "SYSTEM";
        /** 查询业务角色 */
        public static final String BUSINESS = "BUSINESS";
        /** 查询自定义角色 */
        public static final String CUSTOM = "CUSTOM";
    }

    /**
     * 角色验证规则常量
     */
    public static final class Validation {
        /** 角色代码最小长度 */
        public static final Integer CODE_MIN_LENGTH = 2;
        /** 角色代码最大长度 */
        public static final Integer CODE_MAX_LENGTH = 50;
        /** 角色名称最小长度 */
        public static final Integer NAME_MIN_LENGTH = 2;
        /** 角色名称最大长度 */
        public static final Integer NAME_MAX_LENGTH = 100;
        /** 角色描述最大长度 */
        public static final Integer DESCRIPTION_MAX_LENGTH = 500;
        /** 角色代码正则表达式 */
        public static final String CODE_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";
    }

    private RoleConstants() {
        // 工具类，禁止实例化
    }
}