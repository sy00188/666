/**
 * 报表管理 Composable
 */

import { ref, reactive, computed, watch, onMounted, onUnmounted } from "vue";
import { ElMessage, ElNotification } from "element-plus";
import type {
  ReportConfig,
  ReportInstance,
  ReportTemplate,
  ReportStatistics,
  ReportListQuery,
  GenerateReportRequest,
  GenerateReportResponse,
  ReportType,
  ReportFormat,
  ExportOptions,
} from "@/types/report";
import { ReportStatus } from "@/types/report";

// 模拟 API 调用（实际项目中应该替换为真实的 API 调用）
const mockApi = {
  async generateReport(
    request: GenerateReportRequest,
  ): Promise<GenerateReportResponse> {
    // 模拟异步操作
    await new Promise((resolve) => setTimeout(resolve, 1000));
    return {
      reportId: `report_${Date.now()}`,
      status: "pending" as ReportStatus,
      estimatedTime: 30000,
      message: "报表生成任务已提交",
    };
  },

  async getReportList(
    query: ReportListQuery,
  ): Promise<{ reports: ReportInstance[]; total: number }> {
    await new Promise((resolve) => setTimeout(resolve, 500));
    // 模拟数据
    const mockReports: ReportInstance[] = [
      {
        id: "report_1",
        configId: "config_1",
        name: "档案统计报表",
        type: "archive_statistics" as ReportType,
        format: "pdf" as ReportFormat,
        status: "completed" as ReportStatus,
        progress: 100,
        createdAt: "2024-01-15T10:00:00Z",
        completedAt: "2024-01-15T10:02:30Z",
        fileUrl: "/api/reports/download/report_1.pdf",
        fileSize: 1024000,
        metadata: {
          recordCount: 1500,
          chartCount: 5,
          pageCount: 12,
          generationTime: 150000,
        },
      },
    ];
    return { reports: mockReports, total: mockReports.length };
  },

  async getReportById(id: string): Promise<ReportInstance> {
    await new Promise((resolve) => setTimeout(resolve, 300));
    return {
      id,
      configId: "config_1",
      name: "档案统计报表",
      type: "archive_statistics" as ReportType,
      format: "pdf" as ReportFormat,
      status: "completed" as ReportStatus,
      progress: 100,
      createdAt: "2024-01-15T10:00:00Z",
      completedAt: "2024-01-15T10:02:30Z",
      fileUrl: "/api/reports/download/report_1.pdf",
      fileSize: 1024000,
    };
  },

  async deleteReport(id: string): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve, 500));
  },

  async getReportTemplates(): Promise<ReportTemplate[]> {
    await new Promise((resolve) => setTimeout(resolve, 300));
    return [
      {
        id: "template_1",
        name: "标准档案统计模板",
        description: "包含档案数量、分类统计、趋势分析等标准内容",
        type: "archive_statistics" as ReportType,
        sections: [],
        isDefault: true,
        createdAt: "2024-01-01T00:00:00Z",
        updatedAt: "2024-01-01T00:00:00Z",
      },
    ];
  },

  async getReportStatistics(): Promise<ReportStatistics> {
    await new Promise((resolve) => setTimeout(resolve, 400));
    return {
      totalReports: 156,
      pendingReports: 3,
      completedReports: 145,
      failedReports: 8,
      totalFileSize: 52428800,
      averageGenerationTime: 45000,
      popularTypes: [
        { type: "archive_statistics" as ReportType, count: 65 },
        { type: "borrow_analysis" as ReportType, count: 42 },
        { type: "user_activity" as ReportType, count: 28 },
      ],
      recentActivity: [
        { date: "2024-01-15", count: 12 },
        { date: "2024-01-14", count: 8 },
        { date: "2024-01-13", count: 15 },
      ],
    };
  },
};

export function useReports() {
  // 响应式状态
  const reports = ref<ReportInstance[]>([]);
  const templates = ref<ReportTemplate[]>([]);
  const statistics = ref<ReportStatistics | null>(null);
  const currentReport = ref<ReportInstance | null>(null);

  // 加载状态
  const loading = reactive({
    reports: false,
    templates: false,
    statistics: false,
    generating: false,
    deleting: false,
  });

  // 错误状态
  const error = reactive({
    reports: null as string | null,
    templates: null as string | null,
    statistics: null as string | null,
    generating: null as string | null,
    deleting: null as string | null,
  });

  // 查询参数
  const query = reactive<ReportListQuery>({
    page: 1,
    pageSize: 20,
    sortBy: "createdAt",
    sortOrder: "desc",
  });

  // 分页信息
  const pagination = reactive({
    total: 0,
    currentPage: 1,
    pageSize: 20,
  });

  // 轮询状态
  const pollingTimer = ref<NodeJS.Timeout | null>(null);
  const pollingReports = ref<Set<string>>(new Set());

  // 计算属性
  const isLoading = computed(() => Object.values(loading).some(Boolean));
  const hasError = computed(() => Object.values(error).some(Boolean));
  const errorMessages = computed(() =>
    Object.entries(error)
      .filter(([, value]) => value)
      .map(([key, value]) => ({ key, message: value })),
  );

  const pendingReports = computed(() =>
    reports.value.filter(
      (report) => report.status === "pending" || report.status === "generating",
    ),
  );

  const completedReports = computed(() =>
    reports.value.filter((report) => report.status === "completed"),
  );

  const failedReports = computed(() =>
    reports.value.filter((report) => report.status === "failed"),
  );

  // 工具方法
  const clearErrors = () => {
    Object.keys(error).forEach((key) => {
      error[key as keyof typeof error] = null;
    });
  };

  const setLoading = (key: keyof typeof loading, value: boolean) => {
    loading[key] = value;
  };

  const setError = (key: keyof typeof error, message: string | null) => {
    error[key] = message;
  };

  /**
   * 加载报表列表
   */
  const loadReports = async (params?: Partial<ReportListQuery>) => {
    try {
      setLoading("reports", true);
      setError("reports", null);

      const queryParams = { ...query, ...params };
      const response = await mockApi.getReportList(queryParams);

      reports.value = response.reports;
      pagination.total = response.total;
      pagination.currentPage = queryParams.page || 1;

      // 启动对进行中报表的轮询
      startPollingForPendingReports();
    } catch (err: any) {
      setError("reports", err.message || "加载报表列表失败");
      ElMessage.error("加载报表列表失败");
    } finally {
      setLoading("reports", false);
    }
  };

  /**
   * 加载报表模板
   */
  const loadTemplates = async () => {
    try {
      setLoading("templates", true);
      setError("templates", null);

      templates.value = await mockApi.getReportTemplates();
    } catch (err: any) {
      setError("templates", err.message || "加载报表模板失败");
      ElMessage.error("加载报表模板失败");
    } finally {
      setLoading("templates", false);
    }
  };

  /**
   * 加载报表统计信息
   */
  const loadStatistics = async () => {
    try {
      setLoading("statistics", true);
      setError("statistics", null);

      statistics.value = await mockApi.getReportStatistics();
    } catch (err: any) {
      setError("statistics", err.message || "加载统计信息失败");
      ElMessage.error("加载统计信息失败");
    } finally {
      setLoading("statistics", false);
    }
  };

  /**
   * 生成报表
   */
  const generateReport = async (request: GenerateReportRequest) => {
    try {
      setLoading("generating", true);
      setError("generating", null);

      const response = await mockApi.generateReport(request);

      ElNotification({
        title: "报表生成",
        message: response.message || "报表生成任务已提交",
        type: "success",
        duration: 3000,
      });

      // 刷新报表列表
      await loadReports();

      return response;
    } catch (err: any) {
      setError("generating", err.message || "生成报表失败");
      ElMessage.error("生成报表失败");
      throw err;
    } finally {
      setLoading("generating", false);
    }
  };

  /**
   * 获取报表详情
   */
  const getReportById = async (id: string) => {
    try {
      const report = await mockApi.getReportById(id);
      currentReport.value = report;
      return report;
    } catch (err: any) {
      ElMessage.error("获取报表详情失败");
      throw err;
    }
  };

  /**
   * 删除报表
   */
  const deleteReport = async (id: string) => {
    try {
      setLoading("deleting", true);
      setError("deleting", null);

      await mockApi.deleteReport(id);

      // 从列表中移除
      reports.value = reports.value.filter((report) => report.id !== id);

      ElMessage.success("报表删除成功");
    } catch (err: any) {
      setError("deleting", err.message || "删除报表失败");
      ElMessage.error("删除报表失败");
      throw err;
    } finally {
      setLoading("deleting", false);
    }
  };

  /**
   * 下载报表
   */
  const downloadReport = (report: ReportInstance) => {
    if (!report.fileUrl) {
      ElMessage.error("报表文件不存在");
      return;
    }

    const link = document.createElement("a");
    link.href = report.fileUrl;
    link.download = `${report.name}.${report.format}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    ElMessage.success("报表下载开始");
  };

  /**
   * 启动对进行中报表的轮询
   */
  const startPollingForPendingReports = () => {
    // 清除现有轮询
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value);
    }

    // 找出需要轮询的报表
    const pendingIds = reports.value
      .filter(
        (report) =>
          report.status === "pending" || report.status === "generating",
      )
      .map((report) => report.id);

    if (pendingIds.length === 0) {
      return;
    }

    pollingReports.value = new Set(pendingIds);

    // 启动轮询
    pollingTimer.value = setInterval(async () => {
      try {
        for (const reportId of pollingReports.value) {
          const updatedReport = await mockApi.getReportById(reportId);

          // 更新报表状态
          const index = reports.value.findIndex((r) => r.id === reportId);
          if (index !== -1) {
            reports.value[index] = updatedReport;
          }

          // 如果报表完成或失败，从轮询列表中移除
          if (
            updatedReport.status === "completed" ||
            updatedReport.status === "failed"
          ) {
            pollingReports.value.delete(reportId);

            if (updatedReport.status === "completed") {
              ElNotification({
                title: "报表生成完成",
                message: `报表 "${updatedReport.name}" 已生成完成`,
                type: "success",
                duration: 5000,
              });
            } else {
              ElNotification({
                title: "报表生成失败",
                message: `报表 "${updatedReport.name}" 生成失败`,
                type: "error",
                duration: 5000,
              });
            }
          }
        }

        // 如果没有需要轮询的报表，停止轮询
        if (pollingReports.value.size === 0) {
          clearInterval(pollingTimer.value!);
          pollingTimer.value = null;
        }
      } catch (err) {
        console.error("轮询报表状态失败:", err);
      }
    }, 5000); // 每5秒轮询一次
  };

  /**
   * 停止轮询
   */
  const stopPolling = () => {
    if (pollingTimer.value) {
      clearInterval(pollingTimer.value);
      pollingTimer.value = null;
    }
    pollingReports.value.clear();
  };

  /**
   * 更新查询参数
   */
  const updateQuery = (params: Partial<ReportListQuery>) => {
    Object.assign(query, params);
    loadReports();
  };

  /**
   * 重置查询参数
   */
  const resetQuery = () => {
    Object.assign(query, {
      page: 1,
      pageSize: 20,
      type: undefined,
      status: undefined,
      dateRange: undefined,
      search: undefined,
      sortBy: "createdAt",
      sortOrder: "desc",
    });
    loadReports();
  };

  /**
   * 刷新数据
   */
  const refresh = async () => {
    await Promise.all([loadReports(), loadTemplates(), loadStatistics()]);
  };

  // 生命周期管理
  onMounted(() => {
    refresh();
  });

  onUnmounted(() => {
    stopPolling();
  });

  // 监听查询参数变化
  watch(
    () => [query.page, query.pageSize],
    () => {
      loadReports();
    },
  );

  // 取消报表生成
  const cancelReport = async (reportId: string): Promise<void> => {
    try {
      setLoading("deleting", true);

      // 模拟API调用
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // 更新本地状态
      const reportIndex = reports.value.findIndex((r) => r.id === reportId);
      if (reportIndex !== -1) {
        reports.value[reportIndex].status = ReportStatus.CANCELLED;
      }

      ElMessage.success("报表生成已取消");
    } catch (err) {
      const message = err instanceof Error ? err.message : "取消报表生成失败";
      setError("reports", message);
      ElMessage.error(message);
      throw err;
    } finally {
      setLoading("deleting", false);
    }
  };

  // 重试报表生成
  const retryReport = async (reportId: string): Promise<void> => {
    try {
      setLoading("generating", true);

      // 模拟API调用
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // 更新本地状态
      const reportIndex = reports.value.findIndex((r) => r.id === reportId);
      if (reportIndex !== -1) {
        reports.value[reportIndex].status = ReportStatus.PENDING;
        reports.value[reportIndex].progress = 0;
        reports.value[reportIndex].error = undefined;
      }

      ElMessage.success("报表重新生成中");
    } catch (err) {
      const message = err instanceof Error ? err.message : "重试报表生成失败";
      setError("reports", message);
      ElMessage.error(message);
      throw err;
    } finally {
      setLoading("generating", false);
    }
  };

  return {
    // 数据
    reports,
    templates,
    statistics,
    currentReport,

    // 状态
    loading,
    error,
    query,
    pagination,

    // 计算属性
    isLoading,
    hasError,
    errorMessages,
    pendingReports,
    completedReports,
    failedReports,

    // 方法
    clearErrors,
    setLoading,
    setError,
    loadReports,
    loadTemplates,
    loadStatistics,
    generateReport,
    getReportById,
    deleteReport,
    downloadReport,
    cancelReport,
    retryReport,
    updateQuery,
    resetQuery,
    refresh,
    startPollingForPendingReports,
    stopPolling,
  };
}
