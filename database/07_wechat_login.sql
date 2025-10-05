-- =====================================================
-- 微信登录功能数据库变更
-- 创建时间: 2024-01-20
-- 说明: 为User表添加微信登录相关字段，创建第三方登录日志表
-- =====================================================

-- 1. 为User表添加微信相关字段
ALTER TABLE user 
ADD COLUMN wechat_openid VARCHAR(64) UNIQUE COMMENT '微信OpenID' AFTER status,
ADD COLUMN wechat_unionid VARCHAR(64) COMMENT '微信UnionID' AFTER wechat_openid,
ADD COLUMN wechat_nickname VARCHAR(100) COMMENT '微信昵称' AFTER wechat_unionid,
ADD COLUMN wechat_avatar VARCHAR(255) COMMENT '微信头像URL' AFTER wechat_nickname,
ADD COLUMN wechat_binding_time DATETIME COMMENT '微信绑定时间' AFTER wechat_avatar;

-- 2. 为微信OpenID创建索引（用于快速查询）
CREATE INDEX idx_wechat_openid ON user(wechat_openid);

-- 3. 创建第三方登录日志表
CREATE TABLE IF NOT EXISTS oauth_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    provider VARCHAR(20) NOT NULL COMMENT '登录提供商(wechat/qq)',
    openid VARCHAR(64) NOT NULL COMMENT '第三方平台OpenID',
    unionid VARCHAR(64) COMMENT '第三方平台UnionID',
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    status VARCHAR(20) NOT NULL COMMENT '登录状态(success/failed/binding_required)',
    error_message VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_openid (openid),
    INDEX idx_provider (provider),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方登录日志表';

-- 4. 创建微信登录状态临时表（用于轮询状态）
CREATE TABLE IF NOT EXISTS wechat_login_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    state VARCHAR(64) NOT NULL UNIQUE COMMENT '登录状态标识',
    openid VARCHAR(64) COMMENT '微信OpenID',
    unionid VARCHAR(64) COMMENT '微信UnionID',
    nickname VARCHAR(100) COMMENT '微信昵称',
    avatar VARCHAR(255) COMMENT '微信头像',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态(pending/success/expired/error)',
    user_id BIGINT COMMENT '关联的用户ID（登录成功后）',
    access_token VARCHAR(512) COMMENT '微信访问令牌',
    refresh_token VARCHAR(512) COMMENT '微信刷新令牌',
    expires_time DATETIME COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_state (state),
    INDEX idx_status (status),
    INDEX idx_openid (openid),
    INDEX idx_expires_time (expires_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信登录状态表';

-- 5. 插入初始配置数据
INSERT INTO system_config (config_key, config_value, config_desc, create_time, update_time)
VALUES 
    ('wechat.login.enabled', 'true', '微信登录功能开关', NOW(), NOW()),
    ('wechat.login.mock_mode', 'true', '微信登录模拟模式（开发环境使用）', NOW(), NOW()),
    ('wechat.login.qrcode_expire_minutes', '5', '微信登录二维码过期时间（分钟）', NOW(), NOW()),
    ('wechat.login.auto_register', 'true', '微信登录是否自动注册新用户', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    update_time = NOW();

-- 6. 添加注释说明
ALTER TABLE user COMMENT = '用户表（已支持微信登录）';

-- 完成提示
SELECT '微信登录功能数据库变更执行完成' AS message;

