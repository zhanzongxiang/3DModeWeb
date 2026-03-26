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
