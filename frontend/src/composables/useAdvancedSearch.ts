import { 
  getSearchSuggestions as getSearchSuggestionsAPI,
  searchArchives,
  getHotSearchKeywords,
  saveSearchHistory as saveSearchHistoryAPI,
  getUserSearchHistory,
  saveSearchCondition as saveSearchConditionAPI,
  getUserSearchConditions,
  deleteSearchCondition as deleteSearchConditionAPI
} from '@/api/search'
import { ref, computed, reactive, watch } from 'vue'
import type { Ref } from 'vue'

// 搜索建议接口
export interface SearchSuggestion {
  id: string
  text: string
  type: 'title' | 'code' | 'category' | 'content'
  count?: number
}

// 搜索历史接口
export interface SearchHistory {
  id: string
  query: string
  timestamp: number
  mode: 'simple' | 'advanced'
  filters?: Record<string, any>
}

// 保存的搜索条件接口
export interface SavedSearchCondition {
  id: string
  name: string
  description?: string
  conditions: Record<string, any>
  createdAt: number
  updatedAt: number
}

// 搜索参数接口
export interface SearchParams {
  keyword?: string
  title?: string
  code?: string
  category?: string
  dateRange?: [string, string] | []
  status?: string
  [key: string]: any
}

// 搜索结果接口
export interface SearchResult {
  id: string
  title: string
  code: string
  category: string
  createTime: string
  status: string
  content?: string
  highlights?: Record<string, string[]>
}

// 高级搜索组合式函数
export function useAdvancedSearch() {
  // 搜索模式
  const searchMode = ref<'simple' | 'advanced'>('simple')
  
  // 搜索参数
  const searchParams = reactive<SearchParams>({
    keyword: '',
    title: '',
    code: '',
    category: '',
    dateRange: [],
    status: ''
  })
  
  // 搜索结果
  const searchResults = ref<SearchResult[]>([])
  const isSearching = ref(false)
  const searchTotal = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)
  
  // 搜索建议
  const searchSuggestions = ref<SearchSuggestion[]>([])
  const showSuggestions = ref(false)
  
  // 搜索历史
  const searchHistory = ref<SearchHistory[]>([])
  
  // 保存的搜索条件
  const savedConditions = ref<SavedSearchCondition[]>([])
  
  // 计算属性
  const hasSearchParams = computed(() => {
    if (searchMode.value === 'simple') {
      return !!(searchParams.keyword || searchParams.category)
    } else {
      return !!(
        searchParams.title ||
        searchParams.code ||
        searchParams.category ||
        searchParams.dateRange?.length ||
        searchParams.status
      )
    }
  })
  
  const totalPages = computed(() => {
    return Math.ceil(searchTotal.value / pageSize.value)
  })
  
  // 搜索建议方法
  const fetchSuggestions = async (query: string): Promise<SearchSuggestion[]> => {
    if (!query || query.length < 2) {
      return []
    }
    
    // 模拟API调用
    return new Promise((resolve) => {
      setTimeout(() => {
        const suggestions: SearchSuggestion[] = [
          { id: '1', text: `${query}相关档案`, type: 'title' as const, count: 12 },
          { id: '2', text: `HR001-${query}`, type: 'code' as const, count: 5 },
          { id: '3', text: '人事档案', type: 'category' as const, count: 156 },
          { id: '4', text: '财务档案', type: 'category' as const, count: 89 }
        ].filter(item => 
          item.text.toLowerCase().includes(query.toLowerCase())
        )
        resolve(suggestions)
      }, 300)
    })
  }
  
  // 更新搜索建议
  const updateSuggestions = async (query: string) => {
    if (!query) {
      searchSuggestions.value = []
      showSuggestions.value = false
      return
    }
    
    try {
      const suggestions = await getSearchSuggestionsAPI(query)
      searchSuggestions.value = suggestions
      showSuggestions.value = suggestions.length > 0
    } catch (error) {
      console.error('获取搜索建议失败:', error)
      searchSuggestions.value = []
      showSuggestions.value = false
    }
  }
  
  // 执行搜索
  const performSearch = async (params?: Partial<SearchParams>) => {
    isSearching.value = true
    
    try {
      const searchData = { ...searchParams, ...params }
      
      // 调用真实API
      const response = await searchArchives(searchData, currentPage.value, pageSize.value)
      
      searchResults.value = response.list
      searchTotal.value = response.total
      
      // 添加到搜索历史
      addToHistory(searchData)
      
    } catch (error) {
      console.error('搜索失败:', error)
      searchResults.value = []
      searchTotal.value = 0
    } finally {
      isSearching.value = false
    }
  }
  
  // 重置搜索
  const resetSearch = () => {
    if (searchMode.value === 'simple') {
      searchParams.keyword = ''
      searchParams.category = ''
    } else {
      Object.assign(searchParams, {
        keyword: '',
        title: '',
        code: '',
        category: '',
        dateRange: [],
        status: ''
      })
    }
    searchResults.value = []
    searchTotal.value = 0
    currentPage.value = 1
  }
  
  // 添加到搜索历史
  const addToHistory = (params: SearchParams) => {
    const historyItem: SearchHistory = {
      id: Date.now().toString(),
      query: searchMode.value === 'simple' 
        ? params.keyword || '' 
        : `${params.title || ''} ${params.code || ''}`.trim(),
      timestamp: Date.now(),
      mode: searchMode.value,
      filters: { ...params }
    }
    
    // 避免重复
    const existingIndex = searchHistory.value.findIndex(
      item => JSON.stringify(item.filters) === JSON.stringify(params)
    )
    
    if (existingIndex > -1) {
      searchHistory.value.splice(existingIndex, 1)
    }
    
    searchHistory.value.unshift(historyItem)
    
    // 限制历史记录数量
    if (searchHistory.value.length > 20) {
      searchHistory.value = searchHistory.value.slice(0, 20)
    }
    
    // 保存到本地存储
    saveHistoryToStorage()
  }
  
  // 从历史记录搜索
  const searchFromHistory = (historyItem: SearchHistory) => {
    searchMode.value = historyItem.mode
    Object.assign(searchParams, historyItem.filters)
    performSearch()
  }
  
  // 清除搜索历史
  const clearHistory = () => {
    searchHistory.value = []
    localStorage.removeItem('archive-search-history')
  }
  
  // 保存搜索条件
  const saveSearchCondition = (name: string, description?: string) => {
    const condition: SavedSearchCondition = {
      id: Date.now().toString(),
      name,
      description,
      conditions: { ...searchParams },
      createdAt: Date.now(),
      updatedAt: Date.now()
    }
    
    savedConditions.value.unshift(condition)
    saveConditionsToStorage()
    
    return condition
  }
  
  // 应用保存的搜索条件
  const applySavedCondition = (condition: SavedSearchCondition) => {
    Object.assign(searchParams, condition.conditions)
    performSearch()
  }
  
  // 删除保存的搜索条件
  const deleteSavedCondition = (id: string) => {
    const index = savedConditions.value.findIndex(item => item.id === id)
    if (index > -1) {
      savedConditions.value.splice(index, 1)
      saveConditionsToStorage()
    }
  }
  
  // 本地存储方法
  const saveHistoryToStorage = () => {
    localStorage.setItem('archive-search-history', JSON.stringify(searchHistory.value))
  }
  
  const loadHistoryFromStorage = () => {
    try {
      const stored = localStorage.getItem('archive-search-history')
      if (stored) {
        searchHistory.value = JSON.parse(stored)
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error)
    }
  }
  
  const saveConditionsToStorage = () => {
    localStorage.setItem('archive-saved-conditions', JSON.stringify(savedConditions.value))
  }
  
  const loadConditionsFromStorage = () => {
    try {
      const stored = localStorage.getItem('archive-saved-conditions')
      if (stored) {
        savedConditions.value = JSON.parse(stored)
      }
    } catch (error) {
      console.error('加载保存的搜索条件失败:', error)
    }
  }
  
  // 初始化
  const initialize = () => {
    loadHistoryFromStorage()
    loadConditionsFromStorage()
  }
  
  // 监听搜索模式变化
  watch(searchMode, (newMode) => {
    if (newMode === 'simple') {
      // 切换到简单模式时，清除高级搜索的字段
      searchParams.title = ''
      searchParams.code = ''
      searchParams.dateRange = []
      searchParams.status = ''
    } else {
      // 切换到高级模式时，清除简单搜索的字段
      searchParams.keyword = ''
    }
  })
  
  return {
    // 状态
    searchMode,
    searchParams,
    searchResults,
    isSearching,
    searchTotal,
    currentPage,
    pageSize,
    searchSuggestions,
    showSuggestions,
    searchHistory,
    savedConditions,
    
    // 计算属性
    hasSearchParams,
    totalPages,
    
    // 方法
    updateSuggestions,
    performSearch,
    resetSearch,
    searchFromHistory,
    clearHistory,
    saveSearchCondition,
    applySavedCondition,
    deleteSavedCondition,
    initialize
  }
}