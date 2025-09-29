import { ref, reactive, computed, watch } from "vue";
import type {
  ChartFilter,
  ChartInteraction,
  ChartLinkage,
  DrillDownConfig,
  DrillDownLevel,
  DrillDownData,
  LinkageEvent,
  LinkageRule,
  ChartDataPoint,
  InteractiveChart,
} from "@/types/chart";

export function useChartInteraction() {
  // 全局状态
  const charts = ref<Map<string, InteractiveChart>>(new Map());
  const globalFilters = ref<ChartFilter[]>([]);
  const linkageRules = ref<LinkageRule[]>([]);
  const activeChart = ref<string>("");
  const isLoading = ref(false);
  const error = ref<string>("");

  // 注册图表
  const registerChart = (chart: InteractiveChart) => {
    charts.value.set(chart.id, chart);
  };

  // 注销图表
  const unregisterChart = (chartId: string) => {
    charts.value.delete(chartId);
  };

  // 获取图表
  const getChart = (chartId: string) => {
    return charts.value.get(chartId);
  };

  // 数据钻取功能
  const drillDown = async (
    chartId: string,
    level: DrillDownLevel,
    value: any,
  ): Promise<DrillDownData | null> => {
    const chart = getChart(chartId);
    if (!chart || !chart.state.drillDown.enabled) return null;

    try {
      isLoading.value = true;

      // 模拟API调用获取钻取数据
      const drillDownData = await mockDrillDownAPI(chartId, level, value);

      // 更新图表状态
      chart.state.drillDown.currentLevel = level.level;
      chart.state.drillDown.breadcrumb.push({
        level: level.level,
        name: level.name,
        value,
      });

      // 更新图表数据
      if (drillDownData) {
        chart.series = [
          {
            name: level.name,
            data: drillDownData.data,
            type: chart.type === "pie" ? "pie" : "bar",
          },
        ];
      }

      return drillDownData;
    } catch (err) {
      error.value = `钻取失败: ${err}`;
      return null;
    } finally {
      isLoading.value = false;
    }
  };

  // 钻取返回上一级
  const drillUp = (chartId: string) => {
    const chart = getChart(chartId);
    if (!chart || chart.state.drillDown.currentLevel <= 0) return;

    chart.state.drillDown.breadcrumb.pop();
    chart.state.drillDown.currentLevel--;

    // 重新加载上一级数据
    loadChartData(chartId);
  };

  // 重置钻取
  const resetDrillDown = (chartId: string) => {
    const chart = getChart(chartId);
    if (!chart) return;

    chart.state.drillDown.currentLevel = 0;
    chart.state.drillDown.breadcrumb = [];

    // 重新加载原始数据
    loadChartData(chartId);
  };

  // 图表联动功能
  const triggerLinkage = (interaction: ChartInteraction) => {
    const sourceChart = getChart(interaction.chart);
    if (!sourceChart) return;

    // 查找相关的联动规则
    const applicableRules = linkageRules.value.filter(
      (rule) => rule.sourceChart === interaction.chart && rule.enabled,
    );

    applicableRules.forEach((rule) => {
      const filters: ChartFilter[] = [
        {
          field: rule.mapping.targetField,
          operator: "eq",
          value: interaction.data[rule.mapping.sourceField],
          label: `${rule.mapping.targetField}: ${interaction.data[rule.mapping.sourceField]}`,
        },
      ];

      // 应用过滤器到目标图表
      rule.targetCharts.forEach((targetChartId) => {
        applyFilters(targetChartId, filters);
      });

      // 触发联动事件
      const linkageEvent: LinkageEvent = {
        sourceChartId: interaction.chart,
        targetChartIds: rule.targetCharts,
        filters,
        timestamp: Date.now(),
      };

      emitLinkageEvent(linkageEvent);
    });
  };

  // 应用过滤器
  const applyFilters = (chartId: string, filters: ChartFilter[]) => {
    const chart = getChart(chartId);
    if (!chart) return;

    // 合并过滤器
    chart.state.filters = [...chart.state.filters, ...filters];

    // 重新加载数据
    loadChartData(chartId);
  };

  // 清除过滤器
  const clearFilters = (chartId: string) => {
    const chart = getChart(chartId);
    if (!chart) return;

    chart.state.filters = [];
    loadChartData(chartId);
  };

  // 添加联动规则
  const addLinkageRule = (rule: LinkageRule) => {
    linkageRules.value.push(rule);
  };

  // 移除联动规则
  const removeLinkageRule = (ruleId: string) => {
    const index = linkageRules.value.findIndex((rule) => rule.id === ruleId);
    if (index > -1) {
      linkageRules.value.splice(index, 1);
    }
  };

  // 启用/禁用联动规则
  const toggleLinkageRule = (ruleId: string, enabled: boolean) => {
    const rule = linkageRules.value.find((rule) => rule.id === ruleId);
    if (rule) {
      rule.enabled = enabled;
    }
  };

  // 加载图表数据
  const loadChartData = async (chartId: string) => {
    const chart = getChart(chartId);
    if (!chart) return;

    try {
      chart.state.isLoading = true;
      chart.state.error = undefined;

      // 构建查询参数
      const queryParams = {
        chartId,
        filters: chart.state.filters,
        drillDown: chart.state.drillDown,
      };

      // 模拟API调用
      const data = await mockChartDataAPI(queryParams);

      // 更新图表数据
      chart.series = data.series;
    } catch (err) {
      chart.state.error = `加载数据失败: ${err}`;
    } finally {
      chart.state.isLoading = false;
    }
  };

  // 处理图表交互事件
  const handleChartInteraction = (interaction: ChartInteraction) => {
    activeChart.value = interaction.chart;

    // 触发联动
    triggerLinkage(interaction);

    // 调用图表自定义交互处理器
    const chart = getChart(interaction.chart);
    if (chart?.onInteraction) {
      chart.onInteraction(interaction);
    }
  };

  // 模拟API函数
  const mockDrillDownAPI = async (
    chartId: string,
    level: DrillDownLevel,
    value: any,
  ): Promise<DrillDownData> => {
    // 模拟网络延迟
    await new Promise((resolve) => setTimeout(resolve, 500));

    // 根据不同的钻取级别返回不同的数据
    const mockData: ChartDataPoint[] = [];

    if (level.field === "category") {
      // 按类别钻取
      for (let i = 1; i <= 5; i++) {
        mockData.push({
          name: `${value}-子项${i}`,
          value: Math.floor(Math.random() * 100) + 10,
          category: value,
          subcategory: `${value}-子项${i}`,
        });
      }
    } else if (level.field === "time") {
      // 按时间钻取
      const months = ["1月", "2月", "3月", "4月", "5月", "6月"];
      months.forEach((month) => {
        mockData.push({
          name: month,
          value: Math.floor(Math.random() * 200) + 50,
          date: `2024-${month}`,
          year: value,
        });
      });
    }

    return {
      level: level.level,
      field: level.field,
      value,
      label: `${level.name}: ${value}`,
      data: mockData,
      hasChildren: level.level < 2,
    };
  };

  const mockChartDataAPI = async (queryParams: any) => {
    // 模拟网络延迟
    await new Promise((resolve) => setTimeout(resolve, 300));

    // 根据过滤器和钻取状态生成模拟数据
    const mockSeries = [
      {
        name: "数据系列",
        data: Array.from({ length: 6 }, (_, i) => ({
          name: `项目${i + 1}`,
          value: Math.floor(Math.random() * 100) + 20,
        })),
        type: "bar" as const,
      },
    ];

    return { series: mockSeries };
  };

  // 发送联动事件
  const emitLinkageEvent = (event: LinkageEvent) => {
    // 这里可以发送到事件总线或其他监听器
    console.log("Linkage event:", event);
  };

  // 计算属性
  const chartCount = computed(() => charts.value.size);
  const activeChartData = computed(() => {
    return activeChart.value ? getChart(activeChart.value) : null;
  });
  const hasGlobalFilters = computed(() => globalFilters.value.length > 0);

  // 监听全局过滤器变化
  watch(
    globalFilters,
    (newFilters) => {
      // 将全局过滤器应用到所有图表
      charts.value.forEach((chart, chartId) => {
        chart.state.filters = [
          ...newFilters,
          ...chart.state.filters.filter((f) => !f.label?.startsWith("全局:")),
        ];
        loadChartData(chartId);
      });
    },
    { deep: true },
  );

  return {
    // 状态
    charts: computed(() => Array.from(charts.value.values())),
    globalFilters,
    linkageRules,
    activeChart,
    isLoading,
    error,
    chartCount,
    activeChartData,
    hasGlobalFilters,

    // 图表管理
    registerChart,
    unregisterChart,
    getChart,
    loadChartData,

    // 数据钻取
    drillDown,
    drillUp,
    resetDrillDown,

    // 图表联动
    triggerLinkage,
    applyFilters,
    clearFilters,
    addLinkageRule,
    removeLinkageRule,
    toggleLinkageRule,

    // 事件处理
    handleChartInteraction,
  };
}
