<template>
  <div class="personalization-settings">
    <div class="page-header">
      <h1>个性化设置</h1>
      <p class="page-description">自定义您的系统界面和使用偏好</p>
    </div>
    
    <div class="settings-content">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>界面设置</span>
              </div>
            </template>
            
            <el-form :model="settings" label-width="120px" label-position="left">
              <el-form-item label="主题模式">
                <el-radio-group v-model="settings.theme">
                  <el-radio label="light">浅色模式</el-radio>
                  <el-radio label="dark">深色模式</el-radio>
                  <el-radio label="auto">跟随系统</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="主题色彩">
                <el-color-picker v-model="settings.primaryColor" />
              </el-form-item>
              
              <el-form-item label="侧边栏">
                <el-switch
                  v-model="settings.sidebarCollapsed"
                  active-text="折叠"
                  inactive-text="展开"
                />
              </el-form-item>
              
              <el-form-item label="面包屑导航">
                <el-switch
                  v-model="settings.showBreadcrumb"
                  active-text="显示"
                  inactive-text="隐藏"
                />
              </el-form-item>
              
              <el-form-item label="页面标签">
                <el-switch
                  v-model="settings.showTabs"
                  active-text="显示"
                  inactive-text="隐藏"
                />
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card class="settings-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>功能偏好</span>
              </div>
            </template>
            
            <el-form :model="settings" label-width="120px" label-position="left">
              <el-form-item label="默认页面大小">
                <el-select v-model="settings.defaultPageSize" placeholder="请选择">
                  <el-option label="10条/页" :value="10" />
                  <el-option label="20条/页" :value="20" />
                  <el-option label="50条/页" :value="50" />
                  <el-option label="100条/页" :value="100" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="自动保存">
                <el-switch
                  v-model="settings.autoSave"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
              
              <el-form-item label="操作确认">
                <el-switch
                  v-model="settings.confirmBeforeAction"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>预览</span>
              </div>
            </template>
            
            <div class="preview-container">
              <div class="preview-mockup" :class="{ 'dark-theme': settings.theme === 'dark' }">
                <div class="mockup-header" :style="{ backgroundColor: settings.primaryColor }">
                  <div class="mockup-logo"></div>
                  <div class="mockup-title">档案管理系统</div>
                </div>
                <div class="mockup-content">
                  <div class="mockup-sidebar" :class="{ collapsed: settings.sidebarCollapsed }">
                    <div class="mockup-menu-item"></div>
                    <div class="mockup-menu-item"></div>
                    <div class="mockup-menu-item"></div>
                  </div>
                  <div class="mockup-main">
                    <div v-if="settings.showBreadcrumb" class="mockup-breadcrumb"></div>
                    <div v-if="settings.showTabs" class="mockup-tabs"></div>
                    <div class="mockup-page-content"></div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div class="settings-actions">
        <el-button type="primary" @click="saveSettings" :loading="saving">
          保存设置
        </el-button>
        <el-button @click="resetSettings">
          重置默认
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 设置数据
const settings = reactive({
  theme: 'light',
  primaryColor: '#409EFF',
  sidebarCollapsed: false,
  showBreadcrumb: true,
  showTabs: true,
  defaultPageSize: 20,
  autoSave: true,
  confirmBeforeAction: true
})

const saving = ref(false)

// 生命周期
onMounted(() => {
  loadSettings()
})

// 加载设置
const loadSettings = async () => {
  try {
    // TODO: 从后端或本地存储加载用户设置
    console.log('加载个性化设置')
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

// 保存设置
const saveSettings = async () => {
  saving.value = true
  try {
    // TODO: 保存设置到后端或本地存储
    console.log('保存个性化设置:', settings)
    ElMessage.success('设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

// 重置设置
const resetSettings = () => {
  Object.assign(settings, {
    theme: 'light',
    primaryColor: '#409EFF',
    sidebarCollapsed: false,
    showBreadcrumb: true,
    showTabs: true,
    defaultPageSize: 20,
    autoSave: true,
    confirmBeforeAction: true
  })
  ElMessage.success('已重置为默认设置')
}
</script>

<style scoped>
.personalization-settings {
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

.settings-content {
  background: #fff;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.preview-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-mockup {
  width: 200px;
  height: 300px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s;
}

.preview-mockup.dark-theme {
  background: #2b2b2b;
  border-color: #4c4d4f;
}

.mockup-header {
  height: 40px;
  background: #409EFF;
  display: flex;
  align-items: center;
  padding: 0 8px;
  color: white;
  font-size: 12px;
}

.mockup-logo {
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 2px;
  margin-right: 8px;
}

.mockup-title {
  font-size: 10px;
}

.mockup-content {
  display: flex;
  height: 260px;
}

.mockup-sidebar {
  width: 60px;
  background: #f5f7fa;
  padding: 8px 4px;
  transition: width 0.3s;
}

.mockup-sidebar.collapsed {
  width: 20px;
}

.dark-theme .mockup-sidebar {
  background: #363637;
}

.mockup-menu-item {
  height: 20px;
  background: #e4e7ed;
  margin-bottom: 4px;
  border-radius: 2px;
}

.dark-theme .mockup-menu-item {
  background: #4c4d4f;
}

.mockup-main {
  flex: 1;
  padding: 8px;
}

.mockup-breadcrumb {
  height: 12px;
  background: #f0f2f5;
  margin-bottom: 8px;
  border-radius: 2px;
}

.dark-theme .mockup-breadcrumb {
  background: #4c4d4f;
}

.mockup-tabs {
  height: 16px;
  background: #f0f2f5;
  margin-bottom: 8px;
  border-radius: 2px;
}

.dark-theme .mockup-tabs {
  background: #4c4d4f;
}

.mockup-page-content {
  height: 180px;
  background: #f9fafc;
  border-radius: 2px;
}

.dark-theme .mockup-page-content {
  background: #363637;
}

.settings-actions {
  margin-top: 24px;
  text-align: center;
}

.settings-actions .el-button {
  margin: 0 8px;
}
</style>