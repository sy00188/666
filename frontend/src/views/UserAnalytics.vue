<template>
  <div class="user-analytics">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户行为分析</h1>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
        />
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button 
          type="primary" 
          :icon="Download" 
          @click="showExportDialog = true"
        >
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="overview-cards">
      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon active-users">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-info">
            <h3>{{ overviewData.activeUsers }}</h3>
            <p>活跃用户</p>
            <span class="trend" :class="overviewData.activeUsersTrend > 0 ? 'up' : 'down'">
              {{ overviewData.activeUsersTrend > 0 ? '+' : '' }}{{ overviewData.activeUsersTrend }}%
            </span>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon total-borrows">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="card-info">
            <h3>{{ overviewData.totalBorrows }}</h3>
            <p>总借阅量</p>
            <span class="trend" :class="overviewData.totalBorrowsTrend > 0 ? 'up' : 'down'">
              {{ overviewData.totalBorrowsTrend > 0 ? '+' : '' }}{{ overviewData.totalBorrowsTrend }}%
            </span>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon avg-duration">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="card-info">
            <h3>{{ overviewData.avgBorrowDuration }}</h3>
            <p>平均借阅天数</p>
            <span class="trend" :class="overviewData.avgDurationTrend > 0 ? 'down' : 'up'">
              {{ overviewData.avgDurationTrend > 0 ? '+' : '' }}{{ overviewData.avgDurationTrend }}%
            </span>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon return-rate">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="card-info">
            <h3>{{ overviewData.returnRate }}%</h3>
            <p>按时归还率</p>
            <span class="trend" :class="overviewData.returnRateTrend > 0 ? 'up' : 'down'">
              {{ overviewData.returnRateTrend > 0 ? '+' : '' }}{{ overviewData.returnRateTrend }}%
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 用户活跃度趋势 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>用户活跃度趋势</span>
                <el-select v-model="activityPeriod" size="small" @change="updateActivityChart">
                  <el-option label="最近7天" value="7d" />
                  <el-option label="最近30天" value="30d" />
                  <el-option label="最近90天" value="90d" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="activityChartOption" 
                :loading="activityChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>

        <!-- 借阅模式分析 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>借阅模式分析</span>
                <el-radio-group v-model="borrowPatternType" size="small" @change="updateBorrowPatternChart">
                  <el-radio-button label="category">分类</el-radio-button>
                  <el-radio-button label="time">时间</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="borrowPatternChartOption" 
                :loading="borrowPatternChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 热门图书排行 -->
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>热门图书排行</span>
                <div class="header-controls">
                  <el-select v-model="popularBooksType" size="small" @change="updatePopularBooksChart">
                    <el-option label="借阅次数" value="borrow_count" />
                    <el-option label="预约次数" value="reservation_count" />
                    <el-option label="评分" value="rating" />
                  </el-select>
                  <el-input-number 
                    v-model="popularBooksLimit" 
                    :min="5" 
                    :max="20" 
                    size="small"
                    @change="updatePopularBooksChart"
                  />
                </div>
              </div>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="popularBooksChartOption" 
                :loading="popularBooksChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>

        <!-- 用户类型分布 -->
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>用户类型分布</span>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="userTypeChartOption" 
                :loading="userTypeChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <div class="data-tables">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 活跃用户列表 -->
        <el-tab-pane label="活跃用户" name="active-users">
          <el-table 
            :data="activeUsersData" 
            :loading="activeUsersLoading"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="userType" label="用户类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getUserTypeTagType(row.userType)">
                  {{ getUserTypeLabel(row.userType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="borrowCount" label="借阅次数" width="100" />
            <el-table-column prop="returnRate" label="归还率" width="100">
              <template #default="{ row }">
                <span :class="row.returnRate >= 90 ? 'text-success' : row.returnRate >= 70 ? 'text-warning' : 'text-danger'">
                  {{ row.returnRate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="lastActiveTime" label="最后活跃时间" width="150" />
            <el-table-column prop="totalScore" label="活跃度评分" width="120">
              <template #default="{ row }">
                <el-progress 
                  :percentage="row.totalScore" 
                  :color="getScoreColor(row.totalScore)"
                  :show-text="false"
                  style="width: 80px;"
                />
                <span style="margin-left: 10px;">{{ row.totalScore }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="viewUserDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="activeUsersPage"
              v-model:page-size="activeUsersPageSize"
              :total="activeUsersTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleActiveUsersPageSizeChange"
              @current-change="handleActiveUsersPageChange"
            />
          </div>
        </el-tab-pane>

        <!-- 借阅统计 -->
        <el-tab-pane label="借阅统计" name="borrow-stats">
          <el-table 
            :data="borrowStatsData" 
            :loading="borrowStatsLoading"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="totalBorrows" label="总借阅量" width="100" />
            <el-table-column prop="newBorrows" label="新增借阅" width="100" />
            <el-table-column prop="returns" label="归还数量" width="100" />
            <el-table-column prop="overdue" label="逾期数量" width="100">
              <template #default="{ row }">
                <span :class="row.overdue > 0 ? 'text-danger' : 'text-success'">
                  {{ row.overdue }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="renewals" label="续借数量" width="100" />
            <el-table-column prop="avgDuration" label="平均借阅天数" width="120" />
            <el-table-column prop="popularCategory" label="热门分类" width="120" />
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="borrowStatsPage"
              v-model:page-size="borrowStatsPageSize"
              :total="borrowStatsTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleBorrowStatsPageSizeChange"
              @current-change="handleBorrowStatsPageChange"
            />
          </div>
        </el-tab-pane>

        <!-- 图书热度 -->
        <el-tab-pane label="图书热度" name="book-popularity">
          <el-table 
            :data="bookPopularityData" 
            :loading="bookPopularityLoading"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="title" label="书名" width="200" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="borrowCount" label="借阅次数" width="100" />
            <el-table-column prop="reservationCount" label="预约次数" width="100" />
            <el-table-column prop="rating" label="评分" width="100">
              <template #default="{ row }">
                <el-rate 
                  v-model="row.rating" 
                  disabled 
                  show-score 
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </template>
            </el-table-column>
            <el-table-column prop="availability" label="可借数量" width="100">
              <template #default="{ row }">
                <span :class="row.availability > 0 ? 'text-success' : 'text-danger'">
                  {{ row.availability }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="viewBookDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="table-pagination">
            <el-pagination
              v-model:current-page="bookPopularityPage"
              v-model:page-size="bookPopularityPageSize"
              :total="bookPopularityTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleBookPopularityPageSizeChange"
              @current-change="handleBookPopularityPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 导出对话框 -->
    <ExportDialog
      v-model="showExportDialog"
      :data="exportData"
      :headers="exportHeaders"
      :title="'用户行为分析报表'"
      :available-charts="availableCharts"
      :on-export="handleExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  Reading, 
  Timer, 
  CircleCheck, 
  Refresh, 
  Download 
} from '@element-plus/icons-vue'
import ExportDialog from '@/components/ExportDialog.vue'
import { exportData } from '@/utils/exportUtils'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { 
  LineChart, 
  BarChart, 
  PieChart 
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

// 响应式数据
const dateRange = ref([])
const activeTab = ref('active-users')
const showExportDialog = ref(false)

// 导出相关数据
const exportData = computed(() => {
  switch (activeTab.value) {
    case 'active-users':
      return activeUsersData.value
    case 'borrow-stats':
      return borrowStatsData.value
    case 'book-popularity':
      return bookPopularityData.value
    default:
      return []
  }
})

const exportHeaders = computed(() => {
  switch (activeTab.value) {
    case 'active-users':
      return [
        { key: 'rank', label: '排名' },
        { key: 'username', label: '用户名' },
        { key: 'name', label: '姓名' },
        { key: 'userType', label: '用户类型' },
        { key: 'borrowCount', label: '借阅次数' },
        { key: 'returnRate', label: '归还率' },
        { key: 'lastActiveTime', label: '最后活跃时间' },
        { key: 'totalScore', label: '活跃度评分' }
      ]
    case 'borrow-stats':
      return [
        { key: 'date', label: '日期' },
        { key: 'totalBorrows', label: '总借阅量' },
        { key: 'newBorrows', label: '新增借阅' },
        { key: 'returns', label: '归还数量' },
        { key: 'overdue', label: '逾期数量' },
        { key: 'renewals', label: '续借数量' },
        { key: 'avgDuration', label: '平均借阅天数' },
        { key: 'popularCategory', label: '热门分类' }
      ]
    case 'book-popularity':
      return [
        { key: 'rank', label: '排名' },
        { key: 'title', label: '书名' },
        { key: 'author', label: '作者' },
        { key: 'category', label: '分类' },
        { key: 'borrowCount', label: '借阅次数' },
        { key: 'reservationCount', label: '预约次数' },
        { key: 'rating', label: '评分' },
        { key: 'availability', label: '可借数量' }
      ]
    default:
      return []
  }
})

const availableCharts = computed(() => [
  { key: 'activity', label: '用户活跃度趋势', option: activityChartOption.value },
  { key: 'borrowPattern', label: '借阅模式分析', option: borrowPatternChartOption.value },
  { key: 'popularBooks', label: '热门图书排行', option: popularBooksChartOption.value },
  { key: 'userType', label: '用户类型分布', option: userTypeChartOption.value }
])

// 概览数据
const overviewData = reactive({
  activeUsers: 1248,
  activeUsersTrend: 12.5,
  totalBorrows: 3567,
  totalBorrowsTrend: 8.3,
  avgBorrowDuration: 14,
  avgDurationTrend: -2.1,
  returnRate: 92.5,
  returnRateTrend: 3.2
})

// 图表控制
const activityPeriod = ref('30d')
const borrowPatternType = ref('category')
const popularBooksType = ref('borrow_count')
const popularBooksLimit = ref(10)

// 图表加载状态
const activityChartLoading = ref(false)
const borrowPatternChartLoading = ref(false)
const popularBooksChartLoading = ref(false)
const userTypeChartLoading = ref(false)

// 表格数据和分页
const activeUsersData = ref([])
const activeUsersLoading = ref(false)
const activeUsersPage = ref(1)
const activeUsersPageSize = ref(20)
const activeUsersTotal = ref(0)

const borrowStatsData = ref([])
const borrowStatsLoading = ref(false)
const borrowStatsPage = ref(1)
const borrowStatsPageSize = ref(20)
const borrowStatsTotal = ref(0)

const bookPopularityData = ref([])
const bookPopularityLoading = ref(false)
const bookPopularityPage = ref(1)
const bookPopularityPageSize = ref(20)
const bookPopularityTotal = ref(0)

// 图表配置
const activityChartOption = computed(() => ({
  title: {
    text: '用户活跃度趋势',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['活跃用户数', '新注册用户'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: generateDateRange(activityPeriod.value)
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '活跃用户数',
      type: 'line',
      smooth: true,
      data: generateActivityData(activityPeriod.value),
      itemStyle: {
        color: '#409EFF'
      }
    },
    {
      name: '新注册用户',
      type: 'line',
      smooth: true,
      data: generateNewUserData(activityPeriod.value),
      itemStyle: {
        color: '#67C23A'
      }
    }
  ]
}))

const borrowPatternChartOption = computed(() => {
  if (borrowPatternType.value === 'category') {
    return {
      title: {
        text: '借阅分类分布',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '借阅分类',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: 335, name: '文学' },
            { value: 310, name: '科技' },
            { value: 234, name: '历史' },
            { value: 135, name: '艺术' },
            { value: 154, name: '其他' }
          ]
        }
      ]
    }
  } else {
    return {
      title: {
        text: '借阅时间分布',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['00-02', '02-04', '04-06', '06-08', '08-10', '10-12', '12-14', '14-16', '16-18', '18-20', '20-22', '22-24']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '借阅次数',
          type: 'bar',
          data: [12, 8, 15, 45, 78, 125, 156, 189, 167, 145, 98, 56],
          itemStyle: {
            color: '#E6A23C'
          }
        }
      ]
    }
  }
})

const popularBooksChartOption = computed(() => ({
  title: {
    text: `热门图书排行 (Top ${popularBooksLimit.value})`,
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    type: 'category',
    data: generatePopularBooksData(popularBooksLimit.value).map(item => item.name),
    axisLabel: {
      interval: 0,
      formatter: function(value) {
        return value.length > 10 ? value.substring(0, 10) + '...' : value
      }
    }
  },
  series: [
    {
      name: getPopularBooksSeriesName(popularBooksType.value),
      type: 'bar',
      data: generatePopularBooksData(popularBooksLimit.value).map(item => item.value),
      itemStyle: {
        color: '#F56C6C'
      }
    }
  ]
}))

const userTypeChartOption = computed(() => ({
  title: {
    text: '用户类型分布',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  series: [
    {
      name: '用户类型',
      type: 'pie',
      radius: '70%',
      data: [
        { value: 456, name: '学生' },
        { value: 234, name: '教师' },
        { value: 123, name: '职工' },
        { value: 89, name: '访客' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
}))

// 方法
const handleDateRangeChange = (dates) => {
  console.log('日期范围变更:', dates)
  refreshData()
}

const refreshData = async () => {
  ElMessage.success('数据刷新成功')
  await loadAllData()
}

const handleExport = async (exportConfig) => {
  try {
    const { format, includeCharts, selectedCharts, filename } = exportConfig
    
    // 准备导出数据
    const dataToExport = {
      overview: overviewData,
      tableData: exportData.value,
      headers: exportHeaders.value,
      charts: includeCharts ? selectedCharts.map(chartKey => 
        availableCharts.value.find(chart => chart.key === chartKey)
      ).filter(Boolean) : [],
      dateRange: dateRange.value,
      exportTime: new Date().toLocaleString('zh-CN')
    }
    
    // 调用导出工具函数
    await exportData({
      data: dataToExport,
      format,
      filename: filename || `用户行为分析报表_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '')}`
    })
    
    ElMessage.success('报表导出成功')
    showExportDialog.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}

const updateActivityChart = () => {
  activityChartLoading.value = true
  setTimeout(() => {
    activityChartLoading.value = false
  }, 1000)
}

const updateBorrowPatternChart = () => {
  borrowPatternChartLoading.value = true
  setTimeout(() => {
    borrowPatternChartLoading.value = false
  }, 1000)
}

const updatePopularBooksChart = () => {
  popularBooksChartLoading.value = true
  setTimeout(() => {
    popularBooksChartLoading.value = false
  }, 1000)
}

const handleTabChange = (tabName) => {
  console.log('切换标签页:', tabName)
  switch (tabName) {
    case 'active-users':
      loadActiveUsersData()
      break
    case 'borrow-stats':
      loadBorrowStatsData()
      break
    case 'book-popularity':
      loadBookPopularityData()
      break
  }
}

// 表格分页处理
const handleActiveUsersPageSizeChange = (size) => {
  activeUsersPageSize.value = size
  loadActiveUsersData()
}

const handleActiveUsersPageChange = (page) => {
  activeUsersPage.value = page
  loadActiveUsersData()
}

const handleBorrowStatsPageSizeChange = (size) => {
  borrowStatsPageSize.value = size
  loadBorrowStatsData()
}

const handleBorrowStatsPageChange = (page) => {
  borrowStatsPage.value = page
  loadBorrowStatsData()
}

const handleBookPopularityPageSizeChange = (size) => {
  bookPopularityPageSize.value = size
  loadBookPopularityData()
}

const handleBookPopularityPageChange = (page) => {
  bookPopularityPage.value = page
  loadBookPopularityData()
}

// 工具方法
const getUserTypeTagType = (type) => {
  const typeMap = {
    'student': 'primary',
    'teacher': 'success',
    'staff': 'warning',
    'visitor': 'info'
  }
  return typeMap[type] || 'info'
}

const getUserTypeLabel = (type) => {
  const labelMap = {
    'student': '学生',
    'teacher': '教师',
    'staff': '职工',
    'visitor': '访客'
  }
  return labelMap[type] || '未知'
}

const getScoreColor = (score) => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const viewUserDetail = (user) => {
  ElMessage.info(`查看用户详情: ${user.name}`)
}

const viewBookDetail = (book) => {
  ElMessage.info(`查看图书详情: ${book.title}`)
}

// 数据生成方法
const generateDateRange = (period) => {
  const days = period === '7d' ? 7 : period === '30d' ? 30 : 90
  const dates = []
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
  }
  return dates
}

const generateActivityData = (period) => {
  const days = period === '7d' ? 7 : period === '30d' ? 30 : 90
  return Array.from({ length: days }, () => Math.floor(Math.random() * 200) + 50)
}

const generateNewUserData = (period) => {
  const days = period === '7d' ? 7 : period === '30d' ? 30 : 90
  return Array.from({ length: days }, () => Math.floor(Math.random() * 50) + 5)
}

const generatePopularBooksData = (limit) => {
  const books = [
    '《三体》', '《活着》', '《百年孤独》', '《1984》', '《红楼梦》',
    '《西游记》', '《水浒传》', '《三国演义》', '《围城》', '《平凡的世界》',
    '《白夜行》', '《解忧杂货店》', '《小王子》', '《追风筝的人》', '《挪威的森林》',
    '《哈利波特》', '《指环王》', '《权力的游戏》', '《达芬奇密码》', '《时间简史》'
  ]
  
  return books.slice(0, limit).map((name, index) => ({
    name,
    value: Math.floor(Math.random() * 500) + 100 - index * 10
  })).sort((a, b) => b.value - a.value)
}

const getPopularBooksSeriesName = (type) => {
  const nameMap = {
    'borrow_count': '借阅次数',
    'reservation_count': '预约次数',
    'rating': '评分'
  }
  return nameMap[type] || '借阅次数'
}

// 数据加载方法
const loadActiveUsersData = async () => {
  activeUsersLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const mockData = Array.from({ length: activeUsersPageSize.value }, (_, index) => ({
      rank: (activeUsersPage.value - 1) * activeUsersPageSize.value + index + 1,
      username: `user${index + 1}`,
      name: `用户${index + 1}`,
      userType: ['student', 'teacher', 'staff', 'visitor'][Math.floor(Math.random() * 4)],
      borrowCount: Math.floor(Math.random() * 50) + 10,
      returnRate: Math.floor(Math.random() * 30) + 70,
      lastActiveTime: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toLocaleDateString(),
      totalScore: Math.floor(Math.random() * 40) + 60
    }))
    
    activeUsersData.value = mockData
    activeUsersTotal.value = 1248
  } catch (error) {
    ElMessage.error('加载活跃用户数据失败')
  } finally {
    activeUsersLoading.value = false
  }
}

const loadBorrowStatsData = async () => {
  borrowStatsLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const mockData = Array.from({ length: borrowStatsPageSize.value }, (_, index) => {
      const date = new Date()
      date.setDate(date.getDate() - index)
      return {
        date: date.toLocaleDateString(),
        totalBorrows: Math.floor(Math.random() * 100) + 50,
        newBorrows: Math.floor(Math.random() * 30) + 10,
        returns: Math.floor(Math.random() * 80) + 40,
        overdue: Math.floor(Math.random() * 10),
        renewals: Math.floor(Math.random() * 20) + 5,
        avgDuration: Math.floor(Math.random() * 10) + 10,
        popularCategory: ['文学', '科技', '历史', '艺术'][Math.floor(Math.random() * 4)]
      }
    })
    
    borrowStatsData.value = mockData
    borrowStatsTotal.value = 365
  } catch (error) {
    ElMessage.error('加载借阅统计数据失败')
  } finally {
    borrowStatsLoading.value = false
  }
}

const loadBookPopularityData = async () => {
  bookPopularityLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const books = [
      '《三体》', '《活着》', '《百年孤独》', '《1984》', '《红楼梦》',
      '《西游记》', '《水浒传》', '《三国演义》', '《围城》', '《平凡的世界》'
    ]
    
    const authors = [
      '刘慈欣', '余华', '马尔克斯', '乔治·奥威尔', '曹雪芹',
      '吴承恩', '施耐庵', '罗贯中', '钱钟书', '路遥'
    ]
    
    const categories = ['科幻', '文学', '文学', '政治', '古典文学', '古典文学', '古典文学', '古典文学', '文学', '文学']
    
    const mockData = Array.from({ length: Math.min(bookPopularityPageSize.value, books.length) }, (_, index) => ({
      rank: (bookPopularityPage.value - 1) * bookPopularityPageSize.value + index + 1,
      title: books[index],
      author: authors[index],
      category: categories[index],
      borrowCount: Math.floor(Math.random() * 200) + 50 - index * 5,
      reservationCount: Math.floor(Math.random() * 50) + 10 - index * 2,
      rating: (Math.random() * 2 + 3).toFixed(1),
      availability: Math.floor(Math.random() * 5) + 1
    }))
    
    bookPopularityData.value = mockData
    bookPopularityTotal.value = 500
  } catch (error) {
    ElMessage.error('加载图书热度数据失败')
  } finally {
    bookPopularityLoading.value = false
  }
}

const loadAllData = async () => {
  await Promise.all([
    loadActiveUsersData(),
    loadBorrowStatsData(),
    loadBookPopularityData()
  ])
}

// 生命周期
onMounted(() => {
  // 设置默认日期范围为最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  loadAllData()
})
</script>

<style scoped>
.user-analytics {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.overview-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.overview-card:hover {
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.card-icon.active-users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.total-borrows {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.avg-duration {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.return-rate {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-info h3 {
  margin: 0 0 4px 0;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.card-info p {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 14px;
}

.trend {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
}

.trend.up {
  color: #67C23A;
  background-color: #f0f9ff;
}

.trend.down {
  color: #F56C6C;
  background-color: #fef0f0;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.header-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chart-container {
  height: 350px;
  padding: 10px 0;
}

.chart {
  width: 100%;
  height: 100%;
}

.data-tables {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.table-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.text-success {
  color: #67C23A;
}

.text-warning {
  color: #E6A23C;
}

.text-danger {
  color: #F56C6C;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-analytics {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .overview-cards {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>