<template>
  <div class="app-header">
    <div class="header-left">
      <!-- 菜单折叠按钮 -->
      <el-button type="text" class="menu-toggle" @click="toggleSidebar">
        <el-icon><Fold v-if="!sidebarOpened" /><Expand v-else /></el-icon>
      </el-button>

      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item
          v-for="item in breadcrumbList"
          :key="item.path"
          :to="item.path"
        >
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索档案..."
          prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- 通知 -->
      <el-dropdown trigger="click" class="notification-dropdown">
        <el-badge :value="notificationCount" :hidden="notificationCount === 0">
          <el-button type="text" class="header-btn">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>
        <template #dropdown>
          <el-dropdown-menu>
            <div class="notification-header">
              <span>通知消息</span>
              <el-button
                type="text"
                size="small"
                @click="clearAllNotifications"
              >
                全部清除
              </el-button>
            </div>
            <div class="notification-list">
              <div
                v-for="notification in notifications"
                :key="notification.id"
                class="notification-item"
                @click="handleNotificationClick(notification)"
              >
                <div class="notification-content">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-desc">
                    {{ notification.description }}
                  </div>
                  <div class="notification-time">
                    {{ formatTime(notification.time) }}
                  </div>
                </div>
                <el-button
                  type="text"
                  size="small"
                  @click.stop="removeNotification(notification.id)"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
              <div v-if="notifications.length === 0" class="no-notifications">
                暂无通知
              </div>
            </div>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 用户信息 -->
      <el-dropdown trigger="click" class="user-dropdown">
        <div class="user-info">
          <el-avatar :size="32" :src="userInfo.avatar">
            {{ userInfo.name?.charAt(0) }}
          </el-avatar>
          <span class="username">{{ userInfo.name }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goToProfile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item @click="goToSettings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Fold,
  Expand,
  Bell,
  Close,
  ArrowDown,
  User,
  Setting,
  SwitchButton,
} from "@element-plus/icons-vue";
import { useAuthStore } from "@/stores/auth";
import { useAppStore } from "@/stores/app";
import { formatDistanceToNow } from "date-fns";
import { zhCN } from "date-fns/locale";

// 通知类型定义
interface Notification {
  id: number;
  title: string;
  description: string;
  time: Date;
  type: "success" | "warning" | "info" | "error";
}

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const appStore = useAppStore();

// 响应式数据
const searchKeyword = ref("");

// 通知数据
const notifications = ref<Notification[]>([
  {
    id: 1,
    title: "档案审核通知",
    description: '您提交的档案"2024年度报告"已通过审核',
    time: new Date(Date.now() - 1000 * 60 * 30), // 30分钟前
    type: "success",
  },
  {
    id: 2,
    title: "借阅到期提醒",
    description: '您借阅的档案"项目文档"将于明天到期',
    time: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2小时前
    type: "warning",
  },
]);

// 计算属性
const userInfo = computed(() => authStore.user || {});
const sidebarOpened = computed(() => appStore.sidebarOpened);
const notificationCount = computed(() => notifications.value.length);

// 面包屑导航
const breadcrumbList = computed(() => {
  const matched = route.matched.filter((item) => item.meta && item.meta.title);
  return matched.map((item) => ({
    path: item.path,
    title: item.meta?.title || "",
  }));
});

// 方法
const toggleSidebar = () => {
  appStore.toggleSidebar();
};

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: "/archive/list",
      query: { keyword: searchKeyword.value },
    });
  }
};

// 格式化时间
const formatTime = (time: Date) => {
  return formatDistanceToNow(time, {
    addSuffix: true,
    locale: zhCN,
  });
};

// 处理通知点击
const handleNotificationClick = (notification: Notification) => {
  // 根据通知类型跳转到相应页面
  console.log("点击通知:", notification);
};

// 移除通知
const removeNotification = (id: number) => {
  const index = notifications.value.findIndex((item) => item.id === id);
  if (index > -1) {
    notifications.value.splice(index, 1);
  }
};

// 清除所有通知
const clearAllNotifications = () => {
  notifications.value = [];
};

// 跳转到个人中心
const goToProfile = () => {
  router.push("/system/profile");
};

// 跳转到系统设置
const goToSettings = () => {
  router.push("/system/settings");
};

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    await authStore.logout();
    ElMessage.success("退出登录成功");
    router.push("/auth/login");
  } catch {
    // 用户取消操作
  }
};

onMounted(() => {
  // 组件挂载时的初始化逻辑
});
</script>

<style scoped lang="scss">
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .menu-toggle {
      font-size: 18px;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }
    }

    .breadcrumb {
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          color: var(--el-text-color-regular);
          font-weight: normal;

          &:hover {
            color: var(--el-color-primary);
          }
        }

        &:last-child .el-breadcrumb__inner {
          color: var(--el-text-color-primary);
          font-weight: 500;
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .search-box {
      width: 240px;

      :deep(.el-input) {
        .el-input__wrapper {
          border-radius: 20px;
        }
      }
    }

    .header-btn {
      font-size: 18px;
      color: var(--el-text-color-regular);

      &:hover {
        color: var(--el-color-primary);
      }
    }

    .notification-dropdown {
      :deep(.el-dropdown) {
        .el-badge {
          .el-badge__content {
            top: 8px;
            right: 8px;
          }
        }
      }
    }

    .user-dropdown {
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 8px;
        border-radius: 20px;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background: var(--el-fill-color-light);
        }

        .username {
          font-size: 14px;
          color: var(--el-text-color-primary);
          font-weight: 500;
        }

        .el-icon {
          font-size: 12px;
          color: var(--el-text-color-regular);
        }
      }
    }
  }
}

// 通知下拉菜单样式
:deep(.notification-dropdown) {
  .el-dropdown-menu {
    width: 320px;
    padding: 0;

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .notification-list {
      max-height: 300px;
      overflow-y: auto;

      .notification-item {
        display: flex;
        align-items: flex-start;
        padding: 12px 16px;
        border-bottom: 1px solid var(--el-border-color-lighter);
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background: var(--el-fill-color-light);
        }

        &:last-child {
          border-bottom: none;
        }

        .notification-content {
          flex: 1;

          .notification-title {
            font-size: 14px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 4px;
          }

          .notification-desc {
            font-size: 12px;
            color: var(--el-text-color-regular);
            line-height: 1.4;
            margin-bottom: 4px;
          }

          .notification-time {
            font-size: 11px;
            color: var(--el-text-color-placeholder);
          }
        }
      }

      .no-notifications {
        padding: 40px 16px;
        text-align: center;
        color: var(--el-text-color-placeholder);
        font-size: 14px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-header {
    padding: 0 12px;

    .header-left {
      gap: 12px;

      .breadcrumb {
        display: none;
      }
    }

    .header-right {
      gap: 8px;

      .search-box {
        width: 180px;
      }

      .user-info {
        .username {
          display: none;
        }
      }
    }
  }
}
</style>
