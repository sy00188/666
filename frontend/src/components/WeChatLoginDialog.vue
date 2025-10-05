<template>
  <el-dialog
    v-model="dialogVisible"
    title="微信登录"
    width="450px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="wechat-login-content">
      <!-- 模拟模式 -->
      <div v-if="loginData?.mockMode" class="mock-mode">
        <div class="mode-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>当前为开发模式，请输入测试微信ID</span>
        </div>

        <el-form
          ref="mockFormRef"
          :model="mockForm"
          :rules="mockRules"
          label-width="80px"
          style="margin-top: 20px"
        >
          <el-form-item label="微信ID" prop="mockWechatId">
            <el-input
              v-model="mockForm.mockWechatId"
              placeholder="请输入测试微信ID（如：test001）"
              clearable
            />
          </el-form-item>
          <el-form-item label="微信昵称" prop="mockNickname">
            <el-input
              v-model="mockForm.mockNickname"
              placeholder="请输入测试昵称（可选）"
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
          模拟登录
        </el-button>
      </div>

      <!-- 真实模式 - 二维码 -->
      <div v-else class="qrcode-mode">
        <div class="qrcode-container">
          <img
            v-if="loginData?.qrcodeUrl"
            :src="loginData.qrcodeUrl"
            alt="微信登录二维码"
            class="qrcode-image"
          />
          <el-skeleton v-else animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 200px; height: 200px" />
            </template>
          </el-skeleton>
        </div>
        <div class="qrcode-tip">
          <el-icon><Iphone /></el-icon>
          <span>请使用微信扫描二维码登录</span>
        </div>
        <div class="qrcode-status">
          <span v-if="statusText" class="status-text">{{ statusText }}</span>
        </div>
      </div>

      <!-- 绑定账号对话框 -->
      <el-dialog
        v-model="bindDialogVisible"
        title="绑定系统账号"
        width="400px"
        append-to-body
        :close-on-click-modal="false"
      >
        <div class="bind-content">
          <div class="wechat-info">
            <el-avatar :src="bindUserInfo?.avatar" :size="60">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="info-text">
              <p class="nickname">{{ bindUserInfo?.nickname }}</p>
              <p class="tip">首次使用微信登录，请绑定系统账号</p>
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
                style="width: 100%"
              >
                绑定并登录
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
                    placeholder="请输入手机号"
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
                style="width: 100%"
              >
                创建并登录
              </el-button>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onUnmounted } from "vue";
import { ElMessage, ElForm } from "element-plus";
import { InfoFilled, Iphone, User } from "@element-plus/icons-vue";
import { authApi } from "@/api/auth";
import type {
  WeChatLoginResponse,
  WeChatLoginRequest,
  WeChatBindRequest,
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

// 对话框显示状态
const dialogVisible = ref(false);
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val;
    if (val) {
      initWeChatLogin();
    } else {
      cleanup();
    }
  }
);

watch(dialogVisible, (val) => {
  emit("update:visible", val);
});

// 登录数据
const loginData = ref<WeChatLoginResponse>();
const loading = ref(false);
const statusText = ref("");

// 模拟登录表单
const mockFormRef = ref<InstanceType<typeof ElForm>>();
const mockForm = reactive({
  mockWechatId: "",
  mockNickname: "",
});

const mockRules = {
  mockWechatId: [
    { required: true, message: "请输入测试微信ID", trigger: "blur" },
  ],
};

// 绑定对话框
const bindDialogVisible = ref(false);
const bindTab = ref("existing");
const bindUserInfo = ref<any>();
const bindLoading = ref(false);

// 绑定现有账号表单
const bindFormRef = ref<InstanceType<typeof ElForm>>();
const bindForm = reactive({
  username: "",
  password: "",
});

const bindRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
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
    { min: 3, max: 20, message: "用户名长度在3到20个字符", trigger: "blur" },
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" },
  ],
};

// 轮询定时器
let pollTimer: number | null = null;

/**
 * 初始化微信登录
 */
const initWeChatLogin = async () => {
  try {
    loading.value = true;
    const response = await authApi.getWeChatQRCode();

    if (response.data) {
      loginData.value = response.data;
      statusText.value = response.data.message;

      // 如果不是模拟模式，开始轮询登录状态
      if (!response.data.mockMode) {
        startPolling();
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || "获取微信登录信息失败");
    handleClose();
  } finally {
    loading.value = false;
  }
};

/**
 * 开始轮询登录状态
 */
const startPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
  }

  pollTimer = window.setInterval(async () => {
    if (!loginData.value?.state) return;

    try {
      const response = await authApi.checkWeChatLoginStatus(loginData.value.state);

      if (response.data) {
        const status = response.data.status;
        statusText.value = response.data.message || "";

        if (status === "success") {
          // 登录成功
          stopPolling();
          ElMessage.success("登录成功");
          emit("success", response.data.loginResponse);
          handleClose();
        } else if (status === "binding_required") {
          // 需要绑定账号
          stopPolling();
          bindUserInfo.value = {
            openid: response.data.openid,
            nickname: response.data.nickname,
            avatar: response.data.avatar,
          };
          bindDialogVisible.value = true;
        } else if (status === "expired") {
          // 二维码过期
          stopPolling();
          ElMessage.warning("二维码已过期，请重新获取");
          handleClose();
        } else if (status === "error") {
          // 登录失败
          stopPolling();
          ElMessage.error(response.data.message || "登录失败");
          handleClose();
        }
        // pending状态继续轮询
      }
    } catch (error) {
      console.error("轮询登录状态失败:", error);
    }
  }, 2000); // 每2秒轮询一次
};

/**
 * 停止轮询
 */
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
};

/**
 * 处理模拟登录
 */
const handleMockLogin = async () => {
  if (!mockFormRef.value) return;

  try {
    await mockFormRef.value.validate();
    loading.value = true;

    const requestData: WeChatLoginRequest = {
      state: loginData.value!.state,
      mockWechatId: mockForm.mockWechatId,
      mockNickname: mockForm.mockNickname || undefined,
    };

    const response = await authApi.mockWeChatLogin(requestData);

    if (response.code === 200) {
      // 登录成功
      ElMessage.success("登录成功");
      emit("success", response.data);
      handleClose();
    } else if (response.code === 202) {
      // 需要绑定账号
      bindUserInfo.value = response.data;
      bindDialogVisible.value = true;
    }
  } catch (error: any) {
    ElMessage.error(error.message || "登录失败");
  } finally {
    loading.value = false;
  }
};

/**
 * 绑定现有账号
 */
const handleBindExisting = async () => {
  if (!bindFormRef.value) return;

  try {
    await bindFormRef.value.validate();
    bindLoading.value = true;

    const requestData: WeChatBindRequest = {
      state: loginData.value!.state,
      username: bindForm.username,
      password: bindForm.password,
      createNew: false,
    };

    const response = await authApi.bindWeChatAccount(requestData);

    if (response.code === 200) {
      ElMessage.success("绑定成功");
      emit("success", response.data);
      bindDialogVisible.value = false;
      handleClose();
    }
  } catch (error: any) {
    ElMessage.error(error.message || "绑定失败");
  } finally {
    bindLoading.value = false;
  }
};

/**
 * 创建新账号
 */
const handleCreateNew = async () => {
  if (!newAccountFormRef.value) return;

  try {
    await newAccountFormRef.value.validate();
    bindLoading.value = true;

    const requestData: WeChatBindRequest = {
      state: loginData.value!.state,
      createNew: true,
      newUsername: newAccountForm.newUsername,
      email: newAccountForm.email,
      phone: newAccountForm.phone,
      realName: newAccountForm.realName || undefined,
    };

    const response = await authApi.bindWeChatAccount(requestData);

    if (response.code === 200) {
      ElMessage.success("创建成功");
      emit("success", response.data);
      bindDialogVisible.value = false;
      handleClose();
    }
  } catch (error: any) {
    ElMessage.error(error.message || "创建失败");
  } finally {
    bindLoading.value = false;
  }
};

/**
 * 关闭对话框
 */
const handleClose = () => {
  cleanup();
  dialogVisible.value = false;
};

/**
 * 清理资源
 */
const cleanup = () => {
  stopPolling();
  mockForm.mockWechatId = "";
  mockForm.mockNickname = "";
  bindForm.username = "";
  bindForm.password = "";
  newAccountForm.newUsername = "";
  newAccountForm.email = "";
  newAccountForm.phone = "";
  newAccountForm.realName = "";
  bindDialogVisible.value = false;
  bindTab.value = "existing";
  statusText.value = "";
};

// 组件卸载时清理
onUnmounted(() => {
  cleanup();
});
</script>

<style lang="scss" scoped>
.wechat-login-content {
  padding: 20px 0;
}

.mock-mode {
  .mode-tip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 4px;
    color: #409eff;
    font-size: 14px;

    .el-icon {
      font-size: 18px;
    }
  }

  .mock-login-btn {
    width: 100%;
    margin-top: 10px;
  }
}

.qrcode-mode {
  display: flex;
  flex-direction: column;
  align-items: center;

  .qrcode-container {
    width: 200px;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    margin-bottom: 20px;

    .qrcode-image {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }

  .qrcode-tip {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #666;
    font-size: 14px;
    margin-bottom: 12px;

    .el-icon {
      font-size: 20px;
      color: #409eff;
    }
  }

  .qrcode-status {
    min-height: 24px;

    .status-text {
      font-size: 13px;
      color: #999;
    }
  }
}

.bind-content {
  .wechat-info {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 20px;

    .info-text {
      flex: 1;

      .nickname {
        font-size: 16px;
        font-weight: 500;
        color: #333;
        margin: 0 0 8px 0;
      }

      .tip {
        font-size: 13px;
        color: #999;
        margin: 0;
      }
    }
  }

  .bind-tabs {
    :deep(.el-tabs__content) {
      padding: 20px 0;
    }
  }
}
</style>

