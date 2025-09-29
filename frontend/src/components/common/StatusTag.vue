<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    :round="round"
    :closable="closable"
    :disable-transitions="disableTransitions"
    :hit="hit"
    :color="customColor"
    :class="tagClass"
    @close="handleClose"
    @click="handleClick"
  >
    <!-- 状态图标 -->
    <el-icon v-if="showIcon && statusIcon" class="status-icon">
      <component :is="statusIcon" />
    </el-icon>

    <!-- 状态文本 -->
    <span class="status-text">{{ displayText }}</span>

    <!-- 自定义内容插槽 -->
    <slot />
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { TagProps } from "element-plus";
import {
  Check,
  Close,
  Warning,
  InfoFilled,
  Loading,
  Clock,
  CircleCheck,
  CircleClose,
  QuestionFilled,
  Minus,
} from "@element-plus/icons-vue";

// 定义状态类型
type StatusType =
  | "success"
  | "error"
  | "warning"
  | "info"
  | "primary"
  | "danger"
  | "processing"
  | "pending"
  | "disabled"
  | "unknown"
  | "default";

type TagType = "success" | "info" | "warning" | "danger" | "primary";
type TagSize = "large" | "default" | "small";
type TagEffect = "dark" | "light" | "plain";

// 状态配置
interface StatusConfig {
  type: TagType;
  icon?: any;
  color?: string;
  text?: string;
}

// Props
interface Props {
  status: StatusType;
  text?: string;
  size?: TagSize;
  effect?: TagEffect;
  round?: boolean;
  closable?: boolean;
  disableTransitions?: boolean;
  hit?: boolean;
  showIcon?: boolean;
  customColor?: string;
  customText?: string;
  clickable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  status: "default",
  size: "default",
  effect: "light",
  round: false,
  closable: false,
  disableTransitions: false,
  hit: false,
  showIcon: true,
  clickable: false,
});

// Emits
const emit = defineEmits<{
  close: [event: Event];
  click: [event: Event];
}>();

// 状态配置映射
const statusConfigs: Record<StatusType, StatusConfig> = {
  success: {
    type: "success",
    icon: CircleCheck,
    text: "成功",
  },
  error: {
    type: "danger",
    icon: CircleClose,
    text: "失败",
  },
  warning: {
    type: "warning",
    icon: Warning,
    text: "警告",
  },
  info: {
    type: "info",
    icon: InfoFilled,
    text: "信息",
  },
  primary: {
    type: "primary",
    icon: InfoFilled,
    text: "主要",
  },
  danger: {
    type: "danger",
    icon: Close,
    text: "危险",
  },
  processing: {
    type: "primary",
    icon: Loading,
    text: "处理中",
  },
  pending: {
    type: "warning",
    icon: Clock,
    text: "等待中",
  },
  disabled: {
    type: "info",
    icon: Minus,
    text: "已禁用",
    color: "#909399",
  },
  unknown: {
    type: "info",
    icon: QuestionFilled,
    text: "未知",
    color: "#909399",
  },
  default: {
    type: "info",
    text: "默认",
  },
};

// 计算属性
const statusConfig = computed(
  () => statusConfigs[props.status] || statusConfigs.default,
);

const tagType = computed(() => statusConfig.value.type);

const statusIcon = computed(() => statusConfig.value.icon);

const customColor = computed(() => {
  return props.customColor || statusConfig.value.color;
});

const displayText = computed(() => {
  return props.customText || props.text || statusConfig.value.text;
});

const tagClass = computed(() => {
  return {
    "status-tag": true,
    "status-tag-clickable": props.clickable,
    "status-tag-processing": props.status === "processing",
    [`status-tag-${props.status}`]: true,
  };
});

// 方法
const handleClose = (event: Event) => {
  emit("close", event);
};

const handleClick = (event: Event) => {
  if (props.clickable) {
    emit("click", event);
  }
};
</script>

<style lang="scss" scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  .status-icon {
    font-size: 12px;

    &.is-loading {
      animation: rotating 2s linear infinite;
    }
  }

  .status-text {
    font-weight: 500;
  }

  &.status-tag-clickable {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      opacity: 0.8;
      transform: translateY(-1px);
    }
  }

  &.status-tag-processing {
    .status-icon {
      animation: rotating 2s linear infinite;
    }
  }

  // 不同状态的特殊样式
  &.status-tag-success {
    .status-icon {
      color: var(--el-color-success);
    }
  }

  &.status-tag-error {
    .status-icon {
      color: var(--el-color-danger);
    }
  }

  &.status-tag-warning {
    .status-icon {
      color: var(--el-color-warning);
    }
  }

  &.status-tag-info {
    .status-icon {
      color: var(--el-color-info);
    }
  }

  &.status-tag-primary {
    .status-icon {
      color: var(--el-color-primary);
    }
  }

  &.status-tag-danger {
    .status-icon {
      color: var(--el-color-danger);
    }
  }

  &.status-tag-disabled {
    opacity: 0.6;

    .status-icon,
    .status-text {
      color: var(--el-text-color-disabled);
    }
  }

  &.status-tag-unknown {
    .status-icon,
    .status-text {
      color: var(--el-text-color-placeholder);
    }
  }
}

// 旋转动画
@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .status-tag {
    .status-icon {
      font-size: 11px;
    }

    .status-text {
      font-size: 12px;
    }
  }
}
</style>
