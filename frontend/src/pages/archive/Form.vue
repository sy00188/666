<template>
  <div class="archive-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="text" @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="page-title">{{ isEdit ? "编辑档案" : "新增档案" }}</h1>
      </div>
      <div class="header-right">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ isEdit ? "更新" : "保存" }}
        </el-button>
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="form-content">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="archive-form"
      >
        <!-- 基本信息 -->
        <el-card class="form-section" shadow="never">
          <template #header>
            <span class="section-title">基本信息</span>
          </template>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="档案标题" prop="title">
                <el-input
                  v-model="formData.title"
                  placeholder="请输入档案标题"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="档案类别" prop="categoryId">
                <el-select
                  v-model="formData.categoryId"
                  placeholder="请选择档案类别"
                  style="width: 100%"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.categoryId"
                    :label="category.categoryName"
                    :value="category.categoryId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="安全级别" prop="securityLevel">
                <el-select
                  v-model="formData.securityLevel"
                  placeholder="请选择安全级别"
                  style="width: 100%"
                >
                  <el-option
                    v-for="level in securityLevels"
                    :key="level.value"
                    :label="level.label"
                    :value="level.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保存期限" prop="retentionPeriod">
                <el-input-number
                  v-model="formData.retentionPeriod"
                  :min="1"
                  :max="100"
                  placeholder="年"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="关键词" prop="keywords">
            <el-input
              v-model="formData.keywords"
              placeholder="请输入关键词，多个关键词用逗号分隔"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="档案描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="4"
              placeholder="请输入档案描述"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <!-- 文件上传 -->
        <el-card class="form-section" shadow="never">
          <template #header>
            <span class="section-title">文件管理</span>
          </template>

          <el-upload
            ref="uploadRef"
            class="file-upload"
            drag
            multiple
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            :limit="10"
            :on-exceed="handleExceed"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传多个文件，单个文件大小不超过100MB，最多上传10个文件
              </div>
            </template>
          </el-upload>

          <!-- 已上传文件列表 -->
          <div v-if="uploadedFiles.length > 0" class="uploaded-files">
            <h4>已上传文件</h4>
            <div class="file-list">
              <div
                v-for="file in uploadedFiles"
                :key="file.fileId"
                class="file-item"
              >
                <div class="file-info">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <div class="file-details">
                    <div class="file-name">{{ file.originalName }}</div>
                    <div class="file-meta">
                      {{ formatFileSize(file.fileSize) }} |
                      {{ formatDateTime(file.uploadTime) }}
                    </div>
                  </div>
                </div>
                <div class="file-actions">
                  <el-button
                    type="text"
                    size="small"
                    @click="downloadFile(file)"
                  >
                    <el-icon><Download /></el-icon>
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click="removeUploadedFile(file)"
                    class="delete-btn"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type UploadInstance,
} from "element-plus";
import {
  ArrowLeft,
  UploadFilled,
  Document,
  Download,
  Delete,
} from "@element-plus/icons-vue";
import { archiveApi } from "@/api/modules/archive";
import { useAuthStore } from "@/stores/auth";
import { formatDateTime, formatFileSize } from "@/utils/format";
import type {
  Archive,
  ArchiveFile,
  ArchiveCreateRequest,
  ArchiveUpdateRequest,
  ArchiveCategory,
  SecurityLevel,
} from "@/types/archive";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 表单引用
const formRef = ref<FormInstance>();
const uploadRef = ref<UploadInstance>();

// 响应式数据
const loading = ref(false);
const saving = ref(false);
const categories = ref<ArchiveCategory[]>([]);
const fileList = ref<any[]>([]);
const uploadedFiles = ref<ArchiveFile[]>([]);

// 计算属性
const archiveId = computed(() => Number(route.params.id));
const isEdit = computed(() => !!archiveId.value && archiveId.value > 0);
const uploadUrl = computed(() => "/api/files/upload");
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${authStore.token}`,
}));

// 表单数据
const formData = ref<ArchiveCreateRequest & { archiveId?: number }>({
  title: "",
  description: "",
  categoryId: 0,
  securityLevel: 1 as SecurityLevel,
  keywords: "",
  retentionPeriod: 10,
  files: [],
});

// 安全级别选项
const securityLevels = [
  { value: 1, label: "公开" },
  { value: 2, label: "内部" },
  { value: 3, label: "机密" },
  { value: 4, label: "绝密" },
];

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: "请输入档案标题", trigger: "blur" },
    {
      min: 2,
      max: 200,
      message: "标题长度在 2 到 200 个字符",
      trigger: "blur",
    },
  ],
  categoryId: [
    { required: true, message: "请选择档案类别", trigger: "change" },
  ],
  securityLevel: [
    { required: true, message: "请选择安全级别", trigger: "change" },
  ],
  retentionPeriod: [
    { required: true, message: "请输入保存期限", trigger: "blur" },
    {
      type: "number" as const,
      min: 1,
      max: 100,
      message: "保存期限在 1 到 100 年之间",
      trigger: "blur",
    },
  ],
};

// 方法
const goBack = () => {
  router.back();
};

const loadCategories = async () => {
  try {
    const response = await archiveApi.getArchiveCategories();
    categories.value = response.data;
  } catch (error) {
    console.error("加载档案类别失败:", error);
    ElMessage.error("加载档案类别失败");
  }
};

const loadArchiveDetail = async () => {
  if (!isEdit.value) return;

  loading.value = true;
  try {
    const response = await archiveApi.getArchiveDetail(archiveId.value);
    const archive = response.data;

    // 填充表单数据
    formData.value = {
      archiveId: archive.archiveId,
      title: archive.title,
      description: archive.description || "",
      categoryId: archive.categoryId,
      securityLevel: archive.securityLevel,
      keywords: archive.keywords || "",
      retentionPeriod: archive.retentionPeriod || 10,
    };

    // 设置已上传文件
    uploadedFiles.value = archive.files || [];
  } catch (error) {
    console.error("加载档案详情失败:", error);
    ElMessage.error("加载档案详情失败");
    goBack();
  } finally {
    loading.value = false;
  }
};

const beforeUpload = (file: File) => {
  const isValidSize = file.size <= 100 * 1024 * 1024; // 100MB
  if (!isValidSize) {
    ElMessage.error("文件大小不能超过 100MB");
    return false;
  }
  return true;
};

const handleUploadSuccess = (response: any, file: any) => {
  if (response.success) {
    uploadedFiles.value.push(response.data);
    ElMessage.success("文件上传成功");
  } else {
    ElMessage.error(response.message || "文件上传失败");
  }
};

const handleUploadError = (error: any) => {
  console.error("文件上传失败:", error);
  ElMessage.error("文件上传失败");
};

const handleFileRemove = (file: any) => {
  // 处理文件移除
};

const handleExceed = () => {
  ElMessage.warning("最多只能上传 10 个文件");
};

const downloadFile = (file: ArchiveFile) => {
  window.open(`/api/files/download/${file.fileId}`, "_blank");
};

const removeUploadedFile = async (file: ArchiveFile) => {
  try {
    await ElMessageBox.confirm("确定要删除这个文件吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    // 从列表中移除
    const index = uploadedFiles.value.findIndex(
      (f) => f.fileId === file.fileId,
    );
    if (index > -1) {
      uploadedFiles.value.splice(index, 1);
      ElMessage.success("文件删除成功");
    }
  } catch {
    // 用户取消删除
  }
};

const handleSave = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();

    saving.value = true;

    if (isEdit.value) {
      // 更新档案
      const updateData: ArchiveUpdateRequest = {
        title: formData.value.title,
        description: formData.value.description,
        categoryId: formData.value.categoryId,
        securityLevel: formData.value.securityLevel,
        keywords: formData.value.keywords,
        retentionPeriod: formData.value.retentionPeriod,
      };

      await archiveApi.updateArchive(archiveId.value, updateData);
      ElMessage.success("档案更新成功");
    } else {
      // 创建档案
      const createData: ArchiveCreateRequest = {
        title: formData.value.title,
        description: formData.value.description,
        categoryId: formData.value.categoryId,
        securityLevel: formData.value.securityLevel,
        keywords: formData.value.keywords,
        retentionPeriod: formData.value.retentionPeriod,
      };

      await archiveApi.createArchive(createData);
      ElMessage.success("档案创建成功");
    }

    // 返回列表页
    router.push("/archive");
  } catch (error) {
    console.error("保存失败:", error);
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
};

// 生命周期
onMounted(async () => {
  await loadCategories();
  if (isEdit.value) {
    await loadArchiveDetail();
  }
});
</script>

<style scoped>
.archive-form-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 12px;
}

.form-content {
  max-width: 1200px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.archive-form {
  padding: 20px;
}

.file-upload {
  margin-bottom: 20px;
}

.uploaded-files {
  margin-top: 20px;
}

.uploaded-files h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}

.file-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.file-item:last-child {
  border-bottom: none;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-icon {
  font-size: 20px;
  color: #909399;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.file-meta {
  font-size: 12px;
  color: #909399;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f56c6c;
}
</style>
