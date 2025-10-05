<template>
  <el-dialog
    v-model="dialogVisible"
    title="QQ登录"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="qq-login-content">
      <!-- 开发模式 - 模拟登录 -->
      <div v-if="isDevelopment" class="mock-mode">
        <div class="mode-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>开发模式 - 模拟QQ登录</span>
        </div>
        
        <el-form
          ref="mockFormRef"
          :model="mockForm"
          :rules="mockRules"
          label-width="80px"
          style="margin-top: 20px;"
        >
          <el-form-item label="QQ号" prop="mockQQId">
            <el-input
              v-model="mockForm.mockQQId"
              placeholder="请输入测试QQ号"
              clearable
            />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input
              v-model="mockForm.mockNickname"
              placeholder="可选，默认为QQ用户+QQ号"
              clearable
            />
          </el-form-item>
        </el-form>
        
        <el-button
          type="primary"
          :loading="loading"
          @click="handleMockLogin"
          class="mock-login-btn"
        >
          模拟QQ登录
        </el-button>
      </div>

      <!-- 生产模式 - 真实二维码 -->
      <div v-else class="qrcode-mode">
        <div class="qrcode-container">
          <img
            v-if="loginData?.qrcodeUrl"
            :src="loginData.qrcodeUrl"
            alt="QQ登录二维码"
            class="qrcode-image"
          />
          <div v-else class="qrcode-placeholder">
            <el-icon><Loading /></el-icon>
            <span>生成二维码中...</span>
          </div>
        </div>
        
        <div class="qrcode-tip">
          <el-icon><Iphone /></el-icon>
          <span>请使用手机QQ扫描二维码登录</span>
        </div>
        
        <div class="qrcode-status">
          <span class="status-text">{{ statusText }}</span>
        </div>
      </div>
    </div>

    <!-- 绑定账号对话框 -->
    <el-dialog
      v-model="bindDialogVisible"
      title="绑定QQ账号"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="bind-content">
        <div class="qq-info">
          <el-avatar :src="bindUserInfo?.avatar" :size="60">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="info-text">
            <p class="nickname">{{ bindUserInfo?.nickname }}</p>
            <p class="tip">首次使用QQ登录，请绑定系统账号</p>
          </div>
        </div>

        <el-tabs v-model="bindTab" class="bind-tabs">
          <!-- 绑定现有账号 -->
          <el-tab-pane label="绑定已有账号" name="existing">
            <el-form
              ref="bindFormRef"
              :model="bindForm"
              :rules="bindRules"
              label-width="70px"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="bindForm.username"
                  placeholder="请输入用户名"
                  clearable
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="bindForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  clearable
                />
              </el-form-item>
            </el-form>
            <el-button
              type="primary"
              :loading="bindLoading"
              @click="handleBindExisting"
              style="width: 100%;"
            >
              绑定账号
            </el-button>
          </el-tab-pane>

          <!-- 创建新账号 -->
          <el-tab-pane label="创建新账号" name="new">
            <el-form
              ref="newAccountFormRef"
              :model="newAccountForm"
              :rules="newAccountRules"
              label-width="70px"
            >
              <el-form-item label="用户名" prop="newUsername">
                <el-input
                  v-model="newAccountForm.newUsername"
                  placeholder="请输入用户名"
                  clearable
                />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="newAccountForm.email"
                  placeholder="请输入邮箱"
                  clearable
                />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="newAccountForm.phone"
                  placeholder="请输入手机号（可选）"
                  clearable
                />
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="newAccountForm.realName"
                  placeholder="请输入真实姓名（可选）"
                  clearable
                />
              </el-form-item>
            </el-form>
            <el-button
              type="primary"
              :loading="bindLoading"
              @click="handleCreateNew"
              style="width: 100%;"
            >
              创建并绑定账号
            </el-button>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onUnmounted } from "vue";
import { ElMessage, ElForm } from "element-plus";
import { InfoFilled, Loading, Iphone, User } from "@element-plus/icons-vue";
import { authApi } from "@/api/auth";
import type {
  QQLoginResponse,
  QQLoginRequest,
  QQBindRequest,
  QQLoginStatus,
  QQUserInfo,
} from "@/types/auth";

// Props
const props = defineProps<{
  visible: boolean;
}>();

// Emits
const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
  (e: "success", data: any): void;
}>();

// 环境判断
const isDevelopment = import.meta.env.DEV;

// 响应式数据
const dialogVisible = ref(false);
const loading = ref(false);
const bindLoading = ref(false);
const loginData = ref<QQLoginResponse | null>(null);
const statusText = ref("");
const bindDialogVisible = ref(false);
const bindUserInfo = ref<QQUserInfo | null>(null);
const bindTab = ref("existing");

// 轮询相关
let statusTimer: NodeJS.Timeout | null = null;

// 模拟登录表单
const mockFormRef = ref<InstanceType<typeof ElForm>>();
const mockForm = reactive({
  mockQQId: "",
  mockNickname: "",
});

const mockRules = {
  mockQQId: [
    { required: true, message: "请输入测试QQ号", trigger: "blur" },
    { pattern: /^\d{5,11}$/, message: "QQ号格式不正确", trigger: "blur" },
  ],
};

// 绑定现有账号表单
const bindFormRef = ref<InstanceType<typeof ElForm>>();
const bindForm = reactive({
  username: "",
  password: "",
});

const bindRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" },
  ],
};

// 创建新账号表单
const newAccountFormRef = ref<InstanceType<typeof ElForm>>();
const newAccountForm = reactive({
  newUsername: "",
  email: "",
  phone: "",
  realName: "",
});

const newAccountRules = {
  newUsername: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度在 3 到 20 个字符", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email" as const, message: "邮箱格式不正确", trigger: "blur" },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
};

// 停止状态轮询
const stopStatusPolling = () => {
  if (statusTimer) {
    clearInterval(statusTimer);
    statusTimer = null;
  }
};

// 清理资源
const cleanup = () => {
  stopStatusPolling();
  loginData.value = null;
  statusText.value = "";
  bindDialogVisible.value = false;
  bindUserInfo.value = null;
  bindTab.value = "existing";

  // 重置表单
  mockForm.mockQQId = "";
  mockForm.mockNickname = "";
  bindForm.username = "";
  bindForm.password = "";
  newAccountForm.newUsername = "";
  newAccountForm.email = "";
  newAccountForm.phone = "";
  newAccountForm.realName = "";
};

watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal;
    if (newVal) {
      initQQLogin();
    } else {
      cleanup();
    }
  },
  { immediate: true }
);

// 监听dialogVisible变化
watch(dialogVisible, (newVal) => {
  emit("update:visible", newVal);
  if (!newVal) {
    cleanup();
  }
});

// 初始化QQ登录
const initQQLogin = async () => {
  try {
    loading.value = true;
    statusText.value = "正在生成登录二维码...";

    const response = await authApi.getQQQRCode();
    loginData.value = response.data;

    if (!isDevelopment) {
      statusText.value = "二维码已生成，请扫描登录";
      startStatusPolling();
    }
  } catch (error: any) {
    console.error("获取QQ登录二维码失败:", error);
    ElMessage.error(error.message || "获取登录二维码失败");
    statusText.value = "获取二维码失败，请重试";
  } finally {
    loading.value = false;
  }
};

// 开始状态轮询
const startStatusPolling = () => {
  if (!loginData.value?.state) return;

  statusTimer = setInterval(async () => {
    try {
      const response = await authApi.checkQQLoginStatus(loginData.value!.state);
      const status = response.data;

      handleStatusUpdate(status);
    } catch (error: any) {
      console.error("检查QQ登录状态失败:", error);
      statusText.value = "检查登录状态失败";
    }
  }, 2000);
};

// 处理状态更新
const handleStatusUpdate = (status: QQLoginStatus) => {
  switch (status.status) {
    case "pending":
      statusText.value = "等待扫码...";
      break;
    case "success":
      statusText.value = "登录成功！";
      stopStatusPolling();
      if (status.loginResponse) {
        // 直接登录成功
        emit("success", status.loginResponse);
        handleClose();
      }
      break;
    case "binding_required":
      statusText.value = "需要绑定账号";
      stopStatusPolling();
      // 显示绑定对话框
      bindUserInfo.value = {
        openid: status.openid || "",
        nickname: status.nickname || "",
        avatar: status.avatar || "",
      };
      bindDialogVisible.value = true;
      break;
    case "expired":
      statusText.value = "二维码已过期，请重新获取";
      stopStatusPolling();
      break;
    case "error":
      statusText.value = status.message || "登录失败";
      stopStatusPolling();
      break;
  }
};

// 处理模拟登录
const handleMockLogin = async () => {
  if (!mockFormRef.value || !loginData.value?.state) return;

  try {
    await mockFormRef.value.validate();
    loading.value = true;

    const loginRequest: QQLoginRequest = {
      state: loginData.value.state,
      mockQQId: mockForm.mockQQId,
      mockNickname: mockForm.mockNickname || `QQ用户${mockForm.mockQQId}`,
    };

    const response = await authApi.mockQQLogin(loginRequest);

    if (response.data?.loginResponse) {
      // 直接登录成功
      ElMessage.success("QQ登录成功");
      emit("success", response.data.loginResponse);
      handleClose();
    } else if (response.data?.bindingRequired) {
      // 需要绑定账号
      bindUserInfo.value = response.data.userInfo;
      bindDialogVisible.value = true;
    }
  } catch (error: any) {
    console.error("模拟QQ登录失败:", error);
    ElMessage.error(error.message || "登录失败");
  } finally {
    loading.value = false;
  }
};

// 绑定现有账号
const handleBindExisting = async () => {
  if (!bindFormRef.value || !loginData.value?.state) return;

  try {
    await bindFormRef.value.validate();
    bindLoading.value = true;

    const bindRequest: QQBindRequest = {
      state: loginData.value.state,
      createNew: false,
      username: bindForm.username,
      password: bindForm.password,
    };

    const response = await authApi.bindQQAccount(bindRequest);

    ElMessage.success("绑定成功");
    emit("success", response.data);
    bindDialogVisible.value = false;
    handleClose();
  } catch (error: any) {
    console.error("绑定QQ账号失败:", error);
    ElMessage.error(error.message || "绑定失败");
  } finally {
    bindLoading.value = false;
  }
};

// 创建新账号
const handleCreateNew = async () => {
  if (!newAccountFormRef.value || !loginData.value?.state) return;

  try {
    await newAccountFormRef.value.validate();
    bindLoading.value = true;

    const bindRequest: QQBindRequest = {
      state: loginData.value.state,
      createNew: true,
      newUsername: newAccountForm.newUsername,
      email: newAccountForm.email,
      phone: newAccountForm.phone,
      realName: newAccountForm.realName,
    };

    const response = await authApi.bindQQAccount(bindRequest);

    ElMessage.success("账号创建并绑定成功");
    emit("success", response.data);
    bindDialogVisible.value = false;
    handleClose();
  } catch (error: any) {
    console.error("创建并绑定QQ账号失败:", error);
    ElMessage.error(error.message || "创建账号失败");
  } finally {
    bindLoading.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  cleanup();
};

// 组件卸载时清理
onUnmounted(() => {
  cleanup();
});
</script>

<style lang="scss" scoped>
.qq-login-content {
  padding: 20px 0;
}

.mock-mode {
  text-align: center;

  .mode-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px;
    background: #f0f9ff;
    border: 1px solid #0ea5e9;
    border-radius: 8px;
    color: #0369a1;
    font-size: 14px;

    .el-icon {
      color: #0ea5e9;
    }
  }

  .mock-login-btn {
    width: 100%;
    height: 44px;
    background: #12b7f5;
    border-color: #12b7f5;
    font-size: 16px;

    &:hover {
      background: #0ea5e9;
      border-color: #0ea5e9;
    }
  }
}

.qrcode-mode {
  text-align: center;

  .qrcode-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;

    .qrcode-image {
      width: 200px;
      height: 200px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
    }
  }

  .qrcode-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #6b7280;
    font-size: 14px;
    margin-bottom: 12px;

    .el-icon {
      color: #12b7f5;
    }
  }

  .qrcode-status {
    .status-text {
      color: #12b7f5;
      font-size: 14px;
      font-weight: 500;
    }
  }
}

.bind-content {
  .qq-info {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #f8fafc;
    border-radius: 8px;
    margin-bottom: 20px;

    .info-text {
      flex: 1;

      .nickname {
        font-size: 16px;
        font-weight: 500;
        color: #1f2937;
        margin: 0 0 4px 0;
      }

      .tip {
        font-size: 14px;
        color: #6b7280;
        margin: 0;
      }
    }
  }

  .bind-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      background-color: #e5e7eb;
    }

    :deep(.el-tabs__active-bar) {
      background-color: #12b7f5;
    }

    :deep(.el-tabs__item.is-active) {
      color: #12b7f5;
    }
  }
}
</style>