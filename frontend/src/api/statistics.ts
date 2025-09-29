import request from '@/utils/request'

// 档案统计数据接口
export interface ArchiveStatistics {
  totalCount: number
  todayCount: number
  weekCount: number
  monthCount: number
  borrowedCount: number
  availableCount: number
  overdueCount: number
  reservedCount: number
}

// 借阅趋势数据接口
export interface BorrowTrend {
  dates: string[]
  borrowCounts: number[]
  returnCounts: number[]
}

// 用户活跃度数据接口
export interface UserActivity {
  activeUsers: number
  totalUsers: number
  loginCount: number
  operationCount: number
  hourlyActivity: Array<{
    hour: string
    count: number
  }>
}

// 最新动态数据接口
export interface RecentActivity {
  id: number
  type: string
  user: string
  action: string
  target: string
  time: string
  avatar: string
}

// 待办事项数据接口
export interface TodoItem {
  id: number
  title: string
  description: string
  priority: string
  dueDate: string
  status: string
  count: number
}

// 仪表板概览数据接口
export interface DashboardOverviewData {
  archiveCount: ArchiveStatistics
  borrowTrend: BorrowTrend
  userActivity: UserActivity
  recentActivities: RecentActivity[]
  todoItems: TodoItem[]
}

// 统计API服务类
export class StatisticsAPI {
  // 获取档案统计数据
  static async getArchiveCount(): Promise<ArchiveStatistics> {
    const response = await request.get<ArchiveStatistics>('/v1/archives/statistics')
    return response.data
  }

  // 获取借阅趋势数据
  static async getBorrowTrend(): Promise<BorrowTrend> {
    const response = await request.get<BorrowTrend>('/api/system/statistics/borrow-trend')
    return response.data
  }

  // 获取用户活跃度数据
  static async getUserActivity(): Promise<UserActivity> {
    const response = await request.get<UserActivity>('/api/system/statistics/user-activity')
    return response.data
  }

  // 获取最新动态数据
  static async getRecentActivities(): Promise<RecentActivity[]> {
    const response = await request.get<RecentActivity[]>('/api/system/activities/recent')
    return response.data
  }

  // 获取待办事项数据
  static async getTodoItems(): Promise<TodoItem[]> {
    const response = await request.get<TodoItem[]>('/api/system/todos/pending')
    return response.data
  }

  // 获取仪表板概览数据（聚合所有数据）
  static async getDashboardOverview(): Promise<DashboardOverviewData> {
    try {
      // 并行获取所有数据
      const [archiveCount, borrowTrend, userActivity, recentActivities, todoItems] = await Promise.all([
        this.getArchiveCount(),
        this.getBorrowTrend(),
        this.getUserActivity(),
        this.getRecentActivities(),
        this.getTodoItems()
      ])

      return {
        archiveCount,
        borrowTrend,
        userActivity,
        recentActivities,
        todoItems
      }
    } catch (error) {
      console.error('获取仪表板概览数据失败:', error)
      throw error
    }
  }
}
