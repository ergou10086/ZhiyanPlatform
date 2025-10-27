-- 检查和修复 achievement 表结构
-- 执行前请先备份数据！

-- 1. 检查当前表结构
DESCRIBE achievement;

-- 2. 检查 type 字段的数据类型和现有数据
SELECT type, COUNT(*) as count FROM achievement GROUP BY type;

-- 3. 修复 type 字段类型（如果当前是 TINYINT）
-- 注意：这个操作会删除所有现有数据，请先备份！
-- DROP TABLE IF EXISTS achievement_backup;
-- CREATE TABLE achievement_backup AS SELECT * FROM achievement;

-- 修改 type 字段为 ENUM 类型
ALTER TABLE achievement 
MODIFY COLUMN type 
ENUM('paper', 'patent', 'dataset', 'model', 'report', 'custom') 
NOT NULL 
COMMENT '成果类型';

-- 4. 验证修改结果
DESCRIBE achievement;
