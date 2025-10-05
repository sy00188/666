<template>
  <div class="auth-layout">
    <div class="auth-container">
      <!-- 右侧表单区域 -->
      <div class="auth-form">
        <div class="form-container">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document, Lock, DataAnalysis } from "@element-plus/icons-vue";
</script>

<style lang="scss" scoped>
.auth-layout {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  margin: 0;
  position: fixed;
  top: 0;
  left: 0;
}

.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 100%;
  min-height: auto;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  overflow: visible;
}

.auth-background {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #36cfc9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="10" cy="50" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="90" cy="30" r="0.5" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
    opacity: 0.3;
  }
}

.background-content {
  text-align: center;
  z-index: 1;
  position: relative;
}

.logo {
  margin-bottom: $spacing-xl * 2;

  h1 {
    font-size: $font-size-extra-large * 2;
    font-weight: 700;
    margin-bottom: $spacing-sm;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  p {
    font-size: $font-size-medium;
    opacity: 0.9;
    font-weight: 300;
  }
}

.features {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  font-size: $font-size-medium;

  .el-icon {
    font-size: $font-size-large;
    opacity: 0.9;
  }
}

.auth-form {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.form-container {
  width: 100%;
  max-width: 100%;
}

// 响应式设计
@media (max-width: 768px) {
  .auth-layout {
    padding: $spacing-md;
  }

  .auth-container {
    min-height: auto;
    margin: 0;
    border-radius: $border-radius-large;
    max-width: 100%;
  }

  .auth-form {
    padding: $spacing-xl;
  }
}

@media (max-width: 480px) {
  .auth-container {
    margin: 0;
  }

  .auth-form {
    padding: $spacing-lg;
  }

  .form-container {
    max-width: none;
  }
}
</style>
