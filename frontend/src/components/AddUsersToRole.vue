<template>
  <div class="add-users-to-role">
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名、姓名或邮箱"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="users-content">
      <el-table
        :data="availableUsers"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
        max-height="400"
      >
        <el-table-column type="selection" width="55" />
        
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
      </el-table>
    </div>

    <div class="selected-info" v-if="selectedUsers.length > 0">
      <el-alert
        :title="`已选择 ${selectedUsers.length} 个用户`"
        type="info"
        :closable="false"
        show-icon
      />
    </div>

    <div class="dialog-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :loading="submitting"
        :disabled="selectedUsers.length === 0"
      >
        确定添加 ({{ selectedUsers.length }})
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from "vue";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import { getAvailableUsers, addUsersToRole } from "@/api/modules/role";
import type { Role, User } from "@/types/role";

// Props
interface Props {
  role?: Partial<Role>;
  excludeUserIds?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  role: () => ({}),
  excludeUserIds: () => [],
});

// Emits
const emit = defineEmits<{
  success: [addedCount: number];
  cancel: [];
}>();

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const availableUsers = ref<User[]>([]);
const selectedUsers = ref<User[]>([]);
const searchKeyword = ref("");

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0,
});

// 监听角色变化
watch(
  () => props.role,
  (newRole) => {
    if (newRole?.id) {
      loadAvailableUsers();
    }
  },
  { immediate: true }
);

// 加载可用用户
const loadAvailableUsers = async () => {
  try {
    loading.value = true;
    const response = await getAvailableUsers({
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value,
      excludeIds: props.excludeUserIds,
    });

    if (response.success) {
      availableUsers.value = response.data.list;
      pagination.total = response.data.total;
    } else {
      ElMessage.error(response.message || "加载用户列表失败");
    }
  } catch (error) {
    console.error("加载可用用户失败:", error);
    ElMessage.error("加载用户列表失败");
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  pagination.page = 1;
  loadAvailableUsers();
};

// 选择变化处理
const handleSelectionChange = (selection: User[]) => {
  selectedUsers.value = selection;
};

// 提交添加
const handleSubmit = async () => {
  if (!props.role?.id || selectedUsers.value.length === 0) {
    ElMessage.warning("请选择要添加的用户");
    return;
  }

  try {
    submitting.value = true;
    const userIds = selectedUsers.value.map(user => user.id);
    
    const response = await addUsersToRole(props.role.id, userIds);
    
    if (response.success) {
      ElMessage.success(`成功添加 ${selectedUsers.value.length} 个用户`);
      emit("success", selectedUsers.value.length);
    } else {
      ElMessage.error(response.message || "添加用户失败");
    }
  } catch (error) {
    console.error("添加用户失败:", error);
    ElMessage.error("添加用户失败");
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
  if (props.role?.id) {
    loadAvailableUsers();
  }
});
</script>

<style scoped lang="scss">
.add-users-to-role {
  .search-section {
    margin-bottom: 16px;
  }

  .users-content {
    margin-bottom: 16px;
  }

  .selected-info {
    margin-bottom: 16px;
  }

  .dialog-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
  }
}

:deep(.el-table) {
  .el-table__row {
    &:hover {
      background: #f5f7fa;
    }
  }
}
</style>

