<template>
  <div
    v-if="visible"
    :class="[
      'loading-spinner',
      `loading-${type}`,
      `loading-size-${size}`,
      {
        'loading-overlay': overlay,
        'loading-fullscreen': fullscreen,
        'loading-inline': inline,
      },
    ]"
    :style="customStyle"
  >
    <!-- 背景遮罩 -->
    <div v-if="overlay || fullscreen" class="loading-backdrop" />

    <!-- 加载内容 -->
    <div class="loading-content">
      <!-- 自定义加载器 -->
      <slot name="spinner">
        <!-- 默认加载器 -->
        <div v-if="type === 'default'" class="spinner-default">
          <div class="spinner-circle"></div>
        </div>

        <!-- 点状加载器 -->
        <div v-else-if="type === 'dots'" class="spinner-dots">
          <div class="dot" v-for="i in 3" :key="i"></div>
        </div>

        <!-- 波浪加载器 -->
        <div v-else-if="type === 'wave'" class="spinner-wave">
          <div class="wave-bar" v-for="i in 5" :key="i"></div>
        </div>

        <!-- 脉冲加载器 -->
        <div v-else-if="type === 'pulse'" class="spinner-pulse">
          <div class="pulse-circle"></div>
        </div>

        <!-- 旋转方块 -->
        <div v-else-if="type === 'cube'" class="spinner-cube">
          <div class="cube-face" v-for="i in 6" :key="i"></div>
        </div>

        <!-- 弹跳球 -->
        <div v-else-if="type === 'bounce'" class="spinner-bounce">
          <div class="bounce-ball" v-for="i in 3" :key="i"></div>
        </div>

        <!-- 环形进度 -->
        <div v-else-if="type === 'ring'" class="spinner-ring">
          <div class="ring-segment" v-for="i in 4" :key="i"></div>
        </div>

        <!-- Element Plus 加载器 -->
        <el-icon
          v-else-if="type === 'element'"
          class="spinner-element is-loading"
        >
          <Loading />
        </el-icon>
      </slot>

      <!-- 加载文本 -->
      <div v-if="text || $slots.text" class="loading-text">
        <slot name="text">
          {{ text }}
        </slot>
      </div>

      <!-- 进度条 -->
      <div
        v-if="showProgress && progress !== undefined"
        class="loading-progress"
      >
        <el-progress
          :percentage="progress"
          :show-text="showProgressText"
          :stroke-width="progressStrokeWidth"
          :color="progressColor"
        />
      </div>

      <!-- 自定义内容 -->
      <div v-if="$slots.content" class="loading-custom-content">
        <slot name="content" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, CSSProperties } from "vue";
import { Loading } from "@element-plus/icons-vue";

// 定义类型
type SpinnerType =
  | "default"
  | "dots"
  | "wave"
  | "pulse"
  | "cube"
  | "bounce"
  | "ring"
  | "element";
type SpinnerSize = "small" | "medium" | "large";

// Props
interface Props {
  visible?: boolean;
  type?: SpinnerType;
  size?: SpinnerSize;
  text?: string;
  overlay?: boolean;
  fullscreen?: boolean;
  inline?: boolean;
  color?: string;
  backgroundColor?: string;
  zIndex?: number;
  showProgress?: boolean;
  progress?: number;
  showProgressText?: boolean;
  progressStrokeWidth?: number;
  progressColor?: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  type: "default",
  size: "medium",
  overlay: false,
  fullscreen: false,
  inline: false,
  zIndex: 2000,
  showProgress: false,
  showProgressText: true,
  progressStrokeWidth: 6,
});

// 计算属性
const customStyle = computed((): CSSProperties => {
  const style: CSSProperties = {};

  if (props.zIndex) {
    style.zIndex = props.zIndex;
  }

  if (props.color) {
    style["--loading-color"] = props.color;
  }

  if (props.backgroundColor) {
    style["--loading-bg-color"] = props.backgroundColor;
  }

  return style;
});
</script>

<style lang="scss" scoped>
.loading-spinner {
  --loading-color: var(--el-color-primary);
  --loading-bg-color: rgba(255, 255, 255, 0.9);
  --loading-text-color: var(--el-text-color-primary);

  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  &.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: var(--el-index-loading-overlay, 2000);
  }

  &.loading-fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: var(--el-index-loading, 2001);
  }

  &.loading-inline {
    position: static;
    display: inline-flex;
  }

  .loading-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: var(--loading-bg-color);
    backdrop-filter: blur(1px);
  }

  .loading-content {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 20px;
  }

  .loading-text {
    font-size: 14px;
    color: var(--loading-text-color);
    text-align: center;
    line-height: 1.5;
  }

  .loading-progress {
    width: 200px;
    max-width: 80vw;
  }

  .loading-custom-content {
    text-align: center;
  }
}

// 尺寸变体
.loading-size-small {
  .loading-content {
    gap: 8px;
    padding: 12px;
  }

  .loading-text {
    font-size: 12px;
  }

  .loading-progress {
    width: 120px;
  }
}

.loading-size-large {
  .loading-content {
    gap: 24px;
    padding: 32px;
  }

  .loading-text {
    font-size: 16px;
  }

  .loading-progress {
    width: 280px;
  }
}

// 默认圆形加载器
.spinner-default {
  .spinner-circle {
    width: 40px;
    height: 40px;
    border: 4px solid var(--el-color-primary-light-8);
    border-top: 4px solid var(--loading-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
}

.loading-size-small .spinner-default .spinner-circle {
  width: 24px;
  height: 24px;
  border-width: 2px;
}

.loading-size-large .spinner-default .spinner-circle {
  width: 56px;
  height: 56px;
  border-width: 6px;
}

// 点状加载器
.spinner-dots {
  display: flex;
  gap: 4px;

  .dot {
    width: 8px;
    height: 8px;
    background-color: var(--loading-color);
    border-radius: 50%;
    animation: dot-bounce 1.4s ease-in-out infinite both;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
    &:nth-child(3) {
      animation-delay: 0s;
    }
  }
}

.loading-size-small .spinner-dots .dot {
  width: 6px;
  height: 6px;
}

.loading-size-large .spinner-dots .dot {
  width: 12px;
  height: 12px;
}

// 波浪加载器
.spinner-wave {
  display: flex;
  gap: 2px;
  align-items: flex-end;

  .wave-bar {
    width: 4px;
    height: 20px;
    background-color: var(--loading-color);
    animation: wave-scale 1.2s ease-in-out infinite;

    &:nth-child(1) {
      animation-delay: -1.1s;
    }
    &:nth-child(2) {
      animation-delay: -1s;
    }
    &:nth-child(3) {
      animation-delay: -0.9s;
    }
    &:nth-child(4) {
      animation-delay: -0.8s;
    }
    &:nth-child(5) {
      animation-delay: -0.7s;
    }
  }
}

.loading-size-small .spinner-wave .wave-bar {
  width: 3px;
  height: 16px;
}

.loading-size-large .spinner-wave .wave-bar {
  width: 6px;
  height: 28px;
}

// 脉冲加载器
.spinner-pulse {
  .pulse-circle {
    width: 40px;
    height: 40px;
    background-color: var(--loading-color);
    border-radius: 50%;
    animation: pulse-scale 1s ease-in-out infinite;
  }
}

.loading-size-small .spinner-pulse .pulse-circle {
  width: 24px;
  height: 24px;
}

.loading-size-large .spinner-pulse .pulse-circle {
  width: 56px;
  height: 56px;
}

// 旋转方块
.spinner-cube {
  position: relative;
  width: 40px;
  height: 40px;
  transform-style: preserve-3d;
  animation: cube-rotate 2s linear infinite;

  .cube-face {
    position: absolute;
    width: 100%;
    height: 100%;
    background-color: var(--loading-color);
    opacity: 0.8;

    &:nth-child(1) {
      transform: rotateY(0deg) translateZ(20px);
    }
    &:nth-child(2) {
      transform: rotateY(90deg) translateZ(20px);
    }
    &:nth-child(3) {
      transform: rotateY(180deg) translateZ(20px);
    }
    &:nth-child(4) {
      transform: rotateY(-90deg) translateZ(20px);
    }
    &:nth-child(5) {
      transform: rotateX(90deg) translateZ(20px);
    }
    &:nth-child(6) {
      transform: rotateX(-90deg) translateZ(20px);
    }
  }
}

// 弹跳球
.spinner-bounce {
  display: flex;
  gap: 4px;

  .bounce-ball {
    width: 12px;
    height: 12px;
    background-color: var(--loading-color);
    border-radius: 50%;
    animation: bounce-up-down 0.6s ease-in-out infinite alternate;

    &:nth-child(1) {
      animation-delay: 0s;
    }
    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

// 环形进度
.spinner-ring {
  position: relative;
  width: 40px;
  height: 40px;

  .ring-segment {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 4px solid transparent;
    border-radius: 50%;
    animation: ring-rotate 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;

    &:nth-child(1) {
      border-color: var(--loading-color) transparent transparent transparent;
      animation-delay: -0.45s;
    }
    &:nth-child(2) {
      border-color: transparent var(--loading-color) transparent transparent;
      animation-delay: -0.3s;
    }
    &:nth-child(3) {
      border-color: transparent transparent var(--loading-color) transparent;
      animation-delay: -0.15s;
    }
    &:nth-child(4) {
      border-color: transparent transparent transparent var(--loading-color);
    }
  }
}

// Element Plus 加载器
.spinner-element {
  font-size: 40px;
  color: var(--loading-color);
}

.loading-size-small .spinner-element {
  font-size: 24px;
}

.loading-size-large .spinner-element {
  font-size: 56px;
}

// 动画定义
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dot-bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@keyframes wave-scale {
  0%,
  40%,
  100% {
    transform: scaleY(0.4);
  }
  20% {
    transform: scaleY(1);
  }
}

@keyframes pulse-scale {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}

@keyframes cube-rotate {
  0% {
    transform: rotateX(0deg) rotateY(0deg);
  }
  100% {
    transform: rotateX(360deg) rotateY(360deg);
  }
}

@keyframes bounce-up-down {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-20px);
  }
}

@keyframes ring-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .loading-spinner {
    .loading-content {
      padding: 16px;
      gap: 12px;
    }

    .loading-progress {
      width: 160px;
    }
  }

  .loading-size-large {
    .loading-content {
      padding: 24px;
      gap: 20px;
    }

    .loading-progress {
      width: 200px;
    }
  }
}
</style>
