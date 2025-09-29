<template>
  <div class="heatmap-chart">
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
import { HeatmapChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  VisualMapComponent,
  CalendarComponent,
} from "echarts/components";
import type { EChartsOption } from "echarts";

// 注册 ECharts 组件
use([
  CanvasRenderer,
  SVGRenderer,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  VisualMapComponent,
  CalendarComponent,
]);

// Props 定义
interface Props {
  data?: Array<[string, string, number]>; // [x轴, y轴, 值]
  xAxisData?: string[];
  yAxisData?: string[];
  height?: string;
  title?: string;
  colorRange?: [string, string];
  showCalendar?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  height: "400px",
  title: "热力图",
  colorRange: () => ["#313695", "#a50026"],
  showCalendar: false,
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
const chartOption = computed<EChartsOption>(() => {
  if (props.showCalendar) {
    // 日历热力图模式
    return {
      title: {
        text: props.title,
        left: "center",
        textStyle: {
          fontSize: 16,
          fontWeight: "bold",
        },
      },
      tooltip: {
        position: "top",
        formatter: (params: any) => {
          return `${params.data[0]}<br/>数值: ${params.data[1]}`;
        },
      },
      visualMap: {
        min: 0,
        max: Math.max(...(props.data?.map((item) => item[2]) || [100])),
        calculable: true,
        orient: "horizontal",
        left: "center",
        top: "top",
        inRange: {
          color: props.colorRange,
        },
      },
      calendar: {
        top: 120,
        left: 30,
        right: 30,
        cellSize: ["auto", 13],
        range: new Date().getFullYear(),
        itemStyle: {
          borderWidth: 0.5,
        },
        yearLabel: { show: false },
      },
      series: [
        {
          type: "heatmap",
          coordinateSystem: "calendar",
          data: props.data || [],
        },
      ],
    };
  } else {
    // 普通热力图模式
    return {
      title: {
        text: props.title,
        left: "center",
        textStyle: {
          fontSize: 16,
          fontWeight: "bold",
        },
      },
      tooltip: {
        position: "top",
        formatter: (params: any) => {
          return `${props.yAxisData?.[params.data[1]] || params.data[1]} - ${
            props.xAxisData?.[params.data[0]] || params.data[0]
          }<br/>数值: ${params.data[2]}`;
        },
      },
      grid: {
        height: "50%",
        top: "10%",
      },
      xAxis: {
        type: "category",
        data: props.xAxisData || [],
        splitArea: {
          show: true,
        },
      },
      yAxis: {
        type: "category",
        data: props.yAxisData || [],
        splitArea: {
          show: true,
        },
      },
      visualMap: {
        min: 0,
        max: Math.max(...(props.data?.map((item) => item[2]) || [100])),
        calculable: true,
        orient: "horizontal",
        left: "center",
        bottom: "15%",
        inRange: {
          color: props.colorRange,
        },
      },
      series: [
        {
          name: "热力图",
          type: "heatmap",
          data: props.data || [],
          label: {
            show: true,
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        },
      ],
    };
  }
});

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
  () => [props.data, props.xAxisData, props.yAxisData],
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
.heatmap-chart {
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
