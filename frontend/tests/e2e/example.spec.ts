import { test, expect } from '@playwright/test';

test.describe('档案管理系统基础功能测试', () => {
  test('首页加载测试', async ({ page }) => {
    // 访问首页
    await page.goto('/');
    
    // 验证页面标题
    await expect(page).toHaveTitle(/档案管理系统/);
    
    // 验证页面基本元素存在
    await expect(page.locator('body')).toBeVisible();
  });

  test('登录页面导航测试', async ({ page }) => {
    // 访问登录页面
    await page.goto('/auth/login');
    
    // 验证登录表单存在
    await expect(page.locator('form')).toBeVisible();
    
    // 验证基本输入框存在
    const usernameInput = page.locator('input[type="text"], input[placeholder*="用户名"], input[placeholder*="账号"]').first();
    const passwordInput = page.locator('input[type="password"]').first();
    
    await expect(usernameInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
  });

  test('页面响应性测试', async ({ page }) => {
    // 测试移动端视口
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');
    
    // 验证页面在移动端正常显示
    await expect(page.locator('body')).toBeVisible();
    
    // 测试桌面端视口
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/');
    
    // 验证页面在桌面端正常显示
    await expect(page.locator('body')).toBeVisible();
  });
});