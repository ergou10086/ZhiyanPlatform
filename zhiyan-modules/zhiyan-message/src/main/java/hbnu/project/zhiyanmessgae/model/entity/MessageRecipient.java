package hbnu.project.zhiyanmessgae.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 消息收件人实体 - 收件人维度的消息记录
 * 记录每个收件人对消息的阅读状态
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "message_recipient", indexes = {
        @Index(name = "idx_receiver_status", columnList = "receiver_id, read_flag, deleted"),
        @Index(name = "idx_message_body", columnList = "message_body_id"),
        @Index(name = "idx_receiver_scene", columnList = "receiver_id, scene_code"),
        @Index(name = "idx_receiver_unread", columnList = "receiver_id, read_flag, trigger_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecipient {

    /**
     * 收件记录ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @LongToString
    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '收件记录ID'")
    private Long id;

    /**
     * 关联消息体ID
     */
    @LongToString
    @Column(name = "message_body_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '消息体ID'")
    private Long messageBodyId;

    /**
     * 接收人ID
     */
    @LongToString
    @Column(name = "receiver_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '接收人ID'")
    private Long receiverId;

    /**
     * 场景代码（冗余字段，便于查询）
     */
    @Column(name = "scene_code", length = 50, columnDefinition = "VARCHAR(50) COMMENT '场景冗余字段'")
    private String sceneCode;

    /**
     * 是否已读
     */
    @Builder.Default
    @Column(name = "read_flag", nullable = false, columnDefinition = "BIT DEFAULT 0 COMMENT '是否已读'")
    private Boolean readFlag = Boolean.FALSE;

    /**
     * 读取时间
     */
    @Column(name = "read_at", columnDefinition = "DATETIME COMMENT '读取时间'")
    private LocalDateTime readAt;

    /**
     * 消息触发时间（冗余字段，便于排序）
     */
    @Column(name = "trigger_time", nullable = false, columnDefinition = "DATETIME COMMENT '消息触发时间'")
    private LocalDateTime triggerTime;

    /**
     * 是否已删除（软删除）
     */
    @Builder.Default
    @Column(name = "deleted", nullable = false, columnDefinition = "BIT DEFAULT 0 COMMENT '是否已删除'")
    private Boolean deleted = Boolean.FALSE;

    /**
     * 删除时间
     */
    @Column(name = "deleted_at", columnDefinition = "DATETIME COMMENT '删除时间'")
    private LocalDateTime deletedAt;

    /**
     * 关联消息体（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_body_id", insertable = false, updatable = false)
    private MessageBody messageBody;

    @PrePersist
    public void initMessageBodyId() {
        // 如果 messageBodyId 为 null，尝试从关联的 messageBody 获取
        if (this.messageBodyId == null && this.messageBody != null && this.messageBody.getId() != null) {
            this.messageBodyId = this.messageBody.getId();
        }
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.readFlag = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 软删除
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
