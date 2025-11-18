package hbnu.project.zhiyanmessgae.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


/**
 * 消息发送记录实体（可选）
 * 用于记录群组消息和广播消息的发送情况
 * 如果需要追踪"消息发送给了哪些人"的统计信息，可以使用此表
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "message_send_record", indexes = {
        @Index(name = "idx_message_body", columnList = "message_body_id"),
        @Index(name = "idx_send_time", columnList = "send_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRecord extends BaseAuditEntity {

    /**
     * 记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @LongToString
    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '发送记录ID'")
    private Long id;

    /**
     * 消息体ID
     */
    @LongToString
    @Column(name = "message_body_id", nullable = false,
            columnDefinition = "BIGINT COMMENT '消息体ID'")
    private Long messageBodyId;

    /**
     * 发送时间
     */
    @Column(name = "send_time", nullable = false,
            columnDefinition = "DATETIME COMMENT '发送时间'")
    private LocalDateTime sendTime;

    /**
     * 目标收件人总数
     */
    @Column(name = "total_recipients", nullable = false,
            columnDefinition = "INT COMMENT '目标收件人总数'")
    private Integer totalRecipients;

    /**
     * 成功发送数量
     */
    @Column(name = "success_count", nullable = false,
            columnDefinition = "INT DEFAULT 0 COMMENT '成功发送数量'")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Column(name = "failed_count", nullable = false,
            columnDefinition = "INT DEFAULT 0 COMMENT '失败数量'")
    private Integer failedCount;

    /**
     * 发送状态
     */
    @Column(name = "status", length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'SENDING' COMMENT '发送状态：SENDING-发送中, SUCCESS-成功, PARTIAL_FAILED-部分失败, FAILED-失败'")
    private String status;

    /**
     * 关联消息体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_body_id", insertable = false, updatable = false)
    private MessageBody messageBody;
}
