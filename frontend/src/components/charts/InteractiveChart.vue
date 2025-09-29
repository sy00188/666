<template>
  <div class="interactive-chart">
    <!-- 图表头部 -->
    <div class="chart-header">
      <div class="chart-title">
        <h3>{{ chart.title }}</h3>
        <div class="chart-actions">
          <el-button
            v-if="canDrillUp"
            size="small"
            type="primary"
            @click="handleDrillUp"
          >
            <el-icon><ArrowUp /></el-icon>
            返回上级
          </el-button>
          <el-button
            v-if="hasFilters"
            size="small"
            type="warning"
            @click="handleClearFilters"
          >
            <el-icon><Refresh /></el-icon>
            清除过滤
          </el-button>
          <el-dropdown @command="handleMenuCommand">
            <el-button size="small">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="export">导出图表</el-dropdown-item>
                <el-dropdown-item command="fullscreen"
                  >全屏显示</el-dropdown-item
                >
                <el-dropdown-item command="settings">图表设置</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 钻取面包屑 -->
      <div v-if="breadcrumb.length > 0" class="drill-breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="resetDrillDown">
            <el-icon><House /></el-icon>
            根目录
          </el-breadcrumb-item>
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumb"
            :key="index"
            @click="drillToLevel(index)"
          >
            {{ item.name }}: {{ item.value }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 当前过滤器 -->
      <div v-if="activeFilters.length > 0" class="active-filters">
        <el-tag
          v-for="filter in activeFilters"
          :key="filter.field + filter.value"
          closable
          size="small"
          @close="removeFilter(filter)"
        >
          {{ filter.label || `${filter.field}: ${filter.value}` }}
        </el-tag>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="chart-container" :style="{ height: chartHeight + 'px' }">
      <div
        v-if="chart.state.isLoading"
        class="chart-loading"
        v-loading="true"
        element-loading-text="加载中..."
      />

      <div v-else-if="chart.state.error" class="chart-error">
        <el-alert
          :title="chart.state.error"
          type="error"
          show-icon
          :closable="false"
        />
      </div>

      <div
        v-else
        ref="chartRef"
        class="chart-content"
        @click="handleChartClick"
      />
    </div>

    <!-- 图表设置对话框 -->
    <el-dialog v-model="showSettings" title="图表设置" width="600px">
      <el-form :model="chartSettings" label-width="100px">
        <el-form-item label="图表标题">
          <el-input v-model="chartSettings.title" />
        </el-form-item>
        <el-form-item label="图表类型">
          <el-select v-model="chartSettings.type">
            <el-option label="柱状图" value="bar" />
            <el-option label="折线图" value="line" />
            <el-option label="饼图" value="pie" />
            <el-option label="散点图" value="scatter" />
            <el-option label="面积图" value="area" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用钻取">
          <el-switch v-model="chartSettings.drillDown.enabled" />
        </el-form-item>
        <el-form-item label="图表高度">
          <el-input-number
            v-model="chartSettings.height"
            :min="200"
            :max="800"
            :step="50"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="applySettings">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from "vue";
import * as echarts from "echarts";
import { ArrowUp, Refresh, MoreFilled, House } from "@element-plus/icons-vue";
import { useChartInteraction } from "@/composables/useChartInteraction";
import type {
  InteractiveChart,
  ChartFilter,
  ChartInteraction,
} from "@/types/chart";

interface Props {
  chart: InteractiveChart;
  height?: number;
}

const props = withDefaults(defineProps<Props>(), {
  height: 400,
});

const emit = defineEmits<{
  interaction: [interaction: ChartInteraction];
  drillDown: [level: number, value: any];
  filter: [filters: ChartFilter[]];
}>();

const {
  drillDown,
  drillUp,
  resetDrillDown,
  applyFilters,
  clearFilters,
  handleChartInteraction,
} = useChartInteraction();

// 响应式数据
const chartRef = ref<HTMLElement>();
const chartInstance = ref<echarts.ECharts>();
const showSettings = ref(false);
const chartSettings = ref({
  title: props.chart.title,
  type: props.chart.type,
  height: props.height,
  drillDown: { ...props.chart.state.drillDown },
});

// 计算属性
const chartHeight = computed(() => props.chart.config.height || props.height);
const canDrillUp = computed(() => props.chart.state.drillDown.currentLevel > 0);
const hasFilters = computed(() => props.chart.state.filters.length > 0);
const breadcrumb = computed(() => props.chart.state.drillDown.breadcrumb);
const activeFilters = computed(() => props.chart.state.filters);

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return;

  chartInstance.value = echarts.init(chartRef.value);
  updateChart();

  // 绑定点击事件
  chartInstance.value.on("click", (params: any) => {
    const interaction: ChartInteraction = {
      type: "click",
      data: params.data,
      series: props.chart.series[params.seriesIndex],
      chart: props.chart.id,
    };

    emit("interaction", interaction);
    handleChartInteraction(interaction);

    // 如果启用了钻取且有下一级
    if (props.chart.state.drillDown.enabled && canDrillDown(params)) {
      handleDrillDown(params);
    }
  });

  // 绑定鼠标悬停事件
  chartInstance.value.on("mouseover", (params: any) => {
    const interaction: ChartInteraction = {
      type: "hover",
      data: params.data,
      series: props.chart.series[params.seriesIndex],
      chart: props.chart.id,
    };
    emit("interaction", interaction);
  });
};

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) return;

  const option = generateChartOption();
  chartInstance.value.setOption(option, true);
};

// 生成图表配置
const generateChartOption = () => {
  const { config, series, type } = props.chart;

  const baseOption = {
    title: {
      text: config.title,
      left: "center",
    },
    tooltip: {
      trigger: type === "pie" ? "item" : "axis",
      formatter: config.tooltip?.formatter || undefined,
    },
    legend: {
      show: config.legend?.show !== false,
      orient: "horizontal",
      left: "center",
      top: "bottom",
    },
    color: config.colors || [
      "#5470c6",
      "#91cc75",
      "#fac858",
      "#ee6666",
      "#73c0de",
      "#3ba272",
      "#fc8452",
      "#9a60b4",
    ],
  };

  // 根据图表类型生成特定配置
  switch (type) {
    case "bar":
    case "line":
      return {
        ...baseOption,
        xAxis: {
          type: "category",
          data: series[0]?.data.map((item) => item.name) || [],
          name: config.xAxis?.title,
        },
        yAxis: {
          type: "value",
          name: config.yAxis?.title,
          min: config.yAxis?.min,
          max: config.yAxis?.max,
        },
        series: series.map((s) => ({
          name: s.name,
          type: s.type || type,
          data: s.data.map((item) => item.value),
          itemStyle: {
            color: s.color,
          },
        })),
      };

    case "pie":
      return {
        ...baseOption,
        series: series.map((s) => ({
          name: s.name,
          type: "pie",
          radius: "50%",
          data: s.data.map((item) => ({
            name: item.name,
            value: item.value,
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        })),
      };

    case "scatter":
      return {
        ...baseOption,
        xAxis: {
          type: "value",
          name: config.xAxis?.title,
        },
        yAxis: {
          type: "value",
          name: config.yAxis?.title,
        },
        series: series.map((s) => ({
          name: s.name,
          type: "scatter",
          data: s.data.map((item) => [item.value, item.category || 0]),
        })),
      };

    default:
      return baseOption;
  }
};

// 判断是否可以钻取
const canDrillDown = (params: any): boolean => {
  const currentLevel = props.chart.state.drillDown.currentLevel;
  const maxLevel = props.chart.state.drillDown.levels.length - 1;
  return currentLevel < maxLevel;
};

// 处理钻取
const handleDrillDown = async (params: any) => {
  const currentLevel = props.chart.state.drillDown.currentLevel;
  const nextLevel = props.chart.state.drillDown.levels[currentLevel + 1];

  if (nextLevel) {
    await drillDown(props.chart.id, nextLevel, params.data.name);
    emit("drillDown", nextLevel.level, params.data.name);
  }
};

// 处理返回上级
const handleDrillUp = () => {
  drillUp(props.chart.id);
};

// 钻取到指定级别
const drillToLevel = (level: number) => {
  // 实现钻取到指定级别的逻辑
  console.log("Drill to level:", level);
};

// 重置钻取
const handleResetDrillDown = () => {
  resetDrillDown(props.chart.id);
};

// 清除过滤器
const handleClearFilters = () => {
  clearFilters(props.chart.id);
  emit("filter", []);
};

// 移除单个过滤器
const removeFilter = (filter: ChartFilter) => {
  const newFilters = props.chart.state.filters.filter(
    (f) => f.field !== filter.field || f.value !== filter.value,
  );
  applyFilters(props.chart.id, newFilters);
  emit("filter", newFilters);
};

// 处理菜单命令
const handleMenuCommand = (command: string) => {
  switch (command) {
    case "export":
      exportChart();
      break;
    case "fullscreen":
      toggleFullscreen();
      break;
    case "settings":
      showSettings.value = true;
      break;
  }
};

// 导出图表
const exportChart = () => {
  if (chartInstance.value) {
    const url = chartInstance.value.getDataURL({
      type: "png",
      backgroundColor: "#fff",
    });
    const link = document.createElement("a");
    link.download = `${props.chart.title}.png`;
    link.href = url;
    link.click();
  }
};

// 全屏切换
const toggleFullscreen = () => {
  // 实现全屏逻辑
  console.log("Toggle fullscreen");
};

// 应用设置
const applySettings = () => {
  // 更新图表配置
  props.chart.title = chartSettings.value.title;
  props.chart.type = chartSettings.value.type;
  props.chart.config.height = chartSettings.value.height;
  props.chart.state.drillDown = { ...chartSettings.value.drillDown };

  showSettings.value = false;
  nextTick(() => {
    updateChart();
    resizeChart();
  });
};

// 图表点击处理
const handleChartClick = (event: Event) => {
  // 处理图表容器点击事件
};

// 调整图表大小
const resizeChart = () => {
  if (chartInstance.value) {
    chartInstance.value.resize();
  }
};

// 监听图表数据变化
watch(
  () => props.chart.series,
  () => {
    updateChart();
  },
  { deep: true },
);

// 监听图表状态变化
watch(
  () => props.chart.state,
  () => {
    updateChart();
  },
  { deep: true },
);

// 生命周期
onMounted(() => {
  nextTick(() => {
    initChart();
  });

  // 监听窗口大小变化
  window.addEventListener("resize", resizeChart);
});

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }
  window.removeEventListener("resize", resizeChart);
});
</script>

<style scoped>
.interactive-chart {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.drill-breadcrumb {
  margin-bottom: 12px;
}

.drill-breadcrumb :deep(.el-breadcrumb__item) {
  cursor: pointer;
}

.drill-breadcrumb :deep(.el-breadcrumb__item:hover) {
  color: #409eff;
}

.active-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chart-container {
  position: relative;
  padding: 16px;
}

.chart-loading,
.chart-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-content {
  width: 100%;
  height: 100%;
  cursor: pointer;
}
</style>
