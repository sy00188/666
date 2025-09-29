<template>
  <div class="system-role">
    <div class="page-header">
      <h2>角色管理</h2>
      <p>系统角色权限配置管理</p>
    </div>

    <div class="role-content">
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增角色
        </el-button>
        <el-button
          @click="handleBatchDelete"
          :disabled="selectedRoles.length === 0"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索角色名称或描述"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 角色表格 -->
      <el-table
        :data="filteredRoles"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="角色名称" width="150">
          <template #default="{ row }">
            <div class="role-name">
              <el-tag :type="getRoleTagType(row.level)" size="small">
                {{ row.name }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="角色编码" width="120" />
        <el-table-column prop="description" label="角色描述" min-width="200" />
        <el-table-column
          prop="userCount"
          label="用户数量"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-link type="primary" @click="viewRoleUsers(row)">
              {{ row.userCount }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="permissionCount"
          label="权限数量"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-link type="success" @click="viewRolePermissions(row)">
              {{ row.permissionCount }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handlePermission(row)"
            >
              权限
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :disabled="row.level === 'system'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入角色编码"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="角色级别" prop="level">
          <el-select v-model="formData.level" placeholder="请选择角色级别">
            <el-option label="系统级" value="system" />
            <el-option label="管理级" value="admin" />
            <el-option label="普通级" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="800px">
      <div class="permission-config">
        <div class="permission-header">
          <span>为角色 "{{ currentRole?.name }}" 配置权限</span>
          <div class="permission-actions">
            <el-button size="small" @click="expandAll">全部展开</el-button>
            <el-button size="small" @click="collapseAll">全部收起</el-button>
            <el-button size="small" @click="checkAll">全选</el-button>
            <el-button size="small" @click="uncheckAll">取消全选</el-button>
          </div>
        </div>

        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermissions"
          @check="handlePermissionCheck"
        >
          <template #default="{ node, data }">
            <div class="permission-node">
              <el-icon v-if="data.icon">
                <component :is="data.icon" />
              </el-icon>
              <span>{{ data.name }}</span>
              <el-tag
                v-if="data.type"
                size="small"
                :type="getPermissionTagType(data.type)"
              >
                {{ data.type }}
              </el-tag>
            </div>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handlePermissionSubmit"
            :loading="permissionSubmitting"
          >
            保存权限
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Delete,
  Search,
  User,
  Setting,
  Document,
} from "@element-plus/icons-vue";

const loading = ref(false);
const submitting = ref(false);
const permissionSubmitting = ref(false);
const searchKeyword = ref("");
const selectedRoles = ref<any[]>([]);
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);

// 对话框相关
const dialogVisible = ref(false);
const permissionDialogVisible = ref(false);
const isEdit = ref(false);
const currentRole = ref<any>(null);

// 表单相关
const formRef = ref();
const permissionTreeRef = ref();
const formData = ref({
  name: "",
  code: "",
  level: "normal",
  description: "",
  status: 1,
});

const formRules = {
  name: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
  code: [
    { required: true, message: "请输入角色编码", trigger: "blur" },
    {
      pattern: /^[A-Z_]+$/,
      message: "角色编码只能包含大写字母和下划线",
      trigger: "blur",
    },
  ],
  level: [{ required: true, message: "请选择角色级别", trigger: "change" }],
};

// 模拟数据
const roles = ref([
  {
    id: 1,
    name: "超级管理员",
    code: "SUPER_ADMIN",
    level: "system",
    description: "系统超级管理员，拥有所有权限",
    userCount: 2,
    permissionCount: 45,
    status: 1,
    createTime: "2024-01-01 10:00:00",
  },
  {
    id: 2,
    name: "系统管理员",
    code: "SYSTEM_ADMIN",
    level: "admin",
    description: "系统管理员，负责系统配置和用户管理",
    userCount: 5,
    permissionCount: 32,
    status: 1,
    createTime: "2024-01-02 10:00:00",
  },
  {
    id: 3,
    name: "档案管理员",
    code: "ARCHIVE_ADMIN",
    level: "admin",
    description: "档案管理员，负责档案的管理和维护",
    userCount: 8,
    permissionCount: 25,
    status: 1,
    createTime: "2024-01-03 10:00:00",
  },
  {
    id: 4,
    name: "普通用户",
    code: "NORMAL_USER",
    level: "normal",
    description: "普通用户，可以查看和借阅档案",
    userCount: 156,
    permissionCount: 12,
    status: 1,
    createTime: "2024-01-04 10:00:00",
  },
  {
    id: 5,
    name: "访客用户",
    code: "GUEST_USER",
    level: "normal",
    description: "访客用户，只能查看公开档案",
    userCount: 23,
    permissionCount: 5,
    status: 0,
    createTime: "2024-01-05 10:00:00",
  },
]);

const permissionTree = ref([
  {
    id: 1,
    name: "系统管理",
    icon: "Setting",
    type: "module",
    children: [
      {
        id: 11,
        name: "用户管理",
        type: "menu",
        children: [
          { id: 111, name: "查看用户", type: "action" },
          { id: 112, name: "新增用户", type: "action" },
          { id: 113, name: "编辑用户", type: "action" },
          { id: 114, name: "删除用户", type: "action" },
        ],
      },
      {
        id: 12,
        name: "角色管理",
        type: "menu",
        children: [
          { id: 121, name: "查看角色", type: "action" },
          { id: 122, name: "新增角色", type: "action" },
          { id: 123, name: "编辑角色", type: "action" },
          { id: 124, name: "删除角色", type: "action" },
        ],
      },
      {
        id: 13,
        name: "权限管理",
        type: "menu",
        children: [
          { id: 131, name: "查看权限", type: "action" },
          { id: 132, name: "配置权限", type: "action" },
        ],
      },
    ],
  },
  {
    id: 2,
    name: "档案管理",
    icon: "Document",
    type: "module",
    children: [
      {
        id: 21,
        name: "档案列表",
        type: "menu",
        children: [
          { id: 211, name: "查看档案", type: "action" },
          { id: 212, name: "新增档案", type: "action" },
          { id: 213, name: "编辑档案", type: "action" },
          { id: 214, name: "删除档案", type: "action" },
        ],
      },
      {
        id: 22,
        name: "档案分类",
        type: "menu",
        children: [
          { id: 221, name: "查看分类", type: "action" },
          { id: 222, name: "管理分类", type: "action" },
        ],
      },
    ],
  },
]);

const checkedPermissions = ref([111, 112, 211, 212]);
const treeProps = {
  children: "children",
  label: "name",
};

// 计算属性
const filteredRoles = computed(() => {
  if (!searchKeyword.value) return roles.value;
  return roles.value.filter(
    (role) =>
      role.name.includes(searchKeyword.value) ||
      role.description.includes(searchKeyword.value),
  );
});

const dialogTitle = computed(() => {
  return isEdit.value ? "编辑角色" : "新增角色";
});

// 方法
const getRoleTagType = (level: string) => {
  switch (level) {
    case "system":
      return "danger";
    case "admin":
      return "warning";
    case "normal":
      return "primary";
    default:
      return "info";
  }
};

const getPermissionTagType = (type: string) => {
  switch (type) {
    case "module":
      return "primary";
    case "menu":
      return "success";
    case "action":
      return "info";
    default:
      return "info";
  }
};

const handleSearch = () => {
  // 搜索逻辑
};

const handleSelectionChange = (selection: any[]) => {
  selectedRoles.value = selection;
};

const handleAdd = () => {
  isEdit.value = false;
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  currentRole.value = row;
  formData.value = { ...row };
  dialogVisible.value = true;
};

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${row.name}" 吗？`,
      "删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    // 删除逻辑
    ElMessage.success("删除成功");
  } catch {
    // 用户取消
  }
};

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRoles.value.length} 个角色吗？`,
      "批量删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    // 批量删除逻辑
    ElMessage.success("批量删除成功");
    selectedRoles.value = [];
  } catch {
    // 用户取消
  }
};

const handleStatusChange = (row: any) => {
  ElMessage.success(`角色状态已${row.status ? "启用" : "禁用"}`);
};

const handlePermission = (row: any) => {
  currentRole.value = row;
  permissionDialogVisible.value = true;
  // 加载角色权限
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    // 提交逻辑
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success(isEdit.value ? "编辑成功" : "新增成功");
    dialogVisible.value = false;
    resetForm();
  } catch (error) {
    console.error("提交失败:", error);
  } finally {
    submitting.value = false;
  }
};

const resetForm = () => {
  formData.value = {
    name: "",
    code: "",
    level: "normal",
    description: "",
    status: 1,
  };
  formRef.value?.resetFields();
};

const viewRoleUsers = (row: any) => {
  ElMessage.info(`查看角色 "${row.name}" 的用户列表`);
};

const viewRolePermissions = (row: any) => {
  ElMessage.info(`查看角色 "${row.name}" 的权限列表`);
};

const expandAll = () => {
  // 展开所有节点
};

const collapseAll = () => {
  // 收起所有节点
};

const checkAll = () => {
  // 全选所有权限
};

const uncheckAll = () => {
  // 取消全选
};

const handlePermissionCheck = (data: any, checked: any) => {
  // 权限选择逻辑
};

const handlePermissionSubmit = async () => {
  try {
    permissionSubmitting.value = true;

    // 保存权限配置
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success("权限配置保存成功");
    permissionDialogVisible.value = false;
  } catch (error) {
    console.error("保存权限失败:", error);
  } finally {
    permissionSubmitting.value = false;
  }
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  // 重新加载数据
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  // 重新加载数据
};

onMounted(() => {
  total.value = roles.value.length;
});
</script>

<style lang="scss" scoped>
.system-role {
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

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  .search-box {
    width: 300px;
  }
}

.role-name {
  display: flex;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-lg;
}

.permission-config {
  .permission-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-lg;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-light;

    .permission-actions {
      display: flex;
      gap: $spacing-xs;
    }
  }
}

.permission-node {
  display: flex;
  align-items: center;
  gap: $spacing-xs;

  .el-icon {
    color: $color-primary;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
