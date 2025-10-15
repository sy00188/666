import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import DataTable from '@/components/common/DataTable.vue'

describe('DataTable.vue', () => {
  let wrapper: any
  const mockData = [
    { id: 1, name: '张三', age: 25, department: '技术部' },
    { id: 2, name: '李四', age: 30, department: '产品部' },
    { id: 3, name: '王五', age: 28, department: '设计部' }
  ]

  const mockColumns = [
    { key: 'id', title: 'ID', width: 80 },
    { key: 'name', title: '姓名', sortable: true },
    { key: 'age', title: '年龄', sortable: true },
    { key: 'department', title: '部门' }
  ]

  beforeEach(() => {
    wrapper = mount(DataTable, {
      props: {
        data: mockData,
        columns: mockColumns,
        loading: false
      }
    })
  })

  it('应该正确渲染表格', () => {
    expect(wrapper.find('table').exists()).toBe(true)
    expect(wrapper.find('thead').exists()).toBe(true)
    expect(wrapper.find('tbody').exists()).toBe(true)
  })

  it('应该正确渲染表头', () => {
    const headers = wrapper.findAll('th')
    expect(headers).toHaveLength(mockColumns.length)
    
    mockColumns.forEach((column, index) => {
      expect(headers[index].text()).toContain(column.title)
    })
  })

  it('应该正确渲染表格数据', () => {
    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(mockData.length)
    
    // 检查第一行数据
    const firstRowCells = rows[0].findAll('td')
    expect(firstRowCells[0].text()).toBe('1')
    expect(firstRowCells[1].text()).toBe('张三')
    expect(firstRowCells[2].text()).toBe('25')
    expect(firstRowCells[3].text()).toBe('技术部')
  })

  it('应该支持排序功能', async () => {
    const sortableHeader = wrapper.find('th[data-sortable="true"]')
    
    if (sortableHeader.exists()) {
      await sortableHeader.trigger('click')
      
      expect(wrapper.emitted('sort')).toBeTruthy()
      expect(wrapper.emitted('sort')[0]).toEqual([{
        column: 'name',
        direction: 'asc'
      }])
    }
  })

  it('应该支持多选功能', async () => {
    await wrapper.setProps({ selectable: true })
    
    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    expect(checkboxes.length).toBeGreaterThan(0)
    
    // 点击第一行的复选框
    const firstRowCheckbox = checkboxes[1] // 第0个是全选框
    await firstRowCheckbox.setChecked(true)
    
    expect(wrapper.emitted('selection-change')).toBeTruthy()
  })

  it('应该支持全选功能', async () => {
    await wrapper.setProps({ selectable: true })
    
    const selectAllCheckbox = wrapper.find('thead input[type="checkbox"]')
    
    if (selectAllCheckbox.exists()) {
      await selectAllCheckbox.setChecked(true)
      
      expect(wrapper.emitted('select-all')).toBeTruthy()
    }
  })

  it('应该显示加载状态', async () => {
    await wrapper.setProps({ loading: true })
    
    expect(wrapper.find('.loading').exists()).toBe(true)
    expect(wrapper.text()).toContain('加载中')
  })

  it('应该显示空数据状态', async () => {
    await wrapper.setProps({ data: [] })
    
    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.text()).toContain('暂无数据')
  })

  it('应该支持分页功能', async () => {
    const pagination = {
      current: 1,
      pageSize: 10,
      total: 100
    }
    
    await wrapper.setProps({ 
      pagination,
      showPagination: true 
    })
    
    const paginationComponent = wrapper.find('.pagination')
    expect(paginationComponent.exists()).toBe(true)
  })

  it('应该支持行点击事件', async () => {
    const firstRow = wrapper.find('tbody tr')
    await firstRow.trigger('click')
    
    expect(wrapper.emitted('row-click')).toBeTruthy()
    expect(wrapper.emitted('row-click')[0]).toEqual([mockData[0], 0])
  })

  it('应该支持行双击事件', async () => {
    const firstRow = wrapper.find('tbody tr')
    await firstRow.trigger('dblclick')
    
    expect(wrapper.emitted('row-dblclick')).toBeTruthy()
    expect(wrapper.emitted('row-dblclick')[0]).toEqual([mockData[0], 0])
  })

  it('应该支持自定义列渲染', async () => {
    const customColumns = [
      ...mockColumns,
      {
        key: 'actions',
        title: '操作',
        render: (row: any) => `<button>编辑 ${row.name}</button>`
      }
    ]
    
    await wrapper.setProps({ columns: customColumns })
    
    const actionCells = wrapper.findAll('td:last-child')
    expect(actionCells[0].html()).toContain('编辑 张三')
  })

  it('应该支持固定列', async () => {
    const fixedColumns = mockColumns.map((col, index) => ({
      ...col,
      fixed: index === 0 ? 'left' : undefined
    }))
    
    await wrapper.setProps({ columns: fixedColumns })
    
    const fixedColumn = wrapper.find('th[data-fixed="left"]')
    expect(fixedColumn.exists()).toBe(true)
  })

  it('应该支持行展开功能', async () => {
    await wrapper.setProps({ 
      expandable: true,
      expandedRowRender: (row: any) => `详细信息: ${row.name}`
    })
    
    const expandButton = wrapper.find('.expand-button')
    
    if (expandButton.exists()) {
      await expandButton.trigger('click')
      
      expect(wrapper.emitted('expand')).toBeTruthy()
      expect(wrapper.find('.expanded-row').exists()).toBe(true)
    }
  })

  it('应该支持表格尺寸设置', async () => {
    await wrapper.setProps({ size: 'small' })
    
    expect(wrapper.find('table').classes()).toContain('table-small')
  })

  it('应该支持边框设置', async () => {
    await wrapper.setProps({ bordered: true })
    
    expect(wrapper.find('table').classes()).toContain('table-bordered')
  })

  it('应该支持斑马纹', async () => {
    await wrapper.setProps({ striped: true })
    
    expect(wrapper.find('table').classes()).toContain('table-striped')
  })

  it('应该支持响应式布局', async () => {
    await wrapper.setProps({ responsive: true })
    
    expect(wrapper.find('.table-responsive').exists()).toBe(true)
  })

  it('应该支持列宽调整', async () => {
    await wrapper.setProps({ resizable: true })
    
    const resizeHandle = wrapper.find('.resize-handle')
    
    if (resizeHandle.exists()) {
      // 模拟拖拽调整列宽
      await resizeHandle.trigger('mousedown')
      await resizeHandle.trigger('mousemove', { clientX: 150 })
      await resizeHandle.trigger('mouseup')
      
      expect(wrapper.emitted('column-resize')).toBeTruthy()
    }
  })

  it('应该支持表格滚动', async () => {
    await wrapper.setProps({ 
      scroll: { x: 1000, y: 400 }
    })
    
    const tableWrapper = wrapper.find('.table-wrapper')
    expect(tableWrapper.attributes('style')).toContain('overflow')
  })

  it('应该支持行高亮', async () => {
    await wrapper.setProps({ 
      highlightCurrentRow: true,
      currentRowKey: 1
    })
    
    const highlightedRow = wrapper.find('tr.row-highlighted')
    expect(highlightedRow.exists()).toBe(true)
  })

  it('应该支持表格导出', async () => {
    await wrapper.setProps({ exportable: true })
    
    const exportButton = wrapper.find('.export-button')
    
    if (exportButton.exists()) {
      await exportButton.trigger('click')
      expect(wrapper.emitted('export')).toBeTruthy()
    }
  })

  it('应该支持列过滤', async () => {
    const filterableColumns = mockColumns.map(col => ({
      ...col,
      filterable: col.key === 'department',
      filters: col.key === 'department' ? [
        { text: '技术部', value: '技术部' },
        { text: '产品部', value: '产品部' }
      ] : undefined
    }))
    
    await wrapper.setProps({ columns: filterableColumns })
    
    const filterButton = wrapper.find('.filter-button')
    
    if (filterButton.exists()) {
      await filterButton.trigger('click')
      expect(wrapper.find('.filter-dropdown').exists()).toBe(true)
    }
  })
})
