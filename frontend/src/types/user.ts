// 用户管理相关类型定义

// 用户基础信息类型（继承自auth.ts中的User类型，但添加管理相关字段）
export interface UserInfo {
  id: string;
  username: string;
  realName: string;
  email: string;
  phone: string;
  role: "admin" | "user";
  status: "active" | "inactive" | "banned";
  avatar?: string;
  department?: string;
  position?: string;
  employeeId?: string;
  lastLoginTime?: Date;
  loginCount: number;
  createTime: Date;
  updateTime?: Date;
  createUserId?: string;
  createUserName?: string;
  remark?: string;
  permissions: string[];
}

// 用户创建表单类型
export interface CreateUserForm {
  username: string;
  password: string;
  realName: string;
  email: string;
  phone: string;
  role: "admin" | "user";
  department?: string;
  position?: string;
  employeeId?: string;
  remark?: string;
  permissions?: string[];
}

// 用户更新表单类型
export interface UpdateUserForm {
  realName?: string;
  email?: string;
  phone?: string;
  role?: "admin" | "user";
  status?: "active" | "inactive" | "banned";
  department?: string;
  position?: string;
  employeeId?: string;
  remark?: string;
  permissions?: string[];
}

// 用户查询参数类型
export interface UserQueryParams {
  page?: number;
  size?: number;
  keyword?: string;
  role?: string;
  status?: string;
  department?: string;
  startDate?: Date;
  endDate?: Date;
  sortBy?: "createTime" | "lastLoginTime" | "username" | "realName";
  sortOrder?: "asc" | "desc";
}

// 用户统计类型
export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  bannedUsers: number;
  adminUsers: number;
  regularUsers: number;
  newUsersThisMonth: number;
  activeUsersThisMonth: number;
  departmentStats: Array<{
    department: string;
    count: number;
  }>;
  roleStats: Array<{
    role: string;
    count: number;
  }>;
}

// 用户活动日志类型
export interface UserActivityLog {
  id: string;
  userId: string;
  userName: string;
  userRealName: string;
  action:
    | "login"
    | "logout"
    | "create"
    | "update"
    | "delete"
    | "borrow"
    | "return"
    | "other";
  actionDescription: string;
  ipAddress: string;
  userAgent: string;
  location?: string;
  createTime: Date;
  details?: Record<string, unknown>;
}

// 用户权限类型
export interface UserPermission {
  id: string;
  name: string;
  code: string;
  type: "menu" | "button" | "api";
  parentId?: string;
  path?: string;
  component?: string;
  icon?: string;
  sort: number;
  status: "active" | "inactive";
  description?: string;
  createTime: Date;
  updateTime?: Date;
}

// 用户角色类型
export interface UserRole {
  id: string;
  name: string;
  code: string;
  description?: string;
  permissions: UserPermission[];
  status: "active" | "inactive";
  sort: number;
  createTime: Date;
  updateTime?: Date;
  userCount?: number;
}

// 用户部门类型
export interface Department {
  id: string;
  name: string;
  code: string;
  parentId?: string;
  level: number;
  sort: number;
  status: "active" | "inactive";
  description?: string;
  managerId?: string;
  managerName?: string;
  createTime: Date;
  updateTime?: Date;
  children?: Department[];
  userCount?: number;
}

// 用户导入数据类型
export interface UserImportData {
  username: string;
  password: string;
  realName: string;
  email: string;
  phone: string;
  role: string;
  department?: string;
  position?: string;
  employeeId?: string;
  remark?: string;
}

// 用户导入结果类型
export interface UserImportResult {
  total: number;
  success: number;
  failed: number;
  errors: Array<{
    row: number;
    username: string;
    error: string;
  }>;
  successList: UserInfo[];
}

// 用户导出参数类型
export interface UserExportParams {
  ids?: string[];
  role?: string;
  status?: string;
  department?: string;
  startDate?: Date;
  endDate?: Date;
  fields?: string[];
}

// 密码重置表单类型
export interface ResetUserPasswordForm {
  userId: string;
  newPassword: string;
  confirmPassword: string;
  sendEmail?: boolean;
}

// 用户状态变更记录类型
export interface UserStatusChangeLog {
  id: string;
  userId: string;
  userName: string;
  oldStatus: string;
  newStatus: string;
  reason: string;
  operatorId: string;
  operatorName: string;
  createTime: Date;
}

// 用户登录记录类型
export interface UserLoginRecord {
  id: string;
  userId: string;
  userName: string;
  loginTime: Date;
  logoutTime?: Date;
  ipAddress: string;
  userAgent: string;
  location?: string;
  loginType: "web" | "mobile" | "api";
  status: "success" | "failed";
  failReason?: string;
  sessionDuration?: number;
}

// 用户偏好设置类型
export interface UserPreferences {
  userId: string;
  theme: "light" | "dark" | "auto";
  language: "zh-CN" | "en-US";
  timezone: string;
  dateFormat: string;
  pageSize: number;
  notifications: {
    email: boolean;
    sms: boolean;
    browser: boolean;
  };
  privacy: {
    showEmail: boolean;
    showPhone: boolean;
    showLastLogin: boolean;
  };
  customSettings?: Record<string, unknown>;
}

// API响应基础类型
export interface UserApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  timestamp: number;
}

// 分页响应类型
export interface UserPaginatedResponse<T = unknown> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}
