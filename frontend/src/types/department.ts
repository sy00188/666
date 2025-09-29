// 部门管理相关类型定义

// 部门基础信息类型（匹配后端DepartmentResponse结构）
export interface Department {
  id: string;
  name: string;
  code: string;
  parentId?: string;
  parentName?: string;
  level: number;
  sort: number;
  status: "active" | "inactive";
  description?: string;
  managerId?: string;
  managerName?: string;
  managerUsername?: string;
  createTime: Date;
  updateTime?: Date;
  createUserId?: string;
  createUserName?: string;
  updateUserId?: string;
  updateUserName?: string;
  // 树形结构相关
  children?: Department[];
  hasChildren?: boolean;
  path?: string; // 部门路径，如：/总公司/技术部/前端组
  // 统计信息
  userCount?: number;
  childrenCount?: number;
  totalUserCount?: number; // 包含子部门的总用户数
}

// 部门创建表单类型
export interface CreateDepartmentForm {
  name: string;
  code: string;
  parentId?: string;
  sort?: number;
  status?: "active" | "inactive";
  description?: string;
  managerId?: string;
}

// 部门更新表单类型
export interface UpdateDepartmentForm {
  name?: string;
  code?: string;
  parentId?: string;
  sort?: number;
  status?: "active" | "inactive";
  description?: string;
  managerId?: string;
}

// 部门查询参数类型
export interface DepartmentQueryParams {
  page?: number;
  size?: number;
  keyword?: string; // 搜索关键字（部门名称或编码）
  status?: "active" | "inactive";
  parentId?: string;
  level?: number;
  managerId?: string;
  managerName?: string;
  includeChildren?: boolean; // 是否包含子部门
  sortBy?: "name" | "code" | "sort" | "createTime" | "updateTime";
  sortOrder?: "asc" | "desc";
}

// 部门树形查询参数
export interface DepartmentTreeQueryParams {
  status?: "active" | "inactive";
  includeUserCount?: boolean; // 是否包含用户统计
  maxLevel?: number; // 最大层级
  expandAll?: boolean; // 是否展开所有节点
}

// 部门统计信息类型
export interface DepartmentStatistics {
  totalDepartments: number;
  activeDepartments: number;
  inactiveDepartments: number;
  maxLevel: number;
  avgUsersPerDepartment: number;
  departmentsWithoutManager: number;
  departmentsWithoutUsers: number;
  levelStats: Array<{
    level: number;
    count: number;
  }>;
  topDepartmentsByUsers: Array<{
    id: string;
    name: string;
    userCount: number;
  }>;
}

// 部门成员信息类型
export interface DepartmentMember {
  id: string;
  username: string;
  realName: string;
  email: string;
  phone: string;
  position?: string;
  employeeId?: string;
  status: "active" | "inactive" | "banned";
  isManager: boolean;
  joinTime: Date;
  avatar?: string;
}

// 部门成员查询参数
export interface DepartmentMemberQueryParams {
  departmentId: string;
  page?: number;
  size?: number;
  keyword?: string;
  status?: "active" | "inactive" | "banned";
  isManager?: boolean;
  sortBy?: "realName" | "username" | "joinTime" | "position";
  sortOrder?: "asc" | "desc";
}

// 部门层级调整参数
export interface DepartmentMoveParams {
  departmentId: string;
  newParentId?: string;
  newSort?: number;
}

// 批量操作参数
export interface DepartmentBatchOperationParams {
  ids: string[];
  operation: "delete" | "activate" | "deactivate" | "move";
  targetParentId?: string; // 批量移动时的目标父部门
}

// 部门导入数据类型
export interface DepartmentImportData {
  name: string;
  code: string;
  parentCode?: string; // 父部门编码
  sort?: number;
  status?: string;
  description?: string;
  managerUsername?: string; // 管理员用户名
}

// 部门导入结果类型
export interface DepartmentImportResult {
  total: number;
  success: number;
  failed: number;
  errors: Array<{
    row: number;
    name: string;
    code: string;
    error: string;
  }>;
  successList: Department[];
}

// 部门导出参数类型
export interface DepartmentExportParams {
  ids?: string[];
  status?: "active" | "inactive";
  parentId?: string;
  level?: number;
  includeChildren?: boolean;
  fields?: string[]; // 导出字段
  format?: "excel" | "csv";
}

// 部门权限设置类型
export interface DepartmentPermission {
  departmentId: string;
  permissionIds: string[];
  inheritFromParent?: boolean; // 是否继承父部门权限
  applyToChildren?: boolean; // 是否应用到子部门
}

// 部门操作日志类型
export interface DepartmentOperationLog {
  id: string;
  departmentId: string;
  departmentName: string;
  operation:
    | "create"
    | "update"
    | "delete"
    | "move"
    | "assign_manager"
    | "assign_user";
  operationDescription: string;
  oldValue?: Record<string, unknown>;
  newValue?: Record<string, unknown>;
  operatorId: string;
  operatorName: string;
  createTime: Date;
  ipAddress?: string;
}

// API响应类型
export interface DepartmentApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  timestamp: number;
}

// 分页响应类型
export interface DepartmentPaginatedResponse<T = unknown> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 部门树形节点类型（用于树形组件）
export interface DepartmentTreeNode {
  id: string;
  label: string; // 显示名称
  value: string; // 节点值
  disabled?: boolean;
  children?: DepartmentTreeNode[];
  // 扩展属性
  department: Department;
  isLeaf?: boolean;
  loading?: boolean;
}

// 部门选择器选项类型
export interface DepartmentSelectOption {
  value: string;
  label: string;
  disabled?: boolean;
  level: number;
  parentId?: string;
  children?: DepartmentSelectOption[];
}

// 部门表单验证规则类型
export interface DepartmentFormRules {
  name: Array<{
    required?: boolean;
    message: string;
    trigger?: string;
    min?: number;
    max?: number;
  }>;
  code: Array<{
    required?: boolean;
    message: string;
    trigger?: string;
    pattern?: RegExp;
    validator?: (
      rule: unknown,
      value: string,
      callback: (error?: Error) => void,
    ) => void;
  }>;
  parentId?: Array<{
    required?: boolean;
    message: string;
    trigger?: string;
  }>;
  sort?: Array<{
    type?: string;
    message: string;
    trigger?: string;
    min?: number;
    max?: number;
  }>;
}
