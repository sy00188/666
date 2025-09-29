<template>
  <div class="search-history">
    <div class="history-header">
      <h4>搜索历史</h4>
      <el-button 
        v-if="historyList.length > 0"
        type="text" 
        size="small" 
        @click="clearAllHistory"
      >
        清空历史
      </el-button>
    </div>
    
    <div v-if="historyList.length === 0" class="empty-state">
      <el-empty 
        description="暂无搜索历史" 
        :image-size="80"
      />
    </div>
    
    <div v-else class="history-list">
      <div 
        v-for="item in historyList" 
        :key="item.id"
        class="history-item"
        @click="applyHistory(item)"
      >
        <div class="history-content">
          <div class="history-query">
            <el-icon class="history-icon">
              <Clock />
            </el-icon>
            <span class="query-text">{{ formatHistoryQuery(item) }}</span>
          </div>
          <div class="history-meta">
            <span class="search-time">{{ formatTime(item.timestamp) }}</span>
            <span class="result-count">{{ (item.filters?.resultCount || 0) }} 条结果</span>
          </div>
        </div>
        <el-button 
          type="text" 
          size="small"
          class="delete-btn"
          @click.stop="deleteHistory(item.id)"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Clock, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SearchHistory } from '@/composables/useAdvancedSearch'

interface Props {
  historyList: SearchHistory[]
}

interface Emits {
  (e: 'apply-history', history: SearchHistory): void
  (e: 'delete-history', id: string): void
  (e: 'clear-all'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

/**
 * 格式化历史记录查询条件显示
 */
const formatHistoryQuery = (history: SearchHistory): string => {
  if (history.mode === 'simple') {
    return `关键词: ${history.query}`
  }
  
  // 高级搜索模式，从filters中获取条件
  const filters = history.filters || {}
  const parts: string[] = []
  
  if (filters.keyword) {
    parts.push(`关键词: ${filters.keyword}`)
  }
  
  if (filters.title) {
    parts.push(`标题: ${filters.title}`)
  }
  
  if (filters.code) {
    parts.push(`编号: ${filters.code}`)
  }
  
  if (filters.category) {
    parts.push(`分类: ${filters.category}`)
  }
  
  if (filters.status) {
    parts.push(`状态: ${filters.status}`)
  }
  
  if (filters.startDate && filters.endDate) {
    parts.push(`时间: ${filters.startDate} ~ ${filters.endDate}`)
  }
  
  return parts.length > 0 ? parts.join(', ') : history.query || '全部档案'
}

/**
 * 格式化时间显示
 */
const formatTime = (timestamp: number): string => {
  const now = Date.now()
  const diff = now - timestamp
  
  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))} 分钟前`
  } else if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))} 小时前`
  } else {
    const date = new Date(timestamp)
    return `${date.getMonth() + 1}/${date.getDate()}`
  }
}

/**
 * 应用历史搜索条件
 */
const applyHistory = (history: SearchHistory) => {
  emit('apply-history', history)
}

/**
 * 删除单条历史记录
 */
const deleteHistory = async (id: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条搜索历史吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    emit('delete-history', id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

/**
 * 清空所有历史记录
 */
const clearAllHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有搜索历史吗？此操作不可恢复。',
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    emit('clear-all')
    ElMessage.success('清空成功')
  } catch {
    // 用户取消清空
  }
}
</script>

<style scoped lang="scss">
.search-history {
  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h4 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: $text-primary;
    }
  }
  
  .empty-state {
    padding: 20px 0;
    text-align: center;
  }
  
  .history-list {
    .history-item {
      display: flex;
      align-items: center;
      padding: 12px;
      margin-bottom: 8px;
      background: $bg-color;
      border: 1px solid $border-lighter;
      border-radius: $border-radius-base;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: $primary-color;
        background: rgba($primary-color, 0.05);
        
        .delete-btn {
          opacity: 1;
        }
      }
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .history-content {
        flex: 1;
        min-width: 0;
        
        .history-query {
          display: flex;
          align-items: center;
          margin-bottom: 4px;
          
          .history-icon {
            margin-right: 8px;
            color: $text-secondary;
            font-size: 14px;
          }
          
          .query-text {
            font-size: 14px;
            color: $text-primary;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        
        .history-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 12px;
          color: $text-secondary;
          
          .search-time {
            
          }
          
          .result-count {
            
          }
        }
      }
      
      .delete-btn {
        opacity: 0;
        transition: opacity 0.2s ease;
        color: $text-secondary;
        
        &:hover {
          color: $danger-color;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .search-history {
    .history-list {
      .history-item {
        padding: 10px;
        
        .history-content {
          .history-query {
            .query-text {
              font-size: 13px;
            }
          }
          
          .history-meta {
            font-size: 11px;
            gap: 8px;
          }
        }
        
        .delete-btn {
          opacity: 1; // 移动端始终显示删除按钮
        }
      }
    }
  }
}
</style>