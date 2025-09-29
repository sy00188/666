// 角色管理相关类型定义

// 角色状态枚举
export enum RoleStatus {
  ACTIVE = "active",
  INACTIVE = "inactive",
}

// 权限类型枚举
export enum PermissionType {
  MENU = "menu",
  BUTTON = "button",
  API = "api",
  DATA = "data",
}

// 权限接口
export interface Permission {
  id: string;
  name: string;
  code: string;
  type: PermissionType;
  parentId?: string;
  path?: string;
  component?: string;
  icon?: string;
  sort: number;
  status: RoleStatus;
  description?: string;
  createTime: Date;
  updateTime?: Date;
  children?: Permission[];
}

// 角色接口
export interface Role {
  id: string;
  name: string;
  code: string;
  description?: string;
  status: RoleStatus;
  type: "system" | "custom";
  isSystem: boolean;
  sort: number;
  userCount: number;
  permissions: Permission[];
  permissionIds?: string[];
  createdBy?: string;
  createdAt: Date;
  updatedBy?: string;
  updatedAt?: Date;
  remark?: string;
}

// 创建角色表单
export interface CreateRoleForm {
  name: string;
  code: string;
  description?: string;
  status: RoleStatus;
  type: "system" | "custom";
  sort: number;
  permissions?: string[];
  remark?: string;
}

// 更新角色表单
export interface UpdateRoleForm {
  name?: string;
  code?: string;
  description?: string;
  status?: RoleStatus;
  sort?: number;
  permissions?: string[];
  remark?: string;
}

// 角色查询参数
export interface RoleQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  name?: string;
  code?: string;
  status?: RoleStatus;
  type?: "system" | "custom";
  isSystem?: boolean;
  startDate?: Date;
  endDate?: Date;
  sortBy?: "name" | "code" | "createdAt" | "sort";
  sortOrder?: "asc" | "desc";
}

// 权限查询参数
export interface PermissionQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  type?: PermissionType;
  status?: RoleStatus;
  parentId?: string;
  sortBy?: "sort" | "name" | "createTime";
  sortOrder?: "asc" | "desc";
}

// 角色权限分配表单
export interface RolePermissionAssignForm {
  roleId: string;
  permissionIds: string[];
}

// 角色统计信息
export interface RoleStatistics {
  totalRoles: number;
  activeRoles: number;
  inactiveRoles: number;
  systemRoles: number;
  customRoles: number;
  totalPermissions: number;
  roleUserStats: Array<{
    roleId: string;
    roleName: string;
    userCount: number;
  }>;
  permissionStats: Array<{
    type: PermissionType;
    count: number;
  }>;
}

// API响应类型
export interface RoleApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  timestamp: number;
}

// 分页响应类型
export interface RolePaginatedResponse<T = unknown> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 权限树节点
export interface PermissionTreeNode {
  id: string;
  name: string;
  code: string;
  type: PermissionType;
  parentId?: string;
  icon?: string;
  sort: number;
  status: RoleStatus;
  children?: PermissionTreeNode[];
  checked?: boolean;
  indeterminate?: boolean;
}

// 角色关联用户
export interface RoleUser {
  id: string;
  username: string;
  realName: string;
  email: string;
  phone: string;
  department?: string;
  position?: string;
  status: "active" | "inactive" | "banned";
  lastLoginTime?: Date;
  createTime: Date;
}

// 批量角色操作
export interface BatchRoleOperation {
  operation: "enable" | "disable" | "delete";
  roleIds: string[];
  ids?: string[];
  reason?: string;
}

// 角色导出参数
export interface RoleExportParams {
  ids?: string[];
  format?: "excel" | "csv";
}
