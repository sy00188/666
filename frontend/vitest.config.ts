import { defineConfig, mergeConfig } from 'vite'
import { configDefaults } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      // 测试环境
      environment: 'happy-dom',
      
      // 全局API
      globals: true,
      
      // 包含的测试文件
      include: ['src/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
      
      // 排除的文件
      exclude: [...configDefaults.exclude, 'e2e/*'],
      
      // 覆盖率配置
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html', 'lcov'],
        reportsDirectory: './coverage',
        exclude: [
          ...configDefaults.exclude,
          'src/main.ts',
          'src/**/*.d.ts',
          'src/**/*.config.*',
          'src/router/index.ts',
          'playwright.config.ts',
          'vite.config.ts',
          'vitest.config.ts'
        ],
        // 覆盖率阈值（初期设置为0，待第4-5周补充测试后逐步提升至70%）
        thresholds: {
          lines: 0,
          functions: 0,
          branches: 0,
          statements: 0
        }
      },
      
      // 测试超时时间
      testTimeout: 10000,
      
      // 钩子超时时间
      hookTimeout: 10000
    }
  })
)

