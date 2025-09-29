<template>
  <div class="search-results">
    <!-- 搜索结果统计 -->
    <div class="results-header" v-if="!loading">
      <div class="results-stats">
        <span class="total-count">共找到 {{ total }} 条结果</span>
        <span class="search-time" v-if="searchTime">（用时 {{ searchTime }}ms）</span>
      </div>
      <div class="results-actions">
        <el-button-group size="small">
          <el-button 
            :type="viewMode === 'list' ? 'primary' : ''" 
            @click="viewMode = 'list'"
            icon="List"
          >
            列表
          </el-button>
          <el-button 
            :type="viewMode === 'card' ? 'primary' : ''" 
            @click="viewMode = 'card'"
            icon="Grid"
          >
            卡片
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-else-if="!results.length && !loading"
      description="暂无搜索结果"
      :image-size="120"
    >
      <template #description>
        <p>没有找到匹配的档案</p>
        <p class="empty-tips">请尝试调整搜索条件或关键词</p>
      </template>
    </el-empty>

    <!-- 搜索结果列表 -->
    <div v-else class="results-container" :class="`view-${viewMode}`">
      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" class="results-list">
        <div 
          v-for="item in results" 
          :key="item.id"
          class="result-item"
          @click="handleItemClick(item)"
        >
          <div class="item-header">
            <h3 class="item-title" v-html="highlightText(item.title, item.highlights?.title)"></h3>
            <div class="item-meta">
              <el-tag size="small" :type="getStatusType(item.status)">
                {{ item.status }}
              </el-tag>
              <span class="item-code">{{ item.code }}</span>
              <span class="item-category">{{ item.category }}</span>
            </div>
          </div>
          <div class="item-content" v-if="item.content">
            <p v-html="highlightText(item.content, item.highlights?.content)"></p>
          </div>
          <div class="item-footer">
            <span class="create-time">创建时间：{{ formatDate(item.createTime) }}</span>
            <div class="item-actions">
              <el-button size="small" text @click.stop="handleView(item)">
                查看详情
              </el-button>
              <el-button size="small" text @click.stop="handleEdit(item)">
                编辑
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-else class="results-grid">
        <div 
          v-for="item in results" 
          :key="item.id"
          class="result-card"
          @click="handleItemClick(item)"
        >
          <el-card shadow="hover" :body-style="{ padding: '16px' }">
            <template #header>
              <div class="card-header">
                <h4 class="card-title" v-html="highlightText(item.title, item.highlights?.title)"></h4>
                <el-tag size="small" :type="getStatusType(item.status)">
                  {{ item.status }}
                </el-tag>
              </div>
            </template>
            
            <div class="card-content">
              <div class="card-meta">
                <div class="meta-item">
                  <span class="meta-label">档案编号：</span>
                  <span class="meta-value">{{ item.code }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">分类：</span>
                  <span class="meta-value">{{ item.category }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">创建时间：</span>
                  <span class="meta-value">{{ formatDate(item.createTime) }}</span>
                </div>
              </div>
              
              <div class="card-description" v-if="item.content">
                <p v-html="highlightText(item.content, item.highlights?.content)"></p>
              </div>
            </div>

            <template #footer>
              <div class="card-actions">
                <el-button size="small" @click.stop="handleView(item)">
                  查看详情
                </el-button>
                <el-button size="small" type="primary" @click.stop="handleEdit(item)">
                  编辑
                </el-button>
              </div>
            </template>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0 && !loading">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :hide-on-single-page="false"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { SearchResult } from '@/composables/useAdvancedSearch'

interface Props {
  results: SearchResult[]
  loading?: boolean
  total?: number
  currentPage?: number
  pageSize?: number
  searchTime?: number
}

interface Emits {
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
  (e: 'item-click', item: SearchResult): void
  (e: 'item-view', item: SearchResult): void
  (e: 'item-edit', item: SearchResult): void
}

const props = withDefaults(defineProps<Props>(), {
  results: () => [],
  loading: false,
  total: 0,
  currentPage: 1,
  pageSize: 20,
  searchTime: 0
})

const emit = defineEmits<Emits>()

// 视图模式
const viewMode = ref<'list' | 'card'>('list')

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status: string) => {
  const statusMap: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    '正常': 'success',
    '借出': 'warning',
    '损坏': 'danger',
    '丢失': 'danger',
    '维修': 'info'
  }
  return statusMap[status] || 'primary'
}

// 高亮文本
const highlightText = (text: string, highlights?: string[]) => {
  if (!highlights || !highlights.length) {
    return text
  }
  
  let highlightedText = text
  highlights.forEach(highlight => {
    const regex = new RegExp(`(${highlight})`, 'gi')
    highlightedText = highlightedText.replace(regex, '<mark>$1</mark>')
  })
  
  return highlightedText
}

// 事件处理
const handleSizeChange = (size: number) => {
  emit('size-change', size)
}

const handleCurrentChange = (page: number) => {
  emit('page-change', page)
}

const handleItemClick = (item: SearchResult) => {
  emit('item-click', item)
}

const handleView = (item: SearchResult) => {
  emit('item-view', item)
}

const handleEdit = (item: SearchResult) => {
  emit('item-edit', item)
}
</script>

<style scoped lang="scss">
.search-results {
  .results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-light);

    .results-stats {
      display: flex;
      align-items: center;
      gap: 8px;

      .total-count {
        font-weight: 500;
        color: var(--el-text-color-primary);
      }

      .search-time {
        color: var(--el-text-color-regular);
        font-size: 12px;
      }
    }

    .results-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .loading-container {
    padding: 20px 0;
  }

  .results-container {
    min-height: 200px;

    &.view-list {
      .results-list {
        .result-item {
          padding: 16px;
          border: 1px solid var(--el-border-color-light);
          border-radius: 8px;
          margin-bottom: 12px;
          cursor: pointer;
          transition: all 0.2s ease;

          &:hover {
            border-color: var(--el-color-primary);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }

          .item-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 8px;

            .item-title {
              margin: 0;
              font-size: 16px;
              font-weight: 500;
              color: var(--el-text-color-primary);
              flex: 1;
              margin-right: 16px;

              :deep(mark) {
                background-color: var(--el-color-warning-light-7);
                color: var(--el-color-warning-dark-2);
                padding: 2px 4px;
                border-radius: 2px;
              }
            }

            .item-meta {
              display: flex;
              align-items: center;
              gap: 8px;
              flex-shrink: 0;

              .item-code, .item-category {
                font-size: 12px;
                color: var(--el-text-color-regular);
                background: var(--el-fill-color-light);
                padding: 2px 6px;
                border-radius: 4px;
              }
            }
          }

          .item-content {
            margin-bottom: 12px;

            p {
              margin: 0;
              color: var(--el-text-color-regular);
              line-height: 1.5;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;

              :deep(mark) {
                background-color: var(--el-color-warning-light-7);
                color: var(--el-color-warning-dark-2);
                padding: 2px 4px;
                border-radius: 2px;
              }
            }
          }

          .item-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .create-time {
              font-size: 12px;
              color: var(--el-text-color-secondary);
            }

            .item-actions {
              display: flex;
              gap: 8px;
            }
          }
        }
      }
    }

    &.view-card {
      .results-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 16px;

        .result-card {
          cursor: pointer;
          transition: transform 0.2s ease;

          &:hover {
            transform: translateY(-2px);
          }

          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;

            .card-title {
              margin: 0;
              font-size: 14px;
              font-weight: 500;
              flex: 1;
              margin-right: 8px;

              :deep(mark) {
                background-color: var(--el-color-warning-light-7);
                color: var(--el-color-warning-dark-2);
                padding: 2px 4px;
                border-radius: 2px;
              }
            }
          }

          .card-content {
            .card-meta {
              margin-bottom: 12px;

              .meta-item {
                display: flex;
                margin-bottom: 4px;
                font-size: 12px;

                .meta-label {
                  color: var(--el-text-color-secondary);
                  width: 70px;
                  flex-shrink: 0;
                }

                .meta-value {
                  color: var(--el-text-color-regular);
                  flex: 1;
                }
              }
            }

            .card-description {
              p {
                margin: 0;
                font-size: 12px;
                color: var(--el-text-color-regular);
                line-height: 1.4;
                display: -webkit-box;
                -webkit-line-clamp: 3;
                -webkit-box-orient: vertical;
                overflow: hidden;

                :deep(mark) {
                  background-color: var(--el-color-warning-light-7);
                  color: var(--el-color-warning-dark-2);
                  padding: 2px 4px;
                  border-radius: 2px;
                }
              }
            }
          }

          .card-actions {
            display: flex;
            justify-content: flex-end;
            gap: 8px;
          }
        }
      }
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-light);
  }

  .empty-tips {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    margin-top: 8px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .search-results {
    .results-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .results-stats {
        justify-content: center;
      }

      .results-actions {
        justify-content: center;
      }
    }

    .results-container {
      &.view-list {
        .results-list {
          .result-item {
            padding: 12px;

            .item-header {
              flex-direction: column;
              gap: 8px;

              .item-meta {
                justify-content: flex-start;
                flex-wrap: wrap;
              }
            }

            .item-footer {
              flex-direction: column;
              gap: 8px;
              align-items: flex-start;

              .item-actions {
                align-self: stretch;
                justify-content: space-around;
              }
            }
          }
        }
      }

      &.view-card {
        .results-grid {
          grid-template-columns: 1fr;
          gap: 12px;
        }
      }
    }

    .pagination-container {
      :deep(.el-pagination) {
        .el-pagination__sizes,
        .el-pagination__jump {
          display: none;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .search-results {
    .results-container.view-list {
      .results-list {
        .result-item {
          .item-header {
            .item-title {
              font-size: 14px;
            }
          }
        }
      }
    }
  }
}

// 暗色模式支持
@media (prefers-color-scheme: dark) {
  .search-results {
    .results-container.view-list {
      .results-list {
        .result-item {
          &:hover {
            box-shadow: 0 2px 8px rgba(255, 255, 255, 0.1);
          }
        }
      }
    }
  }
}
</style>