import { request } from "@/utils/request";
import type {
  Role,
  Permission,
  CreateRoleForm,
  UpdateRoleForm,
  RoleQueryParams,
  PermissionQueryParams,
  RolePermissionAssignForm,
  RoleStatistics,
  RoleApiResponse,
  RolePaginatedResponse,
  PermissionTreeNode,
  RoleUser,
  BatchRoleOperation,
} from "@/types/role";

// 角色管理相关API

// 获取角色列表
export const getRoleList = (
  params?: RoleQueryParams,
): Promise<RoleApiResponse<RolePaginatedResponse<Role>>> => {
  return request.get("/roles", { params });
};

// 获取角色详情
export const getRoleDetail = (id: string): Promise<RoleApiResponse<Role>> => {
  return request.get(`/roles/${id}`);
};

// 创建角色
export const createRole = (
  data: CreateRoleForm,
): Promise<RoleApiResponse<Role>> => {
  return request.post("/roles", data);
};

// 更新角色
export const updateRole = (
  id: string,
  data: UpdateRoleForm,
): Promise<RoleApiResponse<Role>> => {
  return request.put(`/roles/${id}`, data);
};

// 删除角色
export const deleteRole = (id: string): Promise<RoleApiResponse<null>> => {
  return request.delete(`/roles/${id}`);
};

// 批量删除角色
export const batchDeleteRoles = (
  ids: string[],
): Promise<RoleApiResponse<null>> => {
  return request.post("/roles/batch-delete", { ids });
};

// 批量操作角色
export const batchOperateRoles = (
  data: BatchRoleOperation,
): Promise<RoleApiResponse<null>> => {
  return request.post("/roles/batch-operate", data);
};

// 更改角色状态
export const changeRoleStatus = (
  id: string,
  status: "active" | "inactive",
): Promise<RoleApiResponse<null>> => {
  return request.patch(`/roles/${id}/status`, { status });
};

// 获取角色统计信息
export const getRoleStatistics = (): Promise<
  RoleApiResponse<RoleStatistics>
> => {
  return request.get("/roles/statistics");
};

// 获取角色关联的用户列表
export const getRoleUsers = (
  roleId: string,
  params?: { page?: number; size?: number },
): Promise<RoleApiResponse<RolePaginatedResponse<RoleUser>>> => {
  return request.get(`/roles/${roleId}/users`, { params });
};

// 检查角色编码是否可用
export const checkRoleCodeAvailable = (
  code: string,
  excludeId?: string,
): Promise<RoleApiResponse<boolean>> => {
  return request.get("/roles/check-code", { params: { code, excludeId } });
};

// 权限管理相关API

// 获取权限列表
export const getPermissionList = (
  params?: PermissionQueryParams,
): Promise<RoleApiResponse<Permission[]>> => {
  return request.get("/permissions", { params });
};

// 获取权限树
export const getPermissionTree = (params?: {
  type?: string;
  status?: string;
}): Promise<RoleApiResponse<PermissionTreeNode[]>> => {
  return request.get("/permissions/tree", { params });
};

// 获取权限详情
export const getPermissionDetail = (
  id: string,
): Promise<RoleApiResponse<Permission>> => {
  return request.get(`/permissions/${id}`);
};

// 获取所有权限（用于角色分配）
export const getAllPermissions = (): Promise<RoleApiResponse<Permission[]>> => {
  return request.get("/permissions/all");
};

// 角色权限关联相关API

// 获取角色权限
export const getRolePermissions = (
  roleId: string,
): Promise<RoleApiResponse<Permission[]>> => {
  return request.get(`/roles/${roleId}/permissions`);
};

// 分配角色权限
export const assignRolePermissions = (
  data: RolePermissionAssignForm,
): Promise<RoleApiResponse<null>> => {
  return request.post("/roles/assign-permissions", data);
};

// 获取权限树（带角色权限选中状态）
export const getPermissionTreeWithRoleSelection = (
  roleId: string,
): Promise<RoleApiResponse<PermissionTreeNode[]>> => {
  return request.get(`/roles/${roleId}/permission-tree`);
};

// 复制角色权限
export const copyRolePermissions = (
  fromRoleId: string,
  toRoleId: string,
): Promise<RoleApiResponse<null>> => {
  return request.post("/roles/copy-permissions", { fromRoleId, toRoleId });
};

// 导入导出相关API

// 导出角色
export const exportRoles = (params?: {
  ids?: string[];
  format?: "excel" | "csv";
}): Promise<Blob> => {
  return request.get("/roles/export", {
    params,
    responseType: "blob",
  });
};

// 下载角色导入模板
export const downloadRoleTemplate = (): Promise<Blob> => {
  return request.get("/roles/template", {
    responseType: "blob",
  });
};

// 导入角色
export const importRoles = (
  file: File,
): Promise<
  RoleApiResponse<{ success: number; failed: number; errors: any[] }>
> => {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/roles/import", formData);
};

// 角色验证相关API

// 验证用户是否有指定权限
export const validateUserPermission = (
  userId: string,
  permission: string,
): Promise<RoleApiResponse<boolean>> => {
  return request.get("/roles/validate-permission", {
    params: { userId, permission },
  });
};

// 获取可分配的角色列表（排除系统角色等）
export const getAssignableRoles = (): Promise<RoleApiResponse<Role[]>> => {
  return request.get("/roles/assignable");
};

// 预览权限变更影响
export const previewPermissionChangeImpact = (
  roleId: string,
  permissionIds: string[],
): Promise<
  RoleApiResponse<{ affectedUsers: number; changedPermissions: string[] }>
> => {
  return request.post("/roles/preview-permission-change", {
    roleId,
    permissionIds,
  });
};
