import { describe, it, expect } from 'vitest'
import { 
  formatDate, 
  parseDate, 
  isValidDate, 
  getDateRange, 
  addDays, 
  subtractDays,
  getRelativeTime,
  formatDateTime,
  getWeekRange,
  getMonthRange,
  isToday,
  isSameDay,
  getDaysDiff
} from '@/utils/date'

describe('date.ts', () => {
  describe('formatDate', () => {
    it('应该正确格式化日期', () => {
      const date = new Date('2024-01-15T10:30:00')
      expect(formatDate(date, 'YYYY-MM-DD')).toBe('2024-01-15')
      expect(formatDate(date, 'MM/DD/YYYY')).toBe('01/15/2024')
      expect(formatDate(date, 'DD-MM-YYYY')).toBe('15-01-2024')
    })

    it('应该处理无效日期', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate(undefined)).toBe('')
      expect(formatDate(new Date('invalid'))).toBe('')
    })

    it('应该支持中文格式', () => {
      const date = new Date('2024-01-15')
      expect(formatDate(date, 'YYYY年MM月DD日')).toBe('2024年01月15日')
    })
  })

  describe('parseDate', () => {
    it('应该正确解析日期字符串', () => {
      expect(parseDate('2024-01-15')).toEqual(new Date('2024-01-15'))
      expect(parseDate('01/15/2024', 'MM/DD/YYYY')).toEqual(new Date('2024-01-15'))
    })

    it('应该处理无效日期字符串', () => {
      expect(parseDate('invalid')).toBeNull()
      expect(parseDate('')).toBeNull()
      expect(parseDate(null)).toBeNull()
    })
  })

  describe('isValidDate', () => {
    it('应该正确验证日期', () => {
      expect(isValidDate(new Date())).toBe(true)
      expect(isValidDate(new Date('2024-01-15'))).toBe(true)
      expect(isValidDate(new Date('invalid'))).toBe(false)
      expect(isValidDate(null)).toBe(false)
      expect(isValidDate(undefined)).toBe(false)
    })
  })

  describe('getDateRange', () => {
    it('应该返回正确的日期范围', () => {
      const startDate = new Date('2024-01-01')
      const endDate = new Date('2024-01-03')
      const range = getDateRange(startDate, endDate)
      
      expect(range).toHaveLength(3)
      expect(range[0]).toEqual(startDate)
      expect(range[2]).toEqual(endDate)
    })

    it('应该处理相同的开始和结束日期', () => {
      const date = new Date('2024-01-01')
      const range = getDateRange(date, date)
      
      expect(range).toHaveLength(1)
      expect(range[0]).toEqual(date)
    })
  })

  describe('addDays', () => {
    it('应该正确添加天数', () => {
      const date = new Date('2024-01-15')
      const result = addDays(date, 5)
      
      expect(result).toEqual(new Date('2024-01-20'))
    })

    it('应该处理负数天数', () => {
      const date = new Date('2024-01-15')
      const result = addDays(date, -5)
      
      expect(result).toEqual(new Date('2024-01-10'))
    })

    it('应该处理跨月份的情况', () => {
      const date = new Date('2024-01-30')
      const result = addDays(date, 5)
      
      expect(result).toEqual(new Date('2024-02-04'))
    })
  })

  describe('subtractDays', () => {
    it('应该正确减去天数', () => {
      const date = new Date('2024-01-15')
      const result = subtractDays(date, 5)
      
      expect(result).toEqual(new Date('2024-01-10'))
    })

    it('应该处理跨月份的情况', () => {
      const date = new Date('2024-02-05')
      const result = subtractDays(date, 10)
      
      expect(result).toEqual(new Date('2024-01-26'))
    })
  })

  describe('getRelativeTime', () => {
    it('应该返回相对时间描述', () => {
      const now = new Date()
      const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000)
      const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000)
      
      expect(getRelativeTime(oneHourAgo)).toContain('小时前')
      expect(getRelativeTime(oneDayAgo)).toContain('天前')
    })

    it('应该处理未来时间', () => {
      const now = new Date()
      const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000)
      
      expect(getRelativeTime(oneHourLater)).toContain('小时后')
    })

    it('应该处理刚刚的时间', () => {
      const now = new Date()
      const justNow = new Date(now.getTime() - 30 * 1000) // 30秒前
      
      expect(getRelativeTime(justNow)).toBe('刚刚')
    })
  })

  describe('formatDateTime', () => {
    it('应该正确格式化日期时间', () => {
      const date = new Date('2024-01-15T10:30:45')
      
      expect(formatDateTime(date)).toBe('2024-01-15 10:30:45')
      expect(formatDateTime(date, 'YYYY/MM/DD HH:mm')).toBe('2024/01/15 10:30')
    })

    it('应该处理无效日期时间', () => {
      expect(formatDateTime(null)).toBe('')
      expect(formatDateTime(new Date('invalid'))).toBe('')
    })
  })

  describe('getWeekRange', () => {
    it('应该返回正确的周范围', () => {
      const date = new Date('2024-01-15') // 假设是周一
      const range = getWeekRange(date)
      
      expect(range).toHaveProperty('start')
      expect(range).toHaveProperty('end')
      expect(range.start).toBeInstanceOf(Date)
      expect(range.end).toBeInstanceOf(Date)
    })

    it('应该处理周日开始的情况', () => {
      const date = new Date('2024-01-15')
      const range = getWeekRange(date, true) // 周日开始
      
      expect(range.start.getDay()).toBe(0) // 周日
    })
  })

  describe('getMonthRange', () => {
    it('应该返回正确的月份范围', () => {
      const date = new Date('2024-01-15')
      const range = getMonthRange(date)
      
      expect(range.start.getDate()).toBe(1)
      expect(range.end.getMonth()).toBe(date.getMonth())
    })

    it('应该处理二月份', () => {
      const date = new Date('2024-02-15') // 2024是闰年
      const range = getMonthRange(date)
      
      expect(range.end.getDate()).toBe(29) // 闰年二月有29天
    })
  })

  describe('isToday', () => {
    it('应该正确判断是否为今天', () => {
      const today = new Date()
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      
      expect(isToday(today)).toBe(true)
      expect(isToday(yesterday)).toBe(false)
    })

    it('应该处理不同时间的同一天', () => {
      const morning = new Date()
      morning.setHours(8, 0, 0, 0)
      
      const evening = new Date()
      evening.setHours(20, 0, 0, 0)
      
      expect(isToday(morning)).toBe(true)
      expect(isToday(evening)).toBe(true)
    })
  })

  describe('isSameDay', () => {
    it('应该正确判断是否为同一天', () => {
      const date1 = new Date('2024-01-15T08:00:00')
      const date2 = new Date('2024-01-15T20:00:00')
      const date3 = new Date('2024-01-16T08:00:00')
      
      expect(isSameDay(date1, date2)).toBe(true)
      expect(isSameDay(date1, date3)).toBe(false)
    })

    it('应该处理无效日期', () => {
      const validDate = new Date('2024-01-15')
      const invalidDate = new Date('invalid')
      
      expect(isSameDay(validDate, invalidDate)).toBe(false)
      expect(isSameDay(invalidDate, validDate)).toBe(false)
    })
  })

  describe('getDaysDiff', () => {
    it('应该正确计算日期差', () => {
      const date1 = new Date('2024-01-15')
      const date2 = new Date('2024-01-20')
      
      expect(getDaysDiff(date1, date2)).toBe(5)
      expect(getDaysDiff(date2, date1)).toBe(-5)
    })

    it('应该处理相同日期', () => {
      const date = new Date('2024-01-15')
      
      expect(getDaysDiff(date, date)).toBe(0)
    })

    it('应该处理跨年的情况', () => {
      const date1 = new Date('2023-12-30')
      const date2 = new Date('2024-01-05')
      
      expect(getDaysDiff(date1, date2)).toBe(6)
    })
  })
})
