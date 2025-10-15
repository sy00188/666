# CI/CD 配置说明文档

## 📋 目录
- [概述](#概述)
- [工作流架构](#工作流架构)
- [配置说明](#配置说明)
- [Secrets 配置指南](#secrets-配置指南)
- [覆盖率要求](#覆盖率要求)
- [使用指南](#使用指南)
- [故障排查](#故障排查)
- [最佳实践](#最佳实践)

---

## 概述

本项目采用 GitHub Actions 作为 CI/CD 平台，实现了自动化测试、代码覆盖率检查和质量门禁。

### 核心特性
✅ **并行测试**：后端和前端测试并行执行，节省50%时间  
✅ **智能缓存**：Maven和npm依赖缓存，加速构建  
✅ **覆盖率报告**：自动生成并上传到Codecov  
✅ **质量门禁**：测试失败自动阻止合并  
✅ **多渠道通知**：GitHub内置通知 + Slack可选通知  
✅ **详细报告**：测试结果和覆盖率报告保存为Artifacts  

### 技术栈
- **CI平台**：GitHub Actions
- **后端测试**：Maven + JUnit 5 + JaCoCo
- **前端测试**：Vitest + V8 Coverage
- **覆盖率平台**：Codecov
- **通知**：Slack Incoming Webhooks（可选）

---

## 工作流架构

### 主工作流：`.github/workflows/ci.yml`

```
触发条件: push / pull_request (所有分支)
    ↓
┌───────────────────────────────────────────────────────┐
│                  并行执行 (3个Job)                     │
├───────────────────────────────────────────────────────┤
│                                                       │
│  Job 1: backend-tests                                │
│  ├─ 设置 Java 17                                     │
│  ├─ 缓存 Maven 依赖                                  │
│  ├─ 运行 Maven 测试                                  │
│  ├─ 生成 JaCoCo 报告                                 │
│  └─ 上传覆盖率 Artifact                              │
│                                                       │
│  Job 2: frontend-tests                               │
│  ├─ 设置 Node.js 18                                  │
│  ├─ 缓存 npm 依赖                                    │
│  ├─ 运行 Vitest 测试                                 │
│  ├─ 生成覆盖率报告                                   │
│  └─ 上传覆盖率 Artifact                              │
│                                                       │
│  Job 3: upload-coverage (依赖前两个Job)              │
│  ├─ 下载后端覆盖率                                   │
│  ├─ 下载前端覆盖率                                   │
│  ├─ 上传到 Codecov (后端)                            │
│  ├─ 上传到 Codecov (前端)                            │
│  └─ 生成覆盖率摘要                                   │
│                                                       │
└───────────────────────────────────────────────────────┘
    ↓
Job 4: test-summary (汇总结果)
    ├─ 检查所有Job状态
    ├─ 生成测试摘要
    └─ 失败时标记整体失败
```

### 通知工作流：`.github/workflows/notify-slack.yml` (可选)

```
触发条件: CI工作流完成
    ↓
判断结果
    ├─ 失败 → 发送Slack失败通知
    └─ 成功 (main分支) → 发送Slack成功通知
```

---

## 配置说明

### 1. 触发条件

```yaml
on:
  push:
    branches:
      - '**'  # 所有分支的push都触发
  pull_request:
    branches:
      - '**'  # 所有分支的PR都触发
```

**说明**：
- 每次代码推送都会触发测试
- 每个Pull Request都会自动运行测试
- 确保代码质量，及早发现问题

### 2. 并发控制

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

**说明**：
- 同一分支的多次push会取消之前未完成的工作流
- 节省资源，加快反馈速度

### 3. 缓存策略

#### Maven 缓存
```yaml
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: |
      ${{ runner.os }}-maven-
```

#### npm 缓存
```yaml
- uses: actions/setup-node@v4
  with:
    node-version: '18'
    cache: 'npm'
    cache-dependency-path: frontend/package-lock.json
```

**说明**：
- 缓存key基于依赖文件的哈希值
- 依赖文件未变化时直接使用缓存
- 大幅减少构建时间（首次约10分钟，缓存后约3分钟）

### 4. 超时设置

```yaml
timeout-minutes: 15
```

**说明**：
- 每个Job最多运行15分钟
- 防止工作流卡死消耗资源
- 可根据实际情况调整

---

## Secrets 配置指南

### 必需的 Secrets

#### 1. CODECOV_TOKEN

**用途**：上传覆盖率报告到 Codecov

**获取步骤**：
1. 访问 [https://codecov.io](https://codecov.io)
2. 使用 GitHub 账号登录
3. 点击 "Add new repository"
4. 选择你的仓库
5. 复制显示的 Upload Token
6. 在 GitHub 仓库中配置：
   - 进入 `Settings` > `Secrets and variables` > `Actions`
   - 点击 `New repository secret`
   - Name: `CODECOV_TOKEN`
   - Value: 粘贴你的 Token
   - 点击 `Add secret`

**验证**：
- 提交代码触发CI
- 检查 `upload-coverage` Job 是否成功
- 访问 Codecov 查看覆盖率报告

#### 2. SLACK_WEBHOOK_URL (可选)

**用途**：发送测试失败通知到 Slack

**获取步骤**：
1. 访问 [https://api.slack.com/apps](https://api.slack.com/apps)
2. 点击 "Create New App" > "From scratch"
3. 输入 App Name 和选择 Workspace
4. 在左侧菜单选择 "Incoming Webhooks"
5. 开启 "Activate Incoming Webhooks"
6. 点击 "Add New Webhook to Workspace"
7. 选择要发送通知的频道
8. 复制生成的 Webhook URL
9. 在 GitHub 仓库中配置：
   - 进入 `Settings` > `Secrets and variables` > `Actions`
   - 点击 `New repository secret`
   - Name: `SLACK_WEBHOOK_URL`
   - Value: 粘贴你的 Webhook URL
   - 点击 `Add secret`

**验证**：
- 故意让测试失败（如修改一个测试用例）
- 提交代码触发CI
- 检查 Slack 频道是否收到通知

**注意**：
- Slack 通知是可选的，不影响CI核心功能
- 如果不需要，可以删除 `.github/workflows/notify-slack.yml` 文件

---

## 覆盖率要求

### 当前阈值

| 模块 | 当前阈值 | 目标阈值 | 提升计划 |
|------|---------|---------|---------|
| **后端** | 80% | 80% | 已达标 |
| **前端** | 50% | 70% | 第4-5周提升 |
| **整体** | 70% | 75% | 第6周提升 |

### 覆盖率配置文件：`codecov.yml`

```yaml
coverage:
  status:
    project:
      default:
        target: 70%  # 整体目标
        threshold: 5%  # 允许下降5%
      
      backend:
        target: 80%  # 后端目标
        threshold: 3%
      
      frontend:
        target: 50%  # 前端初期目标
        threshold: 5%
```

### 覆盖率检查行为

1. **PR 检查**：
   - 覆盖率低于阈值 → PR 状态检查失败
   - 覆盖率下降超过阈值 → PR 状态检查失败
   - 阻止不符合质量标准的代码合并

2. **Codecov 评论**：
   - 自动在 PR 中评论覆盖率变化
   - 显示新增代码的覆盖率
   - 高亮未覆盖的行

3. **报告保存**：
   - 覆盖率报告保存为 GitHub Actions Artifacts
   - 保留7天，可随时下载查看
   - 支持离线分析

---

## 使用指南

### 开发流程

#### 1. 本地开发

```bash
# 后端测试
mvn clean test

# 前端测试
cd frontend
npm run test

# 查看覆盖率
npm run test:coverage
```

#### 2. 提交代码

```bash
git add .
git commit -m "feat: 添加新功能"
git push origin feature/your-branch
```

#### 3. 创建 Pull Request

1. 在 GitHub 上创建 PR
2. CI 自动触发测试
3. 等待测试完成（约3-5分钟）
4. 检查测试结果和覆盖率报告
5. 根据反馈修复问题
6. 测试通过后请求代码审查

#### 4. 查看测试结果

**GitHub Actions 页面**：
- 进入仓库的 `Actions` 标签
- 选择对应的工作流运行
- 查看每个 Job 的详细日志

**PR 页面**：
- 在 PR 的 `Checks` 标签查看状态
- 点击 `Details` 查看详细信息
- Codecov 会自动评论覆盖率变化

**Codecov 页面**：
- 访问 `https://codecov.io/gh/你的用户名/你的仓库`
- 查看详细的覆盖率报告
- 分析未覆盖的代码

#### 5. 下载测试报告

1. 进入工作流运行页面
2. 滚动到底部的 `Artifacts` 部分
3. 下载以下报告：
   - `backend-coverage`：后端覆盖率报告
   - `frontend-coverage`：前端覆盖率报告
   - `backend-test-results`：后端测试结果
   - `frontend-test-results`：前端测试结果

---

## 故障排查

### 常见问题

#### 1. Maven 测试失败

**症状**：`backend-tests` Job 失败

**可能原因**：
- 测试用例错误
- 依赖问题
- 数据库连接失败

**解决方案**：
```bash
# 本地运行测试
mvn clean test

# 查看详细日志
mvn clean test -X

# 跳过测试编译（仅验证）
mvn clean test -DskipTests
```

#### 2. Vitest 测试失败

**症状**：`frontend-tests` Job 失败

**可能原因**：
- 测试用例错误
- 依赖未安装
- 环境变量问题

**解决方案**：
```bash
cd frontend

# 清理并重新安装依赖
rm -rf node_modules package-lock.json
npm install

# 运行测试
npm run test

# 查看详细输出
npm run test -- --reporter=verbose
```

#### 3. Codecov 上传失败

**症状**：`upload-coverage` Job 失败或警告

**可能原因**：
- `CODECOV_TOKEN` 未配置或错误
- 覆盖率文件路径错误
- Codecov 服务暂时不可用

**解决方案**：
1. 检查 Secret 配置：
   - 进入 `Settings` > `Secrets and variables` > `Actions`
   - 确认 `CODECOV_TOKEN` 存在且正确

2. 检查覆盖率文件：
   ```bash
   # 后端
   ls -la target/site/jacoco/jacoco.xml
   
   # 前端
   ls -la frontend/coverage/lcov.info
   ```

3. 手动上传测试：
   ```bash
   # 安装 Codecov CLI
   pip install codecov
   
   # 上传覆盖率
   codecov -t YOUR_TOKEN
   ```

#### 4. 缓存问题

**症状**：构建时间异常长或依赖错误

**解决方案**：
1. 清除 GitHub Actions 缓存：
   - 进入 `Actions` > `Caches`
   - 删除相关缓存

2. 强制重新构建：
   - 修改 `pom.xml` 或 `package-lock.json`
   - 提交并推送

#### 5. Slack 通知未收到

**症状**：测试失败但未收到 Slack 通知

**可能原因**：
- `SLACK_WEBHOOK_URL` 未配置
- Webhook URL 过期或错误
- Slack 应用权限问题

**解决方案**：
1. 验证 Webhook URL：
   ```bash
   curl -X POST -H 'Content-type: application/json' \
     --data '{"text":"测试通知"}' \
     YOUR_WEBHOOK_URL
   ```

2. 重新生成 Webhook URL
3. 检查 Slack 应用权限

---

## 最佳实践

### 1. 提交前本地测试

```bash
# 运行所有测试
./run-tests.sh

# 或分别运行
mvn clean test
cd frontend && npm run test:coverage
```

**好处**：
- 及早发现问题
- 减少 CI 失败次数
- 节省时间和资源

### 2. 编写高质量测试

```java
// 后端示例：使用 @Test 和断言
@Test
@DisplayName("应该正确创建档案")
void shouldCreateArchiveSuccessfully() {
    // Given
    ArchiveDTO dto = new ArchiveDTO();
    dto.setTitle("测试档案");
    
    // When
    Archive result = archiveService.create(dto);
    
    // Then
    assertNotNull(result.getId());
    assertEquals("测试档案", result.getTitle());
}
```

```typescript
// 前端示例：使用 Vitest
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ArchiveList from './ArchiveList.vue'

describe('ArchiveList', () => {
  it('应该渲染档案列表', () => {
    const wrapper = mount(ArchiveList, {
      props: { archives: mockArchives }
    })
    
    expect(wrapper.findAll('.archive-item')).toHaveLength(3)
  })
})
```

### 3. 保持覆盖率稳定增长

- **新功能**：覆盖率不低于当前平均值
- **Bug修复**：添加回归测试
- **重构**：保持或提升覆盖率
- **定期审查**：每周检查覆盖率趋势

### 4. PR 检查清单

提交 PR 前确认：
- [ ] 所有测试通过
- [ ] 覆盖率符合要求
- [ ] 代码通过 Linter 检查
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] PR 描述清晰完整

### 5. 处理测试失败

1. **查看日志**：详细阅读失败信息
2. **本地重现**：在本地环境复现问题
3. **修复问题**：修改代码或测试
4. **验证修复**：本地测试通过后再提交
5. **更新 PR**：推送修复，触发重新测试

### 6. 优化 CI 性能

- **并行测试**：已启用，无需额外配置
- **缓存依赖**：已配置，自动生效
- **选择性运行**：仅运行受影响的测试（高级）
- **矩阵策略**：多版本测试（按需启用）

### 7. 监控和维护

- **定期检查**：每周查看 CI 运行统计
- **优化慢测试**：识别并优化耗时测试
- **更新依赖**：定期更新 Actions 版本
- **清理缓存**：定期清理过期缓存

---

## 附录

### 相关文档

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Codecov 文档](https://docs.codecov.com/)
- [JaCoCo 文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [Vitest 文档](https://vitest.dev/)
- [Slack API 文档](https://api.slack.com/)

### 配置文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| 主CI工作流 | `.github/workflows/ci.yml` | 核心测试流程 |
| Slack通知 | `.github/workflows/notify-slack.yml` | 可选通知 |
| Codecov配置 | `codecov.yml` | 覆盖率设置 |
| Maven配置 | `pom.xml` | 后端测试配置 |
| Vitest配置 | `frontend/vitest.config.ts` | 前端测试配置 |

### 联系方式

如有问题，请：
1. 查看本文档的故障排查部分
2. 搜索 GitHub Issues
3. 联系项目维护者

---

**文档版本**：v1.0  
**最后更新**：2025-10-15  
**维护者**：档案管理系统开发团队

