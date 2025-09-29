// 档案管理相关类型定义

// 档案状态枚举
export enum ArchiveStatus {
  DRAFT = 1, // 草稿
  SUBMITTED = 2, // 已提交
  REVIEWING = 3, // 审核中
  APPROVED = 4, // 已通过
  REJECTED = 5, // 已拒绝
  ARCHIVED = 6, // 已归档
}

// 安全级别枚举
export enum SecurityLevel {
  PUBLIC = 1, // 公开
  INTERNAL = 2, // 内部
  CONFIDENTIAL = 3, // 机密
  SECRET = 4, // 绝密
}

// 档案类别接口
export interface ArchiveCategory {
  categoryId: number;
  categoryName: string;
  parentId?: number;
  description?: string;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// 档案文件接口
export interface ArchiveFile {
  fileId: number;
  fileName: string;
  originalName: string;
  filePath: string;
  fileSize: number;
  fileType: string;
  fileHash: string;
  uploadTime: string;
  uploadUserId: number;
  uploadUserName: string;
}

// 档案基础信息接口
export interface Archive {
  archiveId: number;
  archiveNo: string;
  title: string;
  description?: string;
  categoryId: number;
  categoryName: string;
  securityLevel: SecurityLevel;
  securityLevelName: string;
  keywords?: string;
  retentionPeriod?: number;
  status: ArchiveStatus;
  statusName: string;
  fileCount: number;
  totalSize: number;
  submitUserId: number;
  submitUserName: string;
  submitTime: string;
  reviewUserId?: number;
  reviewUserName?: string;
  reviewTime?: string;
  reviewRemark?: string;
  archiveTime?: string;
  createdAt: string;
  updatedAt: string;
  files?: ArchiveFile[];
}

// 档案列表查询参数
export interface ArchiveSearchParams {
  page?: number;
  size?: number;
  archiveNo?: string;
  title?: string;
  categoryId?: number;
  securityLevel?: SecurityLevel;
  status?: ArchiveStatus;
  submitUserId?: number;
  startTime?: string;
  endTime?: string;
  keywords?: string;
}

// 档案创建请求
export interface ArchiveCreateRequest {
  title: string;
  description?: string;
  categoryId: number;
  securityLevel: SecurityLevel;
  keywords?: string;
  retentionPeriod?: number;
  files?: File[];
}

// 档案更新请求
export interface ArchiveUpdateRequest {
  title?: string;
  description?: string;
  categoryId?: number;
  securityLevel?: SecurityLevel;
  keywords?: string;
  retentionPeriod?: number;
}

// 档案审核请求
export interface ArchiveReviewRequest {
  action: "approve" | "reject";
  remark?: string;
}

// 分页响应接口
export interface PaginationInfo {
  page: number;
  size: number;
  total: number;
  pages: number;
}

// 档案列表响应
export interface ArchiveListResponse {
  list: Archive[];
  pagination: PaginationInfo;
}

// 统一API响应格式
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T;
  code?: number;
}

// 文件上传响应
export interface FileUploadResponse {
  fileId: number;
  fileName: string;
  originalName: string;
  filePath: string;
  fileSize: number;
  fileType: string;
  fileHash: string;
}

// 档案统计数据
export interface ArchiveStats {
  total: number;
  byStatus: Record<ArchiveStatus, number>;
  byCategory: Array<{
    categoryId: number;
    categoryName: string;
    count: number;
  }>;
  bySecurityLevel: Record<SecurityLevel, number>;
  recentCount: number;
}
