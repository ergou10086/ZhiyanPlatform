-- =============================================
-- 添加任务状态 PENDING_REVIEW（待审核）
-- 时间：2025-11-09
-- 说明：支持任务提交后的待审核状态
-- =============================================

-- 修改 tasks 表的 status 字段，添加 PENDING_REVIEW 状态
ALTER TABLE `tasks` 
MODIFY COLUMN `status` ENUM('TODO','IN_PROGRESS','BLOCKED','PENDING_REVIEW','DONE') 
DEFAULT 'TODO' 
COMMENT '任务状态（待办/进行中/阻塞/待审核/已完成）';

