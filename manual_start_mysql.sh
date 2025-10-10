#!/bin/bash

echo "正在启动MySQL服务..."
sudo /usr/local/mysql/support-files/mysql.server start

if [ $? -eq 0 ]; then
    echo "✅ MySQL启动成功"
    echo "验证MySQL状态..."
    pgrep -f mysqld > /dev/null && echo "✅ MySQL进程运行中" || echo "❌ MySQL进程未找到"
else
    echo "❌ MySQL启动失败"
    exit 1
fi


