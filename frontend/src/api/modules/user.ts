import request from "@/utils/request";
import type {
  UserInfo,
  CreateUserForm,
  UpdateUserForm,
  UserQueryParams,
  UserStatistics,
  UserActivityLog,
  UserPermission,
  UserRole,
  Department,
  UserImportResult,
  UserExportParams,
  ResetUserPasswordForm,
  UserStatusChangeLog,
  UserLoginRecord,
  UserPreferences,
  UserApiResponse,
  UserPaginatedResponse,
} from "@/types/user";

// 获取用户列表
export const getUserList = (params?: UserQueryParams) => {
  return request.get<UserPaginatedResponse<UserInfo>>("/api/users", { params });
};

// 获取用户详情
export const getUserDetail = (id: string) => {
  return request.get<UserApiResponse<UserInfo>>(`/api/users/${id}`);
};

// 创建用户
export const createUser = (data: CreateUserForm) => {
  return request.post<UserApiResponse<UserInfo>>("/api/users", data);
};

// 更新用户
export const updateUser = (id: string, data: UpdateUserForm) => {
  return request.put<UserApiResponse<UserInfo>>(`/api/users/${id}`, data);
};

// 删除用户
export const deleteUser = (id: string) => {
  return request.delete<UserApiResponse<null>>(`/api/users/${id}`);
};

// 批量删除用户
export const batchDeleteUsers = (ids: string[]) => {
  return request.delete<UserApiResponse<null>>("/api/users/batch", {
    data: { ids },
  });
};

// 重置用户密码
export const resetUserPassword = (data: ResetUserPasswordForm) => {
  return request.post<UserApiResponse<null>>("/api/users/reset-password", data);
};

// 修改用户状态
export const changeUserStatus = (
  id: string,
  status: "active" | "inactive" | "banned",
  reason?: string,
) => {
  return request.put<UserApiResponse<null>>(`/api/users/${id}/status`, {
    status,
    reason,
  });
};

// 获取用户统计信息
export const getUserStatistics = () => {
  return request.get<UserApiResponse<UserStatistics>>("/api/users/statistics");
};

// 获取用户活动日志
export const getUserActivityLogs = (params?: {
  userId?: string;
  page?: number;
  size?: number;
}) => {
  return request.get<UserPaginatedResponse<UserActivityLog>>(
    "/api/users/activity-logs",
    { params },
  );
};

// 获取用户登录记录
export const getUserLoginRecords = (params?: {
  userId?: string;
  page?: number;
  size?: number;
}) => {
  return request.get<UserPaginatedResponse<UserLoginRecord>>(
    "/api/users/login-records",
    { params },
  );
};

// 获取用户状态变更日志
export const getUserStatusChangeLogs = (userId: string) => {
  return request.get<UserApiResponse<UserStatusChangeLog[]>>(
    `/api/users/${userId}/status-logs`,
  );
};

// 获取所有权限
export const getAllPermissions = () => {
  return request.get<UserApiResponse<UserPermission[]>>("/api/permissions");
};

// 获取所有角色
export const getAllRoles = () => {
  return request.get<UserApiResponse<UserRole[]>>("/api/roles");
};

// 获取部门树
export const getDepartmentTree = () => {
  return request.get<UserApiResponse<Department[]>>("/api/departments/tree");
};

// 导入用户
export const importUsers = (file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return request.post<UserApiResponse<UserImportResult>>(
    "/api/users/import",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    },
  );
};

// 导出用户
export const exportUsers = (params?: UserExportParams) => {
  return request.get("/api/users/export", {
    params,
    responseType: "blob",
  });
};

// 下载用户导入模板
export const downloadUserTemplate = () => {
  return request.get("/api/users/template", {
    responseType: "blob",
  });
};

// 获取用户偏好设置
export const getUserPreferences = (userId: string) => {
  return request.get<UserApiResponse<UserPreferences>>(
    `/api/users/${userId}/preferences`,
  );
};

// 更新用户偏好设置
export const updateUserPreferences = (
  userId: string,
  preferences: Partial<UserPreferences>,
) => {
  return request.put<UserApiResponse<null>>(
    `/api/users/${userId}/preferences`,
    preferences,
  );
};

// 检查用户名是否可用
export const checkUsernameAvailable = (username: string) => {
  return request.get<UserApiResponse<{ available: boolean }>>(
    "/api/users/check-username",
    {
      params: { username },
    },
  );
};

// 检查邮箱是否可用
export const checkEmailAvailable = (email: string) => {
  return request.get<UserApiResponse<{ available: boolean }>>(
    "/api/users/check-email",
    {
      params: { email },
    },
  );
};

// 发送用户激活邮件
export const sendActivationEmail = (userId: string) => {
  return request.post<UserApiResponse<null>>(
    `/api/users/${userId}/send-activation`,
  );
};

// 激活用户账户
export const activateUser = (token: string) => {
  return request.post<UserApiResponse<null>>("/api/users/activate", { token });
};
