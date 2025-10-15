import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusTag from '@/components/common/StatusTag.vue'

describe('StatusTag', () => {
  it('应该正确渲染组件', () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        type: 'success'
      }
    })
    
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.status-tag').exists()).toBe(true)
  })

  it('应该根据status显示对应文本', () => {
    const testCases = [
      { status: 1, expected: '启用' },
      { status: 0, expected: '禁用' },
      { status: 2, expected: '待审核' },
      { status: 3, expected: '已拒绝' }
    ]

    testCases.forEach(({ status, expected }) => {
      const wrapper = mount(StatusTag, {
        props: { status }
      })
      expect(wrapper.text()).toContain(expected)
    })
  })

  it('应该根据type显示对应样式', () => {
    const testCases = [
      { type: 'success', class: 'el-tag--success' },
      { type: 'danger', class: 'el-tag--danger' },
      { type: 'warning', class: 'el-tag--warning' },
      { type: 'info', class: 'el-tag--info' }
    ]

    testCases.forEach(({ type, class: expectedClass }) => {
      const wrapper = mount(StatusTag, {
        props: {
          status: 1,
          type
        },
        global: {
          stubs: {
            'el-tag': true
          }
        }
      })
      
      expect(wrapper.attributes('type')).toBe(type)
    })
  })

  it('应该支持自定义文本映射', () => {
    const customTextMap = {
      1: '正常',
      0: '异常'
    }

    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        textMap: customTextMap
      }
    })

    expect(wrapper.text()).toContain('正常')
  })

  it('应该支持自定义类型映射', () => {
    const customTypeMap = {
      1: 'primary',
      0: 'danger'
    }

    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        typeMap: customTypeMap
      },
      global: {
        stubs: {
          'el-tag': true
        }
      }
    })

    expect(wrapper.attributes('type')).toBe('primary')
  })

  it('应该支持小尺寸', () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        size: 'small'
      },
      global: {
        stubs: {
          'el-tag': true
        }
      }
    })

    expect(wrapper.attributes('size')).toBe('small')
  })

  it('应该支持圆角效果', () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        round: true
      },
      global: {
        stubs: {
          'el-tag': true
        }
      }
    })

    expect(wrapper.attributes('round')).toBe('true')
  })

  it('应该处理未知状态', () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 999 // 未知状态
      }
    })

    expect(wrapper.text()).toContain('未知')
  })

  it('应该支持点击事件', async () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        clickable: true
      }
    })

    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')![0]).toEqual([1])
  })

  it('应该支持禁用状态', () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        disabled: true
      },
      global: {
        stubs: {
          'el-tag': true
        }
      }
    })

    expect(wrapper.classes()).toContain('is-disabled')
  })

  it('应该支持显示图标', () => {
    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        showIcon: true
      }
    })

    expect(wrapper.find('.status-icon').exists()).toBe(true)
  })

  it('应该支持自定义图标映射', () => {
    const customIconMap = {
      1: 'check-circle',
      0: 'close-circle'
    }

    const wrapper = mount(StatusTag, {
      props: {
        status: 1,
        showIcon: true,
        iconMap: customIconMap
      }
    })

    expect(wrapper.find('[data-testid="status-icon-check-circle"]').exists()).toBe(true)
  })
})
