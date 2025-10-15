import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { defineStore } from 'pinia'

// 创建简单的store示例
const useUserStore = defineStore('user', {
  state: () => ({
    username: '',
    isLoggedIn: false,
    userId: null as number | null
  }),
  actions: {
    login(username: string, userId: number) {
      this.username = username
      this.userId = userId
      this.isLoggedIn = true
    },
    logout() {
      this.username = ''
      this.userId = null
      this.isLoggedIn = false
    }
  },
  getters: {
    displayName: (state) => state.username || '游客'
  }
})

describe('User Store', () => {
  beforeEach(() => {
    // 每个测试前创建新的pinia实例
    setActivePinia(createPinia())
  })

  it('应该有正确的初始状态', () => {
    const store = useUserStore()
    expect(store.username).toBe('')
    expect(store.isLoggedIn).toBe(false)
    expect(store.userId).toBeNull()
  })

  it('应该能够登录', () => {
    const store = useUserStore()
    store.login('testuser', 123)
    
    expect(store.username).toBe('testuser')
    expect(store.userId).toBe(123)
    expect(store.isLoggedIn).toBe(true)
  })

  it('应该能够登出', () => {
    const store = useUserStore()
    store.login('testuser', 123)
    store.logout()
    
    expect(store.username).toBe('')
    expect(store.userId).toBeNull()
    expect(store.isLoggedIn).toBe(false)
  })

  it('displayName getter应该正确工作', () => {
    const store = useUserStore()
    
    // 未登录时显示"游客"
    expect(store.displayName).toBe('游客')
    
    // 登录后显示用户名
    store.login('testuser', 123)
    expect(store.displayName).toBe('testuser')
  })

  it('应该支持多次登录', () => {
    const store = useUserStore()
    
    store.login('user1', 1)
    expect(store.username).toBe('user1')
    
    store.login('user2', 2)
    expect(store.username).toBe('user2')
    expect(store.userId).toBe(2)
  })
})

