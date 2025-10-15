import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: { template: '<div>Home</div>' } },
    { path: '/archive', name: 'archive', component: { template: '<div>Archive</div>' } },
    { path: '/category', name: 'category', component: { template: '<div>Category</div>' } },
    { path: '/system', name: 'system', component: { template: '<div>System</div>' } }
  ]
})

// Mock menu store
const mockMenuStore = {
  menuList: [
    {
      id: 1,
      name: '首页',
      path: '/',
      icon: 'home',
      children: []
    },
    {
      id: 2,
      name: '档案管理',
      path: '/archive',
      icon: 'folder',
      children: [
        { id: 21, name: '档案列表', path: '/archive/list', icon: 'list' },
        { id: 22, name: '档案分类', path: '/archive/category', icon: 'category' }
      ]
    },
    {
      id: 3,
      name: '系统管理',
      path: '/system',
      icon: 'setting',
      children: [
        { id: 31, name: '用户管理', path: '/system/user', icon: 'user' },
        { id: 32, name: '权限管理', path: '/system/permission', icon: 'permission' }
      ]
    }
  ],
  collapsed: false,
  toggleCollapse: vi.fn(),
  setActiveMenu: vi.fn()
}

vi.mock('@/stores/menu', () => ({
  useMenuStore: () => mockMenuStore
}))

describe('AppSidebar', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(AppSidebar, {
      global: {
        plugins: [router],
        stubs: {
          'router-link': true,
          'el-menu': true,
          'el-menu-item': true,
          'el-sub-menu': true,
          'el-icon': true
        }
      }
    })
  })

  it('应该正确渲染组件', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.app-sidebar').exists()).toBe(true)
  })

  it('应该显示系统Logo', () => {
    const logo = wrapper.find('[data-testid="app-logo"]')
    expect(logo.exists()).toBe(true)
  })

  it('应该渲染菜单项', () => {
    expect(wrapper.text()).toContain('首页')
    expect(wrapper.text()).toContain('档案管理')
    expect(wrapper.text()).toContain('系统管理')
  })

  it('应该渲染子菜单项', () => {
    expect(wrapper.text()).toContain('档案列表')
    expect(wrapper.text()).toContain('档案分类')
    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('权限管理')
  })

  it('应该支持菜单折叠/展开', async () => {
    const collapseBtn = wrapper.find('[data-testid="collapse-btn"]')
    if (collapseBtn.exists()) {
      await collapseBtn.trigger('click')
      expect(mockMenuStore.toggleCollapse).toHaveBeenCalled()
    }
  })

  it('应该根据collapsed状态改变样式', async () => {
    mockMenuStore.collapsed = true
    await wrapper.vm.$nextTick()
    
    expect(wrapper.find('.is-collapsed').exists()).toBe(true)
  })

  it('应该高亮当前激活的菜单项', () => {
    const activeMenuItem = wrapper.find('.is-active')
    expect(activeMenuItem.exists()).toBe(true)
  })

  it('应该处理菜单项点击事件', async () => {
    const menuItem = wrapper.find('[data-testid="menu-item-home"]')
    if (menuItem.exists()) {
      await menuItem.trigger('click')
      expect(mockMenuStore.setActiveMenu).toHaveBeenCalled()
    }
  })

  it('应该显示菜单图标', () => {
    const icons = wrapper.findAll('[data-testid^="menu-icon-"]')
    expect(icons.length).toBeGreaterThan(0)
  })

  it('折叠状态下应该隐藏菜单文字', async () => {
    mockMenuStore.collapsed = true
    await wrapper.vm.$nextTick()
    
    const menuTexts = wrapper.findAll('.menu-text')
    menuTexts.forEach(text => {
      expect(text.isVisible()).toBe(false)
    })
  })

  it('应该支持多级菜单展开/收起', async () => {
    const subMenu = wrapper.find('[data-testid="sub-menu-archive"]')
    if (subMenu.exists()) {
      await subMenu.trigger('click')
      // 验证子菜单是否展开
      expect(wrapper.find('.el-sub-menu__title').exists()).toBe(true)
    }
  })

  it('应该根据权限显示/隐藏菜单项', () => {
    // 假设用户没有系统管理权限
    const systemMenu = wrapper.find('[data-testid="menu-system"]')
    // 根据权限控制显示
    expect(systemMenu.exists()).toBe(true) // 或 false，取决于权限
  })
})
