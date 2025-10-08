<template>
  <div class="excel-preview">
    <!-- 加载状态 -->
    <div v-if="loading" class="excel-preview__loading">
      <Loading type="ring" text="加载Excel表格中..." />
    </div>

    <!-- 表格内容 -->
    <div v-show="!loading && !error" class="excel-preview__container">
      <!-- 工具栏 -->
      <div v-if="showToolbar" class="excel-preview__toolbar">
        <div class="toolbar-left">
          <span class="file-name">{{ filename }}</span>
        </div>

        <div class="toolbar-right">
          <el-button v-if="downloadable" @click="download">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>

      <!-- Excel渲染区域 -->
      <div ref="contentRef" class="excel-preview__content" />
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="excel-preview__error">
      <Empty
        description="Excel表格加载失败"
        icon="WarningFilled"
      >
        <el-button type="primary" @click="reload">重新加载</el-button>
      </Empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { renderAsync } from '@vue-office/excel';
import { Download } from '@element-plus/icons-vue';
import { Loading, Empty } from '@/components/ui';
import { ElMessage } from 'element-plus';
import '@vue-office/excel/lib/index.css';

/**
 * Excel表格预览组件
 * 基于@vue-office/excel实现
 */

interface Props {
  /** Excel文件URL或ArrayBuffer */
  source: string | ArrayBuffer;
  /** 是否显示工具栏 */
  showToolbar?: boolean;
  /** 是否允许下载 */
  downloadable?: boolean;
  /** 文件名 */
  filename?: string;
}

const props = withDefaults(defineProps<Props>(), {
  showToolbar: true,
  downloadable: true,
  filename: 'spreadsheet.xlsx',
});

const emit = defineEmits<{
  loaded: [];
  error: [error: Error];
}>();

// 状态
const contentRef = ref<HTMLElement>();
const loading = ref(true);
const error = ref(false);

// 下载
const download = () => {
  if (typeof props.source === 'string') {
    const link = document.createElement('a');
    link.href = props.source;
    link.download = props.filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.success('开始下载');
  }
};

// 加载Excel
const loadExcel = async () => {
  if (!contentRef.value) return;

  loading.value = true;
  error.value = false;

  try {
    // 清空内容
    contentRef.value.innerHTML = '';

    // 渲染Excel
    await renderAsync(props.source, contentRef.value, {
      // 自定义选项
      minColWidth: 80,
      minRowHeight: 25,
    });

    loading.value = false;
    emit('loaded');
  } catch (err: any) {
    console.error('Excel加载失败:', err);
    loading.value = false;
    error.value = true;
    emit('error', err);
    ElMessage.error('Excel表格加载失败');
  }
};

// 重新加载
const reload = () => {
  loadExcel();
};

// 监听source变化
watch(() => props.source, () => {
  loadExcel();
});

// 初始加载
onMounted(() => {
  loadExcel();
});
</script>

<style lang="scss" scoped>
.excel-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-fill-color-light);

  &__loading,
  &__error {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-light);
    flex-shrink: 0;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .file-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  &__content {
    flex: 1;
    overflow: auto;
    padding: 20px;
    background: #fff;

    :deep(.vue-office-excel) {
      width: 100% !important;
      height: auto !important;
    }

    :deep(table) {
      border-collapse: collapse;
      width: 100%;
      
      td,
      th {
        border: 1px solid var(--el-border-color);
        padding: 8px 12px;
        font-size: 13px;
        min-width: 80px;
        white-space: nowrap;
      }

      th {
        background-color: var(--el-fill-color);
        font-weight: 600;
        position: sticky;
        top: 0;
        z-index: 1;
      }

      tr:nth-child(even) {
        background-color: var(--el-fill-color-lighter);
      }

      tr:hover {
        background-color: var(--el-fill-color-light);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .excel-preview {
    &__toolbar {
      flex-direction: column;
      gap: 12px;

      .toolbar-left,
      .toolbar-right {
        width: 100%;
        justify-content: center;
      }
    }

    &__content {
      padding: 12px;
      overflow-x: auto;

      :deep(table) {
        td,
        th {
          padding: 6px 8px;
          font-size: 12px;
          min-width: 60px;
        }
      }
    }
  }
}
</style>

