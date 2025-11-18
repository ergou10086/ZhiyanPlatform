-- 修复 project_members 表的 project_role 枚举值
-- 将 ENUM('LEADER', 'MEMBER') 改为 ENUM('OWNER', 'MEMBER')
-- 
-- 执行步骤：
-- 1. 先修改已有数据（如果有 LEADER 角色的记录，改为 OWNER）
-- 2. 修改列的枚举定义

USE zhiyan_projectteam_db;

-- 步骤1: 更新已有数据（如果有 LEADER，改为 OWNER）
UPDATE project_members 
SET project_role = 'MEMBER' 
WHERE project_role = 'LEADER';

-- 步骤2: 修改列定义
ALTER TABLE project_members 
MODIFY COLUMN project_role ENUM('OWNER', 'MEMBER') NOT NULL 
COMMENT '项目内角色（创建者/普通成员）';

-- 验证修改
DESCRIBE project_members;

SELECT '✅ project_role 枚举值修复完成！现在支持: OWNER, MEMBER' AS result;

