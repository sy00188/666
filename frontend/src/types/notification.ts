// 通知相关类型定义

/**
 * 通知类型枚举
 */
export enum NotificationType {
  /** 系统通知 */
  SYSTEM = 'system',
  /** 用户相关 */
  USER = 'user',
  /** 档案相关 */
  ARCHIVE = 'archive',
  /** 借阅相关 */
  BORROW = 'borrow',
  /** 审批相关 */
  APPROVAL = 'approval',
  /** 告警通知 */
  ALERT = 'alert',
  /** 成功消息 */
  SUCCESS = 'success',
  /** 警告消息 */
  WARNING = 'warning',
  /** 错误消息 */
  ERROR = 'error',
  /** 信息消息 */
  INFO = 'info',
}

/**
 * 通知优先级
 */
export enum NotificationPriority {
  /** 低优先级 */
  LOW = 'low',
  /** 普通优先级 */
  NORMAL = 'normal',
  /** 高优先级 */
  HIGH = 'high',
  /** 紧急 */
  URGENT = 'urgent',
}

/**
 * 通知状态
 */
export enum NotificationStatus {
  /** 未读 */
  UNREAD = 'unread',
  /** 已读 */
  READ = 'read',
  /** 已删除 */
  DELETED = 'deleted',
}

/**
 * 通知项接口
 */
export interface Notification {
  /** 通知ID */
  id: number | string;
  /** 通知标题 */
  title: string;
  /** 通知内容 */
  content: string;
  /** 通知类型 */
  type: NotificationType;
  /** 优先级 */
  priority?: NotificationPriority;
  /** 是否已读 */
  read: boolean;
  /** 状态 */
  status?: NotificationStatus;
  /** 创建时间 */
  createdAt: string;
  /** 更新时间 */
  updatedAt?: string;
  /** 关联的业务ID（如档案ID、借阅ID等） */
  relatedId?: number | string;
  /** 关联的业务类型 */
  relatedType?: string;
  /** 发送者ID */
  senderId?: number | string;
  /** 发送者名称 */
  senderName?: string;
  /** 接收者ID */
  receiverId?: number | string;
  /** 操作URL（点击通知后跳转的路径） */
  actionUrl?: string;
  /** 操作文本 */
  actionText?: string;
  /** 额外数据 */
  extra?: Record<string, any>;
  /** 图标 */
  icon?: string;
  /** 是否可关闭 */
  closable?: boolean;
  /** 自动关闭延迟（毫秒），0表示不自动关闭 */
  duration?: number;
}

/**
 * 通知列表查询参数
 */
export interface NotificationQueryParams {
  /** 页码 */
  page?: number;
  /** 每页数量 */
  size?: number;
  /** 通知类型 */
  type?: NotificationType | NotificationType[];
  /** 是否已读 */
  read?: boolean;
  /** 状态 */
  status?: NotificationStatus;
  /** 优先级 */
  priority?: NotificationPriority;
  /** 开始日期 */
  startDate?: string;
  /** 结束日期 */
  endDate?: string;
  /** 关键词搜索 */
  keyword?: string;
}

/**
 * 通知统计信息
 */
export interface NotificationStatistics {
  /** 总通知数 */
  total: number;
  /** 未读数量 */
  unreadCount: number;
  /** 已读数量 */
  readCount: number;
  /** 按类型统计 */
  byType?: Record<NotificationType, number>;
  /** 按优先级统计 */
  byPriority?: Record<NotificationPriority, number>;
  /** 今日新增 */
  todayCount?: number;
}

/**
 * 通知设置
 */
export interface NotificationSettings {
  /** 是否启用通知 */
  enabled: boolean;
  /** 是否启用声音 */
  sound: boolean;
  /** 是否启用桌面通知 */
  desktop: boolean;
  /** 是否启用邮件通知 */
  email: boolean;
  /** 是否启用短信通知 */
  sms: boolean;
  /** 接收的通知类型 */
  types: NotificationType[];
  /** 静音时间段 */
  muteHours?: {
    start: string; // HH:mm格式
    end: string;   // HH:mm格式
  };
  /** 最小优先级（低于此优先级的通知不接收） */
  minPriority?: NotificationPriority;
}

/**
 * 通知模板
 */
export interface NotificationTemplate {
  /** 模板ID */
  id: number | string;
  /** 模板名称 */
  name: string;
  /** 模板类型 */
  type: NotificationType;
  /** 标题模板 */
  titleTemplate: string;
  /** 内容模板 */
  contentTemplate: string;
  /** 变量说明 */
  variables?: Record<string, string>;
  /** 是否启用 */
  enabled: boolean;
}

/**
 * 批量操作结果
 */
export interface NotificationBatchResult {
  /** 成功数量 */
  successCount: number;
  /** 失败数量 */
  failureCount: number;
  /** 失败的ID列表 */
  failedIds?: (number | string)[];
  /** 错误消息 */
  errorMessage?: string;
}

/**
 * WebSocket通知消息
 */
export interface WebSocketNotificationMessage {
  /** 消息类型 */
  messageType: 'notification' | 'count' | 'ping' | 'pong';
  /** 通知数据（messageType为notification时） */
  notification?: Notification;
  /** 未读数量（messageType为count时） */
  unreadCount?: number;
  /** 时间戳 */
  timestamp: number;
}

/**
 * 通知过滤器
 */
export interface NotificationFilter {
  /** 类型过滤 */
  types?: NotificationType[];
  /** 优先级过滤 */
  priorities?: NotificationPriority[];
  /** 已读状态过滤 */
  readStatus?: 'all' | 'read' | 'unread';
  /** 日期范围 */
  dateRange?: {
    start: string;
    end: string;
  };
}

