<template>
  <el-drawer
    v-model="visible"
    title="通知中心"
    :size="400"
    direction="rtl"
    class="notification-drawer"
  >
    <!-- 头部操作栏 -->
    <template #header>
      <div class="drawer-header">
        <span class="drawer-title">通知中心</span>
        <div class="drawer-actions">
          <el-button
            v-if="unreadCount > 0"
            type="text"
            size="small"
            @click="handleMarkAllRead"
          >
            全部已读
          </el-button>
          <el-button
            type="text"
            size="small"
            @click="goToNotificationCenter"
          >
            查看全部
          </el-button>
        </div>
      </div>
    </template>

    <!-- 过滤标签 -->
    <div class="filter-tabs">
      <el-radio-group v-model="filterType" size="small">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="unread">
          未读
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 通知列表 -->
    <div v-loading="loading" class="notification-list">
      <!-- 空状态 -->
      <Empty
        v-if="displayNotifications.length === 0"
        description="暂无通知"
        :image-size="80"
      >
        <el-button type="primary" @click="handleRefresh">刷新</el-button>
      </Empty>

      <!-- 通知项 -->
      <div
        v-for="notification in displayNotifications"
        :key="notification.id"
        class="notification-item"
        :class="{ 'is-unread': !notification.read }"
        @click="handleNotificationClick(notification)"
      >
        <!-- 图标 -->
        <div class="notification-icon" :class="`icon-${notification.type}`">
          <el-icon>
            <Bell v-if="notification.type === 'system'" />
            <User v-else-if="notification.type === 'user'" />
            <Warning v-else-if="notification.type === 'warning'" />
            <SuccessFilled v-else-if="notification.type === 'success'" />
            <InfoFilled v-else />
          </el-icon>
        </div>

        <!-- 内容 -->
        <div class="notification-content">
          <div class="notification-title">
            {{ notification.title }}
            <span v-if="!notification.read" class="unread-dot" />
          </div>
          <div class="notification-message">
            {{ notification.content }}
          </div>
          <div class="notification-time">
            {{ formatTime(notification.createdAt) }}
          </div>
        </div>

        <!-- 操作 -->
        <div class="notification-actions" @click.stop>
          <el-button
            v-if="!notification.read"
            type="text"
            size="small"
            @click="handleMarkRead(notification.id)"
          >
            标记已读
          </el-button>
          <el-button
            type="text"
            size="small"
            @click="handleDelete(notification.id)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 底部操作 -->
    <template v-if="notifications.length > 0" #footer>
      <div class="drawer-footer">
        <el-button @click="handleClearAll">清空所有</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  Bell,
  User,
  Warning,
  SuccessFilled,
  InfoFilled,
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Empty } from '@/components/ui';
import { useNotification } from '@/composables/useNotification';
import type { Notification } from '@/types/notification';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

/**
 * 通知抽屉组件
 * 显示最近的通知列表
 */

interface Props {
  modelValue: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'notification-click': [notification: Notification];
}>();

const router = useRouter();

// 使用通知composable
const {
  notifications,
  unreadCount,
  loading,
  unreadNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  clearAll,
  fetchNotifications,
  handleNotificationClick: handleClick,
} = useNotification();

// 状态
const visible = ref(props.modelValue);
const filterType = ref<'all' | 'unread'>('all');

// 显示的通知列表
const displayNotifications = computed(() => {
  const list = filterType.value === 'unread' ? unreadNotifications.value : notifications.value;
  return list.slice(0, 20); // 只显示最近20条
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow();
};

// 刷新
const handleRefresh = () => {
  fetchNotifications();
};

// 标记已读
const handleMarkRead = async (notificationId: number | string) => {
  await markAsRead(notificationId);
  ElMessage.success('已标记为已读');
};

// 全部标记已读
const handleMarkAllRead = async () => {
  await markAllAsRead();
};

// 删除通知
const handleDelete = async (notificationId: number | string) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await deleteNotification(notificationId);
    ElMessage.success('删除成功');
  } catch (error) {
    // 用户取消
  }
};

// 清空所有
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await clearAll();
  } catch (error) {
    // 用户取消
  }
};

// 通知点击
const handleNotificationClick = (notification: Notification) => {
  handleClick(notification);
  emit('notification-click', notification);
};

// 前往通知中心
const goToNotificationCenter = () => {
  visible.value = false;
  router.push('/notifications');
};

// 监听modelValue变化
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal;
  },
);

watch(visible, (newVal) => {
  emit('update:modelValue', newVal);
});
</script>

<style lang="scss" scoped>
.notification-drawer {
  .drawer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    .drawer-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .drawer-actions {
      display: flex;
      gap: 8px;
    }
  }

  .filter-tabs {
    margin-bottom: 16px;

    .unread-badge {
      margin-left: 4px;
      :deep(.el-badge__content) {
        position: static;
        transform: none;
      }
    }
  }

  .notification-list {
    flex: 1;
    overflow-y: auto;

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

  .drawer-footer {
    display: flex;
    justify-content: center;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>

