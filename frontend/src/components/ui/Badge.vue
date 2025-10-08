<template>
  <span class="ui-badge">
    <slot />
    <sup
      v-if="shouldShowBadge"
      class="ui-badge__content"
      :class="[
        `ui-badge__content--${type}`,
        {
          'ui-badge__content--dot': isDot,
          'ui-badge__content--fixed': $slots.default,
        },
      ]"
      :style="badgeStyle"
    >
      <span v-if="!isDot">{{ displayValue }}</span>
    </sup>
  </span>
</template>

<script setup lang="ts">
import { computed, type CSSProperties } from 'vue';

/**
 * UI徽章组件
 * 用于显示数字、状态点等小标记
 */

interface Props {
  /** 显示值 */
  value?: string | number;
  /** 最大值，超过显示为 {max}+ */
  max?: number;
  /** 是否显示小圆点 */
  isDot?: boolean;
  /** 是否隐藏 */
  hidden?: boolean;
  /** 类型 */
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  /** 自定义样式 */
  badgeStyle?: CSSProperties;
}

const props = withDefaults(defineProps<Props>(), {
  max: 99,
  isDot: false,
  hidden: false,
  type: 'danger',
});

// 是否显示徽章
const shouldShowBadge = computed(() => {
  if (props.hidden) return false;
  if (props.isDot) return true;
  const val = props.value;
  return val !== undefined && val !== null && val !== '';
});

// 显示的值
const displayValue = computed(() => {
  if (props.isDot) return '';
  
  const val = props.value;
  if (typeof val === 'number' && typeof props.max === 'number') {
    return val > props.max ? `${props.max}+` : val.toString();
  }
  
  return val?.toString() || '';
});
</script>

<style lang="scss" scoped>
.ui-badge {
  position: relative;
  display: inline-block;
  line-height: 1;

  &__content {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    height: 18px;
    padding: 0 6px;
    font-size: 12px;
    font-weight: 500;
    line-height: 18px;
    white-space: nowrap;
    text-align: center;
    border-radius: 9px;
    color: var(--el-color-white);
    z-index: 1;

    // 固定定位（当有子元素时）
    &--fixed {
      position: absolute;
      top: 0;
      right: 0;
      transform: translate(50%, -50%);
    }

    // 小圆点样式
    &--dot {
      width: 8px;
      height: 8px;
      padding: 0;
      border-radius: 50%;
    }

    // 类型变体
    &--primary {
      background-color: var(--el-color-primary);
    }

    &--success {
      background-color: var(--el-color-success);
    }

    &--warning {
      background-color: var(--el-color-warning);
    }

    &--danger {
      background-color: var(--el-color-danger);
    }

    &--info {
      background-color: var(--el-color-info);
    }
  }
}
</style>

