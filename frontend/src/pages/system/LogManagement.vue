<template>
  <div class="log-management">
    <div class="page-header">
      <h2>日志管理</h2>
      <p>系统操作日志查看与管理</p>
      <div class="header-actions">
        <el-button type="primary" @click="exportLogs" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出日志
        </el-button>
        <el-button type="danger" @click="clearLogs" :loading="clearing">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form
        ref="searchFormRef"
        :model="searchForm"
        inline
        label-width="80px"
      >
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名、操作内容"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="日志级别">
          <el-select
            v-model="searchForm.level"
            placeholder="请选择日志级别"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="信息" value="info" />
            <el-option label="警告" value="warning" />
            <el-option label="错误" value="error" />
            <el-option label="调试" value="debug" />
          </el-select>
        </el-form-item>

        <el-form-item label="操作类型">
          <el-select
            v-model="searchForm.operation"
            placeholder="请选择操作类型"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="登录" value="login" />
            <el-option label="登出" value="logout" />
            <el-option label="创建" value="create" />
            <el-option label="更新" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="查看" value="view" />
            <el-option label="导出" value="export" />
            <el-option label="导入" value="import" />
          </el-select>
        </el-form-item>

        <el-form-item label="用户">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="IP地址">
          <el-input
            v-model="searchForm.ip"
            placeholder="请输入IP地址"
            clearable
            style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon info">
              <el-icon><InfoFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.info }}</div>
              <div class="stat-label">信息日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon warning">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.warning }}</div>
              <div class="stat-label">警告日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon error">
              <el-icon><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.error }}</div>
              <div class="stat-label">错误日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>日志列表</span>
          <div class="header-actions">
            <el-button
              type="primary"
              size="small"
              @click="refreshLogs"
              :loading="loading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="logList"
        stripe
        border
        style="width: 100%"
        :default-sort="{ prop: 'createdAt', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column prop="level" label="级别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="operation"
          label="操作类型"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getOperationTagType(row.operation)" size="small">
              {{ getOperationText(row.operation) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户" width="120" />

        <el-table-column prop="message" label="操作内容" min-width="200">
          <template #default="{ row }">
            <div class="log-message" :title="row.message">
              {{ row.message }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="ip" label="IP地址" width="140" />

        <el-table-column prop="userAgent" label="用户代理" width="200">
          <template #default="{ row }">
            <div class="user-agent" :title="row.userAgent">
              {{ formatUserAgent(row.userAgent) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="createdAt"
          label="时间"
          width="180"
          sortable="custom"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              text
              @click="viewLogDetail(row)"
            >
              详情
            </el-button>
            <el-button type="danger" size="small" text @click="deleteLog(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">
            {{ currentLog.id }}
          </el-descriptions-item>
          <el-descriptions-item label="日志级别">
            <el-tag :type="getLevelTagType(currentLog.level)" size="small">
              {{ getLevelText(currentLog.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag
              :type="getOperationTagType(currentLog.operation)"
              size="small"
            >
              {{ getOperationText(currentLog.operation) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ currentLog.username || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ currentLog.userId || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ currentLog.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            {{ currentLog.method || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="请求路径">
            {{ currentLog.path || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="状态码">
            <el-tag
              :type="
                currentLog?.statusCode && currentLog.statusCode >= 400
                  ? 'danger'
                  : 'success'
              "
              size="small"
            >
              {{ currentLog?.statusCode || "-" }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">
            {{ currentLog.responseTime ? `${currentLog.responseTime}ms` : "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(currentLog.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">操作内容</el-divider>
        <div class="log-message-detail">
          {{ currentLog.message }}
        </div>

        <el-divider content-position="left">用户代理</el-divider>
        <div class="user-agent-detail">
          {{ currentLog.userAgent }}
        </div>

        <template v-if="currentLog.requestData">
          <el-divider content-position="left">请求数据</el-divider>
          <el-input
            v-model="currentLog.requestData"
            type="textarea"
            :rows="6"
            readonly
          />
        </template>

        <template v-if="currentLog.responseData">
          <el-divider content-position="left">响应数据</el-divider>
          <el-input
            v-model="currentLog.responseData"
            type="textarea"
            :rows="6"
            readonly
          />
        </template>

        <template v-if="currentLog.errorStack">
          <el-divider content-position="left">错误堆栈</el-divider>
          <el-input
            v-model="currentLog.errorStack"
            type="textarea"
            :rows="8"
            readonly
          />
        </template>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import {
  Search,
  Refresh,
  Download,
  Delete,
  InfoFilled,
  WarningFilled,
  CircleCloseFilled,
  DocumentCopy,
} from "@element-plus/icons-vue";
import { systemApi } from "@/api/modules/system";
import type { SystemLog, SystemLogQueryParams } from "@/types/system";

// 响应式数据
const loading = ref(false);
const exporting = ref(false);
const clearing = ref(false);
const detailDialogVisible = ref(false);

// 表单引用
const searchFormRef = ref<FormInstance>();

// 搜索表单
const searchForm = reactive<SystemLogQueryParams & { dateRange: string[] }>({
  keyword: "",
  level: undefined,
  operation: "",
  username: "",
  ip: "",
  dateRange: [],
  page: 1,
  size: 20,
  sortBy: "timestamp",
  sortOrder: "desc",
});

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0,
});

// 日志列表
const logList = ref<SystemLog[]>([]);

// 当前查看的日志
const currentLog = ref<SystemLog | null>(null);

// 统计信息
const stats = reactive({
  info: 0,
  warning: 0,
  error: 0,
  total: 0,
});

// 计算属性
const hasSelection = computed(() => {
  return logList.value.some((item) => item.selected);
});

// 方法
const loadLogs = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size,
      startTime: searchForm.dateRange?.[0] || "",
      endTime: searchForm.dateRange?.[1] || "",
    };

    const res = await systemApi.getSystemLogs(params);
    if (res.success) {
      logList.value = res.data.list;
      pagination.total = res.data.total;
    } else {
      ElMessage.error(res.message || "加载日志失败");
    }
  } catch (error) {
    console.error("加载日志失败:", error);
    ElMessage.error("加载日志失败");
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    const res = await systemApi.getLogStatistics();
    if (res.success) {
      Object.assign(stats, res.data);
    }
  } catch (error) {
    console.error("加载统计信息失败:", error);
  }
};

const handleSearch = () => {
  pagination.page = 1;
  loadLogs();
};

const resetSearch = () => {
  searchFormRef.value?.resetFields();
  Object.assign(searchForm, {
    keyword: "",
    level: "",
    operation: "",
    username: "",
    ip: "",
    dateRange: [],
    page: 1,
    size: 20,
    sortBy: "createdAt",
    sortOrder: "desc",
  });
  pagination.page = 1;
  loadLogs();
};

const refreshLogs = () => {
  loadLogs();
  loadStats();
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  loadLogs();
};

const handleCurrentChange = (page: number) => {
  pagination.page = page;
  loadLogs();
};

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  // 只接受支持的排序字段
  const validSortFields: Array<SystemLogQueryParams["sortBy"]> = [
    "type",
    "timestamp",
    "level",
    "module",
    "createdAt",
  ];

  if (validSortFields.includes(prop as SystemLogQueryParams["sortBy"])) {
    searchForm.sortBy = prop as SystemLogQueryParams["sortBy"];
    searchForm.sortOrder = order === "ascending" ? "asc" : "desc";
    loadLogs();
  }
};

const viewLogDetail = (log: SystemLog) => {
  currentLog.value = log;
  detailDialogVisible.value = true;
};

const deleteLog = async (log: SystemLog) => {
  try {
    await ElMessageBox.confirm(`确定要删除这条日志吗？`, "确认删除", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    const res = await systemApi.deleteSystemLog(log.id);
    if (res.success) {
      ElMessage.success("删除成功");
      loadLogs();
      loadStats();
    } else {
      ElMessage.error(res.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除日志失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

const exportLogs = async () => {
  exporting.value = true;
  try {
    const params = {
      ...searchForm,
      startTime: searchForm.dateRange?.[0] || "",
      endTime: searchForm.dateRange?.[1] || "",
    };

    const res = await systemApi.exportSystemLogs(params);
    if (res.success) {
      // 下载文件
      const link = document.createElement("a");
      link.href = res.data.url;
      link.download = `system-logs-${new Date().toISOString().split("T")[0]}.xlsx`;
      link.click();
      ElMessage.success("导出成功");
    } else {
      ElMessage.error(res.message || "导出失败");
    }
  } catch (error) {
    console.error("导出日志失败:", error);
    ElMessage.error("导出失败");
  } finally {
    exporting.value = false;
  }
};

const clearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      "确定要清空所有日志吗？此操作不可恢复！",
      "确认清空",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    clearing.value = true;
    const res = await systemApi.clearSystemLogs();
    if (res.success) {
      ElMessage.success("清空成功");
      loadLogs();
      loadStats();
    } else {
      ElMessage.error(res.message || "清空失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("清空日志失败:", error);
      ElMessage.error("清空失败");
    }
  } finally {
    clearing.value = false;
  }
};

// 辅助方法
const getLevelTagType = (
  level: string,
): "info" | "warning" | "success" | "primary" | "danger" => {
  const typeMap: Record<
    string,
    "info" | "warning" | "success" | "primary" | "danger"
  > = {
    info: "info",
    warning: "warning",
    error: "danger",
    debug: "success",
    critical: "danger",
  };
  return typeMap[level] || "info";
};

const getLevelText = (level: string): string => {
  const textMap: Record<string, string> = {
    info: "信息",
    warning: "警告",
    error: "错误",
    debug: "调试",
    critical: "严重",
  };
  return textMap[level] || level;
};

const getOperationTagType = (
  operation?: string,
): "info" | "warning" | "success" | "primary" | "danger" => {
  if (!operation) return "info";
  const typeMap: Record<
    string,
    "info" | "warning" | "success" | "primary" | "danger"
  > = {
    login: "success",
    logout: "info",
    create: "primary",
    update: "warning",
    delete: "danger",
    view: "info",
    export: "success",
    import: "primary",
  };
  return typeMap[operation] || "info";
};

const getOperationText = (operation?: string): string => {
  if (!operation) return "-";
  const textMap: Record<string, string> = {
    login: "登录",
    logout: "登出",
    create: "创建",
    update: "更新",
    delete: "删除",
    view: "查看",
    export: "导出",
    import: "导入",
  };
  return textMap[operation] || operation;
};

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return "-";
  return new Date(dateTime).toLocaleString("zh-CN");
};

const formatUserAgent = (userAgent: string) => {
  if (!userAgent) return "-";

  // 简化用户代理字符串显示
  if (userAgent.includes("Chrome")) {
    return "Chrome";
  } else if (userAgent.includes("Firefox")) {
    return "Firefox";
  } else if (userAgent.includes("Safari")) {
    return "Safari";
  } else if (userAgent.includes("Edge")) {
    return "Edge";
  } else {
    return userAgent.substring(0, 20) + "...";
  }
};

// 生命周期
onMounted(() => {
  loadLogs();
  loadStats();
});
</script>

<style scoped lang="scss">
.log-management {
  padding: $spacing-lg;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-lg;

    h2 {
      margin: 0 0 $spacing-xs 0;
      color: $text-primary;
      font-size: 24px;
      font-weight: 600;
    }

    p {
      margin: 0;
      color: $text-secondary;
      font-size: 14px;
    }

    .header-actions {
      display: flex;
      gap: $spacing-sm;
    }
  }

  .search-card {
    margin-bottom: $spacing-lg;

    :deep(.el-card__body) {
      padding: $spacing-lg;
    }
  }

  .stats-row {
    margin-bottom: $spacing-lg;

    .stat-card {
      :deep(.el-card__body) {
        padding: $spacing-lg;
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: $spacing-md;

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;

          &.info {
            background: rgba(64, 158, 255, 0.1);
            color: #409eff;
          }

          &.warning {
            background: rgba(230, 162, 60, 0.1);
            color: #e6a23c;
          }

          &.error {
            background: rgba(245, 108, 108, 0.1);
            color: #f56c6c;
          }

          &.total {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: $text-primary;
            line-height: 1;
            margin-bottom: $spacing-xs;
          }

          .stat-label {
            font-size: 14px;
            color: $text-secondary;
          }
        }
      }
    }
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        font-weight: 600;
        color: $text-primary;
      }

      .header-actions {
        display: flex;
        gap: $spacing-sm;
      }
    }

    .log-message {
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-agent {
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: $spacing-lg;
  }

  .log-detail {
    .log-message-detail,
    .user-agent-detail {
      padding: $spacing-md;
      background: $bg-color-light;
      border-radius: 4px;
      font-family: monospace;
      font-size: 14px;
      line-height: 1.5;
      word-break: break-all;
    }
  }
}

:deep(.el-card) {
  border: none;
  box-shadow: $box-shadow-light;
}

:deep(.el-form-item) {
  margin-bottom: $spacing-md;
}

:deep(.el-table) {
  .el-table__header {
    th {
      background: $bg-color-light;
      color: $text-primary;
      font-weight: 600;
    }
  }
}

:deep(.el-descriptions) {
  .el-descriptions__label {
    font-weight: 600;
  }
}

:deep(.el-divider) {
  margin: $spacing-lg 0;
}
</style>
<parameter name="rewrite">false</parameter>
