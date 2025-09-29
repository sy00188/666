<template>
  <div class="borrow-list">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1>借阅管理</h1>
        <p>管理档案借阅申请和归还记录</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleApply">
          <el-icon><Plus /></el-icon>
          新增借阅
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" v-for="stat in borrowStats" :key="stat.key">
        <div class="stat-icon" :style="{ backgroundColor: stat.color }">
          <el-icon>
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="借阅编号">
          <el-input
            v-model="searchForm.borrowNo"
            placeholder="请输入借阅编号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="档案标题">
          <el-input
            v-model="searchForm.archiveTitle"
            placeholder="请输入档案标题"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="借阅人">
          <el-input
            v-model="searchForm.borrower"
            placeholder="请输入借阅人姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="借阅状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="待审核" value="pending" />
            <el-option label="已批准" value="approved" />
            <el-option label="借阅中" value="borrowing" />
            <el-option label="已归还" value="returned" />
            <el-option label="逾期" value="overdue" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <div class="table-info">
          <span>共 {{ pagination.total }} 条记录</span>
        </div>
        <div class="table-actions">
          <el-button-group>
            <el-button
              @click="handleBatchApprove"
              :disabled="selectedRows.length === 0"
            >
              批量审批
            </el-button>
            <el-button
              @click="handleBatchReject"
              :disabled="selectedRows.length === 0"
            >
              批量拒绝
            </el-button>
          </el-button-group>
        </div>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column
          prop="borrowNo"
          label="借阅编号"
          width="120"
          sortable="custom"
        />
        <el-table-column
          prop="archiveTitle"
          label="档案标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="borrower" label="借阅人" width="100" />
        <el-table-column
          prop="borrowDate"
          label="借阅时间"
          width="180"
          sortable="custom"
        />
        <el-table-column
          prop="returnDate"
          label="应还时间"
          width="180"
          sortable="custom"
        >
          <template #default="{ row }">
            <span :class="{ 'overdue-text': isOverdue(row) }">
              {{ row.returnDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="actualReturnDate" label="实际归还" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="100">
          <template #default="{ row }">
            <span v-if="row.overdueDays > 0" class="overdue-text">
              {{ row.overdueDays }} 天
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="text"
              size="small"
              @click="handleApprove(row)"
            >
              审批
            </el-button>
            <el-button
              v-if="row.status === 'borrowing'"
              type="text"
              size="small"
              @click="handleReturn(row)"
            >
              归还
            </el-button>
            <el-button
              v-if="row.status === 'overdue'"
              type="text"
              size="small"
              @click="handleRemind(row)"
            >
              催还
            </el-button>
            <el-dropdown @command="(command) => handleMoreAction(command, row)">
              <el-button type="text" size="small">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="history"
                    >借阅历史</el-dropdown-item
                  >
                  <el-dropdown-item command="extend">延期申请</el-dropdown-item>
                  <el-dropdown-item command="cancel" divided
                    >取消借阅</el-dropdown-item
                  >
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="借阅审批"
      width="500px"
      @close="resetApproveForm"
    >
      <el-form
        :model="approveForm"
        :rules="approveRules"
        ref="approveFormRef"
        label-width="80px"
      >
        <el-form-item label="审批结果" prop="result">
          <el-radio-group v-model="approveForm.result">
            <el-radio label="approved">批准</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="借阅期限"
          prop="duration"
          v-if="approveForm.result === 'approved'"
        >
          <el-input-number
            v-model="approveForm.duration"
            :min="1"
            :max="30"
            controls-position="right"
            style="width: 120px"
          />
          <span style="margin-left: 8px">天</span>
        </el-form-item>
        <el-form-item label="审批意见" prop="remark">
          <el-input
            v-model="approveForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>

    <!-- 归还对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="档案归还"
      width="500px"
      @close="resetReturnForm"
    >
      <el-form
        :model="returnForm"
        :rules="returnRules"
        ref="returnFormRef"
        label-width="80px"
      >
        <el-form-item label="归还状态" prop="condition">
          <el-radio-group v-model="returnForm.condition">
            <el-radio label="good">完好</el-radio>
            <el-radio label="damaged">损坏</el-radio>
            <el-radio label="lost">丢失</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="归还说明" prop="remark">
          <el-input
            v-model="returnForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入归还说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Download,
  Search,
  Refresh,
  ArrowDown,
} from "@element-plus/icons-vue";

const router = useRouter();

// 加载状态
const loading = ref(false);

// 对话框显示状态
const approveDialogVisible = ref(false);
const returnDialogVisible = ref(false);

// 表单引用
const approveFormRef = ref();
const returnFormRef = ref();

// 搜索表单
const searchForm = reactive({
  borrowNo: "",
  archiveTitle: "",
  borrower: "",
  status: "",
  dateRange: null as [string, string] | null,
});

// 审批表单
const approveForm = reactive({
  result: "approved",
  duration: 7,
  remark: "",
});

// 归还表单
const returnForm = reactive({
  condition: "good",
  remark: "",
});

// 当前操作的行
const currentRow = ref<any>(null);

// 借阅统计
const borrowStats = ref([
  {
    key: "pending",
    label: "待审核",
    value: "12",
    color: "#e6a23c",
    icon: "Clock",
  },
  {
    key: "borrowing",
    label: "借阅中",
    value: "45",
    color: "#409eff",
    icon: "Reading",
  },
  {
    key: "overdue",
    label: "逾期未还",
    value: "8",
    color: "#f56c6c",
    icon: "Warning",
  },
  {
    key: "returned",
    label: "本月归还",
    value: "156",
    color: "#67c23a",
    icon: "Check",
  },
]);

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0,
});

// 表格数据
const tableData = ref([
  {
    id: 1,
    borrowNo: "BR2024001",
    archiveTitle: "2024年度财务报告",
    borrower: "张三",
    borrowDate: "2024-01-15 10:30:00",
    returnDate: "2024-01-22 10:30:00",
    actualReturnDate: "",
    status: "borrowing",
    overdueDays: 0,
  },
  {
    id: 2,
    borrowNo: "BR2024002",
    archiveTitle: "项目技术文档",
    borrower: "李四",
    borrowDate: "2024-01-10 14:20:00",
    returnDate: "2024-01-17 14:20:00",
    actualReturnDate: "",
    status: "overdue",
    overdueDays: 5,
  },
  {
    id: 3,
    borrowNo: "BR2024003",
    archiveTitle: "会议纪要合集",
    borrower: "王五",
    borrowDate: "2024-01-08 09:15:00",
    returnDate: "2024-01-15 09:15:00",
    actualReturnDate: "2024-01-14 16:30:00",
    status: "returned",
    overdueDays: 0,
  },
  {
    id: 4,
    borrowNo: "BR2024004",
    archiveTitle: "产品宣传资料",
    borrower: "赵六",
    borrowDate: "",
    returnDate: "",
    actualReturnDate: "",
    status: "pending",
    overdueDays: 0,
  },
]);

// 选中的行
const selectedRows = ref<any[]>([]);

// 表单验证规则
const approveRules = {
  result: [{ required: true, message: "请选择审批结果", trigger: "change" }],
  duration: [{ required: true, message: "请设置借阅期限", trigger: "blur" }],
};

const returnRules = {
  condition: [{ required: true, message: "请选择归还状态", trigger: "change" }],
};

// 判断是否逾期
const isOverdue = (row: { status: string; returnDate?: string }) => {
  if (row.status === "returned" || !row.returnDate) return false;
  return new Date() > new Date(row.returnDate);
};

// 获取状态标签类型
const getStatusTagType = (
  status: string,
): "primary" | "success" | "warning" | "info" | "danger" => {
  const statusMap: Record<
    string,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    pending: "warning",
    approved: "primary",
    borrowing: "info",
    returned: "success",
    overdue: "danger",
    rejected: "danger",
  };
  return statusMap[status] || "info";
};

// 获取状态名称
const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: "待审核",
    approved: "已批准",
    borrowing: "借阅中",
    returned: "已归还",
    overdue: "逾期",
    rejected: "已拒绝",
  };
  return statusMap[status] || "未知";
};

// 搜索
const handleSearch = () => {
  loading.value = true;
  // 模拟API调用
  setTimeout(() => {
    loading.value = false;
    ElMessage.success("搜索完成");
  }, 1000);
};

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    borrowNo: "",
    archiveTitle: "",
    borrower: "",
    status: "",
    dateRange: null,
  });
  handleSearch();
};

// 新增借阅
const handleApply = () => {
  router.push("/borrow/apply");
};

// 导出记录
const handleExport = () => {
  ElMessage.info("导出功能开发中...");
};

// 查看详情
const handleView = (row: { id: string }) => {
  router.push(`/borrow/detail/${row.id}`);
};

// 审批
const handleApprove = (row: unknown) => {
  currentRow.value = row;
  approveDialogVisible.value = true;
};

// 归还
const handleReturn = (row: unknown) => {
  currentRow.value = row;
  returnDialogVisible.value = true;
};

// 催还提醒
const handleRemind = (row: { borrower: string }) => {
  ElMessageBox.confirm(
    `确定要向"${row.borrower}"发送催还提醒吗？`,
    "催还提醒",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    },
  ).then(() => {
    ElMessage.success("催还提醒已发送");
  });
};

// 批量审批
const handleBatchApprove = () => {
  ElMessage.info("批量审批功能开发中...");
};

// 批量拒绝
const handleBatchReject = () => {
  ElMessage.info("批量拒绝功能开发中...");
};

// 更多操作
const handleMoreAction = (
  command: string,
  row: { id: string; archiveTitle: string },
) => {
  switch (command) {
    case "history":
      router.push(`/borrow/history/${row.id}`);
      break;
    case "extend":
      ElMessage.info("延期申请功能开发中...");
      break;
    case "cancel":
      handleCancel(row);
      break;
  }
};

// 取消借阅
const handleCancel = (row: { archiveTitle: string }) => {
  ElMessageBox.confirm(`确定要取消借阅"${row.archiveTitle}"吗？`, "取消借阅", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    ElMessage.success("借阅已取消");
  });
};

// 提交审批
const submitApprove = () => {
  approveFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success("审批完成");
      approveDialogVisible.value = false;
      resetApproveForm();
    }
  });
};

// 提交归还
const submitReturn = () => {
  returnFormRef.value?.validate((valid: boolean) => {
    if (valid) {
      ElMessage.success("归还完成");
      returnDialogVisible.value = false;
      resetReturnForm();
    }
  });
};

// 重置审批表单
const resetApproveForm = () => {
  Object.assign(approveForm, {
    result: "approved",
    duration: 7,
    remark: "",
  });
  currentRow.value = null;
};

// 重置归还表单
const resetReturnForm = () => {
  Object.assign(returnForm, {
    condition: "good",
    remark: "",
  });
  currentRow.value = null;
};

// 选择变化
const handleSelectionChange = (selection: unknown[]) => {
  selectedRows.value = selection;
};

// 排序变化
const handleSortChange = ({
  column,
  prop,
  order,
}: {
  column: unknown;
  prop: string;
  order: string;
}) => {
  console.log("排序变化:", { column, prop, order });
  // 实现排序逻辑
};

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size;
  handleSearch();
};

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.currentPage = page;
  handleSearch();
};

// 初始化数据
const initData = () => {
  pagination.total = tableData.value.length;
};

onMounted(() => {
  initData();
});
</script>

<style lang="scss" scoped>
.borrow-list {
  padding: $spacing-lg;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;

  .header-left {
    h1 {
      font-size: $font-size-extra-large;
      color: $text-primary;
      margin-bottom: $spacing-xs;
    }

    p {
      color: $text-regular;
      font-size: $font-size-medium;
    }
  }

  .header-right {
    display: flex;
    gap: $spacing-md;
  }
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  background: $bg-color;
  border-radius: $border-radius-base;
  box-shadow: $box-shadow-light;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-md;

  .el-icon {
    font-size: $font-size-large;
    color: white;
  }
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: $font-size-large;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.stat-label {
  font-size: $font-size-medium;
  color: $text-regular;
}

.search-card {
  margin-bottom: $spacing-lg;

  .search-form {
    .el-form-item {
      margin-bottom: $spacing-md;
    }
  }
}

.table-card {
  .table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-lg;

    .table-info {
      color: $text-regular;
    }
  }
}

.overdue-text {
  color: $danger-color;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-lg;
}

// 响应式设计
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-md;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .search-form {
    .el-form-item {
      display: block;
      margin-right: 0;
    }
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-md;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
