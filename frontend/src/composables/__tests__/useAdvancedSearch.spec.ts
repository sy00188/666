import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref, nextTick } from 'vue'
import { useAdvancedSearch } from '@/composables/useAdvancedSearch'

// Mock API
const mockSearchAPI = vi.fn()

describe('useAdvancedSearch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockSearchAPI.mockResolvedValue({
      data: [
        { id: 1, name: '档案1', category: '技术文档' },
        { id: 2, name: '档案2', category: '管理制度' }
      ],
      total: 2,
      page: 1,
      pageSize: 10
    })
  })

  it('应该正确初始化搜索状态', () => {
    const { searchForm, loading, results, pagination } = useAdvancedSearch(mockSearchAPI)

    expect(searchForm.value).toEqual({
      keyword: '',
      category: '',
      status: '',
      dateRange: [],
      tags: []
    })
    expect(loading.value).toBe(false)
    expect(results.value).toEqual([])
    expect(pagination.value).toEqual({
      page: 1,
      pageSize: 10,
      total: 0
    })
  })

  it('应该支持自定义初始搜索条件', () => {
    const initialForm = {
      keyword: '测试',
      category: 'tech',
      status: 'active'
    }

    const { searchForm } = useAdvancedSearch(mockSearchAPI, {
      initialForm
    })

    expect(searchForm.value).toEqual({
      keyword: '测试',
      category: 'tech',
      status: 'active',
      dateRange: [],
      tags: []
    })
  })

  it('应该执行搜索并更新结果', async () => {
    const { search, loading, results, pagination } = useAdvancedSearch(mockSearchAPI)

    const searchPromise = search()
    expect(loading.value).toBe(true)

    await searchPromise

    expect(loading.value).toBe(false)
    expect(mockSearchAPI).toHaveBeenCalledWith({
      keyword: '',
      category: '',
      status: '',
      dateRange: [],
      tags: [],
      page: 1,
      pageSize: 10
    })
    expect(results.value).toHaveLength(2)
    expect(pagination.value.total).toBe(2)
  })

  it('应该支持带参数的搜索', async () => {
    const { search } = useAdvancedSearch(mockSearchAPI)

    await search({
      keyword: '档案',
      category: '技术文档'
    })

    expect(mockSearchAPI).toHaveBeenCalledWith({
      keyword: '档案',
      category: '技术文档',
      status: '',
      dateRange: [],
      tags: [],
      page: 1,
      pageSize: 10
    })
  })

  it('应该处理搜索错误', async () => {
    const error = new Error('搜索失败')
    mockSearchAPI.mockRejectedValue(error)

    const { search, loading, error: searchError } = useAdvancedSearch(mockSearchAPI)

    await search()

    expect(loading.value).toBe(false)
    expect(searchError.value).toBe(error)
  })

  it('应该支持重置搜索条件', () => {
    const { searchForm, reset } = useAdvancedSearch(mockSearchAPI)

    // 修改搜索条件
    searchForm.value.keyword = '测试'
    searchForm.value.category = 'tech'
    searchForm.value.status = 'active'

    reset()

    expect(searchForm.value).toEqual({
      keyword: '',
      category: '',
      status: '',
      dateRange: [],
      tags: []
    })
  })

  it('应该支持清空结果', () => {
    const { results, pagination, clear } = useAdvancedSearch(mockSearchAPI)

    // 设置一些结果
    results.value = [{ id: 1, name: 'test' }]
    pagination.value.total = 1

    clear()

    expect(results.value).toEqual([])
    expect(pagination.value.total).toBe(0)
  })

  it('应该支持分页', async () => {
    const { pagination, changePage } = useAdvancedSearch(mockSearchAPI)

    await changePage(2)

    expect(pagination.value.page).toBe(2)
    expect(mockSearchAPI).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 2,
        pageSize: 10
      })
    )
  })

  it('应该支持改变页面大小', async () => {
    const { pagination, changePageSize } = useAdvancedSearch(mockSearchAPI)

    await changePageSize(20)

    expect(pagination.value.pageSize).toBe(20)
    expect(pagination.value.page).toBe(1) // 应该重置到第一页
    expect(mockSearchAPI).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 1,
        pageSize: 20
      })
    )
  })

  it('应该支持添加搜索标签', () => {
    const { searchForm, addTag } = useAdvancedSearch(mockSearchAPI)

    addTag('重要')
    addTag('紧急')

    expect(searchForm.value.tags).toEqual(['重要', '紧急'])
  })

  it('应该支持移除搜索标签', () => {
    const { searchForm, addTag, removeTag } = useAdvancedSearch(mockSearchAPI)

    addTag('重要')
    addTag('紧急')
    addTag('普通')

    removeTag('紧急')

    expect(searchForm.value.tags).toEqual(['重要', '普通'])
  })

  it('应该支持清空所有标签', () => {
    const { searchForm, addTag, clearTags } = useAdvancedSearch(mockSearchAPI)

    addTag('重要')
    addTag('紧急')

    clearTags()

    expect(searchForm.value.tags).toEqual([])
  })

  it('应该支持搜索历史', () => {
    const { searchHistory, addToHistory } = useAdvancedSearch(mockSearchAPI, {
      enableHistory: true
    })

    const searchParams = {
      keyword: '档案',
      category: '技术文档'
    }

    addToHistory(searchParams)

    expect(searchHistory.value).toContainEqual(
      expect.objectContaining(searchParams)
    )
  })

  it('应该限制搜索历史数量', () => {
    const { searchHistory, addToHistory } = useAdvancedSearch(mockSearchAPI, {
      enableHistory: true,
      maxHistorySize: 3
    })

    // 添加4条历史记录
    for (let i = 1; i <= 4; i++) {
      addToHistory({ keyword: `搜索${i}` })
    }

    expect(searchHistory.value).toHaveLength(3)
    expect(searchHistory.value[0].keyword).toBe('搜索4') // 最新的在前面
  })

  it('应该支持从历史记录搜索', async () => {
    const { addToHistory, searchFromHistory } = useAdvancedSearch(mockSearchAPI, {
      enableHistory: true
    })

    const historyItem = {
      keyword: '历史搜索',
      category: '技术文档'
    }

    addToHistory(historyItem)
    await searchFromHistory(0) // 使用第一条历史记录

    expect(mockSearchAPI).toHaveBeenCalledWith(
      expect.objectContaining(historyItem)
    )
  })

  it('应该支持搜索建议', async () => {
    const mockSuggestAPI = vi.fn().mockResolvedValue([
      '档案管理',
      '档案分类',
      '档案查询'
    ])

    const { suggestions, getSuggestions } = useAdvancedSearch(mockSearchAPI, {
      suggestAPI: mockSuggestAPI
    })

    await getSuggestions('档案')

    expect(mockSuggestAPI).toHaveBeenCalledWith('档案')
    expect(suggestions.value).toEqual(['档案管理', '档案分类', '档案查询'])
  })

  it('应该支持防抖搜索', async () => {
    vi.useFakeTimers()

    const { debouncedSearch } = useAdvancedSearch(mockSearchAPI, {
      debounceMs: 300
    })

    // 快速调用多次
    debouncedSearch({ keyword: '测试1' })
    debouncedSearch({ keyword: '测试2' })
    debouncedSearch({ keyword: '测试3' })

    // 等待防抖时间
    vi.advanceTimersByTime(300)
    await nextTick()

    // 只应该调用最后一次
    expect(mockSearchAPI).toHaveBeenCalledTimes(1)
    expect(mockSearchAPI).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: '测试3' })
    )

    vi.useRealTimers()
  })

  it('应该支持搜索条件验证', async () => {
    const validator = vi.fn().mockReturnValue({
      valid: false,
      errors: ['关键词不能为空']
    })

    const { search, validationErrors } = useAdvancedSearch(mockSearchAPI, {
      validator
    })

    await search()

    expect(validator).toHaveBeenCalled()
    expect(mockSearchAPI).not.toHaveBeenCalled()
    expect(validationErrors.value).toEqual(['关键词不能为空'])
  })

  it('应该支持搜索结果缓存', async () => {
    const { search } = useAdvancedSearch(mockSearchAPI, {
      enableCache: true
    })

    const searchParams = { keyword: '档案' }

    // 第一次搜索
    await search(searchParams)
    // 第二次相同搜索
    await search(searchParams)

    // API只应该被调用一次
    expect(mockSearchAPI).toHaveBeenCalledTimes(1)
  })

  it('应该支持导出搜索结果', async () => {
    const mockExportAPI = vi.fn().mockResolvedValue('export-url')

    const { search, exportResults } = useAdvancedSearch(mockSearchAPI, {
      exportAPI: mockExportAPI
    })

    await search({ keyword: '档案' })
    const exportUrl = await exportResults('excel')

    expect(mockExportAPI).toHaveBeenCalledWith({
      format: 'excel',
      searchParams: expect.objectContaining({ keyword: '档案' }),
      results: expect.any(Array)
    })
    expect(exportUrl).toBe('export-url')
  })
})
