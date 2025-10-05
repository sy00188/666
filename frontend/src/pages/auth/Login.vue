<template>
  <div class="login-page">
    <div class="login-form">
      <!-- 头部图标和标题 -->
      <div class="form-header">
        <div class="logo-container">
          <div class="logo-icon">
            <el-icon :size="28">
              <Document />
            </el-icon>
          </div>
        </div>
        <h2 class="title">档案管理系统</h2>
        <p class="subtitle">请登录您的账户</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <!-- 用户名输入框 -->
        <el-form-item prop="username">
          <div class="input-label">
            <el-icon :size="16"><User /></el-icon>
            <span>用户名</span>
          </div>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>

        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <div class="input-label">
            <el-icon :size="16"><Lock /></el-icon>
            <span>密码</span>
          </div>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <!-- 验证码 -->
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

        <!-- 记住我和忘记密码 -->
        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe"> 记住我 </el-checkbox>
            <el-link type="primary" @click="goToForgotPassword">
              忘记密码？
            </el-link>
          </div>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            <el-icon class="login-icon">
              <Right />
            </el-icon>
            <span>登录</span>
          </el-button>
        </el-form-item>

        <!-- 分割线 -->
        <div class="divider"></div>

        <!-- 第三方登录 -->
        <div class="social-login">
          <el-button class="social-btn wechat-btn" @click="handleWechatLogin">
            <el-icon class="social-icon">
              <ChatDotRound />
            </el-icon>
            微信登录
          </el-button>
          <el-button class="social-btn qq-btn" @click="handleQQLogin">
            <el-icon class="social-icon">
              <User />
            </el-icon>
            QQ登录
          </el-button>
        </div>
      </el-form>

      <!-- 底部版权信息 -->
      <div class="footer">
        <p class="copyright">© 2024 档案管理系统. 咸阳市市双科.</p>
        <div class="status-indicators">
          <span class="status-dot online"></span>
          <span class="status-text">系统正常</span>
          <span class="status-dot connected"></span>
          <span class="status-text">数据库连接</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElForm } from "element-plus";
import { Document, Right, ChatDotRound, User, Lock } from "@element-plus/icons-vue";
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

    ElMessage.error(error.message || "登录失败，请重试");
  } finally {
    loading.value = false;
  }
};

// 处理微信登录
const handleWechatLogin = () => {
  ElMessage.info("微信登录功能开发中...");
};

// 处理QQ登录
const handleQQLogin = () => {
  ElMessage.info("QQ登录功能开发中...");
};

// 刷新验证码
const refreshCaptcha = () => {
  captchaUrl.value = `/api/auth/captcha?t=${Date.now()}`;
};

// 跳转到忘记密码页面
const goToForgotPassword = () => {
  ElMessage.info("忘记密码功能开发中...");
};

// 跳转到注册页面
const goToRegister = () => {
  router.push("/auth/register");
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
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-form {
  width: 100%;
  max-width: 450px;
  padding: 48px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-container {
  margin-bottom: 20px;

  .logo-icon {
    width: 64px;
    height: 64px;
    background: #4285f4;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      color: white;
    }
  }
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 16px 0 8px;
}

.subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.input-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  
  .el-icon {
    color: #666;
  }
}

.el-form-item {
  margin-bottom: 24px;
}

.el-input {
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    border: 1px solid #e0e0e0;
    box-shadow: none;
    padding: 0 16px;
    height: 48px;
    
    &:hover {
      border-color: #4285f4;
    }
    
    &.is-focus {
      border-color: #4285f4;
      box-shadow: 0 0 0 3px rgba(66, 133, 244, 0.1);
    }
  }
  
  :deep(.el-input__inner) {
    height: 46px;
    line-height: 46px;
    font-size: 14px;
  }
}

.captcha-container {
  display: flex;
  gap: 12px;

  .el-input {
    flex: 1;
  }
}

.captcha-image {
  position: relative;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
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
    font-size: 12px;
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
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
  font-size: 14px;

  :deep(.el-checkbox__label) {
    font-size: 14px;
    color: #666;
  }

  .el-link {
    font-size: 14px;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  background: #4285f4;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  &:hover {
    background: #3367d6;
  }
  
  :deep(.el-icon) {
    font-size: 16px;
  }
}

.divider {
  text-align: center;
  margin: 24px 0;
  position: relative;
  color: #999;
  font-size: 14px;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #e0e0e0;
    z-index: 1;
  }
  
  &::after {
    content: '或';
    background: white;
    padding: 0 16px;
    position: relative;
    z-index: 2;
  }
}

.social-login {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  
  .social-btn {
    flex: 1;
    height: 48px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    background: white;
    color: #666;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    
    &:hover {
      border-color: #4285f4;
      color: #4285f4;
    }
    
    :deep(.el-icon) {
      font-size: 18px;
    }
  }
}

.footer {
  text-align: center;
  font-size: 13px;
  color: #999;
  margin-top: 16px;
  
  .copyright {
    margin-bottom: 12px;
  }
  
  .status-indicators {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 6px;
    
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #d9d9d9;
      
      &.online {
        background: #52c41a;
      }
      
      &.connected {
        background: #4285f4;
      }
    }
    
    .status-text {
      font-size: 13px;
      margin-right: 12px;
      
      &:last-child {
        margin-right: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }
  
  .login-form {
    padding: 24px;
  }
  
  .captcha-container {
    flex-direction: column;

    .captcha-image {
      align-self: center;
    }
  }
  
  .social-login {
    flex-direction: column;
    
    .social-btn {
      width: 100%;
    }
  }
}
</style>
