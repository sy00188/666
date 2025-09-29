<template>
  <div class="page-tabs">
    <div class="tabs-container">
      <!-- 左侧滚动按钮 -->
      <div
        v-show="showScrollButtons"
        class="scroll-button left"
        @click="scrollLeft"
      >
        <el-icon><ArrowLeft /></el-icon>
      </div>

      <!-- 标签滚动区域 -->
      <div ref="tabsWrapRef" class="tabs-wrap" @wheel="handleWheel">
        <div ref="tabsNavRef" class="tabs-nav">
          <div
            v-for="tab in tabs"
            :key="tab.path"
            class="tab-item"
            :class="{
              'is-active': tab.path === activeTab,
              'is-affix': tab.affix,
            }"
            @click="handleTabClick(tab)"
            @contextmenu.prevent="handleContextMenu($event, tab)"
          >
            <el-icon v-if="tab.icon" class="tab-icon">
              <component :is="tab.icon" />
            </el-icon>
            <span class="tab-title">{{ tab.title }}</span>
            <el-icon
              v-if="!tab.affix"
              class="tab-close"
              @click.stop="handleTabClose(tab)"
            >
              <Close />
            </el-icon>
          </div>
        </div>
      </div>

      <!-- 右侧滚动按钮 -->
      <div
        v-show="showScrollButtons"
        class="scroll-button right"
        @click="scrollRight"
      >
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 操作按钮 -->
      <div class="tabs-actions">
        <el-dropdown @command="handleActionCommand">
          <el-button type="text" class="action-button">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="refresh">
                <el-icon><Refresh /></el-icon>
                刷新当前页
              </el-dropdown-item>
              <el-dropdown-item command="closeOthers">
                <el-icon><Close /></el-icon>
                关闭其他
              </el-dropdown-item>
              <el-dropdown-item command="closeLeft">
                <el-icon><Back /></el-icon>
                关闭左侧
              </el-dropdown-item>
              <el-dropdown-item command="closeRight">
                <el-icon><Right /></el-icon>
                关闭右侧
              </el-dropdown-item>
              <el-dropdown-item command="closeAll">
                <el-icon><CircleClose /></el-icon>
                关闭所有
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-show="contextMenuVisible"
      ref="contextMenuRef"
      class="context-menu"
      :style="contextMenuStyle"
    >
      <div class="menu-item" @click="refreshTab">
        <el-icon><Refresh /></el-icon>
        刷新
      </div>
      <div v-if="!contextTab?.affix" class="menu-item" @click="closeTab">
        <el-icon><Close /></el-icon>
        关闭
      </div>
      <div class="menu-item" @click="closeOthers">
        <el-icon><Close /></el-icon>
        关闭其他
      </div>
      <div class="menu-item" @click="closeLeft">
        <el-icon><Back /></el-icon>
        关闭左侧
      </div>
      <div class="menu-item" @click="closeRight">
        <el-icon><Right /></el-icon>
        关闭右侧
      </div>
      <div class="menu-item" @click="closeAll">
        <el-icon><CircleClose /></el-icon>
        关闭所有
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  ArrowLeft,
  ArrowRight,
  Close,
  MoreFilled,
  Refresh,
  Back,
  Right,
  CircleClose,
} from "@element-plus/icons-vue";
import { useAppStore } from "@/stores/app";

// 定义标签接口
interface Tab {
  path: string;
  title: string;
  icon?: string;
  affix?: boolean;
  closable?: boolean;
}

const router = useRouter();
const route = useRoute();
const appStore = useAppStore();

// 响应式数据
const tabsWrapRef = ref<HTMLElement>();
const tabsNavRef = ref<HTMLElement>();
const contextMenuRef = ref<HTMLElement>();
const showScrollButtons = ref(false);
const contextMenuVisible = ref(false);
const contextTab = ref<Tab | null>(null);
const contextMenuStyle = ref({});

// 模拟标签数据
const tabs = ref<Tab[]>([
  {
    path: "/dashboard",
    title: "仪表盘",
    icon: "Odometer",
    affix: true,
  },
]);

// 计算属性
const activeTab = computed(() => route.path);

// 方法
const handleTabClick = (tab: Tab) => {
  if (tab.path !== route.path) {
    router.push(tab.path);
  }
};

const handleTabClose = (tab: Tab) => {
  if (tab.affix) return;

  const index = tabs.value.findIndex((t) => t.path === tab.path);
  if (index === -1) return;

  tabs.value.splice(index, 1);

  // 如果关闭的是当前标签，跳转到相邻标签
  if (tab.path === route.path) {
    const nextTab = tabs.value[index] || tabs.value[index - 1];
    if (nextTab) {
      router.push(nextTab.path);
    }
  }
};

const handleContextMenu = (event: MouseEvent, tab: Tab) => {
  event.preventDefault();
  contextTab.value = tab;
  contextMenuVisible.value = true;

  nextTick(() => {
    const { clientX, clientY } = event;
    contextMenuStyle.value = {
      left: `${clientX}px`,
      top: `${clientY}px`,
    };
  });
};

const handleActionCommand = (command: string) => {
  switch (command) {
    case "refresh":
      refreshCurrentTab();
      break;
    case "closeOthers":
      closeOthers();
      break;
    case "closeLeft":
      closeLeft();
      break;
    case "closeRight":
      closeRight();
      break;
    case "closeAll":
      closeAll();
      break;
  }
};

const refreshCurrentTab = () => {
  // 刷新当前页面
  router.go(0);
};

const refreshTab = () => {
  if (contextTab.value) {
    if (contextTab.value.path === route.path) {
      refreshCurrentTab();
    } else {
      router.push(contextTab.value.path).then(() => {
        router.go(0);
      });
    }
  }
  hideContextMenu();
};

const closeTab = () => {
  if (contextTab.value) {
    handleTabClose(contextTab.value);
  }
  hideContextMenu();
};

const closeOthers = () => {
  const currentPath = contextTab.value?.path || route.path;
  tabs.value = tabs.value.filter(
    (tab) => tab.affix || tab.path === currentPath,
  );
  hideContextMenu();
};

const closeLeft = () => {
  const currentPath = contextTab.value?.path || route.path;
  const currentIndex = tabs.value.findIndex((tab) => tab.path === currentPath);
  if (currentIndex > 0) {
    tabs.value = tabs.value.filter(
      (tab, index) => tab.affix || index >= currentIndex,
    );
  }
  hideContextMenu();
};

const closeRight = () => {
  const currentPath = contextTab.value?.path || route.path;
  const currentIndex = tabs.value.findIndex((tab) => tab.path === currentPath);
  if (currentIndex < tabs.value.length - 1) {
    tabs.value = tabs.value.filter(
      (tab, index) => tab.affix || index <= currentIndex,
    );
  }
  hideContextMenu();
};

const closeAll = () => {
  tabs.value = tabs.value.filter((tab) => tab.affix);
  const firstTab = tabs.value[0];
  if (firstTab && firstTab.path !== route.path) {
    router.push(firstTab.path);
  }
  hideContextMenu();
};

const hideContextMenu = () => {
  contextMenuVisible.value = false;
  contextTab.value = null;
};

const scrollLeft = () => {
  if (tabsWrapRef.value) {
    tabsWrapRef.value.scrollLeft -= 200;
  }
};

const scrollRight = () => {
  if (tabsWrapRef.value) {
    tabsWrapRef.value.scrollLeft += 200;
  }
};

const handleWheel = (event: WheelEvent) => {
  if (tabsWrapRef.value) {
    event.preventDefault();
    tabsWrapRef.value.scrollLeft += event.deltaY;
  }
};

const checkScrollButtons = () => {
  if (tabsWrapRef.value && tabsNavRef.value) {
    const wrapWidth = tabsWrapRef.value.clientWidth;
    const navWidth = tabsNavRef.value.scrollWidth;
    showScrollButtons.value = navWidth > wrapWidth;
  }
};

const handleClickOutside = (event: Event) => {
  if (
    contextMenuRef.value &&
    !contextMenuRef.value.contains(event.target as Node)
  ) {
    hideContextMenu();
  }
};

// 添加新标签
const addTab = (tab: Tab) => {
  const existingTab = tabs.value.find((t) => t.path === tab.path);
  if (!existingTab) {
    tabs.value.push(tab);
    nextTick(() => {
      checkScrollButtons();
    });
  }
};

// 生命周期
onMounted(() => {
  checkScrollButtons();
  window.addEventListener("resize", checkScrollButtons);
  document.addEventListener("click", handleClickOutside);

  // 监听路由变化，自动添加标签
  // 这里可以根据路由配置自动添加标签
});

onUnmounted(() => {
  window.removeEventListener("resize", checkScrollButtons);
  document.removeEventListener("click", handleClickOutside);
});

// 暴露方法
defineExpose({
  addTab,
});
</script>

<style lang="scss" scoped>
.page-tabs {
  height: 40px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);

  .tabs-container {
    display: flex;
    align-items: center;
    height: 100%;

    .scroll-button {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--el-text-color-regular);
      transition: color 0.2s;

      &:hover {
        color: var(--el-color-primary);
      }

      &.left {
        margin-left: 8px;
      }

      &.right {
        margin-right: 8px;
      }
    }

    .tabs-wrap {
      flex: 1;
      overflow: hidden;
      scroll-behavior: smooth;

      .tabs-nav {
        display: flex;
        height: 40px;
        white-space: nowrap;

        .tab-item {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 0 16px;
          height: 32px;
          margin: 4px 2px;
          background: var(--el-bg-color-page);
          border: 1px solid var(--el-border-color-lighter);
          border-radius: 4px;
          cursor: pointer;
          transition: all 0.2s;
          flex-shrink: 0;

          &:hover {
            background: var(--el-color-primary-light-9);
            border-color: var(--el-color-primary-light-7);
          }

          &.is-active {
            background: var(--el-color-primary);
            border-color: var(--el-color-primary);
            color: white;

            .tab-close {
              color: white;

              &:hover {
                background: rgba(255, 255, 255, 0.2);
              }
            }
          }

          &.is-affix .tab-close {
            display: none;
          }

          .tab-icon {
            font-size: 14px;
          }

          .tab-title {
            font-size: 13px;
            max-width: 120px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .tab-close {
            width: 16px;
            height: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 2px;
            font-size: 12px;
            color: var(--el-text-color-placeholder);
            transition: all 0.2s;

            &:hover {
              background: var(--el-color-danger-light-9);
              color: var(--el-color-danger);
            }
          }
        }
      }
    }

    .tabs-actions {
      padding: 0 8px;

      .action-button {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--el-text-color-regular);

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .context-menu {
    position: fixed;
    z-index: 9999;
    background: var(--el-bg-color-overlay);
    border: 1px solid var(--el-border-color-light);
    border-radius: 6px;
    box-shadow: var(--el-box-shadow);
    padding: 4px 0;
    min-width: 120px;

    .menu-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      font-size: 13px;
      color: var(--el-text-color-primary);
      cursor: pointer;
      transition: background-color 0.2s;

      &:hover {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      .el-icon {
        font-size: 14px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .page-tabs {
    .tabs-container {
      .tabs-wrap .tabs-nav .tab-item {
        padding: 0 12px;

        .tab-title {
          max-width: 80px;
        }
      }
    }
  }
}
</style>
