#!/bin/bash

# 档案管理系统测试运行脚本
# 运行单元测试、集成测试和性能测试

echo "🧪 启动档案管理系统测试..."

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

# 设置测试环境变量
export SPRING_PROFILES_ACTIVE=test
export SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
export SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
export SPRING_CACHE_TYPE=simple

echo "🔧 测试环境配置完成"

# 清理之前的测试结果
echo "🧹 清理之前的测试结果..."
mvn clean

# 运行单元测试
echo "📝 运行单元测试..."
mvn test -Dtest="*Test" -DfailIfNoTests=false

# 检查单元测试结果
if [ $? -eq 0 ]; then
    echo "✅ 单元测试通过"
else
    echo "❌ 单元测试失败"
    exit 1
fi

# 运行集成测试
echo "🔗 运行集成测试..."
mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false

# 检查集成测试结果
if [ $? -eq 0 ]; then
    echo "✅ 集成测试通过"
else
    echo "❌ 集成测试失败"
    exit 1
fi

# 运行性能测试
echo "⚡ 运行性能测试..."
mvn test -Dtest="*PerformanceTest" -DfailIfNoTests=false

# 检查性能测试结果
if [ $? -eq 0 ]; then
    echo "✅ 性能测试通过"
else
    echo "❌ 性能测试失败"
    exit 1
fi

# 生成测试报告
echo "📊 生成测试报告..."
mvn surefire-report:report

# 生成JaCoCo覆盖率报告
echo "📈 生成代码覆盖率报告..."
mvn jacoco:report

# 生成SonarQube报告
echo "🔍 生成代码质量报告..."
mvn sonar:sonar -Dsonar.projectKey=archive-management-system -Dsonar.host.url=http://localhost:9000 -Dsonar.login=admin -Dsonar.password=admin

echo ""
echo "🎉 所有测试完成！"
echo ""
echo "📊 测试报告位置："
echo "   单元测试报告: target/site/surefire-report.html"
echo "   代码覆盖率报告: target/site/jacoco/index.html"
echo "   代码质量报告: http://localhost:9000"
echo ""
echo "🔧 查看测试结果："
echo "   打开浏览器访问: file://$(pwd)/target/site/surefire-report.html"
echo "   打开浏览器访问: file://$(pwd)/target/site/jacoco/index.html"
echo ""
echo "📈 测试统计："
echo "   总测试数: $(find target/surefire-reports -name "*.xml" | xargs grep -h "tests=" | awk '{sum+=$2} END {print sum}')"
echo "   通过测试数: $(find target/surefire-reports -name "*.xml" | xargs grep -h "tests=" | awk '{sum+=$2} END {print sum}')"
echo "   失败测试数: $(find target/surefire-reports -name "*.xml" | xargs grep -h "failures=" | awk '{sum+=$2} END {print sum}')"
echo "   错误测试数: $(find target/surefire-reports -name "*.xml" | xargs grep -h "errors=" | awk '{sum+=$2} END {print sum}')"
echo ""
echo "🚀 下一步："
echo "   1. 查看测试报告了解测试结果"
echo "   2. 查看代码覆盖率报告了解代码覆盖情况"
echo "   3. 查看代码质量报告了解代码质量问题"
echo "   4. 根据报告结果优化代码和测试"
