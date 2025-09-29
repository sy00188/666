import { defineStore } from "pinia";
import { ref, computed } from "vue";

// 主题类型
export type ThemeType = "light" | "dark" | "auto";

// 语言类型
export type LanguageType = "zh-CN" | "en-US";

// 侧边栏状态
export interface SidebarState {
  opened: boolean;
  withoutAnimation: boolean;
}

// 设备类型
export type DeviceType = "desktop" | "tablet" | "mobile";

// 面包屑项
export interface BreadcrumbItem {
  title: string;
  path?: string;
  icon?: string;
}

export const useAppStore = defineStore(
  "app",
  () => {
    // 状态
    const sidebar = ref<SidebarState>({
      opened: true,
      withoutAnimation: false,
    });

    const device = ref<DeviceType>("desktop");
    const theme = ref<ThemeType>("light");
    const language = ref<LanguageType>("zh-CN");
    const loading = ref(false);
    const breadcrumbs = ref<BreadcrumbItem[]>([]);

    // 页面设置
    const pageTitle = ref("");
    const showBreadcrumb = ref(true);
    const showTabs = ref(true);
    const fixedHeader = ref(true);
    const showLogo = ref(true);

    // 计算属性
    const isMobile = computed(() => device.value === "mobile");
    const isTablet = computed(() => device.value === "tablet");
    const isDesktop = computed(() => device.value === "desktop");
    const isDark = computed(() => theme.value === "dark");
    const sidebarOpened = computed(() => sidebar.value.opened);

    // 切换侧边栏
    const toggleSidebar = (withoutAnimation = false) => {
      sidebar.value.opened = !sidebar.value.opened;
      sidebar.value.withoutAnimation = withoutAnimation;
    };

    // 关闭侧边栏
    const closeSidebar = (withoutAnimation = false) => {
      sidebar.value.opened = false;
      sidebar.value.withoutAnimation = withoutAnimation;
    };

    // 打开侧边栏
    const openSidebar = (withoutAnimation = false) => {
      sidebar.value.opened = true;
      sidebar.value.withoutAnimation = withoutAnimation;
    };

    // 设置设备类型
    const setDevice = (deviceType: DeviceType) => {
      device.value = deviceType;

      // 移动端自动关闭侧边栏
      if (deviceType === "mobile") {
        closeSidebar(true);
      }
    };

    // 切换主题
    const toggleTheme = () => {
      theme.value = theme.value === "light" ? "dark" : "light";
      applyTheme();
    };

    // 设置主题
    const setTheme = (themeType: ThemeType) => {
      theme.value = themeType;
      applyTheme();
    };

    // 应用主题
    const applyTheme = () => {
      const html = document.documentElement;

      if (theme.value === "dark") {
        html.classList.add("dark");
      } else if (theme.value === "light") {
        html.classList.remove("dark");
      } else {
        // auto 模式，根据系统主题
        const prefersDark = window.matchMedia(
          "(prefers-color-scheme: dark)",
        ).matches;
        if (prefersDark) {
          html.classList.add("dark");
        } else {
          html.classList.remove("dark");
        }
      }
    };

    // 设置语言
    const setLanguage = (lang: LanguageType) => {
      language.value = lang;
      // 这里可以集成 i18n
      document.documentElement.lang = lang;
    };

    // 设置加载状态
    const setLoading = (isLoading: boolean) => {
      loading.value = isLoading;
    };

    // 设置页面标题
    const setPageTitle = (title: string) => {
      pageTitle.value = title;
      document.title = title ? `${title} - 档案管理系统` : "档案管理系统";
    };

    // 设置面包屑
    const setBreadcrumbs = (items: BreadcrumbItem[]) => {
      breadcrumbs.value = items;
    };

    // 添加面包屑项
    const addBreadcrumb = (item: BreadcrumbItem) => {
      breadcrumbs.value.push(item);
    };

    // 清空面包屑
    const clearBreadcrumbs = () => {
      breadcrumbs.value = [];
    };

    // 设置页面配置
    const setPageConfig = (config: {
      showBreadcrumb?: boolean;
      showTabs?: boolean;
      fixedHeader?: boolean;
      showLogo?: boolean;
    }) => {
      if (config.showBreadcrumb !== undefined) {
        showBreadcrumb.value = config.showBreadcrumb;
      }
      if (config.showTabs !== undefined) {
        showTabs.value = config.showTabs;
      }
      if (config.fixedHeader !== undefined) {
        fixedHeader.value = config.fixedHeader;
      }
      if (config.showLogo !== undefined) {
        showLogo.value = config.showLogo;
      }
    };

    // 初始化应用
    const initApp = () => {
      // 检测设备类型
      const checkDevice = () => {
        const width = window.innerWidth;
        if (width < 768) {
          setDevice("mobile");
        } else if (width < 1024) {
          setDevice("tablet");
        } else {
          setDevice("desktop");
        }
      };

      // 监听窗口大小变化
      window.addEventListener("resize", checkDevice);
      checkDevice();

      // 应用主题
      applyTheme();

      // 监听系统主题变化
      if (theme.value === "auto") {
        const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
        mediaQuery.addEventListener("change", applyTheme);
      }
    };

    // 重置应用状态
    const resetApp = () => {
      sidebar.value = {
        opened: true,
        withoutAnimation: false,
      };
      device.value = "desktop";
      theme.value = "light";
      language.value = "zh-CN";
      loading.value = false;
      breadcrumbs.value = [];
      pageTitle.value = "";
      showBreadcrumb.value = true;
      showTabs.value = true;
      fixedHeader.value = true;
      showLogo.value = true;
    };

    return {
      // 状态
      sidebar,
      device,
      theme,
      language,
      loading,
      breadcrumbs,
      pageTitle,
      showBreadcrumb,
      showTabs,
      fixedHeader,
      showLogo,

      // 计算属性
      isMobile,
      isTablet,
      isDesktop,
      isDark,
      sidebarOpened,

      // 方法
      toggleSidebar,
      closeSidebar,
      openSidebar,
      setDevice,
      toggleTheme,
      setTheme,
      applyTheme,
      setLanguage,
      setLoading,
      setPageTitle,
      setBreadcrumbs,
      addBreadcrumb,
      clearBreadcrumbs,
      setPageConfig,
      initApp,
      resetApp,
    };
  },
  {
    persist: {
      key: "app-store",
      storage: localStorage,
      paths: [
        "sidebar",
        "theme",
        "language",
        "showBreadcrumb",
        "showTabs",
        "fixedHeader",
        "showLogo",
      ],
    },
  },
);
