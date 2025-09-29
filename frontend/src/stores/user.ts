import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { User } from "@/types/auth";
import { request } from "@/utils/request";
import { ElMessage } from "element-plus";

// 用户查询参数接口
export interface UserQueryParams {
  page?: number;
  pageSize?: number;
  keyword?: string;
  role?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
}

// 用户列表响应接口
export interface UserListResponse {
  success: boolean;
  data: {
    list: User[];
    total: number;
    page: number;
    pageSize: number;
  };
  message: string;
}

// 用户操作响应接口
export interface UserResponse {
  success: boolean;
  data?: User;
  message: string;
}

export const useUserStore = defineStore("user", () => {
  // 状态
  const users = ref<User[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const currentPage = ref(1);
  const pageSize = ref(10);

  // 计算属性
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value));
  const hasNextPage = computed(() => currentPage.value < totalPages.value);
  const hasPrevPage = computed(() => currentPage.value > 1);

  // 获取用户列表
  const getUserList = async (params: UserQueryParams = {}): Promise<void> => {
    try {
      loading.value = true;

      const queryParams = {
        page: params.page || currentPage.value,
        pageSize: params.pageSize || pageSize.value,
        ...params,
      };

      const response = await request.get<UserListResponse>("/users", {
        params: queryParams,
      });

      if (response.success) {
        users.value = response.data.list;
        total.value = response.data.total;
        currentPage.value = response.data.page;
        pageSize.value = response.data.pageSize;
      } else {
        ElMessage.error(response.message || "获取用户列表失败");
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "获取用户列表失败");
    } finally {
      loading.value = false;
    }
  };

  // 创建用户
  const createUser = async (userData: Partial<User>): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.post<UserResponse>("/users", userData);

      if (response.success) {
        ElMessage.success("用户创建成功");
        // 刷新用户列表
        await getUserList();
        return true;
      } else {
        ElMessage.error(response.message || "用户创建失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "用户创建失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 更新用户
  const updateUser = async (
    userId: string,
    userData: Partial<User>,
  ): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.put<UserResponse>(
        `/users/${userId}`,
        userData,
      );

      if (response.success) {
        ElMessage.success("用户更新成功");
        // 刷新用户列表
        await getUserList();
        return true;
      } else {
        ElMessage.error(response.message || "用户更新失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "用户更新失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 删除用户
  const deleteUser = async (userId: string): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.delete<UserResponse>(`/users/${userId}`);

      if (response.success) {
        ElMessage.success("用户删除成功");
        // 刷新用户列表
        await getUserList();
        return true;
      } else {
        ElMessage.error(response.message || "用户删除失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "用户删除失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 批量删除用户
  const batchDeleteUsers = async (userIds: string[]): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.post<UserResponse>("/users/batch-delete", {
        ids: userIds,
      });

      if (response.success) {
        ElMessage.success("批量删除成功");
        // 刷新用户列表
        await getUserList();
        return true;
      } else {
        ElMessage.error(response.message || "批量删除失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "批量删除失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 启用用户
  const enableUser = async (userId: string): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.put<UserResponse>(
        `/users/${userId}/enable`,
      );

      if (response.success) {
        ElMessage.success("用户启用成功");
        // 刷新用户列表
        await getUserList();
        return true;
      } else {
        ElMessage.error(response.message || "用户启用失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "用户启用失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 禁用用户
  const disableUser = async (userId: string): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.put<UserResponse>(
        `/users/${userId}/disable`,
      );

      if (response.success) {
        ElMessage.success("用户禁用成功");
        // 刷新用户列表
        await getUserList();
        return true;
      } else {
        ElMessage.error(response.message || "用户禁用失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "用户禁用失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 重置用户密码
  const resetUserPassword = async (userId: string): Promise<boolean> => {
    try {
      loading.value = true;
      const response = await request.put<UserResponse>(
        `/users/${userId}/reset-password`,
      );

      if (response.success) {
        ElMessage.success("密码重置成功");
        return true;
      } else {
        ElMessage.error(response.message || "密码重置失败");
        return false;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "密码重置失败");
      return false;
    } finally {
      loading.value = false;
    }
  };

  // 获取用户详情
  const getUserById = async (userId: string): Promise<User | null> => {
    try {
      loading.value = true;
      const response = await request.get<UserResponse>(`/users/${userId}`);

      if (response.success && response.data) {
        return response.data;
      } else {
        ElMessage.error(response.message || "获取用户详情失败");
        return null;
      }
    } catch (error: unknown) {
      ElMessage.error((error as Error).message || "获取用户详情失败");
      return null;
    } finally {
      loading.value = false;
    }
  };

  // 搜索用户
  const searchUsers = async (keyword: string): Promise<void> => {
    await getUserList({ keyword, page: 1 });
  };

  // 按角色筛选用户
  const filterUsersByRole = async (role: string): Promise<void> => {
    await getUserList({ role, page: 1 });
  };

  // 按状态筛选用户
  const filterUsersByStatus = async (status: string): Promise<void> => {
    await getUserList({ status, page: 1 });
  };

  // 重置筛选条件
  const resetFilters = async (): Promise<void> => {
    currentPage.value = 1;
    await getUserList();
  };

  // 设置页码
  const setCurrentPage = (page: number): void => {
    currentPage.value = page;
  };

  // 设置每页大小
  const setPageSize = (size: number): void => {
    pageSize.value = size;
    currentPage.value = 1; // 重置到第一页
  };

  return {
    // 状态
    users,
    total,
    loading,
    currentPage,
    pageSize,

    // 计算属性
    totalPages,
    hasNextPage,
    hasPrevPage,

    // 方法
    getUserList,
    createUser,
    updateUser,
    deleteUser,
    batchDeleteUsers,
    enableUser,
    disableUser,
    resetUserPassword,
    getUserById,
    searchUsers,
    filterUsersByRole,
    filterUsersByStatus,
    resetFilters,
    setCurrentPage,
    setPageSize,
  };
});
