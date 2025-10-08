// 通知状态管理

import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Notification, NotificationFilter, NotificationType } from '@/types/notification';
import { notificationApi } from '@/api/modules/notification';
import { ElNotification } from 'element-plus';

export const useNotificationStore = defineStore(
  'notification',
  () => {
    // 状态
    const notifications = ref<Notification[]>([]);
    const unreadCount = ref(0);
    const loading = ref(false);
    const pollingInterval = ref<number | null>(null);
    const filter = ref<NotificationFilter>({
      readStatus: 'all',
    });

    // 计算属性
    const unreadNotifications = computed(() =>
      notifications.value.filter((n) => !n.read),
    );

    const readNotifications = computed(() =>
      notifications.value.filter((n) => n.read),
    );

    const filteredNotifications = computed(() => {
      let result = notifications.value;

      // 按已读状态过滤
      if (filter.value.readStatus === 'read') {
        result = result.filter((n) => n.read);
      } else if (filter.value.readStatus === 'unread') {
        result = result.filter((n) => !n.read);
      }

      // 按类型过滤
      if (filter.value.types && filter.value.types.length > 0) {
        result = result.filter((n) => filter.value.types?.includes(n.type));
      }

      // 按优先级过滤
      if (filter.value.priorities && filter.value.priorities.length > 0) {
        result = result.filter((n) => filter.value.priorities?.includes(n.priority!));
      }

      // 按日期过滤
      if (filter.value.dateRange) {
        const { start, end } = filter.value.dateRange;
        result = result.filter((n) => {
          const createdAt = new Date(n.createdAt);
          return createdAt >= new Date(start) && createdAt <= new Date(end);
        });
      }

      return result;
    });

    /**
     * 获取通知列表
     */
    const fetchNotifications = async (params?: any) => {
      try {
        loading.value = true;
        const response = await notificationApi.getUserNotifications({
          page: 1,
          size: 50,
          ...params,
        });

        if (response.success) {
          notifications.value = response.data.list;
          return response.data;
        }
      } catch (error) {
        console.error('获取通知列表失败:', error);
      } finally {
        loading.value = false;
      }
    };

    /**
     * 获取未读数量
     */
    const fetchUnreadCount = async () => {
      try {
        const response = await notificationApi.getUnreadCount();
        if (response.success) {
          unreadCount.value = response.data;
        }
      } catch (error) {
        console.error('获取未读数量失败:', error);
      }
    };

    /**
     * 标记为已读
     */
    const markAsRead = async (notificationId: number | string) => {
      try {
        const response = await notificationApi.markAsRead(notificationId);
        if (response.success) {
          // 更新本地状态
          const notification = notifications.value.find((n) => n.id === notificationId);
          if (notification) {
            notification.read = true;
          }
          unreadCount.value = Math.max(0, unreadCount.value - 1);
          return true;
        }
        return false;
      } catch (error) {
        console.error('标记已读失败:', error);
        return false;
      }
    };

    /**
     * 批量标记为已读
     */
    const markBatchAsRead = async (notificationIds: (number | string)[]) => {
      try {
        const response = await notificationApi.markBatchAsRead(notificationIds);
        if (response.success) {
          // 更新本地状态
          notificationIds.forEach((id) => {
            const notification = notifications.value.find((n) => n.id === id);
            if (notification && !notification.read) {
              notification.read = true;
              unreadCount.value = Math.max(0, unreadCount.value - 1);
            }
          });
          return response.data;
        }
      } catch (error) {
        console.error('批量标记已读失败:', error);
      }
    };

    /**
     * 全部标记为已读
     */
    const markAllAsRead = async () => {
      try {
        const response = await notificationApi.markAllAsRead();
        if (response.success) {
          // 更新本地状态
          notifications.value.forEach((n) => {
            n.read = true;
          });
          unreadCount.value = 0;
          ElNotification.success({
            title: '成功',
            message: '已全部标记为已读',
          });
          return response.data;
        }
      } catch (error) {
        console.error('全部标记已读失败:', error);
      }
    };

    /**
     * 删除通知
     */
    const deleteNotification = async (notificationId: number | string) => {
      try {
        const response = await notificationApi.deleteNotification(notificationId);
        if (response.success) {
          // 从列表中移除
          const index = notifications.value.findIndex((n) => n.id === notificationId);
          if (index > -1) {
            const notification = notifications.value[index];
            if (!notification.read) {
              unreadCount.value = Math.max(0, unreadCount.value - 1);
            }
            notifications.value.splice(index, 1);
          }
          return true;
        }
        return false;
      } catch (error) {
        console.error('删除通知失败:', error);
        return false;
      }
    };

    /**
     * 清空所有通知
     */
    const clearAll = async () => {
      try {
        const response = await notificationApi.clearAllNotifications();
        if (response.success) {
          notifications.value = [];
          unreadCount.value = 0;
          ElNotification.success({
            title: '成功',
            message: '已清空所有通知',
          });
          return response.data;
        }
      } catch (error) {
        console.error('清空通知失败:', error);
      }
    };

    /**
     * 添加通知（用于实时推送）
     */
    const addNotification = (notification: Notification) => {
      // 检查是否已存在
      const exists = notifications.value.some((n) => n.id === notification.id);
      if (!exists) {
        notifications.value.unshift(notification);
        if (!notification.read) {
          unreadCount.value++;
        }

        // 显示桌面通知
        if (Notification.permission === 'granted') {
          new Notification(notification.title, {
            body: notification.content,
            icon: '/favicon.ico',
          });
        }

        // 显示Element Plus通知
        ElNotification({
          title: notification.title,
          message: notification.content,
          type: notification.type === 'error' ? 'error' : notification.type === 'warning' ? 'warning' : 'info',
          duration: 4500,
        });
      }
    };

    /**
     * 设置过滤器
     */
    const setFilter = (newFilter: Partial<NotificationFilter>) => {
      filter.value = { ...filter.value, ...newFilter };
    };

    /**
     * 重置过滤器
     */
    const resetFilter = () => {
      filter.value = {
        readStatus: 'all',
      };
    };

    /**
     * 开始轮询
     */
    const startPolling = (intervalMs: number = 30000) => {
      // 停止旧的轮询
      stopPolling();

      // 立即获取一次
      fetchUnreadCount();

      // 开始新的轮询
      pollingInterval.value = window.setInterval(() => {
        fetchUnreadCount();
      }, intervalMs);
    };

    /**
     * 停止轮询
     */
    const stopPolling = () => {
      if (pollingInterval.value) {
        clearInterval(pollingInterval.value);
        pollingInterval.value = null;
      }
    };

    /**
     * 初始化
     */
    const init = async () => {
      await fetchNotifications();
      await fetchUnreadCount();
      startPolling();

      // 请求桌面通知权限
      if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
      }
    };

    /**
     * 清理
     */
    const cleanup = () => {
      stopPolling();
    };

    return {
      // 状态
      notifications,
      unreadCount,
      loading,
      filter,

      // 计算属性
      unreadNotifications,
      readNotifications,
      filteredNotifications,

      // 方法
      fetchNotifications,
      fetchUnreadCount,
      markAsRead,
      markBatchAsRead,
      markAllAsRead,
      deleteNotification,
      clearAll,
      addNotification,
      setFilter,
      resetFilter,
      startPolling,
      stopPolling,
      init,
      cleanup,
    };
  },
  {
    persist: {
      key: 'notification-store',
      storage: localStorage,
      paths: ['filter'],
    },
  },
);

