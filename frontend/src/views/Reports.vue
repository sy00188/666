<template>
  <div class="reports-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">报表管理</h1>
        <p class="page-description">生成和管理各类数据报表，支持多种格式导出</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showGenerator = true">
          <el-icon><Plus /></el-icon>
          新建报表
        </el-button>
      </div>
    </div>

    <!-- 报表列表 -->
    <ReportList @create="showGenerator = true" />

    <!-- 报表生成器对话框 -->
    <el-dialog
      v-model="showGenerator"
      title="新建报表"
      width="900px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      destroy-on-close
    >
      <ReportGenerator
        @success="handleGenerateSuccess"
        @cancel="showGenerator = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import ReportList from "@/components/reports/ReportList.vue";
import ReportGenerator from "@/components/reports/ReportGenerator.vue";

// 响应式数据
const showGenerator = ref(false);

// 方法
const handleGenerateSuccess = (reportName: string) => {
  showGenerator.value = false;
  ElMessage.success(`报表 "${reportName}" 已开始生成`);

  // 可以在这里添加刷新列表的逻辑
  // 由于ReportList组件内部会自动轮询状态，所以不需要手动刷新
};
</script>

<style scoped>
.reports-page {
  padding: 24px;
  background-color: var(--el-bg-color-page);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px;
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-description {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reports-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }

  .page-title {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .page-header {
    background: var(--el-bg-color-overlay);
  }
}
</style>
