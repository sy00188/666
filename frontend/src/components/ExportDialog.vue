<template>
  <el-dialog
    v-model="visible"
    title="数据导出"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="export-dialog">
      <!-- 导出类型选择 -->
      <div class="export-section">
        <h4>导出格式</h4>
        <el-radio-group v-model="exportConfig.format" @change="handleFormatChange">
          <el-radio value="excel">
            <el-icon><Document /></el-icon>
            Excel 表格
          </el-radio>
          <el-radio value="pdf">
            <el-icon><DocumentCopy /></el-icon>
            PDF 文档
          </el-radio>
          <el-radio value="png">
            <el-icon><Picture /></el-icon>
            PNG 图片
          </el-radio>
          <el-radio value="jpg">
            <el-icon><Picture /></el-icon>
            JPG 图片
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 导出内容选择 -->
      <div class="export-section">
        <h4>导出内容</h4>
        <el-radio-group v-model="exportType">
          <el-radio value="data">数据表格</el-radio>
          <el-radio value="chart">图表</el-radio>
          <el-radio value="page">整个页面</el-radio>
        </el-radio-group>
      </div>

      <!-- 数据导出配置 -->
      <div v-if="exportType === 'data'" class="export-section">
        <h4>数据配置</h4>
        <el-form :model="dataConfig" label-width="100px" size="default">
          <el-form-item label="包含表头">
            <el-switch v-model="dataConfig.includeHeaders" />
          </el-form-item>
          <el-form-item label="工作表名称" v-if="exportConfig.format === 'excel'">
            <el-input v-model="dataConfig.sheetName" placeholder="Sheet1" />
          </el-form-item>
          <el-form-item label="数据范围">
            <el-select v-model="dataConfig.range" placeholder="选择数据范围">
              <el-option label="当前页数据" value="current" />
              <el-option label="所有数据" value="all" />
              <el-option label="选中数据" value="selected" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 图表导出配置 -->
      <div v-if="exportType === 'chart'" class="export-section">
        <h4>图表配置</h4>
        <el-form :model="chartConfig" label-width="100px" size="default">
          <el-form-item label="图表选择">
            <el-select v-model="chartConfig.chartId" placeholder="选择要导出的图表">
              <el-option 
                v-for="chart in availableCharts" 
                :key="chart.id" 
                :label="chart.name" 
                :value="chart.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="分辨率" v-if="['png', 'jpg'].includes(exportConfig.format)">
            <el-select v-model="chartConfig.resolution">
              <el-option label="标准 (1x)" value="1" />
              <el-option label="高清 (2x)" value="2" />
              <el-option label="超高清 (3x)" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="背景颜色" v-if="['png', 'jpg'].includes(exportConfig.format)">
            <el-color-picker v-model="chartConfig.backgroundColor" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 页面导出配置 -->
      <div v-if="exportType === 'page'" class="export-section">
        <h4>页面配置</h4>
        <el-form :model="pageConfig" label-width="100px" size="default">
          <el-form-item label="页面方向" v-if="exportConfig.format === 'pdf'">
            <el-select v-model="pageConfig.orientation">
              <el-option label="纵向" value="portrait" />
              <el-option label="横向" value="landscape" />
            </el-select>
          </el-form-item>
          <el-form-item label="页面大小" v-if="exportConfig.format === 'pdf'">
            <el-select v-model="pageConfig.format">
              <el-option label="A4" value="a4" />
              <el-option label="A3" value="a3" />
              <el-option label="Letter" value="letter" />
            </el-select>
          </el-form-item>
          <el-form-item label="导出区域">
            <el-input 
              v-model="pageConfig.selector" 
              placeholder="CSS选择器，如 .main-content"
              clearable
            />
            <div class="form-tip">留空则导出整个页面</div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 通用配置 -->
      <div class="export-section">
        <h4>文件配置</h4>
        <el-form :model="exportConfig" label-width="100px" size="default">
          <el-form-item label="文件名">
            <el-input 
              v-model="exportConfig.filename" 
              :placeholder="getDefaultFilename()"
              clearable
            >
              <template #append>.{{ exportConfig.format }}</template>
            </el-input>
          </el-form-item>
          <el-form-item label="包含标题" v-if="['excel', 'pdf'].includes(exportConfig.format)">
            <el-switch v-model="exportConfig.includeTitle" />
          </el-form-item>
          <el-form-item label="标题内容" v-if="exportConfig.includeTitle && ['excel', 'pdf'].includes(exportConfig.format)">
            <el-input v-model="exportConfig.title" placeholder="导出文件标题" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 预览区域 -->
      <div class="export-section" v-if="previewData">
        <h4>数据预览</h4>
        <div class="preview-container">
          <el-table 
            :data="previewData.slice(0, 5)" 
            size="small" 
            max-height="200"
            style="width: 100%"
          >
            <el-table-column 
              v-for="(header, index) in previewHeaders" 
              :key="index"
              :prop="header.prop" 
              :label="header.label"
              show-overflow-tooltip
            />
          </el-table>
          <div class="preview-info" v-if="previewData.length > 5">
            <span>显示前5条，共 {{ previewData.length }} 条数据</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button @click="handlePreview" v-if="exportType === 'data'">预览数据</el-button>
        <el-button 
          type="primary" 
          @click="handleExport" 
          :loading="exporting"
          :disabled="!canExport"
        >
          {{ exporting ? '导出中...' : '开始导出' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, DocumentCopy, Picture } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  data: {
    type: Array,
    default: () => []
  },
  headers: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: ''
  },
  availableCharts: {
    type: Array,
    default: () => []
  },
  onExport: {
    type: Function,
    required: true
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'export'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const exportType = ref('data')
const exporting = ref(false)
const previewData = ref(null)
const previewHeaders = ref([])

// 导出配置
const exportConfig = reactive({
  format: 'excel',
  filename: '',
  includeTitle: true,
  title: props.title || '数据导出'
})

// 数据导出配置
const dataConfig = reactive({
  includeHeaders: true,
  sheetName: 'Sheet1',
  range: 'current'
})

// 图表导出配置
const chartConfig = reactive({
  chartId: '',
  resolution: '2',
  backgroundColor: '#ffffff'
})

// 页面导出配置
const pageConfig = reactive({
  orientation: 'portrait',
  format: 'a4',
  selector: ''
})

// 计算属性
const canExport = computed(() => {
  if (exportType.value === 'data') {
    return props.data && props.data.length > 0
  } else if (exportType.value === 'chart') {
    return chartConfig.chartId !== ''
  } else if (exportType.value === 'page') {
    return true
  }
  return false
})

// 方法
const handleFormatChange = () => {
  // 根据格式调整默认配置
  if (exportConfig.format === 'excel') {
    exportType.value = 'data'
  } else if (['png', 'jpg'].includes(exportConfig.format)) {
    if (exportType.value === 'data') {
      exportType.value = 'chart'
    }
  }
}

const getDefaultFilename = () => {
  const timestamp = new Date().toISOString().slice(0, 19).replace(/[:-]/g, '')
  const typeMap = {
    'data': '数据导出',
    'chart': '图表导出',
    'page': '页面导出'
  }
  return `${typeMap[exportType.value]}_${timestamp}`
}

const handlePreview = () => {
  if (!props.data || props.data.length === 0) {
    ElMessage.warning('没有可预览的数据')
    return
  }

  previewData.value = props.data
  previewHeaders.value = props.headers.map((header, index) => ({
    prop: typeof header === 'string' ? header : `col_${index}`,
    label: typeof header === 'string' ? header : header.label || `列${index + 1}`
  }))
}

const handleExport = async () => {
  if (!canExport.value) {
    ElMessage.warning('请检查导出配置')
    return
  }

  exporting.value = true
  
  try {
    const config = {
      format: exportConfig.format,
      filename: exportConfig.filename || getDefaultFilename(),
      title: exportConfig.includeTitle ? exportConfig.title : undefined
    }

    if (exportType.value === 'data') {
      // 数据导出
      config.data = props.data
      config.headers = dataConfig.includeHeaders ? props.headers : undefined
      config.options = {
        sheetName: dataConfig.sheetName
      }
    } else if (exportType.value === 'chart') {
      // 图表导出
      config.chartId = chartConfig.chartId
      config.options = {
        scale: parseInt(chartConfig.resolution),
        backgroundColor: chartConfig.backgroundColor
      }
    } else if (exportType.value === 'page') {
      // 页面导出
      config.element = pageConfig.selector || 'body'
      config.options = {
        orientation: pageConfig.orientation,
        format: pageConfig.format
      }
    }

    // 调用父组件的导出方法
    await props.onExport(config)
    
    ElMessage.success('导出成功')
    handleClose()
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error(`导出失败: ${error.message || '未知错误'}`)
  } finally {
    exporting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  // 重置配置
  exportType.value = 'data'
  exportConfig.format = 'excel'
  exportConfig.filename = ''
  exportConfig.includeTitle = true
  exportConfig.title = props.title || '数据导出'
  
  dataConfig.includeHeaders = true
  dataConfig.sheetName = 'Sheet1'
  dataConfig.range = 'current'
  
  chartConfig.chartId = ''
  chartConfig.resolution = '2'
  chartConfig.backgroundColor = '#ffffff'
  
  pageConfig.orientation = 'portrait'
  pageConfig.format = 'a4'
  pageConfig.selector = ''
  
  previewData.value = null
  previewHeaders.value = []
}

// 监听数据变化，自动预览
watch(() => props.data, (newData) => {
  if (newData && newData.length > 0 && exportType.value === 'data') {
    handlePreview()
  }
}, { immediate: true })
</script>

<style scoped>
.export-dialog {
  max-height: 70vh;
  overflow-y: auto;
}

.export-section {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.export-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.export-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.el-radio {
  margin-right: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.preview-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.preview-info {
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .export-dialog {
    max-height: 60vh;
  }
  
  .el-radio-group {
    flex-direction: column;
    gap: 8px;
  }
  
  .dialog-footer {
    flex-direction: column-reverse;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>