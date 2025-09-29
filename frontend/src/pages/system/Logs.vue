<template>
  <div class="system-logs">
    <div class="page-header">
      <h2>操作日志</h2>
      <p>系统操作记录与审计日志</p>
    </div>

    <div class="logs-content">
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-card>
          <div class="filter-form">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item label="操作模块">
                  <el-select
                    v-model="filters.module"
                    placeholder="选择模块"
                    clearable
                  >
                    <el-option label="用户管理" value="user" />
                    <el-option label="角色管理" value="role" />
                    <el-option label="权限管理" value="permission" />
                    <el-option label="档案管理" value="archive" />
                    <el-option label="借阅管理" value="borrow" />
                    <el-option label="系统设置" value="system" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="操作类型">
                  <el-select
                    v-model="filters.action"
                    placeholder="选择操作"
                    clearable
                  >
                    <el-option label="登录" value="login" />
                    <el-option label="登出" value="logout" />
                    <el-option label="新增" value="create" />
                    <el-option label="编辑" value="update" />
                    <el-option label="删除" value="delete" />
                    <el-option label="查看" value="view" />
                    <el-option label="导出" value="export" />
                    <el-option label="导入" value="import" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="操作结果">
                  <el-select
                    v-model="filters.status"
                    placeholder="选择结果"
                    clearable
                  >
                    <el-option label="成功" value="success" />
                    <el-option label="失败" value="failed" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="操作人员">
                  <el-input
                    v-model="filters.operator"
                    placeholder="输入用户名或姓名"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="操作时间">
                  <el-date-picker
                    v-model="filters.dateRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="IP地址">
                  <el-input
                    v-model="filters.ip"
                    placeholder="输入IP地址"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">
                    <el-icon><Search /></el-icon>
                    查询
                  </el-button>
                  <el-button @click="handleReset">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="left-actions">
          <el-button
            type="danger"
            @click="handleBatchDelete"
            :disabled="selectedLogs.length === 0"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>
            导出日志
          </el-button>
          <el-button @click="handleCleanup">
            <el-icon><Delete /></el-icon>
            清理日志
          </el-button>
        </div>
        <div class="right-info">
          <el-text type="info">共 {{ pagination.total }} 条记录</el-text>
        </div>
      </div>

      <!-- 日志表格 -->
      <el-table
        :data="logs"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="log-detail">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="请求URL">{{
                  row.url
                }}</el-descriptions-item>
                <el-descriptions-item label="请求方法">{{
                  row.method
                }}</el-descriptions-item>
                <el-descriptions-item label="用户代理">{{
                  row.userAgent
                }}</el-descriptions-item>
                <el-descriptions-item label="操作耗时"
                  >{{ row.duration }}ms</el-descriptions-item
                >
                <el-descriptions-item label="请求参数" :span="2">
                  <pre>{{ formatJson(row.params) }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="响应结果" :span="2">
                  <pre>{{ formatJson(row.response) }}</pre>
                </el-descriptions-item>
                <el-descriptions-item
                  label="错误信息"
                  :span="2"
                  v-if="row.error"
                >
                  <el-text type="danger">{{ row.error }}</el-text>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作人员" width="120">
          <template #default="{ row }">
            <div class="operator-info">
              <el-avatar :size="24" :src="row.avatar">{{
                row.operator.charAt(0)
              }}</el-avatar>
              <span style="margin-left: 8px">{{ row.operator }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="100">
          <template #default="{ row }">
            <el-tag :type="getModuleTagType(row.module)" size="small">
              {{ getModuleLabel(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.action)" size="small">
              {{ getActionLabel(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column
          prop="status"
          label="操作结果"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'success' ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === "success" ? "成功" : "失败" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="location" label="地理位置" width="120" />
        <el-table-column prop="createdAt" label="操作时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
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
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="800px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="操作ID">{{
          currentLog.id
        }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{
          currentLog.operator
        }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{
          getModuleLabel(currentLog.module)
        }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{
          getActionLabel(currentLog.action)
        }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{
          currentLog.description
        }}</el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag
            :type="currentLog.status === 'success' ? 'success' : 'danger'"
          >
            {{ currentLog.status === "success" ? "成功" : "失败" }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{
          currentLog.createdAt
        }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{
          currentLog.ip
        }}</el-descriptions-item>
        <el-descriptions-item label="地理位置">{{
          currentLog.location
        }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{
          currentLog.url
        }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{
          currentLog.method
        }}</el-descriptions-item>
        <el-descriptions-item label="用户代理" :span="2">{{
          currentLog.userAgent
        }}</el-descriptions-item>
        <el-descriptions-item label="操作耗时"
          >{{ currentLog.duration }}ms</el-descriptions-item
        >
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <pre class="json-content">{{ formatJson(currentLog.response) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item
          label="错误信息"
          :span="2"
          v-if="currentLog.error"
        >
          <el-text type="danger">{{ currentLog.error }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 清理日志对话框 -->
    <el-dialog v-model="cleanupDialogVisible" title="清理日志" width="500px">
      <el-form :model="cleanupForm" label-width="120px">
        <el-form-item label="清理策略">
          <el-radio-group v-model="cleanupForm.strategy">
            <el-radio value="days">按天数清理</el-radio>
            <el-radio value="count">按数量清理</el-radio>
            <el-radio value="all">清理全部</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="保留天数" v-if="cleanupForm.strategy === 'days'">
          <el-input-number v-model="cleanupForm.days" :min="1" :max="365" />
          <span style="margin-left: 8px; color: #999">天</span>
        </el-form-item>
        <el-form-item label="保留数量" v-if="cleanupForm.strategy === 'count'">
          <el-input-number
            v-model="cleanupForm.count"
            :min="100"
            :max="100000"
          />
          <span style="margin-left: 8px; color: #999">条</span>
        </el-form-item>
        <el-form-item>
          <el-alert
            title="注意：清理操作不可恢复，请谨慎操作！"
            type="warning"
            :closable="false"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cleanupDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmCleanup">确定清理</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Refresh, Delete, Download } from "@element-plus/icons-vue";

const loading = ref(false);
const selectedLogs = ref<any[]>([]);
const detailDialogVisible = ref(false);
const cleanupDialogVisible = ref(false);
const currentLog = ref<any>(null);

// 筛选条件
const filters = reactive({
  module: "",
  action: "",
  status: "",
  operator: "",
  dateRange: [],
  ip: "",
});

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0,
});

// 清理表单
const cleanupForm = reactive({
  strategy: "days",
  days: 30,
  count: 10000,
});

// 模拟日志数据
const logs = ref([
  {
    id: 1,
    operator: "张三",
    avatar: "",
    module: "user",
    action: "login",
    description: "用户登录系统",
    status: "success",
    ip: "192.168.1.100",
    location: "北京市",
    url: "/api/auth/login",
    method: "POST",
    userAgent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    duration: 245,
    params: { username: "zhangsan", password: "******" },
    response: {
      code: 200,
      message: "登录成功",
      token: "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    },
    error: null,
    createdAt: "2024-01-15 09:30:25",
  },
  {
    id: 2,
    operator: "李四",
    avatar: "",
    module: "archive",
    action: "create",
    description: "新增档案记录",
    status: "success",
    ip: "192.168.1.101",
    location: "上海市",
    url: "/api/archives",
    method: "POST",
    userAgent:
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
    duration: 156,
    params: { title: "重要文件", category: "合同", content: "..." },
    response: { code: 200, message: "创建成功", id: 123 },
    error: null,
    createdAt: "2024-01-15 10:15:42",
  },
  {
    id: 3,
    operator: "王五",
    avatar: "",
    module: "user",
    action: "delete",
    description: "删除用户账号",
    status: "failed",
    ip: "192.168.1.102",
    location: "广州市",
    url: "/api/users/456",
    method: "DELETE",
    userAgent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    duration: 89,
    params: { id: 456 },
    response: { code: 403, message: "权限不足" },
    error: "用户权限不足，无法删除该账号",
    createdAt: "2024-01-15 11:22:18",
  },
]);

// 方法
const getModuleTagType = (module: string) => {
  switch (module) {
    case "user":
      return "primary";
    case "role":
      return "success";
    case "permission":
      return "warning";
    case "archive":
      return "info";
    case "borrow":
      return "danger";
    case "system":
      return "info";
    default:
      return "info";
  }
};

const getModuleLabel = (module: string) => {
  switch (module) {
    case "user":
      return "用户管理";
    case "role":
      return "角色管理";
    case "permission":
      return "权限管理";
    case "archive":
      return "档案管理";
    case "borrow":
      return "借阅管理";
    case "system":
      return "系统设置";
    default:
      return module;
  }
};

const getActionTagType = (action: string) => {
  switch (action) {
    case "login":
      return "success";
    case "logout":
      return "info";
    case "create":
      return "primary";
    case "update":
      return "warning";
    case "delete":
      return "danger";
    case "view":
      return "info";
    case "export":
      return "success";
    case "import":
      return "primary";
    default:
      return "info";
  }
};

const getActionLabel = (action: string) => {
  switch (action) {
    case "login":
      return "登录";
    case "logout":
      return "登出";
    case "create":
      return "新增";
    case "update":
      return "编辑";
    case "delete":
      return "删除";
    case "view":
      return "查看";
    case "export":
      return "导出";
    case "import":
      return "导入";
    default:
      return action;
  }
};

const formatJson = (obj: any) => {
  if (!obj) return "";
  return JSON.stringify(obj, null, 2);
};

const handleSearch = () => {
  loading.value = true;
  // 搜索逻辑
  setTimeout(() => {
    loading.value = false;
    ElMessage.success("查询完成");
  }, 1000);
};

const handleReset = () => {
  Object.assign(filters, {
    module: "",
    action: "",
    status: "",
    operator: "",
    dateRange: [],
    ip: "",
  });
  handleSearch();
};

const handleSelectionChange = (selection: any[]) => {
  selectedLogs.value = selection;
};

const handleViewDetail = (row: any) => {
  currentLog.value = row;
  detailDialogVisible.value = true;
};

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除ID为 ${row.id} 的日志记录吗？`,
      "删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    ElMessage.success("删除成功");
  } catch {
    // 用户取消
  }
};

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedLogs.value.length} 条日志记录吗？`,
      "批量删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    ElMessage.success("批量删除成功");
    selectedLogs.value = [];
  } catch {
    // 用户取消
  }
};

const handleExport = () => {
  ElMessage.success("导出功能开发中...");
};

const handleCleanup = () => {
  cleanupDialogVisible.value = true;
};

const confirmCleanup = async () => {
  try {
    let message = "";
    switch (cleanupForm.strategy) {
      case "days":
        message = `确定要清理 ${cleanupForm.days} 天前的日志吗？`;
        break;
      case "count":
        message = `确定要只保留最新的 ${cleanupForm.count} 条日志吗？`;
        break;
      case "all":
        message = "确定要清理全部日志吗？";
        break;
    }

    await ElMessageBox.confirm(message, "清理确认", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    ElMessage.success("日志清理成功");
    cleanupDialogVisible.value = false;
  } catch {
    // 用户取消
  }
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  handleSearch();
};

const handlePageChange = (page: number) => {
  pagination.page = page;
  handleSearch();
};

onMounted(() => {
  pagination.total = logs.value.length;
});
</script>

<style lang="scss" scoped>
.system-logs {
  padding: $spacing-lg;
}

.page-header {
  margin-bottom: $spacing-lg;

  h2 {
    margin: 0 0 $spacing-sm 0;
    color: $text-primary;
  }

  p {
    margin: 0;
    color: $text-secondary;
  }
}

.filter-section {
  margin-bottom: $spacing-lg;

  .filter-form {
    .el-form-item {
      margin-bottom: $spacing-md;
    }
  }
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  .left-actions {
    display: flex;
    gap: $spacing-sm;
  }
}

.operator-info {
  display: flex;
  align-items: center;
}

.log-detail {
  padding: $spacing-lg;
  background-color: $bg-light;

  pre {
    background-color: $bg-white;
    padding: $spacing-sm;
    border-radius: $border-radius;
    border: 1px solid $border-light;
    font-size: 0.85rem;
    max-height: 200px;
    overflow-y: auto;
  }
}

.json-content {
  background-color: $bg-light;
  padding: $spacing-sm;
  border-radius: $border-radius;
  font-size: 0.85rem;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-lg;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
