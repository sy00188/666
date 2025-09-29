<template>
  <div class="gauge-chart">
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
import { GaugeChart } from "echarts/charts";
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
  GaugeChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
]);

// 数据接口定义
interface GaugeData {
  name: string;
  value: number;
  unit?: string;
}

// Props 定义
interface Props {
  data?: GaugeData[];
  height?: string;
  title?: string;
  min?: number;
  max?: number;
  splitNumber?: number;
  colors?: Array<[number, string]>;
  showDetail?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  height: "400px",
  title: "仪表盘",
  min: 0,
  max: 100,
  splitNumber: 10,
  colors: () =>
    [
      [0.2, "#67e0e3"],
      [0.8, "#37a2da"],
      [1, "#fd666d"],
    ] as Array<[number, string]>,
  showDetail: true,
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
const chartOption = computed(() => {
  const gaugeData = props.data || [];
  const isMultiple = gaugeData.length > 1;

  if (isMultiple) {
    // 多仪表盘布局
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
        formatter: (params: any) => {
          return `${params.name}: ${params.value}${params.data?.unit || ""}`;
        },
      },
      series: gaugeData.map((item, index) => ({
        name: item.name,
        type: "gauge",
        center: [
          `${25 + (index % 2) * 50}%`,
          `${35 + Math.floor(index / 2) * 50}%`,
        ],
        radius: "25%",
        min: props.min,
        max: props.max,
        splitNumber: props.splitNumber,
        axisLine: {
          lineStyle: {
            color: props.colors,
            width: 6,
          },
        },
        pointer: {
          itemStyle: {
            color: "auto",
          },
        },
        axisTick: {
          distance: -30,
          length: 8,
          lineStyle: {
            color: "#fff",
            width: 2,
          },
        },
        splitLine: {
          distance: -30,
          length: 30,
          lineStyle: {
            color: "#fff",
            width: 4,
          },
        },
        axisLabel: {
          color: "auto",
          distance: 40,
          fontSize: 10,
        },
        detail: {
          valueAnimation: true,
          formatter: `{value}${item.unit || ""}`,
          color: "auto",
          fontSize: 12,
          offsetCenter: [0, "70%"],
        },
        data: [
          {
            value: item.value,
            name: item.name,
            unit: item.unit,
          },
        ],
      })),
    };
  } else {
    // 单仪表盘布局
    const singleData = gaugeData[0] || { name: "默认", value: 0 };
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
        formatter: (params: any) => {
          return `${params.name}: ${params.value}${singleData.unit || ""}`;
        },
      },
      series: [
        {
          name: singleData.name,
          type: "gauge",
          center: ["50%", "60%"],
          startAngle: 200,
          endAngle: -40,
          min: props.min,
          max: props.max,
          splitNumber: props.splitNumber,
          itemStyle: {
            color: "#58D9F9",
            shadowColor: "rgba(0,138,255,0.45)",
            shadowBlur: 10,
            shadowOffsetX: 2,
            shadowOffsetY: 2,
          },
          progress: {
            show: true,
            roundCap: true,
            width: 18,
          },
          pointer: {
            icon: "path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.2792,617.312956 C2088.32396,616.194028 2089.24407,615.30999 2090.36389,615.30999 Z",
            length: "75%",
            width: 16,
            offsetCenter: [0, "5%"],
          },
          axisLine: {
            roundCap: true,
            lineStyle: {
              width: 18,
              color: props.colors,
            },
          },
          axisTick: {
            distance: -45,
            splitNumber: 5,
            lineStyle: {
              width: 2,
              color: "#999",
            },
          },
          splitLine: {
            distance: -52,
            length: 14,
            lineStyle: {
              width: 3,
              color: "#999",
            },
          },
          axisLabel: {
            distance: -20,
            color: "#999",
            fontSize: 20,
          },
          anchor: {
            show: false,
          },
          title: {
            show: false,
          },
          detail: {
            valueAnimation: true,
            width: "60%",
            lineHeight: 40,
            borderRadius: 8,
            offsetCenter: [0, "-15%"],
            fontSize: 30,
            fontWeight: "bolder",
            formatter: `{value}${singleData.unit || ""}`,
            color: "inherit",
          },
          data: [
            {
              value: singleData.value,
              name: singleData.name,
            },
          ],
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
  () => props.data,
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
.gauge-chart {
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
