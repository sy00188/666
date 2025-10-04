<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>仪表板</h1>
      <p>欢迎回来，{{ userInfo.name }}！今天是 {{ currentDate }}</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statsData" :key="stat.key">
        <div class="stat-icon" :style="{ backgroundColor: stat.color }">
          <el-icon>
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.trend">
            <el-icon>
              <ArrowUp v-if="stat.trend === 'up'" />
              <ArrowDown v-if="stat.trend === 'down'" />
              <Minus v-if="stat.trend === 'stable'" />
            </el-icon>
            {{ stat.change }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <ArchiveTrendChart
            :data="dashboardData?.archiveCount"
            @refresh="handleRefresh"
          />
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <BorrowStatChart
            :data="dashboardData?.borrowTrend"
            @refresh="handleRefresh"
          />
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作和最新动态 -->
    <div class="bottom-section">
      <el-row :gutter="24">
        <!-- 快捷操作 -->
        <el-col :span="8">
          <el-card class="quick-actions-card">
            <template #header>
              <span>快捷操作</span>
            </template>
            <div class="quick-actions">
              <div
                class="action-item"
                v-for="action in quickActions"
                :key="action.key"
                @click="handleQuickAction(action)"
              >
                <div class="action-icon">
                  <el-icon>
                    <component :is="action.icon" />
                  </el-icon>
                </div>
                <div class="action-content">
                  <div class="action-title">{{ action.title }}</div>
                  <div class="action-desc">{{ action.description }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 最新动态 -->
        <el-col :span="8">
          <el-card class="activities-card">
            <template #header>
              <span>最新动态</span>
            </template>
            <div class="activities-list">
              <div
                class="activity-item"
                v-for="activity in recentActivities"
                :key="activity.id"
              >
                <div class="activity-avatar">
                  <el-avatar :size="32" :src="activity.avatar">
                    {{ activity.user?.charAt(0) }}
                  </el-avatar>
                </div>
                <div class="activity-content">
                  <div class="activity-text">
                    <strong>{{ activity.user }}</strong>
                    {{ activity.action }}
                    <span class="activity-target">{{ activity.target }}</span>
                  </div>
                  <div class="activity-time">
                    {{ formatTime(activity.time) }}
                  </div>
                </div>
              </div>
              <div v-if="recentActivities.length === 0" class="no-activities">
                暂无动态
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 待办事项 -->
        <el-col :span="8">
          <el-card class="todos-card">
            <template #header>
              <div class="card-header">
                <span>待办事项</span>
                <el-button type="text" size="small" @click="showAllTodos">
                  查看全部
                </el-button>
              </div>
            </template>
            <div class="todos-list">
              <div
                class="todo-item"
                v-for="todo in pendingTodos"
                :key="todo.id"
                @click="handleTodoClick(todo)"
              >
                <div class="todo-priority" :class="todo.priority"></div>
                <div class="todo-content">
                  <div class="todo-title">{{ todo.title }}</div>
                  <div class="todo-desc">{{ todo.description }}</div>
                  <div class="todo-time">{{ formatTime(todo.dueDate) }}</div>
                </div>
                <div class="todo-status">
                  <el-tag :type="getTodoStatusType(todo.status)" size="small">
                    {{ todo.status }}
                  </el-tag>
                </div>
              </div>
              <div v-if="pendingTodos.length === 0" class="no-todos">
                暂无待办事项
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useDashboard } from "@/composables/useDashboard";
import ArchiveTrendChart from "@/components/dashboard/ArchiveTrendChart.vue";
import BorrowStatChart from "@/components/dashboard/BorrowStatChart.vue";
import { ArrowUp, ArrowDown, Minus } from "@element-plus/icons-vue";
import { useAuthStore } from "@/stores/modules/auth";
import { formatDistanceToNow } from "date-fns";
import { zhCN } from "date-fns/locale";
import * as echarts from "echarts";

const router = useRouter();
const authStore = useAuthStore();

// 使用仪表板数据管理
const { dashboardData, loadDashboardOverview, refreshAll } = useDashboard();

// 图表引用
const archiveChartRef = ref<HTMLElement>();
const borrowChartRef = ref<HTMLElement>();

// 用户信息
const userInfo = computed(() => authStore.user || { name: '用户' });

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long",
  });
});

// 统计数据
const statsData = computed(() => {
  if (!dashboardData.value) {
    return [
      {
        key: "totalArchives",
        label: "档案总数",
        value: "0",
        change: "+0%",
        trend: "stable",
        color: "#409eff",
        icon: "Document",
      },
      {
        key: "activeBorrows",
        label: "在借档案",
        value: "0",
        change: "+0%",
        trend: "stable",
        color: "#67c23a",
        icon: "Reading",
      },
      {
        key: "totalUsers",
        label: "用户总数",
        value: "0",
        change: "+0%",
        trend: "stable",
        color: "#e6a23c",
        icon: "User",
      },
      {
        key: "monthlyViews",
        label: "本月操作",
        value: "0",
        change: "+0%",
        trend: "stable",
        color: "#f56c6c",
        icon: "DataAnalysis",
      },
    ];
  }

  const { archiveCount, borrowTrend, userActivity } = dashboardData.value;
  
  // 安全地计算借阅统计数据
  const totalBorrow = borrowTrend?.borrowCounts?.reduce((a, b) => a + b, 0) || 0;
  const totalReturn = borrowTrend?.returnCounts?.reduce((a, b) => a + b, 0) || 0;
  
  return [
    {
      key: "totalArchives",
      label: "档案总数",
      value: (archiveCount?.totalCount || 0).toLocaleString(),
      change: "+12.5%",
      trend: "up",
      color: "#409eff",
      icon: "Document",
    },
    {
      key: "activeBorrows",
      label: "在借档案",
      value: (totalBorrow - totalReturn).toString(),
      change: "+5.2%",
      trend: "up",
      color: "#67c23a",
      icon: "Reading",
    },
    {
      key: "totalUsers",
      label: "用户总数",
      value: (userActivity?.totalUsers || 0).toString(),
      change: "+8.1%",
      trend: "up",
      color: "#e6a23c",
      icon: "User",
    },
    {
      key: "monthlyViews",
      label: "本月操作",
      value: (userActivity?.operationCount || 0).toLocaleString(),
      change: "-2.3%",
      trend: "down",
      color: "#f56c6c",
      icon: "DataAnalysis",
    },
  ];
});

// 快捷操作
const quickActions = ref([
  {
    key: "addArchive",
    title: "新增档案",
    description: "添加新的档案记录",
    icon: "Plus",
    path: "/archive/add",
  },
  {
    key: "searchArchive",
    title: "搜索档案",
    description: "快速查找档案信息",
    icon: "Search",
    path: "/archive/search",
  },
  {
    key: "borrowManage",
    title: "借阅管理",
    description: "处理借阅申请和归还",
    icon: "Reading",
    path: "/borrow/list",
  },
  {
    key: "systemSettings",
    title: "系统设置",
    description: "配置系统参数",
    icon: "Setting",
    path: "/system/settings",
  },
]);

// 最新动态
const recentActivities = computed(() => {
  return dashboardData.value?.recentActivities || [];
});

// 待办事项
const pendingTodos = computed(() => {
  return (
    dashboardData.value?.todoItems?.filter(
      (item) => item.status !== "completed",
    ) || []
  );
});

// 格式化时间
const formatTime = (time: Date | string) => {
  const date = typeof time === "string" ? new Date(time) : time;
  return formatDistanceToNow(date, {
    addSuffix: true,
    locale: zhCN,
  });
};

// 获取待办事项状态类型
const getTodoStatusType = (
  status: string,
): "primary" | "success" | "warning" | "info" | "danger" => {
  const statusMap: Record<
    string,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    待处理: "warning",
    紧急: "danger",
    计划中: "info",
    进行中: "primary",
    已完成: "success",
  };
  return statusMap[status] || "info";
};

// 处理快捷操作点击
const handleQuickAction = (action: any) => {
  router.push(action.path);
};

// 处理待办事项点击
const handleTodoClick = (todo: any) => {
  ElMessage.info(`点击了待办事项: ${todo.title}`);
};

// 显示全部待办事项
const showAllTodos = () => {
  router.push("/system/todos");
};

// 刷新数据
const handleRefresh = async () => {
  try {
    await refreshAll();
    ElMessage.success("数据刷新成功");
  } catch (err) {
    ElMessage.error("数据刷新失败");
  }
};

// 初始化档案趋势图
const initArchiveChart = () => {
  if (!archiveChartRef.value) return;

  const chart = echarts.init(archiveChartRef.value);
  const option = {
    tooltip: {
      trigger: "axis",
    },
    xAxis: {
      type: "category",
      data: ["1月", "2月", "3月", "4月", "5月", "6月"],
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        data: [120, 200, 150, 80, 70, 110],
        type: "line",
        smooth: true,
        itemStyle: {
          color: "#409eff",
        },
      },
    ],
  };
  chart.setOption(option);

  // 响应式调整
  window.addEventListener("resize", () => {
    chart.resize();
  });
};

// 初始化借阅统计图
const initBorrowChart = () => {
  if (!borrowChartRef.value) return;

  const chart = echarts.init(borrowChartRef.value);
  const option = {
    tooltip: {
      trigger: "item",
    },
    series: [
      {
        type: "pie",
        radius: "60%",
        data: [
          { value: 35, name: "在借" },
          { value: 25, name: "已归还" },
          { value: 15, name: "逾期" },
          { value: 25, name: "预约中" },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
  chart.setOption(option);

  // 响应式调整
  window.addEventListener("resize", () => {
    chart.resize();
  });
};

// 组件挂载后初始化图表
onMounted(async () => {
  try {
    await loadDashboardOverview();
  } catch (err) {
    console.error("加载仪表板数据失败:", err);
  }

  nextTick(() => {
    initArchiveChart();
    initBorrowChart();
  });
});
</script>

<style lang="scss" scoped>
.dashboard {
  padding: $spacing-lg;
}

.page-header {
  margin-bottom: $spacing-xl;

  h1 {
    font-size: $font-size-extra-large;
    color: $text-primary;
    margin-bottom: $spacing-sm;
  }

  p {
    color: $text-regular;
    font-size: $font-size-medium;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  background: $bg-color;
  border-radius: $border-radius-base;
  box-shadow: $box-shadow-light;
  transition:
    transform 0.2s,
    box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $box-shadow-base;
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-lg;

  .el-icon {
    font-size: $font-size-extra-large;
    color: white;
  }
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: $font-size-extra-large;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.stat-label {
  font-size: $font-size-medium;
  color: $text-regular;
  margin-bottom: $spacing-xs;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-small;

  &.up {
    color: $success-color;
  }

  &.down {
    color: $danger-color;
  }

  &.stable {
    color: $text-placeholder;
  }
}

.charts-section {
  margin-bottom: $spacing-xl;
}

.chart-card {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.chart-container {
  height: 300px;
}

.bottom-section {
  .el-card {
    height: 400px;

    :deep(.el-card__body) {
      height: calc(100% - 60px);
      overflow-y: auto;
    }
  }
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.action-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  border-radius: $border-radius-base;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: $bg-color-page;
  }
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: $primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;

  .el-icon {
    font-size: $font-size-large;
    color: white;
  }
}

.action-content {
  flex: 1;
}

.action-title {
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.action-desc {
  font-size: $font-size-small;
  color: $text-regular;
}

.activities-list,
.todos-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
}

.activity-content {
  flex: 1;
}

.activity-text {
  color: $text-primary;
  margin-bottom: $spacing-xs;

  .activity-target {
    color: $primary-color;
  }
}

.activity-time {
  font-size: $font-size-small;
  color: $text-placeholder;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding: $spacing-md;
  border-radius: $border-radius-base;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: $bg-color-page;
  }
}

.todo-priority {
  width: 4px;
  height: 40px;
  border-radius: 2px;

  &.high {
    background-color: $warning-color;
  }

  &.urgent {
    background-color: $danger-color;
  }

  &.medium {
    background-color: $primary-color;
  }

  &.low {
    background-color: $info-color;
  }
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.todo-desc {
  font-size: $font-size-small;
  color: $text-regular;
  margin-bottom: $spacing-xs;
}

.todo-time {
  font-size: $font-size-small;
  color: $text-placeholder;
}

.no-activities,
.no-todos {
  text-align: center;
  color: $text-placeholder;
  padding: $spacing-xl;
}

// 响应式设计
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .bottom-section {
    .el-col {
      margin-bottom: $spacing-lg;
    }
  }
}
</style>
