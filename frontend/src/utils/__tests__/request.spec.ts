import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import type { AxiosResponse, AxiosError } from 'axios'
import { request, setupInterceptors } from '@/utils/request'

// Mock axios
vi.mock('axios')
const mockedAxios = vi.mocked(axios)

// Mock router
const mockRouter = {
  push: vi.fn()
}

vi.mock('vue-router', () => ({
  useRouter: () => mockRouter
}))

// Mock message
const mockMessage = {
  error: vi.fn(),
  success: vi.fn(),
  warning: vi.fn()
}

vi.mock('element-plus', () => ({
  ElMessage: mockMessage
}))

// Mock token store
const mockTokenStore = {
  token: 'mock-token',
  refreshToken: 'mock-refresh-token',
  setToken: vi.fn(),
  clearToken: vi.fn(),
  isTokenExpired: vi.fn().mockReturnValue(false)
}

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => mockTokenStore
}))

describe('request utils', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedAxios.create.mockReturnValue(mockedAxios as any)
    mockedAxios.interceptors = {
      request: { use: vi.fn() },
      response: { use: vi.fn() }
    } as any
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('request instance', () => {
    it('应该创建axios实例', () => {
      expect(mockedAxios.create).toHaveBeenCalledWith({
        baseURL: expect.any(String),
        timeout: expect.any(Number),
        headers: expect.objectContaining({
          'Content-Type': 'application/json'
        })
      })
    })

    it('应该设置请求拦截器', () => {
      setupInterceptors()
      expect(mockedAxios.interceptors.request.use).toHaveBeenCalled()
    })

    it('应该设置响应拦截器', () => {
      setupInterceptors()
      expect(mockedAxios.interceptors.response.use).toHaveBeenCalled()
    })
  })

  describe('request interceptors', () => {
    it('应该在请求头中添加token', () => {
      const config = {
        headers: {},
        url: '/api/test'
      }

      // 模拟请求拦截器
      const requestInterceptor = vi.fn((config) => {
        if (mockTokenStore.token) {
          config.headers.Authorization = `Bearer ${mockTokenStore.token}`
        }
        return config
      })

      const result = requestInterceptor(config)

      expect(result.headers.Authorization).toBe(`Bearer ${mockTokenStore.token}`)
    })

    it('应该添加请求ID用于追踪', () => {
      const config = {
        headers: {},
        url: '/api/test'
      }

      const requestInterceptor = vi.fn((config) => {
        config.headers['X-Request-ID'] = `req-${Date.now()}-${Math.random()}`
        return config
      })

      const result = requestInterceptor(config)

      expect(result.headers['X-Request-ID']).toMatch(/^req-\d+-/)
    })

    it('应该处理请求错误', () => {
      const error = new Error('Request failed')
      const requestErrorHandler = vi.fn((error) => {
        console.error('Request error:', error)
        return Promise.reject(error)
      })

      expect(() => requestErrorHandler(error)).rejects.toThrow('Request failed')
    })
  })

  describe('response interceptors', () => {
    it('应该处理成功响应', () => {
      const response: AxiosResponse = {
        data: {
          code: 200,
          data: { id: 1, name: 'test' },
          message: 'success'
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any
      }

      const responseInterceptor = vi.fn((response) => {
        if (response.data.code === 200) {
          return response.data.data
        }
        return response
      })

      const result = responseInterceptor(response)

      expect(result).toEqual({ id: 1, name: 'test' })
    })

    it('应该处理业务错误响应', () => {
      const response: AxiosResponse = {
        data: {
          code: 400,
          message: '参数错误'
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any
      }

      const responseInterceptor = vi.fn((response) => {
        if (response.data.code !== 200) {
          mockMessage.error(response.data.message)
          return Promise.reject(new Error(response.data.message))
        }
        return response
      })

      expect(() => responseInterceptor(response)).rejects.toThrow('参数错误')
      expect(mockMessage.error).toHaveBeenCalledWith('参数错误')
    })

    it('应该处理401未授权错误', () => {
      const error: AxiosError = {
        response: {
          status: 401,
          data: { message: '未授权' }
        } as any,
        config: {} as any,
        isAxiosError: true,
        name: 'AxiosError',
        message: 'Request failed with status code 401',
        toJSON: () => ({})
      }

      const responseErrorHandler = vi.fn((error) => {
        if (error.response?.status === 401) {
          mockTokenStore.clearToken()
          mockRouter.push('/login')
          mockMessage.error('登录已过期，请重新登录')
        }
        return Promise.reject(error)
      })

      expect(() => responseErrorHandler(error)).rejects.toThrow()
      expect(mockTokenStore.clearToken).toHaveBeenCalled()
      expect(mockRouter.push).toHaveBeenCalledWith('/login')
      expect(mockMessage.error).toHaveBeenCalledWith('登录已过期，请重新登录')
    })

    it('应该处理403权限不足错误', () => {
      const error: AxiosError = {
        response: {
          status: 403,
          data: { message: '权限不足' }
        } as any,
        config: {} as any,
        isAxiosError: true,
        name: 'AxiosError',
        message: 'Request failed with status code 403',
        toJSON: () => ({})
      }

      const responseErrorHandler = vi.fn((error) => {
        if (error.response?.status === 403) {
          mockMessage.error('权限不足，无法访问该资源')
        }
        return Promise.reject(error)
      })

      expect(() => responseErrorHandler(error)).rejects.toThrow()
      expect(mockMessage.error).toHaveBeenCalledWith('权限不足，无法访问该资源')
    })

    it('应该处理500服务器错误', () => {
      const error: AxiosError = {
        response: {
          status: 500,
          data: { message: '服务器内部错误' }
        } as any,
        config: {} as any,
        isAxiosError: true,
        name: 'AxiosError',
        message: 'Request failed with status code 500',
        toJSON: () => ({})
      }

      const responseErrorHandler = vi.fn((error) => {
        if (error.response?.status === 500) {
          mockMessage.error('服务器内部错误，请稍后重试')
        }
        return Promise.reject(error)
      })

      expect(() => responseErrorHandler(error)).rejects.toThrow()
      expect(mockMessage.error).toHaveBeenCalledWith('服务器内部错误，请稍后重试')
    })

    it('应该处理网络错误', () => {
      const error: AxiosError = {
        code: 'NETWORK_ERROR',
        message: 'Network Error',
        config: {} as any,
        isAxiosError: true,
        name: 'AxiosError',
        toJSON: () => ({})
      }

      const responseErrorHandler = vi.fn((error) => {
        if (error.code === 'NETWORK_ERROR') {
          mockMessage.error('网络连接失败，请检查网络设置')
        }
        return Promise.reject(error)
      })

      expect(() => responseErrorHandler(error)).rejects.toThrow()
      expect(mockMessage.error).toHaveBeenCalledWith('网络连接失败，请检查网络设置')
    })

    it('应该处理超时错误', () => {
      const error: AxiosError = {
        code: 'TIMEOUT',
        message: 'timeout of 10000ms exceeded',
        config: {} as any,
        isAxiosError: true,
        name: 'AxiosError',
        toJSON: () => ({})
      }

      const responseErrorHandler = vi.fn((error) => {
        if (error.code === 'TIMEOUT') {
          mockMessage.error('请求超时，请稍后重试')
        }
        return Promise.reject(error)
      })

      expect(() => responseErrorHandler(error)).rejects.toThrow()
      expect(mockMessage.error).toHaveBeenCalledWith('请求超时，请稍后重试')
    })
  })

  describe('token refresh', () => {
    it('应该在token过期时自动刷新', async () => {
      mockTokenStore.isTokenExpired.mockReturnValue(true)
      
      const refreshTokenFn = vi.fn().mockResolvedValue({
        data: {
          token: 'new-token',
          refreshToken: 'new-refresh-token'
        }
      })

      // 模拟token刷新逻辑
      if (mockTokenStore.isTokenExpired()) {
        const result = await refreshTokenFn()
        mockTokenStore.setToken(result.data.token)
      }

      expect(refreshTokenFn).toHaveBeenCalled()
      expect(mockTokenStore.setToken).toHaveBeenCalledWith('new-token')
    })

    it('应该在刷新token失败时跳转登录', async () => {
      const refreshTokenFn = vi.fn().mockRejectedValue(new Error('Refresh failed'))

      try {
        await refreshTokenFn()
      } catch (error) {
        mockTokenStore.clearToken()
        mockRouter.push('/login')
      }

      expect(mockTokenStore.clearToken).toHaveBeenCalled()
      expect(mockRouter.push).toHaveBeenCalledWith('/login')
    })
  })

  describe('request methods', () => {
    it('应该支持GET请求', async () => {
      const mockData = { id: 1, name: 'test' }
      mockedAxios.get.mockResolvedValue({ data: mockData })

      const result = await request.get('/api/test')

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/test', undefined)
      expect(result).toEqual({ data: mockData })
    })

    it('应该支持POST请求', async () => {
      const postData = { name: 'test' }
      const mockResponse = { id: 1, ...postData }
      mockedAxios.post.mockResolvedValue({ data: mockResponse })

      const result = await request.post('/api/test', postData)

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/test', postData, undefined)
      expect(result).toEqual({ data: mockResponse })
    })

    it('应该支持PUT请求', async () => {
      const putData = { id: 1, name: 'updated' }
      mockedAxios.put.mockResolvedValue({ data: putData })

      const result = await request.put('/api/test/1', putData)

      expect(mockedAxios.put).toHaveBeenCalledWith('/api/test/1', putData, undefined)
      expect(result).toEqual({ data: putData })
    })

    it('应该支持DELETE请求', async () => {
      mockedAxios.delete.mockResolvedValue({ data: { success: true } })

      const result = await request.delete('/api/test/1')

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/test/1', undefined)
      expect(result).toEqual({ data: { success: true } })
    })
  })
})
