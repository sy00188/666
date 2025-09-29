<template>
  <div class="archive-search">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>档案搜索</h2>
          <p>通过多种条件搜索档案</p>
        </div>
        <div class="mode-switch">
          <el-radio-group v-model="searchMode" size="small">
            <el-radio-button label="simple">快速搜索</el-radio-button>
            <el-radio-button label="advanced">高级搜索</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div class="search-content">
      <!-- 快速搜索模式 -->
      <el-card v-if="searchMode === 'simple'">
        <template #header>
          <div class="card-header">
            <span>快速搜索</span>
            <el-tag size="small" type="info">简单模式</el-tag>
          </div>
        </template>

        <div class="simple-search">
          <SearchBox
            v-model="simpleKeyword"
            :loading="isSearching"
            :suggestions="searchSuggestions"
            :hot-keywords="hotKeywords"
            placeholder="输入关键词搜索文档..."
            size="large"
            @search="handleSimpleSearch"
            @suggestion-select="handleSuggestionSelect"
            @hot-keyword-select="handleHotKeywordSelect"
          />
        </div>
      </el-card>

      <!-- 高级搜索模式 -->
      <el-card v-else-if="searchMode === 'advanced'" class="search-content">
        <template #header>
          <div class="card-header">
            <span>高级搜索</span>
            <div class="header-actions">
              <el-button 
                type="text" 
                size="small"
                @click="showSearchHistory = !showSearchHistory"
              >
                <el-icon><Clock /></el-icon>
                搜索历史
              </el-button>
              <el-button 
                type="text" 
                size="small"
                @click="showSavedConditions = !showSavedConditions"
              >
                <el-icon><Star /></el-icon>
                保存的条件
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="advanced-search-layout">
            <!-- 左侧搜索表单 -->
            <div class="search-form-section">
              <el-form :model="searchForm" label-width="80px">
                <el-form-item label="档案标题">
                  <el-input 
                    v-model="searchForm.title" 
                    placeholder="请输入档案标题"
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="档案编号">
                  <el-input 
                    v-model="searchForm.code" 
                    placeholder="请输入档案编号"
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="档案分类">
                  <el-select 
                    v-model="searchForm.category" 
                    placeholder="请选择档案分类"
                    clearable
                    style="width: 100%"
                  >
                    <el-option label="人事档案" value="人事档案" />
                    <el-option label="财务档案" value="财务档案" />
                    <el-option label="合同档案" value="合同档案" />
                    <el-option label="会议档案" value="会议档案" />
                    <el-option label="技术档案" value="技术档案" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="创建时间">
                  <el-date-picker
                    v-model="searchForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="档案状态">
                  <el-select 
                    v-model="searchForm.status" 
                    placeholder="请选择档案状态"
                    clearable
                    style="width: 100%"
                  >
                    <el-option label="正常" value="正常" />
                    <el-option label="已归档" value="已归档" />
                    <el-option label="借出中" value="借出中" />
                    <el-option label="维护中" value="维护中" />
                  </el-select>
                </el-form-item>
                
                <el-form-item>
                  <div class="form-actions">
                    <el-button 
                      type="primary" 
                      @click="handleSearch"
                      :loading="isSearching"
                    >
                      <el-icon><Search /></el-icon>
                      搜索
                    </el-button>
                    <el-button @click="handleReset">
                      <el-icon><RefreshRight /></el-icon>
                      重置
                    </el-button>
                    <el-button 
                      v-if="hasSearchParams"
                      type="success"
                      @click="handleSaveCondition"
                    >
                      <el-icon><Star /></el-icon>
                      保存条件
                    </el-button>
                  </div>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 右侧历史记录和保存的条件 -->
            <div class="search-sidebar">
              <!-- 搜索历史 -->
              <div v-show="showSearchHistory" class="sidebar-section">
                <SearchHistory
                  :history-list="searchHistory"
                  @apply-history="applySearchHistory"
                  @delete-history="deleteSearchHistory"
                  @clear-all="clearSearchHistory"
                />
              </div>
              
              <!-- 保存的条件 -->
              <div v-show="showSavedConditions" class="sidebar-section">
                <SavedConditions
                  :conditions-list="savedConditions"
                  :current-conditions="searchForm"
                  @apply-condition="handleApplySavedCondition"
                  @save-condition="handleSaveCondition"
                  @update-condition="handleUpdateSearchCondition"
                  @delete-condition="handleDeleteSavedCondition"
                />
              </div>
            </div>
          </div>
      </el-card>

      <!-- 搜索结果 -->
      <div class="search-results-section" v-if="searchResults.length > 0 || isSearching">
        <SearchResults
          :results="searchResults"
          :loading="isSearching"
          :total="searchTotal"
          :current-page="currentPage"
          :page-size="pageSize"
          :search-time="searchTime"
          @page-change="handlePageChange"
          @size-change="handlePageSizeChange"
          @item-click="handleResultItemClick"
          @item-view="handleResultItemView"
          @item-edit="handleResultItemEdit"
        />
      </div>
    </div>
  </div>
  
  <!-- 保存搜索条件对话框 -->
  <el-dialog
    v-model="showSaveConditionDialog"
    title="保存搜索条件"
    width="500px"
    :before-close="resetSaveConditionForm"
  >
    <el-form :model="saveConditionForm" label-width="80px">
      <el-form-item label="条件名称" required>
        <el-input
          v-model="saveConditionForm.name"
          placeholder="请输入条件名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="saveConditionForm.description"
          type="textarea"
          placeholder="请输入条件描述（可选）"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showSaveConditionDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveCondition">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { Search, RefreshRight, Star, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SearchBox from '@/components/search/SearchBox.vue'
import SearchHistory from '@/components/search/SearchHistory.vue'
import SavedConditions from '@/components/search/SavedConditions.vue'
import { useAdvancedSearch } from '@/composables/useAdvancedSearch'

// 使用高级搜索组合式函数
const {
  searchMode,
  searchParams,
  searchResults,
  isSearching,
  searchTotal,
  searchSuggestions,
  showSuggestions,
  searchHistory,
  savedConditions,
  hasSearchParams,
  updateSuggestions,
  performSearch,
  resetSearch,
  searchFromHistory,
  clearHistory,
  saveSearchCondition,
  applySavedCondition,
  deleteSavedCondition,
  initialize
} = useAdvancedSearch()

// 兼容原有的searchForm结构
const searchForm = searchParams

// 简单搜索关键词
const simpleKeyword = ref('')

// 侧边栏显示状态
const showSearchHistory = ref(false)
const showSavedConditions = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => searchResults.value.length)

// 热门关键词数据
const hotKeywords = ref(['人事档案', '财务报表', '合同文件', '会议纪要', '项目资料'])

// 搜索建议相关方法
const handleSuggestionSelect = (suggestion: any) => {
  simpleKeyword.value = suggestion.text
  handleSearch()
}

const handleHotKeywordSelect = (keyword: string) => {
  simpleKeyword.value = keyword
  handleSearch()
}

const handleSimpleSearch = (query: string) => {
  simpleKeyword.value = query
  handleSearch()
}

// 搜索处理
const handleSearch = async () => {
  if (searchMode.value === 'simple') {
    if (!simpleKeyword.value.trim()) {
      ElMessage.warning('请输入搜索关键词')
      return
    }
    // 简单搜索逻辑
    searchForm.title = simpleKeyword.value
    searchForm.code = ''
    searchForm.category = ''
    searchForm.status = ''
    searchForm.dateRange = []
  }
  
  await performSearch()
}

// 重置搜索
const handleReset = () => {
  if (searchMode.value === 'simple') {
    simpleKeyword.value = ''
  }
  resetSearch()
}

// 保存搜索条件对话框状态
const showSaveConditionDialog = ref(false)
const saveConditionForm = reactive({
  name: '',
  description: ''
})

// 保存搜索条件
const handleSaveCondition = async () => {
  if (!hasSearchParams.value) {
    ElMessage.warning('请先设置搜索条件')
    return
  }
  showSaveConditionDialog.value = true
}

// 确认保存搜索条件
const confirmSaveCondition = async () => {
  if (!saveConditionForm.name.trim()) {
    ElMessage.warning('请输入条件名称')
    return
  }
  
  try {
    await saveSearchCondition(saveConditionForm.name, saveConditionForm.description)
    ElMessage.success('搜索条件保存成功')
    showSaveConditionDialog.value = false
    resetSaveConditionForm()
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  }
}

// 重置保存条件表单
const resetSaveConditionForm = () => {
  saveConditionForm.name = ''
  saveConditionForm.description = ''
}

// 搜索时间记录
const searchTime = ref(0)

// 分页处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  performSearch()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  performSearch()
}

// 搜索结果项目处理
const handleResultItemClick = (item: any) => {
  console.log('点击搜索结果项:', item)
}

const handleResultItemView = (item: any) => {
  console.log('查看详情:', item)
  // TODO: 跳转到详情页面
}

const handleResultItemEdit = (item: any) => {
  console.log('编辑项目:', item)
  // TODO: 跳转到编辑页面
}

// 搜索历史相关方法
const applySearchHistory = (history: any) => {
  // 应用历史搜索条件
  if (history.mode === 'simple') {
    searchMode.value = 'simple'
    simpleKeyword.value = history.query
  } else {
    searchMode.value = 'advanced'
    Object.assign(searchForm, history.filters)
  }
  handleSearch()
}

const deleteSearchHistory = (historyId: string) => {
  // 删除搜索历史
  const index = searchHistory.value.findIndex(h => h.id === historyId)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
    ElMessage.success('删除成功')
  }
}

const clearSearchHistory = () => {
  clearHistory()
  ElMessage.success('清空成功')
}

// 保存条件相关方法 - 使用组合式函数提供的方法
const handleApplySavedCondition = (condition: any) => {
  applySavedCondition(condition)
}

const handleUpdateSearchCondition = (data: { id: string; name: string; description?: string; conditions: Record<string, any> }) => {
  // 这里需要调用API更新条件
  ElMessage.success('更新成功')
}

const handleDeleteSavedCondition = (conditionId: string) => {
  deleteSavedCondition(conditionId)
  ElMessage.success('删除成功')
}

// 结果高亮
const highlightText = (text: string, keyword: string) => {
  if (!keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
}

// 计算当前页显示的数据
const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return searchResults.value.slice(start, end)
})

// 初始化
onMounted(() => {
  initialize()
})
</script>

<style lang="scss" scoped>
.archive-search {
  padding: $spacing-lg;
  
  @media (max-width: 768px) {
    padding: $spacing-md;
  }
  
  @media (max-width: 480px) {
    padding: $spacing-sm;
  }
}

.page-header {
  margin-bottom: $spacing-lg;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title-section {
      h2 {
        margin: 0 0 $spacing-sm 0;
        color: $text-primary;
        
        @media (max-width: 768px) {
          font-size: 1.5rem;
        }
        
        @media (max-width: 480px) {
          font-size: 1.25rem;
        }
      }

      p {
        margin: 0;
        color: $text-secondary;
        
        @media (max-width: 480px) {
          font-size: 14px;
        }
      }
    }

    .mode-switch {
      .el-radio-group {
        --el-radio-button-checked-bg-color: var(--el-color-primary);
        --el-radio-button-checked-text-color: #fff;
      }
    }
  }
}

.search-content {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: $spacing-sm;
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .header-actions {
      display: flex;
      gap: $spacing-sm;
      
      @media (max-width: 768px) {
        width: 100%;
        justify-content: center;
      }
    }
  }
  
  .result-count {
    color: #909399;
    font-size: 14px;
  }
  
  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    
    @media (max-width: 768px) {
      .el-pagination {
        .el-pagination__sizes {
          display: none;
        }
        
        .el-pagination__jump {
          display: none;
        }
      }
    }
  }
  
  :deep(mark) {
    background-color: #fff2cc;
    color: #e6a23c;
    padding: 0 2px;
    border-radius: 2px;
  }
}

// 高级搜索布局
.advanced-search-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  
  @media (max-width: 1200px) {
    grid-template-columns: 1fr 250px;
    gap: 16px;
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .search-form-section {
    .el-form {
      .el-form-item {
        margin-bottom: 20px;
        
        @media (max-width: 768px) {
          margin-bottom: 16px;
        }
        
        @media (max-width: 480px) {
          .el-form-item__label {
            width: 70px !important;
            font-size: 14px;
          }
        }
      }
    }
    
    .form-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      @media (max-width: 768px) {
        justify-content: center;
        
        .el-button {
          flex: 1;
          min-width: 80px;
        }
      }
    }
  }
  
  .search-sidebar {
    @media (max-width: 768px) {
      order: -1;
    }
    
    .sidebar-section {
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

// 搜索结果表格响应式
.search-results-card {
  .el-table {
    @media (max-width: 768px) {
      .el-table__header {
        font-size: 14px;
      }
      
      .el-table__body {
        font-size: 14px;
      }
      
      .el-table-column--selection {
        width: 40px;
      }
    }
    
    @media (max-width: 480px) {
      .el-table__header,
      .el-table__body {
        font-size: 12px;
      }
      
      .el-button {
        padding: 4px 8px;
        font-size: 12px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .page-header .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-md;
  }
  
  .mode-switch {
    width: 100%;
    
    .el-radio-group {
      width: 100%;
      
      .el-radio-button {
        flex: 1;
      }
    }
  }
  
  .el-row {
    .el-col {
      margin-bottom: $spacing-sm;
    }
  }
}

@media (max-width: 480px) {
  .el-card {
    .el-card__header {
      padding: 12px 16px;
      font-size: 14px;
    }
    
    .el-card__body {
      padding: 16px;
    }
  }
  
  .el-form {
    .el-form-item {
      .el-input,
      .el-select,
      .el-date-picker {
        font-size: 14px;
      }
    }
  }
  
  .el-button {
    font-size: 14px;
    padding: 8px 12px;
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .search-content {
    :deep(mark) {
      background-color: rgba(230, 162, 60, 0.3);
      color: #f0c674;
    }
  }
}
</style>
