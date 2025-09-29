<template>
  <div class="statistics-archive">
    <div class="page-header">
      <h2>档案统计</h2>
      <p>档案相关数据统计分析</p>
    </div>

    <div class="archive-content">
      <!-- 档案统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ archiveStats.total }}</div>
              <div class="stat-label">档案总数</div>
              <div class="stat-change">
                <span class="change-text positive"
                  >+{{ archiveStats.monthlyIncrease }}</span
                >
                <span class="change-label">本月新增</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Document /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ archiveStats.categories }}</div>
              <div class="stat-label">档案分类</div>
              <div class="stat-change">
                <span class="change-text">{{
                  archiveStats.avgPerCategory
                }}</span>
                <span class="change-label">平均每类</span>
              </div>
            </div>
            <el-icon class="stat-icon"><FolderOpened /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ archiveStats.digitalRate }}%</div>
              <div class="stat-label">数字化率</div>
              <div class="stat-change">
                <span class="change-text positive"
                  >+{{ archiveStats.digitalIncrease }}%</span
                >
                <span class="change-label">较上月</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Monitor /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ archiveStats.storageUsed }}GB</div>
              <div class="stat-label">存储使用</div>
              <div class="stat-change">
                <span class="change-text"
                  >{{ archiveStats.storageTotal }}GB</span
                >
                <span class="change-label">总容量</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Files /></el-icon>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>档案增长趋势</span>
                <div>
                  <el-radio-group v-model="trendPeriod" size="small">
                    <el-radio-button value="week">周</el-radio-button>
                    <el-radio-button value="month">月</el-radio-button>
                    <el-radio-button value="year">年</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                📈 档案数量增长趋势图
                <p>显示档案数量随时间的变化情况</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>档案分类分布</span>
            </template>
            <div class="category-list">
              <div
                v-for="category in categoryStats"
                :key="category.name"
                class="category-item"
              >
                <div class="category-info">
                  <span class="category-name">{{ category.name }}</span>
                  <span class="category-count">{{ category.count }}</span>
                </div>
                <div class="category-bar">
                  <div
                    class="category-progress"
                    :style="{ width: category.percentage + '%' }"
                  ></div>
                </div>
                <span class="category-percentage"
                  >{{ category.percentage }}%</span
                >
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>档案状态分布</span>
            </template>
            <div class="status-grid">
              <div
                v-for="status in statusStats"
                :key="status.name"
                class="status-item"
              >
                <div class="status-icon" :class="status.type">
                  <el-icon><component :is="status.icon" /></el-icon>
                </div>
                <div class="status-info">
                  <div class="status-name">{{ status.name }}</div>
                  <div class="status-count">{{ status.count }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>档案操作统计</span>
            </template>
            <div class="operation-stats">
              <div
                v-for="operation in operationStats"
                :key="operation.name"
                class="operation-item"
              >
                <div class="operation-label">{{ operation.name }}</div>
                <div class="operation-value">{{ operation.count }}</div>
                <div class="operation-trend" :class="operation.trend">
                  <el-icon v-if="operation.trend === 'up'"
                    ><CaretTop
                  /></el-icon>
                  <el-icon v-else-if="operation.trend === 'down'"
                    ><CaretBottom
                  /></el-icon>
                  <el-icon v-else><Minus /></el-icon>
                  {{ operation.change }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细数据表格 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>档案详细统计</span>
            <div>
              <el-button type="primary" size="small" @click="exportData">
                <el-icon><Download /></el-icon>
                导出数据
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="detailStats" style="width: 100%">
          <el-table-column prop="category" label="档案分类" />
          <el-table-column prop="total" label="总数量" />
          <el-table-column prop="thisMonth" label="本月新增" />
          <el-table-column prop="borrowed" label="已借出" />
          <el-table-column prop="available" label="可借阅" />
          <el-table-column prop="digital" label="数字化" />
          <el-table-column prop="borrowRate" label="借阅率">
            <template #default="{ row }">
              <el-progress
                :percentage="row.borrowRate"
                :color="getBorrowRateColor(row.borrowRate)"
                :show-text="false"
                style="width: 80px"
              />
              <span style="margin-left: 8px">{{ row.borrowRate }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import {
  Document,
  FolderOpened,
  Monitor,
  Files,
  CaretTop,
  CaretBottom,
  Minus,
  Download,
  Check,
  Clock,
  Warning,
  Lock,
} from "@element-plus/icons-vue";

const trendPeriod = ref("month");

const archiveStats = ref({
  total: 1248,
  categories: 8,
  digitalRate: 85,
  storageUsed: 256,
  storageTotal: 500,
  monthlyIncrease: 45,
  avgPerCategory: 156,
  digitalIncrease: 5,
});

const categoryStats = ref([
  { name: "人事档案", count: 320, percentage: 25.6 },
  { name: "财务档案", count: 280, percentage: 22.4 },
  { name: "合同档案", count: 200, percentage: 16.0 },
  { name: "项目档案", count: 180, percentage: 14.4 },
  { name: "会议档案", count: 120, percentage: 9.6 },
  { name: "其他档案", count: 148, percentage: 11.9 },
]);

const statusStats = ref([
  { name: "正常", count: 1050, type: "success", icon: "Check" },
  { name: "借出中", count: 156, type: "warning", icon: "Clock" },
  { name: "逾期", count: 15, type: "danger", icon: "Warning" },
  { name: "封存", count: 27, type: "info", icon: "Lock" },
]);

const operationStats = ref([
  { name: "新增档案", count: 45, change: "+12%", trend: "up" },
  { name: "档案借阅", count: 128, change: "+8%", trend: "up" },
  { name: "档案归还", count: 115, change: "-3%", trend: "down" },
  { name: "档案查询", count: 892, change: "+15%", trend: "up" },
]);

const detailStats = ref([
  {
    category: "人事档案",
    total: 320,
    thisMonth: 12,
    borrowed: 45,
    available: 275,
    digital: 280,
    borrowRate: 14,
  },
  {
    category: "财务档案",
    total: 280,
    thisMonth: 8,
    borrowed: 38,
    available: 242,
    digital: 260,
    borrowRate: 14,
  },
  {
    category: "合同档案",
    total: 200,
    thisMonth: 15,
    borrowed: 32,
    available: 168,
    digital: 180,
    borrowRate: 16,
  },
  {
    category: "项目档案",
    total: 180,
    thisMonth: 6,
    borrowed: 28,
    available: 152,
    digital: 150,
    borrowRate: 16,
  },
]);

const exportData = () => {
  ElMessage.success("数据导出功能开发中...");
};

const getBorrowRateColor = (rate: number) => {
  if (rate >= 20) return "#f56c6c";
  if (rate >= 15) return "#e6a23c";
  if (rate >= 10) return "#67c23a";
  return "#409eff";
};
</script>

<style lang="scss" scoped>
.statistics-archive {
  padding: $spacing-lg;
}

.page-header {
  margin-bottom: $spacing-lg;

  h2 {
    margin: 0 0 $spacing-sm 0;
    color: $text-primary;
  }

  p {
    margin: 0;
    color: $text-secondary;
  }
}

.stats-cards {
  margin-bottom: $spacing-lg;
}

.stat-card {
  position: relative;

  .stat-content {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .stat-number {
    font-size: 2.2rem;
    font-weight: bold;
    color: $text-primary;
    line-height: 1;
  }

  .stat-label {
    color: $text-secondary;
    margin: $spacing-xs 0;
    font-size: 0.9rem;
  }

  .stat-change {
    display: flex;
    flex-direction: column;
    font-size: 0.8rem;

    .change-text {
      font-weight: 500;

      &.positive {
        color: $color-success;
      }
    }

    .change-label {
      color: $text-tertiary;
      margin-top: 2px;
    }
  }

  .stat-icon {
    position: absolute;
    right: $spacing-lg;
    top: 50%;
    transform: translateY(-50%);
    font-size: 2.5rem;
    color: $color-primary;
    opacity: 0.2;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: $text-secondary;
  font-size: 1.2rem;

  p {
    margin-top: $spacing-sm;
    font-size: 0.9rem;
    color: $text-tertiary;
  }
}

.category-list {
  .category-item {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .category-info {
    display: flex;
    justify-content: space-between;
    width: 100px;
    margin-right: $spacing-sm;

    .category-name {
      font-size: 0.9rem;
      color: $text-primary;
    }

    .category-count {
      font-weight: 500;
      color: $text-secondary;
    }
  }

  .category-bar {
    flex: 1;
    height: 8px;
    background: $bg-light;
    border-radius: 4px;
    margin-right: $spacing-sm;
    overflow: hidden;

    .category-progress {
      height: 100%;
      background: $color-primary;
      transition: width 0.3s ease;
    }
  }

  .category-percentage {
    font-size: 0.8rem;
    color: $text-secondary;
    width: 35px;
    text-align: right;
  }
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-lg;
}

.status-item {
  display: flex;
  align-items: center;

  .status-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-sm;

    &.success {
      background: rgba(103, 194, 58, 0.1);
      color: $color-success;
    }
    &.warning {
      background: rgba(230, 162, 60, 0.1);
      color: $color-warning;
    }
    &.danger {
      background: rgba(245, 108, 108, 0.1);
      color: $color-danger;
    }
    &.info {
      background: rgba(144, 147, 153, 0.1);
      color: $color-info;
    }
  }

  .status-info {
    .status-name {
      font-size: 0.9rem;
      color: $text-secondary;
    }

    .status-count {
      font-size: 1.5rem;
      font-weight: bold;
      color: $text-primary;
    }
  }
}

.operation-stats {
  .operation-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .operation-label {
    color: $text-secondary;
    font-size: 0.9rem;
  }

  .operation-value {
    font-size: 1.2rem;
    font-weight: bold;
    color: $text-primary;
  }

  .operation-trend {
    display: flex;
    align-items: center;
    font-size: 0.8rem;

    &.up {
      color: $color-success;
    }
    &.down {
      color: $color-danger;
    }

    .el-icon {
      margin-right: 2px;
    }
  }
}
</style>
