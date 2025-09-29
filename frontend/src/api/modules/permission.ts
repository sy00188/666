import { request } from "@/utils/request";
import type {
  Permission,
  PermissionQueryParams,
  CreatePermissionParams,
  UpdatePermissionParams,
  BatchPermissionOperation,
  PermissionExportParams,
  PermissionImportParams,
  PermissionResponse,
  PermissionListResponse,
  PermissionTreeResponse,
  PermissionStatisticsResponse,
} from "@/types/permission";

// 权限API类
class PermissionAPI {
  // 获取权限树
  async getPermissionTree(params?: {
    type?: string;
    status?: number;
  }): Promise<PermissionTreeResponse> {
    return request.get<PermissionTreeResponse>("/permissions/tree", { params });
  }

  // 获取权限列表（分页）
  async getPermissionList(
    params: PermissionQueryParams,
  ): Promise<PermissionListResponse> {
    return request.get<PermissionListResponse>("/permissions", { params });
  }

  // 获取权限详情
  async getPermissionById(id: string): Promise<PermissionResponse> {
    return request.get<PermissionResponse>(`/permissions/${id}`);
  }

  // 创建权限
  async createPermission(
    data: CreatePermissionParams,
  ): Promise<PermissionResponse> {
    return request.post<PermissionResponse>("/permissions", data);
  }

  // 更新权限
  async updatePermission(
    data: UpdatePermissionParams,
  ): Promise<PermissionResponse> {
    return request.put<PermissionResponse>(`/permissions/${data.id}`, data);
  }

  // 删除权限
  async deletePermission(id: string): Promise<PermissionResponse> {
    return request.delete<PermissionResponse>(`/permissions/${id}`);
  }

  // 批量删除权限
  async batchDeletePermissions(ids: string[]): Promise<PermissionResponse> {
    return request.post<PermissionResponse>("/permissions/batch-delete", {
      ids,
    });
  }

  // 启用权限
  async enablePermission(id: string): Promise<PermissionResponse> {
    return request.put<PermissionResponse>(`/permissions/${id}/enable`);
  }

  // 禁用权限
  async disablePermission(id: string): Promise<PermissionResponse> {
    return request.put<PermissionResponse>(`/permissions/${id}/disable`);
  }

  // 批量操作权限
  async batchOperatePermissions(
    data: BatchPermissionOperation,
  ): Promise<PermissionResponse> {
    return request.post<PermissionResponse>("/permissions/batch-operate", data);
  }

  // 检查权限编码是否存在
  async checkPermissionCode(
    code: string,
    excludeId?: string,
  ): Promise<{ exists: boolean }> {
    return request.get<{ exists: boolean }>("/permissions/check-code", {
      params: { code, excludeId },
    });
  }

  // 获取子权限
  async getChildPermissions(parentId: string): Promise<PermissionListResponse> {
    return request.get<PermissionListResponse>(
      `/permissions/${parentId}/children`,
    );
  }

  // 移动权限
  async movePermission(
    id: string,
    newParentId?: string,
  ): Promise<PermissionResponse> {
    return request.put<PermissionResponse>(`/permissions/${id}/move`, {
      newParentId,
    });
  }

  // 更新权限排序
  async updatePermissionSort(
    id: string,
    sortOrder: number,
  ): Promise<PermissionResponse> {
    return request.put<PermissionResponse>(`/permissions/${id}/sort`, {
      sortOrder,
    });
  }

  // 批量更新权限排序
  async batchUpdatePermissionSort(
    sortData: Array<{ id: string; sortOrder: number }>,
  ): Promise<PermissionResponse> {
    return request.put<PermissionResponse>("/permissions/batch-sort", {
      sortData,
    });
  }

  // 复制权限
  async copyPermission(
    id: string,
    newName: string,
    newCode: string,
  ): Promise<PermissionResponse> {
    return request.post<PermissionResponse>(`/permissions/${id}/copy`, {
      newName,
      newCode,
    });
  }

  // 导出权限
  async exportPermissions(params: PermissionExportParams): Promise<Blob> {
    return request.get<Blob>("/permissions/export", {
      params,
      responseType: "blob",
    });
  }

  // 导入权限
  async importPermissions(
    data: PermissionImportParams,
  ): Promise<PermissionResponse> {
    const formData = new FormData();
    formData.append("file", data.file);
    if (data.overwrite !== undefined) {
      formData.append("overwrite", String(data.overwrite));
    }
    return request.post<PermissionResponse>("/permissions/import", formData);
  }

  // 获取权限统计
  async getPermissionStatistics(): Promise<PermissionStatisticsResponse> {
    return request.get<PermissionStatisticsResponse>("/permissions/statistics");
  }

  // 获取权限路径
  async getPermissionPath(id: string): Promise<{ path: Permission[] }> {
    return request.get<{ path: Permission[] }>(`/permissions/${id}/path`);
  }

  // 搜索权限
  async searchPermissions(
    keyword: string,
    type?: string,
  ): Promise<PermissionListResponse> {
    return request.get<PermissionListResponse>("/permissions/search", {
      params: { keyword, type },
    });
  }

  // 获取权限模板
  async getPermissionTemplates(): Promise<{ templates: Permission[] }> {
    return request.get<{ templates: Permission[] }>("/permissions/templates");
  }

  // 从模板创建权限
  async createFromTemplate(
    templateId: string,
    parentId?: string,
  ): Promise<PermissionResponse> {
    return request.post<PermissionResponse>(
      "/permissions/create-from-template",
      {
        templateId,
        parentId,
      },
    );
  }

  // 验证权限深度
  async validatePermissionDepth(
    parentId?: string,
  ): Promise<{ valid: boolean; maxDepth: number; currentDepth: number }> {
    return request.get<{
      valid: boolean;
      maxDepth: number;
      currentDepth: number;
    }>("/permissions/validate-depth", { params: { parentId } });
  }

  // 获取权限依赖关系
  async getPermissionDependencies(
    id: string,
  ): Promise<{ dependencies: Permission[]; dependents: Permission[] }> {
    return request.get<{
      dependencies: Permission[];
      dependents: Permission[];
    }>(`/permissions/${id}/dependencies`);
  }
}

export const permissionApi = new PermissionAPI();

export const {
  getPermissionTree,
  getPermissionList,
  getPermissionById,
  createPermission,
  updatePermission,
  deletePermission,
  batchDeletePermissions,
  enablePermission,
  disablePermission,
  batchOperatePermissions,
  checkPermissionCode,
  getChildPermissions,
  movePermission,
  updatePermissionSort,
  batchUpdatePermissionSort,
  copyPermission,
  exportPermissions,
  importPermissions,
  getPermissionStatistics,
  getPermissionPath,
  searchPermissions,
  getPermissionTemplates,
  createFromTemplate,
  validatePermissionDepth,
  getPermissionDependencies,
} = permissionApi;
