/**
 * 报表系统类型定义
 */

// 报表类型枚举
export enum ReportType {
  ARCHIVE_STATISTICS = "archive_statistics",
  BORROW_ANALYSIS = "borrow_analysis",
  USER_ACTIVITY = "user_activity",
  SYSTEM_PERFORMANCE = "system_performance",
  CUSTOM = "custom",
}

// 报表格式枚举
export enum ReportFormat {
  PDF = "pdf",
  EXCEL = "excel",
  CSV = "csv",
  JSON = "json",
}

// 报表状态枚举
export enum ReportStatus {
  PENDING = "pending",
  GENERATING = "generating",
  COMPLETED = "completed",
  FAILED = "failed",
  CANCELLED = "cancelled",
  EXPIRED = "expired",
}

// 时间范围类型
export interface DateRange {
  startDate: string;
  endDate: string;
}

// 报表配置接口
export interface ReportConfig {
  id?: string;
  name: string;
  type: ReportType;
  format: ReportFormat;
  dateRange: DateRange;
  filters?: Record<string, any>;
  charts?: string[];
  includeRawData?: boolean;
  customFields?: string[];
  template?: string;
  schedule?: ReportSchedule;
}

// 报表调度配置
export interface ReportSchedule {
  enabled: boolean;
  frequency: "daily" | "weekly" | "monthly" | "quarterly";
  time: string; // HH:mm 格式
  dayOfWeek?: number; // 0-6, 0为周日
  dayOfMonth?: number; // 1-31
  recipients?: string[];
  autoDelete?: boolean;
  deleteAfterDays?: number;
}

// 报表实例接口
export interface ReportInstance {
  id: string;
  configId: string;
  name: string;
  type: ReportType;
  format: ReportFormat;
  status: ReportStatus;
  progress?: number;
  createdAt: string;
  completedAt?: string;
  fileUrl?: string;
  fileSize?: number;
  error?: string;
  metadata?: {
    recordCount?: number;
    chartCount?: number;
    pageCount?: number;
    generationTime?: number;
  };
}

// 报表模板接口
export interface ReportTemplate {
  id: string;
  name: string;
  description: string;
  type: ReportType;
  sections: ReportSection[];
  styles?: ReportStyles;
  isDefault?: boolean;
  createdAt: string;
  updatedAt: string;
}

// 报表章节接口
export interface ReportSection {
  id: string;
  title: string;
  type: "text" | "chart" | "table" | "image" | "pageBreak";
  order: number;
  config: {
    content?: string;
    chartType?: string;
    chartConfig?: any;
    tableConfig?: any;
    imageUrl?: string;
    styles?: any;
  };
}

// 报表样式接口
export interface ReportStyles {
  fontSize: number;
  fontFamily: string;
  primaryColor: string;
  secondaryColor: string;
  headerHeight: number;
  footerHeight: number;
  margins: {
    top: number;
    right: number;
    bottom: number;
    left: number;
  };
  pageSize: "A4" | "A3" | "Letter" | "Legal";
  orientation: "portrait" | "landscape";
}

// 报表生成请求接口
export interface GenerateReportRequest {
  config: ReportConfig;
  templateId?: string;
  priority?: "low" | "normal" | "high";
  notifyOnComplete?: boolean;
}

// 报表生成响应接口
export interface GenerateReportResponse {
  reportId: string;
  status: ReportStatus;
  estimatedTime?: number;
  message?: string;
}

// 报表列表查询参数
export interface ReportListQuery {
  page?: number;
  pageSize?: number;
  type?: ReportType;
  status?: ReportStatus;
  dateRange?: DateRange;
  search?: string;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
}

// 报表统计信息
export interface ReportStatistics {
  totalReports: number;
  pendingReports: number;
  completedReports: number;
  failedReports: number;
  totalFileSize: number;
  averageGenerationTime: number;
  popularTypes: Array<{
    type: ReportType;
    count: number;
  }>;
  recentActivity: Array<{
    date: string;
    count: number;
  }>;
}

// 报表导出选项
export interface ExportOptions {
  includeCharts: boolean;
  includeRawData: boolean;
  compression?: "none" | "zip" | "gzip";
  password?: string;
  watermark?: {
    text: string;
    opacity: number;
    position: "center" | "topLeft" | "topRight" | "bottomLeft" | "bottomRight";
  };
}
