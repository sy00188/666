<template>
  <div class="chart-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <h1>数据分析仪表板</h1>
        <p class="header-description">支持数据钻取和图表联动的交互式分析平台</p>
      </div>

      <div class="header-actions">
        <el-button-group>
          <el-button
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            @click="viewMode = 'grid'"
          >
            <el-icon><Grid /></el-icon>
            网格视图
          </el-button>
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
          >
            <el-icon><List /></el-icon>
            列表视图
          </el-button>
        </el-button-group>

        <el-button type="primary" @click="showLinkagePanel = true">
          <el-icon><Connection /></el-icon>
          联动配置
        </el-button>

        <el-button @click="exportDashboard">
          <el-icon><Download /></el-icon>
          导出仪表板
        </el-button>

        <el-button @click="showSettings = true">
          <el-icon><Setting /></el-icon>
          设置
        </el-button>
      </div>
    </div>

    <!-- 全局过滤器 -->
    <div class="global-filters" v-if="globalFilters.length > 0">
      <div class="filters-header">
        <h3>全局过滤器</h3>
        <el-button size="small" text @click="clearAllFilters">
          <el-icon><RefreshLeft /></el-icon>
          清除所有
        </el-button>
      </div>

      <div class="filters-content">
        <el-tag
          v-for="(filter, index) in globalFilters"
          :key="index"
          closable
          @close="removeGlobalFilter(index)"
          class="filter-tag"
        >
          {{ filter.label || `${filter.field}: ${filter.value}` }}
        </el-tag>
      </div>
    </div>

    <!-- 钻取面包屑 -->
    <DrillDownBreadcrumb
      v-if="currentDrillPath.levels.length > 0"
      :drill-path="currentDrillPath"
      :drill-config="drillConfig"
      :show-history="true"
      @drill-up="handleDrillUp"
      @drill-to="handleDrillTo"
      @reset="handleDrillReset"
    />

    <!-- 图表网格 -->
    <div
      class="charts-container"
      :class="{
        'grid-view': viewMode === 'grid',
        'list-view': viewMode === 'list',
      }"
    >
      <div
        v-for="chart in dashboardCharts"
        :key="chart.id"
        class="chart-item"
        :class="{ 'is-loading': chart.isLoading }"
      >
        <div class="chart-header">
          <h3>{{ chart.title }}</h3>
          <div class="chart-actions">
            <el-button size="small" @click="refreshChart(chart.id)">
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button size="small" @click="removeChart(chart.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="chart-content">
          <div v-if="chart.isLoading" class="chart-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <p>加载中...</p>
          </div>
          <div v-else-if="chart.error" class="chart-error">
            <el-icon><Warning /></el-icon>
            <p>{{ chart.error }}</p>
          </div>
          <div v-else class="chart-placeholder">
            <el-icon><TrendCharts /></el-icon>
            <p>{{ chart.type }} 图表</p>
            <p class="chart-description">
              {{ chart.description || "暂无数据" }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="dashboardCharts.length === 0"
      description="暂无图表数据"
      class="empty-state"
    >
      <el-button type="primary" @click="addChart">
        <el-icon><Plus /></el-icon>
        添加图表
      </el-button>
    </el-empty>

    <!-- 联动配置面板 -->
    <el-drawer
      v-model="showLinkagePanel"
      title="图表联动配置"
      size="600px"
      direction="rtl"
    >
      <ChartLinkagePanel />
    </el-drawer>

    <!-- 仪表板设置 -->
    <el-dialog v-model="showSettings" title="仪表板设置" width="500px">
      <el-form :model="dashboardSettings" label-width="120px">
        <el-form-item label="自动刷新">
          <el-switch v-model="dashboardSettings.autoRefresh" />
        </el-form-item>

        <el-form-item label="刷新间隔" v-if="dashboardSettings.autoRefresh">
          <el-select v-model="dashboardSettings.refreshInterval">
            <el-option label="30秒" :value="30" />
            <el-option label="1分钟" :value="60" />
            <el-option label="5分钟" :value="300" />
            <el-option label="10分钟" :value="600" />
          </el-select>
        </el-form-item>

        <el-form-item label="主题">
          <el-select v-model="dashboardSettings.theme">
            <el-option label="浅色主题" value="light" />
            <el-option label="深色主题" value="dark" />
            <el-option label="自动" value="auto" />
          </el-select>
        </el-form-item>

        <el-form-item label="图表动画">
          <el-switch v-model="dashboardSettings.enableAnimation" />
        </el-form-item>

        <el-form-item label="联动提示">
          <el-switch v-model="dashboardSettings.showLinkageHints" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveDashboardSettings"
          >保存</el-button
        >
      </template>
    </el-dialog>

    <!-- 添加图表对话框 -->
    <el-dialog v-model="showAddChart" title="添加图表" width="600px">
      <el-form :model="newChartForm" label-width="100px">
        <el-form-item label="图表标题">
          <el-input v-model="newChartForm.title" placeholder="请输入图表标题" />
        </el-form-item>

        <el-form-item label="图表类型">
          <el-select v-model="newChartForm.type" placeholder="选择图表类型">
            <el-option label="柱状图" value="bar" />
            <el-option label="折线图" value="line" />
            <el-option label="饼图" value="pie" />
            <el-option label="散点图" value="scatter" />
            <el-option label="热力图" value="heatmap" />
          </el-select>
        </el-form-item>

        <el-form-item label="数据源">
          <el-select v-model="newChartForm.dataSource" placeholder="选择数据源">
            <el-option label="借阅统计" value="borrow_stats" />
            <el-option label="用户分析" value="user_analysis" />
            <el-option label="图书分类" value="book_category" />
            <el-option label="时间趋势" value="time_trend" />
          </el-select>
        </el-form-item>

        <el-form-item label="启用钻取">
          <el-switch v-model="newChartForm.enableDrillDown" />
        </el-form-item>

        <el-form-item label="启用联动">
          <el-switch v-model="newChartForm.enableLinkage" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddChart = false">取消</el-button>
        <el-button type="primary" @click="createChart">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Grid,
  List,
  Connection,
  Download,
  Setting,
  RefreshLeft,
  Plus,
  Refresh,
  Delete,
  Loading,
  Warning,
  TrendCharts,
} from "@element-plus/icons-vue";
import ChartLinkagePanel from "@/components/charts/ChartLinkagePanel.vue";
import DrillDownBreadcrumb from "@/components/charts/DrillDownBreadcrumb.vue";
import type {
  ChartFilter,
  DrillDownPath,
  DrillDownConfig,
} from "@/types/chart";

// 简化的图表接口
interface SimpleChart {
  id: string;
  title: string;
  type: "line" | "bar" | "pie" | "scatter" | "heatmap";
  description?: string;
  isLoading: boolean;
  error?: string;
}

// 响应式数据
const viewMode = ref<"grid" | "list">("grid");
const showLinkagePanel = ref(false);
const showSettings = ref(false);
const showAddChart = ref(false);
const refreshTimer = ref<number | null>(null);

// 图表数据
const dashboardCharts = ref<SimpleChart[]>([
  {
    id: "chart1",
    title: "借阅趋势分析",
    type: "line",
    description: "显示最近6个月的借阅趋势",
    isLoading: false,
  },
  {
    id: "chart2",
    title: "图书分类统计",
    type: "pie",
    description: "按分类统计图书数量",
    isLoading: false,
  },
  {
    id: "chart3",
    title: "用户活跃度",
    type: "bar",
    description: "用户借阅活跃度分析",
    isLoading: false,
  },
]);

const globalFilters = ref<ChartFilter[]>([]);
const currentDrillPath = ref<DrillDownPath>({ levels: [] });

// 仪表板设置
const dashboardSettings = reactive({
  autoRefresh: false,
  refreshInterval: 300, // 5分钟
  theme: "light",
  enableAnimation: true,
  showLinkageHints: true,
});

// 新图表表单
const newChartForm = reactive({
  title: "",
  type: "",
  dataSource: "",
  enableDrillDown: true,
  enableLinkage: true,
});

// 钻取配置
const drillConfig: DrillDownConfig = {
  enabled: true,
  levels: [
    { level: 0, name: "全部", field: "all" },
    { level: 1, name: "分类", field: "category" },
    { level: 2, name: "子分类", field: "subcategory" },
    { level: 3, name: "详细", field: "detail" },
  ],
  currentLevel: 0,
  breadcrumb: [],
};

// 方法
const handleDrillUp = () => {
  if (currentDrillPath.value.levels.length > 0) {
    currentDrillPath.value.levels.pop();
    ElMessage.success("返回上一级");
  }
};

const handleDrillTo = (level: number) => {
  if (level < currentDrillPath.value.levels.length) {
    currentDrillPath.value.levels = currentDrillPath.value.levels.slice(
      0,
      level + 1,
    );
    ElMessage.success(`跳转到第 ${level + 1} 级`);
  }
};

const handleDrillReset = () => {
  currentDrillPath.value = { levels: [] };
  ElMessage.success("重置钻取路径");
};

const removeGlobalFilter = (index: number) => {
  globalFilters.value.splice(index, 1);
  ElMessage.success("已移除过滤器");
};

const clearAllFilters = () => {
  globalFilters.value = [];
  ElMessage.success("已清除所有过滤器");
};

const refreshChart = (chartId: string) => {
  const chart = dashboardCharts.value.find((c) => c.id === chartId);
  if (chart) {
    chart.isLoading = true;
    setTimeout(() => {
      chart.isLoading = false;
      ElMessage.success("图表刷新成功");
    }, 1000);
  }
};

const removeChart = async (chartId: string) => {
  try {
    await ElMessageBox.confirm("确定要删除这个图表吗？", "确认删除", {
      type: "warning",
    });

    const index = dashboardCharts.value.findIndex((c) => c.id === chartId);
    if (index > -1) {
      dashboardCharts.value.splice(index, 1);
      ElMessage.success("图表删除成功");
    }
  } catch {
    // 用户取消删除
  }
};

const addChart = () => {
  showAddChart.value = true;
};

const createChart = () => {
  if (!newChartForm.title || !newChartForm.type || !newChartForm.dataSource) {
    ElMessage.warning("请填写完整的图表信息");
    return;
  }

  const newChart: SimpleChart = {
    id: `chart_${Date.now()}`,
    title: newChartForm.title,
    type: newChartForm.type as 'line' | 'bar' | 'pie' | 'scatter' | 'heatmap',
    description: `基于${newChartForm.dataSource}的${newChartForm.title}`,
    isLoading: false,
  };

  dashboardCharts.value.push(newChart);

  // 重置表单
  Object.assign(newChartForm, {
    title: "",
    type: "",
    dataSource: "",
    enableDrillDown: true,
    enableLinkage: true,
  });

  showAddChart.value = false;
  ElMessage.success("图表创建成功");
};

const exportDashboard = async () => {
  try {
    // 模拟导出功能
    await new Promise((resolve) => setTimeout(resolve, 1000));
    ElMessage.success("仪表板导出成功");
  } catch (error) {
    ElMessage.error("导出失败");
  }
};

const saveDashboardSettings = () => {
  // 保存设置到本地存储
  localStorage.setItem("dashboardSettings", JSON.stringify(dashboardSettings));

  // 应用自动刷新设置
  if (dashboardSettings.autoRefresh) {
    startAutoRefresh();
  } else {
    stopAutoRefresh();
  }

  showSettings.value = false;
  ElMessage.success("设置保存成功");
};

const startAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value);
  }

  refreshTimer.value = window.setInterval(() => {
    // 刷新所有图表数据
    dashboardCharts.value.forEach((chart) => {
      chart.isLoading = true;
      setTimeout(() => {
        chart.isLoading = false;
      }, 1000);
    });
  }, dashboardSettings.refreshInterval * 1000);
};

const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value);
    refreshTimer.value = null;
  }
};

const loadDashboardSettings = () => {
  const saved = localStorage.getItem("dashboardSettings");
  if (saved) {
    Object.assign(dashboardSettings, JSON.parse(saved));
  }
};

// 生命周期
onMounted(() => {
  loadDashboardSettings();

  if (dashboardSettings.autoRefresh) {
    startAutoRefresh();
  }
});

onUnmounted(() => {
  stopAutoRefresh();
});
</script>

<style scoped>
.chart-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.global-filters {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.filters-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.filters-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  margin: 0;
}

.charts-container {
  display: grid;
  gap: 20px;
}

.charts-container.grid-view {
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
}

.charts-container.list-view {
  grid-template-columns: 1fr;
}

.chart-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.chart-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.chart-item.is-loading {
  opacity: 0.7;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-content {
  padding: 20px;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.chart-loading,
.chart-error,
.chart-placeholder {
  text-align: center;
  color: #666;
}

.chart-loading .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.chart-error .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
  color: #f56c6c;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #409eff;
}

.chart-description {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.empty-state {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chart-dashboard {
    padding: 12px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .header-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .charts-container.grid-view {
    grid-template-columns: 1fr;
  }

  .filters-content {
    justify-content: center;
  }
}
</style>
