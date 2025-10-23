
-- 成果表
CREATE TABLE achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    type ENUM('paper', 'patent', 'dataset', 'model', 'report', 'custom') NOT NULL COMMENT '成果类型，如果是custom类型，则自定义成果类型',
    title VARCHAR(50) NOT NULL COMMENT '标题',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    status ENUM('draft', 'under_review', 'published') DEFAULT 'draft' COMMENT '状态',
    custom_template_id BIGINT COMMENT '自定义成果类型模板ID（如果是custom类型）',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    published_at TIMESTAMP COMMENT '发布时间',

    INDEX idx_project_status (project_id, status),
    INDEX idx_creator (creator_id),
    INDEX idx_type (type)
) COMMENT='成果主表';


-- 成果类型模板表
CREATE TABLE achievement_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- 成果类型模板id
    achievement_id BIGINT NOT NULL UNIQUE,    -- 关联的成果id
    temple_name VARCHAR(50) NOT NULL COMMENT '自定义成果类型模板标题',

    -- 使用JSON存储类型特定字段
    -- 用户可以对某个成果设置为某种类型，并且加入一些字段用于描述
    detail_data JSON NOT NULL COMMENT '详细信息JSON',
    /* 示例结构：
    论文: {"authors": [], "journal": "", "abstract": "", "doi": ""}
    专利: {"patent_no": "", "inventors": [], "application_date": ""}
    数据集: {"description": "", "version": "", "format": "", "size": ""}
    模型: {"framework": "", "version": "", "purpose": ""}
    */

    abstract TEXT COMMENT '摘要/描述（冗余存储，便于搜索）',

    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
) COMMENT='成果类型模板详情表';



-- 文件管理表
CREATE TABLE achievement_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    achievement_id BIGINT NOT NULL COMMENT '所属成果ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型（pdf/zip/csv等）',

    -- MinIO存储信息
    bucket_name VARCHAR(100) NOT NULL COMMENT 'MinIO桶名',
    object_key VARCHAR(500) NOT NULL COMMENT 'MinIO对象键',
    minio_url VARCHAR(1000) NOT NULL COMMENT '完整访问URL',

#     -- 版本控制
#     version INT DEFAULT 1 COMMENT '文件版本号',
#     is_latest BOOLEAN DEFAULT TRUE COMMENT '是否最新版本',

    upload_by BIGINT NOT NULL COMMENT '上传者ID',
    upload_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_achievement (achievement_id),
#     INDEX idx_latest (achievement_id, is_latest),
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
) COMMENT='文件管理表';


-- 评审记录表（暂时不实现评审）
CREATE TABLE achievement_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    achievement_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL COMMENT '评审人ID',
    review_status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    comment TEXT COMMENT '评审意见',
    review_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_achievement (achievement_id),
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE
) COMMENT='评审记录表';



CREATE TABLE `wiki_page` (
                             `id` BIGINT NOT NULL COMMENT 'Wiki页面唯一标识（雪花ID）',
                             `project_id` BIGINT NOT NULL COMMENT '所属项目ID',
                             `title` VARCHAR(255) NOT NULL COMMENT '页面标题',
                             `page_type` VARCHAR(20) NOT NULL DEFAULT 'DOCUMENT' COMMENT '页面类型',
                             `mongo_content_id` VARCHAR(24) COMMENT 'MongoDB文档ID',
                             `parent_id` BIGINT COMMENT '父页面ID',
                             `path` VARCHAR(1000) COMMENT '页面路径',
                             `sort_order` INT DEFAULT 0 COMMENT '排序序号',
                             `is_public` BOOLEAN DEFAULT FALSE COMMENT '是否公开',
                             `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
                             `last_editor_id` BIGINT COMMENT '最后编辑者ID',
                             `content_size` INT DEFAULT 0 COMMENT '内容大小',
                             `current_version` INT DEFAULT 1 COMMENT '当前版本号',
                             `content_summary` VARCHAR(200) COMMENT '内容摘要',
                             `is_locked` BOOLEAN DEFAULT FALSE COMMENT '是否被锁定',
                             `locked_by` BIGINT COMMENT '锁定者用户ID',
                             `locked_at` DATETIME COMMENT '锁定时间',
                             `collaborative_mode` BOOLEAN DEFAULT FALSE COMMENT '是否启用协同编辑',

    -- 审计字段（来自 BaseAuditEntity）
                             `created_by` BIGINT COMMENT '创建人ID',
                             `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_by` BIGINT COMMENT '更新人ID',
                             `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `version` INT DEFAULT 1 COMMENT '乐观锁版本号',

                             PRIMARY KEY (`id`),
                             INDEX `idx_project` (`project_id`),
                             INDEX `idx_parent` (`parent_id`),
                             INDEX `idx_project_parent` (`project_id`, `parent_id`),
                             INDEX `idx_project_type` (`project_id`, `page_type`),
                             INDEX `idx_mongo_content` (`mongo_content_id`),
                             INDEX `idx_path` (`path`(255)),
                             INDEX `idx_created_at` (`created_at`),
                             INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Wiki页面表（存储元数据和关系）';