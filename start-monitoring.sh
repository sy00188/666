#!/bin/bash

# 档案管理系统监控启动脚本
# 启动Prometheus、Grafana、Alertmanager等监控工具

echo "🚀 启动档案管理系统监控工具..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 创建监控目录
mkdir -p monitoring
mkdir -p monitoring/data/prometheus
mkdir -p monitoring/data/grafana
mkdir -p monitoring/data/alertmanager

# 设置权限
chmod 755 monitoring/data/prometheus
chmod 755 monitoring/data/grafana
chmod 755 monitoring/data/alertmanager

echo "📁 监控目录已创建"

# 启动监控服务
echo "🔧 启动监控服务..."
docker-compose -f docker-compose.monitoring.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "🔍 检查服务状态..."

# 检查Prometheus
if curl -s http://localhost:9090 > /dev/null; then
    echo "✅ Prometheus已启动: http://localhost:9090"
else
    echo "❌ Prometheus启动失败"
fi

# 检查Grafana
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Grafana已启动: http://localhost:3000 (admin/admin123)"
else
    echo "❌ Grafana启动失败"
fi

# 检查Alertmanager
if curl -s http://localhost:9093 > /dev/null; then
    echo "✅ Alertmanager已启动: http://localhost:9093"
else
    echo "❌ Alertmanager启动失败"
fi

# 检查Node Exporter
if curl -s http://localhost:9100 > /dev/null; then
    echo "✅ Node Exporter已启动: http://localhost:9100"
else
    echo "❌ Node Exporter启动失败"
fi

# 检查cAdvisor
if curl -s http://localhost:8080 > /dev/null; then
    echo "✅ cAdvisor已启动: http://localhost:8080"
else
    echo "❌ cAdvisor启动失败"
fi

# 检查Jaeger
if curl -s http://localhost:16686 > /dev/null; then
    echo "✅ Jaeger已启动: http://localhost:16686"
else
    echo "❌ Jaeger启动失败"
fi

# 检查Zipkin
if curl -s http://localhost:9411 > /dev/null; then
    echo "✅ Zipkin已启动: http://localhost:9411"
else
    echo "❌ Zipkin启动失败"
fi

echo ""
echo "🎉 监控工具启动完成！"
echo ""
echo "📊 监控面板访问地址："
echo "   Prometheus: http://localhost:9090"
echo "   Grafana: http://localhost:3000 (admin/admin123)"
echo "   Alertmanager: http://localhost:9093"
echo "   Node Exporter: http://localhost:9100"
echo "   cAdvisor: http://localhost:8080"
echo "   Jaeger: http://localhost:16686"
echo "   Zipkin: http://localhost:9411"
echo ""
echo "🔧 管理命令："
echo "   查看日志: docker-compose -f docker-compose.monitoring.yml logs -f"
echo "   停止服务: docker-compose -f docker-compose.monitoring.yml down"
echo "   重启服务: docker-compose -f docker-compose.monitoring.yml restart"
echo ""
echo "📈 下一步："
echo "   1. 访问Grafana导入监控面板"
echo "   2. 配置告警规则"
echo "   3. 设置通知渠道"
echo "   4. 启动应用并观察指标"
