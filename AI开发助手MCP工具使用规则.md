# AI 开发助手 MCP 工具使用规则

## 目的
确保在开发任务中充分利用可用的 MCP (Model Context Protocol) 工具，提高工作效率和代码质量。

## 核心原则

### 1. MCP 优先策略
- **默认行为**：在开始任何中等复杂度以上的任务前，首先评估是否可以使用 MCP 工具
- **强制使用场景**：对于明确适合 MCP 工具的场景，必须使用相应工具，不得跳过
- **记录使用**：每次使用 MCP 工具时，简要说明使用原因和预期效果

### 2. 可用 MCP 工具清单

#### A. Sequential Thinking MCP (`mcp_sequential-thinking_sequentialthinking`)
**用途**：复杂问题的多步骤推理和思考

**必须使用的场景**：
- 需要设计复杂算法或数据结构
- 架构决策需要权衡多个方案
- 调试复杂的逻辑问题
- 需要分解复杂任务为多个步骤
- 问题解决需要回溯或修正思路
- 需要生成假设并验证

**使用示例**：
```
场景：设计一个分布式缓存系统
触发：涉及多个技术选型、性能权衡、容错设计
行动：使用 Sequential Thinking 进行：
1. 列出需求和约束
2. 提出3-5个候选方案
3. 逐个分析优缺点
4. 验证最优方案的可行性
5. 识别潜在风险
```

#### B. Context7 MCP (`mcp_context7_resolve-library-id` 和 `mcp_context7_get-library-docs`)
**用途**：获取库和框架的最新文档

**必须使用的场景**：
- 需要了解第三方库的 API 用法
- 需要查找框架的最佳实践
- 遇到库相关的编译错误或运行时错误
- 需要了解库的版本差异
- 技术选型时需要对比不同库

**使用流程**：
1. 使用 `resolve-library-id` 查找库的标准ID
2. 使用 `get-library-docs` 获取相关文档
3. 结合文档编写代码或解决问题

**使用示例**：
```
场景：使用 Spring Boot 实现 Redis 缓存
触发：不确定 Spring Data Redis 的具体配置方法
行动：
1. resolve-library-id("spring-data-redis")
2. get-library-docs("/org/springframework/data-redis", topic="configuration")
3. 根据文档编写配置代码
```

#### C. Playwright MCP (多个 `mcp_Playwright_*` 工具)
**用途**：浏览器自动化、前端测试、UI调试

**必须使用的场景**：
- 需要测试前端页面功能
- 需要截图展示UI效果
- 需要验证表单提交流程
- 需要模拟用户交互
- 需要获取页面渲染后的HTML

**常用工具**：
- `playwright_navigate` - 打开网页
- `playwright_screenshot` - 截图
- `playwright_click` - 点击元素
- `playwright_fill` - 填写表单
- `playwright_get_visible_html` - 获取页面HTML

**使用示例**：
```
场景：验证登录功能是否正常
触发：修改了登录相关代码
行动：
1. playwright_navigate(url="http://localhost:3000/login")
2. playwright_fill(selector="#username", value="testuser")
3. playwright_fill(selector="#password", value="testpass")
4. playwright_click(selector="#loginBtn")
5. playwright_screenshot(name="login-success")
```

## 使用决策流程图

```
开始任务
    ↓
是否涉及复杂思考/多步推理？
    ├─ 是 → 使用 Sequential Thinking MCP
    └─ 否 ↓
是否需要查询库/框架文档？
    ├─ 是 → 使用 Context7 MCP
    └─ 否 ↓
是否涉及前端UI测试/验证？
    ├─ 是 → 使用 Playwright MCP
    └─ 否 → 使用传统工具
```

## MCP 与传统工具的协作策略

### 1. Sequential Thinking + 代码编辑
**场景**：重构复杂代码
```
步骤1：用 Sequential Thinking 分析重构方案
  - 识别代码异味
  - 设计重构步骤
  - 评估风险
步骤2：用 search_replace/write 执行重构
步骤3：用 Sequential Thinking 验证重构效果
```

### 2. Context7 + 代码实现
**场景**：集成新库
```
步骤1：用 Context7 获取库文档
步骤2：用 codebase_search 查找项目中类似用法
步骤3：用 write 编写集成代码
步骤4：用 Context7 验证最佳实践
```

### 3. Playwright + 问题诊断
**场景**：前端bug调试
```
步骤1：用 Playwright 重现问题
步骤2：用 playwright_console_logs 查看错误
步骤3：用 codebase_search 定位相关代码
步骤4：修复代码
步骤5：用 Playwright 验证修复
```

## 强制使用检查清单

在以下情况下，**必须**使用 MCP 工具：

- [ ] 任务描述包含"设计"、"架构"、"算法" → Sequential Thinking
- [ ] 任务涉及不熟悉的库或框架 → Context7
- [ ] 任务要求"测试"、"验证"前端功能 → Playwright
- [ ] 需要权衡多个技术方案 → Sequential Thinking
- [ ] 遇到库相关的编译错误 → Context7
- [ ] 需要展示UI效果 → Playwright

## 最佳实践

### 1. 主动声明 MCP 使用
```
✅ 好的做法：
"我将使用 Sequential Thinking MCP 来分析这个架构决策，因为涉及多个方案的权衡..."

❌ 不好的做法：
直接开始编码，没有考虑 MCP 工具
```

### 2. 组合使用 MCP 工具
```
✅ 好的做法：
1. Sequential Thinking 规划实现方案
2. Context7 查询库文档
3. 编写代码
4. Playwright 验证效果

❌ 不好的做法：
只使用单一工具，错过其他工具的优势
```

### 3. 记录 MCP 使用效果
```
使用后简要说明：
"通过 Sequential Thinking，识别出方案A的性能瓶颈，改用方案B"
"Context7 文档显示新版本API已废弃，改用推荐的新方法"
"Playwright 截图确认UI布局正确"
```

## 性能优化建议

1. **Sequential Thinking**：
   - 设置合理的 total_thoughts 估计值
   - 遇到复杂问题可以分多次使用
   - 每个 thought 保持专注，一次解决一个问题

2. **Context7**：
   - 先 resolve-library-id 确认库ID
   - 使用 topic 参数缩小文档范围
   - 控制 tokens 参数避免过载

3. **Playwright**：
   - 使用 headless 模式提高速度
   - 合理使用 selector 选择器
   - 截图时指定合适的尺寸

## 自我检查问题

在开始每个任务前，问自己：

1. ✓ 这个任务是否足够复杂，需要 Sequential Thinking？
2. ✓ 我是否需要查询某个库的文档（Context7）？
3. ✓ 是否需要验证前端效果（Playwright）？
4. ✓ 能否通过组合使用多个 MCP 工具提高效率？
5. ✓ 使用 MCP 工具是否比传统方法更高效？

## 总结

**记住**：MCP 工具是为了提高效率和质量而设计的。在适合的场景下主动使用它们，但也要避免过度使用。关键是在正确的时机选择正确的工具。

---

## 附录：快速参考卡

### Sequential Thinking 使用模板
```
触发条件：复杂思考、多步推理、方案权衡
调用方式：mcp_sequential-thinking_sequentialthinking
参数：
  - thought: 当前思考步骤
  - thoughtNumber: 当前步骤编号
  - totalThoughts: 预计总步骤数
  - nextThoughtNeeded: 是否需要下一步
```

### Context7 使用模板
```
触发条件：需要库文档、API用法、最佳实践
调用方式：
  1. mcp_context7_resolve-library-id
     参数：libraryName (如 "spring-boot")
  2. mcp_context7_get-library-docs
     参数：
       - context7CompatibleLibraryID (如 "/spring/boot")
       - topic (可选，如 "configuration")
       - tokens (可选，默认5000)
```

### Playwright 使用模板
```
触发条件：前端测试、UI验证、页面截图
常用流程：
  1. mcp_Playwright_playwright_navigate
     参数：url, browserType (可选), headless (可选)
  2. mcp_Playwright_playwright_click / fill / screenshot
  3. mcp_Playwright_playwright_close (完成后关闭)
```

## 版本历史

- **v1.0** (2024-01-10): 初始版本，包含三大 MCP 工具的使用指南

