#!/bin/bash

# 档案管理系统性能优化启动脚本
# 启动性能监控、数据库优化和缓存优化

echo "🚀 启动档案管理系统性能优化..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装，请先安装Java 17+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装，请先安装Maven"
    exit 1
fi

# 检查MySQL环境
if ! command -v mysql &> /dev/null; then
    echo "❌ MySQL未安装，请先安装MySQL"
    exit 1
fi

echo "🔧 环境检查完成"

# 创建性能优化目录
mkdir -p performance
mkdir -p performance/logs
mkdir -p performance/reports
mkdir -p performance/cache

echo "📁 性能优化目录已创建"

# 优化数据库索引
echo "🗄️ 优化数据库索引..."
if [ -f "database/optimize_indexes.sql" ]; then
    mysql -u root -p < database/optimize_indexes.sql
    echo "✅ 数据库索引优化完成"
else
    echo "⚠️ 数据库索引优化脚本不存在"
fi

# 启动应用
echo "🚀 启动应用..."
./start-backend.sh &

# 等待应用启动
echo "⏳ 等待应用启动..."
sleep 30

# 检查应用状态
echo "🔍 检查应用状态..."
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ 应用已启动"
else
    echo "❌ 应用启动失败"
    exit 1
fi

# 启动性能监控
echo "📊 启动性能监控..."
./start-monitoring.sh &

# 等待监控工具启动
echo "⏳ 等待监控工具启动..."
sleep 20

# 检查监控工具状态
echo "🔍 检查监控工具状态..."

# 检查Prometheus
if curl -s http://localhost:9090 > /dev/null; then
    echo "✅ Prometheus已启动"
else
    echo "❌ Prometheus启动失败"
fi

# 检查Grafana
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Grafana已启动"
else
    echo "❌ Grafana启动失败"
fi

# 运行性能测试
echo "🧪 运行性能测试..."
./run-tests.sh

# 生成性能报告
echo "📊 生成性能报告..."
curl -s http://localhost:8080/api/performance/report?reportType=detailed > performance/reports/performance-report.json
curl -s http://localhost:8080/api/performance/optimization-suggestions > performance/reports/optimization-suggestions.json

echo ""
echo "🎉 性能优化启动完成！"
echo ""
echo "📊 监控面板访问地址："
echo "   应用健康检查: http://localhost:8080/actuator/health"
echo "   应用指标: http://localhost:8080/actuator/metrics"
echo "   性能报告: http://localhost:8080/api/performance/report"
echo "   Prometheus: http://localhost:9090"
echo "   Grafana: http://localhost:3000 (admin/admin123)"
echo ""
echo "🔧 性能优化功能："
echo "   慢查询监控: http://localhost:8080/api/performance/slow-queries"
echo "   缓存统计: http://localhost:8080/api/performance/cache-statistics"
echo "   系统概览: http://localhost:8080/api/performance/overview"
echo "   优化建议: http://localhost:8080/api/performance/optimization-suggestions"
echo ""
echo "📈 性能优化建议："
echo "   1. 定期检查慢查询日志"
echo "   2. 监控缓存命中率"
echo "   3. 优化数据库索引"
echo "   4. 调整线程池配置"
echo "   5. 监控系统资源使用情况"
echo ""
echo "🚀 下一步："
echo "   1. 访问Grafana查看性能指标"
echo "   2. 根据优化建议调整系统配置"
echo "   3. 定期运行性能测试"
echo "   4. 监控系统性能变化"
