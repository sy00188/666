<template>
  <div class="user-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      :disabled="mode === 'view'"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="formData.username"
          placeholder="请输入用户名"
          :disabled="mode === 'edit'"
        />
      </el-form-item>

      <el-form-item label="真实姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入真实姓名" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="formData.email"
          placeholder="请输入邮箱"
          type="email"
        />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="formData.phone" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="角色" prop="role">
        <el-select
          v-model="formData.role"
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option
            v-for="role in roleOptions"
            :key="role.value"
            :label="role.label"
            :value="role.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="权限" prop="permissions">
        <div class="permissions-container">
          <div
            v-for="(group, groupKey) in permissionGroups"
            :key="groupKey"
            class="permission-group"
          >
            <div class="group-title">
              <el-checkbox
                :model-value="isGroupChecked(groupKey as string)"
                :indeterminate="isGroupIndeterminate(groupKey as string)"
                @change="handleGroupChange(groupKey as string, $event)"
              >
                {{ group.label }}
              </el-checkbox>
            </div>
            <div class="group-permissions">
              <el-checkbox-group v-model="formData.permissions">
                <el-checkbox
                  v-for="permission in group.permissions"
                  :key="permission"
                  :label="permission"
                  :value="permission"
                >
                  {{ getPermissionLabel(permission) }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item v-if="mode === 'create'" label="初始密码" prop="password">
        <el-input
          v-model="formData.password"
          placeholder="请输入初始密码"
          type="password"
          show-password
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio value="active">启用</el-radio>
          <el-radio value="inactive">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="mode !== 'view'">
        <el-button type="primary" @click="handleSubmit">
          {{ mode === "create" ? "创建" : "更新" }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from "vue";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from "element-plus";
import { UserRole, UserPermission, type User } from "@/types/auth";
import {
  ROLE_PERMISSIONS,
  PERMISSION_GROUPS,
  PERMISSION_LABELS,
  ROLE_LABELS,
} from "@/utils/roleManager";
import { userApi } from "@/api/user";

// 扩展用户接口，用于表单
interface UserFormData extends Partial<User> {
  password?: string;
}

interface Props {
  user?: Partial<User>;
  mode: "create" | "edit" | "view";
}

interface Emits {
  (e: "submit", data: UserFormData): void;
  (e: "cancel"): void;
}

const props = withDefaults(defineProps<Props>(), {
  user: () => ({}),
});

const emit = defineEmits<Emits>();

// 表单引用
const formRef = ref<FormInstance>();

// 表单数据
const formData = reactive<UserFormData>({
  username: "",
  name: "",
  email: "",
  phone: "",
  role: UserRole.USER,
  permissions: [],
  password: "",
  status: "active",
});

// 角色选项
const roleOptions = [
  { label: "管理员", value: UserRole.ADMIN },
  { label: "普通用户", value: UserRole.USER },
];

// 权限分组
const permissionGroups = PERMISSION_GROUPS;

// 加载状态
const loading = ref(false);

// 表单验证规则
const rules = reactive<FormRules>({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 3,
      max: 20,
      message: "用户名长度在 3 到 20 个字符",
      trigger: "blur",
    },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: "用户名只能包含字母、数字和下划线",
      trigger: "blur",
    },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }

        // 如果是编辑模式且用户名未改变，跳过验证
        if (props.mode === "edit" && value === props.user?.username) {
          callback();
          return;
        }

        // 模拟异步验证 - 实际项目中应该调用真实的API
        setTimeout(() => {
          // 这里应该调用真实的API检查用户名是否存在
          // 目前模拟一个简单的检查
          const existingUsernames = ["admin", "test", "user"];
          if (existingUsernames.includes(value)) {
            callback(new Error("用户名已存在"));
          } else {
            callback();
          }
        }, 300);
      },
      trigger: "blur",
    },
  ],
  name: [
    { required: true, message: "请输入真实姓名", trigger: "blur" },
    { min: 2, max: 20, message: "姓名长度在 2 到 20 个字符", trigger: "blur" },
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z\s]+$/,
      message: "姓名只能包含中文、英文和空格",
      trigger: "blur",
    },
  ],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }

        // 如果是编辑模式且邮箱未改变，跳过验证
        if (props.mode === "edit" && value === props.user?.email) {
          callback();
          return;
        }

        // 模拟异步验证
        setTimeout(() => {
          const existingEmails = ["admin@example.com", "test@example.com"];
          if (existingEmails.includes(value)) {
            callback(new Error("邮箱已被使用"));
          } else {
            callback();
          }
        }, 300);
      },
      trigger: "blur",
    },
  ],
  phone: [
    { required: true, message: "请输入手机号码", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: "blur",
    },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }

        // 如果是编辑模式且手机号未改变，跳过验证
        if (props.mode === "edit" && value === props.user?.phone) {
          callback();
          return;
        }

        // 模拟异步验证
        setTimeout(() => {
          const existingPhones = ["13800138000", "13900139000"];
          if (existingPhones.includes(value)) {
            callback(new Error("手机号已被使用"));
          } else {
            callback();
          }
        }, 300);
      },
      trigger: "blur",
    },
  ],
  role: [{ required: true, message: "请选择用户角色", trigger: "change" }],
  permissions: [
    {
      validator: (rule, value: UserPermission[], callback) => {
        if (!value || value.length === 0) {
          callback(new Error("请至少选择一个权限"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (props.mode === "create") {
          if (!value) {
            callback(new Error("请输入密码"));
            return;
          }
          if (value.length < 8) {
            callback(new Error("密码长度至少8位"));
            return;
          }
          if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
            callback(new Error("密码必须包含大小写字母和数字"));
            return;
          }
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  status: [{ required: true, message: "请选择用户状态", trigger: "change" }],
});

// 异步验证函数
const checkUsernameExists = async (username: string): Promise<boolean> => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟一些已存在的用户名
      const existingUsernames = ["admin", "test", "user123", "manager"];
      resolve(existingUsernames.includes(username.toLowerCase()));
    }, 500);
  });
};

const checkEmailExists = async (email: string): Promise<boolean> => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟一些已存在的邮箱
      const existingEmails = [
        "admin@example.com",
        "test@test.com",
        "user@demo.com",
      ];
      resolve(existingEmails.includes(email.toLowerCase()));
    }, 500);
  });
};

const checkPhoneExists = async (phone: string): Promise<boolean> => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟一些已存在的手机号
      const existingPhones = ["13800138000", "13900139000", "15800158000"];
      resolve(existingPhones.includes(phone));
    }, 500);
  });
};

// 获取权限标签
const getPermissionLabel = (permission: UserPermission) => {
  return PERMISSION_LABELS[permission] || permission;
};

// 处理权限组选择
const handleGroupChange = (
  groupKey: string,
  checked: boolean | string | number,
) => {
  const group = permissionGroups[groupKey as keyof typeof permissionGroups];
  if (group && formData.permissions) {
    const isChecked = Boolean(checked);
    if (isChecked) {
      // 添加该组的所有权限
      group.permissions.forEach((permission: UserPermission) => {
        if (!formData.permissions!.includes(permission)) {
          formData.permissions!.push(permission);
        }
      });
    } else {
      // 移除该组的所有权限
      group.permissions.forEach((permission: UserPermission) => {
        const index = formData.permissions!.indexOf(permission);
        if (index > -1) {
          formData.permissions!.splice(index, 1);
        }
      });
    }
  }
};

// 检查权限组是否全选
const isGroupChecked = (groupKey: string) => {
  const group = permissionGroups[groupKey as keyof typeof permissionGroups];
  if (!group || !formData.permissions) return false;
  return group.permissions.every((permission: UserPermission) =>
    formData.permissions!.includes(permission),
  );
};

// 检查权限组是否部分选中
const isGroupIndeterminate = (groupKey: string) => {
  const group = permissionGroups[groupKey as keyof typeof permissionGroups];
  if (!group || !formData.permissions) return false;
  const checkedCount = group.permissions.filter((permission: UserPermission) =>
    formData.permissions!.includes(permission),
  ).length;
  return checkedCount > 0 && checkedCount < group.permissions.length;
};

// 监听用户数据变化
watch(
  () => props.user,
  (newUser) => {
    if (newUser) {
      Object.assign(formData, {
        username: newUser.username || "",
        name: newUser.name || "",
        email: newUser.email || "",
        phone: newUser.phone || "",
        role: newUser.role || UserRole.USER,
        permissions: newUser.permissions || [],
        password: "",
        status: newUser.status || "active",
      });
    }
  },
  { immediate: true },
);

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }

  // 重置表单数据
  Object.assign(formData, {
    username: "",
    name: "",
    email: "",
    phone: "",
    role: UserRole.USER,
    permissions: [],
    password: "",
    status: "active",
  });
};

// 清除验证
const clearValidation = () => {
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 验证表单
const validateForm = async (): Promise<boolean> => {
  if (!formRef.value) return false;

  try {
    await formRef.value.validate();
    return true;
  } catch {
    return false;
  }
};

// 验证单个字段
const validateField = (prop: string) => {
  if (formRef.value) {
    formRef.value.validateField(prop);
  }
};

// 获取字段标签
const getFieldLabel = (field: string): string => {
  const labels: Record<string, string> = {
    username: "用户名",
    name: "真实姓名",
    email: "邮箱",
    phone: "手机号",
    role: "角色",
    permissions: "权限",
    password: "密码",
    status: "状态",
  };
  return labels[field] || field;
};

// 获取角色标签
const getRoleLabel = (role: UserRole): string => {
  return ROLE_LABELS[role] || role;
};

// 表单提交
const handleSubmit = async () => {
  if (loading.value) return;

  try {
    loading.value = true;

    // 1. 表单验证
    const isValid = await validateForm();
    if (!isValid) {
      ElMessage.error("请检查表单输入");
      return;
    }

    // 2. 数据预处理
    const submitData = { ...formData };

    // 编辑模式下移除密码字段（如果为空）
    if (props.mode === "edit" && !submitData.password) {
      delete submitData.password;
    }

    // 创建模式下确保有密码
    if (props.mode === "create" && !submitData.password) {
      ElMessage.error("创建用户时密码不能为空");
      return;
    }

    // 3. 数据完整性检查
    const requiredFields = [
      "username",
      "name",
      "email",
      "phone",
      "role",
      "status",
    ];
    for (const field of requiredFields) {
      if (!submitData[field as keyof typeof submitData]) {
        ElMessage.error(`${getFieldLabel(field)}不能为空`);
        return;
      }
    }

    // 权限验证
    if (!submitData.permissions || submitData.permissions.length === 0) {
      ElMessage.error("请至少选择一个权限");
      return;
    }

    // 4. 数据格式化
    // 确保权限数组不为空且有效
    submitData.permissions = (
      submitData.permissions as UserPermission[]
    ).filter((permission: UserPermission) =>
      Object.values(UserPermission).includes(permission),
    );

    // 5. 角色权限一致性检查
    const rolePermissions = ROLE_PERMISSIONS[submitData.role as UserRole];
    if (rolePermissions) {
      const invalidPermissions = (
        submitData.permissions as UserPermission[]
      ).filter(
        (permission: UserPermission) => !rolePermissions.includes(permission),
      );

      if (invalidPermissions.length > 0) {
        const confirm = await ElMessageBox.confirm(
          `所选权限中包含当前角色不支持的权限，是否继续？`,
          "权限警告",
          {
            confirmButtonText: "继续",
            cancelButtonText: "取消",
            type: "warning",
          },
        ).catch(() => false);

        if (!confirm) return;
      }
    }

    // 6. 提交数据
    emit("submit", submitData);

    ElMessage.success(
      props.mode === "create" ? "用户创建成功" : "用户更新成功",
    );
  } catch (error: any) {
    console.error("表单提交失败:", error);
    ElMessage.error(error.message || "操作失败，请重试");
  } finally {
    loading.value = false;
  }
};

// 监听角色变化，自动同步权限
watch(
  () => formData.role,
  (newRole) => {
    if (newRole && ROLE_PERMISSIONS[newRole as UserRole]) {
      // 角色变化时，建议使用该角色的默认权限
      const rolePermissions = ROLE_PERMISSIONS[newRole as UserRole];

      // 如果当前没有权限或权限为空，使用角色默认权限
      if (!formData.permissions || formData.permissions.length === 0) {
        formData.permissions = [...rolePermissions];
      } else {
        // 如果已有权限，询问是否要同步
        ElMessageBox.confirm(
          `是否要将权限同步为${getRoleLabel(newRole as UserRole)}的默认权限？`,
          "权限同步",
          {
            confirmButtonText: "同步",
            cancelButtonText: "保持当前",
            type: "info",
          },
        )
          .then(() => {
            formData.permissions = [...rolePermissions];
          })
          .catch(() => {
            // 用户选择保持当前权限
          });
      }
    }
  },
  { immediate: false },
);

// ... existing code ...

// 暴露给父组件的方法
defineExpose({
  resetForm,
  validateForm,
  clearValidation,
  validateField,
});

// 取消表单
const handleCancel = () => {
  emit("cancel");
};
</script>

<style scoped>
.user-form {
  padding: 20px;
}

.permissions-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.permission-group {
  margin-bottom: 15px;
}

.permission-group:last-child {
  margin-bottom: 0;
}

.group-title {
  font-weight: 600;
  margin-bottom: 8px;
  padding-bottom: 5px;
  border-bottom: 1px solid #f0f0f0;
}

.group-permissions {
  padding-left: 20px;
}

.group-permissions .el-checkbox {
  display: block;
  margin-bottom: 8px;
  margin-right: 0;
}

.group-permissions .el-checkbox:last-child {
  margin-bottom: 0;
}
</style>
