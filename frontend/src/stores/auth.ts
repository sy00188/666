import { defineStore } from "pinia";
import { ref, computed } from "vue";
import type { User, LoginForm, RegisterForm } from "@/types/auth";
import { authApi } from "@/api/auth";
import { ElMessage } from "element-plus";

export const useAuthStore = defineStore(
  "auth",
  () => {
    // 状态
    const token = ref<string>("");
    const user = ref<User | null>(null);
    const loading = ref(false);

    // 计算属性
    const isAuthenticated = computed(() => !!token.value && !!user.value);
    const userRole = computed(() => user.value?.role || "");
    const userName = computed(() => user.value?.name || "");
    const userAvatar = computed(() => user.value?.avatar || "");

    // 权限检查
    const hasPermission = (permissions: string | string[]): boolean => {
      if (!user.value) return false;

      const userPermissions = user.value.permissions || [];
      const requiredPermissions = Array.isArray(permissions)
        ? permissions
        : [permissions];

      return requiredPermissions.every((permission) =>
        userPermissions.includes(permission),
      );
    };

    const hasRole = (roles: string | string[]): boolean => {
      if (!user.value) return false;

      const requiredRoles = Array.isArray(roles) ? roles : [roles];
      return requiredRoles.includes(user.value.role);
    };

    // 登录
    const login = async (loginForm: LoginForm): Promise<boolean> => {
      try {
        loading.value = true;
        const response = await authApi.login(loginForm);

        if (response?.success && response.data) {
          // 防御性检查：确保token存在
          if (response.data.token) {
            token.value = response.data.token;
            setAuthHeader(response.data.token);
          } else {
            ElMessage.error("登录响应缺少token信息");
            return false;
          }

          // 防御性检查：确保用户信息存在
          if (response.data.user) {
            user.value = response.data.user;
          } else {
            ElMessage.error("登录响应缺少用户信息");
            return false;
          }

          ElMessage.success("登录成功");
          return true;
        } else {
          ElMessage.error(response?.message || "登录失败");
          return false;
        }
      } catch (error: unknown) {
        console.error("登录错误:", error);
        ElMessage.error((error as Error).message || "登录失败");
        return false;
      } finally {
        loading.value = false;
      }
    };

    // 注册
    const register = async (registerForm: RegisterForm): Promise<boolean> => {
      try {
        loading.value = true;
        const response = await authApi.register(registerForm);

        if (response.success) {
          ElMessage.success("注册成功，请登录");
          return true;
        } else {
          ElMessage.error(response.message || "注册失败");
          return false;
        }
      } catch (error: unknown) {
        ElMessage.error((error as Error).message || "注册失败");
        return false;
      } finally {
        loading.value = false;
      }
    };

    // 获取用户信息
    const getUserInfo = async (): Promise<void> => {
      try {
        if (!token.value) return;

        const response = await authApi.getUserInfo();
        if (response.success) {
          user.value = response.data;
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
        // 如果获取用户信息失败，可能token已过期，执行登出
        logout();
      }
    };

    // 更新用户信息
    const updateUserInfo = async (
      userInfo: Partial<User>,
    ): Promise<boolean> => {
      try {
        loading.value = true;
        const response = await authApi.updateUserInfo(userInfo);

        if (response.success) {
          user.value = { ...user.value!, ...response.data };
          ElMessage.success("更新成功");
          return true;
        } else {
          ElMessage.error(response.message || "更新失败");
          return false;
        }
      } catch (error: unknown) {
        ElMessage.error((error as Error).message || "更新失败");
        return false;
      } finally {
        loading.value = false;
      }
    };

    // 修改密码
    const changePassword = async (
      oldPassword: string,
      newPassword: string,
    ): Promise<boolean> => {
      try {
        loading.value = true;
        const response = await authApi.changePassword({
          oldPassword,
          newPassword,
        });

        if (response.success) {
          ElMessage.success("密码修改成功");
          return true;
        } else {
          ElMessage.error(response.message || "密码修改失败");
          return false;
        }
      } catch (error: unknown) {
        ElMessage.error((error as Error).message || "密码修改失败");
        return false;
      } finally {
        loading.value = false;
      }
    };

    // 登出
    const logout = async (): Promise<void> => {
      try {
        if (token.value) {
          await authApi.logout();
        }
      } catch (error) {
        console.error("登出请求失败:", error);
      } finally {
        // 清除本地状态
        token.value = "";
        user.value = null;
        removeAuthHeader();
        ElMessage.success("已退出登录");
      }
    };

    // 刷新token
    const refreshToken = async (): Promise<boolean> => {
      try {
        const response = await authApi.refreshToken();
        if (response?.success && response.data?.token) {
          token.value = response.data.token;
          setAuthHeader(response.data.token);
          return true;
        }
        return false;
      } catch (error) {
        console.error("刷新token失败:", error);
        logout();
        return false;
      }
    };

    // 设置请求头
    const setAuthHeader = (_authToken: string) => {
      // 这里可以设置axios的默认请求头
      // axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`
    };

    // 移除请求头
    const removeAuthHeader = () => {
      // delete axios.defaults.headers.common['Authorization']
    };

    // 初始化
    const init = async () => {
      if (token.value) {
        setAuthHeader(token.value);
        await getUserInfo();
      }
    };

    return {
      // 状态
      token,
      user,
      loading,

      // 计算属性
      isAuthenticated,
      userRole,
      userName,
      userAvatar,

      // 方法
      hasPermission,
      hasRole,
      login,
      register,
      getUserInfo,
      updateUserInfo,
      changePassword,
      logout,
      refreshToken,
      init,
    };
  },
  {
    persist: {
      key: "auth-store",
      storage: localStorage,
      paths: ["token", "user"],
    },
  },
);
