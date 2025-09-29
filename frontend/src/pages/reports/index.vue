<template>
  <div class="reports-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">报表统计</h1>
      <p class="page-description">查看系统各项数据统计和分析报表</p>
    </div>

    <!-- 时间筛选器 -->
    <div class="filter-section">
      <el-card>
        <el-form :model="filterForm" inline>
          <el-form-item label="统计时间">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateChange"
            />
          </el-form-item>
          <el-form-item label="统计类型">
            <el-select
              v-model="filterForm.reportType"
              @change="handleTypeChange"
            >
              <el-option label="档案统计" value="archive" />
              <el-option label="借阅统计" value="borrow" />
              <el-option label="用户统计" value="user" />
              <el-option label="综合统计" value="comprehensive" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-button @click="exportReport">
              <el-icon><Download /></el-icon>
              导出报表
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 概览统计卡片 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon archive">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-number">{{ overviewData.totalArchives }}</div>
              <div class="card-label">总档案数</div>
              <div
                class="card-trend"
                :class="overviewData.archiveTrend > 0 ? 'up' : 'down'"
              >
                <el-icon
                  ><ArrowUp v-if="overviewData.archiveTrend > 0" /><ArrowDown
                    v-else
                /></el-icon>
                {{ Math.abs(overviewData.archiveTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon borrow">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-number">{{ overviewData.totalBorrows }}</div>
              <div class="card-label">总借阅次数</div>
              <div
                class="card-trend"
                :class="overviewData.borrowTrend > 0 ? 'up' : 'down'"
              >
                <el-icon
                  ><ArrowUp v-if="overviewData.borrowTrend > 0" /><ArrowDown
                    v-else
                /></el-icon>
                {{ Math.abs(overviewData.borrowTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon user">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-number">{{ overviewData.totalUsers }}</div>
              <div class="card-label">总用户数</div>
              <div
                class="card-trend"
                :class="overviewData.userTrend > 0 ? 'up' : 'down'"
              >
                <el-icon
                  ><ArrowUp v-if="overviewData.userTrend > 0" /><ArrowDown
                    v-else
                /></el-icon>
                {{ Math.abs(overviewData.userTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon active">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-number">{{ overviewData.activeRate }}%</div>
              <div class="card-label">活跃度</div>
              <div
                class="card-trend"
                :class="overviewData.activeTrend > 0 ? 'up' : 'down'"
              >
                <el-icon
                  ><ArrowUp v-if="overviewData.activeTrend > 0" /><ArrowDown
                    v-else
                /></el-icon>
                {{ Math.abs(overviewData.activeTrend) }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 趋势图 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>数据趋势图</span>
                <el-radio-group v-model="trendChartType" size="small">
                  <el-radio-button value="daily">日</el-radio-button>
                  <el-radio-button value="weekly">周</el-radio-button>
                  <el-radio-button value="monthly">月</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 分类统计饼图 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>分类统计</span>
            </template>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <!-- 热门档案排行 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>热门档案排行</span>
            </template>
            <div class="ranking-list">
              <div
                v-for="(item, index) in hotArchives"
                :key="item.id"
                class="ranking-item"
              >
                <div class="ranking-number" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="ranking-content">
                  <div class="ranking-title">{{ item.title }}</div>
                  <div class="ranking-meta">
                    <span>借阅次数: {{ item.borrowCount }}</span>
                    <span>分类: {{ item.category }}</span>
                  </div>
                </div>
                <div class="ranking-score">{{ item.score }}</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 活跃用户排行 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>活跃用户排行</span>
            </template>
            <div class="ranking-list">
              <div
                v-for="(item, index) in activeUsers"
                :key="item.id"
                class="ranking-item"
              >
                <div class="ranking-number" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="ranking-content">
                  <div class="ranking-title">{{ item.name }}</div>
                  <div class="ranking-meta">
                    <span>借阅次数: {{ item.borrowCount }}</span>
                    <span>最后活跃: {{ formatDate(item.lastActive) }}</span>
                  </div>
                </div>
                <div class="ranking-score">{{ item.score }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>详细数据</span>
            <el-button-group size="small">
              <el-button
                v-for="tab in dataTabs"
                :key="tab.value"
                :type="activeTab === tab.value ? 'primary' : ''"
                @click="activeTab = tab.value"
              >
                {{ tab.label }}
              </el-button>
            </el-button-group>
          </div>
        </template>

        <!-- 档案统计表格 -->
        <el-table
          v-if="activeTab === 'archive'"
          :data="archiveTableData"
          v-loading="loading"
          stripe
        >
          <el-table-column prop="category" label="档案分类" />
          <el-table-column prop="count" label="数量" />
          <el-table-column prop="borrowCount" label="借阅次数" />
          <el-table-column prop="avgBorrowTime" label="平均借阅时长(天)" />
          <el-table-column prop="popularityRate" label="热门度" />
        </el-table>

        <!-- 借阅统计表格 -->
        <el-table
          v-if="activeTab === 'borrow'"
          :data="borrowTableData"
          v-loading="loading"
          stripe
        >
          <el-table-column prop="date" label="日期" />
          <el-table-column prop="borrowCount" label="借阅次数" />
          <el-table-column prop="returnCount" label="归还次数" />
          <el-table-column prop="overdueCount" label="逾期次数" />
          <el-table-column prop="newUserCount" label="新增用户" />
        </el-table>

        <!-- 用户统计表格 -->
        <el-table
          v-if="activeTab === 'user'"
          :data="userTableData"
          v-loading="loading"
          stripe
        >
          <el-table-column prop="department" label="部门" />
          <el-table-column prop="userCount" label="用户数" />
          <el-table-column prop="activeCount" label="活跃用户" />
          <el-table-column prop="borrowCount" label="借阅次数" />
          <el-table-column prop="avgBorrowPerUser" label="人均借阅" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import { ElMessage } from "element-plus";
import {
  Refresh,
  Download,
  Document,
  Reading,
  User,
  TrendCharts,
  ArrowUp,
  ArrowDown,
} from "@element-plus/icons-vue";
import * as echarts from "echarts";
import { format } from "date-fns";

// 响应式数据
const loading = ref(false);
const trendChartRef = ref();
const pieChartRef = ref();
const trendChartType = ref("daily");
const activeTab = ref("archive");

// 筛选表单
const filterForm = reactive({
  dateRange: [] as string[],
  reportType: "comprehensive",
});

// 概览数据
const overviewData = reactive({
  totalArchives: 0,
  totalBorrows: 0,
  totalUsers: 0,
  activeRate: 0,
  archiveTrend: 0,
  borrowTrend: 0,
  userTrend: 0,
  activeTrend: 0,
});

// 热门档案
const hotArchives = ref([]);

// 活跃用户
const activeUsers = ref([]);

// 数据表格标签
const dataTabs = [
  { label: "档案统计", value: "archive" },
  { label: "借阅统计", value: "borrow" },
  { label: "用户统计", value: "user" },
];

// 表格数据
const archiveTableData = ref([]);
const borrowTableData = ref([]);
const userTableData = ref([]);

// 图表实例
let trendChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;

// 方法
const loadOverviewData = async () => {
  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 500));

    // 模拟数据
    Object.assign(overviewData, {
      totalArchives: 1248,
      totalBorrows: 3567,
      totalUsers: 89,
      activeRate: 78.5,
      archiveTrend: 12.3,
      borrowTrend: -5.2,
      userTrend: 8.7,
      activeTrend: 15.6,
    });
  } catch (error) {
    ElMessage.error("加载概览数据失败");
  }
};

const loadRankingData = async () => {
  try {
    // 模拟热门档案数据
    hotArchives.value = [
      {
        id: 1,
        title: "公司章程修订版",
        borrowCount: 45,
        category: "法律文件",
        score: 95,
      },
      {
        id: 2,
        title: "2023年度财务报告",
        borrowCount: 38,
        category: "财务档案",
        score: 89,
      },
      {
        id: 3,
        title: "员工手册第三版",
        borrowCount: 32,
        category: "人事档案",
        score: 85,
      },
      {
        id: 4,
        title: "项目管理规范",
        borrowCount: 28,
        category: "管理制度",
        score: 82,
      },
      {
        id: 5,
        title: "安全操作手册",
        borrowCount: 25,
        category: "安全档案",
        score: 78,
      },
    ];

    // 模拟活跃用户数据
    activeUsers.value = [
      {
        id: 1,
        name: "张三",
        borrowCount: 23,
        lastActive: new Date("2024-01-15"),
        score: 92,
      },
      {
        id: 2,
        name: "李四",
        borrowCount: 19,
        lastActive: new Date("2024-01-14"),
        score: 88,
      },
      {
        id: 3,
        name: "王五",
        borrowCount: 16,
        lastActive: new Date("2024-01-13"),
        score: 84,
      },
      {
        id: 4,
        name: "赵六",
        borrowCount: 14,
        lastActive: new Date("2024-01-12"),
        score: 81,
      },
      {
        id: 5,
        name: "钱七",
        borrowCount: 12,
        lastActive: new Date("2024-01-11"),
        score: 78,
      },
    ];
  } catch (error) {
    ElMessage.error("加载排行数据失败");
  }
};

const loadTableData = async () => {
  loading.value = true;
  try {
    // 模拟档案统计数据
    archiveTableData.value = [
      {
        category: "法律文件",
        count: 156,
        borrowCount: 234,
        avgBorrowTime: 7.5,
        popularityRate: "85%",
      },
      {
        category: "财务档案",
        count: 298,
        borrowCount: 445,
        avgBorrowTime: 5.2,
        popularityRate: "78%",
      },
      {
        category: "人事档案",
        count: 187,
        borrowCount: 312,
        avgBorrowTime: 6.8,
        popularityRate: "72%",
      },
      {
        category: "管理制度",
        count: 89,
        borrowCount: 156,
        avgBorrowTime: 4.3,
        popularityRate: "68%",
      },
      {
        category: "技术文档",
        count: 234,
        borrowCount: 389,
        avgBorrowTime: 8.1,
        popularityRate: "82%",
      },
    ];

    // 模拟借阅统计数据
    borrowTableData.value = [
      {
        date: "2024-01-15",
        borrowCount: 45,
        returnCount: 38,
        overdueCount: 3,
        newUserCount: 2,
      },
      {
        date: "2024-01-14",
        borrowCount: 52,
        returnCount: 41,
        overdueCount: 5,
        newUserCount: 1,
      },
      {
        date: "2024-01-13",
        borrowCount: 38,
        returnCount: 35,
        overdueCount: 2,
        newUserCount: 3,
      },
      {
        date: "2024-01-12",
        borrowCount: 41,
        returnCount: 39,
        overdueCount: 1,
        newUserCount: 0,
      },
      {
        date: "2024-01-11",
        borrowCount: 47,
        returnCount: 42,
        overdueCount: 4,
        newUserCount: 2,
      },
    ];

    // 模拟用户统计数据
    userTableData.value = [
      {
        department: "行政部",
        userCount: 12,
        activeCount: 10,
        borrowCount: 89,
        avgBorrowPerUser: 7.4,
      },
      {
        department: "财务部",
        userCount: 8,
        activeCount: 7,
        borrowCount: 67,
        avgBorrowPerUser: 8.4,
      },
      {
        department: "人事部",
        userCount: 15,
        activeCount: 12,
        borrowCount: 134,
        avgBorrowPerUser: 8.9,
      },
      {
        department: "技术部",
        userCount: 25,
        activeCount: 20,
        borrowCount: 245,
        avgBorrowPerUser: 9.8,
      },
      {
        department: "市场部",
        userCount: 18,
        activeCount: 14,
        borrowCount: 156,
        avgBorrowPerUser: 8.7,
      },
    ];
  } catch (error) {
    ElMessage.error("加载表格数据失败");
  } finally {
    loading.value = false;
  }
};

const initTrendChart = () => {
  if (!trendChartRef.value) return;

  trendChart = echarts.init(trendChartRef.value);

  const option = {
    title: {
      text: "数据趋势",
      left: "center",
      textStyle: {
        fontSize: 16,
        color: "#333",
      },
    },
    tooltip: {
      trigger: "axis",
    },
    legend: {
      data: ["档案数量", "借阅次数", "用户数量"],
      bottom: 10,
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: [
        "1月",
        "2月",
        "3月",
        "4月",
        "5月",
        "6月",
        "7月",
        "8月",
        "9月",
        "10月",
        "11月",
        "12月",
      ],
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        name: "档案数量",
        type: "line",
        data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330],
        smooth: true,
        itemStyle: {
          color: "#5470c6",
        },
      },
      {
        name: "借阅次数",
        type: "line",
        data: [220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149],
        smooth: true,
        itemStyle: {
          color: "#91cc75",
        },
      },
      {
        name: "用户数量",
        type: "line",
        data: [150, 232, 201, 154, 190, 330, 410, 320, 328, 374, 390, 420],
        smooth: true,
        itemStyle: {
          color: "#fac858",
        },
      },
    ],
  };

  trendChart.setOption(option);
};

const initPieChart = () => {
  if (!pieChartRef.value) return;

  pieChart = echarts.init(pieChartRef.value);

  const option = {
    title: {
      text: "档案分类分布",
      left: "center",
      textStyle: {
        fontSize: 16,
        color: "#333",
      },
    },
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} ({d}%)",
    },
    legend: {
      orient: "vertical",
      left: "left",
    },
    series: [
      {
        name: "档案分类",
        type: "pie",
        radius: "50%",
        data: [
          { value: 298, name: "财务档案" },
          { value: 234, name: "技术文档" },
          { value: 187, name: "人事档案" },
          { value: 156, name: "法律文件" },
          { value: 89, name: "管理制度" },
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

  pieChart.setOption(option);
};

const handleDateChange = () => {
  refreshData();
};

const handleTypeChange = () => {
  refreshData();
};

const refreshData = async () => {
  await Promise.all([loadOverviewData(), loadRankingData(), loadTableData()]);

  // 重新初始化图表
  nextTick(() => {
    initTrendChart();
    initPieChart();
  });
};

const exportReport = () => {
  ElMessage.info("导出功能开发中...");
};

const formatDate = (date: Date) => {
  return format(date, "MM-dd");
};

// 生命周期
onMounted(async () => {
  // 设置默认时间范围（最近30天）
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(startDate.getDate() - 30);

  filterForm.dateRange = [
    format(startDate, "yyyy-MM-dd"),
    format(endDate, "yyyy-MM-dd"),
  ];

  await refreshData();

  // 初始化图表
  nextTick(() => {
    initTrendChart();
    initPieChart();
  });

  // 监听窗口大小变化
  window.addEventListener("resize", () => {
    trendChart?.resize();
    pieChart?.resize();
  });
});
</script>

<style lang="scss" scoped>
.reports-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .page-description {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .filter-section {
    margin-bottom: 20px;
  }

  .overview-section {
    margin-bottom: 20px;

    .overview-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      height: 100px;

      .card-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        font-size: 24px;
        color: white;

        &.archive {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.borrow {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.user {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.active {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
      }

      .card-content {
        flex: 1;

        .card-number {
          font-size: 28px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
        }

        .card-label {
          font-size: 14px;
          color: #909399;
          margin: 4px 0;
        }

        .card-trend {
          font-size: 12px;
          display: flex;
          align-items: center;
          gap: 4px;

          &.up {
            color: #67c23a;
          }

          &.down {
            color: #f56c6c;
          }
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 20px;

    .chart-container {
      height: 300px;
    }

    .ranking-list {
      .ranking-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .ranking-number {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          color: white;
          margin-right: 12px;

          &.rank-1 {
            background: #ffd700;
          }

          &.rank-2 {
            background: #c0c0c0;
          }

          &.rank-3 {
            background: #cd7f32;
          }

          &:not(.rank-1):not(.rank-2):not(.rank-3) {
            background: #909399;
          }
        }

        .ranking-content {
          flex: 1;

          .ranking-title {
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .ranking-meta {
            font-size: 12px;
            color: #909399;

            span {
              margin-right: 12px;
            }
          }
        }

        .ranking-score {
          font-size: 18px;
          font-weight: 600;
          color: #409eff;
        }
      }
    }
  }

  .table-section {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .reports-page {
    padding: 10px;

    .overview-section {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .charts-section {
      :deep(.el-col) {
        margin-bottom: 20px;
      }

      .chart-container {
        height: 250px;
      }
    }
  }
}
</style>
