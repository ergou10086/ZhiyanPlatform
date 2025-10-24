-- 为 achievement 表的 status 字段添加 'obsolete' 枚举值
-- 此脚本用于修复数据库枚举定义与实体类定义不一致的问题

ALTER TABLE achievement 
MODIFY COLUMN status 
ENUM('draft', 'under_review', 'published', 'obsolete') 
NOT NULL DEFAULT 'draft' 
COMMENT '状态';

