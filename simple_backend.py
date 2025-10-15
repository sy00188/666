#!/usr/bin/env python3
"""
简单的Python后端服务器 - 用于档案管理系统
提供基本的API端点和CORS支持，包括登录认证
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.parse
from datetime import datetime
import threading
import time
import uuid
import hashlib

# 模拟用户数据库
MOCK_USERS = {
    "admin": {
        "id": "1",
        "username": "admin",
        "password": "admin123",  # 实际应该加密
        "name": "系统管理员",
        "email": "admin@example.com",
        "role": "admin",
        "avatar": "",
        "permissions": ["*"],
        "status": "active",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
    },
    "user": {
        "id": "2",
        "username": "user",
        "password": "user123",
        "name": "普通用户",
        "email": "user@example.com",
        "role": "user",
        "avatar": "",
        "permissions": ["read", "write"],
        "status": "active",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-01T00:00:00"
    }
}

# 模拟token存储
ACTIVE_TOKENS = {}

class ArchiveAPIHandler(BaseHTTPRequestHandler):
    def do_OPTIONS(self):
        """处理CORS预检请求"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

    def do_GET(self):
        """处理GET请求"""
        # 解析URL和查询参数
        parsed_path = urllib.parse.urlparse(self.path)
        path = parsed_path.path
        query_params = urllib.parse.parse_qs(parsed_path.query)
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        # 生成模拟档案数据
        def generate_mock_archives(count=20):
            archives = []
            categories = ["行政文件", "财务报表", "人事档案", "技术文档", "项目资料"]
            statuses = ["normal", "borrowed", "archived"]
            
            for i in range(1, count + 1):
                archives.append({
                    "id": i,
                    "archiveNo": f"ARCH{2024}{str(i).zfill(6)}",
                    "title": f"档案文件 #{i}",
                    "category": categories[i % len(categories)],
                    "categoryId": (i % len(categories)) + 1,
                    "description": f"这是第{i}个测试档案的描述信息",
                    "keywords": f"关键词{i}, 测试, 档案",
                    "archiveDate": f"2024-{str((i % 12) + 1).zfill(2)}-{str((i % 28) + 1).zfill(2)}",
                    "status": statuses[i % len(statuses)],
                    "storageLocation": f"A区-{i}号柜-{i % 10}层",
                    "createUser": "管理员",
                    "createTime": f"2024-01-{str((i % 28) + 1).zfill(2)} 10:00:00",
                    "updateTime": f"2024-01-{str((i % 28) + 1).zfill(2)} 10:00:00",
                    "fileCount": i % 10 + 1,
                    "totalSize": f"{(i * 1.5):.2f}MB"
                })
            return archives
        
        if path == '/':
            response = {
                "message": "Archive Management System Backend is running successfully!",
                "timestamp": datetime.now().isoformat(),
                "version": "1.0.0-python"
            }
        elif path == '/health':
            response = {
                "status": "UP",
                "service": "archive-management",
                "timestamp": datetime.now().isoformat()
            }
        elif path == '/api/test':
            response = {
                "message": "Backend API is working",
                "timestamp": datetime.now().isoformat(),
                "version": "1.0.0-python"
            }
        elif path == '/api/status':
            response = {
                "backend": "running",
                "database": "mock-memory",
                "port": 8080,
                "language": "python"
            }
        # 档案统计 - 必须在档案列表之前判断（更具体的路径优先）
        elif path in ['/api/v1/archives/statistics', '/api/archives/statistics']:
            response = {
                "success": True,
                "message": "查询成功",
                "data": {
                    "totalCount": 100,
                    "todayCount": 5,
                    "weekCount": 23,
                    "monthCount": 78,
                    "borrowedCount": 15,
                    "availableCount": 85,
                    "overdueCount": 3,
                    "reservedCount": 8
                },
                "timestamp": datetime.now().isoformat()
            }
        # 支持 /api/v1/archives 和 /api/archives
        elif path.startswith('/api/v1/archives') or path.startswith('/api/archives'):
            page = int(query_params.get('page', [1])[0])
            size = int(query_params.get('size', [20])[0])
            
            # 生成模拟数据
            all_archives = generate_mock_archives(100)
            
            # 分页处理
            start = (page - 1) * size
            end = start + size
            paged_archives = all_archives[start:end]
            
            response = {
                "success": True,
                "message": "查询成功",
                "data": {
                    "records": paged_archives,
                    "total": len(all_archives),
                    "page": page,
                    "size": size,
                    "pages": (len(all_archives) + size - 1) // size
                },
                "timestamp": datetime.now().isoformat()
            }
        # 支持分类查询
        elif path.startswith('/api/v1/categories') or path.startswith('/api/categories'):
            response = {
                "success": True,
                "message": "查询成功",
                "data": [
                    {"id": 1, "name": "行政文件", "code": "XZWJ", "count": 25},
                    {"id": 2, "name": "财务报表", "code": "CWBB", "count": 150},
                    {"id": 3, "name": "人事档案", "code": "RSDA", "count": 80},
                    {"id": 4, "name": "技术文档", "code": "JSWD", "count": 32},
                    {"id": 5, "name": "项目资料", "code": "XMZL", "count": 45}
                ],
                "total": 5,
                "timestamp": datetime.now().isoformat()
            }
        elif path == '/api/auth/captcha':
            response = {
                "code": 200,
                "data": {
                    "captchaId": "mock-captcha-id",
                    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
                }
            }
        # 用户信息
        elif path == '/api/auth/user':
            auth_header = self.headers.get('Authorization', '')
            if auth_header.startswith('Bearer '):
                token = auth_header[7:]
                if token in ACTIVE_TOKENS:
                    username = ACTIVE_TOKENS[token]['username']
                    user = MOCK_USERS.get(username)
                    if user:
                        user_info = {k: v for k, v in user.items() if k != 'password'}
                        response = {
                            "success": True,
                            "message": "获取用户信息成功",
                            "data": user_info,
                            "timestamp": datetime.now().isoformat()
                        }
                    else:
                        response = {"success": False, "message": "用户不存在"}
                else:
                    response = {"success": False, "message": "Token无效"}
            else:
                response = {"success": False, "message": "未授权"}
        # 借阅趋势统计
        elif path == '/api/system/statistics/borrow-trend':
            # 生成最近7天的模拟数据
            from datetime import timedelta
            today = datetime.now()
            dates = [(today - timedelta(days=i)).strftime('%Y-%m-%d') for i in range(6, -1, -1)]
            
            response = {
                "success": True,
                "message": "查询成功",
                "data": {
                    "dates": dates,
                    "borrowCounts": [12, 15, 8, 20, 18, 22, 16],
                    "returnCounts": [10, 13, 9, 18, 15, 20, 14]
                },
                "timestamp": datetime.now().isoformat()
            }
        # 用户活跃度统计
        elif path == '/api/system/statistics/user-activity':
            response = {
                "success": True,
                "message": "查询成功",
                "data": {
                    "activeUsers": 45,
                    "totalUsers": 128,
                    "loginCount": 256,
                    "operationCount": 1580,
                    "hourlyActivity": [
                        {"hour": "00:00", "count": 5},
                        {"hour": "01:00", "count": 2},
                        {"hour": "02:00", "count": 1},
                        {"hour": "03:00", "count": 0},
                        {"hour": "04:00", "count": 0},
                        {"hour": "05:00", "count": 3},
                        {"hour": "06:00", "count": 8},
                        {"hour": "07:00", "count": 15},
                        {"hour": "08:00", "count": 28},
                        {"hour": "09:00", "count": 35},
                        {"hour": "10:00", "count": 42},
                        {"hour": "11:00", "count": 38},
                        {"hour": "12:00", "count": 25},
                        {"hour": "13:00", "count": 20},
                        {"hour": "14:00", "count": 30},
                        {"hour": "15:00", "count": 35},
                        {"hour": "16:00", "count": 32},
                        {"hour": "17:00", "count": 28},
                        {"hour": "18:00", "count": 15},
                        {"hour": "19:00", "count": 10},
                        {"hour": "20:00", "count": 8},
                        {"hour": "21:00", "count": 5},
                        {"hour": "22:00", "count": 3},
                        {"hour": "23:00", "count": 2}
                    ]
                },
                "timestamp": datetime.now().isoformat()
            }
        # 最新动态
        elif path == '/api/system/activities/recent':
            response = {
                "success": True,
                "message": "查询成功",
                "data": [
                    {
                        "id": 1,
                        "type": "borrow",
                        "user": "张三",
                        "action": "借阅了档案",
                        "target": "ARCH202400001 - 重要文件",
                        "time": "2分钟前",
                        "avatar": ""
                    },
                    {
                        "id": 2,
                        "type": "return",
                        "user": "李四",
                        "action": "归还了档案",
                        "target": "ARCH202400015 - 财务报表",
                        "time": "15分钟前",
                        "avatar": ""
                    },
                    {
                        "id": 3,
                        "type": "archive",
                        "user": "王五",
                        "action": "新增了档案",
                        "target": "ARCH202400102 - 项目资料",
                        "time": "1小时前",
                        "avatar": ""
                    },
                    {
                        "id": 4,
                        "type": "update",
                        "user": "赵六",
                        "action": "更新了档案信息",
                        "target": "ARCH202400045 - 技术文档",
                        "time": "2小时前",
                        "avatar": ""
                    },
                    {
                        "id": 5,
                        "type": "delete",
                        "user": "管理员",
                        "action": "删除了过期档案",
                        "target": "ARCH202300088",
                        "time": "3小时前",
                        "avatar": ""
                    }
                ],
                "timestamp": datetime.now().isoformat()
            }
        # 待办事项
        elif path == '/api/system/todos/pending':
            response = {
                "success": True,
                "message": "查询成功",
                "data": [
                    {
                        "id": 1,
                        "title": "审批借阅申请",
                        "description": "3个档案借阅申请待审批",
                        "priority": "high",
                        "dueDate": "今天",
                        "status": "pending",
                        "count": 3
                    },
                    {
                        "id": 2,
                        "title": "处理逾期归还",
                        "description": "2个档案已逾期未归还",
                        "priority": "urgent",
                        "dueDate": "已逾期",
                        "status": "pending",
                        "count": 2
                    },
                    {
                        "id": 3,
                        "title": "档案整理",
                        "description": "季度档案整理工作",
                        "priority": "medium",
                        "dueDate": "本周",
                        "status": "pending",
                        "count": 1
                    },
                    {
                        "id": 4,
                        "title": "系统备份",
                        "description": "执行系统数据备份",
                        "priority": "low",
                        "dueDate": "本月",
                        "status": "pending",
                        "count": 1
                    }
                ],
                "timestamp": datetime.now().isoformat()
            }
        # 通用统计信息（兼容旧路径）
        elif path.startswith('/api/v1/statistics') or path.startswith('/api/statistics'):
            response = {
                "success": True,
                "message": "查询成功",
                "data": {
                    "totalArchives": 100,
                    "totalCategories": 5,
                    "totalBorrowed": 15,
                    "totalStorage": "256.8MB",
                    "monthlyGrowth": 12,
                    "categoryStats": [
                        {"category": "行政文件", "count": 25},
                        {"category": "财务报表", "count": 150},
                        {"category": "人事档案", "count": 80},
                        {"category": "技术文档", "count": 32},
                        {"category": "项目资料", "count": 45}
                    ]
                },
                "timestamp": datetime.now().isoformat()
            }
        else:
            response = {
                "success": False,
                "error": "Not Found",
                "message": f"Endpoint {path} not found",
                "timestamp": datetime.now().isoformat()
            }
        
        self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))

    def do_POST(self):
        """处理POST请求"""
        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length)
        
        path = self.path
        
        try:
            data = json.loads(post_data.decode('utf-8')) if content_length > 0 else {}
        except json.JSONDecodeError:
            self.send_error_response(400, "Invalid JSON")
            return
        
        # 处理登录请求
        if path == '/api/auth/login':
            self.handle_login(data)
        # 处理登出请求
        elif path == '/api/auth/logout':
            self.handle_logout()
        # 处理注册请求
        elif path == '/api/auth/register':
            self.handle_register(data)
        # 处理微信登录
        elif path == '/api/auth/wechat/mock-login':
            self.handle_wechat_login(data)
        # 处理QQ登录
        elif path == '/api/auth/qq/mock-login':
            self.handle_qq_login(data)
        # 默认处理
        else:
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            response = {
                "success": True,
                "message": "数据已接收",
                "received_data": data,
                "timestamp": datetime.now().isoformat()
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
    
    def send_error_response(self, status_code, message):
        """发送错误响应"""
        self.send_response(status_code)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        response = {
            "success": False,
            "message": message,
            "timestamp": datetime.now().isoformat()
        }
        self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
    
    def handle_login(self, data):
        """处理登录请求"""
        username = data.get('username', '')
        password = data.get('password', '')
        
        # 验证用户名和密码
        if username in MOCK_USERS:
            user = MOCK_USERS[username]
            if user['password'] == password:
                # 生成token
                token = str(uuid.uuid4())
                ACTIVE_TOKENS[token] = {
                    'username': username,
                    'created_at': datetime.now().isoformat()
                }
                
                # 返回用户信息（不包含密码）
                user_info = {k: v for k, v in user.items() if k != 'password'}
                
                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                
                response = {
                    "success": True,
                    "message": "登录成功",
                    "data": {
                        "token": token,
                        "user": user_info
                    },
                    "timestamp": datetime.now().isoformat()
                }
                self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
                return
        
        # 登录失败
        self.send_error_response(401, "用户名或密码错误")
    
    def handle_logout(self):
        """处理登出请求"""
        # 从请求头获取token
        auth_header = self.headers.get('Authorization', '')
        if auth_header.startswith('Bearer '):
            token = auth_header[7:]
            if token in ACTIVE_TOKENS:
                del ACTIVE_TOKENS[token]
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        response = {
            "success": True,
            "message": "登出成功",
            "timestamp": datetime.now().isoformat()
        }
        self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
    
    def handle_register(self, data):
        """处理注册请求"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        response = {
            "success": True,
            "message": "注册成功，请登录",
            "timestamp": datetime.now().isoformat()
        }
        self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
    
    def handle_wechat_login(self, data):
        """处理微信登录"""
        token = str(uuid.uuid4())
        user_info = {
            "id": "wechat_" + str(uuid.uuid4()),
            "username": data.get('nickname', '微信用户'),
            "name": data.get('nickname', '微信用户'),
            "email": "",
            "role": "user",
            "avatar": data.get('headimgurl', ''),
            "permissions": ["read", "write"],
            "status": "active",
            "createdAt": datetime.now().isoformat(),
            "updatedAt": datetime.now().isoformat()
        }
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        response = {
            "success": True,
            "message": "微信登录成功",
            "data": {
                "token": token,
                "user": user_info
            },
            "timestamp": datetime.now().isoformat()
        }
        self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))
    
    def handle_qq_login(self, data):
        """处理QQ登录"""
        token = str(uuid.uuid4())
        user_info = {
            "id": "qq_" + str(uuid.uuid4()),
            "username": data.get('nickname', 'QQ用户'),
            "name": data.get('nickname', 'QQ用户'),
            "email": "",
            "role": "user",
            "avatar": data.get('figureurl_qq_1', ''),
            "permissions": ["read", "write"],
            "status": "active",
            "createdAt": datetime.now().isoformat(),
            "updatedAt": datetime.now().isoformat()
        }
        
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        
        response = {
            "success": True,
            "message": "QQ登录成功",
            "data": {
                "token": token,
                "user": user_info
            },
            "timestamp": datetime.now().isoformat()
        }
        self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))

    def log_message(self, format, *args):
        """自定义日志格式"""
        print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {format % args}")

def run_server(port=8080):
    """启动服务器"""
    server_address = ('', port)
    httpd = HTTPServer(server_address, ArchiveAPIHandler)
    print(f"🚀 Archive Management Backend Server starting on port {port}")
    print(f"📍 Server URL: http://localhost:{port}")
    print(f"🔗 API Base URL: http://localhost:{port}/api")
    print(f"💚 Health Check: http://localhost:{port}/health")
    print("=" * 50)
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n🛑 Server stopped by user")
        httpd.server_close()

if __name__ == '__main__':
    run_server()