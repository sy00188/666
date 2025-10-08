/**
 * 导出功能 Composable
 * 统一的导出逻辑封装
 * @author Archive Management System
 */

import { ref, computed } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  createExportTask, 
  getExportTasks, 
  getExportTask,
  cancelExportTask,
  pauseExportTask,
  resumeExportTask,
  deleteExportTask,
  downloadExportFile,
  quickExport,
  type ExportTask,
  type CreateExportTaskRequest,
  type QuickExportRequest
} from '@/api/modules/export'

export function useExport() {
  // 状态
  const tasks = ref<ExportTask[]>([])
  const loading = ref(false)
  const currentTask = ref<ExportTask | null>(null)
  
  // 轮询定时器
  let pollingTimer: NodeJS.Timeout | null = null
  
  /**
   * 创建导出任务
   */
  const createTask = async (request: CreateExportTaskRequest) => {
    try {
      loading.value = true
      const response = await createExportTask(request)
      
      if (response.data.success) {
        ElMessage.success('导出任务创建成功')
        
        // 刷新任务列表
        await loadTasks()
        
        // 开始轮询任务状态
        startPolling()
        
        return response.data.taskId
      } else {
        throw new Error(response.data.message || '创建任务失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '创建导出任务失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 加载导出任务列表
   */
  const loadTasks = async (page = 0, size = 20, status?: string) => {
    try {
      loading.value = true
      const response = await getExportTasks({ page, size, status })
      
      if (response.data.success) {
        tasks.value = response.data.data
        return response.data
      }
    } catch (error: any) {
      ElMessage.error('加载导出任务失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 获取任务详情
   */
  const getTaskDetail = async (taskId: string) => {
    try {
      const response = await getExportTask(taskId)
      
      if (response.data.success) {
        currentTask.value = response.data.data
        return response.data.data
      }
    } catch (error: any) {
      ElMessage.error('获取任务详情失败')
      throw error
    }
  }
  
  /**
   * 取消任务
   */
  const cancelTask = async (taskId: string) => {
    try {
      const response = await cancelExportTask(taskId)
      
      if (response.data.success) {
        ElMessage.success('任务已取消')
        await loadTasks()
      }
    } catch (error: any) {
      ElMessage.error('取消任务失败')
      throw error
    }
  }
  
  /**
   * 暂停任务
   */
  const pauseTask = async (taskId: string) => {
    try {
      const response = await pauseExportTask(taskId)
      
      if (response.data.success) {
        ElMessage.success('任务已暂停')
        await loadTasks()
      }
    } catch (error: any) {
      ElMessage.error('暂停任务失败')
      throw error
    }
  }
  
  /**
   * 恢复任务
   */
  const resumeTask = async (taskId: string) => {
    try {
      const response = await resumeExportTask(taskId)
      
      if (response.data.success) {
        ElMessage.success('任务已恢复')
        await loadTasks()
        startPolling()
      }
    } catch (error: any) {
      ElMessage.error('恢复任务失败')
      throw error
    }
  }
  
  /**
   * 删除任务
   */
  const deleteTask = async (taskId: string) => {
    try {
      const response = await deleteExportTask(taskId)
      
      if (response.data.success) {
        ElMessage.success('任务已删除')
        await loadTasks()
      }
    } catch (error: any) {
      ElMessage.error('删除任务失败')
      throw error
    }
  }
  
  /**
   * 下载文件
   */
  const downloadFile = (taskId: string) => {
    const url = downloadExportFile(taskId)
    const link = document.createElement('a')
    link.href = url
    link.click()
    ElMessage.success('开始下载')
  }
  
  /**
   * 快速导出
   */
  const quickExportData = async (request: QuickExportRequest) => {
    try {
      loading.value = true
      const response = await quickExport(request)
      
      // 创建Blob并下载
      const blob = new Blob([response.data], { 
        type: getContentType(request.format || 'excel')
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${request.fileName || 'export'}${getFileExtension(request.format || 'excel')}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
    } catch (error: any) {
      ElMessage.error('导出失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 开始轮询任务状态
   */
  const startPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
    }
    
    pollingTimer = setInterval(async () => {
      try {
        const response = await getExportTasks({ page: 0, size: 50 })
        
        if (response.data.success) {
          const newTasks = response.data.data as ExportTask[]
          
          // 检查是否有任务完成或失败
          newTasks.forEach((newTask) => {
            const oldTask = tasks.value.find(t => t.taskId === newTask.taskId)
            
            if (oldTask && oldTask.status !== newTask.status) {
              if (newTask.status === 'completed') {
                ElNotification({
                  title: '导出完成',
                  message: `任务"${newTask.taskName}"已完成`,
                  type: 'success',
                  duration: 5000
                })
              } else if (newTask.status === 'failed') {
                ElNotification({
                  title: '导出失败',
                  message: `任务"${newTask.taskName}"失败：${newTask.errorMessage}`,
                  type: 'error',
                  duration: 5000
                })
              }
            }
          })
          
          tasks.value = newTasks
          
          // 如果没有进行中的任务，停止轮询
          const hasActiveTasks = newTasks.some(
            t => t.status === 'pending' || t.status === 'processing'
          )
          
          if (!hasActiveTasks) {
            stopPolling()
          }
        }
      } catch (error) {
        console.error('轮询任务状态失败:', error)
      }
    }, 3000) // 每3秒轮询一次
  }
  
  /**
   * 停止轮询
   */
  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }
  
  /**
   * 获取内容类型
   */
  const getContentType = (format: string) => {
    const types: Record<string, string> = {
      excel: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      csv: 'text/csv',
      pdf: 'application/pdf'
    }
    return types[format] || 'application/octet-stream'
  }
  
  /**
   * 获取文件扩展名
   */
  const getFileExtension = (format: string) => {
    const extensions: Record<string, string> = {
      excel: '.xlsx',
      csv: '.csv',
      pdf: '.pdf'
    }
    return extensions[format] || '.dat'
  }
  
  // 计算属性
  const activeTasks = computed(() => 
    tasks.value.filter(t => t.status === 'pending' || t.status === 'processing')
  )
  
  const completedTasks = computed(() => 
    tasks.value.filter(t => t.status === 'completed')
  )
  
  const failedTasks = computed(() => 
    tasks.value.filter(t => t.status === 'failed')
  )
  
  // 清理
  const cleanup = () => {
    stopPolling()
  }
  
  return {
    // 状态
    tasks,
    loading,
    currentTask,
    activeTasks,
    completedTasks,
    failedTasks,
    
    // 方法
    createTask,
    loadTasks,
    getTaskDetail,
    cancelTask,
    pauseTask,
    resumeTask,
    deleteTask,
    downloadFile,
    quickExportData,
    startPolling,
    stopPolling,
    cleanup
  }
}

