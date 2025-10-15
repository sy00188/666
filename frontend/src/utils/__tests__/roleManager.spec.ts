import { describe, it, expect, vi, beforeEach } from 'vitest'
import { TestRoleManager, hasPermission, hasRole, hasAnyRole, hasAllRoles } from '@/utils/roleManager'

// Mock user store
const mockUserStore = {
  user: {
    id: 1,
    name: '测试用户',
    roles: ['admin', 'user'],
    permissions: [
      'user:create',
      'user:read',
      'user:update',
      'archive:read',
      'archive:create',
      'system:config'
    ]
  }
}

vi.mock('@/stores/user', () => ({
  useUserStore: () => mockUserStore
}))

describe('RoleManager', () => {
  let roleManager: TestRoleManager

  beforeEach(() => {
    // 重置 mockUserStore
    mockUserStore.user = {
      id: 1,
      name: '测试用户',
      roles: ['admin', 'user'],
      permissions: [
        'user:create',
        'user:read',
        'user:update',
        'archive:read',
        'archive:create',
        'system:config'
      ]
    }
    
    roleManager = new TestRoleManager(() => mockUserStore.user)
    vi.clearAllMocks()
  })

  describe('hasPermission', () => {
    it('应该正确检查用户权限', () => {
      expect(roleManager.hasPermission('user:create')).toBe(true)
      expect(roleManager.hasPermission('user:read')).toBe(true)
      expect(roleManager.hasPermission('user:delete')).toBe(false)
      expect(roleManager.hasPermission('nonexistent:permission')).toBe(false)
    })

    it('应该支持权限数组检查', () => {
      expect(roleManager.hasPermission(['user:create', 'user:read'])).toBe(true)
      expect(roleManager.hasPermission(['user:create', 'user:delete'])).toBe(false)
      expect(roleManager.hasPermission(['nonexistent:permission'])).toBe(false)
    })

    it('应该处理空权限', () => {
      expect(roleManager.hasPermission('')).toBe(false)
      expect(roleManager.hasPermission([])).toBe(false)
    })

    it('应该处理用户无权限的情况', () => {
      mockUserStore.user.permissions = []
      expect(roleManager.hasPermission('user:create')).toBe(false)
    })
  })

  describe('hasRole', () => {
    it('应该正确检查用户角色', () => {
      expect(roleManager.hasRole('admin')).toBe(true)
      expect(roleManager.hasRole('user')).toBe(true)
      expect(roleManager.hasRole('guest')).toBe(false)
      expect(roleManager.hasRole('superadmin')).toBe(false)
    })

    it('应该处理空角色', () => {
      expect(roleManager.hasRole('')).toBe(false)
    })

    it('应该处理用户无角色的情况', () => {
      mockUserStore.user.roles = []
      expect(roleManager.hasRole('admin')).toBe(false)
    })
  })

  describe('hasAnyRole', () => {
    it('应该检查用户是否拥有任一角色', () => {
      expect(roleManager.hasAnyRole(['admin', 'moderator'])).toBe(true)
      expect(roleManager.hasAnyRole(['user', 'guest'])).toBe(true)
      expect(roleManager.hasAnyRole(['guest', 'visitor'])).toBe(false)
    })

    it('应该处理空角色数组', () => {
      expect(roleManager.hasAnyRole([])).toBe(false)
    })
  })

  describe('hasAllRoles', () => {
    it('应该检查用户是否拥有所有角色', () => {
      expect(roleManager.hasAllRoles(['admin', 'user'])).toBe(true)
      expect(roleManager.hasAllRoles(['admin'])).toBe(true)
      expect(roleManager.hasAllRoles(['admin', 'user', 'guest'])).toBe(false)
    })

    it('应该处理空角色数组', () => {
      expect(roleManager.hasAllRoles([])).toBe(true)
    })
  })

  describe('hasAnyPermission', () => {
    it('应该检查用户是否拥有任一权限', () => {
      expect(roleManager.hasAnyPermission(['user:create', 'user:delete'])).toBe(true)
      expect(roleManager.hasAnyPermission(['user:delete', 'role:delete'])).toBe(false)
    })

    it('应该处理空权限数组', () => {
      expect(roleManager.hasAnyPermission([])).toBe(false)
    })
  })

  describe('hasAllPermissions', () => {
    it('应该检查用户是否拥有所有权限', () => {
      expect(roleManager.hasAllPermissions(['user:create', 'user:read'])).toBe(true)
      expect(roleManager.hasAllPermissions(['user:create', 'user:delete'])).toBe(false)
    })

    it('应该处理空权限数组', () => {
      expect(roleManager.hasAllPermissions([])).toBe(true)
    })
  })

  describe('isAdmin', () => {
    it('应该检查用户是否为管理员', () => {
      expect(roleManager.isAdmin()).toBe(true)
      
      mockUserStore.user.roles = ['user']
      expect(roleManager.isAdmin()).toBe(false)
    })
  })

  describe('isSuperAdmin', () => {
    it('应该检查用户是否为超级管理员', () => {
      expect(roleManager.isSuperAdmin()).toBe(false)
      
      mockUserStore.user.roles = ['superadmin']
      expect(roleManager.isSuperAdmin()).toBe(true)
    })
  })

  describe('canAccess', () => {
    it('应该检查用户是否可以访问资源', () => {
      const resource = {
        requiredPermissions: ['user:read'],
        requiredRoles: ['user']
      }
      
      expect(roleManager.canAccess(resource)).toBe(true)
      
      const restrictedResource = {
        requiredPermissions: ['user:delete'],
        requiredRoles: ['superadmin']
      }
      
      expect(roleManager.canAccess(restrictedResource)).toBe(false)
    })

    it('应该支持OR逻辑检查', () => {
      const resource = {
        requiredPermissions: ['user:delete'], // 用户没有此权限
        requiredRoles: ['admin'], // 但有此角色
        logic: 'OR' as const
      }
      
      expect(roleManager.canAccess(resource)).toBe(true)
    })

    it('应该支持AND逻辑检查', () => {
      const resource = {
        requiredPermissions: ['user:read'], // 用户有此权限
        requiredRoles: ['guest'], // 但没有此角色
        logic: 'AND' as const
      }
      
      expect(roleManager.canAccess(resource)).toBe(false)
    })
  })

  describe('getPermissionsByModule', () => {
    it('应该按模块获取权限', () => {
      const userPermissions = roleManager.getPermissionsByModule('user')
      expect(userPermissions).toEqual(['user:create', 'user:read', 'user:update'])
      
      const archivePermissions = roleManager.getPermissionsByModule('archive')
      expect(archivePermissions).toEqual(['archive:read', 'archive:create'])
      
      const nonexistentPermissions = roleManager.getPermissionsByModule('nonexistent')
      expect(nonexistentPermissions).toEqual([])
    })
  })

  describe('utility functions', () => {
    it('hasPermission函数应该工作', () => {
      expect(hasPermission('user:create')).toBe(true)
      expect(hasPermission('user:delete')).toBe(false)
    })

    it('hasRole函数应该工作', () => {
      expect(hasRole('admin')).toBe(true)
      expect(hasRole('guest')).toBe(false)
    })

    it('hasAnyRole函数应该工作', () => {
      expect(hasAnyRole(['admin', 'guest'])).toBe(true)
      expect(hasAnyRole(['guest', 'visitor'])).toBe(false)
    })

    it('hasAllRoles函数应该工作', () => {
      expect(hasAllRoles(['admin', 'user'])).toBe(true)
      expect(hasAllRoles(['admin', 'guest'])).toBe(false)
    })
  })

  describe('edge cases', () => {
    it('应该处理用户未登录的情况', () => {
      mockUserStore.user = null as any
      
      expect(roleManager.hasPermission('user:create')).toBe(false)
      expect(roleManager.hasRole('admin')).toBe(false)
      expect(roleManager.isAdmin()).toBe(false)
    })

    it('应该处理权限为null或undefined的情况', () => {
      mockUserStore.user.permissions = null as any
      
      expect(roleManager.hasPermission('user:create')).toBe(false)
      
      mockUserStore.user.permissions = undefined as any
      
      expect(roleManager.hasPermission('user:create')).toBe(false)
    })

    it('应该处理角色为null或undefined的情况', () => {
      mockUserStore.user.roles = null as any
      
      expect(roleManager.hasRole('admin')).toBe(false)
      
      mockUserStore.user.roles = undefined as any
      
      expect(roleManager.hasRole('admin')).toBe(false)
    })

    it('应该处理权限字符串包含特殊字符的情况', () => {
      mockUserStore.user.permissions = ['user:create-new', 'archive:read_all', 'system:config.update']
      
      expect(roleManager.hasPermission('user:create-new')).toBe(true)
      expect(roleManager.hasPermission('archive:read_all')).toBe(true)
      expect(roleManager.hasPermission('system:config.update')).toBe(true)
    })
  })
})
