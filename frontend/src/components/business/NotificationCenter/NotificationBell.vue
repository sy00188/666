<template>
  <div class="notification-bell">
    <el-badge
      :value="displayCount"
      :hidden="unreadCount === 0"
      :max="99"
      class="notification-bell__badge"
    >
      <el-button
        :icon="Bell"
        circle
        @click="toggleDrawer"
      />
    </el-badge>

    <!-- 通知抽屉 -->
    <NotificationDrawer
      v-model="drawerVisible"
      @notification-click="handleNotificationClick"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { Bell } from '@element-plus/icons-vue';
import { useNotification } from '@/composables/useNotification';
import NotificationDrawer from './NotificationDrawer.vue';
import type { Notification } from '@/types/notification';

/**
 * 通知铃铛组件
 * 显示在顶部导航栏，显示未读通知数量
 */

// 使用通知composable
const {
  unreadCount,
  init,
  cleanup,
} = useNotification();

// 抽屉显示状态
const drawerVisible = ref(false);

// 显示的未读数量
const displayCount = computed(() => {
  return unreadCount.value > 0 ? unreadCount.value : undefined;
});

// 切换抽屉
const toggleDrawer = () => {
  drawerVisible.value = !drawerVisible.value;
};

// 处理通知点击
const handleNotificationClick = (notification: Notification) => {
  // 关闭抽屉
  drawerVisible.value = false;
  
  // 触发点击事件（由父组件处理路由跳转等）
  emit('notification-click', notification);
};

const emit = defineEmits<{
  'notification-click': [notification: Notification];
}>();

// 生命周期
onMounted(() => {
  init();
});

onUnmounted(() => {
  cleanup();
});
</script>

<style lang="scss" scoped>
.notification-bell {
  &__badge {
    :deep(.el-badge__content) {
      background-color: var(--el-color-danger);
      border: 2px solid var(--el-bg-color);
    }
  }
}
</style>

