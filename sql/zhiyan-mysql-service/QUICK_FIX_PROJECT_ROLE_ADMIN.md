# 快速修复：添加 ADMIN 角色支持

## 问题描述

在尝试将项目成员设置为管理员时，出现以下错误：
```
Data truncated for column 'project_role' at row 1
```

## 原因

数据库表 `project_members` 的 `project_role` 列当前只支持 `ENUM('OWNER','MEMBER')`，缺少 `ADMIN` 枚举值。

## 解决方案

执行以下 SQL 脚本修复数据库表结构：

```sql
USE `zhiyan-mysql`;

-- 备份现有数据（可选，建议执行）
CREATE TABLE IF NOT EXISTS `project_members_backup_before_add_admin` AS 
SELECT * FROM `project_members`;

-- 修改 project_role 列定义，添加 ADMIN 支持


-- 验证修改
DESCRIBE `project_members`;
```

## 执行步骤

1. 连接到 MySQL 数据库
2. 选择数据库：`USE zhiyan-mysql;`
3. 执行上述 SQL 语句
4. 验证：运行 `DESCRIBE project_members;` 确认 `project_role` 列包含 `OWNER`, `ADMIN`, `MEMBER`

## 验证

执行以下查询验证修复是否成功：

```sql
-- 查看表结构
DESCRIBE `project_members`;

-- 查看当前角色分布
SELECT 
    `project_role` AS '角色',
    COUNT(*) AS '成员数量'
FROM `project_members`
GROUP BY `project_role`;
```

## 完整脚本

完整脚本文件位置：`ZhiyanPlatform/sql/zhiyan-mysql-service/fix_project_role_add_admin.sql`

