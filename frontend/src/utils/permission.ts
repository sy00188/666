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
 * @param customPermissions 自定义权限列表（可选）
 * @returns 是否有权限
 */
export const hasPermission = (permission: string, customPermissions?: string[]): boolean => {
  if (!permission) return false;

  // 使用自定义权限列表或默认测试权限
  const permissionsToCheck = customPermissions || ['user:read', 'user:write', 'user:delete'];
  
  // 支持通配符权限
  if (permission.includes('*')) {
    const prefix = permission.replace('*', '');
    return permissionsToCheck.some(p => p.startsWith(prefix));
  }

  // 超级管理员拥有所有权限
  if (userPermissions.includes("*") || userPermissions.includes("admin")) {
    return true;
  }

  return permissionsToCheck.includes(permission) || userPermissions.includes(permission);
};

/**
 * 检查是否有任意一个权限
 * @param permissions 权限列表或逗号分隔的权限字符串
 * @returns 是否有权限
 */
export const hasAnyPermission = (permissions: string[] | string): boolean => {
  if (!permissions) return false;
  
  // 处理字符串参数（支持逗号分隔）
  if (typeof permissions === 'string') {
    // 如果包含逗号，分割成数组
    if (permissions.includes(',')) {
      const permissionArray = permissions.split(',').map(p => p.trim());
      return permissionArray.some((permission) => hasPermission(permission));
    }
    return hasPermission(permissions);
  }
  
  if (permissions.length === 0) return false;

  return permissions.some((permission) => hasPermission(permission));
};

/**
 * 检查是否有所有权限
 * @param permissions 权限列表或逗号分隔的权限字符串
 * @returns 是否有权限
 */
export const hasAllPermissions = (permissions: string[] | string): boolean => {
  if (!permissions) return true;
  
  // 处理字符串参数（支持逗号分隔）
  if (typeof permissions === 'string') {
    // 如果包含逗号，分割成数组
    if (permissions.includes(',')) {
      const permissionArray = permissions.split(',').map(p => p.trim());
      return permissionArray.every((permission) => hasPermission(permission));
    }
    return hasPermission(permissions);
  }
  
  if (permissions.length === 0) return true;

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
 * 检查权限并返回详细结果
 * @param permission 权限或权限数组
 * @param options 选项
 * @returns 权限检查结果
 */
export const checkPermission = (
  permission: string | string[],
  options?: { includeDetails?: boolean }
): {
  hasPermission: boolean;
  missingPermissions?: string[];
  details?: any;
} => {
  const permissions = Array.isArray(permission) ? permission : [permission];
  const missingPermissions: string[] = [];

  for (const perm of permissions) {
    if (!hasPermission(perm)) {
      missingPermissions.push(perm);
    }
  }

  const result = {
    hasPermission: missingPermissions.length === 0,
    missingPermissions: missingPermissions,
  };

  if (options?.includeDetails) {
    return {
      ...result,
      details: {
        userPermissions,
        checkedPermissions: permissions,
      },
    };
  }

  return result;
};

/**
 * 获取权限树结构
 * @param permissions 权限列表
 * @param options 选项
 * @returns 权限树
 */
export const getPermissionTree = (
  permissions: any[],
  options?: { filterByPermission?: boolean; filter?: (node: any) => boolean }
): any[] => {
  if (!permissions || !Array.isArray(permissions)) return [];

  let filteredPermissions = permissions;

  if (options?.filter) {
    filteredPermissions = permissions.filter(options.filter);
  } else if (options?.filterByPermission) {
    filteredPermissions = permissions.filter((perm) => hasPermission(perm.code));
  }

  return filteredPermissions;
};

/**
 * 根据权限过滤菜单
 * @param menus 菜单列表
 * @returns 过滤后的菜单
 */
export const filterMenuByPermission = (menus: any[]): any[] => {
  if (!menus || !Array.isArray(menus)) return [];

  return menus.filter((menu) => {
    // 如果没有权限字段，默认显示
    if (!menu.permission) return true;

    // 检查权限
    const hasAuth = hasPermission(menu.permission);
    if (!hasAuth) return false;

    // 递归过滤子菜单
    if (menu.children && menu.children.length > 0) {
      menu.children = filterMenuByPermission(menu.children);
    }

    return true;
  });
};

/**
 * 格式化权限代码
 * @param code 权限代码
 * @param separator 分隔符
 * @returns 格式化后的权限代码
 */
export const formatPermissionCode = (
  code: string | null | undefined,
  separator: string = ':'
): string => {
  if (!code) return '';
  
  // 转换为小写并替换分隔符
  return code.toLowerCase().replace(/[.:_]/g, separator);
};

/**
 * 验证权限代码格式
 * @param code 权限代码
 * @param rules 验证规则
 * @returns 是否有效
 */
export const validatePermissionCode = (
  code: string,
  rules?: { minLength?: number; pattern?: RegExp }
): boolean => {
  if (!code) return false;

  const defaultRules = {
    minLength: 3,
    pattern: /^[a-z]+:[a-z_:]+$/,  // 支持多级权限
  };

  const validationRules = { ...defaultRules, ...rules };

  if (code.length < validationRules.minLength) return false;
  if (!validationRules.pattern.test(code)) return false;

  return true;
};

/**
 * 获取权限级别
 * @param code 权限代码
 * @param separator 分隔符
 * @returns 权限级别
 */
export const getPermissionLevel = (
  code: string | null | undefined,
  separator: string = ':'
): number => {
  if (!code) return 0;
  return code.split(separator).length;
};

/**
 * 判断是否为系统权限
 * @param code 权限代码
 * @param systemPrefixes 系统权限前缀
 * @returns 是否为系统权限
 */
export const isSystemPermission = (
  code: string | null | undefined,
  systemPrefixes: string[] = ['system', 'admin']
): boolean => {
  if (!code) return false;
  
  return systemPrefixes.some(prefix => code.startsWith(prefix + ':'));
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
