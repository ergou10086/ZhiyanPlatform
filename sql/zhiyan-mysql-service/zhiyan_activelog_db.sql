-- ============================================
-- 智研平台 - 操作日志数据库设计
-- 采用多表分离设计，按操作类型分表存储
-- design ErgouTree
-- ============================================

-- ==================== 用户登录日志表 ====================
-- 该记录只在后台记录，供平台分析和管理员查看使用
CREATE TABLE IF NOT EXISTS `login_log`(
    `id` BIGINT NOT NULL COMMENT '日志ID（雪花ID）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) COMMENT '用户名（冗余字段，便于查询）',
    `login_type` VARCHAR(20) NOT NULL DEFAULT 'PASSWORD' COMMENT '登录类型：PASSWORD-账密登录, OAUTH-第三方登录',
    `login_ip` VARCHAR(50) COMMENT '登录IP地址',
    `login_location` VARCHAR(200) COMMENT '登录地区（如：北京市）',
    `user_agent` VARCHAR(500) COMMENT '用户代理（浏览器信息）',
    `login_status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '登录状态：SUCCESS-成功, FAILED-失败',
    `failure_reason` VARCHAR(500) COMMENT '失败原因（登录失败时记录）',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_login_time` (`login_time`),
    INDEX `idx_login_ip` (`login_ip`),
    INDEX `idx_user_login_time` (`user_id`, `login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';


-- ==================== 项目操作日志表 ====================
-- 该内容只记录项目相关的操作日志，例如加人踢人，项目编辑，项目归档，项目内成员权限调整
CREATE TABLE IF NOT EXISTS `project_operation_log`(
    `id` BIGINT NOT NULL COMMENT '日志ID（雪花ID）',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `project_name` VARCHAR(200) COMMENT '项目名称（冗余字段，便于查询）',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '用户名（冗余字段，便于查询）',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE-创建, UPDATE-更新, DELETE-删除, MEMBER_ADD-添加成员, MEMBER_REMOVE-移除成员, ROLE_CHANGE-角色变更, STATUS_CHANGE-状态变更',
    `operation_module` VARCHAR(50) NOT NULL DEFAULT '项目管理' COMMENT '操作模块',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `request_params` JSON COMMENT '请求参数（JSON格式）',
    `operation_result` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS-成功, FAILED-失败',
    `error_message` TEXT COMMENT '错误信息（操作失败时记录）',
    `ip_address` VARCHAR(50) COMMENT '操作IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operation_time` (`operation_time`),
    INDEX `idx_project_user_time` (`project_id`, `user_id`, `operation_time`),
    INDEX `idx_project_time` (`project_id`, `operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目操作日志表';

-- ==================== 任务操作日志表 ====================
-- 记录任务相关操作日志，任务创建，任务分配，任务状态变更，任务完成，任务接取，任务审核，任务删除等
CREATE TABLE IF NOT EXISTS `task_operation_log` (
    `id` BIGINT NOT NULL COMMENT '日志ID（雪花ID）',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `task_id` BIGINT COMMENT '任务ID（可为空，如批量操作）',
    `task_title` VARCHAR(500) COMMENT '任务标题（冗余字段，便于查询）',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '用户名（冗余字段，便于查询）',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE-创建, UPDATE-更新, DELETE-删除, ASSIGN-分配, SUBMIT-提交, REVIEW-审核, STATUS_CHANGE-状态变更',
    `operation_module` VARCHAR(50) NOT NULL DEFAULT '任务管理' COMMENT '操作模块',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `request_params` JSON COMMENT '请求参数（JSON格式）',
    `operation_result` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS-成功, FAILED-失败',
    `error_message` TEXT COMMENT '错误信息（操作失败时记录）',
    `ip_address` VARCHAR(50) COMMENT '操作IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operation_time` (`operation_time`),
    INDEX `idx_project_task_time` (`project_id`, `task_id`, `operation_time`),
    INDEX `idx_project_user_time` (`project_id`, `user_id`, `operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务操作日志表';

-- ==================== 成果操作日志表 ====================
CREATE TABLE IF NOT EXISTS `achievement_operation_log`(
    `id` BIGINT NOT NULL COMMENT '日志ID（雪花ID）',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `achievement_id` BIGINT COMMENT '成果ID（可为空，如批量操作）',
    `achievement_title` VARCHAR(500) COMMENT '成果标题（冗余字段，便于查询）',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '用户名（冗余字段，便于查询）',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE-创建, UPDATE-更新, DELETE-删除, PUBLISH-发布, REVIEW-评审, FILE_UPLOAD-文件上传, FILE_DELETE-文件删除',
    `operation_module` VARCHAR(50) NOT NULL DEFAULT '成果管理' COMMENT '操作模块',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `request_params` JSON COMMENT '请求参数（JSON格式）',
    `operation_result` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS-成功, FAILED-失败',
    `error_message` TEXT COMMENT '错误信息（操作失败时记录）',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_achievement_id` (`achievement_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operation_time` (`operation_time`),
    INDEX `idx_project_achievement_time` (`project_id`, `achievement_id`, `operation_time`),
    INDEX `idx_project_user_time` (`project_id`, `user_id`, `operation_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成果操作日志表';

--  ==================== Wiki操作日志表 ====================
CREATE TABLE IF NOT EXISTS `wiki_operation_log`(
    `id` BIGINT NOT NULL COMMENT '日志ID（雪花ID）',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `wiki_page_id` BIGINT COMMENT 'Wiki页面ID（可为空，如批量操作）',
    `wiki_page_title` VARCHAR(500) COMMENT 'Wiki页面标题（冗余字段，便于查询）',
    `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
    `username` VARCHAR(100) COMMENT '用户名（冗余字段，便于查询）',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE-创建, UPDATE-更新, DELETE-删除, MOVE-移动, COPY-复制, RESTORE-恢复',
    `operation_module` VARCHAR(50) NOT NULL DEFAULT 'Wiki管理' COMMENT '操作模块',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `request_params` JSON COMMENT '请求参数（JSON格式）',
    `operation_result` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS-成功, FAILED-失败',
    `error_message` TEXT COMMENT '错误信息（操作失败时记录）',
    `ip_address` VARCHAR(50) COMMENT '操作IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_wiki_page_id` (`wiki_page_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operation_time` (`operation_time`),
    INDEX `idx_project_wiki_time` (`project_id`, `wiki_page_id`, `operation_time`),
    INDEX `idx_project_user_time` (`project_id`, `user_id`, `operation_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Wiki操作日志表';