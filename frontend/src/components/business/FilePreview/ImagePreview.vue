<template>
  <div class="image-preview">
    <!-- 使用v-viewer实现图片查看功能 -->
    <viewer
      ref="viewerRef"
      :images="images"
      :options="viewerOptions"
      class="image-preview__container"
      @inited="onInited"
    >
      <template v-for="(img, index) in images" :key="index">
        <img
          v-show="index === currentIndex"
          :src="img.url"
          :alt="img.name"
          :data-index="index"
          class="image-preview__image"
        />
      </template>
    </viewer>

    <!-- 自定义工具栏 -->
    <div v-if="showToolbar && !loading" class="image-preview__toolbar">
      <div class="toolbar-left">
        <span v-if="images.length > 1" class="image-info">
          {{ currentIndex + 1 }} / {{ images.length }}
        </span>
      </div>

      <div class="toolbar-center">
        <el-button-group>
          <el-button
            v-if="images.length > 1"
            :disabled="currentIndex <= 0"
            @click="prev"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>

          <el-button @click="zoomOut">
            <el-icon><ZoomOut /></el-icon>
          </el-button>

          <el-button @click="resetZoom">
            <el-icon><FullScreen /></el-icon>
          </el-button>

          <el-button @click="zoomIn">
            <el-icon><ZoomIn /></el-icon>
          </el-button>

          <el-button @click="rotateLeft">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>

          <el-button @click="rotateRight">
            <el-icon><RefreshRight /></el-icon>
          </el-button>

          <el-button
            v-if="images.length > 1"
            :disabled="currentIndex >= images.length - 1"
            @click="next"
          >
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-button v-if="downloadable" @click="download">
          <el-icon><Download /></el-icon>
          下载
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="image-preview__loading">
      <Loading type="ring" text="加载图片中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import 'viewerjs/dist/viewer.css';
import { component as Viewer } from 'v-viewer';
import {
  ArrowLeft,
  ArrowRight,
  ZoomIn,
  ZoomOut,
  FullScreen,
  RefreshLeft,
  RefreshRight,
  Download,
} from '@element-plus/icons-vue';
import { Loading } from '@/components/ui';
import { ElMessage } from 'element-plus';

/**
 * 图片预览组件
 * 基于v-viewer实现
 */

interface ImageItem {
  url: string;
  name: string;
  thumbnailUrl?: string;
}

interface Props {
  /** 图片列表或单张图片URL */
  images: ImageItem[] | string;
  /** 初始显示的索引 */
  initialIndex?: number;
  /** 是否显示工具栏 */
  showToolbar?: boolean;
  /** 是否允许下载 */
  downloadable?: boolean;
  /** 是否循环浏览 */
  loop?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  initialIndex: 0,
  showToolbar: true,
  downloadable: true,
  loop: true,
});

const emit = defineEmits<{
  loaded: [];
  error: [error: Error];
  change: [index: number];
}>();

// 状态
const viewerRef = ref();
const loading = ref(true);
const currentIndex = ref(props.initialIndex);
let viewerInstance: any = null;

// 标准化图片列表
const images = computed((): ImageItem[] => {
  if (typeof props.images === 'string') {
    return [{
      url: props.images,
      name: 'image',
    }];
  }
  return props.images;
});

// Viewer配置
const viewerOptions = computed(() => ({
  inline: false,
  button: false, // 隐藏默认工具栏
  navbar: images.value.length > 1,
  title: false,
  toolbar: false,
  tooltip: false,
  movable: true,
  zoomable: true,
  rotatable: true,
  scalable: true,
  transition: true,
  fullscreen: true,
  keyboard: true,
  loop: props.loop,
  initialViewIndex: currentIndex.value,
  viewed() {
    loading.value = false;
    emit('loaded');
  },
  view(event: any) {
    currentIndex.value = event.detail.index;
    emit('change', currentIndex.value);
  },
}));

// 初始化
const onInited = (viewer: any) => {
  viewerInstance = viewer;
};

// 导航控制
const prev = () => {
  if (viewerInstance) {
    viewerInstance.prev();
  }
};

const next = () => {
  if (viewerInstance) {
    viewerInstance.next();
  }
};

// 缩放控制
const zoomIn = () => {
  if (viewerInstance) {
    viewerInstance.zoom(0.1);
  }
};

const zoomOut = () => {
  if (viewerInstance) {
    viewerInstance.zoom(-0.1);
  }
};

const resetZoom = () => {
  if (viewerInstance) {
    viewerInstance.reset();
  }
};

// 旋转控制
const rotateLeft = () => {
  if (viewerInstance) {
    viewerInstance.rotate(-90);
  }
};

const rotateRight = () => {
  if (viewerInstance) {
    viewerInstance.rotate(90);
  }
};

// 下载
const download = () => {
  const currentImage = images.value[currentIndex.value];
  if (!currentImage) return;

  const link = document.createElement('a');
  link.href = currentImage.url;
  link.download = currentImage.name;
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  ElMessage.success('开始下载');
};

// 监听索引变化
watch(
  () => props.initialIndex,
  (newIndex) => {
    currentIndex.value = newIndex;
    if (viewerInstance) {
      viewerInstance.view(newIndex);
    }
  },
);

// 键盘导航
onMounted(() => {
  const handleKeydown = (e: KeyboardEvent) => {
    if (e.key === 'ArrowLeft') {
      prev();
    } else if (e.key === 'ArrowRight') {
      next();
    } else if (e.key === 'Escape') {
      if (viewerInstance) {
        viewerInstance.hide();
      }
    }
  };

  window.addEventListener('keydown', handleKeydown);

  return () => {
    window.removeEventListener('keydown', handleKeydown);
  };
});
</script>

<style lang="scss" scoped>
.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #000;
  position: relative;

  &__container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }

  &__image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
    cursor: pointer;
  }

  &__toolbar {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background-color: rgba(0, 0, 0, 0.7);
    backdrop-filter: blur(10px);
    border-radius: 8px;
    z-index: 100;
    min-width: 400px;

    .toolbar-left,
    .toolbar-center,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .image-info {
      color: #fff;
      font-size: 14px;
      min-width: 60px;
      text-align: center;
    }

    :deep(.el-button) {
      background-color: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
      color: #fff;

      &:hover {
        background-color: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.3);
      }

      &:disabled {
        background-color: rgba(255, 255, 255, 0.05);
        border-color: rgba(255, 255, 255, 0.1);
        color: rgba(255, 255, 255, 0.3);
      }
    }
  }

  &__loading {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 99;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .image-preview {
    &__toolbar {
      min-width: auto;
      width: 90%;
      flex-direction: column;
      gap: 12px;
      padding: 12px;

      .toolbar-left,
      .toolbar-center,
      .toolbar-right {
        width: 100%;
        justify-content: center;
      }
    }
  }
}
</style>

