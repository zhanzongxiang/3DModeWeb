-- 基础管理数据种子脚本（可重复执行）
-- 适用前提：已执行 init.sql / admin_governance.sql，且相关表结构存在
--
-- 默认超级管理员账号：
-- username: superadmin
-- password: Admin@123456
-- bcrypt : $2b$10$bVh.5wXfAYAmaQQ1ZxdR3ePcC6TmWnIQyMVdkj/RUD3kuZEwNxSu.
--
-- 建议：首次登录后立即修改密码

START TRANSACTION;

-- 1) 根组织
INSERT INTO sys_org (id, parent_id, name, code, status, sort, ancestors, create_time, update_time)
VALUES (1, 0, '平台组织', 'ROOT', 1, 0, '0', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  update_time = NOW();

-- 2) 基础角色
INSERT INTO sys_role (name, code, description, status, data_scope_type, is_system, create_time, update_time)
VALUES
  ('超级管理员', 'SUPER_ADMIN', '平台超级管理员，拥有全部权限', 1, 'ALL', 1, NOW(), NOW()),
  ('平台管理员', 'PLATFORM_ADMIN', '平台管理角色，支持组织与用户管理', 1, 'ORG_AND_CHILDREN', 0, NOW(), NOW()),
  ('运营专员', 'OPS_OPERATOR', '日常运营角色，偏内容与用户运营', 1, 'ORG_ONLY', 0, NOW(), NOW()),
  ('安全审计员', 'SEC_AUDITOR', '安全审计角色，偏只读与审计追踪', 1, 'ORG_AND_CHILDREN', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  status = VALUES(status),
  data_scope_type = VALUES(data_scope_type),
  update_time = NOW();

-- 3) 超级管理员账号
INSERT INTO sys_user (
  username, nickname, real_name, password_hash, status, org_id, create_time, update_time
)
VALUES (
  'superadmin',
  '系统超管',
  '系统超管',
  '$2b$10$bVh.5wXfAYAmaQQ1ZxdR3ePcC6TmWnIQyMVdkj/RUD3kuZEwNxSu.',
  1,
  1,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  real_name = VALUES(real_name),
  password_hash = VALUES(password_hash),
  status = VALUES(status),
  org_id = VALUES(org_id),
  update_time = NOW();

-- 4) 角色绑定：superadmin -> SUPER_ADMIN
INSERT INTO sys_user_role (user_id, role_id, create_time)
SELECT u.id, r.id, NOW()
FROM sys_user u
JOIN sys_role r ON r.code = 'SUPER_ADMIN'
WHERE u.username = 'superadmin'
ON DUPLICATE KEY UPDATE
  create_time = VALUES(create_time);

-- 5) 超级管理员默认拥有全部菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id, create_time)
SELECT r.id, m.id, NOW()
FROM sys_role r
JOIN sys_menu m
WHERE r.code = 'SUPER_ADMIN';

-- 6) 超级管理员默认拥有全部接口权限
INSERT IGNORE INTO sys_role_api (role_id, api_id, create_time)
SELECT r.id, a.id, NOW()
FROM sys_role r
JOIN sys_api_resource a
WHERE r.code = 'SUPER_ADMIN';

-- 7) 平台管理员默认菜单（系统管理/用户管理/角色管理/组织管理）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id, create_time)
SELECT r.id, m.id, NOW()
FROM sys_role r
JOIN sys_menu m ON m.path IN ('/admin', '/admin/users', '/admin/roles', '/admin/orgs')
WHERE r.code = 'PLATFORM_ADMIN';

-- 8) 平台管理员默认接口权限（管理主链路）
INSERT IGNORE INTO sys_role_api (role_id, api_id, create_time)
SELECT r.id, a.id, NOW()
FROM sys_role r
JOIN sys_api_resource a
  ON a.perm_code IN (
    'sys:auth:me',
    'sys:org:list', 'sys:org:create', 'sys:org:update', 'sys:org:status',
    'sys:user:list', 'sys:user:create', 'sys:user:update', 'sys:user:status', 'sys:user:reset-password', 'sys:user:assign-role',
    'sys:role:list', 'sys:role:create', 'sys:role:update', 'sys:role:status', 'sys:role:assign-permission',
    'sys:permission:menu:list', 'sys:permission:api:list'
  )
WHERE r.code = 'PLATFORM_ADMIN';

-- 9) 运营专员默认接口权限（只读 + 用户检索）
INSERT IGNORE INTO sys_role_api (role_id, api_id, create_time)
SELECT r.id, a.id, NOW()
FROM sys_role r
JOIN sys_api_resource a
  ON a.perm_code IN (
    'sys:auth:me',
    'sys:user:list',
    'sys:org:list',
    'sys:role:list',
    'sys:permission:menu:list',
    'sys:permission:api:list'
  )
WHERE r.code = 'OPS_OPERATOR';

-- 10) 安全审计员默认接口权限（只读）
INSERT IGNORE INTO sys_role_api (role_id, api_id, create_time)
SELECT r.id, a.id, NOW()
FROM sys_role r
JOIN sys_api_resource a
  ON a.perm_code IN (
    'sys:auth:me',
    'sys:user:list',
    'sys:org:list',
    'sys:role:list',
    'sys:permission:menu:list',
    'sys:permission:api:list'
  )
WHERE r.code = 'SEC_AUDITOR';

COMMIT;
