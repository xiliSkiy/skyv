-- 创建加密密钥表
CREATE TABLE tb_encryption_keys (
    id                   BIGSERIAL PRIMARY KEY,
    key_name             VARCHAR(100) NOT NULL,
    key_version          INTEGER NOT NULL,
    algorithm            VARCHAR(50) NOT NULL DEFAULT 'AES-256-GCM',
    encrypted_key_data   TEXT NOT NULL,
    key_hash             VARCHAR(64) NOT NULL,
    status               INTEGER NOT NULL DEFAULT 1, -- 1:活跃 0:已停用 2:已轮换
    is_active            BOOLEAN NOT NULL DEFAULT FALSE,
    effective_time       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_time          TIMESTAMP,
    remark               VARCHAR(500),
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT
);

-- 创建索引
CREATE INDEX idx_encryption_keys_active ON tb_encryption_keys(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_encryption_keys_status ON tb_encryption_keys(status);
CREATE INDEX idx_encryption_keys_key_name ON tb_encryption_keys(key_name);
CREATE UNIQUE INDEX uk_encryption_keys_name_version ON tb_encryption_keys(key_name, key_version);

-- 创建设备凭据表
CREATE TABLE tb_device_credentials (
    id                   BIGSERIAL PRIMARY KEY,
    device_id            BIGINT NOT NULL,
    credential_name      VARCHAR(100) NOT NULL,
    credential_type      VARCHAR(50) NOT NULL, -- USERNAME_PASSWORD, API_KEY, CERTIFICATE, TOKEN
    protocol_type        VARCHAR(50), -- SNMP, HTTP, SSH, TELNET, FTP
    encrypted_data       TEXT NOT NULL,
    encryption_iv        VARCHAR(32) NOT NULL,
    encryption_key_id    BIGINT NOT NULL,
    is_default           BOOLEAN NOT NULL DEFAULT FALSE,
    status               INTEGER NOT NULL DEFAULT 1, -- 1:启用 0:禁用
    remark               VARCHAR(500),
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT
);

-- 创建索引
CREATE INDEX idx_device_credentials_device_id ON tb_device_credentials(device_id);
CREATE INDEX idx_device_credentials_protocol ON tb_device_credentials(protocol_type);
CREATE INDEX idx_device_credentials_default ON tb_device_credentials(device_id, is_default) WHERE is_default = TRUE;
CREATE INDEX idx_device_credentials_status ON tb_device_credentials(status);
CREATE INDEX idx_device_credentials_key_id ON tb_device_credentials(encryption_key_id);
CREATE UNIQUE INDEX uk_device_credentials_name ON tb_device_credentials(device_id, credential_name);

-- 添加外键约束
ALTER TABLE tb_device_credentials ADD CONSTRAINT fk_device_credentials_device_id 
    FOREIGN KEY (device_id) REFERENCES tb_devices(id) ON DELETE CASCADE;

ALTER TABLE tb_device_credentials ADD CONSTRAINT fk_device_credentials_key_id 
    FOREIGN KEY (encryption_key_id) REFERENCES tb_encryption_keys(id) ON DELETE RESTRICT;

-- 添加检查约束
ALTER TABLE tb_encryption_keys ADD CONSTRAINT chk_encryption_keys_status 
    CHECK (status IN (0, 1, 2));

ALTER TABLE tb_device_credentials ADD CONSTRAINT chk_device_credentials_status 
    CHECK (status IN (0, 1));

-- 添加注释
COMMENT ON TABLE tb_encryption_keys IS '加密密钥表';
COMMENT ON COLUMN tb_encryption_keys.key_name IS '密钥名称';
COMMENT ON COLUMN tb_encryption_keys.key_version IS '密钥版本号';
COMMENT ON COLUMN tb_encryption_keys.algorithm IS '加密算法';
COMMENT ON COLUMN tb_encryption_keys.encrypted_key_data IS '加密后的密钥数据';
COMMENT ON COLUMN tb_encryption_keys.key_hash IS '密钥哈希值';
COMMENT ON COLUMN tb_encryption_keys.status IS '密钥状态：1=活跃，0=已停用，2=已轮换';
COMMENT ON COLUMN tb_encryption_keys.is_active IS '是否为当前活跃密钥';
COMMENT ON COLUMN tb_encryption_keys.effective_time IS '密钥生效时间';
COMMENT ON COLUMN tb_encryption_keys.expire_time IS '密钥过期时间';

COMMENT ON TABLE tb_device_credentials IS '设备凭据表';
COMMENT ON COLUMN tb_device_credentials.device_id IS '设备ID';
COMMENT ON COLUMN tb_device_credentials.credential_name IS '凭据名称';
COMMENT ON COLUMN tb_device_credentials.credential_type IS '凭据类型';
COMMENT ON COLUMN tb_device_credentials.protocol_type IS '协议类型';
COMMENT ON COLUMN tb_device_credentials.encrypted_data IS '加密后的凭据数据';
COMMENT ON COLUMN tb_device_credentials.encryption_iv IS '加密向量';
COMMENT ON COLUMN tb_device_credentials.encryption_key_id IS '加密密钥ID';
COMMENT ON COLUMN tb_device_credentials.is_default IS '是否为默认凭据';
COMMENT ON COLUMN tb_device_credentials.status IS '凭据状态：1=启用，0=禁用';

-- 插入初始加密密钥（用于演示，生产环境应该使用安全的密钥管理）
INSERT INTO tb_encryption_keys (key_name, key_version, algorithm, encrypted_key_data, key_hash, status, is_active, effective_time)
VALUES ('default-key', 1, 'AES-256-GCM', 'dummy-encrypted-data', 'dummy-hash', 1, TRUE, CURRENT_TIMESTAMP);
