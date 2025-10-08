<template>
  <div 
    class="ui-card" 
    :class="[
      `ui-card--${shadow}`,
      { 'ui-card--hoverable': hoverable }
    ]"
  >
    <!-- 卡片头部 -->
    <div v-if="$slots.header || title" class="ui-card__header">
      <slot name="header">
        <span class="ui-card__title">{{ title }}</span>
      </slot>
      <div v-if="$slots.extra" class="ui-card__extra">
        <slot name="extra" />
      </div>
    </div>

    <!-- 卡片主体 -->
    <div class="ui-card__body" :style="bodyStyle">
      <slot />
    </div>

    <!-- 卡片底部 -->
    <div v-if="$slots.footer" class="ui-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type CSSProperties } from 'vue';

/**
 * UI卡片组件
 * 通用的容器组件，提供阴影、边框、圆角等样式
 */

interface Props {
  /** 卡片标题 */
  title?: string;
  /** 阴影效果：always-始终显示 | hover-悬浮时显示 | never-不显示 */
  shadow?: 'always' | 'hover' | 'never';
  /** 是否可悬浮（hover时轻微上浮） */
  hoverable?: boolean;
  /** 主体样式 */
  bodyStyle?: CSSProperties;
}

const props = withDefaults(defineProps<Props>(), {
  shadow: 'always',
  hoverable: false,
});
</script>

<style lang="scss" scoped>
.ui-card {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: var(--el-border-radius-base);
  overflow: hidden;
  transition: all 0.3s ease;

  // 阴影变体
  &--always {
    box-shadow: var(--el-box-shadow-light);
  }

  &--hover:hover {
    box-shadow: var(--el-box-shadow-light);
  }

  &--never {
    box-shadow: none;
  }

  // 可悬浮效果
  &--hoverable:hover {
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow);
  }

  // 头部
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  &__extra {
    color: var(--el-text-color-regular);
  }

  // 主体
  &__body {
    padding: 20px;
  }

  // 底部
  &__footer {
    padding: 12px 20px;
    border-top: 1px solid var(--el-border-color-lighter);
    background-color: var(--el-fill-color-light);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .ui-card {
    &__header,
    &__body,
    &__footer {
      padding-left: 16px;
      padding-right: 16px;
    }
  }
}
</style>

