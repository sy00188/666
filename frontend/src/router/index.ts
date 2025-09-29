import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw, RouteLocationNormalized } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { ElMessage } from "element-plus";

// 布局组件
const DefaultLayout = () => import("@/layouts/DefaultLayout.vue");
const AuthLayout = () => import("@/layouts/AuthLayout.vue");

// 页面组件
const Login = () => import("@/pages/auth/Login.vue");
const Register = () => import("@/pages/auth/Register.vue");
const Dashboard = () => import("@/pages/dashboard/index.vue");
const ArchiveList = () => import("@/pages/archive/List.vue");
const ArchiveDetail = () => import("@/pages/archive/Detail.vue");
const ArchiveForm = () => import("@/pages/archive/Form.vue");
const BorrowList = () => import("@/pages/borrow/List.vue");
const UserList = () => import("@/pages/user/List.vue");
const Reports = () => import("@/pages/reports/index.vue");
const ReportsNew = () => import("@/views/Reports.vue");
const UserAnalytics = () => import("@/views/UserAnalytics.vue");
const SystemMonitoring = () => import("@/views/SystemMonitoring.vue");
const PersonalizationSettings = () => import("@/views/PersonalizationSettings.vue");

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    redirect: "/dashboard",
  },
  {
    path: "/auth",
    component: AuthLayout,
    redirect: "/auth/login",
    children: [
      {
        path: "login",
        name: "Login",
        component: Login,
        meta: {
          title: "登录",
          requiresAuth: false,
          hideInMenu: true,
        },
      },
      {
        path: "register",
        name: "Register",
        component: Register,
        meta: {
          title: "注册",
          requiresAuth: false,
          hideInMenu: true,
        },
      },
    ],
  },
  {
    path: "/dashboard",
    component: DefaultLayout,
    children: [
      {
        path: "",
        name: "Dashboard",
        component: Dashboard,
        meta: {
          title: "仪表板",
          requiresAuth: true,
          icon: "Dashboard",
          roles: ["admin", "user"],
        },
      },
    ],
  },
  {
    path: "/archive",
    component: DefaultLayout,
    redirect: "/archive/list",
    meta: {
      title: "档案管理",
      icon: "Document",
      requiresAuth: true,
      roles: ["admin", "user"],
    },
    children: [
      {
        path: "list",
        name: "ArchiveList",
        component: ArchiveList,
        meta: {
          title: "档案列表",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "add",
        name: "ArchiveAdd",
        component: ArchiveForm,
        meta: {
          title: "新增档案",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "category",
        name: "ArchiveCategory",
        component: () => import("@/pages/archive/Category.vue"),
        meta: {
          title: "分类管理",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "search",
        name: "ArchiveSearch",
        component: () => import("@/pages/archive/Search.vue"),
        meta: {
          title: "高级搜索",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "detail/:id",
        name: "ArchiveDetail",
        component: ArchiveDetail,
        meta: {
          title: "档案详情",
          requiresAuth: true,
          roles: ["admin", "user"],
          hideInMenu: true,
        },
      },
      {
        path: "create",
        name: "ArchiveCreate",
        component: ArchiveForm,
        meta: {
          title: "新增档案",
          requiresAuth: true,
          roles: ["admin", "user"],
          hideInMenu: true,
        },
      },
      {
        path: "edit/:id",
        name: "ArchiveEdit",
        component: ArchiveForm,
        meta: {
          title: "编辑档案",
          requiresAuth: true,
          roles: ["admin", "user"],
          hideInMenu: true,
        },
      },
    ],
  },
  {
    path: "/borrow",
    component: DefaultLayout,
    redirect: "/borrow/list",
    meta: {
      title: "借阅管理",
      icon: "Reading",
      requiresAuth: true,
      roles: ["admin", "user"],
    },
    children: [
      {
        path: "list",
        name: "BorrowList",
        component: BorrowList,
        meta: {
          title: "借阅记录",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "apply",
        name: "BorrowApply",
        component: () => import("@/pages/borrow/Apply.vue"),
        meta: {
          title: "借阅申请",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "return",
        name: "BorrowReturn",
        component: () => import("@/pages/borrow/Return.vue"),
        meta: {
          title: "归还管理",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
      {
        path: "overdue",
        name: "BorrowOverdue",
        component: () => import("@/pages/borrow/Overdue.vue"),
        meta: {
          title: "逾期管理",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
    ],
  },
  {
    path: "/user",
    component: DefaultLayout,
    redirect: "/user/list",
    meta: {
      title: "用户管理",
      icon: "User",
      requiresAuth: true,
      roles: ["admin"],
    },
    children: [
      {
        path: "list",
        name: "UserList",
        component: UserList,
        meta: {
          title: "用户列表",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
    ],
  },
  {
    path: "/statistics",
    component: DefaultLayout,
    redirect: "/statistics/overview",
    meta: {
      title: "统计分析",
      icon: "DataAnalysis",
      requiresAuth: true,
      roles: ["admin"],
    },
    children: [
      {
        path: "overview",
        name: "StatisticsOverview",
        component: () => import("@/pages/statistics/Overview.vue"),
        meta: {
          title: "数据概览",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "archive",
        name: "StatisticsArchive",
        component: () => import("@/pages/statistics/Archive.vue"),
        meta: {
          title: "档案统计",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "borrow",
        name: "StatisticsBorrow",
        component: () => import("@/pages/statistics/Borrow.vue"),
        meta: {
          title: "借阅统计",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "user",
        name: "StatisticsUser",
        component: () => import("@/pages/statistics/User.vue"),
        meta: {
          title: "用户统计",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "reports",
        name: "StatisticsReports",
        component: ReportsNew,
        meta: {
          title: "报表管理",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "user-analytics",
        name: "UserAnalytics",
        component: UserAnalytics,
        meta: {
          title: "用户行为分析",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "system-monitoring",
        name: "SystemMonitoring",
        component: SystemMonitoring,
        meta: {
          title: "系统监控",
          requiresAuth: true,
          icon: "Monitor",
          roles: ["admin"],
        },
      },
    ],
  },
  {
    path: "/system",
    component: DefaultLayout,
    redirect: "/system/user",
    meta: {
      title: "系统管理",
      icon: "Setting",
      requiresAuth: true,
      roles: ["admin"],
    },
    children: [
      {
        path: "user",
        name: "SystemUser",
        component: () => import("@/pages/system/UserManagement.vue"),
        meta: {
          title: "用户管理",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "role",
        name: "SystemRole",
        component: () => import("@/pages/system/Role.vue"),
        meta: {
          title: "角色管理",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "permission",
        name: "SystemPermission",
        component: () => import("@/pages/system/Permission.vue"),
        meta: {
          title: "权限管理",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "logs",
        name: "SystemLogs",
        component: () => import("@/pages/system/Logs.vue"),
        meta: {
          title: "操作日志",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "settings",
        name: "SystemSettings",
        component: () => import("@/pages/system/Settings.vue"),
        meta: {
          title: "系统设置",
          requiresAuth: true,
          roles: ["admin"],
        },
      },
      {
        path: "personalization",
        name: "PersonalizationSettings",
        component: PersonalizationSettings,
        meta: {
          title: "个性化设置",
          requiresAuth: true,
          roles: ["admin", "user"],
        },
      },
    ],
  },
  {
    path: "/403",
    name: "Forbidden",
    component: () => import("@/pages/error/403.vue"),
    meta: {
      title: "权限不足",
      hideInMenu: true,
    },
  },
  {
    path: "/404",
    name: "NotFound",
    component: () => import("@/pages/error/404.vue"),
    meta: {
      title: "页面不存在",
      hideInMenu: true,
    },
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/404",
  },
];

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  },
});

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 档案管理系统`;
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      ElMessage.warning("请先登录");
      next({
        path: "/auth/login",
        query: { redirect: to.fullPath },
      });
      return;
    }

    // 检查角色权限
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const userRole = authStore.user?.role;
      if (!userRole || !(to.meta.roles as string[]).includes(userRole)) {
        ElMessage.error("权限不足");
        next("/403");
        return;
      }
    }
  }

  // 如果已登录用户访问登录页，重定向到首页
  if (to.path === "/auth/login" && authStore.isAuthenticated) {
    next("/dashboard");
    return;
  }

  next();
});

// 路由错误处理
router.onError((error) => {
  console.error("路由错误:", error);
  ElMessage.error("页面加载失败");
});

// 路由工具函数
export const routeUtils = {
  // 获取菜单路由
  getMenuRoutes(): RouteRecordRaw[] {
    return routes.filter(
      (route) =>
        route.meta &&
        !route.meta.hideInMenu &&
        route.path !== "/" &&
        route.path !== "/:pathMatch(.*)*",
    );
  },

  // 检查用户是否有权限访问路由
  hasPermission(route: RouteRecordRaw, userRole?: string): boolean {
    if (!route.meta?.requiresAuth) return true;
    if (!userRole) return false;
    if (!route.meta.roles || !Array.isArray(route.meta.roles)) return true;
    return (route.meta.roles as string[]).includes(userRole);
  },

  // 生成面包屑
  generateBreadcrumb(
    route: RouteLocationNormalized,
  ): Array<{ title: string; path?: string }> {
    const breadcrumb: Array<{ title: string; path?: string }> = [];

    if (route.matched) {
      route.matched.forEach((match) => {
        if (match.meta?.title) {
          breadcrumb.push({
            title: match.meta.title as string,
            path: match.path === route.path ? undefined : match.path,
          });
        }
      });
    }

    return breadcrumb;
  },

  // 获取当前路由的权限要求
  getRoutePermissions(routeName: string): string[] {
    const route = routes.find(
      (r) =>
        r.name === routeName ||
        r.children?.some((child) => child.name === routeName),
    );
    const roles = route?.meta?.roles;
    return Array.isArray(roles) ? (roles as string[]) : [];
  },
};

export default router;
