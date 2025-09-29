<template>
  <div class="system-permission">
    <div class="page-header">
      <h2>权限管理</h2>
      <p>系统权限资源配置管理</p>
    </div>

    <div class="permission-content">
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增权限
        </el-button>
        <el-button
          @click="handleBatchDelete"
          :disabled="selectedPermissions.length === 0"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button @click="handleSync">
          <el-icon><Refresh /></el-icon>
          同步权限
        </el-button>
        <div class="filter-box">
          <el-select
            v-model="filterType"
            placeholder="权限类型"
            clearable
            style="width: 120px"
          >
            <el-option label="模块" value="module" />
            <el-option label="菜单" value="menu" />
            <el-option label="操作" value="action" />
            <el-option label="API" value="api" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索权限名称或编码"
            clearable
            style="width: 250px; margin-left: 10px"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 权限树表格 -->
      <el-table
        :data="filteredPermissions"
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="权限名称" min-width="200">
          <template #default="{ row }">
            <div class="permission-name">
              <el-icon v-if="row.icon" class="permission-icon">
                <component :is="row.icon" />
              </el-icon>
              <span>{{ row.name }}</span>
              <el-tag
                :type="getPermissionTagType(row.type)"
                size="small"
                style="margin-left: 8px"
              >
                {{ getPermissionTypeLabel(row.type) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="权限编码" width="180" />
        <el-table-column prop="path" label="路径/资源" width="200" />
        <el-table-column
          prop="method"
          label="请求方式"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.method"
              :type="getMethodTagType(row.method)"
              size="small"
            >
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
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
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handleAddChild(row)"
              v-if="row.type !== 'action'"
            >
              添加子权限
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑权限对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入权限名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限编码" prop="code">
              <el-input
                v-model="formData.code"
                placeholder="请输入权限编码"
                :disabled="isEdit"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限类型" prop="type">
              <el-select
                v-model="formData.type"
                placeholder="请选择权限类型"
                @change="handleTypeChange"
              >
                <el-option label="模块" value="module" />
                <el-option label="菜单" value="menu" />
                <el-option label="操作" value="action" />
                <el-option label="API" value="api" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父级权限" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="permissionTreeOptions"
                :props="{ value: 'id', label: 'name', children: 'children' }"
                placeholder="请选择父级权限"
                clearable
                check-strictly
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="路径/资源" prop="path">
              <el-input
                v-model="formData.path"
                placeholder="请输入路径或资源标识"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="请求方式"
              prop="method"
              v-if="formData.type === 'api'"
            >
              <el-select v-model="formData.method" placeholder="请选择请求方式">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
                <el-option label="PATCH" value="PATCH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="formData.icon" placeholder="请输入图标名称">
                <template #append>
                  <el-button @click="showIconSelector">选择</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="formData.sort" :min="0" :max="9999" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权限描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
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

    <!-- 图标选择器对话框 -->
    <el-dialog v-model="iconSelectorVisible" title="选择图标" width="800px">
      <div class="icon-selector">
        <div class="icon-search">
          <el-input
            v-model="iconSearchKeyword"
            placeholder="搜索图标"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="icon-grid">
          <div
            v-for="icon in filteredIcons"
            :key="icon"
            class="icon-item"
            :class="{ active: selectedIcon === icon }"
            @click="selectIcon(icon)"
          >
            <el-icon><component :is="icon" /></el-icon>
            <span>{{ icon }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="iconSelectorVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmIconSelection"
            >确定</el-button
          >
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
  Refresh,
  Setting,
  Document,
  User,
  Menu,
  Operation,
  DataAnalysis,
  Lock,
  Key,
  View,
  Edit,
  Delete as DeleteIcon,
  Upload,
  Download,
} from "@element-plus/icons-vue";

const loading = ref(false);
const submitting = ref(false);
const searchKeyword = ref("");
const filterType = ref("");
const selectedPermissions = ref<any[]>([]);

// 对话框相关
const dialogVisible = ref(false);
const iconSelectorVisible = ref(false);
const isEdit = ref(false);
const isAddChild = ref(false);
const parentPermission = ref<any>(null);

// 表单相关
const formRef = ref();
const formData = ref({
  name: "",
  code: "",
  type: "menu",
  parentId: null,
  path: "",
  method: "",
  icon: "",
  sort: 0,
  description: "",
  status: 1,
});

const formRules = {
  name: [{ required: true, message: "请输入权限名称", trigger: "blur" }],
  code: [
    { required: true, message: "请输入权限编码", trigger: "blur" },
    {
      pattern: /^[A-Z_:]+$/,
      message: "权限编码只能包含大写字母、下划线和冒号",
      trigger: "blur",
    },
  ],
  type: [{ required: true, message: "请选择权限类型", trigger: "change" }],
};

// 图标选择器
const iconSearchKeyword = ref("");
const selectedIcon = ref("");
const availableIcons = [
  "Setting",
  "Document",
  "User",
  "Menu",
  "Operation",
  "DataAnalysis",
  "Lock",
  "Key",
  "View",
  "Edit",
  "Delete",
  "Upload",
  "Download",
  "Plus",
  "Search",
  "Refresh",
];

// 模拟权限数据
const permissions = ref([
  {
    id: 1,
    name: "系统管理",
    code: "SYSTEM",
    type: "module",
    path: "/system",
    method: "",
    icon: "Setting",
    sort: 1,
    status: 1,
    description: "系统管理模块",
    children: [
      {
        id: 11,
        name: "用户管理",
        code: "SYSTEM:USER",
        type: "menu",
        path: "/system/user",
        method: "",
        icon: "User",
        sort: 1,
        status: 1,
        description: "用户管理菜单",
        children: [
          {
            id: 111,
            name: "查看用户",
            code: "SYSTEM:USER:VIEW",
            type: "action",
            path: "",
            method: "",
            icon: "View",
            sort: 1,
            status: 1,
            description: "查看用户列表",
          },
          {
            id: 112,
            name: "新增用户",
            code: "SYSTEM:USER:ADD",
            type: "action",
            path: "",
            method: "",
            icon: "Plus",
            sort: 2,
            status: 1,
            description: "新增用户",
          },
          {
            id: 113,
            name: "编辑用户",
            code: "SYSTEM:USER:EDIT",
            type: "action",
            path: "",
            method: "",
            icon: "Edit",
            sort: 3,
            status: 1,
            description: "编辑用户信息",
          },
          {
            id: 114,
            name: "删除用户",
            code: "SYSTEM:USER:DELETE",
            type: "action",
            path: "",
            method: "",
            icon: "Delete",
            sort: 4,
            status: 1,
            description: "删除用户",
          },
        ],
      },
      {
        id: 12,
        name: "角色管理",
        code: "SYSTEM:ROLE",
        type: "menu",
        path: "/system/role",
        method: "",
        icon: "Lock",
        sort: 2,
        status: 1,
        description: "角色管理菜单",
        children: [
          {
            id: 121,
            name: "查看角色",
            code: "SYSTEM:ROLE:VIEW",
            type: "action",
            path: "",
            method: "",
            icon: "View",
            sort: 1,
            status: 1,
            description: "查看角色列表",
          },
          {
            id: 122,
            name: "新增角色",
            code: "SYSTEM:ROLE:ADD",
            type: "action",
            path: "",
            method: "",
            icon: "Plus",
            sort: 2,
            status: 1,
            description: "新增角色",
          },
          {
            id: 123,
            name: "编辑角色",
            code: "SYSTEM:ROLE:EDIT",
            type: "action",
            path: "",
            method: "",
            icon: "Edit",
            sort: 3,
            status: 1,
            description: "编辑角色信息",
          },
          {
            id: 124,
            name: "删除角色",
            code: "SYSTEM:ROLE:DELETE",
            type: "action",
            path: "",
            method: "",
            icon: "Delete",
            sort: 4,
            status: 1,
            description: "删除角色",
          },
        ],
      },
    ],
  },
  {
    id: 2,
    name: "档案管理",
    code: "ARCHIVE",
    type: "module",
    path: "/archive",
    method: "",
    icon: "Document",
    sort: 2,
    status: 1,
    description: "档案管理模块",
    children: [
      {
        id: 21,
        name: "档案列表",
        code: "ARCHIVE:LIST",
        type: "menu",
        path: "/archive/list",
        method: "",
        icon: "Document",
        sort: 1,
        status: 1,
        description: "档案列表菜单",
        children: [
          {
            id: 211,
            name: "查看档案",
            code: "ARCHIVE:LIST:VIEW",
            type: "action",
            path: "",
            method: "",
            icon: "View",
            sort: 1,
            status: 1,
            description: "查看档案列表",
          },
          {
            id: 212,
            name: "新增档案",
            code: "ARCHIVE:LIST:ADD",
            type: "action",
            path: "",
            method: "",
            icon: "Plus",
            sort: 2,
            status: 1,
            description: "新增档案",
          },
        ],
      },
    ],
  },
  {
    id: 3,
    name: "API权限",
    code: "API",
    type: "module",
    path: "/api",
    method: "",
    icon: "Operation",
    sort: 3,
    status: 1,
    description: "API接口权限",
    children: [
      {
        id: 31,
        name: "获取用户列表",
        code: "API:USER:LIST",
        type: "api",
        path: "/api/users",
        method: "GET",
        icon: "",
        sort: 1,
        status: 1,
        description: "获取用户列表API",
      },
      {
        id: 32,
        name: "创建用户",
        code: "API:USER:CREATE",
        type: "api",
        path: "/api/users",
        method: "POST",
        icon: "",
        sort: 2,
        status: 1,
        description: "创建用户API",
      },
    ],
  },
]);

// 计算属性
const filteredPermissions = computed(() => {
  let result = permissions.value;

  if (filterType.value) {
    result = filterPermissionsByType(result, filterType.value);
  }

  if (searchKeyword.value) {
    result = filterPermissionsByKeyword(result, searchKeyword.value);
  }

  return result;
});

const permissionTreeOptions = computed(() => {
  return buildTreeOptions(permissions.value);
});

const filteredIcons = computed(() => {
  if (!iconSearchKeyword.value) return availableIcons;
  return availableIcons.filter((icon) =>
    icon.toLowerCase().includes(iconSearchKeyword.value.toLowerCase()),
  );
});

const dialogTitle = computed(() => {
  if (isAddChild.value)
    return `为 "${parentPermission.value?.name}" 添加子权限`;
  return isEdit.value ? "编辑权限" : "新增权限";
});

// 方法
const filterPermissionsByType = (data: any[], type: string): any[] => {
  const result: any[] = [];

  for (const item of data) {
    if (item.type === type) {
      result.push(item);
    } else if (item.children) {
      const filteredChildren = filterPermissionsByType(item.children, type);
      if (filteredChildren.length > 0) {
        result.push({
          ...item,
          children: filteredChildren,
        });
      }
    }
  }

  return result;
};

const filterPermissionsByKeyword = (data: any[], keyword: string): any[] => {
  const result: any[] = [];

  for (const item of data) {
    const matchesKeyword =
      item.name.includes(keyword) || item.code.includes(keyword);

    if (matchesKeyword) {
      result.push(item);
    } else if (item.children) {
      const filteredChildren = filterPermissionsByKeyword(
        item.children,
        keyword,
      );
      if (filteredChildren.length > 0) {
        result.push({
          ...item,
          children: filteredChildren,
        });
      }
    }
  }

  return result;
};

const buildTreeOptions = (data: any[]): any[] => {
  return data.map((item) => ({
    id: item.id,
    name: item.name,
    children: item.children ? buildTreeOptions(item.children) : undefined,
  }));
};

const getPermissionTagType = (type: string) => {
  switch (type) {
    case "module":
      return "primary";
    case "menu":
      return "success";
    case "action":
      return "info";
    case "api":
      return "warning";
    default:
      return "info";
  }
};

const getPermissionTypeLabel = (type: string) => {
  switch (type) {
    case "module":
      return "模块";
    case "menu":
      return "菜单";
    case "action":
      return "操作";
    case "api":
      return "API";
    default:
      return type;
  }
};

const getMethodTagType = (method: string) => {
  switch (method) {
    case "GET":
      return "success";
    case "POST":
      return "primary";
    case "PUT":
      return "warning";
    case "DELETE":
      return "danger";
    case "PATCH":
      return "info";
    default:
      return "info";
  }
};

const handleSearch = () => {
  // 搜索逻辑已在计算属性中实现
};

const handleSelectionChange = (selection: any[]) => {
  selectedPermissions.value = selection;
};

const handleAdd = () => {
  isEdit.value = false;
  isAddChild.value = false;
  parentPermission.value = null;
  dialogVisible.value = true;
};

const handleAddChild = (row: any) => {
  isEdit.value = false;
  isAddChild.value = true;
  parentPermission.value = row;
  formData.value.parentId = row.id;
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  isEdit.value = true;
  isAddChild.value = false;
  formData.value = { ...row };
  dialogVisible.value = true;
};

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限 "${row.name}" 吗？删除后其子权限也将被删除。`,
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
      `确定要删除选中的 ${selectedPermissions.value.length} 个权限吗？`,
      "批量删除确认",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    // 批量删除逻辑
    ElMessage.success("批量删除成功");
    selectedPermissions.value = [];
  } catch {
    // 用户取消
  }
};

const handleSync = async () => {
  try {
    loading.value = true;
    // 同步权限逻辑
    await new Promise((resolve) => setTimeout(resolve, 2000));
    ElMessage.success("权限同步成功");
  } catch (error) {
    ElMessage.error("权限同步失败");
  } finally {
    loading.value = false;
  }
};

const handleStatusChange = (row: any) => {
  ElMessage.success(`权限状态已${row.status ? "启用" : "禁用"}`);
};

const handleTypeChange = (type: string) => {
  // 根据类型清空相关字段
  if (type !== "api") {
    formData.value.method = "";
  }
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
    type: "menu",
    parentId: null,
    path: "",
    method: "",
    icon: "",
    sort: 0,
    description: "",
    status: 1,
  };
  formRef.value?.resetFields();
};

const showIconSelector = () => {
  selectedIcon.value = formData.value.icon;
  iconSelectorVisible.value = true;
};

const selectIcon = (icon: string) => {
  selectedIcon.value = icon;
};

const confirmIconSelection = () => {
  formData.value.icon = selectedIcon.value;
  iconSelectorVisible.value = false;
};

onMounted(() => {
  // 初始化数据
});
</script>

<style lang="scss" scoped>
.system-permission {
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

  .filter-box {
    display: flex;
    align-items: center;
  }
}

.permission-name {
  display: flex;
  align-items: center;

  .permission-icon {
    margin-right: $spacing-xs;
    color: $color-primary;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.icon-selector {
  .icon-search {
    margin-bottom: $spacing-lg;
  }

  .icon-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: $spacing-sm;
    max-height: 400px;
    overflow-y: auto;
  }

  .icon-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-sm;
    border: 1px solid $border-light;
    border-radius: $border-radius;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: $color-primary;
      background-color: rgba($color-primary, 0.05);
    }

    &.active {
      border-color: $color-primary;
      background-color: rgba($color-primary, 0.1);
    }

    .el-icon {
      font-size: 24px;
      margin-bottom: $spacing-xs;
      color: $color-primary;
    }

    span {
      font-size: 0.8rem;
      color: $text-secondary;
      text-align: center;
    }
  }
}
</style>
