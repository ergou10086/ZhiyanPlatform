-- =============================================
-- 修复任务状态 ENUM - 添加 PENDING_REVIEW（待审核）
-- 问题：数据库 tasks 表的 status 字段缺少 PENDING_REVIEW 状态
-- 错误：Data truncated for column 'status' at row 1
-- =============================================

-- 修改 tasks 表的 status 字段，添加 PENDING_REVIEW 状态
ALTER TABLE `tasks` 
MODIFY COLUMN `status` ENUM('TODO','IN_PROGRESS','BLOCKED','PENDING_REVIEW','DONE') 
DEFAULT 'TODO' 
COMMENT '任务状态（待办/进行中/阻塞/待审核/已完成）';

-- 验证修改是否成功
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tasks' AND COLUMN_NAME = 'status';

