-- =============================================
-- 创建成果-任务关联表（应用层关联方案）
-- 版本：V1.4
-- 时间：2025-01-XX
-- 说明：用于实现成果与任务的跨数据库关联
-- 数据库：zhiyan_knowledge_db（知识库数据库）
-- =============================================

-- ==================== 数据库分布说明 ====================
-- 成果表（achievement）：zhiyan_knowledge_db 数据库
-- 任务表（tasks）：zhiyan_projectteam_db 数据库
-- 关联表（achievement_task_ref）：zhiyan_knowledge_db 数据库（本表）
-- 
-- 为什么关联表放在知识库数据库？
-- 1. 服务边界：关联关系由知识库服务管理，属于成果领域
-- 2. 数据一致性：与成果表在同一库，便于维护关联关系
-- 3. 查询效率：查询成果时通常需要关联任务，放在同一库减少跨库查询
-- 4. 服务解耦：任务服务不需要知道成果的存在
-- 
-- 跨数据库关联方案：
-- 1. 在知识库数据库中创建关联表，只存储成果ID和任务ID的映射关系
-- 2. 任务详情通过调用项目服务API获取（应用层关联）
-- 3. 无法使用数据库外键约束，需要在应用层保证数据一致性

-- ==================== 表结构设计 ====================
-- 作用：在应用层实现成果与任务的关联关系
-- 原因：成果表和任务表在不同数据库中，无法使用数据库外键关联
-- 方案：在知识库数据库中存储关联关系，任务详情通过调用项目服务API获取

-- 切换到知识库数据库
USE zhiyan_knowledge_db;

-- 成果-任务关联表
CREATE TABLE IF NOT EXISTS `achievement_task_ref` (
    `id` BIGINT NOT NULL COMMENT '关联ID（雪花ID）',
    `achievement_id` BIGINT NOT NULL COMMENT '成果ID（关联achievement表）',
    `task_id` BIGINT NOT NULL COMMENT '任务ID（跨数据库关联，关联项目服务中的tasks表）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '关联备注（可选，用于记录关联原因或说明）',
    
    -- 审计字段（继承BaseAuditEntity）
    `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT DEFAULT 1 COMMENT '乐观锁版本号',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_achievement_task` (`achievement_id`, `task_id`) COMMENT '确保同一成果不会重复关联同一任务',
    INDEX `idx_achievement_id` (`achievement_id`) COMMENT '优化按成果查询关联任务',
    INDEX `idx_task_id` (`task_id`) COMMENT '优化按任务查询关联成果',
    INDEX `idx_created_at` (`created_at`) COMMENT '优化按时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='成果-任务关联表（应用层关联方案，跨数据库关联）';

-- ==================== 数据一致性说明 ====================
-- 1. achievement_id 需要在应用层验证成果存在性
-- 2. task_id 需要通过调用项目服务API验证任务存在性
-- 3. 删除成果时，关联关系通过应用层逻辑级联删除（或使用触发器）
-- 4. 任务删除时，需要在项目服务中通知知识库服务清理关联关系（或定期同步）

-- ==================== 使用场景 ====================
-- 1. 查询成果关联的所有任务：SELECT task_id FROM achievement_task_ref WHERE achievement_id = ?
-- 2. 查询任务关联的所有成果：SELECT achievement_id FROM achievement_task_ref WHERE task_id = ?
-- 3. 批量查询：支持批量查询多个成果的关联任务列表

