<template>
  <div class="treemap-chart">
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
        <el-button
          v-if="enableDrillDown"
          size="small"
          :icon="ArrowUp"
          :disabled="!canGoBack"
          @click="goBack"
        >
          返回上级
        </el-button>
      </div>
    </div>

    <!-- 面包屑导航 -->
    <div v-if="enableDrillDown && breadcrumbs.length > 0" class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item
          v-for="(item, index) in breadcrumbs"
          :key="index"
          @click="drillToLevel(index)"
          :class="{ clickable: index < breadcrumbs.length - 1 }"
        >
          {{ item }}
        </el-breadcrumb-item>
      </el-breadcrumb>
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
import { ElButton, ElBreadcrumb, ElBreadcrumbItem } from "element-plus";
import { Refresh, ArrowUp } from "@element-plus/icons-vue";
import VChart from "vue-echarts";
import { use } from "echarts/core";
import { CanvasRenderer, SVGRenderer } from "echarts/renderers";
import { TreemapChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
} from "echarts/components";

// 注册 ECharts 组件
use([
  CanvasRenderer,
  SVGRenderer,
  TreemapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
]);

// 数据接口定义
interface TreemapNode {
  name: string;
  value?: number;
  children?: TreemapNode[];
  itemStyle?: {
    color?: string;
    borderColor?: string;
    borderWidth?: number;
  };
  label?: {
    show?: boolean;
    position?: string;
    fontSize?: number;
    color?: string;
  };
}

// Props 定义
interface Props {
  data?: TreemapNode[];
  height?: string;
  title?: string;
  enableDrillDown?: boolean;
  roam?: boolean | string;
  nodeClick?: boolean | "zoomToNode" | "link";
  breadcrumbHeight?: number;
  visualDimension?: number;
  colorMappingBy?: "value" | "index" | "id";
  colorSaturation?: [number, number];
  colorAlpha?: [number, number];
  levels?: any[];
}

const props = withDefaults(defineProps<Props>(), {
  height: "500px",
  title: "树状图",
  enableDrillDown: true,
  roam: false,
  nodeClick: "zoomToNode",
  breadcrumbHeight: 22,
  visualDimension: 0,
  colorMappingBy: "value",
  colorSaturation: () => [0.15, 0.8],
  colorAlpha: () => [0.4, 1],
  levels: () => [
    {
      itemStyle: {
        borderColor: "#777",
        borderWidth: 0,
        gapWidth: 1,
      },
      upperLabel: {
        show: false,
      },
    },
    {
      itemStyle: {
        borderColor: "#555",
        borderWidth: 5,
        gapWidth: 1,
      },
      emphasis: {
        itemStyle: {
          borderColor: "#ddd",
        },
      },
    },
    {
      itemStyle: {
        borderColor: "#333",
        borderWidth: 5,
        gapWidth: 1,
      },
      emphasis: {
        itemStyle: {
          borderColor: "#999",
        },
      },
    },
  ],
});

// Emits 定义
interface Emits {
  (e: "refresh"): void;
  (e: "chartClick", params: any): void;
  (e: "drillDown", node: TreemapNode): void;
  (e: "drillUp", level: number): void;
}

const emit = defineEmits<Emits>();

// 响应式数据
const loading = ref(false);
const chartRef = ref();
const currentPath = ref<string[]>([]);
const breadcrumbs = ref<string[]>([]);

// 计算属性
const canGoBack = computed(() => currentPath.value.length > 0);

const currentData = computed(() => {
  if (!props.data || currentPath.value.length === 0) {
    return props.data || [];
  }

  let current: TreemapNode[] = props.data;
  for (const pathItem of currentPath.value) {
    const found = current.find((item) => item.name === pathItem);
    if (found && found.children) {
      current = found.children;
    } else {
      return [];
    }
  }
  return current;
});

// 图表配置
const chartOption = computed(() => {
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
      formatter: (params: any) => {
        const value = params.value || 0;
        const percent = params.treePathInfo
          ? ((value / params.treePathInfo[0].value) * 100).toFixed(2)
          : "0";
        return `${params.name}<br/>值: ${value}<br/>占比: ${percent}%`;
      },
    },
    series: [
      {
        name: props.title,
        type: "treemap",
        data: currentData.value,
        roam: props.roam,
        nodeClick: props.nodeClick,
        breadcrumb: {
          show: props.enableDrillDown,
          height: props.breadcrumbHeight,
          left: "center",
          top: "bottom",
          emptyItemWidth: 25,
          itemStyle: {
            color: "rgba(0,0,0,0.7)",
            borderColor: "rgba(255,255,255,0.7)",
            borderWidth: 1,
            shadowColor: "rgba(150,150,150,1)",
            shadowBlur: 3,
            shadowOffsetX: 0,
            shadowOffsetY: 0,
            textStyle: {
              color: "#fff",
            },
          },
          emphasis: {
            itemStyle: {
              color: "rgba(0,0,0,0.9)",
            },
          },
        },
        label: {
          show: true,
          formatter: "{b}",
          fontSize: 12,
          color: "#fff",
        },
        upperLabel: {
          show: true,
          height: 30,
          fontSize: 14,
          color: "#fff",
        },
        itemStyle: {
          borderColor: "#fff",
        },
        levels: props.levels,
        visualDimension: props.visualDimension,
        visualMap: false,
        colorMappingBy: props.colorMappingBy,
        colorSaturation: props.colorSaturation,
        colorAlpha: props.colorAlpha,
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
  if (props.enableDrillDown && params.data && params.data.children) {
    drillDown(params.data);
  }
  emit("chartClick", params);
};

// 下钻
const drillDown = (node: TreemapNode) => {
  if (node.children && node.children.length > 0) {
    currentPath.value.push(node.name);
    updateBreadcrumbs();
    emit("drillDown", node);
  }
};

// 返回上级
const goBack = () => {
  if (currentPath.value.length > 0) {
    currentPath.value.pop();
    updateBreadcrumbs();
    emit("drillUp", currentPath.value.length);
  }
};

// 钻取到指定层级
const drillToLevel = (level: number) => {
  if (level < currentPath.value.length) {
    currentPath.value = currentPath.value.slice(0, level + 1);
    updateBreadcrumbs();
    emit("drillUp", level);
  }
};

// 更新面包屑
const updateBreadcrumbs = () => {
  breadcrumbs.value = ["根目录", ...currentPath.value];
};

// 重置到根级别
const resetToRoot = () => {
  currentPath.value = [];
  updateBreadcrumbs();
};

// 监听数据变化
watch(
  () => props.data,
  () => {
    resetToRoot();
  },
  { deep: true },
);

// 生命周期
onMounted(async () => {
  await nextTick();
  updateBreadcrumbs();
});

// 暴露方法给父组件
defineExpose({
  refreshChart,
  drillDown,
  goBack,
  drillToLevel,
  resetToRoot,
});
</script>

<style scoped>
.treemap-chart {
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

.breadcrumb {
  padding: 8px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  font-size: 12px;
}

.breadcrumb .clickable {
  cursor: pointer;
  color: #409eff;
}

.breadcrumb .clickable:hover {
  color: #66b1ff;
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

  .breadcrumb {
    padding: 6px 12px;
  }
}
</style>
