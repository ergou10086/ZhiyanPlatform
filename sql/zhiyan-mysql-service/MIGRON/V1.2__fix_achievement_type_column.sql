-- 修复 achievement 表的 type 字段类型
-- 将 TINYINT 类型改为 ENUM 类型，以支持字符串值

-- 首先备份现有数据（如果需要的话）
-- CREATE TABLE achievement_backup AS SELECT * FROM achievement;

-- 修改 type 字段类型为 ENUM
ALTER TABLE achievement 
MODIFY COLUMN type 
ENUM('paper', 'patent', 'dataset', 'model', 'report', 'custom') 
NOT NULL 
COMMENT '成果类型';

-- 如果数据中有无效的 type 值，需要先清理
-- UPDATE achievement SET type = 'custom' WHERE type NOT IN ('paper', 'patent', 'dataset', 'model', 'report', 'custom');
