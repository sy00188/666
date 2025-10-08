<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    :width="dialogWidth"
    :fullscreen="isFullscreen"
    destroy-on-close
    class="file-preview-dialog"
    @close="handleClose"
  >
    <div class="file-preview-wrapper">
      <!-- PDF预览 -->
      <PdfPreview
        v-if="currentFileType === 'pdf'"
        :source="fileSource"
        :show-toolbar="showToolbar"
        :downloadable="downloadable"
        :filename="currentFile?.name"
        @loaded="handleLoaded"
        @error="handleError"
      />

      <!-- Word预览 -->
      <DocxPreview
        v-else-if="currentFileType === 'word'"
        :source="fileSource"
        :show-toolbar="showToolbar"
        :downloadable="downloadable"
        :filename="currentFile?.name"
        @loaded="handleLoaded"
        @error="handleError"
      />

      <!-- Excel预览 -->
      <ExcelPreview
        v-else-if="currentFileType === 'excel'"
        :source="fileSource"
        :show-toolbar="showToolbar"
        :downloadable="downloadable"
        :filename="currentFile?.name"
        @loaded="handleLoaded"
        @error="handleError"
      />

      <!-- 图片预览 -->
      <ImagePreview
        v-else-if="currentFileType === 'image'"
        :images="imageList"
        :show-toolbar="showToolbar"
        :downloadable="downloadable"
        @loaded="handleLoaded"
        @error="handleError"
      />

      <!-- 文本/代码预览 -->
      <TextPreview
        v-else-if="currentFileType === 'text' || currentFileType === 'code'"
        :source="fileSource"
        :filename="currentFile?.name || ''"
        :is-code="currentFileType === 'code'"
        :show-toolbar="showToolbar"
        :downloadable="downloadable"
        @loaded="handleLoaded"
        @error="handleError"
      />

      <!-- 不支持的文件类型 -->
      <div v-else class="file-preview-unsupported">
        <Empty
          description="该文件类型暂不支持在线预览"
          icon="WarningFilled"
        >
          <el-button type="primary" @click="handleDownload">
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
        </Empty>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <template #footer>
      <div class="file-preview-footer">
        <div class="footer-left">
          <el-button
            v-if="showFullscreenButton"
            :icon="isFullscreen ? 'FullScreen' : 'FullScreen'"
            @click="toggleFullscreen"
          >
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </el-button>
        </div>
        <div class="footer-right">
          <el-button @click="handleClose">关闭</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Download } from '@element-plus/icons-vue';
import PdfPreview from './PdfPreview.vue';
import DocxPreview from './DocxPreview.vue';
import ExcelPreview from './ExcelPreview.vue';
import ImagePreview from './ImagePreview.vue';
import TextPreview from './TextPreview.vue';
import { Empty } from '@/components/ui';
import type { FileInfo, FileType } from '@/types/filePreview';
import { getFileType } from '@/types/filePreview';
import { ElMessage } from 'element-plus';

/**
 * 文件预览统一入口组件
 * 根据文件类型自动选择对应的预览组件
 */

interface Props {
  /** 是否显示预览对话框 */
  modelValue: boolean;
  /** 文件信息 */
  fileInfo: FileInfo | null;
  /** 对话框宽度 */
  width?: string;
  /** 是否显示工具栏 */
  showToolbar?: boolean;
  /** 是否允许下载 */
  downloadable?: boolean;
  /** 是否显示全屏按钮 */
  showFullscreenButton?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  width: '80%',
  showToolbar: true,
  downloadable: true,
  showFullscreenButton: true,
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  loaded: [];
  error: [error: Error];
  close: [];
}>();

// 状态
const visible = ref(props.modelValue);
const isFullscreen = ref(false);

// 当前文件
const currentFile = computed(() => props.fileInfo);

// 当前文件类型
const currentFileType = computed<FileType | null>(() => {
  if (!currentFile.value) return null;
  return currentFile.value.type || getFileType(currentFile.value.name);
});

// 文件源
const fileSource = computed(() => {
  if (!currentFile.value) return '';
  return currentFile.value.url || '';
});

// 图片列表（用于图片预览）
const imageList = computed(() => {
  if (!currentFile.value) return [];
  return [{
    url: currentFile.value.url || '',
    name: currentFile.value.name,
    thumbnailUrl: currentFile.value.thumbnailUrl,
  }];
});

// 对话框标题
const dialogTitle = computed(() => {
  if (!currentFile.value) return '文件预览';
  return `预览: ${currentFile.value.name}`;
});

// 对话框宽度
const dialogWidth = computed(() => {
  if (isFullscreen.value) return '100%';
  
  // 根据文件类型调整宽度
  switch (currentFileType.value) {
    case 'image':
      return '90%';
    case 'pdf':
    case 'word':
    case 'excel':
      return props.width;
    case 'text':
    case 'code':
      return '85%';
    default:
      return props.width;
  }
});

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
};

// 加载完成
const handleLoaded = () => {
  emit('loaded');
};

// 加载失败
const handleError = (error: Error) => {
  emit('error', error);
};

// 下载文件
const handleDownload = () => {
  if (!currentFile.value?.url) {
    ElMessage.error('文件地址不存在');
    return;
  }

  const link = document.createElement('a');
  link.href = currentFile.value.url;
  link.download = currentFile.value.name;
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  ElMessage.success('开始下载');
};

// 关闭对话框
const handleClose = () => {
  visible.value = false;
  isFullscreen.value = false;
  emit('update:modelValue', false);
  emit('close');
};

// 监听modelValue变化
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal;
  },
);

// 监听visible变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal);
});
</script>

<style lang="scss" scoped>
.file-preview-dialog {
  :deep(.el-dialog__header) {
    border-bottom: 1px solid var(--el-border-color-light);
    margin-right: 0;
    padding: 16px 20px;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    height: calc(100vh - 200px);
    overflow: hidden;
  }

  :deep(.el-dialog__footer) {
    border-top: 1px solid var(--el-border-color-light);
    padding: 12px 20px;
  }

  &.is-fullscreen {
    :deep(.el-dialog__body) {
      height: calc(100vh - 120px);
    }
  }
}

.file-preview-wrapper {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.file-preview-unsupported {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
}

.file-preview-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .footer-left,
  .footer-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .file-preview-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 5px auto;
    }

    :deep(.el-dialog__body) {
      height: calc(100vh - 180px);
    }
  }

  .file-preview-footer {
    flex-direction: column;
    gap: 12px;

    .footer-left,
    .footer-right {
      width: 100%;
      justify-content: center;
    }
  }
}
</style>

