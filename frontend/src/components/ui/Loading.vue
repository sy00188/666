<template>
  <div 
    class="ui-loading" 
    :class="[
      `ui-loading--${size}`,
      { 'ui-loading--fullscreen': fullscreen }
    ]"
  >
    <div class="ui-loading__spinner">
      <!-- 加载动画 -->
      <div v-if="type === 'default'" class="ui-loading__circle">
        <div 
          v-for="i in 12" 
          :key="i" 
          class="ui-loading__circle-item"
          :style="{ 
            transform: `rotate(${(i - 1) * 30}deg)`,
            animationDelay: `${-(12 - i) * 0.1}s` 
          }"
        />
      </div>

      <!-- 环形加载 -->
      <div v-else-if="type === 'ring'" class="ui-loading__ring" />

      <!-- 点状加载 -->
      <div v-else-if="type === 'dots'" class="ui-loading__dots">
        <span v-for="i in 3" :key="i" />
      </div>

      <!-- 脉冲加载 -->
      <div v-else-if="type === 'pulse'" class="ui-loading__pulse" />
    </div>

    <!-- 加载文本 -->
    <div v-if="text" class="ui-loading__text">
      {{ text }}
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * UI加载组件
 * 提供多种加载动画效果
 */

interface Props {
  /** 加载类型 */
  type?: 'default' | 'ring' | 'dots' | 'pulse';
  /** 大小 */
  size?: 'small' | 'medium' | 'large';
  /** 加载文本 */
  text?: string;
  /** 是否全屏 */
  fullscreen?: boolean;
}

withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'medium',
  fullscreen: false,
});
</script>

<style lang="scss" scoped>
.ui-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;

  // 全屏样式
  &--fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(255, 255, 255, 0.9);
    z-index: 9999;
  }

  // 尺寸变体
  &--small {
    .ui-loading__circle {
      width: 24px;
      height: 24px;
    }
    .ui-loading__ring {
      width: 24px;
      height: 24px;
      border-width: 2px;
    }
    .ui-loading__text {
      font-size: 12px;
    }
  }

  &--medium {
    .ui-loading__circle {
      width: 40px;
      height: 40px;
    }
    .ui-loading__ring {
      width: 40px;
      height: 40px;
      border-width: 3px;
    }
    .ui-loading__text {
      font-size: 14px;
    }
  }

  &--large {
    .ui-loading__circle {
      width: 60px;
      height: 60px;
    }
    .ui-loading__ring {
      width: 60px;
      height: 60px;
      border-width: 4px;
    }
    .ui-loading__text {
      font-size: 16px;
    }
  }

  // 默认圆形加载
  &__circle {
    position: relative;
    width: 40px;
    height: 40px;

    &-item {
      position: absolute;
      top: 0;
      left: 50%;
      width: 2px;
      height: 25%;
      background-color: var(--el-color-primary);
      border-radius: 2px;
      transform-origin: center bottom;
      animation: ui-loading-fade 1.2s linear infinite;
    }
  }

  // 环形加载
  &__ring {
    width: 40px;
    height: 40px;
    border: 3px solid var(--el-border-color-lighter);
    border-top-color: var(--el-color-primary);
    border-radius: 50%;
    animation: ui-loading-rotate 1s linear infinite;
  }

  // 点状加载
  &__dots {
    display: flex;
    gap: 8px;

    span {
      width: 8px;
      height: 8px;
      background-color: var(--el-color-primary);
      border-radius: 50%;
      animation: ui-loading-bounce 1.4s ease-in-out infinite both;

      &:nth-child(1) {
        animation-delay: -0.32s;
      }

      &:nth-child(2) {
        animation-delay: -0.16s;
      }
    }
  }

  // 脉冲加载
  &__pulse {
    width: 40px;
    height: 40px;
    background-color: var(--el-color-primary);
    border-radius: 50%;
    animation: ui-loading-pulse 1.2s ease-in-out infinite;
  }

  // 文本
  &__text {
    margin-top: 12px;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }
}

// 动画定义
@keyframes ui-loading-fade {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.25;
  }
}

@keyframes ui-loading-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes ui-loading-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@keyframes ui-loading-pulse {
  0%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>

