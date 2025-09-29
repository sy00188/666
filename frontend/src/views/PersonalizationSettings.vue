<template>
  <div class="personalization-settings">
    <div class="settings-header">
      <h1>个性化设置</h1>
      <p>自定义您的界面外观和使用体验</p>
    </div>

    <el-row :gutter="24">
      <!-- 主题设置 -->
      <el-col :span="12">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon><Sunny /></el-icon>
              <span>主题设置</span>
            </div>
          </template>

          <div class="setting-group">
            <div class="setting-item">
              <label>主题模式</label>
              <el-radio-group v-model="config.themeMode" @change="handleThemeModeChange">
                <el-radio-button value="light">
                  <el-icon><Sunny /></el-icon>
                  亮色
                </el-radio-button>
                <el-radio-button value="dark">
                  <el-icon><Moon /></el-icon>
                  暗色
                </el-radio-button>
                <el-radio-button value="auto">
                  <el-icon><Monitor /></el-icon>
                  自动
                </el-radio-button>
              </el-radio-group>
            </div>

            <div class="setting-item">
              <label>颜色方案</label>
              <div class="color-scheme-grid">
                <div
                  v-for="(scheme, key) in colorSchemes"
                  :key="key"
                  class="color-scheme-item"
                  :class="{ active: config.colorScheme === key }"
                  @click="handleColorSchemeChange(key as ColorScheme)"
                >
                  <div class="color-preview">
                    <div
                      class="color-dot primary"
                      :style="{ backgroundColor: scheme.primaryColor }"
                    ></div>
                    <div
                      class="color-dot success"
                      :style="{ backgroundColor: scheme.successColor }"
                    ></div>
                    <div
                      class="color-dot warning"
                      :style="{ backgroundColor: scheme.warningColor }"
                    ></div>
                    <div
                      class="color-dot error"
                      :style="{ backgroundColor: scheme.errorColor }"
                    ></div>
                  </div>
                  <span class="scheme-name">{{ getSchemeDisplayName(key) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 布局设置 -->
      <el-col :span="12">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon><Grid /></el-icon>
              <span>布局设置</span>
            </div>
          </template>

          <div class="setting-group">
            <div class="setting-item">
              <label>布局类型</label>
              <el-radio-group v-model="config.layoutType" @change="handleLayoutTypeChange">
                <el-radio-button value="compact">紧凑</el-radio-button>
                <el-radio-button value="default">默认</el-radio-button>
                <el-radio-button value="comfortable">舒适</el-radio-button>
              </el-radio-group>
            </div>

            <div class="setting-item">
              <label>侧边栏宽度</label>
              <el-slider
                v-model="config.sidebarWidth"
                :min="180"
                :max="320"
                :step="20"
                show-input
                @change="handleConfigChange"
              />
            </div>

            <div class="setting-item">
              <label>头部高度</label>
              <el-slider
                v-model="config.headerHeight"
                :min="48"
                :max="80"
                :step="8"
                show-input
                @change="handleConfigChange"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 界面设置 -->
      <el-col :span="12">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>界面设置</span>
            </div>
          </template>

          <div class="setting-group">
            <div class="setting-item">
              <el-switch
                v-model="config.showBreadcrumb"
                @change="handleConfigChange"
              />
              <label>显示面包屑导航</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.showTabs"
                @change="handleConfigChange"
              />
              <label>显示标签页</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.fixedHeader"
                @change="handleConfigChange"
              />
              <label>固定头部</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.fixedSidebar"
                @change="handleConfigChange"
              />
              <label>固定侧边栏</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.showLogo"
                @change="handleConfigChange"
              />
              <label>显示Logo</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.showFooter"
                @change="handleConfigChange"
              />
              <label>显示页脚</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.compactMode"
                @change="handleConfigChange"
              />
              <label>紧凑模式</label>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.showTooltips"
                @change="handleConfigChange"
              />
              <label>显示提示信息</label>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 字体和动画设置 -->
      <el-col :span="12">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon><EditPen /></el-icon>
              <span>字体和动画</span>
            </div>
          </template>

          <div class="setting-group">
            <div class="setting-item">
              <label>字体大小</label>
              <el-radio-group v-model="config.fontSize" @change="handleConfigChange">
                <el-radio-button value="small">小</el-radio-button>
                <el-radio-button value="medium">中</el-radio-button>
                <el-radio-button value="large">大</el-radio-button>
              </el-radio-group>
            </div>

            <div class="setting-item">
              <label>字体系列</label>
              <el-radio-group v-model="config.fontFamily" @change="handleConfigChange">
                <el-radio-button value="default">默认</el-radio-button>
                <el-radio-button value="serif">衬线</el-radio-button>
                <el-radio-button value="monospace">等宽</el-radio-button>
              </el-radio-group>
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.enableTransitions"
                @change="handleConfigChange"
              />
              <label>启用动画效果</label>
            </div>

            <div class="setting-item" v-if="config.enableTransitions">
              <label>动画持续时间 (ms)</label>
              <el-slider
                v-model="config.animationDuration"
                :min="100"
                :max="1000"
                :step="50"
                show-input
                @change="handleConfigChange"
              />
            </div>

            <div class="setting-item">
              <el-switch
                v-model="config.autoSave"
                @change="handleConfigChange"
              />
              <label>自动保存设置</label>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 配置管理 -->
      <el-col :span="24">
        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon><Files /></el-icon>
              <span>配置管理</span>
            </div>
          </template>

          <div class="config-actions">
            <el-button type="primary" @click="handleExportConfig">
              <el-icon><Download /></el-icon>
              导出配置
            </el-button>
            
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="false"
              accept=".json"
              @change="handleImportConfig"
            >
              <el-button>
                <el-icon><Upload /></el-icon>
                导入配置
              </el-button>
            </el-upload>

            <el-button @click="handleResetConfig">
              <el-icon><RefreshRight /></el-icon>
              重置为默认
            </el-button>

            <el-button type="success" @click="handleSaveConfig">
              <el-icon><Check /></el-icon>
              保存设置
            </el-button>
          </div>

          <!-- 预览区域 -->
          <div class="preview-section">
            <h3>预览效果</h3>
            <div class="preview-container">
              <div class="preview-header" :style="{ height: config.headerHeight + 'px' }">
                <div v-if="config.showLogo" class="preview-logo">Logo</div>
                <div class="preview-title">档案管理系统</div>
              </div>
              <div class="preview-content">
                <div class="preview-sidebar" :style="{ width: config.sidebarWidth + 'px' }">
                  <div class="preview-menu-item">菜单项 1</div>
                  <div class="preview-menu-item">菜单项 2</div>
                  <div class="preview-menu-item active">菜单项 3</div>
                </div>
                <div class="preview-main">
                  <div v-if="config.showBreadcrumb" class="preview-breadcrumb">
                    首页 / 设置 / 个性化
                  </div>
                  <div v-if="config.showTabs" class="preview-tabs">
                    <div class="preview-tab active">标签页 1</div>
                    <div class="preview-tab">标签页 2</div>
                  </div>
                  <div class="preview-card">
                    <h4>示例卡片</h4>
                    <p>这是一个示例内容，展示当前主题效果。</p>
                    <el-button type="primary" size="small">示例按钮</el-button>
                  </div>
                </div>
              </div>
              <div v-if="config.showFooter" class="preview-footer">
                页脚信息
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type UploadFile } from 'element-plus'
import {
  Sunny,
  Moon,
  Monitor,
  Grid,
  Setting,
  EditPen,
  Files,
  Download,
  Upload,
  RefreshRight,
  Check
} from '@element-plus/icons-vue'
import { useThemeStore, type ColorScheme, type ThemeMode, type LayoutType } from '@/stores/theme'

// 主题store
const themeStore = useThemeStore()

// 响应式数据
const config = computed({
  get: () => themeStore.config,
  set: (value) => themeStore.updateConfig(value)
})

const colorSchemes = computed(() => themeStore.colorSchemes)

// 上传组件引用
const uploadRef = ref()

// 方法
const getSchemeDisplayName = (key: string) => {
  const names: Record<string, string> = {
    default: '默认蓝',
    blue: '科技蓝',
    green: '自然绿',
    purple: '优雅紫',
    orange: '活力橙',
    red: '热情红'
  }
  return names[key] || key
}

const handleThemeModeChange = (mode: string | number | boolean | undefined) => {
  if (typeof mode === 'string' && ['light', 'dark', 'auto'].includes(mode)) {
    themeStore.setThemeMode(mode as ThemeMode)
    ElMessage.success(`已切换到${mode === 'light' ? '亮色' : mode === 'dark' ? '暗色' : '自动'}主题`)
  }
}

const handleColorSchemeChange = (scheme: ColorScheme) => {
  themeStore.setColorScheme(scheme)
  ElMessage.success(`已应用${getSchemeDisplayName(scheme)}配色方案`)
}

const handleLayoutTypeChange = (layout: string | number | boolean | undefined) => {
  if (typeof layout === 'string' && ['compact', 'default', 'comfortable'].includes(layout)) {
    themeStore.setLayoutType(layout as LayoutType)
    ElMessage.success(`已切换到${layout === 'compact' ? '紧凑' : layout === 'comfortable' ? '舒适' : '默认'}布局`)
  }
}

const handleConfigChange = () => {
  themeStore.applyTheme()
  if (config.value.autoSave) {
    ElMessage.success('设置已自动保存')
  }
}

const handleExportConfig = () => {
  try {
    const configJson = themeStore.exportConfig()
    const blob = new Blob([configJson], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `personalization-config-${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('配置已导出')
  } catch (error) {
    ElMessage.error('导出配置失败')
    console.error('Export config error:', error)
  }
}

const handleImportConfig = (file: UploadFile) => {
  if (!file.raw) return

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const configJson = e.target?.result as string
      const success = themeStore.importConfig(configJson)
      if (success) {
        ElMessage.success('配置导入成功')
      } else {
        ElMessage.error('配置文件格式错误')
      }
    } catch (error) {
      ElMessage.error('导入配置失败')
      console.error('Import config error:', error)
    }
  }
  reader.readAsText(file.raw)
}

const handleResetConfig = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有设置为默认值吗？此操作不可撤销。',
      '重置确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    themeStore.resetConfig()
    ElMessage.success('已重置为默认设置')
  } catch {
    // 用户取消操作
  }
}

const handleSaveConfig = () => {
  // 由于使用了 pinia-plugin-persistedstate，配置会自动持久化
  // 这里主要是给用户一个明确的保存反馈
  themeStore.applyTheme()
  ElMessage.success('设置已保存')
}

// 生命周期
onMounted(() => {
  // 确保主题已初始化
  themeStore.initTheme()
})
</script>

<style lang="scss" scoped>
.personalization-settings {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.settings-header {
  margin-bottom: 24px;
  text-align: center;

  h1 {
    font-size: 28px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 8px;
  }

  p {
    font-size: 16px;
    color: var(--el-text-color-secondary);
  }
}

.settings-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.setting-group {
  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
    }

    label {
      font-weight: 500;
      color: var(--el-text-color-primary);
      min-width: 120px;
    }

    .el-radio-group,
    .el-slider,
    .el-switch {
      flex: 1;
      max-width: 300px;
    }
  }
}

.color-scheme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 12px;
  width: 100%;
}

.color-scheme-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  border: 2px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--el-color-primary);
    transform: translateY(-2px);
  }

  &.active {
    border-color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }

  .color-preview {
    display: flex;
    gap: 4px;
    margin-bottom: 8px;
  }

  .color-dot {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 1px solid var(--el-border-color);
  }

  .scheme-name {
    font-size: 12px;
    color: var(--el-text-color-regular);
    text-align: center;
  }
}

.config-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.preview-section {
  h3 {
    margin-bottom: 16px;
    color: var(--el-text-color-primary);
  }
}

.preview-container {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-bg-color);
  min-height: 300px;
}

.preview-header {
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: var(--el-color-primary);
  color: white;
  gap: 16px;

  .preview-logo {
    font-weight: bold;
    padding: 4px 8px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 4px;
  }

  .preview-title {
    font-weight: 600;
  }
}

.preview-content {
  display: flex;
  min-height: 200px;
}

.preview-sidebar {
  background: var(--el-fill-color-light);
  border-right: 1px solid var(--el-border-color);
  padding: 8px 0;

  .preview-menu-item {
    padding: 8px 16px;
    color: var(--el-text-color-regular);
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background: var(--el-fill-color);
    }

    &.active {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      border-right: 2px solid var(--el-color-primary);
    }
  }
}

.preview-main {
  flex: 1;
  padding: 16px;
}

.preview-breadcrumb {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.preview-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;

  .preview-tab {
    padding: 6px 12px;
    background: var(--el-fill-color-light);
    border-radius: 4px 4px 0 0;
    font-size: 12px;
    color: var(--el-text-color-regular);
    cursor: pointer;

    &.active {
      background: var(--el-bg-color);
      color: var(--el-color-primary);
      border: 1px solid var(--el-border-color);
      border-bottom: 1px solid var(--el-bg-color);
    }
  }
}

.preview-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  padding: 16px;

  h4 {
    margin-bottom: 8px;
    color: var(--el-text-color-primary);
  }

  p {
    margin-bottom: 12px;
    color: var(--el-text-color-regular);
    font-size: 14px;
  }
}

.preview-footer {
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-top: 1px solid var(--el-border-color);
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

// 响应式设计
@media (max-width: 768px) {
  .personalization-settings {
    padding: 16px;
  }

  .color-scheme-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .config-actions {
    justify-content: center;
  }

  .preview-content {
    flex-direction: column;
  }

  .preview-sidebar {
    border-right: none;
    border-bottom: 1px solid var(--el-border-color);
  }
}
</style>