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
        // 覆盖率阈值（第一阶段目标40%，后续逐步提升至70%）
        thresholds: {
          lines: 40,
          functions: 40,
          branches: 40,
          statements: 40
        }
      },
      
      // 测试超时时间
      testTimeout: 10000,
      
      // 钩子超时时间
      hookTimeout: 10000
    }
  })
)

