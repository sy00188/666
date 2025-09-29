#!/bin/bash

# 档案管理系统后端启动脚本
echo "正在启动档案管理系统后端服务..."

# 设置Java版本（如果需要）
export JAVA_HOME=/usr/local/opt/openjdk@17 2>/dev/null || true

# 创建一个简单的Spring Boot应用模拟器
echo "启动模拟后端服务在端口 8080..."

# 使用Python创建一个简单的HTTP服务器来模拟后端API
python3 -c "
import http.server
import socketserver
import json
from urllib.parse import urlparse, parse_qs
import threading
import time

class ArchiveHandler(http.server.BaseHTTPRequestHandler):
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()
    
    def do_POST(self):
        if self.path == '/api/auth/login':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟登录响应
            response = {
                'success': True,
                'code': 200,
                'message': '登录成功',
                'data': {
                    'token': 'mock-jwt-token-12345',
                    'user': {
                        'id': 1,
                        'username': 'admin',
                        'name': '管理员',
                        'role': 'admin',
                        'permissions': ['read', 'write', 'delete']
                    }
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()
    
    def do_GET(self):
        if self.path == '/api/auth/captcha':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            response = {
                'code': 200,
                'data': {
                    'captchaId': 'mock-captcha-id',
                    'captchaImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=='
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path.startswith('/api/v1/archives') or self.path.startswith('/v1/archives'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟档案列表响应
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': {
                    'list': [
                        {
                            'id': 1,
                            'title': '测试档案1',
                            'description': '这是一个测试档案',
                            'categoryId': 1,
                            'categoryName': '行政文件',
                            'securityLevel': 'public',
                            'status': 'active',
                            'createTime': '2024-01-01 10:00:00',
                            'updateTime': '2024-01-01 10:00:00'
                        },
                        {
                            'id': 2,
                            'title': '测试档案2',
                            'description': '这是另一个测试档案',
                            'categoryId': 2,
                            'categoryName': '财务文件',
                            'securityLevel': 'internal',
                            'status': 'active',
                            'createTime': '2024-01-02 10:00:00',
                            'updateTime': '2024-01-02 10:00:00'
                        }
                    ],
                    'total': 2,
                    'page': 1,
                    'size': 10
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path.startswith('/api/v1/categories') or self.path.startswith('/v1/categories'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟分类列表响应
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': [
                    {
                        'id': 1,
                        'name': '行政文件',
                        'description': '行政管理相关文件',
                        'parentId': None,
                        'level': 1,
                        'sort': 1,
                        'status': 'active',
                        'createTime': '2024-01-01 10:00:00'
                    },
                    {
                        'id': 2,
                        'name': '财务文件',
                        'description': '财务管理相关文件',
                        'parentId': None,
                        'level': 1,
                        'sort': 2,
                        'status': 'active',
                        'createTime': '2024-01-01 10:00:00'
                    }
                ]
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path.startswith('/api/users'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟用户列表响应
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': {
                    'list': [
                        {
                            'id': 1,
                            'username': 'admin',
                            'name': '管理员',
                            'email': 'admin@example.com',
                            'role': 'admin',
                            'status': 'active',
                            'createTime': '2024-01-01 10:00:00',
                            'lastLoginTime': '2025-01-15 09:30:00'
                        },
                        {
                            'id': 2,
                            'username': 'user1',
                            'name': '张三',
                            'email': 'zhangsan@example.com',
                            'role': 'user',
                            'status': 'active',
                            'createTime': '2024-01-02 10:00:00',
                            'lastLoginTime': '2025-01-15 08:45:00'
                        },
                        {
                            'id': 3,
                            'username': 'user2',
                            'name': '李四',
                            'email': 'lisi@example.com',
                            'role': 'user',
                            'status': 'active',
                            'createTime': '2024-01-03 10:00:00',
                            'lastLoginTime': '2025-01-14 16:20:00'
                        }
                    ],
                    'total': 3,
                    'page': 1,
                    'size': 10
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path.startswith('/api/v1/statistics') or self.path.startswith('/v1/archives/statistics'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟档案统计数据响应
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': {
                    'totalCount': 1250,
                    'todayCount': 8,
                    'weekCount': 45,
                    'monthCount': 180,
                    'borrowedCount': 320,
                    'availableCount': 930,
                    'overdueCount': 15,
                    'reservedCount': 25
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path == '/api/system/activities/recent':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟最新动态数据
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': [
                    {
                        'id': 1,
                        'type': 'borrow',
                        'user': '张三',
                        'action': '借阅了档案',
                        'target': '《公司章程修订案》',
                        'time': '2025-01-15 14:30:00',
                        'avatar': 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
                    },
                    {
                        'id': 2,
                        'type': 'return',
                        'user': '李四',
                        'action': '归还了档案',
                        'target': '《财务报表2024》',
                        'time': '2025-01-15 13:45:00',
                        'avatar': 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
                    },
                    {
                        'id': 3,
                        'type': 'create',
                        'user': '王五',
                        'action': '创建了新档案',
                        'target': '《员工手册2025版》',
                        'time': '2025-01-15 11:20:00',
                        'avatar': 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
                    },
                    {
                        'id': 4,
                        'type': 'update',
                        'user': '赵六',
                        'action': '更新了档案信息',
                        'target': '《合同模板集》',
                        'time': '2025-01-15 10:15:00',
                        'avatar': 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
                    }
                ]
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path == '/api/system/todos/pending':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟待办事项数据
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': [
                    {
                        'id': 1,
                        'title': '审核新档案',
                        'description': '需要审核5份新提交的档案',
                        'priority': 'high',
                        'dueDate': '2025-01-16',
                        'status': 'pending',
                        'count': 5
                    },
                    {
                        'id': 2,
                        'title': '逾期提醒',
                        'description': '有3份档案已逾期，需要催还',
                        'priority': 'urgent',
                        'dueDate': '2025-01-15',
                        'status': 'pending',
                        'count': 3
                    },
                    {
                        'id': 3,
                        'title': '系统维护',
                        'description': '定期系统备份和维护',
                        'priority': 'medium',
                        'dueDate': '2025-01-18',
                        'status': 'pending',
                        'count': 1
                    },
                    {
                        'id': 4,
                        'title': '月度报告',
                        'description': '生成本月档案管理统计报告',
                        'priority': 'medium',
                        'dueDate': '2025-01-20',
                        'status': 'pending',
                        'count': 1
                    }
                ]
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path == '/api/system/statistics/borrow-trend':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟借阅趋势数据
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': {
                    'dates': ['2025-01-01', '2025-01-02', '2025-01-03', '2025-01-04', '2025-01-05'],
                    'borrowCounts': [12, 15, 8, 20, 18],
                    'returnCounts': [10, 12, 9, 16, 14]
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        elif self.path == '/api/system/statistics/user-activity':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # 模拟用户活跃度数据
            response = {
                'success': True,
                'code': 200,
                'message': '获取成功',
                'data': {
                    'activeUsers': 45,
                    'totalUsers': 120,
                    'loginCount': 89,
                    'operationCount': 234,
                    'hourlyActivity': [
                        {'hour': '09:00', 'count': 15},
                        {'hour': '10:00', 'count': 25},
                        {'hour': '11:00', 'count': 20},
                        {'hour': '14:00', 'count': 30},
                        {'hour': '15:00', 'count': 18},
                        {'hour': '16:00', 'count': 12}
                    ]
                }
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

PORT = 8080
with socketserver.TCPServer(('', PORT), ArchiveHandler) as httpd:
    print(f'档案管理系统后端服务已启动在 http://localhost:{PORT}')
    print('支持的API端点:')
    print('  POST /auth/login - 用户登录')
    print('  GET /auth/captcha - 获取验证码')
    print('按 Ctrl+C 停止服务')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print('\n正在停止服务...')
        httpd.shutdown()
"