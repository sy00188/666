import { request } from '@/utils/request'
import type { 
  SearchSuggestion, 
  SearchParams, 
  SearchResult,
  SearchHistory,
  SavedSearchCondition 
} from '@/composables/useAdvancedSearch'

// API响应接口
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 搜索结果响应接口
export interface SearchResponse {
  list: SearchResult[]
  total: number
  page: number
  pageSize: number
}

// 搜索建议响应接口
export interface SuggestionResponse {
  suggestions: SearchSuggestion[]
}

/**
 * 获取搜索建议
 * @param query 搜索关键词
 * @param type 建议类型，可选
 */
export const getSearchSuggestions = async (
  query: string, 
  type?: 'title' | 'code' | 'category' | 'content'
): Promise<SearchSuggestion[]> => {
  try {
    const response = await request.get<ApiResponse<SuggestionResponse>>('/api/search/suggestions', {
      params: { query, type }
    })
    
    if (response.data.code === 200) {
      return response.data.data.suggestions
    } else {
      console.error('获取搜索建议失败:', response.data.message)
      return []
    }
  } catch (error) {
    console.error('搜索建议API调用失败:', error)
    
    // 返回模拟数据作为降级方案
    return getMockSuggestions(query, type)
  }
}

/**
 * 执行档案搜索
 * @param params 搜索参数
 * @param page 页码
 * @param pageSize 每页大小
 */
export const searchArchives = async (
  params: SearchParams,
  page: number = 1,
  pageSize: number = 20
): Promise<SearchResponse> => {
  try {
    const response = await request.post<ApiResponse<SearchResponse>>('/api/search/archives', {
      ...params,
      page,
      pageSize
    })
    
    if (response.data.code === 200) {
      return response.data.data
    } else {
      throw new Error(response.data.message || '搜索失败')
    }
  } catch (error) {
    console.error('档案搜索API调用失败:', error)
    
    // 返回模拟数据作为降级方案
    return getMockSearchResults(params, page, pageSize)
  }
}

/**
 * 获取热门搜索关键词
 */
export const getHotSearchKeywords = async (): Promise<string[]> => {
  try {
    const response = await request.get<ApiResponse<{ keywords: string[] }>>('/api/search/hot-keywords')
    
    if (response.data.code === 200) {
      return response.data.data.keywords
    } else {
      return []
    }
  } catch (error) {
    console.error('获取热门搜索关键词失败:', error)
    
    // 返回模拟数据
    return [
      '人事档案',
      '财务报表',
      '合同文件',
      '会议纪要',
      '项目资料'
    ]
  }
}

/**
 * 保存搜索历史到服务器（可选）
 * @param history 搜索历史记录
 */
export const saveSearchHistory = async (history: SearchHistory): Promise<boolean> => {
  try {
    const response = await request.post<ApiResponse>('/api/search/history', history)
    return response.data.code === 200
  } catch (error) {
    console.error('保存搜索历史失败:', error)
    return false
  }
}

/**
 * 获取用户搜索历史
 */
export const getUserSearchHistory = async (): Promise<SearchHistory[]> => {
  try {
    const response = await request.get<ApiResponse<{ history: SearchHistory[] }>>('/api/search/history')
    
    if (response.data.code === 200) {
      return response.data.data.history
    } else {
      return []
    }
  } catch (error) {
    console.error('获取搜索历史失败:', error)
    return []
  }
}

/**
 * 保存搜索条件到服务器
 * @param condition 搜索条件
 */
export const saveSearchCondition = async (condition: SavedSearchCondition): Promise<boolean> => {
  try {
    const response = await request.post<ApiResponse>('/api/search/conditions', condition)
    return response.data.code === 200
  } catch (error) {
    console.error('保存搜索条件失败:', error)
    return false
  }
}

/**
 * 获取用户保存的搜索条件
 */
export const getUserSearchConditions = async (): Promise<SavedSearchCondition[]> => {
  try {
    const response = await request.get<ApiResponse<{ conditions: SavedSearchCondition[] }>>('/api/search/conditions')
    
    if (response.data.code === 200) {
      return response.data.data.conditions
    } else {
      return []
    }
  } catch (error) {
    console.error('获取保存的搜索条件失败:', error)
    return []
  }
}

/**
 * 删除保存的搜索条件
 * @param id 条件ID
 */
export const deleteSearchCondition = async (id: string): Promise<boolean> => {
  try {
    const response = await request.delete<ApiResponse>(`/api/search/conditions/${id}`)
    return response.data.code === 200
  } catch (error) {
    console.error('删除搜索条件失败:', error)
    return false
  }
}

// ============ 模拟数据函数 ============

/**
 * 模拟搜索建议数据
 */
const getMockSuggestions = (query: string, type?: string): SearchSuggestion[] => {
  if (!query || query.length < 2) return []
  
  const suggestions: SearchSuggestion[] = [
    { id: '1', text: `${query}相关档案`, type: 'title' as const, count: 12 },
    { id: '2', text: `HR001-${query}`, type: 'code' as const, count: 5 },
    { id: '3', text: '人事档案', type: 'category' as const, count: 156 },
    { id: '4', text: '财务档案', type: 'category' as const, count: 89 },
    { id: '5', text: '合同档案', type: 'category' as const, count: 67 },
    { id: '6', text: `${query}内容摘要`, type: 'content' as const, count: 23 }
  ]
  
  return suggestions.filter(item => {
    const matchesQuery = item.text.toLowerCase().includes(query.toLowerCase())
    const matchesType = !type || item.type === type
    return matchesQuery && matchesType
  }).slice(0, 8) // 限制返回数量
}

/**
 * 模拟搜索结果数据
 */
const getMockSearchResults = (params: SearchParams, page: number, pageSize: number): SearchResponse => {
  const mockData: SearchResult[] = [
    {
      id: '1',
      title: '员工入职档案',
      code: 'HR001-001',
      category: '人事档案',
      createTime: '2024-01-15',
      status: '正常',
      content: '员工张三的入职档案，包含个人信息、学历证明、工作经历等相关材料',
      highlights: {
        title: ['员工', '入职'],
        content: ['张三', '个人信息', '学历证明']
      }
    },
    {
      id: '2',
      title: '财务报表档案',
      code: 'FIN001-001',
      category: '财务档案',
      createTime: '2024-01-20',
      status: '已归档',
      content: '2023年度财务报表及相关凭证，包含损益表、资产负债表等',
      highlights: {
        title: ['财务', '报表'],
        content: ['2023年度', '凭证', '损益表']
      }
    },
    {
      id: '3',
      title: '项目合同档案',
      code: 'CON001-001',
      category: '合同档案',
      createTime: '2024-01-25',
      status: '正常',
      content: 'ABC项目合作协议及相关附件，合同金额500万元',
      highlights: {
        title: ['项目', '合同'],
        content: ['ABC项目', '协议', '500万元']
      }
    },
    {
      id: '4',
      title: '会议纪要档案',
      code: 'MTG001-001',
      category: '会议档案',
      createTime: '2024-02-01',
      status: '正常',
      content: '2024年第一季度工作会议纪要，讨论了年度计划和预算安排',
      highlights: {
        title: ['会议', '纪要'],
        content: ['第一季度', '年度计划', '预算']
      }
    },
    {
      id: '5',
      title: '技术文档档案',
      code: 'TEC001-001',
      category: '技术档案',
      createTime: '2024-02-05',
      status: '正常',
      content: '系统架构设计文档，包含数据库设计、接口规范等技术资料',
      highlights: {
        title: ['技术', '文档'],
        content: ['系统架构', '数据库设计', '接口规范']
      }
    }
  ]
  
  // 根据搜索参数过滤数据
  let filteredData = mockData
  
  if (params.keyword) {
    const keyword = params.keyword.toLowerCase()
    filteredData = filteredData.filter(item => 
      item.title.toLowerCase().includes(keyword) ||
      item.content?.toLowerCase().includes(keyword) ||
      item.code.toLowerCase().includes(keyword)
    )
  }
  
  if (params.title) {
    filteredData = filteredData.filter(item => 
      item.title.toLowerCase().includes(params.title!.toLowerCase())
    )
  }
  
  if (params.code) {
    filteredData = filteredData.filter(item => 
      item.code.toLowerCase().includes(params.code!.toLowerCase())
    )
  }
  
  if (params.category) {
    filteredData = filteredData.filter(item => 
      item.category === params.category
    )
  }
  
  if (params.status) {
    filteredData = filteredData.filter(item => 
      item.status === params.status
    )
  }
  
  // 分页处理
  const total = filteredData.length
  const startIndex = (page - 1) * pageSize
  const endIndex = startIndex + pageSize
  const list = filteredData.slice(startIndex, endIndex)
  
  return {
    list,
    total,
    page,
    pageSize
  }
}