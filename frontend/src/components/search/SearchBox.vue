<template>
  <div class="search-box" :class="{ 'is-focused': isFocused }">
    <div class="search-input-wrapper">
      <el-input
        ref="inputRef"
        v-model="inputValue"
        :placeholder="placeholder"
        :size="size"
        clearable
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
        @clear="handleClear"
        class="search-input"
      >
        <template #prefix>
          <el-icon class="search-icon">
            <Search />
          </el-icon>
        </template>
        
        <template #suffix>
          <div class="search-actions">
            <el-button
              v-if="showSearchButton"
              type="primary"
              :size="size"
              :loading="loading"
              @click="handleSearch"
              class="search-btn"
            >
              搜索
            </el-button>
          </div>
        </template>
      </el-input>
    </div>
    
    <!-- 搜索建议 -->
    <SearchSuggestions
      :visible="showSuggestions"
      :suggestions="suggestions"
      :query="inputValue"
      :show-hot-keywords="showHotKeywords"
      :hot-keywords="hotKeywords"
      @select="handleSelectSuggestion"
      @select-hot-keyword="handleSelectHotKeyword"
      @clear="handleClearSuggestions"
      @close="closeSuggestions"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElInput } from 'element-plus'
import SearchSuggestions from './SearchSuggestions.vue'
import type { SearchSuggestion } from '@/composables/useAdvancedSearch'

interface Props {
  modelValue: string
  placeholder?: string
  size?: 'large' | 'default' | 'small'
  loading?: boolean
  showSearchButton?: boolean
  showSuggestions?: boolean
  suggestions?: SearchSuggestion[]
  showHotKeywords?: boolean
  hotKeywords?: string[]
  debounceTime?: number
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'search', query: string): void
  (e: 'input', value: string): void
  (e: 'focus'): void
  (e: 'blur'): void
  (e: 'clear'): void
  (e: 'suggestion-select', suggestion: SearchSuggestion): void
  (e: 'hot-keyword-select', keyword: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入搜索关键词',
  size: 'default',
  loading: false,
  showSearchButton: true,
  showSuggestions: true,
  suggestions: () => [],
  showHotKeywords: true,
  hotKeywords: () => [],
  debounceTime: 300
})

const emit = defineEmits<Emits>()

// 响应式数据
const inputRef = ref<InstanceType<typeof ElInput>>()
const isFocused = ref(false)
const showSuggestions = ref(false)
const debounceTimer = ref<NodeJS.Timeout>()

// 计算属性
const inputValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

// 方法
const handleInput = (value: string) => {
  emit('input', value)
  
  // 防抖处理搜索建议
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
  }
  
  debounceTimer.value = setTimeout(() => {
    if (value.trim() && props.showSuggestions) {
      showSuggestions.value = true
    } else {
      showSuggestions.value = false
    }
  }, props.debounceTime)
}

const handleFocus = () => {
  isFocused.value = true
  emit('focus')
  
  // 如果有输入值且启用建议，显示建议
  if (inputValue.value.trim() && props.showSuggestions) {
    showSuggestions.value = true
  }
}

const handleBlur = () => {
  // 延迟隐藏建议，以便点击建议项
  setTimeout(() => {
    isFocused.value = false
    showSuggestions.value = false
    emit('blur')
  }, 200)
}

const handleKeydown = (event: Event | KeyboardEvent) => {
  const keyboardEvent = event as KeyboardEvent
  if (keyboardEvent.key === 'Enter') {
    keyboardEvent.preventDefault()
    handleSearch()
  } else if (keyboardEvent.key === 'Escape') {
    closeSuggestions()
    inputRef.value?.blur()
  }
}

const handleSearch = () => {
  const query = inputValue.value.trim()
  if (query) {
    emit('search', query)
    closeSuggestions()
  }
}

const handleClear = () => {
  emit('clear')
  closeSuggestions()
}

const handleSelectSuggestion = (suggestion: SearchSuggestion) => {
  inputValue.value = suggestion.text
  emit('suggestion-select', suggestion)
  closeSuggestions()
  
  // 自动搜索
  nextTick(() => {
    handleSearch()
  })
}

const handleSelectHotKeyword = (keyword: string) => {
  inputValue.value = keyword
  emit('hot-keyword-select', keyword)
  closeSuggestions()
  
  // 自动搜索
  nextTick(() => {
    handleSearch()
  })
}

const handleClearSuggestions = () => {
  // 清除建议相关的逻辑
  showSuggestions.value = false
}

const closeSuggestions = () => {
  showSuggestions.value = false
}

// 外部方法
const focus = () => {
  inputRef.value?.focus()
}

const blur = () => {
  inputRef.value?.blur()
}

// 暴露方法给父组件
defineExpose({
  focus,
  blur
})

// 监听器
watch(() => props.suggestions, (newSuggestions) => {
  if (newSuggestions.length > 0 && inputValue.value.trim() && isFocused.value) {
    showSuggestions.value = true
  }
})

// 点击外部关闭建议
const handleClickOutside = (event: Event) => {
  const target = event.target as HTMLElement
  const searchBox = document.querySelector('.search-box')
  
  if (searchBox && !searchBox.contains(target)) {
    showSuggestions.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
  }
})
</script>

<style scoped lang="scss">
.search-box {
  position: relative;
  width: 100%;
  
  &.is-focused {
    .search-input {
      :deep(.el-input__wrapper) {
        box-shadow: 0 0 0 1px var(--el-color-primary) inset;
      }
    }
  }
}

.search-input-wrapper {
  position: relative;
  
  .search-input {
    :deep(.el-input__wrapper) {
      transition: all 0.3s ease;
      border-radius: 8px;
      
      @media (max-width: 768px) {
        border-radius: 6px;
      }
      
      &:hover {
        box-shadow: 0 0 0 1px var(--el-color-primary-light-7) inset;
      }
    }
    
    :deep(.el-input__inner) {
      font-size: 14px;
      
      @media (max-width: 768px) {
        font-size: 16px; // 防止iOS缩放
      }
      
      &::placeholder {
        color: var(--el-text-color-placeholder);
      }
    }
  }
  
  .search-icon {
    color: var(--el-text-color-placeholder);
    font-size: 16px;
    
    @media (max-width: 768px) {
      font-size: 14px;
    }
  }
  
  .search-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .search-btn {
      border-radius: 6px;
      font-size: 14px;
      
      @media (max-width: 768px) {
        font-size: 13px;
        padding: 6px 12px;
      }
    }
  }
}

// 大尺寸样式
.search-box {
  &.size-large {
    .search-input {
      :deep(.el-input__wrapper) {
        min-height: 48px;
        border-radius: 10px;
      }
      
      :deep(.el-input__inner) {
        font-size: 16px;
      }
    }
    
    .search-icon {
      font-size: 18px;
    }
    
    .search-btn {
      font-size: 15px;
      padding: 10px 20px;
    }
  }
}

// 小尺寸样式
.search-box {
  &.size-small {
    .search-input {
      :deep(.el-input__wrapper) {
        min-height: 32px;
        border-radius: 6px;
      }
      
      :deep(.el-input__inner) {
        font-size: 13px;
      }
    }
    
    .search-icon {
      font-size: 14px;
    }
    
    .search-btn {
      font-size: 12px;
      padding: 4px 8px;
    }
  }
}

// 加载状态
.search-box {
  .search-btn {
    &.is-loading {
      pointer-events: none;
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .search-box {
    .search-input {
      :deep(.el-input__wrapper) {
        min-height: 44px;
      }
    }
    
    .search-actions {
      .search-btn {
        min-width: 60px;
      }
    }
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .search-box {
    .search-input {
      :deep(.el-input__wrapper) {
        &:hover {
          box-shadow: 0 0 0 1px var(--el-color-primary-light-3) inset;
        }
      }
    }
  }
}
</style>