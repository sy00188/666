/**
 * WebSocket连接管理类
 * 用于实时通知推送
 */

import SockJS from 'sockjs-client';
import { Client, type IMessage } from '@stomp/stompjs';
import { ElNotification } from 'element-plus';
import type { Notification } from '@/types/notification';

export type WebSocketMessageType =
  | 'NEW_NOTIFICATION'
  | 'BROADCAST_NOTIFICATION'
  | 'UNREAD_COUNT_UPDATE'
  | 'UNREAD_COUNT_UPDATE_REQUEST'
  | 'MARK_READ_ACK'
  | 'CONNECT_ACK'
  | 'CONNECT_ERROR';

export interface WebSocketMessage {
  type: WebSocketMessageType;
  notification?: Notification;
  unreadCount?: number;
  timestamp: string;
  message?: string;
  notificationId?: number;
}

export interface WebSocketConnectionOptions {
  url: string;
  userId: string;
  onMessage?: (message: WebSocketMessage) => void;
  onConnected?: () => void;
  onDisconnected?: () => void;
  onError?: (error: any) => void;
  reconnectDelay?: number;
  maxReconnectAttempts?: number;
}

/**
 * WebSocket连接管理器
 */
export class WebSocketManager {
  private client: Client | null = null;
  private options: WebSocketConnectionOptions;
  private reconnectAttempts = 0;
  private reconnectTimer: number | null = null;
  private isManualDisconnect = false;

  constructor(options: WebSocketConnectionOptions) {
    this.options = {
      reconnectDelay: 3000,
      maxReconnectAttempts: 10,
      ...options,
    };
  }

  /**
   * 连接WebSocket
   */
  connect(): void {
    if (this.client && this.client.connected) {
      console.log('WebSocket已经连接');
      return;
    }

    this.isManualDisconnect = false;

    try {
      // 创建SockJS实例
      const socket = new SockJS(this.options.url);

      // 创建STOMP客户端
      this.client = new Client({
        webSocketFactory: () => socket as any,
        
        // 连接成功回调
        onConnect: (frame) => {
          console.log('WebSocket连接成功:', frame);
          this.reconnectAttempts = 0;
          this.subscribeToChannels();
          this.options.onConnected?.();

          // 发送连接消息
          this.sendConnectMessage();
        },

        // 断开连接回调
        onDisconnect: () => {
          console.log('WebSocket断开连接');
          this.options.onDisconnected?.();

          // 如果不是手动断开，则尝试重连
          if (!this.isManualDisconnect) {
            this.scheduleReconnect();
          }
        },

        // 错误回调
        onStompError: (frame) => {
          console.error('WebSocket STOMP错误:', frame);
          this.options.onError?.(frame);
        },

        // 连接头
        connectHeaders: {
          userId: this.options.userId,
        },

        // 心跳配置
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,

        // 调试日志
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log('[WebSocket Debug]', str);
          }
        },
      });

      // 激活连接
      this.client.activate();

    } catch (error) {
      console.error('WebSocket连接失败:', error);
      this.options.onError?.(error);
      this.scheduleReconnect();
    }
  }

  /**
   * 订阅消息频道
   */
  private subscribeToChannels(): void {
    if (!this.client) return;

    // 订阅用户私有通知频道
    this.client.subscribe(`/user/queue/notifications`, (message: IMessage) => {
      this.handleMessage(message);
    });

    // 订阅用户未读数量频道
    this.client.subscribe(`/user/queue/notification-count`, (message: IMessage) => {
      this.handleMessage(message);
    });

    // 订阅用户响应频道
    this.client.subscribe(`/user/queue/notification/response`, (message: IMessage) => {
      this.handleMessage(message);
    });

    // 订阅广播通知频道
    this.client.subscribe(`/topic/notifications`, (message: IMessage) => {
      this.handleMessage(message);
    });

    console.log('WebSocket频道订阅成功');
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(message: IMessage): void {
    try {
      const data: WebSocketMessage = JSON.parse(message.body);
      console.log('收到WebSocket消息:', data);

      // 调用外部回调
      this.options.onMessage?.(data);

      // 根据消息类型处理
      this.handleMessageByType(data);
    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
    }
  }

  /**
   * 根据消息类型处理
   */
  private handleMessageByType(data: WebSocketMessage): void {
    switch (data.type) {
      case 'NEW_NOTIFICATION':
        this.handleNewNotification(data);
        break;
      case 'BROADCAST_NOTIFICATION':
        this.handleBroadcastNotification(data);
        break;
      case 'UNREAD_COUNT_UPDATE':
        this.handleUnreadCountUpdate(data);
        break;
      case 'UNREAD_COUNT_UPDATE_REQUEST':
        // 客户端需要重新获取未读数量
        console.log('服务器请求更新未读数量');
        break;
      case 'MARK_READ_ACK':
        console.log('标记已读确认:', data.notificationId);
        break;
      case 'CONNECT_ACK':
        console.log('连接确认:', data.message);
        break;
      default:
        console.log('未处理的消息类型:', data.type);
    }
  }

  /**
   * 处理新通知
   */
  private handleNewNotification(data: WebSocketMessage): void {
    if (!data.notification) return;

    ElNotification({
      title: data.notification.title,
      message: data.notification.content,
      type: this.getNotificationType(data.notification),
      duration: 4500,
      position: 'top-right',
    });

    // 如果浏览器支持桌面通知
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(data.notification.title, {
        body: data.notification.content,
        icon: '/favicon.ico',
        tag: `notification-${data.notification.id}`,
      });
    }
  }

  /**
   * 处理广播通知
   */
  private handleBroadcastNotification(data: WebSocketMessage): void {
    if (!data.notification) return;

    ElNotification({
      title: `📢 ${data.notification.title}`,
      message: data.notification.content,
      type: 'warning',
      duration: 6000,
      position: 'top-right',
    });
  }

  /**
   * 处理未读数量更新
   */
  private handleUnreadCountUpdate(data: WebSocketMessage): void {
    console.log('未读数量更新:', data.unreadCount);
  }

  /**
   * 获取通知类型
   */
  private getNotificationType(notification: Notification): 'success' | 'warning' | 'info' | 'error' {
    if (notification.type === 'error') return 'error';
    if (notification.type === 'warning') return 'warning';
    if (notification.type === 'success') return 'success';
    return 'info';
  }

  /**
   * 发送连接消息
   */
  private sendConnectMessage(): void {
    this.send('/app/notification/connect', {
      userId: this.options.userId,
      timestamp: new Date().toISOString(),
    });
  }

  /**
   * 标记通知为已读
   */
  markAsRead(notificationId: number): void {
    this.send('/app/notification/markRead', {
      notificationId,
    });
  }

  /**
   * 批量标记通知为已读
   */
  markBatchAsRead(notificationIds: number[]): void {
    this.send('/app/notification/markBatchRead', {
      notificationIds,
    });
  }

  /**
   * 标记所有通知为已读
   */
  markAllAsRead(): void {
    this.send('/app/notification/markAllRead', {});
  }

  /**
   * 获取未读数量
   */
  getUnreadCount(): void {
    this.send('/app/notification/getUnreadCount', {});
  }

  /**
   * 发送消息
   */
  private send(destination: string, body: any): void {
    if (!this.client || !this.client.connected) {
      console.warn('WebSocket未连接，无法发送消息');
      return;
    }

    try {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
    } catch (error) {
      console.error('发送WebSocket消息失败:', error);
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.isManualDisconnect = true;

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    if (this.client) {
      // 发送断开连接消息
      this.send('/app/notification/disconnect', {
        userId: this.options.userId,
      });

      this.client.deactivate();
      this.client = null;
    }

    console.log('WebSocket已断开');
  }

  /**
   * 安排重连
   */
  private scheduleReconnect(): void {
    if (this.isManualDisconnect) return;

    if (this.reconnectAttempts >= (this.options.maxReconnectAttempts || 10)) {
      console.error('WebSocket重连次数已达上限');
      ElNotification.error({
        title: '连接失败',
        message: '无法连接到通知服务器，请刷新页面重试',
        duration: 0,
      });
      return;
    }

    this.reconnectAttempts++;
    const delay = this.options.reconnectDelay || 3000;

    console.log(`将在 ${delay}ms 后尝试第 ${this.reconnectAttempts} 次重连...`);

    this.reconnectTimer = window.setTimeout(() => {
      console.log(`开始第 ${this.reconnectAttempts} 次重连...`);
      this.connect();
    }, delay);
  }

  /**
   * 检查连接状态
   */
  isConnected(): boolean {
    return this.client?.connected || false;
  }

  /**
   * 获取重连次数
   */
  getReconnectAttempts(): number {
    return this.reconnectAttempts;
  }
}

/**
 * 创建WebSocket实例
 */
export function createWebSocket(options: WebSocketConnectionOptions): WebSocketManager {
  return new WebSocketManager(options);
}

