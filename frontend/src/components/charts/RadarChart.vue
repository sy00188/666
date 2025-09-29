<template>
  <div class="radar-chart">
    <!-- 图表控制栏 -->
    <div class="chart-controls">
      <div class="chart-title">
        <h4>{{ title }}</h4>
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
import { ElButton } from "element-plus";
import { Refresh } from "@element-plus/icons-vue";
import VChart from "vue-echarts";
import { use } from "echarts/core";
import { CanvasRenderer, SVGRenderer } from "echarts/renderers";
import { RadarChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";
import type { EChartsOption } from "echarts";

// 注册 ECharts 组件
use([
  CanvasRenderer,
  SVGRenderer,
  RadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
]);

// 数据接口定义
interface RadarIndicator {
  name: string;
  max?: number;
  min?: number;
}

interface RadarSeriesData {
  name: string;
  value: number[];
  itemStyle?: {
    color?: string;
  };
  areaStyle?: {
    opacity?: number;
  };
}

// Props 定义
interface Props {
  data?: RadarSeriesData[];
  indicators?: RadarIndicator[];
  height?: string;
  title?: string;
  showArea?: boolean;
  colors?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  height: "400px",
  title: "雷达图",
  showArea: true,
  colors: () => ["#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399"],
});

// Emits 定义
interface Emits {
  (e: "refresh"): void;
  (e: "chartClick", params: any): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const loading = ref(false);
const chartRef = ref();

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
    trigger: "item",
    formatter: (params: any) => {
      const data = params.data;
      let result = `${data.name}<br/>`;
      data.value.forEach((value: number, index: number) => {
        const indicator = props.indicators?.[index];
        if (indicator) {
          result += `${indicator.name}: ${value}<br/>`;
        }
      });
      return result;
    },
  },
  legend: {
    data: props.data?.map((item) => item.name) || [],
    top: 30,
  },
  radar: {
    indicator: props.indicators || [],
    shape: "polygon",
    splitNumber: 5,
    axisName: {
      color: "#666",
      fontSize: 12,
    },
    splitLine: {
      lineStyle: {
        color: "#e0e6ed",
      },
    },
    splitArea: {
      show: false,
    },
    axisLine: {
      lineStyle: {
        color: "#e0e6ed",
      },
    },
  },
  series: [
    {
      name: "雷达图",
      type: "radar",
      data:
        props.data?.map((item, index) => ({
          ...item,
          itemStyle: {
            color: props.colors[index % props.colors.length],
          },
          areaStyle: props.showArea
            ? {
                opacity: 0.3,
              }
            : undefined,
          lineStyle: {
            width: 2,
          },
          symbol: "circle",
          symbolSize: 6,
        })) || [],
    },
  ],
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

// 刷新图表
const refreshChart = () => {
  emit("refresh");
};

// 处理图表点击事件
const handleChartClick = (params: any) => {
  emit("chartClick", params);
};

// 监听数据变化
watch(
  () => [props.data, props.indicators],
  () => {
    // 数据变化时图表会自动更新
  },
  { deep: true },
);

// 生命周期
onMounted(async () => {
  await nextTick();
});

// 暴露方法给父组件
defineExpose({
  refreshChart,
});
</script>

<style scoped>
.radar-chart {
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

.chart-title h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

  .chart-actions {
    justify-content: center;
  }

  .chart-container {
    padding: 12px;
  }
}
</style>
