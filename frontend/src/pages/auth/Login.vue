<template>
  <div class="login-page">
    <div class="login-form">
      <div class="form-header">
        <h2>用户登录</h2>
        <p>欢迎使用档案管理系统</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="captcha" v-if="showCaptcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              clearable
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaUrl" alt="验证码" />
              <div class="refresh-tip">点击刷新</div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe"> 记住我 </el-checkbox>
            <el-link type="primary" @click="goToForgotPassword">
              忘记密码？
            </el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            {{ loading ? "登录中..." : "登录" }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="register-link">
            还没有账号？
            <el-link type="primary" @click="goToRegister"> 立即注册 </el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElForm } from "element-plus";
import { useAuthStore } from "@/stores";

const router = useRouter();
const authStore = useAuthStore();

// 表单引用
const loginFormRef = ref<InstanceType<typeof ElForm>>();

// 加载状态
const loading = ref(false);

// 是否显示验证码
const showCaptcha = ref(false);

// 验证码URL
const captchaUrl = ref("");

// 登录表单数据
const loginForm = reactive({
  username: "",
  password: "",
  captcha: "",
  rememberMe: false,
});

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 3,
      max: 20,
      message: "用户名长度在 3 到 20 个字符",
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur" },
  ],
  captcha: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 4, message: "验证码长度为 4 位", trigger: "blur" },
  ],
};

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return;

  try {
    await loginFormRef.value.validate();
    loading.value = true;

    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      captcha: showCaptcha.value ? loginForm.captcha : undefined,
      rememberMe: loginForm.rememberMe,
    };

    const success = await authStore.login(loginData);

    if (success) {
      ElMessage.success("登录成功");
      // 跳转到首页或之前访问的页面
      const redirect = router.currentRoute.value.query.redirect as string;
      router.push(redirect || "/dashboard");
    }
  } catch (error: any) {
    console.error("登录失败:", error);

    // 如果是验证码错误，显示验证码
    if (error.code === "CAPTCHA_REQUIRED") {
      showCaptcha.value = true;
      refreshCaptcha();
    }

    ElMessage.error(error.message || "登录失败，请检查用户名和密码");
  } finally {
    loading.value = false;
  }
};

// 刷新验证码
const refreshCaptcha = () => {
  captchaUrl.value = `/api/auth/captcha?t=${Date.now()}`;
};

// 跳转到注册页面
const goToRegister = () => {
  router.push("/auth/register");
};

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  router.push("/auth/forgot-password");
};

// 组件挂载时的操作
onMounted(() => {
  // 如果已经登录，直接跳转到首页
  if (authStore.isAuthenticated) {
    router.push("/dashboard");
    return;
  }

  // 检查是否需要显示验证码
  const failedAttempts = localStorage.getItem("loginFailedAttempts");
  if (failedAttempts && parseInt(failedAttempts) >= 3) {
    showCaptcha.value = true;
    refreshCaptcha();
  }
});
</script>

<style lang="scss" scoped>
@use "sass:color";

.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $spacing-lg;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-form {
  width: 100%;
  max-width: 400px;
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

.captcha-container {
  display: flex;
  gap: $spacing-md;

  .el-input {
    flex: 1;
  }
}

.captcha-image {
  position: relative;
  cursor: pointer;
  border: 1px solid $border-base;
  border-radius: $border-radius-base;
  overflow: hidden;

  img {
    display: block;
    width: 100px;
    height: 40px;
    object-fit: cover;
  }

  .refresh-tip {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    font-size: $font-size-small;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .refresh-tip {
    opacity: 1;
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.login-btn {
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

.register-link {
  text-align: center;
  color: $text-regular;
  font-size: $font-size-medium;
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
}

// 响应式设计
@media (max-width: 480px) {
  .login-form {
    padding: $spacing-xl;
  }

  .captcha-container {
    flex-direction: column;

    .captcha-image {
      align-self: center;
    }
  }
}
</style>
