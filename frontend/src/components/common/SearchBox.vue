<template>
  <div class="search-box" :class="{ 'is-expanded': isExpanded }">
    <!-- 基础搜索 -->
    <div class="search-input-wrapper">
      <el-input
        v-model="searchValue"
        :placeholder="placeholder"
        :size="size"
        clearable
        @input="handleInput"
        @clear="handleClear"
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon class="search-icon">
            <Search />
          </el-icon>
        </template>
        <template #suffix>
          <div class="search-actions">
            <!-- 高级搜索按钮 -->
            <el-button
              v-if="showAdvanced"
              type="text"
              size="small"
              @click="toggleAdvanced"
              class="advanced-btn"
            >
              <el-icon>
                <Setting />
              </el-icon>
            </el-button>
            <!-- 搜索按钮 -->
            <el-button
              type="primary"
              size="small"
              @click="handleSearch"
              :loading="loading"
              class="search-btn"
            >
              搜索
            </el-button>
          </div>
        </template>
      </el-input>

      <!-- 搜索建议 -->
      <div
        v-if="
          showSuggestions && props.suggestions && props.suggestions.length > 0
        "
        class="suggestions-dropdown"
      >
        <div
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="suggestion-item"
          @click="selectSuggestion(suggestion)"
        >
          <el-icon class="suggestion-icon">
            <Search />
          </el-icon>
          <span class="suggestion-text">{{ suggestion.text }}</span>
          <span v-if="suggestion.count" class="suggestion-count">
            {{ suggestion.count }}
          </span>
        </div>
      </div>
    </div>

    <!-- 高级搜索面板 -->
    <el-collapse-transition>
      <div v-show="isExpanded" class="advanced-panel">
        <el-form :model="advancedForm" label-width="80px" size="small">
          <el-row :gutter="16">
            <!-- 搜索范围 -->
            <el-col :span="8">
              <el-form-item label="搜索范围">
                <el-select
                  v-model="advancedForm.scope"
                  placeholder="选择范围"
                  style="width: 100%"
                >
                  <el-option
                    v-for="scope in searchScopes"
                    :key="scope.value"
                    :label="scope.label"
                    :value="scope.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 时间范围 -->
            <el-col :span="8">
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="advancedForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <!-- 状态筛选 -->
            <el-col :span="8">
              <el-form-item label="状态">
                <el-select
                  v-model="advancedForm.status"
                  placeholder="选择状态"
                  multiple
                  collapse-tags
                  style="width: 100%"
                >
                  <el-option
                    v-for="status in statusOptions"
                    :key="status.value"
                    :label="status.label"
                    :value="status.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <!-- 分类筛选 -->
            <el-col :span="8">
              <el-form-item label="分类">
                <el-cascader
                  v-model="advancedForm.category"
                  :options="categoryOptions"
                  :props="{ multiple: true, checkStrictly: true }"
                  placeholder="选择分类"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <!-- 排序方式 -->
            <el-col :span="8">
              <el-form-item label="排序">
                <el-select
                  v-model="advancedForm.sortBy"
                  placeholder="排序方式"
                  style="width: 100%"
                >
                  <el-option
                    v-for="sort in sortOptions"
                    :key="sort.value"
                    :label="sort.label"
                    :value="sort.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <!-- 每页数量 -->
            <el-col :span="8">
              <el-form-item label="每页显示">
                <el-select
                  v-model="advancedForm.pageSize"
                  placeholder="每页数量"
                  style="width: 100%"
                >
                  <el-option label="10条" :value="10" />
                  <el-option label="20条" :value="20" />
                  <el-option label="50条" :value="50" />
                  <el-option label="100条" :value="100" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 操作按钮 -->
          <el-form-item>
            <el-button type="primary" @click="handleAdvancedSearch">
              高级搜索
            </el-button>
            <el-button @click="resetAdvanced">重置</el-button>
            <el-button type="text" @click="saveAsTemplate">
              保存为模板
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>

    <!-- 搜索历史 -->
    <div v-if="showHistory && searchHistory.length > 0" class="search-history">
      <div class="history-header">
        <span class="history-title">搜索历史</span>
        <el-button type="text" size="small" @click="clearHistory">
          清空
        </el-button>
      </div>
      <div class="history-items">
        <el-tag
          v-for="(item, index) in searchHistory"
          :key="index"
          size="small"
          closable
          @click="selectHistory(item)"
          @close="removeHistory(index)"
          class="history-tag"
        >
          {{ item }}
        </el-tag>
      </div>
    </div>

    <!-- 快捷筛选标签 -->
    <div v-if="quickFilters.length > 0" class="quick-filters">
      <span class="filter-label">快捷筛选：</span>
      <el-tag
        v-for="filter in quickFilters"
        :key="filter.key"
        :type="filter.active ? 'primary' : undefined"
        size="small"
        @click="toggleQuickFilter(filter)"
        class="filter-tag"
      >
        {{ filter.label }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from "vue";
import { Search, Setting } from "@element-plus/icons-vue";
import { debounce } from "lodash-es";

// 定义接口
interface Suggestion {
  text: string;
  count?: number;
  type?: string;
}

interface QuickFilter {
  key: string;
  label: string;
  value: string | number | boolean;
  active: boolean;
}

interface AdvancedForm {
  scope: string;
  dateRange: [Date, Date] | null;
  status: string[];
  category: string[];
  sortBy: string;
  pageSize: number;
}

interface SearchParams {
  keyword: string;
  scope?: string;
  dateRange?: [Date, Date] | null;
  status?: string[];
  category?: string[];
  sortBy?: string;
  pageSize?: number;
}

// Props
interface Props {
  placeholder?: string;
  size?: "large" | "default" | "small";
  showAdvanced?: boolean;
  showHistory?: boolean;
  loading?: boolean;
  suggestions?: Suggestion[];
  quickFilters?: QuickFilter[];
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: "请输入搜索关键词",
  size: "default",
  showAdvanced: true,
  showHistory: true,
  loading: false,
  suggestions: () => [],
  quickFilters: () => [],
});

// Emits
const emit = defineEmits<{
  search: [params: SearchParams];
  input: [value: string];
  clear: [];
  advancedSearch: [params: SearchParams];
}>();

// 响应式数据
const searchValue = ref("");
const isExpanded = ref(false);
const showSuggestions = ref(false);
const searchHistory = ref<string[]>([]);

// 高级搜索表单
const advancedForm = ref<AdvancedForm>({
  scope: "",
  dateRange: null,
  status: [],
  category: [],
  sortBy: "relevance",
  pageSize: 20,
});

// 搜索选项配置
const searchScopes = ref([
  { label: "全部", value: "all" },
  { label: "标题", value: "title" },
  { label: "内容", value: "content" },
  { label: "作者", value: "author" },
]);

const statusOptions = ref([
  { label: "已发布", value: "published" },
  { label: "草稿", value: "draft" },
  { label: "已归档", value: "archived" },
]);

const categoryOptions = ref([
  {
    value: "tech",
    label: "技术",
    children: [
      { value: "frontend", label: "前端" },
      { value: "backend", label: "后端" },
      { value: "mobile", label: "移动端" },
    ],
  },
  {
    value: "design",
    label: "设计",
    children: [
      { value: "ui", label: "UI设计" },
      { value: "ux", label: "UX设计" },
    ],
  },
]);

const sortOptions = ref([
  { label: "相关度", value: "relevance" },
  { label: "时间（最新）", value: "time_desc" },
  { label: "时间（最早）", value: "time_asc" },
  { label: "热度", value: "popularity" },
]);

// 计算属性
const quickFilters = computed(() => props.quickFilters);

// 防抖搜索
const debouncedSearch = debounce((value: string) => {
  if (value.trim()) {
    showSuggestions.value = true;
    emit("input", value);
  } else {
    showSuggestions.value = false;
  }
}, 300);

// 方法
const handleInput = (value: string) => {
  debouncedSearch(value);
};

const handleSearch = () => {
  if (!searchValue.value.trim()) return;

  // 添加到搜索历史
  addToHistory(searchValue.value);

  // 隐藏建议
  showSuggestions.value = false;

  // 构建搜索参数
  const params = {
    keyword: searchValue.value,
    ...getSearchParams(),
  };

  emit("search", params);
};

const handleAdvancedSearch = () => {
  const params = {
    keyword: searchValue.value,
    ...advancedForm.value,
    ...getSearchParams(),
  };

  emit("advancedSearch", params);
};

const handleClear = () => {
  searchValue.value = "";
  showSuggestions.value = false;
  emit("clear");
};

const toggleAdvanced = () => {
  isExpanded.value = !isExpanded.value;
};

const resetAdvanced = () => {
  advancedForm.value = {
    scope: "",
    dateRange: null,
    status: [],
    category: [],
    sortBy: "relevance",
    pageSize: 20,
  };
};

const selectSuggestion = (suggestion: Suggestion) => {
  searchValue.value = suggestion.text;
  showSuggestions.value = false;
  handleSearch();
};

const addToHistory = (keyword: string) => {
  const history = [...searchHistory.value];
  const index = history.indexOf(keyword);

  if (index > -1) {
    history.splice(index, 1);
  }

  history.unshift(keyword);

  // 限制历史记录数量
  if (history.length > 10) {
    history.pop();
  }

  searchHistory.value = history;

  // 保存到本地存储
  localStorage.setItem("search-history", JSON.stringify(history));
};

const selectHistory = (keyword: string) => {
  searchValue.value = keyword;
  handleSearch();
};

const removeHistory = (index: number) => {
  searchHistory.value.splice(index, 1);
  localStorage.setItem("search-history", JSON.stringify(searchHistory.value));
};

const clearHistory = () => {
  searchHistory.value = [];
  localStorage.removeItem("search-history");
};

const toggleQuickFilter = (filter: QuickFilter) => {
  filter.active = !filter.active;
  handleSearch();
};

const getSearchParams = () => {
  const activeFilters = quickFilters.value
    .filter((f) => f.active)
    .reduce(
      (acc, f) => {
        acc[f.key] = f.value;
        return acc;
      },
      {} as Record<string, string | number | boolean>,
    );

  return activeFilters;
};

const saveAsTemplate = () => {
  // 保存搜索模板的逻辑
  console.log("保存搜索模板", advancedForm.value);
};

// 初始化搜索历史
const initHistory = () => {
  const history = localStorage.getItem("search-history");
  if (history) {
    try {
      searchHistory.value = JSON.parse(history);
    } catch (e) {
      console.error("解析搜索历史失败:", e);
    }
  }
};

// 监听点击外部关闭建议
const handleClickOutside = (event: Event) => {
  const target = event.target as HTMLElement;
  if (!target.closest(".search-box")) {
    showSuggestions.value = false;
  }
};

// 生命周期
onMounted(() => {
  initHistory();
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});

// 暴露方法
defineExpose({
  focus: () => {
    nextTick(() => {
      const input = document.querySelector(
        ".search-input input",
      ) as HTMLInputElement;
      input?.focus();
    });
  },
  clear: handleClear,
  search: handleSearch,
});
</script>

<style lang="scss" scoped>
.search-box {
  position: relative;
  width: 100%;

  .search-input-wrapper {
    position: relative;

    .search-input {
      :deep(.el-input__inner) {
        padding-right: 120px;
      }

      .search-actions {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-right: 8px;

        .advanced-btn {
          padding: 4px;
          color: var(--el-text-color-regular);

          &:hover {
            color: var(--el-color-primary);
          }
        }

        .search-btn {
          height: 24px;
          padding: 0 12px;
        }
      }

    .suggestions-dropdown {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      z-index: 1000;
      background: var(--el-bg-color-overlay);
      border: 1px solid var(--el-border-color-light);
      border-radius: 6px;
      box-shadow: var(--el-box-shadow);
      max-height: 200px;
      overflow-y: auto;

      .suggestion-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background: var(--el-color-primary-light-9);
        }

        .suggestion-icon {
          color: var(--el-text-color-placeholder);
          font-size: 14px;
        }

        .suggestion-text {
          flex: 1;
          font-size: 14px;
        }

        .suggestion-count {
          font-size: 12px;
          color: var(--el-text-color-placeholder);
        }
      }
    }
  }

  .advanced-panel {
    margin-top: 16px;
    padding: 16px;
    background: var(--el-bg-color-page);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
  }

  .search-history {
    margin-top: 12px;

    .history-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;

      .history-title {
        font-size: 13px;
        color: var(--el-text-color-regular);
      }
    }

    .history-items {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .history-tag {
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          color: var(--el-color-primary);
          border-color: var(--el-color-primary);
        }
      }
    }
  }

  .quick-filters {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;
    flex-wrap: wrap;

    .filter-label {
      font-size: 13px;
      color: var(--el-text-color-regular);
      white-space: nowrap;
    }

    .filter-tag {
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        opacity: 0.8;
      }
    }
  }

  &.is-expanded {
    .search-input .advanced-btn {
      color: var(--el-color-primary);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .search-box {
    .advanced-panel {
      .el-row {
        .el-col {
          margin-bottom: 16px;
        }
      }
    }

    .quick-filters {
      .filter-label {
        width: 100%;
        margin-bottom: 4px;
      }
    }
  }
}
</style>
