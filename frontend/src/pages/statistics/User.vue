<template>
  <div class="statistics-user">
    <div class="page-header">
      <h2>用户统计</h2>
      <p>用户相关数据统计分析</p>
    </div>

    <div class="user-content">
      <!-- 用户统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userStats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
              <div class="stat-change">
                <span class="change-text positive"
                  >+{{ userStats.newUsers }}</span
                >
                <span class="change-label">本月新增</span>
              </div>
            </div>
            <el-icon class="stat-icon"><User /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userStats.activeUsers }}</div>
              <div class="stat-label">活跃用户</div>
              <div class="stat-change">
                <span class="change-text">{{ userStats.activeRate }}%</span>
                <span class="change-label">活跃率</span>
              </div>
            </div>
            <el-icon class="stat-icon"><UserFilled /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userStats.borrowingUsers }}</div>
              <div class="stat-label">借阅用户</div>
              <div class="stat-change">
                <span class="change-text">{{ userStats.borrowRate }}%</span>
                <span class="change-label">借阅率</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Reading /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userStats.overdueUsers }}</div>
              <div class="stat-label">逾期用户</div>
              <div class="stat-change">
                <span class="change-text negative"
                  >{{ userStats.overdueRate }}%</span
                >
                <span class="change-label">逾期率</span>
              </div>
            </div>
            <el-icon class="stat-icon"><Warning /></el-icon>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户增长趋势</span>
                <el-radio-group v-model="growthPeriod" size="small">
                  <el-radio-button value="month">月</el-radio-button>
                  <el-radio-button value="quarter">季</el-radio-button>
                  <el-radio-button value="year">年</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                📈 用户增长趋势图
                <p>显示用户注册数量随时间的变化</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>用户活跃度分析</span>
            </template>
            <div class="activity-analysis">
              <div class="activity-item">
                <div class="activity-label">高活跃用户</div>
                <div class="activity-bar">
                  <div class="activity-progress high" style="width: 35%"></div>
                </div>
                <div class="activity-value">
                  35% ({{ Math.round(userStats.totalUsers * 0.35) }}人)
                </div>
              </div>
              <div class="activity-item">
                <div class="activity-label">中活跃用户</div>
                <div class="activity-bar">
                  <div
                    class="activity-progress medium"
                    style="width: 45%"
                  ></div>
                </div>
                <div class="activity-value">
                  45% ({{ Math.round(userStats.totalUsers * 0.45) }}人)
                </div>
              </div>
              <div class="activity-item">
                <div class="activity-label">低活跃用户</div>
                <div class="activity-bar">
                  <div class="activity-progress low" style="width: 20%"></div>
                </div>
                <div class="activity-value">
                  20% ({{ Math.round(userStats.totalUsers * 0.2) }}人)
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>部门用户分布</span>
            </template>
            <div class="department-list">
              <div
                v-for="dept in departmentStats"
                :key="dept.name"
                class="department-item"
              >
                <div class="dept-info">
                  <span class="dept-name">{{ dept.name }}</span>
                  <span class="dept-count">{{ dept.count }}人</span>
                </div>
                <div class="dept-bar">
                  <div
                    class="dept-progress"
                    :style="{ width: dept.percentage + '%' }"
                  ></div>
                </div>
                <span class="dept-percentage">{{ dept.percentage }}%</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>用户角色分布</span>
            </template>
            <div class="role-distribution">
              <div v-for="role in roleStats" :key="role.name" class="role-item">
                <div class="role-icon" :class="role.type">
                  <el-icon><component :is="role.icon" /></el-icon>
                </div>
                <div class="role-info">
                  <div class="role-name">{{ role.name }}</div>
                  <div class="role-count">{{ role.count }}人</div>
                </div>
                <div class="role-percentage">{{ role.percentage }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>用户信用评分</span>
            </template>
            <div class="credit-distribution">
              <div
                v-for="credit in creditStats"
                :key="credit.range"
                class="credit-item"
              >
                <div class="credit-range" :class="credit.level">
                  {{ credit.range }}
                </div>
                <div class="credit-bar">
                  <div
                    class="credit-progress"
                    :class="credit.level"
                    :style="{ width: credit.percentage + '%' }"
                  ></div>
                </div>
                <div class="credit-stats">
                  <span class="credit-count">{{ credit.count }}</span>
                  <span class="credit-percentage"
                    >{{ credit.percentage }}%</span
                  >
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 用户行为分析 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>用户行为统计</span>
            </template>
            <div class="behavior-stats">
              <div
                v-for="behavior in behaviorStats"
                :key="behavior.action"
                class="behavior-item"
              >
                <div class="behavior-label">{{ behavior.action }}</div>
                <div class="behavior-value">{{ behavior.count }}</div>
                <div class="behavior-trend" :class="behavior.trend">
                  <el-icon v-if="behavior.trend === 'up'"><CaretTop /></el-icon>
                  <el-icon v-else-if="behavior.trend === 'down'"
                    ><CaretBottom
                  /></el-icon>
                  <el-icon v-else><Minus /></el-icon>
                  {{ behavior.change }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>登录时间分析</span>
            </template>
            <div class="login-analysis">
              <div class="time-slots">
                <div
                  v-for="slot in loginTimeSlots"
                  :key="slot.time"
                  class="time-slot"
                >
                  <div class="slot-time">{{ slot.time }}</div>
                  <div class="slot-bar">
                    <div
                      class="slot-progress"
                      :style="{ height: slot.percentage + '%' }"
                    ></div>
                  </div>
                  <div class="slot-count">{{ slot.count }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 用户排行榜 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <div class="card-header">
            <span>用户排行榜</span>
            <el-tabs v-model="rankingType" size="small">
              <el-tab-pane label="借阅排行" name="borrow" />
              <el-tab-pane label="活跃排行" name="activity" />
              <el-tab-pane label="信用排行" name="credit" />
            </el-tabs>
          </div>
        </template>

        <el-table :data="getCurrentRanking()" style="width: 100%">
          <el-table-column type="index" label="排名" width="80">
            <template #default="{ $index }">
              <div class="rank-badge" :class="getRankClass($index)">
                {{ $index + 1 }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="用户姓名" />
          <el-table-column prop="department" label="部门" />
          <el-table-column prop="role" label="角色" />
          <el-table-column
            :prop="getRankingProp()"
            :label="getRankingLabel()"
          />
          <el-table-column prop="lastActive" label="最后活跃" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="viewUserDetail(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import {
  User,
  UserFilled,
  Reading,
  Warning,
  CaretTop,
  CaretBottom,
  Minus,
  Avatar,
  Tools,
  Lock,
} from "@element-plus/icons-vue";

const growthPeriod = ref("month");
const rankingType = ref("borrow");

const userStats = ref({
  totalUsers: 324,
  activeUsers: 256,
  borrowingUsers: 189,
  overdueUsers: 12,
  newUsers: 18,
  activeRate: 79,
  borrowRate: 58,
  overdueRate: 3.7,
});

const departmentStats = ref([
  { name: "人事部", count: 45, percentage: 14 },
  { name: "财务部", count: 38, percentage: 12 },
  { name: "技术部", count: 62, percentage: 19 },
  { name: "市场部", count: 35, percentage: 11 },
  { name: "运营部", count: 28, percentage: 9 },
  { name: "其他部门", count: 116, percentage: 36 },
]);

const roleStats = ref([
  { name: "普通用户", count: 280, percentage: 86, type: "user", icon: "User" },
  { name: "管理员", count: 35, percentage: 11, type: "admin", icon: "Tools" },
  { name: "超级管理员", count: 9, percentage: 3, type: "super", icon: "Lock" },
]);

const creditStats = ref([
  { range: "90-100", count: 156, percentage: 48, level: "excellent" },
  { range: "80-89", count: 98, percentage: 30, level: "good" },
  { range: "70-79", count: 45, percentage: 14, level: "average" },
  { range: "60-69", count: 25, percentage: 8, level: "poor" },
]);

const behaviorStats = ref([
  { action: "档案查询", count: 1256, change: "+15%", trend: "up" },
  { action: "档案借阅", count: 856, change: "+8%", trend: "up" },
  { action: "档案归还", count: 742, change: "-2%", trend: "down" },
  { action: "系统登录", count: 2145, change: "+12%", trend: "up" },
]);

const loginTimeSlots = ref([
  { time: "8:00", count: 45, percentage: 60 },
  { time: "10:00", count: 62, percentage: 83 },
  { time: "12:00", count: 38, percentage: 51 },
  { time: "14:00", count: 75, percentage: 100 },
  { time: "16:00", count: 58, percentage: 77 },
  { time: "18:00", count: 32, percentage: 43 },
]);

const borrowRanking = ref([
  {
    name: "张三",
    department: "技术部",
    role: "普通用户",
    borrowCount: 25,
    lastActive: "2024-01-20",
  },
  {
    name: "李四",
    department: "财务部",
    role: "普通用户",
    borrowCount: 22,
    lastActive: "2024-01-19",
  },
  {
    name: "王五",
    department: "人事部",
    role: "管理员",
    borrowCount: 18,
    lastActive: "2024-01-18",
  },
]);

const activityRanking = ref([
  {
    name: "赵六",
    department: "市场部",
    role: "普通用户",
    activityScore: 95,
    lastActive: "2024-01-20",
  },
  {
    name: "钱七",
    department: "运营部",
    role: "普通用户",
    activityScore: 92,
    lastActive: "2024-01-20",
  },
  {
    name: "孙八",
    department: "技术部",
    role: "管理员",
    activityScore: 88,
    lastActive: "2024-01-19",
  },
]);

const creditRanking = ref([
  {
    name: "周九",
    department: "财务部",
    role: "普通用户",
    creditScore: 100,
    lastActive: "2024-01-19",
  },
  {
    name: "吴十",
    department: "人事部",
    role: "管理员",
    creditScore: 98,
    lastActive: "2024-01-18",
  },
  {
    name: "郑一",
    department: "技术部",
    role: "普通用户",
    creditScore: 95,
    lastActive: "2024-01-20",
  },
]);

const getCurrentRanking = () => {
  switch (rankingType.value) {
    case "borrow":
      return borrowRanking.value;
    case "activity":
      return activityRanking.value;
    case "credit":
      return creditRanking.value;
    default:
      return borrowRanking.value;
  }
};

const getRankingProp = () => {
  switch (rankingType.value) {
    case "borrow":
      return "borrowCount";
    case "activity":
      return "activityScore";
    case "credit":
      return "creditScore";
    default:
      return "borrowCount";
  }
};

const getRankingLabel = () => {
  switch (rankingType.value) {
    case "borrow":
      return "借阅次数";
    case "activity":
      return "活跃度";
    case "credit":
      return "信用分";
    default:
      return "借阅次数";
  }
};

const getRankClass = (index: number) => {
  if (index === 0) return "rank-1";
  if (index === 1) return "rank-2";
  if (index === 2) return "rank-3";
  return "rank-other";
};

const viewUserDetail = (user: any) => {
  ElMessage.info(`查看用户 ${user.name} 的详细信息`);
};
</script>

<style lang="scss" scoped>
.statistics-user {
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

.activity-analysis {
  .activity-item {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-lg;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .activity-label {
    width: 100px;
    font-size: 0.9rem;
    color: $text-secondary;
  }

  .activity-bar {
    flex: 1;
    height: 12px;
    background: $bg-light;
    border-radius: 6px;
    margin: 0 $spacing-sm;
    overflow: hidden;

    .activity-progress {
      height: 100%;
      transition: width 0.3s ease;

      &.high {
        background: $color-success;
      }
      &.medium {
        background: $color-warning;
      }
      &.low {
        background: $color-info;
      }
    }
  }

  .activity-value {
    font-size: 0.9rem;
    color: $text-primary;
    font-weight: 500;
    min-width: 120px;
  }
}

.department-list {
  .department-item {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .dept-info {
    display: flex;
    justify-content: space-between;
    width: 100px;
    margin-right: $spacing-sm;

    .dept-name {
      font-size: 0.9rem;
      color: $text-primary;
    }

    .dept-count {
      font-weight: 500;
      color: $text-secondary;
    }
  }

  .dept-bar {
    flex: 1;
    height: 8px;
    background: $bg-light;
    border-radius: 4px;
    margin-right: $spacing-sm;
    overflow: hidden;

    .dept-progress {
      height: 100%;
      background: $color-primary;
      transition: width 0.3s ease;
    }
  }

  .dept-percentage {
    font-size: 0.8rem;
    color: $text-secondary;
    width: 35px;
    text-align: right;
  }
}

.role-distribution {
  .role-item {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-lg;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .role-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-sm;

    &.user {
      background: rgba(64, 158, 255, 0.1);
      color: $color-primary;
    }
    &.admin {
      background: rgba(230, 162, 60, 0.1);
      color: $color-warning;
    }
    &.super {
      background: rgba(245, 108, 108, 0.1);
      color: $color-danger;
    }
  }

  .role-info {
    flex: 1;

    .role-name {
      font-size: 0.9rem;
      color: $text-primary;
      margin-bottom: 2px;
    }

    .role-count {
      font-size: 1.2rem;
      font-weight: bold;
      color: $text-primary;
    }
  }

  .role-percentage {
    font-size: 1rem;
    color: $text-secondary;
    font-weight: 500;
  }
}

.credit-distribution {
  .credit-item {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .credit-range {
    width: 60px;
    font-size: 0.9rem;
    font-weight: 500;

    &.excellent {
      color: $color-success;
    }
    &.good {
      color: $color-primary;
    }
    &.average {
      color: $color-warning;
    }
    &.poor {
      color: $color-danger;
    }
  }

  .credit-bar {
    flex: 1;
    height: 8px;
    background: $bg-light;
    border-radius: 4px;
    margin: 0 $spacing-sm;
    overflow: hidden;

    .credit-progress {
      height: 100%;
      transition: width 0.3s ease;

      &.excellent {
        background: $color-success;
      }
      &.good {
        background: $color-primary;
      }
      &.average {
        background: $color-warning;
      }
      &.poor {
        background: $color-danger;
      }
    }
  }

  .credit-stats {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    width: 50px;

    .credit-count {
      font-size: 0.9rem;
      color: $text-primary;
      font-weight: 500;
    }

    .credit-percentage {
      font-size: 0.8rem;
      color: $text-secondary;
    }
  }
}

.behavior-stats {
  .behavior-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $border-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .behavior-label {
    color: $text-secondary;
    font-size: 0.9rem;
  }

  .behavior-value {
    font-size: 1.2rem;
    font-weight: bold;
    color: $text-primary;
  }

  .behavior-trend {
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

.login-analysis {
  .time-slots {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    height: 200px;
    padding: $spacing-sm 0;
  }

  .time-slot {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
  }

  .slot-time {
    font-size: 0.8rem;
    color: $text-secondary;
    margin-bottom: $spacing-xs;
  }

  .slot-bar {
    width: 20px;
    height: 120px;
    background: $bg-light;
    border-radius: 10px;
    margin-bottom: $spacing-xs;
    display: flex;
    align-items: flex-end;
    overflow: hidden;

    .slot-progress {
      width: 100%;
      background: $color-primary;
      border-radius: 10px;
      transition: height 0.3s ease;
    }
  }

  .slot-count {
    font-size: 0.8rem;
    color: $text-primary;
    font-weight: 500;
  }
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;

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
  &.rank-other {
    background: $bg-light;
    color: $text-secondary;
  }
}
</style>
