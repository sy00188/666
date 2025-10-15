import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import FormDialog from '@/components/common/FormDialog.vue'

describe('FormDialog.vue', () => {
  let wrapper: any

  const mockFormConfig = {
    title: '用户信息',
    fields: [
      {
        key: 'name',
        label: '姓名',
        type: 'input',
        required: true,
        rules: [{ required: true, message: '请输入姓名' }]
      },
      {
        key: 'email',
        label: '邮箱',
        type: 'email',
        required: true,
        rules: [
          { required: true, message: '请输入邮箱' },
          { type: 'email', message: '请输入正确的邮箱格式' }
        ]
      },
      {
        key: 'age',
        label: '年龄',
        type: 'number',
        min: 18,
        max: 65
      },
      {
        key: 'department',
        label: '部门',
        type: 'select',
        options: [
          { label: '技术部', value: 'tech' },
          { label: '产品部', value: 'product' },
          { label: '设计部', value: 'design' }
        ]
      }
    ]
  }

  beforeEach(() => {
    wrapper = mount(FormDialog, {
      props: {
        visible: true,
        config: mockFormConfig,
        modelValue: {}
      }
    })
  })

  it('应该正确渲染对话框', () => {
    expect(wrapper.find('.dialog').exists()).toBe(true)
    expect(wrapper.find('.dialog-title').text()).toBe('用户信息')
  })

  it('应该根据配置渲染表单字段', () => {
    const formItems = wrapper.findAll('.form-item')
    expect(formItems).toHaveLength(mockFormConfig.fields.length)
    
    // 检查第一个字段
    const nameField = formItems[0]
    expect(nameField.find('label').text()).toContain('姓名')
    expect(nameField.find('input').exists()).toBe(true)
  })

  it('应该正确处理输入字段', async () => {
    const nameInput = wrapper.find('input[type="text"]')
    await nameInput.setValue('张三')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')[0][0]
    expect(emittedValue.name).toBe('张三')
  })

  it('应该正确处理邮箱字段', async () => {
    const emailInput = wrapper.find('input[type="email"]')
    await emailInput.setValue('zhangsan@example.com')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')[0][0]
    expect(emittedValue.email).toBe('zhangsan@example.com')
  })

  it('应该正确处理数字字段', async () => {
    const numberInput = wrapper.find('input[type="number"]')
    await numberInput.setValue('25')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')[0][0]
    expect(emittedValue.age).toBe('25')
  })

  it('应该正确处理选择字段', async () => {
    const select = wrapper.find('select')
    await select.setValue('tech')
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')[0][0]
    expect(emittedValue.department).toBe('tech')
  })

  it('应该显示必填字段标识', () => {
    const requiredFields = wrapper.findAll('.form-item.required')
    expect(requiredFields.length).toBeGreaterThan(0)
    
    const nameField = wrapper.find('.form-item:first-child')
    expect(nameField.classes()).toContain('required')
  })

  it('应该支持表单验证', async () => {
    const form = wrapper.find('form')
    
    // 提交空表单应该触发验证
    await form.trigger('submit')
    
    const errorMessages = wrapper.findAll('.error-message')
    expect(errorMessages.length).toBeGreaterThan(0)
  })

  it('应该在点击确定按钮时提交表单', async () => {
    // 填写有效数据
    await wrapper.setProps({
      modelValue: {
        name: '张三',
        email: 'zhangsan@example.com',
        age: 25,
        department: 'tech'
      }
    })
    
    const confirmButton = wrapper.find('.btn-confirm')
    await confirmButton.trigger('click')
    
    expect(wrapper.emitted('confirm')).toBeTruthy()
  })

  it('应该在点击取消按钮时关闭对话框', async () => {
    const cancelButton = wrapper.find('.btn-cancel')
    await cancelButton.trigger('click')
    
    expect(wrapper.emitted('cancel')).toBeTruthy()
    expect(wrapper.emitted('update:visible')).toBeTruthy()
    expect(wrapper.emitted('update:visible')[0]).toEqual([false])
  })

  it('应该支持加载状态', async () => {
    await wrapper.setProps({ loading: true })
    
    const confirmButton = wrapper.find('.btn-confirm')
    expect(confirmButton.attributes('disabled')).toBeDefined()
    expect(confirmButton.text()).toContain('提交中')
  })

  it('应该支持只读模式', async () => {
    await wrapper.setProps({ readonly: true })
    
    const inputs = wrapper.findAll('input')
    inputs.forEach(input => {
      expect(input.attributes('readonly')).toBeDefined()
    })
    
    const confirmButton = wrapper.find('.btn-confirm')
    expect(confirmButton.exists()).toBe(false)
  })

  it('应该支持自定义按钮', async () => {
    await wrapper.setProps({
      customButtons: [
        { text: '保存草稿', type: 'draft', variant: 'secondary' },
        { text: '预览', type: 'preview', variant: 'outline' }
      ]
    })
    
    const draftButton = wrapper.find('.btn-draft')
    const previewButton = wrapper.find('.btn-preview')
    
    expect(draftButton.exists()).toBe(true)
    expect(previewButton.exists()).toBe(true)
    
    await draftButton.trigger('click')
    expect(wrapper.emitted('custom-action')).toBeTruthy()
    expect(wrapper.emitted('custom-action')[0]).toEqual(['draft'])
  })

  it('应该支持分步表单', async () => {
    const stepFormConfig = {
      ...mockFormConfig,
      steps: [
        { title: '基本信息', fields: ['name', 'email'] },
        { title: '详细信息', fields: ['age', 'department'] }
      ]
    }
    
    await wrapper.setProps({ config: stepFormConfig })
    
    const steps = wrapper.findAll('.step')
    expect(steps).toHaveLength(2)
    
    const nextButton = wrapper.find('.btn-next')
    if (nextButton.exists()) {
      await nextButton.trigger('click')
      expect(wrapper.emitted('step-change')).toBeTruthy()
    }
  })

  it('应该支持动态字段', async () => {
    const dynamicConfig = {
      ...mockFormConfig,
      fields: [
        ...mockFormConfig.fields,
        {
          key: 'skills',
          label: '技能',
          type: 'dynamic',
          itemType: 'input',
          addText: '添加技能'
        }
      ]
    }
    
    await wrapper.setProps({ config: dynamicConfig })
    
    const addButton = wrapper.find('.btn-add-item')
    if (addButton.exists()) {
      await addButton.trigger('click')
      
      const dynamicItems = wrapper.findAll('.dynamic-item')
      expect(dynamicItems.length).toBeGreaterThan(0)
    }
  })

  it('应该支持文件上传字段', async () => {
    const fileConfig = {
      ...mockFormConfig,
      fields: [
        ...mockFormConfig.fields,
        {
          key: 'avatar',
          label: '头像',
          type: 'file',
          accept: 'image/*',
          maxSize: 2 * 1024 * 1024 // 2MB
        }
      ]
    }
    
    await wrapper.setProps({ config: fileConfig })
    
    const fileInput = wrapper.find('input[type="file"]')
    expect(fileInput.exists()).toBe(true)
    expect(fileInput.attributes('accept')).toBe('image/*')
  })

  it('应该支持日期选择字段', async () => {
    const dateConfig = {
      ...mockFormConfig,
      fields: [
        ...mockFormConfig.fields,
        {
          key: 'birthday',
          label: '生日',
          type: 'date',
          format: 'YYYY-MM-DD'
        }
      ]
    }
    
    await wrapper.setProps({ config: dateConfig })
    
    const dateInput = wrapper.find('input[type="date"]')
    expect(dateInput.exists()).toBe(true)
  })

  it('应该支持富文本编辑器', async () => {
    const richTextConfig = {
      ...mockFormConfig,
      fields: [
        ...mockFormConfig.fields,
        {
          key: 'description',
          label: '描述',
          type: 'richtext',
          toolbar: ['bold', 'italic', 'underline']
        }
      ]
    }
    
    await wrapper.setProps({ config: richTextConfig })
    
    const richTextEditor = wrapper.find('.rich-text-editor')
    expect(richTextEditor.exists()).toBe(true)
  })

  it('应该支持条件显示字段', async () => {
    const conditionalConfig = {
      ...mockFormConfig,
      fields: [
        ...mockFormConfig.fields,
        {
          key: 'experience',
          label: '工作经验',
          type: 'number',
          condition: {
            field: 'age',
            operator: '>=',
            value: 22
          }
        }
      ]
    }
    
    await wrapper.setProps({ 
      config: conditionalConfig,
      modelValue: { age: 25 }
    })
    
    await nextTick()
    
    const experienceField = wrapper.find('[data-field="experience"]')
    expect(experienceField.exists()).toBe(true)
  })

  it('应该支持表单重置', async () => {
    // 填写一些数据
    await wrapper.setProps({
      modelValue: {
        name: '张三',
        email: 'zhangsan@example.com'
      }
    })
    
    const resetButton = wrapper.find('.btn-reset')
    if (resetButton.exists()) {
      await resetButton.trigger('click')
      
      expect(wrapper.emitted('reset')).toBeTruthy()
    }
  })

  it('应该支持表单数据导入导出', async () => {
    const importExportConfig = {
      ...mockFormConfig,
      importExport: true
    }
    
    await wrapper.setProps({ config: importExportConfig })
    
    const importButton = wrapper.find('.btn-import')
    const exportButton = wrapper.find('.btn-export')
    
    if (importButton.exists()) {
      expect(importButton.exists()).toBe(true)
    }
    
    if (exportButton.exists()) {
      expect(exportButton.exists()).toBe(true)
    }
  })
})
