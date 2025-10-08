// 通知相关API

import { request } from '@/utils/request';
import type {
  Notification,
  NotificationQueryParams,
  NotificationStatistics,
  NotificationSettings,
  NotificationBatchResult,
} from '@/types/notification';
import type { ApiResponse, PaginatedResponse } from '@/types/common';

/**
 * 获取用户通知列表
 */
export function getUserNotifications(params: NotificationQueryParams) {
  return request.get<ApiResponse<PaginatedResponse<Notification>>>(
    '/notifications',
    { params },
  );
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount(userId?: number) {
  return request.get<ApiResponse<number>>('/notifications/unread-count', {
    params: { userId },
  });
}

/**
 * 标记通知为已读
 */
export function markAsRead(notificationId: number | string) {
  return request.put<ApiResponse<boolean>>(
    `/notifications/${notificationId}/read`,
  );
}

/**
 * 批量标记为已读
 */
export function markBatchAsRead(notificationIds: (number | string)[]) {
  return request.put<ApiResponse<NotificationBatchResult>>(
    '/notifications/batch/read',
    { notificationIds },
  );
}

/**
 * 全部标记为已读
 */
export function markAllAsRead() {
  return request.put<ApiResponse<NotificationBatchResult>>(
    '/notifications/all/read',
  );
}

/**
 * 删除通知
 */
export function deleteNotification(notificationId: number | string) {
  return request.delete<ApiResponse<boolean>>(
    `/notifications/${notificationId}`,
  );
}

/**
 * 批量删除通知
 */
export function batchDeleteNotifications(notificationIds: (number | string)[]) {
  return request.delete<ApiResponse<NotificationBatchResult>>(
    '/notifications/batch',
    { data: { notificationIds } },
  );
}

/**
 * 清空所有通知
 */
export function clearAllNotifications() {
  return request.delete<ApiResponse<NotificationBatchResult>>(
    '/notifications/all',
  );
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(notificationId: number | string) {
  return request.get<ApiResponse<Notification>>(
    `/notifications/${notificationId}`,
  );
}

/**
 * 获取通知统计信息
 */
export function getNotificationStatistics() {
  return request.get<ApiResponse<NotificationStatistics>>(
    '/notifications/statistics',
  );
}

/**
 * 获取通知设置
 */
export function getNotificationSettings() {
  return request.get<ApiResponse<NotificationSettings>>(
    '/notifications/settings',
  );
}

/**
 * 更新通知设置
 */
export function updateNotificationSettings(settings: Partial<NotificationSettings>) {
  return request.put<ApiResponse<NotificationSettings>>(
    '/notifications/settings',
    settings,
  );
}

/**
 * 发送通知（管理员功能）
 */
export function sendNotification(notification: Partial<Notification>) {
  return request.post<ApiResponse<Notification>>(
    '/notifications/send',
    notification,
  );
}

/**
 * 批量发送通知（管理员功能）
 */
export function sendBatchNotifications(data: {
  userIds: number[];
  notification: Partial<Notification>;
}) {
  return request.post<ApiResponse<NotificationBatchResult>>(
    '/notifications/send/batch',
    data,
  );
}

/**
 * 发送广播通知（管理员功能）
 */
export function sendBroadcastNotification(notification: Partial<Notification>) {
  return request.post<ApiResponse<NotificationBatchResult>>(
    '/notifications/send/broadcast',
    notification,
  );
}

export const notificationApi = {
  getUserNotifications,
  getUnreadCount,
  markAsRead,
  markBatchAsRead,
  markAllAsRead,
  deleteNotification,
  batchDeleteNotifications,
  clearAllNotifications,
  getNotificationDetail,
  getNotificationStatistics,
  getNotificationSettings,
  updateNotificationSettings,
  sendNotification,
  sendBatchNotifications,
  sendBroadcastNotification,
};

