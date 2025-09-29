import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { UserPermission, UserRole } from "@/types/auth";

/**
 * 权限管理组合式函数
 * 提供统一的权限检查和管理功能
 */
export function usePermission() {
  const authStore = useAuthStore();

  // 当前用户信息
  const currentUser = computed(() => authStore.user);

  // 当前用户角色
  const currentRole = computed(() => authStore.user?.role);

  // 当前用户权限列表
  const currentPermissions = computed(() => authStore.user?.permissions || []);

  /**
   * 检查用户是否具有指定权限
   * @param permission 权限标识
   * @returns 是否具有权限
   */
  const hasPermission = (permission: string | UserPermission): boolean => {
    if (!authStore.isAuthenticated || !currentUser.value) {
      return false;
    }

    // 管理员拥有所有权限
    if (currentRole.value === UserRole.ADMIN) {
      return true;
    }

    // 检查用户权限列表
    return currentPermissions.value.includes(permission as string);
  };

  /**
   * 检查用户是否具有指定角色
   * @param role 角色标识
   * @returns 是否具有角色
   */
  const hasRole = (role: string | UserRole): boolean => {
    if (!authStore.isAuthenticated || !currentUser.value) {
      return false;
    }

    return currentRole.value === role;
  };

  /**
   * 检查用户是否具有任一指定权限
   * @param permissions 权限列表
   * @returns 是否具有任一权限
   */
  const hasAnyPermission = (
    permissions: (string | UserPermission)[],
  ): boolean => {
    return permissions.some((permission) => hasPermission(permission));
  };

  /**
   * 检查用户是否具有所有指定权限
   * @param permissions 权限列表
   * @returns 是否具有所有权限
   */
  const hasAllPermissions = (
    permissions: (string | UserPermission)[],
  ): boolean => {
    return permissions.every((permission) => hasPermission(permission));
  };

  /**
   * 检查用户是否具有任一指定角色
   * @param roles 角色列表
   * @returns 是否具有任一角色
   */
  const hasAnyRole = (roles: (string | UserRole)[]): boolean => {
    return roles.some((role) => hasRole(role));
  };

  /**
   * 获取用户在指定模块的权限
   * @param module 模块名称（如 'archive', 'borrow', 'user'）
   * @returns 模块权限列表
   */
  const getModulePermissions = (module: string): string[] => {
    const modulePrefix = `${module}:`;
    return currentPermissions.value.filter((permission) =>
      permission.startsWith(modulePrefix),
    );
  };

  /**
   * 检查用户是否可以访问指定路由
   * @param routeMeta 路由元信息
   * @returns 是否可以访问
   */
  const canAccessRoute = (routeMeta: {
    requiresAuth?: boolean;
    roles?: string[];
    permissions?: string[];
  }): boolean => {
    // 不需要认证的路由
    if (!routeMeta?.requiresAuth) {
      return true;
    }

    // 需要认证但用户未登录
    if (!authStore.isAuthenticated) {
      return false;
    }

    // 检查角色权限
    if (routeMeta.roles && Array.isArray(routeMeta.roles)) {
      return hasAnyRole(routeMeta.roles);
    }

    // 检查权限
    if (routeMeta.permissions && Array.isArray(routeMeta.permissions)) {
      return hasAnyPermission(routeMeta.permissions);
    }

    return true;
  };

  /**
   * 档案管理权限检查
   */
  const archivePermissions = {
    canView: computed(() => hasPermission(UserPermission.ARCHIVE_VIEW)),
    canCreate: computed(() => hasPermission(UserPermission.ARCHIVE_CREATE)),
    canEdit: computed(() => hasPermission(UserPermission.ARCHIVE_EDIT)),
    canDelete: computed(() => hasPermission(UserPermission.ARCHIVE_DELETE)),
    canExport: computed(() => hasPermission(UserPermission.ARCHIVE_EXPORT)),
  };

  /**
   * 借阅管理权限检查
   */
  const borrowPermissions = {
    canView: computed(() => hasPermission(UserPermission.BORROW_VIEW)),
    canCreate: computed(() => hasPermission(UserPermission.BORROW_CREATE)),
    canApprove: computed(() => hasPermission(UserPermission.BORROW_APPROVE)),
    canReturn: computed(() => hasPermission(UserPermission.BORROW_RETURN)),
    canExport: computed(() => hasPermission(UserPermission.BORROW_EXPORT)),
  };

  /**
   * 用户管理权限检查
   */
  const userPermissions = {
    canView: computed(() => hasPermission(UserPermission.USER_VIEW)),
    canCreate: computed(() => hasPermission(UserPermission.USER_CREATE)),
    canEdit: computed(() => hasPermission(UserPermission.USER_EDIT)),
    canDelete: computed(() => hasPermission(UserPermission.USER_DELETE)),
    canResetPassword: computed(() =>
      hasPermission(UserPermission.USER_RESET_PASSWORD),
    ),
  };

  /**
   * 报表统计权限检查
   */
  const reportPermissions = {
    canView: computed(() => hasPermission(UserPermission.REPORT_VIEW)),
    canExport: computed(() => hasPermission(UserPermission.REPORT_EXPORT)),
  };

  /**
   * 系统管理权限检查
   */
  const systemPermissions = {
    canConfig: computed(() => hasPermission(UserPermission.SYSTEM_CONFIG)),
    canViewLog: computed(() => hasPermission(UserPermission.SYSTEM_LOG)),
    canBackup: computed(() => hasPermission(UserPermission.SYSTEM_BACKUP)),
  };

  return {
    // 用户信息
    currentUser,
    currentRole,
    currentPermissions,

    // 权限检查方法
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAllPermissions,
    hasAnyRole,
    getModulePermissions,
    canAccessRoute,

    // 模块权限
    archivePermissions,
    borrowPermissions,
    userPermissions,
    reportPermissions,
    systemPermissions,
  };
}
