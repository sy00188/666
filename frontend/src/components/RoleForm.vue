<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      :disabled="props.mode === 'view'"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色名称" prop="name">
            <el-input
              v-model="formData.name"
              placeholder="请输入角色名称"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="角色编码" prop="code">
            <el-input
              v-model="formData.code"
              placeholder="请输入角色编码"
              maxlength="20"
              show-word-limit
              :disabled="props.mode === 'edit'"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色状态" prop="status">
            <el-select v-model="formData.status" placeholder="请选择角色状态">
              <el-option label="启用" value="active" />
              <el-option label="禁用" value="inactive" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序值" prop="sort">
            <el-input-number
              v-model="formData.sort"
              :min="0"
              :max="9999"
              controls-position="right"
              placeholder="请输入排序值"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="角色描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入角色描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="权限配置">
        <div class="permission-tree-container">
          <el-tree
            ref="permissionTreeRef"
            :data="permissionTree"
            :props="treeProps"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedPermissions"
            :check-strictly="false"
            @check="handlePermissionCheck"
            v-loading="permissionLoading"
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
              </div>
            </template>
          </el-tree>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="props.mode !== 'view'"
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ props.mode === "create" ? "创建" : "更新" }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick, onMounted } from "vue";
import { ElMessage, ElMessageBox, ElTree } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import {
  createRole,
  updateRole,
  getPermissionTree,
  checkRoleCodeAvailable,
} from "@/api/modules/role";
import type {
  Role,
  CreateRoleForm,
  UpdateRoleForm,
  Permission,
  PermissionTreeNode,
} from "@/types/role";
import { RoleStatus, PermissionType } from "@/types/role";

// 定义组件属性
interface Props {
  visible: boolean;
  mode: "create" | "edit" | "view";
  roleData?: Role;
}

// 定义事件
interface Emits {
  (e: "update:visible", value: boolean): void;
  (e: "success"): void;
}

const props = withDefaults(defineProps<Props>(), {
  mode: "create",
  roleData: undefined,
});

const emit = defineEmits<Emits>();

// 表单引用
const formRef = ref<FormInstance>();
const permissionTreeRef = ref<InstanceType<typeof ElTree>>();

// 表单数据
const formData = reactive<CreateRoleForm>({
  name: "",
  code: "",
  description: "",
  status: RoleStatus.ACTIVE,
  type: "custom",
  sort: 0,
  permissions: [],
});

// 权限树数据
const permissionTree = ref<PermissionTreeNode[]>([]);
const checkedPermissions = ref<string[]>([]);

// 加载状态
const loading = ref(false);
const permissionLoading = ref(false);

// 树形组件属性配置
const treeProps = {
  children: "children",
  label: "name",
};

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: "请输入角色名称", trigger: "blur" },
    {
      min: 2,
      max: 50,
      message: "角色名称长度在 2 到 50 个字符",
      trigger: "blur",
    },
  ],
  code: [
    { required: true, message: "请输入角色编码", trigger: "blur" },
    {
      pattern: /^[A-Z_][A-Z0-9_]*$/,
      message:
        "角色编码只能包含大写字母、数字和下划线，且以大写字母或下划线开头",
      trigger: "blur",
    },
    { validator: validateRoleCode, trigger: "blur" },
  ],
  status: [{ required: true, message: "请选择角色状态", trigger: "change" }],
  sort: [
    { required: true, message: "请输入排序值", trigger: "blur" },
    {
      type: "number",
      min: 0,
      max: 9999,
      message: "排序值必须在 0 到 9999 之间",
      trigger: "blur",
    },
  ],
});

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit("update:visible", value),
});

const dialogTitle = computed(() => {
  const titles = {
    create: "创建角色",
    edit: "编辑角色",
    view: "查看角色",
  };
  return titles[props.mode];
});

// 自定义验证器
function validateRoleCode(
  rule: any,
  value: string,
  callback: (error?: Error) => void,
) {
  if (!value) {
    callback();
    return;
  }

  // 使用异步验证
  checkRoleCodeAvailable(
    value,
    props.mode === "edit" ? props.roleData?.id : undefined,
  )
    .then((response) => {
      if (!response.data) {
        callback(new Error("角色编码已存在"));
      } else {
        callback();
      }
    })
    .catch(() => {
      callback(new Error("验证角色编码失败"));
    });
}

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
  try {
    permissionLoading.value = true;
    const response = await getPermissionTree();
    permissionTree.value = response.data || [];
  } catch (error) {
    console.error("加载权限树失败:", error);
    ElMessage.error("加载权限树失败");
  } finally {
    permissionLoading.value = false;
  }
}

// 处理权限选择
function handlePermissionCheck(data: PermissionTreeNode, checked: any) {
  const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || [];
  const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || [];
  formData.permissions = [...checkedKeys, ...halfCheckedKeys] as string[];
}

// 初始化表单数据
function initFormData() {
  if (props.mode === "edit" && props.roleData) {
    Object.assign(formData, {
      name: props.roleData.name,
      code: props.roleData.code,
      description: props.roleData.description,
      status: props.roleData.status,
      type: props.roleData.type,
      sort: props.roleData.sort,
      permissions: props.roleData.permissionIds || [],
    });
    checkedPermissions.value = props.roleData.permissionIds || [];
  } else {
    // 重置表单数据
    Object.assign(formData, {
      name: "",
      code: "",
      description: "",
      status: RoleStatus.ACTIVE,
      type: "custom",
      sort: 0,
      permissions: [],
    });
    checkedPermissions.value = [];
  }

  // 清除表单验证
  nextTick(() => {
    formRef.value?.clearValidate();
  });
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return;

  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    loading.value = true;

    // 获取选中的权限
    const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || [];
    const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || [];
    const allPermissions = [...checkedKeys, ...halfCheckedKeys] as string[];

    const submitData = {
      ...formData,
      permissions: allPermissions,
    };

    if (props.mode === "create") {
      await createRole(submitData as CreateRoleForm);
      ElMessage.success("创建角色成功");
    } else if (props.mode === "edit" && props.roleData) {
      await updateRole(props.roleData.id, submitData as UpdateRoleForm);
      ElMessage.success("更新角色成功");
    }

    emit("success");
    handleClose();
  } catch (error) {
    console.error("提交失败:", error);
    ElMessage.error(props.mode === "create" ? "创建角色失败" : "更新角色失败");
  } finally {
    loading.value = false;
  }
}

// 关闭对话框
function handleClose() {
  emit("update:visible", false);
}

// 监听对话框显示状态
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      initFormData();
      loadPermissionTree();
    }
  },
  { immediate: true },
);

// 监听角色数据变化
watch(
  () => props.roleData,
  () => {
    if (props.visible) {
      initFormData();
    }
  },
  { deep: true },
);

onMounted(() => {
  if (props.visible) {
    loadPermissionTree();
  }
});
</script>

<style scoped>
.permission-tree-container {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.permission-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.permission-icon {
  font-size: 16px;
  color: var(--el-color-primary);
}

.permission-name {
  flex: 1;
  font-size: 14px;
}

.permission-type {
  margin-left: auto;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-tree-node__content) {
  height: 32px;
}

:deep(.el-tree-node__label) {
  flex: 1;
}
</style>
