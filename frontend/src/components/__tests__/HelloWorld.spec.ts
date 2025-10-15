import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

// 创建一个简单的测试组件
const HelloWorld = {
  name: 'HelloWorld',
  props: {
    msg: {
      type: String,
      required: true
    }
  },
  template: '<div class="hello">{{ msg }}</div>'
}

describe('HelloWorld组件', () => {
  it('应该正确渲染props', () => {
    const msg = 'Hello Vitest'
    const wrapper = mount(HelloWorld, {
      props: { msg }
    })
    
    expect(wrapper.text()).toContain(msg)
    expect(wrapper.find('.hello').exists()).toBe(true)
  })

  it('应该响应props变化', async () => {
    const wrapper = mount(HelloWorld, {
      props: { msg: 'Initial' }
    })
    
    expect(wrapper.text()).toBe('Initial')
    
    await wrapper.setProps({ msg: 'Updated' })
    expect(wrapper.text()).toBe('Updated')
  })

  it('应该有正确的class', () => {
    const wrapper = mount(HelloWorld, {
      props: { msg: 'Test' }
    })
    
    expect(wrapper.classes()).toContain('hello')
  })
})

