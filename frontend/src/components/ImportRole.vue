<template>
  <div class="import-role">
    <div class="import-header">
      <h3>角色导入</h3>
      <p class="import-description">
        支持通过Excel文件批量导入角色数据，请先下载模板文件了解数据格式
      </p>
    </div>

    <div class="import-content">
      <!-- 步骤指示器 -->
      <el-steps :active="currentStep" align-center>
        <el-step title="下载模板" description="获取导入模板" />
        <el-step title="准备数据" description="填写角色数据" />
        <el-step title="上传文件" description="选择Excel文件" />
        <el-step title="导入完成" description="查看导入结果" />
      </el-steps>

      <!-- 步骤1: 下载模板 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-card class="template-card">
          <template #header>
            <div class="card-header">
              <span>下载导入模板</span>
              <el-button type="primary" @click="downloadTemplate" :loading="downloading">
                <el-icon><Download /></el-icon>
                下载模板
              </el-button>
            </div>
          </template>
          
          <div class="template-info">
            <el-alert
              title="模板说明"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <ul class="template-tips">
                  <li>请使用下载的Excel模板文件</li>
                  <li>必填字段：角色名称、角色编码、角色描述</li>
                  <li>角色编码必须唯一，建议使用英文和下划线</li>
                  <li>状态字段：active(启用) 或 inactive(禁用)</li>
                  <li>系统角色字段：true(是) 或 false(否)</li>
                </ul>
              </template>
            </el-alert>
          </div>
        </el-card>
      </div>

      <!-- 步骤2: 准备数据 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-card class="data-card">
          <template #header>
            <span>数据准备说明</span>
          </template>
          
          <div class="data-requirements">
            <h4>数据要求：</h4>
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="requirement-item">
                  <h5>必填字段</h5>
                  <ul>
                    <li>角色名称 (name)</li>
                    <li>角色编码 (code)</li>
                    <li>角色描述 (description)</li>
                  </ul>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="requirement-item">
                  <h5>可选字段</h5>
                  <ul>
                    <li>状态 (status)</li>
                    <li>排序 (sort)</li>
                    <li>系统角色 (isSystem)</li>
                    <li>数据权限 (dataScope)</li>
                    <li>备注 (remark)</li>
                  </ul>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

      <!-- 步骤3: 上传文件 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-card class="upload-card">
          <template #header>
            <span>上传Excel文件</span>
          </template>
          
          <div class="upload-area">
            <el-upload
              ref="uploadRef"
              class="upload-dragger"
              drag
              :action="uploadAction"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :file-list="fileList"
              :auto-upload="false"
              accept=".xlsx,.xls"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  只能上传 xlsx/xls 文件，且不超过 10MB
                </div>
              </template>
            </el-upload>
          </div>

          <div class="upload-actions" v-if="fileList.length > 0">
            <el-button @click="clearFiles">清除文件</el-button>
            <el-button type="primary" @click="submitUpload" :loading="uploading">
              开始导入
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 步骤4: 导入结果 -->
      <div v-if="currentStep === 3" class="step-content">
        <el-card class="result-card">
          <template #header>
            <span>导入结果</span>
          </template>
          
          <div class="import-result">
            <el-alert
              :title="`导入完成！成功 ${importResult.success} 条，失败 ${importResult.failed} 条`"
              :type="importResult.failed === 0 ? 'success' : 'warning'"
              :closable="false"
              show-icon
            />
            
            <div class="result-details" v-if="importResult.errors && importResult.errors.length > 0">
              <h4>错误详情：</h4>
              <el-table :data="importResult.errors" stripe border max-height="300">
                <el-table-column prop="row" label="行号" width="80" />
                <el-table-column prop="field" label="字段" width="120" />
                <el-table-column prop="value" label="值" width="150" />
                <el-table-column prop="message" label="错误信息" />
              </el-table>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="import-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button 
        v-if="currentStep > 0" 
        @click="prevStep"
      >
        上一步
      </el-button>
      <el-button 
        v-if="currentStep < 2" 
        type="primary" 
        @click="nextStep"
      >
        下一步
      </el-button>
      <el-button 
        v-if="currentStep === 3" 
        type="primary" 
        @click="handleFinish"
      >
        完成
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue";
import { ElMessage, ElUpload, type UploadProps, type UploadUserFile } from "element-plus";
import { Download, UploadFilled } from "@element-plus/icons-vue";
import { downloadRoleTemplate, importRoles } from "@/api/modules/role";

// Emits
const emit = defineEmits<{
  success: [result: ImportResult];
  cancel: [];
}>();

// 响应式数据
const currentStep = ref(0);
const downloading = ref(false);
const uploading = ref(false);
const fileList = ref<UploadUserFile[]>([]);
const uploadRef = ref<InstanceType<typeof ElUpload>>();

// 导入结果
const importResult = reactive<ImportResult>({
  success: 0,
  failed: 0,
  errors: [],
});

// 上传配置
const uploadAction = computed(() => "#");

// 接口类型定义
interface ImportResult {
  success: number;
  failed: number;
  errors: Array<{
    row: number;
    field: string;
    value: string;
    message: string;
  }>;
}

// 下载模板
const downloadTemplate = async () => {
  try {
    downloading.value = true;
    const response = await downloadRoleTemplate();
    
    // 创建下载链接
    const blob = new Blob([response], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `角色导入模板_${new Date().toISOString().slice(0, 10)}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success("模板下载成功");
  } catch (error) {
    console.error("下载模板失败:", error);
    ElMessage.error("模板下载失败");
  } finally {
    downloading.value = false;
  }
};

// 上传前验证
const beforeUpload = (file: File) => {
  const isExcel = file.type === "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                  file.type === "application/vnd.ms-excel";
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isExcel) {
    ElMessage.error("只能上传Excel文件!");
    return false;
  }
  if (!isLt10M) {
    ElMessage.error("文件大小不能超过 10MB!");
    return false;
  }

  return true;
};

// 上传成功处理
const handleUploadSuccess = (response: any, file: UploadUserFile) => {
  if (response.success) {
    Object.assign(importResult, response.data);
    currentStep.value = 3;
    ElMessage.success("文件上传成功");
  } else {
    ElMessage.error(response.message || "文件上传失败");
  }
};

// 上传失败处理
const handleUploadError = (error: any) => {
  console.error("上传失败:", error);
  ElMessage.error("文件上传失败");
};

// 提交上传
const submitUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning("请先选择要上传的文件");
    return;
  }

  try {
    uploading.value = true;
    const file = fileList.value[0].raw;
    if (!file) {
      ElMessage.error("文件不存在");
      return;
    }

    const response = await importRoles(file);
    if (response.success) {
      Object.assign(importResult, response.data);
      currentStep.value = 3;
      ElMessage.success("角色导入成功");
    } else {
      ElMessage.error(response.message || "角色导入失败");
    }
  } catch (error) {
    console.error("导入失败:", error);
    ElMessage.error("角色导入失败");
  } finally {
    uploading.value = false;
  }
};

// 清除文件
const clearFiles = () => {
  fileList.value = [];
  uploadRef.value?.clearFiles();
};

// 下一步
const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++;
  }
};

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
};

// 完成导入
const handleFinish = () => {
  emit("success", importResult);
};

// 取消导入
const handleCancel = () => {
  emit("cancel");
};
</script>

<style scoped lang="scss">
.import-role {
  .import-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .import-description {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .import-content {
    margin-bottom: 24px;

    .step-content {
      margin-top: 24px;
    }

    .template-card,
    .data-card,
    .upload-card,
    .result-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-weight: 600;
      }
    }

    .template-info {
      .template-tips {
        margin: 8px 0 0 0;
        padding-left: 20px;
        
        li {
          margin-bottom: 4px;
          font-size: 14px;
        }
      }
    }

    .data-requirements {
      h4 {
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }

      .requirement-item {
        h5 {
          margin: 0 0 12px 0;
          font-size: 14px;
          font-weight: 600;
          color: #606266;
        }

        ul {
          margin: 0;
          padding-left: 20px;
          
          li {
            margin-bottom: 6px;
            font-size: 14px;
            color: #606266;
          }
        }
      }
    }

    .upload-area {
      .upload-dragger {
        width: 100%;
      }
    }

    .upload-actions {
      margin-top: 16px;
      text-align: center;
    }

    .import-result {
      .result-details {
        margin-top: 16px;

        h4 {
          margin: 0 0 12px 0;
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .import-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
  }
}

:deep(.el-steps) {
  margin-bottom: 24px;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>

