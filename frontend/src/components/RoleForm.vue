<template>
  <div class="role-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      :disabled="mode === 'view'"
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
              maxlength="50"
              show-word-limit
            />
            <div class="form-tip">角色编码用于系统内部识别，建议使用英文和下划线</div>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="角色描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入角色描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="角色状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio value="active">启用</el-radio>
              <el-radio value="inactive">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序" prop="sort">
            <el-input-number
              v-model="formData.sort"
              :min="0"
              :max="9999"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="角色类型" prop="isSystem">
        <el-radio-group v-model="formData.isSystem" :disabled="mode === 'edit'">
          <el-radio :value="false">自定义角色</el-radio>
          <el-radio :value="true">系统角色</el-radio>
        </el-radio-group>
        <div class="form-tip">系统角色不允许删除，建议谨慎设置</div>
      </el-form-item>

      <el-form-item label="数据权限" prop="dataScope">
        <el-radio-group v-model="formData.dataScope">
          <el-radio value="all">全部数据权限</el-radio>
          <el-radio value="custom">自定义数据权限</el-radio>
          <el-radio value="dept">本部门数据权限</el-radio>
          <el-radio value="deptAndChild">本部门及以下数据权限</el-radio>
          <el-radio value="self">仅本人数据权限</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="formData.dataScope === 'custom'"
        label="数据权限范围"
        prop="deptIds"
      >
        <el-tree-select
          v-model="formData.deptIds"
          :data="deptTreeData"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          multiple
          check-strictly
          placeholder="请选择数据权限范围"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注信息"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div class="form-actions" v-if="mode !== 'view'">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确定
      </el-button>
    </div>

    <div class="form-actions" v-else>
      <el-button @click="handleCancel">关闭</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from "vue";
import { ElMessage, ElForm, type FormInstance, type FormRules } from "element-plus";
import { createRole, updateRole, checkRoleCodeAvailable } from "@/api/modules/role";
import { getDepartmentTree } from "@/api/modules/department";
import type { Role, CreateRoleForm, UpdateRoleForm, Department } from "@/types/role";

// Props
interface Props {
  role?: Partial<Role>;
  mode: "create" | "edit" | "view";
}

const props = withDefaults(defineProps<Props>(), {
  role: () => ({}),
  mode: "create",
});

// Emits
const emit = defineEmits<{
  submit: [data: CreateRoleForm | UpdateRoleForm];
  cancel: [];
}>();

// 响应式数据
const formRef = ref<FormInstance>();
const submitting = ref(false);
const deptTreeData = ref<Department[]>([]);

// 表单数据
const formData = reactive<CreateRoleForm & { id?: string }>({
  name: "",
  code: "",
  description: "",
  status: "active",
  sort: 0,
  isSystem: false,
  dataScope: "all",
  deptIds: [],
  remark: "",
});

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: "请输入角色名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入角色编码", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: "角色编码只能包含字母、数字和下划线，且以字母开头", trigger: "blur" },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value && value !== props.role?.code) {
          checkRoleCodeAvailable(value, props.role?.id)
            .then((res) => {
              if (res.data) {
                callback(new Error("角色编码已存在"));
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
  status: [{ required: true, message: "请选择角色状态", trigger: "change" }],
  dataScope: [{ required: true, message: "请选择数据权限", trigger: "change" }],
  deptIds: [
    {
      validator: (rule: any, value: string[], callback: any) => {
        if (formData.dataScope === "custom" && (!value || value.length === 0)) {
          callback(new Error("请选择数据权限范围"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
};

// 计算属性
const isEdit = computed(() => props.mode === "edit");
const isView = computed(() => props.mode === "view");

// 监听角色变化
watch(
  () => props.role,
  (newRole) => {
    if (newRole && Object.keys(newRole).length > 0) {
      Object.assign(formData, {
        id: newRole.id,
        name: newRole.name || "",
        code: newRole.code || "",
        description: newRole.description || "",
        status: newRole.status || "active",
        sort: newRole.sort || 0,
        isSystem: newRole.isSystem || false,
        dataScope: newRole.dataScope || "all",
        deptIds: newRole.deptIds || [],
        remark: newRole.remark || "",
      });
    }
  },
  { immediate: true, deep: true }
);

// 加载部门树
const loadDepartmentTree = async () => {
  try {
    const response = await getDepartmentTree();
    if (response.success) {
      deptTreeData.value = response.data;
    }
  } catch (error) {
    console.error("加载部门树失败:", error);
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    submitting.value = true;

    const submitData = { ...formData };
    
    if (isEdit.value) {
      emit("submit", submitData as UpdateRoleForm);
    } else {
      emit("submit", submitData as CreateRoleForm);
    }
  } catch (error) {
    console.error("表单验证失败:", error);
  } finally {
    submitting.value = false;
  }
};

// 取消操作
const handleCancel = () => {
  emit("cancel");
};

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    name: "",
    code: "",
    description: "",
    status: "active",
    sort: 0,
    isSystem: false,
    dataScope: "all",
    deptIds: [],
    remark: "",
  });
  formRef.value?.clearValidate();
};

// 暴露方法给父组件
defineExpose({
  resetForm,
  validate: () => formRef.value?.validate(),
  clearValidate: () => formRef.value?.clearValidate(),
});

// 生命周期
onMounted(() => {
  loadDepartmentTree();
});
</script>

<style scoped lang="scss">
.role-form {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-radio-group) {
  .el-radio {
    margin-right: 20px;
    margin-bottom: 8px;
  }
}

:deep(.el-tree-select) {
  width: 100%;
}
</style>