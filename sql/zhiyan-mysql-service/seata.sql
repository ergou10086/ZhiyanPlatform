-- 在 MySQL 中创建 seata 数据库
-- CREATE DATABASE seata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seata;

-- 全局事务表
CREATE TABLE IF NOT EXISTS `global_table` (
                                              `xid` VARCHAR(128) NOT NULL,
    `transaction_id` BIGINT,
    `status` TINYINT NOT NULL,
    `application_id` VARCHAR(128),
    `transaction_service_group` VARCHAR(128),
    `transaction_name` VARCHAR(128),
    `timeout` INT,
    `begin_time` BIGINT,
    `application_data` VARCHAR(2000),
    `gmt_create` DATETIME,
    `gmt_modified` DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_gmt_modified_status` (`gmt_modified`, `status`),
    KEY `idx_transaction_id` (`transaction_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分支事务表
CREATE TABLE IF NOT EXISTS `branch_table` (
                                              `branch_id` BIGINT NOT NULL,
                                              `xid` VARCHAR(128) NOT NULL,
    `transaction_id` BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id` VARCHAR(256),
    `branch_type` VARCHAR(8),
    `status` TINYINT,
    `client_id` VARCHAR(64),
    `application_data` VARCHAR(2000),
    `gmt_create` DATETIME(6),
    `gmt_modified` DATETIME(6),
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 全局锁表
CREATE TABLE IF NOT EXISTS `lock_table` (
                                            `row_key` VARCHAR(128) NOT NULL,
    `xid` VARCHAR(128),
    `transaction_id` BIGINT,
    `branch_id` BIGINT NOT NULL,
    `resource_id` VARCHAR(256),
    `table_name` VARCHAR(32),
    `pk` VARCHAR(36),
    `status` TINYINT NOT NULL DEFAULT '0',
    `gmt_create` DATETIME,
    `gmt_modified` DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_branch_id` (`branch_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- undo_log 未操作日志表，需要在使用分布式事务的模块进行使用
-- 在 zhiyan_userauth_db、zhiyan_projectteam_db、zhiyan_konwledge_db 中分别执行
-- Seata AT 模式回滚日志表
CREATE TABLE IF NOT EXISTS `undo_log` (
                                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                          `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
                                          `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
                                          `context` VARCHAR(128) NOT NULL COMMENT '上下文',
                                          `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
                                          `log_status` INT NOT NULL COMMENT '状态，0-正常，1-已删除',
                                          `log_created` DATETIME(6) NOT NULL COMMENT '创建时间',
                                          `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式回滚日志表';

-- 创建索引以提升查询性能
CREATE INDEX idx_log_created ON undo_log(log_created);