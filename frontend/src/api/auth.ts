import { request } from "@/utils/request";
import type {
  LoginForm,
  RegisterForm,
  ChangePasswordForm,
  AuthResponse,
  LoginResponseData,
  RefreshTokenResponseData,
  User,
} from "@/types/auth";

// 认证API接口
export const authApi = {
  // 登录
  login: (data: LoginForm): Promise<AuthResponse<LoginResponseData>> => {
    return request.post("/auth/login", data);
  },

  // 注册
  register: (data: RegisterForm): Promise<AuthResponse> => {
    return request.post("/auth/register", data);
  },

  // 登出
  logout: (): Promise<AuthResponse> => {
    return request.post("/auth/logout");
  },

  // 获取用户信息
  getUserInfo: (): Promise<AuthResponse<User>> => {
    return request.get("/auth/user");
  },

  // 更新用户信息
  updateUserInfo: (data: Partial<User>): Promise<AuthResponse<User>> => {
    return request.put("/auth/user", data);
  },

  // 修改密码
  changePassword: (data: ChangePasswordForm): Promise<AuthResponse> => {
    return request.post("/auth/change-password", data);
  },

  // 刷新token
  refreshToken: (): Promise<AuthResponse<RefreshTokenResponseData>> => {
    return request.post("/auth/refresh-token");
  },

  // 发送验证码
  sendCaptcha: (email: string): Promise<AuthResponse> => {
    return request.post("/auth/send-captcha", { email });
  },

  // 验证邮箱
  verifyEmail: (token: string): Promise<AuthResponse> => {
    return request.post("/auth/verify-email", { token });
  },

  // 忘记密码
  forgotPassword: (email: string): Promise<AuthResponse> => {
    return request.post("/auth/forgot-password", { email });
  },

  // 重置密码
  resetPassword: (data: {
    token: string;
    password: string;
  }): Promise<AuthResponse> => {
    return request.post("/auth/reset-password", data);
  },
};
