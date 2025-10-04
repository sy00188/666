<template>
  <div class="role-users">
    <div class="users-header">
      <h3>角色用户管理</h3>
      <p class="users-description">
        管理角色 <strong>{{ role?.name }}</strong> 的用户成员，当前共有 <strong>{{ totalUsers }}</strong> 个用户
      </p>
    </div>

    <div class="users-content">
      <!-- 搜索和操作栏 -->
      <div class="users-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名、姓名或邮箱"
            clearable
            style="width: 300px"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="toolbar-right">
          <el-button type="primary" @click="handleAddUsers" v-if="!isReadOnly">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
          <el-button 
            type="danger" 
            @click="handleBatchRemove" 
            :disabled="selectedUsers.length === 0"
            v-if="!isReadOnly"
          >
            <el-icon><Delete /></el-icon>
            批量移除
          </el-button>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="users-table">
        <el-table
          :data="userList"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          stripe
          border
        >
          <el-table-column type="selection" width="55" v-if="!isReadOnly" />
          
          <el-table-column prop="username" label="用户名" width="120" />
          
          <el-table-column prop="realName" label="真实姓名" width="120" />
          
          <el-table-column prop="email" label="邮箱" min-width="180" />
          
          <el-table-column prop="phone" label="手机号" width="130" />
          
          <el-table-column prop="department" label="部门" width="150">
            <template #default="{ row }">
              {{ row.department?.name || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                {{ row.status === 'active' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="lastLoginAt" label="最后登录" width="160">
            <template #default="{ row }">
              {{ row.lastLoginAt ? formatDateTime(row.lastLoginAt) : '-' }}
            </template>
          </el-table-column>
          
          <el-table-column prop="assignedAt" label="分配时间" width="160">
            <template #default="{ row }">
              {{ row.assignedAt ? formatDateTime(row.assignedAt) : '-' }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right" v-if="!isReadOnly">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                @click="handleRemoveUser(row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="users-pagination">
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
    </div>

    <!-- 添加用户对话框 -->
    <el-dialog
      v-model="addUsersDialogVisible"
      title="添加用户到角色"
      width="800px"
      @close="handleAddUsersDialogClose"
    >
      <AddUsersToRole
        v-if="addUsersDialogVisible"
        :role="role"
        :exclude-user-ids="userList.map(user => user.id)"
        @success="handleAddUsersSuccess"
        @cancel="handleAddUsersDialogClose"
      />
    </el-dialog>

    <!-- 操作确认对话框 -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="confirmDialogTitle"
      width="400px"
    >
      <p>{{ confirmDialogMessage }}</p>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmAction">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Plus, Delete } from "@element-plus/icons-vue";
import { getRoleUsers, removeUserFromRole, batchRemoveUsersFromRole } from "@/api/modules/role";
import { formatDateTime } from "@/utils/format";
import AddUsersToRole from "./AddUsersToRole.vue";
import type { Role, RoleUser } from "@/types/role";

// Props
interface Props {
  role?: Partial<Role>;
  readOnly?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  role: () => ({}),
  readOnly: false,
});

// Emits
const emit = defineEmits<{
  close: [];
  update: [userCount: number];
}>();

// 响应式数据
const loading = ref(false);
const userList = ref<RoleUser[]>([]);
const selectedUsers = ref<RoleUser[]>([]);
const searchKeyword = ref("");
const addUsersDialogVisible = ref(false);
const confirmDialogVisible = ref(false);
const confirmDialogTitle = ref("");
const confirmDialogMessage = ref("");
const pendingAction = ref<(() => void) | null>(null);

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0,
});

// 计算属性
const isReadOnly = computed(() => props.readOnly);
const totalUsers = computed(() => pagination.total);

// 监听角色变化
watch(
  () => props.role,
  (newRole) => {
    if (newRole?.id) {
      loadRoleUsers();
    }
  },
  { immediate: true }
);

// 加载角色用户
const loadRoleUsers = async () => {
  if (!props.role?.id) return;

  try {
    loading.value = true;
    const response = await getRoleUsers(props.role.id, {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value,
    });

    if (response.success) {
      userList.value = response.data.list;
      pagination.total = response.data.total;
    } else {
      ElMessage.error(response.message || "加载用户列表失败");
    }
  } catch (error) {
    console.error("加载角色用户失败:", error);
    ElMessage.error("加载用户列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  pagination.page = 1;
  loadRoleUsers();
};

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  loadRoleUsers();
};

const handleCurrentChange = (page: number) => {
  pagination.page = page;
  loadRoleUsers();
};

// 选择变化处理
const handleSelectionChange = (selection: RoleUser[]) => {
  selectedUsers.value = selection;
};

// 添加用户
const handleAddUsers = () => {
  addUsersDialogVisible.value = true;
};

// 移除单个用户
const handleRemoveUser = (user: RoleUser) => {
  confirmDialogTitle.value = "确认移除用户";
  confirmDialogMessage.value = `确定要将用户 "${user.realName || user.username}" 从角色中移除吗？`;
  pendingAction.value = () => removeUser(user.id);
  confirmDialogVisible.value = true;
};

// 批量移除用户
const handleBatchRemove = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning("请选择要移除的用户");
    return;
  }

  confirmDialogTitle.value = "确认批量移除用户";
  confirmDialogMessage.value = `确定要将选中的 ${selectedUsers.value.length} 个用户从角色中移除吗？`;
  pendingAction.value = () => batchRemoveUsers();
  confirmDialogVisible.value = true;
};

// 移除用户
const removeUser = async (userId: string) => {
  if (!props.role?.id) return;

  try {
    const response = await removeUserFromRole(props.role.id, userId);
    if (response.success) {
      ElMessage.success("用户移除成功");
      loadRoleUsers();
      emit("update", pagination.total - 1);
    } else {
      ElMessage.error(response.message || "用户移除失败");
    }
  } catch (error) {
    console.error("移除用户失败:", error);
    ElMessage.error("用户移除失败");
  }
};

// 批量移除用户
const batchRemoveUsers = async () => {
  if (!props.role?.id || selectedUsers.value.length === 0) return;

  try {
    const userIds = selectedUsers.value.map(user => user.id);
    const response = await batchRemoveUsersFromRole(props.role.id, userIds);
    
    if (response.success) {
      ElMessage.success(`成功移除 ${selectedUsers.value.length} 个用户`);
      selectedUsers.value = [];
      loadRoleUsers();
      emit("update", pagination.total - selectedUsers.value.length);
    } else {
      ElMessage.error(response.message || "批量移除失败");
    }
  } catch (error) {
    console.error("批量移除用户失败:", error);
    ElMessage.error("批量移除失败");
  }
};

// 确认操作
const handleConfirmAction = () => {
  if (pendingAction.value) {
    pendingAction.value();
    pendingAction.value = null;
  }
  confirmDialogVisible.value = false;
};

// 添加用户对话框关闭
const handleAddUsersDialogClose = () => {
  addUsersDialogVisible.value = false;
};

// 添加用户成功
const handleAddUsersSuccess = (addedCount: number) => {
  addUsersDialogVisible.value = false;
  ElMessage.success(`成功添加 ${addedCount} 个用户`);
  loadRoleUsers();
  emit("update", pagination.total + addedCount);
};

// 生命周期
onMounted(() => {
  if (props.role?.id) {
    loadRoleUsers();
  }
});
</script>

<style scoped lang="scss">
.role-users {
  .users-header {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .users-description {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .users-content {
    .users-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 6px;

      .toolbar-left {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .toolbar-right {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }

    .users-table {
      margin-bottom: 16px;
    }

    .users-pagination {
      display: flex;
      justify-content: center;
    }
  }
}

:deep(.el-table) {
  .el-table__row {
    &:hover {
      background: #f5f7fa;
    }
  }
}

:deep(.el-dialog) {
  .el-dialog__body {
    padding: 20px;
  }
}
</style>
