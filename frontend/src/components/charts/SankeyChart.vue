<template>
  <div class="sankey-chart">
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
import { SankeyChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";

// 注册 ECharts 组件
use([
  CanvasRenderer,
  SVGRenderer,
  SankeyChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
]);

// 数据接口定义
interface SankeyNode {
  name: string;
  value?: number;
  depth?: number;
  itemStyle?: {
    color?: string;
  };
}

interface SankeyLink {
  source: string;
  target: string;
  value: number;
  lineStyle?: {
    color?: string;
    opacity?: number;
  };
}

interface SankeyData {
  nodes: SankeyNode[];
  links: SankeyLink[];
}

// Props 定义
interface Props {
  data?: SankeyData;
  height?: string;
  title?: string;
  nodeWidth?: number;
  nodeGap?: number;
  layoutIterations?: number;
  orient?: "horizontal" | "vertical";
  draggable?: boolean;
  focusNodeAdjacency?: boolean | "inEdges" | "outEdges" | "allEdges";
}

const props = withDefaults(defineProps<Props>(), {
  height: "500px",
  title: "桑基图",
  nodeWidth: 20,
  nodeGap: 8,
  layoutIterations: 32,
  orient: "horizontal",
  draggable: true,
  focusNodeAdjacency: "allEdges",
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
  const sankeyData = props.data || { nodes: [], links: [] };

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
      trigger: "item",
      triggerOn: "mousemove",
      formatter: (params: any) => {
        if (params.dataType === "node") {
          return `${params.name}: ${params.value || "N/A"}`;
        } else if (params.dataType === "edge") {
          return `${params.source} → ${params.target}<br/>流量: ${params.value}`;
        }
        return "";
      },
    },
    series: [
      {
        type: "sankey",
        layout: "none",
        emphasis: {
          focus: "adjacency",
        },
        data: sankeyData.nodes,
        links: sankeyData.links,
        orient: props.orient,
        nodeAlign: "justify",
        nodeWidth: props.nodeWidth,
        nodeGap: props.nodeGap,
        layoutIterations: props.layoutIterations,
        draggable: props.draggable,
        focusNodeAdjacency: props.focusNodeAdjacency,
        lineStyle: {
          color: "gradient",
          curveness: 0.5,
        },
        label: {
          show: true,
          position: "right",
          fontSize: 12,
          color: "#333",
          formatter: (params: any) => {
            return params.name;
          },
        },
        itemStyle: {
          borderWidth: 1,
          borderColor: "#aaa",
        },
        levels: [
          {
            depth: 0,
            itemStyle: {
              color: "#fbb4ae",
            },
            lineStyle: {
              color: "source",
              opacity: 0.6,
            },
          },
          {
            depth: 1,
            itemStyle: {
              color: "#b3cde3",
            },
            lineStyle: {
              color: "source",
              opacity: 0.6,
            },
          },
          {
            depth: 2,
            itemStyle: {
              color: "#ccebc5",
            },
            lineStyle: {
              color: "source",
              opacity: 0.6,
            },
          },
          {
            depth: 3,
            itemStyle: {
              color: "#decbe4",
            },
            lineStyle: {
              color: "source",
              opacity: 0.6,
            },
          },
        ],
      },
    ],
  };
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
.sankey-chart {
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
