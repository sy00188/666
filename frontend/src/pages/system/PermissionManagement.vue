<template>
  <div class="permission-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>权限管理</h2>
        <p>管理系统权限，支持菜单权限、操作权限和API权限</p>
      </div>
      <div class="header-right">
        <el-button
          type="primary"
          :icon="Plus"
          @click="handleAdd"
          v-if="hasPermission('permission:create')"
        >
          新增权限
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="权限名称/编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择"
            clearable
            style="width: 120px"
          >
            <el-option label="菜单权限" value="MENU" />
            <el-option label="操作权限" value="BUTTON" />
            <el-option label="API权限" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择"
            clearable
            style="width: 100px"
          >
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="RefreshLeft" @click="handleResetSearch">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 批量操作区域 -->
    <div class="batch-operations" v-if="selectedRows.length > 0">
      <span class="selected-info">已选择 {{ selectedRows.length }} 项</span>
      <el-button
        type="success"
        size="small"
        @click="handleBatchEnable"
        v-if="hasPermission('permission:update')"
      >
        批量启用
      </el-button>
      <el-button
        type="warning"
        size="small"
        @click="handleBatchDisable"
        v-if="hasPermission('permission:update')"
      >
        批量禁用
      </el-button>
      <el-button
        type="danger"
        size="small"
        @click="handleBatchDelete"
        v-if="hasPermission('permission:delete')"
      >
        批量删除
      </el-button>
    </div>

    <!-- 权限表格 -->
    <el-table
      ref="tableRef"
      :data="permissionList"
      v-loading="loading"
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      @selection-change="handleSelectionChange"
      :class="{ 'is-dragging': isDragging }"
      :row-class-name="getRowClassName"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="权限名称" min-width="200">
        <template #default="{ row }">
          <div class="permission-name">
            <el-icon v-if="row.icon" class="permission-icon">
              <component :is="row.icon" />
            </el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="code" label="权限编码" min-width="150" />
      <el-table-column prop="type" label="权限类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getPermissionTypeTag(row.type)">
            {{ getPermissionTypeText(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
            {{ row.status === "ENABLED" ? "启用" : "禁用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="description" label="描述" min-width="150" />
      <el-table-column prop="createdAt" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="handleEdit(row)"
            v-if="hasPermission('permission:update')"
          >
            编辑
          </el-button>
          <el-button
            type="success"
            size="small"
            @click="handleAddChild(row)"
            v-if="hasPermission('permission:create')"
          >
            添加子权限
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleCopy(row)"
            v-if="hasPermission('permission:create')"
          >
            复制
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(row)"
            v-if="hasPermission('permission:delete')"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑权限对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="上级权限" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="permissionTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择上级权限（不选则为顶级权限）"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio value="MENU">菜单权限</el-radio>
            <el-radio value="BUTTON">操作权限</el-radio>
            <el-radio value="API">API权限</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入权限编码，如：user:list"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入权限名称"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="权限描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item
          label="权限路径"
          prop="permissionPath"
          v-if="formData.type === 'MENU'"
        >
          <el-input
            v-model="formData.permissionPath"
            placeholder="请输入路由路径，如：/system/user"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon" v-if="formData.type === 'MENU'">
          <el-input
            v-model="formData.icon"
            placeholder="请输入图标名称"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item
          label="组件路径"
          prop="component"
          v-if="formData.type === 'MENU'"
        >
          <el-input
            v-model="formData.component"
            placeholder="请输入组件路径"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item
          label="重定向"
          prop="redirect"
          v-if="formData.type === 'MENU'"
        >
          <el-input
            v-model="formData.redirect"
            placeholder="请输入重定向路径"
            maxlength="200"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            :max="9999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="设置" v-if="formData.type === 'MENU'">
          <el-checkbox v-model="formData.hidden">隐藏菜单</el-checkbox>
          <el-checkbox v-model="formData.keepAlive">缓存页面</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 复制权限对话框 -->
    <el-dialog
      title="复制权限"
      v-model="copyDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="copyFormRef"
        :model="copyFormData"
        :rules="copyFormRules"
        label-width="100px"
      >
        <el-form-item label="权限名称" prop="name">
          <el-input
            v-model="copyFormData.name"
            placeholder="请输入权限名称"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input
            v-model="copyFormData.code"
            placeholder="请输入权限编码"
            maxlength="100"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="copyDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="copySubmitLoading"
            @click="handleCopySubmit"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  ref,
  reactive,
  onMounted,
  computed,
  nextTick,
  watch,
  onUnmounted,
} from "vue";
import {
  ElMessage,
  ElMessageBox,
  ElTable,
  ElForm,
  type FormRules,
} from "element-plus";
import { Plus, Refresh, Search, RefreshLeft } from "@element-plus/icons-vue";
import { usePermission } from "@/composables/usePermission";
import { formatDateTime } from "@/utils/format";
import {
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
  batchOperatePermissions,
  enablePermission,
  disablePermission,
  copyPermission,
  checkPermissionCode,
  updatePermissionSort,
  batchUpdatePermissionSort,
} from "@/api/modules/permission";
import type {
  Permission,
  PermissionTree,
  PermissionType,
  PermissionStatus,
  CreatePermissionParams,
  UpdatePermissionParams,
  BatchPermissionOperation,
} from "@/types/permission";
import Sortable from "sortablejs";

// 权限检查
const { hasPermission } = usePermission();

// 响应式数据
const loading = ref(false);
const submitLoading = ref(false);
const copySubmitLoading = ref(false);
const dialogVisible = ref(false);
const copyDialogVisible = ref(false);
const selectedRows = ref<Permission[]>([]);
const permissionList = ref<PermissionTree[]>([]);

// 表格引用
const tableRef = ref<InstanceType<typeof ElTable>>();
const formRef = ref<InstanceType<typeof ElForm>>();
const copyFormRef = ref<InstanceType<typeof ElForm>>();

// 拖拽排序相关
const sortableInstance = ref<Sortable | null>(null);
const isDragging = ref(false);

// 搜索表单
const searchForm = reactive({
  keyword: "",
  type: "" as PermissionType | "",
  status: "" as PermissionStatus | "",
});

// 权限表单数据
const formData = reactive<CreatePermissionParams & { id?: string }>({
  code: "",
  name: "",
  description: "",
  type: "MENU" as PermissionType,
  parentId: "",
  sortOrder: 0,
  permissionPath: "",
  icon: "",
  component: "",
  redirect: "",
  hidden: false,
  keepAlive: false,
});

// 复制表单数据
const copyFormData = reactive({
  originalId: "",
  name: "",
  code: "",
});

// 表单验证规则
const formRules: FormRules = {
  code: [
    { required: true, message: "请输入权限编码", trigger: "blur" },
    { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value && value !== formData.code) {
          checkPermissionCode(value)
            .then((exists) => {
              if (exists) {
                callback(new Error("权限编码已存在"));
              } else {
                callback();
              }
            })
            .catch(() => {
              callback();
            });
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  name: [
    { required: true, message: "请输入权限名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  type: [{ required: true, message: "请选择权限类型", trigger: "change" }],
};

// 复制表单验证规则
const copyFormRules: FormRules = {
  name: [
    { required: true, message: "请输入权限名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入权限编码", trigger: "blur" },
    { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value) {
          checkPermissionCode(value)
            .then((exists) => {
              if (exists) {
                callback(new Error("权限编码已存在"));
              } else {
                callback();
              }
            })
            .catch(() => {
              callback();
            });
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

// 计算属性
const dialogTitle = computed(() => {
  return formData.id ? "编辑权限" : "新增权限";
});

const permissionTreeOptions = computed(() => {
  const buildTree = (permissions: PermissionTree[]): any[] => {
    return permissions.map((permission) => ({
      id: permission.id,
      name: permission.name,
      children: permission.children
        ? buildTree(permission.children)
        : undefined,
    }));
  };
  return buildTree(permissionList.value);
});

// 加载权限树
const loadPermissionTree = async () => {
  try {
    loading.value = true;
    const response = await getPermissionTree({
      type: searchForm.type || undefined,
      status: searchForm.status === "" ? undefined : Number(searchForm.status),
    });
    permissionList.value = response.data;
  } catch (error) {
    console.error("加载权限树失败:", error);
    ElMessage.error("加载权限树失败");
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  loadPermissionTree();
};

// 重置搜索
const handleResetSearch = () => {
  Object.assign(searchForm, {
    keyword: "",
    type: "",
    status: "",
  });
  loadPermissionTree();
};

// 选择变化处理
const handleSelectionChange = (selection: Permission[]) => {
  selectedRows.value = selection;
};

// 批量启用
const handleBatchEnable = async () => {
  try {
    const ids = selectedRows.value.map((row) => row.id);
    await batchOperatePermissions({
      operation: "enable",
      permissionIds: ids,
      ids: ids,
    });
    ElMessage.success("批量启用成功");
    loadPermissionTree();
  } catch (error) {
    console.error("批量启用失败:", error);
    ElMessage.error("批量启用失败");
  }
};

// 批量禁用
const handleBatchDisable = async () => {
  try {
    const ids = selectedRows.value.map((row) => row.id);
    await batchOperatePermissions({
      operation: "disable",
      permissionIds: ids,
      ids: ids,
    });
    ElMessage.success("批量禁用成功");
    loadPermissionTree();
  } catch (error) {
    console.error("批量禁用失败:", error);
    ElMessage.error("批量禁用失败");
  }
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个权限吗？`,
      "确认删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const ids = selectedRows.value.map((row) => row.id);
    await batchOperatePermissions({
      operation: "delete",
      permissionIds: ids,
      ids: ids,
    });
    ElMessage.success("批量删除成功");
    loadPermissionTree();
  } catch (error) {
    if (error !== "cancel") {
      console.error("批量删除失败:", error);
      ElMessage.error("批量删除失败");
    }
  }
};

// 新增权限
const handleAdd = () => {
  resetForm();
  dialogVisible.value = true;
};

// 编辑权限
const handleEdit = (row: Permission) => {
  Object.assign(formData, {
    id: row.id,
    code: row.code,
    name: row.name,
    description: row.description,
    type: row.type,
    parentId: row.parentId || "",
    sortOrder: row.sortOrder,
    permissionPath: row.permissionPath || "",
    icon: row.icon || "",
    component: row.component || "",
    redirect: row.redirect || "",
    hidden: row.hidden || false,
    keepAlive: row.keepAlive || false,
  });
  dialogVisible.value = true;
};

// 添加子权限
const handleAddChild = (row: Permission) => {
  resetForm();
  formData.parentId = row.id;
  dialogVisible.value = true;
};

// 复制权限
const handleCopy = (row: Permission) => {
  copyFormData.originalId = row.id;
  copyFormData.name = `${row.name}_副本`;
  copyFormData.code = `${row.code}_copy`;
  copyDialogVisible.value = true;
};

// 删除权限
const handleDelete = async (row: Permission) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除权限"${row.name}"吗？删除后不可恢复！`,
      "确认删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await deletePermission(row.id);
    ElMessage.success("删除成功");
    loadPermissionTree();
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();

    submitLoading.value = true;

    if (formData.id) {
      // 编辑
      await updatePermission(formData as UpdatePermissionParams);
      ElMessage.success("更新成功");
    } else {
      // 新增
      await createPermission(formData as CreatePermissionParams);
      ElMessage.success("创建成功");
    }

    dialogVisible.value = false;
    loadPermissionTree();
  } catch (error) {
    console.error("提交失败:", error);
    ElMessage.error("提交失败");
  } finally {
    submitLoading.value = false;
  }
};

// 复制提交
const handleCopySubmit = async () => {
  try {
    await copyFormRef.value?.validate();

    copySubmitLoading.value = true;

    await copyPermission(
      copyFormData.originalId,
      copyFormData.name,
      copyFormData.code,
    );
    ElMessage.success("复制成功");

    copyDialogVisible.value = false;
    loadPermissionTree();
  } catch (error) {
    console.error("复制失败:", error);
    ElMessage.error("复制失败");
  } finally {
    copySubmitLoading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    code: "",
    name: "",
    description: "",
    type: "MENU" as PermissionType,
    parentId: "",
    sortOrder: 0,
    permissionPath: "",
    icon: "",
    component: "",
    redirect: "",
    hidden: false,
    keepAlive: false,
  });
  formRef.value?.clearValidate();
};

// 获取行类名（用于拖拽层级限制）
const getRowClassName = ({ row }: { row: PermissionTree }) => {
  const level = getPermissionLevel(row, permissionList.value);
  return `permission-row-level-${level}`;
};

// 获取权限层级
const getPermissionLevel = (
  permission: PermissionTree,
  list: PermissionTree[],
  level = 0,
): number => {
  for (const item of list) {
    if (item.id === permission.id) {
      return level;
    }
    if (item.children && item.children.length > 0) {
      const childLevel = getPermissionLevel(
        permission,
        item.children,
        level + 1,
      );
      if (childLevel !== -1) {
        return childLevel;
      }
    }
  }
  return -1;
};

// 获取权限类型文本
const getPermissionTypeText = (type: PermissionType) => {
  const typeMap = {
    MENU: "菜单",
    BUTTON: "操作",
    API: "API",
  };
  return typeMap[type] || type;
};

// 获取权限类型标签
const getPermissionTypeTag = (
  type: PermissionType,
): "primary" | "success" | "warning" | "info" | "danger" => {
  const tagMap: Record<
    PermissionType,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    MENU: "primary",
    BUTTON: "success",
    API: "warning",
  };
  return tagMap[type] || "info";
};

// 初始化拖拽排序
const initSortable = () => {
  nextTick(() => {
    const tableEl = tableRef.value?.$el;
    const tbody = tableEl?.querySelector(".el-table__body-wrapper tbody");

    if (tbody && !sortableInstance.value) {
      sortableInstance.value = Sortable.create(tbody, {
        animation: 150,
        ghostClass: "sortable-ghost",
        chosenClass: "sortable-chosen",
        dragClass: "sortable-drag",
        onStart: () => {
          isDragging.value = true;
        },
        onEnd: (evt) => {
          isDragging.value = false;
          handleDragEnd(evt);
        },
        onMove: (evt) => {
          // 限制只能在同级元素间拖拽
          const draggedElement = evt.dragged;
          const relatedElement = evt.related;

          if (!draggedElement || !relatedElement) return true;

          const draggedLevel = parseInt(
            draggedElement.getAttribute("data-level") || "0",
          );
          const relatedLevel = parseInt(
            relatedElement.getAttribute("data-level") || "0",
          );

          return draggedLevel === relatedLevel;
        },
      });
    }
  });
};

// 处理拖拽结束
const handleDragEnd = async (evt: any) => {
  const { oldIndex, newIndex } = evt;

  if (oldIndex === newIndex) return;

  try {
    // 扁平化权限树以获取正确的索引
    const flattenPermissions = (
      permissions: PermissionTree[],
      level = 0,
    ): Permission[] => {
      const result: Permission[] = [];
      permissions.forEach((permission) => {
        result.push({ ...permission, level });
        if (permission.children && permission.children.length > 0) {
          result.push(...flattenPermissions(permission.children, level + 1));
        }
      });
      return result;
    };

    const flatPermissions = flattenPermissions(permissionList.value);
    const draggedItem = flatPermissions[oldIndex];

    if (!draggedItem) return;

    // 计算新的排序值
    let newSortOrder = 0;
    if (newIndex === 0) {
      newSortOrder = flatPermissions[0].sortOrder - 1;
    } else if (newIndex >= flatPermissions.length - 1) {
      newSortOrder = flatPermissions[flatPermissions.length - 1].sortOrder + 1;
    } else {
      const prevItem = flatPermissions[newIndex - 1];
      const nextItem = flatPermissions[newIndex + 1];
      newSortOrder = Math.floor((prevItem.sortOrder + nextItem.sortOrder) / 2);
    }

    // 调用API更新排序
    await updatePermissionSort(draggedItem.id, newSortOrder);

    ElMessage.success("排序更新成功");
    loadPermissionTree();
  } catch (error) {
    console.error("拖拽排序失败:", error);
    ElMessage.error("拖拽排序失败");
    loadPermissionTree(); // 重新加载以恢复原始顺序
  }
};

// 销毁拖拽实例
const destroySortable = () => {
  if (sortableInstance.value) {
    sortableInstance.value.destroy();
    sortableInstance.value = null;
  }
};

// 生命周期
onMounted(() => {
  loadPermissionTree();
  initSortable();
});

// 监听权限列表变化，重新初始化拖拽
watch(
  permissionList,
  () => {
    destroySortable();
    initSortable();
  },
  { deep: true },
);

onUnmounted(() => {
  destroySortable();
});
</script>

<style scoped lang="scss">
.permission-management {
  padding: 20px;
  background: #fff;
  border-radius: 8px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e4e7ed;

    .header-left {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 6px;
  }

  .batch-operations {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #e8f4fd;
    border: 1px solid #b3d8ff;
    border-radius: 6px;

    .selected-info {
      font-size: 14px;
      color: #409eff;
      font-weight: 500;
    }
  }

  .permission-name {
    display: flex;
    align-items: center;
    gap: 8px;

    .permission-icon {
      font-size: 16px;
      color: #409eff;
    }
  }

  // 拖拽样式
  :deep(.el-table) {
    &.is-dragging {
      .el-table__row {
        cursor: move;
      }
    }

    .sortable-ghost {
      opacity: 0.4;
      background: #f0f9ff;
    }

    .sortable-chosen {
      background: #e8f4fd;
    }

    .sortable-drag {
      background: #fff;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
