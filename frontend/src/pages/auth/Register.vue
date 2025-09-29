<template>
  <div class="register-page">
    <div class="register-form">
      <div class="form-header">
        <h2>用户注册</h2>
        <p>创建您的档案管理系统账号</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        size="large"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号码"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <div class="verification-container">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              prefix-icon="Key"
              clearable
            />
            <el-button
              :disabled="countdown > 0"
              @click="sendVerificationCode"
              class="send-code-btn"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : "发送验证码" }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-link type="primary" @click="showUserAgreement">
              《用户协议》
            </el-link>
            和
            <el-link type="primary" @click="showPrivacyPolicy">
              《隐私政策》
            </el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="register-btn"
          >
            {{ loading ? "注册中..." : "立即注册" }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="login-link">
            已有账号？
            <el-link type="primary" @click="goToLogin"> 立即登录 </el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户协议弹窗 -->
    <el-dialog
      v-model="userAgreementVisible"
      title="用户协议"
      width="600px"
      :before-close="handleCloseAgreement"
    >
      <div class="agreement-content">
        <h3>1. 服务条款</h3>
        <p>欢迎使用档案管理系统。在使用本系统前，请仔细阅读本用户协议。</p>

        <h3>2. 用户责任</h3>
        <p>用户应当妥善保管账号和密码，不得将账号借给他人使用。</p>

        <h3>3. 数据安全</h3>
        <p>系统将采取合理的技术和管理措施保护用户数据安全。</p>

        <h3>4. 服务变更</h3>
        <p>系统有权根据业务需要对服务内容进行调整和变更。</p>
      </div>
      <template #footer>
        <el-button @click="userAgreementVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策弹窗 -->
    <el-dialog
      v-model="privacyPolicyVisible"
      title="隐私政策"
      width="600px"
      :before-close="handleClosePrivacy"
    >
      <div class="agreement-content">
        <h3>1. 信息收集</h3>
        <p>我们仅收集为提供服务所必需的用户信息。</p>

        <h3>2. 信息使用</h3>
        <p>收集的信息仅用于提供和改善服务，不会用于其他商业目的。</p>

        <h3>3. 信息保护</h3>
        <p>我们采用行业标准的安全措施保护用户隐私信息。</p>

        <h3>4. 信息共享</h3>
        <p>除法律要求外，我们不会与第三方共享用户个人信息。</p>
      </div>
      <template #footer>
        <el-button @click="privacyPolicyVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElForm } from "element-plus";
import { useAuthStore } from "@/stores/modules/auth";

const router = useRouter();
const authStore = useAuthStore();

// 表单引用
const registerFormRef = ref<InstanceType<typeof ElForm>>();

// 加载状态
const loading = ref(false);

// 验证码倒计时
const countdown = ref(0);

// 弹窗显示状态
const userAgreementVisible = ref(false);
const privacyPolicyVisible = ref(false);

// 注册表单数据
const registerForm = reactive({
  username: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
  verificationCode: "",
  agreement: false,
});

// 自定义验证规则
const validatePassword = (rule: any, value: string, callback: Function) => {
  if (value === "") {
    callback(new Error("请输入密码"));
  } else if (value.length < 6) {
    callback(new Error("密码长度不能少于6位"));
  } else if (
    !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/.test(value)
  ) {
    callback(new Error("密码必须包含大小写字母和数字"));
  } else {
    callback();
  }
};

const validateConfirmPassword = (
  rule: any,
  value: string,
  callback: Function,
) => {
  if (value === "") {
    callback(new Error("请确认密码"));
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const validateAgreement = (rule: any, value: boolean, callback: Function) => {
  if (!value) {
    callback(new Error("请阅读并同意用户协议和隐私政策"));
  } else {
    callback();
  }
};

// 表单验证规则
const registerRules = {
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
  ],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
  ],
  phone: [
    { required: true, message: "请输入手机号码", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: "blur",
    },
  ],
  password: [{ required: true, validator: validatePassword, trigger: "blur" }],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: "blur" },
  ],
  verificationCode: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 6, message: "验证码长度为 6 位", trigger: "blur" },
  ],
  agreement: [
    { required: true, validator: validateAgreement, trigger: "change" },
  ],
};

// 发送验证码
const sendVerificationCode = async () => {
  // 验证邮箱或手机号
  if (!registerForm.email && !registerForm.phone) {
    ElMessage.error("请先输入邮箱或手机号");
    return;
  }

  try {
    // 这里应该调用发送验证码的API
    // await authStore.sendVerificationCode({
    //   email: registerForm.email,
    //   phone: registerForm.phone
    // })

    ElMessage.success("验证码已发送");

    // 开始倒计时
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error: any) {
    ElMessage.error(error.message || "发送验证码失败");
  }
};

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return;

  try {
    await registerFormRef.value.validate();
    loading.value = true;

    const registerData = {
      username: registerForm.username,
      email: registerForm.email,
      phone: registerForm.phone,
      password: registerForm.password,
      verificationCode: registerForm.verificationCode,
    };

    await authStore.register(registerData);

    ElMessage.success("注册成功，请登录");
    router.push("/auth/login");
  } catch (error: any) {
    console.error("注册失败:", error);
    ElMessage.error(error.message || "注册失败，请重试");
  } finally {
    loading.value = false;
  }
};

// 跳转到登录页面
const goToLogin = () => {
  router.push("/auth/login");
};

// 显示用户协议
const showUserAgreement = () => {
  userAgreementVisible.value = true;
};

// 显示隐私政策
const showPrivacyPolicy = () => {
  privacyPolicyVisible.value = true;
};

// 关闭协议弹窗
const handleCloseAgreement = () => {
  userAgreementVisible.value = false;
};

// 关闭隐私政策弹窗
const handleClosePrivacy = () => {
  privacyPolicyVisible.value = false;
};
</script>

<style lang="scss" scoped>
@use "sass:color";

.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $spacing-lg;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.register-form {
  width: 100%;
  max-width: 420px;
  padding: $spacing-xl * 2;
  background: $bg-color;
  border-radius: $border-radius-large;
  box-shadow: $box-shadow-light;
}

.form-header {
  text-align: center;
  margin-bottom: $spacing-xl * 2;

  h2 {
    font-size: $font-size-extra-large;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: $spacing-sm;
  }

  p {
    color: $text-regular;
    font-size: $font-size-medium;
  }
}

.verification-container {
  display: flex;
  gap: $spacing-md;

  .el-input {
    flex: 1;
  }
}

.send-code-btn {
  white-space: nowrap;
  min-width: 120px;
  background: linear-gradient(
    135deg,
    $success-color 0%,
    color.adjust($success-color, $lightness: 10%) 100%
  );
  border: none;
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba($success-color, 0.3);
  }

  &:disabled {
    background: $border-light;
    color: $text-placeholder;
  }
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: $font-size-medium;
  font-weight: 500;
  background: linear-gradient(
    135deg,
    $primary-color 0%,
    color.adjust($primary-color, $lightness: 10%) 100%
  );
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);
  }
}

.login-link {
  text-align: center;
  color: $text-regular;
  font-size: $font-size-medium;
}

.agreement-content {
  max-height: 400px;
  overflow-y: auto;

  h3 {
    color: $text-primary;
    font-size: $font-size-medium;
    margin: $spacing-lg 0 $spacing-sm 0;

    &:first-child {
      margin-top: 0;
    }
  }

  p {
    color: $text-regular;
    line-height: 1.6;
    margin-bottom: $spacing-md;
  }
}

// 表单样式增强
:deep(.el-form-item) {
  margin-bottom: $spacing-lg;

  .el-input {
    .el-input__wrapper {
      height: 48px;
      border-radius: $border-radius-base;
      box-shadow: 0 0 0 1px $border-base inset;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 0 0 1px $primary-color inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px $primary-color inset;
      }
    }

    .el-input__inner {
      font-size: $font-size-medium;
    }
  }

  .el-form-item__error {
    font-size: $font-size-small;
  }

  .el-checkbox {
    .el-checkbox__label {
      font-size: $font-size-medium;
      color: $text-regular;
    }
  }
}

// 密码强度指示器
.password-strength {
  margin-top: $spacing-xs;

  .strength-bar {
    height: 4px;
    background: $border-light;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: $spacing-xs;

    .strength-fill {
      height: 100%;
      transition: all 0.3s ease;
      border-radius: 2px;

      &.weak {
        width: 33%;
        background: $danger-color;
      }

      &.medium {
        width: 66%;
        background: $warning-color;
      }

      &.strong {
        width: 100%;
        background: $success-color;
      }
    }
  }

  .strength-text {
    font-size: $font-size-small;

    &.weak {
      color: $danger-color;
    }

    &.medium {
      color: $warning-color;
    }

    &.strong {
      color: $success-color;
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .register-form {
    padding: $spacing-xl;
    max-width: 100%;
  }

  .verification-container {
    flex-direction: column;

    .send-code-btn {
      min-width: auto;
    }
  }
}

// 动画效果
.register-form {
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
