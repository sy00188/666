<template>
  <el-button
    :type="buttonType"
    :size="size"
    :plain="plain"
    :round="round"
    :circle="circle"
    :loading="loading || isLoading"
    :disabled="disabled || isLoading"
    :autofocus="autofocus"
    :native-type="nativeType"
    :icon="buttonIcon"
    :loading-icon="loadingIcon"
    :text="text"
    :bg="bg"
    :link="link"
    :class="buttonClass"
    @click="handleClick"
  >
    <!-- 自定义图标插槽 -->
    <template v-if="$slots.icon" #icon>
      <slot name="icon" />
    </template>

    <!-- 按钮内容 -->
    <template v-if="!circle">
      <slot>{{ buttonText }}</slot>
    </template>
  </el-button>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Edit,
  Delete,
  View,
  Download,
  Upload,
  Refresh,
  Search,
  Filter,
  Setting,
  Check,
  Close,
  ArrowLeft,
  ArrowRight,
  More,
  Share,
  Star,
  Lock,
  Unlock,
  DocumentCopy,
  Top,
  Bottom,
} from "@element-plus/icons-vue";

// 定义操作类型
type ActionType =
  | "add"
  | "edit"
  | "delete"
  | "view"
  | "download"
  | "upload"
  | "refresh"
  | "search"
  | "filter"
  | "setting"
  | "confirm"
  | "cancel"
  | "back"
  | "next"
  | "more"
  | "share"
  | "star"
  | "lock"
  | "unlock"
  | "copy"
  | "export"
  | "import"
  | "custom";

type ButtonType =
  | "primary"
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "default";
type ButtonSize = "large" | "default" | "small";
type NativeType = "button" | "submit" | "reset";

// 操作配置
interface ActionConfig {
  type: ButtonType;
  icon?: unknown;
  text: string;
  confirmMessage?: string;
}

// 操作配置映射
const actionConfigs: Record<ActionType, ActionConfig> = {
  add: { type: "primary", icon: Plus, text: "新增" },
  edit: { type: "primary", icon: Edit, text: "编辑" },
  delete: {
    type: "danger",
    icon: Delete,
    text: "删除",
    confirmMessage: "确定要删除吗？此操作不可恢复！",
  },
  view: { type: "info", icon: View, text: "查看" },
  download: { type: "success", icon: Download, text: "下载" },
  upload: { type: "primary", icon: Upload, text: "上传" },
  refresh: { type: "info", icon: Refresh, text: "刷新" },
  search: { type: "primary", icon: Search, text: "搜索" },
  filter: { type: "info", icon: Filter, text: "筛选" },
  setting: { type: "info", icon: Setting, text: "设置" },
  confirm: { type: "primary", icon: Check, text: "确认" },
  cancel: { type: "info", icon: Close, text: "取消" },
  back: { type: "info", icon: ArrowLeft, text: "返回" },
  next: { type: "primary", icon: ArrowRight, text: "下一步" },
  more: { type: "info", icon: More, text: "更多" },
  share: { type: "info", icon: Share, text: "分享" },
  star: { type: "warning", icon: Star, text: "收藏" },
  lock: { type: "warning", icon: Lock, text: "锁定" },
  unlock: { type: "success", icon: Unlock, text: "解锁" },
  copy: { type: "info", icon: DocumentCopy, text: "复制" },
  export: { type: "success", icon: Top, text: "导出" },
  import: { type: "primary", icon: Bottom, text: "导入" },
  custom: { type: "default", text: "自定义" },
};

// Props 定义
interface Props {
  // 操作类型
  action?: ActionType;
  // 按钮类型
  type?: ButtonType;
  // 按钮尺寸
  size?: ButtonSize;
  // 按钮文本
  text?: string;
  // 图标
  icon?: unknown;
  // 是否加载中
  loading?: boolean;
  // 是否禁用
  disabled?: boolean;
  // 是否朴素按钮
  plain?: boolean;
  // 是否圆角按钮
  round?: boolean;
  // 是否圆形按钮
  circle?: boolean;
  // 是否文字按钮
  textButton?: boolean;
  // 是否背景按钮
  bg?: boolean;
  // 是否链接按钮
  link?: boolean;
  // 是否自动聚焦
  autofocus?: boolean;
  // 原生类型
  nativeType?: NativeType;
  // 加载图标
  loadingIcon?: unknown;
  // 确认消息
  confirmMessage?: string;
  // 自定义类名
  customClass?: string;
  // 是否需要确认
  needConfirm?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  action: "custom",
  type: "default",
  size: "default",
  loading: false,
  disabled: false,
  plain: false,
  round: false,
  circle: false,
  textButton: false,
  bg: false,
  link: false,
  autofocus: false,
  nativeType: "button",
  needConfirm: false,
});

// Emits 定义
const emit = defineEmits<{
  click: [event: MouseEvent];
  confirm: [];
  cancel: [];
}>();

// 内部状态
const isLoading = ref(false);

// 计算属性
const actionConfig = computed(() => {
  return actionConfigs[props.action] || actionConfigs.custom;
});

const buttonType = computed(() => {
  return props.type || actionConfig.value.type;
});

const buttonIcon = computed(() => {
  return props.icon || actionConfig.value.icon;
});

const buttonText = computed(() => {
  return props.text || actionConfig.value.text;
});

const buttonClass = computed(() => {
  const classes = ["action-button"];

  if (props.customClass) {
    classes.push(props.customClass);
  }

  if (props.textButton) {
    classes.push("action-button--text");
  }

  return classes;
});

const text = computed(() => {
  return props.textButton;
});

// 方法
const handleClick = async (event: MouseEvent) => {
  if (props.disabled || isLoading.value) {
    return;
  }

  // 如果需要确认
  if (
    props.needConfirm ||
    actionConfig.value.confirmMessage ||
    props.confirmMessage
  ) {
    try {
      await ElMessageBox.confirm(
        props.confirmMessage ||
          actionConfig.value.confirmMessage ||
          "确定要执行此操作吗？",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        },
      );

      emit("confirm");
      emit("click", event);
    } catch {
      emit("cancel");
    }
  } else {
    emit("click", event);
  }
};

// 显示成功消息
const showSuccess = (message = "操作成功") => {
  ElMessage.success(message);
};

// 显示错误消息
const showError = (message = "操作失败") => {
  ElMessage.error(message);
};

// 设置加载状态
const setLoading = (loading: boolean) => {
  isLoading.value = loading;
};

// 暴露方法给父组件
defineExpose({
  showSuccess,
  showError,
  setLoading,
});
</script>

<style lang="scss" scoped>
.action-button {
  &--text {
    padding: 0;
    border: none;
    background: none;

    &:hover {
      background: none;
    }
  }
}

// 按钮间距
.action-button + .action-button {
  margin-left: 8px;
}

// 按钮组样式
.el-button-group .action-button {
  margin-left: 0;
}
</style>
