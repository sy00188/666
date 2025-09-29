<template>
  <div class="app-header">
    <div class="header-left">
      <!-- 侧边栏切换按钮 -->
      <el-button type="text" class="sidebar-toggle" @click="toggleSidebar">
        <el-icon>
          <Fold v-if="sidebarOpened" />
          <Expand v-else />
        </el-icon>
      </el-button>

      <!-- 面包屑导航 -->
      <el-breadcrumb
        v-if="showBreadcrumb && breadcrumbs.length > 0"
        class="breadcrumb"
        separator="/"
      >
        <el-breadcrumb-item
          v-for="(item, index) in breadcrumbs"
          :key="index"
          :to="item.path"
        >
          <el-icon v-if="item.icon" class="breadcrumb-icon">
            <component :is="item.icon" />
          </el-icon>
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <!-- 全屏切换 -->
      <el-tooltip content="全屏" placement="bottom">
        <el-button type="text" class="header-btn" @click="toggleFullscreen">
          <el-icon>
            <FullScreen v-if="!isFullscreen" />
            <Aim v-else />
          </el-icon>
        </el-button>
      </el-tooltip>

      <!-- 主题切换 -->
      <el-tooltip content="切换主题" placement="bottom">
        <el-button type="text" class="header-btn" @click="toggleTheme">
          <el-icon>
            <Sunny v-if="!isDark" />
            <Moon v-else />
          </el-icon>
        </el-button>
      </el-tooltip>

      <!-- 消息通知 -->
      <el-badge
        :value="unreadCount"
        :hidden="unreadCount === 0"
        class="notification-badge"
      >
        <el-button type="text" class="header-btn" @click="showNotifications">
          <el-icon>
            <Bell />
          </el-icon>
        </el-button>
      </el-badge>

      <!-- 用户菜单 -->
      <el-dropdown class="user-dropdown" @command="handleUserCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userAvatar" class="user-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="user-name">{{ userName }}</span>
          <el-icon class="dropdown-icon">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人资料
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item command="changePassword">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 通知抽屉 -->
    <el-drawer
      v-model="notificationDrawer"
      title="消息通知"
      direction="rtl"
      size="400px"
    >
      <div class="notification-content">
        <el-tabs v-model="activeNotificationTab">
          <el-tab-pane label="全部" name="all">
            <notification-list :notifications="allNotifications" />
          </el-tab-pane>
          <el-tab-pane label="未读" name="unread">
            <notification-list :notifications="unreadNotifications" />
          </el-tab-pane>
          <el-tab-pane label="系统" name="system">
            <notification-list :notifications="systemNotifications" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Fold,
  Expand,
  FullScreen,
  Aim,
  Sunny,
  Moon,
  Bell,
  User,
  ArrowDown,
  Setting,
  Lock,
  SwitchButton,
} from "@element-plus/icons-vue";
import { useAppStore } from "@/stores/app";
import { useAuthStore } from "@/stores/auth";
import NotificationList from "@/components/Common/NotificationList.vue";

const router = useRouter();
const appStore = useAppStore();
const authStore = useAuthStore();

// 响应式数据
const isFullscreen = ref(false);
const notificationDrawer = ref(false);
const activeNotificationTab = ref("all");
const unreadCount = ref(5); // 模拟未读消息数量

// 模拟通知数据
const allNotifications = ref([
  {
    id: 1,
    title: "系统更新通知",
    content: "系统将于今晚22:00进行维护更新",
    type: "system",
    read: false,
    time: "2024-01-20 10:30",
  },
  {
    id: 2,
    title: "新用户注册",
    content: "用户张三已成功注册",
    type: "user",
    read: true,
    time: "2024-01-20 09:15",
  },
]);

// 计算属性
const sidebarOpened = computed(() => appStore.sidebarOpened);
const showBreadcrumb = computed(() => appStore.showBreadcrumb);
const breadcrumbs = computed(() => appStore.breadcrumbs);
const isDark = computed(() => appStore.isDark);
const userName = computed(() => authStore.userName || "用户");
const userAvatar = computed(() => authStore.userAvatar);

const unreadNotifications = computed(() =>
  allNotifications.value.filter((n) => !n.read),
);

const systemNotifications = computed(() =>
  allNotifications.value.filter((n) => n.type === "system"),
);

// 方法
const toggleSidebar = () => {
  appStore.toggleSidebar();
};

const toggleTheme = () => {
  appStore.toggleTheme();
};

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
    isFullscreen.value = true;
  } else {
    document.exitFullscreen();
    isFullscreen.value = false;
  }
};

const showNotifications = () => {
  notificationDrawer.value = true;
};

const handleUserCommand = async (command: string) => {
  switch (command) {
    case "profile":
      router.push("/profile");
      break;
    case "settings":
      router.push("/settings");
      break;
    case "changePassword":
      // 打开修改密码对话框
      showChangePasswordDialog();
      break;
    case "logout":
      await handleLogout();
      break;
  }
};

const showChangePasswordDialog = () => {
  // 这里可以打开修改密码的对话框组件
  ElMessage.info("修改密码功能开发中...");
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await authStore.logout();
    router.push("/auth/login");
  } catch (error) {
    // 用户取消
  }
};

// 监听全屏状态变化
document.addEventListener("fullscreenchange", () => {
  isFullscreen.value = !!document.fullscreenElement;
});
</script>

<style lang="scss" scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .sidebar-toggle {
      font-size: 18px;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }
    }

    .breadcrumb {
      .breadcrumb-icon {
        margin-right: 4px;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-btn {
      width: 40px;
      height: 40px;
      font-size: 18px;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
      }
    }

    .notification-badge {
      :deep(.el-badge__content) {
        top: 8px;
        right: 8px;
      }
    }

    .user-dropdown {
      margin-left: 8px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background: var(--el-color-primary-light-9);
        }

        .user-avatar {
          flex-shrink: 0;
        }

        .user-name {
          font-size: 14px;
          color: var(--el-text-color-primary);
          max-width: 100px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .dropdown-icon {
          font-size: 12px;
          color: var(--el-text-color-regular);
          transition: transform 0.2s;
        }

        &:hover .dropdown-icon {
          transform: rotate(180deg);
        }
      }
    }
  }
}

.notification-content {
  height: 100%;

  :deep(.el-tabs__content) {
    height: calc(100% - 40px);
    overflow-y: auto;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-header {
    padding: 0 12px;

    .header-left {
      gap: 8px;

      .breadcrumb {
        display: none;
      }
    }

    .header-right {
      gap: 4px;

      .user-dropdown .user-info .user-name {
        display: none;
      }
    }
  }
}
</style>
