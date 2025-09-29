<template>
  <div class="chart-linkage-panel">
    <div class="panel-header">
      <h3>图表联动配置</h3>
      <el-button type="primary" size="small" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加联动规则
      </el-button>
    </div>

    <!-- 联动规则列表 -->
    <div class="linkage-rules">
      <el-empty v-if="linkageRules.length === 0" description="暂无联动规则" />

      <div
        v-for="rule in linkageRules"
        :key="rule.id"
        class="linkage-rule-item"
        :class="{ disabled: !rule.enabled }"
      >
        <div class="rule-header">
          <div class="rule-info">
            <h4>{{ rule.name }}</h4>
            <p class="rule-description">
              {{ getSourceChartName(rule.sourceChart) }} →
              {{
                rule.targetCharts.map((id) => getTargetChartName(id)).join(", ")
              }}
            </p>
          </div>
          <div class="rule-actions">
            <el-switch
              v-model="rule.enabled"
              @change="toggleRule(rule.id, rule.enabled)"
            />
            <el-button size="small" type="primary" text @click="editRule(rule)">
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              text
              @click="deleteRule(rule.id)"
            >
              删除
            </el-button>
          </div>
        </div>

        <div class="rule-details">
          <div class="mapping-info">
            <el-tag size="small" type="info">
              {{ rule.mapping.sourceField }} → {{ rule.mapping.targetField }}
            </el-tag>
          </div>

          <div v-if="rule.condition" class="condition-info">
            <el-tag size="small" type="warning">
              条件: {{ rule.condition.field }} {{ rule.condition.operator }}
              {{ rule.condition.value }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑联动规则对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingRule ? '编辑联动规则' : '添加联动规则'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="ruleForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>

        <el-form-item label="源图表" prop="sourceChart">
          <el-select
            v-model="ruleForm.sourceChart"
            placeholder="选择源图表"
            style="width: 100%"
          >
            <el-option
              v-for="chart in availableCharts"
              :key="chart.id"
              :label="chart.title"
              :value="chart.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="目标图表" prop="targetCharts">
          <el-select
            v-model="ruleForm.targetCharts"
            multiple
            placeholder="选择目标图表"
            style="width: 100%"
          >
            <el-option
              v-for="chart in availableTargetCharts"
              :key="chart.id"
              :label="chart.title"
              :value="chart.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="字段映射">
          <div class="field-mapping">
            <el-form-item label="源字段" prop="mapping.sourceField">
              <el-select
                v-model="ruleForm.mapping.sourceField"
                placeholder="选择源字段"
              >
                <el-option
                  v-for="field in sourceFields"
                  :key="field"
                  :label="field"
                  :value="field"
                />
              </el-select>
            </el-form-item>

            <div class="mapping-arrow">→</div>

            <el-form-item label="目标字段" prop="mapping.targetField">
              <el-select
                v-model="ruleForm.mapping.targetField"
                placeholder="选择目标字段"
              >
                <el-option
                  v-for="field in targetFields"
                  :key="field"
                  :label="field"
                  :value="field"
                />
              </el-select>
            </el-form-item>
          </div>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="hasCondition">添加触发条件</el-checkbox>
        </el-form-item>

        <template v-if="hasCondition">
          <el-form-item label="条件字段">
            <el-select
              v-model="ruleForm.condition.field"
              placeholder="选择字段"
            >
              <el-option
                v-for="field in conditionFields"
                :key="field"
                :label="field"
                :value="field"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="操作符">
            <el-select
              v-model="ruleForm.condition.operator"
              placeholder="选择操作符"
            >
              <el-option label="等于" value="eq" />
              <el-option label="不等于" value="ne" />
              <el-option label="大于" value="gt" />
              <el-option label="大于等于" value="gte" />
              <el-option label="小于" value="lt" />
              <el-option label="小于等于" value="lte" />
              <el-option label="包含" value="contains" />
            </el-select>
          </el-form-item>

          <el-form-item label="条件值">
            <el-input
              v-model="ruleForm.condition.value"
              placeholder="请输入条件值"
            />
          </el-form-item>
        </template>

        <el-form-item label="启用规则">
          <el-switch v-model="ruleForm.enabled" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRule">
          {{ editingRule ? "更新" : "添加" }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 联动测试面板 -->
    <div class="test-panel">
      <h4>联动测试</h4>
      <div class="test-controls">
        <el-select v-model="testSourceChart" placeholder="选择测试图表">
          <el-option
            v-for="chart in availableCharts"
            :key="chart.id"
            :label="chart.title"
            :value="chart.id"
          />
        </el-select>

        <el-input
          v-model="testValue"
          placeholder="输入测试值"
          style="width: 200px; margin-left: 12px"
        />

        <el-button
          type="primary"
          @click="testLinkage"
          :disabled="!testSourceChart || !testValue"
        >
          测试联动
        </el-button>
      </div>

      <div v-if="testResults.length > 0" class="test-results">
        <h5>测试结果:</h5>
        <ul>
          <li v-for="result in testResults" :key="result.chartId">
            {{ result.chartName }}: {{ result.status }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { useChartInteraction } from "@/composables/useChartInteraction";
import type {
  LinkageRule,
  InteractiveChart,
  ChartInteraction,
} from "@/types/chart";

const {
  charts,
  linkageRules,
  addLinkageRule,
  removeLinkageRule,
  toggleLinkageRule,
  handleChartInteraction,
} = useChartInteraction();

// 响应式数据
const showAddDialog = ref(false);
const editingRule = ref<LinkageRule | null>(null);
const hasCondition = ref(false);
const formRef = ref();

const ruleForm = reactive({
  name: "",
  sourceChart: "",
  targetCharts: [] as string[],
  mapping: {
    sourceField: "",
    targetField: "",
  },
  condition: {
    field: "",
    operator: "eq",
    value: "",
  },
  enabled: true,
});

// 测试相关
const testSourceChart = ref("");
const testValue = ref("");
const testResults = ref<
  Array<{ chartId: string; chartName: string; status: string }>
>([]);

// 表单验证规则
const formRules = {
  name: [{ required: true, message: "请输入规则名称", trigger: "blur" }],
  sourceChart: [{ required: true, message: "请选择源图表", trigger: "change" }],
  targetCharts: [
    { required: true, message: "请选择目标图表", trigger: "change" },
  ],
  "mapping.sourceField": [
    { required: true, message: "请选择源字段", trigger: "change" },
  ],
  "mapping.targetField": [
    { required: true, message: "请选择目标字段", trigger: "change" },
  ],
};

// 计算属性
const availableCharts = computed(() => charts.value);

const availableTargetCharts = computed(() => {
  return charts.value.filter((chart) => chart.id !== ruleForm.sourceChart);
});

const sourceFields = computed(() => {
  const sourceChart = charts.value.find((c) => c.id === ruleForm.sourceChart);
  if (!sourceChart || !sourceChart.series[0]) return [];

  // 从图表数据中提取字段名
  const sampleData = sourceChart.series[0].data[0];
  return sampleData ? Object.keys(sampleData) : ["name", "value", "category"];
});

const targetFields = computed(() => {
  // 通用字段列表
  return ["name", "value", "category", "date", "type"];
});

const conditionFields = computed(() => {
  return [...sourceFields.value, "chartType", "seriesName"];
});

// 方法
const getSourceChartName = (chartId: string) => {
  const chart = charts.value.find((c) => c.id === chartId);
  return chart?.title || chartId;
};

const getTargetChartName = (chartId: string) => {
  const chart = charts.value.find((c) => c.id === chartId);
  return chart?.title || chartId;
};

const toggleRule = (ruleId: string, enabled: boolean) => {
  toggleLinkageRule(ruleId, enabled);
  ElMessage.success(enabled ? "规则已启用" : "规则已禁用");
};

const editRule = (rule: LinkageRule) => {
  editingRule.value = rule;
  Object.assign(ruleForm, {
    name: rule.name,
    sourceChart: rule.sourceChart,
    targetCharts: [...rule.targetCharts],
    mapping: { ...rule.mapping },
    condition: rule.condition
      ? { ...rule.condition }
      : { field: "", operator: "eq", value: "" },
    enabled: rule.enabled,
  });
  hasCondition.value = !!rule.condition;
  showAddDialog.value = true;
};

const deleteRule = async (ruleId: string) => {
  try {
    await ElMessageBox.confirm("确定要删除这个联动规则吗？", "确认删除", {
      type: "warning",
    });

    removeLinkageRule(ruleId);
    ElMessage.success("规则删除成功");
  } catch {
    // 用户取消删除
  }
};

const saveRule = async () => {
  try {
    await formRef.value.validate();

    const rule: LinkageRule = {
      id: editingRule.value?.id || `rule_${Date.now()}`,
      name: ruleForm.name,
      sourceChart: ruleForm.sourceChart,
      targetCharts: [...ruleForm.targetCharts],
      mapping: { ...ruleForm.mapping },
      condition: hasCondition.value ? { ...ruleForm.condition } : undefined,
      enabled: ruleForm.enabled,
    };

    if (editingRule.value) {
      // 更新现有规则
      const index = linkageRules.value.findIndex(
        (r) => r.id === editingRule.value!.id,
      );
      if (index > -1) {
        linkageRules.value[index] = rule;
      }
      ElMessage.success("规则更新成功");
    } else {
      // 添加新规则
      addLinkageRule(rule);
      ElMessage.success("规则添加成功");
    }

    showAddDialog.value = false;
    resetForm();
  } catch (error) {
    console.error("保存规则失败:", error);
  }
};

const resetForm = () => {
  editingRule.value = null;
  hasCondition.value = false;
  Object.assign(ruleForm, {
    name: "",
    sourceChart: "",
    targetCharts: [],
    mapping: {
      sourceField: "",
      targetField: "",
    },
    condition: {
      field: "",
      operator: "eq",
      value: "",
    },
    enabled: true,
  });
  formRef.value?.resetFields();
};

const testLinkage = () => {
  if (!testSourceChart.value || !testValue.value) return;

  // 模拟图表交互事件
  const sourceChart = charts.value.find((c) => c.id === testSourceChart.value);
  if (!sourceChart) return;

  const interaction: ChartInteraction = {
    type: "click",
    data: {
      name: testValue.value,
      value: 100,
      category: "test",
    },
    series: sourceChart.series[0],
    chart: testSourceChart.value,
  };

  // 执行联动测试
  handleChartInteraction(interaction);

  // 模拟测试结果
  testResults.value = linkageRules.value
    .filter(
      (rule) => rule.sourceChart === testSourceChart.value && rule.enabled,
    )
    .flatMap((rule) =>
      rule.targetCharts.map((chartId) => ({
        chartId,
        chartName: getTargetChartName(chartId),
        status: "联动成功",
      })),
    );

  if (testResults.value.length === 0) {
    testResults.value = [
      {
        chartId: "none",
        chartName: "无匹配规则",
        status: "未触发联动",
      },
    ];
  }

  ElMessage.success("联动测试完成");
};
</script>

<style scoped>
.chart-linkage-panel {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.linkage-rules {
  margin-bottom: 24px;
}

.linkage-rule-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.linkage-rule-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.linkage-rule-item.disabled {
  opacity: 0.6;
  background-color: #f5f7fa;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.rule-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.rule-description {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.rule-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rule-details {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.field-mapping {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.field-mapping .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.mapping-arrow {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  margin: 0 8px;
}

.test-panel {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}

.test-panel h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.test-controls {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.test-results {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 12px;
}

.test-results h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.test-results ul {
  margin: 0;
  padding-left: 20px;
}

.test-results li {
  margin-bottom: 4px;
  font-size: 14px;
  color: #666;
}
</style>
