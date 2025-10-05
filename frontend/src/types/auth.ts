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

// ==================== 微信登录相关类型 ====================

// 微信登录响应
export interface WeChatLoginResponse {
  state: string;
  qrcodeUrl?: string;
  expiresIn: number;
  mockMode: boolean;
  message: string;
}

// 微信登录状态
export interface WeChatLoginStatus {
  status: "pending" | "success" | "expired" | "error" | "binding_required";
  message: string;
  openid?: string;
  nickname?: string;
  avatar?: string;
  userId?: number;
  loginResponse?: LoginResponseData;
}

// 微信用户信息
export interface WeChatUserInfo {
  openid: string;
  unionid?: string;
  nickname: string;
  sex?: number;
  province?: string;
  city?: string;
  country?: string;
  headimgurl?: string;
}

// 微信登录请求
export interface WeChatLoginRequest {
  state: string;
  mockWechatId?: string;
  mockNickname?: string;
}

// 微信绑定请求
export interface WeChatBindRequest {
  state: string;
  username?: string;
  password?: string;
  createNew: boolean;
  newUsername?: string;
  email?: string;
  phone?: string;
  realName?: string;
}

// QQ登录响应接口
export interface QQLoginResponse {
  state: string;
  qrcodeUrl?: string;
  expiresIn: number;
  mockMode: boolean;
  message: string;
}

// QQ登录状态接口
export interface QQLoginStatus {
  status: "pending" | "success" | "expired" | "error" | "binding_required";
  message: string;
  openid?: string;
  nickname?: string;
  avatar?: string;
  userId?: number;
  loginResponse?: LoginResponseData;
}

// QQ用户信息接口
export interface QQUserInfo {
  openid: string;
  nickname: string;
  avatar?: string;
}

// QQ登录请求接口
export interface QQLoginRequest {
  state: string;
  mockQQId?: string;
  mockNickname?: string;
}

// QQ账号绑定请求接口
export interface QQBindRequest {
  state: string;
  username?: string;
  password?: string;
  createNew: boolean;
  newUsername?: string;
  email?: string;
  phone?: string;
  realName?: string;
}
