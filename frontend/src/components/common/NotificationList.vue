<template>
  <div class="notification-list">
    <div v-if="notifications.length === 0" class="empty-state">
      <el-empty description="暂无通知" />
    </div>

    <div v-else class="notification-items">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{
          'is-unread': !notification.read,
          [`type-${notification.type}`]: true,
        }"
        @click="handleNotificationClick(notification)"
      >
        <!-- 通知图标 -->
        <div class="notification-icon">
          <el-icon :class="`icon-${notification.type}`">
            <Bell v-if="notification.type === 'system'" />
            <User v-else-if="notification.type === 'user'" />
            <Warning v-else-if="notification.type === 'warning'" />
            <SuccessFilled v-else-if="notification.type === 'success'" />
            <InfoFilled v-else />
          </el-icon>
        </div>

        <!-- 通知内容 -->
        <div class="notification-content">
          <div class="notification-title">
            {{ notification.title }}
            <span v-if="!notification.read" class="unread-dot" />
          </div>
          <div class="notification-message">
            {{ notification.content }}
          </div>
          <div class="notification-time">
            {{ formatTime(notification.time) }}
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="notification-actions">
          <el-button
            v-if="!notification.read"
            type="text"
            size="small"
            @click.stop="markAsRead(notification)"
          >
            标记已读
          </el-button>
          <el-button
            type="text"
            size="small"
            @click.stop="deleteNotification(notification)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 批量操作 -->
    <div v-if="notifications.length > 0" class="batch-actions">
      <el-button type="text" @click="markAllAsRead"> 全部标记已读 </el-button>
      <el-button type="text" @click="clearAll"> 清空所有 </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Bell,
  User,
  Warning,
  SuccessFilled,
  InfoFilled,
} from "@element-plus/icons-vue";

// 定义通知接口
interface Notification {
  id: number;
  title: string;
  content: string;
  type: "system" | "user" | "warning" | "success" | "info";
  read: boolean;
  time: string;
}

// Props
interface Props {
  notifications: Notification[];
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  "notification-click": [notification: Notification];
  "mark-read": [notification: Notification];
  delete: [notification: Notification];
  "mark-all-read": [];
  "clear-all": [];
}>();

// 方法
const handleNotificationClick = (notification: Notification) => {
  emit("notification-click", notification);
};

const markAsRead = (notification: Notification) => {
  emit("mark-read", notification);
  ElMessage.success("已标记为已读");
};

const deleteNotification = async (notification: Notification) => {
  try {
    await ElMessageBox.confirm("确定要删除这条通知吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    emit("delete", notification);
    ElMessage.success("删除成功");
  } catch (error) {
    // 用户取消
  }
};

const markAllAsRead = () => {
  emit("mark-all-read");
  ElMessage.success("已全部标记为已读");
};

const clearAll = async () => {
  try {
    await ElMessageBox.confirm("确定要清空所有通知吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    emit("clear-all");
    ElMessage.success("已清空所有通知");
  } catch (error) {
    // 用户取消
  }
};

const formatTime = (time: string) => {
  const now = new Date();
  const notificationTime = new Date(time);
  const diff = now.getTime() - notificationTime.getTime();

  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 1) {
    return "刚刚";
  } else if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return time;
  }
};
</script>

<style lang="scss" scoped>
.notification-list {
  .empty-state {
    padding: 40px 20px;
    text-align: center;
  }

  .notification-items {
    .notification-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      cursor: pointer;
      transition: background-color 0.2s;

      &:hover {
        background: var(--el-bg-color-page);
      }

      &.is-unread {
        background: var(--el-color-primary-light-9);

        .notification-title {
          font-weight: 600;
        }
      }

      .notification-icon {
        flex-shrink: 0;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;

        .el-icon {
          font-size: 16px;
        }

        &.icon-system {
          background: var(--el-color-info-light-8);
          color: var(--el-color-info);
        }

        &.icon-user {
          background: var(--el-color-primary-light-8);
          color: var(--el-color-primary);
        }

        &.icon-warning {
          background: var(--el-color-warning-light-8);
          color: var(--el-color-warning);
        }

        &.icon-success {
          background: var(--el-color-success-light-8);
          color: var(--el-color-success);
        }

        &.icon-info {
          background: var(--el-color-info-light-8);
          color: var(--el-color-info);
        }
      }

      .notification-content {
        flex: 1;
        min-width: 0;

        .notification-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;

          .unread-dot {
            width: 6px;
            height: 6px;
            background: var(--el-color-danger);
            border-radius: 50%;
            flex-shrink: 0;
          }
        }

        .notification-message {
          font-size: 13px;
          color: var(--el-text-color-regular);
          line-height: 1.4;
          margin-bottom: 8px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .notification-time {
          font-size: 12px;
          color: var(--el-text-color-placeholder);
        }
      }

      .notification-actions {
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        gap: 4px;
        opacity: 0;
        transition: opacity 0.2s;

        .el-button {
          font-size: 12px;
          padding: 4px 8px;
        }
      }

      &:hover .notification-actions {
        opacity: 1;
      }
    }
  }

  .batch-actions {
    padding: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
    display: flex;
    justify-content: center;
    gap: 16px;

    .el-button {
      font-size: 13px;
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .notification-list {
    .notification-items .notification-item {
      padding: 12px;

      .notification-actions {
        opacity: 1;
        flex-direction: row;
        gap: 8px;
      }
    }
  }
}
</style>
