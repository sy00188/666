<template>
  <div class="report-generator">
    <el-card class="generator-card">
      <template #header>
        <div class="card-header">
          <h3>报表生成器</h3>
          <el-button
            type="primary"
            :loading="loading.generating"
            :disabled="!isFormValid"
            @click="handleGenerate"
          >
            <el-icon><Document /></el-icon>
            生成报表
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="reportConfig"
        :rules="formRules"
        label-width="120px"
        class="generator-form"
      >
        <!-- 基本信息 -->
        <el-form-item label="报表名称" prop="name">
          <el-input
            v-model="reportConfig.name"
            placeholder="请输入报表名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="报表类型" prop="type">
          <el-select
            v-model="reportConfig.type"
            placeholder="请选择报表类型"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option
              v-for="option in reportTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            >
              <div class="option-item">
                <span>{{ option.label }}</span>
                <span class="option-desc">{{ option.description }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="输出格式" prop="format">
          <el-radio-group v-model="reportConfig.format">
            <el-radio value="pdf">
              <el-icon><Document /></el-icon>
              PDF
            </el-radio>
            <el-radio value="excel">
              <el-icon><Grid /></el-icon>
              Excel
            </el-radio>
            <el-radio value="csv">
              <el-icon><List /></el-icon>
              CSV
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 时间范围 -->
        <el-form-item label="时间范围" prop="dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="handleDateRangeChange"
          />
        </el-form-item>

        <!-- 报表模板 -->
        <el-form-item label="报表模板" prop="template">
          <el-select
            v-model="reportConfig.template"
            placeholder="请选择报表模板（可选）"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="template in availableTemplates"
              :key="template.id"
              :label="template.name"
              :value="template.id"
            >
              <div class="template-option">
                <span>{{ template.name }}</span>
                <el-tag v-if="template.isDefault" size="small" type="primary"
                  >默认</el-tag
                >
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 高级选项 -->
        <el-collapse v-model="activeCollapse" class="advanced-options">
          <el-collapse-item title="高级选项" name="advanced">
            <el-form-item label="包含图表">
              <el-checkbox-group v-model="reportConfig.charts">
                <el-checkbox
                  v-for="chart in availableCharts"
                  :key="chart.value"
                  :value="chart.value"
                >
                  {{ chart.label }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="数据选项">
              <el-checkbox v-model="reportConfig.includeRawData">
                包含原始数据
              </el-checkbox>
            </el-form-item>

            <el-form-item
              label="自定义字段"
              v-if="reportConfig.type === 'custom'"
            >
              <el-select
                v-model="reportConfig.customFields"
                multiple
                placeholder="请选择要包含的字段"
                style="width: 100%"
              >
                <el-option
                  v-for="field in availableFields"
                  :key="field.value"
                  :label="field.label"
                  :value="field.value"
                />
              </el-select>
            </el-form-item>

            <!-- 筛选条件 -->
            <el-form-item label="筛选条件" v-if="showFilters">
              <div class="filters-container">
                <div
                  v-for="(filter, index) in filters"
                  :key="index"
                  class="filter-item"
                >
                  <el-select
                    v-model="filter.field"
                    placeholder="字段"
                    style="width: 120px"
                  >
                    <el-option
                      v-for="field in filterFields"
                      :key="field.value"
                      :label="field.label"
                      :value="field.value"
                    />
                  </el-select>

                  <el-select
                    v-model="filter.operator"
                    placeholder="条件"
                    style="width: 100px"
                  >
                    <el-option label="等于" value="eq" />
                    <el-option label="不等于" value="ne" />
                    <el-option label="包含" value="contains" />
                    <el-option label="大于" value="gt" />
                    <el-option label="小于" value="lt" />
                  </el-select>

                  <el-input
                    v-model="filter.value"
                    placeholder="值"
                    style="width: 150px"
                  />

                  <el-button
                    type="danger"
                    size="small"
                    :icon="Delete"
                    @click="removeFilter(index)"
                  />
                </div>

                <el-button
                  type="primary"
                  size="small"
                  :icon="Plus"
                  @click="addFilter"
                >
                  添加筛选条件
                </el-button>
              </div>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>

        <!-- 定时生成 -->
        <el-collapse v-model="activeCollapse" class="schedule-options">
          <el-collapse-item title="定时生成" name="schedule">
            <el-form-item label="启用定时">
              <el-switch v-model="scheduleEnabled" />
            </el-form-item>

            <template v-if="scheduleEnabled">
              <el-form-item label="生成频率">
                <el-radio-group v-model="schedule.frequency">
                  <el-radio value="daily">每日</el-radio>
                  <el-radio value="weekly">每周</el-radio>
                  <el-radio value="monthly">每月</el-radio>
                  <el-radio value="quarterly">每季度</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="生成时间">
                <el-time-picker
                  v-model="schedule.time"
                  format="HH:mm"
                  value-format="HH:mm"
                  placeholder="选择时间"
                />
              </el-form-item>

              <el-form-item
                v-if="schedule.frequency === 'weekly'"
                label="星期几"
              >
                <el-select
                  v-model="schedule.dayOfWeek"
                  placeholder="选择星期几"
                >
                  <el-option label="周日" :value="0" />
                  <el-option label="周一" :value="1" />
                  <el-option label="周二" :value="2" />
                  <el-option label="周三" :value="3" />
                  <el-option label="周四" :value="4" />
                  <el-option label="周五" :value="5" />
                  <el-option label="周六" :value="6" />
                </el-select>
              </el-form-item>

              <el-form-item
                v-if="schedule.frequency === 'monthly'"
                label="每月几号"
              >
                <el-input-number
                  v-model="schedule.dayOfMonth"
                  :min="1"
                  :max="31"
                  placeholder="1-31"
                />
              </el-form-item>

              <el-form-item label="邮件通知">
                <el-input
                  v-model="recipientsInput"
                  placeholder="输入邮箱地址，多个用逗号分隔"
                  @blur="handleRecipientsChange"
                />
                <div v-if="schedule.recipients?.length" class="recipients-tags">
                  <el-tag
                    v-for="(email, index) in schedule.recipients"
                    :key="index"
                    closable
                    @close="removeRecipient(index)"
                  >
                    {{ email }}
                  </el-tag>
                </div>
              </el-form-item>

              <el-form-item label="自动删除">
                <el-checkbox v-model="schedule.autoDelete">
                  自动删除过期报表
                </el-checkbox>
                <el-input-number
                  v-if="schedule.autoDelete"
                  v-model="schedule.deleteAfterDays"
                  :min="1"
                  :max="365"
                  style="margin-left: 10px; width: 120px"
                />
                <span v-if="schedule.autoDelete" style="margin-left: 5px"
                  >天后删除</span
                >
              </el-form-item>
            </template>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </el-card>

    <!-- 预览区域 -->
    <el-card v-if="showPreview" class="preview-card">
      <template #header>
        <h4>报表预览</h4>
      </template>
      <div class="preview-content">
        <div class="preview-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="报表名称">
              {{ reportConfig.name }}
            </el-descriptions-item>
            <el-descriptions-item label="报表类型">
              {{ getTypeLabel(reportConfig.type) }}
            </el-descriptions-item>
            <el-descriptions-item label="输出格式">
              {{ reportConfig.format?.toUpperCase() }}
            </el-descriptions-item>
            <el-descriptions-item label="时间范围">
              {{ formatDateRange() }}
            </el-descriptions-item>
            <el-descriptions-item label="包含图表" :span="2">
              <el-tag
                v-for="chart in reportConfig.charts"
                :key="chart"
                size="small"
                style="margin-right: 5px"
              >
                {{ getChartLabel(chart) }}
              </el-tag>
              <span v-if="!reportConfig.charts?.length">无</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Document, Grid, List, Delete, Plus } from "@element-plus/icons-vue";
import { useReports } from "@/composables/useReports";
import type {
  ReportConfig,
  ReportType,
  ReportFormat,
  ReportSchedule,
} from "@/types/report";

// Composables
const { templates, loading, generateReport, loadTemplates } = useReports();

// 表单引用
const formRef = ref();

// 报表配置
const reportConfig = reactive<ReportConfig>({
  name: "",
  type: "archive_statistics" as ReportType,
  format: "pdf" as ReportFormat,
  dateRange: {
    startDate: "",
    endDate: "",
  },
  charts: [],
  includeRawData: false,
  customFields: [],
  template: "",
});

// 日期范围
const dateRange = ref<[string, string]>(["", ""]);

// 定时配置
const scheduleEnabled = ref(false);
const schedule = reactive<ReportSchedule>({
  enabled: false,
  frequency: "daily",
  time: "09:00",
  recipients: [],
  autoDelete: false,
  deleteAfterDays: 30,
});

// 收件人输入
const recipientsInput = ref("");

// 筛选条件
const filters = ref<
  Array<{
    field: string;
    operator: string;
    value: string;
  }>
>([]);

// UI 状态
const activeCollapse = ref<string[]>([]);
const showPreview = ref(false);

// 报表类型选项
const reportTypeOptions = [
  {
    value: "archive_statistics",
    label: "档案统计报表",
    description: "档案数量、分类、状态统计",
  },
  {
    value: "borrow_analysis",
    label: "借阅分析报表",
    description: "借阅趋势、热门档案分析",
  },
  {
    value: "user_activity",
    label: "用户活动报表",
    description: "用户行为、活跃度分析",
  },
  {
    value: "system_performance",
    label: "系统性能报表",
    description: "系统运行状态、性能指标",
  },
  {
    value: "custom",
    label: "自定义报表",
    description: "根据需求自定义内容",
  },
];

// 可用图表
const availableCharts = computed(() => {
  const baseCharts = [
    { value: "trend", label: "趋势图" },
    { value: "pie", label: "饼图" },
    { value: "bar", label: "柱状图" },
    { value: "line", label: "折线图" },
  ];

  switch (reportConfig.type) {
    case "archive_statistics":
      return [
        ...baseCharts,
        { value: "category", label: "分类统计图" },
        { value: "status", label: "状态分布图" },
      ];
    case "borrow_analysis":
      return [
        ...baseCharts,
        { value: "heatmap", label: "热力图" },
        { value: "ranking", label: "排行榜" },
      ];
    case "user_activity":
      return [
        ...baseCharts,
        { value: "activity", label: "活动分布图" },
        { value: "behavior", label: "行为分析图" },
      ];
    default:
      return baseCharts;
  }
});

// 可用字段（自定义报表）
const availableFields = [
  { value: "id", label: "ID" },
  { value: "title", label: "标题" },
  { value: "category", label: "分类" },
  { value: "status", label: "状态" },
  { value: "createTime", label: "创建时间" },
  { value: "updateTime", label: "更新时间" },
];

// 筛选字段
const filterFields = [
  { value: "category", label: "分类" },
  { value: "status", label: "状态" },
  { value: "createTime", label: "创建时间" },
];

// 可用模板
const availableTemplates = computed(() => {
  return templates.value.filter(
    (template) =>
      template.type === reportConfig.type || template.type === "custom",
  );
});

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: "请输入报表名称", trigger: "blur" },
    { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" },
  ],
  type: [{ required: true, message: "请选择报表类型", trigger: "change" }],
  format: [{ required: true, message: "请选择输出格式", trigger: "change" }],
  dateRange: [{ required: true, message: "请选择时间范围", trigger: "change" }],
};

// 表单验证状态
const isFormValid = computed(() => {
  return (
    reportConfig.name &&
    reportConfig.type &&
    reportConfig.format &&
    reportConfig.dateRange.startDate &&
    reportConfig.dateRange.endDate
  );
});

// 是否显示筛选条件
const showFilters = computed(() => {
  return reportConfig.type !== "system_performance";
});

// 方法
const handleTypeChange = () => {
  // 重置图表选择
  reportConfig.charts = [];
  // 重置自定义字段
  reportConfig.customFields = [];
  // 重置模板选择
  reportConfig.template = "";
};

const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates) {
    reportConfig.dateRange.startDate = dates[0];
    reportConfig.dateRange.endDate = dates[1];
  } else {
    reportConfig.dateRange.startDate = "";
    reportConfig.dateRange.endDate = "";
  }
};

const addFilter = () => {
  filters.value.push({
    field: "",
    operator: "eq",
    value: "",
  });
};

const removeFilter = (index: number) => {
  filters.value.splice(index, 1);
};

const handleRecipientsChange = () => {
  if (recipientsInput.value) {
    const emails = recipientsInput.value
      .split(",")
      .map((email) => email.trim())
      .filter((email) => email && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email));

    schedule.recipients = [
      ...new Set([...(schedule.recipients || []), ...emails]),
    ];
    recipientsInput.value = "";
  }
};

const removeRecipient = (index: number) => {
  schedule.recipients?.splice(index, 1);
};

const getTypeLabel = (type: ReportType) => {
  const option = reportTypeOptions.find((opt) => opt.value === type);
  return option?.label || type;
};

const getChartLabel = (chart: string) => {
  const chartOption = availableCharts.value.find((opt) => opt.value === chart);
  return chartOption?.label || chart;
};

const formatDateRange = () => {
  if (reportConfig.dateRange.startDate && reportConfig.dateRange.endDate) {
    return `${reportConfig.dateRange.startDate} 至 ${reportConfig.dateRange.endDate}`;
  }
  return "未设置";
};

const handleGenerate = async () => {
  try {
    // 表单验证
    await formRef.value?.validate();

    // 确认生成
    await ElMessageBox.confirm("确定要生成此报表吗？", "确认生成", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "info",
    });

    // 准备请求数据
    const request = {
      config: {
        ...reportConfig,
        filters:
          filters.value.length > 0
            ? Object.fromEntries(
                filters.value.map((f) => [f.field, { [f.operator]: f.value }]),
              )
            : undefined,
        schedule: scheduleEnabled.value
          ? { ...schedule, enabled: true }
          : undefined,
      },
      templateId: reportConfig.template || undefined,
      priority: "normal" as const,
      notifyOnComplete: true,
    };

    // 生成报表
    await generateReport(request);

    // 重置表单（可选）
    // resetForm();
  } catch (error) {
    if (error !== "cancel") {
      console.error("生成报表失败:", error);
    }
  }
};

const resetForm = () => {
  formRef.value?.resetFields();
  Object.assign(reportConfig, {
    name: "",
    type: "archive_statistics" as ReportType,
    format: "pdf" as ReportFormat,
    dateRange: { startDate: "", endDate: "" },
    charts: [],
    includeRawData: false,
    customFields: [],
    template: "",
  });
  dateRange.value = ["", ""];
  filters.value = [];
  scheduleEnabled.value = false;
  Object.assign(schedule, {
    enabled: false,
    frequency: "daily",
    time: "09:00",
    recipients: [],
    autoDelete: false,
    deleteAfterDays: 30,
  });
};

// 监听配置变化显示预览
watch(
  () => [reportConfig.name, reportConfig.type, reportConfig.format],
  () => {
    showPreview.value = !!(
      reportConfig.name &&
      reportConfig.type &&
      reportConfig.format
    );
  },
  { deep: true },
);

// 初始化
onMounted(() => {
  loadTemplates();
});
</script>

<style scoped>
.report-generator {
  display: flex;
  gap: 20px;
  min-height: 600px;
}

.generator-card {
  flex: 2;
}

.preview-card {
  flex: 1;
  max-height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: var(--el-text-color-primary);
}

.generator-form {
  max-width: 600px;
}

.option-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.template-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.advanced-options,
.schedule-options {
  margin-top: 20px;
}

.filters-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-item {
  display: flex;
  gap: 10px;
  align-items: center;
}

.recipients-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.preview-content {
  padding: 10px 0;
}

.preview-info {
  margin-bottom: 20px;
}

@media (max-width: 1200px) {
  .report-generator {
    flex-direction: column;
  }

  .generator-card,
  .preview-card {
    flex: none;
  }
}
</style>
