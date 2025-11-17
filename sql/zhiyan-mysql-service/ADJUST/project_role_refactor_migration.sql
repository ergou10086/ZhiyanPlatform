-- ========================================
-- 项目角色权限系统重构 - 数据库迁移脚本
-- ========================================
-- 说明：
-- 1. 删除 project_roles 表（角色定义移到枚举中）
-- 2. 保留 project_members 表（用于存储用户-项目-角色关联）
-- 3. 项目角色在 ProjectMemberRole 枚举中定义，不可动态修改
-- 
-- 执行前请备份数据！
-- ========================================

USE `zhiyan-mysql`;

-- ========================================
-- 第一步：备份现有数据（可选，建议执行）
-- ========================================

-- 备份 project_roles 表（如果需要）
CREATE TABLE IF NOT EXISTS `project_roles_backup_20251023` AS 
SELECT * FROM `project_roles`;

-- 备份 project_members 表（如果需要）
CREATE TABLE IF NOT EXISTS `project_members_backup_20251023` AS 
SELECT * FROM `project_members`;

-- ========================================
-- 第二步：删除 project_roles 表
-- ========================================

-- 删除表（角色定义已移到枚举中，不再需要此表）
DROP TABLE IF EXISTS `project_roles`;

-- ========================================
-- 第三步：确认 project_members 表结构正确
-- ========================================

-- 如果 project_members 表不存在，创建它
CREATE TABLE IF NOT EXISTS `project_members` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员记录唯一标识（雪花ID）',
    `project_id` BIGINT NOT NULL COMMENT '项目ID（逻辑关联projects表）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（逻辑关联users表）',
    `project_role` ENUM('OWNER','MEMBER') NOT NULL COMMENT '项目内角色（拥有者/普通成员）',
    `permissions_override` JSON DEFAULT NULL COMMENT '权限覆盖（JSON格式，用于临时修改成员在项目内的权限）',
    `joined_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入项目时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_user` (`project_id`, `user_id`) COMMENT '确保用户不能重复加入同一项目',
    KEY `idx_project` (`project_id`),
    KEY `idx_user` (`user_id`),
    CONSTRAINT `fk_project_member_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目成员表（记录用户在项目中的角色和权限）';

-- ========================================
-- 第四步：数据清理（如果有必要）
-- ========================================

-- 检查是否有无效的成员记录（项目不存在）
-- SELECT pm.* FROM project_members pm 
-- LEFT JOIN projects p ON pm.project_id = p.id 
-- WHERE p.id IS NULL;

-- 删除无效的成员记录（谨慎执行）
-- DELETE pm FROM project_members pm 
-- LEFT JOIN projects p ON pm.project_id = p.id 
-- WHERE p.id IS NULL;

-- ========================================
-- 第五步：验证数据完整性
-- ========================================

-- 检查 project_members 表结构
DESCRIBE `project_members`;

-- 检查项目成员数量
SELECT 
    pm.project_role AS '角色',
    COUNT(*) AS '成员数量'
FROM `project_members` pm
GROUP BY pm.project_role;

-- 检查是否有项目没有 OWNER
SELECT 
    p.id AS '项目ID',
    p.name AS '项目名称',
    COUNT(pm.id) AS 'OWNER数量'
FROM `projects` p
LEFT JOIN `project_members` pm ON p.id = pm.project_id AND pm.project_role = 'OWNER'
GROUP BY p.id, p.name
HAVING COUNT(pm.id) = 0;

-- ========================================
-- 第六步：为没有 OWNER 的项目添加创建者为 OWNER（如果需要）
-- ========================================

-- 如果项目创建者不在成员表中，添加他们为 OWNER
-- 注意：这需要根据你的具体业务逻辑调整
/*
INSERT INTO `project_members` (`project_id`, `user_id`, `project_role`, `joined_at`)
SELECT 
    p.id,
    p.creator_id,
    'OWNER',
    p.created_at
FROM `projects` p
LEFT JOIN `project_members` pm ON p.id = pm.project_id AND p.creator_id = pm.user_id
WHERE pm.id IS NULL AND p.creator_id IS NOT NULL;
*/

-- ========================================
-- 完成！
-- ========================================

-- 验证最终结果
SELECT 'Migration completed successfully!' AS status;
SELECT COUNT(*) AS total_members FROM `project_members`;
SELECT project_role, COUNT(*) AS count FROM `project_members` GROUP BY project_role;

