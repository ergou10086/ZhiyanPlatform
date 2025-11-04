-- =============================================
-- 任务与用户关联表设计
-- 用途：记录任务的执行者、分配历史、接取记录
-- 作者：System
-- 创建时间：2025-11-04
-- =============================================

-- 任务用户关联表
CREATE TABLE task_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录ID',
    
    -- ========== 核心关联字段 ==========
    task_id BIGINT NOT NULL COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '项目ID（冗余字段，提高查询性能）',
    user_id BIGINT NOT NULL COMMENT '用户ID（执行者ID）',
    
    -- ========== 分配信息字段 ==========
    assign_type ENUM('ASSIGNED', 'CLAIMED') NOT NULL DEFAULT 'ASSIGNED' 
        COMMENT '分配类型：ASSIGNED-被管理员分配，CLAIMED-用户主动接取',
    
    assigned_by BIGINT NOT NULL COMMENT '分配人ID（如果是CLAIMED则为user_id本身）',
    
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配/接取时间',
    
    -- ========== 状态字段 ==========
    is_active BOOLEAN NOT NULL DEFAULT TRUE 
        COMMENT '是否有效（TRUE-有效执行者，FALSE-已移除）',
    
    removed_at TIMESTAMP NULL COMMENT '移除时间（仅当is_active=FALSE时有值）',
    
    removed_by BIGINT NULL COMMENT '移除操作人ID（仅当is_active=FALSE时有值）',
    
    -- ========== 扩展字段 ==========
    role_type ENUM('EXECUTOR', 'FOLLOWER', 'REVIEWER') DEFAULT 'EXECUTOR' 
        COMMENT '角色类型：EXECUTOR-执行者，FOLLOWER-关注者，REVIEWER-审核者（预留）',
    
    notes VARCHAR(500) NULL COMMENT '备注信息（可记录分配原因等）',
    
    -- ========== 审计字段 ==========
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    -- ========== 外键约束 ==========
    -- 任务删除时级联删除关联记录
    CONSTRAINT fk_task_user_task FOREIGN KEY (task_id) 
        REFERENCES tasks(id) 
        ON DELETE CASCADE,
    
    -- ========== 索引设计 ==========
    
    -- 1. 查询任务的所有执行者（包括历史）
    INDEX idx_task_id (task_id),
    
    -- 2. 查询用户的所有任务（高频查询）
    INDEX idx_user_id (user_id),
    
    -- 3. 查询用户在特定项目中的任务（高频查询）
    INDEX idx_user_project (user_id, project_id),
    
    -- 4. 查询项目的所有任务分配情况
    INDEX idx_project_id (project_id),
    
    -- 5. 查询有效的任务分配（过滤已移除的记录）
    INDEX idx_active (is_active),
    
    -- 6. 查询任务的当前执行者（最高频查询）
    INDEX idx_task_user_active (task_id, user_id, is_active),
    
    -- 7. 分配时间索引（用于按时间排序）
    INDEX idx_assigned_at (assigned_at),
    
    -- 8. 复合索引：查询用户的有效任务
    INDEX idx_user_active (user_id, is_active, assigned_at),
    
    -- ========== 唯一约束 ==========
    -- 防止同一用户以同一角色重复分配到同一任务
    -- 注意：如果需要支持"移除后重新分配"，应在应用层处理（更新is_active而非插入新记录）
    UNIQUE KEY uk_task_user_role (task_id, user_id, role_type)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='任务用户关联表：记录任务分配、接取历史及当前执行者';

-- =============================================
-- 索引使用说明
-- =============================================
-- 
-- 1. 查询用户的所有任务（我的任务）
--    SELECT * FROM task_user WHERE user_id = ? AND is_active = TRUE
--    使用索引：idx_user_active
--
-- 2. 查询用户在项目X中的任务
--    SELECT * FROM task_user WHERE user_id = ? AND project_id = ? AND is_active = TRUE
--    使用索引：idx_user_project
--
-- 3. 查询任务的所有执行者
--    SELECT * FROM task_user WHERE task_id = ? AND is_active = TRUE
--    使用索引：idx_task_user_active
--
-- 4. 查询任务的分配历史（包括已移除的）
--    SELECT * FROM task_user WHERE task_id = ? ORDER BY assigned_at DESC
--    使用索引：idx_task_id
--
-- 5. 统计用户的任务数量
--    SELECT COUNT(*) FROM task_user WHERE user_id = ? AND is_active = TRUE
--    使用索引：idx_user_active
--
-- 6. 统计项目的任务分配情况
--    SELECT user_id, COUNT(*) FROM task_user 
--    WHERE project_id = ? AND is_active = TRUE GROUP BY user_id
--    使用索引：idx_project_id
--
-- =============================================
-- 重要说明：唯一约束的影响
-- =============================================
--
-- 由于使用了 UNIQUE KEY (task_id, user_id, role_type)，
-- 同一用户不能多次分配到同一任务（即使被移除后）。
--
-- 推荐的应用层处理逻辑：
--
-- 1. 分配任务时：
--    - 先查询是否已存在记录（包括is_active=FALSE的）
--    - 如果存在且is_active=FALSE，则UPDATE设为TRUE
--    - 如果不存在，则INSERT新记录
--
-- 2. 示例代码：
--    SELECT * FROM task_user 
--    WHERE task_id = ? AND user_id = ? AND role_type = ?
--    LIMIT 1;
--    
--    IF exists AND is_active = FALSE THEN
--        UPDATE task_user 
--        SET is_active = TRUE, 
--            assigned_by = ?,
--            assigned_at = NOW()
--        WHERE id = ?;
--    ELSE IF NOT exists THEN
--        INSERT INTO task_user (...) VALUES (...);
--    END IF;
--
-- =============================================


