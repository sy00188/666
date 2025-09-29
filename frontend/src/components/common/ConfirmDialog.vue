<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :modal="modal"
    :modal-class="modalClass"
    :append-to-body="appendToBody"
    :lock-scroll="lockScroll"
    :custom-class="customClass"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :center="center"
    :align-center="alignCenter"
    :destroy-on-close="destroyOnClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 内容区域 -->
    <div class="confirm-dialog-content">
      <!-- 图标 -->
      <div v-if="showIcon" class="confirm-icon">
        <el-icon :size="iconSize" :class="iconClass">
          <component :is="iconComponent" />
        </el-icon>
      </div>

      <!-- 消息内容 -->
      <div class="confirm-message">
        <!-- 主要消息 -->
        <div class="message-primary">
          <slot name="message">
            {{ message }}
          </slot>
        </div>

        <!-- 详细描述 -->
        <div
          v-if="description || $slots.description"
          class="message-description"
        >
          <slot name="description">
            {{ description }}
          </slot>
        </div>

        <!-- 自定义内容 -->
        <div v-if="$slots.content" class="message-content">
          <slot name="content" />
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <slot
        name="footer"
        :loading="loading"
        :confirm="handleConfirm"
        :cancel="handleCancel"
      >
        <div class="dialog-footer">
          <el-button
            v-if="showCancelButton"
            @click="handleCancel"
            :disabled="loading"
            :size="buttonSize"
          >
            {{ cancelButtonText }}
          </el-button>
          <el-button
            :type="confirmButtonType"
            @click="handleConfirm"
            :loading="loading"
            :size="buttonSize"
            :disabled="confirmDisabled"
          >
            {{ confirmButtonText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import {
  WarningFilled,
  InfoFilled,
  SuccessFilled,
  CircleCloseFilled,
  QuestionFilled,
} from "@element-plus/icons-vue";

// 定义类型
type DialogType = "warning" | "info" | "success" | "error" | "question";
type ButtonType = "primary" | "success" | "warning" | "danger" | "info";

// Props
interface Props {
  modelValue: boolean;
  type?: DialogType;
  title?: string;
  message?: string;
  description?: string;
  width?: string | number;
  modal?: boolean;
  modalClass?: string;
  appendToBody?: boolean;
  lockScroll?: boolean;
  customClass?: string;
  closeOnClickModal?: boolean;
  closeOnPressEscape?: boolean;
  showClose?: boolean;
  center?: boolean;
  alignCenter?: boolean;
  destroyOnClose?: boolean;
  showIcon?: boolean;
  iconSize?: number;
  showCancelButton?: boolean;
  confirmButtonText?: string;
  cancelButtonText?: string;
  confirmButtonType?: ButtonType;
  buttonSize?: "large" | "default" | "small";
  loading?: boolean;
  confirmDisabled?: boolean;
  beforeConfirm?: () => Promise<boolean> | boolean;
  beforeCancel?: () => Promise<boolean> | boolean;
}

const props = withDefaults(defineProps<Props>(), {
  type: "warning",
  title: "提示",
  message: "确定要执行此操作吗？",
  width: "420px",
  modal: true,
  appendToBody: true,
  lockScroll: true,
  closeOnClickModal: false,
  closeOnPressEscape: true,
  showClose: true,
  center: false,
  alignCenter: true,
  destroyOnClose: false,
  showIcon: true,
  iconSize: 24,
  showCancelButton: true,
  confirmButtonText: "确定",
  cancelButtonText: "取消",
  buttonSize: "default",
  loading: false,
  confirmDisabled: false,
});

// Emits
const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  confirm: [];
  cancel: [];
  open: [];
  opened: [];
  close: [];
  closed: [];
}>();

// 响应式数据
const internalLoading = ref(false);

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const loading = computed(() => props.loading || internalLoading.value);

const iconComponent = computed(() => {
  const iconMap = {
    warning: WarningFilled,
    info: InfoFilled,
    success: SuccessFilled,
    error: CircleCloseFilled,
    question: QuestionFilled,
  };
  return iconMap[props.type];
});

const iconClass = computed(() => {
  const classMap = {
    warning: "text-warning",
    info: "text-info",
    success: "text-success",
    error: "text-danger",
    question: "text-primary",
  };
  return classMap[props.type];
});

const confirmButtonType = computed(() => {
  if (props.confirmButtonType) {
    return props.confirmButtonType;
  }

  const typeMap: Record<DialogType, ButtonType> = {
    warning: "warning",
    info: "primary",
    success: "success",
    error: "danger",
    question: "primary",
  };
  return typeMap[props.type];
});

// 方法
const handleOpen = () => {
  emit("open");
};

const handleOpened = () => {
  emit("opened");
};

const handleClose = () => {
  emit("close");
};

const handleClosed = () => {
  emit("closed");
};

const handleConfirm = async () => {
  if (props.beforeConfirm) {
    internalLoading.value = true;
    try {
      const result = await props.beforeConfirm();
      if (!result) {
        internalLoading.value = false;
        return;
      }
    } catch (error) {
      internalLoading.value = false;
      return;
    }
    internalLoading.value = false;
  }

  visible.value = false;
  emit("confirm");
};

const handleCancel = async () => {
  if (props.beforeCancel) {
    const result = await props.beforeCancel();
    if (!result) {
      return;
    }
  }

  visible.value = false;
  emit("cancel");
};

// 暴露方法
defineExpose({
  confirm: handleConfirm,
  cancel: handleCancel,
});
</script>

<style lang="scss" scoped>
.confirm-dialog-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 8px 0;

  .confirm-icon {
    flex-shrink: 0;
    margin-top: 2px;

    .text-warning {
      color: var(--el-color-warning);
    }

    .text-info {
      color: var(--el-color-info);
    }

    .text-success {
      color: var(--el-color-success);
    }

    .text-danger {
      color: var(--el-color-danger);
    }

    .text-primary {
      color: var(--el-color-primary);
    }
  }

  .confirm-message {
    flex: 1;
    min-width: 0;

    .message-primary {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
      line-height: 1.5;
      margin-bottom: 8px;
    }

    .message-description {
      font-size: 14px;
      color: var(--el-text-color-regular);
      line-height: 1.5;
      margin-bottom: 12px;
    }

    .message-content {
      margin-top: 12px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 不同类型的对话框样式
:deep(.el-dialog) {
  &.confirm-warning {
    .el-dialog__header {
      border-bottom: 2px solid var(--el-color-warning-light-7);
    }
  }

  &.confirm-error {
    .el-dialog__header {
      border-bottom: 2px solid var(--el-color-danger-light-7);
    }
  }

  &.confirm-success {
    .el-dialog__header {
      border-bottom: 2px solid var(--el-color-success-light-7);
    }
  }

  &.confirm-info {
    .el-dialog__header {
      border-bottom: 2px solid var(--el-color-info-light-7);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .confirm-dialog-content {
    gap: 12px;

    .confirm-message {
      .message-primary {
        font-size: 15px;
      }

      .message-description {
        font-size: 13px;
      }
    }
  }
}
</style>
