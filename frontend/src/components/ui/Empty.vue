<template>
  <div class="ui-empty">
    <!-- 图标或图片 -->
    <div class="ui-empty__image">
      <slot name="image">
        <el-icon :size="imageSize" :color="iconColor">
          <component :is="icon" />
        </el-icon>
      </slot>
    </div>

    <!-- 描述文本 -->
    <div v-if="description || $slots.description" class="ui-empty__description">
      <slot name="description">
        <span>{{ description }}</span>
      </slot>
    </div>

    <!-- 操作按钮 -->
    <div v-if="$slots.default" class="ui-empty__actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';
import { Document } from '@element-plus/icons-vue';

/**
 * UI空状态组件
 * 用于显示空数据、无结果等状态
 */

interface Props {
  /** 描述文本 */
  description?: string;
  /** 图标组件 */
  icon?: Component;
  /** 图标大小 */
  imageSize?: number;
  /** 图标颜色 */
  iconColor?: string;
}

const props = withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  icon: Document,
  imageSize: 100,
  iconColor: '#DCDFE6',
});
</script>

<style lang="scss" scoped>
.ui-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;

  &__image {
    margin-bottom: 16px;
    opacity: 0.5;

    img {
      max-width: 100%;
      height: auto;
    }
  }

  &__description {
    margin-bottom: 20px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }

  &__actions {
    margin-top: 8px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .ui-empty {
    padding: 30px 16px;

    &__image {
      :deep(.el-icon) {
        font-size: 80px !important;
      }
    }
  }
}
</style>

