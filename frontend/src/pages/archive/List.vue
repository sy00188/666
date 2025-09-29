<template>
  <div class="archive-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>档案管理</h2>
        <p>管理和查看所有档案信息</p>
      </div>
      <div class="header-right">
        <el-button
          type="primary"
          @click="handleAdd"
          v-permission="'archive:create'"
        >
          <el-icon><Plus /></el-icon>
          新增档案
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ archiveStats.total }}</div>
          <div class="stat-label">总档案数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon draft">
          <el-icon><Edit /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">
            {{ archiveStats.byStatus[ArchiveStatus.DRAFT] || 0 }}
          </div>
          <div class="stat-label">草稿</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon reviewing">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">
            {{ archiveStats.byStatus[ArchiveStatus.REVIEWING] || 0 }}
          </div>
          <div class="stat-label">审核中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon approved">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">
            {{ archiveStats.byStatus[ArchiveStatus.APPROVED] || 0 }}
          </div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="档案编号">
          <el-input
            v-model="searchForm.archiveNo"
            placeholder="请输入档案编号"
            clearable
          />
        </el-form-item>
        <el-form-item label="档案标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入档案标题"
            clearable
          />
        </el-form-item>
        <el-form-item label="档案类别">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择类别"
            clearable
          >
            <el-option
              v-for="category in categories"
              :key="category.categoryId"
              :label="category.categoryName"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="安全级别">
          <el-select
            v-model="searchForm.securityLevel"
            placeholder="请选择安全级别"
            clearable
          >
            <el-option label="公开" :value="SecurityLevel.PUBLIC" />
            <el-option label="内部" :value="SecurityLevel.INTERNAL" />
            <el-option label="机密" :value="SecurityLevel.CONFIDENTIAL" />
            <el-option label="绝密" :value="SecurityLevel.SECRET" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
          >
            <el-option
              v-for="option in archiveStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
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
    </div>

    <!-- 视图切换和操作栏 -->
    <div class="toolbar">
      <div class="view-toggle">
        <el-radio-group v-model="viewMode">
          <el-radio-button label="table">
            <el-icon><Grid /></el-icon>
            表格视图
          </el-radio-button>
          <el-radio-button label="card">
            <el-icon><Postcard /></el-icon>
            卡片视图
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="toolbar-actions">
        <el-button
          type="danger"
          :disabled="selectedArchives.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-if="viewMode === 'table'" class="table-view">
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="archiveId" label="ID" width="80" />
        <el-table-column prop="archiveNo" label="档案编号" width="150" />
        <el-table-column
          prop="title"
          label="标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="categoryName" label="类型" width="120" />
        <el-table-column prop="securityLevelName" label="安全级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getSecurityLevelTagType(row.securityLevel)">
              {{ row.securityLevelName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileCount" label="文件数" width="80" />
        <el-table-column prop="submitUserName" label="创建者" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="handleView(row)"
              v-permission="'archive:view'"
            >
              查看
            </el-button>
            <el-button
              v-if="canEdit(row)"
              size="small"
              type="primary"
              @click="handleEdit(row)"
              v-permission="'archive:edit'"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleBorrow(row)"
              v-permission="'borrow:create'"
            >
              借阅
            </el-button>
            <el-dropdown @command="(command) => handleMoreAction(command, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    command="download"
                    v-permission-show="'archive:export'"
                  >
                    下载
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="delete"
                    divided
                    v-permission-show="'archive:delete'"
                  >
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 卡片视图 -->
    <div v-else class="card-view">
      <div v-loading="loading" class="archive-cards">
        <div
          v-for="archive in tableData"
          :key="archive.archiveId"
          class="archive-card"
          @click="handleView(archive)"
        >
          <div class="archive-card-header">
            <div class="archive-card-title">{{ archive.title }}</div>
            <el-tag :type="getStatusTagType(archive.status)" size="small">
              {{ archive.statusName }}
            </el-tag>
          </div>
          <div class="archive-card-content">
            <p class="archive-card-desc">
              {{ archive.description || "暂无描述" }}
            </p>
            <div class="archive-card-info">
              <span class="info-item">
                <el-icon><Folder /></el-icon>
                {{ archive.categoryName }}
              </span>
              <span class="info-item">
                <el-icon><Lock /></el-icon>
                {{ archive.securityLevelName }}
              </span>
              <span class="info-item">
                <el-icon><Document /></el-icon>
                {{ archive.fileCount }} 个文件
              </span>
            </div>
          </div>
          <div class="archive-card-footer">
            <div class="archive-card-meta">
              <span>{{ archive.submitUserName }}</span>
              <span>{{ formatDate(archive.createdAt) }}</span>
            </div>
            <div class="archive-card-actions" @click.stop>
              <el-button
                size="small"
                @click="handleEdit(archive)"
                v-permission="'archive:edit'"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                type="success"
                @click="handleBorrow(archive)"
                v-permission="'borrow:create'"
              >
                借阅
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Download,
  Document,
  Edit,
  Clock,
  Check,
  Search,
  Refresh,
  Grid,
  Postcard,
  Delete,
  ArrowDown,
  Folder,
  Lock,
} from "@element-plus/icons-vue";
import { archiveApi } from "@/api/modules/archive";
import { ArchiveStatus, SecurityLevel } from "@/types/archive";
import type {
  Archive,
  ArchiveSearchParams,
  ArchiveStats,
  ArchiveCategory,
} from "@/types/archive";

const router = useRouter();

// 响应式数据
const loading = ref(false);
const viewMode = ref<"table" | "card">("table");
const selectedArchives = ref<Archive[]>([]);
const dateRange = ref<[string, string] | null>(null);

// 搜索表单
const searchForm = reactive<ArchiveSearchParams>({
  page: 1,
  size: 20,
  archiveNo: "",
  title: "",
  categoryId: undefined,
  securityLevel: undefined,
  status: undefined,
  submitUserId: undefined,
  startTime: "",
  endTime: "",
  keywords: "",
});

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0,
  pages: 0,
});

// 表格数据
const tableData = ref<Archive[]>([]);

// 档案统计数据
const archiveStats = ref<ArchiveStats>({
  total: 0,
  byStatus: {} as Record<ArchiveStatus, number>,
  byCategory: [],
  bySecurityLevel: {} as Record<SecurityLevel, number>,
  recentCount: 0,
});

// 档案类别选项
const categories = ref<ArchiveCategory[]>([]);

// 档案状态选项
const archiveStatusOptions = ref([
  { label: "草稿", value: ArchiveStatus.DRAFT },
  { label: "已提交", value: ArchiveStatus.SUBMITTED },
  { label: "审核中", value: ArchiveStatus.REVIEWING },
  { label: "已通过", value: ArchiveStatus.APPROVED },
  { label: "已拒绝", value: ArchiveStatus.REJECTED },
  { label: "已归档", value: ArchiveStatus.ARCHIVED },
]);

// 获取安全级别标签类型
const getSecurityLevelTagType = (
  level: SecurityLevel,
): "primary" | "success" | "warning" | "danger" => {
  const levelMap: Record<
    SecurityLevel,
    "primary" | "success" | "warning" | "danger"
  > = {
    [SecurityLevel.PUBLIC]: "primary",
    [SecurityLevel.INTERNAL]: "success",
    [SecurityLevel.CONFIDENTIAL]: "warning",
    [SecurityLevel.SECRET]: "danger",
  };
  return levelMap[level] || "primary";
};

// 获取状态标签类型
const getStatusTagType = (
  status: ArchiveStatus,
): "primary" | "success" | "warning" | "info" | "danger" => {
  const statusMap: Record<
    ArchiveStatus,
    "primary" | "success" | "warning" | "info" | "danger"
  > = {
    [ArchiveStatus.DRAFT]: "info",
    [ArchiveStatus.SUBMITTED]: "warning",
    [ArchiveStatus.REVIEWING]: "warning",
    [ArchiveStatus.APPROVED]: "success",
    [ArchiveStatus.REJECTED]: "danger",
    [ArchiveStatus.ARCHIVED]: "primary",
  };
  return statusMap[status] || "info";
};

// 判断是否可以编辑
const canEdit = (archive: Archive) => {
  return (
    archive.status === ArchiveStatus.DRAFT ||
    archive.status === ArchiveStatus.REJECTED
  );
};

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString("zh-CN");
};

// 获取档案列表
const getArchiveList = async () => {
  try {
    loading.value = true;
    const response = await archiveApi.getArchiveList(searchForm);

    if (response.success) {
      tableData.value = response.data.list;
      pagination.page = response.data.page;
      pagination.size = response.data.size;
      pagination.total = response.data.total;
      pagination.pages = Math.ceil(response.data.total / response.data.size);
    } else {
      ElMessage.error(response.message || "获取档案列表失败");
    }
  } catch (error) {
    console.error("获取档案列表失败:", error);
    ElMessage.error("获取档案列表失败");
  } finally {
    loading.value = false;
  }
};

// 获取档案统计
const getArchiveStats = async () => {
  try {
    const response = await archiveApi.getArchiveStatistics();
    if (response.success) {
      archiveStats.value = response.data;
    }
  } catch (error) {
    console.error("获取档案统计失败:", error);
  }
};

// 获取档案类别
const getCategories = async () => {
  try {
    const response = await archiveApi.getArchiveCategories();
    if (response.success) {
      categories.value = response.data;
    }
  } catch (error) {
    console.error("获取档案类别失败:", error);
  }
};

// 搜索
const handleSearch = () => {
  searchForm.page = 1;
  pagination.page = 1;
  getArchiveList();
};

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    page: 1,
    size: 20,
    archiveNo: "",
    title: "",
    categoryId: undefined,
    securityLevel: undefined,
    status: undefined,
    submitUserId: undefined,
    startTime: "",
    endTime: "",
    keywords: "",
  });
  dateRange.value = null;
  getArchiveList();
};

// 日期范围变化
const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates) {
    searchForm.startTime = dates[0];
    searchForm.endTime = dates[1];
  } else {
    searchForm.startTime = "";
    searchForm.endTime = "";
  }
};

// 新增档案
const handleAdd = () => {
  router.push("/archive/create");
};

// 导出数据
const handleExport = async () => {
  try {
    ElMessage.info("正在导出数据...");
    await archiveApi.exportArchives(searchForm);
    ElMessage.success("导出成功");
  } catch (error) {
    console.error("导出失败:", error);
    ElMessage.error("导出失败");
  }
};

// 查看档案
const handleView = (archive: Archive) => {
  router.push(`/archive/detail/${archive.archiveId}`);
};

// 编辑档案
const handleEdit = (archive: Archive) => {
  router.push(`/archive/edit/${archive.archiveId}`);
};

// 借阅档案
const handleBorrow = (archive: Archive) => {
  router.push(`/borrow/apply?archiveId=${archive.archiveId}`);
};

// 更多操作
const handleMoreAction = async (command: string, archive: Archive) => {
  switch (command) {
    case "download":
      await downloadArchive(archive);
      break;
    case "delete":
      await handleDelete(archive);
      break;
  }
};

// 下载档案
const downloadArchive = async (archive: Archive) => {
  try {
    const fileId =
      archive.files && archive.files.length > 0 ? archive.files[0].fileId : 0;
    const blob = await archiveApi.downloadArchiveFile(
      archive.archiveId,
      fileId,
    );
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${archive.title}.zip`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    ElMessage.success("档案下载成功");
  } catch (error) {
    console.error("下载档案失败:", error);
    ElMessage.error("下载档案失败");
  }
};

// 删除档案
const handleDelete = async (archive: Archive) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除档案"${archive.title}"吗？此操作不可恢复。`,
      "确认删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const response = await archiveApi.deleteArchive(archive.archiveId);
    if (response.code === 200) {
      ElMessage.success("删除成功");
      getArchiveList();
      getArchiveStats();
    } else {
      ElMessage.error(response.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("删除失败:", error);
      ElMessage.error("删除失败");
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  if (selectedArchives.value.length === 0) {
    ElMessage.warning("请选择要删除的档案");
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedArchives.value.length} 个档案吗？此操作不可恢复。`,
      "确认批量删除",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      },
    );

    const archiveIds = selectedArchives.value.map(
      (archive) => archive.archiveId,
    );
    const response = await archiveApi.batchDeleteArchives(archiveIds);

    if (response.code === 200) {
      ElMessage.success("批量删除成功");
      selectedArchives.value = [];
      getArchiveList();
      getArchiveStats();
    } else {
      ElMessage.error(response.message || "批量删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      console.error("批量删除失败:", error);
      ElMessage.error("批量删除失败");
    }
  }
};

// 选择变化
const handleSelectionChange = (selection: Archive[]) => {
  selectedArchives.value = selection;
};

// 排序变化
const handleSortChange = ({ prop, order }: any) => {
  // 实现排序逻辑
  if (prop && order) {
    // 更新搜索参数并重新获取数据
    getArchiveList();
  }
};

// 分页大小变化
const handleSizeChange = (size: number) => {
  searchForm.size = size;
  pagination.size = size;
  getArchiveList();
};

// 当前页变化
const handleCurrentChange = (page: number) => {
  searchForm.page = page;
  pagination.page = page;
  getArchiveList();
};

// 初始化数据
const initData = async () => {
  await Promise.all([getArchiveList(), getArchiveStats(), getCategories()]);
};

// 组件挂载时初始化数据
onMounted(() => {
  initData();
});
</script>

<style scoped>
.archive-list-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0 0 5px 0;
  color: #333;
}

.header-left p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.draft {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.reviewing {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.approved {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.search-section {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-view {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-view {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.archive-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.archive-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.archive-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.archive-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.archive-card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  flex: 1;
  margin-right: 10px;
}

.archive-card-content {
  margin-bottom: 15px;
}

.archive-card-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.archive-card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.info-item .el-icon {
  margin-right: 4px;
}

.archive-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.archive-card-meta {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: #999;
}

.archive-card-meta span:first-child {
  margin-bottom: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}
</style>
