// 通知Composable

import { computed } from 'vue';
import { useNotificationStore } from '@/stores/notification';
import type { Notification, NotificationFilter } from '@/types/notification';

/**
 * 通知Composable
 * 提供通知相关的逻辑和状态管理
 */
export function useNotification() {
  const notificationStore = useNotificationStore();

  // 状态
  const notifications = computed(() => notificationStore.notifications);
  const unreadCount = computed(() => notificationStore.unreadCount);
  const loading = computed(() => notificationStore.loading);
  const unreadNotifications = computed(() => notificationStore.unreadNotifications);
  const readNotifications = computed(() => notificationStore.readNotifications);
  const filteredNotifications = computed(() => notificationStore.filteredNotifications);

  /**
   * 获取通知列表
   */
  const fetchNotifications = (params?: any) => {
    return notificationStore.fetchNotifications(params);
  };

  /**
   * 刷新未读数量
   */
  const refreshUnreadCount = () => {
    return notificationStore.fetchUnreadCount();
  };

  /**
   * 标记为已读
   */
  const markAsRead = (notificationId: number | string) => {
    return notificationStore.markAsRead(notificationId);
  };

  /**
   * 批量标记为已读
   */
  const markBatchAsRead = (notificationIds: (number | string)[]) => {
    return notificationStore.markBatchAsRead(notificationIds);
  };

  /**
   * 全部标记为已读
   */
  const markAllAsRead = () => {
    return notificationStore.markAllAsRead();
  };

  /**
   * 删除通知
   */
  const deleteNotification = (notificationId: number | string) => {
    return notificationStore.deleteNotification(notificationId);
  };

  /**
   * 清空所有通知
   */
  const clearAll = () => {
    return notificationStore.clearAll();
  };

  /**
   * 设置过滤器
   */
  const setFilter = (filter: Partial<NotificationFilter>) => {
    notificationStore.setFilter(filter);
  };

  /**
   * 重置过滤器
   */
  const resetFilter = () => {
    notificationStore.resetFilter();
  };

  /**
   * 处理通知点击
   */
  const handleNotificationClick = (notification: Notification) => {
    // 标记为已读
    if (!notification.read) {
      markAsRead(notification.id);
    }

    // 如果有操作URL，跳转
    if (notification.actionUrl) {
      window.location.href = notification.actionUrl;
    }
  };

  /**
   * 开始轮询
   */
  const startPolling = (intervalMs?: number) => {
    notificationStore.startPolling(intervalMs);
  };

  /**
   * 停止轮询
   */
  const stopPolling = () => {
    notificationStore.stopPolling();
  };

  /**
   * 初始化
   */
  const init = () => {
    return notificationStore.init();
  };

  /**
   * 清理
   */
  const cleanup = () => {
    notificationStore.cleanup();
  };

  return {
    // 状态
    notifications,
    unreadCount,
    loading,
    unreadNotifications,
    readNotifications,
    filteredNotifications,

    // 方法
    fetchNotifications,
    refreshUnreadCount,
    markAsRead,
    markBatchAsRead,
    markAllAsRead,
    deleteNotification,
    clearAll,
    setFilter,
    resetFilter,
    handleNotificationClick,
    startPolling,
    stopPolling,
    init,
    cleanup,
  };
}

