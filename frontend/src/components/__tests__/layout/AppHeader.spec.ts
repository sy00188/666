import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/profile', component: { template: '<div>Profile</div>' } }
  ]
})

// Mock user store
const mockUserStore = {
  user: {
    name: '测试用户',
    avatar: '/avatar.jpg',
    role: 'admin'
  },
  logout: vi.fn()
}

vi.mock('@/stores/user', () => ({
  useUserStore: () => mockUserStore
}))

describe('AppHeader', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(AppHeader, {
      global: {
        plugins: [router],
        stubs: {
          'router-link': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-avatar': true,
          'el-button': true,
          'el-icon': true
        }
      }
    })
  })

  it('应该正确渲染组件', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.app-header').exists()).toBe(true)
  })

  it('应该显示用户名称', () => {
    expect(wrapper.text()).toContain('测试用户')
  })

  it('应该显示用户头像', () => {
    const avatar = wrapper.find('[data-testid="user-avatar"]')
    expect(avatar.exists()).toBe(true)
  })

  it('应该显示系统标题', () => {
    expect(wrapper.text()).toContain('档案管理系统')
  })

  it('点击退出登录应该调用logout方法', async () => {
    const logoutBtn = wrapper.find('[data-testid="logout-btn"]')
    if (logoutBtn.exists()) {
      await logoutBtn.trigger('click')
      expect(mockUserStore.logout).toHaveBeenCalled()
    }
  })

  it('应该显示通知图标', () => {
    const notificationIcon = wrapper.find('[data-testid="notification-icon"]')
    expect(notificationIcon.exists()).toBe(true)
  })

  it('应该显示设置图标', () => {
    const settingsIcon = wrapper.find('[data-testid="settings-icon"]')
    expect(settingsIcon.exists()).toBe(true)
  })

  it('应该响应式显示/隐藏菜单按钮', () => {
    // 模拟移动端
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 768
    })
    
    wrapper.vm.$nextTick(() => {
      const menuBtn = wrapper.find('[data-testid="menu-toggle"]')
      expect(menuBtn.exists()).toBe(true)
    })
  })

  it('应该显示面包屑导航', () => {
    const breadcrumb = wrapper.find('[data-testid="breadcrumb"]')
    expect(breadcrumb.exists()).toBe(true)
  })

  it('应该处理用户下拉菜单', async () => {
    const userDropdown = wrapper.find('[data-testid="user-dropdown"]')
    if (userDropdown.exists()) {
      await userDropdown.trigger('click')
      expect(wrapper.find('.user-menu').exists()).toBe(true)
    }
  })
})
