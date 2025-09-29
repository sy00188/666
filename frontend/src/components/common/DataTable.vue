<template>
  <div class="data-table">
    <!-- 表格工具栏 -->
    <div v-if="showToolbar" class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left">
          <!-- 批量操作按钮 -->
          <el-button
            v-if="selection.length > 0"
            type="danger"
            size="small"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selection.length }})
          </el-button>

          <!-- 刷新按钮 -->
          <el-button
            type="primary"
            size="small"
            @click="handleRefresh"
            :loading="loading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </slot>
      </div>

      <div class="toolbar-right">
        <slot name="toolbar-right">
          <!-- 列设置 -->
          <el-popover placement="bottom-end" :width="200" trigger="click">
            <template #reference>
              <el-button size="small" type="text">
                <el-icon><Setting /></el-icon>
                列设置
              </el-button>
            </template>
            <div class="column-settings">
              <div class="setting-header">显示列</div>
              <el-checkbox-group v-model="visibleColumns">
                <div
                  v-for="column in columns"
                  :key="column.prop"
                  class="column-item"
                >
                  <el-checkbox :label="column.prop">
                    {{ column.label }}
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
          </el-popover>

          <!-- 密度设置 -->
          <el-dropdown @command="handleDensityChange">
            <el-button size="small" type="text">
              <el-icon><Grid /></el-icon>
              密度
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  command="default"
                  :class="{ 'is-active': density === 'default' }"
                >
                  默认
                </el-dropdown-item>
                <el-dropdown-item
                  command="medium"
                  :class="{ 'is-active': density === 'medium' }"
                >
                  中等
                </el-dropdown-item>
                <el-dropdown-item
                  command="small"
                  :class="{ 'is-active': density === 'small' }"
                >
                  紧凑
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 全屏切换 -->
          <el-button size="small" type="text" @click="toggleFullscreen">
            <el-icon>
              <component :is="isFullscreen ? 'Aim' : 'FullScreen'" />
            </el-icon>
            {{ isFullscreen ? "退出全屏" : "全屏" }}
          </el-button>
        </slot>
      </div>
    </div>

    <!-- 表格容器 -->
    <div
      class="table-container"
      :class="{
        'is-fullscreen': isFullscreen,
        [`density-${density}`]: true,
      }"
    >
      <el-table
        ref="tableRef"
        :data="tableData"
        :loading="loading"
        :height="tableHeight"
        :size="density === 'medium' ? 'default' : density"
        :stripe="stripe"
        :border="border"
        :show-header="showHeader"
        :highlight-current-row="highlightCurrentRow"
        :row-key="rowKey"
        :tree-props="treeProps"
        :expand-row-keys="expandRowKeys"
        :default-sort="defaultSort"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @row-click="handleRowClick"
        @row-dblclick="handleRowDblClick"
        @expand-change="handleExpandChange"
        v-bind="$attrs"
      >
        <!-- 选择列 -->
        <el-table-column
          v-if="showSelection"
          type="selection"
          width="55"
          :selectable="selectable"
          fixed="left"
        />

        <!-- 序号列 -->
        <el-table-column
          v-if="showIndex"
          type="index"
          label="序号"
          width="60"
          fixed="left"
          :index="getIndex"
        />

        <!-- 展开列 -->
        <el-table-column
          v-if="showExpand"
          type="expand"
          width="55"
          fixed="left"
        >
          <template #default="{ row, $index }">
            <slot name="expand" :row="row" :index="$index" />
          </template>
        </el-table-column>

        <!-- 动态列 -->
        <template v-for="column in filteredColumns" :key="column.prop">
          <el-table-column
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :fixed="column.fixed"
            :sortable="column.sortable"
            :sort-method="column.sortMethod"
            :sort-by="column.sortBy"
            :sort-orders="column.sortOrders"
            :resizable="column.resizable !== false"
            :show-overflow-tooltip="column.showOverflowTooltip !== false"
            :align="column.align || 'left'"
            :header-align="column.headerAlign || column.align || 'left'"
            :class-name="column.className"
            :label-class-name="column.labelClassName"
            :filters="column.filters"
            :filter-method="column.filterMethod"
            :filter-multiple="column.filterMultiple"
            :filter-placement="column.filterPlacement"
          >
            <!-- 自定义表头 -->
            <template v-if="column.headerSlot" #header="scope">
              <slot
                :name="`header-${column.prop}`"
                v-bind="scope"
                :column="column"
              />
            </template>

            <!-- 自定义内容 -->
            <template #default="{ row, column: col, $index }">
              <slot
                v-if="column.slot"
                :name="column.prop"
                :row="row"
                :column="col"
                :index="$index"
                :value="row[column.prop]"
              />
              <template v-else>
                <!-- 格式化显示 -->
                <span v-if="column.formatter">
                  {{ column.formatter(row, col, row[column.prop], $index) }}
                </span>
                <!-- 标签显示 -->
                <el-tag
                  v-else-if="column.tag"
                  :type="getTagType(row[column.prop], column.tag)"
                  :size="density === 'small' ? 'small' : 'default'"
                >
                  {{ getTagText(row[column.prop], column.tag) }}
                </el-tag>
                <!-- 图片显示 -->
                <el-image
                  v-else-if="column.image"
                  :src="row[column.prop]"
                  :style="{ width: '40px', height: '40px' }"
                  fit="cover"
                  :preview-src-list="[row[column.prop]]"
                />
                <!-- 链接显示 -->
                <el-link
                  v-else-if="column.link"
                  type="primary"
                  @click="handleLinkClick(row, column)"
                >
                  {{ row[column.prop] }}
                </el-link>
                <!-- 默认显示 -->
                <span v-else>{{ row[column.prop] }}</span>
              </template>
            </template>
          </el-table-column>
        </template>

        <!-- 操作列 -->
        <el-table-column
          v-if="showActions"
          label="操作"
          :width="actionsWidth"
          :min-width="actionsMinWidth"
          :fixed="actionsFixed"
          align="center"
          class-name="table-actions"
        >
          <template #default="{ row, $index }">
            <slot name="actions" :row="row" :index="$index">
              <el-button
                type="primary"
                size="small"
                text
                @click="handleEdit(row, $index)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                text
                @click="handleDelete(row, $index)"
              >
                删除
              </el-button>
            </slot>
          </template>
        </el-table-column>

        <!-- 空数据 -->
        <template #empty>
          <slot name="empty">
            <el-empty :image-size="120" description="暂无数据">
              <el-button type="primary" @click="handleRefresh">
                刷新数据
              </el-button>
            </el-empty>
          </slot>
        </template>
      </el-table>
    </div>

    <!-- 分页 -->
    <div v-if="showPagination && pagination" class="table-pagination">
      <el-pagination
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="pagination.pageSizes || [10, 20, 50, 100]"
        :layout="paginationLayout"
        :background="true"
        :small="density === 'small'"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from "vue";
import { Delete, Refresh, Setting, Grid } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { TableInstance, TableColumnCtx, Sort } from "element-plus";

// 定义接口
interface Column {
  prop: string;
  label: string;
  width?: number | string;
  minWidth?: number | string;
  fixed?: boolean | "left" | "right";
  sortable?: boolean | "custom";
  sortMethod?: (a: any, b: any) => number;
  sortBy?:
    | string
    | string[]
    | ((row: any, index: number, array?: any[]) => string);
  sortOrders?: ("ascending" | "descending" | null)[];
  resizable?: boolean;
  showOverflowTooltip?: boolean;
  align?: "left" | "center" | "right";
  headerAlign?: "left" | "center" | "right";
  className?: string;
  labelClassName?: string;
  filters?: Array<{ text: string; value: any }>;
  filterMethod?: (value: string, row: any, column: TableColumnCtx<any>) => void;
  filterMultiple?: boolean;
  filterPlacement?: string;
  formatter?: (
    row: any,
    column: TableColumnCtx<any>,
    cellValue: any,
    index: number,
  ) => any;
  slot?: boolean;
  headerSlot?: boolean;
  tag?: {
    type?: "success" | "info" | "warning" | "danger";
    map?: Record<string, { type: string; text: string }>;
  };
  image?: boolean;
  link?: boolean;
}

interface Pagination {
  currentPage: number;
  pageSize: number;
  total: number;
  pageSizes?: number[];
}

// Props
interface Props {
  data?: any[];
  columns: Column[];
  loading?: boolean;
  pagination?: Pagination;
  showToolbar?: boolean;
  showSelection?: boolean;
  showIndex?: boolean;
  showExpand?: boolean;
  showActions?: boolean;
  showPagination?: boolean;
  stripe?: boolean;
  border?: boolean;
  showHeader?: boolean;
  highlightCurrentRow?: boolean;
  height?: number | string;
  maxHeight?: number | string;
  rowKey?: string;
  treeProps?: object;
  expandRowKeys?: any[];
  defaultSort?: Sort;
  selectable?: (row: any, index: number) => boolean;
  actionsWidth?: number | string;
  actionsMinWidth?: number | string;
  actionsFixed?: boolean | "left" | "right";
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false,
  showToolbar: true,
  showSelection: false,
  showIndex: false,
  showExpand: false,
  showActions: true,
  showPagination: true,
  stripe: true,
  border: true,
  showHeader: true,
  highlightCurrentRow: true,
  actionsWidth: 150,
  actionsMinWidth: 120,
  actionsFixed: "right",
});

// Emits
const emit = defineEmits<{
  refresh: [];
  selectionChange: [selection: any[]];
  sortChange: [sort: any];
  rowClick: [row: any, column: any, event: Event];
  rowDblClick: [row: any, column: any, event: Event];
  expandChange: [row: any, expandedRows: any[]];
  edit: [row: any, index: number];
  delete: [row: any, index: number];
  batchDelete: [selection: any[]];
  sizeChange: [size: number];
  currentChange: [current: number];
  linkClick: [row: any, column: Column];
}>();

// 响应式数据
const tableRef = ref<TableInstance>();
const selection = ref<any[]>([]);
const visibleColumns = ref<string[]>([]);
const density = ref<"default" | "medium" | "small">("default");
const isFullscreen = ref(false);

// 计算属性
const tableData = computed(() => props.data || []);

const filteredColumns = computed(() => {
  return props.columns.filter((column) =>
    visibleColumns.value.includes(column.prop),
  );
});

const tableHeight = computed(() => {
  if (props.height) return props.height;
  if (props.maxHeight) return props.maxHeight;
  if (isFullscreen.value) return "calc(100vh - 200px)";
  return undefined;
});

const paginationLayout = computed(() => {
  if (density.value === "small") {
    return "prev, pager, next";
  }
  return "total, sizes, prev, pager, next, jumper";
});

// 方法
const handleRefresh = () => {
  emit("refresh");
};

const handleSelectionChange = (val: any[]) => {
  selection.value = val;
  emit("selectionChange", val);
};

const handleSortChange = (sort: any) => {
  emit("sortChange", sort);
};

const handleRowClick = (row: any, column: any, event: Event) => {
  emit("rowClick", row, column, event);
};

const handleRowDblClick = (row: any, column: any, event: Event) => {
  emit("rowDblClick", row, column, event);
};

const handleExpandChange = (row: any, expandedRows: any[]) => {
  emit("expandChange", row, expandedRows);
};

const handleEdit = (row: any, index: number) => {
  emit("edit", row, index);
};

const handleDelete = async (row: any, index: number) => {
  try {
    await ElMessageBox.confirm("确定要删除这条记录吗？", "确认删除", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    emit("delete", row, index);
  } catch {
    // 用户取消删除
  }
};

const handleBatchDelete = async () => {
  if (selection.value.length === 0) {
    ElMessage.warning("请先选择要删除的记录");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selection.value.length} 条记录吗？`,
      "确认批量删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );
    emit("batchDelete", selection.value);
  } catch {
    // 用户取消删除
  }
};

const handleSizeChange = (size: number) => {
  emit("sizeChange", size);
};

const handleCurrentChange = (current: number) => {
  emit("currentChange", current);
};

const handleLinkClick = (row: any, column: Column) => {
  emit("linkClick", row, column);
};

const handleDensityChange = (command: string) => {
  density.value = command as "default" | "medium" | "small";
};

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;

  nextTick(() => {
    tableRef.value?.doLayout();
  });
};

const getIndex = (index: number) => {
  if (props.pagination) {
    return (
      (props.pagination.currentPage - 1) * props.pagination.pageSize + index + 1
    );
  }
  return index + 1;
};

const getTagType = (value: any, tagConfig: any) => {
  if (tagConfig.map && tagConfig.map[value]) {
    return tagConfig.map[value].type;
  }
  return tagConfig.type || "info";
};

const getTagText = (value: any, tagConfig: any) => {
  if (tagConfig.map && tagConfig.map[value]) {
    return tagConfig.map[value].text;
  }
  return value;
};

// 初始化可见列
const initVisibleColumns = () => {
  visibleColumns.value = props.columns.map((column) => column.prop);
};

// 监听列变化
watch(
  () => props.columns,
  () => {
    initVisibleColumns();
  },
  { immediate: true },
);

// 暴露方法
defineExpose({
  clearSelection: () => tableRef.value?.clearSelection(),
  toggleRowSelection: (row: any, selected?: boolean) =>
    tableRef.value?.toggleRowSelection(row, selected),
  toggleAllSelection: () => tableRef.value?.toggleAllSelection(),
  toggleRowExpansion: (row: any, expanded?: boolean) =>
    tableRef.value?.toggleRowExpansion(row, expanded),
  setCurrentRow: (row: any) => tableRef.value?.setCurrentRow(row),
  clearSort: () => tableRef.value?.clearSort(),
  clearFilter: (columnKeys?: string[]) =>
    tableRef.value?.clearFilter(columnKeys),
  doLayout: () => tableRef.value?.doLayout(),
  sort: (prop: string, order: string) => tableRef.value?.sort(prop, order),
  scrollTo: (options: any) => tableRef.value?.scrollTo(options),
  setScrollTop: (top: number) => tableRef.value?.setScrollTop(top),
  setScrollLeft: (left: number) => tableRef.value?.setScrollLeft(left),
});

// 生命周期
onMounted(() => {
  // 监听全屏变化
  const handleFullscreenChange = () => {
    if (!document.fullscreenElement) {
      isFullscreen.value = false;
    }
  };

  document.addEventListener("fullscreenchange", handleFullscreenChange);

  onUnmounted(() => {
    document.removeEventListener("fullscreenchange", handleFullscreenChange);
  });
});
</script>

<style lang="scss" scoped>
.data-table {
  .table-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
    margin-bottom: 16px;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .column-settings {
      .setting-header {
        font-weight: 500;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid var(--el-border-color-lighter);
      }

      .column-item {
        margin-bottom: 8px;

        :deep(.el-checkbox) {
          width: 100%;

          .el-checkbox__label {
            width: 100%;
          }
        }
      }
    }

    :deep(.el-dropdown-menu__item.is-active) {
      color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
    }
  }

  .table-container {
    position: relative;

    &.is-fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
      background: var(--el-bg-color);
      padding: 20px;
    }

    &.density-small {
      :deep(.el-table) {
        .el-table__cell {
          padding: 4px 0;
        }
      }
    }

    &.density-medium {
      :deep(.el-table) {
        .el-table__cell {
          padding: 8px 0;
        }
      }
    }

    :deep(.el-table) {
      .table-actions {
        .cell {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
        }
      }

      .el-table__empty-block {
        min-height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .table-pagination {
    display: flex;
    justify-content: flex-end;
    padding: 16px 0;
    border-top: 1px solid var(--el-border-color-lighter);
    margin-top: 16px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .data-table {
    .table-toolbar {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;

      .toolbar-left,
      .toolbar-right {
        justify-content: center;
      }
    }

    .table-pagination {
      justify-content: center;

      :deep(.el-pagination) {
        .el-pagination__sizes,
        .el-pagination__jump {
          display: none;
        }
      }
    }
  }
}
</style>
