// 系统管理相关类型定义

// 系统基础信息
export interface SystemInfo {
  name: string;
  version: string;
  buildTime: string;
  environment: "development" | "production" | "test";
  uptime: number;
  serverTime: string;
  timezone: string;
  locale: string;
}

// 系统设置
export interface SystemSettings {
  systemName: string;
  version: string;
  logo: string;
  favicon: string;
  description: string;
  copyright: string;
  contact: string;
  timezone: string;
  locale: string;
  dateFormat: string;
  timeFormat: string;
  theme: "light" | "dark" | "auto";
  primaryColor: string;
  enableRegistration: boolean;
  enableGuestAccess: boolean;
  maintenanceMode: boolean;
  maintenanceMessage?: string;
}

// 安全设置
export interface SecuritySettings {
  minPasswordLength: number;
  passwordComplexity: string[];
  passwordExpireDays: number;
  loginFailLock: boolean;
  maxLoginFails: number;
  lockDuration: number;
  sessionTimeout: number;
  forceHttps: boolean;
  ipWhitelist: string;
  enableTwoFactor: boolean;
  enableCaptcha: boolean;
  captchaThreshold: number;
  enableAuditLog: boolean;
  logRetentionDays: number;
}

// 邮件设置
export interface EmailSettings {
  enabled: boolean;
  host: string;
  port: number;
  username: string;
  password: string;
  fromName: string;
  fromEmail: string;
  encryption: "none" | "ssl" | "tls";
  timeout: number;
  enableAuth: boolean;
  enableDebug: boolean;
}

// 存储设置
export interface StorageSettings {
  type: "local" | "oss" | "s3" | "qiniu" | "cos";
  localPath: string;
  localDomain: string;
  accessKey: string;
  secretKey: string;
  bucket: string;
  region: string;
  customDomain: string;
  maxFileSize: number;
  allowedTypes: string;
  enableCompression: boolean;
  compressionQuality: number;
  enableWatermark: boolean;
  watermarkText?: string;
  watermarkImage?: string;
}

// 系统日志
export interface SystemLog {
  id: string;
  level: "debug" | "info" | "warning" | "error" | "critical";
  type: "system" | "security" | "operation" | "api" | "database";
  operation?: string;
  message: string;
  details?: any;
  userId?: string;
  username?: string;
  ip: string;
  userAgent?: string;
  module: string;
  action?: string;
  method?: string;
  path?: string;
  statusCode?: number;
  responseTime?: number;
  requestData?: string;
  responseData?: string;
  errorStack?: string;
  resource?: string;
  resourceId?: string;
  duration?: number;
  status: "success" | "failure" | "pending";
  timestamp: string;
  createdAt: string;
  createTime: Date;
  tags?: string[];
  stackTrace?: string;
  selected?: boolean;
}

// 系统日志查询参数
export interface SystemLogQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  level?: "debug" | "info" | "warning" | "error" | "critical";
  type?: "system" | "security" | "operation" | "api" | "database";
  operation?: string;
  userId?: string;
  username?: string;
  ip?: string;
  module?: string;
  action?: string;
  status?: "success" | "failure" | "pending";
  startTime?: string;
  endTime?: string;
  sortBy?: "type" | "timestamp" | "level" | "module" | "createdAt";
  sortOrder?: "asc" | "desc";
}

// 系统备份
export interface SystemBackup {
  id: string;
  name: string;
  description?: string;
  type: "full" | "incremental" | "differential";
  status: "pending" | "running" | "completed" | "failed" | "cancelled";
  size: number;
  filePath: string;
  includeFiles: boolean;
  includeDatabase: boolean;
  progress: number;
  startTime: Date;
  endTime?: Date;
  duration?: number;
  errorMessage?: string;
  createdBy: string;
  createdAt: Date;
  expiresAt?: Date;
}

// 系统备份查询参数
export interface SystemBackupQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  type?: "full" | "incremental" | "differential";
  status?: "pending" | "running" | "completed" | "failed" | "cancelled";
  createdBy?: string;
  startDate?: string;
  endDate?: string;
  sortBy?: "createdAt" | "size" | "duration" | "name";
  sortOrder?: "asc" | "desc";
}

// 系统监控
export interface SystemMonitor {
  system: {
    os: string;
    arch: string;
    platform: string;
    hostname: string;
    uptime: number;
    loadAverage: number[];
  };
  cpu: {
    model: string;
    cores: number;
    usage: number;
    temperature?: number;
  };
  memory: {
    total: number;
    used: number;
    free: number;
    available: number;
    usage: number;
  };
  disk: {
    total: number;
    used: number;
    free: number;
    usage: number;
    readSpeed: number;
    writeSpeed: number;
  };
  network: {
    interfaces: Array<{
      name: string;
      type: string;
      speed: number;
      bytesReceived: number;
      bytesSent: number;
      packetsReceived: number;
      packetsSent: number;
    }>;
    totalBytesReceived: number;
    totalBytesSent: number;
  };
  database: {
    type: string;
    version: string;
    status: "connected" | "disconnected" | "error";
    connections: {
      active: number;
      idle: number;
      total: number;
      max: number;
    };
    performance: {
      queryTime: number;
      slowQueries: number;
      cacheHitRate: number;
    };
  };
  application: {
    version: string;
    environment: string;
    processes: number;
    threads: number;
    memoryUsage: number;
    responseTime: number;
    requestsPerSecond: number;
    errorRate: number;
  };
  services: Array<{
    name: string;
    status: "running" | "stopped" | "error";
    port?: number;
    pid?: number;
    uptime?: number;
    memoryUsage?: number;
    cpuUsage?: number;
  }>;
  timestamp: string;
}

// 系统通知
export interface SystemNotification {
  id: string;
  title: string;
  content: string;
  type: "info" | "warning" | "error" | "success";
  level: "low" | "medium" | "high" | "urgent";
  status: "draft" | "published" | "sent" | "expired";
  targetType: "all" | "users" | "roles" | "departments";
  targetUsers?: string[];
  targetRoles?: string[];
  targetDepartments?: string[];
  readUsers?: string[];
  totalTargets: number;
  readCount: number;
  publishTime?: Date;
  scheduledTime?: Date;
  expireTime?: Date;
  createdBy: string;
  createdAt: Date;
  updatedBy?: string;
  updatedAt?: Date;
}

// 系统通知查询参数
export interface SystemNotificationQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  type?: "info" | "warning" | "error" | "success";
  level?: "low" | "medium" | "high" | "urgent";
  status?: "draft" | "published" | "sent" | "expired";
  targetType?: "all" | "users" | "roles" | "departments";
  createdBy?: string;
  isRead?: boolean;
  startDate?: string;
  endDate?: string;
  sortBy?: "createdAt" | "publishTime" | "level" | "readCount";
  sortOrder?: "asc" | "desc";
}

// 系统配置导入导出
export interface SystemConfigExport {
  version: string;
  exportTime: string;
  settings: {
    system: SystemSettings;
    security: SecuritySettings;
    email: EmailSettings;
    storage: StorageSettings;
  };
  metadata: {
    exportedBy: string;
    description?: string;
    checksum: string;
  };
}

// 系统健康检查
export interface SystemHealthCheck {
  status: "healthy" | "warning" | "critical";
  checks: Array<{
    name: string;
    status: "pass" | "warn" | "fail";
    message?: string;
    duration?: number;
    details?: any;
  }>;
  timestamp: string;
  uptime: number;
  version: string;
}

// 系统统计
export interface SystemStatistics {
  users: {
    total: number;
    active: number;
    online: number;
    newToday: number;
  };
  archives: {
    total: number;
    newToday: number;
    categories: number;
    totalSize: number;
  };
  operations: {
    totalToday: number;
    successRate: number;
    avgResponseTime: number;
    errorCount: number;
  };
  system: {
    uptime: number;
    cpuUsage: number;
    memoryUsage: number;
    diskUsage: number;
    networkTraffic: number;
  };
  trends: {
    userGrowth: Array<{ date: string; count: number }>;
    archiveGrowth: Array<{ date: string; count: number }>;
    operationTrends: Array<{ date: string; count: number }>;
  };
}

// API响应类型
export interface SystemApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  timestamp: number;
}

// 分页响应类型
export interface SystemPaginatedResponse<T = unknown> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 任务状态
export interface SystemTask {
  id: string;
  type: "backup" | "restore" | "import" | "export" | "cleanup";
  name: string;
  status: "pending" | "running" | "completed" | "failed" | "cancelled";
  progress: number;
  message?: string;
  result?: any;
  startTime: Date;
  endTime?: Date;
  duration?: number;
  createdBy: string;
  createdAt: Date;
}

// 系统配置表单
export interface SystemConfigForm {
  basic: Partial<SystemSettings>;
  security: Partial<SecuritySettings>;
  email: Partial<EmailSettings>;
  storage: Partial<StorageSettings>;
}

// 系统维护
export interface SystemMaintenance {
  enabled: boolean;
  message: string;
  startTime?: Date;
  endTime?: Date;
  allowedIps?: string[];
  allowedUsers?: string[];
  createdBy: string;
  createdAt: Date;
}
