<template>
  <div class="borrow-return">
    <div class="page-header">
      <h2>归还管理</h2>
      <p>管理档案归还流程</p>
    </div>

    <div class="return-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>待归还档案</span>
            <el-button type="primary" size="small" @click="handleBatchReturn">
              批量归还
            </el-button>
          </div>
        </template>

        <el-table
          :data="pendingReturns"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="borrower" label="借阅人" />
          <el-table-column prop="archiveTitle" label="档案名称" />
          <el-table-column prop="borrowTime" label="借阅时间" />
          <el-table-column prop="dueTime" label="应还时间" />
          <el-table-column prop="overdueDays" label="逾期天数">
            <template #default="{ row }">
              <el-tag v-if="row.overdueDays > 0" type="danger">
                逾期 {{ row.overdueDays }} 天
              </el-tag>
              <el-tag v-else type="success">未逾期</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleReturn(row)">
                确认归还
              </el-button>
              <el-button type="warning" size="small" @click="handleRemind(row)">
                催还提醒
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header>
          <span>归还记录</span>
        </template>

        <el-table :data="returnHistory" style="width: 100%">
          <el-table-column prop="borrower" label="借阅人" />
          <el-table-column prop="archiveTitle" label="档案名称" />
          <el-table-column prop="borrowTime" label="借阅时间" />
          <el-table-column prop="returnTime" label="归还时间" />
          <el-table-column prop="actualDays" label="实际借阅天数" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === '正常归还' ? 'success' : 'warning'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 归还确认对话框 -->
    <el-dialog v-model="returnDialogVisible" title="确认归还" width="500px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="档案状态">
          <el-radio-group v-model="returnForm.condition">
            <el-radio value="good">完好</el-radio>
            <el-radio value="damaged">损坏</el-radio>
            <el-radio value="lost">丢失</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请填写归还备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReturn">确认归还</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const returnDialogVisible = ref(false);
const selectedReturns = ref<any[]>([]);
const currentReturn = ref<any>(null);

const returnForm = ref({
  condition: "good",
  remark: "",
});

const pendingReturns = ref([
  {
    id: 1,
    borrower: "张三",
    archiveTitle: "员工入职档案",
    borrowTime: "2024-01-10",
    dueTime: "2024-01-25",
    overdueDays: 0,
  },
  {
    id: 2,
    borrower: "李四",
    archiveTitle: "财务报表",
    borrowTime: "2024-01-05",
    dueTime: "2024-01-20",
    overdueDays: 5,
  },
]);

const returnHistory = ref([
  {
    borrower: "王五",
    archiveTitle: "合同档案",
    borrowTime: "2024-01-01",
    returnTime: "2024-01-15",
    actualDays: 14,
    status: "正常归还",
  },
]);

const handleSelectionChange = (selection: any[]) => {
  selectedReturns.value = selection;
};

const handleReturn = (row: any) => {
  currentReturn.value = row;
  returnDialogVisible.value = true;
};

const handleBatchReturn = () => {
  if (selectedReturns.value.length === 0) {
    ElMessage.warning("请选择要归还的档案");
    return;
  }

  ElMessageBox.confirm(
    `确定要批量归还选中的 ${selectedReturns.value.length} 个档案吗？`,
    "批量归还确认",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  ).then(() => {
    ElMessage.success("批量归还成功");
    // 处理批量归还逻辑
  });
};

const handleRemind = (row: any) => {
  ElMessage.success(`已向 ${row.borrower} 发送催还提醒`);
};

const confirmReturn = () => {
  ElMessage.success("归还确认成功");
  returnDialogVisible.value = false;
  returnForm.value = {
    condition: "good",
    remark: "",
  };
};
</script>

<style lang="scss" scoped>
.borrow-return {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
