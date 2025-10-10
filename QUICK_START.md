# 档案管理系统 - 快速启动指南

> 🎉 **最新更新**: WebSocket实时通知系统 + 移动端响应式优化 (2025-10-10)

---

## 🚀 快速开始

### 1. 安装前端依赖(首次运行必须执行!)

```bash
cd frontend
npm install
```

**新增依赖**:
- `@stomp/stompjs: ^7.0.0` - WebSocket STOMP客户端
- `sockjs-client: ^1.6.1` - WebSocket降级支持

### 2. 启动服务

#### 方式一: 使用启动脚本 (推荐)

```bash
# 在项目根目录
./start-all.sh
```

#### 方式二: 分别启动

```bash
# 终端1: 启动后端
./start_backend.sh

# 终端2: 启动前端  
cd frontend
npm run dev
```

### 3. 访问系统

- 🌐 **前端地址**: http://localhost:3000
- 🔧 **后端API**: http://localhost:8080
- 📡 **WebSocket**: ws://localhost:8080/ws
- 📚 **API文档**: http://localhost:8080/doc.html

### 4. 测试账号

```
用户名: admin
密码: admin123
```

---

## ✨ 新功能体验

### 🔔 WebSocket实时通知

#### 体验步骤:
1. 登录系统
2. 观察右上角的通知铃铛图标
3. 打开浏览器开发者工具 → Network → WS
4. 查看WebSocket连接状态 (应显示已连接)

#### 触发测试通知:

**方法1: 使用curl命令**
```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "userId": 1,
    "title": "测试通知",
    "content": "这是一条实时推送的测试通知",
    "type": "1"
  }'
```

**方法2: 使用Postman/Apifox**
- URL: `POST http://localhost:8080/api/v1/notifications/send`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-token>`
- Body:
```json
{
  "userId": 1,
  "title": "测试通知",
  "content": "这是一条实时推送的测试通知",
  "type": "1"
}
```

#### 预期效果:
- ✅ 右上角铃铛徽章数量+1
- ✅ 浏览器弹出Element Plus通知
- ✅ 桌面弹出系统通知 (如已授权)
- ✅ 控制台显示 "收到WebSocket消息"

### 📱 移动端响应式体验

#### 测试步骤:
1. 打开Chrome开发者工具 (F12)
2. 点击 "Toggle device toolbar" (Ctrl+Shift+M / Cmd+Shift+M)
3. 选择不同的设备尺寸测试:
   - iPhone SE (375x667)
   - iPhone 12 Pro (390x844)
   - iPad Air (820x1180)
   - Galaxy S20 (360x800)

#### 测试页面:
- ✅ 登录页 - 自适应表单布局
- ✅ 仪表板 - 卡片网格响应式
- ✅ 档案列表 - 表格水平滚动
- ✅ 档案详情 - 信息卡片堆叠
- ✅ 通知中心 - 抽屉式展开

#### 预期效果:
- ✅ 布局自动适应屏幕尺寸
- ✅ 按钮和输入框触摸友好 (>=44x44px)
- ✅ 字体大小适中,易读
- ✅ 间距合理,不拥挤
- ✅ 表格可横向滚动

---

## 📋 功能清单

### ✅ 已完成功能

#### WebSocket实时通知
- [x] 实时消息推送 (<500ms延迟)
- [x] 桌面通知支持
- [x] 未读数量实时更新
- [x] 自动重连 (最多10次)
- [x] 心跳检测 (10秒)
- [x] 断线降级到轮询
- [x] 多频道支持 (个人/广播)
- [x] 消息分类 (系统/业务/警告/错误)

#### 移动端响应式
- [x] 响应式断点 (576/768/992/1200px)
- [x] 触摸友好的交互
- [x] 表格水平滚动
- [x] 表单垂直布局
- [x] 对话框自适应
- [x] 导航抽屉式
- [x] 横屏特殊处理
- [x] 高分屏优化
- [x] 暗色模式支持

### ⏭️ 待实施功能 (第三阶段)

#### 高级报表系统
- [ ] 报表配置器UI
- [ ] 动态数据源查询
- [ ] ECharts图表集成
- [ ] 多格式导出 (Excel/PDF/CSV)
- [ ] 报表订阅和推送
- [ ] 定时报表生成

---

## 🔧 故障排查

### 问题1: WebSocket连接失败

**症状**: 控制台显示 "WebSocket连接失败"

**解决方案**:
```bash
# 1. 检查后端是否运行
curl http://localhost:8080/actuator/health

# 2. 检查端口占用
lsof -i :8080

# 3. 重启服务
./stop-all.sh
./start-all.sh
```

### 问题2: 前端依赖安装失败

**症状**: `npm install` 报错

**解决方案**:
```bash
# 清除缓存
npm cache clean --force

# 删除旧依赖
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 问题3: 通知不推送

**症状**: WebSocket已连接但收不到通知

**排查步骤**:
1. 检查用户ID是否正确
2. 查看后端日志: `tail -f logs/archive-management-all.log`
3. 验证Token是否有效
4. 检查浏览器控制台错误

### 问题4: 移动端显示异常

**症状**: 移动端布局错乱

**解决方案**:
1. 清除浏览器缓存
2. 强制刷新 (Ctrl+Shift+R / Cmd+Shift+R)
3. 检查CSS是否正确加载
4. 使用Chrome DevTools检查元素

---

## 📚 详细文档

### 实施文档
- 📄 [WebSocket实时通知实施文档](./WEBSOCKET_IMPLEMENTATION.md)
- 📄 [项目完善总结报告](./PROJECT_IMPROVEMENT_SUMMARY.md)
- 📄 [项目完善计划与行动指南](./项目完善计划与行动指南.md)

### 开发文档
- 📖 [后端开发规范](./后端开发规范.md)
- 📖 [前端开发规范](./前端开发规范.md)
- 📖 [API接口规范](./API接口规范.md)
- 📖 [开发文档导航](./README-开发文档导航.md)

### 设计文档
- 🏗️ [系统架构设计](./系统架构设计.md)
- 🗄️ [数据库设计](./数据库设计.md)
- 🔄 [业务流程详细设计](./业务流程详细设计.md)

---

## 💡 使用提示

### WebSocket最佳实践
1. **首次使用**: 授权浏览器桌面通知权限
2. **网络切换**: 系统会自动重连,无需手动操作
3. **长时间未操作**: 建议刷新页面重新建立连接
4. **断线情况**: 系统会自动降级到30秒轮询

### 移动端使用建议
1. **建议使用Chrome/Safari**: 体验最佳
2. **横屏浏览**: 部分页面适合横屏操作
3. **触摸操作**: 所有按钮和输入框都已优化
4. **网络环境**: 建议使用WiFi以获得最佳体验

---

## 🎯 下一步计划

### 即将实施 (第三阶段)
1. ⏭️ 高级报表系统
   - 可视化报表配置器
   - 动态数据查询引擎
   - 多格式导出功能

2. ⏭️ 智能化功能
   - OCR文字识别
   - 智能分类推荐
   - 智能标签生成

3. ⏭️ 系统集成
   - SSO单点登录
   - 企业微信集成
   - 钉钉集成

### 性能优化计划
- [ ] 首屏加载时间优化
- [ ] 代码分割和懒加载
- [ ] 图片压缩和WebP支持
- [ ] PWA离线支持

---

## 📞 获取帮助

### 遇到问题?
1. 📖 查看相关文档
2. 🐛 检查故障排查章节
3. 📝 查看后端日志: `logs/archive-management-all.log`
4. 💬 联系开发团队

### 反馈建议
欢迎提供功能建议和bug反馈,帮助我们持续改进系统!

---

**最后更新**: 2025年10月10日  
**文档版本**: v1.0  
**系统版本**: v1.0.0

