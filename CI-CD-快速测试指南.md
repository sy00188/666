# CI/CD 快速测试指南

## 🚀 快速开始

本指南帮助您快速验证CI/CD配置是否正确。

---

## ✅ 步骤1：验证配置文件

运行验证脚本：

```bash
./verify-ci-setup.sh
```

**预期结果**：所有检查项显示 ✓（绿色对勾）

---

## ✅ 步骤2：本地测试验证

### 后端测试

```bash
# 运行所有后端测试
mvn clean test

# 生成覆盖率报告
mvn jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

**预期结果**：
- 所有测试通过
- 覆盖率报告生成在 `target/site/jacoco/`
- 覆盖率达到80%以上

### 前端测试

```bash
# 进入前端目录
cd frontend

# 运行测试并生成覆盖率
npm run test:coverage

# 查看覆盖率报告
open coverage/index.html
```

**预期结果**：
- 所有测试通过
- 覆盖率报告生成在 `frontend/coverage/`
- 覆盖率符合预期（初期目标50%）

---

## ✅ 步骤3：提交配置到Git

```bash
# 查看新增文件
git status

# 添加CI/CD配置文件
git add .github/ codecov.yml README-CI-CD.md verify-ci-setup.sh CI-CD-快速测试指南.md

# 提交
git commit -m "feat: 添加CI/CD集成测试配置

- 配置GitHub Actions工作流
- 集成后端Maven测试和JaCoCo覆盖率
- 集成前端Vitest测试和V8覆盖率
- 配置Codecov覆盖率上传
- 添加Slack通知工作流（可选）
- 添加详细的配置文档和验证脚本"

# 推送到远程仓库
git push origin main
```

---

## ✅ 步骤4：配置Codecov Token

### 4.1 注册Codecov账户

1. 访问 [https://codecov.io](https://codecov.io)
2. 点击 "Sign up with GitHub"
3. 授权Codecov访问你的GitHub仓库

### 4.2 添加仓库

1. 登录后点击 "Add new repository"
2. 找到你的仓库 `archive-management-system`
3. 点击 "Setup repo"
4. 复制显示的 **Upload Token**

### 4.3 配置GitHub Secret

1. 进入GitHub仓库页面
2. 点击 `Settings` > `Secrets and variables` > `Actions`
3. 点击 `New repository secret`
4. 填写：
   - **Name**: `CODECOV_TOKEN`
   - **Value**: 粘贴刚才复制的Token
5. 点击 `Add secret`

---

## ✅ 步骤5：触发首次CI运行

### 方法1：推送代码（已完成）

步骤3的push操作会自动触发CI

### 方法2：手动触发

1. 进入GitHub仓库
2. 点击 `Actions` 标签
3. 选择 "CI/CD Pipeline"
4. 点击 "Run workflow"
5. 选择分支（如main）
6. 点击 "Run workflow"

---

## ✅ 步骤6：查看CI运行结果

### 6.1 查看GitHub Actions

1. 进入 `Actions` 标签
2. 点击最新的工作流运行
3. 查看各个Job的状态：
   - ✅ backend-tests：后端测试
   - ✅ frontend-tests：前端测试
   - ✅ upload-coverage：覆盖率上传
   - ✅ test-summary：测试汇总

### 6.2 查看详细日志

点击任意Job查看详细执行日志：
- 测试输出
- 覆盖率统计
- 错误信息（如果有）

### 6.3 下载测试报告

滚动到页面底部的 `Artifacts` 部分，下载：
- `backend-coverage`：后端覆盖率HTML报告
- `frontend-coverage`：前端覆盖率HTML报告
- `backend-test-results`：后端测试结果
- `frontend-test-results`：前端测试结果

---

## ✅ 步骤7：查看Codecov报告

1. 访问 [https://codecov.io/gh/你的用户名/archive-management-system](https://codecov.io)
2. 查看覆盖率仪表板：
   - 整体覆盖率
   - 后端覆盖率（backend flag）
   - 前端覆盖率（frontend flag）
3. 点击文件查看详细覆盖情况
4. 查看覆盖率趋势图

---

## ✅ 步骤8：测试PR工作流

### 8.1 创建测试分支

```bash
# 创建并切换到测试分支
git checkout -b test/ci-verification

# 做一个小改动（如添加注释）
echo "// CI测试" >> src/main/java/com/archive/management/ArchiveManagementApplication.java

# 提交改动
git add .
git commit -m "test: 验证CI/CD流程"

# 推送分支
git push origin test/ci-verification
```

### 8.2 创建Pull Request

1. 进入GitHub仓库
2. 点击 "Pull requests" > "New pull request"
3. 选择 `test/ci-verification` → `main`
4. 点击 "Create pull request"
5. 填写PR描述
6. 点击 "Create pull request"

### 8.3 观察CI检查

在PR页面：
1. 查看 `Checks` 标签
2. 等待CI运行完成
3. 查看Codecov评论（覆盖率变化）
4. 确认所有检查通过

### 8.4 清理测试分支

```bash
# 切回main分支
git checkout main

# 删除本地测试分支
git branch -D test/ci-verification

# 删除远程测试分支
git push origin --delete test/ci-verification
```

---

## ✅ 步骤9：配置Slack通知（可选）

### 9.1 创建Slack应用

1. 访问 [https://api.slack.com/apps](https://api.slack.com/apps)
2. 点击 "Create New App" > "From scratch"
3. 输入App Name（如"CI/CD Bot"）
4. 选择Workspace
5. 点击 "Create App"

### 9.2 启用Incoming Webhooks

1. 在左侧菜单选择 "Incoming Webhooks"
2. 开启 "Activate Incoming Webhooks"
3. 点击 "Add New Webhook to Workspace"
4. 选择要发送通知的频道
5. 点击 "Allow"
6. 复制生成的 Webhook URL

### 9.3 配置GitHub Secret

1. 进入GitHub仓库 `Settings` > `Secrets and variables` > `Actions`
2. 点击 `New repository secret`
3. 填写：
   - **Name**: `SLACK_WEBHOOK_URL`
   - **Value**: 粘贴Webhook URL
4. 点击 `Add secret`

### 9.4 测试Slack通知

1. 故意让测试失败（如修改一个测试用例）
2. 提交并推送代码
3. 等待CI运行失败
4. 检查Slack频道是否收到通知

---

## 📊 验证清单

完成以下所有项目即表示CI/CD配置成功：

- [ ] 验证脚本运行成功，所有检查通过
- [ ] 本地后端测试通过，覆盖率≥80%
- [ ] 本地前端测试通过，覆盖率≥50%
- [ ] 配置文件已提交到Git
- [ ] Codecov Token已配置
- [ ] 首次CI运行成功
- [ ] 后端测试Job通过
- [ ] 前端测试Job通过
- [ ] 覆盖率上传成功
- [ ] Codecov报告可访问
- [ ] PR工作流测试通过
- [ ] Codecov在PR中评论覆盖率
- [ ] Slack通知配置成功（可选）

---

## 🐛 常见问题

### Q1: CI运行失败，提示"CODECOV_TOKEN not found"

**解决方案**：
- 确认已在GitHub Secrets中配置 `CODECOV_TOKEN`
- 检查Token是否正确（无多余空格）
- 重新运行工作流

### Q2: 后端测试失败

**解决方案**：
```bash
# 本地运行测试查看详细错误
mvn clean test -X

# 检查测试依赖
mvn dependency:tree

# 清理并重新构建
mvn clean install
```

### Q3: 前端测试失败

**解决方案**：
```bash
cd frontend

# 清理并重新安装依赖
rm -rf node_modules package-lock.json
npm install

# 运行测试查看详细错误
npm run test -- --reporter=verbose
```

### Q4: 覆盖率未上传到Codecov

**解决方案**：
- 检查覆盖率文件是否生成
  - 后端：`target/site/jacoco/jacoco.xml`
  - 前端：`frontend/coverage/lcov.info`
- 检查Codecov Token配置
- 查看 `upload-coverage` Job的详细日志

### Q5: Slack通知未收到

**解决方案**：
- 确认 `SLACK_WEBHOOK_URL` 已配置
- 测试Webhook是否有效：
  ```bash
  curl -X POST -H 'Content-type: application/json' \
    --data '{"text":"测试通知"}' \
    YOUR_WEBHOOK_URL
  ```
- 检查Slack应用权限

---

## 📚 参考文档

- [README-CI-CD.md](./README-CI-CD.md) - 完整配置文档
- [GitHub Actions文档](https://docs.github.com/en/actions)
- [Codecov文档](https://docs.codecov.com/)
- [Slack API文档](https://api.slack.com/)

---

## 🎉 完成！

恭喜！您已成功配置并验证了CI/CD集成测试流程。

**下一步**：
1. 继续开发新功能
2. 每次提交都会自动运行测试
3. 定期查看覆盖率报告
4. 逐步提升测试覆盖率

**记住**：
- 提交前先本地测试
- 保持覆盖率稳定增长
- 及时修复失败的测试
- 定期审查CI性能

---

**文档版本**：v1.0  
**创建日期**：2025-10-15  
**维护者**：档案管理系统开发团队

