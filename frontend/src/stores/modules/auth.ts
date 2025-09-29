import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { User, LoginForm, RegisterForm } from "@/types/auth";
import { login, register, getUserInfo, logout } from "@/api/modules/auth";

export const useAuthStore = defineStore("auth", () => {
  // 状态
  const token = ref<string>(localStorage.getItem("token") || "");
  const user = ref<User | null>(null);
  const loading = ref(false);

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value);

  // 登录
  const loginAction = async (loginForm: LoginForm) => {
    loading.value = true;
    try {
      const response = await login(loginForm);
      token.value = response.data.token;
      user.value = response.data.user;
      localStorage.setItem("token", token.value);
      return response;
    } finally {
      loading.value = false;
    }
  };

  // 注册
  const registerAction = async (registerForm: RegisterForm) => {
    loading.value = true;
    try {
      const response = await register(registerForm);
      return response;
    } finally {
      loading.value = false;
    }
  };

  // 获取用户信息
  const getUserInfoAction = async () => {
    if (!token.value) return;

    try {
      const response = await getUserInfo();
      user.value = response.data;
      return response;
    } catch (error) {
      // 如果获取用户信息失败，清除token
      logoutAction();
      throw error;
    }
  };

  // 登出
  const logoutAction = async () => {
    try {
      if (token.value) {
        await logout();
      }
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      token.value = "";
      user.value = null;
      localStorage.removeItem("token");
    }
  };

  // 初始化
  const initAuth = async () => {
    if (token.value) {
      try {
        await getUserInfoAction();
      } catch (error) {
        console.error("Init auth error:", error);
      }
    }
  };

  return {
    // 状态
    token,
    user,
    loading,
    // 计算属性
    isAuthenticated,
    // 方法
    loginAction,
    registerAction,
    getUserInfoAction,
    logoutAction,
    initAuth,
  };
});
