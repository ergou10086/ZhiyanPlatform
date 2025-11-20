-- ========================================
-- 修复 project_members 表的 project_role 枚举值
-- 添加 ADMIN 角色支持
-- ========================================
-- 说明：
-- 1. 将 ENUM('OWNER','MEMBER') 改为 ENUM('OWNER','ADMIN','MEMBER')
-- 2. 支持项目管理员角色功能
-- 
-- 执行前请备份数据！
-- ========================================

USE `zhiyan-mysql`;

-- ========================================
-- 第一步：备份现有数据（可选，建议执行）
-- ========================================

-- 备份 project_members 表
CREATE TABLE IF NOT EXISTS `project_members_backup_before_add_admin` AS 
SELECT * FROM `project_members`;

-- ========================================
-- 第二步：修改列定义，添加 ADMIN 枚举值
-- ========================================

-- 修改 project_role 列定义，添加 ADMIN 支持
ALTER TABLE `project_members`
MODIFY COLUMN `project_role` ENUM('OWNER','ADMIN','MEMBER') NOT NULL
COMMENT '项目内角色（拥有者/管理员/普通成员）';
-- ========================================
-- 第三步：验证修改
-- ========================================

-- 查看表结构
DESCRIBE `project_members`;

-- 查看当前角色分布
SELECT 
    `project_role` AS '角色',
    COUNT(*) AS '成员数量'
FROM `project_members`
GROUP BY `project_role`;

SELECT '✅ project_role 枚举值修复完成！现在支持: OWNER, ADMIN, MEMBER' AS result;

ALTER TABLE `project_members`
    MODIFY COLUMN `project_role` ENUM('OWNER','ADMIN','MEMBER') NOT NULL
        COMMENT '项目内角色（拥有者/管理员/普通成员）';