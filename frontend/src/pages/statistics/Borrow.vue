<template>
  <div class="statistics-borrow">
    <div class="page-header">
      <h2>借阅统计</h2>
      <p>借阅相关数据统计分析</p>
    </div>

    <div class="borrow-content">
      <!-- 借阅统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ borrowStats.totalBorrows }}</div>
              <div class="stat-label">总借阅次数</div>
              <div class="stat-change">
                <span class="change-text positive"
                  >+{{ borrowStats.monthlyIncrease }}</span
                >
                <span class="change-label">本月新增</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Reading /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ borrowStats.activeBorrows }}</div>
              <div class="stat-label">当前借出</div>
              <div class="stat-change">
                <span class="change-text">{{ borrowStats.returnRate }}%</span>
                <span class="change-label">归还率</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Clock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ borrowStats.overdueBorrows }}</div>
              <div class="stat-label">逾期档案</div>
              <div class="stat-change">
                <span class="change-text negative"
                  >{{ borrowStats.overdueRate }}%</span
                >
                <span class="change-label">逾期率</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Warning /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ borrowStats.avgDuration }}</div>
              <div class="stat-label">平均借阅天数</div>
              <div class="stat-change">
                <span class="change-text">{{ borrowStats.maxDuration }}</span>
                <span class="change-label">最长天数</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Timer /></el-icon>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>借阅趋势分析</span>
                <div>
                  <el-date-picker
                    v-model="dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    size="small"
                    style="width: 240px"
                  />
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                📊 借阅数量趋势图
                <p>显示借阅和归还数量随时间的变化</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>借阅状态分布</span>
            </template>
            <div class="status-chart">
              <div class="chart-placeholder">
                🥧 状态分布饼图
                <p>借阅状态占比分析</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>热门档案排行</span>
            </template>
            <div class="ranking-list">
              <div
                v-for="(archive, index) in hotArchives"
                :key="archive.id"
                class="ranking-item"
              >
                <div class="rank-number" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="archive-info">
                  <div class="archive-title">{{ archive.title }}</div>
                  <div class="archive-stats">
                    <span class="borrow-count"
                      >{{ archive.borrowCount }} 次借阅</span
                    >
                    <span class="avg-duration"
                      >平均 {{ archive.avgDuration }} 天</span
                    >
                  </div>
                </div>
                <div class="archive-trend">
                  <el-icon :class="archive.trend"
                    ><CaretTop v-if="archive.trend === 'up'" /><CaretBottom
                      v-else
                  /></el-icon>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>活跃用户排行</span>
            </template>
            <div class="ranking-list">
              <div
                v-for="(user, index) in activeUsers"
                :key="user.id"
                class="ranking-item"
              >
                <div class="rank-number" :class="`rank-${index + 1}`">
                  {{ index + 1 }}
                </div>
                <div class="user-info">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-stats">
                    <span class="borrow-count"
                      >{{ user.borrowCount }} 次借阅</span
                    >
                    <span class="overdue-count"
                      >{{ user.overdueCount }} 次逾期</span
                    >
                  </div>
                </div>
                <div class="user-score" :class="getUserScoreClass(user.score)">
                  {{ user.score }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 借阅时间分析 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>借阅时间分布</span>
            </template>
            <div class="time-analysis">
              <div class="time-period">
                <div class="period-label">工作日</div>
                <div class="period-bar">
                  <div class="period-progress" style="width: 75%"></div>
                </div>
                <div class="period-value">75%</div>
              </div>
              <div class="time-period">
                <div class="period-label">周末</div>
                <div class="period-bar">
                  <div class="period-progress" style="width: 25%"></div>
                </div>
                <div class="period-value">25%</div>
              </div>
              <div class="time-period">
                <div class="period-label">上午</div>
                <div class="period-bar">
                  <div class="period-progress" style="width: 45%"></div>
                </div>
                <div class="period-value">45%</div>
              </div>
              <div class="time-period">
                <div class="period-label">下午</div>
                <div class="period-bar">
                  <div class="period-progress" style="width: 55%"></div>
                </div>
                <div class="period-value">55%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>借阅期限分析</span>
            </template>
            <div class="duration-analysis">
              <div
                v-for="duration in durationStats"
                :key="duration.range"
                class="duration-item"
              >
                <div class="duration-range">{{ duration.range }}</div>
                <div class="duration-bar">
                  <div
                    class="duration-progress"
                    :style="{ width: duration.percentage + '%' }"
                  ></div>
                </div>
                <div class="duration-stats">
                  <span class="duration-count">{{ duration.count }}</span>
                  <span class="duration-percentage"
                    >{{ duration.percentage }}%</span
                  >
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细统计表格 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>借阅详细统计</span>
            <div>
              <el-button type="primary" size="small" @click="exportReport">
                <el-icon><Download /></el-icon>
                导出报表
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="detailStats" style="width: 100%">
          <el-table-column prop="period" label="时间段" />
          <el-table-column prop="totalBorrows" label="总借阅" />
          <el-table-column prop="newBorrows" label="新借阅" />
          <el-table-column prop="returns" label="归还数" />
          <el-table-column prop="overdue" label="逾期数" />
          <el-table-column prop="returnRate" label="归还率">
            <template #default="{ row }">
              <el-progress
                :percentage="row.returnRate"
                :color="getReturnRateColor(row.returnRate)"
                :show-text="false"
                style="width: 80px"
              />
              <span style="margin-left: 8px">{{ row.returnRate }}%</span>
            </template>
          </el-table-column>
          <el-table-column prop="avgDuration" label="平均借阅天数" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import {
  Reading,
  Clock,
  Warning,
  Timer,
  CaretTop,
  CaretBottom,
  Download,
} from "@element-plus/icons-vue";

const dateRange = ref<[Date, Date]>([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
  new Date(),
]);

const borrowStats = ref({
  totalBorrows: 856,
  activeBorrows: 156,
  overdueBorrows: 15,
  avgDuration: 12,
  monthlyIncrease: 68,
  returnRate: 92,
  overdueRate: 1.8,
  maxDuration: 45,
});

const hotArchives = ref([
  {
    id: 1,
    title: "员工入职档案",
    borrowCount: 45,
    avgDuration: 8,
    trend: "up",
  },
  { id: 2, title: "财务报表", borrowCount: 38, avgDuration: 15, trend: "up" },
  { id: 3, title: "合同档案", borrowCount: 32, avgDuration: 20, trend: "down" },
  { id: 4, title: "项目文档", borrowCount: 28, avgDuration: 12, trend: "up" },
  { id: 5, title: "会议记录", borrowCount: 24, avgDuration: 5, trend: "down" },
]);

const activeUsers = ref([
  { id: 1, name: "张三", borrowCount: 25, overdueCount: 1, score: 95 },
  { id: 2, name: "李四", borrowCount: 22, overdueCount: 0, score: 100 },
  { id: 3, name: "王五", borrowCount: 18, overdueCount: 2, score: 88 },
  { id: 4, name: "赵六", borrowCount: 15, overdueCount: 0, score: 98 },
  { id: 5, name: "钱七", borrowCount: 12, overdueCount: 3, score: 75 },
]);

const durationStats = ref([
  { range: "1-7天", count: 320, percentage: 45 },
  { range: "8-14天", count: 240, percentage: 34 },
  { range: "15-30天", count: 120, percentage: 17 },
  { range: "30天以上", count: 28, percentage: 4 },
]);

const detailStats = ref([
  {
    period: "本周",
    totalBorrows: 45,
    newBorrows: 28,
    returns: 32,
    overdue: 2,
    returnRate: 95,
    avgDuration: 10,
  },
  {
    period: "上周",
    totalBorrows: 52,
    newBorrows: 35,
    returns: 38,
    overdue: 1,
    returnRate: 97,
    avgDuration: 12,
  },
  {
    period: "本月",
    totalBorrows: 186,
    newBorrows: 125,
    returns: 142,
    overdue: 8,
    returnRate: 92,
    avgDuration: 11,
  },
  {
    period: "上月",
    totalBorrows: 198,
    newBorrows: 132,
    returns: 156,
    overdue: 5,
    returnRate: 95,
    avgDuration: 13,
  },
]);

const exportReport = () => {
  ElMessage.success("报表导出功能开发中...");
};

const getUserScoreClass = (score: number) => {
  if (score >= 95) return "excellent";
  if (score >= 85) return "good";
  if (score >= 75) return "average";
  return "poor";
};

const getReturnRateColor = (rate: number) => {
  if (rate >= 95) return "#67c23a";
  if (rate >= 90) return "#e6a23c";
  return "#f56c6c";
};
</script>

<style lang="scss" scoped>
.statistics-borrow {
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
      &.negative {
        color: $color-danger;
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

.chart-container,
.status-chart {
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

.ranking-list {
  .ranking-item {
    display: flex;
    align-items: center;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .rank-number {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.8rem;
    font-weight: bold;
    margin-right: $spacing-sm;

    &.rank-1 {
      background: #ffd700;
      color: white;
    }
    &.rank-2 {
      background: #c0c0c0;
      color: white;
    }
    &.rank-3 {
      background: #cd7f32;
      color: white;
    }

    &:not(.rank-1):not(.rank-2):not(.rank-3) {
      background: $bg-light;
      color: $text-secondary;
    }
  }

  .archive-info,
  .user-info {
    flex: 1;

    .archive-title,
    .user-name {
      font-weight: 500;
      color: $text-primary;
      margin-bottom: 2px;
    }

    .archive-stats,
    .user-stats {
      font-size: 0.8rem;
      color: $text-secondary;

      span {
        margin-right: $spacing-sm;
      }
    }
  }

  .archive-trend {
    .el-icon {
      &.up {
        color: $color-success;
      }
      &.down {
        color: $color-danger;
      }
    }
  }

  .user-score {
    font-size: 1.2rem;
    font-weight: bold;
    padding: 4px 8px;
    border-radius: 4px;

    &.excellent {
      background: rgba(103, 194, 58, 0.1);
      color: $color-success;
    }
    &.good {
      background: rgba(64, 158, 255, 0.1);
      color: $color-primary;
    }
    &.average {
      background: rgba(230, 162, 60, 0.1);
      color: $color-warning;
    }
    &.poor {
      background: rgba(245, 108, 108, 0.1);
      color: $color-danger;
    }
  }
}

.time-analysis {
  .time-period {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .period-label {
    width: 60px;
    font-size: 0.9rem;
    color: $text-secondary;
  }

  .period-bar {
    flex: 1;
    height: 8px;
    background: $bg-light;
    border-radius: 4px;
    margin: 0 $spacing-sm;
    overflow: hidden;

    .period-progress {
      height: 100%;
      background: $color-primary;
      transition: width 0.3s ease;
    }
  }

  .period-value {
    width: 40px;
    text-align: right;
    font-size: 0.9rem;
    color: $text-primary;
    font-weight: 500;
  }
}

.duration-analysis {
  .duration-item {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .duration-range {
    width: 80px;
    font-size: 0.9rem;
    color: $text-secondary;
  }

  .duration-bar {
    flex: 1;
    height: 8px;
    background: $bg-light;
    border-radius: 4px;
    margin: 0 $spacing-sm;
    overflow: hidden;

    .duration-progress {
      height: 100%;
      background: $color-success;
      transition: width 0.3s ease;
    }
  }

  .duration-stats {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    width: 60px;

    .duration-count {
      font-size: 0.9rem;
      color: $text-primary;
      font-weight: 500;
    }

    .duration-percentage {
      font-size: 0.8rem;
      color: $text-secondary;
    }
  }
}
</style>
