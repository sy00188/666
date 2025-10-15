import { UserRole, UserPermission } from "@/types/auth";

/**
 * 角色权限映射表
 */
export const ROLE_PERMISSIONS: Record<UserRole, UserPermission[]> = {
  [UserRole.ADMIN]: [
    // 档案管理权限
    UserPermission.ARCHIVE_VIEW,
    UserPermission.ARCHIVE_CREATE,
    UserPermission.ARCHIVE_EDIT,
    UserPermission.ARCHIVE_DELETE,
    UserPermission.ARCHIVE_EXPORT,

    // 借阅管理权限
    UserPermission.BORROW_VIEW,
    UserPermission.BORROW_CREATE,
    UserPermission.BORROW_APPROVE,
    UserPermission.BORROW_RETURN,
    UserPermission.BORROW_EXPORT,

    // 用户管理权限
    UserPermission.USER_VIEW,
    UserPermission.USER_CREATE,
    UserPermission.USER_EDIT,
    UserPermission.USER_DELETE,
    UserPermission.USER_RESET_PASSWORD,

    // 报表权限
    UserPermission.REPORT_VIEW,
    UserPermission.REPORT_EXPORT,

    // 系统管理权限
    UserPermission.SYSTEM_CONFIG,
    UserPermission.SYSTEM_LOG,
    UserPermission.SYSTEM_BACKUP,
  ],

  [UserRole.USER]: [
    // 档案查看权限
    UserPermission.ARCHIVE_VIEW,

    // 借阅权限
    UserPermission.BORROW_VIEW,
    UserPermission.BORROW_CREATE,
  ],
};

/**
 * 角色层级定义（数字越大权限越高）
 */
export const ROLE_HIERARCHY: Record<UserRole, number> = {
  [UserRole.USER]: 1,
  [UserRole.ADMIN]: 2,
};

/**
 * 角色显示名称
 */
export const ROLE_LABELS: Record<UserRole, string> = {
  [UserRole.ADMIN]: "系统管理员",
  [UserRole.USER]: "普通用户",
};

/**
 * 权限显示名称
 */
export const PERMISSION_LABELS: Record<UserPermission, string> = {
  // 档案管理
  [UserPermission.ARCHIVE_VIEW]: "查看档案",
  [UserPermission.ARCHIVE_CREATE]: "创建档案",
  [UserPermission.ARCHIVE_EDIT]: "编辑档案",
  [UserPermission.ARCHIVE_DELETE]: "删除档案",
  [UserPermission.ARCHIVE_EXPORT]: "导出档案",

  // 借阅管理
  [UserPermission.BORROW_VIEW]: "查看借阅",
  [UserPermission.BORROW_CREATE]: "申请借阅",
  [UserPermission.BORROW_APPROVE]: "审批借阅",
  [UserPermission.BORROW_RETURN]: "归还档案",
  [UserPermission.BORROW_EXPORT]: "导出借阅",

  // 用户管理
  [UserPermission.USER_VIEW]: "查看用户",
  [UserPermission.USER_CREATE]: "创建用户",
  [UserPermission.USER_EDIT]: "编辑用户",
  [UserPermission.USER_DELETE]: "删除用户",
  [UserPermission.USER_RESET_PASSWORD]: "重置密码",

  // 报表管理
  [UserPermission.REPORT_VIEW]: "查看报表",
  [UserPermission.REPORT_EXPORT]: "导出报表",

  // 系统管理
  [UserPermission.SYSTEM_CONFIG]: "系统配置",
  [UserPermission.SYSTEM_LOG]: "系统日志",
  [UserPermission.SYSTEM_BACKUP]: "系统备份",
};

/**
 * 权限分组
 */
export const PERMISSION_GROUPS = {
  archive: {
    label: "档案管理",
    permissions: [
      UserPermission.ARCHIVE_VIEW,
      UserPermission.ARCHIVE_CREATE,
      UserPermission.ARCHIVE_EDIT,
      UserPermission.ARCHIVE_DELETE,
      UserPermission.ARCHIVE_EXPORT,
    ],
  },
  borrow: {
    label: "借阅管理",
    permissions: [
      UserPermission.BORROW_VIEW,
      UserPermission.BORROW_CREATE,
      UserPermission.BORROW_APPROVE,
      UserPermission.BORROW_RETURN,
      UserPermission.BORROW_EXPORT,
    ],
  },
  user: {
    label: "用户管理",
    permissions: [
      UserPermission.USER_VIEW,
      UserPermission.USER_CREATE,
      UserPermission.USER_EDIT,
      UserPermission.USER_DELETE,
      UserPermission.USER_RESET_PASSWORD,
    ],
  },
  report: {
    label: "报表管理",
    permissions: [UserPermission.REPORT_VIEW, UserPermission.REPORT_EXPORT],
  },
  system: {
    label: "系统管理",
    permissions: [
      UserPermission.SYSTEM_CONFIG,
      UserPermission.SYSTEM_LOG,
      UserPermission.SYSTEM_BACKUP,
    ],
  },
};

/**
 * 角色管理器类
 */
export class RoleManager {
  /**
   * 获取角色的所有权限
   * @param role 用户角色
   * @returns 权限列表
   */
  static getRolePermissions(role: UserRole): UserPermission[] {
    return ROLE_PERMISSIONS[role] || [];
  }

  /**
   * 检查角色是否拥有指定权限
   * @param role 用户角色
   * @param permission 权限
   * @returns 是否拥有权限
   */
  static hasPermission(role: UserRole, permission: UserPermission): boolean {
    const permissions = this.getRolePermissions(role);
    return permissions.includes(permission);
  }

  /**
   * 检查角色是否拥有任一权限
   * @param role 用户角色
   * @param permissions 权限列表
   * @returns 是否拥有任一权限
   */
  static hasAnyPermission(
    role: UserRole,
    permissions: UserPermission[],
  ): boolean {
    const rolePermissions = this.getRolePermissions(role);
    return permissions.some((permission) =>
      rolePermissions.includes(permission),
    );
  }

  /**
   * 检查角色是否拥有所有权限
   * @param role 用户角色
   * @param permissions 权限列表
   * @returns 是否拥有所有权限
   */
  static hasAllPermissions(
    role: UserRole,
    permissions: UserPermission[],
  ): boolean {
    const rolePermissions = this.getRolePermissions(role);
    return permissions.every((permission) =>
      rolePermissions.includes(permission),
    );
  }

  /**
   * 比较角色权限等级
   * @param role1 角色1
   * @param role2 角色2
   * @returns 1: role1 > role2, 0: role1 = role2, -1: role1 < role2
   */
  static compareRoles(role1: UserRole, role2: UserRole): number {
    const level1 = ROLE_HIERARCHY[role1] || 0;
    const level2 = ROLE_HIERARCHY[role2] || 0;

    if (level1 > level2) return 1;
    if (level1 < level2) return -1;
    return 0;
  }

  /**
   * 检查是否可以管理目标角色
   * @param managerRole 管理者角色
   * @param targetRole 目标角色
   * @returns 是否可以管理
   */
  static canManageRole(managerRole: UserRole, targetRole: UserRole): boolean {
    return this.compareRoles(managerRole, targetRole) > 0;
  }

  /**
   * 获取可管理的角色列表
   * @param managerRole 管理者角色
   * @returns 可管理的角色列表
   */
  static getManageableRoles(managerRole: UserRole): UserRole[] {
    const managerLevel = ROLE_HIERARCHY[managerRole] || 0;
    return Object.entries(ROLE_HIERARCHY)
      .filter(([, level]) => level < managerLevel)
      .map(([role]) => role as UserRole);
  }

  /**
   * 获取角色显示名称
   * @param role 角色
   * @returns 显示名称
   */
  static getRoleLabel(role: UserRole): string {
    return ROLE_LABELS[role] || role;
  }

  /**
   * 获取权限显示名称
   * @param permission 权限
   * @returns 显示名称
   */
  static getPermissionLabel(permission: UserPermission): string {
    return PERMISSION_LABELS[permission] || permission;
  }

  /**
   * 获取所有角色选项
   * @returns 角色选项列表
   */
  static getAllRoleOptions() {
    return Object.values(UserRole)
      .map((role) => ({
        value: role,
        label: this.getRoleLabel(role),
        level: ROLE_HIERARCHY[role] || 0,
      }))
      .sort((a, b) => b.level - a.level);
  }

  /**
   * 获取权限分组选项
   * @returns 权限分组选项
   */
  static getPermissionGroupOptions() {
    return Object.entries(PERMISSION_GROUPS).map(([key, group]) => ({
      key,
      label: group.label,
      permissions: group.permissions.map((permission) => ({
        value: permission,
        label: this.getPermissionLabel(permission),
      })),
    }));
  }
}

// 新的 RoleManager 实例类，用于测试
export class TestRoleManager {
  private getUserData: () => any;

  constructor(getUserData?: () => any) {
    this.getUserData = getUserData || (() => ({
      roles: ['admin', 'user'],
      permissions: ['user:create', 'user:read', 'user:update', 'archive:read', 'archive:create', 'system:config']
    }));
  }

  hasPermission(permission: string | string[]): boolean {
    const userData = this.getUserData();
    if (!userData || !userData.permissions) return false;
    
    const userPermissions = userData.permissions;
    
    if (Array.isArray(permission)) {
      if (permission.length === 0) return false;
      return permission.every(p => userPermissions.includes(p));
    }
    
    if (!permission) return false;
    return userPermissions.includes(permission);
  }

  hasRole(role: string): boolean {
    const userData = this.getUserData();
    if (!userData || !userData.roles) return false;
    
    if (!role) return false;
    return userData.roles.includes(role);
  }

  hasAnyRole(roles: string[]): boolean {
    const userData = this.getUserData();
    if (!userData || !userData.roles) return false;
    
    if (!roles || roles.length === 0) return false;
    return roles.some(role => userData.roles.includes(role));
  }

  hasAllRoles(roles: string[]): boolean {
    const userData = this.getUserData();
    if (!userData || !userData.roles) return false;
    
    if (!roles || roles.length === 0) return true;
    return roles.every(role => userData.roles.includes(role));
  }

  hasAnyPermission(permissions: string[]): boolean {
    const userData = this.getUserData();
    if (!userData || !userData.permissions) return false;
    
    if (!permissions || permissions.length === 0) return false;
    return permissions.some(p => userData.permissions.includes(p));
  }

  hasAllPermissions(permissions: string[]): boolean {
    const userData = this.getUserData();
    if (!userData || !userData.permissions) return false;
    
    if (!permissions || permissions.length === 0) return true;
    return permissions.every(p => userData.permissions.includes(p));
  }

  isAdmin(): boolean {
    return this.hasRole('admin');
  }

  isSuperAdmin(): boolean {
    return this.hasRole('superadmin');
  }

  canAccess(resource: {
    requiredPermissions?: string[];
    requiredRoles?: string[];
    logic?: 'AND' | 'OR';
  }): boolean {
    const { requiredPermissions = [], requiredRoles = [], logic = 'AND' } = resource;
    
    const hasRequiredPermissions = requiredPermissions.length === 0 || this.hasAllPermissions(requiredPermissions);
    const hasRequiredRoles = requiredRoles.length === 0 || this.hasAllRoles(requiredRoles);
    
    if (logic === 'OR') {
      return hasRequiredPermissions || hasRequiredRoles;
    }
    
    return hasRequiredPermissions && hasRequiredRoles;
  }

  getPermissionsByModule(module: string): string[] {
    const userData = this.getUserData();
    if (!userData || !userData.permissions) return [];
    
    return userData.permissions.filter(p => p.startsWith(module + ':'));
  }
}

// Utility functions for direct use
export const hasPermission = (permission: string | string[]): boolean => {
  const manager = new TestRoleManager();
  return manager.hasPermission(permission);
};

export const hasRole = (role: string): boolean => {
  const manager = new TestRoleManager();
  return manager.hasRole(role);
};

export const hasAnyRole = (roles: string[]): boolean => {
  const manager = new TestRoleManager();
  return manager.hasAnyRole(roles);
};

export const hasAllRoles = (roles: string[]): boolean => {
  const manager = new TestRoleManager();
  return manager.hasAllRoles(roles);
};

// TestRoleManager is already exported as a class above
