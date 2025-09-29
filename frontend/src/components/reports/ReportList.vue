<template>
  <div class="report-list">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics?.totalReports || 0 }}</div>
              <div class="stat-label">总报表数</div>
            </div>
            <el-icon class="stat-icon"><Document /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">
                {{ statistics?.pendingReports || 0 }}
              </div>
              <div class="stat-label">生成中</div>
            </div>
            <el-icon class="stat-icon"><Loading /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">
                {{ statistics?.completedReports || 0 }}
              </div>
              <div class="stat-label">今日完成</div>
            </div>
            <el-icon class="stat-icon"><Check /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">
                {{ statistics?.failedReports || 0 }}
              </div>
              <div class="stat-label">失败</div>
            </div>
            <el-icon class="stat-icon"><Close /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input
            v-model="searchText"
            placeholder="搜索报表名称..."
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="待生成" :value="ReportStatus.PENDING" />
            <el-option label="生成中" :value="ReportStatus.GENERATING" />
            <el-option label="已完成" :value="ReportStatus.COMPLETED" />
            <el-option label="失败" :value="ReportStatus.FAILED" />
            <el-option label="已取消" :value="ReportStatus.CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="typeFilter" placeholder="类型筛选" clearable>
            <el-option label="全部" value="" />
            <el-option
              label="档案统计"
              :value="ReportType.ARCHIVE_STATISTICS"
            />
            <el-option label="借阅分析" :value="ReportType.BORROW_ANALYSIS" />
            <el-option label="用户活动" :value="ReportType.USER_ACTIVITY" />
            <el-option
              label="系统性能"
              :value="ReportType.SYSTEM_PERFORMANCE"
            />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-col>
      </el-row>
    </el-card>

    <!-- 报表列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>报表列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="$emit('create')">
              <el-icon><Plus /></el-icon>
              新建报表
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading.reports"
        :data="filteredReports"
        style="width: 100%"
      >
        <el-table-column prop="name" label="报表名称" min-width="200">
          <template #default="{ row }">
            <div class="report-name">
              <el-icon><Document /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="format" label="格式" width="80">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{
              row.format.toUpperCase()
            }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <div
              v-if="row.status === ReportStatus.GENERATING"
              class="progress-container"
            >
              <el-progress :percentage="row.progress || 0" :stroke-width="6" />
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="fileSize" label="文件大小" width="100">
          <template #default="{ row }">
            <span v-if="row.fileSize">{{ formatFileSize(row.fileSize) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === ReportStatus.COMPLETED"
                type="primary"
                size="small"
                @click="handleDownload(row)"
              >
                下载
              </el-button>

              <el-button
                v-if="row.status === ReportStatus.GENERATING"
                type="warning"
                size="small"
                @click="handleCancel(row)"
              >
                取消
              </el-button>

              <el-button
                v-if="row.status === ReportStatus.FAILED"
                type="success"
                size="small"
                @click="handleRetry(row)"
              >
                重试
              </el-button>

              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredReports.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Document,
  Loading,
  Check,
  Close,
  Search,
  Plus,
} from "@element-plus/icons-vue";
import { useReports } from "@/composables/useReports";
import type { ReportInstance, ReportStatistics } from "@/types/report";
import { ReportStatus, ReportType, ReportFormat } from "@/types/report";

// Emits
defineEmits<{
  create: [];
}>();

// Composables
const {
  reports,
  statistics,
  loading,
  loadReports,
  loadStatistics,
  deleteReport,
  downloadReport,
  cancelReport,
  retryReport,
} = useReports();

// 响应式数据
const searchText = ref("");
const statusFilter = ref<ReportStatus | "">("");
const typeFilter = ref<ReportType | "">("");
const dateRange = ref<[string, string] | null>(null);
const currentPage = ref(1);
const pageSize = ref(20);

// 计算属性
const filteredReports = computed(() => {
  let filtered = reports.value;

  // 搜索过滤
  if (searchText.value) {
    filtered = filtered.filter((report) =>
      report.name.toLowerCase().includes(searchText.value.toLowerCase()),
    );
  }

  // 状态过滤
  if (statusFilter.value) {
    filtered = filtered.filter(
      (report) => report.status === statusFilter.value,
    );
  }

  // 类型过滤
  if (typeFilter.value) {
    filtered = filtered.filter((report) => report.type === typeFilter.value);
  }

  // 日期范围过滤
  if (dateRange.value) {
    const [startDate, endDate] = dateRange.value;
    filtered = filtered.filter((report) => {
      const reportDate = report.createdAt.split(" ")[0];
      return reportDate >= startDate && reportDate <= endDate;
    });
  }

  return filtered;
});

// 方法
const handleSearch = () => {
  currentPage.value = 1;
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
};

const handleDownload = async (report: ReportInstance) => {
  try {
    await downloadReport(report.id);
    ElMessage.success("报表下载成功");
  } catch (error) {
    console.error("下载失败:", error);
    ElMessage.error("报表下载失败");
  }
};

const handleCancel = async (report: ReportInstance) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消生成报表"${report.name}"吗？`,
      "取消生成",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await cancelReport(report.id);
    await loadReports();
  } catch (error) {
    if (error !== "cancel") {
      console.error("取消失败:", error);
    }
  }
};

const handleRetry = async (report: ReportInstance) => {
  try {
    await retryReport(report.id);
    await loadReports();
  } catch (error) {
    console.error("重试失败:", error);
  }
};

const handleDelete = async (report: ReportInstance) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除报表"${report.name}"吗？此操作不可恢复。`,
      "删除报表",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await deleteReport(report.id);
    await loadReports();
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
    }
  }
};

// 辅助方法
const getStatusLabel = (status: ReportStatus): string => {
  const statusMap = {
    [ReportStatus.PENDING]: "待生成",
    [ReportStatus.GENERATING]: "生成中",
    [ReportStatus.COMPLETED]: "已完成",
    [ReportStatus.FAILED]: "失败",
    [ReportStatus.CANCELLED]: "已取消",
    [ReportStatus.EXPIRED]: "已过期",
  };
  return statusMap[status] || status;
};

const getStatusType = (
  status: ReportStatus,
): "primary" | "success" | "warning" | "info" | "danger" => {
  const typeMap: Record<
    ReportStatus,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    [ReportStatus.PENDING]: "info",
    [ReportStatus.GENERATING]: "warning",
    [ReportStatus.COMPLETED]: "success",
    [ReportStatus.FAILED]: "danger",
    [ReportStatus.CANCELLED]: "info",
    [ReportStatus.EXPIRED]: "info",
  };
  return typeMap[status] || "info";
};

const getTypeLabel = (type: ReportType): string => {
  const typeMap = {
    [ReportType.ARCHIVE_STATISTICS]: "档案统计",
    [ReportType.BORROW_ANALYSIS]: "借阅分析",
    [ReportType.USER_ACTIVITY]: "用户活动",
    [ReportType.SYSTEM_PERFORMANCE]: "系统性能",
    [ReportType.CUSTOM]: "自定义",
  };
  return typeMap[type] || type;
};

const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleString("zh-CN");
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

// 生命周期
onMounted(() => {
  loadReports();
  loadStatistics();
});

// 监听筛选条件变化
watch([statusFilter, typeFilter, dateRange], () => {
  currentPage.value = 1;
});
</script>

<style scoped>
.report-list {
  padding: 20px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.stat-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 32px;
  color: var(--el-color-primary);
  opacity: 0.3;
}

.filter-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-container {
  width: 80px;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
