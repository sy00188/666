<template>
  <div class="drill-down-breadcrumb" v-if="drillPath.levels.length > 0">
    <div class="breadcrumb-container">
      <div class="breadcrumb-items">
        <div
          v-for="(item, index) in breadcrumbItems"
          :key="index"
          class="breadcrumb-item"
          :class="{
            'is-active': index === breadcrumbItems.length - 1,
            'is-clickable': index < breadcrumbItems.length - 1,
          }"
          @click="handleBreadcrumbClick(index)"
        >
          <el-icon v-if="index === 0" class="home-icon">
            <House />
          </el-icon>
          <span class="item-text">{{ item.label }}</span>
          <el-icon v-if="index < breadcrumbItems.length - 1" class="separator">
            <ArrowRight />
          </el-icon>
        </div>
      </div>

      <div class="breadcrumb-actions">
        <el-tooltip content="返回上一级" placement="top">
          <el-button
            size="small"
            type="primary"
            text
            :disabled="drillPath.levels.length <= 1"
            @click="handleDrillUp"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </el-tooltip>

        <el-tooltip content="重置到根级别" placement="top">
          <el-button
            size="small"
            type="danger"
            text
            :disabled="drillPath.levels.length === 0"
            @click="handleReset"
          >
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-tooltip>

        <el-dropdown @command="handleQuickJump" trigger="click">
          <el-button size="small" type="info" text>
            <el-icon><More /></el-icon>
            快速跳转
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="(item, index) in breadcrumbItems"
                :key="index"
                :command="index"
                :disabled="index === breadcrumbItems.length - 1"
              >
                <el-icon v-if="index === 0"><House /></el-icon>
                {{ item.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 当前级别信息 -->
    <div class="level-info" v-if="currentLevel">
      <div class="level-details">
        <el-tag size="small" type="info">
          级别: {{ currentLevel.name }}
        </el-tag>
        <el-tag size="small" type="success" v-if="currentLevel.field">
          字段: {{ currentLevel.field }}
        </el-tag>
        <el-tag size="small" type="warning" v-if="currentValue">
          值: {{ currentValue }}
        </el-tag>
      </div>

      <div class="level-stats" v-if="levelStats">
        <span class="stat-item">
          <el-icon><DataAnalysis /></el-icon>
          数据点: {{ levelStats.dataPoints }}
        </span>
        <span class="stat-item">
          <el-icon><Timer /></el-icon>
          更新时间: {{ formatTime(levelStats.lastUpdate) }}
        </span>
      </div>
    </div>

    <!-- 钻取历史 -->
    <div class="drill-history" v-if="showHistory && drillHistory.length > 0">
      <div class="history-header">
        <h4>钻取历史</h4>
        <el-button size="small" text @click="clearHistory">
          <el-icon><Delete /></el-icon>
          清空历史
        </el-button>
      </div>

      <div class="history-items">
        <div
          v-for="(historyItem, index) in drillHistory"
          :key="index"
          class="history-item"
          @click="restoreFromHistory(historyItem)"
        >
          <div class="history-path">
            <span
              v-for="(pathItem, pathIndex) in historyItem.path.levels"
              :key="pathIndex"
              class="path-segment"
            >
              {{ pathItem.value }}
              <el-icon v-if="pathIndex < historyItem.path.levels.length - 1">
                <ArrowRight />
              </el-icon>
            </span>
          </div>
          <div class="history-time">
            {{ formatTime(historyItem.timestamp) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import {
  House,
  ArrowRight,
  ArrowLeft,
  RefreshLeft,
  More,
  DataAnalysis,
  Timer,
  Delete,
} from "@element-plus/icons-vue";
import type { DrillDownPath, DrillDownLevel } from "@/types/chart";

interface BreadcrumbItem {
  label: string;
  value: string;
  level: DrillDownLevel;
}

interface LevelStats {
  dataPoints: number;
  lastUpdate: Date;
}

interface DrillHistory {
  path: DrillDownPath;
  timestamp: Date;
}

interface Props {
  drillPath: DrillDownPath;
  drillConfig?: {
    levels: DrillDownLevel[];
  };
  showHistory?: boolean;
}

interface Emits {
  (e: "drill-up"): void;
  (e: "drill-to", level: number): void;
  (e: "reset"): void;
}

const props = withDefaults(defineProps<Props>(), {
  showHistory: false,
});

const emit = defineEmits<Emits>();

// 响应式数据
const drillHistory = ref<DrillHistory[]>([]);

// 计算属性
const breadcrumbItems = computed<BreadcrumbItem[]>(() => {
  const items: BreadcrumbItem[] = [];

  // 添加根级别
  if (props.drillConfig?.levels[0]) {
    items.push({
      label: "全部数据",
      value: "root",
      level: props.drillConfig.levels[0],
    });
  }

  // 添加钻取路径中的每一级
  props.drillPath.levels.forEach((pathItem, index) => {
    const level = props.drillConfig?.levels[index + 1];
    if (level) {
      items.push({
        label: pathItem.label || pathItem.value,
        value: pathItem.value,
        level,
      });
    }
  });

  return items;
});

const currentLevel = computed(() => {
  if (!props.drillConfig) return null;
  const levelIndex = props.drillPath.levels.length;
  return props.drillConfig.levels[levelIndex] || null;
});

const currentValue = computed(() => {
  if (props.drillPath.levels.length === 0) return null;
  return props.drillPath.levels[props.drillPath.levels.length - 1]?.value;
});

const levelStats = computed<LevelStats | null>(() => {
  // 模拟级别统计数据
  if (props.drillPath.levels.length === 0) {
    return {
      dataPoints: 1000,
      lastUpdate: new Date(),
    };
  }

  return {
    dataPoints: Math.floor(Math.random() * 500) + 50,
    lastUpdate: new Date(),
  };
});

// 方法
const handleBreadcrumbClick = (index: number) => {
  if (index < breadcrumbItems.value.length - 1) {
    emit("drill-to", index);
  }
};

const handleDrillUp = () => {
  if (props.drillPath.levels.length > 0) {
    // 保存到历史记录
    saveToHistory();
    emit("drill-up");
  }
};

const handleReset = () => {
  if (props.drillPath.levels.length > 0) {
    // 保存到历史记录
    saveToHistory();
    emit("reset");
  }
};

const handleQuickJump = (index: number) => {
  if (index < breadcrumbItems.value.length - 1) {
    // 保存到历史记录
    saveToHistory();
    emit("drill-to", index);
  }
};

const saveToHistory = () => {
  if (props.drillPath.levels.length > 0) {
    const historyItem: DrillHistory = {
      path: {
        levels: [...props.drillPath.levels],
      },
      timestamp: new Date(),
    };

    drillHistory.value.unshift(historyItem);

    // 限制历史记录数量
    if (drillHistory.value.length > 10) {
      drillHistory.value = drillHistory.value.slice(0, 10);
    }
  }
};

const restoreFromHistory = (historyItem: DrillHistory) => {
  // 这里需要通过父组件来恢复钻取路径
  // 可以通过事件或者直接操作 drillPath
  console.log("恢复钻取路径:", historyItem.path);
};

const clearHistory = () => {
  drillHistory.value = [];
};

const formatTime = (date: Date) => {
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};
</script>

<style scoped>
.drill-down-breadcrumb {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.breadcrumb-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.breadcrumb-items {
  display: flex;
  align-items: center;
  flex: 1;
  overflow-x: auto;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;
}

.breadcrumb-item.is-clickable {
  cursor: pointer;
  color: #409eff;
}

.breadcrumb-item.is-clickable:hover {
  background-color: #ecf5ff;
}

.breadcrumb-item.is-active {
  color: #333;
  font-weight: 600;
}

.home-icon {
  margin-right: 4px;
  color: #409eff;
}

.item-text {
  margin: 0 4px;
}

.separator {
  margin: 0 8px;
  color: #c0c4cc;
  font-size: 12px;
}

.breadcrumb-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.level-info {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  margin-bottom: 12px;
}

.level-details {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.level-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #666;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.drill-history {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.history-items {
  max-height: 120px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-path {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.path-segment {
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-time {
  font-size: 11px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .breadcrumb-container {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .breadcrumb-actions {
    margin-left: 0;
    justify-content: center;
  }

  .level-stats {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
