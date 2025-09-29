<template>
  <div class="user-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-description">
        管理系统用户信息，包括用户的创建、编辑、删除和权限设置
      </p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
        <el-button
          type="danger"
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出用户
        </el-button>
      </div>
      <div class="action-right">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.total }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon active">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.active }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon admin">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.admin }}</div>
              <div class="stat-label">管理员</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon new">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.newThisMonth }}</div>
              <div class="stat-label">本月新增</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入真实姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
            v-model="searchForm.email"
            placeholder="请输入邮箱"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
          >
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="访客" value="guest" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
          >
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === "active" ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 'active' ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === "active" ? "禁用" : "启用" }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
            <el-option label="访客" value="guest" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="userForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Delete,
  Download,
  Refresh,
  Search,
  RefreshLeft,
} from "@element-plus/icons-vue";
import { format } from "date-fns";

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const selectedRows = ref<unknown[]>([]);

// 统计数据
const stats = reactive({
  total: 0,
  active: 0,
  admin: 0,
  newThisMonth: 0,
});

// 搜索表单
const searchForm = reactive({
  username: "",
  realName: "",
  email: "",
  role: "",
  status: "",
});

// 分页数据
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0,
});

// 表格数据
const tableData = ref<unknown[]>([]);

// 用户表单
const userForm = reactive({
  id: null,
  username: "",
  realName: "",
  email: "",
  phone: "",
  password: "",
  role: "user",
  status: "active",
  remark: "",
});

// 表单验证规则
const userFormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 3,
      max: 20,
      message: "用户名长度在 3 到 20 个字符",
      trigger: "blur",
    },
  ],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    {
      type: "email" as const,
      message: "请输入正确的邮箱格式",
      trigger: "blur",
    },
  ],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号格式",
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" },
  ],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
};

const userFormRef = ref();

// 计算属性
const dialogTitle = computed(() => (isEdit.value ? "编辑用户" : "新增用户"));

// 方法
const loadData = async () => {
  loading.value = true;
  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 模拟数据
    const mockData = [
      {
        id: 1,
        username: "admin",
        realName: "系统管理员",
        email: "admin@example.com",
        phone: "13800138000",
        role: "admin",
        status: "active",
        lastLoginTime: new Date("2024-01-15 10:30:00"),
        createTime: new Date("2024-01-01 09:00:00"),
        remark: "系统管理员账户",
      },
      {
        id: 2,
        username: "user001",
        realName: "张三",
        email: "zhangsan@example.com",
        phone: "13800138001",
        role: "user",
        status: "active",
        lastLoginTime: new Date("2024-01-14 16:20:00"),
        createTime: new Date("2024-01-02 14:30:00"),
        remark: "普通用户",
      },
      {
        id: 3,
        username: "user002",
        realName: "李四",
        email: "lisi@example.com",
        phone: "13800138002",
        role: "user",
        status: "inactive",
        lastLoginTime: new Date("2024-01-10 11:15:00"),
        createTime: new Date("2024-01-03 10:45:00"),
        remark: "已禁用用户",
      },
    ];

    tableData.value = mockData;
    pagination.total = mockData.length;

    // 更新统计数据
    stats.total = mockData.length;
    stats.active = mockData.filter((item) => item.status === "active").length;
    stats.admin = mockData.filter((item) => item.role === "admin").length;
    stats.newThisMonth = mockData.filter((item) => {
      const createTime = new Date(item.createTime);
      const now = new Date();
      return (
        createTime.getMonth() === now.getMonth() &&
        createTime.getFullYear() === now.getFullYear()
      );
    }).length;
  } catch (error) {
    ElMessage.error("加载数据失败");
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadData();
};

const handleReset = () => {
  Object.assign(searchForm, {
    username: "",
    realName: "",
    email: "",
    role: "",
    status: "",
  });
  handleSearch();
};

const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = (row: {
  id: number;
  username: string;
  realName: string;
  email: string;
  phone: string;
  role: string;
  status: string;
  remark: string;
}) => {
  isEdit.value = true;
  Object.assign(userForm, { ...row });
  dialogVisible.value = true;
};

const handleDelete = async (row: { realName: string }) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.realName}" 吗？`,
      "删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    // 模拟删除操作
    ElMessage.success("删除成功");
    loadData();
  } catch {
    // 用户取消删除
  }
};

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个用户吗？`,
      "批量删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    // 模拟批量删除操作
    ElMessage.success("批量删除成功");
    selectedRows.value = [];
    loadData();
  } catch {
    // 用户取消删除
  }
};

const handleToggleStatus = async (row: {
  status: string;
  realName: string;
}) => {
  const action = row.status === "active" ? "禁用" : "启用";
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.realName}" 吗？`,
      `${action}确认`,
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    // 模拟状态切换操作
    row.status = row.status === "active" ? "inactive" : "active";
    ElMessage.success(`${action}成功`);
  } catch {
    // 用户取消操作
  }
};

const handleExport = () => {
  ElMessage.info("导出功能开发中...");
};

const refreshData = () => {
  loadData();
};

const handleSelectionChange = (selection: unknown[]) => {
  selectedRows.value = selection;
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  loadData();
};

const handleCurrentChange = (current: number) => {
  pagination.current = current;
  loadData();
};

const handleSubmit = async () => {
  if (!userFormRef.value) return;

  try {
    await userFormRef.value.validate();
    submitting.value = true;

    // 模拟提交操作
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success(isEdit.value ? "更新成功" : "创建成功");
    dialogVisible.value = false;
    loadData();
  } catch (error) {
    console.error("表单验证失败:", error);
  } finally {
    submitting.value = false;
  }
};

const handleDialogClose = () => {
  resetForm();
};

const resetForm = () => {
  Object.assign(userForm, {
    id: null,
    username: "",
    realName: "",
    email: "",
    phone: "",
    password: "",
    role: "user",
    status: "active",
    remark: "",
  });
  userFormRef.value?.clearValidate();
};

const getRoleType = (role: string) => {
  const typeMap: Record<
    string,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    admin: "danger",
    user: "primary",
    guest: "info",
  };
  return typeMap[role] || "info";
};

const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    admin: "管理员",
    user: "普通用户",
    guest: "访客",
  };
  return textMap[role] || "未知";
};

const formatDateTime = (date: Date | string) => {
  if (!date) return "-";
  return format(new Date(date), "yyyy-MM-dd HH:mm:ss");
};

// 生命周期
onMounted(() => {
  loadData();
});
</script>

<style lang="scss" scoped>
.user-list-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .page-description {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .action-left {
      display: flex;
      gap: 12px;
    }
  }

  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        font-size: 24px;
        color: white;

        &.total {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.active {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.admin {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.new {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
      }

      .stat-content {
        .stat-number {
          font-size: 28px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .table-section {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .pagination-section {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .user-list-page {
    padding: 10px;

    .action-bar {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .action-left {
        justify-content: center;
      }
    }

    .stats-cards {
      :deep(.el-col) {
        margin-bottom: 12px;
      }
    }

    .search-section {
      :deep(.el-form) {
        .el-form-item {
          margin-bottom: 12px;
        }
      }
    }
  }
}
</style>
