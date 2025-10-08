<template>
  <div class="pdf-preview">
    <!-- PDF加载状态 -->
    <div v-if="loading" class="pdf-preview__loading">
      <Loading type="ring" text="加载PDF中..." />
    </div>

    <!-- PDF预览容器 -->
    <div v-show="!loading && !error" class="pdf-preview__container">
      <!-- 工具栏 -->
      <div v-if="showToolbar" class="pdf-preview__toolbar">
        <div class="toolbar-left">
          <el-button-group>
            <el-button :disabled="currentPage <= 1" @click="prevPage">
              <el-icon><ArrowLeft /></el-icon>
              上一页
            </el-button>
            <el-button :disabled="currentPage >= totalPages" @click="nextPage">
              下一页
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </el-button-group>
          
          <span class="page-info">
            <el-input-number
              v-model="currentPage"
              :min="1"
              :max="totalPages"
              size="small"
              :controls="false"
              style="width: 60px; margin: 0 8px"
            />
            / {{ totalPages }}
          </span>
        </div>

        <div class="toolbar-right">
          <el-button-group>
            <el-button :disabled="scale <= minScale" @click="zoomOut">
              <el-icon><ZoomOut /></el-icon>
            </el-button>
            <el-button @click="resetZoom">
              {{ Math.round(scale * 100) }}%
            </el-button>
            <el-button :disabled="scale >= maxScale" @click="zoomIn">
              <el-icon><ZoomIn /></el-icon>
            </el-button>
          </el-button-group>

          <el-button @click="rotate">
            <el-icon><RefreshRight /></el-icon>
            旋转
          </el-button>

          <el-button v-if="downloadable" @click="download">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>

      <!-- PDF内容 -->
      <div ref="pdfContainer" class="pdf-preview__content">
        <vue-pdf-embed
          ref="pdfRef"
          :source="pdfSource"
          :page="currentPage"
          :rotation="rotation"
          :scale="scale"
          @loaded="onLoaded"
          @loading-failed="onError"
        />
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="pdf-preview__error">
      <Empty
        description="PDF加载失败"
        icon="WarningFilled"
      >
        <el-button type="primary" @click="reload">重新加载</el-button>
      </Empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import VuePdfEmbed from 'vue-pdf-embed';
import {
  ArrowLeft,
  ArrowRight,
  ZoomIn,
  ZoomOut,
  RefreshRight,
  Download,
} from '@element-plus/icons-vue';
import { Loading, Empty } from '@/components/ui';
import { ElMessage } from 'element-plus';

/**
 * PDF预览组件
 * 基于vue-pdf-embed实现
 */

interface Props {
  /** PDF文件URL或ArrayBuffer */
  source: string | ArrayBuffer;
  /** 初始页码 */
  initialPage?: number;
  /** 是否显示工具栏 */
  showToolbar?: boolean;
  /** 是否允许下载 */
  downloadable?: boolean;
  /** 文件名（用于下载） */
  filename?: string;
}

const props = withDefaults(defineProps<Props>(), {
  initialPage: 1,
  showToolbar: true,
  downloadable: true,
  filename: 'document.pdf',
});

const emit = defineEmits<{
  loaded: [pages: number];
  error: [error: Error];
}>();

// 状态
const pdfRef = ref();
const pdfContainer = ref<HTMLElement>();
const loading = ref(true);
const error = ref(false);
const currentPage = ref(props.initialPage);
const totalPages = ref(0);
const scale = ref(1);
const rotation = ref(0);

// 配置
const minScale = 0.5;
const maxScale = 3;
const scaleStep = 0.25;

// PDF源
const pdfSource = computed(() => {
  if (typeof props.source === 'string') {
    // 如果是相对路径，添加API基础URL
    if (!props.source.startsWith('http') && !props.source.startsWith('data:')) {
      const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
      return `${baseURL}${props.source}`;
    }
  }
  return props.source;
});

// 页面跳转
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

// 缩放控制
const zoomIn = () => {
  if (scale.value < maxScale) {
    scale.value = Math.min(scale.value + scaleStep, maxScale);
  }
};

const zoomOut = () => {
  if (scale.value > minScale) {
    scale.value = Math.max(scale.value - scaleStep, minScale);
  }
};

const resetZoom = () => {
  scale.value = 1;
};

// 旋转
const rotate = () => {
  rotation.value = (rotation.value + 90) % 360;
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

// 加载完成
const onLoaded = (pdfProxy: any) => {
  loading.value = false;
  totalPages.value = pdfProxy.numPages || 0;
  emit('loaded', totalPages.value);
};

// 加载失败
const onError = (err: Error) => {
  loading.value = false;
  error.value = true;
  emit('error', err);
  ElMessage.error('PDF加载失败');
};

// 重新加载
const reload = () => {
  error.value = false;
  loading.value = true;
  currentPage.value = props.initialPage;
  scale.value = 1;
  rotation.value = 0;
};

// 监听页码变化（防止超出范围）
watch(currentPage, (newPage) => {
  if (newPage < 1) {
    currentPage.value = 1;
  } else if (newPage > totalPages.value && totalPages.value > 0) {
    currentPage.value = totalPages.value;
  }
});

// 键盘导航
onMounted(() => {
  const handleKeydown = (e: KeyboardEvent) => {
    if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
      prevPage();
    } else if (e.key === 'ArrowRight' || e.key === 'PageDown') {
      nextPage();
    }
  };

  window.addEventListener('keydown', handleKeydown);
  
  // 组件卸载时移除监听
  return () => {
    window.removeEventListener('keydown', handleKeydown);
  };
});
</script>

<style lang="scss" scoped>
.pdf-preview {
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

    .page-info {
      display: flex;
      align-items: center;
      font-size: 14px;
      color: var(--el-text-color-regular);
    }
  }

  &__content {
    flex: 1;
    overflow: auto;
    padding: 20px;
    display: flex;
    justify-content: center;

    :deep(.vue-pdf-embed) {
      canvas {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        margin: 0 auto;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .pdf-preview {
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
    }
  }
}
</style>

