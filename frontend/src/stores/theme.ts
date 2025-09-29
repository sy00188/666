import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 主题类型
export type ThemeMode = 'light' | 'dark' | 'auto'

// 颜色方案类型
export type ColorScheme = 'default' | 'blue' | 'green' | 'purple' | 'orange' | 'red'

// 布局类型
export type LayoutType = 'default' | 'compact' | 'comfortable'

// 个性化配置接口
export interface PersonalizationConfig {
  // 主题设置
  themeMode: ThemeMode
  colorScheme: ColorScheme
  
  // 布局设置
  layoutType: LayoutType
  sidebarWidth: number
  headerHeight: number
  
  // 界面设置
  showBreadcrumb: boolean
  showTabs: boolean
  fixedHeader: boolean
  fixedSidebar: boolean
  showLogo: boolean
  showFooter: boolean
  
  // 动画设置
  enableTransitions: boolean
  animationDuration: number
  
  // 字体设置
  fontSize: 'small' | 'medium' | 'large'
  fontFamily: 'default' | 'serif' | 'monospace'
  
  // 其他设置
  compactMode: boolean
  showTooltips: boolean
  autoSave: boolean
}

// 颜色方案配置
const colorSchemes = {
  default: {
    primaryColor: '#1890ff',
    successColor: '#52c41a',
    warningColor: '#faad14',
    errorColor: '#f5222d',
    infoColor: '#1890ff'
  },
  blue: {
    primaryColor: '#2563eb',
    successColor: '#059669',
    warningColor: '#d97706',
    errorColor: '#dc2626',
    infoColor: '#0ea5e9'
  },
  green: {
    primaryColor: '#059669',
    successColor: '#16a34a',
    warningColor: '#ca8a04',
    errorColor: '#dc2626',
    infoColor: '#0891b2'
  },
  purple: {
    primaryColor: '#7c3aed',
    successColor: '#059669',
    warningColor: '#d97706',
    errorColor: '#dc2626',
    infoColor: '#8b5cf6'
  },
  orange: {
    primaryColor: '#ea580c',
    successColor: '#059669',
    warningColor: '#d97706',
    errorColor: '#dc2626',
    infoColor: '#f97316'
  },
  red: {
    primaryColor: '#dc2626',
    successColor: '#059669',
    warningColor: '#d97706',
    errorColor: '#ef4444',
    infoColor: '#f87171'
  }
}

// 布局配置
const layoutConfigs = {
  default: {
    sidebarWidth: 240,
    headerHeight: 64,
    contentPadding: 24
  },
  compact: {
    sidebarWidth: 200,
    headerHeight: 56,
    contentPadding: 16
  },
  comfortable: {
    sidebarWidth: 280,
    headerHeight: 72,
    contentPadding: 32
  }
}

export const useThemeStore = defineStore('theme', () => {
  // 个性化配置状态
  const config = ref<PersonalizationConfig>({
    // 主题设置
    themeMode: 'light',
    colorScheme: 'default',
    
    // 布局设置
    layoutType: 'default',
    sidebarWidth: 240,
    headerHeight: 64,
    
    // 界面设置
    showBreadcrumb: true,
    showTabs: true,
    fixedHeader: true,
    fixedSidebar: true,
    showLogo: true,
    showFooter: true,
    
    // 动画设置
    enableTransitions: true,
    animationDuration: 300,
    
    // 字体设置
    fontSize: 'medium',
    fontFamily: 'default',
    
    // 其他设置
    compactMode: false,
    showTooltips: true,
    autoSave: true
  })

  // 计算属性
  const isDark = computed(() => {
    if (config.value.themeMode === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return config.value.themeMode === 'dark'
  })

  const naiveTheme = computed(() => {
    return isDark.value ? 'dark' : 'light'
  })

  const currentColorScheme = computed(() => {
    return colorSchemes[config.value.colorScheme]
  })

  const currentLayoutConfig = computed(() => {
    return layoutConfigs[config.value.layoutType]
  })

  // Element Plus 主题覆盖
  const elementPlusThemeOverrides = computed(() => {
    const colors = currentColorScheme.value
    const layout = currentLayoutConfig.value
    
    return {
      common: {
        primaryColor: colors.primaryColor,
        primaryColorHover: colors.primaryColor + '80',
        primaryColorPressed: colors.primaryColor + 'cc',
        successColor: colors.successColor,
        warningColor: colors.warningColor,
        errorColor: colors.errorColor,
        infoColor: colors.infoColor,
        
        // 字体设置
        fontSize: config.value.fontSize === 'small' ? '12px' : 
                 config.value.fontSize === 'large' ? '16px' : '14px',
        fontFamily: config.value.fontFamily === 'serif' ? 'Georgia, serif' :
                   config.value.fontFamily === 'monospace' ? 'Monaco, monospace' :
                   '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
        
        // 动画设置
        cubicBezierEaseInOut: config.value.enableTransitions ? 
          'cubic-bezier(0.4, 0, 0.2, 1)' : 'linear',
        
        // 布局设置
        borderRadius: config.value.compactMode ? '4px' : '6px',
        heightMedium: config.value.compactMode ? '32px' : '36px'
      },
      
      Layout: {
        siderWidth: `${config.value.sidebarWidth}px`,
        headerHeight: `${config.value.headerHeight}px`
      },
      
      Menu: {
        itemHeight: config.value.compactMode ? '36px' : '42px'
      },
      
      Button: {
        heightMedium: config.value.compactMode ? '32px' : '36px',
        paddingMedium: config.value.compactMode ? '0 12px' : '0 16px'
      },
      
      Card: {
        paddingMedium: config.value.compactMode ? '16px' : '24px'
      }
    }
  })

  // CSS 变量
  const cssVariables = computed(() => {
    const colors = currentColorScheme.value
    const layout = currentLayoutConfig.value
    
    return {
      '--primary-color': colors.primaryColor,
      '--success-color': colors.successColor,
      '--warning-color': colors.warningColor,
      '--error-color': colors.errorColor,
      '--info-color': colors.infoColor,
      '--sidebar-width': `${config.value.sidebarWidth}px`,
      '--header-height': `${config.value.headerHeight}px`,
      '--content-padding': `${layout.contentPadding}px`,
      '--animation-duration': `${config.value.animationDuration}ms`,
      '--font-size': config.value.fontSize === 'small' ? '12px' : 
                    config.value.fontSize === 'large' ? '16px' : '14px',
      '--border-radius': config.value.compactMode ? '4px' : '6px'
    }
  })

  // 方法
  const updateConfig = (newConfig: Partial<PersonalizationConfig>) => {
    config.value = { ...config.value, ...newConfig }
    applyTheme()
  }

  const setThemeMode = (mode: ThemeMode) => {
    config.value.themeMode = mode
    applyTheme()
  }

  const setColorScheme = (scheme: ColorScheme) => {
    config.value.colorScheme = scheme
    applyTheme()
  }

  const setLayoutType = (layout: LayoutType) => {
    config.value.layoutType = layout
    const layoutConfig = layoutConfigs[layout]
    config.value.sidebarWidth = layoutConfig.sidebarWidth
    config.value.headerHeight = layoutConfig.headerHeight
    applyTheme()
  }

  const toggleTheme = () => {
    const modes: ThemeMode[] = ['light', 'dark', 'auto']
    const currentIndex = modes.indexOf(config.value.themeMode)
    const nextIndex = (currentIndex + 1) % modes.length
    setThemeMode(modes[nextIndex])
  }

  const applyTheme = () => {
    const html = document.documentElement
    
    // 应用主题类
    html.classList.toggle('dark', isDark.value)
    html.classList.toggle('compact', config.value.compactMode)
    
    // 应用CSS变量
    const variables = cssVariables.value
    Object.entries(variables).forEach(([key, value]) => {
      html.style.setProperty(key, value)
    })
    
    // 应用字体类
    html.className = html.className.replace(/font-\w+/g, '')
    html.classList.add(`font-${config.value.fontFamily}`)
    html.classList.add(`text-${config.value.fontSize}`)
  }

  const resetConfig = () => {
    config.value = {
      themeMode: 'light',
      colorScheme: 'default',
      layoutType: 'default',
      sidebarWidth: 240,
      headerHeight: 64,
      showBreadcrumb: true,
      showTabs: true,
      fixedHeader: true,
      fixedSidebar: true,
      showLogo: true,
      showFooter: true,
      enableTransitions: true,
      animationDuration: 300,
      fontSize: 'medium',
      fontFamily: 'default',
      compactMode: false,
      showTooltips: true,
      autoSave: true
    }
    applyTheme()
  }

  const exportConfig = () => {
    return JSON.stringify(config.value, null, 2)
  }

  const importConfig = (configJson: string) => {
    try {
      const importedConfig = JSON.parse(configJson)
      config.value = { ...config.value, ...importedConfig }
      applyTheme()
      return true
    } catch (error) {
      console.error('导入配置失败:', error)
      return false
    }
  }

  // 初始化
  const initTheme = () => {
    applyTheme()
    
    // 监听系统主题变化
    if (config.value.themeMode === 'auto') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', applyTheme)
    }
  }

  return {
    // 状态
    config,
    
    // 计算属性
    isDark,
    naiveTheme,
    elementPlusThemeOverrides,
    currentColorScheme,
    currentLayoutConfig,
    cssVariables,
    
    // 方法
    updateConfig,
    setThemeMode,
    setColorScheme,
    setLayoutType,
    toggleTheme,
    applyTheme,
    resetConfig,
    exportConfig,
    importConfig,
    initTheme,
    
    // 常量
    colorSchemes,
    layoutConfigs
  }
}, {
  persist: {
    key: 'theme-store',
    storage: localStorage,
    paths: ['config']
  }
})