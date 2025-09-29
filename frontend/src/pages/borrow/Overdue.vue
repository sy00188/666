<template>
  <div class="borrow-overdue">
    <div class="page-header">
      <h2>逾期管理</h2>
      <p>管理逾期未还的档案</p>
    </div>

    <div class="overdue-content">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ overdueStats.total }}</div>
              <div class="stat-label">逾期总数</div>
            </div>
            <el-icon class="stat-icon danger"><Warning /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ overdueStats.severe }}</div>
              <div class="stat-label">严重逾期</div>
            </div>
            <el-icon class="stat-icon danger"><CircleCloseFilled /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ overdueStats.reminded }}</div>
              <div class="stat-label">已催还</div>
            </div>
            <el-icon class="stat-icon warning"><Bell /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ overdueStats.penalty }}</div>
              <div class="stat-label">罚金总额</div>
            </div>
            <el-icon class="stat-icon success"><Money /></el-icon>
          </el-card>
        </el-col>
      </el-row>

      <!-- 逾期列表 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span>逾期档案列表</span>
            <div>
              <el-button type="warning" size="small" @click="handleBatchRemind">
                批量催还
              </el-button>
              <el-button type="danger" size="small" @click="handleBatchPenalty">
                批量罚金
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          :data="overdueList"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="borrower" label="借阅人" />
          <el-table-column prop="contact" label="联系方式" />
          <el-table-column prop="archiveTitle" label="档案名称" />
          <el-table-column prop="borrowTime" label="借阅时间" />
          <el-table-column prop="dueTime" label="应还时间" />
          <el-table-column prop="overdueDays" label="逾期天数">
            <template #default="{ row }">
              <el-tag :type="getOverdueType(row.overdueDays)">
                {{ row.overdueDays }} 天
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="penalty" label="罚金">
            <template #default="{ row }">
              <span class="penalty-amount">¥{{ row.penalty }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remindCount" label="催还次数" />
          <el-table-column label="操作" width="250">
            <template #default="{ row }">
              <el-button type="warning" size="small" @click="handleRemind(row)">
                催还
              </el-button>
              <el-button type="danger" size="small" @click="handlePenalty(row)">
                罚金
              </el-button>
              <el-button
                type="primary"
                size="small"
                @click="handleContact(row)"
              >
                联系
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 催还记录 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <span>催还记录</span>
        </template>

        <el-table :data="remindHistory" style="width: 100%">
          <el-table-column prop="borrower" label="借阅人" />
          <el-table-column prop="archiveTitle" label="档案名称" />
          <el-table-column prop="remindTime" label="催还时间" />
          <el-table-column prop="remindType" label="催还方式">
            <template #default="{ row }">
              <el-tag :type="getRemindTypeColor(row.remindType)">
                {{ row.remindType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" />
          <el-table-column prop="result" label="结果" />
        </el-table>
      </el-card>
    </div>

    <!-- 催还对话框 -->
    <el-dialog v-model="remindDialogVisible" title="发送催还通知" width="500px">
      <el-form :model="remindForm" label-width="100px">
        <el-form-item label="催还方式">
          <el-checkbox-group v-model="remindForm.methods">
            <el-checkbox value="email">邮件</el-checkbox>
            <el-checkbox value="sms">短信</el-checkbox>
            <el-checkbox value="phone">电话</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="催还内容">
          <el-input
            v-model="remindForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入催还内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRemind">发送催还</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Warning,
  CircleCloseFilled,
  Bell,
  Money,
} from "@element-plus/icons-vue";

const remindDialogVisible = ref(false);
const selectedOverdue = ref<any[]>([]);
const currentOverdue = ref<any>(null);

const remindForm = ref({
  methods: [],
  content: "",
});

const overdueStats = ref({
  total: 15,
  severe: 5,
  reminded: 8,
  penalty: 1250,
});

const overdueList = ref([
  {
    id: 1,
    borrower: "张三",
    contact: "13800138001",
    archiveTitle: "员工入职档案",
    borrowTime: "2024-01-01",
    dueTime: "2024-01-15",
    overdueDays: 10,
    penalty: 100,
    remindCount: 2,
  },
  {
    id: 2,
    borrower: "李四",
    contact: "13800138002",
    archiveTitle: "财务报表",
    borrowTime: "2023-12-20",
    dueTime: "2024-01-05",
    overdueDays: 25,
    penalty: 250,
    remindCount: 3,
  },
]);

const remindHistory = ref([
  {
    borrower: "王五",
    archiveTitle: "合同档案",
    remindTime: "2024-01-20 10:30",
    remindType: "邮件",
    operator: "管理员",
    result: "已回复",
  },
]);

const handleSelectionChange = (selection: any[]) => {
  selectedOverdue.value = selection;
};

const handleRemind = (row: any) => {
  currentOverdue.value = row;
  remindDialogVisible.value = true;
};

const handleBatchRemind = () => {
  if (selectedOverdue.value.length === 0) {
    ElMessage.warning("请选择要催还的档案");
    return;
  }

  ElMessageBox.confirm(
    `确定要向选中的 ${selectedOverdue.value.length} 个借阅人发送催还通知吗？`,
    "批量催还确认",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  ).then(() => {
    ElMessage.success("批量催还通知已发送");
  });
};

const handlePenalty = (row: any) => {
  ElMessageBox.confirm(
    `确定要对 ${row.borrower} 执行罚金操作吗？当前罚金：¥${row.penalty}`,
    "罚金确认",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  ).then(() => {
    ElMessage.success("罚金记录已生成");
  });
};

const handleBatchPenalty = () => {
  if (selectedOverdue.value.length === 0) {
    ElMessage.warning("请选择要执行罚金的档案");
    return;
  }

  const totalPenalty = selectedOverdue.value.reduce(
    (sum, item) => sum + item.penalty,
    0,
  );

  ElMessageBox.confirm(
    `确定要对选中的 ${selectedOverdue.value.length} 个借阅人执行罚金操作吗？总罚金：¥${totalPenalty}`,
    "批量罚金确认",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  ).then(() => {
    ElMessage.success("批量罚金记录已生成");
  });
};

const handleContact = (row: any) => {
  ElMessage.info(`联系 ${row.borrower}：${row.contact}`);
};

const confirmRemind = () => {
  ElMessage.success("催还通知已发送");
  remindDialogVisible.value = false;
  remindForm.value = {
    methods: [],
    content: "",
  };
};

const getOverdueType = (days: number) => {
  if (days >= 30) return "danger";
  if (days >= 15) return "warning";
  return "info";
};

const getRemindTypeColor = (type: string) => {
  switch (type) {
    case "邮件":
      return "primary";
    case "短信":
      return "success";
    case "电话":
      return "warning";
    default:
      return "info";
  }
};
</script>

<style lang="scss" scoped>
.borrow-overdue {
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
  .stat-content {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }

  .stat-number {
    font-size: 2rem;
    font-weight: bold;
    color: $text-primary;
  }

  .stat-label {
    color: $text-secondary;
    margin-top: $spacing-xs;
  }

  .stat-icon {
    position: absolute;
    right: $spacing-lg;
    top: 50%;
    transform: translateY(-50%);
    font-size: 2rem;

    &.danger {
      color: $color-danger;
    }
    &.warning {
      color: $color-warning;
    }
    &.success {
      color: $color-success;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.penalty-amount {
  color: $color-danger;
  font-weight: bold;
}
</style>
