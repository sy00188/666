# 档案管理系统 - API接口规范文档

## 1. 文档概述

### 1.1 文档目的
本文档详细描述档案管理系统的API接口规范，包括接口设计原则、请求响应格式、认证授权机制、错误处理等，为前后端开发提供统一的接口标准。

### 1.2 接口设计原则
- **RESTful风格**：遵循REST架构风格，使用标准HTTP方法
- **统一响应格式**：所有接口采用统一的响应数据结构
- **版本控制**：支持API版本管理，向后兼容
- **安全性**：完善的认证授权和数据验证机制
- **可扩展性**：支持业务扩展和功能迭代

### 1.3 技术规范
- **协议**：HTTPS
- **数据格式**：JSON
- **字符编码**：UTF-8
- **API版本**：v1.0
- **基础URL**：`https://api.archive.company.com/v1`

## 2. 通用规范

### 2.1 HTTP状态码
| 状态码 | 含义 | 说明 |
|--------|------|------|
| 200 | OK | 请求成功 |
| 201 | Created | 创建成功 |
| 204 | No Content | 删除成功 |
| 400 | Bad Request | 请求参数错误 |
| 401 | Unauthorized | 未认证 |
| 403 | Forbidden | 无权限 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突 |
| 422 | Unprocessable Entity | 数据验证失败 |
| 500 | Internal Server Error | 服务器内部错误 |

### 2.2 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2025-01-20T10:30:00Z",
  "requestId": "req_123456789"
}
```

#### 2.2.1 成功响应示例
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "档案名称"
  },
  "timestamp": "2025-01-20T10:30:00Z",
  "requestId": "req_123456789"
}
```

#### 2.2.2 分页响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "pages": 5
    }
  },
  "timestamp": "2025-01-20T10:30:00Z",
  "requestId": "req_123456789"
}
```

#### 2.2.3 错误响应示例
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "errors": [
    {
      "field": "username",
      "message": "用户名不能为空"
    }
  ],
  "timestamp": "2025-01-20T10:30:00Z",
  "requestId": "req_123456789"
}
```

### 2.3 请求头规范
```http
Content-Type: application/json
Authorization: Bearer {access_token}
X-Request-ID: req_123456789
X-Client-Version: 1.0.0
Accept-Language: zh-CN
```

### 2.4 分页参数
```json
{
  "page": 1,
  "size": 20,
  "sort": "createTime",
  "order": "desc"
}
```

## 3. 认证授权

### 3.1 JWT Token认证
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3.2 Token刷新机制
```json
POST /auth/refresh
{
  "refreshToken": "refresh_token_string"
}
```

### 3.3 权限验证
- **角色权限**：基于用户角色进行权限控制
- **数据权限**：基于数据范围进行访问控制
- **功能权限**：基于具体功能进行操作控制

## 4. 用户管理接口

### 4.1 用户认证

#### 4.1.1 用户登录
```http
POST /auth/login
```

**请求参数：**
```json
{
  "username": "admin",
  "password": "123456",
  "captcha": "ABCD",
  "captchaId": "captcha_123"
}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_string",
    "expiresIn": 7200,
    "user": {
      "userId": 1,
      "username": "admin",
      "realName": "管理员",
      "email": "admin@example.com",
      "avatar": "https://example.com/avatar.jpg",
      "roles": ["SYSTEM_ADMIN"],
      "permissions": ["user:create", "user:update"]
    }
  }
}
```

#### 4.1.2 用户登出
```http
POST /auth/logout
```

#### 4.1.3 获取当前用户信息
```http
GET /auth/profile
```

### 4.2 用户管理

#### 4.2.1 获取用户列表
```http
GET /users?page=1&size=20&keyword=admin&departmentId=1&status=1
```

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "userId": 1,
        "username": "admin",
        "realName": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "departmentName": "技术部",
        "status": 1,
        "createTime": "2025-01-20T10:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "pages": 5
    }
  }
}
```

#### 4.2.2 创建用户
```http
POST /users
```

**请求参数：**
```json
{
  "username": "newuser",
  "password": "123456",
  "realName": "新用户",
  "email": "newuser@example.com",
  "phone": "13800138001",
  "departmentId": 2,
  "employeeNo": "EMP001",
  "position": "开发工程师",
  "roleIds": [2, 3]
}
```

#### 4.2.3 更新用户
```http
PUT /users/{userId}
```

#### 4.2.4 删除用户
```http
DELETE /users/{userId}
```

#### 4.2.5 重置密码
```http
POST /users/{userId}/reset-password
```

## 5. 档案管理接口

### 5.1 档案分类

#### 5.1.1 获取分类树
```http
GET /categories/tree
```

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "人事档案",
      "categoryCode": "HR",
      "parentId": 0,
      "children": [
        {
          "categoryId": 2,
          "categoryName": "员工档案",
          "categoryCode": "HR_EMPLOYEE",
          "parentId": 1,
          "children": []
        }
      ]
    }
  ]
}
```

#### 5.1.2 创建分类
```http
POST /categories
```

**请求参数：**
```json
{
  "categoryName": "合同档案",
  "categoryCode": "CONTRACT",
  "parentId": 0,
  "defaultSecurityLevel": 2,
  "retentionPeriod": "长期",
  "businessType": "合同管理"
}
```

### 5.2 档案管理

#### 5.2.1 获取档案列表
```http
GET /archives?page=1&size=20&categoryId=1&status=2&keyword=合同&startTime=2025-01-01&endTime=2025-01-31
```

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "archiveId": 1,
        "archiveNo": "DA-2025-000001",
        "title": "员工劳动合同",
        "categoryName": "人事档案",
        "securityLevel": 2,
        "securityLevelName": "内部",
        "fileCount": 3,
        "totalSize": 1024000,
        "status": 2,
        "statusName": "已归档",
        "submitUserName": "张三",
        "submitTime": "2025-01-20T10:30:00Z",
        "archiveTime": "2025-01-20T14:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "pages": 5
    }
  }
}
```

#### 5.2.2 获取档案详情
```http
GET /archives/{archiveId}
```

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "archiveId": 1,
    "archiveNo": "DA-2025-000001",
    "title": "员工劳动合同",
    "categoryId": 1,
    "categoryName": "人事档案",
    "securityLevel": 2,
    "businessId": "CONTRACT_001",
    "keywords": "劳动合同,员工,人事",
    "abstract": "员工劳动合同档案",
    "metadata": {
      "contractType": "劳动合同",
      "contractPeriod": "3年",
      "signDate": "2025-01-01"
    },
    "files": [
      {
        "fileId": 1,
        "fileName": "contract.pdf",
        "originalName": "劳动合同.pdf",
        "fileSize": 1024000,
        "fileType": "pdf",
        "uploadTime": "2025-01-20T10:30:00Z"
      }
    ],
    "retentionPeriod": "长期",
    "expiryDate": null,
    "status": 2,
    "submitUserName": "张三",
    "submitTime": "2025-01-20T10:30:00Z",
    "archiveUserName": "李四",
    "archiveTime": "2025-01-20T14:30:00Z"
  }
}
```

#### 5.2.3 创建档案
```http
POST /archives
```

**请求参数：**
```json
{
  "title": "员工劳动合同",
  "categoryId": 1,
  "securityLevel": 2,
  "businessId": "CONTRACT_001",
  "keywords": "劳动合同,员工,人事",
  "abstract": "员工劳动合同档案",
  "metadata": {
    "contractType": "劳动合同",
    "contractPeriod": "3年",
    "signDate": "2025-01-01"
  },
  "retentionPeriod": "长期",
  "fileIds": [1, 2, 3]
}
```

#### 5.2.4 更新档案
```http
PUT /archives/{archiveId}
```

#### 5.2.5 删除档案
```http
DELETE /archives/{archiveId}
```

#### 5.2.6 档案审核
```http
POST /archives/{archiveId}/review
```

**请求参数：**
```json
{
  "action": "approve",
  "remark": "审核通过"
}
```

### 5.3 文件管理

#### 5.3.1 文件上传
```http
POST /files/upload
Content-Type: multipart/form-data
```

**请求参数：**
```
file: [文件二进制数据]
archiveId: 1
```

**响应数据：**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileId": 1,
    "fileName": "file_123456.pdf",
    "originalName": "合同.pdf",
    "filePath": "/uploads/2025/01/20/file_123456.pdf",
    "fileSize": 1024000,
    "fileType": "pdf",
    "fileHash": "sha256_hash_value"
  }
}
```

#### 5.3.2 文件下载
```http
GET /files/{fileId}/download
```

#### 5.3.3 文件预览
```http
GET /files/{fileId}/preview
```

#### 5.3.4 删除文件
```http
DELETE /files/{fileId}
```

## 6. 借阅管理接口

### 6.1 借阅申请

#### 6.1.1 获取借阅列表
```http
GET /borrows?page=1&size=20&status=1&applyUserId=1&startTime=2025-01-01&endTime=2025-01-31
```

#### 6.1.2 创建借阅申请
```http
POST /borrows
```

**请求参数：**
```json
{
  "archiveId": 1,
  "purpose": "业务需要查阅相关合同",
  "expectedDays": 7
}
```

#### 6.1.3 借阅审批
```http
POST /borrows/{borrowId}/approve
```

**请求参数：**
```json
{
  "action": "approve",
  "actualDays": 7,
  "remark": "同意借阅"
}
```

#### 6.1.4 借阅归还
```http
POST /borrows/{borrowId}/return
```

## 7. 系统管理接口

### 7.1 部门管理

#### 7.1.1 获取部门树
```http
GET /departments/tree
```

#### 7.1.2 创建部门
```http
POST /departments
```

### 7.2 角色管理

#### 7.2.1 获取角色列表
```http
GET /roles
```

#### 7.2.2 创建角色
```http
POST /roles
```

### 7.3 权限管理

#### 7.3.1 获取权限树
```http
GET /permissions/tree
```

### 7.4 系统配置

#### 7.4.1 获取配置列表
```http
GET /configs
```

#### 7.4.2 更新配置
```http
PUT /configs/{configKey}
```

## 8. 统计报表接口

### 8.1 档案统计

#### 8.1.1 档案数量统计
```http
GET /statistics/archives/count?startTime=2025-01-01&endTime=2025-01-31&groupBy=category
```

**响应数据：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 1000,
    "groups": [
      {
        "name": "人事档案",
        "count": 300
      },
      {
        "name": "财务档案",
        "count": 200
      }
    ]
  }
}
```

#### 8.1.2 借阅统计
```http
GET /statistics/borrows/trend?startTime=2025-01-01&endTime=2025-01-31
```

### 8.2 用户活跃度统计
```http
GET /statistics/users/activity?startTime=2025-01-01&endTime=2025-01-31
```

## 9. 日志审计接口

### 9.1 操作日志

#### 9.1.1 获取操作日志
```http
GET /logs/operations?page=1&size=20&userId=1&operationType=CREATE&startTime=2025-01-01&endTime=2025-01-31
```

### 9.2 登录日志

#### 9.2.1 获取登录日志
```http
GET /logs/logins?page=1&size=20&username=admin&status=1&startTime=2025-01-01&endTime=2025-01-31
```

## 10. 错误码定义

### 10.1 通用错误码
| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 200 | 操作成功 | 请求处理成功 |
| 400 | 请求参数错误 | 请求参数格式或内容错误 |
| 401 | 未认证 | 用户未登录或token无效 |
| 403 | 无权限 | 用户无权限访问该资源 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 409 | 资源冲突 | 资源已存在或状态冲突 |
| 422 | 数据验证失败 | 请求数据验证不通过 |
| 500 | 服务器内部错误 | 服务器处理异常 |

### 10.2 业务错误码
| 错误码 | 错误信息 | 说明 |
|--------|----------|------|
| 1001 | 用户名或密码错误 | 登录认证失败 |
| 1002 | 用户已被禁用 | 用户状态异常 |
| 1003 | 验证码错误 | 图形验证码验证失败 |
| 2001 | 档案编号已存在 | 档案编号重复 |
| 2002 | 档案状态不允许操作 | 档案当前状态不支持该操作 |
| 2003 | 档案密级权限不足 | 用户无权限访问该密级档案 |
| 3001 | 借阅申请已存在 | 该档案已有未完成的借阅申请 |
| 3002 | 档案已被借出 | 档案当前被其他用户借阅中 |
| 3003 | 借阅已逾期 | 借阅时间已超期 |
| 4001 | 文件上传失败 | 文件上传过程中发生错误 |
| 4002 | 文件格式不支持 | 上传的文件格式不被系统支持 |
| 4003 | 文件大小超限 | 上传文件大小超过系统限制 |

## 11. 接口版本管理

### 11.1 版本策略
- **URL版本控制**：`/v1/users`, `/v2/users`
- **向后兼容**：新版本保持对旧版本的兼容
- **废弃通知**：提前通知接口废弃计划

### 11.2 版本变更记录
| 版本 | 发布日期 | 变更内容 |
|------|----------|----------|
| v1.0 | 2025-01-20 | 初始版本发布 |

## 12. 接口测试

### 12.1 测试环境
- **开发环境**：`https://dev-api.archive.company.com/v1`
- **测试环境**：`https://test-api.archive.company.com/v1`
- **生产环境**：`https://api.archive.company.com/v1`

### 12.2 Postman集合
提供完整的Postman接口测试集合，包含所有接口的示例请求。

### 12.3 Mock数据
开发阶段提供Mock数据服务，支持前端独立开发。

## 13. 性能要求

### 13.1 响应时间
- **查询接口**：< 500ms
- **创建/更新接口**：< 1000ms
- **文件上传接口**：< 5000ms
- **报表接口**：< 2000ms

### 13.2 并发要求
- **支持并发用户数**：1000+
- **峰值QPS**：5000+
- **文件上传并发**：100+

## 14. 安全规范

### 14.1 数据加密
- **传输加密**：HTTPS/TLS 1.2+
- **敏感数据加密**：AES-256
- **密码加密**：BCrypt

### 14.2 访问控制
- **IP白名单**：限制访问来源
- **频率限制**：防止接口滥用
- **参数验证**：严格的输入验证

### 14.3 审计日志
- **操作记录**：记录所有关键操作
- **访问日志**：记录接口访问情况
- **异常监控**：实时监控异常情况

---

**文档版本**：v1.0  
**创建日期**：2025年1月  
**更新日期**：2025年1月  
**文档状态**：待评审