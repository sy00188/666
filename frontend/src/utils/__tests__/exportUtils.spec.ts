import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import * as exportUtils from '@/utils/exportUtils'

// Mock file download
const mockCreateObjectURL = vi.fn()
const mockRevokeObjectURL = vi.fn()
const mockClick = vi.fn()

Object.defineProperty(window.URL, 'createObjectURL', {
  value: mockCreateObjectURL
})

Object.defineProperty(window.URL, 'revokeObjectURL', {
  value: mockRevokeObjectURL
})

// Mock document.createElement
const mockAnchorElement = {
  href: '',
  download: '',
  click: mockClick,
  style: { display: '' }
}

const originalCreateElement = document.createElement
document.createElement = vi.fn((tagName) => {
  if (tagName === 'a') {
    return mockAnchorElement as any
  }
  return originalCreateElement.call(document, tagName)
})

describe('exportUtils', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCreateObjectURL.mockReturnValue('blob:mock-url')
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('exportToExcel', () => {
    it('应该导出数据到Excel文件', () => {
      const data = [
        { name: '张三', age: 25, department: '技术部' },
        { name: '李四', age: 30, department: '产品部' }
      ]
      const filename = 'users.xlsx'

      exportUtils.exportToExcel(data, filename)

      expect(mockCreateObjectURL).toHaveBeenCalled()
      expect(mockAnchorElement.download).toBe(filename)
      expect(mockClick).toHaveBeenCalled()
      expect(mockRevokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
    })

    it('应该使用默认文件名', () => {
      const data = [{ id: 1, name: 'test' }]

      exportUtils.exportToExcel(data)

      expect(mockAnchorElement.download).toMatch(/export_\d+\.xlsx/)
    })

    it('应该处理空数据', () => {
      const data: any[] = []

      expect(() => exportUtils.exportToExcel(data)).not.toThrow()
      expect(mockCreateObjectURL).toHaveBeenCalled()
    })

    it('应该支持自定义列配置', () => {
      const data = [
        { name: '张三', age: 25, salary: 10000 }
      ]
      const columns = [
        { key: 'name', title: '姓名' },
        { key: 'age', title: '年龄' }
      ]

      exportUtils.exportToExcel(data, 'test.xlsx', columns)

      expect(mockCreateObjectURL).toHaveBeenCalled()
    })
  })

  describe('exportToPDF', () => {
    it('应该导出数据到PDF文件', () => {
      const data = [
        { name: '张三', age: 25 },
        { name: '李四', age: 30 }
      ]
      const filename = 'users.pdf'

      exportUtils.exportToPDF(data, filename)

      expect(mockCreateObjectURL).toHaveBeenCalled()
      expect(mockAnchorElement.download).toBe(filename)
      expect(mockClick).toHaveBeenCalled()
    })

    it('应该支持自定义PDF选项', () => {
      const data = [{ name: 'test' }]
      const options = {
        title: '用户报表',
        orientation: 'landscape' as const,
        pageSize: 'A3' as const
      }

      exportUtils.exportToPDF(data, 'test.pdf', options)

      expect(mockCreateObjectURL).toHaveBeenCalled()
    })
  })

  describe('exportToCSV', () => {
    it('应该导出数据到CSV文件', () => {
      const data = [
        { name: '张三', age: 25 },
        { name: '李四', age: 30 }
      ]
      const filename = 'users.csv'

      exportUtils.exportToCSV(data, filename)

      expect(mockCreateObjectURL).toHaveBeenCalled()
      expect(mockAnchorElement.download).toBe(filename)
      expect(mockClick).toHaveBeenCalled()
    })

    it('应该正确处理CSV格式', () => {
      const data = [
        { name: '张,三', description: '包含"引号"的文本' }
      ]

      exportUtils.exportToCSV(data, 'test.csv')

      expect(mockCreateObjectURL).toHaveBeenCalled()
    })

    it('应该支持自定义分隔符', () => {
      const data = [{ name: 'test', value: 123 }]
      const options = { delimiter: ';' }

      exportUtils.exportToCSV(data, 'test.csv', options)

      expect(mockCreateObjectURL).toHaveBeenCalled()
    })
  })

  describe('downloadFile', () => {
    it('应该下载文件', () => {
      const url = 'https://example.com/file.pdf'
      const filename = 'document.pdf'

      exportUtils.downloadFile(url, filename)

      expect(mockAnchorElement.href).toBe(url)
      expect(mockAnchorElement.download).toBe(filename)
      expect(mockClick).toHaveBeenCalled()
    })

    it('应该处理Blob URL', () => {
      const blob = new Blob(['test content'], { type: 'text/plain' })
      const filename = 'test.txt'

      exportUtils.downloadFile(blob, filename)

      expect(mockCreateObjectURL).toHaveBeenCalledWith(blob)
      expect(mockClick).toHaveBeenCalled()
      expect(mockRevokeObjectURL).toHaveBeenCalled()
    })
  })

  describe('formatDataForExport', () => {
    it('应该格式化数据用于导出', () => {
      const data = [
        { id: 1, name: '张三', createdAt: new Date('2023-01-01') },
        { id: 2, name: '李四', createdAt: new Date('2023-01-02') }
      ]
      const columns = [
        { key: 'name', title: '姓名' },
        { key: 'createdAt', title: '创建时间', formatter: (value: Date) => value.toLocaleDateString() }
      ]

      const result = exportUtils.formatDataForExport(data, columns)

      expect(result).toHaveLength(2)
      expect(result[0]).toEqual({
        '姓名': '张三',
        '创建时间': '2023/1/1'
      })
    })

    it('应该处理嵌套属性', () => {
      const data = [
        { user: { profile: { name: '张三' } }, department: { name: '技术部' } }
      ]
      const columns = [
        { key: 'user.profile.name', title: '姓名' },
        { key: 'department.name', title: '部门' }
      ]

      const result = exportUtils.formatDataForExport(data, columns)

      expect(result[0]).toEqual({
        '姓名': '张三',
        '部门': '技术部'
      })
    })
  })

  describe('generateExportFilename', () => {
    it('应该生成带时间戳的文件名', () => {
      const prefix = 'users'
      const extension = 'xlsx'

      const filename = exportUtils.generateExportFilename(prefix, extension)

      expect(filename).toMatch(/^users_\d{8}_\d{6}\.xlsx$/)
    })

    it('应该使用默认前缀', () => {
      const filename = exportUtils.generateExportFilename()

      expect(filename).toMatch(/^export_\d{8}_\d{6}\.xlsx$/)
    })
  })

  describe('validateExportData', () => {
    it('应该验证导出数据', () => {
      const validData = [{ id: 1, name: 'test' }]
      const invalidData: any[] = []

      expect(exportUtils.validateExportData(validData)).toBe(true)
      expect(exportUtils.validateExportData(invalidData)).toBe(false)
    })

    it('应该检查数据结构', () => {
      const validData = [{ name: 'test' }, { name: 'test2' }]
      const invalidData = [{ name: 'test' }, { age: 25 }] // 不一致的结构

      expect(exportUtils.validateExportData(validData)).toBe(true)
      expect(exportUtils.validateExportData(invalidData)).toBe(false)
    })
  })
})
