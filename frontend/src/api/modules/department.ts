import { request } from "@/utils/request";
import type {
  Department,
  DepartmentQueryParams,
  DepartmentTreeQueryParams,
  CreateDepartmentForm,
  UpdateDepartmentForm,
  DepartmentStatistics,
  DepartmentMember,
  DepartmentMemberQueryParams,
  DepartmentMoveParams,
  DepartmentBatchOperationParams,
  DepartmentImportData,
  DepartmentImportResult,
  DepartmentExportParams,
  DepartmentPermission,
  DepartmentOperationLog,
  DepartmentApiResponse,
  DepartmentPaginatedResponse,
  DepartmentTreeNode,
  DepartmentSelectOption,
} from "@/types/department";

// 部门管理API类
class DepartmentAPI {
  // ==================== 基础CRUD操作 ====================

  // 获取部门树
  async getDepartmentTree(
    params?: DepartmentTreeQueryParams,
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get("/departments/tree", { params });
  }

  // 获取部门列表（分页）
  async getDepartmentList(
    params: DepartmentQueryParams,
  ): Promise<DepartmentPaginatedResponse<Department>> {
    return request.get("/departments", { params });
  }

  // 获取部门详情
  async getDepartmentById(
    id: string,
  ): Promise<DepartmentApiResponse<Department>> {
    return request.get(`/departments/${id}`);
  }

  // 创建部门
  async createDepartment(
    data: CreateDepartmentForm,
  ): Promise<DepartmentApiResponse<Department>> {
    return request.post("/departments", data);
  }

  // 更新部门
  async updateDepartment(
    id: string,
    data: UpdateDepartmentForm,
  ): Promise<DepartmentApiResponse<Department>> {
    return request.put(`/departments/${id}`, data);
  }

  // 删除部门
  async deleteDepartment(id: string): Promise<DepartmentApiResponse<null>> {
    return request.delete(`/departments/${id}`);
  }

  // ==================== 层级管理 ====================

  // 移动部门（调整层级）
  async moveDepartment(
    data: DepartmentMoveParams,
  ): Promise<DepartmentApiResponse<Department>> {
    return request.put(`/departments/${data.departmentId}/move`, {
      newParentId: data.newParentId,
      newSort: data.newSort,
    });
  }

  // 更新部门排序
  async updateDepartmentSort(
    id: string,
    sort: number,
  ): Promise<DepartmentApiResponse<Department>> {
    return request.put(`/departments/${id}/sort`, { sort });
  }

  // 批量更新部门排序
  async batchUpdateDepartmentSort(
    sortData: Array<{ id: string; sort: number }>,
  ): Promise<DepartmentApiResponse<null>> {
    return request.put("/departments/batch/sort", { sortData });
  }

  // 获取部门路径
  async getDepartmentPath(
    id: string,
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get(`/departments/${id}/path`);
  }

  // 获取子部门
  async getChildDepartments(
    parentId: string,
    includeInactive?: boolean,
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get(`/departments/${parentId}/children`, {
      params: { includeInactive },
    });
  }

  // 获取部门祖先
  async getDepartmentAncestors(
    id: string,
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get(`/departments/${id}/ancestors`);
  }

  // 获取部门后代
  async getDepartmentDescendants(
    id: string,
    maxLevel?: number,
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get(`/departments/${id}/descendants`, {
      params: { maxLevel },
    });
  }

  // ==================== 成员管理 ====================

  // 获取部门成员
  async getDepartmentMembers(
    params: DepartmentMemberQueryParams,
  ): Promise<DepartmentPaginatedResponse<DepartmentMember>> {
    return request.get(`/departments/${params.departmentId}/members`, {
      params: { ...params, departmentId: undefined },
    });
  }

  // 添加成员到部门
  async addMemberToDepartment(
    departmentId: string,
    userId: string,
    isManager?: boolean,
  ): Promise<DepartmentApiResponse<null>> {
    return request.post(`/departments/${departmentId}/members`, {
      userId,
      isManager,
    });
  }

  // 从部门移除成员
  async removeMemberFromDepartment(
    departmentId: string,
    userId: string,
  ): Promise<DepartmentApiResponse<null>> {
    return request.delete(`/departments/${departmentId}/members/${userId}`);
  }

  // 设置部门管理员
  async setDepartmentManager(
    departmentId: string,
    userId: string,
  ): Promise<DepartmentApiResponse<Department>> {
    return request.put(`/departments/${departmentId}/manager`, { userId });
  }

  // 批量添加成员
  async batchAddMembers(
    departmentId: string,
    userIds: string[],
  ): Promise<DepartmentApiResponse<null>> {
    return request.post(`/departments/${departmentId}/members/batch`, {
      userIds,
    });
  }

  // 批量移除成员
  async batchRemoveMembers(
    departmentId: string,
    userIds: string[],
  ): Promise<DepartmentApiResponse<null>> {
    return request.delete(`/departments/${departmentId}/members/batch`, {
      data: { userIds },
    });
  }

  // 转移部门成员
  async transferMembers(
    fromDepartmentId: string,
    toDepartmentId: string,
    userIds?: string[],
  ): Promise<DepartmentApiResponse<null>> {
    return request.post("/departments/transfer-members", {
      fromDepartmentId,
      toDepartmentId,
      userIds,
    });
  }

  // ==================== 批量操作 ====================

  // 批量删除部门
  async batchDeleteDepartments(
    ids: string[],
  ): Promise<DepartmentApiResponse<null>> {
    return request.delete("/departments/batch", {
      data: { ids },
    });
  }

  // 批量操作部门
  async batchOperateDepartments(
    data: DepartmentBatchOperationParams,
  ): Promise<DepartmentApiResponse<null>> {
    return request.post("/departments/batch/operate", data);
  }

  // 批量激活部门
  async batchActivateDepartments(
    ids: string[],
  ): Promise<DepartmentApiResponse<null>> {
    return request.put("/departments/batch/activate", { ids });
  }

  // 批量停用部门
  async batchDeactivateDepartments(
    ids: string[],
  ): Promise<DepartmentApiResponse<null>> {
    return request.put("/departments/batch/deactivate", { ids });
  }

  // ==================== 验证和检查 ====================

  // 检查部门编码是否可用
  async checkDepartmentCode(
    code: string,
    excludeId?: string,
  ): Promise<DepartmentApiResponse<{ available: boolean }>> {
    return request.get("/departments/check-code", {
      params: { code, excludeId },
    });
  }

  // 检查部门名称是否可用
  async checkDepartmentName(
    name: string,
    parentId?: string,
    excludeId?: string,
  ): Promise<DepartmentApiResponse<{ available: boolean }>> {
    return request.get("/departments/check-name", {
      params: { name, parentId, excludeId },
    });
  }

  // 验证部门层级深度
  async validateDepartmentDepth(parentId?: string): Promise<
    DepartmentApiResponse<{
      valid: boolean;
      maxDepth: number;
      currentDepth: number;
    }>
  > {
    return request.get("/departments/validate-depth", {
      params: { parentId },
    });
  }

  // 检查部门是否可删除
  async checkDepartmentDeletable(
    id: string,
  ): Promise<DepartmentApiResponse<{ deletable: boolean; reason?: string }>> {
    return request.get(`/departments/${id}/check-deletable`);
  }

  // ==================== 统计和分析 ====================

  // 获取部门统计信息
  async getDepartmentStatistics(): Promise<
    DepartmentApiResponse<DepartmentStatistics>
  > {
    return request.get("/departments/statistics");
  }

  // 获取部门用户统计
  async getDepartmentUserStats(
    departmentId?: string,
  ): Promise<
    DepartmentApiResponse<{ userCount: number; activeUserCount: number }>
  > {
    return request.get("/departments/user-stats", {
      params: { departmentId },
    });
  }

  // 获取部门层级统计
  async getDepartmentLevelStats(): Promise<
    DepartmentApiResponse<Array<{ level: number; count: number }>>
  > {
    return request.get("/departments/level-stats");
  }

  // ==================== 搜索和过滤 ====================

  // 搜索部门
  async searchDepartments(
    keyword: string,
    options?: {
      status?: "active" | "inactive";
      includeChildren?: boolean;
      maxResults?: number;
    },
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get("/departments/search", {
      params: { keyword, ...options },
    });
  }

  // 根据管理员搜索部门
  async searchDepartmentsByManager(
    managerId: string,
  ): Promise<DepartmentApiResponse<Department[]>> {
    return request.get("/departments/by-manager", {
      params: { managerId },
    });
  }

  // ==================== 导入导出 ====================

  // 导出部门数据
  async exportDepartments(params: DepartmentExportParams): Promise<Blob> {
    const response = await request.get("/departments/export", {
      params,
      responseType: "blob",
    });
    return response as unknown as Blob;
  }

  // 导入部门数据
  async importDepartments(
    file: File,
  ): Promise<DepartmentApiResponse<DepartmentImportResult>> {
    const formData = new FormData();
    formData.append("file", file);
    return request.post("/departments/import", formData);
  }

  // 下载部门导入模板
  async downloadDepartmentTemplate(): Promise<Blob> {
    const response = await request.get("/departments/template", {
      responseType: "blob",
    });
    return response as unknown as Blob;
  }

  // 验证导入数据
  async validateImportData(
    data: DepartmentImportData[],
  ): Promise<DepartmentApiResponse<{ valid: boolean; errors: string[] }>> {
    return request.post("/departments/validate-import", { data });
  }

  // ==================== 权限管理 ====================

  // 获取部门权限
  async getDepartmentPermissions(
    departmentId: string,
  ): Promise<DepartmentApiResponse<string[]>> {
    return request.get(`/departments/${departmentId}/permissions`);
  }

  // 设置部门权限
  async setDepartmentPermissions(
    data: DepartmentPermission,
  ): Promise<DepartmentApiResponse<null>> {
    return request.put(`/departments/${data.departmentId}/permissions`, data);
  }

  // 继承父部门权限
  async inheritParentPermissions(
    departmentId: string,
  ): Promise<DepartmentApiResponse<null>> {
    return request.post(`/departments/${departmentId}/inherit-permissions`);
  }

  // ==================== 操作日志 ====================

  // 获取部门操作日志
  async getDepartmentOperationLogs(
    departmentId?: string,
    params?: {
      page?: number;
      size?: number;
      operation?: string;
      startDate?: Date;
      endDate?: Date;
    },
  ): Promise<DepartmentPaginatedResponse<DepartmentOperationLog>> {
    return request.get("/departments/operation-logs", {
      params: { departmentId, ...params },
    });
  }

  // ==================== 工具方法 ====================

  // 获取部门选择器选项
  async getDepartmentSelectOptions(options?: {
    status?: "active" | "inactive";
    excludeIds?: string[];
    maxLevel?: number;
  }): Promise<DepartmentApiResponse<DepartmentSelectOption[]>> {
    return request.get("/departments/select-options", {
      params: options,
    });
  }

  // 获取部门树形节点
  async getDepartmentTreeNodes(options?: {
    status?: "active" | "inactive";
    lazy?: boolean;
    parentId?: string;
  }): Promise<DepartmentApiResponse<DepartmentTreeNode[]>> {
    return request.get("/departments/tree-nodes", {
      params: options,
    });
  }

  // 复制部门结构
  async copyDepartmentStructure(
    sourceDepartmentId: string,
    targetParentId?: string,
    options?: {
      copyMembers?: boolean;
      copyPermissions?: boolean;
      namePrefix?: string;
    },
  ): Promise<DepartmentApiResponse<Department>> {
    return request.post(`/departments/${sourceDepartmentId}/copy`, {
      targetParentId,
      ...options,
    });
  }
}

// 创建API实例
export const departmentApi = new DepartmentAPI();

// 导出所有方法，方便直接使用
export const {
  // 基础CRUD
  getDepartmentTree,
  getDepartmentList,
  getDepartmentById,
  createDepartment,
  updateDepartment,
  deleteDepartment,

  // 层级管理
  moveDepartment,
  updateDepartmentSort,
  batchUpdateDepartmentSort,
  getDepartmentPath,
  getChildDepartments,
  getDepartmentAncestors,
  getDepartmentDescendants,

  // 成员管理
  getDepartmentMembers,
  addMemberToDepartment,
  removeMemberFromDepartment,
  setDepartmentManager,
  batchAddMembers,
  batchRemoveMembers,
  transferMembers,

  // 批量操作
  batchDeleteDepartments,
  batchOperateDepartments,
  batchActivateDepartments,
  batchDeactivateDepartments,

  // 验证检查
  checkDepartmentCode,
  checkDepartmentName,
  validateDepartmentDepth,
  checkDepartmentDeletable,

  // 统计分析
  getDepartmentStatistics,
  getDepartmentUserStats,
  getDepartmentLevelStats,

  // 搜索过滤
  searchDepartments,
  searchDepartmentsByManager,

  // 导入导出
  exportDepartments,
  importDepartments,
  downloadDepartmentTemplate,
  validateImportData,

  // 权限管理
  getDepartmentPermissions,
  setDepartmentPermissions,
  inheritParentPermissions,

  // 操作日志
  getDepartmentOperationLogs,

  // 工具方法
  getDepartmentSelectOptions,
  getDepartmentTreeNodes,
  copyDepartmentStructure,
} = departmentApi;
