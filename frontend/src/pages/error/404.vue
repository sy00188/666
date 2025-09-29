<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-icon">
        <el-icon :size="120" color="#e6a23c">
          <QuestionFilled />
        </el-icon>
      </div>
      <h1 class="error-title">404</h1>
      <p class="error-message">页面不存在</p>
      <p class="error-description">
        抱歉，您访问的页面不存在或已被删除<br />
        请检查网址是否正确，或返回首页继续浏览
      </p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { QuestionFilled, HomeFilled, ArrowLeft } from "@element-plus/icons-vue";

const router = useRouter();

const goHome = () => {
  router.push("/dashboard");
};

const goBack = () => {
  router.go(-1);
};
</script>

<style lang="scss" scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-icon {
  margin-bottom: 30px;
}

.error-title {
  font-size: 72px;
  font-weight: bold;
  color: #e6a23c;
  margin: 0 0 20px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-message {
  font-size: 24px;
  color: #303133;
  margin: 0 0 15px 0;
  font-weight: 500;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin: 0 0 40px 0;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;

  .el-button {
    padding: 12px 24px;
    font-size: 16px;
    border-radius: 8px;

    .el-icon {
      margin-right: 8px;
    }
  }
}

@media (max-width: 768px) {
  .error-content {
    padding: 40px 20px;
  }

  .error-title {
    font-size: 60px;
  }

  .error-message {
    font-size: 20px;
  }

  .error-actions {
    flex-direction: column;
    align-items: center;

    .el-button {
      width: 200px;
    }
  }
}
</style>
