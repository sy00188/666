<template>
  <div class="role-management">
    <PermissionGuard permission="role:view">
      <!-- 页面头部 -->
      <div class="page-header">
        <h2>角色管理</h2>
        <div class="header-actions">
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleAdd"
            v-permission="'role:create'"
          >
            新增角色
          </el-button>
          <el-button
            :icon="Download"
            @click="handleExport"
            v-permission="'role:export'"
          >
            导出
          </el-button>
          <el-button
            :icon="Upload"
            @click="handleImport"
            v-permission="'role:import'"
          >
            导入
          </el-button>
        </div>
      </div>

      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入角色名称或编码"
              clearable
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="类型">
            <el-select
              v-model="searchForm.type"
              placeholder="请选择类型"
              clearable
            >
              <el-option label="系统角色" value="true" />
              <el-option label="自定义角色" value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 批量操作栏 -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <span class="selected-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button
          size="small"
          @click="handleBatchEnable"
          v-permission="'role:edit'"
        >
          批量启用
        </el-button>
        <el-button
          size="small"
          @click="handleBatchDisable"
          v-permission="'role:edit'"
        >
          批量禁用
        </el-button>
        <el-button
          size="small"
          type="danger"
          @click="handleBatchDelete"
          v-permission="'role:delete'"
        >
          批量删除
        </el-button>
      </div>

      <!-- 数据表格 -->
      <div class="table-container">
        <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="角色名称" width="150" />
          <el-table-column prop="code" label="角色代码" width="150" />
          <el-table-column
            prop="description"
            label="描述"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                {{ row.status === "active" ? "启用" : "禁用" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isSystem" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isSystem ? 'warning' : 'primary'">
                {{ row.isSystem ? "系统" : "自定义" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="userCount" label="用户数" width="80" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                @click="handleView(row)"
                v-permission="'role:view'"
              >
                查看
              </el-button>
              <el-button
                size="small"
                type="primary"
                @click="handleEdit(row)"
                v-permission="'role:edit'"
                :disabled="row.isSystem"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="warning"
                @click="handleAssignPermissions(row)"
                v-permission="'role:assign_permission'"
              >
                权限
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
                      command="users"
                      v-permission-show="'role:view'"
                    >
                      查看用户
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="copy"
                      v-permission-show="'role:create'"
                    >
                      复制角色
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="row.status === 'active' ? 'disable' : 'enable'"
                      v-permission-show="'role:edit'"
                      :disabled="row.isSystem"
                    >
                      {{ row.status === "active" ? "禁用" : "启用" }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      command="delete"
                      divided
                      v-permission-show="'role:delete'"
                      :disabled="row.isSystem || row.userCount > 0"
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

      <!-- 权限不足提示 -->
      <template #fallback>
        <el-result
          icon="warning"
          title="权限不足"
          sub-title="您没有权限访问角色管理功能"
        >
          <template #extra>
            <el-button @click="$router.go(-1)">返回</el-button>
          </template>
        </el-result>
      </template>
    </PermissionGuard>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <RoleForm
        v-if="dialogVisible"
        :role="currentRole"
        :mode="dialogMode"
        @submit="handleSubmit"
        @cancel="handleDialogClose"
      />
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限分配"
      width="800px"
      @close="handlePermissionDialogClose"
    >
      <PermissionAssign
        v-if="permissionDialogVisible"
        :role="currentRole"
        @submit="handlePermissionSubmit"
        @cancel="handlePermissionDialogClose"
      />
    </el-dialog>

    <!-- 角色用户对话框 -->
    <el-dialog
      v-model="usersDialogVisible"
      :title="`${currentRole?.name} - 关联用户`"
      width="800px"
      @close="handleUsersDialogClose"
    >
      <RoleUsers
        v-if="usersDialogVisible"
        :role="currentRole"
        @close="handleUsersDialogClose"
      />
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入角色"
      width="500px"
      @close="handleImportDialogClose"
    >
      <ImportRole
        v-if="importDialogVisible"
        @success="handleImportSuccess"
        @cancel="handleImportDialogClose"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import {
  Plus,
  Search,
  Refresh,
  Download,
  Upload,
  Edit,
  Delete,
  View,
  Setting,
  More,
} from "@element-plus/icons-vue";
import PermissionGuard from "@/components/PermissionGuard.vue";
import RoleForm from "@/components/RoleForm.vue";
import PermissionAssign from "@/components/PermissionAssign.vue";
import RoleUsers from "@/components/RoleUsers.vue";
import ImportRole from "@/components/ImportRole.vue";
import {
  getRoleList,
  deleteRole,
  changeRoleStatus,
  batchOperateRoles,
  exportRoles,
} from "@/api/modules/role";
import type {
  Role,
  RoleQueryParams,
  BatchRoleOperation,
  RoleStatus,
  RolePermissionAssignForm,
  RoleExportParams,
} from "@/types/role";

// 响应式数据
const loading = ref(false);
const tableData = ref<Role[]>([]);
const selectedRows = ref<Role[]>([]);

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
});

// 搜索表单
const searchForm = reactive<RoleQueryParams>({
  keyword: "",
  status: undefined,
  type: undefined,
});

// 状态选项
const statusOptions = [
  { label: "启用", value: "active" },
  { label: "禁用", value: "inactive" },
];

// 对话框状态
const dialogVisible = ref(false);
const dialogMode = ref<"create" | "edit" | "view">("create");
const currentRole = ref<Partial<Role>>({});

const permissionDialogVisible = ref(false);
const usersDialogVisible = ref(false);
const importDialogVisible = ref(false);

// 计算属性
const dialogTitle = computed(() => {
  const titles = {
    create: "新增角色",
    edit: "编辑角色",
    view: "查看角色",
  };
  return titles[dialogMode.value];
});

// 获取角色列表
const fetchRoles = async () => {
  try {
    loading.value = true;
    const params: RoleQueryParams = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm,
    };

    const response = await getRoleList(params);
    if (response.success) {
      tableData.value = response.data.list;
      pagination.total = response.data.total;
    } else {
      ElMessage.error(response.message || "获取角色列表失败");
    }
  } catch (error) {
    console.error("获取角色列表失败:", error);
    ElMessage.error("获取角色列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchRoles();
};

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: "",
    status: undefined,
    type: undefined,
  });
  pagination.page = 1;
  fetchRoles();
};

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  fetchRoles();
};

const handleCurrentChange = (page: number) => {
  pagination.page = page;
  fetchRoles();
};

// 表格选择处理
const handleSelectionChange = (selection: Role[]) => {
  selectedRows.value = selection;
};

// 新增角色
const handleAdd = () => {
  dialogMode.value = "create";
  currentRole.value = {};
  dialogVisible.value = true;
};

// 查看角色
const handleView = (role: Role) => {
  dialogMode.value = "view";
  currentRole.value = { ...role };
  dialogVisible.value = true;
};

// 编辑角色
const handleEdit = (role: Role) => {
  if (role.isSystem) {
    ElMessage.warning("系统角色不允许编辑");
    return;
  }
  dialogMode.value = "edit";
  currentRole.value = { ...role };
  dialogVisible.value = true;
};

// 权限分配
const handleAssignPermissions = (role: Role) => {
  currentRole.value = { ...role };
  permissionDialogVisible.value = true;
};

// 更多操作
const handleMoreAction = async (command: string, role: Role) => {
  switch (command) {
    case "users":
      currentRole.value = { ...role };
      usersDialogVisible.value = true;
      break;
    case "copy":
      handleCopyRole(role);
      break;
    case "enable":
    case "disable":
      await handleStatusChange(
        role,
        command === "enable" ? "active" : "inactive",
      );
      break;
    case "delete":
      await handleDelete(role);
      break;
  }
};

// 复制角色
const handleCopyRole = (role: Role) => {
  dialogMode.value = "create";
  currentRole.value = {
    name: `${role.name}_副本`,
    code: `${role.code}_copy`,
    description: role.description,
    status: role.status,
    sort: role.sort,
    permissionIds: role.permissionIds ? [...role.permissionIds] : [],
  };
  dialogVisible.value = true;
};

// 状态变更
const handleStatusChange = async (
  role: Role,
  status: "active" | "inactive",
) => {
  if (role.isSystem) {
    ElMessage.warning("系统角色不允许修改状态");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要${status === "active" ? "启用" : "禁用"}角色"${role.name}"吗？`,
      "确认操作",
      { type: "warning" },
    );

    const response = await changeRoleStatus(role.id, status);
    if (response.success) {
      ElMessage.success(`${status === "active" ? "启用" : "禁用"}成功`);
      fetchRoles();
    } else {
      ElMessage.error(response.message || "操作失败");
    }
  } catch (error) {
    // 用户取消操作
  }
};

// 删除角色
const handleDelete = async (role: Role) => {
  if (role.isSystem) {
    ElMessage.warning("系统角色不允许删除");
    return;
  }

  if (role.userCount > 0) {
    ElMessage.warning("该角色下还有用户，不允许删除");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${role.name}"吗？此操作不可恢复！`,
      "确认删除",
      { type: "warning" },
    );

    const response = await deleteRole(role.id);
    if (response.success) {
      ElMessage.success("删除成功");
      fetchRoles();
    } else {
      ElMessage.error(response.message || "删除失败");
    }
  } catch (error) {
    // 用户取消操作
  }
};

// 批量操作
const handleBatchEnable = () => {
  handleBatchOperation("enable");
};

const handleBatchDisable = () => {
  handleBatchOperation("disable");
};

const handleBatchDelete = () => {
  handleBatchOperation("delete");
};

const handleBatchOperation = async (
  operation: "enable" | "disable" | "delete",
) => {
  if (!selectedRows.value?.length) {
    ElMessage.warning("请选择要操作的角色");
    return;
  }

  // 检查系统角色
  const systemRoles = selectedRows.value.filter((role) => role.isSystem);
  if (systemRoles.length > 0) {
    ElMessage.warning("选择的角色中包含系统角色，无法进行批量操作");
    return;
  }

  // 检查有用户的角色（仅删除操作）
  if (operation === "delete") {
    const rolesWithUsers = selectedRows.value.filter(
      (role) => role.userCount > 0,
    );
    if (rolesWithUsers.length > 0) {
      ElMessage.warning("选择的角色中包含有用户的角色，无法删除");
      return;
    }
  }

  const operationText = {
    enable: "启用",
    disable: "禁用",
    delete: "删除",
  };

  try {
    await ElMessageBox.confirm(
      `确定要${operationText[operation]}选中的 ${selectedRows.value.length} 个角色吗？`,
      "确认操作",
      { type: "warning" },
    );

    const batchData: BatchRoleOperation = {
      operation,
      roleIds: selectedRows.value.map((role) => role.id),
      ids: selectedRows.value.map((role) => role.id),
    };

    const response = await batchOperateRoles(batchData);
    if (response.success) {
      ElMessage.success(`批量${operationText[operation]}成功`);
      selectedRows.value = [];
      fetchRoles();
    } else {
      ElMessage.error(response.message || "批量操作失败");
    }
  } catch (error) {
    // 用户取消操作
  }
};

// 导出
const handleExport = async () => {
  try {
    const exportParams: RoleExportParams = {
      ids: selectedRows.value.map((role) => role.id) || [],
      format: "excel",
    };
    const response = await exportRoles(exportParams);
    const blob = new Blob([response], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `角色列表_${new Date().toISOString().slice(0, 10)}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    ElMessage.success("导出成功");
  } catch (error) {
    console.error("导出失败:", error);
    ElMessage.error("导出失败");
  }
};

// 导入
const handleImport = () => {
  importDialogVisible.value = true;
};

// 对话框处理
const handleDialogClose = () => {
  dialogVisible.value = false;
  currentRole.value = {};
};

const handlePermissionDialogClose = () => {
  permissionDialogVisible.value = false;
  currentRole.value = {};
};

const handleUsersDialogClose = () => {
  usersDialogVisible.value = false;
  currentRole.value = {};
};

const handleImportDialogClose = () => {
  importDialogVisible.value = false;
};

// 表单提交
const handleSubmit = () => {
  dialogVisible.value = false;
  fetchRoles();
};

const handlePermissionSubmit = () => {
  permissionDialogVisible.value = false;
  fetchRoles();
};

const handleImportSuccess = () => {
  importDialogVisible.value = false;
  fetchRoles();
};

// 初始化
onMounted(() => {
  fetchRoles();
});
</script>

<style scoped>
.role-management {
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
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  margin-bottom: 10px;
}

.selected-info {
  color: #409eff;
  font-weight: 500;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
