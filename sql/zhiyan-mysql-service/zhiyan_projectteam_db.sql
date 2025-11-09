-- 项目表
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目唯一标识',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    status ENUM('PLANNING', 'ONGOING', 'COMPLETED', 'ARCHIVED') DEFAULT 'PLANNING' COMMENT '项目状态（规划中/进行中/已完成/已归档）',
    visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE' COMMENT '项目可见性（公开/私有）',
    start_date DATE COMMENT '项目开始日期',
    end_date DATE COMMENT '项目结束日期',
    created_by BIGINT NOT NULL COMMENT '创建人ID（逻辑关联users表）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '项目基本信息表';


-- 项目成员表
CREATE TABLE project_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员记录唯一标识',
    project_id BIGINT NOT NULL COMMENT '项目ID（逻辑关联projects表）',
    user_id BIGINT NOT NULL COMMENT '用户ID（逻辑关联users表）',
    project_role ENUM('OWNER', 'MEMBER') NOT NULL COMMENT '项目内角色（创建者/普通成员）',
    permissions_override JSON COMMENT '权限覆盖（JSON格式，用于临时修改成员在项目内的权限）',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入项目时间',
#     FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE COMMENT '项目删除时级联删除成员记录',
#     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '用户删除时级联删除成员记录',
#     FOREIGN KEY (invited_by) REFERENCES users(id) COMMENT '关联邀请人信息',
    UNIQUE KEY (project_id, user_id) COMMENT '确保用户不能重复加入同一项目',
    -- 索引：优化项目成员查询
    INDEX idx_project (project_id),
    INDEX idx_user (user_id)
) COMMENT '项目成员关系表（记录用户在项目中的角色和权限）';


-- 项目加入申请表
CREATE TABLE project_join_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请记录唯一标识',
    project_id BIGINT NOT NULL COMMENT '项目ID（逻辑关联projects表）',
    user_id BIGINT NOT NULL COMMENT '申请人ID（逻辑关联users表）',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申请状态（待处理/已批准/已拒绝）',
    message TEXT COMMENT '申请说明（申请人填写的加入理由）',
    responded_at TIMESTAMP NULL COMMENT '处理时间',
    responded_by BIGINT NULL COMMENT '处理人ID（逻辑关联users表），处理人可以是项目负责人或管理员',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
#     FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE COMMENT '项目删除时级联删除申请记录',
#     FOREIGN KEY (user_id) REFERENCES users(id) COMMENT '关联申请人信息',
#     FOREIGN KEY (responded_by) REFERENCES users(id) COMMENT '关联处理人信息',
    UNIQUE KEY (project_id, user_id, status) COMMENT '同一用户对同一项目的同一状态申请唯一',
    -- 索引：优化申请查询
    INDEX idx_project_status (project_id, status),
    INDEX idx_user (user_id)
) COMMENT '用户加入项目的申请表';


-- 任务表
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务唯一标识',
    project_id BIGINT NOT NULL COMMENT '所属项目ID（本服务内关联projects表）',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    status ENUM('TODO', 'IN_PROGRESS', 'BLOCKED', 'PENDING_REVIEW', 'DONE') DEFAULT 'TODO' COMMENT '任务状态（待办/进行中/阻塞/待审核/已完成）',
    priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM' COMMENT '任务优先级（高/中/低）',
    -- 移除与用户服务users表的外键约束，仅保留逻辑关联的用户ID
    assignee_id JSON NOT NULL COMMENT '负责人ID（逻辑关联用户服务的用户ID，可为空表示未分配）JSON类型存储多个负责人ID',
    due_date DATE COMMENT '任务截止日期',
    -- 和创建人ID仅作为逻辑关联，移除外键约束
    created_by BIGINT NOT NULL COMMENT '创建人ID（逻辑关联用户服务的用户ID）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    worktime DECIMAL(10, 2) COMMENT '预估工时（单位：小时，支持小数，例如2.5表示2.5小时）',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除（软删除标记，FALSE为未删除，TRUE为已删除）',
    -- 保留服务内部的外键约束（项目服务内的表关联）
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,  -- '项目删除时级联删除任务（服务内部约束）'
    -- 新增索引
    INDEX idx_assignee_ids ((CAST(assignee_id AS UNSIGNED ARRAY))) COMMENT '为负责人ID数组创建多值索引',
    INDEX idx_created_by (created_by) COMMENT '优化按创建人查询'
) COMMENT '项目任务表（与用户服务松耦合，通过ID逻辑关联用户）';
