<template>
  <div class="user-management">
    <PermissionGuard permission="user:view">
      <div class="page-header">
        <h2>用户管理</h2>
        <div class="header-actions">
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleAdd"
            v-permission="'user:create'"
          >
            新增用户
          </el-button>
        </div>
      </div>

      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select
              v-model="searchForm.role"
              placeholder="请选择角色"
              clearable
            >
              <el-option
                v-for="role in roleOptions"
                :key="role.value"
                :label="role.label"
                :value="role.value"
              />
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
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <div class="table-container">
        <!-- 错误状态 -->
        <div v-if="error && !loading" class="error-container">
          <el-alert :title="error" type="error" show-icon :closable="false">
            <template #default>
              <div class="error-actions">
                <el-button type="primary" size="small" @click="handleRetry">
                  重试
                </el-button>
                <el-button size="small" @click="handleRefresh">
                  刷新
                </el-button>
              </div>
            </template>
          </el-alert>
        </div>

        <!-- 表格工具栏 -->
        <div v-if="!error" class="table-toolbar">
          <div class="toolbar-left">
            <span class="total-count">共 {{ total }} 条记录</span>
          </div>
          <div class="toolbar-right">
            <el-button
              type="primary"
              :icon="Plus"
              @click="handleAdd"
              v-permission="'user:create'"
            >
              新增用户
            </el-button>
            <el-button
              :icon="ArrowDown"
              @click="handleRefresh"
              :loading="loading"
            >
              刷新
            </el-button>
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table
          v-if="!error"
          :data="tableData"
          v-loading="loading"
          stripe
          border
          empty-text="暂无数据"
          :default-sort="{ prop: 'createdAt', order: 'descending' }"
        >
          <el-table-column
            prop="username"
            label="用户名"
            width="120"
            sortable
          />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="200" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="roleName" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)">
                {{ row.roleName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                {{ row.status === "active" ? "启用" : "禁用" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="lastLoginAt"
            label="最后登录"
            width="180"
            sortable
          />
          <el-table-column
            prop="createdAt"
            label="创建时间"
            width="180"
            sortable
          />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                @click="handleView(row)"
                v-permission="'user:view'"
              >
                查看
              </el-button>
              <el-button
                size="small"
                type="primary"
                @click="handleEdit(row)"
                v-permission="'user:edit'"
              >
                编辑
              </el-button>
              <el-dropdown
                @command="(command) => handleMoreAction(command, row)"
              >
                <el-button size="small">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      command="resetPassword"
                      v-permission-show="'user:reset_password'"
                    >
                      重置密码
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="row.status === 'active' ? 'disable' : 'enable'"
                      v-permission-show="'user:edit'"
                    >
                      {{ row.status === "active" ? "禁用" : "启用" }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="delete"
                      divided
                      v-permission-show="'user:delete'"
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <template #fallback>
        <el-result
          icon="warning"
          title="权限不足"
          sub-title="您没有权限访问用户管理功能"
        >
          <template #extra>
            <el-button @click="$router.go(-1)">返回</el-button>
          </template>
        </el-result>
      </template>
    </PermissionGuard>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleFormCancel"
    >
      <UserForm
        v-if="dialogVisible"
        :user="currentUser"
        :mode="dialogMode"
        @submit="handleFormSubmit"
        @cancel="handleFormCancel"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance } from "element-plus";
import { Plus, ArrowDown } from "@element-plus/icons-vue";
import PermissionGuard from "@/components/PermissionGuard.vue";
import UserForm from "@/components/UserForm.vue";
import { userApi, type UserListParams } from "@/api/user";
import { UserRole, UserStatus, type User } from "@/types/auth";

// 响应式数据
const loading = ref(false);
const tableData = ref<User[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const error = ref<string>("");
const retryCount = ref(0);
const maxRetries = 3;

// 搜索表单
const searchForm = reactive({
  username: "",
  role: "",
  status: "",
});

// 对话框状态
const dialogVisible = ref(false);
const dialogMode = ref<"create" | "edit" | "view">("create");
const currentUser = ref<Partial<User>>({});

// 角色选项
const roleOptions = [
  { label: "管理员", value: UserRole.ADMIN },
  { label: "普通用户", value: UserRole.USER },
];

// 状态选项
const statusOptions = [
  { label: "启用", value: UserStatus.ACTIVE },
  { label: "禁用", value: UserStatus.INACTIVE },
  { label: "封禁", value: UserStatus.BANNED },
];

// 获取角色标签类型
const getRoleTagType = (role: UserRole) => {
  switch (role) {
    case UserRole.ADMIN:
      return "danger";
    case UserRole.USER:
      return "primary";
    default:
      return "info";
  }
};

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case UserStatus.ACTIVE:
      return "success";
    case UserStatus.INACTIVE:
      return "warning";
    case UserStatus.BANNED:
      return "danger";
    default:
      return "info";
  }
};

// 获取状态标签文本
const getStatusText = (status: string) => {
  switch (status) {
    case UserStatus.ACTIVE:
      return "启用";
    case UserStatus.INACTIVE:
      return "禁用";
    case UserStatus.BANNED:
      return "封禁";
    default:
      return status;
  }
};

// 获取用户列表 - 增强版本
const fetchUsers = async (showLoading = true) => {
  try {
    if (showLoading) {
      loading.value = true;
    }
    error.value = "";

    const params: UserListParams = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm,
    };

    const response = await userApi.getUsers(params);
    if (response.success) {
      tableData.value = response.data.list || [];
      total.value = response.data.total || 0;
      retryCount.value = 0; // 重置重试计数

      // 如果没有数据且不是第一页，自动跳转到第一页
      if (tableData.value.length === 0 && currentPage.value > 1) {
        currentPage.value = 1;
        await fetchUsers(false);
        return;
      }
    } else {
      error.value = response.message || "获取用户列表失败";
      ElMessage.error(error.value);
    }
  } catch (err: any) {
    console.error("获取用户列表失败:", err);
    error.value = err.message || "网络错误，请检查网络连接";

    // 自动重试机制
    if (retryCount.value < maxRetries) {
      retryCount.value++;
      ElMessage.warning(
        `获取数据失败，正在重试 (${retryCount.value}/${maxRetries})`,
      );
      setTimeout(() => {
        fetchUsers(false);
      }, 2000 * retryCount.value); // 递增延迟
    } else {
      ElMessage.error("多次尝试失败，请检查网络连接或联系管理员");
    }
  } finally {
    if (showLoading) {
      loading.value = false;
    }
  }
};

// 手动重试
const handleRetry = () => {
  retryCount.value = 0;
  fetchUsers();
};

// 刷新数据
const handleRefresh = () => {
  retryCount.value = 0;
  error.value = "";
  fetchUsers();
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchUsers();
};

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    username: "",
    role: "",
    status: "",
  });
  currentPage.value = 1;
  fetchUsers();
};

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchUsers();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchUsers();
};

// 新增用户
const handleAdd = () => {
  dialogMode.value = "create";
  currentUser.value = {};
  dialogVisible.value = true;
};

// 查看用户
const handleView = (user: User) => {
  dialogMode.value = "view";
  currentUser.value = { ...user };
  dialogVisible.value = true;
};

// 编辑用户
const handleEdit = (user: User) => {
  dialogMode.value = "edit";
  currentUser.value = { ...user };
  dialogVisible.value = true;
};

// 删除用户
const handleDelete = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复。`,
      "确认删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await userApi.deleteUser(user.id!);
    if (response.success) {
      ElMessage.success("删除成功");
      fetchUsers();
    } else {
      ElMessage.error(response.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除用户失败:", error);
      ElMessage.error("删除用户失败");
    }
  }
};

// 重置密码
const handleResetPassword = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？`,
      "确认重置密码",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await userApi.resetPassword(user.id!);
    if (response.success) {
      ElMessage.success(`密码重置成功，新密码：${response.data.newPassword}`);
    } else {
      ElMessage.error(response.message || "重置密码失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("重置密码失败:", error);
      ElMessage.error("重置密码失败");
    }
  }
};

// 切换用户状态
const handleToggleStatus = async (user: User) => {
  const newStatus =
    user.status === UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE;
  const statusText = newStatus === UserStatus.ACTIVE ? "启用" : "禁用";

  try {
    await ElMessageBox.confirm(
      `确定要${statusText}用户 "${user.username}" 吗？`,
      `确认${statusText}`,
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await userApi.updateUserStatus(user.id!, newStatus);
    if (response.success) {
      ElMessage.success(`${statusText}成功`);
      fetchUsers();
    } else {
      ElMessage.error(response.message || `${statusText}失败`);
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error(`${statusText}用户失败:`, error);
      ElMessage.error(`${statusText}用户失败`);
    }
  }
};

// 更多操作
const handleMoreAction = (command: string, user: User) => {
  switch (command) {
    case "resetPassword":
      handleResetPassword(user);
      break;
    case "enable":
    case "disable":
      handleToggleStatus(user);
      break;
    case "delete":
      handleDelete(user);
      break;
  }
};

// 表单提交
const handleFormSubmit = async (userData: Partial<User>) => {
  try {
    let response;
    if (dialogMode.value === "create") {
      response = await userApi.createUser(userData);
    } else {
      response = await userApi.updateUser(currentUser.value.id!, userData);
    }

    if (response.success) {
      ElMessage.success(
        dialogMode.value === "create" ? "创建成功" : "更新成功",
      );
      dialogVisible.value = false;
      fetchUsers();
    } else {
      ElMessage.error(response.message || "操作失败");
    }
  } catch (error) {
    console.error("表单提交失败:", error);
    ElMessage.error("操作失败");
  }
};

// 取消表单
const handleFormCancel = () => {
  dialogVisible.value = false;
};

// 对话框标题
const dialogTitle = computed(() => {
  switch (dialogMode.value) {
    case "create":
      return "新增用户";
    case "edit":
      return "编辑用户";
    case "view":
      return "查看用户";
    default:
      return "用户信息";
  }
});

// 组件挂载时获取数据
onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.search-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-container {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.error-container {
  padding: 20px;
}

.error-actions {
  margin-top: 10px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.toolbar-left .total-count {
  color: #606266;
  font-size: 14px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
