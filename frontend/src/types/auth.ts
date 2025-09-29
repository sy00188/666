// 用户信息接口
export interface User {
  id: string;
  username: string;
  name: string;
  email: string;
  phone?: string;
  avatar?: string;
  role: "admin" | "user";
  permissions: string[];
  status: "active" | "inactive" | "banned";
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

// 登录表单接口
export interface LoginForm {
  username: string;
  password: string;
  remember?: boolean;
  captcha?: string;
}

// 注册表单接口
export interface RegisterForm {
  username: string;
  password: string;
  confirmPassword: string;
  name: string;
  email: string;
  phone?: string;
  captcha?: string;
}

// 修改密码表单接口
export interface ChangePasswordForm {
  oldPassword: string;
  newPassword: string;
  confirmPassword?: string;
}

// 认证响应接口
export interface AuthResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T;
  code?: number;
}

// 登录响应数据
export interface LoginResponseData {
  token: string;
  user: User;
  expiresIn: number;
}

// 刷新token响应数据
export interface RefreshTokenResponseData {
  token: string;
  expiresIn: number;
}

// 用户权限枚举
export enum UserPermission {
  // 档案管理
  ARCHIVE_VIEW = "archive:view",
  ARCHIVE_CREATE = "archive:create",
  ARCHIVE_EDIT = "archive:edit",
  ARCHIVE_DELETE = "archive:delete",
  ARCHIVE_EXPORT = "archive:export",

  // 借阅管理
  BORROW_VIEW = "borrow:view",
  BORROW_CREATE = "borrow:create",
  BORROW_APPROVE = "borrow:approve",
  BORROW_RETURN = "borrow:return",
  BORROW_EXPORT = "borrow:export",

  // 用户管理
  USER_VIEW = "user:view",
  USER_CREATE = "user:create",
  USER_EDIT = "user:edit",
  USER_DELETE = "user:delete",
  USER_RESET_PASSWORD = "user:reset_password",

  // 报表统计
  REPORT_VIEW = "report:view",
  REPORT_EXPORT = "report:export",

  // 系统管理
  SYSTEM_CONFIG = "system:config",
  SYSTEM_LOG = "system:log",
  SYSTEM_BACKUP = "system:backup",
}

// 用户角色枚举
export enum UserRole {
  ADMIN = "admin",
  USER = "user",
}

// 用户状态枚举
export enum UserStatus {
  ACTIVE = "active",
  INACTIVE = "inactive",
  BANNED = "banned",
}
