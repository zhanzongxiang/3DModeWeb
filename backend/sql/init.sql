CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tb_model (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  artwork_name VARCHAR(128) NOT NULL,
  type VARCHAR(64) NOT NULL,
  image_urls JSON NULL,
  disk_link VARCHAR(512) NOT NULL,
  is_free TINYINT NOT NULL DEFAULT 1,
  print_layer_height DECIMAL(3,2) NULL,
  print_infill INT NULL,
  print_support TINYINT NULL,
  print_material VARCHAR(32) NULL,
  license_type VARCHAR(32) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_model_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_model_type ON tb_model(type);
CREATE INDEX idx_model_name ON tb_model(name);
CREATE INDEX idx_model_artwork_name ON tb_model(artwork_name);

CREATE TABLE IF NOT EXISTS tb_make (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  image_url VARCHAR(512) NOT NULL,
  description TEXT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_make_model FOREIGN KEY (model_id) REFERENCES tb_model(id),
  CONSTRAINT fk_make_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_make_model_id ON tb_make(model_id);

CREATE TABLE IF NOT EXISTS tb_collection (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  model_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_collection_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_collection_model FOREIGN KEY (model_id) REFERENCES tb_model(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uk_collection_user_model ON tb_collection(user_id, model_id);

ALTER TABLE sys_user
  ADD COLUMN IF NOT EXISTS nickname VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS real_name VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS mobile VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS email VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS status TINYINT NOT NULL DEFAULT 1,
  ADD COLUMN IF NOT EXISTS org_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS last_login_time DATETIME NULL,
  ADD COLUMN IF NOT EXISTS update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_user_org_id ON sys_user(org_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status);

CREATE TABLE IF NOT EXISTS sys_org (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NULL,
  leader_user_id BIGINT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  sort INT NOT NULL DEFAULT 0,
  ancestors VARCHAR(512) NOT NULL DEFAULT '0',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_org_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_org_parent_id ON sys_org(parent_id);
CREATE INDEX IF NOT EXISTS idx_org_status ON sys_org(status);

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  data_scope_type VARCHAR(32) NOT NULL DEFAULT 'ORG_ONLY',
  is_system TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL,
  path VARCHAR(128) NULL,
  component VARCHAR(255) NULL,
  perm_code VARCHAR(128) NULL,
  sort INT NOT NULL DEFAULT 0,
  visible TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_perm_code ON sys_menu(perm_code);

CREATE TABLE IF NOT EXISTS sys_api_resource (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  path VARCHAR(255) NOT NULL,
  method VARCHAR(16) NOT NULL,
  perm_code VARCHAR(128) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_api_perm_code (perm_code),
  UNIQUE KEY uk_api_method_path (method, path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, menu_id),
  CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_api (
  role_id BIGINT NOT NULL,
  api_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, api_id),
  CONSTRAINT fk_role_api_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  CONSTRAINT fk_role_api_api FOREIGN KEY (api_id) REFERENCES sys_api_resource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_data_scope_org (
  role_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, org_id),
  CONSTRAINT fk_scope_org_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  CONSTRAINT fk_scope_org_org FOREIGN KEY (org_id) REFERENCES sys_org(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(64) NULL,
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(512) NULL,
  status TINYINT NOT NULL DEFAULT 1,
  message VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_operate_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(64) NULL,
  module VARCHAR(64) NULL,
  action VARCHAR(64) NULL,
  target_id VARCHAR(64) NULL,
  ip VARCHAR(64) NULL,
  request_path VARCHAR(255) NULL,
  request_method VARCHAR(16) NULL,
  request_body TEXT NULL,
  result VARCHAR(32) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_org (id, parent_id, name, code, status, sort, ancestors)
VALUES (1, 0, '平台组织', 'ROOT', 1, 0, '0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO sys_role (id, name, code, description, status, data_scope_type, is_system)
VALUES (1, '超级管理员', 'SUPER_ADMIN', 'System super administrator', 1, 'ALL', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), data_scope_type = VALUES(data_scope_type);

INSERT INTO sys_api_resource (name, path, method, perm_code, status) VALUES
('Current admin profile', '/api/admin/auth/me', 'GET', 'sys:auth:me', 1),
('List organizations', '/api/admin/orgs/tree', 'GET', 'sys:org:list', 1),
('Create organization', '/api/admin/orgs', 'POST', 'sys:org:create', 1),
('Update organization', '/api/admin/orgs/{id}', 'PUT', 'sys:org:update', 1),
('Delete organization', '/api/admin/orgs/{id}', 'DELETE', 'sys:org:delete', 1),
('Update organization status', '/api/admin/orgs/{id}/status', 'PATCH', 'sys:org:status', 1),
('List users', '/api/admin/users', 'GET', 'sys:user:list', 1),
('Create user', '/api/admin/users', 'POST', 'sys:user:create', 1),
('Update user', '/api/admin/users/{id}', 'PUT', 'sys:user:update', 1),
('Update user status', '/api/admin/users/{id}/status', 'PATCH', 'sys:user:status', 1),
('Reset user password', '/api/admin/users/{id}/reset-password', 'POST', 'sys:user:reset-password', 1),
('Assign user roles', '/api/admin/users/{id}/roles', 'PUT', 'sys:user:assign-role', 1),
('List roles', '/api/admin/roles', 'GET', 'sys:role:list', 1),
('Create role', '/api/admin/roles', 'POST', 'sys:role:create', 1),
('Update role', '/api/admin/roles/{id}', 'PUT', 'sys:role:update', 1),
('Delete role', '/api/admin/roles/{id}', 'DELETE', 'sys:role:delete', 1),
('Update role status', '/api/admin/roles/{id}/status', 'PATCH', 'sys:role:status', 1),
('Update role permissions', '/api/admin/roles/{id}/permissions', 'PUT', 'sys:role:assign-permission', 1),
('List menu resources', '/api/admin/permissions/menus', 'GET', 'sys:permission:menu:list', 1),
('Create menu resource', '/api/admin/permissions/menus', 'POST', 'sys:permission:menu:create', 1),
('Update menu resource', '/api/admin/permissions/menus/{id}', 'PUT', 'sys:permission:menu:update', 1),
('List api resources', '/api/admin/permissions/apis', 'GET', 'sys:permission:api:list', 1),
('Create api resource', '/api/admin/permissions/apis', 'POST', 'sys:permission:api:create', 1),
('Update api resource', '/api/admin/permissions/apis/{id}', 'PUT', 'sys:permission:api:update', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status);

INSERT INTO sys_menu (id, parent_id, name, path, component, perm_code, sort, visible, status) VALUES
(1, 0, '系统管理', '/admin', 'Layout', NULL, 1, 1, 1),
(2, 1, '用户管理', '/admin/users', 'AdminUsersView', 'sys:user:list', 1, 1, 1),
(3, 1, '角色管理', '/admin/roles', 'AdminRolesView', 'sys:role:list', 2, 1, 1),
(4, 1, '组织管理', '/admin/orgs', 'AdminOrgsView', 'sys:org:list', 3, 1, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), path = VALUES(path), perm_code = VALUES(perm_code), status = VALUES(status);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

INSERT IGNORE INTO sys_role_api (role_id, api_id)
SELECT 1, id FROM sys_api_resource;

INSERT INTO sys_user_role (user_id, role_id)
SELECT id, 1 FROM sys_user WHERE id = 1
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
