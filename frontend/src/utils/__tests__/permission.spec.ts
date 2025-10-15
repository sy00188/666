import { describe, it, expect, beforeEach, vi } from 'vitest'
import { 
  hasPermission,
  hasAnyPermission,
  hasAllPermissions,
  checkPermission,
  getPermissionTree,
  filterMenuByPermission,
  formatPermissionCode,
  validatePermissionCode,
  getPermissionLevel,
  isSystemPermission,
  setUserPermissions
} from '@/utils/permission'

// Mock用户权限数据
const mockUserPermissions = [
  'user:read',
  'user:write',
  'user:delete',
  'role:read',
  'role:write',
  'system:config',
  'system:manage',
  'archive:read',
  'archive:write'
]

// Mock权限树数据
const mockPermissionTree = [
  {
    code: 'system',
    name: '系统管理',
    children: [
      { code: 'system:user', name: '用户管理' },
      { code: 'system:role', name: '角色管理' },
      { code: 'system:config', name: '系统配置' }
    ]
  },
  {
    code: 'archive',
    name: '档案管理',
    children: [
      { code: 'archive:read', name: '查看档案' },
      { code: 'archive:write', name: '编辑档案' },
      { code: 'archive:delete', name: '删除档案' }
    ]
  }
]

// Mock菜单数据
const mockMenus = [
  {
    path: '/system',
    name: '系统管理',
    permission: 'system:manage',
    children: [
      {
        path: '/system/user',
        name: '用户管理',
        permission: 'user:read'
      },
      {
        path: '/system/role',
        name: '角色管理',
        permission: 'role:read'
      }
    ]
  },
  {
    path: '/archive',
    name: '档案管理',
    permission: 'archive:read',
    children: [
      {
        path: '/archive/list',
        name: '档案列表',
        permission: 'archive:read'
      },
      {
        path: '/archive/create',
        name: '创建档案',
        permission: 'archive:write'
      }
    ]
  }
]

describe('permission.ts', () => {
  beforeEach(() => {
    // 设置用户权限
    setUserPermissions(mockUserPermissions)
    
    // Mock localStorage
    vi.stubGlobal('localStorage', {
      getItem: vi.fn(() => JSON.stringify(mockUserPermissions)),
      setItem: vi.fn(),
      removeItem: vi.fn()
    })
  })

  describe('hasPermission', () => {
    it('应该正确检查单个权限', () => {
      expect(hasPermission('user:read')).toBe(true)
      expect(hasPermission('user:delete')).toBe(true)
      expect(hasPermission('user:admin')).toBe(false)
    })

    it('应该处理空权限', () => {
      expect(hasPermission('')).toBe(false)
      expect(hasPermission(null)).toBe(false)
      expect(hasPermission(undefined)).toBe(false)
    })

    it('应该支持通配符权限', () => {
      expect(hasPermission('user:*')).toBe(true)
      expect(hasPermission('admin:*')).toBe(false)
    })

    it('应该支持自定义权限列表', () => {
      const customPermissions = ['custom:read', 'custom:write']
      expect(hasPermission('custom:read', customPermissions)).toBe(true)
      expect(hasPermission('custom:delete', customPermissions)).toBe(false)
    })
  })

  describe('hasAnyPermission', () => {
    it('应该正确检查多个权限中的任意一个', () => {
      expect(hasAnyPermission(['user:read', 'user:admin'])).toBe(true)
      expect(hasAnyPermission(['admin:read', 'admin:write'])).toBe(false)
    })

    it('应该处理空权限数组', () => {
      expect(hasAnyPermission([])).toBe(false)
      expect(hasAnyPermission(null)).toBe(false)
    })

    it('应该支持字符串参数', () => {
      expect(hasAnyPermission('user:read,user:admin')).toBe(true)
      expect(hasAnyPermission('admin:read,admin:write')).toBe(false)
    })
  })

  describe('hasAllPermissions', () => {
    it('应该正确检查是否拥有所有权限', () => {
      expect(hasAllPermissions(['user:read', 'user:write'])).toBe(true)
      expect(hasAllPermissions(['user:read', 'user:admin'])).toBe(false)
    })

    it('应该处理空权限数组', () => {
      expect(hasAllPermissions([])).toBe(true)
      expect(hasAllPermissions(null)).toBe(true)
    })

    it('应该支持字符串参数', () => {
      expect(hasAllPermissions('user:read,user:write')).toBe(true)
      expect(hasAllPermissions('user:read,user:admin')).toBe(false)
    })
  })

  describe('checkPermission', () => {
    it('应该返回权限检查结果对象', () => {
      const result = checkPermission('user:read')
      
      expect(result).toHaveProperty('hasPermission')
      expect(result).toHaveProperty('missingPermissions')
      expect(result.hasPermission).toBe(true)
      expect(result.missingPermissions).toEqual([])
    })

    it('应该返回缺失的权限', () => {
      const result = checkPermission(['user:read', 'user:admin'])
      
      expect(result.hasPermission).toBe(false)
      expect(result.missingPermissions).toEqual(['user:admin'])
    })

    it('应该包含权限详细信息', () => {
      const result = checkPermission('user:read', { includeDetails: true })
      
      expect(result).toHaveProperty('details')
      expect(result.details).toHaveProperty('checkedPermissions')
      expect(result.details).toHaveProperty('userPermissions')
    })
  })

  describe('getPermissionTree', () => {
    it('应该返回权限树结构', () => {
      const tree = getPermissionTree(mockPermissionTree)
      
      expect(Array.isArray(tree)).toBe(true)
      expect(tree[0]).toHaveProperty('code')
      expect(tree[0]).toHaveProperty('name')
      expect(tree[0]).toHaveProperty('children')
    })

    it('应该过滤用户无权限的节点', () => {
      const tree = getPermissionTree(mockPermissionTree, { filterByPermission: true })
      
      // 应该只包含用户有权限的节点
      const hasUnauthorizedNode = tree.some(node => 
        !mockUserPermissions.some(permission => 
          permission.startsWith(node.code)
        )
      )
      expect(hasUnauthorizedNode).toBe(false)
    })

    it('应该支持自定义过滤函数', () => {
      const customFilter = (node: any) => node.code.includes('system')
      const tree = getPermissionTree(mockPermissionTree, { filter: customFilter })
      
      expect(tree.every(node => node.code.includes('system'))).toBe(true)
    })
  })

  describe('filterMenuByPermission', () => {
    it('应该过滤用户无权限的菜单', () => {
      const filteredMenus = filterMenuByPermission(mockMenus)
      
      // 应该包含有权限的父菜单
      const systemMenu = filteredMenus.find(menu => menu.permission === 'system:manage')
      expect(systemMenu).toBeDefined()
      
      // 应该在子菜单中包含有权限的菜单
      const userMenu = systemMenu?.children?.find(child => child.permission === 'user:read')
      expect(userMenu).toBeDefined()
      
      // 应该排除无权限的菜单（admin:read不在mockUserPermissions中）
      const adminMenu = filteredMenus.find(menu => menu.permission === 'admin:read')
      expect(adminMenu).toBeUndefined()
    })

    it('应该递归过滤子菜单', () => {
      const filteredMenus = filterMenuByPermission(mockMenus)
      
      const systemMenu = filteredMenus.find(menu => menu.path === '/system')
      if (systemMenu && systemMenu.children) {
        const userSubMenu = systemMenu.children.find(child => child.permission === 'user:read')
        expect(userSubMenu).toBeDefined()
      }
    })

    it('应该处理没有权限字段的菜单', () => {
      const menusWithoutPermission = [
        { path: '/public', name: '公开页面' },
        { path: '/help', name: '帮助中心' }
      ]
      
      const filteredMenus = filterMenuByPermission(menusWithoutPermission)
      expect(filteredMenus).toHaveLength(2)
    })
  })

  describe('formatPermissionCode', () => {
    it('应该正确格式化权限代码', () => {
      expect(formatPermissionCode('user:read')).toBe('user:read')
      expect(formatPermissionCode('USER:READ')).toBe('user:read')
      expect(formatPermissionCode('user.read')).toBe('user:read')
      expect(formatPermissionCode('user_read')).toBe('user:read')
    })

    it('应该处理无效的权限代码', () => {
      expect(formatPermissionCode('')).toBe('')
      expect(formatPermissionCode(null)).toBe('')
      expect(formatPermissionCode('invalid')).toBe('invalid')
    })

    it('应该支持自定义分隔符', () => {
      expect(formatPermissionCode('user.read', '.')).toBe('user.read')
      expect(formatPermissionCode('user:read', '.')).toBe('user.read')
    })
  })

  describe('validatePermissionCode', () => {
    it('应该正确验证权限代码格式', () => {
      expect(validatePermissionCode('user:read')).toBe(true)
      expect(validatePermissionCode('system:config:update')).toBe(true)
      expect(validatePermissionCode('invalid')).toBe(false)
      expect(validatePermissionCode('')).toBe(false)
    })

    it('应该支持自定义验证规则', () => {
      const customRules = {
        minLength: 5,
        maxLength: 50,
        allowedChars: /^[a-z:]+$/
      }
      
      expect(validatePermissionCode('user:read', customRules)).toBe(true)
      expect(validatePermissionCode('USER:READ', customRules)).toBe(false)
      expect(validatePermissionCode('ab', customRules)).toBe(false)
    })
  })

  describe('getPermissionLevel', () => {
    it('应该正确获取权限级别', () => {
      expect(getPermissionLevel('user')).toBe(1)
      expect(getPermissionLevel('user:read')).toBe(2)
      expect(getPermissionLevel('user:read:detail')).toBe(3)
    })

    it('应该处理无效权限代码', () => {
      expect(getPermissionLevel('')).toBe(0)
      expect(getPermissionLevel(null)).toBe(0)
    })

    it('应该支持自定义分隔符', () => {
      expect(getPermissionLevel('user.read.detail', '.')).toBe(3)
    })
  })

  describe('isSystemPermission', () => {
    it('应该正确识别系统权限', () => {
      expect(isSystemPermission('system:config')).toBe(true)
      expect(isSystemPermission('admin:manage')).toBe(true)
      expect(isSystemPermission('user:read')).toBe(false)
    })

    it('应该支持自定义系统权限前缀', () => {
      const customPrefixes = ['custom', 'internal']
      expect(isSystemPermission('custom:config', customPrefixes)).toBe(true)
      expect(isSystemPermission('user:read', customPrefixes)).toBe(false)
    })

    it('应该处理无效权限代码', () => {
      expect(isSystemPermission('')).toBe(false)
      expect(isSystemPermission(null)).toBe(false)
    })
  })
})
