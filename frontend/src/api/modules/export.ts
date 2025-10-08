/**
 * 导出API模块
 * @author Archive Management System
 * @version 1.0
 */

import request from '@/utils/request'

export interface ExportTask {
  id: number
  taskId: string
  taskName: string
  exportType: string
  format: string
  status: string
  progress: number
  totalCount: number
  processedCount: number
  filePath?: string
  fileName?: string
  fileSize?: number
  errorMessage?: string
  createdAt: string
  startedAt?: string
  completedAt?: string
  expireAt?: string
  estimatedTime?: number
  pausable: boolean
  paused: boolean
}

export interface CreateExportTaskRequest {
  taskName: string
  exportType: string
  format: 'excel' | 'csv' | 'pdf'
  parameters: Record<string, any>
  totalCount: number
}

export interface QuickExportRequest {
  data: Record<string, any>[]
  headers: string[]
  fieldNames: string[]
  format?: 'excel' | 'csv' | 'pdf'
  fileName?: string
}

/**
 * 创建导出任务
 */
export function createExportTask(data: CreateExportTaskRequest) {
  return request({
    url: '/api/export/tasks',
    method: 'post',
    data
  })
}

/**
 * 获取导出任务列表
 */
export function getExportTasks(params?: {
  page?: number
  size?: number
  status?: string
}) {
  return request({
    url: '/api/export/tasks',
    method: 'get',
    params
  })
}

/**
 * 获取导出任务详情
 */
export function getExportTask(taskId: string) {
  return request({
    url: `/api/export/tasks/${taskId}`,
    method: 'get'
  })
}

/**
 * 取消导出任务
 */
export function cancelExportTask(taskId: string) {
  return request({
    url: `/api/export/tasks/${taskId}/cancel`,
    method: 'post'
  })
}

/**
 * 暂停导出任务
 */
export function pauseExportTask(taskId: string) {
  return request({
    url: `/api/export/tasks/${taskId}/pause`,
    method: 'post'
  })
}

/**
 * 恢复导出任务
 */
export function resumeExportTask(taskId: string) {
  return request({
    url: `/api/export/tasks/${taskId}/resume`,
    method: 'post'
  })
}

/**
 * 删除导出任务
 */
export function deleteExportTask(taskId: string) {
  return request({
    url: `/api/export/tasks/${taskId}`,
    method: 'delete'
  })
}

/**
 * 下载导出文件
 */
export function downloadExportFile(taskId: string) {
  return `/api/export/tasks/${taskId}/download`
}

/**
 * 快速导出（小数据量，直接下载）
 */
export function quickExport(data: QuickExportRequest) {
  return request({
    url: '/api/export/quick',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

