-- 消息表，用于持久化站内信
-- 消息表
CREATE TABLE inbox_message (
    id BIGINT PRIMARY KEY COMMENT '消息ID',
    sender_id BIGINT COMMENT '发送人ID（null表示系统消息）',
    receiver_id BIGINT COMMENT '接收人ID（null表示广播消息）',
    message_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL' COMMENT '消息类型：PERSONAL-个人消息, GROUP-群组消息, BROADCAST-广播消息',
    scene VARCHAR(50) NOT NULL COMMENT '消息场景',
    priority VARCHAR(20) NOT NULL COMMENT '优先级',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容正文',
    business_id BIGINT COMMENT '业务关联ID',
    business_type VARCHAR(50) COMMENT '业务类型',
    read_flag BIT NOT NULL DEFAULT 0 COMMENT '是否已读',
    extend_data JSON COMMENT '扩展字段',

    -- 审计字段
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '最后修改人ID',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',

    -- 索引
    INDEX idx_receiver_status (receiver_id, read_flag),
    INDEX idx_sender_receiver (sender_id, receiver_id),
    INDEX idx_business (business_type, business_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息收件箱表';