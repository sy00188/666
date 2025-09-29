import { request } from "@/utils/request";
import type {
  BorrowApplication,
  BorrowApplicationForm,
  BorrowApprovalForm,
  ReturnForm,
  BorrowRecord,
  BorrowStatistics,
  BorrowQueryParams,
  BorrowReminder,
  BorrowConfig,
  Archive,
  BorrowApiResponse,
  BorrowPaginatedResponse,
} from "@/types/borrow";

// 获取档案列表（用于借阅申请）
export const getArchiveList = (params?: {
  page?: number;
  size?: number;
  keyword?: string;
  category?: string;
  status?: string;
}): Promise<BorrowApiResponse<BorrowPaginatedResponse<Archive>>> => {
  return request.get("/v1/archives", { params });
};

// 获取档案详情
export const getArchiveDetail = (
  id: string,
): Promise<BorrowApiResponse<Archive>> => {
  return request.get(`/v1/archives/${id}`);
};

// 提交借阅申请
export const submitBorrowApplication = (
  data: BorrowApplicationForm,
): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.post("/borrow/applications", data);
};

// 获取借阅申请列表
export const getBorrowApplications = (
  params?: BorrowQueryParams,
): Promise<BorrowApiResponse<BorrowPaginatedResponse<BorrowApplication>>> => {
  return request.get("/borrow/applications", { params });
};

// 获取借阅申请详情
export const getBorrowApplicationDetail = (
  id: string,
): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.get(`/borrow/applications/${id}`);
};

// 审批借阅申请
export const approveBorrowApplication = (
  data: BorrowApprovalForm,
): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.post("/borrow/applications/approve", data);
};

// 拒绝借阅申请
export const rejectBorrowApplication = (data: {
  applicationId: string;
  rejectReason: string;
  remark?: string;
}): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.post("/borrow/applications/reject", data);
};

// 取消借阅申请
export const cancelBorrowApplication = (
  id: string,
  reason?: string,
): Promise<BorrowApiResponse<null>> => {
  return request.post(`/borrow/applications/${id}/cancel`, { reason });
};

// 确认借出
export const confirmBorrow = (data: {
  applicationId: string;
  borrowTime: Date;
  remark?: string;
}): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.post("/borrow/applications/confirm", data);
};

// 归还档案
export const returnArchive = (
  data: ReturnForm,
): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.post("/borrow/return", data);
};

// 获取借阅记录列表
export const getBorrowRecords = (
  params?: BorrowQueryParams,
): Promise<BorrowApiResponse<BorrowPaginatedResponse<BorrowRecord>>> => {
  return request.get("/borrow/records", { params });
};

// 获取借阅记录详情
export const getBorrowRecordDetail = (
  id: string,
): Promise<BorrowApiResponse<BorrowRecord>> => {
  return request.get(`/borrow/records/${id}`);
};

// 获取用户借阅记录
export const getUserBorrowRecords = (
  userId: string,
  params?: BorrowQueryParams,
): Promise<BorrowApiResponse<BorrowPaginatedResponse<BorrowRecord>>> => {
  return request.get(`/borrow/records/user/${userId}`, { params });
};

// 获取档案借阅记录
export const getArchiveBorrowRecords = (
  archiveId: string,
  params?: BorrowQueryParams,
): Promise<BorrowApiResponse<BorrowPaginatedResponse<BorrowRecord>>> => {
  return request.get(`/borrow/records/archive/${archiveId}`, { params });
};

// 获取借阅统计数据
export const getBorrowStatistics = (params?: {
  startDate?: Date;
  endDate?: Date;
  userId?: string;
  archiveId?: string;
}): Promise<BorrowApiResponse<BorrowStatistics>> => {
  return request.get("/borrow/statistics", { params });
};

// 获取逾期借阅列表
export const getOverdueBorrows = (
  params?: BorrowQueryParams,
): Promise<BorrowApiResponse<BorrowPaginatedResponse<BorrowApplication>>> => {
  return request.get("/borrow/overdue", { params });
};

// 发送归还提醒
export const sendReturnReminder = (data: {
  applicationIds: string[];
  message?: string;
}): Promise<BorrowApiResponse<null>> => {
  return request.post("/borrow/remind", data);
};

// 获取借阅提醒列表
export const getBorrowReminders = (params?: {
  page?: number;
  size?: number;
  userId?: string;
  status?: string;
  reminderType?: string;
}): Promise<BorrowApiResponse<BorrowPaginatedResponse<BorrowReminder>>> => {
  return request.get("/borrow/reminders", { params });
};

// 标记提醒为已读
export const markReminderAsRead = (
  id: string,
): Promise<BorrowApiResponse<null>> => {
  return request.post(`/borrow/reminders/${id}/read`);
};

// 批量标记提醒为已读
export const markRemindersAsRead = (
  ids: string[],
): Promise<BorrowApiResponse<null>> => {
  return request.post("/borrow/reminders/batch-read", { ids });
};

// 续借申请
export const renewBorrow = (data: {
  applicationId: string;
  renewalDays: number;
  reason: string;
}): Promise<BorrowApiResponse<BorrowApplication>> => {
  return request.post("/borrow/renew", data);
};

// 获取借阅配置
export const getBorrowConfig = (): Promise<BorrowApiResponse<BorrowConfig>> => {
  return request.get("/borrow/config");
};

// 更新借阅配置
export const updateBorrowConfig = (
  data: Partial<BorrowConfig>,
): Promise<BorrowApiResponse<BorrowConfig>> => {
  return request.put("/borrow/config", data);
};

// 导出借阅记录
export const exportBorrowRecords = (params?: {
  ids?: string[];
  status?: string;
  startDate?: Date;
  endDate?: Date;
  format?: "excel" | "csv";
}): Promise<BorrowApiResponse<{ downloadUrl: string }>> => {
  return request.post("/borrow/export", params);
};

// 批量操作借阅申请
export const batchOperateBorrowApplications = (data: {
  ids: string[];
  action: "approve" | "reject" | "cancel";
  reason?: string;
  remark?: string;
}): Promise<
  BorrowApiResponse<{ success: number; failed: number; errors: string[] }>
> => {
  return request.post("/borrow/applications/batch", data);
};

// 获取用户当前借阅状态
export const getUserBorrowStatus = (
  userId?: string,
): Promise<
  BorrowApiResponse<{
    currentBorrows: number;
    maxBorrows: number;
    overdueCount: number;
    pendingApplications: number;
    canBorrow: boolean;
  }>
> => {
  return request.get("/borrow/user-status", {
    params: userId ? { userId } : undefined,
  });
};
