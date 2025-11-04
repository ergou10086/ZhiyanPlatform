-- =============================================
-- tasks表优化方案
-- 背景：建立task_user关联表后的表结构调整
-- 时间：2025-11-04
-- =============================================

-- ==================== 现状分析 ====================
/*
当前tasks表的问题：
1. ✅ 基础字段完善（id, project_id, title, description, status, priority等）
2. ✅ 审计字段完整（created_by, updated_by, created_at, updated_at, version）
3. ✅ 软删除支持（is_deleted）
4. ⚠️ assignee_id使用JSON存储，性能差，需要迁移到task_user表
5. ⚠️ 部分索引可以优化
6. ⚠️ created_by字段允许NULL，建议改为NOT NULL
*/

-- ==================== 优化方案 ====================

-- 方案A：渐进式迁移（推荐）⭐
-- 第一阶段：保留assignee_id字段，标记为废弃
-- 第二阶段：运行一段时间后，确认无问题再删除

-- ========== 第一步：添加废弃标记（不影响现有功能）==========

ALTER TABLE `tasks` 
MODIFY COLUMN `assignee_id` json NULL 
COMMENT '【已废弃】负责人ID，已迁移到task_user表，仅作备份保留';

-- ========== 第二步：优化字段定义 ==========

-- 2.1 created_by改为NOT NULL（避免数据不一致）
ALTER TABLE `tasks` 
MODIFY COLUMN `created_by` bigint NOT NULL 
COMMENT '创建人ID（逻辑关联用户服务的用户ID）';

-- 2.2 确保worktime不为负数
ALTER TABLE `tasks` 
ADD CONSTRAINT `chk_worktime_positive` 
CHECK (`worktime` IS NULL OR `worktime` >= 0);

-- ========== 第三步：索引优化 ==========

-- 3.1 添加软删除标记索引（提高查询性能）
ALTER TABLE `tasks` 
ADD INDEX `idx_is_deleted` (`is_deleted`) 
COMMENT '优化软删除过滤';

-- 3.2 添加组合索引：project_id + is_deleted + status（覆盖最常见查询）
ALTER TABLE `tasks` 
ADD INDEX `idx_project_active_status` (`project_id`, `is_deleted`, `status`) 
COMMENT '优化项目任务看板查询（过滤已删除任务）';

-- 3.3 添加组合索引：created_by + is_deleted（查询用户创建的任务）
ALTER TABLE `tasks` 
ADD INDEX `idx_creator_active` (`created_by`, `is_deleted`) 
COMMENT '优化查询用户创建的任务';

-- 3.4 添加截止日期 + 状态组合索引（查询逾期任务）
ALTER TABLE `tasks` 
ADD INDEX `idx_due_status` (`due_date`, `status`, `is_deleted`) 
COMMENT '优化逾期任务查询';


