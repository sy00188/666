#!/bin/bash
# CI/CD 配置验证脚本
# 用于验证所有CI/CD配置是否正确

set -e

echo "=========================================="
echo "  CI/CD 配置验证脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} 文件存在: $1"
        return 0
    else
        echo -e "${RED}✗${NC} 文件缺失: $1"
        return 1
    fi
}

check_yaml_syntax() {
    if python3 -c "import yaml; yaml.safe_load(open('$1'))" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} YAML语法正确: $1"
        return 0
    else
        echo -e "${RED}✗${NC} YAML语法错误: $1"
        return 1
    fi
}

# 1. 检查必需文件
echo "1. 检查配置文件..."
echo "-------------------"
check_file ".github/workflows/ci.yml"
check_file ".github/workflows/notify-slack.yml"
check_file "codecov.yml"
check_file "README-CI-CD.md"
echo ""

# 2. 检查YAML语法
echo "2. 验证YAML语法..."
echo "-------------------"
check_yaml_syntax ".github/workflows/ci.yml"
check_yaml_syntax ".github/workflows/notify-slack.yml"
check_yaml_syntax "codecov.yml"
echo ""

# 3. 检查后端测试配置
echo "3. 检查后端测试配置..."
echo "-------------------"
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}✓${NC} pom.xml 存在"
    
    # 检查JaCoCo插件
    if grep -q "jacoco-maven-plugin" pom.xml; then
        echo -e "${GREEN}✓${NC} JaCoCo插件已配置"
    else
        echo -e "${YELLOW}⚠${NC} JaCoCo插件未配置"
    fi
    
    # 检查Surefire插件
    if grep -q "maven-surefire-plugin" pom.xml; then
        echo -e "${GREEN}✓${NC} Surefire插件已配置"
    else
        echo -e "${YELLOW}⚠${NC} Surefire插件未配置"
    fi
else
    echo -e "${RED}✗${NC} pom.xml 不存在"
fi
echo ""

# 4. 检查前端测试配置
echo "4. 检查前端测试配置..."
echo "-------------------"
if [ -f "frontend/package.json" ]; then
    echo -e "${GREEN}✓${NC} package.json 存在"
    
    # 检查测试脚本
    if grep -q '"test"' frontend/package.json; then
        echo -e "${GREEN}✓${NC} 测试脚本已配置"
    else
        echo -e "${YELLOW}⚠${NC} 测试脚本未配置"
    fi
    
    # 检查覆盖率脚本
    if grep -q '"test:coverage"' frontend/package.json; then
        echo -e "${GREEN}✓${NC} 覆盖率脚本已配置"
    else
        echo -e "${YELLOW}⚠${NC} 覆盖率脚本未配置"
    fi
else
    echo -e "${RED}✗${NC} frontend/package.json 不存在"
fi

if [ -f "frontend/vitest.config.ts" ]; then
    echo -e "${GREEN}✓${NC} vitest.config.ts 存在"
else
    echo -e "${YELLOW}⚠${NC} vitest.config.ts 不存在"
fi
echo ""

# 5. 检查Git状态
echo "5. 检查Git状态..."
echo "-------------------"
if git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Git仓库已初始化"
    
    # 检查是否有远程仓库
    if git remote -v | grep -q "origin"; then
        echo -e "${GREEN}✓${NC} 远程仓库已配置"
        REMOTE_URL=$(git remote get-url origin)
        echo "   远程URL: $REMOTE_URL"
    else
        echo -e "${YELLOW}⚠${NC} 远程仓库未配置"
    fi
else
    echo -e "${RED}✗${NC} 不是Git仓库"
fi
echo ""

# 6. 检查GitHub Secrets（提示）
echo "6. GitHub Secrets 配置检查..."
echo "-------------------"
echo -e "${YELLOW}ℹ${NC} 需要手动配置以下Secrets："
echo "   1. CODECOV_TOKEN (必需)"
echo "      - 访问 https://codecov.io 获取"
echo "   2. SLACK_WEBHOOK_URL (可选)"
echo "      - 访问 https://api.slack.com/apps 获取"
echo ""
echo "   配置路径: GitHub仓库 > Settings > Secrets and variables > Actions"
echo ""

# 7. 本地测试验证
echo "7. 本地测试验证..."
echo "-------------------"
echo -e "${YELLOW}ℹ${NC} 建议运行以下命令验证测试："
echo ""
echo "   后端测试:"
echo "   $ mvn clean test"
echo ""
echo "   前端测试:"
echo "   $ cd frontend && npm run test:coverage"
echo ""

# 8. 下一步操作
echo "=========================================="
echo "  下一步操作"
echo "=========================================="
echo ""
echo "1. 提交配置文件到Git："
echo "   $ git add .github/ codecov.yml README-CI-CD.md"
echo "   $ git commit -m 'feat: 添加CI/CD配置'"
echo ""
echo "2. 推送到GitHub："
echo "   $ git push origin main"
echo ""
echo "3. 配置Codecov Token："
echo "   - 访问 https://codecov.io"
echo "   - 登录并添加仓库"
echo "   - 复制Token到GitHub Secrets"
echo ""
echo "4. 查看工作流运行："
echo "   - 访问 GitHub仓库 > Actions"
echo "   - 查看CI/CD Pipeline运行状态"
echo ""
echo "5. 验证覆盖率报告："
echo "   - 访问 https://codecov.io/gh/你的用户名/你的仓库"
echo "   - 查看覆盖率报告"
echo ""
echo "=========================================="
echo -e "${GREEN}✓${NC} 验证完成！"
echo "=========================================="

