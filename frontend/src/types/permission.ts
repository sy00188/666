// 权限类型枚举
export enum PermissionType {
  MENU = "MENU",
  BUTTON = "BUTTON",
  API = "API",
}

// 权限状态枚举
export enum PermissionStatus {
  DISABLED = 0,
  ENABLED = 1,
}

// 权限基础接口
export interface Permission {
  id: string;
  code: string;
  name: string;
  description?: string;
  type: PermissionType;
  status: PermissionStatus;
  parentId?: string;
  level: number;
  sortOrder: number;
  isSystem: boolean;
  permissionPath?: string;
  icon?: string;
  component?: string;
  redirect?: string;
  hidden?: boolean;
  keepAlive?: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  children?: Permission[];
}

// 权限树节点接口
export interface PermissionTree extends Permission {
  children: PermissionTree[];
}

// 权限查询参数接口
export interface PermissionQueryParams {
  keyword?: string;
  type?: PermissionType;
  status?: PermissionStatus;
  parentId?: string;
  level?: number;
  isSystem?: boolean;
  page?: number;
  size?: number;
  sortBy?:
    | "name"
    | "code"
    | "type"
    | "status"
    | "level"
    | "sortOrder"
    | "createdAt";
  sortOrder?: "asc" | "desc";
}

// 权限创建参数接口
export interface CreatePermissionParams {
  code: string;
  name: string;
  description?: string;
  type: PermissionType;
  parentId?: string;
  sortOrder?: number;
  permissionPath?: string;
  icon?: string;
  component?: string;
  redirect?: string;
  hidden?: boolean;
  keepAlive?: boolean;
}

// 权限更新参数接口
export interface UpdatePermissionParams
  extends Partial<CreatePermissionParams> {
  id: string;
  status?: PermissionStatus;
}

// 批量权限操作接口
export interface BatchPermissionOperation {
  operation: "enable" | "disable" | "delete";
  permissionIds: string[];
  ids: string[];
}

// 权限导出参数接口
export interface PermissionExportParams {
  ids?: string[];
  type?: PermissionType;
  status?: PermissionStatus;
  format?: "excel" | "csv";
}

// 权限导入参数接口
export interface PermissionImportParams {
  file: File;
  overwrite?: boolean;
}

// 权限统计接口
export interface PermissionStatistics {
  total: number;
  enabled: number;
  disabled: number;
  menuCount: number;
  buttonCount: number;
  apiCount: number;
  systemCount: number;
  customCount: number;
  levelStatistics: Array<{
    level: number;
    count: number;
  }>;
}

// 权限验证规则接口
export interface PermissionValidationRules {
  code: Array<{
    required: boolean;
    message: string;
    trigger: string;
  }>;
  name: Array<{
    required: boolean;
    message: string;
    trigger: string;
  }>;
  type: Array<{
    required: boolean;
    message: string;
    trigger: string;
  }>;
}

// API响应接口
export interface PermissionResponse {
  success: boolean;
  message: string;
  data: Permission;
}

export interface PermissionListResponse {
  success: boolean;
  message: string;
  data: {
    list: Permission[];
    total: number;
    page: number;
    size: number;
  };
}

export interface PermissionTreeResponse {
  success: boolean;
  message: string;
  data: PermissionTree[];
}

export interface PermissionStatisticsResponse {
  success: boolean;
  message: string;
  data: PermissionStatistics;
}
