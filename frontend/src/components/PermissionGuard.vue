<template>
  <div v-if="hasPermission">
    <slot />
  </div>
  <div v-else-if="showFallback" class="permission-denied">
    <slot name="fallback">
      <el-empty description="您没有权限访问此内容" />
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { usePermission } from "@/composables/usePermission";

interface Props {
  permission?: string | string[];
  role?: string | string[];
  requireAll?: boolean;
  showFallback?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  requireAll: false,
  showFallback: true,
});

const { hasPermission: checkPermission, hasRole } = usePermission();

const hasPermission = computed(() => {
  if (props.permission && props.role) {
    // 同时检查权限和角色
    const permissionCheck = Array.isArray(props.permission)
      ? props.requireAll
        ? props.permission.every((p) => checkPermission(p))
        : props.permission.some((p) => checkPermission(p))
      : checkPermission(props.permission);

    const roleCheck = Array.isArray(props.role)
      ? props.requireAll
        ? props.role.every((r) => hasRole(r))
        : props.role.some((r) => hasRole(r))
      : hasRole(props.role);

    return permissionCheck && roleCheck;
  }

  if (props.permission) {
    return Array.isArray(props.permission)
      ? props.requireAll
        ? props.permission.every((p) => checkPermission(p))
        : props.permission.some((p) => checkPermission(p))
      : checkPermission(props.permission);
  }

  if (props.role) {
    return Array.isArray(props.role)
      ? props.requireAll
        ? props.role.every((r) => hasRole(r))
        : props.role.some((r) => hasRole(r))
      : hasRole(props.role);
  }

  return true;
});
</script>

<style scoped>
.permission-denied {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  color: #909399;
}
</style>
