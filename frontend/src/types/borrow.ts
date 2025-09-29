// 借阅管理相关类型定义

// 档案信息类型
export interface Archive {
  id: string;
  title: string;
  archiveNumber: string;
  category: string;
  description?: string;
  location: string;
  status: "available" | "borrowed" | "maintenance" | "lost";
  totalQuantity: number;
  availableQuantity: number;
  borrowedQuantity: number;
  createTime: Date;
  updateTime?: Date;
  tags?: string[];
  metadata?: Record<string, unknown>;
}

// 借阅申请类型
export interface BorrowApplication {
  id: string;
  userId: string;
  userName: string;
  userRealName: string;
  archiveId: string;
  archiveTitle: string;
  archiveNumber: string;
  quantity: number;
  borrowReason: string;
  expectedReturnDate: Date;
  status:
    | "pending"
    | "approved"
    | "rejected"
    | "borrowed"
    | "returned"
    | "overdue"
    | "cancelled";
  applyTime: Date;
  approveTime?: Date;
  approveUserId?: string;
  approveUserName?: string;
  borrowTime?: Date;
  expectedReturnTime?: Date;
  actualReturnTime?: Date;
  returnUserId?: string;
  returnUserName?: string;
  rejectReason?: string;
  remark?: string;
  overdueReason?: string;
  penaltyAmount?: number;
}

// 借阅申请表单类型
export interface BorrowApplicationForm {
  archiveId: string;
  quantity: number;
  borrowReason: string;
  expectedReturnDate: Date;
  remark?: string;
}

// 借阅审批表单类型
export interface BorrowApprovalForm {
  applicationId: string;
  action: "approve" | "reject";
  remark?: string;
  rejectReason?: string;
  borrowTime?: Date;
  expectedReturnTime?: Date;
}

// 归还表单类型
export interface ReturnForm {
  applicationId: string;
  actualReturnTime: Date;
  returnCondition: "good" | "damaged" | "lost";
  damageDescription?: string;
  penaltyAmount?: number;
  remark?: string;
}

// 借阅记录类型
export interface BorrowRecord {
  id: string;
  userId: string;
  userName: string;
  userRealName: string;
  archiveId: string;
  archiveTitle: string;
  archiveNumber: string;
  quantity: number;
  borrowTime: Date;
  expectedReturnTime: Date;
  actualReturnTime?: Date;
  status: "borrowed" | "returned" | "overdue";
  returnCondition?: "good" | "damaged" | "lost";
  penaltyAmount?: number;
  remark?: string;
  createTime: Date;
  updateTime?: Date;
}

// 借阅统计类型
export interface BorrowStatistics {
  totalApplications: number;
  pendingApplications: number;
  approvedApplications: number;
  rejectedApplications: number;
  borrowedItems: number;
  returnedItems: number;
  overdueItems: number;
  totalPenalty: number;
  popularArchives: Array<{
    archiveId: string;
    archiveTitle: string;
    borrowCount: number;
  }>;
  activeUsers: Array<{
    userId: string;
    userName: string;
    borrowCount: number;
  }>;
}

// 借阅查询参数类型
export interface BorrowQueryParams {
  page?: number;
  size?: number;
  status?: string;
  userId?: string;
  archiveId?: string;
  startDate?: Date;
  endDate?: Date;
  keyword?: string;
  sortBy?:
    | "applyTime"
    | "borrowTime"
    | "expectedReturnTime"
    | "actualReturnTime";
  sortOrder?: "asc" | "desc";
}

// 借阅提醒类型
export interface BorrowReminder {
  id: string;
  applicationId: string;
  userId: string;
  userName: string;
  archiveTitle: string;
  reminderType: "return_due" | "overdue" | "penalty";
  reminderTime: Date;
  message: string;
  status: "pending" | "sent" | "read";
  createTime: Date;
}

// 借阅配置类型
export interface BorrowConfig {
  maxBorrowQuantity: number; // 单次最大借阅数量
  maxBorrowDays: number; // 最大借阅天数
  maxConcurrentBorrows: number; // 最大同时借阅数量
  overdueGraceDays: number; // 逾期宽限天数
  penaltyPerDay: number; // 每日罚金
  reminderDaysBefore: number; // 提前提醒天数
  autoRejectDays: number; // 自动拒绝天数
  allowRenewal: boolean; // 是否允许续借
  maxRenewalTimes: number; // 最大续借次数
  renewalDays: number; // 续借天数
}

// API响应基础类型
export interface BorrowApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  timestamp: number;
}

// 分页响应类型
export interface BorrowPaginatedResponse<T = unknown> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}
