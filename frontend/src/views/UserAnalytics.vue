<template>
  <div class="user-analytics">
    <div class="page-header">
      <h1>用户行为分析</h1>
      <p class="page-description">分析用户在系统中的行为模式和使用习惯</p>
    </div>
    
    <div class="analytics-content">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="analytics-card">
            <template #header>
              <div class="card-header">
                <span>用户活跃度</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="placeholder-chart">
                <el-icon size="48" color="#409EFF">
                  <DataAnalysis />
                </el-icon>
                <p>用户活跃度图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="analytics-card">
            <template #header>
              <div class="card-header">
                <span>功能使用统计</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="placeholder-chart">
                <el-icon size="48" color="#67C23A">
                  <PieChart />
                </el-icon>
                <p>功能使用分布图</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card class="analytics-card">
            <template #header>
              <div class="card-header">
                <span>用户行为趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <div class="placeholder-chart">
                <el-icon size="48" color="#E6A23C">
                  <TrendCharts />
                </el-icon>
                <p>用户行为趋势分析</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { DataAnalysis, PieChart, TrendCharts } from '@element-plus/icons-vue'

// 页面数据
const loading = ref(false)

// 生命周期
onMounted(() => {
  loadAnalyticsData()
})

// 加载分析数据
const loadAnalyticsData = async () => {
  loading.value = true
  try {
    // TODO: 实现用户行为分析数据加载
    console.log('加载用户行为分析数据')
  } catch (error) {
    console.error('加载用户行为分析数据失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.user-analytics {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.analytics-content {
  background: #fff;
}

.analytics-card {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.chart-container {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-chart {
  text-align: center;
  color: #909399;
}

.placeholder-chart p {
  margin: 12px 0 0 0;
  font-size: 14px;
}
</style>