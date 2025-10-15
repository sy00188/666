import { describe, it, expect } from 'vitest'

// 示例工具函数
function formatDate(date: Date): string {
  return date.toISOString().split('T')[0]
}

function formatNumber(num: number): string {
  return num.toLocaleString('zh-CN')
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

describe('formatter工具函数', () => {
  describe('formatDate', () => {
    it('应该正确格式化日期', () => {
      const date = new Date('2024-01-15T10:30:00Z')
      expect(formatDate(date)).toBe('2024-01-15')
    })

    it('应该处理不同的日期', () => {
      const date = new Date('2025-12-31T23:59:59Z')
      expect(formatDate(date)).toBe('2025-12-31')
    })
  })

  describe('formatNumber', () => {
    it('应该正确格式化数字', () => {
      expect(formatNumber(1000)).toBe('1,000')
      expect(formatNumber(1000000)).toBe('1,000,000')
    })

    it('应该处理小数字', () => {
      expect(formatNumber(0)).toBe('0')
      expect(formatNumber(100)).toBe('100')
    })
  })

  describe('formatFileSize', () => {
    it('应该正确格式化字节', () => {
      expect(formatFileSize(0)).toBe('0 B')
      expect(formatFileSize(1024)).toBe('1 KB')
      expect(formatFileSize(1048576)).toBe('1 MB')
    })

    it('应该保留两位小数', () => {
      expect(formatFileSize(1536)).toBe('1.5 KB')
      expect(formatFileSize(2621440)).toBe('2.5 MB')
    })
  })
})

