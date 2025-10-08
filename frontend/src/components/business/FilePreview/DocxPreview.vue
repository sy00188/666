<template>
  <div class="docx-preview">
    <!-- 加载状态 -->
    <div v-if="loading" class="docx-preview__loading">
      <Loading type="ring" text="加载Word文档中..." />
    </div>

    <!-- 文档内容 -->
    <div v-show="!loading && !error" class="docx-preview__container">
      <!-- 工具栏 -->
      <div v-if="showToolbar" class="docx-preview__toolbar">
        <div class="toolbar-left">
          <span class="file-name">{{ filename }}</span>
        </div>

        <div class="toolbar-right">
          <el-button @click="zoomOut">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <span class="zoom-level">{{ Math.round(zoom * 100) }}%</span>
          <el-button @click="zoomIn">
            <el-icon><ZoomIn /></el-icon>
          </el-button>

          <el-button v-if="downloadable" @click="download">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>

      <!-- 文档渲染区域 -->
      <div
        ref="contentRef"
        class="docx-preview__content"
        :style="{ transform: `scale(${zoom})` }"
      />
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="docx-preview__error">
      <Empty
        description="Word文档加载失败"
        icon="WarningFilled"
      >
        <el-button type="primary" @click="reload">重新加载</el-button>
      </Empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { renderAsync } from '@vue-office/docx';
import { ZoomIn, ZoomOut, Download } from '@element-plus/icons-vue';
import { Loading, Empty } from '@/components/ui';
import { ElMessage } from 'element-plus';

/**
 * Word文档预览组件
 * 基于@vue-office/docx实现
 */

interface Props {
  /** 文档URL或ArrayBuffer */
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
  filename: 'document.docx',
});

const emit = defineEmits<{
  loaded: [];
  error: [error: Error];
}>();

// 状态
const contentRef = ref<HTMLElement>();
const loading = ref(true);
const error = ref(false);
const zoom = ref(1);

// 配置
const minZoom = 0.5;
const maxZoom = 2;
const zoomStep = 0.1;

// 缩放控制
const zoomIn = () => {
  if (zoom.value < maxZoom) {
    zoom.value = Math.min(zoom.value + zoomStep, maxZoom);
  }
};

const zoomOut = () => {
  if (zoom.value > minZoom) {
    zoom.value = Math.max(zoom.value - zoomStep, minZoom);
  }
};

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

// 加载文档
const loadDocument = async () => {
  if (!contentRef.value) return;

  loading.value = true;
  error.value = false;

  try {
    // 清空内容
    contentRef.value.innerHTML = '';

    // 渲染文档
    await renderAsync(props.source, contentRef.value);

    loading.value = false;
    emit('loaded');
  } catch (err: any) {
    console.error('Word文档加载失败:', err);
    loading.value = false;
    error.value = true;
    emit('error', err);
    ElMessage.error('Word文档加载失败');
  }
};

// 重新加载
const reload = () => {
  zoom.value = 1;
  loadDocument();
};

// 监听source变化
watch(() => props.source, () => {
  loadDocument();
});

// 初始加载
onMounted(() => {
  loadDocument();
});
</script>

<style lang="scss" scoped>
.docx-preview {
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

    .zoom-level {
      font-size: 14px;
      color: var(--el-text-color-regular);
      min-width: 50px;
      text-align: center;
    }
  }

  &__content {
    flex: 1;
    overflow: auto;
    padding: 20px;
    transform-origin: top center;
    transition: transform 0.3s ease;

    :deep(.docx-wrapper) {
      max-width: 800px;
      margin: 0 auto;
      background: #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      padding: 40px;
      min-height: 100%;
    }

    :deep(p) {
      margin: 0 0 12px 0;
      line-height: 1.6;
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4),
    :deep(h5),
    :deep(h6) {
      margin: 20px 0 12px 0;
      font-weight: 600;
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 16px 0;

      td,
      th {
        border: 1px solid var(--el-border-color);
        padding: 8px;
      }

      th {
        background-color: var(--el-fill-color);
        font-weight: 600;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .docx-preview {
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

      :deep(.docx-wrapper) {
        padding: 20px;
      }
    }
  }
}
</style>

