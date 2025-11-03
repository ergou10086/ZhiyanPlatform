-- Wiki附件表
-- 用于存储Wiki页面的附件元数据（图片和文件）
-- 实际文件存储在MinIO中

DROP TABLE IF EXISTS `wiki_attachment`;

CREATE TABLE `wiki_attachment` (
    `id` BIGINT NOT NULL COMMENT '附件唯一标识（雪花ID）',
    `wiki_page_id` BIGINT NOT NULL COMMENT '所属Wiki页面ID',
    `project_id` BIGINT NOT NULL COMMENT '所属项目ID',
    `attachment_type` VARCHAR(20) NOT NULL COMMENT '附件类型（IMAGE=图片, FILE=文件）',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型/扩展名（jpg/png/pdf/zip等）',
    `bucket_name` VARCHAR(100) NOT NULL COMMENT 'MinIO桶名',
    `object_key` VARCHAR(500) NOT NULL COMMENT 'MinIO对象键（存储路径）',
    `file_url` VARCHAR(1000) NOT NULL COMMENT '完整访问URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '文件描述/备注',
    `upload_by` BIGINT NOT NULL COMMENT '上传者ID',
    `upload_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已删除（软删除标记）',
    `deleted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_wiki_page` (`wiki_page_id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_type` (`attachment_type`),
    INDEX `idx_upload_by` (`upload_by`),
    INDEX `idx_upload_at` (`upload_at`),
    INDEX `idx_object_key` (`object_key`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Wiki附件表';

-- 插入示例数据（可选）
-- INSERT INTO `wiki_attachment` 
-- (`id`, `wiki_page_id`, `project_id`, `attachment_type`, `file_name`, `file_size`, `file_type`, 
--  `bucket_name`, `object_key`, `file_url`, `description`, `upload_by`, `upload_at`, `is_deleted`)
-- VALUES
-- (1, 1, 1, 'IMAGE', 'example.png', 102400, 'png', 
--  'wikiassets', 'project-1/images/1/20231201123456_example.png', 
--  'http://localhost:9000/wikiassets/project-1/images/1/20231201123456_example.png',
--  '示例图片', 1, NOW(), FALSE);


