-- 消息体表 - 存储消息的核心内容
CREATE TABLE message_body
(
    id            BIGINT PRIMARY KEY COMMENT '消息体ID（雪花ID）',
    sender_id     BIGINT COMMENT '发送人ID（null表示系统消息）',
    message_type  VARCHAR(20)  NOT NULL DEFAULT 'PERSONAL' COMMENT '消息类型：PERSONAL-个人消息, GROUP-群组消息, BROADCAST-广播消息',
    scene         VARCHAR(50)  NOT NULL COMMENT '消息场景',
    priority      VARCHAR(20)  NOT NULL COMMENT '优先级',
    title         VARCHAR(200) NOT NULL COMMENT '标题',
    content       TEXT COMMENT '内容正文',
    business_id   BIGINT COMMENT '业务关联ID',
    business_type VARCHAR(50) COMMENT '业务类型',
    trigger_time  DATETIME     NOT NULL COMMENT '消息触发时间（业务发生时间）',
    extend_data   JSON COMMENT '扩展字段',

    -- 审计字段（继承BaseAuditEntity）
    created_at    DATETIME     NOT NULL COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL COMMENT '更新时间',
    created_by    BIGINT COMMENT '创建人ID',
    updated_by    BIGINT COMMENT '最后修改人ID',
    version       INT          NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',

    -- 索引
    INDEX idx_scene_time (scene, trigger_time),
    INDEX idx_biz_type (business_type, business_id),
    INDEX idx_sender (sender_id),
    INDEX idx_trigger_time (trigger_time),
    INDEX idx_message_type (message_type),
    INDEX idx_priority (priority)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='消息体表';

-- 消息收件人表 - 收件人维度的消息记录
CREATE TABLE message_recipient
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收件记录ID',
    message_body_id BIGINT   NOT NULL COMMENT '消息体ID',
    receiver_id     BIGINT   NOT NULL COMMENT '接收人ID',
    scene_code      VARCHAR(50) COMMENT '场景冗余字段',
    read_flag       BIT      NOT NULL DEFAULT 0 COMMENT '是否已读',
    read_at         DATETIME COMMENT '读取时间',
    trigger_time    DATETIME NOT NULL COMMENT '消息触发时间',
    deleted         BIT      NOT NULL DEFAULT 0 COMMENT '是否已删除（软删除）',
    deleted_at      DATETIME COMMENT '删除时间',

    -- 索引
    INDEX idx_receiver_status (receiver_id, read_flag, deleted),
    INDEX idx_message_body (message_body_id),
    INDEX idx_receiver_scene (receiver_id, scene_code),
    INDEX idx_receiver_unread (receiver_id, read_flag, trigger_time),
    INDEX idx_trigger_time (trigger_time),
    INDEX idx_deleted (deleted),

    -- 外键约束
    FOREIGN KEY (message_body_id) REFERENCES message_body (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='消息收件人表';

-- 消息发送记录表 - 记录群组消息和广播消息的发送情况
CREATE TABLE message_send_record
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发送记录ID',
    message_body_id  BIGINT      NOT NULL COMMENT '消息体ID',
    send_time        DATETIME    NOT NULL COMMENT '发送时间',
    total_recipients INT         NOT NULL COMMENT '目标收件人总数',
    success_count    INT         NOT NULL DEFAULT 0 COMMENT '成功发送数量',
    failed_count     INT         NOT NULL DEFAULT 0 COMMENT '失败数量',
    status           VARCHAR(20) NOT NULL DEFAULT 'SENDING' COMMENT '发送状态：SENDING-发送中, SUCCESS-成功, PARTIAL_FAILED-部分失败, FAILED-失败',

    -- 审计字段（继承BaseAuditEntity）
    created_at       DATETIME    NOT NULL COMMENT '创建时间',
    updated_at       DATETIME    NOT NULL COMMENT '更新时间',
    created_by       BIGINT COMMENT '创建人ID',
    updated_by       BIGINT COMMENT '最后修改人ID',
    version          INT         NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',

    -- 索引
    INDEX idx_message_body (message_body_id),
    INDEX idx_send_time (send_time),
    INDEX idx_status (status),

    -- 外键约束
    FOREIGN KEY (message_body_id) REFERENCES message_body (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='消息发送记录表';

-- 消息收件箱表（兼容原有设计）
CREATE TABLE inbox_message
(
    id            BIGINT PRIMARY KEY COMMENT '消息ID（雪花ID）',
    sender_id     BIGINT COMMENT '发送人ID（null表示系统消息）',
    receiver_id   BIGINT COMMENT '接收人ID（null表示广播消息）',
    message_type  VARCHAR(20)  NOT NULL DEFAULT 'PERSONAL' COMMENT '消息类型：PERSONAL-个人消息, GROUP-群组消息, BROADCAST-广播消息',
    scene         VARCHAR(50)  NOT NULL COMMENT '消息场景',
    priority      VARCHAR(20)  NOT NULL COMMENT '优先级',
    title         VARCHAR(200) NOT NULL COMMENT '标题',
    content       TEXT COMMENT '内容正文',
    business_id   BIGINT COMMENT '业务关联ID',
    business_type VARCHAR(50) COMMENT '业务类型',
    read_flag     BIT          NOT NULL DEFAULT 0 COMMENT '是否已读',
    extend_data   JSON COMMENT '扩展字段',

    -- 审计字段
    created_at    DATETIME     NOT NULL COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL COMMENT '更新时间',
    created_by    BIGINT COMMENT '创建人ID',
    updated_by    BIGINT COMMENT '最后修改人ID',
    version       INT          NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',

    -- 索引
    INDEX idx_receiver_status (receiver_id, read_flag),
    INDEX idx_sender_receiver (sender_id, receiver_id),
    INDEX idx_business (business_type, business_id),
    INDEX idx_scene (scene),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='消息收件箱表';