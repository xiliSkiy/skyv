-- ========================================
-- SkyEye 智能监控系统 - 用户权限管理表
-- 版本: V1.0.1
-- 创建时间: 2024-12-19
-- 描述: 用户、角色、权限相关表结构
-- ========================================

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE tb_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status INTEGER DEFAULT 1,
    is_admin BOOLEAN DEFAULT FALSE,
    last_login_ip VARCHAR(50),
    last_login_time TIMESTAMP,
    login_fail_count INTEGER DEFAULT 0,
    locked_time TIMESTAMP,
    login_count INTEGER DEFAULT 0,
    password_changed_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    remark TEXT
);

-- 创建索引
CREATE INDEX idx_users_username ON tb_users (username);
CREATE INDEX idx_users_email ON tb_users (email);
CREATE INDEX idx_users_phone ON tb_users (phone);
CREATE INDEX idx_users_status ON tb_users (status);
CREATE INDEX idx_users_is_admin ON tb_users (is_admin);
CREATE INDEX idx_users_created_at ON tb_users (created_at DESC);

-- 创建更新时间触发器
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON tb_users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_users IS '用户信息表';
COMMENT ON COLUMN tb_users.id IS '用户ID';
COMMENT ON COLUMN tb_users.username IS '用户名';
COMMENT ON COLUMN tb_users.password IS '密码哈希';
COMMENT ON COLUMN tb_users.real_name IS '真实姓名';
COMMENT ON COLUMN tb_users.email IS '邮箱';
COMMENT ON COLUMN tb_users.phone IS '手机号';
COMMENT ON COLUMN tb_users.avatar IS '头像URL';
COMMENT ON COLUMN tb_users.status IS '状态：1启用 0禁用';
COMMENT ON COLUMN tb_users.is_admin IS '是否管理员';
COMMENT ON COLUMN tb_users.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN tb_users.last_login_time IS '最后登录时间';
COMMENT ON COLUMN tb_users.login_fail_count IS '登录失败次数';
COMMENT ON COLUMN tb_users.locked_time IS '锁定时间';
COMMENT ON COLUMN tb_users.login_count IS '登录次数';

-- ========================================
-- 2. 角色表
-- ========================================
CREATE TABLE tb_roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(200),
    status INTEGER DEFAULT 1,
    is_system BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- 创建索引
CREATE INDEX idx_roles_code ON tb_roles (code);
CREATE INDEX idx_roles_status ON tb_roles (status);
CREATE INDEX idx_roles_is_system ON tb_roles (is_system);
CREATE INDEX idx_roles_sort_order ON tb_roles (sort_order);

-- 创建更新时间触发器
CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON tb_roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_roles IS '角色信息表';
COMMENT ON COLUMN tb_roles.id IS '角色ID';
COMMENT ON COLUMN tb_roles.name IS '角色名称';
COMMENT ON COLUMN tb_roles.code IS '角色编码';
COMMENT ON COLUMN tb_roles.description IS '角色描述';
COMMENT ON COLUMN tb_roles.status IS '状态：1启用 0禁用';
COMMENT ON COLUMN tb_roles.is_system IS '是否系统角色';
COMMENT ON COLUMN tb_roles.sort_order IS '排序';

-- ========================================
-- 3. 权限表
-- ========================================
CREATE TABLE tb_permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    type VARCHAR(20) DEFAULT 'BUTTON',
    description VARCHAR(200),
    status INTEGER DEFAULT 1,
    is_system BOOLEAN DEFAULT FALSE,
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (parent_id) REFERENCES tb_permissions(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_permissions_code ON tb_permissions (code);
CREATE INDEX idx_permissions_resource ON tb_permissions (resource);
CREATE INDEX idx_permissions_action ON tb_permissions (action);
CREATE INDEX idx_permissions_type ON tb_permissions (type);
CREATE INDEX idx_permissions_status ON tb_permissions (status);
CREATE INDEX idx_permissions_parent ON tb_permissions (parent_id);
CREATE INDEX idx_permissions_sort_order ON tb_permissions (sort_order);

-- 创建更新时间触发器
CREATE TRIGGER update_permissions_updated_at
    BEFORE UPDATE ON tb_permissions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_permissions IS '权限信息表';
COMMENT ON COLUMN tb_permissions.id IS '权限ID';
COMMENT ON COLUMN tb_permissions.name IS '权限名称';
COMMENT ON COLUMN tb_permissions.code IS '权限编码';
COMMENT ON COLUMN tb_permissions.resource IS '资源标识';
COMMENT ON COLUMN tb_permissions.action IS '操作类型';
COMMENT ON COLUMN tb_permissions.type IS '权限类型：MENU菜单 BUTTON按钮';
COMMENT ON COLUMN tb_permissions.description IS '权限描述';
COMMENT ON COLUMN tb_permissions.status IS '状态：1启用 0禁用';
COMMENT ON COLUMN tb_permissions.is_system IS '是否系统权限';
COMMENT ON COLUMN tb_permissions.parent_id IS '父权限ID';
COMMENT ON COLUMN tb_permissions.sort_order IS '排序';

-- ========================================
-- 4. 用户角色关联表
-- ========================================
CREATE TABLE tb_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_user_roles_user ON tb_user_roles (user_id);
CREATE INDEX idx_user_roles_role ON tb_user_roles (role_id);
CREATE INDEX idx_user_roles_assigned_at ON tb_user_roles (assigned_at DESC);

-- 添加注释
COMMENT ON TABLE tb_user_roles IS '用户角色关联表';
COMMENT ON COLUMN tb_user_roles.user_id IS '用户ID';
COMMENT ON COLUMN tb_user_roles.role_id IS '角色ID';
COMMENT ON COLUMN tb_user_roles.assigned_at IS '分配时间';
COMMENT ON COLUMN tb_user_roles.assigned_by IS '分配人';

-- ========================================
-- 5. 角色权限关联表
-- ========================================
CREATE TABLE tb_role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES tb_permissions(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_role_permissions_role ON tb_role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission ON tb_role_permissions (permission_id);
CREATE INDEX idx_role_permissions_assigned_at ON tb_role_permissions (assigned_at DESC);

-- 添加注释
COMMENT ON TABLE tb_role_permissions IS '角色权限关联表';
COMMENT ON COLUMN tb_role_permissions.role_id IS '角色ID';
COMMENT ON COLUMN tb_role_permissions.permission_id IS '权限ID';
COMMENT ON COLUMN tb_role_permissions.assigned_at IS '分配时间';
COMMENT ON COLUMN tb_role_permissions.assigned_by IS '分配人'; 