<template>
  <div class="saved-conditions">
    <div class="conditions-header">
      <h4>保存的条件</h4>
      <el-button 
        v-if="conditionsList.length > 0"
        type="text" 
        size="small" 
        @click="showCreateDialog = true"
      >
        新建条件
      </el-button>
    </div>
    
    <div v-if="conditionsList.length === 0" class="empty-state">
      <el-empty 
        description="暂无保存的搜索条件" 
        :image-size="80"
      >
        <el-button type="primary" @click="showCreateDialog = true">
          创建第一个搜索条件
        </el-button>
      </el-empty>
    </div>
    
    <div v-else class="conditions-list">
      <div 
        v-for="item in conditionsList" 
        :key="item.id"
        class="condition-item"
      >
        <div class="condition-content" @click="applyCondition(item)">
          <div class="condition-header">
            <h5 class="condition-name">{{ item.name }}</h5>
            <div class="condition-actions">
              <el-button 
                type="text" 
                size="small"
                @click.stop="editCondition(item)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button 
                type="text" 
                size="small"
                @click.stop="deleteCondition(item.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div v-if="item.description" class="condition-description">
            {{ item.description }}
          </div>
          
          <div class="condition-preview">
            {{ formatConditionPreview(item) }}
          </div>
          
          <div class="condition-meta">
            <span class="created-time">创建于 {{ formatTime(item.createdAt) }}</span>
            <span v-if="item.updatedAt !== item.createdAt" class="updated-time">
              更新于 {{ formatTime(item.updatedAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 创建/编辑条件对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingCondition ? '编辑搜索条件' : '创建搜索条件'"
      width="500px"
      @close="resetDialog"
    >
      <el-form
        ref="formRef"
        :model="conditionForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="条件名称" prop="name">
          <el-input
            v-model="conditionForm.name"
            placeholder="请输入条件名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="conditionForm.description"
            type="textarea"
            placeholder="请输入条件描述（可选）"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveCondition">
            {{ editingCondition ? '更新' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { SavedSearchCondition } from '@/composables/useAdvancedSearch'

interface Props {
  conditionsList: SavedSearchCondition[]
  currentConditions?: Record<string, any>
}

interface Emits {
  (e: 'apply-condition', condition: SavedSearchCondition): void
  (e: 'save-condition', data: { name: string; description?: string; conditions: Record<string, any> }): void
  (e: 'update-condition', data: { id: string; name: string; description?: string; conditions: Record<string, any> }): void
  (e: 'delete-condition', id: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 对话框状态
const showCreateDialog = ref(false)
const editingCondition = ref<SavedSearchCondition | null>(null)
const formRef = ref<FormInstance>()

// 表单数据
const conditionForm = reactive({
  name: '',
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入条件名称', trigger: 'blur' },
    { min: 2, max: 50, message: '条件名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

/**
 * 格式化条件预览
 */
const formatConditionPreview = (condition: SavedSearchCondition): string => {
  const conditions = condition.conditions || {}
  const parts: string[] = []
  
  if (conditions.keyword) {
    parts.push(`关键词: ${conditions.keyword}`)
  }
  
  if (conditions.title) {
    parts.push(`标题: ${conditions.title}`)
  }
  
  if (conditions.code) {
    parts.push(`编号: ${conditions.code}`)
  }
  
  if (conditions.category) {
    parts.push(`分类: ${conditions.category}`)
  }
  
  if (conditions.status) {
    parts.push(`状态: ${conditions.status}`)
  }
  
  if (conditions.dateRange && conditions.dateRange.length === 2) {
    parts.push(`时间: ${conditions.dateRange[0]} ~ ${conditions.dateRange[1]}`)
  }
  
  return parts.length > 0 ? parts.join(', ') : '无特定条件'
}

/**
 * 格式化时间显示
 */
const formatTime = (timestamp: number): string => {
  const date = new Date(timestamp)
  return `${date.getFullYear()}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')}`
}

/**
 * 应用搜索条件
 */
const applyCondition = (condition: SavedSearchCondition) => {
  emit('apply-condition', condition)
}

/**
 * 编辑条件
 */
const editCondition = (condition: SavedSearchCondition) => {
  editingCondition.value = condition
  conditionForm.name = condition.name
  conditionForm.description = condition.description || ''
  showCreateDialog.value = true
}

/**
 * 保存条件
 */
const saveCondition = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const conditionData = {
      name: conditionForm.name,
      description: conditionForm.description || undefined,
      conditions: props.currentConditions || {}
    }
    
    if (editingCondition.value) {
      // 更新现有条件
      emit('update-condition', {
        id: editingCondition.value.id,
        ...conditionData
      })
      ElMessage.success('条件更新成功')
    } else {
      // 创建新条件
      emit('save-condition', conditionData)
      ElMessage.success('条件保存成功')
    }
    
    showCreateDialog.value = false
    resetDialog()
  } catch (error) {
    console.error('保存条件失败:', error)
  }
}

/**
 * 删除条件
 */
const deleteCondition = async (id: string) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个搜索条件吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    emit('delete-condition', id)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

/**
 * 重置对话框
 */
const resetDialog = () => {
  editingCondition.value = null
  conditionForm.name = ''
  conditionForm.description = ''
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.saved-conditions {
  .conditions-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h4 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: $text-primary;
    }
  }
  
  .empty-state {
    padding: 20px 0;
    text-align: center;
  }
  
  .conditions-list {
    .condition-item {
      margin-bottom: 12px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .condition-content {
        padding: 16px;
        background: $bg-color;
        border: 1px solid $border-lighter;
        border-radius: $border-radius-base;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          border-color: $primary-color;
          background: rgba($primary-color, 0.05);
          
          .condition-actions {
            opacity: 1;
          }
        }
        
        .condition-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 8px;
          
          .condition-name {
            margin: 0;
            font-size: 16px;
            font-weight: 500;
            color: $text-primary;
            flex: 1;
          }
          
          .condition-actions {
            opacity: 0;
            transition: opacity 0.2s ease;
            display: flex;
            gap: 4px;
            
            .el-button {
              color: $text-secondary;
              
              &:hover {
                color: $primary-color;
              }
            }
          }
        }
        
        .condition-description {
          margin-bottom: 8px;
          font-size: 13px;
          color: $text-secondary;
          line-height: 1.4;
        }
        
        .condition-preview {
          margin-bottom: 8px;
          padding: 8px 12px;
          background: rgba($primary-color, 0.1);
          border-radius: $border-radius-small;
          font-size: 13px;
          color: $text-regular;
          line-height: 1.4;
        }
        
        .condition-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 12px;
          color: $text-secondary;
          
          .created-time {
            
          }
          
          .updated-time {
            
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .saved-conditions {
    .conditions-list {
      .condition-item {
        .condition-content {
          padding: 12px;
          
          .condition-header {
            .condition-name {
              font-size: 15px;
            }
            
            .condition-actions {
              opacity: 1; // 移动端始终显示操作按钮
            }
          }
          
          .condition-description {
            font-size: 12px;
          }
          
          .condition-preview {
            font-size: 12px;
            padding: 6px 10px;
          }
          
          .condition-meta {
            font-size: 11px;
            gap: 8px;
          }
        }
      }
    }
  }
}
</style>