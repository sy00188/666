<template>
  <div class="image-upload">
    <!-- 上传区域 -->
    <el-upload
      ref="uploadRef"
      :action="action"
      :headers="headers"
      :data="data"
      :name="name"
      :with-credentials="withCredentials"
      :multiple="multiple"
      :accept="accept"
      :auto-upload="autoUpload"
      :list-type="listType"
      :drag="drag"
      :disabled="disabled"
      :limit="limit"
      :file-list="fileList"
      :http-request="httpRequest"
      :before-upload="handleBeforeUpload"
      :on-progress="handleProgress"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-change="handleChange"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      :on-exceed="handleExceed"
      :class="uploadClass"
    >
      <!-- 拖拽上传区域 -->
      <template v-if="drag">
        <div class="upload-dragger">
          <el-icon class="upload-icon">
            <UploadFilled />
          </el-icon>
          <div class="upload-text">
            <div class="upload-title">
              {{ dragText || "将文件拖到此处，或点击上传" }}
            </div>
            <div class="upload-hint">
              {{
                dragHint ||
                `支持 ${acceptText} 格式，单个文件不超过 ${maxSizeText}`
              }}
            </div>
          </div>
        </div>
      </template>

      <!-- 普通上传按钮 -->
      <template v-else-if="listType === 'text'">
        <el-button :type="buttonType" :size="buttonSize" :disabled="disabled">
          <el-icon><Upload /></el-icon>
          {{ buttonText || "选择文件" }}
        </el-button>
      </template>

      <!-- 图片卡片上传 -->
      <template v-else-if="listType === 'picture-card'">
        <div class="upload-card">
          <el-icon><Plus /></el-icon>
          <div class="upload-card-text">{{ cardText || "上传图片" }}</div>
        </div>
      </template>

      <!-- 自定义上传触发器 -->
      <template v-else>
        <slot name="trigger">
          <el-button :type="buttonType" :size="buttonSize" :disabled="disabled">
            <el-icon><Upload /></el-icon>
            {{ buttonText || "选择文件" }}
          </el-button>
        </slot>
      </template>

      <!-- 上传提示 -->
      <template #tip>
        <slot name="tip">
          <div v-if="showTip" class="upload-tip">
            {{
              tipText ||
              `支持 ${acceptText} 格式，单个文件不超过 ${maxSizeText}`
            }}
          </div>
        </slot>
      </template>
    </el-upload>

    <!-- 自定义文件列表 -->
    <div v-if="showCustomList && fileList.length > 0" class="custom-file-list">
      <div
        v-for="(file, index) in fileList"
        :key="file.uid || index"
        class="file-item"
        :class="{ 'file-item-error': file.status === 'fail' }"
      >
        <!-- 文件预览 -->
        <div class="file-preview">
          <img
            v-if="isImage(file)"
            :src="getFileUrl(file)"
            :alt="file.name"
            class="file-image"
            @click="handlePreview(file)"
          />
          <div v-else class="file-icon">
            <el-icon><Document /></el-icon>
          </div>
        </div>

        <!-- 文件信息 -->
        <div class="file-info">
          <div class="file-name" :title="file.name">{{ file.name }}</div>
          <div class="file-size">{{ formatFileSize(file.size) }}</div>
          <div v-if="file.status === 'uploading'" class="file-progress">
            <el-progress
              :percentage="file.percentage || 0"
              :show-text="false"
            />
          </div>
          <div v-else-if="file.status === 'fail'" class="file-error">
            上传失败
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="file-actions">
          <el-button
            v-if="isImage(file) && file.status === 'success'"
            type="primary"
            size="small"
            text
            @click="handlePreview(file)"
          >
            预览
          </el-button>
          <el-button
            type="danger"
            size="small"
            text
            @click="handleRemove(file)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="图片预览"
      width="80%"
      :modal="true"
      :append-to-body="true"
      :destroy-on-close="true"
    >
      <div class="image-preview">
        <img :src="previewUrl" :alt="previewName" class="preview-image" />
      </div>
      <template #footer>
        <div class="preview-footer">
          <span class="preview-name">{{ previewName }}</span>
          <div class="preview-actions">
            <el-button @click="downloadImage">下载</el-button>
            <el-button type="primary" @click="previewVisible = false"
              >关闭</el-button
            >
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import type {
  UploadProps,
  UploadUserFile,
  UploadFile,
  UploadRequestOptions,
} from "element-plus";
import { ElMessage } from "element-plus";
import { Upload, UploadFilled, Plus, Document } from "@element-plus/icons-vue";

// 定义类型
type ListType = "text" | "picture" | "picture-card";
type ButtonType = "primary" | "success" | "warning" | "danger" | "info";
type ButtonSize = "large" | "default" | "small";

// Props
interface Props {
  modelValue?: UploadUserFile[];
  action?: string;
  headers?: Record<string, any>;
  data?: Record<string, any>;
  name?: string;
  withCredentials?: boolean;
  multiple?: boolean;
  accept?: string;
  autoUpload?: boolean;
  listType?: ListType;
  drag?: boolean;
  disabled?: boolean;
  limit?: number;
  maxSize?: number; // MB
  showTip?: boolean;
  showCustomList?: boolean;
  buttonType?: ButtonType;
  buttonSize?: ButtonSize;
  buttonText?: string;
  cardText?: string;
  dragText?: string;
  dragHint?: string;
  tipText?: string;
  httpRequest?: (
    options: UploadRequestOptions,
  ) => XMLHttpRequest | Promise<any>;
  beforeUpload?: (file: File) => boolean | Promise<boolean>;
  validateType?: boolean;
  validateSize?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  action: "#",
  name: "file",
  withCredentials: false,
  multiple: false,
  accept: "image/*",
  autoUpload: true,
  listType: "picture-card",
  drag: false,
  disabled: false,
  limit: 5,
  maxSize: 10,
  showTip: true,
  showCustomList: false,
  buttonType: "primary",
  buttonSize: "default",
  validateType: true,
  validateSize: true,
});

// Emits
const emit = defineEmits<{
  "update:modelValue": [files: UploadUserFile[]];
  change: [files: UploadUserFile[]];
  success: [response: any, file: UploadFile, fileList: UploadUserFile[]];
  error: [error: Error, file: UploadFile, fileList: UploadUserFile[]];
  progress: [
    event: ProgressEvent,
    file: UploadFile,
    fileList: UploadUserFile[],
  ];
  remove: [file: UploadFile, fileList: UploadUserFile[]];
  preview: [file: UploadFile];
  exceed: [files: File[], fileList: UploadUserFile[]];
}>();

// 响应式数据
const uploadRef = ref();
const fileList = ref<UploadUserFile[]>([]);
const previewVisible = ref(false);
const previewUrl = ref("");
const previewName = ref("");

// 计算属性
const acceptText = computed(() => {
  if (!props.accept) return "所有文件";

  const types = props.accept.split(",").map((type) => type.trim());
  const imageTypes = [
    "image/*",
    ".jpg",
    ".jpeg",
    ".png",
    ".gif",
    ".bmp",
    ".webp",
  ];
  const hasImage = types.some((type) =>
    imageTypes.includes(type.toLowerCase()),
  );

  if (hasImage && types.length === 1 && types[0] === "image/*") {
    return "图片";
  }

  return types.join(", ");
});

const maxSizeText = computed(() => {
  return props.maxSize >= 1024
    ? `${(props.maxSize / 1024).toFixed(1)}GB`
    : `${props.maxSize}MB`;
});

const uploadClass = computed(() => {
  return {
    "upload-disabled": props.disabled,
    "upload-drag": props.drag,
    "upload-limit-exceeded": fileList.value.length >= props.limit,
  };
});

// 监听器
watch(
  () => props.modelValue,
  (newValue) => {
    fileList.value = newValue || [];
  },
  { immediate: true, deep: true },
);

watch(
  fileList,
  (newValue) => {
    emit("update:modelValue", newValue);
    emit("change", newValue);
  },
  { deep: true },
);

// 方法
const isImage = (file: UploadUserFile | UploadFile): boolean => {
  const imageTypes = [
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/gif",
    "image/bmp",
    "image/webp",
  ];
  return file.raw
    ? imageTypes.includes(file.raw.type)
    : /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(file.name || "");
};

const getFileUrl = (file: UploadUserFile | UploadFile): string => {
  if (file.url) return file.url;
  if (file.raw) return URL.createObjectURL(file.raw);
  return "";
};

const formatFileSize = (size?: number): string => {
  if (!size) return "0 B";

  const units = ["B", "KB", "MB", "GB"];
  let index = 0;
  let fileSize = size;

  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024;
    index++;
  }

  return `${fileSize.toFixed(1)} ${units[index]}`;
};

const validateFile = (file: File): boolean => {
  // 验证文件类型
  if (props.validateType && props.accept && props.accept !== "*") {
    const acceptTypes = props.accept.split(",").map((type) => type.trim());
    const isValidType = acceptTypes.some((type) => {
      if (type === "image/*") {
        return file.type.startsWith("image/");
      }
      if (type.startsWith(".")) {
        return file.name.toLowerCase().endsWith(type.toLowerCase());
      }
      return file.type === type;
    });

    if (!isValidType) {
      ElMessage.error(`文件格式不正确，请选择 ${acceptText.value} 格式的文件`);
      return false;
    }
  }

  // 验证文件大小
  if (props.validateSize && props.maxSize) {
    const maxSizeBytes = props.maxSize * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      ElMessage.error(`文件大小不能超过 ${maxSizeText.value}`);
      return false;
    }
  }

  return true;
};

const handleBeforeUpload: UploadProps["beforeUpload"] = async (file) => {
  // 自定义验证
  if (props.beforeUpload) {
    const result = await props.beforeUpload(file);
    if (!result) return false;
  }

  // 内置验证
  return validateFile(file);
};

const handleProgress: UploadProps["onProgress"] = (event, file, fileList) => {
  emit("progress", event, file, fileList);
};

const handleSuccess: UploadProps["onSuccess"] = (response, file, fileList) => {
  emit("success", response, file, fileList);
};

const handleError: UploadProps["onError"] = (error, file, fileList) => {
  ElMessage.error(`文件上传失败: ${error.message}`);
  emit("error", error, file, fileList);
};

const handleChange: UploadProps["onChange"] = (file, uploadFileList) => {
  // 更新文件列表
  const newFileList = uploadFileList.map((item) => ({
    ...item,
    uid: item.uid,
    name: item.name,
    status: item.status || ("ready" as const),
    size: item.size,
    percentage: item.percentage,
    url: item.url,
    raw: item.raw,
  }));

  fileList.value = newFileList;
};

const handleRemove: UploadProps["onRemove"] = (file, uploadFileList) => {
  fileList.value = uploadFileList.map((f) => ({
    ...f,
    uid: f.uid,
    name: f.name,
    status: f.status || ("ready" as const),
    size: f.size,
    percentage: f.percentage,
    url: f.url,
    raw: f.raw,
  }));
  emit("remove", file, uploadFileList);
};

const handlePreview: UploadProps["onPreview"] = (file) => {
  if (isImage(file)) {
    previewUrl.value = getFileUrl(file);
    previewName.value = file.name || "";
    previewVisible.value = true;
  }
  emit("preview", file);
};

const handleExceed: UploadProps["onExceed"] = (files, fileList) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`);
  emit("exceed", files, fileList);
};

const downloadImage = () => {
  if (previewUrl.value) {
    const link = document.createElement("a");
    link.href = previewUrl.value;
    link.download = previewName.value;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};

// 暴露方法
const clearFiles = () => {
  uploadRef.value?.clearFiles();
};

const abort = (file?: UploadFile) => {
  uploadRef.value?.abort(file);
};

const submit = () => {
  uploadRef.value?.submit();
};

defineExpose({
  clearFiles,
  abort,
  submit,
});
</script>

<style lang="scss" scoped>
.image-upload {
  .upload-dragger {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    text-align: center;
    border: 2px dashed var(--el-border-color);
    border-radius: var(--el-border-radius-base);
    background-color: var(--el-fill-color-lighter);
    transition: all 0.3s;

    &:hover {
      border-color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
    }

    .upload-icon {
      font-size: 48px;
      color: var(--el-text-color-placeholder);
      margin-bottom: 16px;
    }

    .upload-text {
      .upload-title {
        font-size: 16px;
        color: var(--el-text-color-primary);
        margin-bottom: 8px;
      }

      .upload-hint {
        font-size: 14px;
        color: var(--el-text-color-regular);
      }
    }
  }

  .upload-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 148px;
    height: 148px;
    border: 1px dashed var(--el-border-color);
    border-radius: var(--el-border-radius-base);
    background-color: var(--el-fill-color-lighter);
    transition: all 0.3s;
    cursor: pointer;

    &:hover {
      border-color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
    }

    .el-icon {
      font-size: 28px;
      color: var(--el-text-color-placeholder);
      margin-bottom: 8px;
    }

    .upload-card-text {
      font-size: 14px;
      color: var(--el-text-color-regular);
    }
  }

  .upload-tip {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    margin-top: 8px;
    line-height: 1.5;
  }

  .custom-file-list {
    margin-top: 16px;

    .file-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border: 1px solid var(--el-border-color);
      border-radius: var(--el-border-radius-base);
      background-color: var(--el-bg-color);
      margin-bottom: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: var(--el-color-primary-light-7);
        background-color: var(--el-color-primary-light-9);
      }

      &.file-item-error {
        border-color: var(--el-color-danger-light-7);
        background-color: var(--el-color-danger-light-9);
      }

      .file-preview {
        flex-shrink: 0;
        width: 48px;
        height: 48px;
        margin-right: 12px;
        border-radius: var(--el-border-radius-small);
        overflow: hidden;
        background-color: var(--el-fill-color-light);
        display: flex;
        align-items: center;
        justify-content: center;

        .file-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          cursor: pointer;
        }

        .file-icon {
          font-size: 24px;
          color: var(--el-text-color-placeholder);
        }
      }

      .file-info {
        flex: 1;
        min-width: 0;

        .file-name {
          font-size: 14px;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .file-size {
          font-size: 12px;
          color: var(--el-text-color-regular);
          margin-bottom: 4px;
        }

        .file-progress {
          margin-top: 4px;
        }

        .file-error {
          font-size: 12px;
          color: var(--el-color-danger);
        }
      }

      .file-actions {
        flex-shrink: 0;
        display: flex;
        gap: 8px;
      }
    }
  }

  .image-preview {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;

    .preview-image {
      max-width: 100%;
      max-height: 70vh;
      object-fit: contain;
    }
  }

  .preview-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .preview-name {
      font-size: 14px;
      color: var(--el-text-color-regular);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 60%;
    }

    .preview-actions {
      display: flex;
      gap: 12px;
    }
  }
}

// 上传组件状态样式
:deep(.el-upload) {
  &.upload-disabled {
    .upload-dragger,
    .upload-card {
      cursor: not-allowed;
      opacity: 0.6;

      &:hover {
        border-color: var(--el-border-color);
        background-color: var(--el-fill-color-lighter);
      }
    }
  }

  &.upload-limit-exceeded {
    .upload-dragger,
    .upload-card {
      display: none;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .image-upload {
    .upload-dragger {
      padding: 30px 15px;

      .upload-icon {
        font-size: 36px;
        margin-bottom: 12px;
      }

      .upload-text {
        .upload-title {
          font-size: 15px;
        }

        .upload-hint {
          font-size: 13px;
        }
      }
    }

    .upload-card {
      width: 120px;
      height: 120px;

      .el-icon {
        font-size: 24px;
      }

      .upload-card-text {
        font-size: 13px;
      }
    }

    .custom-file-list {
      .file-item {
        padding: 10px;

        .file-preview {
          width: 40px;
          height: 40px;
          margin-right: 10px;
        }
      }
    }
  }
}
</style>
