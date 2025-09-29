<template>
  <div class="statistics-overview">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>统计概览</h1>
      <p>档案管理系统数据统计与分析</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="metrics-cards">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-number">{{ archiveStats.totalCount.toLocaleString() }}</div>
            <div class="metric-label">档案总数</div>
            <div class="metric-trend">
              <el-icon class="trend-icon up"><ArrowUp /></el-icon>
              <span>+{{ archiveStats.todayCount }}今日新增</span>
            </div>
          </div>
          <el-icon class="metric-icon primary"><Document /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-number">{{ archiveStats.borrowedCount.toLocaleString() }}</div>
            <div class="metric-label">借阅总数</div>
            <div class="metric-trend">
              <el-icon class="trend-icon up"><ArrowUp /></el-icon>
              <span>+{{ archiveStats.weekCount }}本周</span>
            </div>
          </div>
          <el-icon class="metric-icon success"><Reading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-number">{{ userActivityData.activeUsers }}</div>
            <div class="metric-label">活跃用户</div>
            <div class="metric-trend">
              <el-icon class="trend-icon up"><ArrowUp /></el-icon>
              <span>{{ userActivityData.totalUsers }}总用户</span>
            </div>
          </div>
          <el-icon class="metric-icon warning"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-number">{{ archiveStats.overdueCount }}</div>
            <div class="metric-label">逾期档案</div>
            <div class="metric-trend">
              <el-icon class="trend-icon down"><ArrowDown /></el-icon>
              <span>{{ archiveStats.reservedCount }}预约中</span>
            </div>
          </div>
          <el-icon class="metric-icon danger"><Warning /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>档案增长趋势图</span>
              <el-select v-model="archivePeriod" size="small">
                <el-option label="最近7天" value="7d" />
                <el-option label="最近30天" value="30d" />
                <el-option label="最近90天" value="90d" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <ArchiveTrendChart 
              v-if="borrowTrendData.dates.length > 0"
              :data="borrowTrendData" 
            />
            <div v-else class="chart-placeholder">
              <el-icon><Document /></el-icon>
              <p>暂无数据</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>借阅活跃度</span>
              <el-select v-model="borrowPeriod" size="small">
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
                <el-option label="本季度" value="quarter" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <BorrowStatChart 
              v-if="userActivityData.hourlyActivity.length > 0"
              :data="userActivityData" 
            />
            <div v-else class="chart-placeholder">
              <el-icon><Reading /></el-icon>
              <p>暂无数据</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜和快捷操作 -->
    <div class="bottom-section">
      <div class="ranking-section">
        <h3>热门档案排行</h3>
        <div class="ranking-list">
          <div v-for="(item, index) in hotArchives" :key="item.id" class="ranking-item">
            <div class="ranking-number">{{ index + 1 }}</div>
            <div class="ranking-content">
              <div class="ranking-title">{{ item.title }}</div>
              <div class="ranking-count">借阅次数: {{ item.count }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="top-users-section">
        <h3>活跃用户排行</h3>
        <div class="users-list">
          <div v-for="(user, index) in topUsers" :key="user.id" class="user-item">
            <div class="user-rank">{{ index + 1 }}</div>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-count">借阅: {{ user.borrowCount }}次</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <el-button type="primary" @click="exportReport">
        <el-icon><Download /></el-icon>
        导出报告
      </el-button>
      <el-button @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
      <el-button @click="viewDetails">查看详情</el-button>
      <el-button @click="generateReport">生成报告</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  Reading, 
  User, 
  Warning, 
  ArrowUp, 
  ArrowDown, 
  Download, 
  Refresh 
} from '@element-plus/icons-vue'
import { StatisticsAPI } from '@/api/statistics'
import type { ArchiveStatistics, BorrowTrend, UserActivity } from '@/api/statistics'
import ArchiveTrendChart from '@/components/charts/ArchiveTrendChart.vue'
import BorrowStatChart from '@/components/charts/BorrowStatChart.vue'

// 响应式数据
const archivePeriod = ref('30d')
const borrowPeriod = ref('week')
const loading = ref(false)

// 统计数据
const archiveStats = ref<ArchiveStatistics>({
  totalCount: 0,
  todayCount: 0,
  weekCount: 0,
  monthCount: 0,
  borrowedCount: 0,
  availableCount: 0,
  overdueCount: 0,
  reservedCount: 0
})

const borrowTrendData = ref<BorrowTrend>({
  dates: [],
  borrowCounts: [],
  returnCounts: []
})

const userActivityData = ref<UserActivity>({
  activeUsers: 0,
  totalUsers: 0,
  loginCount: 0,
  operationCount: 0,
  hourlyActivity: []
})

const userStats = computed(() => userActivityData.value)

// 模拟排行榜数据（这些可以后续通过API获取）
const topUsers = ref([
  { id: 1, name: '张三', borrowCount: 25 },
  { id: 2, name: '李四', borrowCount: 18 },
  { id: 3, name: '王五', borrowCount: 15 },
  { id: 4, name: '赵六', borrowCount: 12 },
  { id: 5, name: '钱七', borrowCount: 10 }
])

const hotArchives = ref([
  { id: 1, title: '公司章程修订案', count: 45 },
  { id: 2, title: '财务报表2024', count: 38 },
  { id: 3, title: '员工手册2025版', count: 32 },
  { id: 4, title: '合同模板集', count: 28 },
  { id: 5, title: '项目管理规范', count: 25 }
])

// 加载统计数据
const loadStatisticsData = async () => {
  try {
    loading.value = true
    
    // 并行获取所有统计数据
    const [archiveData, borrowData, activityData, recentActivities, todoItems] = await Promise.all([
      StatisticsAPI.getArchiveCount(),
      StatisticsAPI.getBorrowTrend(),
      StatisticsAPI.getUserActivity(),
      StatisticsAPI.getRecentActivities(),
      StatisticsAPI.getTodoItems()
    ])
    
    archiveStats.value = archiveData
    borrowTrendData.value = borrowData
    userActivityData.value = activityData
    
    console.log('最近活动数据:', recentActivities)
    console.log('待办事项数据:', todoItems)
    
    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 操作方法
const exportReport = () => {
  ElMessage.success('报告导出功能开发中...')
}

const refreshData = async () => {
  await loadStatisticsData()
}

const viewDetails = () => {
  ElMessage.info('详情页面开发中...')
}

const generateReport = () => {
  ElMessage.success('报告生成功能开发中...')
}

// 组件挂载时加载数据
onMounted(() => {
  loadStatisticsData()
})
</script>

<style lang="scss" scoped>
.statistics-overview {
  padding: $spacing-lg;
}

.page-header {
  margin-bottom: $spacing-lg;

  h2 {
    margin: 0 0 $spacing-sm 0;
    color: $text-primary;
  }

  p {
    margin: 0;
    color: $text-secondary;
  }
}

.metrics-cards {
  margin-bottom: $spacing-lg;
}

.metric-card {
  position: relative;

  .metric-content {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .metric-number {
    font-size: 2.5rem;
    font-weight: bold;
    color: $text-primary;
    line-height: 1;
  }

  .metric-label {
    color: $text-secondary;
    margin: $spacing-xs 0;
    font-size: 0.9rem;
  }

  .metric-trend {
    display: flex;
    align-items: center;
    font-size: 0.8rem;

    .trend-icon {
      margin-right: $spacing-xs;

      &.up {
        color: $color-success;
      }
      &.down {
        color: $color-danger;
      }
    }
  }

  .metric-icon {
    position: absolute;
    right: $spacing-lg;
    top: 50%;
    transform: translateY(-50%);
    font-size: 2.5rem;
    opacity: 0.3;

    &.primary {
      color: $color-primary;
    }
    &.success {
      color: $color-success;
    }
    &.warning {
      color: $color-warning;
    }
    &.danger {
      color: $color-danger;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: $text-secondary;
  font-size: 1.2rem;

  p {
    margin-top: $spacing-sm;
    font-size: 0.9rem;
    color: $text-tertiary;
  }
}

.ranking-list {
  .ranking-item {
    display: flex;
    align-items: center;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .rank-number {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.8rem;
    font-weight: bold;
    margin-right: $spacing-sm;

    &.rank-1 {
      background: #ffd700;
      color: white;
    }
    &.rank-2 {
      background: #c0c0c0;
      color: white;
    }
    &.rank-3 {
      background: #cd7f32;
      color: white;
    }

    &:not(.rank-1):not(.rank-2):not(.rank-3) {
      background: $bg-light;
      color: $text-secondary;
    }
  }

  .user-info,
  .archive-info {
    flex: 1;

    .user-name,
    .archive-title {
      font-weight: 500;
      color: $text-primary;
    }

    .user-count,
    .archive-count {
      font-size: 0.8rem;
      color: $text-secondary;
      margin-top: 2px;
    }
  }
}

.quick-actions {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}
</style>
