<template>
  <div class="app-sidebar" :class="{ 'is-collapsed': !sidebarOpened }">
    <!-- Logo区域 -->
    <div class="sidebar-header">
      <div class="logo-container">
        <img src="/logo.svg" alt="Logo" class="logo-image" />
        <transition name="fade">
          <span v-show="sidebarOpened" class="logo-text">管理系统</span>
        </transition>
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="sidebar-content">
      <el-scrollbar class="sidebar-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="!sidebarOpened"
          :unique-opened="true"
          class="sidebar-menu"
          router
          @select="handleMenuSelect"
        >
          <!-- 仪表盘 -->
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <!-- 用户管理 -->
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/user/list">
              <el-icon><List /></el-icon>
              <template #title>用户列表</template>
            </el-menu-item>
            <el-menu-item index="/user/roles">
              <el-icon><UserFilled /></el-icon>
              <template #title>角色管理</template>
            </el-menu-item>
            <el-menu-item index="/user/permissions">
              <el-icon><Lock /></el-icon>
              <template #title>权限管理</template>
            </el-menu-item>
          </el-sub-menu>

          <!-- 内容管理 -->
          <el-sub-menu index="content">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/content/articles">
              <el-icon><EditPen /></el-icon>
              <template #title>文章管理</template>
            </el-menu-item>
            <el-menu-item index="/content/categories">
              <el-icon><Collection /></el-icon>
              <template #title>分类管理</template>
            </el-menu-item>
            <el-menu-item index="/content/tags">
              <el-icon><PriceTag /></el-icon>
              <template #title>标签管理</template>
            </el-menu-item>
          </el-sub-menu>

          <!-- 系统管理 -->
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/config">
              <el-icon><Tools /></el-icon>
              <template #title>系统配置</template>
            </el-menu-item>
            <el-menu-item index="/system/logs">
              <el-icon><Document /></el-icon>
              <template #title>系统日志</template>
            </el-menu-item>
            <el-menu-item index="/system/monitor">
              <el-icon><Monitor /></el-icon>
              <template #title>系统监控</template>
            </el-menu-item>
          </el-sub-menu>

          <!-- 数据统计 -->
          <el-sub-menu index="statistics">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </template>
            <el-menu-item index="/statistics/overview">
              <el-icon><TrendCharts /></el-icon>
              <template #title>数据概览</template>
            </el-menu-item>
            <el-menu-item index="/statistics/reports">
              <el-icon><Histogram /></el-icon>
              <template #title>统计报表</template>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </div>

    <!-- 底部信息 -->
    <div class="sidebar-footer">
      <transition name="fade">
        <div v-show="sidebarOpened" class="version-info">
          <span class="version-text">v1.0.0</span>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import {
  Odometer,
  User,
  UserFilled,
  Lock,
  Document,
  EditPen,
  Collection,
  PriceTag,
  Setting,
  Tools,
  Monitor,
  DataAnalysis,
  TrendCharts,
  Histogram,
  List,
} from "@element-plus/icons-vue";
import { useAppStore } from "@/stores/app";

const route = useRoute();
const appStore = useAppStore();

// 计算属性
const sidebarOpened = computed(() => appStore.sidebarOpened);
const activeMenu = computed(() => {
  const { path } = route;
  // 处理子菜单的激活状态
  if (path.startsWith("/user")) return path;
  if (path.startsWith("/content")) return path;
  if (path.startsWith("/system")) return path;
  if (path.startsWith("/statistics")) return path;
  return path;
});

// 方法
const handleMenuSelect = (index: string) => {
  // 更新面包屑导航
  updateBreadcrumbs(index);
};

const updateBreadcrumbs = (path: string) => {
  const breadcrumbMap: Record<
    string,
    Array<{ title: string; path: string; icon?: string }>
  > = {
    "/dashboard": [{ title: "首页", path: "/dashboard", icon: "Odometer" }],
    "/user/list": [
      { title: "用户管理", path: "/user" },
      { title: "用户列表", path: "/user/list" },
    ],
    "/user/roles": [
      { title: "用户管理", path: "/user" },
      { title: "角色管理", path: "/user/roles" },
    ],
    "/user/permissions": [
      { title: "用户管理", path: "/user" },
      { title: "权限管理", path: "/user/permissions" },
    ],
    "/content/articles": [
      { title: "内容管理", path: "/content" },
      { title: "文章管理", path: "/content/articles" },
    ],
    "/content/categories": [
      { title: "内容管理", path: "/content" },
      { title: "分类管理", path: "/content/categories" },
    ],
    "/content/tags": [
      { title: "内容管理", path: "/content" },
      { title: "标签管理", path: "/content/tags" },
    ],
    "/system/config": [
      { title: "系统管理", path: "/system" },
      { title: "系统配置", path: "/system/config" },
    ],
    "/system/logs": [
      { title: "系统管理", path: "/system" },
      { title: "系统日志", path: "/system/logs" },
    ],
    "/system/monitor": [
      { title: "系统管理", path: "/system" },
      { title: "系统监控", path: "/system/monitor" },
    ],
    "/statistics/overview": [
      { title: "数据统计", path: "/statistics" },
      { title: "数据概览", path: "/statistics/overview" },
    ],
    "/statistics/reports": [
      { title: "数据统计", path: "/statistics" },
      { title: "统计报表", path: "/statistics/reports" },
    ],
  };

  const breadcrumbs = breadcrumbMap[path] || [];
  appStore.setBreadcrumbs(breadcrumbs);
};
</script>

<style lang="scss" scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 240px;
  background: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);
  transition: width 0.3s ease;
  overflow: hidden;

  &.is-collapsed {
    width: 64px;
  }

  .sidebar-header {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid var(--el-border-color-light);

    .logo-container {
      display: flex;
      align-items: center;
      gap: 12px;
      width: 100%;

      .logo-image {
        width: 32px;
        height: 32px;
        flex-shrink: 0;
      }

      .logo-text {
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        white-space: nowrap;
      }
    }
  }

  .sidebar-content {
    flex: 1;
    overflow: hidden;

    .sidebar-scrollbar {
      height: 100%;

      :deep(.el-scrollbar__view) {
        height: 100%;
      }
    }

    .sidebar-menu {
      border: none;
      height: 100%;

      :deep(.el-menu-item) {
        height: 48px;
        line-height: 48px;
        margin: 4px 8px;
        border-radius: 6px;

        &:hover {
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
        }

        &.is-active {
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
          font-weight: 500;

          &::before {
            content: "";
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 20px;
            background: var(--el-color-primary);
            border-radius: 0 2px 2px 0;
          }
        }
      }

      :deep(.el-sub-menu) {
        .el-sub-menu__title {
          height: 48px;
          line-height: 48px;
          margin: 4px 8px;
          border-radius: 6px;

          &:hover {
            background: var(--el-color-primary-light-9);
            color: var(--el-color-primary);
          }
        }

        .el-menu {
          background: transparent;

          .el-menu-item {
            margin: 2px 16px;
            padding-left: 40px !important;
          }
        }
      }

      // 折叠状态下的样式
      &.el-menu--collapse {
        :deep(.el-menu-item),
        :deep(.el-sub-menu__title) {
          margin: 4px 8px;
          padding: 0 !important;
          justify-content: center;

          .el-icon {
            margin-right: 0;
          }
        }
      }
    }
  }

  .sidebar-footer {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-top: 1px solid var(--el-border-color-light);
    padding: 0 20px;

    .version-info {
      .version-text {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
      }
    }
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式设计
@media (max-width: 768px) {
  .app-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;

    &:not(.is-collapsed) {
      transform: translateX(0);
    }
  }
}
</style>
