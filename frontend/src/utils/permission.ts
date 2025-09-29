/**
 * 权限工具函数
 */

// 模拟用户权限数据（实际项目中应该从状态管理或API获取）
let userPermissions: string[] = [];

/**
 * 设置用户权限
 * @param permissions 权限列表
 */
export const setUserPermissions = (permissions: string[]): void => {
  userPermissions = permissions;
};

/**
 * 获取用户权限
 * @returns 权限列表
 */
export const getUserPermissions = (): string[] => {
  return userPermissions;
};

/**
 * 检查是否有指定权限
 * @param permission 权限标识
 * @returns 是否有权限
 */
export const hasPermission = (permission: string): boolean => {
  if (!permission) return true;

  // 超级管理员拥有所有权限
  if (userPermissions.includes("*") || userPermissions.includes("admin")) {
    return true;
  }

  return userPermissions.includes(permission);
};

/**
 * 检查是否有任意一个权限
 * @param permissions 权限列表
 * @returns 是否有权限
 */
export const hasAnyPermission = (permissions: string[]): boolean => {
  if (!permissions || permissions.length === 0) return true;

  return permissions.some((permission) => hasPermission(permission));
};

/**
 * 检查是否有所有权限
 * @param permissions 权限列表
 * @returns 是否有权限
 */
export const hasAllPermissions = (permissions: string[]): boolean => {
  if (!permissions || permissions.length === 0) return true;

  return permissions.every((permission) => hasPermission(permission));
};

/**
 * 权限指令（用于 v-permission）
 */
export const permissionDirective = {
  mounted(el: HTMLElement, binding: { value: string | string[] }) {
    const { value } = binding;

    if (!value) return;

    const permissions = Array.isArray(value) ? value : [value];
    const hasAuth = hasAnyPermission(permissions);

    if (!hasAuth) {
      el.style.display = "none";
      // 或者移除元素
      // el.parentNode?.removeChild(el);
    }
  },

  updated(el: HTMLElement, binding: { value: string | string[] }) {
    const { value } = binding;

    if (!value) return;

    const permissions = Array.isArray(value) ? value : [value];
    const hasAuth = hasAnyPermission(permissions);

    if (!hasAuth) {
      el.style.display = "none";
    } else {
      el.style.display = "";
    }
  },
};

/**
 * 权限角色定义
 */
export const ROLES = {
  SUPER_ADMIN: "super_admin",
  ADMIN: "admin",
  MANAGER: "manager",
  USER: "user",
  GUEST: "guest",
} as const;

/**
 * 权限模块定义
 */
export const PERMISSIONS = {
  // 部门管理
  DEPARTMENT: {
    VIEW: "department:view",
    CREATE: "department:create",
    UPDATE: "department:update",
    DELETE: "department:delete",
    EXPORT: "department:export",
    IMPORT: "department:import",
    MEMBER: {
      VIEW: "department:member:view",
      ADD: "department:member:add",
      REMOVE: "department:member:remove",
      MANAGER: "department:member:manager",
    },
  },

  // 用户管理
  USER: {
    VIEW: "user:view",
    CREATE: "user:create",
    UPDATE: "user:update",
    DELETE: "user:delete",
    EXPORT: "user:export",
    IMPORT: "user:import",
    RESET_PASSWORD: "user:reset_password",
  },

  // 角色管理
  ROLE: {
    VIEW: "role:view",
    CREATE: "role:create",
    UPDATE: "role:update",
    DELETE: "role:delete",
    ASSIGN: "role:assign",
  },

  // 权限管理
  PERMISSION: {
    VIEW: "permission:view",
    CREATE: "permission:create",
    UPDATE: "permission:update",
    DELETE: "permission:delete",
  },
} as const;

/**
 * 检查用户角色
 * @param role 角色
 * @returns 是否有该角色
 */
export const hasRole = (_role: string): boolean => {
  // TODO: 实现角色检查逻辑
  // 这里应该从用户信息中获取角色并进行比较
  return true;
};

/**
 * 获取权限描述
 * @param permission 权限标识
 * @returns 权限描述
 */
export const getPermissionDescription = (permission: string): string => {
  const descriptions: Record<string, string> = {
    "department:view": "查看部门",
    "department:create": "创建部门",
    "department:update": "编辑部门",
    "department:delete": "删除部门",
    "department:export": "导出部门",
    "department:import": "导入部门",
    "department:member:view": "查看部门成员",
    "department:member:add": "添加部门成员",
    "department:member:remove": "移除部门成员",
    "department:member:manager": "设置部门管理员",
    "user:view": "查看用户",
    "user:create": "创建用户",
    "user:update": "编辑用户",
    "user:delete": "删除用户",
    "user:export": "导出用户",
    "user:import": "导入用户",
    "user:reset_password": "重置密码",
  };

  return descriptions[permission] || permission;
};
