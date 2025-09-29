import { ref, reactive, computed, onMounted, onUnmounted, watch } from "vue";
import { StatisticsAPI, type DashboardOverviewData } from "@/api/statistics";
import { ElMessage } from "element-plus";

// WebSocket 连接状态类型
type WebSocketStatus = "connecting" | "connected" | "disconnected" | "error";

// 实时数据更新配置
interface RealTimeConfig {
  enabled: boolean;
  interval: number; // 轮询间隔（毫秒）
  useWebSocket: boolean;
  websocketUrl?: string;
  autoReconnect: boolean;
  maxReconnectAttempts: number;
}

// WebSocket 消息类型
interface WebSocketMessage {
  type:
    | "dashboard_update"
    | "archive_update"
    | "borrow_update"
    | "user_activity_update";
  data: any;
  timestamp: number;
}

// 数据加载状态类型
interface LoadingState {
  overview: boolean;
  archiveCount: boolean;
  borrowTrend: boolean;
  userActivity: boolean;
}

// 错误状态类型
interface ErrorState {
  overview: string | null;
  archiveCount: string | null;
  borrowTrend: string | null;
  userActivity: string | null;
  websocket: string | null;
}

/**
 * 仪表板数据管理 Composable
 */
export function useDashboard(config?: Partial<RealTimeConfig>) {
  // 默认实时配置
  const defaultConfig: RealTimeConfig = {
    enabled: false,
    interval: 30000, // 30秒
    useWebSocket: false,
    websocketUrl: undefined,
    autoReconnect: true,
    maxReconnectAttempts: 5,
  };

  const realTimeConfig = reactive<RealTimeConfig>({
    ...defaultConfig,
    ...config,
  });

  // WebSocket 相关状态
  const websocket = ref<WebSocket | null>(null);
  const websocketStatus = ref<WebSocketStatus>("disconnected");
  const reconnectAttempts = ref(0);
  const reconnectTimer = ref<NodeJS.Timeout | null>(null);
  const pollingTimer = ref<NodeJS.Timeout | null>(null);

  // 数据状态
  const dashboardData = ref<DashboardOverviewData | null>(null);
  const lastUpdateTime = ref<Date | null>(null);

  // 加载状态
  const loading = reactive<LoadingState>({
    overview: false,
    archiveCount: false,
    borrowTrend: false,
    userActivity: false,
  });

  // 错误状态
  const error = reactive<ErrorState>({
    overview: null,
    archiveCount: null,
    borrowTrend: null,
    userActivity: null,
    websocket: null,
  });

  // 计算属性：是否有任何加载中的状态
  const isLoading = computed(() => {
    return Object.values(loading).some((state) => state);
  });

  // 计算属性：是否有任何错误
  const hasError = computed(() => {
    return Object.values(error).some((err) => err !== null);
  });

  // 计算属性：获取所有错误信息
  const errorMessages = computed(() => {
    return Object.values(error).filter((err) => err !== null);
  });

  /**
   * 清除所有错误状态
   */
  const clearErrors = () => {
    Object.keys(error).forEach((key) => {
      error[key as keyof ErrorState] = null;
    });
  };

  /**
   * 设置加载状态
   */
  const setLoading = (key: keyof LoadingState, value: boolean) => {
    loading[key] = value;
  };

  /**
   * 设置错误状态
   */
  const setError = (key: keyof ErrorState, message: string | null) => {
    error[key] = message;
  };

  /**
   * 初始化 WebSocket 连接
   */
  const initWebSocket = () => {
    if (!realTimeConfig.useWebSocket || !realTimeConfig.websocketUrl) {
      return;
    }

    try {
      websocketStatus.value = "connecting";
      setError("websocket", null);

      websocket.value = new WebSocket(realTimeConfig.websocketUrl);

      websocket.value.onopen = () => {
        websocketStatus.value = "connected";
        reconnectAttempts.value = 0;
        console.log("WebSocket 连接已建立");
      };

      websocket.value.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          handleWebSocketMessage(message);
        } catch (err) {
          console.error("解析 WebSocket 消息失败:", err);
        }
      };

      websocket.value.onclose = (event) => {
        websocketStatus.value = "disconnected";
        console.log("WebSocket 连接已关闭:", event.code, event.reason);

        if (
          realTimeConfig.autoReconnect &&
          reconnectAttempts.value < realTimeConfig.maxReconnectAttempts
        ) {
          scheduleReconnect();
        }
      };

      websocket.value.onerror = (event) => {
        websocketStatus.value = "error";
        setError("websocket", "WebSocket 连接错误");
        console.error("WebSocket 错误:", event);
      };
    } catch (err: any) {
      websocketStatus.value = "error";
      setError("websocket", err.message || "WebSocket 初始化失败");
    }
  };

  /**
   * 处理 WebSocket 消息
   */
  const handleWebSocketMessage = (message: WebSocketMessage) => {
    lastUpdateTime.value = new Date(message.timestamp);

    switch (message.type) {
      case "dashboard_update":
        if (dashboardData.value) {
          dashboardData.value = { ...dashboardData.value, ...message.data };
        }
        break;
      case "archive_update":
        if (dashboardData.value) {
          dashboardData.value.archiveCount = message.data;
        }
        break;
      case "borrow_update":
        if (dashboardData.value) {
          dashboardData.value.borrowTrend = message.data;
        }
        break;
      case "user_activity_update":
        if (dashboardData.value) {
          dashboardData.value.userActivity = message.data;
        }
        break;
      default:
        console.warn("未知的 WebSocket 消息类型:", message.type);
    }
  };

  /**
   * 计划重连
   */
  const scheduleReconnect = () => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value);
    }

    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts.value), 30000); // 指数退避，最大30秒
    reconnectAttempts.value++;

    reconnectTimer.value = setTimeout(() => {
      console.log(`尝试重连 WebSocket (第 ${reconnectAttempts.value} 次)`);
      initWebSocket();
    }, delay);
  };

  /**
   * 关闭 WebSocket 连接
   */
  const closeWebSocket = () => {
    if (websocket.value) {
      websocket.value.close();
      websocket.value = null;
    }
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value);
      reconnectTimer.value = null;
    }
    websocketStatus.value = "disconnected";
  };

  /**
   * 启动轮询
   */
  const startPolling = () => {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value);
    }

    pollingTimer.value = setInterval(async () => {
      try {
        await loadDashboardOverview();
      } catch (err) {
        console.error("轮询更新失败:", err);
      }
    }, realTimeConfig.interval);
  };

  /**
   * 停止轮询
   */
  const stopPolling = () => {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value);
      pollingTimer.value = null;
    }
  };

  /**
   * 启用实时更新
   */
  const enableRealTime = () => {
    realTimeConfig.enabled = true;

    if (realTimeConfig.useWebSocket) {
      initWebSocket();
    } else {
      startPolling();
    }
  };

  /**
   * 禁用实时更新
   */
  const disableRealTime = () => {
    realTimeConfig.enabled = false;
    closeWebSocket();
    stopPolling();
  };

  /**
   * 更新实时配置
   */
  const updateRealTimeConfig = (newConfig: Partial<RealTimeConfig>) => {
    const wasEnabled = realTimeConfig.enabled;
    const wasUsingWebSocket = realTimeConfig.useWebSocket;

    Object.assign(realTimeConfig, newConfig);

    // 如果配置发生变化，重新启动实时更新
    if (wasEnabled) {
      if (wasUsingWebSocket && !realTimeConfig.useWebSocket) {
        closeWebSocket();
        if (realTimeConfig.enabled) {
          startPolling();
        }
      } else if (!wasUsingWebSocket && realTimeConfig.useWebSocket) {
        stopPolling();
        if (realTimeConfig.enabled) {
          initWebSocket();
        }
      } else if (realTimeConfig.useWebSocket) {
        closeWebSocket();
        initWebSocket();
      } else {
        stopPolling();
        startPolling();
      }
    }
  };

  /**
   * 加载仪表板概览数据
   */
  const loadDashboardOverview = async () => {
    try {
      setLoading("overview", true);
      setError("overview", null);

      const data = await StatisticsAPI.getDashboardOverview();
      dashboardData.value = data;

      return data;
    } catch (err: any) {
      const errorMessage = err?.message || "加载仪表板数据失败";
      setError("overview", errorMessage);
      ElMessage.error(errorMessage);
      throw err;
    } finally {
      setLoading("overview", false);
    }
  };

  /**
   * 加载档案统计数据
   */
  const loadArchiveCount = async (params?: {
    startTime?: string;
    endTime?: string;
    groupBy?: string;
  }) => {
    try {
      setLoading("archiveCount", true);
      setError("archiveCount", null);

      const data = await StatisticsAPI.getArchiveCount(params);

      // 更新仪表板数据中的档案统计部分
      if (dashboardData.value) {
        dashboardData.value.archiveCount = data;
      }

      return data;
    } catch (err: any) {
      const errorMessage = err?.message || "加载档案统计数据失败";
      setError("archiveCount", errorMessage);
      ElMessage.error(errorMessage);
      throw err;
    } finally {
      setLoading("archiveCount", false);
    }
  };

  /**
   * 加载借阅趋势数据
   */
  const loadBorrowTrend = async (params?: {
    startTime?: string;
    endTime?: string;
  }) => {
    try {
      setLoading("borrowTrend", true);
      setError("borrowTrend", null);

      const data = await StatisticsAPI.getBorrowTrend(params);

      // 更新仪表板数据中的借阅趋势部分
      if (dashboardData.value) {
        dashboardData.value.borrowTrend = data;
      }

      return data;
    } catch (err: any) {
      const errorMessage = err?.message || "加载借阅趋势数据失败";
      setError("borrowTrend", errorMessage);
      ElMessage.error(errorMessage);
      throw err;
    } finally {
      setLoading("borrowTrend", false);
    }
  };

  /**
   * 加载用户活跃度数据
   */
  const loadUserActivity = async (params?: {
    startTime?: string;
    endTime?: string;
  }) => {
    try {
      setLoading("userActivity", true);
      setError("userActivity", null);

      const data = await StatisticsAPI.getUserActivity(params);

      // 更新仪表板数据中的用户活跃度部分
      if (dashboardData.value) {
        dashboardData.value.userActivity = data;
      }

      return data;
    } catch (err: any) {
      const errorMessage = err?.message || "加载用户活跃度数据失败";
      setError("userActivity", errorMessage);
      ElMessage.error(errorMessage);
      throw err;
    } finally {
      setLoading("userActivity", false);
    }
  };

  /**
   * 刷新所有数据
   */
  const refreshAll = async () => {
    await Promise.all([
      loadDashboardOverview(),
      loadArchiveCount(),
      loadBorrowTrend(),
      loadUserActivity(),
    ]);
  };

  /**
   * 重试失败的请求
   */
  const retryFailedRequests = async () => {
    const promises: Promise<any>[] = [];

    if (error.overview) promises.push(loadDashboardOverview());
    if (error.archiveCount) promises.push(loadArchiveCount());
    if (error.borrowTrend) promises.push(loadBorrowTrend());
    if (error.userActivity) promises.push(loadUserActivity());

    await Promise.all(promises);
  };

  // 生命周期管理
  onMounted(() => {
    if (realTimeConfig.enabled) {
      enableRealTime();
    }
  });

  onUnmounted(() => {
    disableRealTime();
  });

  // 监听配置变化
  watch(
    () => realTimeConfig.enabled,
    (enabled) => {
      if (enabled) {
        enableRealTime();
      } else {
        disableRealTime();
      }
    },
  );

  // 监听数据变化，更新最后更新时间
  watch(
    dashboardData,
    () => {
      if (dashboardData.value) {
        lastUpdateTime.value = new Date();
      }
    },
    { deep: true },
  );

  return {
    // 数据
    dashboardData,
    lastUpdateTime,

    // 状态
    loading,
    error,
    isLoading,
    hasError,
    errorMessages,

    // WebSocket 状态
    websocketStatus,
    reconnectAttempts,
    realTimeConfig,

    // 计算属性
    isConnected: computed(() => websocketStatus.value === "connected"),
    isConnecting: computed(() => websocketStatus.value === "connecting"),
    hasWebSocketError: computed(() => websocketStatus.value === "error"),
    dataAge: computed(() => {
      if (!lastUpdateTime.value) return null;
      return Date.now() - lastUpdateTime.value.getTime();
    }),

    // 方法
    clearErrors,
    setLoading,
    setError,
    loadDashboardOverview,
    loadArchiveCount,
    loadBorrowTrend,
    loadUserActivity,
    refreshAll,
    retryFailedRequests,

    // 实时数据方法
    enableRealTime,
    disableRealTime,
    updateRealTimeConfig,
    initWebSocket,
    closeWebSocket,
    startPolling,
    stopPolling,
  };
}
