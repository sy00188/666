<template>
  <div class="main-layout" :class="{ 'is-mobile': isMobile }">
    <!-- 侧边栏 -->
    <app-sidebar class="layout-sidebar" />

    <!-- 主内容区域 -->
    <div class="layout-main" :class="{ 'sidebar-collapsed': !sidebarOpened }">
      <!-- 头部 -->
      <app-header class="layout-header" />

      <!-- 页面内容 -->
      <div class="layout-content">
        <el-scrollbar class="content-scrollbar">
          <div class="content-wrapper">
            <!-- 页面标签栏 -->
            <page-tabs v-if="showPageTabs" class="page-tabs" />

            <!-- 路由视图 -->
            <div class="router-view-container">
              <router-view v-slot="{ Component, route }">
                <transition :name="transitionName" mode="out-in" appear>
                  <keep-alive :include="cachedViews">
                    <component :is="Component" :key="route.fullPath" />
                  </keep-alive>
                </transition>
              </router-view>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- 移动端遮罩层 -->
    <div
      v-if="isMobile && sidebarOpened"
      class="mobile-overlay"
      @click="closeSidebar"
    />

    <!-- 回到顶部按钮 -->
    <el-backtop
      :right="40"
      :bottom="40"
      :visibility-height="300"
      target=".content-scrollbar .el-scrollbar__wrap"
    >
      <div class="backtop-button">
        <el-icon><Top /></el-icon>
      </div>
    </el-backtop>

    <!-- 全局加载遮罩 -->
    <div v-if="globalLoading" class="global-loading">
      <el-loading
        :visible="true"
        text="加载中..."
        background="rgba(0, 0, 0, 0.7)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { Top } from "@element-plus/icons-vue";
import { useAppStore } from "@/stores/app";
import AppHeader from "./AppHeader.vue";
import AppSidebar from "./AppSidebar.vue";
import PageTabs from "@/components/Common/PageTabs.vue";

const appStore = useAppStore();

// 响应式数据
const isMobile = ref(false);
const globalLoading = ref(false);

// 计算属性
const sidebarOpened = computed(() => appStore.sidebarOpened);
const showPageTabs = computed(() => appStore.showPageTabs);
const cachedViews = computed(() => appStore.cachedViews);
const transitionName = computed(() => {
  return appStore.pageTransition || "fade";
});

// 方法
const closeSidebar = () => {
  if (isMobile.value) {
    appStore.closeSidebar();
  }
};

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768;

  // 移动端默认收起侧边栏
  if (isMobile.value && sidebarOpened.value) {
    appStore.closeSidebar();
  }
};

const handleResize = () => {
  checkMobile();
};

// 生命周期
onMounted(() => {
  checkMobile();
  window.addEventListener("resize", handleResize);

  // 初始化应用设置
  appStore.initializeApp();
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
});

// 暴露方法给父组件
defineExpose({
  setGlobalLoading: (loading: boolean) => {
    globalLoading.value = loading;
  },
});
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;

  .layout-sidebar {
    flex-shrink: 0;
    z-index: 100;
  }

  .layout-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    transition: margin-left 0.3s ease;

    &.sidebar-collapsed {
      margin-left: 0;
    }

    .layout-header {
      flex-shrink: 0;
      z-index: 99;
    }

    .layout-content {
      flex: 1;
      overflow: hidden;
      background: var(--el-bg-color-page);

      .content-scrollbar {
        height: 100%;

        :deep(.el-scrollbar__wrap) {
          overflow-x: hidden;
        }
      }

      .content-wrapper {
        min-height: 100%;
        display: flex;
        flex-direction: column;

        .page-tabs {
          flex-shrink: 0;
        }

        .router-view-container {
          flex: 1;
          padding: 20px;
          min-height: calc(100vh - 140px); // 减去头部和标签栏高度
        }
      }
    }
  }

  // 移动端样式
  &.is-mobile {
    .layout-sidebar {
      position: fixed;
      left: 0;
      top: 0;
      height: 100vh;
      z-index: 1001;
    }

    .layout-main {
      width: 100%;
      margin-left: 0 !important;

      .router-view-container {
        padding: 16px;
      }
    }
  }

  .mobile-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    z-index: 1000;
  }

  .backtop-button {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--el-color-primary);
    color: white;
    border-radius: 50%;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;

    &:hover {
      background: var(--el-color-primary-dark-2);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }
  }

  .global-loading {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
  }
}

// 页面过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.slide-left-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.zoom-enter-active,
.zoom-leave-active {
  transition: all 0.3s ease;
}

.zoom-enter-from,
.zoom-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

// 响应式设计
@media (max-width: 1200px) {
  .main-layout
    .layout-main
    .layout-content
    .content-wrapper
    .router-view-container {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .main-layout
    .layout-main
    .layout-content
    .content-wrapper
    .router-view-container {
    padding: 12px;
    min-height: calc(100vh - 120px);
  }
}
</style>
