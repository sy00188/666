import { request } from "@/utils/request";
import type { User, AuthResponse } from "@/types/auth";

export interface UserListParams {
  username?: string;
  role?: string;
  status?: string;
  page: number;
  size: number;
}

export interface UserListResponse {
  list: User[];
  total: number;
  page: number;
  size: number;
}

export const userApi = {
  // 获取用户列表
  getUsers: (
    params: UserListParams,
  ): Promise<AuthResponse<UserListResponse>> => {
    return request.get("/api/users", { params });
  },

  // 获取用户详情
  getUserById: (id: string): Promise<AuthResponse<User>> => {
    return request.get(`/api/users/${id}`);
  },

  // 创建用户
  createUser: (userData: Partial<User>): Promise<AuthResponse<User>> => {
    return request.post("/api/users", userData);
  },

  // 更新用户
  updateUser: (
    id: string,
    userData: Partial<User>,
  ): Promise<AuthResponse<User>> => {
    return request.put(`/api/users/${id}`, userData);
  },

  // 删除用户
  deleteUser: (id: string): Promise<AuthResponse<void>> => {
    return request.delete(`/api/users/${id}`);
  },

  // 重置密码
  resetPassword: (
    id: string,
  ): Promise<AuthResponse<{ newPassword: string }>> => {
    return request.post(`/api/users/${id}/reset-password`);
  },

  // 更新用户状态
  updateUserStatus: (
    id: string,
    status: string,
  ): Promise<AuthResponse<void>> => {
    return request.patch(`/api/users/${id}/status`, { status });
  },

  // 批量删除用户
  batchDeleteUsers: (ids: string[]): Promise<AuthResponse<void>> => {
    return request.delete("/api/users/batch", { data: { ids } });
  },

  // 导出用户列表
  exportUsers: (params: Partial<UserListParams>): Promise<Blob> => {
    return request.get("/api/users/export", {
      params,
      responseType: "blob",
    });
  },
};
