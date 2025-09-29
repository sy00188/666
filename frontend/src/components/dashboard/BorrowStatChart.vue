<template>
  <div class="borrow-stat-chart">
    <div class="chart-header">
      <h3 class="chart-title">借阅统计</h3>
      <div class="chart-controls">
        <el-select
          v-model="selectedType"
          placeholder="选择统计类型"
          size="small"
          style="width: 120px"
          @change="handleTypeChange"
        >
          <el-option label="借阅趋势" value="trend" />
          <el-option label="状态分布" value="status" />
          <el-option label="部门统计" value="department" />
        </el-select>
        <el-button
          size="small"
          :icon="RefreshRight"
          :loading="loading"
          @click="refreshChart"
        >
          刷新
        </el-button>
      </div>
    </div>

    <div class="chart-container">
      <div
        v-if="loading"
        class="chart-loading"
        v-loading="loading"
        element-loading-text="加载中..."
      >
        <div class="loading-placeholder"></div>
      </div>

      <div v-else-if="error" class="chart-error">
        <el-empty :image-size="100" description="数据加载失败">
          <el-button type="primary" @click="refreshChart"> 重新加载 </el-button>
        </el-empty>
      </div>

      <div v-else ref="chartRef" class="chart-content"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from "vue";
import { RefreshRight } from "@element-plus/icons-vue";
import * as echarts from "echarts";
import type { ECharts } from "echarts";
import { useDashboard } from "@/composables/useDashboard";
import type { BorrowTrendData } from "@/api/statistics";

// Props
interface Props {
  data?: BorrowTrendData;
  height?: string;
}

const props = withDefaults(defineProps<Props>(), {
  height: "300px",
});

// Emits
const emit = defineEmits<{
  refresh: [];
  typeChange: [type: string];
}>();

// 响应式数据
const chartRef = ref<HTMLElement>();
const chartInstance = ref<ECharts>();
const selectedType = ref("trend");

// 使用仪表板数据管理
const {
  loading: loadingState,
  error: errorState,
  loadBorrowTrend,
} = useDashboard();

// 计算当前组件的加载状态
const loading = computed(() => loadingState.borrowTrend);
const error = computed(() => errorState.borrowTrend);

// 获取趋势图表配置
const getTrendChartOption = (data: BorrowTrendData) => {
  const dates = data.trends.map((item) => item.date);
  const borrowCounts = data.trends.map((item) => item.borrowCount);
  const returnCounts = data.trends.map((item) => item.returnCount);

  return {
    title: {
      text: `总借阅: ${data.totalBorrow} | 总归还: ${data.totalReturn}`,
      textStyle: {
        fontSize: 14,
        fontWeight: "normal",
        color: "#666",
      },
      left: "center",
      top: 10,
    },
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "cross",
      },
    },
    legend: {
      data: ["借阅数量", "归还数量"],
      top: 35,
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "20%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: dates,
      axisLabel: {
        fontSize: 12,
      },
    },
    yAxis: {
      type: "value",
      axisLabel: {
        fontSize: 12,
      },
    },
    series: [
      {
        name: "借阅数量",
        type: "line",
        data: borrowCounts,
        smooth: true,
        itemStyle: {
          color: "#409EFF",
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(64, 158, 255, 0.3)" },
            { offset: 1, color: "rgba(64, 158, 255, 0.1)" },
          ]),
        },
      },
      {
        name: "归还数量",
        type: "line",
        data: returnCounts,
        smooth: true,
        itemStyle: {
          color: "#67C23A",
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(103, 194, 58, 0.3)" },
            { offset: 1, color: "rgba(103, 194, 58, 0.1)" },
          ]),
        },
      },
    ],
  };
};

// 获取状态分布图表配置
const getStatusChartOption = (data: BorrowTrendData) => {
  const statusData = [
    { name: "已借出", value: data.totalBorrow - data.totalReturn },
    { name: "已归还", value: data.totalReturn },
    { name: "逾期", value: Math.floor(data.totalBorrow * 0.1) }, // 模拟逾期数据
  ];

  return {
    title: {
      text: "借阅状态分布",
      textStyle: {
        fontSize: 14,
        fontWeight: "normal",
        color: "#666",
      },
      left: "center",
      top: 10,
    },
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} ({d}%)",
    },
    legend: {
      orient: "vertical",
      left: "left",
      top: "middle",
    },
    series: [
      {
        name: "借阅状态",
        type: "pie",
        radius: ["40%", "70%"],
        center: ["60%", "50%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: statusData.map((item, index) => ({
          ...item,
          itemStyle: {
            color: ["#409EFF", "#67C23A", "#F56C6C"][index],
          },
        })),
      },
    ],
  };
};

// 获取部门统计图表配置
const getDepartmentChartOption = (data: BorrowTrendData) => {
  // 模拟部门数据
  const departments = ["技术部", "市场部", "人事部", "财务部", "行政部"];
  const departmentData = departments.map((dept) => ({
    name: dept,
    value: Math.floor(Math.random() * 50) + 10,
  }));

  return {
    title: {
      text: "部门借阅统计",
      textStyle: {
        fontSize: 14,
        fontWeight: "normal",
        color: "#666",
      },
      left: "center",
      top: 10,
    },
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "15%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: departmentData.map((item) => item.name),
      axisLabel: {
        fontSize: 12,
      },
    },
    yAxis: {
      type: "value",
      axisLabel: {
        fontSize: 12,
      },
    },
    series: [
      {
        name: "借阅数量",
        type: "bar",
        data: departmentData.map((item) => item.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#E6A23C" },
            { offset: 1, color: "#f0c78a" },
          ]),
        },
      },
    ],
  };
};

// 获取图表配置
const getChartOption = (data: BorrowTrendData) => {
  switch (selectedType.value) {
    case "trend":
      return getTrendChartOption(data);
    case "status":
      return getStatusChartOption(data);
    case "department":
      return getDepartmentChartOption(data);
    default:
      return getTrendChartOption(data);
  }
};

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) return;

  try {
    // 销毁已存在的图表实例
    if (chartInstance.value) {
      chartInstance.value.dispose();
    }

    // 创建新的图表实例
    chartInstance.value = echarts.init(chartRef.value);

    // 设置图表容器高度
    chartRef.value.style.height = props.height;

    // 如果有数据，直接渲染
    if (props.data) {
      updateChart(props.data);
    } else {
      // 否则加载数据
      await loadChartData();
    }

    // 监听窗口大小变化
    window.addEventListener("resize", handleResize);
  } catch (err) {
    console.error("初始化图表失败:", err);
  }
};

// 更新图表
const updateChart = (data: BorrowTrendData) => {
  if (!chartInstance.value || !data) return;

  try {
    const option = getChartOption(data);
    chartInstance.value.setOption(option, true);
  } catch (err) {
    console.error("更新图表失败:", err);
  }
};

// 加载图表数据
const loadChartData = async () => {
  try {
    const data = await loadBorrowTrend();
    if (data) {
      updateChart(data);
    }
  } catch (err) {
    console.error("加载图表数据失败:", err);
  }
};

// 处理窗口大小变化
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize();
  }
};

// 处理统计类型变化
const handleTypeChange = (type: string) => {
  selectedType.value = type;
  if (props.data) {
    updateChart(props.data);
  } else {
    loadChartData();
  }
  emit("typeChange", type);
};

// 刷新图表
const refreshChart = () => {
  loadChartData();
  emit("refresh");
};

// 监听数据变化
watch(
  () => props.data,
  (newData) => {
    if (newData && chartInstance.value) {
      updateChart(newData);
    }
  },
  { deep: true },
);

// 生命周期
onMounted(async () => {
  await nextTick();
  initChart();
});

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }
  window.removeEventListener("resize", handleResize);
});

// 暴露方法给父组件
defineExpose({
  refreshChart,
  updateChart,
});
</script>

<style scoped>
.borrow-stat-chart {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chart-container {
  position: relative;
  min-height: 300px;
}

.chart-loading {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  border-radius: 4px;
}

.chart-error {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-content {
  width: 100%;
  height: 300px;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .chart-controls {
    justify-content: center;
  }
}
</style>
