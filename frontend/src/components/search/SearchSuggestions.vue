<template>
  <div class="search-suggestions" v-if="visible && suggestions.length > 0">
    <div class="suggestions-header">
      <span class="header-text">搜索建议</span>
      <el-button 
        type="text" 
        size="small" 
        @click="clearSuggestions"
        class="clear-btn"
      >
        清除
      </el-button>
    </div>
    
    <div class="suggestions-list">
      <div
        v-for="(suggestion, index) in suggestions"
        :key="suggestion.id || index"
        class="suggestion-item"
        :class="{ 'active': activeIndex === index }"
        @click="selectSuggestion(suggestion)"
        @mouseenter="setActiveIndex(index)"
      >
        <div class="suggestion-content">
          <el-icon class="suggestion-icon">
            <Document v-if="suggestion.type === 'title'" />
            <Folder v-else-if="suggestion.type === 'category'" />
            <Search v-else-if="suggestion.type === 'code'" />
            <Clock v-else />
          </el-icon>
          
          <div class="suggestion-text">
            <div class="suggestion-main" v-html="highlightText(suggestion.text, query)"></div>
          </div>
          
          <div class="suggestion-meta">
            <el-tag 
              v-if="suggestion.type" 
              size="small" 
              :type="getTagType(suggestion.type) as any"
              effect="plain"
            >
              {{ getTypeLabel(suggestion.type) }}
            </el-tag>
            <span v-if="suggestion.count" class="suggestion-count">
              {{ suggestion.count }}条
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="showHotKeywords && hotKeywords.length > 0" class="hot-keywords">
      <div class="hot-keywords-header">热门搜索</div>
      <div class="hot-keywords-list">
        <el-tag
          v-for="keyword in hotKeywords"
          :key="keyword"
          class="hot-keyword"
          @click="selectHotKeyword(keyword)"
          effect="plain"
          size="small"
        >
          {{ keyword }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Search, Document, Folder, Clock } from '@element-plus/icons-vue'
import type { SearchSuggestion } from '@/composables/useAdvancedSearch'

interface Props {
  visible: boolean
  suggestions: SearchSuggestion[]
  query: string
  showHotKeywords?: boolean
  hotKeywords?: string[]
}

interface Emits {
  (e: 'select', suggestion: SearchSuggestion): void
  (e: 'selectHotKeyword', keyword: string): void
  (e: 'clear'): void
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  showHotKeywords: true,
  hotKeywords: () => ['人事档案', '财务报表', '合同文件', '会议纪要', '技术文档']
})

const emit = defineEmits<Emits>()

const activeIndex = ref(-1)

// 计算属性
const maxSuggestions = computed(() => {
  // 移动端显示更少的建议
  return window.innerWidth <= 768 ? 5 : 8
})

// 方法
const selectSuggestion = (suggestion: SearchSuggestion) => {
  emit('select', suggestion)
}

const selectHotKeyword = (keyword: string) => {
  emit('selectHotKeyword', keyword)
}

const clearSuggestions = () => {
  emit('clear')
}

const setActiveIndex = (index: number) => {
  activeIndex.value = index
}

const getTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    title: 'success',
    code: 'primary',
    category: 'warning',
    content: 'info'
  }
  return typeMap[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    title: '标题',
    code: '编号',
    category: '分类',
    content: '内容'
  }
  return labelMap[type] || '其他'
}

const highlightText = (text: string, query: string) => {
  if (!query || !text) return text
  
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<mark class="highlight">$1</mark>')
}

// 键盘导航
const handleKeydown = (event: KeyboardEvent) => {
  if (!props.visible || props.suggestions.length === 0) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      activeIndex.value = Math.min(activeIndex.value + 1, props.suggestions.length - 1)
      break
    case 'ArrowUp':
      event.preventDefault()
      activeIndex.value = Math.max(activeIndex.value - 1, -1)
      break
    case 'Enter':
      event.preventDefault()
      if (activeIndex.value >= 0 && activeIndex.value < props.suggestions.length) {
        selectSuggestion(props.suggestions[activeIndex.value])
      }
      break
    case 'Escape':
      event.preventDefault()
      emit('close')
      break
  }
}

// 监听器
watch(() => props.visible, (visible) => {
  if (visible) {
    activeIndex.value = -1
  }
})

watch(() => props.suggestions, () => {
  activeIndex.value = -1
})

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
  
  @media (max-width: 768px) {
    max-height: 300px;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.suggestions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
  
  @media (max-width: 768px) {
    padding: 10px 12px;
  }
  
  .header-text {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-regular);
    
    @media (max-width: 768px) {
      font-size: 13px;
    }
  }
  
  .clear-btn {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.suggestions-list {
  .suggestion-item {
    padding: 12px 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-bottom: 1px solid var(--el-border-color-lighter);
    
    @media (max-width: 768px) {
      padding: 10px 12px;
    }
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover,
    &.active {
      background: var(--el-color-primary-light-9);
      
      .suggestion-icon {
        color: var(--el-color-primary);
      }
    }
    
    .suggestion-content {
      display: flex;
      align-items: center;
      gap: 12px;
      
      @media (max-width: 768px) {
        gap: 8px;
      }
    }
    
    .suggestion-icon {
      font-size: 16px;
      color: var(--el-text-color-placeholder);
      flex-shrink: 0;
      
      @media (max-width: 768px) {
        font-size: 14px;
      }
    }
    
    .suggestion-text {
      flex: 1;
      min-width: 0;
      
      .suggestion-main {
        font-size: 14px;
        color: var(--el-text-color-primary);
        line-height: 1.4;
        
        @media (max-width: 768px) {
          font-size: 13px;
        }
        
        :deep(.highlight) {
          background: var(--el-color-warning-light-7);
          color: var(--el-color-warning-dark-2);
          padding: 1px 2px;
          border-radius: 2px;
          font-weight: 500;
        }
      }
      
      .suggestion-desc {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
        margin-top: 2px;
        line-height: 1.3;
        
        @media (max-width: 768px) {
          font-size: 11px;
        }
      }
    }
    
    .suggestion-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
      
      @media (max-width: 768px) {
        gap: 4px;
      }
      
      .suggestion-count {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
        
        @media (max-width: 768px) {
          font-size: 11px;
        }
      }
    }
  }
}

.hot-keywords {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color-page);
  
  @media (max-width: 768px) {
    padding: 10px 12px;
  }
  
  .hot-keywords-header {
    font-size: 12px;
    color: var(--el-text-color-regular);
    margin-bottom: 8px;
    font-weight: 500;
    
    @media (max-width: 768px) {
      font-size: 11px;
      margin-bottom: 6px;
    }
  }
  
  .hot-keywords-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    
    @media (max-width: 768px) {
      gap: 4px;
    }
    
    .hot-keyword {
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 12px;
      
      @media (max-width: 768px) {
        font-size: 11px;
        padding: 2px 6px;
      }
      
      &:hover {
        color: var(--el-color-primary);
        border-color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
      }
    }
  }
}

// 滚动条样式
.search-suggestions::-webkit-scrollbar {
  width: 6px;
}

.search-suggestions::-webkit-scrollbar-track {
  background: var(--el-bg-color-page);
}

.search-suggestions::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
  
  &:hover {
    background: var(--el-border-color-dark);
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .search-suggestions {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    
    @media (max-width: 768px) {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    }
  }
}
</style>