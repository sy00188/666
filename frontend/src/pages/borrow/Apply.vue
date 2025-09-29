<template>
  <div class="borrow-apply">
    <div class="page-header">
      <h2>借阅申请</h2>
      <p>申请借阅档案</p>
    </div>

    <div class="apply-content">
      <el-card>
        <template #header>
          <span>借阅申请表</span>
        </template>

        <el-form
          :model="applyForm"
          :rules="rules"
          ref="formRef"
          label-width="120px"
        >
          <el-form-item label="档案选择" prop="archiveId">
            <el-select
              v-model="applyForm.archiveId"
              placeholder="请选择要借阅的档案"
              style="width: 100%"
            >
              <el-option
                v-for="archive in archiveList"
                :key="archive.id"
                :label="archive.title"
                :value="archive.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="借阅期限" prop="borrowDays">
            <el-select
              v-model="applyForm.borrowDays"
              placeholder="请选择借阅期限"
            >
              <el-option label="7天" :value="7" />
              <el-option label="15天" :value="15" />
              <el-option label="30天" :value="30" />
            </el-select>
          </el-form-item>

          <el-form-item label="借阅用途" prop="purpose">
            <el-input
              v-model="applyForm.purpose"
              type="textarea"
              :rows="4"
              placeholder="请说明借阅用途"
            />
          </el-form-item>

          <el-form-item label="联系方式" prop="contact">
            <el-input
              v-model="applyForm.contact"
              placeholder="请输入联系方式"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit">提交申请</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header>
          <span>我的申请记录</span>
        </template>

        <el-table :data="myApplications" style="width: 100%">
          <el-table-column prop="archiveTitle" label="档案名称" />
          <el-table-column prop="applyTime" label="申请时间" />
          <el-table-column prop="borrowDays" label="借阅期限" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{
                row.status
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button
                v-if="row.status === '待审核'"
                type="danger"
                size="small"
                >撤销</el-button
              >
              <el-button
                v-if="row.status === '已批准'"
                type="success"
                size="small"
                >查看</el-button
              >
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

const formRef = ref();

const applyForm = ref({
  archiveId: "",
  borrowDays: "",
  purpose: "",
  contact: "",
});

const rules = {
  archiveId: [{ required: true, message: "请选择档案", trigger: "change" }],
  borrowDays: [
    { required: true, message: "请选择借阅期限", trigger: "change" },
  ],
  purpose: [{ required: true, message: "请填写借阅用途", trigger: "blur" }],
  contact: [{ required: true, message: "请填写联系方式", trigger: "blur" }],
};

const archiveList = ref([
  { id: "1", title: "员工入职档案 - 张三" },
  { id: "2", title: "财务报表 - 2024年第一季度" },
  { id: "3", title: "合同档案 - 供应商协议" },
]);

const myApplications = ref([
  {
    archiveTitle: "员工入职档案 - 李四",
    applyTime: "2024-01-15 10:30",
    borrowDays: 15,
    status: "待审核",
  },
  {
    archiveTitle: "财务报表 - 2023年年报",
    applyTime: "2024-01-10 14:20",
    borrowDays: 7,
    status: "已批准",
  },
]);

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    // 提交申请逻辑
    ElMessage.success("申请提交成功");
    handleReset();
  } catch (error) {
    console.error("表单验证失败:", error);
  }
};

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const getStatusType = (status: string) => {
  switch (status) {
    case "待审核":
      return "warning";
    case "已批准":
      return "success";
    case "已拒绝":
      return "danger";
    default:
      return "info";
  }
};
</script>

<style lang="scss" scoped>
.borrow-apply {
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
</style>
