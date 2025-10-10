#!/bin/bash

echo "========== MySQL 诊断脚本 =========="
echo ""

echo "1. 检查MySQL进程："
pgrep -fl mysqld

echo ""
echo "2. 检查数据目录权限："
sudo ls -la /usr/local/mysql/data/ | head -10

echo ""
echo "3. 查看错误日志（最后30行）："
sudo tail -30 /usr/local/mysql/data/*.err 2>/dev/null || echo "无法读取错误日志"

echo ""
echo "4. 检查端口占用："
lsof -i:3306 || echo "端口3306未被占用"

echo ""
echo "5. 检查PID文件："
sudo ls -la /usr/local/mysql/data/*.pid 2>/dev/null || echo "PID文件不存在"

echo ""
echo "6. 检查磁盘空间："
df -h /usr/local/mysql/data/

echo ""
echo "========== 诊断完成 =========="

