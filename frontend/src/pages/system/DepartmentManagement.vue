<template>
  <div class="department-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>部门管理</h2>
        <p>管理组织架构、部门信息和成员分配</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增部门
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="部门名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入部门名称"
            clearable
            @keyup.enter="handleSearch"
          />
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
    </el-card>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧部门树 -->
      <el-card class="tree-card">
        <template #header>
          <div class="card-header">
            <span>部门结构</span>
            <el-input
              v-model="filterText"
              placeholder="搜索部门"
              size="small"
              clearable
            />
          </div>
        </template>

        <el-tree
          ref="treeRef"
          :data="departmentTree"
          :props="treeProps"
          :filter-node-method="filterNode"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <el-icon class="node-icon">
                <OfficeBuilding />
              </el-icon>
              <span class="node-label">{{ data.name }}</span>
              <span class="node-count">({{ data.userCount || 0 }})</span>
              <div class="node-actions">
                <el-button
                  type="text"
                  size="small"
                  @click.stop="handleEdit(data)"
                >
                  编辑
                </el-button>
                <el-button
                  type="text"
                  size="small"
                  @click.stop="handleAddChild(data)"
                >
                  添加子部门
                </el-button>
                <el-button
                  type="text"
                  size="small"
                  danger
                  @click.stop="handleDelete(data)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </el-card>

      <!-- 右侧详情区域 -->
      <el-card class="detail-card">
        <template #header>
          <span>{{ selectedDepartment ? "部门详情" : "请选择部门" }}</span>
        </template>

        <div v-if="selectedDepartment" class="department-detail">
          <!-- 基本信息 -->
          <div class="info-section">
            <h3>基本信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="部门名称">
                {{ selectedDepartment.name }}
              </el-descriptions-item>
              <el-descriptions-item label="部门编码">
                {{ selectedDepartment.code }}
              </el-descriptions-item>
              <el-descriptions-item label="上级部门">
                {{ selectedDepartment.parentName || "无" }}
              </el-descriptions-item>
              <el-descriptions-item label="部门层级">
                {{ selectedDepartment.level }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag
                  :type="
                    selectedDepartment.status === 'active'
                      ? 'success'
                      : 'danger'
                  "
                >
                  {{ selectedDepartment.status === "active" ? "启用" : "禁用" }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(selectedDepartment.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="部门描述" :span="2">
                {{ selectedDepartment.description || "无" }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 成员管理 -->
          <div class="member-section">
            <div class="section-header">
              <h3>部门成员 ({{ memberList.length }})</h3>
              <el-button
                type="primary"
                size="small"
                @click="showAddMemberDialog"
              >
                添加成员
              </el-button>
            </div>

            <el-table :data="memberList" v-loading="memberLoading">
              <el-table-column prop="realName" label="姓名" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="position" label="职位" />
              <el-table-column label="是否管理员">
                <template #default="{ row }">
                  <el-tag v-if="row.isManager" type="warning">管理员</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag
                    :type="row.status === 'active' ? 'success' : 'danger'"
                  >
                    {{ row.status === "active" ? "正常" : "禁用" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button
                    type="text"
                    size="small"
                    @click="handleSetManager(row)"
                  >
                    {{ row.isManager ? "取消管理员" : "设为管理员" }}
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    danger
                    @click="handleRemoveMember(row)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div v-else class="empty-state">
          <el-empty description="请从左侧选择部门查看详情" />
        </div>
      </el-card>
    </div>

    <!-- 部门表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑部门' : '新增部门'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="departmentForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input
            v-model="departmentForm.name"
            placeholder="请输入部门名称"
          />
        </el-form-item>
        <el-form-item label="部门编码" prop="code">
          <el-input
            v-model="departmentForm.code"
            placeholder="请输入部门编码"
          />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="departmentForm.parentId"
            :data="departmentSelectOptions"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级部门"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="departmentForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="departmentForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="submitLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="memberDialogVisible" title="添加成员" width="500px">
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="选择用户">
          <el-select
            v-model="memberForm.userIds"
            multiple
            placeholder="请选择用户"
            style="width: 100%"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设为管理员">
          <el-switch v-model="memberForm.isManager" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMembers">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from "vue";
import { ElMessage, ElMessageBox, ElTree } from "element-plus";
import { Plus, Refresh, OfficeBuilding } from "@element-plus/icons-vue";
import type {
  Department,
  DepartmentMember,
  CreateDepartmentForm,
  UpdateDepartmentForm,
  DepartmentTreeQueryParams,
} from "@/types/department";
import {
  getDepartmentTree,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  getDepartmentMembers,
  addMemberToDepartment,
  removeMemberFromDepartment,
  setDepartmentManager,
} from "@/api/modules/department";
import { formatDateTime } from "@/utils/date";

// 响应式数据
const loading = ref(false);
const memberLoading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const memberDialogVisible = ref(false);
const isEdit = ref(false);
const filterText = ref("");

// 搜索表单
const searchForm = reactive({
  keyword: "",
  status: undefined as "active" | "inactive" | undefined,
});

// 部门数据
const departmentTree = ref<Department[]>([]);
const selectedDepartment = ref<Department | null>(null);
const memberList = ref<DepartmentMember[]>([]);
const departmentSelectOptions = ref<Department[]>([]);
const availableUsers = ref<any[]>([]);

// 表单数据
const departmentForm = reactive<CreateDepartmentForm>({
  name: "",
  code: "",
  parentId: undefined,
  sort: 0,
  status: "active",
  description: "",
});

const memberForm = reactive({
  userIds: [] as string[],
  isManager: false,
});

// 树形组件配置
const treeProps = {
  children: "children",
  label: "name",
};

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: "请输入部门名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入部门编码", trigger: "blur" },
    {
      pattern: /^[A-Z0-9_]+$/,
      message: "编码只能包含大写字母、数字和下划线",
      trigger: "blur",
    },
  ],
};

// 组件引用
const treeRef = ref<InstanceType<typeof ElTree>>();
const formRef = ref();

// 加载部门树
const loadDepartmentTree = async () => {
  loading.value = true;
  try {
    const params: DepartmentTreeQueryParams = {
      includeUserCount: true,
    };
    if (searchForm.status) {
      params.status = searchForm.status;
    }

    const response = await getDepartmentTree(params);
    departmentTree.value = response.data;
    departmentSelectOptions.value = response.data;
  } catch (error) {
    ElMessage.error("加载部门树失败");
    console.error("Load department tree error:", error);
  } finally {
    loading.value = false;
  }
};

// 加载部门成员
const loadDepartmentMembers = async (departmentId: string) => {
  memberLoading.value = true;
  try {
    const response = await getDepartmentMembers({
      departmentId,
      page: 1,
      size: 100,
    });
    memberList.value = response.list;
  } catch (error) {
    ElMessage.error("加载部门成员失败");
    console.error("Load department members error:", error);
  } finally {
    memberLoading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  loadDepartmentTree();
};

// 重置
const handleReset = () => {
  searchForm.keyword = "";
  searchForm.status = undefined;
  loadDepartmentTree();
};

// 刷新
const handleRefresh = () => {
  loadDepartmentTree();
  if (selectedDepartment.value) {
    loadDepartmentMembers(selectedDepartment.value.id);
  }
};

// 树节点点击
const handleNodeClick = (data: Department) => {
  selectedDepartment.value = data;
  loadDepartmentMembers(data.id);
};

// 过滤树节点
const filterNode = (value: string, data: any) => {
  if (!value) return true;
  return data.name?.includes(value) || data.code?.includes(value);
};

// 新增部门
const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

// 添加子部门
const handleAddChild = (parent: Department) => {
  isEdit.value = false;
  resetForm();
  departmentForm.parentId = parent.id;
  dialogVisible.value = true;
};

// 编辑部门
const handleEdit = (department: Department) => {
  isEdit.value = true;
  Object.assign(departmentForm, {
    name: department.name,
    code: department.code,
    parentId: department.parentId,
    sort: department.sort,
    status: department.status,
    description: department.description,
  });
  dialogVisible.value = true;
};

// 删除部门
const handleDelete = async (department: Department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${department.name}"吗？`,
      "确认删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await deleteDepartment(department.id);
    ElMessage.success("删除成功");
    loadDepartmentTree();

    if (selectedDepartment.value?.id === department.id) {
      selectedDepartment.value = null;
      memberList.value = [];
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除失败");
      console.error("Delete department error:", error);
    }
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    submitLoading.value = true;

    if (isEdit.value && selectedDepartment.value) {
      const updateData: UpdateDepartmentForm = { ...departmentForm };
      await updateDepartment(selectedDepartment.value.id, updateData);
      ElMessage.success("更新成功");
    } else {
      await createDepartment(departmentForm);
      ElMessage.success("创建成功");
    }

    dialogVisible.value = false;
    loadDepartmentTree();
  } catch (error) {
    ElMessage.error(isEdit.value ? "更新失败" : "创建失败");
    console.error("Submit form error:", error);
  } finally {
    submitLoading.value = false;
  }
};

// 显示添加成员对话框
const showAddMemberDialog = () => {
  memberForm.userIds = [];
  memberForm.isManager = false;
  memberDialogVisible.value = true;
  // TODO: 加载可用用户列表
};

// 添加成员
const handleAddMembers = async () => {
  if (!selectedDepartment.value || memberForm.userIds.length === 0) {
    ElMessage.warning("请选择要添加的用户");
    return;
  }

  try {
    for (const userId of memberForm.userIds) {
      await addMemberToDepartment(
        selectedDepartment.value.id,
        userId,
        memberForm.isManager,
      );
    }

    ElMessage.success("添加成员成功");
    memberDialogVisible.value = false;
    loadDepartmentMembers(selectedDepartment.value.id);
  } catch (error) {
    ElMessage.error("添加成员失败");
    console.error("Add members error:", error);
  }
};

// 设置/取消管理员
const handleSetManager = async (member: DepartmentMember) => {
  if (!selectedDepartment.value) return;

  try {
    if (member.isManager) {
      // TODO: 取消管理员的API调用
      ElMessage.success("已取消管理员");
    } else {
      await setDepartmentManager(selectedDepartment.value.id, member.id);
      ElMessage.success("设置管理员成功");
    }

    loadDepartmentMembers(selectedDepartment.value.id);
  } catch (error) {
    ElMessage.error("操作失败");
    console.error("Set manager error:", error);
  }
};

// 移除成员
const handleRemoveMember = async (member: DepartmentMember) => {
  if (!selectedDepartment.value) return;

  try {
    await ElMessageBox.confirm(
      `确定要将"${member.realName}"从部门中移除吗？`,
      "确认移除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    await removeMemberFromDepartment(selectedDepartment.value.id, member.id);
    ElMessage.success("移除成功");
    loadDepartmentMembers(selectedDepartment.value.id);
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("移除失败");
      console.error("Remove member error:", error);
    }
  }
};

// 重置表单
const resetForm = () => {
  Object.assign(departmentForm, {
    name: "",
    code: "",
    parentId: undefined,
    sort: 0,
    status: "active",
    description: "",
  });
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 监听过滤文本
watch(filterText, (val) => {
  treeRef.value?.filter(val);
});

// 组件挂载
onMounted(() => {
  loadDepartmentTree();
});
</script>

<style scoped>
.department-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #303133;
}

.header-left p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

.tree-card {
  width: 400px;
  flex-shrink: 0;
}

.detail-card {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.node-icon {
  margin-right: 8px;
  color: #409eff;
}

.node-label {
  flex: 1;
  font-size: 14px;
}

.node-count {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
}

.node-actions {
  display: none;
}

.tree-node:hover .node-actions {
  display: flex;
  gap: 4px;
}

.department-detail {
  padding: 20px;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.member-section h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}
</style>
