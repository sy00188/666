import { createPinia } from "pinia";
import { createPersistedState } from "pinia-plugin-persistedstate";

// 创建pinia实例
const pinia = createPinia();

// 配置持久化插件
pinia.use(
  createPersistedState({
    storage: localStorage,
    auto: true,
  }),
);

// 导出所有store
export { useAuthStore } from "./auth";
export { useUserStore } from "./user";
export { useAppStore } from "./app";

export default pinia;
