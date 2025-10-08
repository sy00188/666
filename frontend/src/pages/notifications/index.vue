<template>
  <div class="notifications-page">
    <Card title="通知中心" hoverable>
      <!-- 操作栏 -->
      <template #extra>
        <div class="page-actions">
          <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
          <el-button
            v-if="unreadCount > 0"
            type="primary"
            @click="handleMarkAllRead"
          >
            全部标记已读
          </el-button>
          <el-button
            v-if="notifications.length > 0"
            :icon="Delete"
            @click="handleClearAll"
          >
            清空所有
          </el-button>
        </div>
      </template>

      <!-- 过滤器 -->
      <div class="filter-section">
        <el-space wrap>
          <el-radio-group v-model="filterStatus" size="default">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="unread">
              未读 ({{ unreadCount }})
            </el-radio-button>
            <el-radio-button label="read">已读</el-radio-button>
          </el-radio-group>

          <el-select
            v-model="filterTypes"
            multiple
            placeholder="选择通知类型"
            clearable
            style="width: 240px"
          >
            <el-option label="系统通知" value="system" />
            <el-option label="用户相关" value="user" />
            <el-option label="档案相关" value="archive" />
            <el-option label="借阅相关" value="borrow" />
            <el-option label="审批相关" value="approval" />
          </el-select>

          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 300px"
          />

          <el-button @click="handleResetFilter">重置筛选</el-button>
        </el-space>
      </div>

      <!-- 通知列表 -->
      <div v-loading="loading" class="notification-list">
        <!-- 空状态 -->
        <Empty
          v-if="filteredNotifications.length === 0"
          description="暂无通知"
        />

        <!-- 通知项 -->
        <div
          v-for="notification in paginatedNotifications"
          :key="notification.id"
          class="notification-card"
          :class="{ 'is-unread': !notification.read }"
        >
          <div class="card-header">
            <div class="header-left">
              <el-icon
                :size="20"
                :class="`type-icon icon-${notification.type}`"
              >
                <Bell v-if="notification.type === 'system'" />
                <User v-else-if="notification.type === 'user'" />
                <Warning v-else-if="notification.type === 'warning'" />
                <SuccessFilled v-else-if="notification.type === 'success'" />
                <InfoFilled v-else />
              </el-icon>
              <span class="notification-title">{{ notification.title }}</span>
              <Badge
                v-if="!notification.read"
                value="未读"
                type="danger"
              />
            </div>
            <div class="header-right">
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
          </div>

          <div class="card-content">
            <p class="notification-content">{{ notification.content }}</p>
          </div>

          <div class="card-footer">
            <div class="footer-left">
              <el-tag v-if="notification.priority" :type="getPriorityType(notification.priority)" size="small">
                {{ getPriorityLabel(notification.priority) }}
              </el-tag>
            </div>
            <div class="footer-right">
              <el-button
                v-if="!notification.read"
                type="text"
                size="small"
                @click="handleMarkRead(notification.id)"
              >
                标记已读
              </el-button>
              <el-button
                v-if="notification.actionUrl"
                type="text"
                size="small"
                @click="handleNotificationClick(notification)"
              >
                查看详情
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
      </div>

      <!-- 分页 -->
      <div v-if="filteredNotifications.length > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredNotifications.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  Bell,
  User,
  Warning,
  SuccessFilled,
  InfoFilled,
  Refresh,
  Delete,
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Card, Badge, Empty } from '@/components/ui';
import { useNotification } from '@/composables/useNotification';
import type { Notification, NotificationType, NotificationPriority } from '@/types/notification';
import dayjs from 'dayjs';

/**
 * 通知中心页面
 * 显示所有通知，支持筛选、分页等功能
 */

const router = useRouter();

// 使用通知composable
const {
  notifications,
  unreadCount,
  loading,
  filteredNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  clearAll,
  fetchNotifications,
  setFilter,
  resetFilter,
  handleNotificationClick: handleClick,
} = useNotification();

// 筛选状态
const filterStatus = ref<'all' | 'unread' | 'read'>('all');
const filterTypes = ref<NotificationType[]>([]);
const dateRange = ref<[string, string] | null>(null);

// 分页
const currentPage = ref(1);
const pageSize = ref(20);

// 分页后的通知
const paginatedNotifications = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredNotifications.value.slice(start, end);
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 获取优先级类型
const getPriorityType = (priority: NotificationPriority) => {
  const map: Record<NotificationPriority, any> = {
    low: 'info',
    normal: '',
    high: 'warning',
    urgent: 'danger',
  };
  return map[priority] || '';
};

// 获取优先级标签
const getPriorityLabel = (priority: NotificationPriority) => {
  const map: Record<NotificationPriority, string> = {
    low: '低',
    normal: '普通',
    high: '高',
    urgent: '紧急',
  };
  return map[priority] || '';
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
    await ElMessageBox.confirm('确定要清空所有通知吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    await clearAll();
  } catch (error) {
    // 用户取消
  }
};

// 重置筛选
const handleResetFilter = () => {
  filterStatus.value = 'all';
  filterTypes.value = [];
  dateRange.value = null;
  resetFilter();
  currentPage.value = 1;
};

// 通知点击
const handleNotificationClick = (notification: Notification) => {
  handleClick(notification);
  
  // 如果有操作URL，跳转
  if (notification.actionUrl) {
    router.push(notification.actionUrl);
  }
};

// 应用筛选
const applyFilter = () => {
  setFilter({
    readStatus: filterStatus.value,
    types: filterTypes.value.length > 0 ? filterTypes.value : undefined,
    dateRange: dateRange.value ? {
      start: dateRange.value[0],
      end: dateRange.value[1],
    } : undefined,
  });
};

// 监听筛选条件变化
watch([filterStatus, filterTypes, dateRange], () => {
  applyFilter();
  currentPage.value = 1; // 重置到第一页
});

// 初始化
onMounted(() => {
  fetchNotifications();
});
</script>

<style lang="scss" scoped>
.notifications-page {
  padding: 20px;

  .page-actions {
    display: flex;
    gap: 12px;
  }

  .filter-section {
    margin-bottom: 20px;
  }

  .notification-list {
    margin-bottom: 20px;

    .notification-card {
      padding: 20px;
      margin-bottom: 12px;
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-light);
      border-radius: 8px;
      transition: all 0.2s;

      &:hover {
        box-shadow: var(--el-box-shadow-light);
      }

      &.is-unread {
        background: var(--el-color-primary-light-9);
        border-color: var(--el-color-primary-light-7);
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .header-left {
          display: flex;
          align-items: center;
          gap: 12px;

          .type-icon {
            &.icon-system {
              color: var(--el-color-info);
            }
            &.icon-user {
              color: var(--el-color-primary);
            }
            &.icon-warning {
              color: var(--el-color-warning);
            }
            &.icon-success {
              color: var(--el-color-success);
            }
            &.icon-info {
              color: var(--el-color-info);
            }
          }

          .notification-title {
            font-size: 16px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }
        }

        .header-right {
          .notification-time {
            font-size: 14px;
            color: var(--el-text-color-secondary);
          }
        }
      }

      .card-content {
        margin-bottom: 16px;

        .notification-content {
          font-size: 14px;
          color: var(--el-text-color-regular);
          line-height: 1.6;
          margin: 0;
        }
      }

      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .footer-left,
        .footer-right {
          display: flex;
          gap: 8px;
        }
      }
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .notifications-page {
    padding: 12px;

    .filter-section {
      :deep(.el-space) {
        width: 100%;

        .el-select,
        .el-date-picker {
          width: 100% !important;
        }
      }
    }

    .notification-list {
      .notification-card {
        padding: 16px;

        .card-header {
          flex-direction: column;
          align-items: flex-start;
          gap: 8px;
        }

        .card-footer {
          flex-direction: column;
          gap: 12px;

          .footer-left,
          .footer-right {
            width: 100%;
            justify-content: flex-start;
          }
        }
      }
    }
  }
}
</style>

