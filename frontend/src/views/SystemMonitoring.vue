<template>
  <div class="system-monitoring">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">系统监控</h1>
      <div class="header-actions">
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          inactive-text="手动刷新"
          @change="handleAutoRefreshChange"
        />
        <el-select v-model="refreshInterval" size="default" style="width: 120px;" @change="updateRefreshInterval">
          <el-option label="5秒" :value="5000" />
          <el-option label="10秒" :value="10000" />
          <el-option label="30秒" :value="30000" />
          <el-option label="1分钟" :value="60000" />
        </el-select>
        <el-button type="primary" @click="refreshAllData" :loading="isRefreshing">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button 
          @click="showExportDialog = true"
        >
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 系统状态概览 -->
    <div class="status-overview">
      <el-card class="status-card" :class="{ 'status-healthy': systemStatus.overall === 'healthy', 'status-warning': systemStatus.overall === 'warning', 'status-critical': systemStatus.overall === 'critical' }">
        <div class="status-content">
          <div class="status-icon">
            <el-icon v-if="systemStatus.overall === 'healthy'"><CircleCheck /></el-icon>
            <el-icon v-else-if="systemStatus.overall === 'warning'"><Warning /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
          </div>
          <div class="status-info">
            <h2>{{ getSystemStatusText(systemStatus.overall) }}</h2>
            <p>最后更新: {{ systemStatus.lastUpdate }}</p>
            <div class="status-details">
              <span class="detail-item">
                <el-icon><Monitor /></el-icon>
                服务: {{ systemStatus.services.running }}/{{ systemStatus.services.total }}
              </span>
              <span class="detail-item">
                <el-icon><Connection /></el-icon>
                连接: {{ systemStatus.connections.active }}
              </span>
              <span class="detail-item">
                <el-icon><Timer /></el-icon>
                运行时间: {{ systemStatus.uptime }}
              </span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 关键指标卡片 -->
    <div class="metrics-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon cpu">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="metric-info">
                <h3>{{ metricsData.cpu.usage }}%</h3>
                <p>CPU 使用率</p>
                <div class="metric-progress">
                  <el-progress 
                    :percentage="metricsData.cpu.usage" 
                    :color="getCpuColor(metricsData.cpu.usage)"
                    :show-text="false"
                  />
                </div>
                <span class="metric-detail">负载: {{ metricsData.cpu.load }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon memory">
                <el-icon><Memo /></el-icon>
              </div>
              <div class="metric-info">
                <h3>{{ metricsData.memory.usage }}%</h3>
                <p>内存使用率</p>
                <div class="metric-progress">
                  <el-progress 
                    :percentage="metricsData.memory.usage" 
                    :color="getMemoryColor(metricsData.memory.usage)"
                    :show-text="false"
                  />
                </div>
                <span class="metric-detail">{{ metricsData.memory.used }}GB / {{ metricsData.memory.total }}GB</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon disk">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <div class="metric-info">
                <h3>{{ metricsData.disk.usage }}%</h3>
                <p>磁盘使用率</p>
                <div class="metric-progress">
                  <el-progress 
                    :percentage="metricsData.disk.usage" 
                    :color="getDiskColor(metricsData.disk.usage)"
                    :show-text="false"
                  />
                </div>
                <span class="metric-detail">{{ metricsData.disk.used }}GB / {{ metricsData.disk.total }}GB</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon network">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="metric-info">
                <h3>{{ metricsData.network.latency }}ms</h3>
                <p>网络延迟</p>
                <div class="metric-progress">
                  <el-progress 
                    :percentage="getNetworkPercentage(metricsData.network.latency)" 
                    :color="getNetworkColor(metricsData.network.latency)"
                    :show-text="false"
                  />
                </div>
                <span class="metric-detail">↑{{ metricsData.network.upload }}MB/s ↓{{ metricsData.network.download }}MB/s</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 性能图表 -->
    <div class="performance-charts">
      <el-row :gutter="20">
        <!-- 系统资源使用趋势 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>系统资源使用趋势</span>
                <el-select v-model="resourceTimeRange" size="small" @change="updateResourceChart">
                  <el-option label="最近1小时" value="1h" />
                  <el-option label="最近6小时" value="6h" />
                  <el-option label="最近24小时" value="24h" />
                  <el-option label="最近7天" value="7d" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="resourceChartOption" 
                :loading="resourceChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>

        <!-- 响应时间分析 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>API响应时间分析</span>
                <el-select v-model="responseTimeRange" size="small" @change="updateResponseTimeChart">
                  <el-option label="最近1小时" value="1h" />
                  <el-option label="最近6小时" value="6h" />
                  <el-option label="最近24小时" value="24h" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="responseTimeChartOption" 
                :loading="responseTimeChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 错误率统计 -->
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>错误率统计</span>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="errorRateChartOption" 
                :loading="errorRateChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>

        <!-- 并发连接数 -->
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>并发连接数</span>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="connectionChartOption" 
                :loading="connectionChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>

        <!-- 数据库性能 -->
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <span>数据库性能</span>
            </template>
            <div class="chart-container">
              <v-chart 
                class="chart" 
                :option="databaseChartOption" 
                :loading="databaseChartLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 服务状态和告警 -->
    <div class="services-alerts">
      <el-row :gutter="20">
        <!-- 服务状态 -->
        <el-col :span="12">
          <el-card class="service-card">
            <template #header>
              <div class="card-header">
                <span>服务状态</span>
                <el-button type="text" size="small" @click="refreshServiceStatus">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <div class="service-list">
              <div 
                v-for="service in servicesData" 
                :key="service.name" 
                class="service-item"
                :class="{ 'service-running': service.status === 'running', 'service-stopped': service.status === 'stopped', 'service-error': service.status === 'error' }"
              >
                <div class="service-info">
                  <div class="service-name">
                    <el-icon v-if="service.status === 'running'"><CircleCheck /></el-icon>
                    <el-icon v-else-if="service.status === 'stopped'"><Remove /></el-icon>
                    <el-icon v-else><CircleClose /></el-icon>
                    {{ service.name }}
                  </div>
                  <div class="service-details">
                    <span>端口: {{ service.port }}</span>
                    <span>CPU: {{ service.cpu }}%</span>
                    <span>内存: {{ service.memory }}MB</span>
                    <span>运行时间: {{ service.uptime }}</span>
                  </div>
                </div>
                <div class="service-actions">
                  <el-button 
                    v-if="service.status === 'stopped'" 
                    type="success" 
                    size="small" 
                    @click="startService(service.name)"
                  >
                    启动
                  </el-button>
                  <el-button 
                    v-else-if="service.status === 'running'" 
                    type="warning" 
                    size="small" 
                    @click="restartService(service.name)"
                  >
                    重启
                  </el-button>
                  <el-button 
                    v-if="service.status === 'running'" 
                    type="danger" 
                    size="small" 
                    @click="stopService(service.name)"
                  >
                    停止
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 系统告警 -->
        <el-col :span="12">
          <el-card class="alert-card">
            <template #header>
              <div class="card-header">
                <span>系统告警</span>
                <div class="alert-controls">
                  <el-select v-model="alertLevel" size="small" @change="filterAlerts">
                    <el-option label="全部" value="all" />
                    <el-option label="严重" value="critical" />
                    <el-option label="警告" value="warning" />
                    <el-option label="信息" value="info" />
                  </el-select>
                  <el-button type="text" size="small" @click="clearAllAlerts">
                    清除全部
                  </el-button>
                </div>
              </div>
            </template>
            <div class="alert-list">
              <div 
                v-for="alert in filteredAlerts" 
                :key="alert.id" 
                class="alert-item"
                :class="`alert-${alert.level}`"
              >
                <div class="alert-icon">
                  <el-icon v-if="alert.level === 'critical'"><CircleClose /></el-icon>
                  <el-icon v-else-if="alert.level === 'warning'"><Warning /></el-icon>
                  <el-icon v-else><InfoFilled /></el-icon>
                </div>
                <div class="alert-content">
                  <div class="alert-title">{{ alert.title }}</div>
                  <div class="alert-message">{{ alert.message }}</div>
                  <div class="alert-time">{{ alert.time }}</div>
                </div>
                <div class="alert-actions">
                  <el-button type="text" size="small" @click="acknowledgeAlert(alert.id)">
                    确认
                  </el-button>
                  <el-button type="text" size="small" @click="dismissAlert(alert.id)">
                    忽略
                  </el-button>
                </div>
              </div>
              <div v-if="filteredAlerts.length === 0" class="no-alerts">
                <el-icon><CircleCheck /></el-icon>
                <p>暂无告警信息</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 日志监控 -->
    <div class="log-monitoring">
      <el-card class="log-card">
        <template #header>
          <div class="card-header">
            <span>实时日志</span>
            <div class="log-controls">
              <el-select v-model="logLevel" size="small" @change="filterLogs">
                <el-option label="全部" value="all" />
                <el-option label="错误" value="error" />
                <el-option label="警告" value="warn" />
                <el-option label="信息" value="info" />
                <el-option label="调试" value="debug" />
              </el-select>
              <el-switch
                v-model="autoScrollLogs"
                active-text="自动滚动"
                inactive-text="停止滚动"
                size="small"
              />
              <el-button type="text" size="small" @click="clearLogs">
                清空日志
              </el-button>
            </div>
          </div>
        </template>
        <div class="log-container" ref="logContainer">
          <div 
            v-for="log in filteredLogs" 
            :key="log.id" 
            class="log-entry"
            :class="`log-${log.level}`"
          >
            <span class="log-time">{{ log.time }}</span>
            <span class="log-level">{{ log.level.toUpperCase() }}</span>
            <span class="log-source">{{ log.source }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 导出对话框 -->
    <ExportDialog
      v-model="showExportDialog"
      :data="exportData"
      :headers="exportHeaders"
      :title="'系统监控报表'"
      :available-charts="availableCharts"
      :on-export="handleExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, 
  Download, 
  CircleCheck, 
  Warning, 
  CircleClose, 
  Monitor, 
  Connection, 
  Timer, 
  Cpu, 
  Memo, 
  FolderOpened, 
  Remove, 
  InfoFilled 
} from '@element-plus/icons-vue'
import ExportDialog from '@/components/ExportDialog.vue'
import { exportData as exportDataUtil } from '@/utils/exportUtils'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { 
  LineChart, 
  BarChart, 
  PieChart,
  GaugeChart 
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
  GaugeChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

// 响应式数据
const autoRefresh = ref(true)
const refreshInterval = ref(10000)
const isRefreshing = ref(false)
const resourceTimeRange = ref('1h')
const responseTimeRange = ref('1h')
const alertLevel = ref('all')
const logLevel = ref('all')
const autoScrollLogs = ref(true)
const logContainer = ref(null)
const showExportDialog = ref(false)

let refreshTimer = null

// 导出相关数据
const exportData = computed(() => {
  return [
    {
      type: '系统状态',
      name: '整体状态',
      value: systemStatus.overall,
      uptime: systemStatus.uptime,
      services: `${systemStatus.services.running}/${systemStatus.services.total}`,
      connections: systemStatus.connections.active
    },
    {
      type: 'CPU指标',
      name: 'CPU使用率',
      value: metricsData.cpu.usage,
      unit: '%',
      status: metricsData.cpu.status
    },
    {
      type: '内存指标',
      name: '内存使用率',
      value: metricsData.memory.usage,
      unit: '%',
      used: metricsData.memory.used,
      total: metricsData.memory.total
    },
    {
      type: '磁盘指标',
      name: '磁盘使用率',
      value: metricsData.disk.usage,
      unit: '%',
      used: metricsData.disk.used,
      total: metricsData.disk.total
    },
    {
      type: '网络指标',
      name: '网络流量',
      inbound: metricsData.network.inbound,
      outbound: metricsData.network.outbound,
      unit: 'MB/s'
    }
  ]
})

const exportHeaders = computed(() => [
  { key: 'type', label: '类型' },
  { key: 'name', label: '名称' },
  { key: 'value', label: '数值' },
  { key: 'unit', label: '单位' },
  { key: 'status', label: '状态' },
  { key: 'uptime', label: '运行时间' },
  { key: 'services', label: '服务状态' },
  { key: 'connections', label: '连接数' },
  { key: 'used', label: '已使用' },
  { key: 'total', label: '总量' },
  { key: 'inbound', label: '入站流量' },
  { key: 'outbound', label: '出站流量' }
])

const availableCharts = computed(() => [
  { id: 'resource-usage-chart', name: '资源使用趋势' },
  { id: 'response-time-chart', name: 'API响应时间' },
  { id: 'error-rate-chart', name: '错误率统计' },
  { id: 'connections-chart', name: '并发连接数' },
  { id: 'db-performance-chart', name: '数据库性能' }
])

// 系统状态
const systemStatus = reactive({
  overall: 'healthy', // healthy, warning, critical
  lastUpdate: new Date().toLocaleString(),
  services: {
    running: 8,
    total: 10
  },
  connections: {
    active: 245
  },
  uptime: '15天 8小时 32分钟'
})

// 关键指标数据
const metricsData = reactive({
  cpu: {
    usage: 45,
    load: '1.2, 1.5, 1.8'
  },
  memory: {
    usage: 68,
    used: 5.4,
    total: 8.0
  },
  disk: {
    usage: 72,
    used: 360,
    total: 500
  },
  network: {
    latency: 25,
    upload: 12.5,
    download: 45.8
  }
})

// 图表加载状态
const resourceChartLoading = ref(false)
const responseTimeChartLoading = ref(false)
const errorRateChartLoading = ref(false)
const connectionChartLoading = ref(false)
const databaseChartLoading = ref(false)

// 服务数据
const servicesData = ref([
  {
    name: 'Web服务器',
    status: 'running',
    port: 8080,
    cpu: 15,
    memory: 256,
    uptime: '15天 8小时'
  },
  {
    name: '数据库服务',
    status: 'running',
    port: 3306,
    cpu: 25,
    memory: 512,
    uptime: '15天 8小时'
  },
  {
    name: 'Redis缓存',
    status: 'running',
    port: 6379,
    cpu: 5,
    memory: 128,
    uptime: '15天 8小时'
  },
  {
    name: '消息队列',
    status: 'stopped',
    port: 5672,
    cpu: 0,
    memory: 0,
    uptime: '0分钟'
  },
  {
    name: '文件服务',
    status: 'error',
    port: 9000,
    cpu: 0,
    memory: 0,
    uptime: '0分钟'
  }
])

// 告警数据
const alertsData = ref([
  {
    id: 1,
    level: 'critical',
    title: 'CPU使用率过高',
    message: 'CPU使用率已达到85%，请检查系统负载',
    time: '2024-01-15 14:30:25'
  },
  {
    id: 2,
    level: 'warning',
    title: '磁盘空间不足',
    message: '系统磁盘剩余空间不足20%',
    time: '2024-01-15 14:25:10'
  },
  {
    id: 3,
    level: 'info',
    title: '系统更新可用',
    message: '检测到新的系统更新版本',
    time: '2024-01-15 14:20:05'
  }
])

// 日志数据
const logsData = ref([
  {
    id: 1,
    level: 'info',
    time: '14:35:20',
    source: 'WebServer',
    message: '用户登录成功: admin@example.com'
  },
  {
    id: 2,
    level: 'warn',
    time: '14:35:15',
    source: 'Database',
    message: '查询执行时间较长: 2.5秒'
  },
  {
    id: 3,
    level: 'error',
    time: '14:35:10',
    source: 'FileService',
    message: '文件上传失败: 磁盘空间不足'
  }
])

// 计算属性
const filteredAlerts = computed(() => {
  if (alertLevel.value === 'all') {
    return alertsData.value
  }
  return alertsData.value.filter(alert => alert.level === alertLevel.value)
})

const filteredLogs = computed(() => {
  if (logLevel.value === 'all') {
    return logsData.value
  }
  return logsData.value.filter(log => log.level === logLevel.value)
})

// 图表配置
const resourceChartOption = computed(() => ({
  title: {
    text: '系统资源使用趋势',
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
    data: ['CPU使用率', '内存使用率', '磁盘使用率'],
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
    data: generateTimeRange(resourceTimeRange.value)
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLabel: {
      formatter: '{value}%'
    }
  },
  series: [
    {
      name: 'CPU使用率',
      type: 'line',
      smooth: true,
      data: generateResourceData('cpu', resourceTimeRange.value),
      itemStyle: {
        color: '#409EFF'
      }
    },
    {
      name: '内存使用率',
      type: 'line',
      smooth: true,
      data: generateResourceData('memory', resourceTimeRange.value),
      itemStyle: {
        color: '#67C23A'
      }
    },
    {
      name: '磁盘使用率',
      type: 'line',
      smooth: true,
      data: generateResourceData('disk', resourceTimeRange.value),
      itemStyle: {
        color: '#E6A23C'
      }
    }
  ]
}))

const responseTimeChartOption = computed(() => ({
  title: {
    text: 'API响应时间分析',
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
    data: generateTimeRange(responseTimeRange.value)
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value}ms'
    }
  },
  series: [
    {
      name: '平均响应时间',
      type: 'line',
      smooth: true,
      data: generateResponseTimeData(responseTimeRange.value),
      itemStyle: {
        color: '#F56C6C'
      },
      areaStyle: {
        opacity: 0.3
      }
    }
  ]
}))

const errorRateChartOption = computed(() => ({
  title: {
    text: '错误率统计',
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
      name: '错误类型',
      type: 'pie',
      radius: '70%',
      data: [
        { value: 15, name: '4xx错误' },
        { value: 8, name: '5xx错误' },
        { value: 3, name: '超时错误' },
        { value: 2, name: '连接错误' }
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

const connectionChartOption = computed(() => ({
  title: {
    text: '并发连接数',
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
    data: generateTimeRange('1h')
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '活跃连接',
      type: 'bar',
      data: generateConnectionData(),
      itemStyle: {
        color: '#909399'
      }
    }
  ]
}))

const databaseChartOption = computed(() => ({
  title: {
    text: '数据库性能',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['查询数/秒', '连接数'],
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
    data: generateTimeRange('1h')
  },
  yAxis: [
    {
      type: 'value',
      name: '查询数/秒',
      position: 'left'
    },
    {
      type: 'value',
      name: '连接数',
      position: 'right'
    }
  ],
  series: [
    {
      name: '查询数/秒',
      type: 'line',
      data: generateDatabaseQueryData(),
      itemStyle: {
        color: '#409EFF'
      }
    },
    {
      name: '连接数',
      type: 'line',
      yAxisIndex: 1,
      data: generateDatabaseConnectionData(),
      itemStyle: {
        color: '#67C23A'
      }
    }
  ]
}))

// 方法
const handleAutoRefreshChange = (value) => {
  if (value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const updateRefreshInterval = () => {
  if (autoRefresh.value) {
    stopAutoRefresh()
    startAutoRefresh()
  }
}

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    refreshAllData()
  }, refreshInterval.value)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const refreshAllData = async () => {
  isRefreshing.value = true
  try {
    // 模拟数据刷新
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新系统状态
    systemStatus.lastUpdate = new Date().toLocaleString()
    
    // 更新指标数据
    metricsData.cpu.usage = Math.floor(Math.random() * 30) + 30
    metricsData.memory.usage = Math.floor(Math.random() * 20) + 60
    metricsData.disk.usage = Math.floor(Math.random() * 10) + 70
    metricsData.network.latency = Math.floor(Math.random() * 20) + 20
    
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    isRefreshing.value = false
  }
}

const exportMonitoringReport = () => {
  ElMessageBox.confirm('确定要导出系统监控报告吗？', '确认导出', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    ElMessage.success('报告导出成功')
  }).catch(() => {
    ElMessage.info('已取消导出')
  })
}

const updateResourceChart = () => {
  resourceChartLoading.value = true
  setTimeout(() => {
    resourceChartLoading.value = false
  }, 1000)
}

const updateResponseTimeChart = () => {
  responseTimeChartLoading.value = true
  setTimeout(() => {
    responseTimeChartLoading.value = false
  }, 1000)
}

const refreshServiceStatus = () => {
  ElMessage.success('服务状态已刷新')
}

const startService = (serviceName) => {
  ElMessage.success(`正在启动服务: ${serviceName}`)
}

const stopService = (serviceName) => {
  ElMessage.warning(`正在停止服务: ${serviceName}`)
}

const restartService = (serviceName) => {
  ElMessage.info(`正在重启服务: ${serviceName}`)
}

const filterAlerts = () => {
  // 过滤告警已通过计算属性实现
}

const clearAllAlerts = () => {
  ElMessageBox.confirm('确定要清除所有告警吗？', '确认清除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    alertsData.value = []
    ElMessage.success('已清除所有告警')
  })
}

const acknowledgeAlert = (alertId) => {
  const index = alertsData.value.findIndex(alert => alert.id === alertId)
  if (index !== -1) {
    alertsData.value.splice(index, 1)
    ElMessage.success('告警已确认')
  }
}

const dismissAlert = (alertId) => {
  const index = alertsData.value.findIndex(alert => alert.id === alertId)
  if (index !== -1) {
    alertsData.value.splice(index, 1)
    ElMessage.info('告警已忽略')
  }
}

const filterLogs = () => {
  // 过滤日志已通过计算属性实现
}

const clearLogs = () => {
  logsData.value = []
  ElMessage.success('日志已清空')
}

// 导出处理方法
const handleExport = async (config) => {
  try {
    await exportDataUtil(config)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}

// 工具方法
const getSystemStatusText = (status) => {
  const statusMap = {
    'healthy': '系统正常',
    'warning': '系统警告',
    'critical': '系统异常'
  }
  return statusMap[status] || '未知状态'
}

const getCpuColor = (usage) => {
  if (usage < 50) return '#67C23A'
  if (usage < 80) return '#E6A23C'
  return '#F56C6C'
}

const getMemoryColor = (usage) => {
  if (usage < 60) return '#67C23A'
  if (usage < 85) return '#E6A23C'
  return '#F56C6C'
}

const getDiskColor = (usage) => {
  if (usage < 70) return '#67C23A'
  if (usage < 90) return '#E6A23C'
  return '#F56C6C'
}

const getNetworkColor = (latency) => {
  if (latency < 50) return '#67C23A'
  if (latency < 100) return '#E6A23C'
  return '#F56C6C'
}

const getNetworkPercentage = (latency) => {
  return Math.min(100, (latency / 200) * 100)
}

// 数据生成方法
const generateTimeRange = (range) => {
  const now = new Date()
  const points = range === '1h' ? 12 : range === '6h' ? 24 : range === '24h' ? 24 : 7
  const interval = range === '1h' ? 5 : range === '6h' ? 15 : range === '24h' ? 60 : 1440
  
  const times = []
  for (let i = points - 1; i >= 0; i--) {
    const time = new Date(now.getTime() - i * interval * 60 * 1000)
    if (range === '7d') {
      times.push(time.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
    } else {
      times.push(time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }))
    }
  }
  return times
}

const generateResourceData = (type, range) => {
  const points = range === '1h' ? 12 : range === '6h' ? 24 : range === '24h' ? 24 : 7
  const baseValue = type === 'cpu' ? 45 : type === 'memory' ? 68 : 72
  
  return Array.from({ length: points }, () => {
    return Math.max(0, Math.min(100, baseValue + (Math.random() - 0.5) * 20))
  })
}

const generateResponseTimeData = (range) => {
  const points = range === '1h' ? 12 : range === '6h' ? 24 : 24
  return Array.from({ length: points }, () => Math.floor(Math.random() * 100) + 50)
}

const generateConnectionData = () => {
  return Array.from({ length: 12 }, () => Math.floor(Math.random() * 100) + 200)
}

const generateDatabaseQueryData = () => {
  return Array.from({ length: 12 }, () => Math.floor(Math.random() * 50) + 100)
}

const generateDatabaseConnectionData = () => {
  return Array.from({ length: 12 }, () => Math.floor(Math.random() * 20) + 30)
}

// 生命周期
onMounted(() => {
  if (autoRefresh.value) {
    startAutoRefresh()
  }
  
  // 模拟实时日志
  setInterval(() => {
    if (logsData.value.length > 100) {
      logsData.value.splice(0, 50) // 保持日志数量在合理范围内
    }
    
    const newLog = {
      id: Date.now(),
      level: ['info', 'warn', 'error', 'debug'][Math.floor(Math.random() * 4)],
      time: new Date().toLocaleTimeString(),
      source: ['WebServer', 'Database', 'FileService', 'Cache'][Math.floor(Math.random() * 4)],
      message: `系统运行日志 ${Math.floor(Math.random() * 1000)}`
    }
    
    logsData.value.push(newLog)
    
    if (autoScrollLogs.value) {
      nextTick(() => {
        if (logContainer.value) {
          logContainer.value.scrollTop = logContainer.value.scrollHeight
        }
      })
    }
  }, 3000)
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.system-monitoring {
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

.status-overview {
  margin-bottom: 20px;
}

.status-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.status-card.status-healthy {
  border-left: 4px solid #67C23A;
}

.status-card.status-warning {
  border-left: 4px solid #E6A23C;
}

.status-card.status-critical {
  border-left: 4px solid #F56C6C;
}

.status-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
  color: white;
}

.status-card.status-healthy .status-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.status-card.status-warning .status-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
}

.status-card.status-critical .status-icon {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
}

.status-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.status-info p {
  margin: 0 0 12px 0;
  color: #909399;
  font-size: 14px;
}

.status-details {
  display: flex;
  gap: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.metrics-cards {
  margin-bottom: 20px;
}

.metric-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.metric-card:hover {
  transform: translateY(-2px);
}

.metric-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.metric-icon.cpu {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.metric-icon.memory {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
}

.metric-icon.disk {
  background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
}

.metric-icon.network {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.metric-info h3 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.metric-info p {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 14px;
}

.metric-progress {
  margin-bottom: 8px;
}

.metric-detail {
  font-size: 12px;
  color: #909399;
}

.performance-charts {
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

.chart-container {
  height: 300px;
  padding: 10px 0;
}

.chart {
  width: 100%;
  height: 100%;
}

.services-alerts {
  margin-bottom: 20px;
}

.service-card, .alert-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.service-list {
  max-height: 400px;
  overflow-y: auto;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.service-item:hover {
  background-color: #f9f9f9;
}

.service-item:last-child {
  border-bottom: none;
}

.service-item.service-running {
  border-left: 3px solid #67C23A;
}

.service-item.service-stopped {
  border-left: 3px solid #909399;
}

.service-item.service-error {
  border-left: 3px solid #F56C6C;
}

.service-info {
  flex: 1;
}

.service-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.service-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.service-actions {
  display: flex;
  gap: 8px;
}

.alert-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.alert-list {
  max-height: 400px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.alert-item:hover {
  background-color: #f9f9f9;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-item.alert-critical {
  border-left: 3px solid #F56C6C;
  background-color: #fef0f0;
}

.alert-item.alert-warning {
  border-left: 3px solid #E6A23C;
  background-color: #fdf6ec;
}

.alert-item.alert-info {
  border-left: 3px solid #409EFF;
  background-color: #ecf5ff;
}

.alert-icon {
  margin-right: 12px;
  margin-top: 2px;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.alert-message {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.alert-time {
  color: #909399;
  font-size: 12px;
}

.alert-actions {
  display: flex;
  gap: 8px;
}

.no-alerts {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.no-alerts .el-icon {
  font-size: 48px;
  color: #67C23A;
  margin-bottom: 12px;
}

.log-monitoring {
  margin-bottom: 20px;
}

.log-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.log-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.log-container {
  height: 300px;
  overflow-y: auto;
  background-color: #1e1e1e;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-entry {
  display: flex;
  gap: 12px;
  padding: 2px 0;
  border-bottom: 1px solid #333;
}

.log-entry:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  min-width: 60px;
}

.log-level {
  min-width: 50px;
  font-weight: bold;
}

.log-entry.log-error .log-level {
  color: #F56C6C;
}

.log-entry.log-warn .log-level {
  color: #E6A23C;
}

.log-entry.log-info .log-level {
  color: #409EFF;
}

.log-entry.log-debug .log-level {
  color: #909399;
}

.log-source {
  color: #67C23A;
  min-width: 80px;
}

.log-message {
  color: #ffffff;
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-monitoring {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .status-content {
    flex-direction: column;
    text-align: center;
  }
  
  .status-details {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .metric-content {
    flex-direction: column;
    text-align: center;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .service-item {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .service-details {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .alert-item {
    flex-direction: column;
    gap: 8px;
  }
}
</style>