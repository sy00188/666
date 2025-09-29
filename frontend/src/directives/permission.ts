import type { App, DirectiveBinding } from "vue";
import { useAuthStore } from "@/stores/auth";
import { UserPermission, UserRole } from "@/types/auth";

/**
 * 权限指令接口
 */
interface PermissionDirectiveValue {
  permission?: string | UserPermission | (string | UserPermission)[];
  role?: string | UserRole | (string | UserRole)[];
  mode?: "any" | "all"; // 权限检查模式：any表示满足任一条件，all表示满足所有条件
}

/**
 * 检查权限
 * @param value 指令值
 * @returns 是否有权限
 */
function checkPermission(value: string | PermissionDirectiveValue): boolean {
  const authStore = useAuthStore();

  // 未登录用户无权限
  if (!authStore.isAuthenticated || !authStore.user) {
    return false;
  }

  // 管理员拥有所有权限
  if (authStore.user.role === UserRole.ADMIN) {
    return true;
  }

  // 简单字符串权限检查
  if (typeof value === "string") {
    return authStore.hasPermission(value);
  }

  const { permission, role, mode = "any" } = value;
  const userPermissions = authStore.user.permissions || [];
  const userRole = authStore.user.role;

  let hasPermission = true;
  let hasRole = true;

  // 检查权限
  if (permission) {
    if (Array.isArray(permission)) {
      if (mode === "all") {
        hasPermission = permission.every((p) =>
          userPermissions.includes(p as string),
        );
      } else {
        hasPermission = permission.some((p) =>
          userPermissions.includes(p as string),
        );
      }
    } else {
      hasPermission = userPermissions.includes(permission as string);
    }
  }

  // 检查角色
  if (role) {
    if (Array.isArray(role)) {
      if (mode === "all") {
        hasRole = role.every((r) => userRole === r);
      } else {
        hasRole = role.some((r) => userRole === r);
      }
    } else {
      hasRole = userRole === role;
    }
  }

  // 根据模式返回结果
  if (mode === "all") {
    return hasPermission && hasRole;
  } else {
    // 如果只指定了权限或角色，则只检查对应项
    if (permission && !role) return hasPermission;
    if (role && !permission) return hasRole;
    // 如果都指定了，则满足任一即可
    return hasPermission || hasRole;
  }
}

/**
 * 权限指令
 * 用法：
 * v-permission="'archive:create'" // 简单权限检查
 * v-permission="{ permission: 'archive:create' }" // 对象形式
 * v-permission="{ permission: ['archive:create', 'archive:edit'], mode: 'any' }" // 多权限任一
 * v-permission="{ role: 'admin' }" // 角色检查
 * v-permission="{ permission: 'archive:create', role: 'user', mode: 'all' }" // 权限和角色都要满足
 */
const permissionDirective = {
  mounted(
    el: HTMLElement,
    binding: DirectiveBinding<string | PermissionDirectiveValue>,
  ) {
    const hasAuth = checkPermission(binding.value);
    if (!hasAuth) {
      // 移除元素
      el.remove();
    }
  },

  updated(
    el: HTMLElement,
    binding: DirectiveBinding<string | PermissionDirectiveValue>,
  ) {
    const hasAuth = checkPermission(binding.value);
    if (!hasAuth) {
      // 隐藏元素
      el.style.display = "none";
    } else {
      // 显示元素
      el.style.display = "";
    }
  },
};

/**
 * 权限显示指令
 * 与 v-permission 不同，此指令只控制显示/隐藏，不移除DOM元素
 * 用法：v-permission-show="'archive:create'"
 */
const permissionShowDirective = {
  mounted(
    el: HTMLElement,
    binding: DirectiveBinding<string | PermissionDirectiveValue>,
  ) {
    const hasAuth = checkPermission(binding.value);
    if (!hasAuth) {
      el.style.display = "none";
    }
  },

  updated(
    el: HTMLElement,
    binding: DirectiveBinding<string | PermissionDirectiveValue>,
  ) {
    const hasAuth = checkPermission(binding.value);
    if (!hasAuth) {
      el.style.display = "none";
    } else {
      el.style.display = "";
    }
  },
};

/**
 * 权限禁用指令
 * 用于禁用没有权限的按钮或表单元素
 * 用法：v-permission-disabled="'archive:delete'"
 */
const permissionDisabledDirective = {
  mounted(
    el: HTMLElement,
    binding: DirectiveBinding<string | PermissionDirectiveValue>,
  ) {
    const hasAuth = checkPermission(binding.value);
    if (!hasAuth) {
      el.setAttribute("disabled", "true");
      el.style.opacity = "0.5";
      el.style.cursor = "not-allowed";
      el.title = "权限不足";
    }
  },

  updated(
    el: HTMLElement,
    binding: DirectiveBinding<string | PermissionDirectiveValue>,
  ) {
    const hasAuth = checkPermission(binding.value);
    if (!hasAuth) {
      el.setAttribute("disabled", "true");
      el.style.opacity = "0.5";
      el.style.cursor = "not-allowed";
      el.title = "权限不足";
    } else {
      el.removeAttribute("disabled");
      el.style.opacity = "";
      el.style.cursor = "";
      el.title = "";
    }
  },
};

/**
 * 安装权限指令
 * @param app Vue应用实例
 */
export function setupPermissionDirectives(app: App) {
  app.directive("permission", permissionDirective);
  app.directive("permission-show", permissionShowDirective);
  app.directive("permission-disabled", permissionDisabledDirective);
}

export {
  permissionDirective,
  permissionShowDirective,
  permissionDisabledDirective,
  checkPermission,
};
