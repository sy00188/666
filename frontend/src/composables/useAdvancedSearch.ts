import { ref, reactive, computed } from 'vue'
import { debounce } from 'lodash-es'

export interface SearchForm {
  keyword: string
  category: string
  status: string
  dateRange: any[]
  tags: string[]
}

export interface Pagination {
  page: number
  pageSize: number
  total: number
}

export interface SearchOptions {
  initialForm?: Partial<SearchForm>
  enableHistory?: boolean
  maxHistorySize?: number
  suggestAPI?: (query: string) => Promise<string[]>
  debounceMs?: number
  validator?: (form: SearchForm) => { valid: boolean; errors: string[] }
  enableCache?: boolean
  exportAPI?: (params: any) => Promise<string>
}

export function useAdvancedSearch(
  searchAPI: (params: any) => Promise<any>,
  options: SearchOptions = {}
) {
  // 搜索表单
  const searchForm = ref<SearchForm>({
    keyword: '',
    category: '',
    status: '',
    dateRange: [],
    tags: [],
    ...options.initialForm
  })

  // 加载状态
  const loading = ref(false)
  
  // 搜索结果
  const results = ref<any[]>([])
  
  // 分页信息
  const pagination = ref<Pagination>({
    page: 1,
    pageSize: 10,
    total: 0
  })

  // 错误信息
  const error = ref<Error | null>(null)

  // 搜索历史
  const searchHistory = ref<any[]>([])

  // 搜索建议
  const suggestions = ref<string[]>([])

  // 验证错误
  const validationErrors = ref<string[]>([])

  // 缓存
  const cache = new Map<string, any>()

  // 执行搜索
  const search = async (params?: Partial<SearchForm>) => {
    loading.value = true
    error.value = null
    validationErrors.value = []

    try {
      const searchParams = { ...searchForm.value, ...params, ...pagination.value }

      // 验证搜索条件
      if (options.validator) {
        const validation = options.validator(searchForm.value)
        if (!validation.valid) {
          validationErrors.value = validation.errors
          return
        }
      }

      // 检查缓存
      const cacheKey = JSON.stringify(searchParams)
      if (options.enableCache && cache.has(cacheKey)) {
        const cachedResult = cache.get(cacheKey)
        results.value = cachedResult.data
        pagination.value.total = cachedResult.total
        return
      }

      // 调用API
      const response = await searchAPI(searchParams)
      
      results.value = response.data || []
      pagination.value.total = response.total || 0

      // 缓存结果
      if (options.enableCache) {
        cache.set(cacheKey, { data: results.value, total: pagination.value.total })
      }

      // 添加到历史记录
      if (options.enableHistory) {
        addToHistory(searchParams)
      }

    } catch (err) {
      error.value = err as Error
      results.value = []
      pagination.value.total = 0
    } finally {
      loading.value = false
    }
  }

  // 重置搜索条件
  const reset = () => {
    searchForm.value = {
      keyword: '',
      category: '',
      status: '',
      dateRange: [],
      tags: []
    }
  }

  // 清空结果
  const clear = () => {
    results.value = []
    pagination.value.total = 0
  }

  // 分页
  const changePage = async (page: number) => {
    pagination.value.page = page
    await search()
  }

  // 改变页面大小
  const changePageSize = async (pageSize: number) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    await search()
  }

  // 标签管理
  const addTag = (tag: string) => {
    if (!searchForm.value.tags.includes(tag)) {
      searchForm.value.tags.push(tag)
    }
  }

  const removeTag = (tag: string) => {
    const index = searchForm.value.tags.indexOf(tag)
    if (index > -1) {
      searchForm.value.tags.splice(index, 1)
    }
  }

  const clearTags = () => {
    searchForm.value.tags = []
  }

  // 搜索历史
  const addToHistory = (params: any) => {
    if (!options.enableHistory) return

    const historyItem = {
      ...params,
      timestamp: Date.now()
    }

    // 移除重复项
    const existingIndex = searchHistory.value.findIndex(
      item => JSON.stringify(item) === JSON.stringify(historyItem)
    )
    if (existingIndex > -1) {
      searchHistory.value.splice(existingIndex, 1)
    }

    // 添加到开头
    searchHistory.value.unshift(historyItem)

    // 限制数量
    const maxSize = options.maxHistorySize || 10
    if (searchHistory.value.length > maxSize) {
      searchHistory.value = searchHistory.value.slice(0, maxSize)
    }
  }

  const searchFromHistory = async (index: number) => {
    if (index >= 0 && index < searchHistory.value.length) {
      const historyItem = searchHistory.value[index]
      Object.assign(searchForm.value, historyItem)
      await search()
    }
  }

  // 搜索建议
  const getSuggestions = async (query: string) => {
    if (!options.suggestAPI) return

    try {
      const result = await options.suggestAPI(query)
      suggestions.value = result
    } catch (err) {
      console.error('获取搜索建议失败:', err)
      suggestions.value = []
    }
  }

  // 防抖搜索
  const debouncedSearch = debounce(search, options.debounceMs || 300)

  // 导出结果
  const exportResults = async (format: string) => {
    if (!options.exportAPI) return null

    try {
      return await options.exportAPI({
        format,
        searchParams: searchForm.value,
        results: results.value
      })
    } catch (err) {
      console.error('导出失败:', err)
      return null
    }
  }

  return {
    searchForm,
    loading,
    results,
    pagination,
    error,
    searchHistory,
    suggestions,
    validationErrors,
    search,
    reset,
    clear,
    changePage,
    changePageSize,
    addTag,
    removeTag,
    clearTags,
    addToHistory,
    searchFromHistory,
    getSuggestions,
    debouncedSearch,
    exportResults
  }
}