<template>
  <div id="app" :class="themeClasses">
    <el-config-provider :locale="locale" :theme="isDark ? 'dark' : 'light'">
      <router-view />
    </el-config-provider>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ElConfigProvider } from 'element-plus'
import { useThemeStore } from '@/stores/theme'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 主题store
const themeStore = useThemeStore()

// 计算属性
const isDark = computed(() => themeStore.isDark)
const themeClasses = computed(() => ({
  'theme-dark': isDark.value,
  'theme-light': !isDark.value,
  'layout-compact': themeStore.config.compactMode,
  [`color-scheme-${themeStore.config.colorScheme}`]: true,
  [`layout-${themeStore.config.layoutType}`]: true,
  [`font-${themeStore.config.fontFamily}`]: true,
  [`text-${themeStore.config.fontSize}`]: true
}))

// 国际化
const locale = zhCn

// 生命周期
onMounted(() => {
  themeStore.initTheme()
})
</script>

<style lang="scss">
// 基础样式
#app {
  width: 100%;
  height: 100vh;
  font-family: var(--font-family, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", sans-serif);
  font-size: var(--font-size, 14px);
  transition: all var(--animation-duration, 300ms) ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
  margin: 0;
  padding: 0;
}

// 主题变量
:root {
  // 默认颜色
  --el-color-primary: #409eff;
  --el-color-success: #67c23a;
  --el-color-warning: #e6a23c;
  --el-color-danger: #f56c6c;
  --el-color-info: #909399;
  
  // 背景色
  --el-bg-color: #ffffff;
  --el-bg-color-page: #f2f3f5;
  --el-bg-color-overlay: #ffffff;
  
  // 文字颜色
  --el-text-color-primary: #303133;
  --el-text-color-regular: #606266;
  --el-text-color-secondary: #909399;
  --el-text-color-placeholder: #a8abb2;
  
  // 边框颜色
  --el-border-color: #dcdfe6;
  --el-border-color-light: #e4e7ed;
  --el-border-color-lighter: #ebeef5;
  --el-border-color-extra-light: #f2f6fc;
  
  // 阴影
  --el-box-shadow: 0 12px 32px 4px rgba(0, 0, 0, 0.04), 0 8px 20px rgba(0, 0, 0, 0.08);
  --el-box-shadow-light: 0 0 12px rgba(0, 0, 0, 0.12);
  --el-box-shadow-lighter: 0 0 6px rgba(0, 0, 0, 0.12);
  --el-box-shadow-dark: 0 16px 48px 16px rgba(0, 0, 0, 0.08), 0 12px 32px rgba(0, 0, 0, 0.12), 0 8px 16px -8px rgba(0, 0, 0, 0.16);
}

// 暗色主题
.theme-dark {
  --el-bg-color: #141414;
  --el-bg-color-page: #0a0a0a;
  --el-bg-color-overlay: #1d1e1f;
  
  --el-text-color-primary: #e5eaf3;
  --el-text-color-regular: #cfd3dc;
  --el-text-color-secondary: #a3a6ad;
  --el-text-color-placeholder: #8d9095;
  
  --el-border-color: #4c4d4f;
  --el-border-color-light: #414243;
  --el-border-color-lighter: #363637;
  --el-border-color-extra-light: #2b2b2c;
  
  --el-fill-color: #2b2b2c;
  --el-fill-color-light: #262727;
  --el-fill-color-lighter: #1d1d1d;
  --el-fill-color-extra-light: #191919;
  --el-fill-color-dark: #39393a;
  --el-fill-color-darker: #424243;
  --el-fill-color-blank: transparent;
}

// 颜色方案
.color-scheme-blue {
  --el-color-primary: #2563eb;
  --el-color-primary-light-3: #6b93f0;
  --el-color-primary-light-5: #9bb8f3;
  --el-color-primary-light-7: #c4d7f7;
  --el-color-primary-light-8: #d9e6f9;
  --el-color-primary-light-9: #ecf4fc;
  --el-color-primary-dark-2: #1e56d8;
}

.color-scheme-green {
  --el-color-primary: #059669;
  --el-color-primary-light-3: #4ade80;
  --el-color-primary-light-5: #86efac;
  --el-color-primary-light-7: #bbf7d0;
  --el-color-primary-light-8: #d1fae5;
  --el-color-primary-light-9: #ecfdf5;
  --el-color-primary-dark-2: #047857;
}

.color-scheme-purple {
  --el-color-primary: #7c3aed;
  --el-color-primary-light-3: #a78bfa;
  --el-color-primary-light-5: #c4b5fd;
  --el-color-primary-light-7: #ddd6fe;
  --el-color-primary-light-8: #ede9fe;
  --el-color-primary-light-9: #f3f4f6;
  --el-color-primary-dark-2: #6d28d9;
}

.color-scheme-orange {
  --el-color-primary: #ea580c;
  --el-color-primary-light-3: #fb923c;
  --el-color-primary-light-5: #fdba74;
  --el-color-primary-light-7: #fed7aa;
  --el-color-primary-light-8: #fee8c8;
  --el-color-primary-light-9: #fff7ed;
  --el-color-primary-dark-2: #c2410c;
}

.color-scheme-red {
  --el-color-primary: #dc2626;
  --el-color-primary-light-3: #f87171;
  --el-color-primary-light-5: #fca5a5;
  --el-color-primary-light-7: #fecaca;
  --el-color-primary-light-8: #fee2e2;
  --el-color-primary-light-9: #fef2f2;
  --el-color-primary-dark-2: #b91c1c;
}

// 布局类型
.layout-compact {
  --el-component-size: small;
  
  .el-button {
    --el-button-size: 32px;
    padding: 0 12px;
  }
  
  .el-input {
    --el-input-height: 32px;
  }
  
  .el-card {
    --el-card-padding: 16px;
  }
}

.layout-comfortable {
  --el-component-size: large;
  
  .el-button {
    --el-button-size: 40px;
    padding: 0 20px;
  }
  
  .el-input {
    --el-input-height: 40px;
  }
  
  .el-card {
    --el-card-padding: 32px;
  }
}

// 字体设置
.font-serif {
  font-family: Georgia, 'Times New Roman', Times, serif;
}

.font-monospace {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.text-small {
  font-size: 12px;
  
  .el-button {
    font-size: 12px;
  }
  
  .el-input {
    font-size: 12px;
  }
}

.text-large {
  font-size: 16px;
  
  .el-button {
    font-size: 16px;
  }
  
  .el-input {
    font-size: 16px;
  }
}

// 动画设置
.no-transitions * {
  transition: none !important;
  animation: none !important;
}

// 滚动条样式
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
  
  &:hover {
    background: var(--el-border-color-light);
  }
}

// 响应式设计
@media (max-width: 768px) {
  #app {
    font-size: 14px;
  }
  
  .layout-comfortable {
    --el-component-size: default;
  }
}
</style>
