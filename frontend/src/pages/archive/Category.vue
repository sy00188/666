<template>
  <div class="archive-category">
    <div class="page-header">
      <h2>分类管理</h2>
      <p>管理档案分类信息</p>
    </div>

    <div class="category-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>档案分类</span>
            <el-button type="primary" size="small">
              <el-icon><Plus /></el-icon>
              新增分类
            </el-button>
          </div>
        </template>

        <el-table :data="categoryList" style="width: 100%">
          <el-table-column prop="name" label="分类名称" />
          <el-table-column prop="code" label="分类编码" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="count" label="档案数量" />
          <el-table-column label="操作" width="200">
            <template #default>
              <el-button type="primary" size="small">编辑</el-button>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { Plus } from "@element-plus/icons-vue";

const categoryList = ref([
  {
    name: "人事档案",
    code: "HR001",
    description: "员工人事相关档案",
    count: 156,
  },
  { name: "财务档案", code: "FIN001", description: "财务相关档案", count: 89 },
  { name: "合同档案", code: "CON001", description: "合同协议档案", count: 234 },
]);
</script>

<style lang="scss" scoped>
.archive-category {
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
