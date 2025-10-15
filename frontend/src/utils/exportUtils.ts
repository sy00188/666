import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'

// 导出配置接口
export interface ExportConfig {
  format: 'excel' | 'pdf' | 'png' | 'jpg'
  filename: string
  title?: string
  data?: any[]
  headers?: string[]
  chartId?: string
  element?: string | HTMLElement
  options?: any
}

/**
 * 数据导出工具类
 */
export class ExportUtils {
  /**
   * 导出数据到指定格式
   */
  static async exportData(config: ExportConfig): Promise<void> {
    try {
      switch (config.format) {
        case 'excel':
          await this.exportToExcel(config)
          break
        case 'pdf':
          await this.exportToPDF(config)
          break
        case 'png':
        case 'jpg':
          await this.exportToImage(config)
          break
        default:
          throw new Error(`不支持的导出格式: ${config.format}`)
      }
      
      ElMessage.success(`${config.format.toUpperCase()} 文件导出成功`)
    } catch (error) {
      console.error('导出失败:', error)
      ElMessage.error(`导出失败: ${error instanceof Error ? error.message : '未知错误'}`)
      throw error
    }
  }

  /**
   * 导出到 Excel
   */
  private static async exportToExcel(config: ExportConfig): Promise<void> {
    if (!config.data || !Array.isArray(config.data)) {
      throw new Error('Excel 导出需要提供数据数组')
    }

    const workbook = XLSX.utils.book_new()
    const sheetName = config.options?.sheetName || 'Sheet1'
    
    // 创建工作表数据
    let worksheetData: any[][] = []
    
    // 添加标题
    if (config.title) {
      worksheetData.push([config.title])
      worksheetData.push([]) // 空行
    }
    
    // 添加表头
    if (config.headers && config.headers.length > 0) {
      worksheetData.push(config.headers)
    }
    
    // 添加数据行
    config.data.forEach(row => {
      if (Array.isArray(row)) {
        worksheetData.push(row)
      } else if (typeof row === 'object') {
        // 如果是对象，根据 headers 提取值
        if (config.headers && Array.isArray(config.headers)) {
          const values = config.headers.map(header => {
            // 支持嵌套属性访问，如 'user.name'
            return this.getNestedValue(row, header) || ''
          })
          worksheetData.push(values)
        } else {
          // 如果没有 headers，使用对象的所有值
          worksheetData.push(Object.values(row))
        }
      }
    })
    
    // 验证数据格式
    const validWorksheetData = worksheetData.filter(row => Array.isArray(row))
    
    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(validWorksheetData)
    
    // 自动调整列宽
    if (config.options?.autoWidth !== false) {
      const colWidths = this.calculateColumnWidths(worksheetData)
      worksheet['!cols'] = colWidths
    }
    
    // 设置样式（如果有标题）
    if (config.title) {
      // 合并标题单元格
      const titleRange = XLSX.utils.encode_range({
        s: { c: 0, r: 0 },
        e: { c: Math.max(0, (config.headers?.length || 1) - 1), r: 0 }
      })
      worksheet['!merges'] = [XLSX.utils.decode_range(titleRange)]
    }
    
    XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
    
    // 生成文件名
    const filename = config.filename || `export_${new Date().getTime()}.xlsx`
    
    // 导出文件
    XLSX.writeFile(workbook, filename)
  }

  /**
   * 导出到 PDF
   */
  private static async exportToPDF(config: ExportConfig): Promise<void> {
    const orientation = config.options?.orientation || 'portrait'
    const unit = config.options?.unit || 'mm'
    const format = config.options?.format || 'a4'
    
    const pdf = new jsPDF({
      orientation,
      unit,
      format
    })
    
    // 设置字体（支持中文）
    pdf.setFont('helvetica')
    
    if (config.element) {
      // 从 DOM 元素导出
      const element = typeof config.element === 'string' 
        ? document.querySelector(config.element) as HTMLElement
        : config.element
        
      if (!element) {
        throw new Error('未找到指定的 DOM 元素')
      }
      
      // 使用 html2canvas 截图
      const canvas = await html2canvas(element, {
        scale: config.options?.scale || 2,
        backgroundColor: config.options?.backgroundColor || '#ffffff',
        useCORS: true,
        allowTaint: true
      })
      
      const imgData = canvas.toDataURL('image/png')
      const imgWidth = pdf.internal.pageSize.getWidth()
      const imgHeight = (canvas.height * imgWidth) / canvas.width
      
      pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight)
      
    } else if (config.data && config.headers) {
      // 从数据生成表格
      let yPosition = 20
      
      // 添加标题
      if (config.title) {
        pdf.setFontSize(16)
        pdf.text(config.title, 20, yPosition)
        yPosition += 20
      }
      
      // 添加表格
      pdf.setFontSize(10)
      
      // 表头
      let xPosition = 20
      const colWidth = (pdf.internal.pageSize.getWidth() - 40) / config.headers.length
      
      config.headers.forEach(header => {
        pdf.text(header, xPosition, yPosition)
        xPosition += colWidth
      })
      yPosition += 10
      
      // 数据行
      config.data.forEach(row => {
        xPosition = 20
        const values = Array.isArray(row) ? row : 
          config.headers!.map(header => this.getNestedValue(row, header) || '')
        
        values.forEach(value => {
          pdf.text(String(value), xPosition, yPosition)
          xPosition += colWidth
        })
        yPosition += 8
        
        // 检查是否需要新页面
        if (yPosition > pdf.internal.pageSize.getHeight() - 20) {
          pdf.addPage()
          yPosition = 20
        }
      })
    }
    
    // 生成文件名
    const filename = config.filename || `export_${new Date().getTime()}.pdf`
    
    // 保存文件
    pdf.save(filename)
  }

  /**
   * 导出到图片
   */
  private static async exportToImage(config: ExportConfig): Promise<void> {
    if (!config.element) {
      throw new Error('图片导出需要指定 DOM 元素')
    }
    
    const element = typeof config.element === 'string' 
      ? document.querySelector(config.element) as HTMLElement
      : config.element
      
    if (!element) {
      throw new Error('未找到指定的 DOM 元素')
    }
    
    const canvas = await html2canvas(element, {
      scale: config.options?.scale || 2,
      backgroundColor: config.options?.backgroundColor || '#ffffff',
      useCORS: true,
      allowTaint: true,
      width: config.options?.width,
      height: config.options?.height
    })
    
    // 转换为指定格式
    const mimeType = config.format === 'jpg' ? 'image/jpeg' : 'image/png'
    const quality = config.options?.quality || 0.9
    
    const dataURL = canvas.toDataURL(mimeType, quality)
    
    // 创建下载链接
    const link = document.createElement('a')
    link.download = config.filename || `export_${new Date().getTime()}.${config.format}`
    link.href = dataURL
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  /**
   * 获取嵌套对象的值
   */
  private static getNestedValue(obj: any, path: string): any {
    return path.split('.').reduce((current, key) => {
      return current && current[key] !== undefined ? current[key] : undefined
    }, obj)
  }

  /**
   * 计算 Excel 列宽
   */
  private static calculateColumnWidths(data: any[][]): Array<{ wch: number }> {
    const colWidths: number[] = []
    
    data.forEach(row => {
      row.forEach((cell, colIndex) => {
        const cellLength = String(cell || '').length
        colWidths[colIndex] = Math.max(colWidths[colIndex] || 0, cellLength)
      })
    })
    
    return colWidths.map(width => ({ wch: Math.min(width + 2, 50) }))
  }

  /**
   * 导出图表为图片
   */
  static async exportChart(chartInstance: any, config: Partial<ExportConfig> = {}): Promise<void> {
    if (!chartInstance) {
      throw new Error('图表实例不能为空')
    }
    
    try {
      const dataURL = chartInstance.getDataURL({
        type: config.format === 'jpg' ? 'jpeg' : 'png',
        pixelRatio: config.options?.scale || 2,
        backgroundColor: config.options?.backgroundColor || '#ffffff'
      })
      
      const link = document.createElement('a')
      link.download = config.filename || `chart_${new Date().getTime()}.${config.format || 'png'}`
      link.href = dataURL
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('图表导出成功')
    } catch (error) {
      console.error('图表导出失败:', error)
      ElMessage.error('图表导出失败')
      throw error
    }
  }

  /**
   * 批量导出多个图表
   */
  static async exportMultipleCharts(
    charts: Array<{ instance: any; filename: string }>,
    format: 'png' | 'jpg' = 'png'
  ): Promise<void> {
    try {
      for (const chart of charts) {
        await this.exportChart(chart.instance, {
          filename: chart.filename,
          format
        })
        // 添加延迟避免浏览器阻止多个下载
        await new Promise(resolve => setTimeout(resolve, 500))
      }
      ElMessage.success(`成功导出 ${charts.length} 个图表`)
    } catch (error) {
      console.error('批量导出失败:', error)
      ElMessage.error('批量导出失败')
      throw error
    }
  }

  /**
   * 导出报表数据（包含多个工作表的 Excel）
   */
  static async exportReport(
    sheets: Array<{
      name: string
      data: any[]
      headers?: string[]
      title?: string
    }>,
    filename?: string
  ): Promise<void> {
    try {
      const workbook = XLSX.utils.book_new()
      
      sheets.forEach(sheet => {
        let worksheetData: any[][] = []
        
        // 添加标题
        if (sheet.title) {
          worksheetData.push([sheet.title])
          worksheetData.push([]) // 空行
        }
        
        // 添加表头
        if (sheet.headers && sheet.headers.length > 0) {
          worksheetData.push(sheet.headers)
        }
        
        // 添加数据
        sheet.data.forEach(row => {
          if (Array.isArray(row)) {
            worksheetData.push(row)
          } else if (typeof row === 'object') {
            if (sheet.headers) {
              const values = sheet.headers.map(header => 
                this.getNestedValue(row, header) || ''
              )
              worksheetData.push(values)
            } else {
              worksheetData.push(Object.values(row))
            }
          }
        })
        
        const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)
        
        // 自动调整列宽
        const colWidths = this.calculateColumnWidths(worksheetData)
        worksheet['!cols'] = colWidths
        
        XLSX.utils.book_append_sheet(workbook, worksheet, sheet.name)
      })
      
      const exportFilename = filename || `report_${new Date().getTime()}.xlsx`
      XLSX.writeFile(workbook, exportFilename)
      
      ElMessage.success('报表导出成功')
    } catch (error) {
      console.error('报表导出失败:', error)
      ElMessage.error('报表导出失败')
      throw error
    }
  }
}

/**
 * 快捷导出方法
 */
export const exportToExcel = (data: any[], headersOrFilename?: string[] | string, filename?: string) => {
  let headers: string[] | undefined
  let finalFilename: string | undefined
  
  // 如果第二个参数是字符串，则认为是filename
  if (typeof headersOrFilename === 'string') {
    headers = undefined
    finalFilename = headersOrFilename
  } else {
    headers = headersOrFilename
    finalFilename = filename
  }
  
  return ExportUtils.exportData({
    format: 'excel',
    data,
    headers,
    filename: finalFilename
  })
}

export const exportToPDF = (element: HTMLElement | string, filename?: string) => {
  return ExportUtils.exportData({
    format: 'pdf',
    element,
    filename
  })
}

export const exportToImage = (element: HTMLElement | string, format: 'png' | 'jpg' = 'png', filename?: string) => {
  return ExportUtils.exportData({
    format,
    element,
    filename
  })
}

export const exportChart = (chartInstance: any, filename?: string, format: 'png' | 'jpg' = 'png') => {
  return ExportUtils.exportChart(chartInstance, { filename, format })
}

// 通用导出方法
export const exportData = async (config: ExportConfig) => {
  const { format, filename, title, data, headers, chartId, element, options = {} } = config
  
  try {
    switch (format) {
      case 'excel':
        if (!data || !headers) {
          throw new Error('Excel导出需要数据和表头')
        }
        return exportToExcel(data, headers, filename)
        
      case 'pdf':
        if (element) {
          return exportToPDF(element, filename)
        } else {
          throw new Error('PDF导出需要指定元素')
        }
        
      case 'png':
      case 'jpg':
        if (chartId) {
          // 查找图表实例
          const chartElement = document.getElementById(chartId)
          if (!chartElement) {
            throw new Error(`未找到ID为 ${chartId} 的图表元素`)
          }
          
          // 尝试获取ECharts实例
          const chartInstance = (window as any).echarts?.getInstanceByDom(chartElement)
          if (chartInstance) {
            return exportChart(chartInstance, filename, format)
          } else {
            // 如果不是ECharts实例，则导出DOM元素
            return exportToImage(chartElement, format, filename)
          }
        } else if (element) {
          return exportToImage(element, format, filename)
        } else {
          throw new Error('图片导出需要指定图表ID或元素')
        }
        
      default:
        throw new Error(`不支持的导出格式: ${format}`)
    }
  } catch (error) {
    console.error('导出失败:', error)
    return { success: false, message: `导出失败: ${(error as Error).message}` }
  }
}

/**
 * 导出到CSV文件
 */
export const exportToCSV = (data: any[], filename?: string, options?: { delimiter?: string }) => {
  const delimiter = options?.delimiter || ','
  
  if (!data || data.length === 0) {
    throw new Error('没有数据可导出')
  }
  
  // 获取表头
  const headers = Object.keys(data[0])
  
  // 构建CSV内容
  let csvContent = headers.join(delimiter) + '\n'
  
  // 添加数据行
  data.forEach(row => {
    const values = headers.map(header => {
      let value = row[header]
      // 处理包含逗号或引号的值
      if (typeof value === 'string' && (value.includes(delimiter) || value.includes('"'))) {
        value = '"' + value.replace(/"/g, '""') + '"'
      }
      return value || ''
    })
    csvContent += values.join(delimiter) + '\n'
  })
  
  // 创建Blob并下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const finalFilename = filename || generateExportFilename('export', 'csv')
  downloadFile(blob, finalFilename)
}

/**
 * 下载文件
 */
export const downloadFile = (data: Blob | string, filename: string) => {
  const link = document.createElement('a')
  
  if (data instanceof Blob) {
    const url = URL.createObjectURL(data)
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } else {
    // 如果是字符串URL
    link.href = data
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

/**
 * 格式化数据用于导出
 */
export const formatDataForExport = (data: any[], columns?: { key: string; title: string; formatter?: (value: any) => any }[]) => {
  if (!data || data.length === 0) return []
  
  if (!columns) {
    // 如果没有指定列配置，返回原始数据
    return data
  }
  
  return data.map(row => {
    const formattedRow: any = {}
    columns.forEach(col => {
      // 支持嵌套属性访问，如 'user.name'
      let value = getNestedValue(row, col.key)
      
      // 如果有格式化函数，应用它
      if (col.formatter && typeof col.formatter === 'function') {
        value = col.formatter(value)
      }
      
      formattedRow[col.title] = value
    })
    return formattedRow
  })
}

/**
 * 获取嵌套属性值
 */
const getNestedValue = (obj: any, path: string): any => {
  return path.split('.').reduce((current, key) => {
    return current && current[key] !== undefined ? current[key] : ''
  }, obj)
}

/**
 * 生成导出文件名
 */
export const generateExportFilename = (prefix: string = 'export', extension: string = 'xlsx') => {
  const now = new Date()
  const dateStr = now.getFullYear().toString() + 
                  (now.getMonth() + 1).toString().padStart(2, '0') + 
                  now.getDate().toString().padStart(2, '0')
  const timeStr = now.getHours().toString().padStart(2, '0') + 
                  now.getMinutes().toString().padStart(2, '0') + 
                  now.getSeconds().toString().padStart(2, '0')
  
  return `${prefix}_${dateStr}_${timeStr}.${extension}`
}

/**
 * 验证导出数据
 */
export const validateExportData = (data: any[]): boolean => {
  if (!data || !Array.isArray(data) || data.length === 0) {
    return false
  }
  
  // 检查数据结构一致性
  const firstRowKeys = Object.keys(data[0])
  return data.every(row => {
    const rowKeys = Object.keys(row)
    return firstRowKeys.length === rowKeys.length && 
           firstRowKeys.every(key => rowKeys.includes(key))
  })
}