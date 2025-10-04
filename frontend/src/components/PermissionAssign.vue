<template>
  <div class="permission-assign">
    <div class="assign-header">
      <h3>权限分配</h3>
      <p class="assign-description">
        为角色 <strong>{{ role?.name }}</strong> 分配权限，支持菜单权限、操作权限和API权限
      </p>
    </div>

    <div class="assign-content">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="permission-tree-container">
            <div class="tree-header">
              <span>权限树</span>
              <div class="tree-actions">
                <el-button size="small" @click="expandAll">展开全部</el-button>
                <el-button size="small" @click="collapseAll">收起全部</el-button>
                <el-button size="small" @click="selectAll">全选</el-button>
                <el-button size="small" @click="unselectAll">取消全选</el-button>
              </div>
            </div>
            
            <el-tree
              ref="permissionTreeRef"
              :data="permissionTree"
              :props="treeProps"
              :default-checked-keys="checkedKeys"
              :default-expanded-keys="expandedKeys"
              show-checkbox
              node-key="id"
              check-strictly
              :filter-node-method="filterNode"
              class="permission-tree"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon v-if="data.icon" class="node-icon">
                    <component :is="data.icon" />
                  </el-icon>
                  <span class="node-label">{{ data.name }}</span>
                  <el-tag
                    v-if="data.type"
                    :type="getPermissionTypeTag(data.type)"
                    size="small"
                    class="node-tag"
                  >
                    {{ getPermissionTypeText(data.type) }}
                  </el-tag>
                </div>
              </template>
            </el-tree>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="selected-permissions">
            <div class="selected-header">
              <span>已选权限 ({{ selectedPermissions.length }})</span>
              <el-button size="small" @click="clearSelected">清空</el-button>
            </div>
            
            <div class="selected-list">
              <el-scrollbar height="400px">
                <div
                  v-for="permission in selectedPermissions"
                  :key="permission.id"
                  class="selected-item"
                >
                  <div class="item-content">
                    <el-icon v-if="permission.icon" class="item-icon">
                      <component :is="permission.icon" />
                    </el-icon>
                    <span class="item-label">{{ permission.name }}</span>
                    <el-tag
                      :type="getPermissionTypeTag(permission.type)"
                      size="small"
                    >
                      {{ getPermissionTypeText(permission.type) }}
                    </el-tag>
                  </div>
                  <el-button
                    type="danger"
                    size="small"
                    text
                    @click="removePermission(permission.id)"
                  >
                    移除
                  </el-button>
                </div>
              </el-scrollbar>
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="permission-summary">
        <el-alert
          :title="`已选择 ${selectedPermissions.length} 个权限`"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="summary-stats">
              <span>菜单权限: {{ getPermissionCountByType('MENU') }}</span>
              <span>操作权限: {{ getPermissionCountByType('BUTTON') }}</span>
              <span>API权限: {{ getPermissionCountByType('API') }}</span>
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <div class="assign-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确定分配
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, nextTick } from "vue";
import { ElMessage, ElTree, type TreeNode } from "element-plus";
import { getPermissionTree, getRolePermissions, assignRolePermissions } from "@/api/modules/role";
import type { Role, Permission, PermissionTreeNode } from "@/types/role";

// Props
interface Props {
  role?: Partial<Role>;
}

const props = withDefaults(defineProps<Props>(), {
  role: () => ({}),
});

// Emits
const emit = defineEmits<{
  submit: [permissionIds: string[]];
  cancel: [];
}>();

// 响应式数据
const permissionTreeRef = ref<InstanceType<typeof ElTree>>();
const submitting = ref(false);
const permissionTree = ref<PermissionTreeNode[]>([]);
const checkedKeys = ref<string[]>([]);
const expandedKeys = ref<string[]>([]);
const selectedPermissions = ref<Permission[]>([]);

// 树形组件配置
const treeProps = {
  children: "children",
  label: "name",
};

// 计算属性
const hasSelectedPermissions = computed(() => selectedPermissions.value.length > 0);

// 监听角色变化
watch(
  () => props.role,
  (newRole) => {
    if (newRole?.id) {
      loadRolePermissions();
    }
  },
  { immediate: true }
);

// 监听选中的权限变化
watch(
  checkedKeys,
  (newKeys) => {
    updateSelectedPermissions(newKeys);
  },
  { deep: true }
);

// 加载权限树
const loadPermissionTree = async () => {
  try {
    const response = await getPermissionTree();
    if (response.success) {
      permissionTree.value = response.data;
      // 默认展开第一级
      expandedKeys.value = response.data.map(item => item.id);
    }
  } catch (error) {
    console.error("加载权限树失败:", error);
    ElMessage.error("加载权限树失败");
  }
};

// 加载角色权限
const loadRolePermissions = async () => {
  if (!props.role?.id) return;

  try {
    const response = await getRolePermissions(props.role.id);
    if (response.success) {
      checkedKeys.value = response.data.map(permission => permission.id);
    }
  } catch (error) {
    console.error("加载角色权限失败:", error);
    ElMessage.error("加载角色权限失败");
  }
};

// 更新选中的权限列表
const updateSelectedPermissions = (checkedKeys: string[]) => {
  const flattenPermissions = (permissions: PermissionTreeNode[]): Permission[] => {
    const result: Permission[] = [];
    permissions.forEach(permission => {
      if (checkedKeys.includes(permission.id)) {
        result.push(permission);
      }
      if (permission.children && permission.children.length > 0) {
        result.push(...flattenPermissions(permission.children));
      }
    });
    return result;
  };

  selectedPermissions.value = flattenPermissions(permissionTree.value);
};

// 展开全部
const expandAll = () => {
  const getAllKeys = (permissions: PermissionTreeNode[]): string[] => {
    const keys: string[] = [];
    permissions.forEach(permission => {
      keys.push(permission.id);
      if (permission.children && permission.children.length > 0) {
        keys.push(...getAllKeys(permission.children));
      }
    });
    return keys;
  };
  expandedKeys.value = getAllKeys(permissionTree.value);
};

// 收起全部
const collapseAll = () => {
  expandedKeys.value = [];
};

// 全选
const selectAll = () => {
  const getAllKeys = (permissions: PermissionTreeNode[]): string[] => {
    const keys: string[] = [];
    permissions.forEach(permission => {
      keys.push(permission.id);
      if (permission.children && permission.children.length > 0) {
        keys.push(...getAllKeys(permission.children));
      }
    });
    return keys;
  };
  checkedKeys.value = getAllKeys(permissionTree.value);
};

// 取消全选
const unselectAll = () => {
  checkedKeys.value = [];
};

// 清空选中
const clearSelected = () => {
  checkedKeys.value = [];
};

// 移除权限
const removePermission = (permissionId: string) => {
  checkedKeys.value = checkedKeys.value.filter(id => id !== permissionId);
};

// 过滤节点
const filterNode = (value: string, data: PermissionTreeNode) => {
  if (!value) return true;
  return data.name.includes(value);
};

// 获取权限类型文本
const getPermissionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    MENU: "菜单",
    BUTTON: "操作",
    API: "API",
  };
  return typeMap[type] || type;
};

// 获取权限类型标签
const getPermissionTypeTag = (type: string): "primary" | "success" | "warning" | "info" | "danger" => {
  const tagMap: Record<string, "primary" | "success" | "warning" | "info" | "danger"> = {
    MENU: "primary",
    BUTTON: "success",
    API: "warning",
  };
  return tagMap[type] || "info";
};

// 获取指定类型的权限数量
const getPermissionCountByType = (type: string) => {
  return selectedPermissions.value.filter(permission => permission.type === type).length;
};

// 提交分配
const handleSubmit = async () => {
  if (!props.role?.id) {
    ElMessage.error("角色信息不完整");
    return;
  }

  if (selectedPermissions.value.length === 0) {
    ElMessage.warning("请至少选择一个权限");
    return;
  }

  try {
    submitting.value = true;

    const permissionIds = selectedPermissions.value.map(permission => permission.id);
    
    const response = await assignRolePermissions({
      roleId: props.role.id,
      permissionIds,
    });

    if (response.success) {
      ElMessage.success("权限分配成功");
      emit("submit", permissionIds);
    } else {
      ElMessage.error(response.message || "权限分配失败");
    }
  } catch (error) {
    console.error("权限分配失败:", error);
    ElMessage.error("权限分配失败");
  } finally {
    submitting.value = false;
  }
};

// 取消操作
const handleCancel = () => {
  emit("cancel");
};

// 生命周期
onMounted(() => {
  loadPermissionTree();
});
</script>

<style scoped lang="scss">
.permission-assign {
  .assign-header {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .assign-description {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .assign-content {
    margin-bottom: 24px;
  }

  .permission-tree-container {
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    overflow: hidden;

    .tree-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f5f7fa;
      border-bottom: 1px solid #e4e7ed;
      font-weight: 600;
      color: #303133;

      .tree-actions {
        display: flex;
        gap: 8px;
      }
    }

    .permission-tree {
      padding: 12px;
      max-height: 400px;
      overflow-y: auto;

      .tree-node {
        display: flex;
        align-items: center;
        gap: 8px;
        width: 100%;

        .node-icon {
          font-size: 16px;
          color: #409eff;
        }

        .node-label {
          flex: 1;
          font-size: 14px;
        }

        .node-tag {
          margin-left: auto;
        }
      }
    }
  }

  .selected-permissions {
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    overflow: hidden;

    .selected-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f5f7fa;
      border-bottom: 1px solid #e4e7ed;
      font-weight: 600;
      color: #303133;
    }

    .selected-list {
      .selected-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #f0f2f5;

        &:last-child {
          border-bottom: none;
        }

        .item-content {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;

          .item-icon {
            font-size: 16px;
            color: #409eff;
          }

          .item-label {
            flex: 1;
            font-size: 14px;
          }
        }

        &:hover {
          background: #f5f7fa;
        }
      }
    }
  }

  .permission-summary {
    margin-top: 20px;

    .summary-stats {
      display: flex;
      gap: 20px;
      margin-top: 8px;

      span {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .assign-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
  }
}

:deep(.el-tree) {
  .el-tree-node__content {
    padding: 8px 0;
  }

  .el-checkbox {
    margin-right: 8px;
  }
}

:deep(.el-scrollbar__view) {
  padding: 0;
}
</style>