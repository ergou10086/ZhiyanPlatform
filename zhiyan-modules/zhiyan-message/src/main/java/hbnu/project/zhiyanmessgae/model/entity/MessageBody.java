package hbnu.project.zhiyanmessgae.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanmessgae.model.enums.MessagePriority;
import hbnu.project.zhiyanmessgae.model.enums.MessageScene;
import hbnu.project.zhiyanmessgae.model.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 消息体实体 - 存储消息的核心内容
 * 一条消息体可以对应多个收件人
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "message_body", indexes = {
        @Index(name = "idx_scene_time", columnList = "scene, trigger_time"),
        @Index(name = "idx_biz_type", columnList = "business_type, business_id"),
        @Index(name = "idx_sender", columnList = "sender_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBody {

    /**
     * 消息体ID
     */
    @Id
    @LongToString
    @Column(nullable = false, columnDefinition = "BIGINT COMMENT '消息体ID'")
    private Long id;

    /**
     * 发送人ID
     * 为null表示系统消息
     */
    @LongToString
    @Column(name = "sender_id", columnDefinition = "BIGINT COMMENT '发送人ID（null表示系统消息）'")
    private Long senderId;

    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'PERSONAL' COMMENT '消息类型：PERSONAL-个人消息, GROUP-群组消息, BROADCAST-广播消息'")
    private MessageType messageType;

    /**
     * 消息场景
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scene", nullable = false, length = 50,
            columnDefinition = "VARCHAR(50) COMMENT '消息场景'")
    private MessageScene scene;

    /**
     * 优先级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) COMMENT '优先级'")
    private MessagePriority priority;

    /**
     * 消息标题
     */
    @Column(name = "title", nullable = false, length = 200,
            columnDefinition = "VARCHAR(200) COMMENT '标题'")
    private String title;

    /**
     * 消息正文
     */
    @Column(name = "content", columnDefinition = "TEXT COMMENT '内容正文'")
    private String content;

    /**
     * 业务关联ID（如任务ID、项目ID、成果ID等）
     */
    @LongToString
    @Column(name = "business_id", columnDefinition = "BIGINT COMMENT '业务关联ID'")
    private Long businessId;

    /**
     * 业务类型
     */
    @Column(name = "business_type", length = 50,
            columnDefinition = "VARCHAR(50) COMMENT '业务类型'")
    private String businessType;

    /**
     * 消息触发时间（业务发生时间）
     */
    @Column(name = "trigger_time", nullable = false,
            columnDefinition = "DATETIME COMMENT '消息触发时间'")
    private LocalDateTime triggerTime;

    /**
     * 扩展字段（JSON格式）
     * 可存储额外的业务数据，如跳转链接、操作按钮等
     */
    @Column(name = "extend_data", columnDefinition = "JSON COMMENT '扩展字段'")
    private String extendData;

    /**
     * 收件人列表（一对多关系）
     */
    @OneToMany(mappedBy = "messageBody", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MessageRecipient> recipients = new ArrayList<>();

    @PrePersist
    public void initIdAndTime() {
        if (this.id == null) {
            this.id = SnowflakeIdUtil.nextId();
        }
        if (this.triggerTime == null) {
            this.triggerTime = LocalDateTime.now();
        }
        // 在保存前，更新所有收件人的 messageBodyId
        if (this.id != null && this.recipients != null) {
            for (MessageRecipient recipient : this.recipients) {
                if (recipient.getMessageBodyId() == null) {
                    recipient.setMessageBodyId(this.id);
                }
            }
        }
    }

    @PostPersist
    public void updateRecipientsMessageBodyId() {
        // 保存后，更新所有收件人的 messageBodyId
        if (this.id != null && this.recipients != null) {
            for (MessageRecipient recipient : this.recipients) {
                if (recipient.getMessageBodyId() == null) {
                    recipient.setMessageBodyId(this.id);
                }
            }
        }
    }

    /**
     * 便捷方法：添加收件人
     */
    public void addRecipient(MessageRecipient recipient) {
        recipients.add(recipient);
        recipient.setMessageBody(this);
        // 如果消息体已经有ID，设置messageBodyId
        if (this.id != null) {
            recipient.setMessageBodyId(this.id);
        }
    }

    /**
     * 便捷方法：批量添加收件人
     */
    public void addRecipients(List<MessageRecipient> recipientList) {
        recipientList.forEach(this::addRecipient);
    }
}