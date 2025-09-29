<template>
  <div class="default-layout">
    <!-- 头部 -->
    <div class="layout-header">
      <AppHeader />
    </div>

    <!-- 主体内容 -->
    <div class="layout-body">
      <!-- 侧边栏 -->
      <div class="layout-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <AppSidebar />
      </div>

      <!-- 主内容区 -->
      <div class="layout-main">
        <div class="main-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import AppHeader from "@/components/common/AppHeader/index.vue";
import AppSidebar from "@/components/common/AppSidebar/index.vue";

// 侧边栏折叠状态
const sidebarCollapsed = ref(false);

// 提供给子组件使用
provide("sidebarCollapsed", sidebarCollapsed);
</script>

<style lang="scss" scoped>
.default-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $bg-color-page;
}

.layout-header {
  height: $header-height;
  background-color: $bg-color;
  border-bottom: 1px solid $border-base;
  box-shadow: $box-shadow-base;
  z-index: $z-index-top;
}

.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.layout-sidebar {
  width: $sidebar-width;
  background-color: $bg-color;
  border-right: 1px solid $border-base;
  transition: width 0.3s ease;
  overflow: hidden;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-content {
  flex: 1;
  padding: $spacing-lg;
  overflow-y: auto;
  background-color: $bg-color-page;
}

// 响应式设计
@media (max-width: 768px) {
  .layout-sidebar {
    position: fixed;
    left: 0;
    top: $header-height;
    height: calc(100vh - #{$header-height});
    z-index: $z-index-top;
    transform: translateX(-100%);
    transition: transform 0.3s ease;

    &:not(.collapsed) {
      transform: translateX(0);
    }
  }

  .layout-main {
    margin-left: 0;
  }

  .main-content {
    padding: $spacing-md;
  }
}
</style>
