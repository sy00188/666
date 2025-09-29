import request from "@/utils/request";
import type { ApiResponse, PaginatedResponse } from "@/types/common";
import type {
  SystemInfo,
  SystemSettings,
  SecuritySettings,
  EmailSettings,
  StorageSettings,
  SystemLog,
  SystemLogQueryParams,
  SystemBackup,
  SystemBackupQueryParams,
  SystemMonitor,
  SystemNotification,
  SystemNotificationQueryParams,
  SystemConfigExport,
  SystemHealthCheck,
  SystemStatistics,
  SystemTask,
} from "@/types/system";

// 系统信息相关API
export const systemApi = {
  // 获取系统信息
  getSystemInfo(): Promise<ApiResponse<SystemInfo>> {
    return request.get("/api/system/info");
  },

  // 系统设置
  getSystemSettings(): Promise<ApiResponse<SystemSettings>> {
    return request.get("/api/system/settings");
  },

  updateSystemSettings(data: SystemSettings): Promise<ApiResponse<void>> {
    return request.put("/api/system/settings", data);
  },

  // 安全设置
  getSecuritySettings(): Promise<ApiResponse<SecuritySettings>> {
    return request.get("/api/system/security");
  },

  updateSecuritySettings(data: SecuritySettings): Promise<ApiResponse<void>> {
    return request.put("/api/system/security", data);
  },

  // 邮件设置
  getEmailSettings(): Promise<ApiResponse<EmailSettings>> {
    return request.get("/api/system/email");
  },

  updateEmailSettings(data: EmailSettings): Promise<ApiResponse<void>> {
    return request.put("/api/system/email", data);
  },

  testEmailConnection(
    data: EmailSettings,
  ): Promise<ApiResponse<{ success: boolean; message: string }>> {
    return request.post("/api/system/email/test", data);
  },

  // 存储设置
  getStorageSettings(): Promise<ApiResponse<StorageSettings>> {
    return request.get("/api/system/storage");
  },

  updateStorageSettings(data: StorageSettings): Promise<ApiResponse<void>> {
    return request.put("/api/system/storage", data);
  },

  testStorageConnection(
    data: StorageSettings,
  ): Promise<ApiResponse<{ success: boolean; message: string }>> {
    return request.post("/api/system/storage/test", data);
  },

  // 系统日志
  getSystemLogs(
    params: SystemLogQueryParams,
  ): Promise<ApiResponse<PaginatedResponse<SystemLog>>> {
    return request.get("/api/system/logs", { params });
  },

  getLogStatistics(): Promise<
    ApiResponse<{ info: number; warning: number; error: number; total: number }>
  > {
    return request.get("/api/system/logs/statistics");
  },

  deleteSystemLog(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/system/logs/${id}`);
  },

  clearSystemLogs(): Promise<ApiResponse<void>> {
    return request.delete("/api/system/logs");
  },

  exportSystemLogs(
    params: SystemLogQueryParams,
  ): Promise<ApiResponse<{ url: string }>> {
    return request.get("/api/system/logs/export", { params });
  },

  // 系统备份
  getSystemBackups(
    params: SystemBackupQueryParams,
  ): Promise<ApiResponse<PaginatedResponse<SystemBackup>>> {
    return request.get("/api/system/backups", { params });
  },

  createSystemBackup(data: {
    name: string;
    description?: string;
    type: "full" | "incremental" | "differential";
    includeFiles: boolean;
    includeDatabase: boolean;
  }): Promise<ApiResponse<SystemBackup>> {
    return request.post("/api/system/backups", data);
  },

  deleteSystemBackup(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/system/backups/${id}`);
  },

  restoreSystemBackup(id: string): Promise<ApiResponse<SystemTask>> {
    return request.post(`/api/system/backups/${id}/restore`);
  },

  downloadSystemBackup(id: string): Promise<ApiResponse<{ url: string }>> {
    return request.get(`/api/system/backups/${id}/download`);
  },

  // 系统监控
  getSystemMonitor(): Promise<ApiResponse<SystemMonitor>> {
    return request.get("/api/system/monitor");
  },

  // 系统通知
  getSystemNotifications(
    params: SystemNotificationQueryParams,
  ): Promise<ApiResponse<PaginatedResponse<SystemNotification>>> {
    return request.get("/api/system/notifications", { params });
  },

  createSystemNotification(
    data: Omit<
      SystemNotification,
      | "id"
      | "createdAt"
      | "updatedAt"
      | "readUsers"
      | "readCount"
      | "totalTargets"
    >,
  ): Promise<ApiResponse<SystemNotification>> {
    return request.post("/api/system/notifications", data);
  },

  updateSystemNotification(
    id: string,
    data: Partial<SystemNotification>,
  ): Promise<ApiResponse<void>> {
    return request.put(`/api/system/notifications/${id}`, data);
  },

  deleteSystemNotification(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/system/notifications/${id}`);
  },

  publishSystemNotification(id: string): Promise<ApiResponse<void>> {
    return request.post(`/api/system/notifications/${id}/publish`);
  },

  markNotificationAsRead(id: string): Promise<ApiResponse<void>> {
    return request.post(`/api/system/notifications/${id}/read`);
  },

  // 系统配置导入导出
  exportSystemConfig(): Promise<ApiResponse<SystemConfigExport>> {
    return request.get("/api/system/config/export");
  },

  importSystemConfig(file: File): Promise<ApiResponse<void>> {
    const formData = new FormData();
    formData.append("file", file);
    return request.post("/api/system/config/import", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      } as any,
    });
  },

  // 系统健康检查
  getSystemHealth(): Promise<ApiResponse<SystemHealthCheck>> {
    return request.get("/api/system/health");
  },

  // 系统统计
  getSystemStatistics(): Promise<ApiResponse<SystemStatistics>> {
    return request.get("/api/system/statistics");
  },

  // 文件上传
  uploadLogo(file: File): Promise<ApiResponse<{ url: string }>> {
    const formData = new FormData();
    formData.append("file", file);
    return request.post("/api/system/upload/logo", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      } as any,
    });
  },

  uploadFavicon(file: File): Promise<ApiResponse<{ url: string }>> {
    const formData = new FormData();
    formData.append("file", file);
    return request.post("/api/system/upload/favicon", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      } as any,
    });
  },
};
