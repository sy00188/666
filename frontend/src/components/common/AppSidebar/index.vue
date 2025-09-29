<template>
  <div class="app-sidebar">
    <!-- Logo区域 -->
    <div class="sidebar-logo" :class="{ collapsed: sidebarCollapsed }">
      <div class="logo-icon">
        <el-icon><Files /></el-icon>
      </div>
      <div v-show="!sidebarCollapsed" class="logo-text">
        <h3>档案管理</h3>
        <span>AMS</span>
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="sidebar-menu">
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :unique-opened="true"
        router
        background-color="transparent"
        text-color="#606266"
        active-text-color="#409eff"
      >
        <template v-for="item in menuList" :key="item.path">
          <!-- 单级菜单 -->
          <el-menu-item
            v-if="!item.children || item.children.length === 0"
            :index="item.path"
            :disabled="item.disabled"
          >
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>

          <!-- 多级菜单 -->
          <el-sub-menu v-else :index="item.path" :disabled="item.disabled">
            <template #title>
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </template>

            <template v-for="child in item.children" :key="child.path">
              <!-- 二级菜单 -->
              <el-menu-item
                v-if="!child.children || child.children.length === 0"
                :index="child.path"
                :disabled="child.disabled"
              >
                <el-icon>
                  <component :is="child.icon" />
                </el-icon>
                <template #title>{{ child.title }}</template>
              </el-menu-item>

              <!-- 三级菜单 -->
              <el-sub-menu
                v-else
                :index="child.path"
                :disabled="child.disabled"
              >
                <template #title>
                  <el-icon>
                    <component :is="child.icon" />
                  </el-icon>
                  <span>{{ child.title }}</span>
                </template>

                <el-menu-item
                  v-for="grandChild in child.children"
                  :key="grandChild.path"
                  :index="grandChild.path"
                  :disabled="grandChild.disabled"
                >
                  <el-icon>
                    <component :is="grandChild.icon" />
                  </el-icon>
                  <template #title>{{ grandChild.title }}</template>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>

    <!-- 底部信息 -->
    <div class="sidebar-footer" :class="{ collapsed: sidebarCollapsed }">
      <div v-show="!sidebarCollapsed" class="version-info">
        <span>版本 v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from "vue";
import { useRoute } from "vue-router";
import { Files } from "@element-plus/icons-vue";
import type { MenuItem } from "@/types/menu";

const route = useRoute();

// 侧边栏折叠状态
const sidebarCollapsed = inject("sidebarCollapsed", ref(false));

// 当前激活的菜单
const activeMenu = computed(() => route.path);

// 菜单列表
const menuList = ref<MenuItem[]>([
  {
    path: "/dashboard",
    title: "仪表板",
    icon: "HomeFilled",
    children: [],
  },
  {
    path: "/archive",
    title: "档案管理",
    icon: "Document",
    children: [
      {
        path: "/archive/list",
        title: "档案列表",
        icon: "List",
      },
      {
        path: "/archive/add",
        title: "新增档案",
        icon: "Plus",
      },
      {
        path: "/archive/category",
        title: "分类管理",
        icon: "Files",
      },
      {
        path: "/archive/search",
        title: "高级搜索",
        icon: "Search",
      },
    ],
  },
  {
    path: "/borrow",
    title: "借阅管理",
    icon: "Reading",
    children: [
      {
        path: "/borrow/list",
        title: "借阅记录",
        icon: "List",
      },
      {
        path: "/borrow/apply",
        title: "借阅申请",
        icon: "Plus",
      },
      {
        path: "/borrow/return",
        title: "归还管理",
        icon: "Download",
      },
      {
        path: "/borrow/overdue",
        title: "逾期管理",
        icon: "Calendar",
      },
    ],
  },
  {
    path: "/statistics",
    title: "统计分析",
    icon: "DataAnalysis",
    children: [
      {
        path: "/statistics/overview",
        title: "数据概览",
        icon: "View",
      },
      {
        path: "/statistics/archive",
        title: "档案统计",
        icon: "Document",
      },
      {
        path: "/statistics/borrow",
        title: "借阅统计",
        icon: "Reading",
      },
      {
        path: "/statistics/user",
        title: "用户统计",
        icon: "User",
      },
    ],
  },
  {
    path: "/system",
    title: "系统管理",
    icon: "Setting",
    children: [
      {
        path: "/system/user",
        title: "用户管理",
        icon: "User",
      },
      {
        path: "/system/role",
        title: "角色管理",
        icon: "Lock",
      },
      {
        path: "/system/permission",
        title: "权限管理",
        icon: "Lock",
      },
      {
        path: "/system/logs",
        title: "操作日志",
        icon: "List",
      },
      {
        path: "/system/settings",
        title: "系统设置",
        icon: "Setting",
      },
    ],
  },
]);
</script>

<style lang="scss" scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: $bg-color;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  border-bottom: 1px solid $border-lighter;
  transition: all 0.3s ease;

  &.collapsed {
    justify-content: center;
    padding: $spacing-lg $spacing-sm;
  }
}

.logo-icon {
  font-size: $font-size-extra-large;
  color: $primary-color;
  margin-right: $spacing-md;

  .collapsed & {
    margin-right: 0;
  }
}

.logo-text {
  h3 {
    margin: 0;
    font-size: $font-size-large;
    color: $text-primary;
    font-weight: 600;
  }

  span {
    font-size: $font-size-small;
    color: $text-placeholder;
  }
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-md 0;

  :deep(.el-menu) {
    border-right: none;

    .el-menu-item,
    .el-sub-menu__title {
      height: 48px;
      line-height: 48px;
      margin: 0 $spacing-md;
      border-radius: $border-radius-base;
      transition: all 0.3s ease;

      &:hover {
        background-color: $bg-color-page;
        color: $primary-color;
      }

      .el-icon {
        margin-right: $spacing-sm;
        font-size: $font-size-medium;
      }
    }

    .el-menu-item.is-active {
      background-color: rgba($primary-color, 0.1);
      color: $primary-color;
      font-weight: 500;

      &::before {
        content: "";
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        background-color: $primary-color;
        border-radius: 0 2px 2px 0;
      }
    }

    .el-sub-menu {
      .el-menu-item {
        padding-left: 60px !important;

        &.is-active {
          &::before {
            left: 20px;
          }
        }
      }

      .el-sub-menu {
        .el-menu-item {
          padding-left: 80px !important;

          &.is-active {
            &::before {
              left: 40px;
            }
          }
        }
      }
    }

    &.el-menu--collapse {
      .el-menu-item,
      .el-sub-menu__title {
        margin: 0 $spacing-sm;

        .el-icon {
          margin-right: 0;
        }
      }
    }
  }
}

.sidebar-footer {
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $border-lighter;
  text-align: center;

  &.collapsed {
    padding: $spacing-md $spacing-sm;
  }
}

.version-info {
  font-size: $font-size-small;
  color: $text-placeholder;
}

// 滚动条样式
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: $border-base;
  border-radius: 2px;

  &:hover {
    background: $border-light;
  }
}
</style>
