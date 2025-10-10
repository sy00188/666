import { test, expect } from '@playwright/test'

test.describe('登录页面注册按钮测试', () => {
  test('应该显示注册按钮', async ({ page }) => {
    // 访问登录页面
    await page.goto('http://localhost:5173/auth/login')
    
    // 等待页面加载
    await page.waitForLoadState('networkidle')
    
    // 截图保存登录页面
    await page.screenshot({ path: 'login-with-register-button.png', fullPage: true })
    
    // 检查是否有注册提示文字
    const registerHint = await page.locator('.register-hint').textContent()
    expect(registerHint).toBe('还没有账号？')
    
    // 检查注册链接是否存在
    const registerLink = page.locator('.register-link')
    await expect(registerLink).toBeVisible()
    
    const registerLinkText = await registerLink.textContent()
    expect(registerLinkText).toContain('立即注册')
    
    console.log('✓ 注册按钮显示正常')
  })
  
  test('点击注册按钮应该跳转到注册页面', async ({ page }) => {
    // 访问登录页面
    await page.goto('http://localhost:5173/auth/login')
    
    // 等待页面加载
    await page.waitForLoadState('networkidle')
    
    // 点击注册链接
    await page.click('.register-link')
    
    // 等待导航完成
    await page.waitForURL('**/auth/register')
    
    // 验证URL
    expect(page.url()).toContain('/auth/register')
    
    // 截图保存注册页面
    await page.screenshot({ path: 'register-page.png', fullPage: true })
    
    // 验证注册页面的关键元素
    await expect(page.locator('h2')).toContainText('用户注册')
    
    console.log('✓ 注册按钮跳转功能正常')
  })
})

