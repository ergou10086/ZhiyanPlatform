package hbnu.project.zhiyanmessgae.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息列表DTO
 *
 * @author ErgouTree
 */
@Data
public class MessageListDTO {
    /**
     * 收件记录ID
     */
    private Long recipientId;

    /**
     * 消息体ID
     */
    private Long messageId;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 消息场景
     */
    private String scene;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 扩展数据
     */
    private String extendData;

    /**
     * 是否已读
     */
    private Boolean readFlag;

    /**
     * 读取时间
     */
    private LocalDateTime readAt;

    /**
     * 触发时间
     */
    private LocalDateTime triggerTime;
}
