-- =====================================================
-- 数据库表清理脚本
-- 用途：清理由于JPA自动建表配置错误导致的混乱表
-- 说明：删除在错误数据库中创建的操作日志表和其他模块的表
-- 作者：ErgouTree
-- 日期：2025-11-20
-- =====================================================

-- 警告：执行此脚本前请务必备份数据库！
-- 建议：先在测试环境执行，确认无误后再在生产环境执行

-- =====================================================
-- 1. 清理 zhiyan_knowledge_db 中的混入表
-- =====================================================

USE zhiyan_knowledge_db;

-- 删除操作日志表（这些表应该在 zhiyan_activelog_db 中）
DROP TABLE IF EXISTS `wiki_operation_log`;
DROP TABLE IF EXISTS `project_operation_log`;
DROP TABLE IF EXISTS `task_operation_log`;
DROP TABLE IF EXISTS `achievement_operation_log`;
DROP TABLE IF EXISTS `login_log`;

-- 删除消息模块的表（这些表应该在 zhiyan_message_db 中）
DROP TABLE IF EXISTS `message_body`;
DROP TABLE IF EXISTS `message_recipient`;
DROP TABLE IF EXISTS `message_send_record`;

-- 删除项目模块的表（这些表应该在 zhiyan_projectteam_db 中）
DROP TABLE IF EXISTS `projects`;
DROP TABLE IF EXISTS `project_members`;
DROP TABLE IF EXISTS `project_members_backup`;
DROP TABLE IF EXISTS `project_join_requests`;
DROP TABLE IF EXISTS `tasks`;
DROP TABLE IF EXISTS `task_user`;
DROP TABLE IF EXISTS `task_submission`;

SELECT '✅ zhiyan_knowledge_db 清理完成' AS status;

-- =====================================================
-- 2. 清理 zhiyan_projectteam_db 中的混入表
-- =====================================================

USE zhiyan_projectteam_db;

-- 删除操作日志表
DROP TABLE IF EXISTS `wiki_operation_log`;
DROP TABLE IF EXISTS `project_operation_log`;
DROP TABLE IF EXISTS `task_operation_log`;
DROP TABLE IF EXISTS `achievement_operation_log`;
DROP TABLE IF EXISTS `login_log`;

-- 删除消息模块的表
DROP TABLE IF EXISTS `message_body`;
DROP TABLE IF EXISTS `message_recipient`;
DROP TABLE IF EXISTS `message_send_record`;

-- 删除知识库模块的表（这些表应该在 zhiyan_knowledge_db 中）
DROP TABLE IF EXISTS `achievement`;
DROP TABLE IF EXISTS `achievement_detail`;
DROP TABLE IF EXISTS `achievement_file`;
DROP TABLE IF EXISTS `achievement_file_upload_record`;
DROP TABLE IF EXISTS `achievement_review`;
DROP TABLE IF EXISTS `achievement_task_ref`;

-- 删除Wiki模块的表（这些表应该在 zhiyan_knowledge_db 中）
DROP TABLE IF EXISTS `wiki_page`;
DROP TABLE IF EXISTS `wiki_attachment`;

SELECT '✅ zhiyan_projectteam_db 清理完成' AS status;

-- =====================================================
-- 3. 清理 zhiyan_userauth_db 中的混入表
-- =====================================================

USE zhiyan_userauth_db;

-- 删除操作日志表（login_log 应该保留在这里，但其他操作日志表应该删除）
DROP TABLE IF EXISTS `wiki_operation_log`;
DROP TABLE IF EXISTS `project_operation_log`;
DROP TABLE IF EXISTS `task_operation_log`;
DROP TABLE IF EXISTS `achievement_operation_log`;
-- 注意：login_log 可以保留在 auth 数据库，也可以移到 activelog 数据库
-- 如果要统一管理所有日志，取消下面的注释
-- DROP TABLE IF EXISTS `login_log`;

-- 删除消息模块的表
DROP TABLE IF EXISTS `message_body`;
DROP TABLE IF EXISTS `message_recipient`;
DROP TABLE IF EXISTS `message_send_record`;

-- 删除项目模块的表
DROP TABLE IF EXISTS `projects`;
DROP TABLE IF EXISTS `project_members`;
DROP TABLE IF EXISTS `project_members_backup`;
DROP TABLE IF EXISTS `project_join_requests`;
DROP TABLE IF EXISTS `tasks`;
DROP TABLE IF EXISTS `task_user`;
DROP TABLE IF EXISTS `task_submission`;

-- 删除知识库模块的表
DROP TABLE IF EXISTS `achievement`;
DROP TABLE IF EXISTS `achievement_detail`;
DROP TABLE IF EXISTS `achievement_file`;
DROP TABLE IF EXISTS `achievement_file_upload_record`;
DROP TABLE IF EXISTS `achievement_review`;
DROP TABLE IF EXISTS `achievement_task_ref`;

SELECT '✅ zhiyan_userauth_db 清理完成' AS status;

-- =====================================================
-- 4. 清理 zhiyan_message_db 中的混入表（如果存在）
-- =====================================================

USE zhiyan_message_db;

-- 删除操作日志表
DROP TABLE IF EXISTS `wiki_operation_log`;
DROP TABLE IF EXISTS `project_operation_log`;
DROP TABLE IF EXISTS `task_operation_log`;
DROP TABLE IF EXISTS `achievement_operation_log`;
DROP TABLE IF EXISTS `login_log`;

-- 删除项目模块的表
DROP TABLE IF EXISTS `projects`;
DROP TABLE IF EXISTS `project_members`;
DROP TABLE IF EXISTS `tasks`;

-- 删除知识库模块的表
DROP TABLE IF EXISTS `achievement`;
DROP TABLE IF EXISTS `wiki_page`;

SELECT '✅ zhiyan_message_db 清理完成' AS status;

-- =====================================================
-- 5. 验证清理结果
-- =====================================================

-- 查看各数据库的表列表，确认清理成功
SELECT '========== zhiyan_knowledge_db 剩余表 ==========' AS info;
USE zhiyan_knowledge_db;
SHOW TABLES;

SELECT '========== zhiyan_projectteam_db 剩余表 ==========' AS info;
USE zhiyan_projectteam_db;
SHOW TABLES;

SELECT '========== zhiyan_userauth_db 剩余表 ==========' AS info;
USE zhiyan_userauth_db;
SHOW TABLES;

SELECT '========== zhiyan_activelog_db 剩余表 ==========' AS info;
USE zhiyan_activelog_db;
SHOW TABLES;

SELECT '========== zhiyan_message_db 剩余表 ==========' AS info;
USE zhiyan_message_db;
SHOW TABLES;

SELECT '
=====================================================
✅ 数据库清理完成！
=====================================================

下一步操作：
1. 重启所有微服务
2. JPA 的 update 策略会在正确的数据库中重新创建表
3. 检查各数据库的表结构是否正确

注意事项：
- 操作日志表会在 zhiyan_activelog_db 中创建
- 各业务模块的表会在各自的数据库中创建
- 不会再出现表混乱的情况

=====================================================
' AS summary;

