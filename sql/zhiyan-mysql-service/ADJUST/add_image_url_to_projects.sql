-- ================================================================
-- 项目表添加 image_url 字段（非空）
-- 创建时间: 2025-10-23
-- 描述: 为 projects 表添加 image_url 字段，用于存储项目图片URL（必填字段）
-- ================================================================

USE zhiyan_projectteam_db;

-- 添加 image_url 字段（非空，默认值为空字符串）
ALTER TABLE projects 
ADD COLUMN image_url VARCHAR(500) NOT NULL DEFAULT '' COMMENT '项目图片URL' AFTER end_date;

-- 验证字段是否添加成功
DESC projects;

-- 查看表结构
SHOW CREATE TABLE projects;

