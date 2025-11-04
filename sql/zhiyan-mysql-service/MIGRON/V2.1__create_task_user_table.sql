-- =============================================
-- 数据库迁移脚本 V2.1
-- 功能：创建task_user关联表并迁移数据
-- 时间：2025-11-04
-- =============================================

-- ==================== 步骤1：创建task_user表 ====================

CREATE TABLE IF NOT EXISTS task_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录ID',
    
    task_id BIGINT NOT NULL COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '项目ID（冗余字段，提高查询性能）',
    user_id BIGINT NOT NULL COMMENT '用户ID（执行者ID）',
    
    assign_type ENUM('ASSIGNED', 'CLAIMED') NOT NULL DEFAULT 'ASSIGNED' 
        COMMENT '分配类型：ASSIGNED-被管理员分配，CLAIMED-用户主动接取',
    
    assigned_by BIGINT NOT NULL COMMENT '分配人ID',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配/接取时间',
    
    is_active BOOLEAN NOT NULL DEFAULT TRUE 
        COMMENT '是否有效（TRUE-有效执行者，FALSE-已移除）',
    
    removed_at TIMESTAMP NULL COMMENT '移除时间',
    removed_by BIGINT NULL COMMENT '移除操作人ID',
    
    role_type ENUM('EXECUTOR', 'FOLLOWER', 'REVIEWER') DEFAULT 'EXECUTOR' 
        COMMENT '角色类型：EXECUTOR-执行者，FOLLOWER-关注者，REVIEWER-审核者',
    
    notes VARCHAR(500) NULL COMMENT '备注信息',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    
    CONSTRAINT fk_task_user_task FOREIGN KEY (task_id) 
        REFERENCES tasks(id) ON DELETE CASCADE,
    
    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_project (user_id, project_id),
    INDEX idx_project_id (project_id),
    INDEX idx_active (is_active),
    INDEX idx_task_user_active (task_id, user_id, is_active),
    INDEX idx_assigned_at (assigned_at),
    INDEX idx_user_active (user_id, is_active, assigned_at),
    
    UNIQUE KEY uk_task_user_role (task_id, user_id, role_type)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='任务用户关联表：记录任务分配、接取历史及当前执行者';

-- ==================== 步骤2：数据迁移 ====================

-- 使用JSON_TABLE展开assignee_id数组（MySQL 8.0+）
INSERT INTO task_user (
    task_id,
    project_id,
    user_id,
    assign_type,
    assigned_by,
    assigned_at,
    is_active,
    role_type,
    notes,
    created_at,
    updated_at
)
SELECT 
    t.id AS task_id,
    t.project_id AS project_id,
    CAST(jt.user_id AS UNSIGNED) AS user_id,
    'ASSIGNED' AS assign_type,
    t.created_by AS assigned_by,
    t.created_at AS assigned_at,
    TRUE AS is_active,
    'EXECUTOR' AS role_type,
    '历史数据迁移' AS notes,
    NOW() AS created_at,
    NOW() AS updated_at
FROM tasks t
CROSS JOIN JSON_TABLE(
    t.assignee_id,
    '$[*]' COLUMNS (user_id VARCHAR(20) PATH '$')
) AS jt
WHERE 
    t.is_deleted = FALSE
    AND t.assignee_id IS NOT NULL
    AND t.assignee_id != '[]'
    AND t.assignee_id != ''
    AND CAST(jt.user_id AS UNSIGNED) > 0;

-- ==================== 步骤3：数据验证 ====================

-- 验证迁移结果
SELECT 
    '数据迁移统计' AS category,
    COUNT(*) AS total_relations,
    COUNT(DISTINCT task_id) AS tasks_with_assignees,
    COUNT(DISTINCT user_id) AS unique_users,
    COUNT(DISTINCT project_id) AS projects_affected
FROM task_user;

-- 检查数据一致性
SELECT 
    '数据一致性检查' AS category,
    COUNT(*) AS inconsistent_tasks
FROM (
    SELECT 
        t.id,
        JSON_LENGTH(t.assignee_id) AS json_count,
        COUNT(tu.id) AS table_count
    FROM tasks t
    LEFT JOIN task_user tu ON t.id = tu.task_id AND tu.is_active = TRUE
    WHERE t.is_deleted = FALSE 
      AND t.assignee_id IS NOT NULL 
      AND t.assignee_id != '[]'
    GROUP BY t.id, t.assignee_id
    HAVING JSON_LENGTH(t.assignee_id) != COUNT(tu.id)
) AS inconsistent;

-- ==================== 步骤4：标记assignee_id为废弃（保留备份）====================

ALTER TABLE tasks 
MODIFY COLUMN assignee_id json NULL 
COMMENT '【已废弃】负责人ID，已迁移到task_user表，仅作备份保留';

-- ==================== 完成 ====================

SELECT '✅ task_user表创建并数据迁移完成！' AS message;

