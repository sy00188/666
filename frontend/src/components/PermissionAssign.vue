<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="permission-assign">
      <div class="role-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="角色名称">
            {{ roleData?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="角色编码">
            {{ roleData?.code }}
          </el-descriptions-item>
          <el-descriptions-item label="角色描述" :span="2">
            {{ roleData?.description || "暂无描述" }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="permission-tree-container">
        <div class="tree-header">
          <h4>权限分配</h4>
          <div class="tree-actions">
            <el-button size="small" @click="expandAll">展开全部</el-button>
            <el-button size="small" @click="collapseAll">收起全部</el-button>
            <el-button size="small" @click="checkAll">全选</el-button>
            <el-button size="small" @click="uncheckAll">取消全选</el-button>
          </div>
        </div>

        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          :props="treeProps"
          :default-checked-keys="checkedPermissions"
          show-checkbox
          node-key="id"
          check-strictly
          :expand-on-click-node="false"
          v-loading="loading"
          class="permission-tree"
        >
          <template #default="{ node, data }">
            <div class="permission-node">
              <el-icon v-if="data.icon" class="permission-icon">
                <component :is="data.icon" />
              </el-icon>
              <span class="permission-name">{{ node.label }}</span>
              <el-tag
                :type="getPermissionTypeTag(data.type)"
                size="small"
                class="permission-type"
              >
                {{ getPermissionTypeLabel(data.type) }}
              </el-tag>
              <span v-if="data.description" class="permission-desc">
                {{ data.description }}
              </span>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { ElMessage, ElTree } from "element-plus";
import {
  getPermissionTreeWithRoleSelection,
  assignRolePermissions,
} from "@/api/modules/role";
import type {
  Role,
  PermissionTreeNode,
  RolePermissionAssignForm,
} from "@/types/role";
import { PermissionType } from "@/types/role";

// 定义组件属性
interface Props {
  visible: boolean;
  roleData?: Role | null;
}

// 定义事件
interface Emits {
  (e: "update:visible", value: boolean): void;
  (e: "success"): void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  roleData: null,
});

const emit = defineEmits<Emits>();

// 响应式数据
const permissionTreeRef = ref<InstanceType<typeof ElTree>>();
const permissionTree = ref<PermissionTreeNode[]>([]);
const checkedPermissions = ref<string[]>([]);
const loading = ref(false);
const submitting = ref(false);

// 树形组件属性配置
const treeProps = {
  children: "children",
  label: "name",
};

// 计算属性
const dialogTitle = computed(() => {
  return props.roleData ? `分配权限 - ${props.roleData.name}` : "分配权限";
});

// 监听对话框显示状态
watch(
  () => props.visible,
  (newVal) => {
    if (newVal && props.roleData) {
      loadPermissionTree();
    }
  },
);

// 获取权限类型标签样式
function getPermissionTypeTag(
  type: PermissionType,
): "primary" | "success" | "warning" | "info" | "danger" {
  const typeMap: Record<
    PermissionType,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    [PermissionType.MENU]: "primary",
    [PermissionType.BUTTON]: "success",
    [PermissionType.API]: "warning",
    [PermissionType.DATA]: "info",
  };
  return typeMap[type] || "info";
}

// 获取权限类型标签文本
function getPermissionTypeLabel(type: PermissionType): string {
  const typeMap = {
    [PermissionType.MENU]: "菜单",
    [PermissionType.BUTTON]: "按钮",
    [PermissionType.API]: "接口",
    [PermissionType.DATA]: "数据",
  };
  return typeMap[type] || "未知";
}

// 加载权限树
async function loadPermissionTree() {
  if (!props.roleData?.id) return;

  loading.value = true;
  try {
    const response = await getPermissionTreeWithRoleSelection(
      props.roleData.id.toString(),
    );
    if (response.success) {
      permissionTree.value = response.data || [];
      // 提取已选中的权限ID
      checkedPermissions.value = extractCheckedPermissions(
        permissionTree.value,
      );

      // 等待DOM更新后设置选中状态
      await nextTick();
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(checkedPermissions.value);
      }
    } else {
      ElMessage.error(response.message || "加载权限树失败");
    }
  } catch (error) {
    console.error("加载权限树失败:", error);
    ElMessage.error("加载权限树失败");
  } finally {
    loading.value = false;
  }
}

// 提取已选中的权限ID
function extractCheckedPermissions(tree: PermissionTreeNode[]): string[] {
  const checked: string[] = [];

  function traverse(nodes: PermissionTreeNode[]) {
    for (const node of nodes) {
      if (node.checked) {
        checked.push(node.id.toString());
      }
      if (node.children && node.children.length > 0) {
        traverse(node.children);
      }
    }
  }

  traverse(tree);
  return checked;
}

// 展开全部
function expandAll() {
  if (permissionTreeRef.value) {
    const allKeys = getAllNodeKeys(permissionTree.value);
    allKeys.forEach((key) => {
      const node = permissionTreeRef.value?.getNode(key);
      if (node) {
        node.expanded = true;
      }
    });
  }
}

// 收起全部
function collapseAll() {
  if (permissionTreeRef.value) {
    const allKeys = getAllNodeKeys(permissionTree.value);
    allKeys.forEach((key) => {
      const node = permissionTreeRef.value?.getNode(key);
      if (node) {
        node.expanded = false;
      }
    });
  }
}

// 全选
function checkAll() {
  if (permissionTreeRef.value) {
    const allKeys = getAllNodeKeys(permissionTree.value);
    permissionTreeRef.value.setCheckedKeys(allKeys);
  }
}

// 取消全选
function uncheckAll() {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys([]);
  }
}

// 获取所有节点的key
function getAllNodeKeys(tree: PermissionTreeNode[]): string[] {
  const keys: string[] = [];

  function traverse(nodes: PermissionTreeNode[]) {
    for (const node of nodes) {
      keys.push(node.id.toString());
      if (node.children && node.children.length > 0) {
        traverse(node.children);
      }
    }
  }

  traverse(tree);
  return keys;
}

// 提交权限分配
async function handleSubmit() {
  if (!props.roleData?.id || !permissionTreeRef.value) return;

  const checkedKeys = permissionTreeRef.value.getCheckedKeys() as string[];
  const halfCheckedKeys =
    permissionTreeRef.value.getHalfCheckedKeys() as string[];

  // 合并全选和半选的权限ID
  const allSelectedKeys = [...checkedKeys, ...halfCheckedKeys];

  if (allSelectedKeys.length === 0) {
    ElMessage.warning("请至少选择一个权限");
    return;
  }

  submitting.value = true;
  try {
    const assignForm: RolePermissionAssignForm = {
      roleId: props.roleData.id,
      permissionIds: allSelectedKeys,
    };

    const response = await assignRolePermissions(assignForm);
    if (response.success) {
      ElMessage.success("权限分配成功");
      emit("success");
      handleClose();
    } else {
      ElMessage.error(response.message || "权限分配失败");
    }
  } catch (error) {
    console.error("权限分配失败:", error);
    ElMessage.error("权限分配失败");
  } finally {
    submitting.value = false;
  }
}

// 关闭对话框
function handleClose() {
  emit("update:visible", false);
  // 重置数据
  permissionTree.value = [];
  checkedPermissions.value = [];
}
</script>

<style scoped>
.permission-assign {
  padding: 0;
}

.role-info {
  margin-bottom: 20px;
}

.permission-tree-container {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 16px;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.tree-header h4 {
  margin: 0;
  color: var(--el-text-color-primary);
}

.tree-actions {
  display: flex;
  gap: 8px;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}

.permission-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;
}

.permission-icon {
  color: var(--el-color-primary);
}

.permission-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.permission-type {
  margin-left: auto;
}

.permission-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 树形组件样式优化 */
:deep(.el-tree-node__content) {
  height: auto;
  min-height: 32px;
  padding: 4px 0;
}

:deep(.el-tree-node__label) {
  flex: 1;
}

/* 加载状态样式 */
:deep(.el-loading-mask) {
  border-radius: 4px;
}
</style>
