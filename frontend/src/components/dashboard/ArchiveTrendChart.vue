<template>
  <div class="archive-trend-chart">
    <!-- 图表控制栏 -->
    <div class="chart-controls">
      <div class="period-selector">
        <el-radio-group
          v-model="selectedPeriod"
          size="small"
          @change="
            (val: string | number | boolean | undefined) =>
              handlePeriodChange(val as string)
          "
        >
          <el-radio-button label="7days">近7天</el-radio-button>
          <el-radio-button label="30days">近30天</el-radio-button>
          <el-radio-button label="3months">近3个月</el-radio-button>
          <el-radio-button label="1year">近1年</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart-actions">
        <el-button
          size="small"
          type="primary"
          :icon="Refresh"
          @click="refreshChart"
        >
          刷新
        </el-button>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="chart-container">
      <v-chart
        ref="chartRef"
        class="chart"
        :option="chartOption"
        :loading="loading"
        :loading-options="loadingOptions"
        :init-options="initOptions"
        :update-options="updateOptions"
        :autoresize="true"
        @click="handleChartClick"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from "vue";
import { ElRadioGroup, ElRadioButton, ElButton } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import VChart from "vue-echarts";
import { use } from "echarts/core";
import { CanvasRenderer, SVGRenderer } from "echarts/renderers";
import { BarChart, LineChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
} from "echarts/components";
import type { EChartsOption } from "echarts";
import { useDashboard } from "@/composables/useDashboard";

// 注册 ECharts 组件
use([
  CanvasRenderer,
  SVGRenderer,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
]);

// Props 定义
interface Props {
  data?: any;
  height?: string;
  title?: string;
}

const props = withDefaults(defineProps<Props>(), {
  height: "400px",
  title: "档案趋势统计",
});

// Emits 定义
interface Emits {
  (e: "periodChange", period: string): void;
  (e: "refresh"): void;
  (e: "chartClick", params: any): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const selectedPeriod = ref("30days");
const loading = ref(false);
const chartRef = ref();

// 使用 Dashboard composable
const { loadArchiveCount } = useDashboard();

// 图表配置
const chartOption = computed<EChartsOption>(() => ({
  title: {
    text: props.title,
    left: "center",
    textStyle: {
      fontSize: 16,
      fontWeight: "bold",
    },
  },
  tooltip: {
    trigger: "axis",
    axisPointer: {
      type: "shadow",
    },
    formatter: (params: any) => {
      if (Array.isArray(params) && params.length > 0) {
        const data = params[0];
        return `${data.name}<br/>${data.seriesName}: ${data.value}`;
      }
      return "";
    },
  },
  legend: {
    data: ["档案数量"],
    top: 30,
  },
  grid: {
    left: "3%",
    right: "4%",
    bottom: "3%",
    containLabel: true,
  },
  xAxis: {
    type: "category",
    data: props.data?.categories || [],
    axisLabel: {
      rotate: 45,
    },
  },
  yAxis: {
    type: "value",
    name: "数量",
  },
  series: [
    {
      name: "档案数量",
      type: "bar",
      data: props.data?.values || [],
      itemStyle: {
        color: "#409EFF",
      },
      emphasis: {
        itemStyle: {
          color: "#66b1ff",
        },
      },
    },
  ],
  dataZoom: [
    {
      type: "inside",
      start: 0,
      end: 100,
    },
    {
      type: "slider",
      start: 0,
      end: 100,
      height: 20,
      bottom: 10,
    },
  ],
  toolbox: {
    feature: {
      saveAsImage: {
        title: "保存为图片",
      },
      dataZoom: {
        title: {
          zoom: "区域缩放",
          back: "区域缩放还原",
        },
      },
    },
  },
}));

// Vue-ECharts 配置
const loadingOptions = {
  text: "加载中...",
  color: "#409EFF",
  textColor: "#000",
  maskColor: "rgba(255, 255, 255, 0.8)",
  zlevel: 0,
};

const initOptions = {
  renderer: "canvas" as const,
};

const updateOptions = {
  replaceMerge: ["series"],
};

// 获取时间周期参数
const getPeriodParams = (period: string) => {
  const now = new Date();
  const params: { startTime: string; endTime: string; groupBy?: string } = {
    startTime: "",
    endTime: now.toISOString().split("T")[0],
  };

  switch (period) {
    case "7days":
      params.startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];
      params.groupBy = "day";
      break;
    case "30days":
      params.startTime = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];
      params.groupBy = "day";
      break;
    case "3months":
      params.startTime = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];
      params.groupBy = "week";
      break;
    case "1year":
      params.startTime = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000)
        .toISOString()
        .split("T")[0];
      params.groupBy = "month";
      break;
  }

  return params;
};

// 加载图表数据
const loadChartData = async () => {
  try {
    loading.value = true;
    const params = getPeriodParams(selectedPeriod.value);
    const data = await loadArchiveCount(params);
    if (data) {
      // 数据将通过 props 传入，这里只是触发数据加载
      console.log("图表数据加载完成:", data);
    }
  } catch (err) {
    console.error("加载图表数据失败:", err);
  } finally {
    loading.value = false;
  }
};

// 处理时间周期变化
const handlePeriodChange = (period: string) => {
  selectedPeriod.value = period;
  loadChartData();
  emit("periodChange", period);
};

// 刷新图表
const refreshChart = () => {
  loadChartData();
  emit("refresh");
};

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit("chartClick", params);
};

// 监听数据变化
watch(
  () => props.data,
  () => {
    // 数据变化时图表会自动更新
  },
  { deep: true },
);

// 生命周期
onMounted(async () => {
  await nextTick();
  loadChartData();
});

// 暴露方法给父组件
defineExpose({
  refreshChart,
  loadChartData,
});
</script>

<style scoped>
.archive-trend-chart {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.period-selector {
  flex: 1;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-container {
  padding: 20px;
  height: v-bind(height);
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 768px) {
  .chart-controls {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .period-selector {
    display: flex;
    justify-content: center;
  }

  .chart-actions {
    justify-content: center;
  }

  .chart-container {
    padding: 12px;
  }
}
</style>
