import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { nextTick } from 'vue'
import { useNotification } from '@/composables/useNotification'

// Mock Element Plus
const mockElMessage = {
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
  info: vi.fn()
}

const mockElNotification = {
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
  info: vi.fn(),
  close: vi.fn(),
  closeAll: vi.fn()
}

vi.mock('element-plus', () => ({
  ElMessage: mockElMessage,
  ElNotification: mockElNotification
}))

// Mock WebSocket
class MockWebSocket {
  onopen: ((event: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null
  onclose: ((event: CloseEvent) => void) | null = null
  onerror: ((event: Event) => void) | null = null
  readyState: number = WebSocket.CONNECTING

  constructor(public url: string) {
    setTimeout(() => {
      this.readyState = WebSocket.OPEN
      if (this.onopen) {
        this.onopen(new Event('open'))
      }
    }, 0)
  }

  send(data: string) {
    // Mock send
  }

  close() {
    this.readyState = WebSocket.CLOSED
    if (this.onclose) {
      this.onclose(new CloseEvent('close'))
    }
  }
}

global.WebSocket = MockWebSocket as any

describe('useNotification', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('基础消息通知', () => {
    it('应该显示成功消息', () => {
      const { showSuccess } = useNotification()

      showSuccess('操作成功')

      expect(mockElMessage.success).toHaveBeenCalledWith('操作成功')
    })

    it('应该显示错误消息', () => {
      const { showError } = useNotification()

      showError('操作失败')

      expect(mockElMessage.error).toHaveBeenCalledWith('操作失败')
    })

    it('应该显示警告消息', () => {
      const { showWarning } = useNotification()

      showWarning('警告信息')

      expect(mockElMessage.warning).toHaveBeenCalledWith('警告信息')
    })

    it('应该显示信息消息', () => {
      const { showInfo } = useNotification()

      showInfo('提示信息')

      expect(mockElMessage.info).toHaveBeenCalledWith('提示信息')
    })

    it('应该支持自定义消息选项', () => {
      const { showSuccess } = useNotification()

      showSuccess('操作成功', {
        duration: 5000,
        showClose: true
      })

      expect(mockElMessage.success).toHaveBeenCalledWith({
        message: '操作成功',
        duration: 5000,
        showClose: true
      })
    })
  })

  describe('通知框', () => {
    it('应该显示成功通知', () => {
      const { notifySuccess } = useNotification()

      notifySuccess('操作成功', '数据已保存')

      expect(mockElNotification.success).toHaveBeenCalledWith({
        title: '操作成功',
        message: '数据已保存'
      })
    })

    it('应该显示错误通知', () => {
      const { notifyError } = useNotification()

      notifyError('操作失败', '网络连接异常')

      expect(mockElNotification.error).toHaveBeenCalledWith({
        title: '操作失败',
        message: '网络连接异常'
      })
    })

    it('应该支持自定义通知选项', () => {
      const { notifyInfo } = useNotification()

      notifyInfo('系统通知', '有新消息', {
        duration: 0,
        position: 'bottom-right'
      })

      expect(mockElNotification.info).toHaveBeenCalledWith({
        title: '系统通知',
        message: '有新消息',
        duration: 0,
        position: 'bottom-right'
      })
    })
  })

  describe('WebSocket实时通知', () => {
    it('应该连接WebSocket并接收消息', async () => {
      const { connectWebSocket, notifications } = useNotification()

      connectWebSocket('ws://localhost:8080/notifications')

      await nextTick()

      // 模拟接收消息
      const mockMessage = {
        type: 'success',
        title: '系统通知',
        message: '有新的档案需要审核',
        timestamp: Date.now()
      }

      // 获取WebSocket实例并模拟消息
      const ws = (global as any).lastWebSocket
      if (ws && ws.onmessage) {
        ws.onmessage(new MessageEvent('message', {
          data: JSON.stringify(mockMessage)
        }))
      }

      expect(notifications.value).toContainEqual(mockMessage)
    })

    it('应该处理WebSocket连接错误', async () => {
      const { connectWebSocket, connectionStatus } = useNotification()

      connectWebSocket('ws://invalid-url')

      // 模拟连接错误
      const ws = (global as any).lastWebSocket
      if (ws && ws.onerror) {
        ws.onerror(new Event('error'))
      }

      expect(connectionStatus.value).toBe('error')
    })

    it('应该支持断开WebSocket连接', () => {
      const { connectWebSocket, disconnectWebSocket, connectionStatus } = useNotification()

      connectWebSocket('ws://localhost:8080/notifications')
      disconnectWebSocket()

      expect(connectionStatus.value).toBe('disconnected')
    })

    it('应该支持WebSocket重连', async () => {
      const { connectWebSocket, reconnect } = useNotification()

      connectWebSocket('ws://localhost:8080/notifications')

      // 模拟连接断开
      const ws = (global as any).lastWebSocket
      if (ws && ws.onclose) {
        ws.onclose(new CloseEvent('close'))
      }

      await reconnect()

      // 应该尝试重新连接
      expect((global as any).lastWebSocket).toBeDefined()
    })
  })

  describe('通知管理', () => {
    it('应该添加通知到列表', () => {
      const { addNotification, notifications } = useNotification()

      const notification = {
        id: '1',
        type: 'info' as const,
        title: '测试通知',
        message: '这是一条测试通知',
        timestamp: Date.now()
      }

      addNotification(notification)

      expect(notifications.value).toContainEqual(notification)
    })

    it('应该移除指定通知', () => {
      const { addNotification, removeNotification, notifications } = useNotification()

      const notification = {
        id: '1',
        type: 'info' as const,
        title: '测试通知',
        message: '这是一条测试通知',
        timestamp: Date.now()
      }

      addNotification(notification)
      removeNotification('1')

      expect(notifications.value).not.toContainEqual(notification)
    })

    it('应该清空所有通知', () => {
      const { addNotification, clearNotifications, notifications } = useNotification()

      addNotification({
        id: '1',
        type: 'info',
        title: '通知1',
        message: '消息1',
        timestamp: Date.now()
      })

      addNotification({
        id: '2',
        type: 'success',
        title: '通知2',
        message: '消息2',
        timestamp: Date.now()
      })

      clearNotifications()

      expect(notifications.value).toHaveLength(0)
    })

    it('应该标记通知为已读', () => {
      const { addNotification, markAsRead, notifications } = useNotification()

      const notification = {
        id: '1',
        type: 'info' as const,
        title: '测试通知',
        message: '这是一条测试通知',
        timestamp: Date.now(),
        read: false
      }

      addNotification(notification)
      markAsRead('1')

      const updatedNotification = notifications.value.find(n => n.id === '1')
      expect(updatedNotification?.read).toBe(true)
    })

    it('应该标记所有通知为已读', () => {
      const { addNotification, markAllAsRead, notifications } = useNotification()

      addNotification({
        id: '1',
        type: 'info',
        title: '通知1',
        message: '消息1',
        timestamp: Date.now(),
        read: false
      })

      addNotification({
        id: '2',
        type: 'success',
        title: '通知2',
        message: '消息2',
        timestamp: Date.now(),
        read: false
      })

      markAllAsRead()

      expect(notifications.value.every(n => n.read)).toBe(true)
    })
  })

  describe('通知统计', () => {
    it('应该计算未读通知数量', () => {
      const { addNotification, unreadCount } = useNotification()

      addNotification({
        id: '1',
        type: 'info',
        title: '通知1',
        message: '消息1',
        timestamp: Date.now(),
        read: false
      })

      addNotification({
        id: '2',
        type: 'success',
        title: '通知2',
        message: '消息2',
        timestamp: Date.now(),
        read: true
      })

      expect(unreadCount.value).toBe(1)
    })

    it('应该按类型过滤通知', () => {
      const { addNotification, getNotificationsByType } = useNotification()

      addNotification({
        id: '1',
        type: 'info',
        title: '信息通知',
        message: '消息1',
        timestamp: Date.now()
      })

      addNotification({
        id: '2',
        type: 'error',
        title: '错误通知',
        message: '消息2',
        timestamp: Date.now()
      })

      const infoNotifications = getNotificationsByType('info')
      expect(infoNotifications).toHaveLength(1)
      expect(infoNotifications[0].type).toBe('info')
    })

    it('应该获取最近的通知', () => {
      const { addNotification, getRecentNotifications } = useNotification()

      const now = Date.now()

      addNotification({
        id: '1',
        type: 'info',
        title: '旧通知',
        message: '消息1',
        timestamp: now - 3600000 // 1小时前
      })

      addNotification({
        id: '2',
        type: 'success',
        title: '新通知',
        message: '消息2',
        timestamp: now
      })

      const recentNotifications = getRecentNotifications(1800000) // 30分钟内
      expect(recentNotifications).toHaveLength(1)
      expect(recentNotifications[0].title).toBe('新通知')
    })
  })

  describe('通知设置', () => {
    it('应该支持启用/禁用通知', () => {
      const { setNotificationEnabled, isNotificationEnabled } = useNotification()

      setNotificationEnabled(false)
      expect(isNotificationEnabled.value).toBe(false)

      setNotificationEnabled(true)
      expect(isNotificationEnabled.value).toBe(true)
    })

    it('应该在禁用时不显示通知', () => {
      const { setNotificationEnabled, showSuccess } = useNotification()

      setNotificationEnabled(false)
      showSuccess('测试消息')

      expect(mockElMessage.success).not.toHaveBeenCalled()
    })

    it('应该支持设置通知类型过滤', () => {
      const { setNotificationFilter, showError, showSuccess } = useNotification()

      setNotificationFilter(['success']) // 只显示成功消息

      showSuccess('成功消息')
      showError('错误消息')

      expect(mockElMessage.success).toHaveBeenCalled()
      expect(mockElMessage.error).not.toHaveBeenCalled()
    })

    it('应该支持设置通知持续时间', () => {
      const { setDefaultDuration, showInfo } = useNotification()

      setDefaultDuration(3000)
      showInfo('测试消息')

      expect(mockElMessage.info).toHaveBeenCalledWith({
        message: '测试消息',
        duration: 3000
      })
    })
  })

  describe('浏览器通知', () => {
    it('应该请求浏览器通知权限', async () => {
      // Mock Notification API
      const mockNotification = vi.fn()
      mockNotification.permission = 'default'
      mockNotification.requestPermission = vi.fn().mockResolvedValue('granted')
      global.Notification = mockNotification as any

      const { requestNotificationPermission } = useNotification()

      const result = await requestNotificationPermission()

      expect(mockNotification.requestPermission).toHaveBeenCalled()
      expect(result).toBe('granted')
    })

    it('应该显示浏览器通知', () => {
      // Mock Notification API
      const mockNotification = vi.fn()
      mockNotification.permission = 'granted'
      global.Notification = mockNotification as any

      const { showBrowserNotification } = useNotification()

      showBrowserNotification('新消息', {
        body: '您有一条新的系统消息',
        icon: '/icon.png'
      })

      expect(mockNotification).toHaveBeenCalledWith('新消息', {
        body: '您有一条新的系统消息',
        icon: '/icon.png'
      })
    })
  })
})
