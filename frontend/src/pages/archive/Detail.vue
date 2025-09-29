<template>
  <div class="archive-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button
          type="text"
          :icon="ArrowLeft"
          @click="goBack"
          class="back-button"
        >
          返回列表
        </el-button>
        <div class="header-info">
          <h2>{{ archiveDetail?.title || "档案详情" }}</h2>
          <p>档案编号：{{ archiveDetail?.archiveNo }}</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canEdit"
          type="primary"
          :icon="Edit"
          @click="handleEdit"
        >
          编辑
        </el-button>
        <el-button
          v-if="canBorrow"
          type="success"
          :icon="Download"
          @click="handleBorrow"
        >
          借阅
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button type="default" :icon="More">
            更多操作
            <el-icon class="el-icon--right">
              <ArrowDown />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="download" :icon="Download">
                下载
              </el-dropdown-item>
              <el-dropdown-item command="archive" :icon="Box" v-if="canArchive">
                归档
              </el-dropdown-item>
              <el-dropdown-item
                command="delete"
                :icon="Delete"
                v-if="canDelete"
              >
                删除
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 档案基本信息 -->
    <div class="info-section">
      <div class="section-header">
        <h3>基本信息</h3>
        <el-tag :type="getStatusTagType(archiveDetail?.status)">
          {{ getStatusText(archiveDetail?.status) }}
        </el-tag>
      </div>
      <div class="info-grid">
        <div class="info-item">
          <label>档案标题：</label>
          <span>{{ archiveDetail?.title }}</span>
        </div>
        <div class="info-item">
          <label>档案编号：</label>
          <span>{{ archiveDetail?.archiveNo }}</span>
        </div>
        <div class="info-item">
          <label>档案分类：</label>
          <span>{{ archiveDetail?.categoryName }}</span>
        </div>
        <div class="info-item">
          <label>保密等级：</label>
          <el-tag :type="getSecurityLevelTagType(archiveDetail?.securityLevel)">
            {{ getSecurityLevelText(archiveDetail?.securityLevel) }}
          </el-tag>
        </div>
        <div class="info-item">
          <label>关键词：</label>
          <span>{{ archiveDetail?.keywords || "无" }}</span>
        </div>
        <div class="info-item">
          <label>保存期限：</label>
          <span>{{
            archiveDetail?.retentionPeriod
              ? `${archiveDetail.retentionPeriod}年`
              : "永久"
          }}</span>
        </div>
        <div class="info-item">
          <label>创建人：</label>
          <span>{{ archiveDetail?.createdByName }}</span>
        </div>
        <div class="info-item">
          <label>创建时间：</label>
          <span>{{ formatDateTime(archiveDetail?.createdAt) }}</span>
        </div>
        <div class="info-item">
          <label>最后更新：</label>
          <span>{{ formatDateTime(archiveDetail?.updatedAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 档案描述 -->
    <div class="info-section" v-if="archiveDetail?.description">
      <div class="section-header">
        <h3>档案描述</h3>
      </div>
      <div class="description-content">
        <p>{{ archiveDetail.description }}</p>
      </div>
    </div>

    <!-- 附件文件 -->
    <div
      class="info-section"
      v-if="archiveDetail?.files && archiveDetail.files.length > 0"
    >
      <div class="section-header">
        <h3>附件文件</h3>
        <span class="file-count"
          >共 {{ archiveDetail.files.length }} 个文件</span
        >
      </div>
      <div class="files-list">
        <div
          v-for="file in archiveDetail.files"
          :key="file.fileId"
          class="file-item"
        >
          <div class="file-info">
            <el-icon class="file-icon">
              <Document />
            </el-icon>
            <div class="file-details">
              <div class="file-name">{{ file.originalName }}</div>
              <div class="file-meta">
                <span>{{ formatFileSize(file.fileSize) }}</span>
                <span>{{ formatDateTime(file.uploadTime) }}</span>
              </div>
            </div>
          </div>
          <div class="file-actions">
            <el-button
              type="text"
              :icon="View"
              @click="previewFile(file)"
              v-if="canPreview(file)"
            >
              预览
            </el-button>
            <el-button type="text" :icon="Download" @click="downloadFile(file)">
              下载
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作日志 -->
    <div class="info-section">
      <div class="section-header">
        <h3>操作日志</h3>
      </div>
      <el-timeline>
        <el-timeline-item
          v-for="log in operationLogs"
          :key="log.id"
          :timestamp="formatDateTime(log.createdAt)"
          placement="top"
        >
          <div class="log-content">
            <div class="log-action">{{ log.action }}</div>
            <div class="log-user">操作人：{{ log.operatorName }}</div>
            <div class="log-remark" v-if="log.remark">{{ log.remark }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ArrowLeft,
  Edit,
  Download,
  More,
  ArrowDown,
  Box,
  Delete,
  Document,
  View,
} from "@element-plus/icons-vue";
import { archiveApi } from "@/api/modules/archive";
import { useUserStore } from "@/stores/user";
import type {
  Archive,
  ArchiveFile,
  OperationLog,
  ArchiveStatus,
  SecurityLevel,
} from "@/types/archive";
import { formatDateTime, formatFileSize } from "@/utils/format";

// 路由和状态管理
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const loading = ref(false);
const archiveDetail = ref<Archive | null>(null);
const operationLogs = ref<OperationLog[]>([]);

// 计算属性
const archiveId = computed(() => route.params.id as string);

// 权限检查
const canEdit = computed(() => {
  if (!archiveDetail.value) return false;
  return (
    userStore.hasPermission("archive:edit") ||
    archiveDetail.value.createdBy === userStore.currentUser?.id
  );
});

const canBorrow = computed(() => {
  return (
    userStore.hasPermission("archive:borrow") &&
    archiveDetail.value?.status === "approved"
  );
});

const canArchive = computed(() => {
  return (
    userStore.hasPermission("archive:archive") &&
    archiveDetail.value?.status === "approved"
  );
});

const canDelete = computed(() => {
  if (!archiveDetail.value) return false;
  return (
    userStore.hasPermission("archive:delete") ||
    archiveDetail.value.createdBy === userStore.currentUser?.id
  );
});

// 获取档案详情
const getArchiveDetail = async () => {
  const archiveId = Number(route.params.id);
  if (!archiveId) {
    ElMessage.error("档案ID无效");
    return;
  }

  loading.value = true;
  try {
    const response = await archiveApi.getArchiveDetail(archiveId);
    if (response.success) {
      archiveDetail.value = response.data;
      // 获取操作日志
      await getOperationLogs(archiveId);
    } else {
      ElMessage.error(response.message || "获取档案详情失败");
    }
  } catch (error) {
    console.error("获取档案详情失败:", error);
    ElMessage.error("获取档案详情失败");
  } finally {
    loading.value = false;
  }
};

// 获取操作日志
const getOperationLogs = async (archiveId: number) => {
  try {
    // 这里应该调用获取操作日志的API
    // const response = await archiveApi.getOperationLogs(archiveId);
    // operationLogs.value = response.data;

    // 临时模拟数据
    operationLogs.value = [
      {
        id: 1,
        action: "创建档案",
        operatorName: "张三",
        createdAt: new Date().toISOString(),
        remark: "初始创建",
      },
      {
        id: 2,
        action: "提交审核",
        operatorName: "张三",
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        remark: "提交审核",
      },
    ];
  } catch (error) {
    console.error("获取操作日志失败:", error);
  }
};

// 状态标签类型
const getStatusTagType = (status?: string) => {
  const statusMap: Record<string, string> = {
    draft: "info",
    pending: "warning",
    approved: "success",
    rejected: "danger",
    archived: "info",
  };
  return statusMap[status || ""] || "info";
};

// 状态文本
const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    draft: "草稿",
    pending: "审核中",
    approved: "已通过",
    rejected: "已拒绝",
    archived: "已归档",
  };
  return statusMap[status || ""] || "未知";
};

// 保密等级标签类型
const getSecurityLevelTagType = (level?: string) => {
  const levelMap: Record<string, string> = {
    public: "success",
    internal: "warning",
    confidential: "danger",
    secret: "danger",
  };
  return levelMap[level || ""] || "info";
};

// 保密等级文本
const getSecurityLevelText = (level?: string) => {
  const levelMap: Record<string, string> = {
    public: "公开",
    internal: "内部",
    confidential: "机密",
    secret: "绝密",
  };
  return levelMap[level || ""] || "未知";
};

// 判断文件是否可预览
const canPreview = (file: ArchiveFile) => {
  const previewableTypes = [
    "pdf",
    "jpg",
    "jpeg",
    "png",
    "gif",
    "txt",
    "doc",
    "docx",
  ];
  const fileExt = file.originalName.split(".").pop()?.toLowerCase();
  return previewableTypes.includes(fileExt || "");
};

// 返回列表
const goBack = () => {
  router.back();
};

// 编辑档案
const handleEdit = () => {
  router.push(`/archive/edit/${route.params.id}`);
};

// 借阅档案
const handleBorrow = async () => {
  try {
    await ElMessageBox.confirm("确定要借阅此档案吗？", "确认借阅", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "info",
    });

    // 调用借阅API
    ElMessage.success("借阅申请已提交");
  } catch (error) {
    if (error !== "cancel") {
      console.error("借阅失败:", error);
      ElMessage.error("借阅失败");
    }
  }
};

// 更多操作
const handleMoreAction = async (command: string) => {
  switch (command) {
    case "download":
      await downloadArchive();
      break;
    case "archive":
      await archiveDocument();
      break;
    case "delete":
      await deleteArchive();
      break;
  }
};

// 下载档案
const downloadArchive = async () => {
  try {
    ElMessage.success("开始下载档案");
    // 实现下载逻辑
  } catch (error) {
    console.error("下载失败:", error);
    ElMessage.error("下载失败");
  }
};

// 归档文档
const archiveDocument = async () => {
  try {
    await ElMessageBox.confirm(
      "确定要归档此档案吗？归档后档案将不可编辑。",
      "确认归档",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await archiveApi.archiveDocument(Number(route.params.id));
    if (response.success) {
      ElMessage.success("归档成功");
      await getArchiveDetail();
    } else {
      ElMessage.error(response.message || "归档失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("归档失败:", error);
      ElMessage.error("归档失败");
    }
  }
};

// 删除档案
const deleteArchive = async () => {
  try {
    await ElMessageBox.confirm(
      "确定要删除此档案吗？此操作不可恢复。",
      "确认删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await archiveApi.deleteArchive(Number(route.params.id));
    if (response.success) {
      ElMessage.success("删除成功");
      router.push("/archive");
    } else {
      ElMessage.error(response.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// 预览文件
const previewFile = (file: ArchiveFile) => {
  // 实现文件预览逻辑
  ElMessage.info("文件预览功能开发中");
};

// 下载文件
const downloadFile = (file: ArchiveFile) => {
  // 实现文件下载逻辑
  const link = document.createElement("a");
  link.href = file.filePath;
  link.download = file.originalName;
  link.click();
};

// 组件挂载时获取数据
onMounted(() => {
  getArchiveDetail();
});
</script>

<style scoped>
.archive-detail-container {
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
}

.back-button {
  margin-right: 15px;
  font-size: 14px;
}

.header-info h2 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 20px;
}

.header-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.info-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.file-count {
  color: #666;
  font-size: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 500;
  color: #666;
  min-width: 100px;
  margin-right: 10px;
}

.info-item span {
  color: #333;
  flex: 1;
}

.description-content {
  color: #333;
  line-height: 1.6;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
}

.file-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.file-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 12px;
}

.file-details {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.file-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.log-content {
  padding: 10px 0;
}

.log-action {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.log-user {
  font-size: 14px;
  color: #666;
  margin-bottom: 3px;
}

.log-remark {
  font-size: 13px;
  color: #999;
}

.loading-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-left {
    width: 100%;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
