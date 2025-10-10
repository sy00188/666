import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosResponse,
} from "axios";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/stores/auth";

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: (import.meta as ImportMeta).env?.VITE_API_BASE_URL || "/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore();

    // 添加token到请求头 - 防御性检查
    if (authStore?.token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }

    return config;
  },
  (error) => {
    console.error("请求错误:", error);
    return Promise.reject(error);
  },
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response;

    // 如果是文件下载等特殊响应，直接返回
    if (response.config.responseType === "blob") {
      return response;
    }

    // 正常响应处理
    if (data.success !== false) {
      return data;
    }

    // 业务错误处理
    ElMessage.error(data.message || "请求失败");
    return Promise.reject(new Error(data.message || "请求失败"));
  },
  async (error) => {
    const { response } = error;
    const authStore = useAuthStore();

    if (response) {
      const { status, data } = response;

      switch (status) {
        case 401:
          // token过期或无效
          ElMessage.error("登录已过期，请重新登录");
          authStore.logout();
          // 跳转到登录页
          window.location.href = "/auth/login";
          break;

        case 403:
          ElMessage.error("权限不足，无法访问");
          break;

        case 404:
          ElMessage.error("请求的资源不存在");
          break;

        case 422:
          // 表单验证错误
          if (data.errors) {
            const errorMessages = Object.values(data.errors).flat();
            ElMessage.error(errorMessages.join(", "));
          } else {
            ElMessage.error(data.message || "请求参数错误");
          }
          break;

        case 429:
          ElMessage.error("请求过于频繁，请稍后再试");
          break;

        case 500:
          ElMessage.error("服务器内部错误");
          break;

        case 502:
        case 503:
        case 504:
          ElMessage.error("服务暂时不可用，请稍后再试");
          break;

        default:
          ElMessage.error(data?.message || `请求失败 (${status})`);
      }
    } else if (error.code === "ECONNABORTED") {
      ElMessage.error("请求超时，请检查网络连接");
    } else if (error.message === "Network Error") {
      ElMessage.error("网络连接失败，请检查网络");
    } else {
      ElMessage.error("请求失败，请稍后再试");
    }

    return Promise.reject(error);
  },
);

// 请求方法封装
export const request = {
  get<T = unknown>(
    url: string,
    config?: Partial<InternalAxiosRequestConfig>,
  ): Promise<T> {
    return service.get(url, config);
  },

  post<T = unknown>(
    url: string,
    data?: unknown,
    config?: Partial<InternalAxiosRequestConfig>,
  ): Promise<T> {
    return service.post(url, data, config);
  },

  put<T = unknown>(
    url: string,
    data?: unknown,
    config?: Partial<InternalAxiosRequestConfig>,
  ): Promise<T> {
    return service.put(url, data, config);
  },

  delete<T = unknown>(
    url: string,
    config?: Partial<InternalAxiosRequestConfig>,
  ): Promise<T> {
    return service.delete(url, config);
  },

  patch<T = unknown>(
    url: string,
    data?: unknown,
    config?: Partial<InternalAxiosRequestConfig>,
  ): Promise<T> {
    return service.patch(url, data, config);
  },
};

// 文件上传
export const uploadFile = (
  url: string,
  file: File,
  onProgress?: (progress: number) => void,
) => {
  const formData = new FormData();
  formData.append("file", file);

  return service.post(url, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const progress = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total,
        );
        onProgress(progress);
      }
    },
  });
};

// 文件下载
export const downloadFile = async (url: string, filename?: string) => {
  try {
    const response = await service.get(url, {
      responseType: "blob",
    });

    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = filename || "download";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  } catch (error) {
    ElMessage.error("文件下载失败");
    throw error;
  }
};

export default service;
