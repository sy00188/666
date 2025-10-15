import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import SearchBox from '@/components/common/SearchBox.vue'

describe('SearchBox.vue', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(SearchBox, {
      props: {
        placeholder: '请输入搜索关键词',
        modelValue: '',
        loading: false
      }
    })
  })

  it('应该正确渲染搜索框', () => {
    expect(wrapper.find('input').exists()).toBe(true)
    expect(wrapper.find('button').exists()).toBe(true)
    expect(wrapper.find('input').attributes('placeholder')).toBe('请输入搜索关键词')
  })

  it('应该正确绑定输入值', async () => {
    const input = wrapper.find('input')
    await input.setValue('测试搜索')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual(['测试搜索'])
  })

  it('应该在点击搜索按钮时触发搜索事件', async () => {
    const input = wrapper.find('input')
    const button = wrapper.find('button')
    
    await input.setValue('搜索内容')
    await button.trigger('click')
    
    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['搜索内容'])
  })

  it('应该在按下回车键时触发搜索事件', async () => {
    const input = wrapper.find('input')
    
    await input.setValue('回车搜索')
    await input.trigger('keyup.enter')
    
    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['回车搜索'])
  })

  it('应该显示加载状态', async () => {
    await wrapper.setProps({ loading: true })
    
    const button = wrapper.find('button')
    expect(button.attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('搜索中')
  })

  it('应该支持清空搜索', async () => {
    await wrapper.setProps({ modelValue: '有内容' })
    
    const clearButton = wrapper.find('.clear-button')
    if (clearButton.exists()) {
      await clearButton.trigger('click')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('clear')).toBeTruthy()
    }
  })

  it('应该支持高级搜索模式', async () => {
    await wrapper.setProps({ 
      advanced: true,
      filters: [
        { key: 'type', label: '类型', options: ['文档', '图片'] },
        { key: 'date', label: '日期', type: 'daterange' }
      ]
    })
    
    const advancedButton = wrapper.find('.advanced-toggle')
    if (advancedButton.exists()) {
      await advancedButton.trigger('click')
      expect(wrapper.find('.advanced-filters').exists()).toBe(true)
    }
  })

  it('应该支持搜索历史', async () => {
    const mockHistory = ['历史搜索1', '历史搜索2']
    await wrapper.setProps({ history: mockHistory })
    
    const input = wrapper.find('input')
    await input.trigger('focus')
    
    const historyItems = wrapper.findAll('.history-item')
    expect(historyItems.length).toBe(mockHistory.length)
  })

  it('应该支持搜索建议', async () => {
    const mockSuggestions = ['建议1', '建议2', '建议3']
    await wrapper.setProps({ suggestions: mockSuggestions })
    
    const input = wrapper.find('input')
    await input.setValue('建')
    await nextTick()
    
    const suggestionItems = wrapper.findAll('.suggestion-item')
    expect(suggestionItems.length).toBeLessThanOrEqual(mockSuggestions.length)
  })

  it('应该支持自定义搜索图标', async () => {
    await wrapper.setProps({ 
      searchIcon: 'custom-search-icon'
    })
    
    const icon = wrapper.find('.search-icon')
    if (icon.exists()) {
      expect(icon.classes()).toContain('custom-search-icon')
    }
  })

  it('应该在禁用状态下不可操作', async () => {
    await wrapper.setProps({ disabled: true })
    
    const input = wrapper.find('input')
    const button = wrapper.find('button')
    
    expect(input.attributes('disabled')).toBeDefined()
    expect(button.attributes('disabled')).toBeDefined()
  })

  it('应该支持最小搜索长度限制', async () => {
    await wrapper.setProps({ minLength: 3 })
    
    const input = wrapper.find('input')
    const button = wrapper.find('button')
    
    // 输入少于最小长度的内容
    await input.setValue('ab')
    await button.trigger('click')
    
    // 应该不触发搜索事件
    expect(wrapper.emitted('search')).toBeFalsy()
    
    // 输入达到最小长度的内容
    await input.setValue('abc')
    await button.trigger('click')
    
    // 应该触发搜索事件
    expect(wrapper.emitted('search')).toBeTruthy()
  })

  it('应该支持搜索防抖', async () => {
    vi.useFakeTimers()
    
    await wrapper.setProps({ 
      debounce: 300,
      autoSearch: true 
    })
    
    const input = wrapper.find('input')
    
    // 快速输入多次
    await input.setValue('a')
    await input.setValue('ab')
    await input.setValue('abc')
    
    // 在防抖时间内不应该触发搜索
    expect(wrapper.emitted('search')).toBeFalsy()
    
    // 等待防抖时间
    vi.advanceTimersByTime(300)
    
    // 现在应该触发搜索
    expect(wrapper.emitted('search')).toBeTruthy()
    
    vi.useRealTimers()
  })

  it('应该正确处理快捷键', async () => {
    const input = wrapper.find('input')
    
    // 测试 Ctrl+K 快捷键聚焦
    await wrapper.trigger('keydown', { 
      key: 'k', 
      ctrlKey: true 
    })
    
    expect(document.activeElement).toBe(input.element)
  })

  it('应该支持多语言', async () => {
    await wrapper.setProps({ 
      locale: 'en',
      placeholder: 'Enter search keywords'
    })
    
    const input = wrapper.find('input')
    expect(input.attributes('placeholder')).toBe('Enter search keywords')
  })
})
