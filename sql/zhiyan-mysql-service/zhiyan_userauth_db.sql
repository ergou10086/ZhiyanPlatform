-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一标识',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT '用户邮箱（登录账号）',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值（加密存储）',
    name VARCHAR(100) NOT NULL COMMENT '用户姓名',
    avatar_url VARCHAR(500) COMMENT '用户头像URL',
    title VARCHAR(100) COMMENT '用户职称/职位',
    institution VARCHAR(200) COMMENT '用户所属机构',
    is_locked BOOLEAN DEFAULT FALSE COMMENT '是否锁定（禁止登录）',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '软删除标记',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '系统用户基本信息表';


-- 角色表
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色唯一标识',
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称（如：ADMIN、USER）',
    description TEXT COMMENT '角色描述（如：系统管理员、普通用户）'
) COMMENT '系统角色定义表（用于权限分组）';


-- 用户角色关联表
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID（关联users表）',
    role_id BIGINT NOT NULL COMMENT '角色ID（关联roles表）',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '角色分配时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,     -- 角色删除时级联删除关联记录
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,     -- 权限删除时级联删除关联记录
    UNIQUE KEY (user_id, role_id) COMMENT '确保用户不能重复关联同一角色'
) COMMENT '用户与角色的多对多关联表';


-- 权限表
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限唯一标识',
    name VARCHAR(100) UNIQUE NOT NULL COMMENT '权限名称（如：project:create、task:edit）',
    description TEXT COMMENT '权限描述（如：创建项目权限、编辑任务权限）'
) COMMENT '系统权限定义表（细粒度操作权限）';


-- 角色权限关联表
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL COMMENT '角色ID（关联roles表，角色删除时级联删除此记录）',
    permission_id BIGINT NOT NULL COMMENT '权限ID（关联permissions表，权限删除时级联删除此记录）',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '权限授予时间',
    PRIMARY KEY (role_id, permission_id) COMMENT '复合主键（角色+权限唯一）',
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) COMMENT '角色与权限的多对多关联表（角色继承权限，关联关系随角色/权限删除而自动删除）';

-- RememberMe Token表
CREATE TABLE remember_me_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Token唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID（关联users表）',
    token VARCHAR(128) UNIQUE NOT NULL COMMENT 'RememberMe Token值',
    expiry_time TIMESTAMP NOT NULL COMMENT 'Token过期时间',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Token创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '用户删除时级联删除Token'
) COMMENT 'RememberMe自动登录Token表';

