import { mount, VueWrapper } from '@vue/test-utils'
import type { ComponentPublicInstance } from 'vue'
import ElementPlus from 'element-plus'

/**
 * 挂载组件的辅助函数
 * 自动注册Element Plus等全局插件
 */
export function mountWithPlugins(component: any, options: any = {}) {
  return mount(component, {
    global: {
      plugins: [ElementPlus],
      ...options.global
    },
    ...options
  })
}

/**
 * 等待异步更新
 */
export async function flushPromises() {
  return new Promise((resolve) => setTimeout(resolve, 0))
}

/**
 * 模拟用户输入
 */
export async function setInputValue(
  wrapper: VueWrapper<ComponentPublicInstance>,
  selector: string,
  value: string
) {
  const input = wrapper.find(selector)
  await input.setValue(value)
  await wrapper.vm.$nextTick()
}

